package linc.com.heifconverter.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import linc.com.heifconverter.decoder.legacy.HeifReaderHeicDecoder
import java.io.File
import java.io.InputStream

/**
 * Default implementation of [HeicDecoder].
 *
 * Will use the appropriate [HeicDecoder] based on the current [Build.VERSION.SDK_INT].
 * On Android 10 and higher a decoder using [BitmapFactory] will be used. On Android 9 and
 * lower, a custom HEIC reader will be used.
 *
 * **Warning:** The HEIC reader for Android 9 and lower is incomplete and may not be able to
 * decode all HEIC files, and an exception will be thrown.
 */
public class DefaultHeicDecoder(context: Context) : HeicDecoder {

    private val decoder: HeicDecoder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) BitmapFactoryHeicDecoder(context)
        else HeifReaderHeicDecoder(context)

    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap? =
        decoder.fromByteArray(byteArray)

    override suspend fun fromFile(file: File): Bitmap? = decoder.fromFile(file)

    override suspend fun fromInputStream(stream: InputStream): Bitmap? =
        decoder.fromInputStream(stream)

    override suspend fun fromResources(resId: Int): Bitmap? = decoder.fromResources(resId)
}