@ECHO OFF

IF "%1" == "" GOTO USAGE
IF "%2" == "" GOTO USAGE
IF "%3" == "" GOTO USAGE

IF NOT EXIST %3 GOTO NOTARGETDIR

:START
SET count=0

:BEGINLOOP
SET /a count+=1

ECHO ON
simbatch.exe %1 %3\%4%count%.csv
@ECHO OFF

IF %count% == %2 GOTO ENDLOOP
GOTO BEGINLOOP
:ENDLOOP

ECHO.
ECHO      %2 simulations performed. Exiting...
ECHO.

GOTO FINISH

:USAGE
ECHO.
ECHO.
ECHO      Usage: %0 config_file number_of_simulations output_directory [file_prefix]
ECHO.
ECHO.
GOTO FINISH

:NOTARGETDIR
ECHO.
ECHO      Creating %3 directory...
ECHO.
md %3
GOTO START

:FINISH
