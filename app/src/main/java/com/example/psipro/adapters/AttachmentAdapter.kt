package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.databinding.ItemAttachmentBinding
import java.io.File

data class AttachmentItem(
    val path: String,
    val type: AttachmentType,
    val fileName: String
)

enum class AttachmentType {
    IMAGE, AUDIO
}

class AttachmentAdapter(
    private val onRemoveClick: (AttachmentItem) -> Unit,
    private val onItemClick: (AttachmentItem) -> Unit
) : ListAdapter<AttachmentItem, AttachmentAdapter.ViewHolder>(AttachmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttachmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: AttachmentItem) {
            // Configurar ícone baseado no tipo
            val iconRes = when (item.type) {
                AttachmentType.IMAGE -> com.example.psipro.R.drawable.ic_camera_alt
                AttachmentType.AUDIO -> com.example.psipro.R.drawable.ic_mic
            }
            binding.iconAttachment.setImageResource(iconRes)
            
            // Configurar nome do arquivo
            binding.textFileName.text = item.fileName
            
            // Configurar informações do arquivo
            val file = File(item.path)
            val fileSize = if (file.exists()) {
                val sizeInBytes = file.length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                String.format("%.1f MB", sizeInMB)
            } else {
                "Arquivo não encontrado"
            }
            
            val typeText = when (item.type) {
                AttachmentType.IMAGE -> "Imagem"
                AttachmentType.AUDIO -> "Áudio"
            }
            
            binding.textFileInfo.text = "$typeText • $fileSize"
            
            // Configurar click listeners
            binding.root.setOnClickListener {
                onItemClick(item)
            }
            
            binding.btnRemove.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    private class AttachmentDiffCallback : DiffUtil.ItemCallback<AttachmentItem>() {
        override fun areItemsTheSame(oldItem: AttachmentItem, newItem: AttachmentItem): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: AttachmentItem, newItem: AttachmentItem): Boolean {
            return oldItem == newItem
        }
    }
} 