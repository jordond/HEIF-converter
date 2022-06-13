package linc.com.heifconverter.dsl.extension.convert

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.dsl.HeifConverterDsl
import linc.com.heifconverter.dsl.HeifConverterResult
import linc.com.heifconverter.dsl.extension.create.create
import java.io.File
import java.io.InputStream

/**
 * A DSL builder for converting a HEIC from [File] to a [Bitmap].
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[file] Input HEIC [File].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    file: File,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, file, block).convert()

/**
 * A DSL builder for converting a HEIC from [File] to a [Bitmap].
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[inputStream] Input HEIC as an [InputStream].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    inputStream: InputStream,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, inputStream, block).convert()

/**
 * A DSL builder for converting a HEIC from [DrawableRes] resource ID [Int] to a [Bitmap].
 *
 * Example:
 *
 * ```
 * val result = HeifConverter.convert(context, R.drawable.heic_image) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[resId] Input HEIC resource id [Int].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    @DrawableRes resId: Int,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, resId, block).convert()

/**
 * A DSL builder for converting a HEIC from a [String] image URL to a [Bitmap].
 *
 * First [imageUrl] will be downloaded then converted to a [Bitmap].
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[imageUrl] A URL pointing to a HEIC file.
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    imageUrl: String,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, imageUrl, block).convert()

/**
 * A DSL builder for converting a HEIC from [ByteArray] to a [Bitmap].
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[byteArray] Input HEIC data as a [ByteArray].
 * @param[block] A lambda scoped to [HeifConverterDsl] for customizing the conversion.
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    byteArray: ByteArray,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, byteArray, block).convert()

/**
 * A DSL builder for creating a [HeifConverter].
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[input] Input HEIC data a instance of [HeifConverter.Input].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return Result mapped to an instance of [HeifConverterResult]
 * @see HeifConverter.convertBlocking for more info.
 * @see HeifConverterDsl for all available options.
 */
public suspend fun HeifConverter.Companion.convert(
    context: Context,
    input: HeifConverter.Input,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterResult = create(context, input, block).convert()