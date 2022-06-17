package linc.com.heifconverter.decoder

import androidx.annotation.CallSuper
import kotlinx.coroutines.suspendCancellableCoroutine
import linc.com.heifconverter.HeifConverter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * An implementation of [HeicDecoder.ImageLoader] that uses [OkHttpClient] to make the request.
 *
 * You can use it with [HeifConverter.withImageLoader]:
 *
 * ```
 * HeifConverter.useContext(this)
 *     .fromUrl("https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic")
 *     .withImageLoader(OkHttpImageLoader())
 *     .convert { }
 * ```
 *
 * If you need to customize how the request is made, you can either pass in a custom [client] or
 * use the [customizeRequest] lambda.
 *
 * ```
 * val token = "" // An auth token
 * val imageLoader = OkHttpImageLoader() {
 *     header("Authorization", "Bearer $token")
 * }
 *
 * HeifConverter.useContext(this)
 *     .fromUrl("https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic")
 *     .withImageLoader(imageLoader)
 *     .convert { }
 * ```
 *
 * Or if you are using the `heifconverter-dsl` package:
 *
 * ```
 * val inputUrl = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
 * val result = HeifConverter.convert(context, inputUrl) {
 *     // Property access
 *     imageLoader = OkHttpImageLoader()
 *
 *     // Function setter
 *     imageLoader(OkHttpImageLoader())
 * }
 * ```
 *
 * @param[client] Instance of [OkHttpClient] for making the request.
 * @param[customizeRequest] Lambda for customizing the [Request.Builder] before the request is executed.
 * @constructor Default implementation of a [HeicDecoder.ImageLoader] that uses [OkHttpClient].
 */
public open class OkHttpImageLoader(
    private val client: OkHttpClient = OkHttpClient(),
    private val customizeRequest: Request.Builder.() -> Request.Builder,
) : HeicDecoder.ImageLoader() {

    /**
     * Download the [url] to an [InputStream].
     *
     * @param[url] URL of the HEIC file.
     * @throws RuntimeException if the response has no body.
     */
    @CallSuper
    override suspend fun download(url: String): InputStream {
        val request = Request.Builder().url(url).run(customizeRequest).build()
        val response = client.newCall(request).await()

        return response.body?.byteStream()
            ?: throw RuntimeException("No response body from download")
    }
}

/**
 * Convert the [Call.enqueue] into a cancelable coroutine.
 */
private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    // Make the request
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isCancelled) return
            continuation.resumeWithException(e)
        }
    })

    // Cancel request if coroutine is cancelled
    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (ex: Throwable) {
            // Ignore cancel exception
        }
    }
}