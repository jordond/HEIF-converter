package linc.com.heifconverter.decoder

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Defines an object that can take the different HEIC input types and convert it to a [Bitmap].
 */
public interface HeicDecoder {

    /**
     * Decode a HEIC [ByteArray] into a [Bitmap].
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [ByteArray] is not a valid HEIC file.
     */
    public suspend fun fromByteArray(byteArray: ByteArray): Bitmap

    /**
     * Decode a HEIC [File] into a [Bitmap].
     *
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [File] is not a valid HEIC file, or not a valid path.
     */
    public suspend fun fromFile(file: File): Bitmap

    /**
     * Decode a HEIC [InputStream] into a [Bitmap].
     *
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [InputStream] is not a valid HEIC file.
     */
    public suspend fun fromInputStream(stream: InputStream): Bitmap

    /**
     * Decode a HEIC [RawRes] resource id [resId] into a [Bitmap].
     *
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [RawRes] resource id [resId] is not a valid HEIC file.
     */
    public suspend fun fromResources(@RawRes resId: Int): Bitmap

    /**
     * Decode a HEIC [ByteArray] into a [Bitmap].
     *
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [url] is not a valid HEIC file, or the download fails.
     */
    public suspend fun fromUrl(
        url: String,
        urlLoader: UrlLoader? = null,
    ): Bitmap {
        val loader = urlLoader ?: UrlLoader.Default()
        return fromInputStream(loader.download(url))
    }

    /**
     * Decode a HEIC [Uri] into a [Bitmap].
     *
     * @return Decoded [Bitmap].
     * @throws [Throwable] if the [Uri] is not a valid HEIC file.
     */
    public suspend fun fromUri(uri: Uri): Bitmap

    /**
     * Used for extension functions.
     */
    public companion object {

        public var fallbackInstance: HeicDecoder? = null
            internal set

        /**
         * Set a fallback [HeicDecoder] to use if you have not included any.
         */
        public fun setFallback(decoder: HeicDecoder) {
            fallbackInstance = decoder
        }
    }

    /**
     * A class for implementing how to download a URL [String] to a [InputStream] that can be
     * passed to [HeicDecoder.fromInputStream].
     *
     * A default implementation is provided via [UrlLoader.Default]. Which you can override the
     * behaviour of by creating a subclass:
     *
     */
    public abstract class UrlLoader {

        /**
         * Open a remote connection to the [url] and return a [InputStream]
         *
         * @param[url] URL pointing to the HEIC file to download.
         */
        public abstract suspend fun download(url: String): InputStream

        /**
         * A default implementation of [UrlLoader].
         *
         * You can override [Default.download] to perform logging or analytics:
         *
         * ```
         * class LoggingImageLoader : HeicDecoder.UrlLoader.Default() {
         *
         *     override suspend fun download(url: String): InputStream {
         *         Log.i("Downloading: $url")
         *         return super.download(url)
         *     }
         * }
         * ```
         *
         * Or you can customize the [HttpURLConnection] object like so:
         *
         * ```
         * class AuthImageLoader(private val auth: AuthRepo) : HeicDecoder.UrlLoader.Default() {
         *
         *     override fun customizeConnection(connection: HttpUrlConnection) {
         *         val authToken = auth.getAuthToken()
         *         connection.setRequestProperty("Authorization", "Bearer $authToken")
         *     }
         * }
         * ```
         */
        public open class Default : UrlLoader() {

            /**
             * Customize the [HttpURLConnection] before the connection is opened.
             *
             * **Note:** You can call [HttpURLConnection.connect] but it is already called after
             * this method returns.
             *
             * @param[connection] Instance of the [HttpURLConnection]
             */
            public open fun customizeConnection(connection: HttpURLConnection) {}

            @Suppress("BlockingMethodInNonBlockingContext")
            @CallSuper
            override suspend fun download(url: String): InputStream {
                return withContext(Dispatchers.IO) {
                    (URL(url).openConnection() as HttpURLConnection).run {
                        doInput = true
                        customizeConnection(this)
                        connect()

                        inputStream
                    }
                }
            }
        }
    }
}