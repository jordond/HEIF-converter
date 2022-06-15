package linc.com.heifconverter.decoder

import android.graphics.Bitmap
import androidx.annotation.RawRes
import linc.com.heifconverter.HeifConverter
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Defines an object that can take the different HEIC input types and convert it to a [Bitmap].
 */
public interface HeicDecoder {

    public suspend fun fromByteArray(byteArray: ByteArray): Bitmap?

    public suspend fun fromFile(file: File): Bitmap?

    public suspend fun fromInputStream(stream: InputStream): Bitmap?

    public suspend fun fromResources(@RawRes resId: Int): Bitmap?

    public suspend fun fromUrl(url: String): Bitmap? = fromInputStream(downloadToStream(url))

    public suspend fun decode(input: HeifConverter.Input): Bitmap? = when (input) {
        is HeifConverter.Input.ByteArray -> fromByteArray(input.data)
        is HeifConverter.Input.File -> fromFile(input.data)
        is HeifConverter.Input.InputStream -> fromInputStream(input.data)
        is HeifConverter.Input.Resources -> fromResources(input.data)
        is HeifConverter.Input.Url -> fromUrl(input.data)
        else -> throw IllegalStateException(
            "You forget to pass input type: File, Url etc. Use such functions: fromFile() etc."
        )
    }
}

private fun downloadToStream(urlString: String): InputStream {
    val url = URL(urlString)
    val connection = url.openConnection() as HttpURLConnection
    connection.doInput = true
    connection.connect()
    return connection.inputStream
}