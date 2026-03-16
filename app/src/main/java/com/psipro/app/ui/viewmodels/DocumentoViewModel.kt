package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.TipoDocumento
import com.psipro.app.data.repository.DocumentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import com.psipro.app.data.AppDatabase
import com.psipro.app.data.entities.Patient
import com.psipro.app.data.dao.PatientDao
import android.content.Context

@HiltViewModel
class DocumentoViewModel @Inject constructor(
    private val repository: DocumentoRepository,
    private val patientDao: PatientDao,
    private val context: Context
) : ViewModel() {
    
    private val _documentos = MutableStateFlow<List<Documento>>(emptyList())
    val documentos: StateFlow<List<Documento>> = _documentos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _documentoSelecionado = MutableStateFlow<Documento?>(null)
    val documentoSelecionado: StateFlow<Documento?> = _documentoSelecionado.asStateFlow()
    
    fun carregarDocumentos(patientId: Long) {
        viewModelScope.launch {
            try {
                val documentos = repository.getByPatientId(patientId).first()
                _documentos.value = documentos
            } catch (e: Exception) {
                _error.value = "Erro ao carregar documentos: ${e.message}"
            }
        }
    }

    suspend fun getPatientById(patientId: Long): Patient? {
        return try {
            // Buscar paciente do banco de dados usando o DAO injetado
            val patient = patientDao.getPatientById(patientId)
            Log.d("DocumentoViewModel", "Paciente encontrado: ${patient?.name} - CPF: ${patient?.cpf}")
            patient
        } catch (e: Exception) {
            Log.e("DocumentoViewModel", "Erro ao buscar paciente", e)
            null
        }
    }
    
    fun getPsychologistData(): Pair<String, String> {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val nome = prefs.getString("profile_name", "Profissional") ?: "Profissional"
        val crp = prefs.getString("profile_crp", "CRP") ?: "CRP"
        return Pair(nome, crp)
    }
    
    fun getPsychologistLocation(): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("profile_address", "Local não informado") ?: "Local não informado"
    }
    
    fun criarDocumento(
        patientId: Long,
        titulo: String,
        tipo: TipoDocumento,
        conteudo: String,
        template: String = ""
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentoViewModel", "Criando documento: $titulo, tipo: $tipo, patientId: $patientId")
                
                val documento = Documento(
                    patientId = patientId,
                    titulo = titulo,
                    tipo = tipo,
                    conteudo = conteudo,
                    conteudoOriginal = template,
                    dataCriacao = Date(),
                    dataModificacao = Date()
                )
                
                val id = repository.insert(documento)
                android.util.Log.d("DocumentoViewModel", "Documento criado com ID: $id")
                
                _error.value = null
            } catch (e: Exception) {
                android.util.Log.e("DocumentoViewModel", "Erro ao criar documento", e)
                _error.value = "Erro ao criar documento: ${e.message}"
            }
        }
    }

    fun criarDocumentoComDadosPaciente(
        patientId: Long,
        titulo: String,
        tipo: TipoDocumento,
        nomePaciente: String,
        cpfPaciente: String,
        dataNascimento: String,
        nomePsicologo: String,
        crpPsicologo: String,
        local: String
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentoViewModel", "Criando documento com dados do paciente: $titulo")
                
                val template = getTemplates()[tipo] ?: ""
                val templatePreenchido = preencherTemplateComDadosPaciente(
                    template = template,
                    nomePaciente = nomePaciente,
                    cpfPaciente = cpfPaciente,
                    dataNascimento = dataNascimento,
                    nomePsicologo = nomePsicologo,
                    crpPsicologo = crpPsicologo,
                    local = local
                )
                
                val documento = Documento(
                    patientId = patientId,
                    titulo = titulo,
                    tipo = tipo,
                    conteudo = templatePreenchido,
                    conteudoOriginal = template,
                    dataCriacao = Date(),
                    dataModificacao = Date()
                )
                
                val id = repository.insert(documento)
                android.util.Log.d("DocumentoViewModel", "Documento criado com ID: $id")
                
                _error.value = null
            } catch (e: Exception) {
                android.util.Log.e("DocumentoViewModel", "Erro ao criar documento", e)
                _error.value = "Erro ao criar documento: ${e.message}"
            }
        }
    }
    
    fun atualizarDocumento(documento: Documento) {
        viewModelScope.launch {
            try {
                val documentoAtualizado = documento.copy(
                    dataModificacao = Date(),
                    dirty = true
                )
                repository.update(documentoAtualizado)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar documento: ${e.message}"
            }
        }
    }
    
    fun excluirDocumento(documento: Documento) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentoViewModel", "Excluindo documento: ${documento.titulo}")
                repository.delete(documento)
                _error.value = null
            } catch (e: Exception) {
                android.util.Log.e("DocumentoViewModel", "Erro ao excluir documento", e)
                _error.value = "Erro ao excluir documento: ${e.message}"
            }
        }
    }

    fun compartilharDocumento(documento: Documento, context: android.content.Context) {
        try {
            android.util.Log.d("DocumentoViewModel", "Compartilhando documento: ${documento.titulo}")
            
            // Criar intent para compartilhamento
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/html"
                putExtra(android.content.Intent.EXTRA_SUBJECT, documento.titulo)
                putExtra(android.content.Intent.EXTRA_TEXT, documento.conteudo)
            }
            
            // Iniciar activity de compartilhamento
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar Documento"))
            
        } catch (e: Exception) {
            android.util.Log.e("DocumentoViewModel", "Erro ao compartilhar documento", e)
            _error.value = "Erro ao compartilhar documento: ${e.message}"
        }
    }

    fun editarDocumento(documento: Documento, novoConteudo: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DocumentoViewModel", "Editando documento: ${documento.titulo}")
                
                val documentoAtualizado = documento.copy(
                    conteudo = novoConteudo,
                    dataModificacao = Date(),
                    dirty = true
                )
                
                repository.update(documentoAtualizado)
                _error.value = null
            } catch (e: Exception) {
                android.util.Log.e("DocumentoViewModel", "Erro ao editar documento", e)
                _error.value = "Erro ao editar documento: ${e.message}"
            }
        }
    }
    
    fun assinarPaciente(documentoId: Long, assinatura: String) {
        viewModelScope.launch {
            try {
                repository.assinarPaciente(documentoId, assinatura)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao assinar documento: ${e.message}"
            }
        }
    }
    
    fun assinarProfissional(documentoId: Long, assinatura: String) {
        viewModelScope.launch {
            try {
                repository.assinarProfissional(documentoId, assinatura)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao assinar documento: ${e.message}"
            }
        }
    }
    
    fun selecionarDocumento(documento: Documento?) {
        _documentoSelecionado.value = documento
    }
    
    fun limparErro() {
        _error.value = null
    }
    
    fun getTemplates(): Map<TipoDocumento, String> {
        return mapOf(
            TipoDocumento.TERMO_CONSENTIMENTO to getTemplateConsentimento(),
            TipoDocumento.TERMO_CONFIDENCIALIDADE to getTemplateConfidencialidade(),
            TipoDocumento.ENCAMINHAMENTO_PSICOLOGICO to getTemplateEncaminhamento(),
            TipoDocumento.DECLARACAO_COMPARECIMENTO to getTemplateDeclaracao(),
            TipoDocumento.SOLICITACAO_EXAMES to getTemplateSolicitacao(),
            TipoDocumento.AUTORIZACAO_IMAGEM to getTemplateAutorizacao(),
            TipoDocumento.DOCUMENTO_PERSONALIZADO to getTemplatePersonalizado()
        )
    }

    fun preencherTemplateComDadosPaciente(
        template: String,
        nomePaciente: String,
        cpfPaciente: String,
        dataNascimento: String,
        nomePsicologo: String,
        crpPsicologo: String,
        local: String
    ): String {
        val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        
        return template
            .replace("{NOME_PACIENTE}", nomePaciente)
            .replace("{CPF_PACIENTE}", cpfPaciente)
            .replace("{DATA_NASCIMENTO}", dataNascimento)
            .replace("{NOME_PSICOLOGO}", nomePsicologo)
            .replace("{CRP_PSICOLOGO}", crpPsicologo)
            .replace("{LOCAL}", local)
            .replace("{DATA_ATUAL}", dataAtual)
    }
    
    private fun getTemplateConsentimento(): String {
        return """
            <h2>TERMO DE CONSENTIMENTO LIVRE E ESCLARECIDO</h2>
            
            <p>Eu, {NOME_PACIENTE}, inscrito(a) no CPF sob o nº {CPF_PACIENTE}, declaro que fui devidamente informado(a) pelo(a) profissional {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, sobre os objetivos, procedimentos, riscos e benefícios envolvidos no processo terapêutico.</p>
            
            <p>Declaro estar ciente de que a participação é voluntária, podendo ser interrompida a qualquer momento, sem prejuízo.</p>
            
            <p><strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Paciente:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
    
    private fun getTemplateConfidencialidade(): String {
        return """
            <h2>TERMO DE CONFIDENCIALIDADE</h2>
            
            <p>Eu, {NOME_PACIENTE}, CPF {CPF_PACIENTE}, declaro estar ciente de que todas as informações compartilhadas nas sessões de atendimento psicológico com {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, são confidenciais.</p>
            
            <p>Autorizo o uso de dados apenas para fins técnicos e científicos, resguardando sempre minha identidade e privacidade.</p>
            
            <p><strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Paciente:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
    
    private fun getTemplateEncaminhamento(): String {
        return """
            <h2>ENCAMINHAMENTO PSICOLÓGICO</h2>
            
            <p>Encaminhamos o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, para avaliação/atendimento junto ao(à) profissional/especialidade: ____________________.</p>
            
            <p><strong>Motivo do encaminhamento:</strong> _____________________________________________________________</p>
            
            <p><strong>Atenciosamente,</strong></p>
            
            <p><strong>{NOME_PSICOLOGO}</strong><br>
            CRP {CRP_PSICOLOGO}<br>
            <strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
    
    private fun getTemplateDeclaracao(): String {
        return """
            <h2>DECLARAÇÃO DE COMPARECIMENTO</h2>
            
            <p>Declaro, para os devidos fins, que o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, compareceu à sessão de atendimento psicológico no dia {DATA_ATUAL}, com duração aproximada de 50 minutos.</p>
            
            <p><strong>{NOME_PSICOLOGO}</strong><br>
            CRP {CRP_PSICOLOGO}<br>
            <strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
    
    private fun getTemplateSolicitacao(): String {
        return """
            <h2>SOLICITAÇÃO DE EXAMES</h2>
            
            <p>Solicitamos que o(a) paciente {NOME_PACIENTE}, CPF {CPF_PACIENTE}, realize os seguintes exames/avaliações: ________________________________.</p>
            
            <p><strong>Motivo da solicitação:</strong> _____________________________________________________________</p>
            
            <p><strong>{NOME_PSICOLOGO}</strong><br>
            CRP {CRP_PSICOLOGO}<br>
            <strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
    
    private fun getTemplateAutorizacao(): String {
        return """
            <h2>AUTORIZAÇÃO DE USO DE IMAGEM</h2>
            
            <p>Eu, {NOME_PACIENTE}, CPF {CPF_PACIENTE}, autorizo o uso de minha imagem, voz ou registros gráficos capturados durante as atividades realizadas com {NOME_PSICOLOGO}, CRP {CRP_PSICOLOGO}, para fins exclusivamente profissionais ou educativos, garantindo o sigilo e anonimato quando necessário.</p>
            
            <p><strong>{LOCAL}, {DATA_ATUAL}</strong></p>
            
            <p><strong>Assinatura do Paciente:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }

    private fun getTemplatePersonalizado(): String {
        return """
            <h2>DOCUMENTO PERSONALIZADO</h2>
            
            <p><strong>Paciente:</strong> {NOME_PACIENTE}</p>
            <p><strong>CPF:</strong> {CPF_PACIENTE}</p>
            <p><strong>Data de Nascimento:</strong> {DATA_NASCIMENTO}</p>
            <p><strong>Profissional:</strong> {NOME_PSICOLOGO}</p>
            <p><strong>CRP:</strong> {CRP_PSICOLOGO}</p>
            <p><strong>Data:</strong> {DATA_ATUAL}</p>
            
            <p><strong>Conteúdo Personalizado:</strong></p>
            <p>{{CONTEUDO_PERSONALIZADO}}</p>
            
            <p><strong>Assinatura do Paciente:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
            
            <p><strong>Assinatura do Profissional:</strong></p>
            <div style="border: 1px solid #ccc; height: 100px; margin: 10px 0;"></div>
        """.trimIndent()
    }
} 



