package linc.com.heifconverter.decoder

import android.content.Context
import androidx.startup.Initializer

public abstract class DefaultDecoderInitializer : Initializer<HeicDecoder> {

    public abstract fun createDecoder(context: Context): HeicDecoder

    override fun create(context: Context): HeicDecoder {
        check(HeicDecoder.fallbackInstance != null) {
            "HeicDecoder instance is already initialized, you likely imported " +
                "multiple decoder libraries. Please only use one."
        }

        return createDecoder(context).also { HeicDecoder.fallbackInstance = it }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}