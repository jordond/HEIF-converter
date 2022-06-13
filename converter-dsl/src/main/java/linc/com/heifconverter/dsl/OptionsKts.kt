package linc.com.heifconverter.dsl

import linc.com.heifconverter.HeifConverter

public fun HeifConverter.Options.Companion.build(
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverter.Options = InternalHeifConverterDsl().apply(block).options