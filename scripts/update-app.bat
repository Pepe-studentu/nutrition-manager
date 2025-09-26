@echo off
setlocal EnableDelayedExpansion

REM ========================================
REM  Nutrition App Auto-Updater
REM  Double-click to update to latest version
REM ========================================

title Nutrition App Updater

REM Configuration - Update these for your specific GitHub repository
set GITHUB_USER=YourGitHubUsername
set GITHUB_REPO=NutritionApp
set APP_NAME=KotlinProjectTest

REM Installation directory (default: Program Files)
set INSTALL_DIR=%ProgramFiles%\%APP_NAME%

echo.
echo ========================================
echo  Nutrition App Updater
echo ========================================
echo.
echo This will update your Nutrition App to the latest version.
echo Your data in %USERPROFILE%\NutritionApp will be preserved.
echo.
pause

echo.
echo [1/4] Checking for latest version...

REM Create temp directory
set TEMP_DIR=%TEMP%\nutrition-app-update
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

REM Download latest release info from GitHub API
powershell -Command "& {Invoke-WebRequest -Uri 'https://api.github.com/repos/%GITHUB_USER%/%GITHUB_REPO%/releases/latest' -OutFile '%TEMP_DIR%\latest.json'}" 2>nul

if not exist "%TEMP_DIR%\latest.json" (
    echo ERROR: Failed to check for updates. Please check your internet connection.
    pause
    exit /b 1
)

REM Extract download URL using PowerShell
for /f "delims=" %%i in ('powershell -Command "& {$json = Get-Content '%TEMP_DIR%\latest.json' | ConvertFrom-Json; $json.assets | Where-Object {$_.name -like '*windows*' -or $_.name -like '*exe*' -or $_.name -like '*zip*'} | Select-Object -First 1 | ForEach-Object {$_.browser_download_url}}"') do set DOWNLOAD_URL=%%i

if "!DOWNLOAD_URL!"=="" (
    echo ERROR: No Windows release found. Please check the repository.
    pause
    exit /b 1
)

echo Found latest version. Downloading...

REM Extract filename from URL
for %%f in ("!DOWNLOAD_URL!") do set FILENAME=%%~nxf
set DOWNLOAD_FILE=%TEMP_DIR%\!FILENAME!

echo [2/4] Downloading latest version...
powershell -Command "& {Invoke-WebRequest -Uri '!DOWNLOAD_URL!' -OutFile '%DOWNLOAD_FILE%'}" 2>nul

if not exist "%DOWNLOAD_FILE%" (
    echo ERROR: Failed to download update.
    pause
    exit /b 1
)

echo [3/4] Installing update...

REM Stop any running instances of the app (optional)
taskkill /f /im "%APP_NAME%.exe" 2>nul

REM Create installation directory if it doesn't exist
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Extract/copy based on file type
if /i "!FILENAME:~-4!"==".zip" (
    echo Extracting ZIP file...
    powershell -Command "& {Expand-Archive -Path '%DOWNLOAD_FILE%' -DestinationPath '%INSTALL_DIR%' -Force}"
) else if /i "!FILENAME:~-4!"==".exe" (
    echo Copying executable...
    copy "%DOWNLOAD_FILE%" "%INSTALL_DIR%\%APP_NAME%.exe" >nul
) else (
    echo Copying application files...
    copy "%DOWNLOAD_FILE%" "%INSTALL_DIR%\" >nul
)

echo [4/4] Cleaning up...
rmdir /s /q "%TEMP_DIR%"

REM Create desktop shortcut if it doesn't exist
set SHORTCUT_PATH=%USERPROFILE%\Desktop\Nutrition App.lnk
if not exist "%SHORTCUT_PATH%" (
    echo Creating desktop shortcut...
    powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%SHORTCUT_PATH%'); $Shortcut.TargetPath = '%INSTALL_DIR%\%APP_NAME%.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Save()}"
)

echo.
echo ========================================
echo  Update Complete!
echo ========================================
echo.
echo The Nutrition App has been updated successfully.
echo Your data is preserved in: %USERPROFILE%\NutritionApp
echo.
echo You can now close this window and launch the app from:
echo - Desktop shortcut (if created)
echo - Start menu
echo - %INSTALL_DIR%\%APP_NAME%.exe
echo.
pause

REM Optional: Launch the app after update
set /p LAUNCH="Launch the app now? (y/n): "
if /i "!LAUNCH!"=="y" (
    start "" "%INSTALL_DIR%\%APP_NAME%.exe"
)

exit /b 0