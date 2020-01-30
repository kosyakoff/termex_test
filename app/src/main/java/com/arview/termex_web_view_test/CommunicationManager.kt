package com.arview.termex_web_view_test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.random.Random

data class TimeData(var x: Int, var y: Int)

enum class TimeWindowSize(val value: Double) {
    Second20(20.0),
    Min5(60 * 5.0),
    Min15(60 * 15.0),
    Min30(60 * 15.0),
    Hour1(60 * 60.0),
    Hour2(60 * 120.0)
}

class CommunicationManager {
    constructor(c: Context, w: WebView) {
        this.mContext = c
        this.webView = w
    }

    fun start() {

        startChart()
    }

    private var currentXScaleMin = 0.0
    private var currentYScaleMin = 0.0
    private var currentYScaleMax = 0.0
    private var currentXScaleMax = 0.0
    private var mContext: Context?
    private var webView: WebView
    private var arrayIndex = 15
    private var margin = 5
    private var timeWindowWidth = TimeWindowSize.Second20.value
    private var gson = Gson()
    private var initArray1 =
        MutableList(0) { ind ->
            TimeData(
                ind,
                Random.nextInt(-margin, margin)
            )
        }

    private var initArray2 =
        MutableList(0) { ind ->
            TimeData(
                ind,
                Random.nextInt(-margin, margin)
            )
        }

    private var isRealTime = true

    private fun updateDataWithArray() {

        initArray1 =
            MutableList(arrayIndex) { ind ->
                TimeData(
                    ind,
                    Random.nextInt(-margin, margin)
                )
            }

        initArray2 =
            MutableList(arrayIndex) { ind ->
                TimeData(
                    ind,
                    Random.nextInt(-margin, margin)
                )
            }

        val arrayString1 = gson.toJson(initArray1)
        val arrayString2 = gson.toJson(initArray2)

        val xEnd = arrayIndex.toDouble()

        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(
                "javascript:updateData(${arrayString1},${arrayString2},0,$xEnd,false, true)",
                null
            )
        }
    }

    @JavascriptInterface
    fun startChart() {

        updateDataWithArray()

        val timerTask: TimerTask =
            timerTask {
                    appendData()
            }

        Timer().scheduleAtFixedRate(timerTask, 0L, 1000L)
    }

    private fun appendData() {

        val xStart =
            if (arrayIndex.toDouble() - timeWindowWidth < 0) 0.0 else arrayIndex.toDouble() - timeWindowWidth
        val xEnd = arrayIndex.toDouble()

        val appendArray1 = MutableList(1) {
            TimeData(
                arrayIndex,
                Random.nextInt(-margin, margin)
            )
        }
        var arrayString1 = gson.toJson(appendArray1)

        val appendArray2 = MutableList(1) {
            TimeData(
                arrayIndex,
                Random.nextInt(-margin, margin)
            )
        }
        var arrayString2 = gson.toJson(appendArray2)

        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(
                "javascript:updateData(${arrayString1},${arrayString2},${xStart},$xEnd,$isRealTime, false)",
                null
            )
        }

        arrayIndex++
    }

    fun switchToRealTime() {
        isRealTime = true
    }

    fun switchToFreeMode() {
        isRealTime = false
    }

    fun getChartImage() {
        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(
                "javascript:getScreenshot()",
                null
            )
        }
    }

    fun getBitmapFrom64String(base64Str: String): Bitmap {
        val base64Image: String = base64Str.split(",").get(1)
        var imageBytes: ByteArray = Base64.getDecoder().decode(base64Image)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    @JavascriptInterface
    fun getChartImageInner(base64Str: String) {

        val decodedImage = getBitmapFrom64String(base64Str)

        saveBitmapToFile(decodedImage)
    }

    @JavascriptInterface
    fun getScalesValue(xScaleMin: Double, xScaleMax: Double, yScaleMin: Double, yScaleMax: Double) {

        currentXScaleMin = xScaleMin
        currentXScaleMax = xScaleMax
        currentYScaleMin = yScaleMin
        currentYScaleMax = yScaleMax
    }

    @JavascriptInterface
    fun getClickData(xStr: String, yStr: String) {

       var clickedX = xStr.toDoubleOrNull()
        var clickedY = yStr.toDoubleOrNull()

    }

    fun saveBitmapToFile(decodedImage: Bitmap) {
        try {

            val logDirPath = mContext!!.externalCacheDir?.path + "/images"
            val direct = File(logDirPath)

            if (!direct.exists()) {
                direct.mkdir()
            }

            val fileName = "image1.png"

            val file = File(logDirPath + File.separator + fileName)

            file.createNewFile()

            if (file.exists()) {

                val fileOutputStream = FileOutputStream(file, false)

                decodedImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

                fileOutputStream.close()
            }

        } catch (e: Throwable) {
            Log.e("", "Error while logging into file : $e")
        }
    }

    fun setTimeWindow(timeWindowSize: TimeWindowSize) {

        if (isRealTime) {
            timeWindowWidth = timeWindowSize.value
        } else {
            val newWindowCenter = currentXScaleMax - (currentXScaleMin / 2)
            var windowStart = newWindowCenter - (timeWindowSize.value / 2)
            var windowEnd = newWindowCenter + (timeWindowSize.value / 2)

            if (windowStart < 0) {
                windowStart = 0.0
                windowEnd = timeWindowSize.value
            }

            Handler(Looper.getMainLooper()).post {
                webView.evaluateJavascript(
                    "javascript:updateTimeWindow(${windowStart},${windowEnd})",
                    null
                )
            }
        }
    }

    fun initDataWithValue(initElementSize: Int) {
        arrayIndex = initElementSize
        updateDataWithArray()
    }

}