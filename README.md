# HEIF-converter

![GitHub release (latest by date)](https://img.shields.io/github/v/release/lincollincol/HEIF-converter)
![GitHub](https://img.shields.io/github/license/lincollincol/HEIF-converter)  
![GitHub followers](https://img.shields.io/github/followers/lincollincol?style=social)
![GitHub stars](https://img.shields.io/github/stars/lincollincol/HEIF-converter?style=social)
![GitHub forks](https://img.shields.io/github/forks/lincollincol/HEIF-converter?style=social)

<p align="center">
  <img src="https://github.com/lincollincol/HEIF-converter/blob/master/img/header.png" width="550" height="250">
</p>  

Converter for High Efficiency Image Format(HEIF) to other image format

## Available formats

* JPEG
* PNG
* WEBP

## Download

``` groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

``` groovy
dependencies {
  // Include everything
  implementation 'com.github.lincollincol:HEIF-converter:v2.0'
  
  // Or you can add them indiviually
  
  // Main library
  implementation 'com.github.lincollincol:HEIF-converter:heifconverter:v2.0'
  
  // Optional DSL + extension library
  implementation 'com.github.lincollincol:HEIF-converter:heifconverter-dsl:v2.0'
  
  // Optional Decoder using Glide (see below about Android <= 9)
  implementation 'com.github.lincollincol:HEIF-converter:decoder-glide:v2.0'
}
```

## Usage

### Builder syntax

```kotlin
HeifConverter.useContext(this)
    .fromUrl("https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic")
    .withOutputFormat(HeifConverter.Format.PNG)
    .withOutputQuality(100) // optional - default value = 100. Available range (0 .. 100)
    .saveFileWithName("Image_Converted_Name_2") // optional - default value = uuid random string
    .saveResultImage(true) // optional - default value = true
    .convert {
        println(it[HeifConverter.Key.IMAGE_PATH] as String)
        resultImage.setImageBitmap((it[HeifConverter.Key.BITMAP] as Bitmap))
    }
```

### Note

If your source HEIC file is large (for example 4K), then once it is converted into a `Bitmap` it
could potentially be very very large. Large enough to cause a crash if you pass it directly to
an `ImageView`.

## Android 9 and lower

If you are using this and need to support Android API 28 (9 Pie) or lower. Then there is a good
chance the converter will crash because the current implementation of the `HeifReader` class does
not support all types of HEIF files.

To fix this you can use a custom `HeifConverter.HeicDecoder` instance. You can create one yourself,
but one is provided in the `:decoder-glide` module.

### DSL + extension syntax

**Note:** Make sure you include the `heifconverter-dsl` dependency!

Unlike the above builder syntax which returns a `Map<String, Any?>` all of the DSL and extension
methods return a `HeifConverterResult` class.

Using `HeifConverter.create`:

```kotlin
val inputUrl = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
val converter = HeifConverter.create(this, inputUrl) {
    saveResultImage(true)
    outputFormat(HeifConverter.Format.PNG)
    outputQuality(50)
    outputName("Image_Converted_Name")
}

// Using Coroutines
val (bitmap, imagePath) = converter.convert()
if (bitmap != null) {
    // do something with bitmap
}

// Using Callback
converter.convert(lifecycleScope /* optional coroutine scope */) { (bitmap, imagePath) ->
    if (bitmap != null) {
        // do something with bitmap
    }
}
```

There are `HeifConverter.create` extension functions for each input type. `File`, `InputStream`
, `ByteArray`, etc.

If you want to skip the step of creating a `HeifConverter` instance you can use
the `HeifConverter.convert` extensions instead:

```kotlin
val inputUrl = "https://github.com/nokiatech/heif/raw/gh-pages/content/images/crowd_1440x960.heic"
val result = HeifConverter.convert(context, inputUrl) {
    saveResultImage = true
    outputName = "Image_Converted_Name"
    outputDirectory = File(context.cacheDir)
    outputFormat = HeifConverter.Format.JPEG
}
```

There are `HeifConverter.convert` extension functions for each input type. `File`, `InputStream`
, `ByteArray`, etc.

You can also pass in a `HeifConverter.Options` object to each of the DSL extensions:

```kotlin
class SampleClass(context: Context) {
    private val convertOptions = HeifConverter.Options.build {
        saveResultImage = true
        outputQuality(50)
        outputDirectory(context.cacheDir)
    }

    suspend fun convertImage(url: String): Bitmap? {
        val result = HeifConverter.create(context, url, convertOptions).convert()
        return result.bitmap
    }
}
```

## Glide decoder

To use the Glide decoder first you must include the dependency:

```groovy
implementation 'com.github.lincollincol:HEIF-converter:decoder-glide:v2.0'

// Optional for using DSL syntax
implementation 'com.github.lincollincol:HEIF-converter:heifconverter-dsl:v2.0'
```

Then set the custom decoder when using `HeifConverter`:

```kotlin
val file: File = File("sample.heic")
HeifConverter.create(contex)
    .fromFile(file)
    .customDecoder(GlideHeicDecoder(context))
    .setCustomDecoder { result ->
        // handle result
    }
```

Here is an example using the DSL:

```kotlin
val (bitmap, imagePath) = HeifConverter.convert(context, file) {
    customDecoder(GlideHeicDecoder(context))
}
```

If you only want to use the Glide decoder on Android 9 and lower:

```kotlin
val decoder: HeifConverter.HeicDecoder? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) null
    else GildeHeicDecoder(context)

val (bitmap, imagePath) = HeifConverter.convert(context, file) {
    customDecoder = decoder
}
```

### Glide and Hardware Bitmaps

Glide has a concept
called [Hardware Bitmaps](https://bumptech.github.io/glide/doc/hardwarebitmaps.html). They have the
potential to save memory and be quicker. However support is only on Android 9 and above, so keep
that in mind.

To enable them you can do:

```kotlin
val decoder = GlideHeicDecoder(context, useHardwareBitmaps = true)
```

## Customizing the conversion

Look at `HeifConverter.Options` to see all the available options and their defaults. Both the
builder and DSL-builder provide methods for changing all of the options.

```kotlin
data class Options constructor(
    val input: Input = None,
    val saveResultImage: Boolean = true, // Save the converting Bitmap to the disk
    val outputQuality: Int = 100, // Quality of the saved image 0-100
    val outputFormat: Format = Format.JPEG,
    val outputFileName: String = UUID.randomUUID().toString(),
    val pathToSaveDirectory: File? = null, // Directory to save the converted bitmap
    val decoder: HeicDecoder? = null, // A custom implementation for decoding a HEIC file
)
```

### convert function

* Lambda, inside convert method, will return map of Objects. If you want to get result bitmap, you
  need to get value by library key ``` HeifConverter.Key.BITMAP ``` and cast it to Bitmap

* If you need path to converted image - use ``` HeifConverter.Key.IMAGE_PATH ``` key and cast map
  value to String.

### saveResultImage function

* Set false, if you need bitmap only without saving.
* You can skip this function if you want to save converted image, because it is true by default

### saveFileWithName function

* Use custom file name.
* Skip this function if you don't need custom converted image name, because UUID generate unique
  name by default.

### withOutputQuality function

* Use this function if you need custom output quality.
* Skip this function if you don't need custom output quality, default quality - 100

### withOutputFormat function

* Set output image format.
* Use values from HeifConverter.Format. (Current available: PNG, JPEG, WEBP).

### from (Source)

* Convert heic image from sources such file, url, bytes, input stream etc.
* You can call this function only one time. If you call this function few times - converter will use
  last called source.

## Based on

<a href="https://github.com/yohhoy/heifreader">heifreader by yohhoy</a>

## License

```
MIT License

Copyright (c) 2022 lincollincol

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
