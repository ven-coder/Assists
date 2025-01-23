package com.ven.assists.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ven.assists.simple.databinding.ScreenshotReviewBinding

class ScreenshotReviewActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenshotReviewBinding.inflate(layoutInflater).apply {

            val path = intent.getStringExtra("path")
            Glide.with(this@ScreenshotReviewActivity).load(path).into(ivImage)

            setContentView(root)
        }
    }

}