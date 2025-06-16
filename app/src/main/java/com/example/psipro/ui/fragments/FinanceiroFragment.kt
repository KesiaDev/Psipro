package com.example.psipro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.psipro.R
import com.example.psipro.databinding.FragmentFinanceiroBinding
import com.example.psipro.data.repository.FinancialRecordRepository
import com.example.psipro.data.entities.FinancialRecord
import com.example.psipro.ui.fragments.FinanceiroAdapter
import com.example.psipro.ui.fragments.FinanceiroRegistro
import com.example.psipro.ui.FinanceiroRegistroDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import com.example.psipro.ui.viewmodels.PatientViewModel

@AndroidEntryPoint
class FinanceiroFragment : Fragment() {
    @Inject lateinit var financialRecordRepository: FinancialRecordRepository
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val patientViewModel: PatientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_financeiro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.financeiroRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            financialRecordRepository.getAll().collectLatest { records ->
                val totalRecebido = records.filter { it.type == "RECEITA" }.sumOf { it.value }
                val aReceber = records.filter { it.type == "DESPESA" }.sumOf { it.value }
                val sessoes = records.size
                val resumoText = "Total recebido: R$ %.2f\nA receber: R$ %.2f\nSessões: %d".format(totalRecebido, aReceber, sessoes)
                view.findViewById<android.widget.TextView>(R.id.financeiroResumo)?.text = resumoText

                // Buscar todos os pacientes para mapear id -> nome
                val pacientes = patientViewModel.patients.value
                val pacienteMap = pacientes.associateBy { it.id }

                val registros = records.map { rec ->
                    val pacienteNome = pacienteMap[rec.patientId]?.name ?: "Paciente desconhecido"
                    val dataSessao = dateFormat.format(rec.date)
                    val descricaoLinha1 = pacienteNome
                    val descricaoLinha2 = "${rec.description} realizada em $dataSessao"
                    val descricaoLinha3 = buildString {
                        append("R$ %.2f".format(rec.value))
                        append("  •  ")
                        append(rec.type)
                    }
                    FinanceiroRegistro(
                        id = rec.id,
                        descricao = "$descricaoLinha1\n$descricaoLinha2\n$descricaoLinha3",
                        valor = rec.value,
                        data = dataSessao,
                        status = rec.type
                    )
                }
                recyclerView?.adapter = FinanceiroAdapter(
                    registros,
                    onItemClick = { registro ->
                        // Diálogo de edição/exclusão
                        showEditDeleteDialog(registro, records)
                    }
                )
            }
        }
    }

    private fun showEditDeleteDialog(registro: FinanceiroRegistro, records: List<FinancialRecord>) {
        val record = records.find { it.id == registro.id } ?: return
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        val input = android.widget.EditText(context)
        input.setText(registro.valor.toString())
        builder.setTitle("Editar valor da sessão")
        builder.setView(input)
        builder.setPositiveButton("Salvar") { _, _ ->
            val novoValor = input.text.toString().toDoubleOrNull() ?: registro.valor
            lifecycleScope.launch {
                financialRecordRepository.update(record.copy(value = novoValor))
            }
        }
        builder.setNegativeButton("Excluir") { _, _ ->
            lifecycleScope.launch {
                financialRecordRepository.delete(record)
            }
        }
        builder.setNeutralButton("Cancelar", null)
        builder.show()
    }
} 