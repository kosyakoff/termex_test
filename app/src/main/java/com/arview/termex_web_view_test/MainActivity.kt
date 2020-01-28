package com.arview.termex_web_view_test

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.random.Random

class CommunicationManager {
    constructor(c: Context, w: WebView) {
        this.mContext = c
        this.webView = w
    }

    private var mContext: Context?
    private var webView: WebView
    private var arrayIndex = 100
    private var margin = 20
    private var xWindow = 100
    private var maxX = 500
    private var xStart = 0
    private var xEnd = 0
    private var initArray1 =
        MutableList<Pair<Int,Int>>(arrayIndex) { ind -> Pair(ind, Random.nextInt(-margin,margin)) }

    private var initArray2 =
        MutableList<Pair<Int,Int>>(arrayIndex) { ind -> Pair(ind, Random.nextInt(-margin,margin)) }

    private var stopUpdate = false

    private fun updateDataWithArray()
    {
        var arrayString1 = initArray1.toString()
        arrayString1 = arrayString1.replace('(','[').replace(')',']')

        var arrayString2 = initArray2.toString()
        arrayString2 = arrayString2.replace('(','[').replace(')',']')

        xStart = 0
        xEnd = arrayIndex

        Handler(Looper.getMainLooper()).post {webView.loadUrl("javascript:updateData(${arrayString1},${arrayString2},0,$xEnd)")}
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

        val timerTask: TimerTask = timerTask {

            if (Looper.myLooper() == Looper.getMainLooper()) {
                appendData()
            } else {
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post { appendData() }
            }
        }

        Timer().scheduleAtFixedRate(timerTask, 0L, 1500L)
    }

    private fun appendData() {

        if (!stopUpdate)
        {
            xStart = arrayIndex - xWindow
            xEnd = arrayIndex
        }

        val appendArray1 = MutableList<Pair<Int,Int>>(0) { Pair(0,0)}
        appendArray1.add(0, Pair(arrayIndex, Random.nextInt(-margin,margin)))
        var arrayString1 = appendArray1.toString()
        arrayString1 = arrayString1.replace('(','[').replace(')',']')

        val appendArray2 = MutableList<Pair<Int,Int>>(0) { Pair(0,0)}
        appendArray2.add(0, Pair(arrayIndex, Random.nextInt(-margin,margin)))
        var arrayString2 = appendArray2.toString()
        arrayString2 = arrayString2.replace('(','[').replace(')',']')

        Handler(Looper.getMainLooper()).post {webView.loadUrl("javascript:updateData(${arrayString1},${arrayString2},${xStart},$xEnd)")}

        arrayIndex++
    }
}

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    private fun startCharts()
    {
        val webSettings = web_view.settings
        webSettings.javaScriptEnabled = true

        web_view.addJavascriptInterface(CommunicationManager(this, web_view),"CommunicationManager")
        web_view.loadUrl("file:///android_asset/index.html")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        startCharts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
