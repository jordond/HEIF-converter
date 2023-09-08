package linc.com.heifconverter.decoder.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import linc.com.heifconverter.decoder.HeicDecoder
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A [HeicDecoder] that will work on all supported Android versions.
 *
 * Use this decoder if you need to run on Android 9 and lower, and have HEICs that are not
 * supported by the default decoder.
 *
 * @param[context] [Context] option used to initialize [Glide].
 * @constructor A [HeicDecoder] that uses [Glide] to decode the HEIC.
 */
public class GlideHeicDecoder(private val context: Context) : HeicDecoder {

    /**
     * A [HeicDecoder] that will work on all supported Android versions.
     *
     * For information on [useHardwareBitmaps] see the [Glide documentation](https://bumptech.github.io/glide/doc/hardwarebitmaps.html).
     *
     * @param[context] [Context] option used to initialize [Glide].
     * @param[useHardwareBitmaps] Enable the use of Glide's hardware bitmaps.
     * @constructor A [HeicDecoder] that uses [Glide] and Hardware Bitmaps to decode the HEIC.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public constructor(context: Context, useHardwareBitmaps: Boolean) : this(context) {
        this.useHardwareBitmaps = useHardwareBitmaps
    }

    private var useHardwareBitmaps: Boolean = false

    private val glideBuilder: RequestBuilder<Bitmap> = Glide.with(context)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .set(Downsampler.ALLOW_HARDWARE_CONFIG, useHardwareBitmaps)

    /**
     * @see HeicDecoder.fromByteArray
     */
    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap {
        return glideBuilder.load(byteArray).decode()
    }

    /**
     * @see HeicDecoder.fromFile
     */
    override suspend fun fromFile(file: File): Bitmap = glideBuilder.load(file).decode()

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
    override suspend fun fromResources(resId: Int): Bitmap = glideBuilder.load(resId).decode()

    /**
     * Custom implementation of [HeicDecoder.fromUrl], if [urlLoader] is `null` then use [Glide]
     * to download the [url] and pass it to [fromInputStream].
     *
     * @see HeicDecoder.fromUrl
     */
    override suspend fun fromUrl(url: String, urlLoader: HeicDecoder.UrlLoader?): Bitmap {
        return if (urlLoader == null) glideBuilder.load(url).decode()
        else fromInputStream(urlLoader.download(url))
    }

    /**
     * @see HeicDecoder.fromUri
     */
    override suspend fun fromUri(uri: Uri): Bitmap = glideBuilder.load(uri).decode()
}

/**
 * Decode the loaded source file into a [Bitmap].
 *
 * **Note:** You must call `RequestBuilder<Bitmap>.load()` before calling `decode()`.
 */
private suspend fun RequestBuilder<Bitmap>.decode(): Bitmap = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { continuation ->
        into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                continuation.resume(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                continuation.resumeWithException(Throwable("Unable to decode!"))
            }
        })
    }
}