package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.HeifConverter.Companion.create
import linc.com.heifconverter.HeifConverter.InputDataType.None
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
     * Use an absolute file path for loading the HEIC file.
     *
     * @param[pathToFile] Absolute filepath to the image.
     * @throws FileNotFoundException if [pathToFile] doesn't point to an accessible existing file.
     */
    fun fromFile(pathToFile: String) = fromFile(File(pathToFile))

    /**
     * Use a [File] object for loading the HEIC file.
     *
     * @see fromFile
     * @param[file] [File] object pointing to the HEIC image.
     * @throws FileNotFoundException if [file] doesn't point to an accessible existing file.
     */
    fun fromFile(file: File) = apply {
        if (!file.exists()) {
            throw FileNotFoundException("HEIC file not found! ${file.absolutePath}")
        }

        updateOptions { copy(inputDataType = InputDataType.File(file)) }
    }

    /**
     * Use an [InputStream] for loading the HEIC file.
     *
     * @param[inputStream] [InputStream] for HEIC image.
     */
    fun fromInputStream(inputStream: InputStream) = apply {
        updateOptions { copy(inputDataType = InputDataType.InputStream(inputStream)) }
    }

    /**
     * Load the HEIC input from the app's resources.
     *
     * @param[resId] A [DrawableRes] ID for your HEIC image.
     * @throws FileNotFoundException if the [resId] is not valid.
     */
    fun fromResource(@DrawableRes resId: Int) = apply {
        val isResValid = context.resources.getIdentifier(
            context.resources.getResourceName(resId),
            "drawable",
            context.packageName,
        ) != 0

        if (!isResValid) {
            throw FileNotFoundException("Resource not found!")
        }

        updateOptions { copy(inputDataType = InputDataType.Resources(resId)) }
    }

    /**
     * Set the input HEIC file to a URL.
     *
     * This URL will be used to download the HEIC then convert it.
     *
     * @param[heicImageUrl] URL pointing to an HEIC file.
     */
    fun fromUrl(heicImageUrl: String) = apply {
        updateOptions { copy(inputDataType = InputDataType.Url(heicImageUrl)) }
    }

    /**
     * Use a [ByteArray] as the source for conversion.
     *
     * @param[data] [ByteArray] containing the HEIC data.
     * @throws FileNotFoundException if [data] is empty.
     */
    fun fromByteArray(data: ByteArray) = apply {
        if (data.isEmpty()) {
            throw FileNotFoundException("Empty byte array!")
        }

        updateOptions { copy(inputDataType = InputDataType.ByteArray(data)) }
    }

    /**
     * Save the converted [Bitmap] to the disk. Default: true.
     *
     * **Note:** By default this is saved in the devices DCIM folder.
     *
     * @param[saveResultImage] Whether or not to save the bitmap to the disk
     */
    fun saveResultImage(saveResultImage: Boolean) = apply {
        updateOptions { copy(saveResultImage = saveResultImage) }
    }

    /**
     * Set output image for when saving the result [Bitmap] to the disk. Default: [Format.JPEG].
     *
     * **Note:** This is unused if [saveResultImage] is passed `false`
     *
     * @param[format] The [Format] to use for the output.
     * @see saveResultImage
     */
    fun withOutputFormat(format: Format) = apply {
        updateOptions { copy(outputFormat = format) }
    }

    /**
     * Set output image for when saving the result [Bitmap] to the disk. Default: [Format.JPEG]
     *
     * @param[format] The output format to use. **Note:** if [format] does not match any [Format]
     * then [Format.JPEG] will be used as a default.
     * @see saveResultImage
     */
    fun withOutputFormat(format: String) = withOutputFormat(Format.fromString(format))

    /**
     * Set the quality of the saved file. Default: 100
     *
     * **Note:** [quality] will be clamped between 0 and 100.
     *
     * @param[quality] A quality value between 0 and 100.
     * @see saveResultImage
     */
    fun withOutputQuality(@IntRange(from = 0, to = 100) quality: Int) = apply {
        updateOptions {
            copy(outputQuality = quality.coerceIn(0..100))
        }
    }

    /**
     * Set the filename of the saved file. Default: A random UUID
     *
     * @param[convertedFileName] Filename to use for saving the result.
     * @see saveResultImage
     */
    fun saveFileWithName(convertedFileName: String) = apply {
        updateOptions { copy(convertedFileName = convertedFileName) }
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

        updateOptions { copy(pathToSaveDirectory = pathToDirectory) }
    }

    /**
     * Convert the HEIC input into a [Bitmap].
     *
     * There is no way of tracking the saved file or accessing the generated [Bitmap]. For that
     * reason this method is deprecated and you should use [convertBlocking] or [convert].
     *
     * @return The [Job] used to launch the conversion coroutine.
     * @see convert for asynchronous conversion.
     * @see convertBlocking for synchronous conversion.
     */
    @Deprecated(
        "You should really use convertBlocking or convert {}",
        ReplaceWith("convert { }"),
    )
    fun convert(): Job = converter.convert {}

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
     * @return Result map containing the [Bitmap] and a path to the saved bitmap if [saveResultImage] is `true`.
     * @throws RuntimeException if no input file was provided, see [create].
     */
    suspend fun convertBlocking(): Map<String, Any?> = converter.convertBlocking()

    /**
     * Convert the HEIC input into a [Bitmap] using a callback to get the results asynchronously.
     *
     * @see convertBlocking for information on retrieving the results.
     *
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @param[block] Lambda for retrieving the results asynchronously.
     * @return The [Job] used to launch the conversion coroutine.
     * @throws RuntimeException if no input file was provided, see [create].
     */
    fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = converter.convert(coroutineScope, block)

    private fun updateOptions(block: Options.() -> Options) {
        options = options.run(block)
    }

    /**
     * A model for representing all the available options for [HeifConverter].
     */
    internal data class Options(
        val inputDataType: InputDataType = None,
        val outputQuality: Int = 100,
        val saveResultImage: Boolean = true,
        val outputFormat: Format = Format.JPEG,
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
    sealed class Format(val extension: String) {
        object JPEG : Format(".jpg")
        object PNG : Format(".png")
        object WEBP : Format(".webp")

        companion object {

            fun fromString(value: String): Format = when (value) {
                JPEG.extension -> JPEG
                PNG.extension -> PNG
                WEBP.extension -> WEBP
                else -> JPEG
            }
        }
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
        class File(val data: java.io.File) : InputDataType()
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
        fun create(context: Context) = HeifConverter(context)
    }
}