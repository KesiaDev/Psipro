package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.databinding.ItemAnotacaoBinding
import com.example.psipro.utils.AttachmentManager
import java.text.SimpleDateFormat
import java.util.Locale

class AnotacoesAdapter(
    private val onFavoriteClick: ((PatientNote) -> Unit)? = null,
    private val onItemClick: ((PatientNote) -> Unit)? = null
) : ListAdapter<PatientNote, AnotacoesAdapter.ViewHolder>(NoteDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private var attachmentManager: AttachmentManager? = null

    fun setAttachmentManager(manager: AttachmentManager) {
        attachmentManager = manager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnotacaoBinding.inflate(
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
        private val binding: ItemAnotacaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(note: PatientNote) {
            binding.anotacaoText.text = note.content
            binding.dataText.text = dateFormat.format(note.createdAt)
            
            // Configurar favorito
            binding.iconFavorite.alpha = if (note.isFavorite) 1.0f else 0.3f
            binding.iconFavorite.setOnClickListener {
                onFavoriteClick?.invoke(note)
            }
            
            binding.root.setOnClickListener {
                onItemClick?.invoke(note)
            }
            
            // Configurar anexos
            setupAttachments(note)
        }
        
        private fun setupAttachments(note: PatientNote) {
            attachmentManager?.let { manager ->
                val images = manager.getImageAttachments(note.imageAttachments)
                val audios = manager.getAudioAttachments(note.audioAttachments)
                
                // Mostrar indicador de imagens
                if (images.isNotEmpty()) {
                    binding.imageIndicator.visibility = android.view.View.VISIBLE
                    binding.imageCount.text = images.size.toString()
                } else {
                    binding.imageIndicator.visibility = android.view.View.GONE
                }
                
                // Mostrar indicador de áudios
                if (audios.isNotEmpty()) {
                    binding.audioIndicator.visibility = android.view.View.VISIBLE
                    binding.audioCount.text = audios.size.toString()
                } else {
                    binding.audioIndicator.visibility = android.view.View.GONE
                }
                
                // Mostrar área de anexos se houver algum
                if (images.isNotEmpty() || audios.isNotEmpty()) {
                    binding.attachmentsLayout.visibility = android.view.View.VISIBLE
                } else {
                    binding.attachmentsLayout.visibility = android.view.View.GONE
                }
            }
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<PatientNote>() {
        override fun areItemsTheSame(oldItem: PatientNote, newItem: PatientNote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatientNote, newItem: PatientNote): Boolean {
            return oldItem == newItem
        }
    }
} 