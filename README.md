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

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
dependencies {
  def latest_version = "3.1.0"

  // Main library
  implementation("com.github.lincollincol:heif-converter:heifconverter:$latest_version")

  // To support Android 9 and lower you need to include ONE of the following dependencies:
  // Use Glide for decoding
  implementation("com.github.lincollincol:heif-converter:decoder-glide:$latest_version")
  // Use coil for decoding
  implementation("com.github.lincollincol:heif-converter:decoder-coil:$latest_version")

  // Optional DSL + extension library
  implementation("com.github.lincollincol:heif-converter:heifconverter-dsl:$latest_version")

  // Optional OkHttp3 HeifDecoder.ImageLoader for loading URLs
  implementation("com.github.lincollincol:heif-converter:imageloader-okhttp3:$latest_version")
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

If you are using this and need to support Android API 28 (9 Pie) or lower then you need to include
either the `decoder-glide` or `decoder-coil` dependency. Or you can implement your own and pass
call `HeicDecoder.setFallback()`.

## Customizing the conversion

Look at `HeifConverter.Options` to see all the available options and their defaults. Both the
builder and DSL-builder provide methods for changing all of the options.

```kotlin
data class Options(
    val input: Input = None,
    val saveResultImage: Boolean = true, // Save the converting Bitmap to the disk
    val outputQuality: Int = 100, // Quality of the saved image 0-100
    val outputFormat: Format = Format.JPEG,
    val outputFileName: String = UUID.randomUUID().toString(),
    val pathToSaveDirectory: File? = null, // Directory to save the converted bitmap
    val decoder: HeicDecoder? = null, // A custom implementation for decoding a HEIC file
    val urlLoader: HeicDecoder.UrlLoader? = null, // A custom url loader for downloading Input.URL
)
```

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

### Convert

* Lambda, inside convert method, will return map of Objects. If you want to get result bitmap, you
  need to get value by library key ``` HeifConverter.Key.BITMAP ``` and cast it to Bitmap

* If you need path to converted image - use ``` HeifConverter.Key.IMAGE_PATH ``` key and cast map
  value to String.

## DSL + extension syntax

**Note:** Make sure you include the `heifconverter-dsl` dependency!

Unlike the above builder syntax which returns a `Map<String, Any?>` all of the DSL and extension
methods return a `HeifConverterResult` class:

```kotlin
data class HeifConverterResult(
    val bitmap: Bitmap?, // Null if the decode failed but didn't throw an exception
    val imagePath: String?, // Only non-null if `saveResultImage` was true
)
```

Which you can get by calling `HeifConverter.create` then `convert`:

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

## Custom Decoder

If you need to customize the HEIC decoding behaviour you can either subclass `HeicDecoder.Default`
or you can create your own `HeicDecoder` implementation.

```kotlin
class MyDecoder(context: Context) : HeicDecoder.Default(context) {

    override suspend fun fromFile(file: File): Bitmap? {
        // override the default behaviour
    }
}
```

Or create your own decoder:

```kotlin
class MyDecoder(context: Context) : HeicDecoder {

    override suspend fun fromFile(file: File): Bitmap? {
        // Decode the HEIC file yourself
    }

    // implement the rest
}
```

Then set it when constructing `HeifConverter`:

```kotlin
val file: File = File("sample.heic")
HeifConverter.create(contex)
    .fromFile(file)
    .withCustomDecoder(MyDecoder(context))
    .convert { }
```

### Glide decoder

Alongside `HeicDecoder.Default` there is also `GlideHeicDecoder`. Which is useful for decoding HEIC
on all versions of Android.

Then set the custom decoder when using `HeifConverter`:

```kotlin
val file: File = File("sample.heic")
HeifConverter.create(contex)
    .fromFile(file)
    .withCustomDecoder(GlideHeicDecoder(context))
    .convert { result -> /* handle result */ }
```

Here is an example using the DSL:

```kotlin
val (bitmap, imagePath) = HeifConverter.convert(context, file) {
    customDecoder(GlideHeicDecoder(context))
}
```

If you only want to use the Glide decoder on Android 9 and lower:

```kotlin
val decoder: HeifConverter.HeicDecoder = HeicDecoder.default(context)

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

## Custom URL loader

By default when using `HeifConverter.Input.URL` the `HeicDecoder.Default` uses `HttpURLConnection`
to open a connection to the URL and return a `InputStream` that gets passed to the `HeicDecoder`.

If you want to customize this. For example, adding authentication or other custom headers, you have
three options:

- Customize [HeicDecoder.UrlLoader.Default].
- Implement your own `HeicDecoder.UrlLoader`.
- Import and customize the `OkHttpUrlLoader`.

### Customize Default URL loader

You can override `HeicDecoder.UrlLoader.Default.download` to perform logging or analytics:

```kotlin
class MyImageLoader : HeicDecoder.UrlLoader.Default() {
    override suspend fun download(url: String): InputStream {
        Log.i("Downloading: $url")
        return super.download(url)
    }
}
```

Or you can customize the [HttpURLConnection] object like so:

```kotlin
class MyImageLoader(private val auth: AuthRepo) : HeicDecoder.UrlLoader.Default() {
    override fun customizeConnection(connection: HttpUrlConnection) {
        val authToken = auth.getAuthToken()
        connection.setRequestProperty("Authorization", "Bearer $authToken")
    }
}
```

Then set the custom decoder when using `HeifConverter`:

```kotlin
HeifConverter.create(contex)
    .fromUrl("https://sample.com/image.heic")
    .withUrlLoader(MyImageLoader())
    .convert { result -> /* handle result */ }
```

### OkHttp3 URL loader

If you prefer to use OkHttp instead you can import and use the `urlloader-okhttp3` dependency:

```groovy
implementation("com.github.lincollincol:HEIF-converter:urlloader-okhttp3:$latest_version")
```

Then set the custom url loader when using `HeifConverter`:

```kotlin
HeifConverter.create(contex)
    .fromUrl("https://sample.com/image.heic")
    .withUrlLoader(OkHttpUrlLoader())
    .convert { result -> /* handle result */ }
```

Here is an example using the DSL:

```kotlin
val (bitmap, imagePath) = HeifConverter.convert(context, file) {
    urlLoader(OkHttpUrlLoader())
}
```

You also have the ability to customize the request before it is executed:

```kotlin
val token = "" // An auth token
val imageLoader = OkHttpUrlLoader {
    header("Authorization", "Bearer $token")
}
```

Or if you already have an instance of `OkHttpClient` that is configured for your app you can pass
that into the constructor:

```kotlin
// Created elsewhere in the app
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    )
    .build()

val imageLoader = OkHttpUrlLoader(okHttpClient)
```

Then add it to the `HeifConverter` builder:

```kotlin
HeifConverter.create(contex)
    .fromUrl("https://sample.com/image.heic")
    .withUrlLoader(OkHttpUrlLoader())
```

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
