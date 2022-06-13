package linc.com.heifconverter.dsl

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import linc.com.heifconverter.HeifConverter

/**
 * Wrapper for [HeifConverter] that only exposes the [convert] methods.
 *
 * This wrapper hides all of the builder methods of [HeifConverter], if you need access to them
 * use [HeifConverter.create] or one of the builder DSL functions.
 */
public class HeifConverterInstance internal constructor(private val converter: HeifConverter) {

    /**
     * Convert the HEIC input into a [Bitmap] using coroutines to get the result synchronously.
     *
     * @return Result map containing the [Bitmap] and a path to the saved bitmap.
     * @see HeifConverter.convertBlocking for more info.
     */
    public suspend fun convert(): Map<String, Any?> = converter.convertBlocking()

    /**
     * Convert the HEIC input into a [Bitmap] using coroutines to get the result asynchronously.
     *
     * @param[coroutineScope]
     *
     * @param[coroutineScope] Custom [CoroutineScope] for launching the conversion coroutine.
     * @param[block] Lambda for retrieving the results asynchronously.
     * @return The [Job] used to launch the conversion coroutine.
     * @see HeifConverter.convert for more info.
     */
    public fun convert(
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
        block: (Map<String, Any?>) -> Unit,
    ): Job = converter.convert(coroutineScope, block)
}