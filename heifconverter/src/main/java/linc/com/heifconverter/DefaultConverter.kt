package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import linc.com.heifconverter.HeifConverter.Format
import linc.com.heifconverter.HeifConverter.Key
import linc.com.heifconverter.HeifConverter.Options
import linc.com.heifconverter.decoder.HeicDecoder
import linc.com.heifconverter.decoder.decode
import linc.com.heifconverter.decoder.default
import java.io.File
import java.io.FileOutputStream

internal class DefaultConverter(
    private val context: Context,
    private val options: Options,
) : HeifConverter.Converter {

    /**
     * @see HeifConverter.Converter.convert
     */
    override suspend fun convert(): Map<String, Any?> {
        val bitmap = withContext(Dispatchers.IO) {
            val heicDecoder: HeicDecoder = options.decoder ?: HeicDecoder.default(context)
            heicDecoder.decode(input = options.input, urlLoader = options.urlLoader)
        } ?: return createResultMap(null)

        // Return early if we don't need to save the bitmap
        if (!options.saveResultImage || options.pathToSaveDirectory == null) {
            return createResultMap(bitmap)
        }

        // Figure out where to save it
        val outputFile = File(options.pathToSaveDirectory, options.outputFileNameWithFormat)

        // Save the bitmap and trigger callback
        val savedFilePath = bitmap.saveToFile(outputFile)
        return createResultMap(bitmap, savedFilePath)
    }

    /**
     * @see HeifConverter.Converter.convert
     */
    override fun convert(
        coroutineScope: CoroutineScope,
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = coroutineScope.launch(Dispatchers.Main) {
        val result = convert()
        block(result)
    }

    private fun createResultMap(bitmap: Bitmap?, path: String? = null) = mapOf(
        Key.BITMAP to bitmap,
        Key.IMAGE_PATH to path,
    )

    /**
     * Attempt to save the [Bitmap] to the device.
     *
     * @throws RuntimeException if file was not saved.
     */
    private suspend fun Bitmap.saveToFile(outputFile: File): String = withContext(Dispatchers.IO) {
        val quality = options.outputQuality.coerceIn(0..100)

        val result = runCatching {
            FileOutputStream(outputFile).use { outputStream ->
                val format = useFormat(options.outputFormat)
                compress(format, quality, outputStream)
            }
        }

        val saved = result.getOrThrow()
        if (saved) outputFile.absolutePath
        else throw RuntimeException("Unable to save bitmap to ${outputFile.absolutePath}")
    }

    /**
     * Determine which [Bitmap.CompressFormat] to use based on the type of image we're saving.
     *
     * **Note:** On Android R and higher a lossy WEBP is used.
     */
    private fun useFormat(format: Format) = when (format) {
        Format.WEBP ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        Format.PNG -> Bitmap.CompressFormat.PNG
        else -> Bitmap.CompressFormat.JPEG
    }
}
