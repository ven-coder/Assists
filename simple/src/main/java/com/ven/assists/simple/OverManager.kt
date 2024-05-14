package com.ven.assists.simple

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.ven.assists.GestureListener
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.AssistsWindowLayout
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.databinding.ViewMainOverBinding
import com.ven.assists.simple.databinding.ViewMatchImageBinding
import com.ven.assists.simple.step.AntForestEnergy
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.simple.step.Step
import com.ven.assists.stepper.StepListener
import com.ven.assists.stepper.StepManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

object OverManager : StepListener, GestureListener {
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null

    val matchImage = "1715047396748.jpg"

    private fun createView(): ViewMainOverBinding? {
        return Assists.service?.let { it ->
            Assists.gestureListeners.add(this)
            StepManager.stepListeners.add(this)
            ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                llOption.isVisible = true
                llLog.isVisible = false
                btnCloseLog.isVisible = false
                btnOpenSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(OpenWechatSocial::class.java, Step.STEP_1, isBegin = true)
                }
                btnPublishSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(PublishSocial::class.java, Step.STEP_1, isBegin = true)
                }
                btnStop.setOnClickListener {
                    stop()
                }
                btnCloseLog.setOnClickListener { showOption() }
                btnStopScrollLog.setOnClickListener {
                    isAutoScrollLog = !isAutoScrollLog
                }
                btnLog.setOnClickListener {
                    showLog()
                    btnCloseLog.isVisible = true
                    btnStop.isVisible = false
                }
                btnScrollContacts.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(ScrollContacts::class.java, Step.STEP_1, isBegin = true)
                }
                btnClickBottomTab.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(GestureBottomTab::class.java, Step.STEP_1, isBegin = true)
                }
                btnScrollSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(GestureScrollSocial::class.java, Step.STEP_1, isBegin = true)
                }
                root.setOnCloseClickListener {
                    clear()
                    return@setOnCloseClickListener false
                }

                btnAntForestEnergy.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(AntForestEnergy::class.java, Step.STEP_1, isBegin = true)
                }

                btnScreenSave.setOnClickListener {
                    ScreenCaptureService.instance?.let {
                        Assists.service?.let {
                            val matchImageBinding = ViewMatchImageBinding.inflate(LayoutInflater.from(it)).apply {
                                Glide.with(it).load("file:///android_asset/$matchImage")
                                    .into(ivImage)
                                btnDiscern.setOnClickListener {

                                    root.isInvisible = true

                                    GlobalScope.launch(Dispatchers.Main) {
                                        flow {

                                            delay(50)

                                            val value = getMatchResult()

                                            emit(value)


                                        }.flowOn(Dispatchers.IO)
                                            .onCompletion {
                                                LogUtils.d(it)
                                            }
                                            .catch {
                                                LogUtils.d(it)
                                            }
                                            .collect { value ->
//                                                value?.let {
//                                                    if (it.maxVal < 0.8) {
//                                                        root.isVisible = true
//                                                        Toast.makeText(Assists.service, "未搜索到匹配度>0.8区域", Toast.LENGTH_LONG).show()
//                                                        return@collect
//                                                    }
//                                                }

                                                Assists.service?.let {
                                                    AssistsWindowManager.hideAll()
                                                    AssistsWindowManager.addView(CaptureLayout(it).apply {
                                                        value?.let {
                                                        }
                                                    }, AssistsWindowManager.createLayoutParams())
                                                }
                                            }
                                    }
                                }
                            }
                            matchImageBinding.root.layoutParams.width = ScreenUtils.getScreenWidth() - 60
                            matchImageBinding.root.layoutParams.height = ScreenUtils.getScreenWidth()
                            matchImageBinding.root.setCenter()
                            AssistsWindowManager.addAssistsWindowLayout(matchImageBinding.root)

                        }
                    } ?: let {
                        ActivityUtils.getTopActivity()?.let {
                            if (it is MainActivity) {
                                it.requestMediaProjectionService()
                                GlobalScope.launch {
                                    withContext(Dispatchers.IO) {
                                        delay(1000)
                                        Assists.findByText("立即开始").firstOrNull()?.click()
                                    }
                                }
                            }
                        }

                    }

                }
            }

        }
    }

    private fun getMatchResult(): Core.MinMaxLocResult? {
        val big = Mat()
        val bigmap = ScreenCaptureService.instance?.toBitmap()
        val bitmap = bigmap?.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bitmap, big)
        Imgproc.cvtColor(big, big, Imgproc.COLOR_RGBA2BGR)

        val temp1 = getMat("temp1.jpg")
        val result1 = match(temp1, big)

        val temp2 = getMat("temp3.jpg")
        val result2 = match(temp2, big)

        val temp4 = getMat("temp4.jpg")
        val result3 = match(temp4, big)
        LogUtils.d(result1, result2, result3)

        arrayListOf(result1, result2, result3).apply {
            val newList = sortedByDescending { it?.maxVal }
            return newList.firstOrNull()
        }
    }

    private fun getMat(temp1path: String): Mat {
        val path = PathUtils.getInternalAppCachePath() + "/" + temp1path
        ResourceUtils.copyFileFromAssets(temp1path, path)
        val temp1 = Mat()
        val image1Stream = FileIOUtils.readFile2BytesByStream(path)
        val bytes2Bitmap = ImageUtils.bytes2Bitmap(image1Stream)
        val bitmap2 = bytes2Bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bitmap2, temp1)
        Imgproc.cvtColor(temp1, temp1, Imgproc.COLOR_RGBA2BGR)
        return temp1
    }

    fun match(temp: Mat, big: Mat): Core.MinMaxLocResult? {
        try {
            var currentTimeMillis = System.currentTimeMillis()
            val image1 = PathUtils.getInternalAppCachePath() + "/$matchImage"
            ResourceUtils.copyFileFromAssets(matchImage, image1)
            val value1 = System.currentTimeMillis() - currentTimeMillis

            currentTimeMillis = System.currentTimeMillis()

            val value3 = System.currentTimeMillis() - currentTimeMillis
            currentTimeMillis = System.currentTimeMillis()


            val method = Imgproc.TM_CCOEFF_NORMED
            val result = Mat()
            val capMat = Mat(big, Rect(0, 0, big.width(), big.height() / 2))
            Imgproc.matchTemplate(capMat, temp, result, method)

            val value5 = System.currentTimeMillis() - currentTimeMillis
            currentTimeMillis = System.currentTimeMillis()
//            for (i in 0 until result.rows()) {
//                for (j in 0 until result.cols()) {
//                    val matchValue = result[i, j][0]
//                    if (matchValue < 0.1) {
//
//                    }
//                }
//            }
            val value6 = System.currentTimeMillis() - currentTimeMillis
            currentTimeMillis = System.currentTimeMillis()

            val mmr = Core.minMaxLoc(result)
            val value4 = System.currentTimeMillis() - currentTimeMillis

//            //获取最匹配区域的亮度值
//            val capResultMap = Mat(bigMat, Rect(mmr.maxLoc.x.toInt(), mmr.maxLoc.y.toInt(), templateMat.width(), templateMat.height()))
//            //灰度化
//            val grayMat = Mat()
//            Imgproc.cvtColor(capResultMap, grayMat, Imgproc.COLOR_BGR2GRAY)
//            // 计算ROI区域的平均亮度
//            val mean = Core.mean(grayMat)
//            val brightness = mean.`val`[0] // 平均亮度值
//            LogUtils.d(value1, value2, value3, value4, value5, value6, brightness)

            return mmr
        } catch (cause: Throwable) {
            throw cause
        }
    }

    fun show() {
        viewMainOver ?: let {
            viewMainOver = createView()
            var width = ScreenUtils.getScreenWidth() - 60
            var height = SizeUtils.dp2px(400f)
            viewMainOver?.root?.layoutParams?.width = width
            viewMainOver?.root?.layoutParams?.height = height
            viewMainOver?.root?.minWidth = ScreenUtils.getScreenWidth() / 2
            viewMainOver?.root?.minHeight = height
            viewMainOver?.root?.setCenter()
            AssistsWindowManager.addAssistsWindowLayout(viewMainOver?.root)
        }
    }

    private fun beginStart(view: ViewMainOverBinding) {
        with(view) {
            clearLog()
            showLog()
            isAutoScrollLog = true
            btnCloseLog.isVisible = false
            btnStop.isVisible = true
        }
    }

    override fun onGestureBegin(startLocation: FloatArray, endLocation: FloatArray) {
        viewMainOver?.root?.let {
            val viewXY = IntArray(2)
            it.getLocationOnScreen(viewXY)
            if (startLocation[0] >= viewXY[0] &&
                startLocation[0] <= viewXY[0] + it.measuredWidth &&
                startLocation[1] >= viewXY[1] &&
                startLocation[1] <= viewXY[1] + it.measuredHeight
            ) {
                Assists.gestureBeginDelay = 1000L
                viewMainOver?.root?.switchNotTouchable()
            }
        }
    }

    override fun onGestureEnd() {
        viewMainOver?.root?.switchTouchable()
    }

    override fun onStepStop() {
        log("已停止")
    }

    private fun stop() {
        if (StepManager.isStop) {
            showOption()
            return
        }
        StepManager.isStop = true
        isAutoScrollLog = false
        viewMainOver?.btnStop?.isVisible = false
        viewMainOver?.btnCloseLog?.isVisible = true
    }

    fun showLog() {
        viewMainOver?.llOption?.isVisible = false
        viewMainOver?.llLog?.isVisible = true
    }

    fun showOption() {
        viewMainOver?.llOption?.isVisible = true
        viewMainOver?.llLog?.isVisible = false
    }

    fun clear() {
        Assists.gestureListeners.remove(this)
        StepManager.stepListeners.remove(this)
        viewMainOver = null
    }

    private val logStr: StringBuilder = StringBuilder()
    fun log(value: Any) {
        if (logStr.length > 1000) logStr.delete(0, 50)
        if (logStr.isNotEmpty()) logStr.append("\n")
        logStr.append(TimeUtils.getNowString())
        logStr.append("\n")
        logStr.append(value.toString())
        viewMainOver?.tvLog?.text = logStr
    }

    fun clearLog() {
        logStr.delete(0, logStr.length)
        viewMainOver?.tvLog?.text = ""
    }

    var isAutoScrollLog = true
        set(value) {
            if (value) onAutoScrollLog()
            viewMainOver?.btnStopScrollLog?.text = if (value) "停止滚动" else "继续滚动"
            field = value
        }

    private fun onAutoScrollLog() {
        viewMainOver?.scrollView?.fullScroll(NestedScrollView.FOCUS_DOWN)
        ThreadUtils.runOnUiThreadDelayed({
            if (!isAutoScrollLog) return@runOnUiThreadDelayed
            onAutoScrollLog()
        }, 250)
    }
}