@echo off
dotnet run GenerateLocalization.cs 0 ../Data/strings.ini > ../Localization/Debug/Data/strings.ini
echo Generated debug localization strings
dotnet run GenerateLocalization.cs 1 ../Data/strings.ini > ../Localization.bb
echo Generated static localization code
echo Done!
pause
