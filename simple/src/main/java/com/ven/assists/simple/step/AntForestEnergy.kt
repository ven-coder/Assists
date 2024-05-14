package com.ven.assists.simple.step

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.FileIOUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.CaptureLayout
import com.ven.assists.simple.MainActivity
import com.ven.assists.simple.ScreenCaptureService
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.io.InputStream


class AntForestEnergy : StepImpl {
    override fun onImpl(collector: StepCollector) {
        collector.nextLoop(Step.STEP_1) {

            ScreenCaptureService.instance?.let {

                AppUtils.launchApp("com.eg.android.AlipayGphone")
                StepManager.execute(this::class.java, Step.STEP_2)

                return@nextLoop true
            } ?: let {
                ActivityUtils.getTopActivity()?.let {
                    if (it is MainActivity) {
                        it.requestMediaProjectionService()
                        Assists.findByText("立即开始").firstOrNull()?.let {
                            it.click()
                            StepManager.execute(this::class.java, Step.STEP_1)
                            return@nextLoop true
                        }
                    }
                }

            }

            return@nextLoop false
        }.next(Step.STEP_2) {

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val tempBitmap = getBitmapFromAsset("mayisenlin.jpg")
                    val tempMat = Mat()
                    Utils.bitmapToMat(tempBitmap, tempMat)
                    Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2BGR)

                    val screenBitmap = ScreenCaptureService.instance?.toBitmap()
                    val screenMat = Mat()
                    Utils.bitmapToMat(screenBitmap, screenMat)
                    Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)

                    val method = Imgproc.TM_CCOEFF_NORMED
                    val result = Mat()
                    val capMat = Mat(screenMat, Rect(0, 0, screenMat.width(), screenMat.height() / 2))
                    Imgproc.matchTemplate(capMat, tempMat, result, method)

                    val minMaxLoc = Core.minMaxLoc(result)
                    withContext(Dispatchers.Main) {
                        Assists.service?.let {
                            AssistsWindowManager.addView(CaptureLayout(it).apply {
                                setPoint(minMaxLoc.maxLoc, tempMat.width(), tempMat.height())
                            }, AssistsWindowManager.createLayoutParams())
                            AssistsWindowManager.switchNotTouchableAll()
                            Assists.gestureClick((minMaxLoc.maxLoc.x + tempMat.width() / 2).toFloat(), (minMaxLoc.maxLoc.y + tempMat.height() / 2).toFloat())
                        }
                    }
                }
            }


        }.next(Step.STEP_3) {

        }
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