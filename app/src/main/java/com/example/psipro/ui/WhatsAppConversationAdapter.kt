package com.example.psipro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.WhatsAppConversation
import com.example.psipro.databinding.ItemWhatsappConversationBinding

class WhatsAppConversationAdapter : ListAdapter<WhatsAppConversation, WhatsAppConversationAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWhatsappConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemWhatsappConversationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(conversation: WhatsAppConversation) {
            binding.textMessage.text = conversation.message
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR"))
            binding.textTimestamp.text = sdf.format(conversation.sentAt)
            binding.textStatus.text = ""
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WhatsAppConversation>() {
        override fun areItemsTheSame(oldItem: WhatsAppConversation, newItem: WhatsAppConversation): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WhatsAppConversation, newItem: WhatsAppConversation): Boolean = oldItem == newItem
    }
} 