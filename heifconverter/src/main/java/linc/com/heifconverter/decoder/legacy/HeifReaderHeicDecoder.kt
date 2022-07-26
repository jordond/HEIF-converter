package linc.com.heifconverter.decoder.legacy

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.heifconverter.decoder.HeicDecoder
import java.io.File
import java.io.InputStream

/**
 * A [HeicDecoder] for Android P and below, using the [HeifReader] to convert the files.
 */
internal class HeifReaderHeicDecoder(private val context: Context) : HeicDecoder {

    private val reader: HeifReader
        get() = HeifReader(context)

    override suspend fun fromByteArray(byteArray: ByteArray) = reader.decodeByteArray(byteArray)

    override suspend fun fromFile(file: File) = reader.decodeFile(file.absolutePath)

    override suspend fun fromInputStream(stream: InputStream) = reader.decodeStream(stream)

    override suspend fun fromResources(resId: Int) = reader.decodeResource(context.resources, resId)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)
            ?.use { inputStream -> fromInputStream(inputStream) }
    }
}