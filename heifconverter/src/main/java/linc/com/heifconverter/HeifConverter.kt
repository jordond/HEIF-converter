package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.HeifConverter.Companion.create
import linc.com.heifconverter.HeifConverter.InputDataType.None
import linc.com.heifconverter.util.Converter
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

/**
 * Convert a HEIF image from a HEIC file into a [Bitmap].
 *
 * To get started use the [create] builder function, then call one of the source
 * functions like [fromFile], [fromUrl], [fromInputStream], etc.
 *
 * @see create
 * @constructor Converter with all default options.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class HeifConverter internal constructor(private val context: Context) {

    private var options = Options.default(context)

    /**
     * Will throw an [IllegalArgumentException] if invoked before [Options.inputDataType] is set.
     */
    private val converter: Converter
        get() {
            if (options.inputDataType is None) throw IllegalArgumentException(
                "No input type was provided! Use .fromFile, fromUrl, fromInputStream, etc!",
            )

            return Converter(context = context, options)
        }

    /**
     * Set the input HEIC file from an absolute filepath on the device.
     *
     * @param[pathToFile] Absolute filepath to the image.
     * @throws FileNotFoundException if [pathToFile] doesn't point to an accessible existing file.
     */
    fun fromFile(pathToFile: String) = apply {
        if (!File(pathToFile).exists()) {
            throw FileNotFoundException("HEIC file not found! $pathToFile")
        }

        update { copy(inputDataType = InputDataType.File(pathToFile)) }
    }

    /**
     * Set the input HEIC file from a [File] object.
     *
     * @see fromFile
     * @param[file] [File] object pointing to the HEIC image.
     * @throws FileNotFoundException if [file] doesn't point to an accessible existing file.
     */
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

    /**
     * Convert the HEIC input into a [Bitmap] using coroutines to get the result synchronously.
     *
     * The results are returned in a [Map] using [HeifConverter.Key]s as keys. Access them like:
     *
     * ```
     *  val result = HeifConverter.create(this)
     *      .fromUrl("https://imagehost.com/random/image.heic")
     *      .convertBlocking()
     *
     *  // Only available if `.saveResultImage()` was used (default: true)
     *  val filePath: String? = result[HeifConverter.Key.IMAGE_PATH]
     *  val bitmap: Bitmap? = result[HeifConverter.Key.BITMAP]
     * ```
     *
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @return Result map containing the [Bitmap] and a path to the saved bitmap if [saveResultImage] is `true`.
     * @throws RuntimeException if no input file was provided, see [create].
     */
    suspend fun convertBlocking(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    ): Map<String, Any?> = converter.convertBlocking(coroutineScope)

    /**
     * Convert the HEIC input into a [Bitmap] using a callback to get the results asynchronously.
     *
     * @see convertBlocking for information on retrieving the results.
     *
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @param[block] Lambda for retrieving the results asynchronously.
     * @return Result map containing the [Bitmap] and a path to the saved bitmap if [saveResultImage] is `true`.
     * @throws RuntimeException if no input file was provided, see [create].
     */
    fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = converter.convert(coroutineScope, block)

    private fun update(block: Options.() -> Options) {
        options = options.run(block)
    }

    internal data class Options(
        val inputDataType: InputDataType = None,
        val outputQuality: Int = 100,
        val saveResultImage: Boolean = false,
        val outputFormat: String = Format.JPEG,
        val convertedFileName: String = UUID.randomUUID().toString(),
        val pathToSaveDirectory: String? = null,
    ) {

        val outputFileName = "${convertedFileName}${outputFormat}"

        companion object {

            /**
             * Create a default instance of [Options].
             *
             * @param[context] Used to get the [Options.pathToSaveDirectory].
             */
            internal fun default(context: Context) = Options(
                pathToSaveDirectory = ContextCompat.getExternalFilesDirs(
                    context,
                    Environment.DIRECTORY_DCIM
                )[0].path,
            )
        }
    }

    /**
     * The supported output formats when saving the HEIF converted Bitmap to the disk.
     */
    object Format {
        const val JPEG = ".jpg"
        const val PNG = ".png"
        const val WEBP = ".webp"
    }

    /**
     * Key accessors for getting the results from [convert] or [convertBlocking]
     */
    object Key {
        const val BITMAP = "converted_bitmap_heic"
        const val IMAGE_PATH = "path_to_converted_heic"
    }

    /**
     * Models the methods of supplying an input HEIC.
     *
     * **Note**: [None] is the default when constructing an [Options] object but an exception
     * will be thrown when trying to convert.
     */
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
        fun useContext(context: Context) = create(context)

        /**
         * Create a builder for converting a HEIF image from a HEIC file into a [Bitmap].
         *
         * To get started use the [create] builder function, then call one of the source
         * functions like [fromFile], [fromUrl], [fromInputStream], etc.
         *
         * Example:
         *
         * ```
         * HeifConverter.create(this)
         *  .fromUrl("https://imagehost.com/random/image.heic")
         *  .convert { result ->
         *      val bitmap = result[HeicConverter.Key.Bitmap] as Bitmap
         *      // ... use bitmap
         *  }
         * ```
         *
         * **Note:** You should only call a `from*` function _once_. For example if
         * you call [fromUrl] then [fromFile]. Only the last one will be used.
         *
         * ```
         * val heicFile: File = sampleSource.getImageFile()
         * HeifConverter.create(this)
         *  .fromUrl("https://imagehost.com/random/image.heic") // This will be ignored
         *  .fromFile(heicFile)
         *  // ...
         * ```
         *
         * Supported input types: [File], [ByteArray], drawable resource ID, remote URL,
         * absolute filepath, and [InputStream].
         *
         * To start the conversion process and get the [Bitmap] and/or the saved
         * file path (see [saveFileWithName]) you must invoke [convert] or [convertBlocking].
         *
         * **Warning:** If you do not call one of the input methods a [RuntimeException] will
         * be thrown when [convert] or [convertBlocking] is invoked.
         */
        fun create(context: Context) = HeifConverter(context).apply {
            HeifReader.initialize(context)
        }
    }
}