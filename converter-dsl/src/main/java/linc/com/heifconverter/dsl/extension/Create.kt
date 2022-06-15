package linc.com.heifconverter.dsl.extension

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RawRes
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.HeifConverter.Input
import linc.com.heifconverter.dsl.HeifConverterDsl
import linc.com.heifconverter.dsl.HeifConverterInstance
import linc.com.heifconverter.dsl.InternalHeifConverterDsl
import java.io.File
import java.io.InputStream

/**
 * A DSL builder for creating a [HeifConverter] with a [File] input.
 *
 * DSL example:
 *
 * ```
 * val heicFile = File(context.cacheDir, "image.heic")
 * val converter = HeifConverter.create(context, heicFile) {
 *     saveResultImage = true
 *     outputName = "image"
 *     outputDirectory = File(context.cacheDir)
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 *
 * val result = converter.convert()
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
 * val result = HeifConverter.create(context, heicFile, options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[file] Input HEIC [File].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [file].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    file: File,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = create(context, Input.File(file), options, block)

/**
 * A DSL builder for creating a [HeifConverter] with a [InputStream] input.
 *
 * DSL example:
 *
 * ```
 * File(context.cacheDir, "image.heic").inputStream().use { inputStream ->
 *     val converter = HeifConverter.create(context, inputStream) {
 *         saveResultImage = true
 *         outputDirectory = File(context.cacheDir)
 *     }
 *
 *     val result = converter.convert()
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
 *     val result = HeifConverter.create(context, heicFile, options).convert()
 * }
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[inputStream] Input HEIC as an [InputStream].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [inputStream].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    inputStream: InputStream,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = create(context, Input.InputStream(inputStream), options, block)

/**
 * A DSL builder for creating a [HeifConverter] with a [RawRes] [Int] as the input.
 *
 * DSL example:
 *
 * ```
 * val converter = HeifConverter.create(context, R.raw.my_heic_image) {
 *     saveResultImage = true
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 *
 * val result = converter.convert()
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
 * val result = HeifConverter.create(context, R.raw.heic_image, options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[resId] Input HEIC resource id [Int].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the resource [resId].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    @RawRes resId: Int,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = create(context, Input.Resources(resId), options, block)

/**
 * A DSL builder for creating a [HeifConverter] with [String] image URL as the input.
 *
 * First [imageUrl] will be downloaded then converted to a [Bitmap].
 *
 * DSL example:
 *
 * ```
 * val converter = HeifConverter.create(context, "https://sample.com/image.heic") {
 *     saveResultImage = true
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 *
 * val result = converter.convert()
 * ```
 * Or you can pass in a [HeifConverter.Options] instance:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val result = HeifConverter.create(context, "https://sample.com/image.heic", options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[imageUrl] A URL pointing to a HEIC file.
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for downloading and converting the HEIC at [imageUrl].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    imageUrl: String,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = create(context, Input.Url(imageUrl), options, block)

/**
 * A DSL builder for creating a [HeifConverter] with a [ByteArray] input.
 *
 * Example:
 *
 * ```
 * val heicByteArray = File(context.cacheDir, "image.heic").readBytes()
 * val converter = HeifConverter.create(context, heicByteArray) {
 *     saveResultImage = true
 *     outputDirectory = File(context.cacheDir)
 * }
 *
 * val result = converter.convert()
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
 * val result = HeifConverter.create(context, heicByteArray, options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[byteArray] Input HEIC data as a [ByteArray].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [byteArray].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    byteArray: ByteArray,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = create(context, Input.ByteArray(byteArray), options, block)

/**
 * A DSL builder for creating a [HeifConverter].
 *
 * Example:
 *
 * ```
 * val input = HeifConverter.Input.Url("https://sample.com/image.heic")
 * val converter = HeifConverter.create(context, input) {
 *     saveResultImage = true
 *     outputDirectory = File(context.cacheDir)
 * }
 *
 * val result = converter.convert()
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
 * val result = HeifConverter.create(context, input, options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[input] Input HEIC data a instance of [HeifConverter.Input].
 * @param[options] Optional [HeifConverter.Options] instance to configure [HeifConverter].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [HeifConverter.Input].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    input: Input,
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(createConverter(context, input, options, block))

private fun createConverter(
    context: Context,
    input: Input,
    options: HeifConverter.Options = HeifConverter.Options(),
    customizeBlock: HeifConverterDsl.() -> Unit = {},
): HeifConverter {
    val builtOptions = buildOptions(options, customizeBlock).copy(input = input)
    return InternalHeifConverterDsl(builtOptions).build(context)
}
