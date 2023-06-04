package com.example.myapplication.ui.detectors.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DetectorFragmentBinding
import com.example.myapplication.manger.MediaManager
import com.example.myapplication.pref.PreferenceCacheImpl
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.utils.selectImageDialog
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.Locale


abstract class BaseDetectorFragment:Fragment(R.layout.detector_fragment) {

    companion object{
        val colors = listOf(
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
            Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
        )
    }

    private val binding:DetectorFragmentBinding by viewBinding()
    private lateinit var labels:List<String>
    private var mediaManager: MediaManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaManager = MediaManager(fragment = this, isActivity = false){ type,path, uri ->
            path?.let {
                if (it.isEmpty())return@let
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeFile(path, options)
                setupSurfaceListener(bitmap,buildImageProcessor(setupResizeOp()))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        labels = getLabelsFile()
        binding.btnImg.setOnClickListener { context?.selectImageDialog(mediaManager) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeModel()
    }

    abstract fun closeModel()

    abstract fun getLabelsFile():List<String>

    abstract fun getOutput(bitmap: Bitmap,imageProcessor: ImageProcessor):ArrayList<FloatArray>

    abstract fun getImageHeight():Int

    abstract fun getImageWidth():Int

    private fun setupSurfaceListener(bitmap: Bitmap,imageProcessor: ImageProcessor){
        val out = getOutput(bitmap,imageProcessor)
        val locations = out[0]
        val classes = out[1]
        val scores = out[2]
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val h = mutable.height
        val w = mutable.width
        val paint = Paint().apply { textSize = h/15f;strokeWidth = h/85f }
        var x: Int
        Log.e("hackino","blabladfa")

        scores.filter {it>0.5}.forEachIndexed{ index, fl ->
            x = index
            x *= 4

            if(fl > 0.5){
                paint.color = colors[index]
                paint.style = Paint.Style.STROKE
                Log.e("hackino","filtering")

                MainActivity.List.filter { it.name ==  labels[classes[index].toInt()] }.let {
                    Log.e("hackino","filtered")

                    Log.e("hackino","${it}")
                    if (it.isEmpty()) {
                        canvas.drawRect(
                            RectF(
                                locations[x + 1] * w,
                                locations[x] * h,
                                locations[x + 3] * w,
                                locations[x + 2] * h
                            ),
                            paint
                        )
                        paint.style = Paint.Style.FILL
                        canvas.setupDrawingText(
                            labels[classes[index].toInt()],
                            mutable,
                            fl,
                            paint,
                            x,
                            locations
                        )
                    }
                }
            }
        }
        binding.imageView.setImageBitmap(mutable)
    }

    private fun Canvas.setupDrawingText(text: String,bitmap: Bitmap,fl:Float,paint: Paint,x:Int,locations:FloatArray){
        initFirebase(text){
            drawText(
                "$it $fl",
                locations[x+1] * bitmap.width,
                locations[x] * bitmap.height, paint
            )
        }
    }

    private fun buildImageProcessor(resizeOp: ResizeOp)= ImageProcessor.Builder().add(resizeOp).build()

    private fun setupResizeOp() = ResizeOp(getImageHeight(), getImageWidth(), ResizeOp.ResizeMethod.BILINEAR)

    private fun initFirebase(text: String,action:(String)->Unit){
        PreferenceCacheImpl.getLanguageCode()?.let {
            TranslatorOptions.Builder()
                .setSourceLanguage(Locale.getDefault().language)
                .setTargetLanguage(it)
                .build().also {options-> Translation.getClient(options).downloadModel(text,action) }
        }?:action(text)
    }

    private fun Translator.downloadModel(text: String,action:(String)->Unit){
       val options = DownloadConditions.Builder().requireWifi().build()
        downloadModelIfNeeded(options).addOnSuccessListener {
            translate(text)
                .addOnSuccessListener { translatedText -> action(translatedText);close() }
                .addOnFailureListener { exception -> action(text);close() }
        }.addOnFailureListener { exception -> action(text);close() }
    }
}