package com.ven.assists_mp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ven.assists.base.R


/**
 * 屏幕录制服务
 */
class MPService : Service() {

    companion object {
        var onStartCommand: ((service: MPService, intent: Intent, flags: Int, startId: Int) -> Unit)? = null
    }

    override fun onCreate() {
        initNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onStartCommand?.invoke(this, intent, flags, startId)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun captureScreen(mediaProjection: MediaProjection) {

//        val screenWidth = ScreenUtils.getScreenWidth()
//        val screenHeight = ScreenUtils.getScreenHeight()
//        val screenDensityDpi = ScreenUtils.getScreenDensityDpi()
//
//        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
//
//        mediaProjection.createVirtualDisplay(
//            "ScreenCapture",
//            screenWidth,
//            screenHeight,
//            screenDensityDpi,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//            imageReader?.surface,
//            null,
//            null
//        )
    }


//    fun saveBitmap(): File? {
//        val image: Image? = imageReader?.acquireLatestImage()
//
//        try {
//            val width = ScreenUtils.getScreenWidth()
//            val height = ScreenUtils.getScreenHeight()
//            val planes: Array<Image.Plane> = image?.planes ?: return null
//            val buffer: ByteBuffer = planes[0].buffer
//            val pixelStride: Int = planes[0].pixelStride
//            val rowStride: Int = planes[0].rowStride
//            val rowPadding = rowStride - pixelStride * width
//            val bitmap = Bitmap.createBitmap(
//                width + rowPadding / pixelStride,
//                height,
//                Bitmap.Config.ARGB_8888
//            )
//            bitmap.copyPixelsFromBuffer(buffer)
//            val file = File(PathUtils.getInternalAppCachePath(), "${System.currentTimeMillis()}.png")
//            val fileOutputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//            fileOutputStream.flush()
//            fileOutputStream.close()
//            image.close()
//            return file
//        } catch (e: Throwable) {
//            return null
//        } finally {
//            image?.close()
//        }
//    }

//    fun toBitmap(): Bitmap? {
//        val image: Image? = imageReader?.acquireLatestImage()
//
//        return try {
//            val width = ScreenUtils.getScreenWidth()
//            val height = ScreenUtils.getScreenHeight()
//            val planes: Array<Image.Plane> = image?.planes ?: return null
//            val buffer: ByteBuffer = planes[0].buffer
//            val pixelStride: Int = planes[0].pixelStride
//            val rowStride: Int = planes[0].rowStride
//            val rowPadding = rowStride - pixelStride * width
//            val bitmap = Bitmap.createBitmap(
//                width + rowPadding / pixelStride,
//                height,
//                Bitmap.Config.ARGB_8888
//            )
//            bitmap.copyPixelsFromBuffer(buffer)
//            return bitmap
//        } catch (e: Throwable) {
//            null
//        } finally {
//            image?.close()
//        }
//    }

    private fun initNotificationChannel() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }
        val builder = NotificationCompat.Builder(this, channelId)
        val notification = builder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    /**
     * 创建通知通道
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "mirror.hsl"
        val chan = NotificationChannel(channelId, "ForegroundService", NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}