package com.example.psipro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.PatientNote
import java.text.SimpleDateFormat
import java.util.*

class AnotacaoAdapter : RecyclerView.Adapter<AnotacaoAdapter.AnotacaoViewHolder>() {
    private val anotacoes = mutableListOf<PatientNote>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    class AnotacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val anotacaoText: TextView = view.findViewById(R.id.anotacaoText)
        val dataText: TextView = view.findViewById(R.id.dataText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnotacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anotacao, parent, false)
        return AnotacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnotacaoViewHolder, position: Int) {
        val anotacao = anotacoes[position]
        holder.anotacaoText.text = anotacao.texto
        holder.dataText.text = dateFormat.format(anotacao.data)
    }

    override fun getItemCount() = anotacoes.size

    fun atualizarAnotacoes(novasAnotacoes: List<PatientNote>) {
        anotacoes.clear()
        anotacoes.addAll(novasAnotacoes)
        notifyDataSetChanged()
    }
} 