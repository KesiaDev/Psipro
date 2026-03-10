# PRD — Claude Voice Agent
**Versao do documento:** 3.0 — Fevereiro 2026
**Produto pai:** VoiceAI v4.1.3 (github.com/ericluciano/voice-ai)
**Arquivo principal:** `voice_agent.py` (standalone, mesmo repositorio)
**Plataformas:** Windows 10/11 (Fase 1-3) · macOS 13+ (Fase 4)

---

## Visao do Produto

O **Claude Voice Agent** e um assistente de voz bidirecional que usa o Claude Code como backend.
O usuario fala, o sistema transcreve, envia ao Claude (que tem acesso a todos os MCPs configurados), e responde em voz natural.

**Diferencial:** Claude Code CLI como backend = acesso a Pipedrive, ClickUp, Outlook, n8n, ChatGuru, Zoom — tudo por voz.

---

## Estado Atual (v2 — Fev 2026)

### O que funciona hoje

| Feature | Status | Detalhes |
|---|---|---|
| STT (Whisper API) | Funcionando | Via OpenAI, PT-BR |
| LLM (Claude Code CLI) | Funcionando | --resume p/ sessao persistente, --append-system-prompt |
| TTS (Edge TTS) | Funcionando | Francisca, Antonio, Thalita, Macerio (PT-BR gratis) |
| UI (Tkinter) | Funcionando | Chat bubbles, status bar, audio level, settings completa |
| System prompt PT-BR | Funcionando | Ultra-detalhado, respostas naturais sem markdown/emoji |
| Barge-in | Funcionando | Interrompe TTS ao clicar MIC ou digitar |
| Hotkey global | Funcionando | Ctrl+Shift+Alt+F4 via RegisterHotKey |
| Sessao persistente | Funcionando | --resume com session_id |
| ESC cancelar | Funcionando | Cancela gravacao, processamento ou TTS |
| Seletor de microfone | Funcionando | Combobox na Settings |
| Config persistente | Funcionando | voice_agent_config.json |
| Modos de resposta | Funcionando | Texto / Audio / Ambos (toggle na UI) |
| Modo escuta continua | Implementado | VAD energy-based, toggle Manual/Continuo |
| Botao Play | Implementado | Replay de qualquer mensagem do agente |
| Info dialog | Implementado | Explicacoes detalhadas de cada feature |
| clean_for_tts | Robusto | Remove emojis, markdown, URLs, expande R$/% |

### O que falta

| Feature | Prioridade | Complexidade |
|---|---|---|
| Silero VAD (ML) ao inves de energy VAD | Media | Media |
| ElevenLabs ou OpenAI TTS (melhor qualidade) | Baixa | Baixa |
| Deepgram Nova-3 (STT real-time streaming) | Media | Media |
| Integracao no whisper_transcriber.py | Alta | Alta |
| Wake word ("Ei Claude") | Baixa | Media |
| App mobile via Claude Code Remote | Media | Baixa (ja existe) |

---

## Benchmark de APIs TTS (Text-to-Speech)

### Comparacao completa

| Provider | Preco por 1M chars | Vozes PT-BR | Latencia | Qualidade | Voice Cloning | Status no projeto |
|---|---|---|---|---|---|---|
| **Edge TTS** | GRATIS | Sim (4 vozes) | Baixa | 3.8-4.0 | Nao | **EM USO** |
| **ElevenLabs** | ~$180/1M chars | Sim (29 idiomas) | Baixa | 4.5+ | Sim | Candidato futuro |
| **OpenAI gpt-4o-mini-tts** | $12/1M tokens | Sim (13 vozes) | Baixa | 4.0+ | Nao | Candidato futuro |
| **Google Cloud TTS** | $16/1M chars | Sim | Baixa | 4.0 | Nao | Alternativa |
| **Amazon Polly** | $16/1M chars (Neural) | Sim (Camila, Ricardo) | Baixa | 3.8-4.0 | Nao | Alternativa |
| **Coqui XTTS-v2** | GRATIS (local) | Sim | ~200ms | 3.5-4.0 | Sim (10s) | Empresa fechou Dez 2025 |

### Recomendacao

- **MVP (atual):** Edge TTS — gratis, boa qualidade, funciona perfeitamente
- **Producao:** OpenAI gpt-4o-mini-tts — melhor custo-beneficio com emocao e tom ajustaveis
- **Premium:** ElevenLabs — melhor qualidade geral, mas mais caro
- **PT-BR especifico:** Amazon Polly Camila — elogiada por naturalidade em BR

---

## Benchmark de APIs STT (Speech-to-Text)

| Provider | Preco/min | PT-BR Real-Time | Latencia | WER | Local | Status |
|---|---|---|---|---|---|---|
| **OpenAI Whisper API** | $0.006/min | Nao (batch) | 1-3s | Baseline | Nao | **EM USO** |
| **OpenAI gpt-4o-mini-transcribe** | $0.003/min | Nao (batch) | ~1s | Melhor que Whisper | Nao | Candidato |
| **Deepgram Nova-3** | $0.0092/min | Sim (streaming) | <300ms | -54% WER | Nao | Candidato (melhor real-time) |
| **faster-whisper local** | GRATIS | Sim (com eng.) | ~1s GPU | Igual Whisper | Sim | Instalado, nao integrado |
| **AssemblyAI** | $0.0045/min | Sim (streaming) | Baixa | N/A | Nao | Alternativa |

### Recomendacao

- **MVP (atual):** OpenAI Whisper API — simples, funciona
- **Real-time streaming:** Deepgram Nova-3 — <300ms, PT-BR, code-switching
- **Local/gratis:** faster-whisper com large-v3-turbo — requer GPU 6GB

---

## Benchmark de VAD (Voice Activity Detection)

| Engine | Tipo | TPR@5%FPR | Velocidade | Licenca | Status |
|---|---|---|---|---|---|
| **Energy-based** | Sinal | ~70% | Muito rapido | N/A | **EM USO** |
| **WebRTC VAD** | GMM | 50% | Muito rapido | Open source | Obsoleto |
| **Silero VAD** | Deep Learning | 87.7% | 0.004 RTF | MIT | Codigo pronto (agent_vad.py) |
| **Cobra VAD** | Deep Learning | 98.9% | 0.0005 RTF | Comercial | Candidato producao |

### Recomendacao

- **MVP (atual):** Energy-based — simples, funciona para modo continuo
- **Proximo passo:** Silero VAD — gratis, 87.7% accuracy, ja tem wrapper
- **Producao:** Cobra VAD (Picovoice) — 98.9% accuracy

---

## Claude Code Remote

### Como funciona

O Claude Code (v2.1.52+) tem **Remote Control** nativo:

1. Na maquina local: `claude remote-control` ou `claude rc`
2. Aparece QR code
3. Escaneia com celular (app Claude iOS/Android) ou abre URL no browser
4. Controla a sessao remota de qualquer lugar
5. Todo codigo fica na maquina local, so HTTPS outbound

### Requisitos
- Plano Max do Claude
- Claude Code v2.1.52+
- Autenticado via `/login`

### Implicacao pro Voice Agent
O usuario pode iniciar o Voice Agent no PC e controlar remotamente do celular.
Mas o Voice Agent tambem pode ter sua propria interface web no futuro.

---

## Arquitetura Atual

```
voice_agent.py (standalone ~1200 linhas)
    |
    |-- VoiceAgentApp (Tkinter UI)
    |   |-- Chat bubbles com Play button
    |   |-- Mode toggles (Manual/Continuo, Texto/Audio/Ambos)
    |   |-- Settings completa
    |   |-- Info dialog
    |   |-- Status bar com audio level
    |
    |-- ClaudeSession
    |   |-- subprocess.run(claude.cmd -p --output-format json)
    |   |-- --resume para sessao persistente
    |   |-- --append-system-prompt para PT-BR natural
    |
    |-- STT: OpenAI Whisper API
    |-- TTS: Microsoft Edge TTS (edge-tts + av decoder)
    |-- VAD: SimpleVAD (energy-based)
    |-- GlobalHotkey: RegisterHotKey (Windows)
```

### Fluxo

```
[Microfone] --frame--> [VAD] --speech_end--> [Whisper API] --texto-->
[Claude CLI] --resposta--> [clean_for_tts] --texto limpo-->
[Edge TTS] --MP3--> [av decode] --PCM--> [sounddevice play]
```

### Dependencias

| Pacote | Funcao |
|---|---|
| edge-tts | TTS Microsoft (gratis) |
| av (PyAV) | Decoder MP3→PCM |
| openai | Whisper API |
| sounddevice | Captura e reproducao de audio |
| numpy | Arrays de audio |

---

## Arquivos do Projeto

| Arquivo | Status | Descricao |
|---|---|---|
| `voice_agent.py` | **Ativo** | App standalone principal |
| `voice_agent_config.json` | Runtime | Config persistente |
| `agent_config.py` | Legado | Constantes (pode ser removido) |
| `agent_core.py` | Legado | Loop VAD→STT→LLM→TTS (nao usado) |
| `agent_stt.py` | Legado | faster-whisper wrapper (nao usado) |
| `agent_tts.py` | Legado | Edge TTS wrapper (nao usado, inline no voice_agent) |
| `agent_vad.py` | Referencia | Silero VAD wrapper (futuro) |
| `agent_window.py` | Legado | Tkinter UI (nao usado, inline no voice_agent) |

---

## Roadmap

### Fase 1 — MVP Standalone (COMPLETA)

- [x] STT via OpenAI Whisper API
- [x] LLM via Claude Code CLI
- [x] TTS via Edge TTS
- [x] UI Tkinter com chat bubbles
- [x] Barge-in, ESC, hotkey global
- [x] Config persistente, seletor de mic
- [x] System prompt PT-BR ultra-detalhado
- [x] Modos de resposta (texto/audio/ambos)
- [x] Modo escuta continua (VAD)
- [x] Botao Play em mensagens
- [x] Info dialog

### Fase 2 — Qualidade e Polimento

- [ ] Migrar VAD para Silero (ML, melhor accuracy)
- [ ] Testar OpenAI gpt-4o-mini-tts como alternativa
- [ ] Testar Deepgram Nova-3 para STT real-time
- [ ] Echo cancellation para barge-in durante TTS
- [ ] Streaming TTS (sentence splitter → audio chunk a chunk)
- [ ] Multiplas vozes com preview na Settings
- [ ] Wake word ("Ei Claude")

### Fase 3 — Integracao com VoiceAI

- [ ] Integrar no whisper_transcriber.py (4 pontos de contato)
- [ ] Item "Modo Agente" no menu da bandeja
- [ ] Aba "Agente" nas Settings do app principal
- [ ] Build unificado (Voice AI.exe)

### Fase 4 — Cloud e Mobile

- [ ] Claude Code Remote para controle do celular
- [ ] Interface web simples
- [ ] App mobile (iOS/Android)

### Fase 5 — Features Avancadas

- [ ] Memoria persistente entre sessoes
- [ ] Briefing matinal autonomo
- [ ] Modo reuniao real-time
- [ ] Resumo pos-reuniao com atualizacao CRM
- [ ] Voice cloning (ElevenLabs)

---

## Custo Estimado por Minuto de Conversa

| Componente | Atual | Futuro (otimizado) |
|---|---|---|
| STT | $0.006/min (Whisper) | $0.003/min (gpt-4o-mini-transcribe) |
| TTS | $0 (Edge TTS) | $0.015/min (OpenAI TTS) ou $0 |
| LLM | ~$0.01-0.05/min (Claude) | ~$0.01/min (Haiku) |
| **Total** | **~$0.02-0.06/min** | **~$0.015-0.025/min** |

---

## Segmentos-Alvo

| Segmento | Caso de uso principal | ROI |
|---|---|---|
| Vendedores / SDRs | Atualizar CRM por voz apos cada call | 3h+/semana |
| Gestores e executivos | Briefing matinal + capture pos-reuniao | 2h+/semana |
| Trabalhadores remotos | Orquestrar ferramentas por voz | 1-2h/semana |
| Profissionais em mobilidade | CRM ao dirigir | Seguranca |
| Usuarios com limitacao motora | Operacao completa por voz | Inclusao |

---

*Versao 3.0 — Fevereiro 2026 — Expert Integrado*
*Baseado em: pesquisa de mercado de APIs de voz, benchmarks TTS/STT/VAD, Claude Code Remote, analise de features de concorrentes (ChatGPT Voice, Gemini Live, Siri, Alexa)*
