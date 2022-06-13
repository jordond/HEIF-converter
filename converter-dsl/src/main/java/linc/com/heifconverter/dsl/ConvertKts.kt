package linc.com.heifconverter.dsl

import android.content.Context
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter
import java.io.File
import java.io.InputStream

public suspend fun HeifConverter.Companion.convert(
    context: Context,
    file: File,
    block: HeifConverterDsl.() -> Unit = {},
): Map<String, Any?> = create(context, file, block).convert()

public suspend fun HeifConverter.Companion.convert(
    context: Context,
    inputStream: InputStream,
    block: HeifConverterDsl.() -> Unit = {},
): Map<String, Any?> = create(context, inputStream, block).convert()

public suspend fun HeifConverter.Companion.convert(
    context: Context,
    @DrawableRes resId: Int,
    block: HeifConverterDsl.() -> Unit = {},
): Map<String, Any?> = create(context, resId, block).convert()

public suspend fun HeifConverter.Companion.convert(
    context: Context,
    imageUrl: String,
    block: HeifConverterDsl.() -> Unit = {},
): Map<String, Any?> = create(context, imageUrl, block).convert()

public suspend fun HeifConverter.Companion.convert(
    context: Context,
    byteArray: ByteArray,
    block: HeifConverterDsl.() -> Unit = {},
): Map<String, Any?> = create(context, byteArray, block).convert()