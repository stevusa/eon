package com.codex.eonbrowser

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {

    companion object {
        // Promeni ako tvoj EON servis koristi drugi regionalni URL.
        private const val HOME_URL = "https://eon.tv"

        // Desktop browser identitet. Ne zaobilazi DRM/autentikaciju.
        private const val DESKTOP_UA =
            "Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/140.0.0.0 Safari/537.36"

        private const val CURSOR_STEP_DP = 38f
        private const val FAST_CURSOR_STEP_DP = 90f
    }

    private lateinit var root: FrameLayout
    private lateinit var webView: WebView
    private lateinit var cursor: ImageView

    private var cursorX = 0f
    private var cursorY = 0f
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()

        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFocusable = true
            isFocusableInTouchMode = true
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = DESKTOP_UA
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                CookieManager.getInstance().flush()
                webView.requestFocus()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(
                view: View?,
                callback: CustomViewCallback?
            ) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                cursor.visibility = View.GONE

                view?.let {
                    root.addView(
                        it,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            }

            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                webView.visibility = View.VISIBLE
                cursor.visibility = View.VISIBLE
                webView.requestFocus()
                hideSystemUi()
            }
        }

        cursor = ImageView(this).apply {
            setImageResource(com.codex.eonbrowser.R.drawable.tv_cursor)
            isClickable = false
            isFocusable = false
            elevation = 100f
        }

        val cursorSize = dp(30f).toInt()
        val cursorParams = FrameLayout.LayoutParams(cursorSize, cursorSize)

        root.addView(webView)
        root.addView(cursor, cursorParams)
        setContentView(root)

        root.post {
            cursorX = root.width / 2f
            cursorY = root.height / 2f
            updateCursor()
        }

        if (savedInstanceState == null) {
            webView.loadUrl(HOME_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun moveCursor(dx: Float, dy: Float, fast: Boolean = false) {
        val step = dp(if (fast) FAST_CURSOR_STEP_DP else CURSOR_STEP_DP)
        val half = cursor.width / 2f

        cursorX = min(max(cursorX + dx * step, half), root.width - half)
        cursorY = min(max(cursorY + dy * step, half), root.height - half)
        updateCursor()
    }

    private fun updateCursor() {
        cursor.x = cursorX - cursor.width / 2f
        cursor.y = cursorY - cursor.height / 2f
    }

    private fun clickAtCursor() {
        val downTime = SystemClock.uptimeMillis()
        val x = cursorX
        val y = cursorY

        val down = MotionEvent.obtain(
            downTime, downTime,
            MotionEvent.ACTION_DOWN, x, y, 0
        )
        val up = MotionEvent.obtain(
            downTime, downTime + 80,
            MotionEvent.ACTION_UP, x, y, 0
        )

        webView.dispatchTouchEvent(down)
        webView.dispatchTouchEvent(up)

        down.recycle()
        up.recycle()
    }

    private fun longClickAtCursor() {
        val downTime = SystemClock.uptimeMillis()
        val x = cursorX
        val y = cursorY

        val down = MotionEvent.obtain(
            downTime, downTime,
            MotionEvent.ACTION_DOWN, x, y, 0
        )
        val up = MotionEvent.obtain(
            downTime, downTime + 650,
            MotionEvent.ACTION_UP, x, y, 0
        )

        webView.dispatchTouchEvent(down)
        webView.postDelayed({
            webView.dispatchTouchEvent(up)
            up.recycle()
        }, 650)

        down.recycle()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (customView != null) {
            if (event.action == KeyEvent.ACTION_UP &&
                event.keyCode == KeyEvent.KEYCODE_BACK) {
                webView.webChromeClient.onHideCustomView()
                return true
            }
            return super.dispatchKeyEvent(event)
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            val fast = event.repeatCount > 3
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    moveCursor(-1f, 0f, fast); return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    moveCursor(1f, 0f, fast); return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    moveCursor(0f, -1f, fast); return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveCursor(0f, 1f, fast); return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    if (event.repeatCount == 0) clickAtCursor()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    // Prosledi Play/Pause web stranici.
                    return webView.dispatchKeyEvent(event) ||
                        super.dispatchKeyEvent(event)
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            customView != null -> webView.webChromeClient.onHideCustomView()
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        hideSystemUi()
    }

    override fun onPause() {
        CookieManager.getInstance().flush()
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            webChromeClient = null
            destroy()
        }
        super.onDestroy()
    }
}
