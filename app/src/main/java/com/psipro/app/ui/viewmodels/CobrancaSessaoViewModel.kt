package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.data.repository.CobrancaSessaoRepository
import com.psipro.app.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CobrancaSessaoViewModel @Inject constructor(
    private val cobrancaRepository: CobrancaSessaoRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _cobrancas = MutableStateFlow<List<CobrancaSessao>>(emptyList())
    val cobrancas: StateFlow<List<CobrancaSessao>> = _cobrancas.asStateFlow()

    private val _resumoFinanceiro = MutableStateFlow(ResumoFinanceiro())
    val resumoFinanceiro: StateFlow<ResumoFinanceiro> = _resumoFinanceiro.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun carregarCobrancasPorPaciente(patientId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cobrancaRepository.getByPatientId(patientId).collect { cobrancas ->
                    _cobrancas.value = cobrancas
                    calcularResumoFinanceiroPaciente(cobrancas)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun carregarCobrancasPorStatus(status: StatusPagamento) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cobrancaRepository.getByStatus(status).collect { cobrancas ->
                    _cobrancas.value = cobrancas
                    calcularResumoFinanceiro()
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarCobrancasPorPeriodo(dataInicio: Date, dataFim: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                cobrancaRepository.getByPeriodo(dataInicio, dataFim).collect { cobrancas ->
                    _cobrancas.value = cobrancas
                    calcularResumoFinanceiro()
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun marcarComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                cobrancaRepository.marcarComoPago(cobrancaId)
                // Recarregar dados
                val currentCobrancas = _cobrancas.value
                if (currentCobrancas.isNotEmpty()) {
                    carregarCobrancasPorPaciente(currentCobrancas.first().patientId)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao marcar como pago: ${e.message}"
            }
        }
    }

    fun desmarcarCobrancaComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                cobrancaRepository.desmarcarComoPago(cobrancaId)
                // Recarregar dados
                val currentCobrancas = _cobrancas.value
                if (currentCobrancas.isNotEmpty()) {
                    carregarCobrancasPorPaciente(currentCobrancas.first().patientId)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao desmarcar como pago: ${e.message}"
            }
        }
    }

    fun criarCobrancaAutomatica(
        patientId: Long,
        anotacaoSessaoId: Long,
        numeroSessao: Int,
        valor: Double,
        dataSessao: Date
    ) {
        viewModelScope.launch {
            try {
                val dataVencimento = Calendar.getInstance().apply {
                    time = dataSessao
                    add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                }.time

                val cobranca = CobrancaSessao(
                    patientId = patientId,
                    anotacaoSessaoId = anotacaoSessaoId,
                    numeroSessao = numeroSessao,
                    valor = valor,
                    dataSessao = dataSessao,
                    dataVencimento = dataVencimento,
                    status = StatusPagamento.A_RECEBER
                )
                
                cobrancaRepository.insert(cobranca)
            } catch (e: Exception) {
                _error.value = "Erro ao criar cobrança: ${e.message}"
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
            
            ${if (cobranca.pixCopiaCola.isNotEmpty()) "PIX: $cobranca.pixCopiaCola" else ""}
            
            Obrigado!
        """.trimIndent()
    }

    private fun calcularResumoFinanceiroPaciente(cobrancas: List<CobrancaSessao>) {
        val totalRecebido = cobrancas.filter { it.status == StatusPagamento.PAGO }.sumOf { it.valor }
        val totalAReceber = cobrancas.filter { it.status == StatusPagamento.A_RECEBER || it.status == StatusPagamento.VENCIDO }.sumOf { it.valor }
        val countPendentes = cobrancas.count { it.status == StatusPagamento.A_RECEBER }
        val countVencidas = cobrancas.count { it.status == StatusPagamento.VENCIDO }

        _resumoFinanceiro.value = ResumoFinanceiro(
            totalRecebido = totalRecebido,
            totalAReceber = totalAReceber,
            countPendentes = countPendentes,
            countVencidas = countVencidas
        )
    }

    private fun calcularResumoFinanceiro() {
        viewModelScope.launch {
            try {
                val totalRecebido = cobrancaRepository.getTotalRecebidoGeral()
                val totalAReceber = cobrancaRepository.getTotalAReceber()
                val countPendentes = cobrancaRepository.getCountPendentes()
                val countVencidas = cobrancaRepository.getCountByStatus(StatusPagamento.VENCIDO)

                _resumoFinanceiro.value = ResumoFinanceiro(
                    totalRecebido = totalRecebido,
                    totalAReceber = totalAReceber,
                    countPendentes = countPendentes,
                    countVencidas = countVencidas
                )
            } catch (e: Exception) {
                _error.value = "Erro ao calcular resumo: ${e.message}"
            }
        }
    }

    fun limparErro() {
        _error.value = null
    }

    data class ResumoFinanceiro(
        val totalRecebido: Double = 0.0,
        val totalAReceber: Double = 0.0,
        val countPendentes: Int = 0,
        val countVencidas: Int = 0
    )
} 



