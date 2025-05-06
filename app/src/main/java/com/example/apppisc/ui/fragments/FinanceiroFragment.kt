package com.example.apppisc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apppisc.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FinanceiroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_financeiro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exemplo de dados mockados
        val totalRecebido = 1500.00
        val aReceber = 300.00
        val sessoes = 12

        val resumoText = "Total recebido: R$ %.2f\nA receber: R$ %.2f\nSessões: %d".format(totalRecebido, aReceber, sessoes)
        view.findViewById<android.widget.TextView>(R.id.financeiroResumo)?.text = resumoText

        // Lista de registros mockados
        val registros = listOf(
            FinanceiroRegistro("Sessão 01", 120.0, "01/05/2024", "Pago"),
            FinanceiroRegistro("Sessão 02", 120.0, "08/05/2024", "Pendente"),
            FinanceiroRegistro("Sessão 03", 120.0, "15/05/2024", "Pago")
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.financeiroRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = FinanceiroAdapter(registros)
    }
} 