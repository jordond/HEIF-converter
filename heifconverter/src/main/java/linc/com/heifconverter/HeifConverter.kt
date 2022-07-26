package linc.com.heifconverter

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.annotation.IntRange
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.HeifConverter.Companion.create
import linc.com.heifconverter.HeifConverter.Input.None
import linc.com.heifconverter.decoder.HeicDecoder
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
public class HeifConverter internal constructor(
    private val context: Context,
    private var options: Options,
) {

    /**
     * Use an absolute file path for loading the HEIC file.
     *
     * @param[pathToFile] Absolute filepath to the image.
     * @throws FileNotFoundException if [pathToFile] doesn't point to an accessible existing file.
     */
    public fun fromFile(pathToFile: String): HeifConverter = fromFile(File(pathToFile))

    /**
     * Use a [File] object for loading the HEIC file.
     *
     * @see fromFile
     * @param[file] [File] object pointing to the HEIC image.
     * @throws FileNotFoundException if [file] doesn't point to an accessible existing file.
     */
    public fun fromFile(file: File): HeifConverter = apply {
        if (!file.exists()) {
            throw FileNotFoundException("HEIC file not found! ${file.absolutePath}")
        }

        updateOptions { copy(input = Input.File(file)) }
    }

    /**
     * Use an [InputStream] for loading the HEIC file.
     *
     * This method is deprecated because passing around [InputStream]s is inherently dangerous and
     * prone to memory leaks. Instead use another one of the input methods.
     *
     * @param[inputStream] [InputStream] for HEIC image.
     */
    @Deprecated("It is dangerous to pass around a InputStream, use one of the other input sources.")
    public fun fromInputStream(inputStream: InputStream): HeifConverter = apply {
        updateOptions { copy(input = Input.InputStream(inputStream)) }
    }

    /**
     * Load the HEIC input from the app's resources.
     *
     * @param[resId] A [RawRes] ID for your HEIC image.
     * @throws FileNotFoundException if the [resId] is not valid.
     */
    public fun fromResource(@RawRes resId: Int): HeifConverter = apply {
        val isResValid = context.resources.getIdentifier(
            context.resources.getResourceName(resId),
            "drawable",
            context.packageName,
        ) != 0

        if (!isResValid) {
            throw FileNotFoundException("Resource not found!")
        }

        updateOptions { copy(input = Input.Resources(resId)) }
    }

    /**
     * Set the input HEIC file to a URL.
     *
     * This URL will be used to download the HEIC then convert it.
     *
     * @param[heicImageUrl] URL pointing to an HEIC file.
     */
    public fun fromUrl(heicImageUrl: String): HeifConverter = apply {
        updateOptions { copy(input = Input.Url(heicImageUrl)) }
    }

    /**
     * Use a [ByteArray] as the source for conversion.
     *
     * @param[data] [ByteArray] containing the HEIC data.
     * @throws FileNotFoundException if [data] is empty.
     */
    public fun fromByteArray(data: ByteArray): HeifConverter = apply {
        if (data.isEmpty()) {
            throw FileNotFoundException("Empty byte array!")
        }

        updateOptions { copy(input = Input.ByteArray(data)) }
    }

    /**
     * Save the converted [Bitmap] to the disk. Default: true.
     *
     * **Note:** By default this is saved in the devices DCIM folder.
     *
     * @param[saveResultImage] Whether or not to save the bitmap to the disk
     */
    public fun saveResultImage(saveResultImage: Boolean): HeifConverter = apply {
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
    public fun withOutputFormat(format: Format): HeifConverter = apply {
        updateOptions { copy(outputFormat = format) }
    }

    /**
     * Set output image for when saving the result [Bitmap] to the disk. Default: [Format.JPEG]
     *
     * @param[format] The output format to use. **Note:** if [format] does not match any [Format]
     * then [Format.JPEG] will be used as a default.
     * @see saveResultImage
     */
    @Deprecated("In favour of explicit format objects", ReplaceWith("withOutputFormat()"))
    public fun withOutputFormat(format: String): HeifConverter =
        withOutputFormat(Format.fromString(format))

    /**
     * Set the quality of the saved file. Default: 100
     *
     * **Note:** [quality] will be clamped between 0 and 100.
     *
     * @param[quality] A quality value between 0 and 100.
     * @see saveResultImage
     */
    public fun withOutputQuality(
        @IntRange(from = 0, to = 100) quality: Int,
    ): HeifConverter = apply {
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
    public fun saveFileWithName(convertedFileName: String): HeifConverter = apply {
        updateOptions { copy(outputFileName = convertedFileName) }
    }

    /**
     * Set the output directory of the saved file using a [File] reference. Default: DCIM folder
     *
     * @param[directory] A [File] reference to the directory you want the image to be saved to.
     * @throws FileNotFoundException if the [File] does not exist.
     * @throws IllegalArgumentException if [File] is not a directory.
     */
    public fun saveToDirectory(directory: File): HeifConverter = apply {
        if (!directory.exists()) throw FileNotFoundException("Directory not found!")
        else if (!directory.isDirectory) {
            throw IllegalArgumentException("${directory.absolutePath} is not a directory!")
        }

        updateOptions { copy(pathToSaveDirectory = directory) }
    }

    /**
     * Set the output directory of the saved file using an absolute path. Default: DCIM folder
     *
     * Uses [saveToDirectory] to convert it to a [File] object.
     *
     * @param[pathToDirectory] An absolute filepath to a directory to save the image to.
     * @throws FileNotFoundException if the created [File] does not exist.
     * @throws IllegalArgumentException if created [File] is not a directory.
     */
    public fun saveToDirectory(pathToDirectory: String): HeifConverter =
        saveToDirectory(File(pathToDirectory))

    /**
     * Set a custom [HeicDecoder] for decoding the HEIC [Input] to a [Bitmap]. Default: [HeicDecoder.Default]
     *
     * @param[heicDecoder] A custom [HeicDecoder] implementation for decoding HEIC. If `null` then
     * the [HeicDecoder.Default] will be used.
     */
    public fun withCustomDecoder(heicDecoder: HeicDecoder?): HeifConverter = apply {
        updateOptions { copy(decoder = heicDecoder) }
    }

    /**
     * A custom [HeicDecoder.UrlLoader] for downloading the URL from [HeifConverter.fromUrl].
     *
     * If set to `null` then a default implementation will be used.
     *
     * @param[urlLoader] A custom [HeicDecoder.UrlLoader] for downloading the [Input.Url].
     */
    public fun withUrlLoader(urlLoader: HeicDecoder.UrlLoader?): HeifConverter = apply {
        updateOptions { copy(urlLoader = urlLoader) }
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
    @Deprecated("You should really use convertBlocking or convert {}", ReplaceWith("convert { }"))
    public fun convert(): Job = converter().convert {}

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
     * @throws IllegalStateException if no input file was provided, see [create].
     */
    public suspend fun convertBlocking(): Map<String, Any?> = converter().convert()

    /**
     * Convert the HEIC input into a [Bitmap] using a callback to get the results asynchronously.
     *
     * @see convertBlocking for information on retrieving the results.
     *
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @param[block] Lambda for retrieving the results asynchronously.
     * @return The [Job] used to launch the conversion coroutine.
     * @throws IllegalStateException if no input file was provided, see [create].
     */
    public fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (result: Map<String, Any?>) -> Unit,
    ): Job = converter().convert(coroutineScope, block)

    private fun updateOptions(block: Options.() -> Options) {
        options = options.run(block)
    }

    /**
     * Will throw an [IllegalArgumentException] if invoked before [Options.input] is set.
     */
    private fun converter(): Converter {
        if (options.input is None) throw IllegalArgumentException(
            "No input type was provided! Use .fromFile, fromUrl, fromInputStream, etc!",
        )

        if (options.saveResultImage && options.pathToSaveDirectory == null) {
            updateOptions { copy(pathToSaveDirectory = Options.defaultOutputPath(context)) }
        }

        return DefaultConverter(context, options)
    }

    /**
     * A model for representing all the available options for [HeifConverter].
     *
     * @property[input] The [Input] source for the HEIC data. See [fromFile], [fromUrl], etc.
     * @property[saveResultImage] Save the converted [Bitmap] to the device. See [saveResultImage].
     * @property[outputQuality] The quality of the saved image. See [withOutputQuality].
     * @property[outputFormat] Format of the saved image. See [withOutputFormat].
     * @property[outputFileName] The file name for the saved image. See [saveFileWithName].
     * @property[pathToSaveDirectory] The folder to save converted image to. See [saveToDirectory].
     * @property[decoder] A custom decoder for converting a HEIC [Input] to a [Bitmap].
     * @property[urlLoader] A custom url loader for downloading the [Input.Url].
     */
    public data class Options constructor(
        val input: Input = None,
        val saveResultImage: Boolean = true,
        val outputQuality: Int = 100,
        val outputFormat: Format = Format.JPEG,
        val outputFileName: String = UUID.randomUUID().toString(),
        val pathToSaveDirectory: File? = null,
        val decoder: HeicDecoder? = null,
        val urlLoader: HeicDecoder.UrlLoader? = null,
    ) {

        internal val outputFileNameWithFormat = "${outputFileName}${outputFormat.extension}"

        public companion object {

            /**
             * Get a [File] reference for the default output path.
             */
            public fun defaultOutputPath(context: Context): File? {
                val path = ContextCompat.getExternalFilesDirs(
                    context,
                    Environment.DIRECTORY_DCIM
                )[0].path ?: return null

                return File(path)
            }
        }
    }

    /**
     * The supported output formats when saving the HEIF converted Bitmap to the disk.
     */
    public sealed class Format(public val extension: String) {
        public object JPEG : Format(".jpg")
        public object PNG : Format(".png")
        public object WEBP : Format(".webp")

        internal companion object {

            /**
             * Map a [String] value to a [Format] object, default to [JPEG] if the string
             * is not valid.
             *
             * @param[value] Format string to map to [Format]
             * @return Mapped [Format] value or [Format.JPEG] if [value] is invalid.
             */
            internal fun fromString(value: String): Format = when (value) {
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
    public object Key {
        public const val BITMAP: String = "converted_bitmap_heic"
        public const val IMAGE_PATH: String = "path_to_converted_heic"
    }

    /**
     * Models the methods of supplying an input HEIC.
     *
     * **Note**: [None] is the default when constructing an [Options] object but an exception
     * will be thrown when trying to convert.
     */
    public sealed class Input {
        public class File(public val data: java.io.File) : Input()
        public class Url(public val data: String) : Input()
        public class Resources(@RawRes public val data: Int) : Input()
        public class InputStream(public val data: java.io.InputStream) : Input()
        public class ByteArray(public val data: kotlin.ByteArray) : Input()
        public class Uri(public val data: android.net.Uri) : Input()
        public object None : Input()

        /**
         * A stub so extension functions can be created in `heifconverter-dsl` module
         */
        public companion object
    }

    internal interface Converter {

        /**
         * Convert the HEIC image to a [Bitmap] synchronously.
         *
         * @return Result map containing the [Bitmap] and a path to the saved bitmap..
         * @throws RuntimeException if no input file was provided, see [create].
         */
        suspend fun convert(): Map<String, Any?>

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
        ): Job
    }

    public companion object {

        /**
         * Convert a HEIF image from a HEIC file into a [Bitmap].
         *
         * @see create for more information.
         */
        @Deprecated(
            "In favour of new HeifConverter.with()",
            ReplaceWith("HeifConverter.with(context)"),
        )
        public fun useContext(context: Context): HeifConverter = create(context)

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
         * Supported input types: [File], [ByteArray], raw resource ID, remote URL,
         * absolute filepath, and [InputStream].
         *
         * To start the conversion process and get the [Bitmap] and/or the saved
         * file path (see [saveFileWithName]) you must invoke [convert] or [convertBlocking].
         *
         * **Warning:** If you do not call one of the input methods a [IllegalStateException] will
         * be thrown when [convert] or [convertBlocking] is invoked.
         */
        public fun create(
            context: Context,
            options: Options = Options(),
        ): HeifConverter = HeifConverter(context, options)
    }
}