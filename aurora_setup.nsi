; aurora_setup.nsi
;
; The script for aurora setup, to be compiled by NSIS

;--------------------------------

!ifdef HAVE_UPX
!packhdr tmp.dat "upx\upx -9 tmp.dat"
!endif

!ifdef NOCOMPRESS
SetCompress off
!endif

!define PRODUCT_NAME "Aurora Road Network Modeler"

; Definitions for Java 1.6 Detection
!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=18714&/jre-6u5-windows-i586-p.exe"

;--------------------------------

Name "AuroraInstaller"
Caption "Aurora Road Network Modeler"
;Icon "${NSISDIR}\Contrib\Graphics\Icons\nsis1-install.ico"
OutFile "build\AuroraRNM_ver.exe" ; release file

SetDateSave on
SetDatablockOptimize on
CRCCheck on
SilentInstall normal
;BGGradient 000000 800000 FFFFFF
BGGradient 000000 0000FF FFFFFF
InstallColors FF8080 000030
RequestExecutionLevel admin
XPStyle on

InstallDir "$PROGRAMFILES\TOPL\Aurora"
InstallDirRegKey HKLM "Software\TOPL\Aurora" ""

;CheckBitmap "${NSISDIR}\Contrib\Graphics\Checks\classic-cross.bmp"

LicenseText "License Agreement"
LicenseData "LICENSE.txt"

;--------------------------------

Page license
Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

AutoCloseWindow false
ShowInstDetails show

;--------------------------------

Section "" ; empty string makes it hidden, so would starting with -

  Call DetectJRE 
  ; write uninstall strings
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Aurora" "DisplayName" "TOPL Aurora(remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Aurora" "UninstallString" '"$INSTDIR\aurora-uninst.exe"'

  CreateDirectory "$INSTDIR\libGUI" 
  CreateDirectory "$INSTDIR\libGIS" 
;  CreateDirectory "$INSTDIR\libdbDerby" 

  SetOutPath $INSTDIR
  File /a "silent.nsi"
  File "build\aurora.jar"
  File "AuroraRNM_UserGuide.pdf"
  File "LICENSE.txt"
  File "simbatch.bat"
  WriteUninstaller "aurora-uninst.exe"

  SetOutPath "$INSTDIR\libGUI"
  File "libGUI\commons-collections-3.2.jar"
  File "libGUI\jcommon-1.0.9.jar"
  File "libGUI\jfree*.jar"
  File "libGUI\colt.jar"
  File "libGUI\jung-1.7.6.jar"
  

  SetOutPath "$INSTDIR\libGIS"
  File "libGIS\*.jar"

  SetOutPath "$INSTDIR\icons"
  File "icons\*.*"

SectionEnd


Section "Simulator"
  SetOutPath $INSTDIR
  File "build\simulator.exe"
  File "build\simbatch.exe"
;  File "Readme.txt"
SectionEnd


Section "Configurator"
  SetOutPath $INSTDIR
  File "build\configurator.exe"
  File "build\gis_importer.exe"
SectionEnd

Section "Sample Networks"
  SetOutPath "$INSTDIR\Examples"
  File "config_files\*.*"
SectionEnd


;--------------------------------

; Uninstaller

UninstallText "This will uninstall Aurora RNM. Hit next to continue."
UninstallIcon "${NSISDIR}\Contrib\Graphics\Icons\nsis1-uninstall.ico"

Section "Uninstall"
  SetShellVarContext all

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Aurora"
  DeleteRegKey HKLM "Software\TOPL\Aurora"
  Delete "$INSTDIR\Examples\*.*"
  Delete "$INSTDIR\libGUI\*.*"
  Delete "$INSTDIR\libGIS\*.*"
  Delete "$INSTDIR\icons\*.*"

  Delete "$INSTDIR\silent.nsi"
  Delete "$INSTDIR\aurora-uninst.exe"
  Delete "$INSTDIR\simulator.exe"
  Delete "$INSTDIR\simbatch.exe"
  Delete "$INSTDIR\configurator.exe"
  Delete "$INSTDIR\*.*"
; maybe this is too brutal...
  Delete "$SMPROGRAMS\TOPL\Simulator.lnk"
  Delete "$SMPROGRAMS\TOPL\Aurora RNM User Guide.lnk"
  Delete "$SMPROGRAMS\TOPL\*.*"

  RMDir "$INSTDIR\libGIS"
  RMDir "$INSTDIR\libGUI"
  RMDir "$INSTDIR\Examples"
  RMDir "$INSTDIR\icons"
  RMDir "$INSTDIR"
  RMDir "$PROGRAMFILES\TOPL"
  RMDir "$SMPROGRAMS\TOPL"


  IfFileExists "$INSTDIR" 0 NoErrorMsg
    MessageBox MB_OK "Note: $INSTDIR could not be removed!" IDOK 0 ; skipped if file doesn't exist
  NoErrorMsg:

SectionEnd

Section "Create ShortCuts"

;  SectionIn 1 2 3

  Call CSCTest

SectionEnd


;---
Function "CSCTest"
  
  SetShellVarContext all
  
  CreateDirectory "$SMPROGRAMS\TOPL"
  Delete "$SMPROGRAMS\TOPL\*.*"
  SetOutPath $INSTDIR ; for working directory
  CreateShortCut "$SMPROGRAMS\TOPL\Simulator.lnk" "$INSTDIR\simulator.exe" 
  CreateShortCut "$SMPROGRAMS\TOPL\Configurator.lnk" "$INSTDIR\configurator.exe" 
  CreateShortCut "$SMPROGRAMS\TOPL\GIS Importer.lnk" "$INSTDIR\gis_importer.exe" 

  CreateShortCut "$SMPROGRAMS\TOPL\LICENSE.lnk" "$INSTDIR\LICENSE.txt" "" "$WINDIR\notepad.exe" 0 SW_SHOWMINIMIZED 
  CreateShortCut "$SMPROGRAMS\TOPL\Aurora RNM User Guide.lnk" "$INSTDIR\AuroraRNM_UserGuide.pdf" 

  CreateShortCut "$SMPROGRAMS\TOPL\Uninstall Aurora RNM.lnk" "$INSTDIR\aurora-uninst.exe" ; use defaults for parameters, icon, etc.

FunctionEnd

Function GetJRE
        MessageBox MB_OK "${PRODUCT_NAME} uses Java ${JRE_VERSION}, it will now \
                         be downloaded and installed"
 
        StrCpy $2 "$TEMP\Java Runtime Environment.exe"
        nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
        Pop $R0 ;Get the return value
                StrCmp $R0 "success" +3
                MessageBox MB_OK "Download failed: $R0"
                Quit
        ExecWait $2
        Delete $2
FunctionEnd
 
 
Function DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" \
             "CurrentVersion"
  StrCmp $2 ${JRE_VERSION} done
 
  Call GetJRE
 
  done:
FunctionEnd

; TODO: correct ordering of shortcuts
; TODO: partial uninstallation?
