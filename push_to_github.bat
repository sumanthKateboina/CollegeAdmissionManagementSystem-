@echo off
setlocal
title Push College Admission Management System to GitHub
echo ====================================================================
echo        PUSH COLLEGE ADMISSION MANAGEMENT SYSTEM TO GITHUB
echo ====================================================================
echo.
echo Make sure you have created an empty repository on GitHub first!
echo (Link: https://github.com/new)
echo Suggested Repo Name: CollegeAdmissionManagementSystem
echo.
set /p REPO_URL="Enter your GitHub repository URL (e.g. https://github.com/sumanthKateboina/CollegeAdmissionManagementSystem.git): "

if "%REPO_URL%"=="" (
    echo [ERROR] No URL entered! Exiting.
    pause
    exit /b
)

echo.
echo [1/3] Checking Git remote...
git remote remove origin >nul 2>&1
git remote add origin %REPO_URL%

echo [2/3] Setting branch to main...
git branch -M main

echo [3/3] Pushing to GitHub...
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================================
    echo [SUCCESS] Project pushed successfully to GitHub!
    echo URL: %REPO_URL%
    echo ====================================================================
) else (
    echo.
    echo [NOTE] If prompted for credentials, use your GitHub Personal Access Token (PAT).
)

pause
