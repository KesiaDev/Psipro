"""
Agent Window — Interface gráfica do Claude Voice Agent.
Chat UI com Tkinter, estados visuais, histórico de mensagens.
"""

import logging
import threading
import tkinter as tk
from tkinter import ttk
import time

log = logging.getLogger(__name__).info

# Importação condicional para evitar dependência circular
_get_theme = None


def _init_theme():
    global _get_theme
    if _get_theme is None:
        try:
            from whisper_transcriber import get_theme
            _get_theme = get_theme
        except ImportError:
            _get_theme = lambda name=None: {
                "bg_primary": "#141820", "bg_secondary": "#191E26",
                "bg_tertiary": "#1C2D4A", "bg_elevated": "#1A1F2A",
                "accent": "#21A353", "text_primary": "#F1F3F5",
                "text_secondary": "#8D9DB5", "text_dim": "#6B7A90",
                "success": "#66bb6a", "warning": "#ffaa00",
                "error": "#ff4444", "info": "#6c8dfa",
                "processing": "#a78bfa", "border": "#1C2D4A",
                "white": "#ffffff",
            }


# ─── Cores de estado ─────────────────────────────────────────

STATE_COLORS = {
    "idle":         "#6B7A90",   # cinza
    "listening":    "#21A353",   # verde
    "transcribing": "#ffaa00",  # amarelo
    "thinking":     "#a78bfa",  # roxo
    "speaking":     "#6c8dfa",  # azul
    "error":        "#ff4444",  # vermelho
}

STATE_LABELS = {
    "idle":         "Aguardando...",
    "listening":    "Ouvindo...",
    "transcribing": "Transcrevendo...",
    "thinking":     "Pensando...",
    "speaking":     "Falando...",
    "error":        "Erro",
}


class AgentWindow:
    """Janela do Claude Voice Agent com chat UI."""

    def __init__(self, agent_core, root=None):
        _init_theme()
        self.agent = agent_core
        self._root = root
        self.win = None
        self._messages = []  # [{role: "user"|"agent", text: str, time: str}]
        self._visible = False

        # Conectar callbacks do agent_core
        self.agent.on_state_change = self._on_state_change
        self.agent.on_user_transcript = self._on_user_transcript
        self.agent.on_agent_text = self._on_agent_text
        self.agent.on_audio_level = self._on_audio_level
        self.agent.on_error = self._on_error

    @property
    def is_visible(self):
        return self._visible

    def show(self):
        """Abre a janela do agente."""
        if self._visible and self.win:
            try:
                self.win.lift()
                self.win.focus_force()
                return
            except tk.TclError:
                pass

        self._create_window()
        self._visible = True

    def hide(self):
        """Fecha a janela do agente."""
        if self.win:
            try:
                self.win.withdraw()
            except tk.TclError:
                pass
        self._visible = False

    def toggle(self):
        """Alterna visibilidade."""
        if self._visible:
            self.hide()
        else:
            self.show()

    def _create_window(self):
        """Cria a janela Toplevel."""
        t = _get_theme()

        if self._root:
            self.win = tk.Toplevel(self._root)
        else:
            self.win = tk.Tk()

        self.win.title("Claude Voice Agent")
        self.win.geometry("650x700")
        self.win.minsize(500, 400)
        self.win.configure(bg=t["bg_primary"])
        self.win.protocol("WM_DELETE_WINDOW", self.hide)

        # Tentar usar ícone do app
        try:
            import os, sys
            if getattr(sys, "frozen", False):
                icon_dir = os.path.dirname(sys.executable)
            else:
                icon_dir = os.path.dirname(os.path.abspath(__file__))
            icon_path = os.path.join(icon_dir, "assets", "icon.ico")
            if os.path.exists(icon_path):
                self.win.iconbitmap(icon_path)
        except Exception:
            pass

        self._build_ui(t)
        self._render_messages(t)

    def _build_ui(self, t):
        """Constrói todos os widgets."""
        # ─── Header ──────────────────────────────────────
        header = tk.Frame(self.win, bg=t["bg_secondary"], height=50)
        header.pack(fill="x")
        header.pack_propagate(False)

        tk.Label(
            header, text="Claude Voice Agent",
            font=("Segoe UI Semibold", 14),
            fg=t["text_primary"], bg=t["bg_secondary"],
        ).pack(side="left", padx=16, pady=10)

        # Botão Nova Conversa
        btn_new = tk.Button(
            header, text="Nova conversa",
            font=("Segoe UI", 9),
            fg=t["text_secondary"], bg=t["bg_tertiary"],
            activeforeground=t["text_primary"], activebackground=t["accent"],
            bd=0, padx=12, pady=4,
            cursor="hand2",
            command=self._new_conversation,
        )
        btn_new.pack(side="right", padx=16, pady=12)

        # ─── Chat area ───────────────────────────────────
        self._chat_frame_outer = tk.Frame(self.win, bg=t["bg_primary"])
        self._chat_frame_outer.pack(fill="both", expand=True, padx=0, pady=0)

        self._canvas = tk.Canvas(
            self._chat_frame_outer,
            bg=t["bg_primary"], highlightthickness=0,
        )
        self._scrollbar = ttk.Scrollbar(
            self._chat_frame_outer, orient="vertical",
            command=self._canvas.yview,
        )
        self._chat_frame = tk.Frame(self._canvas, bg=t["bg_primary"])

        self._chat_frame.bind(
            "<Configure>",
            lambda e: self._canvas.configure(scrollregion=self._canvas.bbox("all")),
        )
        self._canvas_window = self._canvas.create_window(
            (0, 0), window=self._chat_frame, anchor="nw",
        )
        self._canvas.configure(yscrollcommand=self._scrollbar.set)

        self._scrollbar.pack(side="right", fill="y")
        self._canvas.pack(side="left", fill="both", expand=True)

        # Ajustar largura do chat_frame ao canvas
        self._canvas.bind("<Configure>", self._on_canvas_configure)

        # Mouse wheel scroll
        self._canvas.bind_all("<MouseWheel>", self._on_mousewheel)

        # ─── Status bar ──────────────────────────────────
        self._status_frame = tk.Frame(self.win, bg=t["bg_secondary"], height=50)
        self._status_frame.pack(fill="x", side="bottom")
        self._status_frame.pack_propagate(False)

        # Indicador de estado (bolinha colorida + texto)
        self._status_dot = tk.Canvas(
            self._status_frame, width=14, height=14,
            bg=t["bg_secondary"], highlightthickness=0,
        )
        self._status_dot.pack(side="left", padx=(16, 6), pady=18)
        self._status_dot_id = self._status_dot.create_oval(2, 2, 12, 12, fill="#6B7A90", outline="")

        self._status_label = tk.Label(
            self._status_frame, text="Aguardando...",
            font=("Segoe UI", 10),
            fg=t["text_secondary"], bg=t["bg_secondary"],
        )
        self._status_label.pack(side="left", pady=10)

        # Nível de áudio (barra simples)
        self._level_canvas = tk.Canvas(
            self._status_frame, width=120, height=8,
            bg=t["bg_tertiary"], highlightthickness=0,
        )
        self._level_canvas.pack(side="left", padx=(20, 0), pady=21)
        self._level_bar = self._level_canvas.create_rectangle(0, 0, 0, 8, fill=t["accent"], outline="")

        # Input de texto (modo push_enter)
        self._input_frame = tk.Frame(self.win, bg=t["bg_secondary"])
        self._input_frame.pack(fill="x", side="bottom", before=self._status_frame)

        self._text_input = tk.Entry(
            self._input_frame,
            font=("Segoe UI", 11),
            fg=t["text_primary"], bg=t["bg_tertiary"],
            insertbackground=t["text_primary"],
            bd=0, relief="flat",
        )
        self._text_input.pack(fill="x", padx=12, pady=8, ipady=6)
        self._text_input.bind("<Return>", self._on_text_submit)
        self._text_input.insert(0, "Digite ou fale...")
        self._text_input.bind("<FocusIn>", lambda e: self._clear_placeholder())
        self._text_input.bind("<FocusOut>", lambda e: self._restore_placeholder())
        self._text_input.config(fg=t["text_dim"])
        self._placeholder_active = True

    def _on_canvas_configure(self, event):
        self._canvas.itemconfig(self._canvas_window, width=event.width)

    def _on_mousewheel(self, event):
        self._canvas.yview_scroll(int(-1 * (event.delta / 120)), "units")

    def _clear_placeholder(self):
        if self._placeholder_active:
            self._text_input.delete(0, "end")
            t = _get_theme()
            self._text_input.config(fg=t["text_primary"])
            self._placeholder_active = False

    def _restore_placeholder(self):
        if not self._text_input.get():
            t = _get_theme()
            self._text_input.insert(0, "Digite ou fale...")
            self._text_input.config(fg=t["text_dim"])
            self._placeholder_active = True

    def _on_text_submit(self, event=None):
        text = self._text_input.get().strip()
        if not text or self._placeholder_active:
            return
        self._text_input.delete(0, "end")
        self.agent.process_text_input(text)

    def _new_conversation(self):
        self.agent.new_conversation()
        self._messages = []
        t = _get_theme()
        self._render_messages(t)

    # ─── Renderização de mensagens ────────────────────────

    def _render_messages(self, t=None):
        """Redesenha todas as mensagens no chat."""
        if not self.win:
            return
        if t is None:
            t = _get_theme()

        # Limpar chat
        for widget in self._chat_frame.winfo_children():
            widget.destroy()

        if not self._messages:
            # Mensagem de boas-vindas
            self._add_message_widget(
                "Olá! Sou o Claude Voice Agent. Fale comigo ou digite abaixo.",
                "agent", t,
            )
            return

        for msg in self._messages:
            self._add_message_widget(msg["text"], msg["role"], t)

        # Scroll para baixo
        self.win.after(50, lambda: self._canvas.yview_moveto(1.0))

    def _add_message_widget(self, text, role, t):
        """Adiciona um balão de mensagem ao chat."""
        is_agent = role == "agent"

        # Container
        msg_frame = tk.Frame(self._chat_frame, bg=t["bg_primary"])
        msg_frame.pack(fill="x", padx=16, pady=4)

        # Avatar
        avatar_text = "VA" if is_agent else "Eu"
        avatar_bg = t["accent"] if is_agent else t["info"]

        avatar_canvas = tk.Canvas(
            msg_frame, width=30, height=30,
            bg=t["bg_primary"], highlightthickness=0,
        )
        avatar_canvas.create_oval(2, 2, 28, 28, fill=avatar_bg, outline="")
        avatar_canvas.create_text(
            15, 15, text=avatar_text,
            font=("Segoe UI Semibold", 8),
            fill=t["white"],
        )

        if is_agent:
            avatar_canvas.pack(side="left", anchor="n", padx=(0, 8), pady=4)
        else:
            avatar_canvas.pack(side="right", anchor="n", padx=(8, 0), pady=4)

        # Balão
        bubble_bg = t["bg_elevated"] if is_agent else t["bg_tertiary"]
        bubble = tk.Frame(msg_frame, bg=bubble_bg, padx=12, pady=8)

        text_label = tk.Label(
            bubble, text=text,
            font=("Segoe UI", 10),
            fg=t["text_primary"], bg=bubble_bg,
            wraplength=450, justify="left",
            anchor="w",
        )
        text_label.pack(fill="x")

        if is_agent:
            bubble.pack(side="left", fill="x", expand=True)
        else:
            bubble.pack(side="right", fill="x", expand=True)

    def _append_message(self, text, role):
        """Adiciona mensagem e atualiza UI."""
        self._messages.append({
            "role": role,
            "text": text,
            "time": time.strftime("%H:%M"),
        })

        if self.win:
            try:
                t = _get_theme()
                self._add_message_widget(text, role, t)
                self.win.after(50, lambda: self._canvas.yview_moveto(1.0))
            except tk.TclError:
                pass

    def _update_last_agent_message(self, text):
        """Atualiza última mensagem do agente (streaming parcial)."""
        # Encontrar último widget do agente e atualizar
        if self.win and self._chat_frame.winfo_children():
            try:
                last = self._chat_frame.winfo_children()[-1]
                # Encontrar o Label dentro do bubble
                for child in last.winfo_children():
                    if isinstance(child, tk.Frame):  # bubble
                        for label in child.winfo_children():
                            if isinstance(label, tk.Label):
                                label.config(text=text)
                                return
            except (tk.TclError, IndexError):
                pass

    # ─── Callbacks do AgentCore ──────────────────────────

    def _on_state_change(self, state):
        """Atualiza indicador de estado."""
        if not self.win:
            return
        try:
            color = STATE_COLORS.get(state, "#6B7A90")
            label = STATE_LABELS.get(state, state)
            self._status_dot.itemconfig(self._status_dot_id, fill=color)
            self._status_label.config(text=label)
        except tk.TclError:
            pass

    def _on_user_transcript(self, text):
        """Usuário falou algo — adicionar ao chat."""
        if self.win:
            try:
                self.win.after(0, lambda: self._append_message(text, "user"))
            except tk.TclError:
                pass

    def _on_agent_text(self, text, partial):
        """Claude respondeu — atualizar chat."""
        if not self.win:
            return
        try:
            if partial:
                # Streaming — atualizar última mensagem ou criar nova
                if not self._messages or self._messages[-1]["role"] != "agent":
                    self.win.after(0, lambda: self._append_message(text, "agent"))
                else:
                    self._messages[-1]["text"] = text
                    self.win.after(0, lambda: self._update_last_agent_message(text))
            else:
                # Resposta final
                if self._messages and self._messages[-1]["role"] == "agent":
                    self._messages[-1]["text"] = text
                    self.win.after(0, lambda: self._update_last_agent_message(text))
                else:
                    self.win.after(0, lambda: self._append_message(text, "agent"))
        except tk.TclError:
            pass

    def _on_audio_level(self, level):
        """Atualiza barra de nível de áudio."""
        if not self.win:
            return
        try:
            width = min(120, int(level * 3000))
            self._level_canvas.coords(self._level_bar, 0, 0, width, 8)
        except tk.TclError:
            pass

    def _on_error(self, error):
        """Exibe erro no chat."""
        if self.win:
            try:
                self.win.after(0, lambda: self._append_message(f"Erro: {error}", "agent"))
            except tk.TclError:
                pass

    # ─── Cleanup ─────────────────────────────────────────

    def destroy(self):
        """Destrói a janela."""
        if self.win:
            try:
                self.win.destroy()
            except tk.TclError:
                pass
        self.win = None
        self._visible = False
