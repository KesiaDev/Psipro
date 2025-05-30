package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.PatientMessage
import com.example.psipro.databinding.ItemMensagemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PatientMessageAdapter : ListAdapter<PatientMessage, PatientMessageAdapter.ViewHolder>(MessageDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMensagemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMensagemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: PatientMessage) {
            binding.mensagemText.text = message.texto
            binding.dataMensagemText.text = dateFormat.format(message.data)
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<PatientMessage>() {
        override fun areItemsTheSame(oldItem: PatientMessage, newItem: PatientMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatientMessage, newItem: PatientMessage): Boolean {
            return oldItem == newItem
        }
    }
} 