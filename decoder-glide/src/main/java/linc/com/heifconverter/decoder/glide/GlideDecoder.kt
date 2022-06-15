package linc.com.heifconverter.decoder.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.suspendCancellableCoroutine
import linc.com.heifconverter.decoder.HeicDecoder
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class GlideDecoder(private val context: Context) : HeicDecoder {

    private val glideBuilder: RequestBuilder<Bitmap>
        get() = Glide.with(context).asBitmap()

    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap? {
        return glideBuilder.load(byteArray).decode()
    }

    override suspend fun fromFile(file: File): Bitmap? {
        return glideBuilder.load(file).decode()
    }

    override suspend fun fromInputStream(stream: InputStream): Bitmap? {
        TODO("Not yet implemented")
    }

    override suspend fun fromResources(resId: Int): Bitmap? {
        return glideBuilder.load(resId).decode()
    }

    override suspend fun fromUrl(url: String): Bitmap? {
        return glideBuilder.load(url).decode()
    }
}

private suspend fun RequestBuilder<Bitmap>.decode(): Bitmap? {
    return suspendCancellableCoroutine { continuation ->
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