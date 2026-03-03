# Relatório de Arquitetura — Projeto PsiPro

**Data:** 2025  
**Módulo analisado:** `app` (Android)  
**Versão do app:** 1.1 (versionCode 2)

---

## 1. Estrutura de Pastas (Árvore do Projeto)

```
Psipro/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/psipro/app/
│       │   ├── adapters/              # RecyclerView Adapters (XML)
│       │   ├── adapter/               # Alternativa (inconsistência)
│       │   ├── Anotacao.kt            # Modelo em raiz
│       │   ├── auth/                  # Autenticação
│       │   │   ├── AuthManager.kt
│       │   │   └── PasswordRecoveryService.kt
│       │   ├── backup/
│       │   │   └── BackupService.kt
│       │   ├── cache/                 # Cache (banco separado)
│       │   │   ├── CacheDatabase.kt
│       │   │   ├── CacheDaos.kt
│       │   │   ├── CacheEntities.kt
│       │   │   └── DateConverter.kt
│       │   ├── config/
│       │   │   └── AppConfig.kt
│       │   ├── data/                  # Camada de Dados
│       │   │   ├── dao/               # 26 DAOs Room
│       │   │   ├── entities/          # 28 entidades
│       │   │   ├── repository/        # Repositories (20)
│       │   │   ├── repositories/      # Alternativa (inconsistência)
│       │   │   ├── converters/        # Type converters
│       │   │   ├── models/
│       │   │   ├── local/
│       │   │   ├── service/
│       │   │   └── AppDatabase.kt
│       │   ├── di/                    # Hilt
│       │   │   ├── AppModule.kt
│       │   │   ├── DatabaseModule.kt
│       │   │   ├── CacheModule.kt
│       │   │   └── NotificationModule.kt
│       │   ├── notification/
│       │   │   ├── di/
│       │   │   ├── *Receiver.kt
│       │   │   ├── *Service.kt
│       │   │   └── LembreteCobrancaWorker.kt
│       │   ├── security/
│       │   │   ├── EncryptionManager.kt
│       │   │   └── SessionManager.kt
│       │   ├── sync/                  # Sincronização backend
│       │   │   ├── di/
│       │   │   ├── api/
│       │   │   ├── work/
│       │   │   └── BackendAuthManager.kt
│       │   ├── ui/                    # Camada de Apresentação
│       │   │   ├── compose/           # Componentes Compose
│       │   │   ├── fragments/         # 10 Fragments
│       │   │   ├── screens/           # 30+ Compose Screens
│       │   │   │   └── home/
│       │   │   ├── schedule/
│       │   │   ├── adapters/
│       │   │   ├── viewmodels/        # 25+ ViewModels
│       │   │   └── *Activity.kt
│       │   ├── utils/                 # Utilitários
│       │   ├── viewmodel/             # ViewModels legados (inconsistência)
│       │   └── App.kt
│       └── res/
│           ├── anim/
│           ├── color/, color-night/
│           ├── drawable/
│           ├── font/
│           ├── layout/, layout-night/
│           ├── menu/
│           ├── mipmap-*/
│           ├── navigation/
│           ├── raw/
│           ├── values/, values-night/
│           └── xml/
├── build.gradle
├── gradle.properties
├── gradle/wrapper/
│   ├── gradle-wrapper.properties
│   └── gradle-wrapper.jar
└── settings.gradle
```

---

## 2. Arquitetura Utilizada

### Padrão principal: **MVVM (Model-View-ViewModel)**

| Aspecto | Implementação |
|---------|---------------|
| **View** | Activities, Fragments, Compose Screens |
| **ViewModel** | `ViewModel` / `AndroidViewModel` com Hilt |
| **Model** | Room Entities + Repositories |
| **Binding** | LiveData, StateFlow, `hiltViewModel()` |

### Características

- ViewModels expõem dados via LiveData/StateFlow
- Repositories abstraem acesso a dados
- Hilt para injeção de dependências
- Coroutines para fluxos assíncronos

### O que não existe

- Camada de **Domain** (Use Cases)
- Padrão **Clean Architecture**
- Padrão **MVI** (Model-View-Intent)

---

## 3. Camadas Existentes

### 3.1 Camada de Dados (`data/`)

| Componente | Localização | Quantidade |
|------------|-------------|------------|
| **Entities** | `data/entities/` | 28 |
| **DAOs** | `data/dao/` | 26 |
| **Repositories** | `data/repository/` + `data/repositories/` | 22 |
| **Converters** | `data/converters/` | 9 |
| **Database** | `AppDatabase.kt` | 1 |

### 3.2 Camada de Apresentação (`ui/`)

| Componente | Localização | Quantidade |
|------------|-------------|------------|
| **Activities** | Raiz + `ui/` | ~34 |
| **Fragments** | `ui/fragments/` | 10 |
| **Compose Screens** | `ui/screens/` | 30+ |
| **ViewModels** | `ui/viewmodels/` + `viewmodel/` | ~30 |

### 3.3 Camada de Domain

Não há camada de domínio. Regras de negócio estão em Repositories e ViewModels.

---

## 4. ViewModels

### 4.1 Em `ui/viewmodels/` (25)

| ViewModel | Responsabilidade |
|-----------|------------------|
| `PatientViewModel` | Pacientes |
| `AppointmentViewModel` | Agendamentos |
| `AnamneseViewModel` | Formulários de anamnese |
| `AnotacaoSessaoViewModel` | Anotações de sessão |
| `CobrancaSessaoViewModel` | Cobrança de sessões |
| `CobrancaAgendamentoViewModel` | Cobrança de agendamentos |
| `FinanceiroUnificadoViewModel` | Unificação financeira |
| `DocumentoViewModel` | Documentos |
| `ArquivoViewModel` | Arquivos |
| `AutoavaliacaoViewModel` | Autoavaliação |
| `HistoricoMedicoViewModel` | Histórico médico |
| `HistoricoFamiliarViewModel` | Histórico familiar |
| `VidaEmocionalViewModel` | Vida emocional |
| `ObservacoesClinicasViewModel` | Observações clínicas |
| `PatientNoteViewModel` | Notas de paciente |
| `PatientMessageViewModel` | Mensagens |
| `NotificationViewModel` | Notificações |
| `TipoSessaoViewModel` | Tipos de sessão |
| `PacienteSessoesViewModel` | Sessões do paciente |
| `AudioTranscriptionViewModel` | Transcrição de áudio |
| `HomeViewModel` | Tela inicial |

### 4.2 Em `viewmodel/` (8)

| ViewModel | Responsabilidade |
|-----------|------------------|
| `BaseViewModel` | Base com tratamento de erros |
| `AuthViewModel` | Autenticação |
| `PatientViewModel` | ⚠️ **Duplicado** |
| `AppointmentViewModel` | ⚠️ **Duplicado** |
| `ProntuarioViewModel` | Prontuário |
| `PatientNoteViewModel` | ⚠️ **Duplicado** |
| `DadosPessoaisViewModel` | Dados pessoais |
| `BackendLoginViewModel` | Login backend |

### 4.3 Em `ui/schedule/`

| ViewModel | Responsabilidade |
|-----------|------------------|
| `ScheduleViewModel` | Agenda |

### Duplicações

- `PatientViewModel` em 2 pacotes  
- `AppointmentViewModel` em 2 pacotes  
- `PatientNoteViewModel` em 2 pacotes  

---

## 5. Repositories

### Em `data/repository/` (20)

| Repository | Entidade/Área |
|------------|----------------|
| `PatientRepository` | Patient |
| `UserRepository` | User |
| `AppointmentRepository` | Appointment |
| `PatientReportRepository` | PatientReport |
| `FinancialRecordRepository` | FinancialRecord |
| `ProntuarioRepository` | Prontuario |
| `AuditLogRepository` | AuditLog |
| `HistoricoMedicoRepository` | HistoricoMedico |
| `HistoricoFamiliarRepository` | HistoricoFamiliar |
| `VidaEmocionalRepository` | VidaEmocional |
| `ObservacoesClinicasRepository` | ObservacoesClinicas |
| `AnotacaoSessaoRepository` | AnotacaoSessao |
| `CobrancaSessaoRepository` | CobrancaSessao |
| `CobrancaAgendamentoRepository` | CobrancaAgendamento |
| `AutoavaliacaoRepository` | Autoavaliacao |
| `DocumentoRepository` | Documento |
| `ArquivoRepository` | Arquivo |
| `NotificationRepository` | Notification |
| `PatientNoteRepository` | PatientNote |
| `PatientMessageRepository` | PatientMessage |

### Em `data/repositories/` (2)

- `PatientMessageRepository`
- `PatientNoteRepository` (⚠️ duplicado)

---

## 6. Use Cases

**Não há Use Cases.** Regras de negócio estão em Repositories e ViewModels.

Exemplos que poderiam virar Use Cases:

- `PatientRepository.canAddMorePatients()` — limite de pacientes  
- `PatientRepository.isNearLimit()` — aviso de limite  

---

## 7. Dependências Principais

### Build

| Dependência | Versão |
|-------------|--------|
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 26 |
| Kotlin | 1.9.22 |

### Core

| Dependência | Versão |
|-------------|--------|
| androidx.core:core-ktx | 1.12.0 |
| androidx.appcompat | 1.6.1 |
| androidx.constraintlayout | 2.1.4 |
| com.google.android.material | 1.11.0 |

### Arquitetura

| Dependência | Versão |
|-------------|--------|
| lifecycle-runtime-ktx | 2.6.2 |
| lifecycle-livedata-ktx | 2.6.2 |
| lifecycle-viewmodel-ktx | 2.6.2 |
| lifecycle-viewmodel-compose | 2.6.1 |

### Injeção de Dependências

| Dependência | Versão |
|-------------|--------|
| dagger:hilt-android | 2.48 |
| hilt-navigation-compose | 1.0.0 |
| hilt-work | 1.2.0 |

### Banco de Dados

| Dependência | Versão |
|-------------|--------|
| androidx.room | 2.6.1 |
| android-database-sqlcipher | 4.5.4 |
| androidx.sqlite:sqlite-ktx | 2.4.0 |

### Rede

| Dependência | Versão |
|-------------|--------|
| retrofit2:retrofit | 2.9.0 |
| retrofit2:converter-gson | 2.9.0 |
| okhttp3:logging-interceptor | 4.12.0 |

### UI

| Dependência | Versão |
|-------------|--------|
| Compose BOM (ui, material3) | 1.5.10 |
| activity-compose | 1.7.2 |
| navigation-fragment-ktx | 2.7.7 |
| Glide | 4.16.0 |
| Coil (Compose) | 2.5.0 |

### Firebase

| Dependência |
|-------------|
| firebase-auth-ktx |
| firebase-firestore-ktx |
| firebase-storage-ktx |
| firebase-messaging-ktx |

### Outras

| Dependência | Uso |
|-------------|-----|
| kotlinx-coroutines-android | 1.7.3 |
| work-runtime-ktx | 2.9.0 |
| play-services-auth | 20.7.0 |
| security-crypto | 1.1.0-alpha06 |
| biometric | 1.2.0-alpha05 |
| vosk-android | 0.3.38 |

---

## 8. Problemas Estruturais

### Críticos

| # | Problema | Impacto | Sugestão |
|---|----------|---------|----------|
| 1 | **ViewModels duplicados** (Patient, Appointment, PatientNote em 2 pacotes) | Risco de conflito, difícil manutenção | Unificar em `ui/viewmodels/` e remover duplicados |
| 2 | **Repositories duplicados** (repository vs repositories) | Estrutura confusa | Centralizar em `data/repository/` |
| 3 | **Ausência de Domain** | Regras de negócio espalhadas | Criar `domain/usecases/` e extrair lógica de negócio |
| 4 | **Regras em Repositories** (ex.: `canAddMorePatients`, `isNearLimit`) | Repositories com responsabilidade extra | Mover para Use Cases ou Services |
| 5 | **Activities na raiz e em `ui/`** | Organização inconsistente | Agrupar em `ui/activities/` ou similar |

### Moderados

| # | Problema | Impacto |
|---|----------|---------|
| 6 | **XML + Compose** (100+ layouts XML e 30+ telas Compose) | Duas stacks de UI |
| 7 | **ViewModels grandes** | Lógica complexa e difícil de testar |
| 8 | **Sincronização misturada na camada de dados** | Menor clareza de responsabilidades |

### Menores

| # | Problema | Impacto |
|---|----------|---------|
| 9 | Nomenclatura mista (PT/EN) | Menor consistência |
| 10 | Dois pacotes de adapters (`adapter/` e `adapters/`) | Pequena inconsistência estrutural |
| 11 | Cache separado (`CacheDatabase`) | Propósito pouco explícito frente ao banco principal |

---

## 9. Resumo Estatístico

| Métrica | Valor |
|---------|-------|
| Arquivos Kotlin | ~276 |
| Activities | 34 |
| Fragments | 10 |
| Compose Screens | 30+ |
| ViewModels | ~30 |
| Repositories | 22 |
| Entities | 28 |
| DAOs | 26 |
| Use Cases | 0 |

---

## 10. Recomendações Gerais

1. Unificar ViewModels em `ui/viewmodels/` e eliminar duplicados.  
2. Concentrar Repositories em `data/repository/`.  
3. Criar camada de Domain com Use Cases e extrair regras de negócio.  
4. Mover Activities para `ui/activities/` (ou equivalente) para padronizar.  
5. Definir padrão de idioma (PT ou EN) e aplicar em nomes de classes/pacotes.  
6. Avaliar migração gradual de XML para Compose ou delimitar onde cada um é usado.  

---

*Relatório gerado com base em análise do código-fonte do projeto PsiPro.*
