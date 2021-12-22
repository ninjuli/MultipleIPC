package com.ninjuli.bigobject.server

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn).setOnClickListener {
            EventBus.getDefault().post(MessageEvent("sendClient"))
            this@MainActivity.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val bundle = intent.extras
        bundle?.let {
            val imageView = findViewById<ImageView>(R.id.image)
            val imageBinder = it.getBinder("bitmap") as ImageBinder?
            imageBinder?.let {
                val bitMap = BitmapFactory.decodeByteArray(imageBinder.byteArray, 0, imageBinder.byteArray.size)
                runOnUiThread {
                    imageView.setImageBitmap(bitMap)
                }
            }
        }
    }
}