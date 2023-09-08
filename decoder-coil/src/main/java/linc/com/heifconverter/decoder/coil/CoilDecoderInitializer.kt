package linc.com.heifconverter.decoder.coil

import android.content.Context
import androidx.startup.Initializer
import linc.com.heifconverter.decoder.DefaultDecoderInitializer
import linc.com.heifconverter.decoder.HeicDecoder

internal class CoilDecoderInitializer : DefaultDecoderInitializer() {

    override fun createDecoder(context: Context): HeicDecoder {
        return CoilHeicDecoder(context)
    }
}