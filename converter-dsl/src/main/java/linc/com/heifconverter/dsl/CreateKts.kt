package linc.com.heifconverter.dsl

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter
import java.io.File
import java.io.InputStream

/**
 * A method for creating a [HeifConverter] with a [File] input and the provided [options].
 *
 * Example:
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
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [file].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    file: File,
    options: HeifConverter.Options,
): HeifConverterInstance = create(context, HeifConverter.Input.File(file), options)

/**
 * A method for creating a [HeifConverter] with a [InputStream] input and the provided [options].
 *
 * Example:
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
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [inputStream].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    inputStream: InputStream,
    options: HeifConverter.Options,
): HeifConverterInstance = create(context, HeifConverter.Input.InputStream(inputStream), options)

/**
 * A DSL builder for creating a [HeifConverter] with a [DrawableRes] [Int] as the input.
 *
 * Example:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val result = HeifConverter.create(context, R.drawable.heic_image, options).convert()
 * ```
 *
 * @param[context] [Context] reference to initialize [HeifConverter].
 * @param[resId] Input HEIC resource id [Int].
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for converting the resource [resId].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    @DrawableRes resId: Int,
    options: HeifConverter.Options,
): HeifConverterInstance = create(context, HeifConverter.Input.Resources(resId), options)

/**
 * A DSL builder for creating a [HeifConverter] with [String] image URL as the input
 *
 * First [imageUrl] will be downloaded then converted to a [Bitmap].
 *
 * Example:
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
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for downloading and converting the HEIC at [imageUrl].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    imageUrl: String,
    options: HeifConverter.Options,
): HeifConverterInstance = create(context, HeifConverter.Input.Url(imageUrl), options)

/**
 * A DSL builder for creating a [HeifConverter] with a [ByteArray] input.
 *
 * Example:
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
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [byteArray].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    byteArray: ByteArray,
    options: HeifConverter.Options,
): HeifConverterInstance = create(context, HeifConverter.Input.ByteArray(byteArray), options)

/**
 * A DSL builder for creating a [HeifConverter].
 *
 * Example:
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
 * @param[options] Custom [HeifConverter.Options] for [HeifConverter].
 * @return A [HeifConverterInstance] for converting the [HeifConverter.Input].
 * @see HeifConverterDsl for all available options.
 */
public fun HeifConverter.Companion.create(
    context: Context,
    input: HeifConverter.Input,
    options: HeifConverter.Options,
): HeifConverterInstance = HeifConverterInstance(createConverter(context, input, options))

private fun createConverter(
    context: Context,
    input: HeifConverter.Input,
    options: HeifConverter.Options,
): HeifConverter = InternalHeifConverterDsl(options.copy(input = input)).build(context)