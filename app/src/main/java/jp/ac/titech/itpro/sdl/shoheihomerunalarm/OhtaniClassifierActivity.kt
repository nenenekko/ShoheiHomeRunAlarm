package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import jp.ac.titech.itpro.sdl.shoheihomerunalarm.ml.MobilenetV2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_ohtani_classifier.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class OhtaniClassifierActivity: AppCompatActivity() {
    companion object {
        private const val GALLERY_REQUEST_CODE = 100
    }

    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ohtani_classifier)

        // RUN ボタンクリックで推論結果を表示
        inferrenceBtn.setOnClickListener {
            if (bitmap == null) return@setOnClickListener
            lifecycleScope.launch {
                // 推論結果を取得
                val output = classifyImage(bitmap!!)
                if(output){
                    resultTextView.text = "あなたは大谷翔平ですね！？！？"
                }
                else{
                    resultTextView.text = "大谷翔平！！ \n ...ではなさそう．．"
                }
            }
        }

        // ギャラリーから画像を取得
        showGalleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("image/*")
            }
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        // テストデータの入力
        inputTestDataBtn.setOnClickListener {
            val notification = Intent(applicationContext, MainActivity::class.java)
            //画面起動に必要
            notification.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext!!.startActivity(notification)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                // ギャラリーから画像データの取得
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data ?: return)
                previewImg.setImageBitmap(bitmap)
            }
        }
    }

    /**
     * モデルをロードして画像分類を行う
     */
    private suspend fun classifyImage(targetBitmap: Bitmap) = withContext(Dispatchers.IO) {
        val model = MobilenetV2.newInstance(this@OhtaniClassifierActivity)
        val image = TensorImage.fromBitmap(targetBitmap)
        val output = model.process(image)  // 推論
        val categoryList = output.probabilityAsCategoryList
        model.close()
        return@withContext categoryList[0].score >= 0.5
    }

    // テスト画像の読み込み
    private fun getTestImgData(): Bitmap {
        //val randomInt = (1..11).random()
        //val fileName = "test$randomInt.jpg"
        val fileName = "test1.jpg"
        return BitmapFactory.decodeStream(assets.open(fileName))
    }
}