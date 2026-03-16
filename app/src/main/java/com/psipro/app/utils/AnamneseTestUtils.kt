package com.psipro.app.utils

import com.psipro.app.data.entities.AnamneseCampo
import com.psipro.app.data.entities.AnamneseModel

object AnamneseTestUtils {
    
    /**
     * Cria modelos de exemplo para teste
     */
    fun createSampleModels(): List<AnamneseModel> {
        return listOf(
            AnamneseModel(id = 1, nome = "Anamnese Adulto", isDefault = true),
            AnamneseModel(id = 2, nome = "Anamnese Infantil", isDefault = true),
            AnamneseModel(id = 3, nome = "Anamnese Casal", isDefault = true),
            AnamneseModel(id = 4, nome = "Anamnese Idosos", isDefault = false),
            AnamneseModel(id = 5, nome = "Anamnese Adolescentes", isDefault = false)
        )
    }
    
    /**
     * Cria campos de exemplo para o modelo "Anamnese Adulto"
     */
    fun createAdultoFields(): List<AnamneseCampo> {
        return listOf(
            AnamneseCampo(
                id = 1,
                modeloId = 1,
                tipo = "TITULO",
                label = "DADOS PESSOAIS",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 2,
                modeloId = 1,
                tipo = "TEXTO_CURTO",
                label = "Nome Completo",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 3,
                modeloId = 1,
                tipo = "DATA",
                label = "Data de Nascimento",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 4,
                modeloId = 1,
                tipo = "SELECAO_UNICA",
                label = "Estado Civil",
                opcoes = "Solteiro(a),Casado(a),Divorciado(a),Viúvo(a)",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 5,
                modeloId = 1,
                tipo = "TITULO",
                label = "QUEIXA PRINCIPAL",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 6,
                modeloId = 1,
                tipo = "TEXTO_LONGO",
                label = "Descreva sua queixa principal",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 7,
                modeloId = 1,
                tipo = "MULTIPLA_ESCOLHA",
                label = "Sintomas apresentados",
                opcoes = "Ansiedade,Depressão,Insônia,Irritabilidade,Problemas de concentração,Problemas de memória",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 8,
                modeloId = 1,
                tipo = "TITULO",
                label = "HISTÓRICO MÉDICO",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 9,
                modeloId = 1,
                tipo = "TEXTO_LONGO",
                label = "Doenças prévias e tratamentos",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 10,
                modeloId = 1,
                tipo = "MULTIPLA_ESCOLHA",
                label = "Medicamentos em uso",
                opcoes = "Antidepressivos,Ansiolíticos,Antipsicóticos,Hormônios,Outros",
                obrigatorio = false
            )
        )
    }
    
    /**
     * Cria campos de exemplo para o modelo "Anamnese Infantil"
     */
    fun createInfantilFields(): List<AnamneseCampo> {
        return listOf(
            AnamneseCampo(
                id = 11,
                modeloId = 2,
                tipo = "TITULO",
                label = "DADOS DA CRIANÇA",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 12,
                modeloId = 2,
                tipo = "TEXTO_CURTO",
                label = "Nome da Criança",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 13,
                modeloId = 2,
                tipo = "DATA",
                label = "Data de Nascimento",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 14,
                modeloId = 2,
                tipo = "TEXTO_CURTO",
                label = "Nome dos Pais/Responsáveis",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 15,
                modeloId = 2,
                tipo = "TITULO",
                label = "COMPORTAMENTO",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 16,
                modeloId = 2,
                tipo = "TEXTO_LONGO",
                label = "Descreva o comportamento da criança",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 17,
                modeloId = 2,
                tipo = "MULTIPLA_ESCOLHA",
                label = "Problemas identificados",
                opcoes = "Hiperatividade,Déficit de atenção,Agressividade,Timidez,Dificuldades escolares,Problemas de sono",
                obrigatorio = false
            )
        )
    }
    
    /**
     * Cria campos de exemplo para o modelo "Anamnese Casal"
     */
    fun createCasalFields(): List<AnamneseCampo> {
        return listOf(
            AnamneseCampo(
                id = 18,
                modeloId = 3,
                tipo = "TITULO",
                label = "DADOS DO CASAL",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 19,
                modeloId = 3,
                tipo = "TEXTO_CURTO",
                label = "Nome do Parceiro 1",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 20,
                modeloId = 3,
                tipo = "TEXTO_CURTO",
                label = "Nome do Parceiro 2",
                obrigatorio = true
            ),
            AnamneseCampo(
                id = 21,
                modeloId = 3,
                tipo = "DATA",
                label = "Data do Casamento/União",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 22,
                modeloId = 3,
                tipo = "TITULO",
                label = "RELACIONAMENTO",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 23,
                modeloId = 3,
                tipo = "TEXTO_LONGO",
                label = "Histórico do relacionamento",
                obrigatorio = false
            ),
            AnamneseCampo(
                id = 24,
                modeloId = 3,
                tipo = "MULTIPLA_ESCOLHA",
                label = "Principais problemas",
                opcoes = "Comunicação,Infidelidade,Problemas financeiros,Problemas com filhos,Problemas com família,Problemas sexuais",
                obrigatorio = false
            )
        )
    }
    
    /**
     * Retorna todos os campos de exemplo
     */
    fun getAllSampleFields(): List<AnamneseCampo> {
        return createAdultoFields() + createInfantilFields() + createCasalFields()
    }
} 



