@echo off
echo Required for this script: .NET 10 SDK (https://dotnet.microsoft.com/en-us/download)
echo:
dotnet run GenerateLocalization.cs 0 ../Data/strings.ini > ../Localization/Debug/Data/strings.ini
echo Generated debug localization strings
dotnet run GenerateLocalization.cs 1 ../Data/strings.ini > ../Localization.bb
echo Generated static localization code
echo Done!
pause
