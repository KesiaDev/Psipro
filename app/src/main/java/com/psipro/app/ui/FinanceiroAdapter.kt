package com.psipro.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.psipro.app.R
import com.psipro.app.data.entities.FinancialRecord
import com.psipro.app.databinding.ItemFinanceiroBinding

class FinanceiroAdapter(
    private val registros: List<FinancialRecord>,
    private val onItemClick: ((FinancialRecord) -> Unit)? = null,
    private val onItemLongClick: ((FinancialRecord) -> Unit)? = null
) : RecyclerView.Adapter<FinanceiroAdapter.FinanceiroViewHolder>() {

    class FinanceiroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descricao: TextView = view.findViewById(R.id.itemDescricao)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceiroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_financeiro, parent, false)
        return FinanceiroViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceiroViewHolder, position: Int) {
        val registro = registros[position]
        holder.descricao.text = registro.description

        holder.itemView.setOnClickListener { onItemClick?.invoke(registro) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(registro)
            true
        }
    }

    override fun getItemCount() = registros.size
} 



