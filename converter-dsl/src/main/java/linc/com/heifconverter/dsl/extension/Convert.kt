package linc.com.heifconverter.dsl.extension

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RawRes
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.dsl.HeifConverterDsl
import linc.com.heifconverter.dsl.HeifConverterResult
import java.io.File
import java.io.InputStream

/**
 * A DSL builder for converting a HEIC from [File] to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * val heicFile = File(context.cacheDir, "image.heic")
 * val result = HeifConverter.convert(context, heicFile) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val heicFile = File(context.cacheDir, "image.heic")
 * val result = HeifConverter.convert(context, heicFile, options)
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[file] Input HEIC [File].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    file: File,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, file, options, block).convert()

/**
 * A DSL builder for converting a HEIC from [File] to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * File(context.cacheDir, "image.heic").inputStream().use { inputStream ->
 *     val result = HeifConverter.convert(context, inputStream) {
 *         saveResultImage = true
 *         outputName = "image"
 *         outputDirectory = File(context.cacheDir)
 *         outputFormat = HeifConverter.Format.JPEG
 *     }
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * File(context.cacheDir, "image.heic").inputStream().use { inputStream ->
 *     val result = HeifConverter.convert(context, inputStream, options)
 * }
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[inputStream] Input HEIC as an [InputStream].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("It is dangerous to pass around a InputStream, use one of the other input sources.")
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    inputStream: InputStream,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, inputStream, options, block).convert()

/**
 * A DSL builder for converting a HEIC from [RawRes] resource ID [Int] to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * val result = HeifConverter.convert(context, R.raw.heic_image) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val result = HeifConverter.convert(context, R.raw.heic_image, options)
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[resId] Input HEIC resource id [Int].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    @RawRes resId: Int,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, resId, options, block).convert()

/**
 * A DSL builder for converting a HEIC from a [String] image URL to a [Bitmap].
 *
 * First [imageUrl] will be downloaded then converted to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * val result = HeifConverter.convert(context, "https://sample.com/image.heic") {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val result = HeifConverter.convert(context, "https://sample.com/image.heic", options)
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[imageUrl] A URL pointing to a HEIC file.
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    imageUrl: String,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, imageUrl, options, block).convert()

/**
 * A DSL builder for converting a HEIC from [ByteArray] to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * val heicByteArray = File(context.cacheDir, "image.heic").readBytes()
 * val result = HeifConverter.convert(context, heicByteArray) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val heicByteArray = File(context.cacheDir, "image.heic").readBytes()
 * val result = HeifConverter.convert(context, heicByteArray, options)
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[byteArray] Input HEIC data as a [ByteArray].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    byteArray: ByteArray,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, byteArray, options, block).convert()

/**
 * A DSL builder for creating a [HeifConverter].
 *
 * DSL example:
 *
 * ```
 * val input = HeifConverter.Input.Url("https://sample.com/image.heic")
 * val result = HeifConverter.convert(context, input) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val input = HeifConverter.Input.Url("https://sample.com/image.heic")
 * val result = HeifConverter.convert(context, input, options)
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[input] Input HEIC data a instance of [HeifConverter.Input].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    input: HeifConverter.Input,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, input, options, block).convert()