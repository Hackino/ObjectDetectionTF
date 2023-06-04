package com.example.myapplication.ui.detectors

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.example.myapplication.ml.SsdMobilenetV11Metadata1
import com.example.myapplication.extensions.loadLabelsFile
import com.example.myapplication.ui.detectors.base.BaseDetectorFragment
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage

class PreTrainedDetectorV1Fragment : BaseDetectorFragment() {

    private lateinit  var model: SsdMobilenetV11Metadata1

    override fun closeModel() { model.close() }

    override fun getLabelsFile(): MutableList<String> =
        requireContext().loadLabelsFile("pretrained_lables.txt")

    override fun getOutput(bitmap: Bitmap, imageProcessor: ImageProcessor): ArrayList<FloatArray> {
        return TensorImage.fromBitmap(bitmap)
            .let{imageProcessor.process(it)}
            .let{  model.process(it)}.let {
                arrayListOf<FloatArray>().apply {
                    add(it.locationsAsTensorBuffer.floatArray)
                    add( it.classesAsTensorBuffer.floatArray)
                    add( it.scoresAsTensorBuffer.floatArray)
                }
            }
    }

    override fun getImageHeight(): Int  =300

    override fun getImageWidth(): Int = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        model = SsdMobilenetV11Metadata1.newInstance(requireContext())
        super.onCreate(savedInstanceState)
    }

}