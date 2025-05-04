package com.ven.assists.opcv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.AssistsCore
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists.mp.MPManager
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

/**
 * OpenCV包装器
 * 提供图像处理和模板匹配等计算机视觉功能的封装
 * 主要用于自动化过程中的图像识别和处理
 */
object OpencvWrapper {

    /**
     * 初始化OpenCV库
     * 在后台协程中加载OpenCV本地库，并输出加载结果日志
     */
    fun init() {
        CoroutineWrapper.launch {
            if (OpenCVLoader.initLocal()) {
                LogUtils.dTag(AssistsCore.LOG_TAG, "OpenCV loaded successfully")
            } else {
                LogUtils.dTag(AssistsCore.LOG_TAG, "OpenCV initialization failed!")
            }
        }
    }

    /**
     * 执行模板匹配操作
     * 使用标准化相关系数匹配方法（TM_CCORR_NORMED）
     * 
     * @param image 要搜索的源图像
     * @param template 要匹配的模板图像
     * @param mask 可选的掩码图像，用于指定模板中要考虑的区域
     * @return 返回匹配结果矩阵，如果输入无效则返回null
     */
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

    /**
     * 从匹配结果中获取符合阈值的点位置
     * 支持忽略相近点的功能
     * 
     * @param result 匹配结果矩阵
     * @param threshold 匹配阈值，只返回大于等于此值的点
     * @param ignoreX X轴方向上忽略的距离，-1表示不忽略
     * @param ignoreY Y轴方向上忽略的距离，-1表示不忽略
     * @return 符合条件的点位置列表
     */
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

    /**
     * 执行模板匹配并返回最佳匹配位置
     * 使用标准化相关系数匹配方法，返回包含最小值、最大值及其位置的结果
     * 
     * @param image 要搜索的源图像
     * @param template 要匹配的模板图像
     * @param mask 可选的掩码图像
     * @return MinMaxLocResult对象，包含最小值、最大值及其位置，如果输入无效则返回null
     */
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
     * 创建图像掩膜
     * 基于HSV颜色空间的阈值分割，支持添加必要区域和冗余区域
     * 
     * @param source 源图像
     * @param lowerScalar HSV颜色空间的下限
     * @param upperScalar HSV颜色空间的上限
     * @param requisiteExtraRectList 必要区域列表（黑色区域）
     * @param redundantExtraRectList 冗余区域列表（白色区域）
     * @return 生成的掩膜图像
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
     * 获取当前屏幕的Mat对象
     * 将屏幕截图转换为OpenCV的Mat格式，并进行颜色空间转换
     * 
     * @return 屏幕内容的Mat对象，如果截图失败则返回null
     */
    fun getScreenMat(): Mat? {
        val screenBitmap = MPManager.takeScreenshot2Bitmap() ?: return null
        val screenMat = Mat()
        Utils.bitmapToMat(screenBitmap, screenMat)
        Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)
        return screenMat
    }

    /**
     * 从Assets资源中加载模板图像
     * 将图像转换为OpenCV的Mat格式，并进行颜色空间转换
     * 
     * @param assetPath Assets中的图像文件路径
     * @return 模板图像的Mat对象，如果加载失败则返回null
     */
    fun getTemplateFromAssets(assetPath: String): Mat? {
        val bitmap = getBitmapFromAsset(assetPath)
        bitmap ?: return null
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR)
        return mat
    }

    /**
     * 从Assets资源中加载Bitmap图像
     * 
     * @param filePath Assets中的图像文件路径
     * @return Bitmap对象，如果加载失败则返回null
     */
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