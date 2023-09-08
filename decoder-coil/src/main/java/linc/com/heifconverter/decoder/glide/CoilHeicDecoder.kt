package linc.com.heifconverter.decoder.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.heifconverter.decoder.HeicDecoder
import java.io.File
import java.io.InputStream

/**
 * A [HeicDecoder] that will work on all supported Android versions.
 *
 * Use this decoder if you need to run on Android 9 and lower, and have HEICs that are not
 * supported by the default decoder.
 *
 * @param[context] [Context] option used to initialize [ImageLoader].
 * @constructor A [HeicDecoder] that uses Coil to decode the HEIC.
 */
public class CoilHeicDecoder(private val context: Context) : HeicDecoder {

    private val imageLoader = ImageLoader.Builder(context).build()

    /**
     * @see HeicDecoder.fromByteArray
     */
    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap {
        return decode(context, byteArray)
    }

    /**
     * @see HeicDecoder.fromFile
     */
    override suspend fun fromFile(file: File): Bitmap = decode(context, file)

    /**
     * @see HeicDecoder.fromInputStream
     */
    override suspend fun fromInputStream(
        stream: InputStream,
    ): Bitmap = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("glide_heic_download", ".heic", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            stream.copyTo(outputStream)
        }

        fromFile(tempFile)
    }

    /**
     * @see HeicDecoder.fromResources
     */
    override suspend fun fromResources(resId: Int): Bitmap = decode(context, resId)

    /**
     * Custom implementation of [HeicDecoder.fromUrl], if [urlLoader] is `null` then use Coil
     * to download the [url] and pass it to [fromInputStream].
     *
     * @see HeicDecoder.fromUrl
     */
    override suspend fun fromUrl(url: String, urlLoader: HeicDecoder.UrlLoader?): Bitmap {
        return if (urlLoader == null) decode(context, url)
        else fromInputStream(urlLoader.download(url))
    }

    /**
     * @see HeicDecoder.fromUri
     */
    override suspend fun fromUri(uri: Uri): Bitmap = decode(context, uri)

    /**
     * Decode the loaded source file into a [Bitmap].
     */
    private suspend fun decode(context: Context, data: Any): Bitmap = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(data)
            .allowHardware(false)
            .build()

        when (val result = imageLoader.execute(request)) {
            is ErrorResult -> throw result.throwable
            is SuccessResult -> (result.drawable as? BitmapDrawable)?.bitmap
                ?: error("Decoded bitmap was not a BitmapDrawable")
        }
    }
}

