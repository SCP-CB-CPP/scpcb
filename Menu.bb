Global MenuBack% = LoadImage_Strict("GFX\menu\back.jpg")
Global MenuText% = LoadImage_Strict("GFX\menu\scptext.jpg")
Global Menu173% = LoadImage_Strict("GFX\menu\173back.jpg")
MenuWhite = LoadImage_Strict("GFX\menu\menuwhite.jpg")
MenuBlack = LoadImage_Strict("GFX\menu\menublack.jpg")
MaskImage MenuBlack, 255,255,0

ResizeImage(MenuBack, ImageWidth(MenuBack) * MenuScale, ImageHeight(MenuBack) * MenuScale)
ResizeImage(MenuText, ImageWidth(MenuText) * MenuScale, ImageHeight(MenuText) * MenuScale)
ResizeImage(Menu173, ImageWidth(Menu173) * MenuScale, ImageHeight(Menu173) * MenuScale)

For i = 0 To 3
	ArrowIMG(i) = LoadImage_Strict("GFX\menu\arrow.png")
	ScaleImage(ArrowIMG(i), HUDScale, HUDScale)
	RotateImage(ArrowIMG(i), -90 * i)
	HandleImage(ArrowIMG(i), 0, 0)
Next

Global RandomSeed$, RandomSeedNumeric%, HasNumericSeed%
Function GetRandomSeed%()
	If HasNumericSeed Then Return RandomSeedNumeric Else Return GenerateSeedNumber(RandomSeed)
End Function

Dim MenuBlinkTimer%(2), MenuBlinkDuration%(2)
MenuBlinkTimer%(0) = 1
MenuBlinkTimer%(1) = 1

Global MenuStr$, MenuStrX%, MenuStrY%

Global MainMenuTab%


Global IntroEnabled% = GetOptionInt("general", "intro enabled")

Global SelectedInputBox%

Global SavePath$ = "Saves\"
Global SaveMSG$

;nykyisen tallennuksen nimi ja samalla missä kansiossa tallennustiedosto sijaitsee saves-kansiossa
Global PrevSave$, CurrSave$

Global SaveGameAmount%
Dim SaveGames$(SaveGameAmount+1) 
Dim SaveGameTime$(SaveGameAmount + 1)
Dim SaveGameDate$(SaveGameAmount + 1)
Dim SaveGameVersion$(SaveGameAmount + 1)
Dim SaveGamePlayTime$(SaveGameAmount + 1)

Global SavedMapsAmount% = 0
Dim SavedMaps$(SavedMapsAmount+1)
Dim SavedMapsAuthor$(SavedMapsAmount+1)

Global SelectedMap$

LoadSaveGames()

Global CurrLoadGamePage% = 0

; 0 is idle; 1 is upload confirmation; 2 is update confirmation 
Global ModUIState%
Global ModChangelog$
Global ShouldKeepModDescription% = True
Global ModsDirty% = False
Global SelectedMod.Mods
Global NewModBlink% = LoadImage_Strict("GFX\newmod.png")
ResizeImage(NewModBlink, 24 * MenuScale, 24 * MenuScale)

Function EllipsisLeft$(txt$, maxLen%)
	If Len(txt) > maxLen Then Return Left(txt, maxLen-3) + "…"
	Return txt
End Function

Function UpdateMainMenu()
	Local x%, y%, width%, height%, temp%
	
	Color 0,0,0
	Rect 0,0,GraphicWidth,GraphicHeight,True
	
	ShowPointer()
	
	DrawImage(MenuBack, 0, 0)
	
	If (MilliSecs() Mod MenuBlinkTimer(0)) >= Rand(MenuBlinkDuration(0)) Then
		DrawImage(Menu173, GraphicWidth - ImageWidth(Menu173), GraphicHeight - ImageHeight(Menu173))
	EndIf
	
	If Rand(300) = 1 Then
		MenuBlinkTimer(0) = Rand(4000, 8000)
		MenuBlinkDuration(0) = Rand(200, 500)
	End If
	
	SetFont Font1
	
	MenuBlinkTimer(1)=MenuBlinkTimer(1)-FPSfactor
	If MenuBlinkTimer(1) < MenuBlinkDuration(1) Then
		Color(50, 50, 50)
		Text(MenuStrX + Rand(-5, 5), MenuStrY + Rand(-5, 5), MenuStr, True)
		If MenuBlinkTimer(1) < 0 Then
			MenuBlinkTimer(1) = Rand(700, 800)
			MenuBlinkDuration(1) = Rand(10, 35)
			MenuStrX = Rand(700, 1000) * MenuScale
			MenuStrY = Rand(100, 600) * MenuScale
			
			Select Rand(0, 22)
				Case 0, 2, 3
					MenuStr = I_Loc\Menu_Random[1]
				Case 4, 5
					MenuStr = I_Loc\Menu_Random[2]
				Case 6, 7, 8
					MenuStr = I_Loc\Menu_Random[3]
				Case 9, 10, 11
					MenuStr = I_Loc\Menu_Random[4]
				Case 12, 19
					MenuStr = I_Loc\Menu_Random[5]
				Case 13
					MenuStr = I_Loc\Menu_Random[6]
				Case 14
					MenuStr = I_Loc\Menu_Random[7]
				Case 15
					MenuStr = I_Loc\Menu_Random[8]
				Case 16
					MenuStr = I_Loc\Menu_Random[9]
				Case 17
					MenuStr = I_Loc\Menu_Random[10]
				Case 18
					MenuStr = I_Loc\Menu_Random[11]
				Case 20
					MenuStr = I_Loc\Menu_Random[12]
				Case 21
					MenuStr = I_Loc\Menu_Random[13]
				Case 22
					MenuStr = I_Loc\Menu_Random[14]
			End Select
		EndIf
	EndIf
	
	SetFont Font2
	
	DrawImage(MenuText, GraphicWidth / 2 - ImageWidth(MenuText) / 2, GraphicHeight - 20 * MenuScale - ImageHeight(MenuText))
	
	If GraphicWidth > 1240 * MenuScale Then
		DrawTiledImageRect(MenuWhite, 0, 5, 512, 7 * MenuScale, 985.0 * MenuScale, 407.0 * MenuScale, (GraphicWidth - 1240 * MenuScale) + 300, 7 * MenuScale)
	EndIf
	
	If (Not MouseDown1)
		OnSliderID = 0
	EndIf
	
	If MainMenuTab = 0 Then
		For i% = 0 To 4
			temp = False
			x = 159 * MenuScale
			y = (286 + 80 * i) * MenuScale
			
			width = 400 * MenuScale
			height = 70 * MenuScale
			
			temp = (MouseHit1 And MouseOn(x, y, width, height))
			
			Local txt$
			Select i
				Case 0
					txt = I_Loc\Menu_NewUpper
					If temp Then
						HasNumericSeed = UseNumericSeeds
						If HasNumericSeed Then
							RandomSeedNumeric = MilliSecs()
						Else
							RandomSeed = ""
							If Rand(15)=1 Then 
								Select Rand(13)
									Case 1 
										RandomSeed = "NIL"
									Case 2
										RandomSeed = "NO"
									Case 3
										RandomSeed = "d9341"
									Case 4
										RandomSeed = "5CP_I73"
									Case 5
										RandomSeed = "DONTBLINK"
									Case 6
										RandomSeed = "CRUNCH"
									Case 7
										RandomSeed = "die"
									Case 8
										RandomSeed = "HTAED"
									Case 9
										RandomSeed = "rustledjim"
									Case 10
										RandomSeed = "larry"
									Case 11
										RandomSeed = "JORGE"
									Case 12
										RandomSeed = "dirtymetal"
									Case 13
										RandomSeed = "whatpumpkin"
								End Select
							Else
								n = Rand(4,8)
								For i = 1 To n
									If Rand(3)=1 Then
										RandomSeed = RandomSeed + Rand(0,9)
									Else
										RandomSeed = RandomSeed + Chr(Rand(97,122))
									EndIf
								Next							
							EndIf
						EndIf
						
						MainMenuTab = 1
					EndIf
				Case 1
					txt = I_Loc\Menu_LoadUpper
					If temp Then
						LoadSaveGames()
						MainMenuTab = 2
					EndIf
				Case 2
					txt = I_Loc\Menu_ModsUpper
					If temp Then MainMenuTab = 8
				Case 3
					txt = I_Loc\Menu_OptionsUpper
					If temp Then MainMenuTab = 3 : OnSliderID = 66
				Case 4
					txt = I_Loc\Menu_QuitUpper
					If temp Then
						StopChannel(CurrMusicStream)
						End
					EndIf
			End Select
			
			DrawButton(x, y, width, height, txt)
			
			;rect(x + 4, y + 4, width - 8, height - 8)
			;color 255, 255, 255	
			;text(x + width / 2, y + height / 2, Str, True, True)
		Next	
		
	Else
		
		x = 159 * MenuScale
		y = 286 * MenuScale
		
		width = 400 * MenuScale
		height = 70 * MenuScale
		
		DrawFrame(x, y, width, height)
		
		If DrawButton(x + width + 20 * MenuScale, y, 580 * MenuScale - width - 20 * MenuScale, height, I_Loc\Menu_BackUpper, False, False, UpdatingMod<>Null) Then 
			Select MainMenuTab
				Case 1
					PutINIValue(OptionFile, "general", "intro enabled", IntroEnabled%)
					MainMenuTab = 0
				Case 2
					CurrLoadGamePage = 0
					MainMenuTab = 0
				Case 3,5,6,7 ;save the options
					SaveOptionsINI()
					
					UserTrackCheck% = 0
					UserTrackCheck2% = 0
					
					AntiAlias Opt_AntiAlias
					UpdateHUDOffsets()
					MainMenuTab = 0
				Case 4 ;move back to the "new game" tab
					MainMenuTab = 1
					CurrLoadGamePage = 0
					MouseHit1 = False
				Case 8
					MainMenuTab = 0
					SerializeMods()
					If ModsDirty Then
						ModsDirty = False
						Restart()
					Else
						UpdateActiveMods()
					EndIf
				Default
					MainMenuTab = 0
			End Select
		EndIf
		
		Select MainMenuTab
			Case 1 ; New game
				;[Block]
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, I_Loc\Menu_NewUpper, True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 330 * MenuScale
				
				DrawFrame(x, y, width, height)				
				
				SetFont Font1
				
				Text (x + 20 * MenuScale, y + 20 * MenuScale, I_Loc\NewGame_Name)
				CurrSave = InputBox(x + 150 * MenuScale, y + 15 * MenuScale, 200 * MenuScale, 30 * MenuScale, CurrSave, 1)
				CurrSave = Left(CurrSave, 15)
				CurrSave = Replace(CurrSave,":","")
				CurrSave = Replace(CurrSave,".","")
				CurrSave = Replace(CurrSave,"/","")
				CurrSave = Replace(CurrSave,"\","")
				CurrSave = Replace(CurrSave,"<","")
				CurrSave = Replace(CurrSave,">","")
				CurrSave = Replace(CurrSave,"|","")
				CurrSave = Replace(CurrSave,"?","")
				CurrSave = Replace(CurrSave,Chr(34),"")
				CurrSave = Replace(CurrSave,"*","")
				
				Color 255,255,255
				If SelectedMap = "" Then
					Text (x + 20 * MenuScale, y + 60 * MenuScale, I_Loc\Menu_Seed)
					If HasNumericSeed Then
						Local inputBoxSeed$ = InputBox(x+150*MenuScale, y+55*MenuScale, 200*MenuScale, 30*MenuScale, Str(RandomSeedNumeric), 3)
						If Instr(inputBoxSeed, "-", 2) <> 0 Then
							RandomSeedNumeric = -RandomSeedNumeric
						Else
							RandomSeedNumeric = Int(inputBoxSeed)
						EndIf
					Else
						RandomSeed = Left(InputBox(x+150*MenuScale, y+55*MenuScale, 200*MenuScale, 30*MenuScale, RandomSeed, 3),15)
					EndIf
				Else
					Text (x + 20 * MenuScale, y + 60 * MenuScale, I_Loc\Menu_Map)
					Color (255, 255, 255)
					Rect(x+150*MenuScale, y+55*MenuScale, 200*MenuScale, 30*MenuScale)
					Color (0, 0, 0)
					Rect(x+150*MenuScale+2, y+55*MenuScale+2, 200*MenuScale-4, 30*MenuScale-4)
					
					Color (255, 0,0)
					If Len(SelectedMap)>15 Then
						Text(x+150*MenuScale + 100*MenuScale, y+55*MenuScale + 15*MenuScale, Left(SelectedMap,14)+"...", True, True)
					Else
						Text(x+150*MenuScale + 100*MenuScale, y+55*MenuScale + 15*MenuScale, SelectedMap, True, True)
					EndIf
					
					If DrawButton(x+370*MenuScale, y+55*MenuScale, 120*MenuScale, 30*MenuScale, I_Loc\NewGame_Deselect, False) Then
						SelectedMap=""
					EndIf
				EndIf	
				
				Text(x + 20 * MenuScale, y + 110 * MenuScale, I_Loc\NewGame_Enableintro)
				IntroEnabled = DrawTick(x + 280 * MenuScale, y + 110 * MenuScale, IntroEnabled)	
				
				;Local modeName$, modeDescription$, selectedDescription$
				Text (x + 20 * MenuScale, y + 150 * MenuScale, I_Loc\Menu_Difficulty)				
				For i = SAFE To CUSTOM
					Local dif.Difficulty = Difficulties[i]
					If DrawTick(x + 20 * MenuScale, y + (180+30*i) * MenuScale, (SelectedDifficulty = dif)) Then SelectedDifficulty = dif
					Color(dif\r,dif\g,dif\b)
					Text(x + 50 * MenuScale, y + (185+30*i) * MenuScale, dif\localName)
				Next
				
				Color(255, 255, 255)
				DrawFrame(x + 150 * MenuScale,y + 155 * MenuScale, 410*MenuScale, 150*MenuScale)
				
				If SelectedDifficulty\customizable Then
					SelectedDifficulty\permaDeath =  DrawTick(x + 160 * MenuScale, y + 165 * MenuScale, SelectedDifficulty\permaDeath)
					Text(x + 200 * MenuScale, y + 165 * MenuScale, I_Loc\Difficulty_Permadeath)
					
					If DrawTick(x + 160 * MenuScale, y + 195 * MenuScale, SelectedDifficulty\saveType = SAVEANYWHERE And (Not SelectedDifficulty\permaDeath)) Then 
						SelectedDifficulty\saveType = SAVEANYWHERE
						SelectedDifficulty\permaDeath = False
					Else
						SelectedDifficulty\saveType = SAVEONSCREENS
					EndIf
					
					Text(x + 200 * MenuScale, y + 195 * MenuScale, I_Loc\Difficulty_Saveanywhere)	
					
					SelectedDifficulty\aggressiveNPCs =  DrawTick(x + 160 * MenuScale, y + 225 * MenuScale, SelectedDifficulty\aggressiveNPCs)
					Text(x + 200 * MenuScale, y + 225 * MenuScale, I_Loc\Difficulty_Aggressivenpcs)
					
					;Other factor's difficulty
					Color 255,255,255
					DrawImage ArrowIMG(1),x + 155 * MenuScale, y+251*MenuScale
					If MouseHit1
						If ImageRectOverlap(ArrowIMG(1),x + 155 * MenuScale, y+251*MenuScale, ScaledMouseX(),ScaledMouseY(),0,0)
							If SelectedDifficulty\otherFactors < HARD
								SelectedDifficulty\otherFactors = SelectedDifficulty\otherFactors + 1
							Else
								SelectedDifficulty\otherFactors = EASY
							EndIf
							PlaySound_Strict(ButtonSFX)
						EndIf
					EndIf
					Color 255,255,255
					Select SelectedDifficulty\otherFactors
						Case EASY
							Text(x + 200 * MenuScale, y + 255 * MenuScale, Format(I_Loc\Difficulty_Other, I_Loc\Difficulty_OtherEasy))
						Case NORMAL
							Text(x + 200 * MenuScale, y + 255 * MenuScale, Format(I_Loc\Difficulty_Other, I_Loc\Difficulty_OtherNormal))
						Case HARD
							Text(x + 200 * MenuScale, y + 255 * MenuScale, Format(I_Loc\Difficulty_Other, I_Loc\Difficulty_OtherHard))
					End Select
				Else
					RowText(SelectedDifficulty\description, x+160*MenuScale, y+165*MenuScale, (410-20)*MenuScale, 140*MenuScale)					
				EndIf
				
				If DrawButton(x, y + height + 20 * MenuScale, 160 * MenuScale, 70 * MenuScale, I_Loc\NewGame_Loadmap, False) Then
					MainMenuTab = 4
					LoadSavedMaps()
				EndIf
				
				SetFont Font2
				
				If DrawButton(x + 420 * MenuScale, y + height + 20 * MenuScale, 160 * MenuScale, 70 * MenuScale, I_Loc\NewGame_Start, False) Then
					TimerStopped = True

					If CurrSave = "" Then CurrSave = I_Loc\NewGame_Untitled
					Local SaveName$ = CurrSave
					
					Local SameFound% = 1

					For i% = 1 To SaveGameAmount
						If SaveGames(i - 1) = CurrSave Then
							SameFound = SameFound + 1
							i = 0
							CurrSave = SaveName + " (" + Str(SameFound) + ")"
						EndIf
					Next
					
					If (Not HasNumericSeed) And RandomSeed = "" Then
						RandomSeed = Abs(MilliSecs())
					EndIf

					SeedRnd GetRandomSeed()

					SetErrorMsg(7, GetSeedString())

					LoadEntities()
					LoadAllSounds()
					InitRoomTemplates()
					InitNewGame()
					MainMenuOpen = False
					FlushKeys()
					FlushMouse()
					
					PutINIValue(OptionFile, "general", "intro enabled", IntroEnabled%)
					
				EndIf
				
				;[End Block]
			Case 2 ;load game
				;[Block]
				
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				;height = 300 * MenuScale
				height = 510 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, I_Loc\Menu_LoadUpper, True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				;SetFont Font1	
				
				SetFont Font2
				
				If CurrLoadGamePage < Ceil(Float(SaveGameAmount)/6.0)-1 And SaveMSG = "" Then 
					If DrawButton(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, ">") Then
						CurrLoadGamePage = CurrLoadGamePage+1
					EndIf
				Else
					DrawFrame(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+555*MenuScale, y + 537.5*MenuScale, ">", True, True)
				EndIf
				If CurrLoadGamePage > 0 And SaveMSG = "" Then
					If DrawButton(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, "<") Then
						CurrLoadGamePage = CurrLoadGamePage-1
					EndIf
				Else
					DrawFrame(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+25*MenuScale, y + 537.5*MenuScale, "<", True, True)
				EndIf
				
				DrawFrame(x+50*MenuScale,y+510*MenuScale,width-100*MenuScale,55*MenuScale)
				
				Text(x+(width/2.0),y+536*MenuScale,Format(I_Loc\Menu_Page, Int(Max((CurrLoadGamePage+1),1)), Int(Max((Int(Ceil(Float(SaveGameAmount)/6.0))),1))),True,True)
				
				SetFont Font1
				
				If CurrLoadGamePage > Ceil(Float(SaveGameAmount)/6.0)-1 Then
					CurrLoadGamePage = CurrLoadGamePage - 1
				EndIf
				
				If SaveGameAmount = 0 Then
					Text (x + 20 * MenuScale, y + 20 * MenuScale, I_Loc\LoadGame_Nosaved)
				Else
					x = x + 20 * MenuScale
					y = y + 20 * MenuScale
					
					For i% = (1+(6*CurrLoadGamePage)) To 6+(6*CurrLoadGamePage)
						If i <= SaveGameAmount Then
							DrawFrame(x,y,540* MenuScale, 70* MenuScale)
							
							If SaveGameVersion(i - 1) <> CompatibleNumber Then
								Color 255,0,0
							Else
								Color 255,255,255
							EndIf
							
							Text(x + 20 * MenuScale, y + 10 * MenuScale, SaveGames(i - 1))
							Text(x + 20 * MenuScale, y + (10+18) * MenuScale, SaveGameTime(i - 1)) ;y + (10+23) * MenuScale
							Text(x + 120 * MenuScale, y + (10+18) * MenuScale, SaveGameDate(i - 1))
							Text(x + 20 * MenuScale, y + (10+36) * MenuScale, SaveGameVersion(i - 1) + RSet(FormatDuration(SaveGamePlayTime(i - 1), False), 14))
							
							If SaveMSG = "" Then
								If SaveGameVersion(i - 1) <> CompatibleNumber Then
									DrawFrame(x + 280 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale)
									Color(255, 0, 0)
									Text(x + 330 * MenuScale, y + 34 * MenuScale, I_Loc\LoadGame_Load, True, True)
								Else
									If DrawButton(x + 280 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\LoadGame_Load, False) Then
										LoadEntities()
										LoadAllSounds()
										InitRoomTemplates()
										LoadGame(SaveGames(i - 1))
										CurrSave = SaveGames(i - 1)
										InitLoadGame()
										MainMenuOpen = False
										Return
									EndIf
								EndIf
								
								If DrawButton(x + 400 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\LoadGame_Delete, False) Then
									SaveMSG = SaveGames(i - 1)
									DebugLog SaveMSG
									Exit
								EndIf
							Else
								DrawFrame(x + 280 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale)
								If SaveGameVersion(i - 1) <> CompatibleNumber Then
									Color(255, 0, 0)
								Else
									Color(100, 100, 100)
								EndIf
								Text(x + 330 * MenuScale, y + 34 * MenuScale, I_Loc\LoadGame_Load, True, True)
								
								DrawFrame(x + 400 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale)
								Color(100, 100, 100)
								Text(x + 450 * MenuScale, y + 34 * MenuScale, I_Loc\LoadGame_Delete, True, True)
							EndIf
							
							y = y + 80 * MenuScale
						Else
							Exit
						EndIf
					Next
					
					If SaveMSG <> ""
						x = 740 * MenuScale
						y = 376 * MenuScale
						DrawFrame(x, y, 420 * MenuScale, 200 * MenuScale)
						RowText(I_Loc\LoadGame_DeleteConfirm, x + 20 * MenuScale, y + 15 * MenuScale, 400 * MenuScale, 200 * MenuScale)
						If DrawButton(x + 50 * MenuScale, y + 150 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_Yes, False) Then
							DeleteFile(CurrentDir() + SavePath + SaveMSG + ".cbsav")
							SaveMSG = ""
							LoadSaveGames()
						EndIf
						If DrawButton(x + 250 * MenuScale, y + 150 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_No, False) Then
							SaveMSG = ""
						EndIf
					EndIf
				EndIf
				
				
				
				;[End Block]
			Case 3,5,6,7 ;options
				;[Block]
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, I_Loc\Menu_OptionsUpper, True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 60 * MenuScale
				DrawFrame(x, y, width, height)
				
				Color 0,255,0
				If MainMenuTab = 3
					Rect(x+15*MenuScale,y+10*MenuScale,(width/5)+10*MenuScale,(height/2)+10*MenuScale,True)
				ElseIf MainMenuTab = 5
					Rect(x+155*MenuScale,y+10*MenuScale,(width/5)+10*MenuScale,(height/2)+10*MenuScale,True)
				ElseIf MainMenuTab = 6
					Rect(x+295*MenuScale,y+10*MenuScale,(width/5)+10*MenuScale,(height/2)+10*MenuScale,True)
				ElseIf MainMenuTab = 7
					Rect(x+435*MenuScale,y+10*MenuScale,(width/5)+10*MenuScale,(height/2)+10*MenuScale,True)
				EndIf
				
				Color 255,255,255
				If DrawButton(x+20*MenuScale,y+15*MenuScale,width/5,height/2, I_Loc\Option_Graphics, False) Then MainMenuTab = 3
				If DrawButton(x+160*MenuScale,y+15*MenuScale,width/5,height/2, I_Loc\Option_Audio, False) Then MainMenuTab = 5
				If DrawButton(x+300*MenuScale,y+15*MenuScale,width/5,height/2, I_Loc\Option_Controls, False) Then MainMenuTab = 6
				If DrawButton(x+440*MenuScale,y+15*MenuScale,width/5,height/2, I_Loc\Option_Advanced, False) Then MainMenuTab = 7
				
				SetFont Font1
				y = y + 70 * MenuScale
				
				If MainMenuTab <> 5
					UserTrackCheck% = 0
					UserTrackCheck2% = 0
				EndIf
				
				Local tx# = x+width
				Local ty# = y
				Local tw# = 400*MenuScale
				Local th# = 150*MenuScale
				
				;DrawOptionsTooltip(tx,ty,tw,th,"")
				
				If MainMenuTab = 3 ;Graphics
					;[Block]
					;height = 380 * MenuScale
					height = 330 * MenuScale
					DrawFrame(x, y, width, height)
					
					y=y+20*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Vsync)
					Vsync% = DrawTick(x + 310 * MenuScale, y + MenuScale, Vsync%)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"vsync")
					EndIf
					
					y=y+30*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Antialias)
					Opt_AntiAlias = DrawTick(x + 310 * MenuScale, y + MenuScale, Opt_AntiAlias%)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"antialias")
					EndIf
					
					y=y+30*MenuScale
					
					;Local prevGamma# = ScreenGamma
					ScreenGamma = (SlideBar(x + 310*MenuScale, y+6*MenuScale, 150*MenuScale, ScreenGamma*50.0, 1)/50.0)
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Gamma)
					If (MouseOn(x+310*MenuScale,y+6*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"gamma",ScreenGamma)
					EndIf

					y=y+50*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Texlod)
					TextureDetails = Slider5(x+310*MenuScale,y+6*MenuScale,150*MenuScale,TextureDetails,3,"0.8","0.4","0.0","-0.4","-0.8")
					Select TextureDetails%
						Case 0
							TextureFloat# = 0.8
						Case 1
							TextureFloat# = 0.4
						Case 2
							TextureFloat# = 0.0
						Case 3
							TextureFloat# = -0.4
						Case 4
							TextureFloat# = -0.8
					End Select
					TextureLodBias TextureFloat
					If (MouseOn(x+310*MenuScale,y-6*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Or OnSliderID=3
						DrawOptionsTooltip(tx,ty,tw,th+100*MenuScale,"texquality")
					EndIf
					
					y=y+50*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Vram)
					EnableVRam = DrawTick(x + 310 * MenuScale, y + MenuScale, EnableVRam)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"vram")
					EndIf

					y=y+50*MenuScale

					HUDOffsetScale = SlideBar(x + 310*MenuScale, y+6*MenuScale,150*MenuScale, HUDOffsetScale*100, 5)/100
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Hudoffset)
					If (MouseOn(x+310*MenuScale,y+6*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=5
						DrawOptionsTooltip(tx,ty,tw,th,"hudoffset")
					EndIf

					y=y+50*MenuScale
					
					Local SlideBarFOV# = FOV-40
					SlideBarFOV = (SlideBar(x + 310*MenuScale, y+6*MenuScale,150*MenuScale, SlideBarFOV*2.0, 4)/2.0)
					FOV = Int(SlideBarFOV+40)
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_fov)
					Color 255,255,0
					Text(x + 25 * MenuScale, y + 25 * MenuScale, FOV+"°")
					If (MouseOn(x+310*MenuScale,y+6*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=4
						DrawOptionsTooltip(tx,ty,tw,th,"fov")
					EndIf

					;[End Block]
				ElseIf MainMenuTab = 5 ;Audio
					;[Block]
					height = 220 * MenuScale
					DrawFrame(x, y, width, height)	
					
					y = y + 20*MenuScale
					
					MusicVolume = (SlideBar(x + 310*MenuScale, y-4*MenuScale, 150*MenuScale, MusicVolume*100.0, 1)/100.0)
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Musicvol)
					If (MouseOn(x+310*MenuScale,y-4*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"musicvol",MusicVolume)
					EndIf
					
					y = y + 40*MenuScale
					
					;SFXVolume = (SlideBar(x + 310*MenuScale, y-4*MenuScale, 150*MenuScale, SFXVolume*100.0)/100.0)
					PrevSFXVolume = (SlideBar(x + 310*MenuScale, y-4*MenuScale, 150*MenuScale, SFXVolume*100.0, 2)/100.0)
					SFXVolume = PrevSFXVolume
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Soundvol)
					If (MouseOn(x+310*MenuScale,y-4*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=2
						DrawOptionsTooltip(tx,ty,tw,th,"soundvol",PrevSFXVolume)
					EndIf
					;If MouseDown1 Then
					;	If MouseX() >= x And MouseX() <= x + width + 14 And MouseY() >= y And MouseY() <= y + 20 Then
					;		PlayTestSound(True)
					;	Else
					;		PlayTestSound(False)
					;	EndIf
					;Else
					;	PlayTestSound(False)
					;EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text x + 20 * MenuScale, y, I_Loc\OptionName_Sfxautorelease
					EnableSFXRelease = DrawTick(x + 310 * MenuScale, y + MenuScale, EnableSFXRelease)
					If EnableSFXRelease_Prev% <> EnableSFXRelease
						If EnableSFXRelease%
							For snd.Sound = Each Sound
								For i=0 To 31
									If snd\channels[i]<>0 Then
										If ChannelPlaying(snd\channels[i]) Then
											StopChannel(snd\channels[i])
										EndIf
									EndIf
								Next
								If snd\internalHandle<>0 Then
									FreeSound snd\internalHandle
									snd\internalHandle = 0
								EndIf
								snd\releaseTime = 0
							Next
						Else
							For snd.Sound = Each Sound
								If snd\internalHandle = 0 Then snd\internalHandle = LoadSound_Strict(snd\name)
							Next
						EndIf
						EnableSFXRelease_Prev% = EnableSFXRelease
					EndIf
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th+220*MenuScale,"sfxautorelease")
					EndIf
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text x + 20 * MenuScale, y, I_Loc\OptionName_Usertrack
					EnableUserTracks = DrawTick(x + 310 * MenuScale, y + MenuScale, EnableUserTracks)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"usertrack")
					EndIf
					
					If EnableUserTracks
						y = y + 30 * MenuScale
						Color 255,255,255
						Text x + 20 * MenuScale, y, I_Loc\OptionName_Usertrackmode
						UserTrackMode = DrawTick(x + 310 * MenuScale, y + MenuScale, UserTrackMode)
						If UserTrackMode
							Text x + 350 * MenuScale, y + MenuScale, I_Loc\OptionName_UsertrackmodeRepeat
						Else
							Text x + 350 * MenuScale, y + MenuScale, I_Loc\OptionName_UsertrackmodeRandom
						EndIf
						If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
							DrawOptionsTooltip(tx,ty,tw,th,"usertrackmode")
						EndIf
						If DrawButton(x + 20 * MenuScale, y + 30 * MenuScale, 250 * MenuScale, 25 * MenuScale, I_Loc\OptionName_Usertrackscan,False)
							DebugLog "User Tracks Check Started"
							
							UserTrackCheck% = 0
							UserTrackCheck2% = 0
							
							Dir=ReadDir("SFX\Radio\UserTracks\")
							Repeat
								file$=NextFile(Dir)
								If file$="" Then Exit
								If FileType("SFX\Radio\UserTracks\"+file$) = 1 Then
									UserTrackCheck = UserTrackCheck + 1
									test = LoadSound("SFX\Radio\UserTracks\"+file$)
									If test<>0
										UserTrackCheck2 = UserTrackCheck2 + 1
									EndIf
									FreeSound test
								EndIf
							Forever
							CloseDir Dir
							
							DebugLog "User Tracks Check Ended"
						EndIf
						If MouseOn(x+20*MenuScale,y+30*MenuScale,190*MenuScale,25*MenuScale) And OnSliderID=0
							DrawOptionsTooltip(tx,ty,tw,th,"usertrackscan")
						EndIf
						If UserTrackCheck%>0
							Text x + 20 * MenuScale, y + 100 * MenuScale, Format(I_Loc\OptionName_UsertrackscanFound, UserTrackCheck2, UserTrackCheck)
						EndIf
					Else
						UserTrackCheck%=0
					EndIf
					;[End Block]
				ElseIf MainMenuTab = 6 ;Controls
					;[Block]
					height = 270 * MenuScale
					DrawFrame(x, y, width, height)	
					
					y = y + 20*MenuScale
					
					MouseSens = (SlideBar(x + 310*MenuScale, y-4*MenuScale, 150*MenuScale, (MouseSens+0.5)*100.0, 1)/100.0)-0.5
					Color(255, 255, 255)
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Mousesensitivity)
					If (MouseOn(x+310*MenuScale,y-4*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"mousesensitivity",MouseSens)
					EndIf
					
					y = y + 40*MenuScale
					
					Color(255, 255, 255)
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Mouseinvert)
					InvertMouse = DrawTick(x + 310 * MenuScale, y + MenuScale, InvertMouse)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"mouseinvert")
					EndIf
					
					y = y + 40*MenuScale
					
					MouseSmooth = (SlideBar(x + 310*MenuScale, y-4*MenuScale, 150*MenuScale, (MouseSmooth)*50.0, 2)/50.0)
					Color(255, 255, 255)
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Mousesmoothing)
					If (MouseOn(x+310*MenuScale,y-4*MenuScale,150*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=2
						DrawOptionsTooltip(tx,ty,tw,th,"mousesmoothing",MouseSmooth)
					EndIf
					
					Color(255, 255, 255)
					
					y = y + 30*MenuScale
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Binds)
					y = y + 10*MenuScale
					
					Text(x + 20 * MenuScale, y + 20 * MenuScale, I_Loc\OptionName_BindMoveForward)
					InputBox(x + 160 * MenuScale, y + 20 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_UP,210)),5)		
					Text(x + 20 * MenuScale, y + 40 * MenuScale, I_Loc\OptionName_BindMoveLeft)
					InputBox(x + 160 * MenuScale, y + 40 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_LEFT,210)),3)	
					Text(x + 20 * MenuScale, y + 60 * MenuScale, I_Loc\OptionName_BindMoveBack)
					InputBox(x + 160 * MenuScale, y + 60 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_DOWN,210)),6)				
					Text(x + 20 * MenuScale, y + 80 * MenuScale, I_Loc\OptionName_BindMoveRight)
					InputBox(x + 160 * MenuScale, y + 80 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_RIGHT,210)),4)	
					Text(x + 20 * MenuScale, y + 100 * MenuScale, I_Loc\OptionName_BindSave)
					InputBox(x + 160 * MenuScale, y + 100 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_SAVE,210)),11)

					Text(x + 280 * MenuScale, y + 20 * MenuScale, I_Loc\OptionName_BindBlink)
					InputBox(x + 470 * MenuScale, y + 20 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_BLINK,210)),7)				
					Text(x + 280 * MenuScale, y + 40 * MenuScale, I_Loc\OptionName_BindSprint)
					InputBox(x + 470 * MenuScale, y + 40 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_SPRINT,210)),8)
					Text(x + 280 * MenuScale, y + 60 * MenuScale, I_Loc\OptionName_BindInv)
					InputBox(x + 470 * MenuScale, y + 60 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_INV,210)),9)
					Text(x + 280 * MenuScale, y + 80 * MenuScale, I_Loc\OptionName_BindCrouch)
					InputBox(x + 470 * MenuScale, y + 80 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_CROUCH,210)),10)	
					Text(x + 280 * MenuScale, y + 100 * MenuScale, I_Loc\OptionName_BindConsole)
					InputBox(x + 470 * MenuScale, y + 100 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_CONSOLE,210)),12)
					
					If MouseOn(x+20*MenuScale,y,width-40*MenuScale,120*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"controls")
					EndIf
					
					For i = 0 To 227
						If KeyHit(i) Then key = i : Exit
					Next
					If key<>0 Then
						Select SelectedInputBox
							Case 3
								KEY_LEFT = key
							Case 4
								KEY_RIGHT = key
							Case 5
								KEY_UP = key
							Case 6
								KEY_DOWN = key
							Case 7
								KEY_BLINK = key
							Case 8
								KEY_SPRINT = key
							Case 9
								KEY_INV = key
							Case 10
								KEY_CROUCH = key
							Case 11
								KEY_SAVE = key
							Case 12
								KEY_CONSOLE = key
						End Select
						SelectedInputBox = 0
					EndIf
					;[End Block]
				ElseIf MainMenuTab = 7 ;Advanced
					;[Block]
					height = 370 * MenuScale
					DrawFrame(x, y, width, height)	
					
					y = y + 20*MenuScale
					
					Color 255,255,255				
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Showhud)	
					HUDenabled = DrawTick(x + 310 * MenuScale, y + MenuScale, HUDenabled)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"hud")
					EndIf
					
					y=y+30*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Console)
					CanOpenConsole = DrawTick(x + 310 * MenuScale, y + MenuScale, CanOpenConsole)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"consoleenable")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Consoleerror)
					ConsoleOpening = DrawTick(x + 310 * MenuScale, y + MenuScale, ConsoleOpening)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"consoleerror")
					EndIf

					y = y + 30*MenuScale

					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Speedrunmode)
					SpeedRunMode = DrawTick(x + 310 * MenuScale, y + MenuScale, SpeedRunMode)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"speedrunmode")
					EndIf

					y = y + 30*MenuScale

					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Numericseeds)
					UseNumericSeeds = DrawTick(x + 310 * MenuScale, y + MenuScale, UseNumericSeeds)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"numericseeds")
					EndIf
					
					y = y + 50*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Achpopup)
					AchvMSGenabled% = DrawTick(x + 310 * MenuScale, y + MenuScale, AchvMSGenabled%)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"achpopup")
					EndIf

					y = y + 50*MenuScale

					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Launcher)
					LauncherEnabled% = DrawTick(x + 310 * MenuScale, y + MenuScale, LauncherEnabled%)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"launcher")
					EndIf
					
					y = y + 50*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Showfps)
					ShowFPS% = DrawTick(x + 310 * MenuScale, y + MenuScale, ShowFPS%)
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"showfps")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text(x + 20 * MenuScale, y, I_Loc\OptionName_Framelimit)
					Color 255,255,255
					If DrawTick(x + 310 * MenuScale, y, CurrFrameLimit > 0.0) Then
						;CurrFrameLimit# = (SlideBar(x + 150*MenuScale, y+30*MenuScale, 100*MenuScale, CurrFrameLimit#*50.0, 1)/50.0)
						;CurrFrameLimit = Max(CurrFrameLimit, 0.1)
						;Framelimit% = CurrFrameLimit#*100.0
						CurrFrameLimit# = (SlideBar(x + 150*MenuScale, y+30*MenuScale, 100*MenuScale, CurrFrameLimit#*99.0, 1)/99.0)
						CurrFrameLimit# = Max(CurrFrameLimit, 0.01)
						Framelimit% = 19+(CurrFrameLimit*100.0)
						Color 255,255,0
						Text(x + 25 * MenuScale, y + 25 * MenuScale, Format(I_Loc\OptionName_FramelimitFps, Framelimit%))
						If (MouseOn(x+150*MenuScale,y+30*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
							DrawOptionsTooltip(tx,ty,tw,th,"framelimit",Framelimit)
						EndIf
					Else
						CurrFrameLimit# = 0.0
						Framelimit = 0
					EndIf
					If MouseOn(x+310*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"framelimit",Framelimit)
					EndIf
					;[End Block]
				EndIf
				;[End Block]
			Case 4 ; load map
				;[Block]
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 510 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, I_Loc\NewGame_LoadmapUpper, True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 350 * MenuScale
				
				SetFont Font2
				
				tx# = x+width
				ty# = y
				tw# = 400*MenuScale
				th# = 150*MenuScale
				
				If CurrLoadGamePage < Ceil(Float(SavedMapsAmount)/6.0)-1 Then 
					If DrawButton(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, ">") Then
						CurrLoadGamePage = CurrLoadGamePage+1
					EndIf
				Else
					DrawFrame(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+555*MenuScale, y + 537.5*MenuScale, ">", True, True)
				EndIf
				If CurrLoadGamePage > 0 Then
					If DrawButton(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, "<") Then
						CurrLoadGamePage = CurrLoadGamePage-1
					EndIf
				Else
					DrawFrame(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+25*MenuScale, y + 537.5*MenuScale, "<", True, True)
				EndIf
				
				DrawFrame(x+50*MenuScale,y+510*MenuScale,width-100*MenuScale,55*MenuScale)
				
				Text(x+(width/2.0),y+536*MenuScale,Format(I_Loc\Menu_Page, Int(Max((CurrLoadGamePage+1),1)), Int(Max((Int(Ceil(Float(SavedMapsAmount)/6.0))),1))),True,True)
				
				SetFont Font1
				
				If CurrLoadGamePage > Ceil(Float(SavedMapsAmount)/6.0)-1 Then
					CurrLoadGamePage = CurrLoadGamePage - 1
				EndIf
				
				SetFont Font1
				
				If SavedMaps(0)="" Then 
					Text (x + 20 * MenuScale, y + 20 * MenuScale, I_Loc\LoadMap_Nomaps)
				Else
					x = x + 20 * MenuScale
					y = y + 20 * MenuScale
					For i = (1+(6*CurrLoadGamePage)) To 6+(6*CurrLoadGamePage)
						If i <= SavedMapsAmount Then
							DrawFrame(x,y,540* MenuScale, 70* MenuScale)
							
							Text(x + 20 * MenuScale, y + 10 * MenuScale, SavedMaps(i - 1))
							Text(x + 20 * MenuScale, y + (10+27) * MenuScale, SavedMapsAuthor(i - 1))
							
							If DrawButton(x + 400 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\LoadGame_Load, False) Then
								SelectedMap=SavedMaps(i - 1)
								MainMenuTab = 1
							EndIf
							If MouseOn(x + 400 * MenuScale, y + 20 * MenuScale, 100*MenuScale,30*MenuScale)
								DrawMapCreatorTooltip(tx,ty,tw,th,SavedMaps(i-1))
							EndIf
							
							y = y + 80 * MenuScale
						Else
							Exit
						EndIf
					Next
				EndIf
				;[End Block]
			Case 8 ;Mods

				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 510 * MenuScale
				
				DrawFrame(x, y, width, height)
				
				x = 159 * MenuScale
				y = 286 * MenuScale
				
				width = 400 * MenuScale
				height = 70 * MenuScale
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2, y + height / 2, I_Loc\Menu_ModsUpper, True, True)
				
				x = 160 * MenuScale
				y = y + height + 20 * MenuScale
				width = 580 * MenuScale
				height = 296 * MenuScale
				
				SetFont Font2

				If CurrLoadGamePage < Ceil(Float(ModCount)/6.0)-1 And SaveMSG = "" Then 
					If DrawButton(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, ">") Then
						CurrLoadGamePage = CurrLoadGamePage+1
					EndIf
				Else
					DrawFrame(x+530*MenuScale, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+555*MenuScale, y + 537.5*MenuScale, ">", True, True)
				EndIf
				If CurrLoadGamePage > 0 And SaveMSG = "" Then
					If DrawButton(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale, "<") Then
						CurrLoadGamePage = CurrLoadGamePage-1
					EndIf
				Else
					DrawFrame(x, y + 510*MenuScale, 50*MenuScale, 55*MenuScale)
					Color(100, 100, 100)
					Text(x+25*MenuScale, y + 537.5*MenuScale, "<", True, True)
				EndIf
				
				DrawFrame(x+50*MenuScale,y+510*MenuScale,width-100*MenuScale,55*MenuScale)
				
				Text(x+(width/2.0),y+536*MenuScale,Format(I_Loc\Menu_Page, Int(Max((CurrLoadGamePage+1),1)), Int(Max((Int(Ceil(Float(ModCount)/6.0))),1))),True,True)
				
				SetFont Font1
				
				If CurrLoadGamePage > Ceil(Float(ModCount)/6.0)-1 Then
					CurrLoadGamePage = CurrLoadGamePage - 1
				EndIf

				If ModCount = 0 Then
					Text (x + 20 * MenuScale, y + 20 * MenuScale, I_Loc\Mods_Nomods)
				Else
					x = x + 10 * MenuScale
					y = y + (20 + 5 * 80) * MenuScale
					
					UpdateUpdatingMod()

					Local xStart = x

					Local milis% = MilliSecs()
					i% = ModCount
					If (i Mod 6) <> 0 Then i = i - (i Mod 6) + 6
					Local drawn% = 0
					Local m.Mods = Last Mods
					Repeat
						If i <= (6+(6*CurrLoadGamePage)) Then
							If i <= ModCount Then
								x = xStart

								Local mActive = DrawTick(x, y + 25 * MenuScale, m\IsActive)
								If mActive <> m\IsActive Then
									m\IsActive = mActive
									m\IsNew = False
									If m\RequiresReload Then ModsDirty = True
								EndIf

								x = x + 25 * MenuScale

								DrawFrame(x,y,490* MenuScale, 70 * MenuScale)
								If m\Icon = 0 And m\Iconpath <> "" Then
									m\Icon = LoadImage_Strict(m\IconPath)
									m\DisabledIcon = CreateGrayScaleImage(m\Icon)
									ResizeImage(m\Icon, 64 * MenuScale, 64 * MenuScale)
									ResizeImage(m\DisabledIcon, 64 * MenuScale, 64 * MenuScale)
								EndIf

								If m\Icon <> 0 Then
									Local ico%
									If m\IsActive Then ico = m\Icon Else ico = m\DisabledIcon
									DrawImage(ico, x + 3 * MenuScale, y + 3 * MenuScale)
								EndIf

								If m\IsNew And milis Mod 1200 >= 600 Then DrawImage(NewModBlink, x + 2 * MenuScale, y + 2 * MenuScale)

								If m\IsActive Then
									Color 255, 255, 255
								Else
									Color 150, 150, 150
								EndIf

								Text(x + 85 * MenuScale, y + 10 * MenuScale, EllipsisLeft(m\Name, 24))
								Text(x + 85 * MenuScale, y + (10+18) * MenuScale, EllipsisLeft(m\Description, 24))
								Text(x + 85 * MenuScale, y + (10+18*2) * MenuScale, EllipsisLeft(m\Author, 24))

								If DrawButton(x + 500 * MenuScale, y + 10 * MenuScale, 30 * MenuScale, 20 * MenuScale, "▲", False, False, i = 1) Then
									Insert m Before Before m
									m = After m
									If m\IsActive And m\RequiresReload Then ModsDirty = True
								EndIf
								
								If DrawButton(x + 500 * MenuScale, y + (70 - 30) * MenuScale, 30 * MenuScale, 20 * MenuScale, "▼", False, False, i = ModCount) Then
									Insert m After After m
									m = Before m
									If m\IsActive And m\RequiresReload Then ModsDirty = True
								EndIf

								If UpdatingMod = m Then
									Local strr$ = ""
									Local slice% = (milis Mod 1200) / 200
									Select slice
										Case 0
											strr = "   "
										Case 1
											strr = ".  "
										Case 2
											strr = ".. "
										Case 3
											strr = "..."
										Case 4
											strr = " .."
										Case 5
											strr = "  ."
									End Select
									DrawButton(x + 370 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, strr, False, False, True)
								Else
									Local buttonsInactive% = UpdateModErrorCode <> 0 Or ModUIState <> 0 Or UpdatingMod <> Null Or (Not SteamActive)
									If m\SteamWorkshopId = "" Then
										If DrawButton(x + 370 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Mods_Upload, False, False, buttonsInactive) Then
											ModUIState = 1
											SelectedMod = m
										EndIf
									Else
										If m\IsUserOwner Then
											If DrawButton(x + 370 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Mods_Update, False, False, buttonsInactive) Then
												ModUIState = 2
												SelectedMod = m
											EndIf
										Else
											If DrawButton(x + 370 * MenuScale, y + 20 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Mods_Visit, False, False, buttonsInactive) Then
												VisitModPage(m)
											EndIf
										EndIf
									EndIf
								EndIf

								If Len(m\Name) > 24 And MouseX() > x + 85 * MenuScale And MouseY() > y + 10 * MenuScale And MouseX() < x + 340 * MenuScale And MouseY() < y + (10 + 15) * MenuScale Then
									DrawFramedRowText(m\Name, MouseX(), MouseY(), 400 * MenuScale)
								EndIf

								If Len(m\Description) > 24 And MouseX() > x + 85 * MenuScale And MouseY() > y + (10 + 18) * MenuScale And MouseX() < x + 340 * MenuScale And MouseY() < y + (10 + 15 + 18) * MenuScale Then
									DrawFramedRowText(m\Description, MouseX(), MouseY(), 400 * MenuScale)
								EndIf

								If Len(m\Author) > 24 And MouseX() > x + 85 * MenuScale And MouseY() > y + (10 + 2*18) * MenuScale And MouseX() < x + 340 * MenuScale And MouseY() < y + (10 + 15 + 2*18) * MenuScale Then
									DrawFramedRowText(m\Author, MouseX(), MouseY(), 400 * MenuScale)
								EndIf
							EndIf

							y = y - 80 * MenuScale
							drawn = drawn + 1
						EndIf
						If i <= ModCount Then m = Before m
						i = i - 1
					Until i <= 0 Lor drawn => 6

					x = 740 * MenuScale
					y = 376 * MenuScale
					If UpdateModErrorCode <> 0
						DrawFrame(x, y, 420 * MenuScale, 200 * MenuScale)
						Color(255, 0, 0)
						RowText(Format(I_Loc\Mods_UpdateFailed, GetWorkshopErrorCodeStr(UpdateModErrorCode)), x + 20 * MenuScale, y + 15 * MenuScale, 400 * MenuScale, 200 * MenuScale)
						If DrawButton(x + 150 * MenuScale, y + 150 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_Ok, False) Then
							UpdateModErrorCode = 0
						EndIf
					Else If ModUIState = 1 Then
						DrawFrame(x, y, 420 * MenuScale, 200 * MenuScale)
						RowText(I_Loc\Mods_UploadConfirm, x + 20 * MenuScale, y + 15 * MenuScale, 400 * MenuScale, 200 * MenuScale)
						If DrawButton(x + 25 * MenuScale, y + 150 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_Yes, False) Then
							UploadMod(SelectedMod)
							ModUIState = 0
							SelectedMod = Null
						EndIf
						If DrawButton(x + 150 * MenuScale, y + 150 * MenuScale, 125 * MenuScale, 30 * MenuScale, I_Loc\Mods_Viewterms, False) Then
							ExecFile("https://steamcommunity.com/sharedfiles/workshoplegalagreement")
						EndIf
						If DrawButton(x + 300 * MenuScale, y + 150 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_No, False) Then
							ModUIState = 0
							SelectedMod = Null
						EndIf
					Else If ModUIState = 2 Then
						DrawFrame(x, y, 420 * MenuScale, 200 * MenuScale)
						RowText(I_Loc\Mods_UpdateConfirm, x + 20 * MenuScale, y + 15 * MenuScale, 400 * MenuScale, 200 * MenuScale)
						ModChangelog = InputBox(x + 20 * MenuScale, y + 80 * MenuScale, 380 * MenuScale, 30 * MenuScale, ModChangelog, 99)
						Text(x + 20 * MenuScale, y + 125 * MenuScale, I_Loc\Mods_Keepdesc)
						ShouldKeepModDescription = DrawTick(x + 325 * MenuScale, y + 121 * MenuScale, ShouldKeepModDescription)
						If DrawButton(x + 50 * MenuScale, y + 155 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_Yes, False) Then
							UpdateMod(SelectedMod, ModChangelog)
							ModChangelog = ""
							ModUIState = 0
							SelectedMod = Null
						EndIf
						If DrawButton(x + 250 * MenuScale, y + 155 * MenuScale, 100 * MenuScale, 30 * MenuScale, I_Loc\Menu_No, False) Then
							ModChangelog = ""
							ModUIState = 0
							SelectedMod = Null
						EndIf
					Else
						If DrawButton(x + 10 * MenuScale, y, 150 * MenuScale, 30 * MenuScale, I_Loc\Mods_Reloadmods, False, False, UpdatingMod<>Null) Then
							SerializeMods()
							ReloadMods()
						EndIf

						If DrawButton(x + 10 * MenuScale, y + 40 * MenuScale, 150 * MenuScale, 30 * MenuScale, I_Loc\Mods_Reloadgame, False, False, UpdatingMod<>Null) Then
							SerializeMods()
							Restart()
							Return
						EndIf

						If DrawButton(x + 10 * MenuScale, y + 80 * MenuScale, 150 * MenuScale, 50 * MenuScale, "", False, False, UpdatingMod<>Null) Then
							ExecFile("Mods")
						EndIf
						RowText(I_Loc\Mods_Openlocal, x + 10 * MenuScale, y + (80 + 10) * MenuScale, 150 * MenuScale, (50 - 15) * MenuScale, True)
					EndIf
				EndIf

		End Select
		
	End If
	
	If SpeedRunMode And (Not TimerStopped) Then
		DrawTimer()
		If MainMenuOpen Then
			If DrawButton(GraphicWidth - 150 * MenuScale - 24, 60 * MenuScale + 24, 150 * MenuScale, 30 * MenuScale, "Stop timer", False) Then
				TimerStopped = True
			EndIf
		EndIf
	EndIf

	Color 255,255,255
	SetFont ConsoleFont
	Text 20,GraphicHeight-30,"v"+VersionNumber
	
	;DrawTiledImageRect(MenuBack, 985 * MenuScale, 860 * MenuScale, 200 * MenuScale, 20 * MenuScale, 1200 * MenuScale, 866 * MenuScale, 300, 20 * MenuScale)
	
	If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
	
	SetFont Font1
End Function

Function CreateGrayScaleImage%(img%)
	Local ret% = CreateImage(ImageWidth(img), ImageHeight(img))
	Local rbuf% = ImageBuffer(img)
	Local buf% = ImageBuffer(ret)
	LockBuffer(rbuf)
	LockBuffer(buf)
	For x = 0 To ImageWidth(img)-1
		For y = 0 To ImageHeight(img)-1
			Local color% = ReadPixelFast(x, y, rbuf)
			Local g% = ((color Shr 16) And 255) * 0.21 + ((color Shr 8) And 255) * 0.72 + (color And 255) * 0.07
			WritePixelFast(x, y, (color And $FF000000) + (g Shl 16) + (g Shl 8) + g, buf)
		Next
	Next
	UnlockBuffer(rbuf)
	UnlockBuffer(buf)
	Return ret
End Function

Dim GfxDrivers$(0)
Dim AspectRatioWidths%(0), AspectRatioHeights%(0)
Dim GfxModeCountPerAspectRatio%(0)
Dim GfxModeWidthsByAspectRatio%(0, 0), GfxModeHeightsByAspectRatio%(0, 0)

Function UpdateLauncher()
	AspectRatioRatio = 1.0

	MenuScale = 1
	
	Graphics3DExt(LauncherWidth, LauncherHeight, 0, 2)

	;InitExt
	
	SetBuffer BackBuffer()
	
	RealGraphicWidth = GraphicWidth
	RealGraphicHeight = GraphicHeight

	Local TotalGfxModes% = CountGfxModes3D()

	Local selectedGdc% = GreatestCommonDivsior(GraphicWidth, GraphicHeight)
	Local SelectedAspectRatioWidth% = GraphicWidth / selectedGdc, SelectedAspectRatioHeight% = GraphicHeight / selectedGdc
	
	Local SelectedGfxMode% = -1, AspectRatioCount%
	Local SelectedAspectRatio% = -1
	Local nativeGdc% = GreatestCommonDivsior(DesktopWidth(), DesktopHeight())
	Local NativeAspectRatioWidth = DesktopWidth() / nativeGdc : NativeAspectRatioHeight = DesktopHeight() / nativeGdc
	Local nativeAspectRatio%, nativeGfxMode

	Dim AspectRatioWidths%(TotalGfxModes), AspectRatioHeights%(TotalGfxModes)
	Dim GfxModeCountPerAspectRatio%(TotalGfxModes)
	Dim GfxModeWidthsByAspectRatio%(TotalGfxModes, TotalGfxModes), GfxModeHeightsByAspectRatio%(TotalGfxModes, TotalGfxModes)

	Font1 = LoadFont_Strict("GFX\font\cour\Courier New.ttf", 18)
	SetFont Font1
	MenuWhite = LoadImage_Strict("GFX\menu\menuwhite.jpg")
	MenuBlack = LoadImage_Strict("GFX\menu\menublack.jpg")	
	MaskImage MenuBlack, 255,255,0
	Local LauncherIMG% = LoadImage_Strict("GFX\menu\launcher.png")
	Local i%	
	
	For i = 0 To 3
		ArrowIMG(i) = LoadImage_Strict("GFX\menu\arrow.png")
		RotateImage(ArrowIMG(i), 90 * i)
		HandleImage(ArrowIMG(i), 0, 0)
	Next
	
	For i% = 1 To TotalGfxModes
		Local w% = GfxModeWidth(i), h% = GfxModeHeight(i)
		Local gdc% = GreatestCommonDivsior(w, h)
		Local aw% = w / gdc, ah% = h / gdc
		If (aw < 50 And ah < 50) Lor (aw = NativeAspectRatioWidth And ah = NativeAspectRatioHeight) Lor (aw = SelectedAspectRatioWidth And ah = SelectedAspectRatioHeight) Then
			Local ai% = -1
			For n% = 0 To AspectRatioCount - 1
				If AspectRatioWidths(n) = aw And AspectRatioHeights(n) = ah Then ai = n : Exit
			Next
			If ai = -1 Then
				ai = AspectRatioCount
				AspectRatioWidths(ai) = aw : AspectRatioHeights(ai) = ah
				AspectRatioCount = AspectRatioCount + 1
			EndIf
			Local sameFound% = False
			Local lai% = GfxModeCountPerAspectRatio(ai)
			For n = 0 To lai-1
				If GfxModeWidthsByAspectRatio(ai, n) = w And GfxModeHeightsByAspectRatio(ai, n) = h Then sameFound = True : Exit
			Next
			If Not sameFound
				GfxModeWidthsByAspectRatio(ai, lai) = w : GfxModeHeightsByAspectRatio(ai, lai) = h
				GfxModeCountPerAspectRatio(ai) = GfxModeCountPerAspectRatio(ai) + 1

				If GraphicWidth = w And GraphicHeight = h Then SelectedGfxMode = lai : SelectedAspectRatio = ai
				If DesktopWidth() = w And DesktopHeight() = h Then nativeGfxMode = lai : nativeAspectRatio = ai
			EndIf
		EndIf
	Next

	If SelectedGfxMode = -1 Then SelectedGfxMode = nativeGfxMode : SelectedAspectRatio = nativeAspectRatio

	Local gfxDriverCount = CountGfxDrivers()
	Dim GfxDrivers$(gfxDriverCount + 1)
	For i = 1 To gfxDriverCount
		GfxDrivers(i) = GfxDriverName(i)
	Next
	
	BlinkMeterIMG% = LoadImage_Strict("GFX\blinkmeter.jpg")
	
	AppTitle "SCP - Containment Breach Launcher"

	Local quit% = False

	Local height% = 18
	
	Repeat
		;Cls
		Color 0,0,0
		Rect 0,0,LauncherWidth,LauncherHeight,True
		
		MouseHit1 = MouseHit(1)
		MouseDown1 = MouseDown(1)
		If Not MouseDown1 Then OnSliderID = 0
		
		Color 255, 255, 255
		DrawImage(LauncherIMG, 0, 0)
		
		Local x% = 20
		Local y% = 240 - 65

		Text(x, y, I_Loc\Launcher_Resolution)

		x = x + 130
		y = y - 5

		Color 255, 255, 255
		Rect(x, y, 400, 20)
		For i = 0 To (AspectRatioCount - 1)
			Color 0, 0, 0
			Local txt$ = Str(AspectRatioWidths(i)) + ":" + Str(AspectRatioHeights(i))
			Local txtW% = StringWidth(txt)
			Text(x + 5, y + 5, txt)
			Local temp% = False
			If SelectedAspectRatio = i Then temp = True
			If MouseOn(x + 1, y + 1, txtW + 8, height) Then
				Color 100, 100, 100
				temp = True
				If MouseHit1 Then SelectedAspectRatio = i : SelectedGfxMode = Min(GfxModeCountPerAspectRatio(i) - 1, SelectedGfxMode)
			EndIf
			If temp Then Rect(x + 1, y + 1, txtW + 8, height, False)
			If nativeAspectRatio = i Then
				Color 0, 255, 0
				Rect(x + 0, y + 0, txtW + 10, height + 2, False)
			EndIf
			x = x + txtW + 15
		Next

		x% = 40
		y% = 270 - 65

		For i = 0 To (GfxModeCountPerAspectRatio(SelectedAspectRatio) - 1)
			Color 0, 0, 0

			Local gfxWidth% = GfxModeWidthsByAspectRatio(SelectedAspectRatio, i), gfxHeight% = GfxModeHeightsByAspectRatio(SelectedAspectRatio, i)
			txt$ = gfxWidth + "x" + gfxHeight
			txtW% = StringWidth(txt)

			If SelectedGfxMode = i Then Rect(x - 4, y - 4, txtW + 8, height, False)

			Text(x, y, txt)

			If gfxWidth = DesktopWidth() And gfxHeight = DesktopHeight() Then
				Color 0, 255, 0
				Rect(x - 5, y - 5, txtW + 10, height + 2, False)
			EndIf

			If MouseOn(x - 4, y - 4, txtW + 8, height) Then
				Color 100, 100, 100
				Rect(x - 4, y - 4, txtW + 8, height, False)
				If MouseHit1 Then SelectedGfxMode = i
			EndIf
			
			y=y+20
			If y >= 250 - 65 + (LauncherHeight - 80 - 260) Then y = 270 - 65 : x=x+105
		Next
		
		;-----------------------------------------------------------------
		Color 255, 255, 255
		x = 30
		y = 369
		Rect(x - 10, y, 340, 95)
		Text(x - 10, y - 25, I_Loc\Launcher_Graphics)
		
		y=y+10
		For i = 1 To gfxDriverCount
			Color 0, 0, 0
			txt$ = EllipsisLeft(GfxDrivers(i), 30)
			txtW% = StringWidth(txt)
			If SelectedGFXDriver = i Then Rect(x - 4, y - 4, txtW + 8, height, False)
			Text(x, y, txt)
			If MouseOn(x - 4, y - 4, txtW + 8, height) Then
				Color 100, 100, 100
				Rect(x - 4, y - 4, txtW + 8, height, False)
				If MouseHit1 Then SelectedGFXDriver = i
			EndIf
			
			y=y+20
		Next
		
		Fullscreen = DrawTick(40 + 430 - 15, 260 - 55 + 5 - 8, Fullscreen)
		If Fullscreen Then BorderlessWindowed = False
		BorderlessWindowed = DrawTick(40 + 430 - 15, 260 - 55 + 35, BorderlessWindowed)
		If BorderlessWindowed Then Fullscreen = False

		lock% = False

		If BorderlessWindowed Or (Not Fullscreen) Then lock% = True
		Bit16Mode = DrawTick(40 + 430 - 15, 260 - 55 + 65 + 8, Bit16Mode,lock%)
		LauncherEnabled = DrawTick(40 + 430 - 15, 260 - 55 + 95 + 8, LauncherEnabled)

		Text(40 + 430 + 15, 262 - 55 + 5 - 8, I_Loc\Launcher_FullscreenExclusive)
		Color 255, 255, 255
		RowText(I_Loc\Launcher_Fullscreen, 40 + 430 + 15, 262 - 55 + 35 - 6, 150, 50)

		If BorderlessWindowed Or (Not Fullscreen)
 		   Color 255, 0, 0
 		   Bit16Mode = False
		Else
		    Color 255, 255, 255
		EndIf

		Text(40 + 430 + 15, 262 - 55 + 65 + 8, I_Loc\Launcher_16bit)
		Color 255, 255, 255
		Text(40 + 430 + 15, 262 - 55 + 95 + 8, I_Loc\Launcher_Launcher)
		
		gfxWidth% = GfxModeWidthsByAspectRatio(SelectedAspectRatio, SelectedGfxMode) : gfxHeight% = GfxModeHeightsByAspectRatio(SelectedAspectRatio, SelectedGfxMode)

		If Fullscreen
			Text(260 + 15, 262 - 55 + 140, I_Loc\Launcher_ResolutionCurrent+" "+gfxWidth + "x" + gfxHeight + "," + (16+(16*(Not Bit16Mode))))
		Else
			Text(260 + 15, 262 - 55 + 140, I_Loc\Launcher_ResolutionCurrent+" "+gfxWidth + "x" + gfxHeight + ",32")
		EndIf

		If DrawButton(LauncherWidth - 30 - 90 - 130 - 15, LauncherHeight - 50 - 55, 130, 30, I_Loc\Launcher_Mapcreator, False, False) Then
			ExecFile(Chr(34)+"Map Creator\StartMapCreator.bat"+Chr(34))
			quit = True
			Exit
		EndIf

		If DrawButton(LauncherWidth - 30 - 90 - 130 - 15, LauncherHeight - 50, 130, 30, I_Loc\Launcher_Discord, False, False) Then
			ExecFile("https://discord.gg/guqwRtQPdq")
		EndIf
		
		If DrawButton(LauncherWidth - 30 - 90, LauncherHeight - 50 - 55, 100, 30, I_Loc\Launcher_Launch, False, False) Then
			GraphicWidth = gfxWidth
			GraphicHeight = gfxHeight
			RealGraphicWidth = GraphicWidth
			RealGraphicHeight = GraphicHeight
			Exit
		EndIf
		
		If DrawButton(LauncherWidth - 30 - 90, LauncherHeight - 50, 100, 30, I_Loc\Launcher_Exit, False, False) Then quit = True : Exit
		Flip
	Forever
	
	PutINIValue(OptionFile, "graphics", "width", GfxModeWidthsByAspectRatio(SelectedAspectRatio, SelectedGfxMode))
	PutINIValue(OptionFile, "graphics", "height", GfxModeHeightsByAspectRatio(SelectedAspectRatio, SelectedGfxMode))
	If Fullscreen Then
		PutINIValue(OptionFile, "graphics", "fullscreen", "true")
	Else
		PutINIValue(OptionFile, "graphics", "fullscreen", "false")
	EndIf
	If LauncherEnabled Then
		PutINIValue(OptionFile, "launcher", "launcher enabled", "true")
	Else
		PutINIValue(OptionFile, "launcher", "launcher enabled", "false")
	EndIf
	If BorderlessWindowed Then
		PutINIValue(OptionFile, "graphics", "borderless windowed", "true")
	Else
		PutINIValue(OptionFile, "graphics", "borderless windowed", "false")
	EndIf
	If Bit16Mode Then
		PutINIValue(OptionFile, "graphics", "16bit", "true")
	Else
		PutINIValue(OptionFile, "graphics", "16bit", "false")
	EndIf
	PutINIValue(OptionFile, "graphics", "gfx driver", SelectedGFXDriver)
	
	FreeImage(LauncherIMG) : LauncherIMG = 0
	
	If quit Then End

	Dim AspectRatioWidths%(0), AspectRatioHeights%(0)
	Dim GfxModeCountPerAspectRatio%(0)
	Dim GfxModeWidthsByAspectRatio%(0, 0), GfxModeHeightsByAspectRatio%(0, 0)
End Function

Function GreatestCommonDivsior(u%, v%)
	If u <= 0 Lor v <= 0 Then Return 1

	Local k% = 0, t% = u Or v, d

	While (t And 1) = 0
		k = k + 1
		t = t Shr 1
	Wend

	v = v Shr k
	u = u Shr k

	If (u And 1) = 0 Then d = (u Shr 1) Else If (v And 1) = 0 Then d = -(v Shr 1) Else d = (u Shr 1) - (v Shr 1)
	While d <> 0
		While (d And 1) = 0 d = d / 2 Wend
		If d > 0 Then u = d Else v = -d
		d = (u Shr 1) - (v Shr 1)
	Wend
	
	Return u Shl k
End Function


Function DrawBar(img%, x%, y%, width%, filled#, centerX% = False)
	Local spacing = ImageWidth(img) + 2
	width = Int(width / spacing) * spacing + 3
	Local height = ImageHeight(img) + 6
	If centerX Then x = x - width / 2
	Color 255, 255, 255
	Rect (x, y, width, height, False)
	For i = 1 To Int(((width - 6) * filled) / spacing)
		DrawImage(img, x + 3 + spacing * (i - 1), y + 3)
	Next
End Function

Function DrawTiledImageRect(img%, srcX%, srcY%, srcwidth#, srcheight#, x%, y%, width%, height%)
	
	Local x2% = x
	While x2 < x+width
		If x2 + srcwidth > x + width Then srcwidth = (x + width) - x2
		Local y2% = y
		While y2 < y+height
			DrawImageRect(img, x2, y2, srcX, srcY, srcwidth, Min((y + height) - y2, srcheight))
			y2 = y2 + srcheight
		Wend
		x2 = x2 + srcwidth
	Wend
	
End Function



Type LoadingScreens
	Field imgpath$
	Field img%
	Field ID%
	Field title$
	Field alignx%, aligny%
	Field disablebackground%
	Field txt$[5], txtamount%
	; Inclusive prefix sum
	Field txtDelay%[5], totalTxtDelay%
End Type

Const LOADING_SCREENS_DATA_PATH$ = "Loadingscreens\loadingscreens.ini"
Global LoadingScreenTimePerCharacter%, LoadingScreenStartTime%

Function InitLoadingScreens()
	Delete Each LoadingScreens
	LoadingScreenTimePerCharacter% = GetOptionInt("general", "loading screen cycle per char ms")
	Local hasOverride%
	For m.ActiveMods = Each ActiveMods
		Local modPath$ = m\Path + LOADING_SCREENS_DATA_PATH
		If FileType(modPath) = 1 Then
			LoadLoadingScreens(modPath)
			If FileType(modPath + ".OVERRIDE") = 1 Then
				hasOverride = True
				Exit
			EndIf
		EndIf
	Next
	If Not hasOverride Then LoadLoadingScreens(LOADING_SCREENS_DATA_PATH)
End Function

Function LoadLoadingScreens(file$)
	Local TemporaryString$, i%
	Local ls.LoadingScreens
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			ls.LoadingScreens = New LoadingScreens
			LoadingScreenAmount=LoadingScreenAmount+1
			ls\ID = LoadingScreenAmount
			
			ls\title = TemporaryString
			ls\imgpath = GetINIString(file, TemporaryString, "image path")
			
			ls\totalTxtDelay = 0
			For i = 0 To 4
				ls\txt[i] = GetINIString(file, TemporaryString, "text"+(i+1))
				ls\totalTxtDelay = ls\totalTxtDelay + Len(ls\txt[i]) * LoadingScreenTimePerCharacter
				ls\txtDelay[i] = ls\totalTxtDelay
				If ls\txt[i]<> "" Then ls\txtamount=ls\txtamount+1
			Next
			
			ls\disablebackground = GetINIInt(file, TemporaryString, "disablebackground")
			
			Select Lower(GetINIString(file, TemporaryString, "align x"))
				Case "left"
					ls\alignx = -1
				Case "middle", "center"
					ls\alignx = 0
				Case "right" 
					ls\alignx = 1
			End Select 
			
			Select Lower(GetINIString(file, TemporaryString, "align y"))
				Case "top", "up"
					ls\aligny = -1
				Case "middle", "center"
					ls\aligny = 0
				Case "bottom", "down"
					ls\aligny = 1
			End Select 			
			
		EndIf
	Wend
	
	CloseFile f
End Function



Function DrawLoading(percent%, shortloading=False)
	
	Local x%, y%
	
	If percent = 0 Then
		LoadingScreenStartTime = MilliSecs()
		
		temp = Rand(1,LoadingScreenAmount)
		For ls.loadingscreens = Each LoadingScreens
			If ls\id = temp Then
				If ls\img=0 Then
					ls\img = LoadImage_Strict("Loadingscreens\"+ls\imgpath)
					ScaleImage(ls\img, MenuScale, MenuScale)
					MaskImage(ls\img, 0, 0, 0)
				EndIf
				SelectedLoadingScreen = ls 
				Exit
			EndIf
		Next
	EndIf	
	
	firstloop = True
	Repeat 
		
		;Color 0,0,0
		;Rect 0,0,GraphicWidth,GraphicHeight,True
		;Color 255, 255, 255
		ClsColor 0,0,0
		Cls
		
		;Cls(True,False)
		
		If percent > 20 Then
			UpdateMusic()
		EndIf

		Local LoadingScreenText = 0
		If SelectedLoadingScreen\totalTxtDelay <> 0 Then
			Local elapsedTime = (MilliSecs() - LoadingScreenStartTime) Mod SelectedLoadingScreen\totalTxtDelay
			While elapsedTime >= SelectedLoadingScreen\txtDelay[LoadingScreenText]
				LoadingScreenText = LoadingScreenText + 1
			Wend
		EndIf
		
		If (Not SelectedLoadingScreen\disablebackground) Then
			DrawImage LoadingBack, GraphicWidth/2 - ImageWidth(LoadingBack)/2, GraphicHeight/2 - ImageHeight(LoadingBack)/2
		EndIf	
		
		If SelectedLoadingScreen\alignx = 0 Then
			x = GraphicWidth/2 - ImageWidth(SelectedLoadingScreen\img)/2 
		ElseIf  SelectedLoadingScreen\alignx = 1
			x = GraphicWidth - ImageWidth(SelectedLoadingScreen\img)
		Else
			x = 0
		EndIf
		
		If SelectedLoadingScreen\aligny = 0 Then
			y = GraphicHeight/2 - ImageHeight(SelectedLoadingScreen\img)/2 
		ElseIf  SelectedLoadingScreen\aligny = 1
			y = GraphicHeight - ImageHeight(SelectedLoadingScreen\img)
		Else
			y = 0
		EndIf	
		
		DrawImage SelectedLoadingScreen\img, x, y
		
		DrawBar(BlinkMeterIMG, GraphicWidth / 2, GraphicHeight / 2 - 70 * MenuScale, 300 * HUDScale, percent / 100.0, True)
		
		If SelectedLoadingScreen\title = "CWM" Then
			
			If Not shortloading Then 
				If firstloop Then 
					If percent = 0 Then
						PlaySound_Strict LoadTempSound("SFX\SCP\990\cwm1.cwm")
					ElseIf percent = 100
						PlaySound_Strict LoadTempSound("SFX\SCP\990\cwm2.cwm")
					EndIf
				EndIf
			EndIf
			
			SetFont Font2
			strtemp$ = ""
			temp = Rand(2,9)
			For i = 0 To temp
				strtemp$ = STRTEMP + RandomDefaultWidthChar(48,122,"?")
			Next
			Text(GraphicWidth / 2, GraphicHeight / 2 + 80*MenuScale, strtemp, True, True)
			
			If percent = 0 Then 
				If Rand(5)=1 Then
					Select Rand(2)
						Case 1
							SelectedLoadingScreen\txt[0] = Format(I_Loc\Menu_LoadingCwm[1], CurrentDate())
						Case 2
							SelectedLoadingScreen\txt[0] = CurrentTime()
					End Select
				Else
					Select Rand(13)
						Case 1
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[2]
						Case 2
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[3]
						Case 3
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[4]
						Case 4
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[5]
						Case 5
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[6]
						Case 6 
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[7]
						Case 7
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[8]
						Case 8, 9
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[9]
						Case 10
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[10]
						Case 11
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[11]
						Case 12
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[12]
						Case 13
							SelectedLoadingScreen\txt[0] = I_Loc\Menu_LoadingCwm[13]
					End Select
				EndIf
			EndIf
			
			strtemp$ = SelectedLoadingScreen\txt[0]
			temp = Int(Len(SelectedLoadingScreen\txt[0])-Rand(5))
			For i = 0 To Rand(10,15);temp
				strtemp$ = Replace(SelectedLoadingScreen\txt[0],Mid(SelectedLoadingScreen\txt[0],Rand(1,Len(strtemp)-1),1),RandomDefaultWidthChar(130,250,"?"))
			Next		
			SetFont Font1
			RowText(strtemp, GraphicWidth / 2-200*MenuScale, GraphicHeight / 2 +120*MenuScale,400*MenuScale,300*MenuScale,True)		
		Else
			
			Color 0,0,0
			SetFont Font2
			Text(GraphicWidth / 2 + Max(1, MenuScale), GraphicHeight / 2 + 80*MenuScale+Max(1, MenuScale), SelectedLoadingScreen\title, True, True)
			SetFont Font1
			RowText(SelectedLoadingScreen\txt[LoadingScreenText], GraphicWidth / 2-200*MenuScale+Max(1, MenuScale), GraphicHeight / 2 +120*MenuScale+Max(1, MenuScale),400*MenuScale,300*MenuScale,True)
			
			Color 255,255,255
			SetFont Font2
			Text(GraphicWidth / 2, GraphicHeight / 2 +80*MenuScale, SelectedLoadingScreen\title, True, True)
			SetFont Font1
			RowText(SelectedLoadingScreen\txt[LoadingScreenText], GraphicWidth / 2-200*MenuScale, GraphicHeight / 2 +120*MenuScale,400*MenuScale,300*MenuScale,True)
			
		EndIf

		If SpeedRunMode And (Not TimerStopped) And PlayTime > 0 Then
			DrawTimer()
		EndIf
		
		Color 0,0,0
		Text(GraphicWidth / 2 + Max(1, enuScale), GraphicHeight / 2 - 100 * MenuScale + Max(1, MenuScale), Format(I_Loc\Menu_Loading, percent), True, True)
		Color 255,255,255
		Text(GraphicWidth / 2, GraphicHeight / 2 - 100 * MenuScale, Format(I_Loc\Menu_Loading, percent), True, True)
		
		If percent = 100 Then 
			If firstloop And SelectedLoadingScreen\title <> "CWM" Then PlaySound_Strict LoadTempSound(("SFX\Horror\Horror8.ogg"))
			Text(GraphicWidth / 2, GraphicHeight - 50 * MenuScale, I_Loc\Menu_Pressany, True, True)
		Else
			FlushKeys()
			FlushMouse()
		EndIf
		
		If BorderlessWindowed Then
			If (RealGraphicWidth<>GraphicWidth) Or (RealGraphicHeight<>GraphicHeight) Then
				SetBuffer TextureBuffer(fresize_texture)
				ClsColor 0,0,0 : Cls
				CopyRect 0,0,GraphicWidth,GraphicHeight,1024-GraphicWidth/2,1024-GraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
				SetBuffer BackBuffer()
				ClsColor 0,0,0 : Cls
				ScaleRender(0,0,2050.0 / Float(GraphicWidth) * AspectRatioRatio, 2050.0 / Float(GraphicWidth) * AspectRatioRatio)
				;might want to replace Float(GraphicWidth) with Max(GraphicWidth,GraphicHeight) if portrait sizes cause issues
				;everyone uses landscape so it's probably a non-issue
			EndIf
		EndIf
		
		;not by any means a perfect solution
		;Not even proper gamma correction but it's a nice looking alternative that works in windowed mode
		If ScreenGamma>1.0 Then
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			ClsColor 0,0,0 : Cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,3
			EntityAlpha fresize_image,ScreenGamma-1.0
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
		ElseIf ScreenGamma<1.0 Then ;todo: maybe optimize this if it's too slow, alternatively give players the option to disable gamma
			CopyRect 0,0,RealGraphicWidth,RealGraphicHeight,1024-RealGraphicWidth/2,1024-RealGraphicHeight/2,BackBuffer(),TextureBuffer(fresize_texture)
			EntityBlend fresize_image,1
			ClsColor 0,0,0 : Cls
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			EntityFX fresize_image,1+32
			EntityBlend fresize_image,2
			EntityAlpha fresize_image,1.0
			SetBuffer TextureBuffer(fresize_texture2)
			ClsColor 255*ScreenGamma,255*ScreenGamma,255*ScreenGamma
			Cls
			SetBuffer BackBuffer()
			ScaleRender(-1.0/Float(RealGraphicWidth),1.0/Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth),2048.0 / Float(RealGraphicWidth))
			SetBuffer(TextureBuffer(fresize_texture2))
			ClsColor 0,0,0
			Cls
			SetBuffer(BackBuffer())
		EndIf
		EntityFX fresize_image,1
		EntityBlend fresize_image,1
		EntityAlpha fresize_image,1.0
		
		Flip False
		
		firstloop = False
		If percent <> 100 Then Exit
		
	Until (GetKey()<>0 Or MouseHit(1))
	Cls
End Function

Function RandomDefaultWidthChar$(min%, max%, def$)
	Local c$ = Chr(Rand(min%, max%))
	If StringWidth(c) <> StringWidth("L") Then Return def Else Return c
End Function

Function InputBox$(x%, y%, width%, height%, Txt$, ID% = 0)
	;TextBox(x,y,width,height,Txt$)
	Color (255, 255, 255)
	DrawTiledImageRect(MenuWhite, (x Mod 256), (y Mod 256), 512, 512, x, y, width, height)
	;Rect(x, y, width, height)
	Color (0, 0, 0)
	
	Local MouseOnBox% = False
	If MouseOn(x, y, width, height) Then
		Color(50, 50, 50)
		MouseOnBox = True
		If MouseHit1 Then SelectedInputBox = ID : FlushKeys
	EndIf
	
	Rect(x + 2, y + 2, width - 4, height - 4)
	Color (255, 255, 255)	
	
	If (Not MouseOnBox) And MouseHit1 And SelectedInputBox = ID Then SelectedInputBox = 0
	
	Text(x + width / 2, y + height / 2, Txt, True, True)

	If SelectedInputBox = ID Then
		If (MilliSecs() Mod 800) < 400 Then Rect (x + width / 2 + StringWidth(Txt) / 2 + 2, y + height / 2 - 17 * MenuScale / 2, 2, 17 * MenuScale)
		If KeyDown(29) And KeyHit(47) Then
			Txt = Txt + GetClipboardContents()
		Else If KeyDown(29) And KeyHit(46) Then
			SetClipboardContents(Txt)
		Else
			Txt = TextInput(Txt)
		EndIf
	EndIf

	Return Txt
End Function

Function DrawFrame(x%, y%, width%, height%, xoffset%=0, yoffset%=0, scrollY%=True)
	Local srcY%
	If scrollY Then srcY = y Mod 256
	Color 255, 255, 255
	DrawTiledImageRect(MenuWhite, xoffset, srcY, 512, 512, x, y, width, height)
	
	DrawTiledImageRect(MenuBlack, yoffset, srcY, 512, 512, x+3*MenuScale, y+3*MenuScale, width-6*MenuScale, height-6*MenuScale)	
End Function

Function DrawButton%(x%, y%, width%, height%, txt$, bigfont% = True, waitForMouseUp%=False, disabled%=False)
	Local clicked% = False
	
	DrawFrame (x, y, width, height)
	If (Not disabled) And MouseOn(x, y, width, height) Then
		Color(30, 30, 30)
		If (MouseHit1 And (Not waitForMouseUp)) Or (MouseUp1 And waitForMouseUp) Then 
			clicked = True
			PlaySound_Strict(ButtonSFX)
		EndIf
		Rect(x + 4, y + 4, width - 8, height - 8)
	EndIf
	
	If disabled Then
		Color(100, 100, 100)
	Else
		Color (255, 255, 255)
	EndIf
	If bigfont Then SetFont Font2 Else SetFont Font1
	Text(x + width / 2, y + height / 2, txt, True, True)
	
	Return clicked
End Function

Function DrawButton2%(x%, y%, width%, height%, txt$, bigfont% = True)
	Local clicked% = False
	
	DrawFrame (x, y, width, height)
	Local hit% = MouseHit(1)
	If MouseOn(x, y, width, height) Then
		Color(30, 30, 30)
		If hit Then clicked = True : PlaySound_Strict(ButtonSFX)
		Rect(x + 4, y + 4, width - 8, height - 8)	
	Else
		Color(0, 0, 0)
	EndIf
	
	Color (255, 255, 255)
	If bigfont Then SetFont Font2 Else SetFont Font1
	Text(x + width / 2, y + height / 2, txt, True, True)
	
	Return clicked
End Function

Function DrawTick%(x%, y%, selected%, locked% = False)
	Local width% = 20 * MenuScale, height% = 20 * MenuScale
	
	Color (255, 255, 255)
	DrawTiledImageRect(MenuWhite, (x Mod 256), (y Mod 256), 512, 512, x, y, width, height)
	;Rect(x, y, width, height)
	
	Local Highlight% = MouseOn(x, y, width, height) And (Not locked)
	
	If Highlight Then
		Color(50, 50, 50)
		If MouseHit1 Then selected = (Not selected) : PlaySound_Strict (ButtonSFX)
	Else
		Color(0, 0, 0)		
	End If
	
	Rect(x + 2, y + 2, width - 4, height - 4)
	
	If selected Then
		If Highlight Then
			Color 255,255,255
		Else
			Color 200,200,200
		EndIf
		DrawTiledImageRect(MenuWhite, (x Mod 256), (y Mod 256), 512, 512, x + 4, y + 4, width - 8, height - 8)
		;Rect(x + 4, y + 4, width - 8, height - 8)
	EndIf
	
	Color 255, 255, 255
	
	Return selected
End Function

Function SlideBar#(x%, y%, width%, value#, ID%)
	
	If MouseDown1 And OnSliderID=0 Then
		If ScaledMouseX() >= x And ScaledMouseX() <= x + width + 14 And ScaledMouseY() >= y And ScaledMouseY() <= y + 20 Then
			OnSliderID = ID
		EndIf
	EndIf

	If ID = OnSliderID Then
		value = Min(Max((ScaledMouseX() - x) * 100 / width, 0), 100)
	EndIf

	Local height% = ImageHeight(BlinkMeterIMG) + 6

	Color 255,255,255
	Rect(x, y, width + 14, height,False)

	DrawImage(BlinkMeterIMG, x + width * value / 100.0 +3, y+3)
	
	Color 170,170,170 
	Text (x - 20 * MenuScale - StringWidth(I_Loc\Option_SliderLow), y + 3*MenuScale, I_Loc\Option_SliderLow)					
	Text (x + width + 20 * MenuScale + 14, y+3*MenuScale, I_Loc\Option_SliderHigh)	
	
	Return value
	
End Function




Function RowText(A$, X, Y, W, H, align% = 0, Leading#=1)
	;Display A$ starting at X,Y - no wider than W And no taller than H (all in pixels).
	;Leading is optional extra vertical spacing in pixels
	
	If H<1 Then H=2048
	
	Local LinesShown = 0
	Local Height = StringHeight(A$) + Leading
	Local b$
	
	While Len(A) > 0
		Local space = Instr(A$, " ")
		If space = 0 Then space = Len(A$) + 1
		Local temp$ = Left(A$, space - 1)
		
		If StringWidth (b$ + temp$) > W Then ;too big, so Print what will fit
			If align Then
				Text(X + W / 2, LinesShown * Height + Y, b, True)
			Else
				Text(X, LinesShown * Height + Y, b)
			EndIf			
			
			LinesShown = LinesShown + 1
			b$=""
		Else ;append it To b$ (which will eventually be printed) And remove it from A$
			If b <> "" Then b = b + " "
			b$ = b$ + temp$
			A$ = Right(A$, Max(0, Len(A$) - Len(temp$) - 1))
		EndIf
		
		If ((LinesShown + 1) * Height) > H Then Exit ;the Next Line would be too tall, so leave
	Wend
	
	If (b$ <> "") And((LinesShown + 1) <= H) Then
		If align Then
			Text(X + W / 2, LinesShown * Height + Y, b, True) ;Print any remaining Text If it'll fit vertically
		Else
			Text(X, LinesShown * Height + Y, b) ;Print any remaining Text If it'll fit vertically
		EndIf
	EndIf
	
End Function

Function GetLineAmount(A$, W, H, Leading#=1)
	;Display A$ starting at X,Y - no wider than W And no taller than H (all in pixels).
	;Leading is optional extra vertical spacing in pixels
	
	If H<1 Then H=2048
	
	Local LinesShown = 0
	Local Height = StringHeight(A$) + Leading
	Local b$
	
	While Len(A) > 0
		Local space = Instr(A$, " ")
		If space = 0 Then space = Len(A$)
		Local temp$ = Left(A$, space)
		Local trimmed$ = Trim(temp) ;we might ignore a final space 
		Local extra = 0 ;we haven't ignored it yet
		;ignore final space If doing so would make a word fit at End of Line:
		If (StringWidth (b$ + temp$) > W) And (StringWidth (b$ + trimmed$) <= W) Then
			temp = trimmed
			extra = 1
		EndIf
		
		If StringWidth (b$ + temp$) > W Then ;too big, so Print what will fit
			
			LinesShown = LinesShown + 1
			b$=""
		Else ;append it To b$ (which will eventually be printed) And remove it from A$
			b$ = b$ + temp$
			A$ = Right(A$, Len(A$) - (Len(temp$) + extra))
		EndIf
		
		If ((LinesShown + 1) * Height) > H Then Exit ;the Next Line would be too tall, so leave
	Wend
	
	Return LinesShown+1
	
End Function

Function DrawTooltip(message$)
	Local scale# = GraphicHeight/768.0
	
	Local width = (StringWidth(message$))+20*MenuScale
	
	Color 25,25,25
	Rect(ScaledMouseX()+20,ScaledMouseY(),width,19*scale,True)
	Color 150,150,150
	Rect(ScaledMouseX()+20,ScaledMouseY(),width,19*scale,False)
	SetFont Font1
	Text(ScaledMouseX()+(20*MenuScale)+(width/2),ScaledMouseY()+(12*MenuScale), message$, True, True)
End Function

Global QuickLoadPercent% = -1
Global QuickLoad_CurrEvent.Events

Function DrawQuickLoading()
	
	If QuickLoadPercent > -1
		If QuickLoadPercent > 99
			QuickLoadPercent = -1
		EndIf
		QuickLoadEvents()
	Else
		QuickLoadPercent = -1
		QuickLoad_CurrEvent = Null
	EndIf
	
End Function

Function DrawOptionsTooltip(x%,y%,width%,height%,option$,value#=0,ingame%=False)
	Local fx# = x+6*MenuScale
	Local fy# = y+6*MenuScale
	Local fw# = width-12*MenuScale
	Local fh# = height-12*MenuScale
	Local lines% = 0, lines2% = 0
	Local txt$ = ""
	Local txt2$ = "", R% = 0, G% = 0, B% = 0
	Local extraspace% = 0
	
	SetFont Font1
	Color 255,255,255
	Select Lower(option$)
		;Graphic options
			;[Block]
		Case "vsync"
			txt = I_Loc\OptionTooltip_Vsync
		Case "antialias"
			txt = I_Loc\OptionTooltip_Antialias
			txt2 = I_Loc\Option_HintFullscreen
			R = 255
		Case "gamma"
			txt = I_Loc\OptionTooltip_Gamma
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int(value*100)), "100")
		Case "texquality"
			txt = I_Loc\OptionTooltip_Texlod
		Case "vram"
			txt = I_Loc\OptionTooltip_Vram
			txt2 = I_Loc\Option_HintMenuonly
			R = 255
		Case "hudoffset"
			txt = I_Loc\OptionTooltip_Hudoffset
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int(HUDOffsetScale*100)), "0")
		Case "fov"
			txt = I_Loc\OptionTooltip_Fov
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "°", Str(FOV), Str(DEFAULT_FOV))
			;[End Block]
		;Sound options
			;[Block]
		Case "musicvol"
			txt = I_Loc\OptionTooltip_Musicvol
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int(value*100)), "50")
		Case "soundvol"
			txt = I_Loc\OptionTooltip_Soundvol
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int(value*100)), "100")
		Case "sfxautorelease"
			txt = I_Loc\OptionTooltip_Sfxautorelease
			R = 255
			txt2 = I_Loc\Option_HintMenuonly
		Case "usertrack"
			txt = I_Loc\OptionTooltip_Usertrack
			R = 255
			txt2 = I_Loc\Option_HintMenuonly
		Case "usertrackmode"
			txt = I_Loc\OptionTooltip_Usertrackmode
			R = 255
			G = 255
			txt2 = I_Loc\OptionTooltip_UsertrackmodeNote
		Case "usertrackscan"
			txt = I_Loc\OptionTooltip_Usertrackscan
			;[End Block]
		;Control options	
			;[Block]
		Case "mousesensitivity"
			txt = I_Loc\OptionTooltip_Mousesensitivty
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int((0.5+value)*100)), "50")
		Case "mouseinvert"
			txt = Format(I_Loc\Option_HintSelfexplanatory, I_Loc\OptionName_Mouseinvert)
		Case "mousesmoothing"
			txt = I_Loc\OptionTooltip_Mousesmoothing
			R = 255
			G = 255
			B = 255
			txt2 = Format(I_Loc\Option_HintDefault, "%", Str(Int(value*100)), "100")
		Case "controls"
			txt = I_Loc\OptionTooltip_Binds
			;[End Block]
		;Advanced options	
			;[Block]
		Case "hud"
			txt = I_Loc\OptionTooltip_Showhud
		Case "consoleenable"
			txt = I_Loc\OptionTooltip_Console
			R = 255
			txt2 = I_Loc\OptionTooltip_ConsoleNote
		Case "consoleerror"
			txt = Format(I_Loc\Option_HintSelfexplanatory, I_Loc\OptionName_Consoleerror)
		Case "speedrunmode"
			txt = I_Loc\OptionTooltip_Speedrunmode
		Case "numericseeds"
			txt = I_Loc\OptionTooltip_Numericseeds
		Case "achpopup"
			txt = I_Loc\OptionTooltip_Achpopup
		Case "launcher"
			txt = I_Loc\OptionTooltip_Launcher
		Case "showfps"
			txt = I_Loc\OptionTooltip_Showfps
		Case "framelimit"
			txt = I_Loc\OptionTooltip_Framelimit
			If value > 0 And value < 60
				R = 255
				G = 255
				txt2 = I_Loc\OptionTooltip_FramelimitNote
			EndIf
			;[End Block]
	End Select
	
	lines% = GetLineAmount(txt,fw,fh)
	If txt2$ = ""
		DrawFrame(x,y,width,((StringHeight(txt)*lines)+(10+lines)*MenuScale)+extraspace)
	Else
		lines2% = GetLineAmount(txt2,fw,fh)
		DrawFrame(x,y,width,(((StringHeight(txt)*lines)+(10+lines)*MenuScale)+(StringHeight(txt2)*lines2)+(10+lines2)*MenuScale)+extraspace)
	EndIf
	RowText(txt,fx,fy,fw,fh)
	If txt2$ <> ""
		Color R,G,B
		RowText(txt2,fx,(fy+(StringHeight(txt)*lines)+(5+lines)*MenuScale),fw,fh)
	EndIf
End Function

Function DrawFramedRowText(txt$, x%, y%, width%)
	Local fw% = width - 12*MenuScale
	Local lines% = GetLineAmount(txt, fw, 0)
	DrawFrame(x, y, width, ((StringHeight(txt)*lines)+(10+lines)*MenuScale), 0, 0, False)
	RowText(txt, x + 6*MenuScale, y + 6*MenuScale, fw, 0)
End Function

Function DrawMapCreatorTooltip(x%,y%,width%,height%,mapname$)
	Local fx# = x+6*MenuScale
	Local fy# = y+6*MenuScale
	Local fw# = width-12*MenuScale
	Local fh# = height-12*MenuScale
	Local lines% = 0
	
	SetFont Font1
	Color 255,255,255
	
	Local txt$[6]
	If Right(mapname,6)="cbmap2" Then
		txt[0] = Left(mapname$,Len(mapname$)-7)
		Local f% = OpenFile("Map Creator\Maps\"+mapname$)
		
		Local author$ = ReadLine(f)
		Local descr$ = ReadLine(f)
		ReadByte(f)
		ReadByte(f)
		Local ramount% = ReadInt(f)
		If ReadInt(f) > 0 Then
			Local hasForest% = True
		Else
			hasForest% = False
		EndIf
		If ReadInt(f) > 0 Then
			Local hasMT% = True
		Else
			hasMT% = False
		EndIf
		
		CloseFile f%
	Else
		txt[0] = Left(mapname$,Len(mapname$)-6)
		author$ = I_Loc\LoadMap_Unknown
		descr$ = I_Loc\LoadMap_Nodesc
		ramount% = 0
		hasForest% = False
		hasMT% = False
	EndIf
	txt[1] = Format(I_Loc\LoadMap_Madeby, author)
	txt[2] = Format(I_Loc\LoadMap_Desc, descr)
	If ramount > 0 Then
		txt[3] = Format(I_Loc\LoadMap_Roomcount, ramount)
	Else
		txt[3] = Format(I_Loc\LoadMap_Madeby, I_Loc\LoadMap_Unknown)
	EndIf
	If hasForest Then
		txt[4] = Format(I_Loc\LoadMap_Customforest, I_Loc\Menu_Yes)
	Else
		txt[4] = Format(I_Loc\LoadMap_Customforest, I_Loc\Menu_No)
	EndIf
	If hasMT Then
		txt[5] = Format(I_Loc\LoadMap_Custommaintenance, I_Loc\Menu_Yes)
	Else
		txt[5] = Format(I_Loc\LoadMap_Custommaintenance, I_Loc\Menu_No)
	EndIf
	
	lines% = GetLineAmount(txt[2],fw,fh)
	DrawFrame(x,y,width,(StringHeight(txt[0])*6)+StringHeight(txt[2])*lines+5*MenuScale)
	
	Color 255,255,255
	Text(fx,fy,txt[0])
	Text(fx,fy+StringHeight(txt[0]),txt[1])
	RowText(txt[2],fx,fy+(StringHeight(txt[0])*2),fw,fh)
	Text(fx,fy+((StringHeight(txt[0])*2)+StringHeight(txt[2])*lines+5*MenuScale),txt[3])
	Text(fx,fy+((StringHeight(txt[0])*3)+StringHeight(txt[2])*lines+5*MenuScale),txt[4])
	Text(fx,fy+((StringHeight(txt[0])*4)+StringHeight(txt[2])*lines+5*MenuScale),txt[5])
	
End Function

Global OnSliderID% = 0

Function Slider3(x%,y%,width%,value%,ID%,val1$,val2$,val3$)
	
	If MouseDown1 And OnSliderID = 0 Then
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			OnSliderID = ID
		EndIf
	EndIf
	
	Color 200,200,200
	Rect(x,y,width+14,10,True)
	Rect(x,y-8,4,14,True)
	Rect(x+(width/2)+5,y-8,4,14,True)
	Rect(x+width+10,y-8,4,14,True)
	
	If ID = OnSliderID
		If (ScaledMouseX() <= x+8)
			value = 0
		ElseIf (ScaledMouseX() >= x+width/2) And (ScaledMouseX() <= x+(width/2)+8)
			value = 1
		ElseIf (ScaledMouseX() >= x+width)
			value = 2
		EndIf
		Color 0,255,0
		Rect(x,y,width+14,10,True)
	Else
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			Color 0,200,0
			Rect(x,y,width+14,10,False)
		EndIf
	EndIf
	
	If value = 0
		DrawImage(BlinkMeterIMG,x,y-8)
	ElseIf value = 1
		DrawImage(BlinkMeterIMG,x+(width/2)+3,y-8)
	Else
		DrawImage(BlinkMeterIMG,x+width+6,y-8)
	EndIf
	
	Color 170,170,170
	If value = 0
		Text(x+2,y+10+MenuScale,val1,True)
	ElseIf value = 1
		Text(x+(width/2)+7,y+10+MenuScale,val2,True)
	Else
		Text(x+width+12,y+10+MenuScale,val3,True)
	EndIf
	
	Return value
	
End Function

Function Slider4(x%,y%,width%,value%,ID%,val1$,val2$,val3$,val4$)
	
	If MouseDown1 And OnSliderID = 0 Then
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			OnSliderID = ID
		EndIf
	EndIf
	
	Color 200,200,200
	Rect(x,y,width+14,10,True)
	Rect(x,y-8,4,14,True) ;1
	Rect(x+(width*(1.0/3.0))+(10.0/3.0),y-8,4,14,True) ;2
	Rect(x+(width*(2.0/3.0))+(20.0/3.0),y-8,4,14,True) ;3
	Rect(x+width+10,y-8,4,14,True) ;4
	
	If ID = OnSliderID
		If (ScaledMouseX() <= x+8)
			value = 0
		ElseIf (ScaledMouseX() >= x+width*(1.0/3.0)) And (ScaledMouseX() <= x+width*(1.0/3.0)+8)
			value = 1
		ElseIf (ScaledMouseX() >= x+width*(2.0/3.0)) And (ScaledMouseX() <= x+width*(2.0/3.0)+8)
			value = 2
		ElseIf (ScaledMouseX() >= x+width)
			value = 3
		EndIf
		Color 0,255,0
		Rect(x,y,width+14,10,True)
	Else
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			Color 0,200,0
			Rect(x,y,width+14,10,False)
		EndIf
	EndIf
	
	If value = 0
		DrawImage(BlinkMeterIMG,x,y-8)
	ElseIf value = 1
		DrawImage(BlinkMeterIMG,x+width*(1.0/3.0)+2,y-8)
	ElseIf value = 2
		DrawImage(BlinkMeterIMG,x+width*(2.0/3.0)+4,y-8)
	Else
		DrawImage(BlinkMeterIMG,x+width+6,y-8)
	EndIf
	
	Color 170,170,170
	If value = 0
		Text(x+2,y+10+MenuScale,val1,True)
	ElseIf value = 1
		Text(x+width*(1.0/3.0)+2+(10.0/3.0),y+10+MenuScale,val2,True)
	ElseIf value = 2
		Text(x+width*(2.0/3.0)+2+((10.0/3.0)*2),y+10+MenuScale,val3,True)
	Else
		Text(x+width+12,y+10+MenuScale,val4,True)
	EndIf
	
	Return value
	
End Function

Function Slider5(x%,y%,width%,value%,ID%,val1$,val2$,val3$,val4$,val5$)
	
	If MouseDown1 And OnSliderID = 0 Then
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			OnSliderID = ID
		EndIf
	EndIf
	
	Color 200,200,200
	Rect(x,y,width+14,10,True)
	Rect(x,y-8,4,14,True) ;1
	Rect(x+(width/4)+2.5,y-8,4,14,True) ;2
	Rect(x+(width/2)+5,y-8,4,14,True) ;3
	Rect(x+(width*0.75)+7.5,y-8,4,14,True) ;4
	Rect(x+width+10,y-8,4,14,True) ;5
	
	If ID = OnSliderID
		If (ScaledMouseX() <= x+8)
			value = 0
		ElseIf (ScaledMouseX() >= x+width/4) And (ScaledMouseX() <= x+(width/4)+8)
			value = 1
		ElseIf (ScaledMouseX() >= x+width/2) And (ScaledMouseX() <= x+(width/2)+8)
			value = 2
		ElseIf (ScaledMouseX() >= x+width*0.75) And (ScaledMouseX() <= x+(width*0.75)+8)
			value = 3
		ElseIf (ScaledMouseX() >= x+width)
			value = 4
		EndIf
		Color 0,255,0
		Rect(x,y,width+14,10,True)
	Else
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			Color 0,200,0
			Rect(x,y,width+14,10,False)
		EndIf
	EndIf
	
	If value = 0
		DrawImage(BlinkMeterIMG,x,y-8)
	ElseIf value = 1
		DrawImage(BlinkMeterIMG,x+(width/4)+1.5,y-8)
	ElseIf value = 2
		DrawImage(BlinkMeterIMG,x+(width/2)+3,y-8)
	ElseIf value = 3
		DrawImage(BlinkMeterIMG,x+(width*0.75)+4.5,y-8)
	Else
		DrawImage(BlinkMeterIMG,x+width+6,y-8)
	EndIf
	
	Color 170,170,170
	If value = 0
		Text(x+2,y+10+MenuScale,val1,True)
	ElseIf value = 1
		Text(x+(width/4)+4.5,y+10+MenuScale,val2,True)
	ElseIf value = 2
		Text(x+(width/2)+7,y+10+MenuScale,val3,True)
	ElseIf value = 3
		Text(x+(width*0.75)+9.5,y+10+MenuScale,val4,True)
	Else
		Text(x+width+12,y+10+MenuScale,val5,True)
	EndIf
	
	Return value
	
End Function

Function Slider7(x%,y%,width%,value%,ID%,val1$,val2$,val3$,val4$,val5$,val6$,val7$)
	
	If MouseDown1 And OnSliderID = 0 Then
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			OnSliderID = ID
		EndIf
	EndIf
	
	Color 200,200,200
	Rect(x,y,width+14,10,True)
	Rect(x,y-8,4,14,True) ;1
	Rect(x+(width*(1.0/6.0))+(10.0/6.0),y-8,4,14,True) ;2
	Rect(x+(width*(2.0/6.0))+(20.0/6.0),y-8,4,14,True) ;3
	Rect(x+(width*(3.0/6.0))+(30.0/6.0),y-8,4,14,True) ;4
	Rect(x+(width*(4.0/6.0))+(40.0/6.0),y-8,4,14,True) ;5
	Rect(x+(width*(5.0/6.0))+(50.0/6.0),y-8,4,14,True) ;6
	Rect(x+width+10,y-8,4,14,True) ;7
	
	If ID = OnSliderID
		If (ScaledMouseX() <= x+8)
			value = 0
		ElseIf (ScaledMouseX() >= x+(width*(1.0/6.0))) And (ScaledMouseX() <= x+(width*(1.0/6.0))+8)
			value = 1
		ElseIf (ScaledMouseX() >= x+(width*(2.0/6.0))) And (ScaledMouseX() <= x+(width*(2.0/6.0))+8)
			value = 2
		ElseIf (ScaledMouseX() >= x+(width*(3.0/6.0))) And (ScaledMouseX() <= x+(width*(3.0/6.0))+8)
			value = 3
		ElseIf (ScaledMouseX() >= x+(width*(4.0/6.0))) And (ScaledMouseX() <= x+(width*(4.0/6.0))+8)
			value = 4
		ElseIf (ScaledMouseX() >= x+(width*(5.0/6.0))) And (ScaledMouseX() <= x+(width*(5.0/6.0))+8)
			value = 5
		ElseIf (ScaledMouseX() >= x+width)
			value = 6
		EndIf
		Color 0,255,0
		Rect(x,y,width+14,10,True)
	Else
		If (ScaledMouseX() >= x) And (ScaledMouseX() <= x+width+14) And (ScaledMouseY() >= y-8) And (ScaledMouseY() <= y+10)
			Color 0,200,0
			Rect(x,y,width+14,10,False)
		EndIf
	EndIf
	
	If value = 0
		DrawImage(BlinkMeterIMG,x,y-8)
	ElseIf value = 1
		DrawImage(BlinkMeterIMG,x+(width*(1.0/6.0))+1,y-8)
	ElseIf value = 2
		DrawImage(BlinkMeterIMG,x+(width*(2.0/6.0))+2,y-8)
	ElseIf value = 3
		DrawImage(BlinkMeterIMG,x+(width*(3.0/6.0))+3,y-8)
	ElseIf value = 4
		DrawImage(BlinkMeterIMG,x+(width*(4.0/6.0))+4,y-8)
	ElseIf value = 5
		DrawImage(BlinkMeterIMG,x+(width*(5.0/6.0))+5,y-8)
	Else
		DrawImage(BlinkMeterIMG,x+width+6,y-8)
	EndIf
	
	Color 170,170,170
	If value = 0
		Text(x+2,y+10+MenuScale,val1,True)
	ElseIf value = 1
		Text(x+(width*(1.0/6.0))+2+(10.0/6.0),y+10+MenuScale,val2,True)
	ElseIf value = 2
		Text(x+(width*(2.0/6.0))+2+((10.0/6.0)*2),y+10+MenuScale,val3,True)
	ElseIf value = 3
		Text(x+(width*(3.0/6.0))+2+((10.0/6.0)*3),y+10+MenuScale,val4,True)
	ElseIf value = 4
		Text(x+(width*(4.0/6.0))+2+((10.0/6.0)*4),y+10+MenuScale,val5,True)
	ElseIf value = 5
		Text(x+(width*(5.0/6.0))+2+((10.0/6.0)*5),y+10+MenuScale,val6,True)
	Else
		Text(x+width+12,y+10+MenuScale,val7,True)
	EndIf
	
	Return value
	
End Function

Global OnBar%
Global ScrollBarY# = 0.0
Global ScrollMenuHeight# = 0.0

Function DrawScrollBar#(x, y, width, height, barx, bary, barwidth, barheight, bar#, dir = 0)
	;0 = vaakasuuntainen, 1 = pystysuuntainen
	
	Local MouseSpeedX = MouseXSpeed()
	Local MouseSpeedY = MouseYSpeed()
	
	Color(0, 0, 0)
	;Rect(x, y, width, height)
	Button(barx, bary, barwidth, barheight, "")
	
	If dir = 0 Then ;vaakasuunnassa
		If height > 10 Then
			Color 250,250,250
			Rect(barx + barwidth / 2, bary + 5*MenuScale, 2*MenuScale, barheight - 10)
			Rect(barx + barwidth / 2 - 3*MenuScale, bary + 5*MenuScale, 2*MenuScale, barheight - 10)
			Rect(barx + barwidth / 2 + 3*MenuScale, bary + 5*MenuScale, 2*MenuScale, barheight - 10)
		EndIf
	Else ;pystysuunnassa
		If width > 10 Then
			Color 250,250,250
			Rect(barx + 4*MenuScale, bary + barheight / 2, barwidth - 10*MenuScale, 2*MenuScale)
			Rect(barx + 4*MenuScale, bary + barheight / 2 - 3*MenuScale, barwidth - 10*MenuScale, 2*MenuScale)
			Rect(barx + 4*MenuScale, bary + barheight / 2 + 3*MenuScale, barwidth - 10*MenuScale, 2*MenuScale)
		EndIf
	EndIf
	
	If MouseX()>barx And MouseX()<barx+barwidth
		If MouseY()>bary And MouseY()<bary+barheight
			OnBar = True
		Else
			If (Not MouseDown1)
				OnBar = False
			EndIf
		EndIf
	Else
		If (Not MouseDown1)
			OnBar = False
		EndIf
	EndIf
	
	If MouseDown1
		If OnBar
			If dir = 0
				Return Min(Max(bar + MouseSpeedX / Float(width - barwidth), 0), 1)
			Else
				Return Min(Max(bar + MouseSpeedY / Float(height - barheight), 0), 1)
			EndIf
		EndIf
	EndIf
	
	Return bar
	
End Function

Function Button%(x,y,width,height,txt$, disabled%=False)
	Local Pushed = False
	
	Color 50, 50, 50
	If Not disabled Then 
		If MouseX() > x And MouseX() < x+width Then
			If MouseY() > y And MouseY() < y+height Then
				If MouseDown1 Then
					Pushed = True
					Color 50*0.6, 50*0.6, 50*0.6
				Else
					Color Min(50*1.2,255),Min(50*1.2,255),Min(50*1.2,255)
				EndIf
			EndIf
		EndIf
	EndIf
	
	If Pushed Then 
		Rect x,y,width,height
		Color 133,130,125
		Rect x+1*MenuScale,y+1*MenuScale,width-1*MenuScale,height-1*MenuScale,False	
		Color 10,10,10
		Rect x,y,width,height,False
		Color 250,250,250
		Line x,y+height-1*MenuScale,x+width-1*MenuScale,y+height-1*MenuScale
		Line x+width-1*MenuScale,y,x+width-1*MenuScale,y+height-1*MenuScale
	Else
		Rect x,y,width,height
		Color 133,130,125
		Rect x,y,width-1*MenuScale,height-1*MenuScale,False	
		Color 250,250,250
		Rect x,y,width,height,False
		Color 10,10,10
		Line x,y+height-1,x+width-1,y+height-1
		Line x+width-1,y,x+width-1,y+height-1		
	EndIf
	
	Color 255,255,255
	If disabled Then Color 70,70,70
	Text x+width/2, y+height/2-1*MenuScale, txt, True, True
	
	Color 0,0,0
	
	If Pushed And MouseHit1 Then PlaySound_Strict ButtonSFX : Return True
End Function






;~IDEal Editor Parameters:
;~F#33#499#4AB#4B5#4E8#5C3#5D6#5F3#5FA#615#629#64A#662#693#6C4#6EA#710#72D#73E#756
;~F#764#787#79F#7A8#7D9#7ED#821#867#8A9
;~C#Blitz3D