@echo off
chcp 65001 >nul
echo ==========================================
echo GIT PUSH SCRIPT FOR TUNGTUNG PROJECT
echo ==========================================
echo.

REM Kiểm tra xem có phải git repo không
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Không phải git repository!
    echo Vui lòng chạy: git init
echo.
    pause
    exit /b 1
)

echo [1/5] Kiểm tra remote origin...
git remote -v

echo.
echo [2/5] Thêm remote origin (nếu chưa có)...
git remote add origin https://github.com/nguyenthetan752005-max/MobileFinalGroup11.git 2>nul
git remote set-url origin https://github.com/nguyenthetan752005-max/MobileFinalGroup11.git

echo.
echo [3/5] Kiểm tra branch hiện tại...
for /f "tokens=*" %%a in ('git branch --show-current') do set BRANCH=%%a
echo Branch hiện tại: %BRANCH%

echo.
echo [4/5] Add và commit tất cả file...
git add .
git commit -m "Initial commit: TungTung Android app with Room, MVVM, and backend sync" 2>nul
if errorlevel 1 (
    echo Không có thay đổi mới hoặc đã commit trước đó.
)

echo.
echo [5/5] Push lên GitHub...
if "%BRANCH%"=="master" (
    git push -u origin master:main
) else (
    git push -u origin %BRANCH%
)

if errorlevel 1 (
    echo.
    echo [ERROR] Push thất bại!
    echo Có thể cần đăng nhập GitHub hoặc xung đột branch.
    echo Thử chạy: git push -f origin main (force push - CẨN THẬN!)
    pause
    exit /b 1
)

echo.
echo ==========================================
echo SUCCESS! Code đã push lên GitHub
echo Repository: https://github.com/nguyenthetan752005-max/MobileFinalGroup11
echo ==========================================
pause
