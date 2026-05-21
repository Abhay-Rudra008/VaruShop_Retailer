package com.example.varushopretailer.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.varushopretailer.R
import com.example.varushopretailer.databinding.ItemAddImageBinding
import com.example.varushopretailer.databinding.ItemImagePickerBinding

class ImagePickerAdapter(
    private val onRemoveClick: (Int) -> Unit, private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var images = mutableListOf<Uri>()
    private val MAX_IMAGES = 5

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_IMAGE = 1
    }

    fun submitList(newImages: List<Uri>) {
        val diffCallback = ImageDiffCallback(this.images, newImages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.images.clear()
        this.images.addAll(newImages)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < images.size) TYPE_IMAGE else TYPE_ADD
    }

    override fun getItemCount(): Int {
        return if (images.size >= MAX_IMAGES) images.size else images.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD) {
            val binding = ItemAddImageBinding.inflate(inflater, parent, false)
            AddViewHolder(binding)
        } else {
            val binding = ItemImagePickerBinding.inflate(inflater, parent, false)
            ImageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                val uri = images[position]

                holder.binding.ivPickedImage.load(uri) {
                    crossfade(true)
                    placeholder(R.color.edit_text_bg)
                    transformations(RoundedCornersTransformation(12f))
                }

                holder.binding.ivRemoveImage.setOnClickListener {
                    val currentPos = holder.bindingAdapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        onRemoveClick(currentPos)
                    }
                }
            }

            is AddViewHolder -> {
                holder.binding.layoutAddImage.setOnClickListener { onAddClick() }
            }
        }
    }

    inner class ImageViewHolder(val binding: ItemImagePickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AddViewHolder(val binding: ItemAddImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ImageDiffCallback(
        private val oldList: List<Uri>, private val newList: List<Uri>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos] == newList[newPos]

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos] == newList[newPos]
    }
}