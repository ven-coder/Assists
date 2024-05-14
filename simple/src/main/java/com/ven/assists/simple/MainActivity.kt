package com.ven.assists.simple

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ToastUtils
import com.ven.assists.Assists
import com.ven.assists.AssistsService
import com.ven.assists.AssistsServiceListener
import com.ven.assists.simple.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity(), AssistsServiceListener {
    val viewBind: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).apply {
            btnOption.setOnClickListener {
                if (Assists.isAccessibilityServiceEnabled()) {
                    OverManager.show()
                } else {
                    Assists.openAccessibilitySetting()
                }
            }
        }
    }

    lateinit var mediaProjectionService: MediaProjectionManager

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val scapService = Intent(this, ScreenCaptureService::class.java)
            scapService.putExtra("rCode", result.resultCode)
            scapService.putExtra("rData", result.data)
            startService(scapService)
        } else {
            LogUtils.d(result)
        }
    }

    override fun onResume() {
        super.onResume()
        checkServiceEnable()
    }

    private fun checkServiceEnable() {
        if (Assists.isAccessibilityServiceEnabled()) {
            viewBind.btnOption.text = "显示操作浮窗"
        } else {
            viewBind.btnOption.text = "开启服务"
        }
    }

    override fun onServiceConnected(service: AssistsService) {
        GlobalScope.launch {
            onBackApp()
        }
    }

    private suspend fun onBackApp() {
        flow<String> {
            while (Assists.getPackageName() != packageName) {
                Assists.back()
                delay(500)
            }
        }.flowOn(Dispatchers.IO).collect {}
    }

    override fun onUnbind() {
        OverManager.clear()
        checkServiceEnable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionService = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        BarUtils.setStatusBarLightMode(this, true)
        setContentView(viewBind.root)
        Assists.serviceListeners.add(this)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                if (OpenCVLoader.initLocal()) {
                    LogUtils.d("OpenCV loaded successfully")
                } else {
                    LogUtils.d("OpenCV initialization failed!")
                }
            }
        }
    }

    fun requestMediaProjectionService() {

        activityResultLauncher.launch(mediaProjectionService.createScreenCaptureIntent())
    }

    override fun onDestroy() {
        super.onDestroy()
        Assists.serviceListeners.remove(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}