@echo off
echo ====================================================
echo Compiling College Admission Management System...
echo ====================================================

if not exist bin mkdir bin
if not exist output mkdir output

javac -encoding UTF-8 -cp "lib/*" -d bin src/com/college/admission/model/*.java src/com/college/admission/dao/*.java src/com/college/admission/service/*.java src/com/college/admission/ui/console/*.java src/com/college/admission/ui/gui/*.java src/com/college/admission/Main.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed successfully! Classes saved to bin/
) else (
    echo [ERROR] Compilation failed! Check error messages above.
)
pause
