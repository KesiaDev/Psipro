"""
Voice AI — Instalador para cliente
Janela única: tutorial de API key + instalação automática.
"""

import os
import sys
import json
import shutil
import base64
import ctypes
import winreg
import threading
import subprocess
import tkinter as tk
from tkinter import font as tkfont
import webbrowser
from datetime import datetime

VERSION = "4.1.6"  # Manter em sincronia com whisper_transcriber.py

# ─── Ícone embutido (mesmo do app principal) ──────────────────
ICON_B64 = "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"

# ─── Paleta de cores ──────────────────────────────────────────
BG         = "#141820"   # background dark  — 213 13% 9%
PANEL      = "#191E26"   # card dark        — 213 13% 11%
ACCENT     = "#1C2D4A"   # secondary dark   — 217 32% 17%
HIGHLIGHT  = "#21A353"   # primary verde    — 142 70% 45%
TEXT       = "#F1F3F5"   # foreground claro — 210 11% 96%
TEXT_DIM   = "#8D9DB5"   # muted-foreground — 215 20% 65%
SUCCESS    = "#1DCC65"   # primary-glow     — 142 85% 55%
BTN_BG     = "#21A353"   # verde principal
BTN_HOVER  = "#1A8744"   # verde hover (escuro)

# ─── Helpers ─────────────────────────────────────────────────

def install_dir():
    return os.path.join(os.environ.get("LOCALAPPDATA", os.path.expanduser("~")), "VoiceAI")

def _search_dirs():
    """Retorna lista de pastas onde procurar arquivos embutidos."""
    dirs = []
    if getattr(sys, "_MEIPASS", None):
        dirs.append(sys._MEIPASS)
    if getattr(sys, "frozen", False):
        dirs.append(os.path.dirname(sys.executable))
    base = os.path.dirname(os.path.abspath(__file__))
    dirs.append(base)
    dirs.append(os.path.join(base, "dist"))
    return dirs

def exe_source():
    """Localiza VoiceAI.exe — busca em _MEIPASS (frozen), mesma pasta, e dist/."""
    for folder in _search_dirs():
        candidate = os.path.join(folder, "VoiceAI.exe")
        if os.path.exists(candidate):
            return candidate
    return None

def agent_exe_source():
    """Localiza VoiceAgent.exe — busca em _MEIPASS (frozen), mesma pasta, e dist/."""
    for folder in _search_dirs():
        candidate = os.path.join(folder, "VoiceAgent.exe")
        if os.path.exists(candidate):
            return candidate
    return None

def find_bundled_file(filename):
    """Localiza arquivo embutido no installer (voice_agent.py, etc.)."""
    for folder in _search_dirs():
        candidate = os.path.join(folder, filename)
        if os.path.exists(candidate):
            return candidate
    return None

def create_shortcut(target, shortcut_path, working_dir="", description="Voice AI"):
    script = (
        f'$ws = New-Object -ComObject WScript.Shell; '
        f'$s = $ws.CreateShortcut("{shortcut_path}"); '
        f'$s.TargetPath = "{target}"; '
        f'$s.WorkingDirectory = "{working_dir}"; '
        f'$s.Description = "{description}"; '
        f'$s.Save()'
    )
    subprocess.run(
        ["powershell", "-NoProfile", "-Command", script],
        capture_output=True
    )

def register_app_in_windows(exe_path, version=VERSION):
    """Registra o app no Windows (Apps & Features) via registry HKCU."""
    idir = install_dir()
    key_path = r"Software\Microsoft\Windows\CurrentVersion\Uninstall\VoiceAI"
    try:
        key = winreg.CreateKeyEx(winreg.HKEY_CURRENT_USER, key_path, 0, winreg.KEY_WRITE)
        values = {
            "DisplayName": "Voice AI",
            "DisplayVersion": version,
            "Publisher": "Expert Integrado",
            "InstallLocation": idir,
            "UninstallString": f'"{exe_path}" --uninstall',
            "QuietUninstallString": f'"{exe_path}" --uninstall --quiet',
            "DisplayIcon": f"{exe_path},0",
            "NoModify": 1,
            "NoRepair": 1,
            "EstimatedSize": os.path.getsize(exe_path) // 1024,
            "InstallDate": datetime.now().strftime("%Y%m%d"),
            "URLInfoAbout": "https://github.com/ericluciano/voice-ai",
        }
        for name, value in values.items():
            if isinstance(value, int):
                winreg.SetValueEx(key, name, 0, winreg.REG_DWORD, value)
            else:
                winreg.SetValueEx(key, name, 0, winreg.REG_SZ, value)
        winreg.CloseKey(key)
    except Exception:
        pass  # Non-critical: app funciona mesmo sem registry

def is_admin():
    try:
        return ctypes.windll.shell32.IsUserAnAdmin()
    except Exception:
        return False


# ─── Janela principal ─────────────────────────────────────────

class InstallerApp:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Voice AI — Instalação")
        self.root.configure(bg=BG)
        self.root.resizable(False, False)

        # Centralizar
        w, h = 860, 600
        sw = self.root.winfo_screenwidth()
        sh = self.root.winfo_screenheight()
        self.root.geometry(f"{w}x{h}+{(sw-w)//2}+{(sh-h)//2}")

        # Ícone
        try:
            ico_data = base64.b64decode(ICON_B64)
            import tempfile
            tmp = tempfile.NamedTemporaryFile(suffix=".ico", delete=False)
            tmp.write(ico_data)
            tmp.close()
            self.root.iconbitmap(tmp.name)
        except Exception:
            pass

        self._build_ui()
        self.root.mainloop()

    # ── Layout ──────────────────────────────────────────────

    def _build_ui(self):
        root = self.root

        # Cabeçalho
        hdr = tk.Frame(root, bg=ACCENT, height=64)
        hdr.pack(fill="x")
        hdr.pack_propagate(False)
        tk.Label(
            hdr, text="Voice AI", font=("Segoe UI", 22, "bold"),
            bg=ACCENT, fg=TEXT
        ).pack(side="left", padx=24, pady=12)
        tk.Label(
            hdr, text="Instalação rápida · Expert Integrado",
            font=("Segoe UI", 11), bg=ACCENT, fg=TEXT_DIM
        ).pack(side="left", padx=0, pady=18)

        # Corpo: dois painéis lado a lado
        body = tk.Frame(root, bg=BG)
        body.pack(fill="both", expand=True, padx=20, pady=16)

        self._build_left(body)
        self._build_right(body)

    def _build_left(self, parent):
        """Painel esquerdo: tutorial de como pegar a API key."""
        frame = tk.Frame(parent, bg=PANEL, bd=0)
        frame.pack(side="left", fill="both", expand=True, padx=(0, 10))

        tk.Label(
            frame, text="Como obter sua API Key da OpenAI",
            font=("Segoe UI", 12, "bold"), bg=PANEL, fg=HIGHLIGHT
        ).pack(anchor="w", padx=16, pady=(16, 4))

        tk.Label(
            frame,
            text="A API Key é o que conecta o Voice AI ao serviço\nde transcrição da OpenAI. É simples e gratuito criar.",
            font=("Segoe UI", 10), bg=PANEL, fg=TEXT_DIM,
            justify="left"
        ).pack(anchor="w", padx=16, pady=(0, 12))

        steps = [
            ("1", "Acesse platform.openai.com/api-keys", "Clique no botão abaixo para abrir no navegador."),
            ("2", "Faça login ou crie uma conta", "A conta é gratuita. Use seu email do Google se quiser."),
            ("3", 'Clique em "Create new secret key"', 'De um nome como "Voice AI" e clique em Create.'),
            ("4", "Copie a key gerada", "Começa com sk-... Aparece só uma vez — copie agora!"),
            ("5", "Cole no campo ao lado e instale", "Pronto. O Voice AI estará funcionando em 30 segundos."),
        ]

        for num, title, desc in steps:
            row = tk.Frame(frame, bg=PANEL)
            row.pack(fill="x", padx=16, pady=4)

            # Número
            circle = tk.Label(
                row, text=num, font=("Segoe UI", 10, "bold"),
                bg=HIGHLIGHT, fg="white", width=2, relief="flat"
            )
            circle.pack(side="left", padx=(0, 10))

            col = tk.Frame(row, bg=PANEL)
            col.pack(side="left", fill="x", expand=True)
            tk.Label(
                col, text=title, font=("Segoe UI", 10, "bold"),
                bg=PANEL, fg=TEXT, anchor="w"
            ).pack(anchor="w")
            tk.Label(
                col, text=desc, font=("Segoe UI", 9),
                bg=PANEL, fg=TEXT_DIM, anchor="w"
            ).pack(anchor="w")

        # Botão abrir site
        btn = tk.Button(
            frame,
            text="Abrir platform.openai.com/api-keys →",
            font=("Segoe UI", 10, "bold"),
            bg=BTN_BG, fg="white", relief="flat",
            cursor="hand2", bd=0, padx=12, pady=8,
            command=lambda: webbrowser.open("https://platform.openai.com/api-keys")
        )
        btn.pack(padx=16, pady=(12, 8), fill="x")
        btn.bind("<Enter>", lambda e: btn.config(bg=BTN_HOVER))
        btn.bind("<Leave>", lambda e: btn.config(bg=BTN_BG))

        tk.Label(
            frame,
            text="Custo estimado: ~R$ 0,02 / minuto de áudio gravado.\nPara uso típico (20 min/dia), menos de R$ 12/mês.",
            font=("Segoe UI", 9), bg=PANEL, fg=TEXT_DIM,
            justify="left"
        ).pack(anchor="w", padx=16, pady=(4, 16))

    def _build_right(self, parent):
        """Painel direito: campo de API key + botão instalar."""
        frame = tk.Frame(parent, bg=PANEL, bd=0)
        frame.pack(side="right", fill="both", expand=True, padx=(10, 0))

        tk.Label(
            frame, text="Instalar Voice AI",
            font=("Segoe UI", 12, "bold"), bg=PANEL, fg=HIGHLIGHT
        ).pack(anchor="w", padx=16, pady=(16, 4))

        tk.Label(
            frame,
            text="Cole aqui a API Key da OpenAI:",
            font=("Segoe UI", 10), bg=PANEL, fg=TEXT
        ).pack(anchor="w", padx=16, pady=(8, 2))

        # Pré-preenche com a API key existente (se já instalado)
        existing_key = ""
        try:
            config_path = os.path.join(install_dir(), "config.json")
            if os.path.exists(config_path):
                with open(config_path, "r", encoding="utf-8") as _f:
                    existing_key = json.load(_f).get("api_key", "")
        except Exception:
            pass

        # Entry da API key
        self.key_var = tk.StringVar(value=existing_key)
        entry_frame = tk.Frame(frame, bg=ACCENT, bd=1, relief="flat")
        entry_frame.pack(fill="x", padx=16, pady=(0, 4))
        self.key_entry = tk.Entry(
            entry_frame,
            textvariable=self.key_var,
            font=("Consolas", 10),
            bg="#0d2137", fg=TEXT, insertbackground=TEXT,
            relief="flat", bd=8, show="•"
        )
        self.key_entry.pack(fill="x")

        # Toggle mostrar/ocultar
        self.show_key = False
        toggle = tk.Label(
            frame, text="Mostrar key", font=("Segoe UI", 9),
            bg=PANEL, fg=HIGHLIGHT, cursor="hand2"
        )
        toggle.pack(anchor="e", padx=16)
        toggle.bind("<Button-1>", lambda e: self._toggle_key(toggle))

        # Informação de onde vai instalar
        idir = install_dir()
        tk.Label(
            frame,
            text=f"O Voice AI será instalado em:\n{idir}",
            font=("Segoe UI", 9), bg=PANEL, fg=TEXT_DIM,
            justify="left"
        ).pack(anchor="w", padx=16, pady=(16, 4))

        # Checkbox startup
        self.startup_var = tk.BooleanVar(value=True)
        chk = tk.Checkbutton(
            frame,
            text="Iniciar automaticamente com o Windows",
            variable=self.startup_var,
            font=("Segoe UI", 10), bg=PANEL, fg=TEXT,
            selectcolor=ACCENT, activebackground=PANEL,
            activeforeground=TEXT, relief="flat"
        )
        chk.pack(anchor="w", padx=16, pady=(4, 0))

        # Checkbox atalho desktop
        self.desktop_var = tk.BooleanVar(value=True)
        chk2 = tk.Checkbutton(
            frame,
            text="Criar atalho na Área de Trabalho",
            variable=self.desktop_var,
            font=("Segoe UI", 10), bg=PANEL, fg=TEXT,
            selectcolor=ACCENT, activebackground=PANEL,
            activeforeground=TEXT, relief="flat"
        )
        chk2.pack(anchor="w", padx=16, pady=(4, 0))

        # Status label
        self.status_var = tk.StringVar(value="")
        self.status_lbl = tk.Label(
            frame, textvariable=self.status_var,
            font=("Segoe UI", 9), bg=PANEL, fg=TEXT_DIM,
            justify="left", wraplength=340
        )
        self.status_lbl.pack(anchor="w", padx=16, pady=(12, 4))

        # Botão instalar
        self.install_btn = tk.Button(
            frame,
            text="Instalar agora",
            font=("Segoe UI", 13, "bold"),
            bg=BTN_BG, fg="white", relief="flat",
            cursor="hand2", bd=0, padx=12, pady=12,
            command=self._start_install
        )
        self.install_btn.pack(fill="x", padx=16, pady=(4, 8))
        self.install_btn.bind("<Enter>", lambda e: self.install_btn.config(bg=BTN_HOVER))
        self.install_btn.bind("<Leave>", lambda e: self.install_btn.config(bg=BTN_BG))

        # Rodapé
        tk.Label(
            frame,
            text="Como usar após instalar:\n"
                 "• Abra o Voice AI pelo atalho na Área de Trabalho\n"
                 "• Um ícone aparecerá perto do relógio\n"
                 "• Pressione Ctrl+Alt+Space para gravar\n"
                 "• Solte para transcrever — texto aparece onde seu cursor estiver",
            font=("Segoe UI", 9), bg=PANEL, fg=TEXT_DIM,
            justify="left"
        ).pack(anchor="w", padx=16, pady=(8, 16))

    # ── Ações ────────────────────────────────────────────────

    def _toggle_key(self, label):
        self.show_key = not self.show_key
        self.key_entry.config(show="" if self.show_key else "•")
        label.config(text="Ocultar key" if self.show_key else "Mostrar key")

    def _set_status(self, msg, color=TEXT_DIM):
        self.status_var.set(msg)
        self.status_lbl.config(fg=color)
        self.root.update_idletasks()

    def _start_install(self):
        api_key = self.key_var.get().strip()
        if not api_key:
            self._set_status("Cole sua API Key antes de instalar.", HIGHLIGHT)
            return
        if not api_key.startswith("sk-"):
            self._set_status(
                "Isso não parece uma API Key válida.\nElas sempre começam com sk-...",
                HIGHLIGHT
            )
            return

        src = exe_source()
        if not src:
            self._set_status(
                "VoiceAI.exe não encontrado.\nCertifique-se de que o instalador está na pasta correta.",
                HIGHLIGHT
            )
            return

        self.install_btn.config(state="disabled", text="Instalando...")
        threading.Thread(target=self._do_install, args=(api_key, src), daemon=True).start()

    def _do_install(self, api_key, src):
        try:
            idir = install_dir()
            self._set_status("Criando pasta de instalação...")
            os.makedirs(idir, exist_ok=True)

            # Encerra instância anterior para liberar o EXE antes de copiar
            self._set_status("Encerrando versão anterior (se ativa)...")
            try:
                subprocess.run(
                    ["taskkill", "/F", "/IM", "VoiceAI.exe"],
                    capture_output=True
                )
                import time as _time
                _time.sleep(0.8)
            except Exception:
                pass

            self._set_status("Copiando VoiceAI.exe...")
            shutil.copy2(src, os.path.join(idir, "VoiceAI.exe"))

            # Copiar VoiceAgent.exe
            agent_src = agent_exe_source()
            if agent_src:
                self._set_status("Copiando VoiceAgent.exe...")
                # Encerrar VoiceAgent se estiver rodando
                try:
                    subprocess.run(
                        ["taskkill", "/F", "/IM", "VoiceAgent.exe"],
                        capture_output=True
                    )
                    import time as _time2
                    _time2.sleep(0.8)
                except Exception:
                    pass
                shutil.copy2(agent_src, os.path.join(idir, "VoiceAgent.exe"))

            self._set_status("Salvando configuração...")
            config_path = os.path.join(idir, "config.json")
            # Preserva configurações existentes (hotkey, idioma, modelo, etc.)
            config = {}
            if os.path.exists(config_path):
                try:
                    with open(config_path, "r", encoding="utf-8") as f:
                        config = json.load(f)
                except Exception:
                    config = {}
            # Só sobrescreve a API key (que veio do campo do instalador)
            config["api_key"] = api_key
            with open(config_path, "w", encoding="utf-8") as f:
                json.dump(config, f, indent=4)

            exe_path = os.path.join(idir, "VoiceAI.exe")

            if self.desktop_var.get():
                self._set_status("Criando atalho na Área de Trabalho...")
                desktop = os.path.join(os.environ.get("USERPROFILE", ""), "Desktop")
                create_shortcut(exe_path, os.path.join(desktop, "Voice AI.lnk"), idir,
                               "Voice AI - Transcritor por voz")

            if self.startup_var.get():
                self._set_status("Configurando inicialização automática...")
                startup = os.path.join(
                    os.environ.get("APPDATA", ""),
                    "Microsoft", "Windows", "Start Menu", "Programs", "Startup"
                )
                create_shortcut(exe_path, os.path.join(startup, "Voice AI.lnk"), idir)

            # Menu Iniciar
            self._set_status("Criando atalhos no Menu Iniciar...")
            start_menu = os.path.join(
                os.environ.get("APPDATA", ""),
                "Microsoft", "Windows", "Start Menu", "Programs"
            )
            create_shortcut(exe_path, os.path.join(start_menu, "Voice AI.lnk"), idir,
                           "Voice AI - Transcritor por voz")

            # Atalho do Voice Agent no Menu Iniciar
            agent_path = os.path.join(idir, "VoiceAgent.exe")
            if os.path.exists(agent_path):
                create_shortcut(agent_path, os.path.join(start_menu, "Voice Agent.lnk"), idir,
                               "Voice Agent - Assistente de voz com IA")

            # Registrar no Windows (Apps & Features)
            self._set_status("Registrando no Windows...")
            register_app_in_windows(exe_path)

            self._set_status("Instalação concluída! ✓", SUCCESS)
            self.root.after(200, self._show_success)

        except Exception as ex:
            self._set_status(f"Erro durante instalação:\n{ex}", HIGHLIGHT)
            self.root.after(0, lambda: self.install_btn.config(
                state="normal", text="Instalar agora"
            ))

    def _show_success(self):
        """Substitui o conteúdo da janela pela tela de sucesso."""
        for widget in self.root.winfo_children():
            widget.destroy()

        frame = tk.Frame(self.root, bg=BG)
        frame.pack(fill="both", expand=True)

        tk.Label(
            frame, text="✓", font=("Segoe UI", 64),
            bg=BG, fg=SUCCESS
        ).pack(pady=(60, 8))

        tk.Label(
            frame, text="Voice AI instalado com sucesso!",
            font=("Segoe UI", 18, "bold"), bg=BG, fg=TEXT
        ).pack()

        tk.Label(
            frame,
            text=(
                "O que fazer agora:\n\n"
                "1. Feche este instalador\n"
                "2. Clique duas vezes em \"Voice AI\" na Área de Trabalho\n"
                "3. Um ícone de microfone aparecerá perto do relógio\n"
                "4. Pressione  Ctrl + Alt + Space  para gravar\n"
                "5. Solte — o texto aparece onde seu cursor estiver"
            ),
            font=("Segoe UI", 11), bg=BG, fg=TEXT_DIM,
            justify="center"
        ).pack(pady=24)

        btn = tk.Button(
            frame,
            text="Abrir Voice AI agora",
            font=("Segoe UI", 12, "bold"),
            bg=BTN_BG, fg="white", relief="flat",
            cursor="hand2", bd=0, padx=20, pady=10,
            command=self._launch_and_close
        )
        btn.pack(pady=8)
        btn.bind("<Enter>", lambda e: btn.config(bg=BTN_HOVER))
        btn.bind("<Leave>", lambda e: btn.config(bg=BTN_BG))

        tk.Button(
            frame,
            text="Fechar instalador",
            font=("Segoe UI", 10),
            bg=PANEL, fg=TEXT_DIM, relief="flat",
            cursor="hand2", bd=0, padx=12, pady=6,
            command=self.root.destroy
        ).pack(pady=4)

    def _launch_and_close(self):
        exe = os.path.join(install_dir(), "VoiceAI.exe")
        if os.path.exists(exe):
            subprocess.Popen([exe, "--open-panel"], cwd=install_dir())
        self.root.destroy()


# ─── Entry point ─────────────────────────────────────────────

if __name__ == "__main__":
    InstallerApp()
