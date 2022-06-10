package linc.com.heifconverter.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.DrawableRes
import linc.com.heifconverter.HeifConverter.InputData
import linc.com.heifconverter.HeifReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal suspend fun InputData.createBitmap(context: Context): Bitmap? = when (this) {
    is InputData.ByteArray -> data.decode()
    is InputData.File -> decode()
    is InputData.InputStream -> data.decode()
    is InputData.Resources -> data.decodeBitmap(context)
    is InputData.Url -> download()
    else -> {
        val message =
            "You forget to pass input type: File, Url etc. Use such functions: fromFile() etc."
        throw IllegalStateException(message)
    }
}

internal suspend fun ByteArray.decode(): Bitmap? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(this, 0, size, bitmapOptions)
    }
    else -> HeifReader.decodeByteArray(this)
}

internal suspend fun InputData.File.decode() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> BitmapFactory.decodeFile(data)
    else -> HeifReader.decodeFile(data)
}

internal suspend fun InputStream.decode(): Bitmap? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
        BitmapFactory.decodeStream(this)
    }
    else -> HeifReader.decodeStream(this)
}

internal suspend fun @receiver:DrawableRes Int.decodeBitmap(context: Context) = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
        BitmapFactory.decodeResource(context.resources, this)
    }
    else -> HeifReader.decodeResource(context.resources, this)
}

internal fun InputData.Url.download(): Bitmap {
    val url = URL(data)
    val connection = url.openConnection() as HttpURLConnection
    connection.doInput = true
    connection.connect()
    return BitmapFactory.decodeStream(connection.inputStream)
}
