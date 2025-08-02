package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.FinancialRecord
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.data.repository.CobrancaSessaoRepository
import com.psipro.app.data.repository.FinancialRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _periodoAtual = MutableStateFlow<Pair<Date, Date>?>(null)
    val periodoAtual: StateFlow<Pair<Date, Date>?> = _periodoAtual.asStateFlow()

    private var dadosCarregados = false
    private var ultimoCarregamento = 0L

    fun carregarDadosFinanceiros() {
        val agora = System.currentTimeMillis()
        val tempoDesdeUltimoCarregamento = agora - ultimoCarregamento
        
        android.util.Log.d("FinanceiroViewModel", "Tentando carregar dados. Dados carregados: $dadosCarregados, Tempo desde último: $tempoDesdeUltimoCarregamento")
        
        // Evitar carregamento desnecessário se já foi carregado recentemente (menos de 30 segundos)
        if (dadosCarregados && _cobrancas.value.isNotEmpty() && tempoDesdeUltimoCarregamento < 30000) {
            android.util.Log.d("FinanceiroViewModel", "Pulando carregamento - dados recentes")
            return
        }
        
        viewModelScope.launch {
            android.util.Log.d("FinanceiroViewModel", "Iniciando carregamento de dados")
            _isLoading.value = true
            try {
                // Carregar dados de ambas as fontes de forma independente
                val cobrancas = cobrancaRepository.getByStatus(StatusPagamento.A_RECEBER).first()
                val records = financialRecordRepository.getAll().first()
                
                android.util.Log.d("FinanceiroViewModel", "Dados carregados - Cobranças: ${cobrancas.size}, Records: ${records.size}")
                
                _cobrancas.value = cobrancas
                _financialRecords.value = records
                calcularResumoFinanceiroUnificado(cobrancas, records)
                dadosCarregados = true
                ultimoCarregamento = agora
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao carregar dados", e)
                _error.value = "Erro ao carregar dados financeiros: ${e.message}"
            } finally {
                _isLoading.value = false
                android.util.Log.d("FinanceiroViewModel", "Carregamento finalizado")
            }
        }
    }

    fun carregarDadosPorPeriodo(dataInicio: Date, dataFim: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            _periodoAtual.value = Pair(dataInicio, dataFim)
            
            try {
                // Carregar cobranças por período
                val cobrancas = cobrancaRepository.getByPeriodo(dataInicio, dataFim).first()
                _cobrancas.value = cobrancas
                
                // Carregar records por período
                val records = financialRecordRepository.getAll().first()
                val recordsFiltrados = records.filter { 
                    it.date >= dataInicio && it.date <= dataFim 
                }
                _financialRecords.value = recordsFiltrados
                
                calcularResumoFinanceiroUnificado(cobrancas, recordsFiltrados)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados por período: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarDadosPorStatus(status: StatusPagamento) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cobrancas = cobrancaRepository.getByStatus(status).first()
                _cobrancas.value = cobrancas
                calcularResumoFinanceiroUnificado(cobrancas, _financialRecords.value)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados por status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarCobrancasVencidas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cobrancas = cobrancaRepository.getVencidas(Date()).first()
                _cobrancas.value = cobrancas
                calcularResumoFinanceiroUnificado(cobrancas, _financialRecords.value)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças vencidas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarDadosPorPaciente(patientId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cobrancas = cobrancaRepository.getByPatientId(patientId).first()
                _cobrancas.value = cobrancas
                calcularResumoFinanceiroUnificado(cobrancas, _financialRecords.value)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados do paciente: ${e.message}"
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
                // Forçar recarregamento após mudança
                dadosCarregados = false
                ultimoCarregamento = 0L
                carregarDadosFinanceiros()
            } catch (e: Exception) {
                _error.value = "Erro ao marcar como pago: ${e.message}"
            }
        }
    }

    fun forcarRecarregamento() {
        dadosCarregados = false
        ultimoCarregamento = 0L
        carregarDadosFinanceiros()
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

    fun limparEstado() {
        _cobrancas.value = emptyList()
        _financialRecords.value = emptyList()
        _resumoFinanceiro.value = ResumoFinanceiroUnificado()
        _isLoading.value = false
        _error.value = null
        _periodoAtual.value = null
        dadosCarregados = false
        ultimoCarregamento = 0L
    }

    fun limparErro() {
        _error.value = null
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



