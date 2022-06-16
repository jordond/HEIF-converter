package linc.com.heifconverter.dsl

import android.graphics.Bitmap
import linc.com.heifconverter.HeifConverter

/**
 * A model representing the result of a [HeifConverter.convert].
 *
 * Use [HeifConverterResult.parse] to convert a result [Map] to a [HeifConverterResult].
 *
 * @property[bitmap] The converted [Bitmap], this might be `null` if the conversion failed without
 * throwing an exception.
 * @property[imagePath] The absolute path to the converted [Bitmap], this will only be non-null
 * if [HeifConverter.saveResultImage] is set to `true`.
 */
public data class HeifConverterResult(
    val bitmap: Bitmap?,
    val imagePath: String?,
) {

    /**
     * Get the [Bitmap] reference if it is not `null`, or throw a [RuntimeException].
     *
     * @throws RuntimeException if [bitmap] is `null`
     * @return An instance of the converted [Bitmap].
     */
    public fun getBitmapOrThrow(): Bitmap {
        if (bitmap == null) {
            throw RuntimeException("Bitmap is null!")
        }

        return bitmap
    }

    /**
     * Get the saved [imagePath] if it is not `null`, or throw a [RuntimeException].
     *
     * @throws RuntimeException if [imagePath] is `null`
     * @return The path to the saved [Bitmap].
     */
    public fun getImagePathOrThrow(): String {
        if (imagePath == null) {
            throw RuntimeException("Image Path is null!")
        }

        return imagePath
    }

    public companion object {

        /**
         * Parse the result of [HeifConverter.convertBlocking] or [HeifConverter.convert].
         *
         * Example:
         *
         * ```
         * val resultMap: Map<String, Any?> = HeifConverter.create(context)
         *     .fromUrl("https://sample.com/image.heic")
         *     .convertBlocking()
         *
         * val (bitmap, imagePath) = HeifConverterResult.parse(resultMap)
         * if (bitmap != null) {
         *     // Handle the bitmap
         * }
         */
        public fun parse(result: Map<String, Any?>): HeifConverterResult = HeifConverterResult(
            bitmap = result[HeifConverter.Key.BITMAP] as Bitmap?,
            imagePath = result[HeifConverter.Key.IMAGE_PATH] as String?,
        )
    }
}