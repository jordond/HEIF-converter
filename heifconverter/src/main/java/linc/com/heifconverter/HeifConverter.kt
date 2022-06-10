package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import linc.com.heifconverter.util.createBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.resume

class HeifConverter internal constructor(
    private val context: Context,
    private val options: Options,
) {

    @Deprecated(
        "You should really use convertBlocking or convert {}",
        ReplaceWith("convert { }"),
    )
    fun convert(): Job {
        return convert {}
    }

    /**
     * convert using coroutines to get result synchronously
     * @return map of [Key] to values
     */
    suspend fun convertBlocking(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    ): Map<String, Any?> = suspendCancellableCoroutine { cont ->
        val job = convert(scope) { result -> cont.resume(result) }

        cont.invokeOnCancellation { job.cancel() }
    }

    fun convert(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = scope.launch(Dispatchers.Main) {
        val bitmap = withContext(Dispatchers.IO) {
            options.inputData.createBitmap(context)
        } ?: return@launch block(createResultMap(null))

        // Return early if we don't need to save the bitmap
        if (!options.saveResultImage || options.pathToSaveDirectory == null) {
            return@launch block(createResultMap(bitmap))
        }

        // Figure out where to save it
        val directoryToSave = File(options.pathToSaveDirectory)
        val outputFile = File(directoryToSave, options.outputFileName)

        // Save the bitmap and trigger callback
        val savedFilePath = bitmap.saveToFile(outputFile)
        block(createResultMap(bitmap, savedFilePath))
    }

    private suspend fun Bitmap.saveToFile(outputFile: File): String = withContext(Dispatchers.IO) {
        val result = runCatching {
            FileOutputStream(outputFile).use { outputStream ->
                val format = useFormat(options.outputFormat)
                compress(format, options.outputQuality, outputStream)
            }
        }

        val saved = result.getOrThrow()
        if (saved) outputFile.absolutePath
        else throw RuntimeException("Unable to save bitmap to ${outputFile.absolutePath}")
    }

    private fun useFormat(format: String) = when (format) {
        Format.WEBP ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        Format.PNG -> Bitmap.CompressFormat.PNG
        else -> Bitmap.CompressFormat.JPEG
    }

    private fun createResultMap(bitmap: Bitmap?, path: String? = null) = mapOf(
        Key.BITMAP to bitmap,
        Key.IMAGE_PATH to path,
    )

    internal data class Options(
        val inputData: InputData = InputData.None,
        val outputQuality: Int = 100,
        val saveResultImage: Boolean = false,
        val outputFormat: String = Format.JPEG,
        val convertedFileName: String = UUID.randomUUID().toString(),
        val pathToSaveDirectory: String? = null,
    ) {

        val outputFileName = "${convertedFileName}${outputFormat}"

        companion object {

            internal fun default(context: Context) = Options(
                pathToSaveDirectory = ContextCompat.getExternalFilesDirs(
                    context,
                    Environment.DIRECTORY_DCIM
                )[0].path,
            )
        }
    }

    object Format {
        const val JPEG = ".jpg"
        const val PNG = ".png"
        const val WEBP = ".webp"
    }

    object Key {
        const val BITMAP = "converted_bitmap_heic"
        const val IMAGE_PATH = "path_to_converted_heic"
    }

    internal sealed class InputData(val type: InputDataType) {
        class File(val data: String) : InputData(InputDataType.FILE)
        class Url(val data: String) : InputData(InputDataType.URL)
        class Resources(@DrawableRes val data: Int) : InputData(InputDataType.RESOURCES)
        class InputStream(val data: java.io.InputStream) : InputData(InputDataType.INPUT_STREAM)
        class ByteArray(val data: kotlin.ByteArray) : InputData(InputDataType.BYTE_ARRAY)
        object None : InputData(InputDataType.NONE)
    }

    internal enum class InputDataType {
        FILE,
        URL,
        RESOURCES,
        INPUT_STREAM,
        BYTE_ARRAY,
        NONE,
    }

    companion object {

        @Deprecated(
            "In favour of new HeifConverter.with()",
            ReplaceWith("HeifConverter.with(context)"),
        )
        fun useContext(context: Context) = with(context)


        // default pathToSaveDirectory:
        fun with(context: Context) = HeifConverterBuilder(context).apply {
            HeifReader.initialize(context)
        }
    }
}
