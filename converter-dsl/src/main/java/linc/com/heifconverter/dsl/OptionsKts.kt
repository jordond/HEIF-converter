package linc.com.heifconverter.dsl

import linc.com.heifconverter.HeifConverter

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
 * @param[block] A lambda scoped to [HeifConverterDsl] for creating an [HeifConverter.Options].
 * @return The built [HeifConverter.Options] object.
 */
public fun HeifConverter.Options.Companion.build(
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverter.Options = InternalHeifConverterDsl().apply(block).options