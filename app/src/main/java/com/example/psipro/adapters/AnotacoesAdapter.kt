package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.databinding.ItemAnotacaoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AnotacoesAdapter : ListAdapter<PatientNote, AnotacoesAdapter.ViewHolder>(NoteDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

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