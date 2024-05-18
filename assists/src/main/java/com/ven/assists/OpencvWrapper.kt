package com.ven.assists

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.io.InputStream

object OpencvWrapper {

    fun init() {
        Assists.coroutine.launch {
            if (OpenCVLoader.initLocal()) {
                LogUtils.d("OpenCV loaded successfully")
            } else {
                LogUtils.d("OpenCV initialization failed!")
            }
        }
    }

    fun matchTemplateFromScreen(image: Mat, template: Mat): Point {
        val result = Mat()
        val method = Imgproc.TM_CCOEFF_NORMED
        Imgproc.matchTemplate(image, template, result, method)
        val minMaxLocResult = Core.minMaxLoc(result)
        return minMaxLocResult.maxLoc
    }

    fun getScreen() {
        val screenBitmap = Assists.screenCaptureService?.toBitmap()
        val screenMat = Mat()
        Utils.bitmapToMat(screenBitmap, screenMat)
        Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)
    }

    fun getTemplateFromAsset(assetPath: String): Mat? {
        val bitmap = getBitmapFromAsset(assetPath)
        bitmap ?: return null
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR)
        return mat
    }

    private fun getBitmapFromAsset(filePath: String): Bitmap? {
        val assetManager = ActivityUtils.getTopActivity().assets
        var inputStream: InputStream? = null
        return try {
            inputStream = assetManager.open(filePath)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


}