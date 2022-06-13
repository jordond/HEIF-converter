package linc.com.heifconverter.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.HeifConverter

public class HeifConverterInstance(private val converter: HeifConverter) {

    public suspend fun convert(): Map<String, Any?> = converter.convertBlocking()

    public fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (Map<String, Any?>) -> Unit,
    ): Job = converter.convert(coroutineScope, block)
}