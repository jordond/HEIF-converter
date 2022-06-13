package linc.com.heifconverter.dsl

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.HeifConverter.Input
import java.io.File
import java.io.InputStream

/**
 * A DSL builder for creating a [HeifConverter] with a [File] input.
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[file] Input HEIC [File].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [file].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    file: File,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.File(file), block)
)

/**
 * A DSL builder for creating a [HeifConverter] with a [InputStream] input.
 *
 * Example:
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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[inputStream] Input HEIC as an [InputStream].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [inputStream].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    inputStream: InputStream,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.InputStream(inputStream), block)
)

/**
 * A DSL builder for creating a [HeifConverter] with a [DrawableRes] [Int] as the input.
 *
 * Example:
 *
 * ```
 * val converter = HeifConverter.create(context, R.drawable.my_heic_image) {
 *     saveResultImage = true
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 *
 * val result = converter.convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[resId] Input HEIC resource id [Int].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the resource [resId].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    @DrawableRes resId: Int,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.Resources(resId), block)
)

/**
 * A DSL builder for creating a [HeifConverter] with [String] image URL as the input
 *
 * First [imageUrl] will be downloaded then converted to a [Bitmap].
 *
 * Example:
 *
 * ```
 * val converter = HeifConverter.create(context, "https://sample.com/image.heic") {
 *     saveResultImage = true
 *     outputFormat = HeifConverter.Format.JPEG
 * }
 *
 * val result = converter.convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[imageUrl] A URL pointing to a HEIC file.
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for downloading and converting the HEIC at [imageUrl].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    imageUrl: String,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.Url(imageUrl), block)
)

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
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[byteArray] Input HEIC data as a [ByteArray].
 * @param[block] A lambda scoped to [HeifConverterDsl] for building a [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [byteArray].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    byteArray: ByteArray,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.ByteArray(byteArray), block)
)

private fun createConverter(
    context: Context,
    input: Input,
    customizeBlock: HeifConverterDsl.() -> Unit = {},
): HeifConverter = InternalHeifConverterDsl(HeifConverter.Options(input = input))
    .apply(customizeBlock)
    .build(context)