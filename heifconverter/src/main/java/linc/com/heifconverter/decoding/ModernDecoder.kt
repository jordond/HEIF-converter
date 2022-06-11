package linc.com.heifconverter.decoding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.InputStream

/**
 * [Decoder] class for Android Q and higher.
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class ModernDecoder(private val context: Context) : Decoder {

    override suspend fun fromByteArray(byteArray: ByteArray): Bitmap? {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, bitmapOptions)
    }

    override suspend fun fromFile(file: File): Bitmap? =
        BitmapFactory.decodeFile(file.absolutePath)

    override suspend fun fromInputStream(stream: InputStream): Bitmap? =
        BitmapFactory.decodeStream(stream)

    override suspend fun fromResources(resId: Int): Bitmap? =
        BitmapFactory.decodeResource(context.resources, resId)
}