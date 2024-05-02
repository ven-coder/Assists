package com.ven.assists.simple

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
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
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


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
        BarUtils.setStatusBarLightMode(this, true)
        setContentView(viewBind.root)
        Assists.serviceListeners.add(this)

        if (OpenCVLoader.initLocal()) {
            LogUtils.d("OpenCV loaded successfully")
        } else {
            LogUtils.d("OpenCV initialization failed!")
            Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG).show()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            flow<Point?> {

                ResourceUtils.copyFileFromAssets("a.jpg", PathUtils.getInternalAppCachePath() + "/a.jpg")
                ResourceUtils.copyFileFromAssets("b.jpg", PathUtils.getInternalAppCachePath() + "/b.jpg")

                val point = match(PathUtils.getInternalAppCachePath() + "/a.jpg", PathUtils.getInternalAppCachePath() + "/b.jpg");


                emit(point)


            }.flowOn(Dispatchers.IO)
                .onCompletion {
                    LogUtils.d(it)
                }
                .catch {
                    LogUtils.d(it)
                }
                .collect {
                    LogUtils.d(it)
                }
        }
    }

    fun match(small: String, big: String): Point? {
        try {
            val bigMat = Imgcodecs.imread(big)
            val templateMat = Imgcodecs.imread(small)
            if (bigMat.empty()) {
                System.out.println("")
            }
            if (bigMat.empty()) {
                System.out.println("")
            }

            val method = Imgproc.TM_CCORR_NORMED
            val width = bigMat.cols() - templateMat.cols() + 1
            val height = bigMat.rows() - templateMat.rows() + 1
            val result = Mat(width, height, CvType.CV_32FC1)
            val currentTimeMillis = System.currentTimeMillis()
            Imgproc.matchTemplate(bigMat, templateMat, result, method)
            Core.normalize(result, result, 0.0, 1.0, Core.NORM_MINMAX, -1, Mat())
            val mmr = Core.minMaxLoc(result)
            val value = System.currentTimeMillis() - currentTimeMillis
            LogUtils.d(value)
            return mmr.maxLoc
        } catch (cause: Throwable) {
            throw cause
        }
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