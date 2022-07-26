package linc.com.heifconverter.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import linc.com.heifconverter.decoder.OkHttpUrlLoader
import linc.com.heifconverter.decoder.glide.GlideHeicDecoder
import linc.com.heifconverter.dsl.HeifConverterResult
import linc.com.heifconverter.dsl.extension.create
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : AppCompatActivity() {

    private val optionUrl by lazy { findViewById<RadioButton>(R.id.optionUrl) }
    private val optionResource by lazy { findViewById<RadioButton>(R.id.optionResource) }
    private val progress by lazy { findViewById<ProgressBar>(R.id.progress) }
    private val chooseFile by lazy { findViewById<Button>(R.id.chooseFile) }
    private val convert by lazy { findViewById<Button>(R.id.convert) }
    private val useGlide by lazy { findViewById<CheckBox>(R.id.useGlide) }
    private val useOkHttp by lazy { findViewById<CheckBox>(R.id.useOkHttp) }
    private val resultImage by lazy { findViewById<ImageView>(R.id.resultImage) }

    // Create a custom OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        )
        .build()

    // Pass in the custom OkHttpClient and also customize the Request
    private val okHttpUrlLoader = OkHttpUrlLoader(okHttpClient) {
        header("Foo", "Bar")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val useDsl = findViewById<CheckBox>(R.id.useDsl)

        progress.visibility = View.GONE

        chooseFile.setOnClickListener {
            chooseHeic()
        }

        convert.setOnClickListener {
            start()
            if (useDsl.isChecked) useDsl()
            else useBuilder()
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

    private fun useDsl(source: HeifConverter.Input? = null) {
        HeifConverter
            .create(this, source ?: getSource()) {
                outputFormat = HeifConverter.Format.PNG
                outputQuality(100)
                outputName("Image_Converted_Name")
                saveResultImage(true)
                if (useGlide.isChecked) {
                    customDecoder(GlideHeicDecoder(context = this@MainActivity))
                }
                if (useOkHttp.isChecked) {
                    urlLoader(okHttpUrlLoader)
                }
            }
            .convert(lifecycleScope) { result ->
                stop()
                handleResult(resultImage, result)
            }
    }

    private fun useBuilder() {
        HeifConverter.create(this)
            .apply {
                when (val source = getSource()) {
                    is HeifConverter.Input.Resources -> fromResource(source.data)
                    is HeifConverter.Input.Url -> fromUrl(source.data)
                    else -> throw IllegalStateException("Not supported")
                }

                if (useGlide.isChecked) {
                    withCustomDecoder(GlideHeicDecoder(context = this@MainActivity))
                }

                if (useOkHttp.isChecked) {
                    withUrlLoader(okHttpUrlLoader)
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


    private fun chooseHeic() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/heic"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/heic", "image/heif"))
        }

        startActivityForResult(intent, REQUEST_HEIC_GET)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == REQUEST_HEIC_GET && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data.data ?: return
            val input = HeifConverter.Input.Uri(uri)
            useDsl(input)

            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {

        const val REQUEST_HEIC_GET = 1

        private const val URL = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
    }
}
