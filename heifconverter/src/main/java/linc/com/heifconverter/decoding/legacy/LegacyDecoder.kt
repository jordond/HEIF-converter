package linc.com.heifconverter.decoding.legacy

import android.content.Context
import linc.com.heifconverter.decoding.Decoder
import java.io.File
import java.io.InputStream

/**
 * A [Decoder] for Android P and below, using the [HeifReader] to convert the files.
 */
internal class LegacyDecoder(private val context: Context) : Decoder {

    private val reader: HeifReader
        get() = HeifReader(context)

    override suspend fun fromByteArray(byteArray: ByteArray) = reader.decodeByteArray(byteArray)

    override suspend fun fromFile(file: File) = reader.decodeFile(file.absolutePath)

    override suspend fun fromInputStream(stream: InputStream) = reader.decodeStream(stream)

    override suspend fun fromResources(resId: Int) = reader.decodeResource(context.resources, resId)
}