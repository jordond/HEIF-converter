package linc.com.heifconverter.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.decoder.glide.GlideHeicDecoder

/**
 * Decode the [input] HEIC into a [Bitmap].
 *
 * @param[input] The HEIC [HeifConverter.Input] source to decode.
 * @param[urlLoader] Optional [HeicDecoder.UrlLoader] for downloading [HeifConverter.Input.Url].
 * @return The decoded [Bitmap] or `null`.
 */
internal suspend fun HeicDecoder.decode(
    input: HeifConverter.Input,
    urlLoader: HeicDecoder.UrlLoader? = null,
): Bitmap? = when (input) {
    is HeifConverter.Input.ByteArray -> fromByteArray(input.data)
    is HeifConverter.Input.File -> fromFile(input.data)
    is HeifConverter.Input.InputStream -> fromInputStream(input.data)
    is HeifConverter.Input.Resources -> fromResources(input.data)
    is HeifConverter.Input.Url -> fromUrl(input.data, urlLoader)
    is HeifConverter.Input.Uri -> fromUri(input.data)
    else -> throw IllegalStateException(
        "You forget to pass input type: File, Url etc. Use such functions: fromFile() etc."
    )
}

/**
 * Default implementation of [HeicDecoder].
 *
 * Will use the appropriate [HeicDecoder] based on the current [Build.VERSION.SDK_INT].
 * On Android 10 and higher a decoder using [BitmapFactory] will be used. On Android 9 and
 * lower, the Glide decoder will be used.
 *
 * You can customize the behaviour by:
 *
 * ```
 * class MyDecoder(context: Context) : HeicDecoder by HeicDecoder.default(context) {
 *
 *     override suspend fun fromFile(file: File): Bitmap? {
 *          val bitmap = super.fromFile(file)
 *          // do something with bitmap
 *          return bitmap
 *     }
 * }
 * ```
 *
 * @constructor A instance of the default [HeicDecoder].
 */
public fun HeicDecoder.Companion.default(context: Context): HeicDecoder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) BitmapFactoryHeicDecoder(context)
    else GlideHeicDecoder(context)
