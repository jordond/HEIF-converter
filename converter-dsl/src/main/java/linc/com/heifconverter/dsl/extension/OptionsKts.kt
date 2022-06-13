package linc.com.heifconverter.dsl.extension

import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.dsl.HeifConverterDsl
import linc.com.heifconverter.dsl.InternalHeifConverterDsl

/**
 * A DSL builder method for creating an instance of [HeifConverter.Options].
 *
 * Example:
 *
 * ```
 * val options = HeifConverter.Options.build {
 *     saveResultImage = true
 *     outputQuality(50)
 *     outputDirectory(context.cacheDir)
 * }
 *
 * val result = HeifConverter.create(context, "https://sample.com/image.heic", options).convert()
 * ```
 *
 * @param[options] A instance of [HeifConverter.Options] to start building from.
 * @param[block] A lambda scoped to [HeifConverterDsl] for creating an [HeifConverter.Options].
 * @return The built [HeifConverter.Options] object.
 */
public fun HeifConverter.Options.Companion.build(
    options: HeifConverter.Options = HeifConverter.Options(),
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverter.Options = InternalHeifConverterDsl(options).apply(block).options