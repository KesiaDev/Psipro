# CLAUDE.md — Voice AI

## Sobre o Projeto

Voice AI é um transcritor de áudio global para Windows (Python + Tkinter + pystray).
Arquivo principal único: `whisper_transcriber.py` (~4100 linhas).
Versão atual: **v4.1.1** — Sons de gravação distintos (chime), clique na StatusBar copia texto.

## Regras do Projeto

### Autonomia total
- O usuário autorizou operação autônoma COMPLETA: implementar, commitar, pushar, buildar e executar qualquer comando sem pedir permissão.
- Frase do usuário: "não precisa mais me pedir permissão vai por conta própria"
- Frase do usuário (2026-02-26): "pode começar agora a fazer uma atrás da outra... eu queria que você não me pedisse nenhuma permissão a partir de agora"
- **NUNCA usar ExitPlanMode, AskUserQuestion ou qualquer outra interrupção para pedir aprovação de ação.**
- Executar diretamente: git add, git commit, git push, build.bat, python, cmd — sem confirmação.

### Build obrigatório após cada versão
Toda vez que o código for alterado e commitado (nova versão, bugfix, qualquer mudança funcional):
1. Rodar `build.bat` automaticamente após o commit+push
2. Comando: `cmd.exe //c ".\\build.bat"` (timeout 300s)
3. O build gera apenas `dist\Voice AI.exe` (deleta o VoiceAI.exe avulso)
4. Avisar o usuário que o `Voice AI.exe` está pronto para instalar
5. **NUNCA** entregar uma versão sem o build do executável

### Fluxo padrão de entrega
1. Implementar mudanças no `whisper_transcriber.py`
2. Atualizar `VERSION` no código
3. Atualizar `PRD.md` com as features entregues (marcar como ✅, atualizar tabela de estado)
4. Verificar sintaxe: `python -c "import py_compile; py_compile.compile('whisper_transcriber.py', doraise=True)"`
5. `git add whisper_transcriber.py PRD.md` + `git commit` + `git push`
6. Rodar build: `cmd.exe //c ".\\build.bat"`
7. Confirmar que `dist\Voice AI.exe` foi gerado
8. Avisar: "Voice AI.exe v{VERSION} pronto"

### Estrutura do projeto
- `whisper_transcriber.py` — app principal (arquivo único, ~4100 linhas)
- `installer.py` — instalador GUI (busca VoiceAI.exe em sys._MEIPASS, registra no Windows via winreg, cria atalho Menu Iniciar)
- `build.bat` — compila app + instalador, deleta exe avulso
- `VoiceAI.spec` — spec do PyInstaller para o app
- `VoiceAI_Setup.spec` — spec do PyInstaller para o instalador (embute VoiceAI.exe como datas)
- `PRD.md` — Product Requirements Document (manter atualizado a cada versão)
- `assets/icon.ico` — ícone do app
- `config.json` — configurações do usuário (gerado em runtime)

### Convenções
- Versão semântica: vX.Y.Z
- Commit messages em português, começando com versão quando for release
- Idioma do código: nomes de variáveis/funções em inglês, strings de UI em português
- Co-Author obrigatório nos commits: `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>`

## Arquitetura do Código (whisper_transcriber.py)

### Classes principais (em ordem no arquivo)
1. **Constantes** (linhas 60-145): VK_MAP, MOD_MAP, VERSION, RATE, CHANNELS
2. **RECORDING_MODE_OPTIONS** + **THEMES** + **get_theme()** (após VERSION): toggle/ptt, sistema de temas, helpers de cor
3. **Funções utilitárias**: check_for_update(), apply_inline_commands(), apply_text_style(), hex_to_rgb(), interpolate_color()
4. **KBDLLHOOKSTRUCT / HOOKPROC**: ctypes para LL keyboard hook
5. **SingleInstance**: mutex para instância única
6. **ConfigManager**: config.json com DEFAULTS, load/save, properties
   - Campos: api_key, hotkey, language, model, auto_paste, auto_start, notification, notification_sound, inline_commands, max_recording_secs, mic_device, text_style, text_style_auto, gpt_model, recording_mode, edit_before_paste, theme
7. **SettingsDialog**: janela de configuração Tkinter (480x950) — inclui seletor de tema
8. **AudioRecorder**: gravação via sounddevice, aceita `device` param
9. **Transcriber**: chamada OpenAI Whisper API com retry
10. **TextPaster**: stealth_paste via SendInput + clipboard
11. **StatusBar**: Canvas-based (360x44), gradiente PIL, mic icon animado, dots transcribing, state machine (idle/recording/transcribing/result)
    - `_draw_gradient_bg()`: renderiza fundo com PIL Image
    - `_draw_mic_icon()`: desenha mic + pulse arcs no Canvas
    - `show_transcribing()`: ativa dots animation
    - `apply_theme()`: reaplica tema ao vivo
12. **MainPanel**: painel de histórico + settings inline (650x500)
    - Properties dinâmicas para cores (BG_DARK, BG_CARD, etc. → get_theme())
    - Avatar "VA" no header (Canvas oval + text)
    - Cards expandíveis (120 chars default, click para expandir)
    - Fade-in staggered na abertura
    - Contagem de palavras no timestamp
13. **TranscriptionHistory**: persistência em history.json
14. **UsageTracker**: analytics JSONL
15. **UsageReportWindow**: janela de relatório de uso (cores via get_theme())
16. **TrayIcon**: pystray com menu, toast, ícone dinâmico
17. **HotkeyManager**: RegisterHotKey (toggle) + WH_KEYBOARD_LL (PTT)
    - `_register_hotkey_thread()` para modo toggle
    - `_ptt_hook_thread()` para modo PTT (key-down=start, key-up=stop)
    - `change(hotkey_str, new_mode)` para trocar ao vivo
18. **EditPopup**: popup para revisar texto antes de colar (cores via properties dinâmicas)
19. **App**: orquestrador principal
    - `_poll_queue()`: processa toggle, ptt_start, ptt_stop, cancel
    - `_start_recording()`: inicia gravação + UI loop + ESC monitor
    - `_on_settings_saved()`: propaga tema ao vivo via `bar.apply_theme()`
    - `_ptt_start()` / `_ptt_stop()`: controle PTT
    - `_stop_and_transcribe()`: para, transcreve, aplica inline cmds, aplica estilo GPT, edit popup ou paste direto
20. **_run_uninstall()**: desinstalação completa (registry, atalhos, auto-deleção)
21. **main()**: entry point — trata `--uninstall` antes de qualquer inicialização

### Config fields atuais (v3.1.0+)
```python
DEFAULTS = {
    "api_key": "",
    "hotkey": "Ctrl+Shift+Alt+F3",
    "language": "pt",
    "model": "whisper-1",
    "auto_paste": True,
    "auto_start": True,
    "notification": True,
    "notification_sound": True,
    "inline_commands": True,
    "max_recording_secs": 300,   # v3.1.0: 120 → 300 (5 min)
    "mic_device": None,
    "text_style": "none",
    "text_style_auto": False,
    "gpt_model": "gpt-4.1-mini",
    "recording_mode": "toggle",
    "edit_before_paste": False,
    "theme": "dark",
    "silent_until": 0.0,          # v3.1.0: timestamp até quando notificações ficam silenciadas
}
```

## Sistema de Temas (v3.0.0+)

- `THEMES` dict no topo do arquivo com paletas `"dark"` e `"light"`
- `get_theme(name=None)` retorna o dict de cores do tema ativo
- `_current_theme` variável global, atualizada via ConfigManager.load() e _on_settings_saved()
- Helpers: `hex_to_rgb()`, `rgb_to_hex()`, `interpolate_color()`
- `THEME_OPTIONS` lista de opções para combobox
- Todas as classes usam `get_theme()` via properties dinâmicas (MainPanel, EditPopup, UsageReportWindow)
- StatusBar: Canvas-based com gradiente PIL, mic icon animado, dots de transcribing

## Instalação e Registro no Windows (v3.0.1+)

### installer.py
- `VERSION` constante no topo (manter em sincronia com whisper_transcriber.py)
- `register_app_in_windows()`: escreve em `HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\VoiceAI`
  - DisplayName, DisplayVersion, Publisher, InstallLocation, UninstallString, DisplayIcon, EstimatedSize
- Cria atalho no **Menu Iniciar** (`%APPDATA%\...\Start Menu\Programs\Voice AI.lnk`)
- App aparece em **Configurações > Apps > Aplicativos instalados**

### whisper_transcriber.py — Desinstalação
- `_run_uninstall(quiet=False)`: chamada via `VoiceAI.exe --uninstall`
- Remove: registry key, atalhos (Desktop, Menu Iniciar, Startup), exe
- Preserva: `history.json`, `usage_log.jsonl`, `config.json`
- Auto-deleção via `cmd /c ping -n 3 ... & del ...` (padrão Windows)
- `--quiet` pula diálogos de confirmação

## Novidades v3.1.0 (UX Polish)

- **StatusBar durante gravação:** apenas waveform + timer — sem mic pulsante distrativo
- **Notificação clicável:** `NIN_BALLOONUSERCLICK` interceptado via WndProc subclassing (ctypes). Clique copia texto ao clipboard + abre MainPanel. `_on_balloon_click()` no App.
- **Dia silencioso:** `ConfigManager.silent_until` (float timestamp). `is_silenced()` / `silence_for_day()` / `unsilence()`. Item dinâmico no menu da bandeja (🔕/🔔). `_notify_transcription()` respeita `is_silenced()`.
- **Sons sutis:** `_make_beep_wav(freq, duration_ms)` gera WAV sine wave em memória (stdlib: wave/io/array/math). `App._snd_start` (880Hz 70ms) + `_snd_stop` (660Hz 90ms). Tocados via `winsound.PlaySound(data, SND_MEMORY)` em thread daemon. Substituem o `MessageBeep` removido do `show_toast()`.
- **Limite padrão 5 min:** `max_recording_secs` default = 300s

## Próxima Versão a Implementar

**v4.1.0 — Voice AI para macOS** — parada para macOS conforme instrução do usuário.

## Bugs Conhecidos / Atenções

- `installer.py` busca VoiceAI.exe em `sys._MEIPASS` (fix da v2.5.0) — não alterar essa lógica
- `installer.py` tem `VERSION` separada do app — manter em sincronia manualmente a cada release
- LL hook do PTT: pode conflitar com PowerToys se ambos usarem WH_KEYBOARD_LL — por isso toggle usa RegisterHotKey
- SettingsDialog height (950px): já está grande, se adicionar mais campos considerar scroll
- MainPanel inline settings: duplica a UI do SettingsDialog — manter ambos sincronizados ao adicionar novos campos
- StatusBar usa PIL para gradiente — `_draw_gradient_bg()` pixel-by-pixel pode ser lento em telas >4K, mas OK para 360x44px
- `_expanded_cards` set é baseado em `id(entry)` — resetado ao reabrir o painel
