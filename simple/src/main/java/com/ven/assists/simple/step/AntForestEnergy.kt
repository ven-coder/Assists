package com.ven.assists.simple.step

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.ven.assists.AssistsServiceListener
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepOperator
import java.io.IOException
import java.io.InputStream


class AntForestEnergy : StepImpl(), AssistsServiceListener {
    override fun onImpl(collector: StepCollector) {
        collector.next(1) {
            return@next Step.none
        }
//        collector.next(Step.STEP_1) {
//            AppUtils.launchApp("com.eg.android.AlipayGphone")
//            StepManager.execute(this::class.java, Step.STEP_2)
//            return@next com.ven.assists.stepper.Step.none
//        }.next(Step.STEP_2, isRunCoroutineIO = true) {
//
//            val tempBitmap1 = getBitmapFromAsset("mayisenlin.jpg")
//            val tempMat1 = Mat()
//            Utils.bitmapToMat(tempBitmap1, tempMat1)
//            Imgproc.cvtColor(tempMat1, tempMat1, Imgproc.COLOR_RGBA2BGR)
//
//            val tempBitmap2 = getBitmapFromAsset("mayisenlin2.jpg")
//            val tempMat2 = Mat()
//            Utils.bitmapToMat(tempBitmap2, tempMat2)
//            Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_RGBA2BGR)
//
//            val tempBitmap3 = getBitmapFromAsset("mayisenlin3.jpg")
//            val tempMat3 = Mat()
//            Utils.bitmapToMat(tempBitmap3, tempMat3)
//            Imgproc.cvtColor(tempMat3, tempMat3, Imgproc.COLOR_RGBA2BGR)
//
//            val screenBitmap = ScreenCaptureService.instance?.toBitmap()
//            val screenMat = Mat()
//            Utils.bitmapToMat(screenBitmap, screenMat)
//            Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_RGBA2BGR)
//
//            val method = Imgproc.TM_CCOEFF_NORMED
//            val capMat = Mat(screenMat, Rect(0, 0, screenMat.width(), screenMat.height() / 2))
//
//            val result = Mat()
//
//            Imgproc.matchTemplate(capMat, tempMat1, result, method)
//            val minMaxLoc1 = Core.minMaxLoc(result)
//
//            Imgproc.matchTemplate(capMat, tempMat2, result, method)
//            val minMaxLoc2 = Core.minMaxLoc(result)
//
//            Imgproc.matchTemplate(capMat, tempMat3, result, method)
//            val minMaxLoc3 = Core.minMaxLoc(result)
//
//            val maxLocResult = arrayListOf(minMaxLoc1, minMaxLoc2, minMaxLoc3).sortedByDescending { it?.maxVal }.first()
//
//            withContext(Dispatchers.Main) {
//                Assists.service?.let {
//                    AssistsWindowManager.addView(CaptureLayout(it).apply {
//                        setPoint(maxLocResult.maxLoc, tempMat1.width(), tempMat1.height())
//                    }, AssistsWindowManager.createLayoutParams())
//                    AssistsWindowManager.switchNotTouchableAll()
//                }
//            }
//            delay(1500)
//            withContext(Dispatchers.Main) {
//                Assists.gestureClick((maxLocResult.maxLoc.x + tempMat1.width() / 2).toFloat(), (maxLocResult.maxLoc.y + tempMat1.height() / 2).toFloat())
//            }
//            delay(1500)
//            withContext(Dispatchers.Main) {
//                AssistsWindowManager.pop()
//            }
//            StepManager.execute(this::class.java, Step.STEP_3)
//
//        }.next(Step.STEP_3, isRunCoroutineIO = true) {
//
//        }
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

    override fun screenCaptureEnable() {

    }
}