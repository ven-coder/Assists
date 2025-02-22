package com.ven.assists_opcv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.AssistsWindowManager
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.delay
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
        CoroutineWrapper.launch {
            if (OpenCVLoader.initLocal()) {
                LogUtils.dTag(Assists.LOG_TAG, "OpenCV loaded successfully")
            } else {
                LogUtils.dTag(Assists.LOG_TAG, "OpenCV initialization failed!")
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
    fun getScreenMat(): Mat? {
//        val screenBitmap = Assists.mediaProjectionService?.toBitmap() ?: return null
        val screenMat = Mat()
//        Utils.bitmapToMat(screenBitmap, screenMat)
        Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)
        return screenMat
    }

//    fun getScreenBitmap(): Bitmap? {
//        return Assists.mediaProjectionService?.toBitmap()
//    }
//
//    suspend fun getScreenBitmapIgnoreFloatWindow(): Bitmap? {
//        AssistsWindowManager.hideAll()
//        delay(50)
//        val bitmap = Assists.mediaProjectionService?.toBitmap()
//        AssistsWindowManager.showLastView()
//        return bitmap
//    }

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

    /**
     * 悬挂函数，用于获取当前AccessibilityNodeInfo对象的屏幕截图
     * 此函数通过获取节点在屏幕上的边界，然后截取相应区域的屏幕内容来生成Bitmap对象
     * 如果节点的屏幕高度小于等于0，则返回null，表示无法获取有效的截图
     *
     * @return 截取的Bitmap对象，如果无法获取截图则返回null
     */
    suspend fun AccessibilityNodeInfo.getBitmap(): Bitmap? {
        // 获取当前节点在屏幕上的边界
        val screen = this.getBoundsInScreen()
        // 如果屏幕高度小于等于0，说明无法获取有效的截图，直接返回null
        if (screen.height() <= 0) return null
        // 隐藏所有辅助视图，以避免影响截图结果
        AssistsWindowManager.hideAll()
        // 延迟100毫秒，等待视图隐藏完成
        delay(100)
        // 尝试获取整个屏幕的截图
//        getScreenBitmap()?.let {
//            // 显示最后一个辅助视图
//            AssistsWindowManager.showLastView()
//            // 从整个屏幕截图中裁剪出当前节点对应的区域
//            val bitmap = Bitmap.createBitmap(
//                it,
//                screen.left,
//                screen.top,
//                screen.width(),
//                screen.height(),
//            )
//            // 返回裁剪后的Bitmap对象
//            return bitmap
//        }
        // 如果无法获取屏幕截图，则返回null
        return null
    }
}