@echo off
dotnet run GenerateAchievementsLocalizationCsv.cs 0 ../Localization/Steam/ > out/achv.csv
echo Generated achievement localization csv
echo Done!
pause
