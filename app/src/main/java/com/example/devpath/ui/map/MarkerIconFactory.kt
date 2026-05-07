package com.example.devpath.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarkerIconFactory(private val context: Context) {

    private val imageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .allowHardware(false)
        .build()

    private val cache = mutableMapOf<String, ImageProvider>()

    suspend fun getUserIcon(
        userId: String,
        name: String,
        avatarUrl: String?,
        isCurrentUser: Boolean = false
    ): ImageProvider {
        val key = "$userId|$avatarUrl|$isCurrentUser"
        cache[key]?.let { return it }

        val bitmap = createUserBitmap(name, avatarUrl, isCurrentUser)
        val provider = ImageProvider.fromBitmap(bitmap)
        cache[key] = provider
        return provider
    }

    fun getEventIcon(type: String): ImageProvider {
        val key = "event_$type"
        cache[key]?.let { return it }

        val bitmap = createEventBitmap(type)
        val provider = ImageProvider.fromBitmap(bitmap)
        cache[key] = provider
        return provider
    }

    private suspend fun createUserBitmap(
        name: String,
        avatarUrl: String?,
        isCurrentUser: Boolean
    ): Bitmap = withContext(Dispatchers.IO) {
        val avatarSize = 120
        val totalWidth = 220
        val totalHeight = 270
        val markerColor = if (isCurrentUser) AndroidColor.rgb(33, 150, 243) else AndroidColor.rgb(244, 67, 54)

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val centerX = totalWidth / 2f
        val avatarY = 20f
        val radius = avatarSize / 2f

        // Фон маркера
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = markerColor; style = Paint.Style.FILL }
        canvas.drawCircle(centerX, avatarY + radius, radius + 8, bgPaint)

        // Обводка
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE; style = Paint.Style.STROKE; strokeWidth = 6f
        }
        canvas.drawCircle(centerX, avatarY + radius, radius + 8, strokePaint)

        // Аватар или инициалы
        if (!avatarUrl.isNullOrEmpty()) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .size(avatarSize)
                    .scale(Scale.FILL)
                    .crossfade(true)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val clipPath = Path().apply { addCircle(centerX, avatarY + radius, radius, Path.Direction.CW) }
                    canvas.save()
                    canvas.clipPath(clipPath)
                    drawable.setBounds(
                        (centerX - radius).toInt(), avatarY.toInt(),
                        (centerX + radius).toInt(), (avatarY + avatarSize).toInt()
                    )
                    drawable.draw(canvas)
                    canvas.restore()
                } else {
                    drawInitials(canvas, name, centerX, avatarY + radius, radius)
                }
            } catch (e: Exception) {
                drawInitials(canvas, name, centerX, avatarY + radius, radius)
            }
        } else {
            drawInitials(canvas, name, centerX, avatarY + radius, radius)
        }

        // Имя пользователя
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK; textSize = 28f
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, AndroidColor.WHITE)
        }
        val displayName = if (name.length > 14) name.take(12) + "…" else name
        canvas.drawText(displayName, centerX, avatarY + avatarSize + 50f, textPaint)

        // Остриё маркера
        val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = markerColor; style = Paint.Style.FILL }
        val pointerPath = Path().apply {
            moveTo(centerX - 16f, totalHeight - 40f)
            lineTo(centerX + 16f, totalHeight - 40f)
            lineTo(centerX, totalHeight - 10f)
            close()
        }
        canvas.drawPath(pointerPath, pointerPaint)
        canvas.drawPath(pointerPath, strokePaint)

        bitmap
    }

    private fun drawInitials(canvas: Canvas, name: String, cx: Float, cy: Float, radius: Float) {
        val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE; textSize = 52f
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val initial = name.take(1).uppercase()
        canvas.drawText(initial, cx, cy + 18f, letterPaint)
    }

    private fun createEventBitmap(type: String): Bitmap {
        var color = when (type) {
            "ANNOUNCEMENT" -> AndroidColor.rgb(33, 150, 243)
            "EVENT" -> AndroidColor.rgb(76, 175, 80)
            "COMMUNITY" -> AndroidColor.rgb(156, 39, 176)
            "DISCUSSION" -> AndroidColor.rgb(255, 152, 0)
            else -> AndroidColor.rgb(33, 150, 243)
        }
        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 8, paint)
        paint.color = AndroidColor.WHITE; paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 8, paint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE; textSize = 36f; textAlign = Paint.Align.CENTER
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
        val iconChar = when (type) {
            "ANNOUNCEMENT" -> "📢"; "EVENT" -> "🎉"; "COMMUNITY" -> "👥"; "DISCUSSION" -> "💬"
            else -> "📍"
        }
        canvas.drawText(iconChar, size / 2f, size / 2f + 12f, textPaint)
        return bitmap
    }

    fun clearCache() { cache.clear() }
}