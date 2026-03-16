# PsiPro: O que funciona localmente vs o que precisa de API

## Resumo rápido

| Funcionalidade | Local | API |
|----------------|-------|-----|
| CRUD pacientes, agendamentos, sessões, pagamentos, documentos | ✅ Room | Sync opcional |
| Login/registro | ❌ (fallback admin@teste.com) | ✅ |
| Transcrição de áudio | ✅ Vosk | ✅ Whisper (QuickSession) |
| Insights de sessão (IA) | ❌ | ✅ OpenAI via backend |
| Comandos de voz | ⚠️ SpeechRecognizer (usa Google Cloud) | - |
| Text-to-Speech | ✅ | - |
| Gravação de áudio | ✅ | - |
| Firestore (transcrições) | ❌ | ✅ |

---

## Detalhamento

### 1. Transcrição de áudio

Existem **dois fluxos** diferentes no app:

| Tela/ViewModel | Método | Local ou API |
|----------------|--------|--------------|
| **AudioTranscriptionScreen** | `AudioTranscriptionViewModel.transcreverAudioVosk` | **Local (Vosk)** |
| **QuickSession** (sessão rápida com gravação) | `QuickSessionViewModel` → `backendApi.transcribe` | **API (Whisper)** |

- **Vosk**: modelo `model-pt` em `assets/`, descompactado em runtime. Funciona offline.
- **Whisper (API)**: `POST /voice/transcribe` no backend. Requer internet e `OPENAI_API_KEY` no servidor.

**Recomendação**: Para fallback local na QuickSession seria necessário converter o áudio m4a (VoiceRecorder) para WAV 16kHz antes de passar ao Vosk. Enquanto isso não for implementado, use a **AudioTranscriptionScreen** para transcrição offline.

---

### 2. Insights de sessão (IA)

- Endpoint: `POST /sessions/voice-note`
- Usa OpenAI para gerar resumo, temas e emoções.
- **Não há alternativa local** – depende do backend.

---

### 3. Comandos de voz (VoiceCommandManager)

- Usa `SpeechRecognizer` do Android.
- Na prática, envia áudio para o **Google Cloud** – requer internet.
- Para reconhecimento 100% offline, seria necessário Vosk ou similar.

---

### 4. Firestore

- `AudioTranscriptionViewModel` salva transcrições em `transcricoes`.
- Requer conexão com Firebase.

---

### 5. Sync (pacientes, agendamentos, sessões, pagamentos, documentos)

- Tudo funciona localmente no Room.
- Sync com backend é **opcional** – melhora quando há internet e usuário logado.

---

## Ambiente local para testes

Para testar com backend local (emulador):

```gradle
// app/build.gradle - debug
buildConfigField "String", "PSIPRO_API_BASE_URL", "\"http://10.0.2.2:3001/api\""
```

E em `AndroidManifest.xml` (debug):

```xml
<application android:usesCleartextTraffic="true" ...>
```

---

## Conclusão

- **Transcrição**: Pode ser 100% local com Vosk na tela de transcrição de áudio. Na sessão rápida, depende da API (Whisper); fallback local exigiria conversão m4a→WAV.
- **Resto do CRUD**: Totalmente local (Room).
- **Login, sync, insights, Firestore**: Dependem de API/Firebase.
