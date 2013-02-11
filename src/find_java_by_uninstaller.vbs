Const HKEY_LOCAL_MACHINE = &H80000002
Const ForReading = 1, ForWriting = 2
strComputer = "."
 
Set WshShell = WScript.CreateObject("WScript.Shell")
Set oReg=GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & strComputer & "\root\default:StdRegProv")
 
' Get array of subkeys under uninstall registry entry
strKeyPath = "SOFTWARE\microsoft\windows\currentversion\uninstall"
oReg.EnumKey HKEY_LOCAL_MACHINE, strKeyPath, arrSubKeys
 
For Each subkey In arrSubKeys
    CheckForJava subkey
Next
 
wscript.quit
 
 
sub CheckForJava(strKey)
	On Error Resume Next
	'DisplayName
	DisplayName=WshShell.RegRead ("HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\" & strKey & "\DisplayName")
	'Publisher
	Publisher=WshShell.RegRead ("HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\" & strKey & "\Publisher")
	 
	'Search for presence of Java and Sun in DisplayName and Publisher
	search1 = Instr(1, DisplayName, "Java", 1)
	search2 = Instr(1, Publisher, "Sun", 1)
    search3 = Instr(1, DisplayName, "J2SE", 1)
	search4 = Instr(1, Publisher, "Oracle", 1)
	 
	'Execute removal if there is a match
	   if (search1>0 And search2>0) Or (search3>0 And search2>0) Or (search3>0 And search4>0) Then
		wscript.echo "Java Version Found:" & strKey
	   end if
end sub