@echo off
mkdir out
for /D %%i in (..\Localization\*) do tar.exe --strip-components=1 -caf out/%%~ni.zip %%i
pause
