# Diagrama Simplificado da Arquitetura - Psipro

## Visão geral do fluxo de telas

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  SplashActivity  │ ──► │   MainActivity   │ ──► │ DashboardActivity │
│   (LAUNCHER)     │     │  (Login Firebase)│     │  (host principal) │
└──────────────────┘     └──────────────────┘     └────────┬─────────┘
                                   │                        │
                                   │                        │ NavHostFragment
                                   ▼                        │ (nav_graph.xml)
                          ┌─────────────────┐               │
                          │CreateAccountAct. │               ▼
                          │PasswordRecovery  │     ┌─────────────────────────┐
                          └─────────────────┘     │      Fragmentos          │
                                                  │  Home | Agenda | Pacientes│
                                                  │  Financeiro | Notificações│
                                                  │  Autoavaliação | Config   │
                                                  │  Suporte | Aniversariantes│
                                                  └────────────┬─────────────┘
                                                               │
                                    ┌──────────────────────────┼──────────────────────────┐
                                    │                          │                          │
                                    ▼                          ▼                          ▼
                          ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
                          │Activities internas│      │FinanceiroDashboard│      │ PatientListAct.  │
                          │ AppointmentDetail│      │     Activity      │      │ DetalhePaciente  │
                          │ NovaSessaoAct.    │      │                   │      │ CadastroPaciente │
                          └──────────────────┘      └──────────────────┘      └──────────────────┘
```

---

## Diagrama de camadas (arquitetura)

```mermaid
flowchart TB
    subgraph UI["Camada de Apresentação"]
        direction TB
        A[Activities] --> B[Fragments]
        B --> C[Compose Screens]
        A --> C
        D[ViewModel]
        B --> D
        C --> D
    end

    subgraph DOMAIN["Camada de Domínio / Lógica"]
        E[Repositories]
        F[Services]
    end

    subgraph DATA["Camada de Dados"]
        G[Room AppDatabase]
        H[DAOs]
        G --> H
    end

    subgraph EXTERNAL["Serviços Externos"]
        I[Firebase Auth]
        J[Backend API / Sync]
        K[WorkManager]
    end

    D --> E
    E --> H
    E --> F
    F --> I
    F --> J
    J --> K
```

---

## Fluxo de navegação principal

```mermaid
flowchart LR
    Splash["SplashActivity\n(LAUNCHER)"]
    Main["MainActivity\n(Login)"]
    Dashboard["DashboardActivity\n(Drawer + Nav)"]
    
    Splash -->|"500ms delay"| Main
    Main -->|"Login OK"| Dashboard
    
    subgraph Dashboard_Fragments["Fragmentos no Drawer"]
        Home[Home]
        Agenda[Agenda]
        Pacientes[Pacientes]
        Financeiro[Financeiro]
        Notif[Notificações]
        Autoaval[Autoavaliação]
        Config[Configurações]
        Suporte[Suporte]
        Anivers[Aniversariantes]
    end

    Dashboard --> Home
    Dashboard --> Agenda
    Dashboard --> Pacientes
    Dashboard --> Financeiro
    Dashboard --> Notif
    Dashboard --> Autoaval
    Dashboard --> Config
    Dashboard --> Suporte
    Dashboard --> Anivers

    Home -->|Intent| AppointmentDetail
    Home -->|Intent| NovaSessao
    Home -->|Intent| FinanceiroDashboard
    Pacientes -->|Intent| DetalhePaciente
    Pacientes -->|Intent| CadastroPaciente
    Financeiro -->|Intent| FinanceiroDashboard
```

---

## Camadas e responsabilidades

| Camada | Componentes | Responsabilidade |
|--------|-------------|------------------|
| **App** | `App.kt` | Inicialização, Hilt, Firebase, tema |
| **UI - Activity** | Splash, Main, Dashboard, +30 Activities | Entry points, navegação, host de fragmentos |
| **UI - Fragment** | 9 fragmentos no `nav_graph` | Telas principais do drawer/menu |
| **UI - Compose** | Screens em `ui/screens/`, `ui/compose/` | Telas compostas por Jetpack Compose |
| **ViewModel** | `viewmodel/` e `ui/viewmodels/` | Estado, lógica de apresentação |
| **Repository** | `data/repository/`, `data/repositories/` | Acesso unificado a fontes de dados |
| **Data** | Room `AppDatabase`, DAOs, Entities | Persistência local SQLite |
| **Services** | `notification/`, `auth/`, `sync/`, `backup/` | Notificações, autenticação, sincronização |
| **DI** | Hilt (`di/`, `@HiltAndroidApp`) | Injeção de dependências |

---

## Diagrama simplificado de dependências

```mermaid
flowchart TB
    subgraph ANDROID
        UI[Activities / Fragments / Compose]
        VM[ViewModels]
    end

    subgraph HILT["Hilt DI"]
        APP[AppModule]
        DB[DatabaseModule]
        NOTIF[NotificationModule]
    end

    subgraph PERSISTENCE
        Room[(Room DB)]
        Prefs[SharedPreferences]
    end

    subgraph EXTERNAL
        Firebase[Firebase Auth]
        Backend[Backend API]
        WorkManager[WorkManager]
    end

    UI --> VM
    VM --> Repo[Repositories]
    Repo --> Room
    Repo --> Firebase
    Repo --> Backend
    VM --> Services[Services]
    Services --> WorkManager
    APP --> Repo
    DB --> Room
    NOTIF --> Services
```

---

## Resumo estrutural

- **Padrão:** MVVM com Repository
- **DI:** Hilt (Dagger)
- **Navegação:** Navigation Component + Intents
- **UI:** Mistura de Views (XML) e Jetpack Compose
- **Dados:** Room (local) + Firebase Auth + Backend sync via WorkManager
- **Notificações:** Vários serviços (Appointment, Agendamento, Cobrança, Financeiro, Autoavaliação)
