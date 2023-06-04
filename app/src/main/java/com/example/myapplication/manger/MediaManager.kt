package com.example.myapplication.manger

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.example.myapplication.utils.askForPermissions
import com.example.myapplication.utils.getPath
import com.example.myapplication.utils.isRPlus
import com.example.myapplication.utils.isTiramisuPlus

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaManager(
    private val fragment: Fragment?=null,
    private  val activity: FragmentActivity?=null,
    private val isActivity:Boolean = true,
    private  val callBack:(type: MediaTypes, path:String?, uri:Uri?)->Unit) {

    companion object{
        const val MEDIA_TYPE = "media_type"
        const val FILE_PROVIDER_AUTHORITY = ".provider"
        const val TYPE_IMAGE = 1
    }

    var context: Context? = null
    private var pickImage: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickVideo: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var audioIntentLauncher: ActivityResultLauncher<Intent>? = null
    private var videoIntentLauncher: ActivityResultLauncher<Intent>? = null
    private var photoIntentLauncher: ActivityResultLauncher<Intent>? = null
    private var galleryIntentLauncher: ActivityResultLauncher<Intent>? = null
    private var mCurrentPhotoPath: String? = null

    init {
        context = (if (isActivity) activity else fragment?.requireContext())?.also{register(it) }
    }

    private fun register(context: Context?){
        if (context == null)return




        pickVideo = (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                if ((((context?.getPath(uri)?.let { File(it).length() }?:0) / 1024) / 1024) > 20) {
                    context.showFileSizeDialog(20)
                } else {
                    callBack(MediaTypes.VIDEO, context?.getPath(it),it)
                }
            }
        }

        pickImage = (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                if ((((context?.getPath(uri)?.let { File(it).length() }?:0) / 1024) / 1024)> 20) {
                    context.showFileSizeDialog(20)
                } else {
                    callBack(MediaTypes.GALLERY, context?.getPath(it),it)
                }
            }
        }

        videoIntentLauncher = (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let{ context.getVideo(it)}
            }
        }

        galleryIntentLauncher = (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let{context?.getImage(it)}
            }
        }

        audioIntentLauncher= (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let{ context.getAudio(it)}
            }
        }

        photoIntentLauncher = (if (isActivity) activity else fragment)?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath?.let {
                    saveBitmapToFile(File(it))
                    callBack(MediaTypes.PHOTO,it,Uri.fromFile(File(it)))
                }
            }
        }
    }

    private fun convertBitmaptoFile(destinationFile: File, bitmap: Bitmap,uri: Uri) {
        try {
            destinationFile.createNewFile()
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
            val bitmapData = bos.toByteArray()
            val fos = FileOutputStream(destinationFile)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            callBack(MediaTypes.GALLERY,destinationFile.absolutePath,uri)
        }catch (_:Exception){ }
    }

    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
        } else {
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    fun handleMediaClick(it: String) {
        when (it) {
            MediaTypes.GALLERY.name -> checkGalleryPermission()
            MediaTypes.PHOTO.name -> checkCameraPermission()
            MediaTypes.VIDEO.name -> checkVideoPermission()
            MediaTypes.AUDIO.name -> checkAudioPermission()
        }
    }

    private fun launchTypedIntent(type: MediaTypes) {
        when (type) {
            MediaTypes.VIDEO -> {
                pickVideo?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }
            MediaTypes.GALLERY -> {
                pickImage?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            else -> {}
        }
    }

    private fun checkAudioPermission(){
        context?.let {context->
            if (isTiramisuPlus()) {
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                }.also {context.askForPermissions(it){ pickAudio()}  }
            }else{
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (!isRPlus())  add( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }.also {context.askForPermissions(it){pickAudio()}  }
            }
        }
    }

    private fun checkVideoPermission(){
        context?.let {context->
            if (isTiramisuPlus()) {
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                }.also {context.askForPermissions(it){launchTypedIntent(MediaTypes.VIDEO)}  }
            }else{
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (!isRPlus())  add( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }.also {context.askForPermissions(it){pickVideoGallery()}  }
            }
        }
    }

    private fun checkGalleryPermission(){
        context?.let {context->
            if (isTiramisuPlus()) {
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                }.also {context.askForPermissions(it){launchTypedIntent(MediaTypes.GALLERY)}  }
            }else{
                arrayListOf<String>().apply {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (!isRPlus())  add( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }.also {context.askForPermissions(it){dispatchGalleryIntent()}  }
            }
        }
    }

    private fun checkCameraPermission(){
        context?.let {context->
            arrayListOf<String>().apply {
                if (!isTiramisuPlus()){
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                add(Manifest.permission.CAMERA)
            }.also {context.askForPermissions(it){ context.dispatchTakePictureIntent()}  }
        }
    }

    private fun dispatchGalleryIntent() {
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(MEDIA_TYPE, MediaTypes.GALLERY.name )
        }.also {
            galleryIntentLauncher?.launch(it)
        }
    }

    private fun pickVideoGallery() {
        Intent().apply {
            type = "video/*"
            action = Intent.ACTION_PICK
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MEDIA_TYPE, MediaTypes.VIDEO.name )
        }.also {
            videoIntentLauncher?.launch(Intent.createChooser(it, "Video"))
        }
    }

    private fun pickAudio() {
        Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(MEDIA_TYPE, MediaTypes.AUDIO.name)
        }.also {  audioIntentLauncher?.launch(it) }
    }

    private fun Context.dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                kotlin.runCatching { photoFile = createMediaFile(TYPE_IMAGE) }
                photoFile?.let {
                    putExtra(MEDIA_TYPE, MediaTypes.PHOTO.name)
                    FileProvider.getUriForFile(
                        this@dispatchTakePictureIntent,
                        packageName + FILE_PROVIDER_AUTHORITY,
                        it
                    )?.also { putExtra(MediaStore.EXTRA_OUTPUT, it) }
                }
            }
        }.also { photoIntentLauncher?.launch(it) }
    }

    private fun Context.createMediaFile(type: Int): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val fileName = if (type == TYPE_IMAGE) "JPEG_" + timeStamp + "_" else "VID_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val file = File.createTempFile(fileName, if (type == TYPE_IMAGE) ".jpg" else ".mp4", storageDir)
        mCurrentPhotoPath = file.absolutePath
        return file
    }

    private fun Context.getVideo(uri: Uri){
        if ((((context?.getPath(uri)?.let { File(it).length() }?:0) / 1024) / 1024) > 20) {
            showFileSizeDialog(20)
        } else {
            callBack(MediaTypes.VIDEO, context?.getPath(uri),null)
        }
    }

    private fun Context.getAudio(uri: Uri){
        var fileDescriptor: ParcelFileDescriptor?
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fileDescriptor = contentResolver.openFile(uri, "r", null)
                if (fileDescriptor == null){
                    return@runCatching
                }else{
                    val input = FileInputStream(fileDescriptor?.fileDescriptor)
                    val byteArray = readBinaryStream(input, fileDescriptor?.statSize?.toInt()?:0)
                    val cachedFile = File(cacheDir, "${System.currentTimeMillis()}.mp3")
                    writeFile(cachedFile, byteArray)
                    if (((cachedFile.length() / 1024) / 1024) >= 20) showFileSizeDialog(20)
                    else callBack(MediaTypes.AUDIO,cachedFile.absolutePath,uri)
                }
            } else {
                ((File(getAudioPath(uri)).length() / 1024) / 1024).also {
                    if (it >= 20) showFileSizeDialog(20) else callBack(MediaTypes.AUDIO,getAudioPath(uri),uri)
                }
            }
        }
    }

    private fun Context.getImage(uri: Uri){
        val selectedBitmap: Bitmap? = getBitmap(this, uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
            .format(Date())
        val selectedImgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            timeStamp + ".jpg"
        )
        selectedBitmap?.let { convertBitmaptoFile(selectedImgFile, it,uri) }
    }

    private fun readBinaryStream(stream: InputStream, byteCount: Int): ByteArray {
        val output = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(if (byteCount > 0) byteCount else 4096)
            var read: Int
            while (stream.read(buffer).also { read = it } >= 0) {
                output.write(buffer, 0, read)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return output.toByteArray()
    }

    private fun Context.showFileSizeDialog(size: Int) {
        AlertDialog.Builder(this).create().apply {
            setMessage("File size should not be more than $size mb")
            setButton(DialogInterface.BUTTON_POSITIVE, "Ok") { dialog, which -> dismiss() }
            show()
        }
    }

    private fun writeFile(cachedFile: File, data: ByteArray): Boolean {
        return try {
            var output: BufferedOutputStream? = null
            try {
                output = BufferedOutputStream(FileOutputStream(cachedFile))
                output.write(data)
                output.flush()
                true
            } finally {
                output?.close()
            }
        } catch (ex: Exception) {
            false
        }
    }

    private fun Context.getAudioPath(uri: Uri): String {
        val data = arrayOf(MediaStore.Audio.Media.DATA)
        val loader = CursorLoader(this, uri, data, null, null, null)
        val cursor = loader.loadInBackground()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return columnIndex?.let { cursor.getString(it) }?:""
    }

    private fun rotateImageIfRequired( img: Bitmap, selectedImage: Uri): Bitmap? {
        val ei  = selectedImage.path?.let { ExifInterface(it) }
        return when (ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(img, 270)
            else -> img
        }
    }

    private fun saveBitmapToFile(file: File): File? {
        return try {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            var inputStream = FileInputStream(file)
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()
            val REQUIRED_SIZE = 75
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            var selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()
            file.createNewFile()
            val uri: Uri = Uri.fromFile(File(mCurrentPhotoPath))
            selectedBitmap = selectedBitmap?.let { rotateImageIfRequired( it, uri) }
            val outputStream = FileOutputStream(file)
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            file
        } catch (e: java.lang.Exception) {
            null
        }
    }

    private fun unregister(){
        pickVideo?.unregister()
        pickVideo?.unregister()
        videoIntentLauncher?.unregister()
        photoIntentLauncher?.unregister()
        galleryIntentLauncher?.unregister()
        audioIntentLauncher?.unregister()
    }
    enum class MediaTypes { GALLERY, VIDEO, AUDIO,PHOTO }
}

