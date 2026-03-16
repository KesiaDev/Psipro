package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.AnotacaoSessao
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.data.repository.AnotacaoSessaoRepository
import com.psipro.app.data.repository.CobrancaSessaoRepository
import com.psipro.app.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AnotacaoSessaoViewModel @Inject constructor(
    private val repository: AnotacaoSessaoRepository,
    private val cobrancaRepository: CobrancaSessaoRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {
    private val _anotacoes = MutableStateFlow<List<AnotacaoSessao>>(emptyList())
    val anotacoes: StateFlow<List<AnotacaoSessao>> = _anotacoes.asStateFlow()

    private val _anotacaoSelecionada = MutableStateFlow<AnotacaoSessao?>(null)
    val anotacaoSelecionada: StateFlow<AnotacaoSessao?> = _anotacaoSelecionada.asStateFlow()

    private val _statusPagamentos = MutableStateFlow<Map<Long, StatusPagamento>>(emptyMap())
    val statusPagamentos: StateFlow<Map<Long, StatusPagamento>> = _statusPagamentos.asStateFlow()

    fun carregarAnotacoes(patientId: Long) {
        viewModelScope.launch {
            repository.getByPatientId(patientId).collect { anotacoes ->
                _anotacoes.value = anotacoes
            }
        }
        carregarStatusPagamentos(patientId)
    }

    fun carregarAnotacao(patientId: Long, numeroSessao: Int) {
        viewModelScope.launch {
            _anotacaoSelecionada.value = repository.getByPatientAndSession(patientId, numeroSessao)
        }
    }

    fun salvarAnotacao(
        patientId: Long,
        numeroSessao: Int,
        assuntos: String,
        estadoEmocional: String,
        intervencoes: String,
        tarefas: String,
        evolucao: String,
        observacoes: String,
        tipoSessaoId: Long?,
        valorSessao: Double?,
        anexos: String = "",
        metaTerapeutica: String = "",
        proximoAgendamento: String = ""
    ) {
        viewModelScope.launch {
            val anotacao = AnotacaoSessao(
                patientId = patientId,
                numeroSessao = numeroSessao,
                dataHora = Date(),
                assuntos = assuntos,
                estadoEmocional = estadoEmocional,
                intervencoes = intervencoes,
                tarefas = tarefas,
                evolucao = evolucao,
                observacoes = observacoes,
                anexos = anexos,
                metaTerapeutica = metaTerapeutica,
                proximoAgendamento = proximoAgendamento,
                updatedAt = Date()
            )
            val anotacaoId = repository.insert(anotacao)
            _anotacaoSelecionada.value = anotacao
            
            // Criar cobrança automática
            try {
                val valor = valorSessao ?: 0.0
                android.util.Log.d("AnotacaoSessaoVM", "Valor da sessão recebido: $valor")
                
                if (valor > 0) {
                    val dataVencimento = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 7) // Vencimento em 7 dias
                    }.time
                    
                    val cobranca = CobrancaSessao(
                        patientId = patientId,
                        anotacaoSessaoId = anotacaoId,
                        appointmentId = null, // Será preenchido se a anotação estiver vinculada a um agendamento
                        numeroSessao = numeroSessao,
                        valor = valor,
                        dataSessao = anotacao.dataHora,
                        dataVencimento = dataVencimento,
                        status = StatusPagamento.A_RECEBER,
                        metodoPagamento = "", // Será preenchido quando o pagamento for registrado
                        observacoes = "",
                        pixCopiaCola = "",
                        tipoSessaoId = tipoSessaoId,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    val cobrancaId = cobrancaRepository.insert(cobranca)
                    android.util.Log.d("AnotacaoSessaoVM", "✅ Cobrança criada com sucesso: ID=$cobrancaId, Valor=$valor, Paciente=$patientId, Sessão=$numeroSessao")
                } else {
                    android.util.Log.w("AnotacaoSessaoVM", "⚠️ Valor da sessão é zero ou nulo, cobrança não será criada")
                }
            } catch (e: Exception) {
                // Log do erro, mas não falha a operação principal
                android.util.Log.e("AnotacaoSessaoVM", "❌ Erro ao criar cobrança automática: ${e.message}", e)
            }
        }
    }

    fun editarAnotacao(anotacao: AnotacaoSessao) {
        viewModelScope.launch {
            val anotacaoAtualizada = anotacao.copy(updatedAt = Date())
            repository.update(anotacaoAtualizada)
            _anotacaoSelecionada.value = anotacaoAtualizada
        }
    }

    fun getProximoNumeroSessao(patientId: Long): Int {
        val maxSession = _anotacoes.value.maxOfOrNull { it.numeroSessao } ?: 0
        return maxSession + 1
    }

    fun excluirAnotacao(anotacaoId: Long) {
        viewModelScope.launch {
            try {
                // Primeiro, excluir a cobrança associada (se existir)
                cobrancaRepository.deleteByAnotacaoSessaoId(anotacaoId)
                
                // Depois, excluir a anotação
                repository.deleteById(anotacaoId)
                
                // Limpar anotação selecionada se for a mesma que foi excluída
                if (_anotacaoSelecionada.value?.id == anotacaoId) {
                    _anotacaoSelecionada.value = null
                }
            } catch (e: Exception) {
                // Log do erro
                println("Erro ao excluir anotação: ${e.message}")
            }
        }
    }

    fun carregarStatusPagamentos(patientId: Long) {
        viewModelScope.launch {
            val anotacoes = repository.getByPatientId(patientId)
            anotacoes.collect { lista ->
                val mapa = mutableMapOf<Long, StatusPagamento>()
                for (anotacao in lista) {
                    val cobranca = cobrancaRepository.getByAnotacaoSessao(anotacao.id) // anotacao.id nunca é null aqui
                    mapa[anotacao.id] = cobranca?.status ?: StatusPagamento.A_RECEBER
                }
                _statusPagamentos.value = mapa
            }
        }
    }

    fun marcarCobrancaComoPaga(anotacaoSessaoId: Long) {
        viewModelScope.launch {
            val cobranca = cobrancaRepository.getByAnotacaoSessao(anotacaoSessaoId)
            cobranca?.let {
                cobrancaRepository.marcarComoPago(it.id)
                // Atualizar statusPagamentos
                val mapa = _statusPagamentos.value.toMutableMap()
                mapa[anotacaoSessaoId] = StatusPagamento.PAGO
                _statusPagamentos.value = mapa
            }
        }
    }

    /**
     * Retorna o status da cobrança associada à anotação de sessão informada, ou null se não houver cobrança.
     */
    suspend fun getStatusCobrancaPorAnotacao(anotacaoId: Long): StatusPagamento? {
        return cobrancaRepository.getByAnotacaoSessao(anotacaoId)?.status
    }
} 



