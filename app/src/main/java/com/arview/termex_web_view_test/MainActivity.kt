package com.arview.termex_web_view_test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    lateinit var communicationManager: CommunicationManager

    @SuppressLint("SetJavaScriptEnabled")
    private fun startCharts() {
        communicationManager = CommunicationManager(this, web_view)
        val webSettings = web_view.settings
        webSettings.javaScriptEnabled = true

        web_view.addJavascriptInterface(communicationManager, "CommunicationManager")
        web_view.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                communicationManager.start()

            }
        }
        web_view.loadUrl("file:///android_asset/index.html")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        real_time_btn.setOnClickListener { view ->
            communicationManager.switchToRealTime()
        }

        free_btn.setOnClickListener { view ->
            communicationManager.switchToFreeMode()
        }

        camera_btn.setOnClickListener { view ->
            communicationManager.getChartImage()

           // val bitmap = createBitmapFromView(this, web_view_layout)
          //  communicationManager.saveBitmapToFile(bitmap!!)
        }

        startCharts()
    }

    private fun createBitmapFromView(context: Context, view: View): Bitmap? {
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
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
