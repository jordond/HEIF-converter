package linc.com.heifconverter.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.decoder.glide.GlideHeicDecoder
import linc.com.heifconverter.dsl.HeifConverterResult
import linc.com.heifconverter.dsl.extension.create

class MainActivity : AppCompatActivity() {

    private val optionUrl by lazy { findViewById<RadioButton>(R.id.optionUrl) }
    private val optionResource by lazy { findViewById<RadioButton>(R.id.optionResource) }
    private val progress by lazy { findViewById<ProgressBar>(R.id.progress) }
    private val convert by lazy { findViewById<Button>(R.id.convert) }
    private val useGlide by lazy { findViewById<CheckBox>(R.id.useGlide) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val resultImage = findViewById<ImageView>(R.id.resultImage)
        val useDsl = findViewById<CheckBox>(R.id.useDsl)

        progress.visibility = View.GONE

        convert.setOnClickListener {
            start()
            if (useDsl.isChecked) useDsl(resultImage)
            else useBuilder(resultImage)
        }
    }

    private fun start() {
        convert.isEnabled = false
        progress.visibility = View.VISIBLE
    }

    private fun stop() {
        convert.isEnabled = true
        progress.visibility = View.GONE
    }

    private fun useDsl(resultImage: ImageView) {
        HeifConverter
            .create(this, getSource()) {
                outputFormat = HeifConverter.Format.PNG
                outputQuality(100)
                outputName("Image_Converted_Name")
                saveResultImage(true)
                if (useGlide.isChecked) {
                    customDecoder(GlideHeicDecoder(context = this@MainActivity))
                }
            }
            .convert(lifecycleScope) { result ->
                stop()
                handleResult(resultImage, result)
            }
    }

    private fun useBuilder(resultImage: ImageView) {
        HeifConverter.create(this)
            .apply {
                when (val source = getSource()) {
                    is HeifConverter.Input.Resources -> fromResource(source.data)
                    is HeifConverter.Input.Url -> fromUrl(source.data)
                    else -> throw IllegalStateException("Not supported")
                }

                if (useGlide.isChecked) {
                    setCustomDecoder(GlideHeicDecoder(context = this@MainActivity))
                }
            }
            .withOutputFormat(HeifConverter.Format.PNG)
            .withOutputQuality(100)
            .saveFileWithName("Image_Converted_Name")
            .saveResultImage(true)
            .convert(lifecycleScope) { result ->
                stop()
                handleResult(resultImage, HeifConverterResult.parse(result))
            }
    }

    private fun getSource(): HeifConverter.Input = when {
        optionUrl.isChecked -> HeifConverter.Input.Url(URL)
        optionResource.isChecked -> HeifConverter.Input.Resources(R.raw.sample)
        else -> throw IllegalStateException("Not supported")
    }

    private fun handleResult(resultImage: ImageView, result: HeifConverterResult) {
        if (result.imagePath != null) {
            Log.i("MainActivity", "Saved bitmap to: ${result.imagePath}")
        }

        val bitmap = result.bitmap ?: return
        val bytes = bitmap.allocationByteCount / 1e+6
        val message = "Bitmap size: ${bytes}mb"
        Log.i("MainActivity", message)

        if (bitmap.height > 2000 || bitmap.width > 2000) {
            // If the source HEIC is 4K, the inflated Bitmap will be very large.
            val scaled = bitmap.scale(bitmap.width / 4, bitmap.height / 4)
            resultImage.setImageBitmap(scaled)
        } else {
            resultImage.setImageBitmap(bitmap)
        }
    }

    companion object {

        private const val URL = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
    }
}
