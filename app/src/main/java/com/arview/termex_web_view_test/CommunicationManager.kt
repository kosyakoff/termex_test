package com.arview.termex_web_view_test

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.random.Random

data class TimeData(var x: Int, var y: Int)

class CommunicationManager {
    constructor(c: Context, w: WebView) {
        this.mContext = c
        this.webView = w
    }

    fun start() {

        //webView.evaluateJavascript("startAll(${chart.toJson()})", null)
        startChart()
    }

    private var mContext: Context?
    private var webView: WebView
    private var arrayIndex = 1500
    private var margin = 3
    private var xWindow = 100
    private var maxX = 500
    private var xStart = 0
    private var xEnd = 0
    private var gson = Gson()
    private var initArray1 =
        MutableList(arrayIndex) { ind -> TimeData(ind,
            Random.nextInt(-margin, margin)
        ) }

    private var initArray2 =
        MutableList(arrayIndex) { ind -> TimeData(ind,
            Random.nextInt(-margin, margin)
        ) }

    private var stopUpdate = false

    private fun updateDataWithArray()
    {
        var arrayString1 = gson.toJson(initArray1)
        var arrayString2 = gson.toJson(initArray2)

        xStart = 0
        xEnd = arrayIndex

        Handler(Looper.getMainLooper()).post { webView.evaluateJavascript("javascript:updateData(${arrayString1},${arrayString2},0,$xEnd)", null)}
    }

    @JavascriptInterface
    fun showAll()
    {
        xStart = 0
        xEnd = maxX
        stopUpdate = true

    }

    @JavascriptInterface
    fun lastPoint()
    {
        stopUpdate = false
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

        if (!stopUpdate)
        {
            xStart = arrayIndex - xWindow
            xEnd = arrayIndex
        }

        val appendArray1 = MutableList(1) { TimeData(arrayIndex,
            Random.nextInt(-margin, margin))}
        var arrayString1 = gson.toJson(appendArray1)


        val appendArray2 = MutableList(1) { TimeData(arrayIndex,
            Random.nextInt(-margin, margin))}
        var arrayString2 = gson.toJson(appendArray2)

        Handler(Looper.getMainLooper()).post {webView.evaluateJavascript("javascript:updateData(${arrayString1},${arrayString2},${xStart},$xEnd)", null)}

        arrayIndex++
    }
}