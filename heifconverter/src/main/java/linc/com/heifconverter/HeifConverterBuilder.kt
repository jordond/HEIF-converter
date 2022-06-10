package linc.com.heifconverter

import android.content.Context
import androidx.annotation.DrawableRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

@Suppress("MemberVisibilityCanBePrivate")
class HeifConverterBuilder internal constructor(private val context: Context) {

    private var options = HeifConverter.Options.default(context)

    fun fromFile(pathToFile: String) = apply {
        if (!File(pathToFile).exists()) {
            throw FileNotFoundException("HEIC file not found! $pathToFile")
        }

        update { copy(inputData = HeifConverter.InputData.File(pathToFile)) }
    }

    fun fromFile(file: File) = fromFile(file.absolutePath)

    fun fromInputStream(inputStream: InputStream) = apply {
        update { copy(inputData = HeifConverter.InputData.InputStream(inputStream)) }
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

        update { copy(inputData = HeifConverter.InputData.Resources(resId)) }
    }

    fun fromUrl(heicImageUrl: String) = apply {
        update { copy(inputData = HeifConverter.InputData.Url(heicImageUrl)) }
    }

    fun fromByteArray(data: ByteArray) = apply {
        if (data.isEmpty()) {
            throw FileNotFoundException("Empty byte array!")
        }

        update { copy(inputData = HeifConverter.InputData.ByteArray(data)) }
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

    fun build() = HeifConverter(context = context, options)

    @Deprecated(
        "You should really use convertBlocking or convert {}",
        ReplaceWith("convert { }"),
    )
    @Suppress("DEPRECATION")
    fun convert(): Job = build().convert()

    suspend fun convertBlocking(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    ): Map<String, Any?> = build().convertBlocking(scope)

    fun convert(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = build().convert(scope, block)

    private fun update(block: HeifConverter.Options.() -> HeifConverter.Options) {
        options = options.run(block)
    }
}