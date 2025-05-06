package com.example.apppisc.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.R

class FinanceiroAdapter(private val registros: List<FinanceiroRegistro>) :
    RecyclerView.Adapter<FinanceiroAdapter.FinanceiroViewHolder>() {

    class FinanceiroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descricao: TextView = view.findViewById(R.id.itemDescricao)
        val valor: TextView = view.findViewById(R.id.itemValor)
        val data: TextView = view.findViewById(R.id.itemData)
        val status: TextView = view.findViewById(R.id.itemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceiroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_financeiro, parent, false)
        return FinanceiroViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceiroViewHolder, position: Int) {
        val registro = registros[position]
        holder.descricao.text = registro.descricao
        holder.valor.text = "R$ %.2f".format(registro.valor)
        holder.data.text = registro.data
        holder.status.text = registro.status
    }

    override fun getItemCount() = registros.size
} 