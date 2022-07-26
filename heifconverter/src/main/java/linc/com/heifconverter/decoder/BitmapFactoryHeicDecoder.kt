package linc.com.heifconverter.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * [HeicDecoder] class for Android Q and higher.
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class BitmapFactoryHeicDecoder(private val context: Context) : HeicDecoder {

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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)
            ?.use { inputStream -> fromInputStream(inputStream) }
    }
}