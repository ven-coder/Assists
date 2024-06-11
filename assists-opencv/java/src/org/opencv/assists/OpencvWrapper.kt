package org.opencv.assists

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.Assists
import com.ven.assists.AssistsServiceListener
import com.ven.assists.ScreenCaptureService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.io.InputStream
import kotlin.math.abs


object OpencvWrapper {

    fun init() {
        Assists.coroutine.launch {
            if (OpenCVLoader.initLocal()) {
                LogUtils.dTag("123456", "OpenCV loaded successfully")
            } else {
                LogUtils.dTag("123456", "OpenCV initialization failed!")
            }
        }
    }

    fun matchTemplate(image: Mat?, template: Mat?, mask: Mat? = null): Mat? {
        image ?: return null
        template ?: return null
        val resultCols: Int = image.cols() - template.cols() + 1
        val resultRows: Int = image.rows() - template.rows() + 1
        val result = Mat(resultRows, resultCols, CvType.CV_32FC1)
        val method = Imgproc.TM_CCORR_NORMED
        if (mask == null) {
            Imgproc.matchTemplate(image, template, result, method)

        } else {
            Imgproc.matchTemplate(image, template, result, method, mask)

        }
        return result
    }

    fun getResultWithThreshold(
        result: Mat,
        threshold: Double,
        ignoreX: Double = -1.0,
        ignoreY: Double = -1.0,
    ): ArrayList<Point> {
        val resultList = arrayListOf<Point>()
        for (y in 0 until result.rows()) {
            for (x in 0 until result.cols()) {
                val matchValue = result[y, x]
                if (matchValue[0] >= threshold) {
                    val point = Point(x.toDouble(), y.toDouble())
                    if (resultList.isEmpty() || (ignoreX == -1.0 && ignoreY == -1.0)) {
                        resultList.add(point)
                    } else {
                        var ignore = false
                        for (value in resultList) {
                            val ignoreValueX = abs(point.x - value.x)
                            val ignoreValueY = abs(point.y - value.y)
                            if (ignoreX != -1.0 && ignoreValueX < ignoreX) {
                                ignore = true
                                break
                            }
                            if (ignoreY != -1.0 && ignoreValueY < ignoreY) {
                                ignore = true
                                break
                            }
                        }
                        if (!ignore) {
                            resultList.add(point)
                        }
                    }
                }
            }
        }
        return resultList
    }

    fun matchTemplateFromScreenToMinMaxLoc(image: Mat?, template: Mat?, mask: Mat? = null): Core.MinMaxLocResult? {
        image ?: return null
        template ?: return null
        val result = Mat()
        val method = Imgproc.TM_CCORR_NORMED
        Imgproc.matchTemplate(image, template, result, method, mask)
        val minMaxLocResult = Core.minMaxLoc(result)
        return minMaxLocResult
    }


    /**
     * 创建掩膜
     */
    fun createMask(
        source: Mat,
        lowerScalar: Scalar,
        upperScalar: Scalar,
        requisiteExtraRectList: List<Rect> = arrayListOf(),
        redundantExtraRectList: List<Rect> = arrayListOf()
    ): Mat {
        val hsvImage = Mat()
        Imgproc.cvtColor(source, hsvImage, Imgproc.COLOR_BGR2HSV)
        val mask = Mat()
        Core.inRange(hsvImage, lowerScalar, upperScalar, mask)
        requisiteExtraRectList.forEach {
            Imgproc.rectangle(mask, it, Scalar(0.0), -1)
        }
        redundantExtraRectList.forEach {
            Imgproc.rectangle(mask, it, Scalar(255.0), -1)

        }
        return mask
    }

    /**
     * 获取屏幕图像
     */
    fun getScreen(): Mat? {
        val screenBitmap = Assists.screenCaptureService?.toBitmap() ?: return null
        val screenMat = Mat()
        Utils.bitmapToMat(screenBitmap, screenMat)
        Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)
        return screenMat
    }

    /**
     * 从Assets获取图像
     */
    fun getTemplateFromAssets(assetPath: String): Mat? {
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