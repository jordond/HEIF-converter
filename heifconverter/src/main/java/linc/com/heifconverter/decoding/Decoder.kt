package linc.com.heifconverter.decoding

import android.graphics.Bitmap
import androidx.annotation.RawRes
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Defines an object that can take the different HEIC input types and convert it to a [Bitmap].
 */
internal interface Decoder {

    suspend fun fromByteArray(byteArray: ByteArray): Bitmap?

    suspend fun fromFile(file: File): Bitmap?

    suspend fun fromInputStream(stream: InputStream): Bitmap?

    suspend fun fromResources(@RawRes resId: Int): Bitmap?

    suspend fun fromUrl(url: String): Bitmap? = fromInputStream(downloadToStream(url))
}

private fun downloadToStream(urlString: String): InputStream {
    val url = URL(urlString)
    val connection = url.openConnection() as HttpURLConnection
    connection.doInput = true
    connection.connect()
    return connection.inputStream
}