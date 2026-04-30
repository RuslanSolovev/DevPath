package com.example.devpath.data.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class YandexStorageClient(
    private val context: Context,
    private val accessKey: String,
    private val secretKey: String,
    private val bucketName: String
) {
    private val endpoint = "storage.yandexcloud.net"
    private val region = "ru-central1"
    private val service = "s3"

    suspend fun uploadImage(uri: Uri, contentResolver: ContentResolver): String =
        withContext(Dispatchers.IO) {
            val tempFile = createTempFileFromUri(uri, contentResolver)
            val key = "chat_images/${UUID.randomUUID()}.jpg"
            val url = "https://$endpoint/$bucketName/$key"

            val fileBytes = tempFile.readBytes()

            val date = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            val dateShort = date.substring(0, 8)

            val payloadHash = hashSHA256(fileBytes)
            val credentialScope = "$dateShort/$region/$service/aws4_request"

            // ✅ Добавляем x-amz-acl в подпись
            val canonicalHeaders = "host:$endpoint\n" +
                    "x-amz-acl:public-read\n" +
                    "x-amz-content-sha256:$payloadHash\n" +
                    "x-amz-date:$date\n"
            val signedHeaders = "host;x-amz-acl;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = "PUT\n/$bucketName/$key\n\n" +
                    canonicalHeaders + "\n" +
                    signedHeaders + "\n" +
                    payloadHash

            println("DEBUG: CanonicalRequest:\n$canonicalRequest")

            val stringToSign = "AWS4-HMAC-SHA256\n$date\n$credentialScope\n${hashSHA256(canonicalRequest.toByteArray())}"
            println("DEBUG: StringToSign:\n$stringToSign")

            val signingKey = getSignatureKey(secretKey, dateShort, region, service)
            val signature = hmacSHA256(stringToSign, signingKey)

            val authorization = "AWS4-HMAC-SHA256 " +
                    "Credential=$accessKey/$credentialScope, " +
                    "SignedHeaders=$signedHeaders, " +
                    "Signature=$signature"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Authorization", authorization)
            connection.setRequestProperty("x-amz-date", date)
            connection.setRequestProperty("x-amz-content-sha256", payloadHash)
            connection.setRequestProperty("x-amz-acl", "public-read")  // ✅ Публичный доступ
            connection.setRequestProperty("Content-Type", "image/jpeg")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            connection.outputStream.use { it.write(fileBytes) }

            val responseCode = connection.responseCode
            tempFile.delete()

            if (responseCode == 200) {
                println("DEBUG: ✅ Аватар загружен (публичный): $url")
                url
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown"
                println("DEBUG: ❌ Ошибка $responseCode: $error")
                throw Exception("Upload failed: $responseCode")
            }
        }

    private fun setPublicAccess(key: String) {
        val aclUrl = "https://$endpoint/$bucketName/$key?acl"
        val date = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        val dateShort = date.substring(0, 8)

        val payloadHash = hashSHA256("".toByteArray())
        val credentialScope = "$dateShort/$region/$service/aws4_request"

        val canonicalHeaders = "host:$endpoint\n" +
                "x-amz-content-sha256:$payloadHash\n" +
                "x-amz-date:$date\n"
        val signedHeaders = "host;x-amz-content-sha256;x-amz-date"

        val canonicalRequest = "PUT\n/$bucketName/$key\nacl=\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                payloadHash

        val stringToSign = "AWS4-HMAC-SHA256\n$date\n$credentialScope\n${hashSHA256(canonicalRequest.toByteArray())}"
        val signingKey = getSignatureKey(secretKey, dateShort, region, service)
        val signature = hmacSHA256(stringToSign, signingKey)

        val authorization = "AWS4-HMAC-SHA256 " +
                "Credential=$accessKey/$credentialScope, " +
                "SignedHeaders=$signedHeaders, " +
                "Signature=$signature"

        val body = """<AccessControlPolicy xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
            <Owner><ID>$accessKey</ID></Owner>
            <AccessControlList>
                <Grant>
                    <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="Group">
                        <URI>http://acs.amazonaws.com/groups/global/AllUsers</URI>
                    </Grantee>
                    <Permission>READ</Permission>
                </Grant>
            </AccessControlList>
        </AccessControlPolicy>""".trimIndent()

        val bodyHash = hashSHA256(body.toByteArray())

        val putCanonicalHeaders = "host:$endpoint\n" +
                "x-amz-content-sha256:$bodyHash\n" +
                "x-amz-date:$date\n"
        val putSignedHeaders = "host;x-amz-content-sha256;x-amz-date"

        val putCanonicalRequest = "PUT\n/$bucketName/$key\nacl=\n" +
                putCanonicalHeaders + "\n" +
                putSignedHeaders + "\n" +
                bodyHash

        val putStringToSign = "AWS4-HMAC-SHA256\n$date\n$credentialScope\n${hashSHA256(putCanonicalRequest.toByteArray())}"
        val putSignature = hmacSHA256(putStringToSign, signingKey)

        val putAuthorization = "AWS4-HMAC-SHA256 " +
                "Credential=$accessKey/$credentialScope, " +
                "SignedHeaders=$putSignedHeaders, " +
                "Signature=$putSignature"

        val connection = URL(aclUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Authorization", putAuthorization)
        connection.setRequestProperty("x-amz-date", date)
        connection.setRequestProperty("x-amz-content-sha256", bodyHash)
        connection.setRequestProperty("Content-Type", "application/xml")
        connection.doOutput = true
        connection.outputStream.use { it.write(body.toByteArray()) }

        val aclResponse = connection.responseCode
        println("DEBUG: ACL response: $aclResponse")
    }

    private fun createTempFileFromUri(uri: Uri, contentResolver: ContentResolver): File {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open stream")
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { inputStream.copyTo(it) }
        inputStream.close()
        return tempFile
    }

    private fun hashSHA256(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(data)
            .joinToString("") { "%02x".format(it) }
    }

    private fun hashSHA256(data: String): String = hashSHA256(data.toByteArray())

    private fun hmacSHA256(data: String, key: ByteArray): String {
        return Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key, "HmacSHA256"))
            doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        fun hmac(k: ByteArray, d: String): ByteArray = Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(k, "HmacSHA256"))
            doFinal(d.toByteArray())
        }
        val kDate = hmac("AWS4$key".toByteArray(), dateStamp)
        val kRegion = hmac(kDate, regionName)
        val kService = hmac(kRegion, serviceName)
        return hmac(kService, "aws4_request")
    }
}