@echo off
chcp 65001 >nul

REM Detect branch
for /f "delims=" %%b in ('git rev-parse --abbrev-ref HEAD') do set branch=%%b

REM Date/time
for /f "tokens=1-4 delims=-/ " %%i in ('date /t') do set today=%%i-%%j-%%k
for /f "tokens=1-2 delims=: " %%i in ("%TIME%") do set now=%%i-%%j
set msg=Auto commit on %today% %now%

echo ===============================
echo Current Dir: %CD%
echo Branch: %branch%
echo Message: %msg%
echo ===============================

REM 1. Stage all changes
git add .

REM 2. If there are staged changes, commit them
git diff --cached --quiet
if not %errorlevel%==0 (
    git commit -m "Temp pre-pull commit"
)

REM 3. Pull (rebase)
echo Pulling remote branch...
git pull origin %branch% --rebase
if %errorlevel% neq 0 (
    echo [Error] Pull failed! Resolve conflicts manually.
    pause
    exit /b 1
)

REM 4. Stage any new changes (from conflict resolution/merge)
git add .

REM 5. If there are staged changes, do final commit
git diff --cached --quiet
if not %errorlevel%==0 (
    git commit -m "%msg%"
)

REM 6. Push
echo Pushing to GitHub...
git push origin %branch%
if %errorlevel%==0 (
    echo Push success!
) else (
    echo [Error] Push failed!
)

pause
exit /b 0
