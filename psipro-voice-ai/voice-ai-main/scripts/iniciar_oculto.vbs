Set fso = CreateObject("Scripting.FileSystemObject")
Set WshShell = CreateObject("WScript.Shell")
strPath = fso.GetParentFolderName(WScript.ScriptFullName)
WshShell.CurrentDirectory = strPath
WshShell.Run """C:\Users\Eric Luciano\AppData\Local\Python\pythoncore-3.14-64\pythonw.exe"" ""whisper_transcriber.py""", 0, False
