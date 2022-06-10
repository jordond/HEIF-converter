package linc.com.heifconverter.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import linc.com.heifconverter.HeifConverter
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

internal class Converter constructor(
    private val context: Context,
    private val options: HeifConverter.Options,
) {

    fun convert(): Job {
        return convert {}
    }

    /**
     * convert using coroutines to get result synchronously
     * @return map of [HeifConverter.Key] to values
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
        HeifConverter.Format.WEBP ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        HeifConverter.Format.PNG -> Bitmap.CompressFormat.PNG
        else -> Bitmap.CompressFormat.JPEG
    }

    private fun createResultMap(bitmap: Bitmap?, path: String? = null) = mapOf(
        HeifConverter.Key.BITMAP to bitmap,
        HeifConverter.Key.IMAGE_PATH to path,
    )
}
