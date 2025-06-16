package com.example.psipro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.databinding.ItemAnotacaoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AnotacoesAdapter : ListAdapter<PatientNote, AnotacoesAdapter.AnotacaoViewHolder>(AnotacaoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnotacaoViewHolder {
        val binding = ItemAnotacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnotacaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnotacaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AnotacaoViewHolder(
        private val binding: ItemAnotacaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "BR"))

        fun bind(anotacao: PatientNote) {
            val resumo = if (anotacao.content.length > 60) anotacao.content.substring(0, 60) + "..." else anotacao.content
            binding.anotacaoText.text = resumo
            binding.dataText.text = dateFormat.format(anotacao.createdAt)
        }
    }

    private class AnotacaoDiffCallback : DiffUtil.ItemCallback<PatientNote>() {
        override fun areItemsTheSame(oldItem: PatientNote, newItem: PatientNote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatientNote, newItem: PatientNote): Boolean {
            return oldItem == newItem
        }
    }
} 