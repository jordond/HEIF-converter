package linc.com.heifconverter.sample

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.dsl.extension.create.create

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val convert = findViewById<Button>(R.id.convert)
        val resultImage = findViewById<ImageView>(R.id.resultImage)
        val useDsl = findViewById<CheckBox>(R.id.useDsl)

        convert.setOnClickListener {
            if (useDsl.isChecked) useDsl(resultImage)
            else useBuilder(resultImage)
        }
    }

    private fun useDsl(resultImage: ImageView) {
        HeifConverter
            .create(this, URL) {
                outputFormat = HeifConverter.Format.PNG
                outputQuality(100)
                outputName("Image_Converted_Name")
                saveResultImage(true)
            }
            .convert(lifecycleScope) { result ->
                handleResult(resultImage, result)
            }
    }

    private fun useBuilder(resultImage: ImageView) {
        HeifConverter.create(this)
            .fromUrl(URL)
            .withOutputFormat(HeifConverter.Format.PNG)
            .withOutputQuality(100)
            .saveFileWithName("Image_Converted_Name")
            .saveResultImage(true)
            .convert(lifecycleScope) { result ->
                handleResult(resultImage, result)
            }
    }

    private fun handleResult(resultImage: ImageView, result: Map<String, Any?>) {
        val path = result[HeifConverter.Key.IMAGE_PATH] as String
        Log.i("MainActivity", "Saved bitmap to: $path")
        resultImage.setImageBitmap((result[HeifConverter.Key.BITMAP] as Bitmap))
    }

    companion object {

        private const val URL = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
    }
}
