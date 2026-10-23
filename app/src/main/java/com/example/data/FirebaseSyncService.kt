package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.AdminUser
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.Payment
import com.example.model.SystemSettings
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseSyncService(private val context: Context) {

    companion object {
        private const val TAG = "AmarBankFirebase"
        const val PROJECT_ID = "gen-lang-client-0985161510"
        const val API_KEY = "AIzaSyCYujCh4rx9wpdk0O_8xzq7MEqkgVQuHv0"
        const val APP_ID = "1:785826970288:web:0376568bb7b3e21b3b9975"
        const val STORAGE_BUCKET = "gen-lang-client-0985161510.firebasestorage.app"
        const val COLLECTION_NAME = "amar_bank_cloud"
        const val DOCUMENT_NAME = "database"
        const val APP_NAME = "AmarBankApp"
    }

    private var firestoreInstance: FirebaseFirestore? = null
    private var snapshotListener: ListenerRegistration? = null
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val existingApp = FirebaseApp.getApps(context).find { it.name == APP_NAME }
            val firebaseApp = existingApp ?: run {
                val options = FirebaseOptions.Builder()
                    .setProjectId(PROJECT_ID)
                    .setApiKey(API_KEY)
                    .setApplicationId(APP_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(context, options, APP_NAME)
            }
            firestoreInstance = FirebaseFirestore.getInstance(firebaseApp)
            Log.d(TAG, "Firebase Firestore initialized successfully for project: $PROJECT_ID")
        } catch (e: Exception) {
            Log.w(TAG, "Native Firebase SDK init note (fallback will be used if needed): ${e.message}")
        }
    }

    data class CloudSyncResult(
        val success: Boolean,
        val message: String,
        val customers: List<Customer>? = null,
        val loans: List<Loan>? = null,
        val payments: List<Payment>? = null,
        val settings: SystemSettings? = null
    )

    fun startRealtimeListener(
        onUpdate: (customers: List<Customer>, loans: List<Loan>, payments: List<Payment>, settings: SystemSettings?) -> Unit,
        onError: (String) -> Unit
    ) {
        val db = firestoreInstance ?: return
        try {
            snapshotListener?.remove()
            snapshotListener = db.collection(COLLECTION_NAME).document(DOCUMENT_NAME)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Snapshot listener error: ${error.message}")
                        onError(error.message ?: "Firebase listener error")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        if (data != null) {
                            try {
                                val jsonString = JSONObject(data).toString()
                                val parsed = parseJsonPayload(jsonString)
                                onUpdate(parsed.first, parsed.second, parsed.third, parsed.fourth)
                                Log.d(TAG, "Realtime update received from Firestore")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing realtime snapshot: ${e.message}")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start realtime listener: ${e.message}")
        }
    }

    fun stopRealtimeListener() {
        snapshotListener?.remove()
        snapshotListener = null
    }

    suspend fun uploadToFirebase(
        customers: List<Customer>,
        loans: List<Loan>,
        payments: List<Payment>,
        settings: SystemSettings,
        admin: AdminUser,
        note: String = "Backup dari Android APK"
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        val payloadJson = buildJsonPayload(customers, loans, payments, settings, admin, note)
        
        // 1. Try native Firestore SDK
        val db = firestoreInstance
        if (db != null) {
            try {
                val map = jsonToMap(payloadJson)
                var sdkSuccess = false
                var sdkError: String? = null
                val task = db.collection(COLLECTION_NAME).document(DOCUMENT_NAME).set(map)
                
                // Wait synchronously on IO thread
                val latch = java.util.concurrent.CountDownLatch(1)
                task.addOnSuccessListener {
                    sdkSuccess = true
                    latch.countDown()
                }.addOnFailureListener {
                    sdkError = it.message
                    latch.countDown()
                }
                latch.await(10, TimeUnit.SECONDS)

                if (sdkSuccess) {
                    return@withContext CloudSyncResult(
                        success = true,
                        message = "Berhasil Upload ke Firebase Cloud! Seluruh data aplikasi telah tersinkronisasi."
                    )
                } else {
                    Log.w(TAG, "Firestore SDK upload failed ($sdkError), trying REST API...")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore SDK exception: ${e.message}, trying REST API...")
            }
        }

        // 2. Direct REST API Fallback
        try {
            val restResult = uploadViaRestApi(payloadJson)
            if (restResult) {
                CloudSyncResult(
                    success = true,
                    message = "Berhasil Upload ke Firebase Cloud via REST API!"
                )
            } else {
                CloudSyncResult(
                    success = false,
                    message = "Gagal upload ke Firebase. Periksa koneksi internet."
                )
            }
        } catch (e: Exception) {
            CloudSyncResult(
                success = false,
                message = "Gagal upload ke Firebase: ${e.message ?: "Koneksi terputus"}"
            )
        }
    }

    suspend fun updateFromFirebase(): CloudSyncResult = withContext(Dispatchers.IO) {
        val db = firestoreInstance
        if (db != null) {
            try {
                var docData: Map<String, Any>? = null
                var docError: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)
                db.collection(COLLECTION_NAME).document(DOCUMENT_NAME).get()
                    .addOnSuccessListener { snap ->
                        if (snap.exists()) {
                            docData = snap.data
                        }
                        latch.countDown()
                    }
                    .addOnFailureListener {
                        docError = it.message
                        latch.countDown()
                    }
                latch.await(10, TimeUnit.SECONDS)

                if (docData != null) {
                    val jsonStr = JSONObject(docData!!).toString()
                    val (custs, lns, pays, stg) = parseJsonPayload(jsonStr)
                    return@withContext CloudSyncResult(
                        success = true,
                        message = "Berhasil Update dari Firebase! (${custs.size} Nasabah, ${lns.size} Akad, ${pays.size} Pembayaran)",
                        customers = custs,
                        loans = lns,
                        payments = pays,
                        settings = stg
                    )
                } else {
                    Log.w(TAG, "Firestore SDK get failed ($docError), trying REST API...")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore SDK get exception: ${e.message}")
            }
        }

        // REST API Fallback
        try {
            val jsonStr = fetchViaRestApi()
            if (jsonStr != null) {
                val (custs, lns, pays, stg) = parseJsonPayload(jsonStr)
                CloudSyncResult(
                    success = true,
                    message = "Berhasil Update dari Firebase via REST API! (${custs.size} Nasabah, ${lns.size} Akad, ${pays.size} Pembayaran)",
                    customers = custs,
                    loans = lns,
                    payments = pays,
                    settings = stg
                )
            } else {
                CloudSyncResult(
                    success = false,
                    message = "Belum ada dokumen cadangan di Firebase Cloud."
                )
            }
        } catch (e: Exception) {
            CloudSyncResult(
                success = false,
                message = "Gagal membaca database dari Firebase: ${e.message ?: "Error"}"
            )
        }
    }

    private fun uploadViaRestApi(payloadJson: JSONObject): Boolean {
        val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/$COLLECTION_NAME/$DOCUMENT_NAME?key=$API_KEY"
        val firestoreDocumentJson = convertJsonToFirestoreFields(payloadJson)
        val body = firestoreDocumentJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .patch(body)
            .build()
        val response = httpClient.newCall(request).execute()
        val isSuccessful = response.isSuccessful
        response.close()
        return isSuccessful
    }

    private fun fetchViaRestApi(): String? {
        val url = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/$COLLECTION_NAME/$DOCUMENT_NAME?key=$API_KEY"
        val request = Request.Builder().url(url).get().build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            return null
        }
        val responseBody = response.body?.string() ?: return null
        response.close()
        return convertFirestoreFieldsToJson(responseBody)
    }

    private fun buildJsonPayload(
        customers: List<Customer>,
        loans: List<Loan>,
        payments: List<Payment>,
        settings: SystemSettings,
        admin: AdminUser,
        note: String
    ): JSONObject {
        val root = JSONObject()
        val nowIso = com.example.utils.Formatters.getCurrentIsoDate()
        root.put("version", "2.0-firebase-android")
        root.put("exportedAt", nowIso)
        root.put("updatedBy", admin.fullName)
        root.put("note", note)

        val custArr = JSONArray()
        for (c in customers) {
            custArr.put(JSONObject().apply {
                put("id", c.id)
                put("fullName", c.fullName)
                put("phone", c.phone)
                put("address", c.address)
                put("notes", c.notes ?: "")
                put("pin", c.pin)
                put("createdAt", c.createdAt)
            })
        }
        root.put("customers", custArr)

        val loanArr = JSONArray()
        for (l in loans) {
            loanArr.put(JSONObject().apply {
                put("id", l.id)
                put("customerId", l.customerId)
                put("customerName", l.customerName)
                put("customerPhone", l.customerPhone)
                put("type", l.type.name)
                put("itemDescription", l.itemDescription ?: "")
                put("loanTitle", l.loanTitle)
                put("principalAmount", l.principalAmount)
                put("dpAmount", l.dpAmount)
                put("netPrincipal", l.netPrincipal)
                put("marginPercentage", l.marginPercentage)
                put("marginAmount", l.marginAmount)
                put("totalBill", l.totalBill)
                put("tenorCount", l.tenorCount)
                put("tenorUnit", l.tenorUnit.name)
                put("installmentPerPeriod", l.installmentPerPeriod)
                put("startDate", l.startDate)
                put("dueDate", l.dueDate)
                put("nextDueDate", l.nextDueDate)
                put("totalPaid", l.totalPaid)
                put("remainingBalance", l.remainingBalance)
                put("status", l.status.name)
                put("notes", l.notes ?: "")
                put("createdAt", l.createdAt)
            })
        }
        root.put("loans", loanArr)

        val payArr = JSONArray()
        for (p in payments) {
            payArr.put(JSONObject().apply {
                put("id", p.id)
                put("invoiceNo", p.invoiceNo)
                put("loanId", p.loanId)
                put("loanTitle", p.loanTitle)
                put("customerId", p.customerId)
                put("customerName", p.customerName)
                put("customerPhone", p.customerPhone)
                put("amount", p.amount)
                put("installmentNumber", p.installmentNumber)
                put("totalInstallments", p.totalInstallments)
                put("remainingAfter", p.remainingAfter)
                put("paymentMethod", p.paymentMethod.name)
                put("bankOrWalletDetail", p.bankOrWalletDetail ?: "")
                put("referenceNumber", p.referenceNumber ?: "")
                put("notes", p.notes ?: "")
                put("date", p.date)
                put("verifiedBy", p.verifiedBy)
            })
        }
        root.put("payments", payArr)

        root.put("systemSettings", JSONObject().apply {
            put("cityName", settings.cityName)
            put("branchName", settings.branchName)
            put("divisionName", settings.divisionName)
            put("branchAddress", settings.branchAddress)
            put("branchPhone", settings.branchPhone)
            put("officialWhatsApp", settings.officialWhatsApp)
            put("responsibleName", settings.responsibleName)
            put("responsibleTitle", settings.responsibleTitle)
        })

        val adminArr = JSONArray().apply {
            put(JSONObject().apply {
                put("id", admin.id)
                put("fullName", admin.fullName)
                put("email", admin.email)
                put("phone", admin.phone)
                put("pin", admin.pin)
                put("createdAt", admin.createdAt)
            })
        }
        root.put("admins", adminArr)

        return root
    }

    fun parseJsonPayload(jsonStr: String): Quadruple<List<Customer>, List<Loan>, List<Payment>, SystemSettings?> {
        val root = JSONObject(jsonStr)
        val customersList = mutableListOf<Customer>()
        val loansList = mutableListOf<Loan>()
        val paymentsList = mutableListOf<Payment>()
        var parsedSettings: SystemSettings? = null

        if (root.has("customers")) {
            val arr = root.getJSONArray("customers")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                customersList.add(
                    Customer(
                        id = obj.optString("id", ""),
                        fullName = obj.optString("fullName", ""),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        pin = obj.optString("pin", "123456"),
                        createdAt = obj.optString("createdAt", "")
                    )
                )
            }
        }

        if (root.has("loans")) {
            val arr = root.getJSONArray("loans")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", "BARANG")
                val tenorStr = obj.optString("tenorUnit", "BULAN")
                val statusStr = obj.optString("status", "LANCAR")
                loansList.add(
                    Loan(
                        id = obj.optString("id", ""),
                        customerId = obj.optString("customerId", ""),
                        customerName = obj.optString("customerName", ""),
                        customerPhone = obj.optString("customerPhone", ""),
                        type = try { com.example.model.LoanType.valueOf(typeStr) } catch (_: Exception) { com.example.model.LoanType.BARANG },
                        itemDescription = obj.optString("itemDescription").takeIf { it.isNotBlank() },
                        loanTitle = obj.optString("loanTitle", ""),
                        principalAmount = obj.optDouble("principalAmount", 0.0),
                        dpAmount = obj.optDouble("dpAmount", 0.0),
                        netPrincipal = obj.optDouble("netPrincipal", 0.0),
                        marginPercentage = obj.optDouble("marginPercentage", 5.0),
                        marginAmount = obj.optDouble("marginAmount", 0.0),
                        totalBill = obj.optDouble("totalBill", 0.0),
                        tenorCount = obj.optInt("tenorCount", 6),
                        tenorUnit = try { com.example.model.TenorUnit.valueOf(tenorStr) } catch (_: Exception) { com.example.model.TenorUnit.BULAN },
                        installmentPerPeriod = obj.optDouble("installmentPerPeriod", 0.0),
                        startDate = obj.optString("startDate", ""),
                        dueDate = obj.optString("dueDate", ""),
                        nextDueDate = obj.optString("nextDueDate", ""),
                        totalPaid = obj.optDouble("totalPaid", 0.0),
                        remainingBalance = obj.optDouble("remainingBalance", 0.0),
                        status = try { com.example.model.LoanStatus.valueOf(statusStr) } catch (_: Exception) { com.example.model.LoanStatus.LANCAR },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        createdAt = obj.optString("createdAt", "")
                    )
                )
            }
        }

        if (root.has("payments")) {
            val arr = root.getJSONArray("payments")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val methodStr = obj.optString("paymentMethod", "TRANSFER_BANK")
                paymentsList.add(
                    Payment(
                        id = obj.optString("id", ""),
                        invoiceNo = obj.optString("invoiceNo", ""),
                        loanId = obj.optString("loanId", ""),
                        loanTitle = obj.optString("loanTitle", ""),
                        customerId = obj.optString("customerId", ""),
                        customerName = obj.optString("customerName", ""),
                        customerPhone = obj.optString("customerPhone", ""),
                        amount = obj.optDouble("amount", 0.0),
                        installmentNumber = obj.optInt("installmentNumber", 1),
                        totalInstallments = obj.optInt("totalInstallments", 1),
                        remainingAfter = obj.optDouble("remainingAfter", 0.0),
                        paymentMethod = try { com.example.model.PaymentMethod.valueOf(methodStr) } catch (_: Exception) { com.example.model.PaymentMethod.TRANSFER_BANK },
                        bankOrWalletDetail = obj.optString("bankOrWalletDetail").takeIf { it.isNotBlank() },
                        referenceNumber = obj.optString("referenceNumber").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        date = obj.optString("date", ""),
                        verifiedBy = obj.optString("verifiedBy", "Bambang Prasetyo (Admin Amar Bank)")
                    )
                )
            }
        }

        if (root.has("systemSettings")) {
            val obj = root.getJSONObject("systemSettings")
            parsedSettings = SystemSettings(
                cityName = obj.optString("cityName", "Surabaya"),
                branchName = obj.optString("branchName", "PT BANK AMAR INDONESIA Tbk"),
                divisionName = obj.optString("divisionName", "Divisi Manajemen Piutang & Pembiayaan Ritel"),
                branchAddress = obj.optString("branchAddress", "Graha Amar, Jl. Basuki Rahmat 122"),
                branchPhone = obj.optString("branchPhone", "(031) 567-8910"),
                officialWhatsApp = obj.optString("officialWhatsApp", "0812-9876-5432"),
                responsibleName = obj.optString("responsibleName", "Bambang Prasetyo, S.E."),
                responsibleTitle = obj.optString("responsibleTitle", "Head of Retail & Credit Admin")
            )
        }

        return Quadruple(customersList, loansList, paymentsList, parsedSettings)
    }

    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            if (value is JSONObject) {
                map[key] = jsonToMap(value)
            } else if (value is JSONArray) {
                map[key] = jsonArrayToList(value)
            } else {
                map[key] = value
            }
        }
        return map
    }

    private fun jsonArrayToList(arr: JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until arr.length()) {
            val item = arr.get(i)
            if (item is JSONObject) {
                list.add(jsonToMap(item))
            } else if (item is JSONArray) {
                list.add(jsonArrayToList(item))
            } else {
                list.add(item)
            }
        }
        return list
    }

    private fun convertJsonToFirestoreFields(json: JSONObject): JSONObject {
        val doc = JSONObject()
        val fields = JSONObject()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            fields.put(key, anyToFirestoreValue(value))
        }
        doc.put("fields", fields)
        return doc
    }

    private fun anyToFirestoreValue(value: Any): JSONObject {
        val obj = JSONObject()
        when (value) {
            is String -> obj.put("stringValue", value)
            is Boolean -> obj.put("booleanValue", value)
            is Int, is Long -> obj.put("integerValue", value.toString())
            is Double, is Float -> obj.put("doubleValue", value)
            is JSONObject -> {
                val mapObj = JSONObject()
                val fields = JSONObject()
                val keys = value.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    fields.put(k, anyToFirestoreValue(value.get(k)))
                }
                mapObj.put("fields", fields)
                obj.put("mapValue", mapObj)
            }
            is JSONArray -> {
                val arrObj = JSONObject()
                val values = JSONArray()
                for (i in 0 until value.length()) {
                    values.put(anyToFirestoreValue(value.get(i)))
                }
                arrObj.put("values", values)
                obj.put("arrayValue", arrObj)
            }
            else -> obj.put("stringValue", value.toString())
        }
        return obj
    }

    private fun convertFirestoreFieldsToJson(firestoreDocResponse: String): String {
        return try {
            val root = JSONObject(firestoreDocResponse)
            val fields = root.optJSONObject("fields") ?: return firestoreDocResponse
            val result = JSONObject()
            val keys = fields.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val fieldVal = fields.getJSONObject(key)
                result.put(key, unwrapFirestoreValue(fieldVal))
            }
            result.toString()
        } catch (e: Exception) {
            firestoreDocResponse
        }
    }

    private fun unwrapFirestoreValue(fieldObj: JSONObject): Any {
        return when {
            fieldObj.has("stringValue") -> fieldObj.getString("stringValue")
            fieldObj.has("booleanValue") -> fieldObj.getBoolean("booleanValue")
            fieldObj.has("integerValue") -> fieldObj.getString("integerValue").toLongOrNull() ?: 0L
            fieldObj.has("doubleValue") -> fieldObj.getDouble("doubleValue")
            fieldObj.has("arrayValue") -> {
                val arrayValue = fieldObj.getJSONObject("arrayValue")
                val values = arrayValue.optJSONArray("values") ?: JSONArray()
                val list = JSONArray()
                for (i in 0 until values.length()) {
                    list.put(unwrapFirestoreValue(values.getJSONObject(i)))
                }
                list
            }
            fieldObj.has("mapValue") -> {
                val mapValue = fieldObj.getJSONObject("mapValue")
                val fields = mapValue.optJSONObject("fields") ?: JSONObject()
                val res = JSONObject()
                val keys = fields.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    res.put(k, unwrapFirestoreValue(fields.getJSONObject(k)))
                }
                res
            }
            else -> ""
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
