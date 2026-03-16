; Voice AI - Inno Setup Script
; Substitui o installer.py compilado via PyInstaller
; Gera um instalador nativo Windows confiavel para AV/SmartScreen

#define MyAppName "Voice AI"
#define MyAppPublisher "Expert Integrado"
#define MyAppURL "https://github.com/ericluciano/voice-ai"
#define MyAppExeName "VoiceAI.exe"

; Versao lida do version_cache.ini (gerado por version_info.py)
#define MyAppVersion ReadIni(SourcePath + "\version_cache.ini", "version", "value", "0.0.0")

[Setup]
AppId={{7A8E2F3B-C4D5-4E6F-A1B2-VOICEAI00001}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} v{#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}/issues
AppUpdatesURL={#MyAppURL}/releases
DefaultDirName={localappdata}\VoiceAI
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
OutputDir=dist
OutputBaseFilename=Voice AI Setup
SetupIconFile=assets\icon.ico
UninstallDisplayIcon={app}\VoiceAI.exe
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
CloseApplications=yes
CloseApplicationsFilter=VoiceAI.exe,VoiceAgent.exe
RestartApplications=no
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
VersionInfoVersion={#MyAppVersion}.0
VersionInfoCompany={#MyAppPublisher}
VersionInfoDescription={#MyAppName} - Transcritor de audio por voz
VersionInfoProductName={#MyAppName}
VersionInfoCopyright=Copyright (C) 2026 Expert Integrado

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "Criar atalho na Area de Trabalho"; GroupDescription: "Atalhos:"
Name: "startupicon"; Description: "Iniciar automaticamente com o Windows"; GroupDescription: "Inicializacao:"

[Files]
Source: "dist\VoiceAI.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\VoiceAgent.exe"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{group}\Voice Agent"; Filename: "{app}\VoiceAgent.exe"; WorkingDir: "{app}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon
Name: "{userstartup}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: startupicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Abrir Voice AI agora"; Parameters: "--open-panel"; Flags: nowait postinstall skipifsilent

