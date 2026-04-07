package com.example.devpath.data.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class YandexStorageClient(
    private val context: Context,
    private val accessKey: String,
    private val secretKey: String,
    private val bucketName: String
) {
    private val endpoint = "https://storage.yandexcloud.net"

    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val client = AmazonS3Client(credentials)
        client.setEndpoint(endpoint)
        client
    }

    private val transferUtility: TransferUtility by lazy {
        TransferUtility.builder()
            .s3Client(s3Client)
            .context(context)
            .build()
    }

    suspend fun uploadImage(uri: Uri, contentResolver: ContentResolver): String =
        withContext(Dispatchers.IO) {
            // Сохраняем URI во временный файл
            val tempFile = createTempFileFromUri(uri, contentResolver)
            val key = "chat_images/${UUID.randomUUID()}.jpg"

            suspendCancellableCoroutine { continuation ->
                val observer = transferUtility.upload(
                    bucketName,
                    key,
                    tempFile,
                    CannedAccessControlList.PublicRead  // Делаем файл публичным
                )

                observer.setTransferListener(object : TransferListener {
                    override fun onStateChanged(id: Int, state: TransferState?) {
                        if (state == TransferState.COMPLETED) {
                            val publicUrl = "https://$bucketName.storage.yandexcloud.net/$key"
                            // Удаляем временный файл
                            tempFile.delete()
                            continuation.resume(publicUrl)
                        }
                    }

                    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                        // Можно добавить прогресс
                    }

                    override fun onError(id: Int, ex: Exception?) {
                        tempFile.delete()
                        continuation.resumeWithException(ex ?: Exception("Upload failed"))
                    }
                })
            }
        }

    private fun createTempFileFromUri(uri: Uri, contentResolver: ContentResolver): File {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        return tempFile
    }
}