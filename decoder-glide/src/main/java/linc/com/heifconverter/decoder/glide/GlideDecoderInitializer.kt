package linc.com.heifconverter.decoder.glide

import android.content.Context
import linc.com.heifconverter.decoder.DefaultDecoderInitializer
import linc.com.heifconverter.decoder.HeicDecoder

internal class GlideDecoderInitializer : DefaultDecoderInitializer() {

    override fun createDecoder(context: Context): HeicDecoder {
        return GlideHeicDecoder(context)
    }
}