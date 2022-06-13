package linc.com.heifconverter.dsl

import android.content.Context
import androidx.annotation.IntRange
import linc.com.heifconverter.HeifConverter
import java.io.File

public interface HeifConverterDsl {

    public var saveResultImage: Boolean
    public fun saveResultImage(saveResultImage: Boolean)

    public var outputFormat: HeifConverter.Format
    public fun outputFormat(format: HeifConverter.Format)

    public fun outputQuality(@IntRange(from = 0, to = 100) quality: Int)

    public var outputName: String
    public fun outputName(fileName: String)

    public var outputDirectory: File?
    public fun outputDirectory(directory: File)
    public fun outputDirectory(path: String) {
        outputDirectory(File(path))
    }

    public fun useDefaultOutputPath(context: Context)
}

internal class InternalHeifConverterDsl(
    internal var options: HeifConverter.Options = HeifConverter.Options(),
) : HeifConverterDsl {

    override var saveResultImage: Boolean
        get() = options.saveResultImage
        set(value) {
            options = options.copy(saveResultImage = value)
        }

    override fun saveResultImage(saveResultImage: Boolean) {
        this.saveResultImage = saveResultImage
    }

    override var outputFormat: HeifConverter.Format
        get() = options.outputFormat
        set(value) {
            options = options.copy(outputFormat = value)
        }

    override fun outputFormat(format: HeifConverter.Format) {
        this.outputFormat = format
    }

    override fun outputQuality(quality: Int) {
        options = options.copy(outputQuality = quality)
    }

    override var outputName: String
        get() = options.outputFileName
        set(value) {
            options = options.copy(convertedFileName = value)
        }

    override fun outputName(fileName: String) {
        this.outputName = fileName
    }

    override var outputDirectory: File?
        get() = options.pathToSaveDirectory
        set(value) {
            options = options.copy(pathToSaveDirectory = value)
        }

    override fun outputDirectory(directory: File) {
        this.outputDirectory = directory
    }

    override fun useDefaultOutputPath(context: Context) {
        val path = HeifConverter.Options.defaultOutputPath(context)
        options = options.copy(pathToSaveDirectory = path)
    }

    fun build(context: Context) = HeifConverter.create(context, options)
}

