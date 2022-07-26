package linc.com.heifconverter.dsl.extension

import android.net.Uri
import androidx.annotation.RawRes
import linc.com.heifconverter.HeifConverter
import java.io.File
import java.io.InputStream

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[file] Input [File].
 * @return [HeifConverter.Input.File] reference.
 */
public fun HeifConverter.Input.Companion.from(
    file: File,
): HeifConverter.Input = HeifConverter.Input.File(file)

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[url] Input url as a [String].
 * @return [HeifConverter.Input.Url] reference.
 */
public fun HeifConverter.Input.Companion.from(
    url: String,
): HeifConverter.Input = HeifConverter.Input.Url(url)

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[resId] Input resource ID [Int].
 * @return [HeifConverter.Input.Resources] reference.
 */
public fun HeifConverter.Input.Companion.from(
    @RawRes resId: Int,
): HeifConverter.Input = HeifConverter.Input.Resources(resId)

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[inputStream] Input [InputStream].
 * @return [HeifConverter.Input.InputStream] reference.
 */
public fun HeifConverter.Input.Companion.from(
    inputStream: InputStream,
): HeifConverter.Input = HeifConverter.Input.InputStream(inputStream)

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[byteArray] Input [ByteArray].
 * @return [HeifConverter.Input.ByteArray] reference.
 */
public fun HeifConverter.Input.Companion.from(
    byteArray: ByteArray,
): HeifConverter.Input = HeifConverter.Input.ByteArray(byteArray)

/**
 * Convenience function for getting an instance of [HeifConverter.Input].
 *
 * @param[uri] Input [Uri].
 * @return [HeifConverter.Input.Uri] reference.
 */
public fun HeifConverter.Input.Companion.from(
    uri: Uri,
): HeifConverter.Input = HeifConverter.Input.Uri(uri)