package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class WebpageActivity : AppCompatActivity(), View.OnClickListener {

    private var nextButton: Button? = null
    private var testWebView: WebView? = null
    private var testProgress: ProgressBar? = null
    private var testScroll: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webpage)


        // 「Page2」のボタン
        nextButton = findViewById(R.id.next_button)
        // 「Page2」リスナー
        nextButton?.setOnClickListener(this)
        // スクロール
        testScroll = findViewById<View>(R.id.testScroll) as ScrollView?
        // WebView
        testWebView = findViewById<View>(R.id.testWebView) as WebView?
        // プログレスバー
        testProgress = findViewById<View>(R.id.test_progress) as ProgressBar?
        // webViewのローディングイベント設定
        settingWebViewClient()

        // Java Scriptを有効にする
        testWebView?.getSettings()?.setJavaScriptEnabled(true)
        // WebView内に表示するURL 検索キーワード「クラゲ」
        testWebView?.loadUrl("https://twitter.com/Angels/status/1413693796554928129")

        // webViewにフォーカスセットする
        testWebView?.requestFocus()
    }

    /**
     * settingWebViewClient
     * webViewのローディングイベント設定
     */
    fun settingWebViewClient() {

        testWebView?.webViewClient = object: WebViewClient() {
            /**
             * onPageStarted
             * ローディング開始時に呼ばれる
             */
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // プログレスバー表示
                testProgress?.setVisibility(View.VISIBLE)
                // webView非表示
                testWebView?.setVisibility(View.GONE)
            }

            /**
             * onPageFinished
             * ローディング終了時に呼ばれる
             * @param view
             * @param url
             */
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // プログレスバー非表示
                testProgress?.setVisibility(View.GONE)
                // webView表示
                testWebView?.setVisibility(View.VISIBLE)
            }

            /**
             * shouldOverrideUrlLoading
             * WebViewにロードされようとしているときに呼ばれる
             * @param view
             * @param request
             * @return trueは外部ブラウザ起動 or falseはWebView内に表示する
             */
            override fun shouldOverrideUrlLoading(view: WebView?,request: WebResourceRequest): Boolean {
                // 外部ブラウザ起動する場合はtrueを返す

                if (request.url != null) {
                    // 外部ブラウザ起動
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.url.toString()))
                    startActivity(intent)
                }
                return true
            }
        }
    }

    /**
     * onClick
     * @param view
     */
    override fun onClick(view: View) {
        /*
        when (view.getId()) {
            R.id.next_button -> {
                val intent = Intent(this, Page2Activity::class.java)
                startActivity(intent)
            }
            else -> {
                println("else")
            }
        }
         */
    }
}