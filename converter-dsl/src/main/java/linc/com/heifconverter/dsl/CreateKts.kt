package linc.com.heifconverter.dsl

import android.content.Context
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter
import linc.com.heifconverter.HeifConverter.Input
import java.io.File
import java.io.InputStream

public fun HeifConverter.Companion.create(
    context: Context,
    file: File,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.File(file), block)
)

public fun HeifConverter.Companion.create(
    context: Context,
    inputStream: InputStream,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.InputStream(inputStream), block)
)

public fun HeifConverter.Companion.create(
    context: Context,
    @DrawableRes resId: Int,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.Resources(resId), block)
)

public fun HeifConverter.Companion.create(
    context: Context,
    imageUrl: String,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.Url(imageUrl), block)
)

public fun HeifConverter.Companion.create(
    context: Context,
    byteArray: ByteArray,
    block: HeifConverterDsl.() -> Unit = {},
): HeifConverterInstance = HeifConverterInstance(
    converter = createConverter(context, Input.ByteArray(byteArray), block)
)

private fun createConverter(
    context: Context,
    input: Input,
    customizeBlock: HeifConverterDsl.() -> Unit = {},
): HeifConverter = InternalHeifConverterDsl(HeifConverter.Options(input = input))
    .apply(customizeBlock)
    .build(context)
