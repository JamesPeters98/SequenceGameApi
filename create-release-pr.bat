@echo off
setlocal

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd"') do set RELEASE_DATE=%%I

gh pr create --base main --head staging --title "Release: %RELEASE_DATE%" --body "Promote staging to production"
