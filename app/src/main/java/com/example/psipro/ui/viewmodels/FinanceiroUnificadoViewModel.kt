package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.CobrancaSessao
import com.example.psipro.data.entities.FinancialRecord
import com.example.psipro.data.entities.StatusPagamento
import com.example.psipro.data.repository.CobrancaSessaoRepository
import com.example.psipro.data.repository.FinancialRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FinanceiroUnificadoViewModel @Inject constructor(
    private val cobrancaRepository: CobrancaSessaoRepository,
    private val financialRecordRepository: FinancialRecordRepository
) : ViewModel() {

    private val _cobrancas = MutableStateFlow<List<CobrancaSessao>>(emptyList())
    val cobrancas: StateFlow<List<CobrancaSessao>> = _cobrancas.asStateFlow()

    private val _financialRecords = MutableStateFlow<List<FinancialRecord>>(emptyList())
    val financialRecords: StateFlow<List<FinancialRecord>> = _financialRecords.asStateFlow()

    private val _resumoFinanceiro = MutableStateFlow(ResumoFinanceiroUnificado())
    val resumoFinanceiro: StateFlow<ResumoFinanceiroUnificado> = _resumoFinanceiro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun carregarDadosFinanceiros() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Carregar dados de ambas as fontes
                combine(
                    cobrancaRepository.getByStatus(StatusPagamento.A_RECEBER),
                    financialRecordRepository.getAll()
                ) { cobrancas, records ->
                    _cobrancas.value = cobrancas
                    _financialRecords.value = records
                    calcularResumoFinanceiroUnificado(cobrancas, records)
                }.collect { }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados financeiros: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarDadosPorPeriodo(dataInicio: Date, dataFim: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Carregar cobranças por período
                cobrancaRepository.getByPeriodo(dataInicio, dataFim).collect { cobrancas ->
                    _cobrancas.value = cobrancas
                }
                
                // Carregar records por período (implementar quando necessário)
                financialRecordRepository.getAll().collect { records ->
                    val recordsFiltrados = records.filter { 
                        it.date >= dataInicio && it.date <= dataFim 
                    }
                    _financialRecords.value = recordsFiltrados
                    calcularResumoFinanceiroUnificado(_cobrancas.value, recordsFiltrados)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados por período: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calcularResumoFinanceiroUnificado(
        cobrancas: List<CobrancaSessao>,
        records: List<FinancialRecord>
    ) {
        // Calcular totais das cobranças
        val totalRecebidoCobrancas = cobrancas
            .filter { it.status == StatusPagamento.PAGO }
            .sumOf { it.valor }
        
        val totalAReceberCobrancas = cobrancas
            .filter { it.status == StatusPagamento.A_RECEBER || it.status == StatusPagamento.VENCIDO }
            .sumOf { it.valor }
        
        val countPendentesCobrancas = cobrancas.count { it.status == StatusPagamento.A_RECEBER }
        val countVencidasCobrancas = cobrancas.count { it.status == StatusPagamento.VENCIDO }

        // Calcular totais dos records financeiros
        val totalRecebidoRecords = records
            .filter { it.type == "RECEITA" }
            .sumOf { it.value }
        
        val totalDespesasRecords = records
            .filter { it.type == "DESPESA" }
            .sumOf { it.value }

        // Totais unificados
        val totalRecebido = totalRecebidoCobrancas + totalRecebidoRecords
        val totalAReceber = totalAReceberCobrancas
        val totalDespesas = totalDespesasRecords
        val resultadoPrevisto = totalAReceber - totalDespesas

        _resumoFinanceiro.value = ResumoFinanceiroUnificado(
            totalRecebido = totalRecebido,
            totalAReceber = totalAReceber,
            totalDespesas = totalDespesas,
            resultadoPrevisto = resultadoPrevisto,
            countPendentes = countPendentesCobrancas,
            countVencidas = countVencidasCobrancas,
            countCobrancas = cobrancas.size,
            countRecords = records.size
        )
    }

    fun marcarCobrancaComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                cobrancaRepository.marcarComoPago(cobrancaId)
                // Recarregar dados
                carregarDadosFinanceiros()
            } catch (e: Exception) {
                _error.value = "Erro ao marcar como pago: ${e.message}"
            }
        }
    }

    fun gerarMensagemWhatsApp(cobranca: CobrancaSessao): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        
        return """
            Olá! Aqui está o lembrete de pagamento da sessão ${cobranca.numeroSessao}.
            
            📅 Data da sessão: ${dateFormatter.format(cobranca.dataSessao)}
            💰 Valor: ${formatter.format(cobranca.valor)}
            📅 Vencimento: ${dateFormatter.format(cobranca.dataVencimento)}
            
            ${if (cobranca.pixCopiaCola.isNotEmpty()) "PIX: ${cobranca.pixCopiaCola}" else ""}
            
            Obrigado!
        """.trimIndent()
    }

    data class ResumoFinanceiroUnificado(
        val totalRecebido: Double = 0.0,
        val totalAReceber: Double = 0.0,
        val totalDespesas: Double = 0.0,
        val resultadoPrevisto: Double = 0.0,
        val countPendentes: Int = 0,
        val countVencidas: Int = 0,
        val countCobrancas: Int = 0,
        val countRecords: Int = 0
    )
} 