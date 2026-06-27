;achievement menu & messages by InnocentSam

Global AchievementCount% = 0

Delete Each Achievements

Type Achievements
	Field Name$
	Field IsSCP%
	Field LocalName$
	Field Description$
	Field Img%
	Field Unlocked%
End Type

Function CreateAchievement.Achievements(name$, isSCP% = False)
	AchievementCount = AchievementCount + 1

	Local a.Achievements = New Achievements
	a\Name = name
	a\IsSCP = isSCP
	a\LocalName = GetModdedINIString(StringsFile, "Achievement", name)
	a\Description = GetModdedINIString(StringsFile, "Achievement Desc", name)
	a\Img = LoadImage_Strict("GFX\menu\achievements\Achv"+name+".jpg")
	a\Img = ResizeImage2(a\Img,ImageWidth(a\Img)*GraphicHeight/768.0,ImageHeight(a\Img)*GraphicHeight/768.0)
	a\Unlocked = False
	Return a
End Function

Global Achv008.Achievements = CreateAchievement("008", True)
Global Achv012.Achievements = CreateAchievement("012", True)
Global Achv035.Achievements = CreateAchievement("035", True)
Global Achv049.Achievements = CreateAchievement("049", True)
Global Achv055.Achievements = CreateAchievement("055", True)
Global Achv066.Achievements = CreateAchievement("066", True)
Global Achv079.Achievements = CreateAchievement("079", True)
Global Achv096.Achievements = CreateAchievement("096", True)
Global Achv106.Achievements = CreateAchievement("106", True)
Global Achv148.Achievements = CreateAchievement("148", True)
Global Achv205.Achievements = CreateAchievement("205", True)
Global Achv294.Achievements = CreateAchievement("294", True)
Global Achv372.Achievements = CreateAchievement("372", True)
Global Achv420.Achievements = CreateAchievement("420J", True)
Global Achv427.Achievements = CreateAchievement("427", True)
Global Achv500.Achievements = CreateAchievement("500", True)
Global Achv513.Achievements = CreateAchievement("513", True)
Global Achv714.Achievements = CreateAchievement("714", True)
Global Achv789.Achievements = CreateAchievement("789J", True)
Global Achv860.Achievements = CreateAchievement("860", True)
Global Achv895.Achievements = CreateAchievement("895", True)
Global Achv914.Achievements = CreateAchievement("914", True)
Global Achv939.Achievements = CreateAchievement("939", True)
Global Achv966.Achievements = CreateAchievement("966", True)
Global Achv970.Achievements = CreateAchievement("970", True)
Global Achv1025.Achievements = CreateAchievement("1025", True)
Global Achv1048.Achievements = CreateAchievement("1048", True)
Global Achv1123.Achievements = CreateAchievement("1123", True)
Global Achv1162.Achievements = CreateAchievement("1162", True)
Global Achv1499.Achievements = CreateAchievement("1499", True)

Global AchvMaynard.Achievements = CreateAchievement("Maynard")
Global AchvHarp.Achievements = CreateAchievement("Harp")
Global AchvSNAV.Achievements = CreateAchievement("SNAV")
Global AchvOmni.Achievements = CreateAchievement("Omni")
Global AchvTesla.Achievements = CreateAchievement("Tesla")
Global AchvPD.Achievements = CreateAchievement("PD")

Global AchvConsole.Achievements = CreateAchievement("Console")
Global AchvKeter.Achievements = CreateAchievement("Keter")

Global UsedConsole

Global AchievementsMenu%
Global AchvMSGenabled% = GetOptionInt("general", "achievement popup enabled")

Global AchvLocked = LoadImage_Strict("GFX\menu\achievements\achvlocked.jpg")
AchvLocked = ResizeImage2(AchvLocked,ImageWidth(AchvLocked)*GraphicHeight/768.0,ImageHeight(AchvLocked)*GraphicHeight/768.0)

Function GiveAchievement(achv.Achievements, showMessage%=True)
	If Not achv\Unlocked Then
		achv\Unlocked=True
		If AchvMSGenabled And showMessage Then
			CreateAchievementMsg(achv)
		EndIf
		; The "Fair Play" achievement cannot be found on Steam because every achievement there requires the console to not be used.
		If SteamActive And (Not UsedConsole) And achv<>AchvConsole Then
			Steam_Achieve("Achv" + achv\Name)
		End If
	EndIf
End Function

Function AchievementTooltip(achv.Achievements)
    SetFont Font3
    Local width = StringWidth(achv\LocalName)
    SetFont Font1
    If (StringWidth(achv\Description)>width) Then
        width = StringWidth(achv\Description)
    EndIf
    width = width+20*MenuScale
    
    Local height = 49*MenuScale
    
    Color 25,25,25
    Rect(ScaledMouseX()+(20*MenuScale),ScaledMouseY()+(20*MenuScale),width,height,True)
    Color 150,150,150
    Rect(ScaledMouseX()+(20*MenuScale),ScaledMouseY()+(20*MenuScale),width,height,False)
    SetFont Font3
    Text(ScaledMouseX()+(20*MenuScale)+(width/2),ScaledMouseY()+(35*MenuScale), achv\LocalName, True, True)
    SetFont Font1
    Text(ScaledMouseX()+(20*MenuScale)+(width/2),ScaledMouseY()+(55*MenuScale), achv\Description, True, True)
End Function

Function DrawAchvIMG(x%, y%, achv.Achievements)
	Color 0,0,0
	Local scale# = GraphicHeight/768.0
	Rect(x, y, 64*scale, 64*scale, True)
	If achv\Unlocked Then
		DrawImage(achv\Img,x,y)
	Else
		DrawImage(AchvLocked,x,y)
	EndIf
	Color 50,50,50
	
	Rect(x, y, 64*scale, 64*scale, False)
End Function

Global CurrAchvMSGID% = 0

Type AchievementMsg
	Field achv.Achievements
	Field msgx#
	Field msgtime#
	Field msgID%
End Type

Function CreateAchievementMsg.AchievementMsg(achv.Achievements)
	Local amsg.AchievementMsg = New AchievementMsg
	
	amsg\achv = achv
	amsg\msgx = 0.0
	amsg\msgtime = FPSfactor2
	amsg\msgID = CurrAchvMSGID
	CurrAchvMSGID = CurrAchvMSGID + 1
	
	Return amsg
End Function

Function UpdateAchievementMsg()
	Local amsg.AchievementMsg,amsg2.AchievementMsg
	Local scale# = GraphicHeight/768.0
	Local width% = 264*scale
	Local height% = 84*scale
	Local x%,y%
	
	For amsg = Each AchievementMsg
		If amsg\msgtime <> 0
			x=GraphicWidth+amsg\msgx
			y=(GraphicHeight-height)
			For amsg2 = Each AchievementMsg
				If amsg2 <> amsg
					If amsg2\msgID > amsg\msgID
						y=y-height
					EndIf
				EndIf
			Next
			DrawFrame(x,y,width,height)
			Color 0,0,0
			Rect(x+10*scale,y+10*scale,64*scale,64*scale,True)
			DrawImage(amsg\achv\Img,x+10*scale,y+10*scale)
			Color 50,50,50
			Rect(x+10*scale,y+10*scale,64*scale,64*scale,False)
			Color 255,255,255
			SetFont Font1
			RowText(Format(I_Loc\HUD_AchvUnlocked, amsg\achv\LocalName),x+84*scale,y+10*scale,width-94*scale,y-20*scale)
			If amsg\msgtime > 0.0 And amsg\msgtime < 70*7
				amsg\msgtime = amsg\msgtime + FPSfactor2
				If amsg\msgx > -width%
					amsg\msgx = Max(amsg\msgx-4*FPSfactor2,-width%)
				EndIf
			ElseIf amsg\msgtime >= 70*7
				amsg\msgtime = -1
			ElseIf amsg\msgtime = -1
				If amsg\msgx < 0.0
					amsg\msgx = Min(amsg\msgx+4*FPSfactor2,0.0)
				Else
					amsg\msgtime = 0.0
				EndIf
			EndIf
		Else
			Delete amsg
		EndIf
	Next
	
End Function




;~IDEal Editor Parameters:
;~F#31#48
;~C#Blitz3D