package com.ven.assists.simple

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ven.assists.simple.databinding.ActivityImageGalleryBinding
import com.ven.assists.simple.databinding.ItemImageBinding

class ImageGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageGalleryBinding
    private lateinit var adapter: ImageAdapter
    private var imagePaths: ArrayList<String> = ArrayList()

    companion object {
        private const val EXTRA_IMAGE_PATHS = "extra_image_paths"

        fun start(context: Context, imagePaths: ArrayList<String>) {
            val intent = Intent(context, ImageGalleryActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGE_PATHS, imagePaths)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传入的图片路径列表
        imagePaths = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS) ?: ArrayList()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ImageAdapter(imagePaths)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3) // 设置为3列
        binding.recyclerView.adapter = adapter
    }

    inner class ImageAdapter(private val imagePaths: List<String>) :
        RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ImageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imagePath = imagePaths[position]

            // 使用Glide加载图片
            Glide.with(holder.itemView.context)
                .load(imagePath)
                .centerCrop()
                .into(holder.binding.imageView)
        }

        override fun getItemCount(): Int = imagePaths.size
    }
} 