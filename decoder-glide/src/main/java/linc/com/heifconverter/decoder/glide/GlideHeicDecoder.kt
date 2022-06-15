package linc.com.heifconverter.decoder.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
 */
public class GlideHeicDecoder(context: Context) : HeicDecoder {

    private val glideBuilder: RequestBuilder<Bitmap> = Glide.with(context)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap? {
        return glideBuilder.load(byteArray).decode()
    }

    override suspend fun fromFile(file: File): Bitmap? {
        return glideBuilder.load(file).decode()
    }

    override suspend fun fromInputStream(stream: InputStream): Bitmap? {
        val byteArray = withContext(Dispatchers.IO) { stream.readBytes() }
        return fromByteArray(byteArray)
    }

    override suspend fun fromResources(resId: Int): Bitmap? {
        return glideBuilder.load(resId).decode()
    }

    override suspend fun fromUrl(url: String): Bitmap? {
        return glideBuilder.load(url).decode()
    }
}

/**
 * Decode the loaded source file into a [Bitmap].
 *
 * **Note:** You must call `RequestBuilder<Bitmap>.load()` before calling `decode()`.
 */
private suspend fun RequestBuilder<Bitmap>.decode(): Bitmap? = withContext(Dispatchers.IO) {
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