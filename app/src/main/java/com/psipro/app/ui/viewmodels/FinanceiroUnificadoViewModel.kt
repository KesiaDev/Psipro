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
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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
        android.util.Log.d("FinanceiroViewModel", "Iniciando carregamento ULTRA-SEGURO")
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Carregamento ULTRA-SIMPLES com timeout
                kotlinx.coroutines.withTimeout(5000) { // 5 segundos máximo
                    val cobrancas = try {
                        cobrancaRepository.getByStatus(StatusPagamento.A_RECEBER).first()
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao carregar cobranças", e)
                        emptyList<CobrancaSessao>()
                    }
                    
                    val records = try {
                        financialRecordRepository.getAll().first()
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao carregar records", e)
                        emptyList<FinancialRecord>()
                    }
                    
                    // Atualizar UI na thread principal
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _cobrancas.value = cobrancas
                        _financialRecords.value = records
                    }
                    
                    // Calcular resumo ULTRA-SIMPLES
                    calcularResumoFinanceiroSimples(cobrancas, records)
                    
                    android.util.Log.d("FinanceiroViewModel", "Carregamento concluído com sucesso")
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                android.util.Log.e("FinanceiroViewModel", "Timeout no carregamento", e)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _error.value = "Carregamento demorou muito. Tente novamente."
                }
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro no carregamento", e)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _error.value = "Erro: ${e.message}"
                }
            } finally {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _isLoading.value = false
                }
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

    private fun calcularResumoFinanceiroSimples(
        cobrancas: List<CobrancaSessao>,
        records: List<FinancialRecord>
    ) {
        try {
            android.util.Log.d("FinanceiroViewModel", "Iniciando cálculo simples do resumo")
            
            // Calcular totais de forma simples e direta
            val totalRecebidoCobrancas = cobrancas
                .filter { it.status == StatusPagamento.PAGO }
                .sumOf { it.valor }
            
            val totalAReceberCobrancas = cobrancas
                .filter { it.status == StatusPagamento.A_RECEBER }
                .sumOf { it.valor }
            
            val countPendentesCobrancas = cobrancas
                .count { it.status == StatusPagamento.A_RECEBER }
            
            val countVencidasCobrancas = cobrancas
                .count { it.status == StatusPagamento.VENCIDO }

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

            android.util.Log.d("FinanceiroViewModel", "Resumo calculado - Total Recebido Cobranças: $totalRecebidoCobrancas, Records: $totalRecebidoRecords, Total: $totalRecebido")

            // Atualizar UI na thread principal
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
        } catch (e: Exception) {
            android.util.Log.e("FinanceiroViewModel", "Erro ao calcular resumo simples", e)
            _error.value = "Erro ao calcular resumo: ${e.message}"
        }
    }

    private fun calcularResumoFinanceiroUnificado(
        cobrancas: List<CobrancaSessao>,
        records: List<FinancialRecord>
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                android.util.Log.d("FinanceiroViewModel", "Iniciando cálculo do resumo")
                
                // Calcular totais das cobranças de forma assíncrona
                val totalRecebidoCobrancasDeferred = async { 
                    try {
                        cobrancaRepository.getTotalRecebidoGeral()
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao calcular total recebido", e)
                        0.0
                    }
                }
                
                val totalAReceberCobrancasDeferred = async { 
                    try {
                        cobrancaRepository.getTotalAReceber()
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao calcular total a receber", e)
                        0.0
                    }
                }
                
                val countPendentesCobrancasDeferred = async { 
                    try {
                        cobrancaRepository.getCountPendentes()
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao contar pendentes", e)
                        0
                    }
                }
                
                val countVencidasCobrancasDeferred = async { 
                    try {
                        cobrancaRepository.getCountByStatus(StatusPagamento.VENCIDO)
                    } catch (e: Exception) {
                        android.util.Log.e("FinanceiroViewModel", "Erro ao contar vencidas", e)
                        0
                    }
                }

                // Calcular totais dos records financeiros de forma otimizada
                val totalRecebidoRecords = records
                    .asSequence()
                    .filter { it.type == "RECEITA" }
                    .sumOf { it.value }
                
                val totalDespesasRecords = records
                    .asSequence()
                    .filter { it.type == "DESPESA" }
                    .sumOf { it.value }

                // Aguardar resultados das queries
                val totalRecebidoCobrancas = totalRecebidoCobrancasDeferred.await()
                val totalAReceberCobrancas = totalAReceberCobrancasDeferred.await()
                val countPendentesCobrancas = countPendentesCobrancasDeferred.await()
                val countVencidasCobrancas = countVencidasCobrancasDeferred.await()

                // Totais unificados
                val totalRecebido = totalRecebidoCobrancas + totalRecebidoRecords
                val totalAReceber = totalAReceberCobrancas
                val totalDespesas = totalDespesasRecords
                val resultadoPrevisto = totalAReceber - totalDespesas

                android.util.Log.d("FinanceiroViewModel", "Resumo calculado - Total Recebido Cobranças: $totalRecebidoCobrancas, Records: $totalRecebidoRecords, Total: $totalRecebido")

                // Atualizar UI na thread principal
                withContext(kotlinx.coroutines.Dispatchers.Main) {
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
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao calcular resumo", e)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _error.value = "Erro ao calcular resumo: ${e.message}"
                }
            }
        }
    }

    fun marcarCobrancaComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FinanceiroViewModel", "Marcando cobrança $cobrancaId como pago")
                cobrancaRepository.marcarComoPago(cobrancaId)
                
                // Forçar recarregamento imediato após mudança
                dadosCarregados = false
                ultimoCarregamento = 0L
                
                android.util.Log.d("FinanceiroViewModel", "Cobrança marcada como pago, recarregando dados...")
                carregarDadosFinanceiros()
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao marcar como pago", e)
                _error.value = "Erro ao marcar como pago: ${e.message}"
            }
        }
    }

    fun desmarcarCobrancaComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FinanceiroViewModel", "Desmarcando cobrança $cobrancaId")
                cobrancaRepository.desmarcarComoPago(cobrancaId)
                
                // Forçar recarregamento imediato após mudança
                dadosCarregados = false
                ultimoCarregamento = 0L
                
                android.util.Log.d("FinanceiroViewModel", "Cobrança desmarcada, recarregando dados...")
                carregarDadosFinanceiros()
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao desmarcar como pago", e)
                _error.value = "Erro ao desmarcar como pago: ${e.message}"
            }
        }
    }

    fun editarCobranca(cobrancaId: Long, valor: Double, observacoes: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FinanceiroViewModel", "Editando cobrança $cobrancaId")
                cobrancaRepository.editarCobranca(cobrancaId, valor, observacoes)
                
                // Forçar recarregamento imediato após mudança
                dadosCarregados = false
                ultimoCarregamento = 0L
                
                android.util.Log.d("FinanceiroViewModel", "Cobrança editada, recarregando dados...")
                carregarDadosFinanceiros()
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao editar cobrança", e)
                _error.value = "Erro ao editar cobrança: ${e.message}"
            }
        }
    }

    fun carregarPagamentosDoDia(data: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Por enquanto, vamos usar uma abordagem simples
                // TODO: Implementar query complexa quando possível
                android.util.Log.d("FinanceiroViewModel", "Carregamento de pagamentos do dia simplificado")
            } catch (e: Exception) {
                android.util.Log.e("FinanceiroViewModel", "Erro ao carregar pagamentos do dia", e)
                _error.value = "Erro ao carregar pagamentos do dia: ${e.message}"
            } finally {
                _isLoading.value = false
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



