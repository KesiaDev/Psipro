# Voice AI — Revisão de Código v4.0.2
**Data:** 27/02/2026
**Revisor original:** Codecs
**Versão base da revisão:** v4.0.1
**Versão com as correções aplicadas:** v4.0.2
**Commit:** `99366aa` — branch `main`

---

## Contexto

Este documento descreve as 4 correções aplicadas com base na auditoria estática do Codecs sobre o código v4.0.1 do `whisper_transcriber.py`.

---

## Correções aplicadas

### Fix A — Timeout perdido ao salvar configurações
**Severidade:** Alta
**Arquivo:** `whisper_transcriber.py`
**Método:** `App._on_settings_saved()`

**Problema identificado:**
Quando o usuário salvava as configurações (ex: troca de API key), o client OpenAI era recriado **sem timeout**, revertendo para timeout infinito e anulando o timeout configurado na inicialização do app.

**Código antes (linha ~5589):**
```python
self.transcriber.client = OpenAI(api_key=self.config.api_key)
```

**Código depois:**
```python
self.transcriber.client = OpenAI(
    api_key=self.config.api_key,
    timeout=httpx.Timeout(30.0, connect=10.0),
)
```

---

### Fix B — `_transcribing_count` sem lock
**Severidade:** Média
**Arquivo:** `whisper_transcriber.py`
**Método:** `App._process_transcription()` e `App.__init__()`

**Problema identificado:**
Múltiplas threads daemon incrementam/decrementam `_transcribing_count` sem sincronização. A sequência read-modify-write (`+= 1`, `-= 1`) não é atômica entre GIL releases. Em Python 3.14 (versão usada no projeto), o GIL pode ser desabilitado — tornando a race condition um risco real.

**Código antes:**
```python
# __init__
self._transcribing_count = 0
self._lock = threading.Lock()

# _process_transcription (increment)
self._transcribing_count += 1

# _process_transcription (finally — decrement)
self._transcribing_count -= 1
self._transcribing = self._transcribing_count > 0
```

**Código depois:**
```python
# __init__ — lock dedicado adicionado
self._transcribing_count = 0
self._lock = threading.Lock()
self._transcribing_lock = threading.Lock()  # v4.0.2

# _process_transcription (increment)
with self._transcribing_lock:
    self._transcribing_count += 1

# _process_transcription (finally — decrement + update atômicos)
with self._transcribing_lock:
    self._transcribing_count -= 1
    self._transcribing = self._transcribing_count > 0
```

**Nota:** o update de `self._transcribing` foi movido para dentro do bloco `with` — agora é atômico em relação ao decrement.

---

### Fix C — `apply_text_style` criava client OpenAI a cada chamada
**Severidade:** Média
**Arquivo:** `whisper_transcriber.py`
**Função:** `apply_text_style()` (linha ~552) + chamadas em `App._process_transcription()` e `MainPanel._card_apply_style()`

**Problema identificado:**
A função standalone `apply_text_style` criava um novo `OpenAI(api_key=...)` a cada invocação — overhead de conexão desnecessário e sem timeout configurado.

**Código antes:**
```python
def apply_text_style(text, style, api_key, gpt_model="gpt-4.1-mini"):
    ...
    client = OpenAI(api_key=api_key)
```

**Código depois:**
```python
def apply_text_style(text, style, api_key, gpt_model="gpt-4.1-mini", client=None):
    ...
    if client is None:
        client = OpenAI(
            api_key=api_key,
            timeout=httpx.Timeout(30.0, connect=10.0),
        )
```

**Chamada em `_process_transcription` (App):** passa `client=self.transcriber.client`, reutilizando a conexão já inicializada com timeout.

```python
# antes
text = apply_text_style(text, self.config.text_style,
                        self.config.api_key, self.config.gpt_model)

# depois
text = apply_text_style(text, self.config.text_style,
                        self.config.api_key, self.config.gpt_model,
                        client=self.transcriber.client)
```

**Chamada em `_card_apply_style` (MainPanel):** sem acesso ao `transcriber`, usa o path de fallback — cria client com timeout ao invés de sem timeout como antes.

---

### Fix D — CLAUDE.md com nome de executável desatualizado
**Severidade:** Baixa
**Arquivo:** `CLAUDE.md`

**Problema identificado:**
O CLAUDE.md referenciava `VoiceAI_Setup.exe` como output do build, mas o executável gerado pelo `build.bat` se chama `Voice AI.exe`.

**Linhas corrigidas:**
```
# antes
3. O build gera apenas `dist\VoiceAI_Setup.exe`
4. Avisar o usuário que o `VoiceAI_Setup.exe` está pronto
7. Confirmar que `dist\VoiceAI_Setup.exe` foi gerado
8. Avisar: "VoiceAI_Setup.exe v{VERSION} pronto"

# depois
3. O build gera apenas `dist\Voice AI.exe`
4. Avisar o usuário que o `Voice AI.exe` está pronto
7. Confirmar que `dist\Voice AI.exe` foi gerado
8. Avisar: "Voice AI.exe v{VERSION} pronto"
```

---

## Achados descartados

Os itens abaixo foram levantados na auditoria, mas **não foram corrigidos** pelas razões indicadas:

| Achado | Motivo de descarte |
|--------|-------------------|
| ESC não cancela transcrição | **Refutado** — já há `catch` da exception `"cancelled"` em `_process_transcription` |
| Licença validada client-side | Risco de negócio, não bug de código — fora de escopo desta versão |
| `openai` sem upper bound no `requirements.txt` | Decisão arquitetural consciente; sem bug imediato |

---

## Arquivos modificados

| Arquivo | Tipo de mudança |
|---------|----------------|
| `whisper_transcriber.py` | Fix A, B, C + bump de versão para `4.0.2` |
| `installer.py` | Bump de versão para `4.0.2` |
| `CLAUDE.md` | Fix D + atualização de versão |
| `PRD.md` | Atualização de versão e registro das mudanças |

---

## Build

```
dist\Voice AI.exe   v4.0.2  ✅ gerado e validado
```
