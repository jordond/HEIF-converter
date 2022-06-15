package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import linc.com.heifconverter.HeifConverter.Companion.create
import linc.com.heifconverter.HeifConverter.Format
import linc.com.heifconverter.HeifConverter.Input
import linc.com.heifconverter.HeifConverter.Key
import linc.com.heifconverter.HeifConverter.Options
import linc.com.heifconverter.decoder.HeicDecoder
import linc.com.heifconverter.decoder.ModernHeicDecoder
import linc.com.heifconverter.decoder.legacy.LegacyHeicDecoder
import java.io.File
import java.io.FileOutputStream

internal class Converter constructor(
    private val context: Context,
    private val options: Options,
) {

    /**
     * Convert the HEIC image to a [Bitmap] synchronously.
     *
     * @return Result map containing the [Bitmap] and a path to the saved bitmap..
     * @throws RuntimeException if no input file was provided, see [create].
     */
    suspend fun convert(): Map<String, Any?> {
        val bitmap = withContext(Dispatchers.IO) {
            options.input.createBitmap(context)
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
     * Convert the HEIC image to a [Bitmap] asynchronously.
     *
     * @see convert
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @param[block] Lambda for retrieving the results asynchronously.
     * @return Result map containing the [Bitmap] and a path to the saved bitmap.
     * @throws RuntimeException if no input file was provided, see [create].
     */
    fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = coroutineScope.launch(Dispatchers.Main) {
        val result = convert()
        block(result)
    }

    /**
     * Create the [Bitmap] using a [HeicDecoder] based on the Android OS level.
     */
    private suspend fun Input.createBitmap(context: Context): Bitmap? {
        val heicDecoder: HeicDecoder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ModernHeicDecoder(context)
            else LegacyHeicDecoder(context)

        return when (this) {
            is Input.ByteArray -> heicDecoder.fromByteArray(data)
            is Input.File -> heicDecoder.fromFile(data)
            is Input.InputStream -> heicDecoder.fromInputStream(data)
            is Input.Resources -> heicDecoder.fromResources(data)
            is Input.Url -> heicDecoder.fromUrl(data)
            else -> throw IllegalStateException(
                "You forget to pass input type: File, Url etc. Use such functions: fromFile() etc."
            )
        }
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
