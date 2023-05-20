package com.example.myapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ml.SsdMobilenetV11Metadata1
import com.example.myapplication.utils.extensions.PERMISSION_CAMERA
import com.example.myapplication.utils.extensions.fullScreen
import com.example.myapplication.utils.extensions.getCameraManager
import com.example.myapplication.utils.extensions.isCameraPermissionGranted
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {

    companion object{
        val colors = listOf(
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
            Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
        )
    }

    lateinit var model: SsdMobilenetV11Metadata1
    private val binding: ActivityMainBinding by viewBinding()
    private  val labels:List<String> by lazy {
        FileUtil.loadLabels(this, "labels.txt")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreen()
        getPermissions()
        model = SsdMobilenetV11Metadata1.newInstance(this)
        Handler(HandlerThread("videoThread").let { it.start();it}.looper).also {
            setupSurfaceListener(getCameraManager(),it,buildImageProcessor(setupResizeOp()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    private fun buildImageProcessor(resizeOp: ResizeOp)= ImageProcessor.Builder().add(resizeOp).build()

    private fun setupResizeOp() =
        ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)

    private fun setupSurfaceListener(cameraManager:CameraManager,handler: Handler,imageProcessor: ImageProcessor){
        binding.textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean  = false
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera(cameraManager,handler)
            }
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                val bitmap = binding.textureView.bitmap!!
                val outputs = TensorImage.fromBitmap(bitmap)
                    .let{imageProcessor.process(it)}
                    .let{model.process(it)}
                val locations = outputs.locationsAsTensorBuffer.floatArray
                val classes = outputs.classesAsTensorBuffer.floatArray
                val scores = outputs.scoresAsTensorBuffer.floatArray
                val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)
                val h = mutable.height
                val w = mutable.width
                val paint = Paint().apply { textSize = h/15f;strokeWidth = h/85f }
                var x: Int
                scores.forEachIndexed { index, fl ->
                    x = index
                    x *= 4
                    if(fl > 0.5){
                        paint.color = colors[index]
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(
                            locations[x+1] * w,
                            locations[x] * h,
                            locations[x+3] * w,
                            locations[x+2] * h),
                            paint
                        )
                        paint.style = Paint.Style.FILL
                        canvas.drawText(
                            labels[classes[index].toInt()] +" "+fl.toString(),
                            locations[x+1] * w,
                            locations[x] * h, paint
                        )
                    }
                }
                binding.imageView.setImageBitmap(mutable)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraManager:CameraManager,handler: Handler){
        getPermissions{
            cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
                override fun onOpened(p0: CameraDevice) {
                   p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                       Surface( binding.textureView.surfaceTexture).also {
                           p0.createCaptureSession(listOf(it), object: CameraCaptureSession.StateCallback(){
                               override fun onConfigured(p0: CameraCaptureSession) {
                                   p0.setRepeatingRequest(build(), null, null)
                               }
                               override fun onConfigureFailed(p0: CameraCaptureSession) {
                               }
                           }, handler)
                           addTarget(it)
                       }
                   }
                }
                override fun onDisconnected(p0: CameraDevice) {}
                override fun onError(p0: CameraDevice, p1: Int) {}
            }, handler)
        }
    }

    private fun getPermissions(isGranted:()->Unit = {}){
        if(!isCameraPermissionGranted()) requestPermissions(arrayOf(PERMISSION_CAMERA), 101)
        else isGranted()
    }

    override fun onRequestPermissionsResult(  requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) getPermissions()
    }
}