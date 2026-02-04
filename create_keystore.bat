@echo off
echo Creating SOUNDWAVE Release Keystore...
echo.

REM Try to find keytool in common locations
set "KEYTOOL_PATH="

REM Check JAVA_HOME
if exist "%JAVA_HOME%\bin\keytool.exe" (
    set "KEYTOOL_PATH=%JAVA_HOME%\bin\keytool.exe"
    goto :found
)

REM Check Android Studio's embedded JDK
if exist "%LOCALAPPDATA%\Android\Sdk\openjdk" (
    for /f "delims=" %%i in ('dir /b /s "%LOCALAPPDATA%\Android\Sdk\openjdk\keytool.exe" 2^>nul') do (
        set "KEYTOOL_PATH=%%i"
        goto :found
    )
)

REM Check Program Files for JDK
if exist "C:\Program Files\Android" (
    for /f "delims=" %%i in ('dir /b /s "C:\Program Files\Android\*\keytool.exe" 2^>nul') do (
        set "KEYTOOL_PATH=%%i"
        goto :found
    )
)

echo ERROR: Could not find keytool.exe
echo Please make sure Java JDK is installed or Android Studio is installed.
echo.
echo You can manually create the keystore using:
echo keytool -genkeypair -v -keystore keystore\soundwave_release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias soundwave_key
pause
exit /b 1

:found
echo Found keytool at: %KEYTOOL_PATH%
echo.

"%KEYTOOL_PATH%" -genkeypair -v ^
    -keystore keystore\soundwave_release.jks ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 10000 ^
    -alias soundwave_key ^
    -storepass soundwave2026 ^
    -keypass soundwave2026 ^
    -dname "CN=SoundWave, OU=Music Player, O=SoundWave, L=Cairo, ST=Cairo, C=EG"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================
    echo Keystore created successfully!
    echo ================================
    echo.
    echo Location: keystore\soundwave_release.jks
    echo Alias: soundwave_key
    echo Store Password: soundwave2026
    echo Key Password: soundwave2026
    echo.
    echo IMPORTANT: Keep the keystore file and passwords safe!
    echo.
) else (
    echo.
    echo ERROR: Failed to create keystore
)

pause
