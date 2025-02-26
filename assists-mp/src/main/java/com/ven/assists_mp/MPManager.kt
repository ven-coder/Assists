package com.ven.assists_mp

import android.app.Activity
import android.app.Application
import android.app.Service.MEDIA_PROJECTION_SERVICE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.nodeGestureClick
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

object MPManager {
    const val REQUEST_CODE = "request_code_media_projection"
    const val REQUEST_DATA = "request_data_media_projection"

    private var requestLaunchers = hashMapOf<Activity, ActivityResultLauncher<Intent>>()

    var onEnable: ((service: MPService, intent: Intent, flags: Int, startId: Int) -> Unit)? = null
    private var completableDeferredEnable: CompletableDeferred<Boolean>? = null
    private var autoAllowJob: Job? = null

    private var imageReader: ImageReader? = null

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is ComponentActivity && requestLaunchers[activity] == null) {
                requestLaunchers[activity] = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    onResult(result)
                }
            }
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            requestLaunchers.remove(activity)
        }
    }

    private fun onStartCommand(service: MPService, intent: Intent, flags: Int, startId: Int) {
        onCreateImageReader(service, intent, flags, startId)
        onEnable?.invoke(service, intent, flags, startId)
        completableDeferredEnable?.complete(true)
        completableDeferredEnable = null
    }

    private fun onResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            MPService.onStartCommand = ::onStartCommand
            val service = Intent(ActivityUtils.getTopActivity(), MPService::class.java)
            service.putExtra(REQUEST_CODE, result.resultCode)
            service.putExtra(REQUEST_DATA, result.data)
            ActivityUtils.getTopActivity().startService(service)
            LogUtils.d("enable screen capture")
        }
    }

    private fun onCreateImageReader(service: MPService, intent: Intent, flags: Int, startId: Int) {
        imageReader?.close()
        val mediaProjectionManager = service.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()
        val screenDensityDpi = ScreenUtils.getScreenDensityDpi()
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2).apply {
            val requestCode = intent.getIntExtra(REQUEST_CODE, -1)
            val requestData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(REQUEST_DATA, Intent::class.java)
            } else {
                intent.getParcelableExtra<Intent>(REQUEST_DATA)
            }
            val mediaProjection = mediaProjectionManager.getMediaProjection(requestCode, requestData!!)
            mediaProjection.createVirtualDisplay(
                "assists_mp",
                screenWidth,
                screenHeight,
                screenDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )
        }
    }

    fun init(application: Application) {
        requestLaunchers.clear()
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    suspend fun request(autoAllow: Boolean = true, timeOut: Long = 5000): Boolean {
        var projectionManager: MediaProjectionManager? = null
        Assists.service?.let {
            projectionManager = it.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        } ?: let {
            projectionManager = ActivityUtils.getTopActivity()?.getSystemService(Context.MEDIA_PROJECTION_SERVICE)?.let {
                it as MediaProjectionManager
            }
        }
        projectionManager ?: return false
        val intent = projectionManager!!.createScreenCaptureIntent()
        requestLaunchers[ActivityUtils.getTopActivity()]?.launch(intent)
        completableDeferredEnable = CompletableDeferred()
        if (autoAllow) {
            CoroutineWrapper.launch {
                var time = 0L
                while (time <= timeOut) {
                    Assists.findByTags("android.widget.Button", "android:id/button1", "立即开始").firstOrNull()?.click()
                    Assists.findByTags("android.widget.Spinner", "com.android.systemui:id/screen_share_mode_spinner").firstOrNull()?.let {
                        if (it.click()) {
                            delay(250)
                            Assists.findByTags("android.widget.TextView", "android:id/text1", "整个屏幕").firstOrNull()?.nodeGestureClick()
                        }
                    }
                    Assists.findByTags("android.widget.Button", "android:id/button1", "开始").firstOrNull()?.click()
                    val delayMillis = 250L
                    time += delayMillis
                    delay(delayMillis)
                }
                completableDeferredEnable?.complete(false)
            }
        } else {
            autoAllowJob?.cancel()
            autoAllowJob = CoroutineWrapper.launch {
                var time = 0L
                while (time <= timeOut) {
                    val delayMillis = 250L
                    time += delayMillis
                    delay(delayMillis)
                }
                completableDeferredEnable?.complete(false)
            }
        }
        return completableDeferredEnable?.await() ?: false
    }

    fun takeScreenshot2Bitmap(): Bitmap? {
        imageReader?.let {
            val image: Image = it.acquireLatestImage()
            try {
                val bitmap = imageToBitmap(image)
                return bitmap
            } catch (e: Throwable) {
                return null
            } finally {
                image.close()
            }
        } ?: let { throw RuntimeException("Please request permission for screen recording first") }
    }

    fun AccessibilityNodeInfo.getBitmap(screenshot: Bitmap): Bitmap? {
        runCatching {
            getBoundsInScreen().let {
                val bitmap = Bitmap.createBitmap(screenshot, it.left, it.top, it.width(), it.height())
                return@runCatching bitmap
            }
        }.onSuccess {
            return it
        }
        return null
    }

    fun AccessibilityNodeInfo.takeScreenshot2File(
        screenshot: Bitmap,
        file: File = File(
            PathUtils.getInternalAppFilesPath(),
            "screenshot_${System.currentTimeMillis()}.png"
        )
    ): File? {
        val bitmap = getBitmap(screenshot) ?: return null
        val result = ImageUtils.save(bitmap, file, Bitmap.CompressFormat.PNG)
        if (result) {
            return file
        }
        return null
    }

    fun takeScreenshot2File(
        file: File = File(
            PathUtils.getInternalAppFilesPath(),
            "screenshot_${System.currentTimeMillis()}.png"
        )
    ): File? {
        val bitmap = takeScreenshot2Bitmap()
        val result = ImageUtils.save(bitmap, file, Bitmap.CompressFormat.PNG)
        if (result) {
            return file
        }
        return null
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

}