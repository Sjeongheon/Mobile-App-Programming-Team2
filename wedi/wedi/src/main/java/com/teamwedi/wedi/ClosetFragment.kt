package com.teamwedi.wedi

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.appsearch.AppSearchResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.checkSelfPermission
import com.teamwedi.wedi.databinding.FragmentClosetBinding
import com.teamwedi.wedi.ml.ClothingClassifiertfliteModel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ClosetFragment : Fragment() {
    lateinit var camera: Button
    lateinit var gallery: Button
    lateinit var imageView: ImageView
    lateinit var result: TextView
    var imageSize = 96
    lateinit var binding: FragmentClosetBinding
    var container: ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        binding = FragmentClosetBinding.inflate(inflater, container, false)

        camera = binding.button
        gallery = binding.button2
        result = binding.result
        imageView = binding.imageView
        camera!!.setOnClickListener(View.OnClickListener {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context?.let { it1 -> checkSelfPermission(it1, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            ) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
                }
            }
        })
        gallery!!.setOnClickListener(View.OnClickListener {
            val cameraIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(cameraIntent, 1)
        })
        return binding.root
    }

    fun classifyImage(image: Bitmap?) {
        try {
            val model = context?.let { ClothingClassifiertfliteModel.newInstance(it) }

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), org.tensorflow.lite.DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image!!.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model!!.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.getOutputFeature0AsTensorBuffer()
            val confidences: FloatArray = outputFeature0.getFloatArray()
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf(
                "Accessories",
                "Apparel Set",
                "Bags",
                "Bath and Body",
                "Beauty Accessories",
                "Belts",
                "Bottomwear",
                "Dress",
                "Eyes",
                "Eyewear",
                "Flip Flops",
                "Fragrance",
                "Free Gifts",
                "Gloves",
                "Headwear",
                "Innerwear",
                "Jewellery",
                "Lips",
                "Loungewear and Nightwear",
                "Makeup",
                "Mufflers",
                "Nails",
                "Sandal",
                "Saree",
                "Scarves",
                "Shoe Accessories",
                "Shoes",
                "Skin",
                "Skin Care",
                "Socks",
                "Sports Accessories",
                "Ties",
                "Topwear",
                "Wallets",
                "Watches",
                "Water Bottle"
            )
            result!!.text = classes[maxPos]
            var s = ""
            var map : Map<String, Float> = mutableMapOf<String, Float>().apply {
                for( i in confidences.indices) this[classes[i]] = confidences[i]
            }
            map = map.toList().sortedByDescending { it.second }.toMap() as MutableMap
            for (i in 0..2) {
                s += map.toList()[i]
            }
            val confidence = binding.confidence
            confidence.setText(s)
            // Releases model resources if no longer used.
            model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                var image = data!!.extras!!["data"] as Bitmap?
                val dimension = Math.min(image!!.width, image.height)
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                imageView!!.setImageBitmap(image)
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
                classifyImage(image)
            } else {
                val dat = data!!.data
                var image: Bitmap? = null
                try {
                    image = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, dat)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageView!!.setImageBitmap(image)
                image = Bitmap.createScaledBitmap(image!!, imageSize, imageSize, false)
                classifyImage(image)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}