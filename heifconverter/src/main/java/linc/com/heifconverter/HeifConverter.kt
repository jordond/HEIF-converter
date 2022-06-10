package linc.com.heifconverter

import android.content.Context
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.util.Converter
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class HeifConverter internal constructor(private val context: Context) {

    private var options = Options.default(context)

    private val converter: Converter
        get() = Converter(context = context, options)

    fun fromFile(pathToFile: String) = apply {
        if (!File(pathToFile).exists()) {
            throw FileNotFoundException("HEIC file not found! $pathToFile")
        }

        update { copy(inputDataType = InputDataType.File(pathToFile)) }
    }

    fun fromFile(file: File) = fromFile(file.absolutePath)

    fun fromInputStream(inputStream: InputStream) = apply {
        update { copy(inputDataType = InputDataType.InputStream(inputStream)) }
    }

    fun fromResource(@DrawableRes resId: Int) = apply {
        val isResValid = context.resources.getIdentifier(
            context.resources.getResourceName(resId),
            "drawable",
            context.packageName,
        ) != 0

        if (!isResValid) {
            throw FileNotFoundException("Resource not found!")
        }

        update { copy(inputDataType = InputDataType.Resources(resId)) }
    }

    fun fromUrl(heicImageUrl: String) = apply {
        update { copy(inputDataType = InputDataType.Url(heicImageUrl)) }
    }

    fun fromByteArray(data: ByteArray) = apply {
        if (data.isEmpty()) {
            throw FileNotFoundException("Empty byte array!")
        }

        update { copy(inputDataType = InputDataType.ByteArray(data)) }
    }

    fun withOutputFormat(format: String) = apply {
        update { copy(outputFormat = format) }
    }

    fun withOutputQuality(quality: Int) = apply {
        update {
            copy(outputQuality = quality.coerceIn(0..100))
        }
    }

    fun saveResultImage(saveResultImage: Boolean) = apply {
        update { copy(saveResultImage = saveResultImage) }
    }

    fun saveFileWithName(convertedFileName: String) = apply {
        update { copy(convertedFileName = convertedFileName) }
    }

    @Deprecated(
        "Will be added in future",
        ReplaceWith("", ""),
        DeprecationLevel.HIDDEN,
    )
    fun saveToDirectory(pathToDirectory: String) = apply {
        if (!File(pathToDirectory).exists()) {
            throw FileNotFoundException("Directory not found!")
        }

        update { copy(pathToSaveDirectory = pathToDirectory) }
    }

    @Deprecated(
        "You should really use convertBlocking or convert {}",
        ReplaceWith("convert { }"),
    )
    fun convert(): Job = converter.convert()

    suspend fun convertBlocking(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    ): Map<String, Any?> = converter.convertBlocking(scope)

    fun convert(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = converter.convert(scope, block)

    private fun update(block: Options.() -> Options) {
        options = options.run(block)
    }

    internal data class Options(
        val inputDataType: InputDataType = InputDataType.None,
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

    internal sealed class InputDataType {
        class File(val data: String) : InputDataType()
        class Url(val data: String) : InputDataType()
        class Resources(@DrawableRes val data: Int) : InputDataType()
        class InputStream(val data: java.io.InputStream) : InputDataType()
        class ByteArray(val data: kotlin.ByteArray) : InputDataType()
        object None : InputDataType()
    }

    companion object {

        @Deprecated(
            "In favour of new HeifConverter.with()",
            ReplaceWith("HeifConverter.with(context)"),
        )
        fun useContext(context: Context) = with(context)


        // default pathToSaveDirectory:
        fun with(context: Context) = HeifConverter(context).apply {
            HeifReader.initialize(context)
        }
    }
}