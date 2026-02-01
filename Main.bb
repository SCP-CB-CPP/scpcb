Const VersionNumber$ = "1.3.12-pre9"
Const CompatibleNumber$ = "1.3.12" ;Only change this if the version given isn't working with the current build version - ENDSHN

InitErrorMsgs(10, True)
SetErrorMsg(0, "An error occured in SCP - Containment Breach v" + VersionNumber)
SetErrorMsg(1, "Please send us the generated minidump along with a screenshot of this window!")
SetErrorMsg(2, "---------------------------------------------------")
SetErrorMsg(3, "OS: " + SystemProperty("os") + " " + (32 + (GetEnv("ProgramFiles(X86)") <> 0) * 32) + " Bit (Build: " + SystemProperty("osbuild") + ")")
SetErrorMsg(4, "CPU: " + Trim(SystemProperty("cpuname")) + " (Arch: " + SystemProperty("cpuarch") + ", " + GetEnv("NUMBER_OF_PROCESSORS") + " Threads)")

SetErrorMsg(8, "Caught exception: " + "_CaughtError_")

Function CatchErrors(location$)
	SetErrorMsg(9, location)
End Function

Function RuntimeErrorExt%(Message$)
	SetErrorMsg(8, "Caught exception: " + Message)
	MemoryAccessViolation()
End Function

Include "StrictLoads.bb"
Include "KeyName.bb"

Global DataDir$ = InitDataDir()
Global OptionFile$ = InitOptionsFile()
Global ModsFile$ = DataDir + "\mods.ini"
Const OptionDefaultFile$ = "defaults.ini"

Function InitDataDir$()
	Local dir$ = GetEnv("AppData") + "\Undertow Games"
	If FileType(dir) <> 2 Then CreateDir(dir)
	dir = dir + "\SCP - Containment Breach"
	If FileType(dir) <> 2 Then CreateDir(dir)
	Return dir
End Function

Function InitOptionsFile$()
	Local file$ = DataDir + "\options.ini"
	If FileType(file) <> 1 Then
		Local f% = WriteFile(file)
		CloseFile(f)
	EndIf
	Return file
End Function

Function GetOptionString$(section$, key$)
	Local opt$ = GetINIString(OptionFile, section, key)
	If opt = "" Then Return GetINIString(OptionDefaultFile, section, key)
	Return opt
End Function

Function GetOptionInt%(section$, key$)
	Return ParseINIInt(GetOptionString(section, key))
End Function

Function GetOptionFloat#(section$, key$)
	Return Float(GetOptionString(section, key))
End Function

Include "Blitz_File_FileName.bb"

Include "DevilParticleSystem.bb"

Global SteamActive% = GetOptionInt("general", "enable steam")
If SteamActive Then
	If Steam_RestartAppIfNecessary(2178380) Then Return
	If Steam_Init() <> 0 Then RuntimeErrorExt("Steam failed to initialize")
EndIf

Global DiscordLastStatus%, DiscordCooldown%
Global DiscordActive% = GetOptionInt("general", "enable discord rich presence")
If DiscordActive Then
	DiscordActive = (BlitzcordCreateCore("1465275739342377014") = 0)
	If DiscordActive Then BlitzcordSetLargeImage("logo")
EndIf

Global IsRestart% = False
.Start
Global IsRunning% = True
Global ShouldRestart% = False

Include "ModManager.bb"
ReloadMods()

Global Font1%, Font2%, Font3%, Font4%, Font5%
Global ConsoleFont%

Global MenuWhite%, MenuBlack%
Global ButtonSFX% = LoadSound_Strict("SFX\Interact\Button.ogg")

Global EnableSFXRelease% = GetOptionInt("audio", "sfx release")
Global EnableSFXRelease_Prev% = EnableSFXRelease%

Global CanOpenConsole% = GetOptionInt("console", "enabled")

Global DebugResourcePacks% = GetOptionInt("debug", "resource pack strict load")

Global UseNumericSeeds% = GetOptionInt("general", "numeric seeds")

Dim ArrowIMG(4)

;[Block]

Global LauncherWidth%= Min(GetOptionInt("launcher", "launcher width"), 1024)
Global LauncherHeight% = Min(GetOptionInt("launcher", "launcher height"), 768)
Global LauncherEnabled% = GetOptionInt("launcher", "launcher enabled")

Global GraphicWidth% = GetOptionInt("graphics", "width"), GraphicHeight% = GetOptionInt("graphics", "height")
If GraphicWidth <= 0 Then GraphicWidth = DesktopWidth()
If GraphicHeight <= 0 Then GraphicHeight = DesktopHeight()

Global Depth% = 0, Fullscreen% = GetOptionInt("graphics", "fullscreen")

Global SelectedGFXDriver% = Min(Max(GetOptionInt("graphics", "gfx driver"), 1), CountGfxDrivers())

Global fresize_image%, fresize_texture%, fresize_texture2%
Global fresize_cam%

Global ShowFPS = GetOptionInt("graphics", "show FPS")

Global WireframeState
Global HalloweenTex

Global BorderlessWindowed% = GetOptionInt("graphics", "borderless windowed")
Global RealGraphicWidth%,RealGraphicHeight%
Global AspectRatioRatio#

Global EnableRoomLights% = GetOptionInt("graphics", "room lights enabled")

Global TextureDetails% = GetOptionInt("graphics", "texture details")
Global TextureFloat#
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
Global ConsoleOpening% = GetOptionInt("console", "auto opening")
Global SFXVolume# = GetOptionFloat("audio", "sound volume")

Global Bit16Mode = GetOptionInt("graphics", "16bit")

Global HUDScaleFactor# = GetOptionFloat("graphics", "hud scale factor")

Const StringsFile$ = "Data\strings.ini"
Include "Localization.bb"

Global I_Loc.LocalizationTable
If I_Loc <> Null Then Delete I_Loc ; Happens on reload
I_Loc = New LocalizationTable
For m.ActiveMods = Each ActiveMods
	Local modPath$ = m\Path + StringsFile
	If FileType(modPath) = 1 Then LoadLocalization(I_Loc, modPath)
Next
LoadLocalization(I_Loc, StringsFile)

; Exclusive fullscreen ONLY supports the reported resolutions
If LauncherEnabled And (Not IsRestart) Lor Fullscreen And (Not GfxMode3DExists(GraphicWidth, GraphicHeight, 32-16*Bit16Mode)) Then
	UpdateLauncher()
EndIf

SetGfxDriver(SelectedGFXDriver)
Global GFXDriverName$ = GFXDriverName(SelectedGFXDriver)

;New "fake fullscreen" - ENDSHN Psst, it's called borderless windowed mode --Love Mark,
If BorderlessWindowed
	DebugLog "Using Faked Fullscreen"
	Graphics3DExt DesktopWidth(), DesktopHeight(), 0, 4
	
	RealGraphicWidth = DesktopWidth()
	RealGraphicHeight = DesktopHeight()
	
	AspectRatioRatio = (Float(GraphicWidth)/Float(GraphicHeight))/(Float(RealGraphicWidth)/Float(RealGraphicHeight))
	
	Fullscreen = False
Else
	AspectRatioRatio = 1.0
	RealGraphicWidth = GraphicWidth
	RealGraphicHeight = GraphicHeight
	If Fullscreen Then
		Graphics3DExt(GraphicWidth, GraphicHeight, (16*Bit16Mode), 1)
	Else
		Graphics3DExt(GraphicWidth, GraphicHeight, 0, 2)
	End If
EndIf

Global MenuScale# = (Min(GraphicWidth, GraphicHeight) / 1024.0)
Global HUDScale# = Max(MenuScale * HUDScaleFactor, 1)

SetBuffer(BackBuffer())

Global CurTime%, PrevTime%, LoopDelay%, FPSfactor#, FPSfactor2#, PrevFPSFactor#
Local CheckFPS%, ElapsedLoops%, FPS%

Global Framelimit% = GetOptionInt("graphics", "framelimit")
Global Vsync% = GetOptionInt("graphics", "vsync")

Global Opt_AntiAlias = GetOptionInt("graphics", "antialias")

Global CurrFrameLimit# = (Framelimit%-19)/100.0

Global ScreenGamma# = GetOptionFloat("graphics", "screengamma")
;If Fullscreen Then UpdateScreenGamma()

Global FOV% = GetOptionInt("graphics", "fov")
Const DEFAULT_FOV% = 59

Global HUDStartX%, HUDEndX%, HUDStartY%, HUDEndY%
Global HUDOffsetScale# = GetOptionFloat("graphics", "hud offset")

UpdateHUDOffsets()

Function UpdateHUDOffsets()
	If GraphicWidth > GraphicHeight Then
		HUDStartY = 0 : HUDEndY = GraphicHeight
		HUDStartX = Int(HUDOffsetScale * GraphicWidth / 2)
		HUDEndX = GraphicWidth - HUDStartX
	Else
		HUDStartX = 0 : HUDEndX = GraphicWidth
		HUDStartY = Int(HUDOffsetScale * GraphicHeight / 2)
		HUDEndY = GraphicHeight - HUDStartY
	EndIf
End Function

Const HIT_MAP% = 1, HIT_PLAYER% = 2, HIT_ITEM% = 3, HIT_APACHE% = 4, HIT_178% = 5, HIT_DEAD% = 6
SeedRnd MilliSecs()

;[End block]

Global GameSaved%

Global CanSave% = True

AppTitle "SCP - Containment Breach v"+VersionNumber

PlayStartupVideos()

;---------------------------------------------------------------------------------------------------------------------

;[Block]

Global CursorIMG% = LoadImage_Strict("GFX\cursor.png")

Global SelectedLoadingScreen.LoadingScreens, LoadingScreenAmount% = 0, LoadingScreenText%
Global LoadingBack% = LoadImage_Strict("Loadingscreens\loadingback.jpg")
InitLoadingScreens()

;For some reason, Blitz3D doesn't load fonts that have filenames that
;don't match their "internal name" (i.e. their display name in applications
;like Word and such). As a workaround, I moved the files and renamed them so they
;can load without FastText.
Font1% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(19 * MenuScale))
Font2% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(52 * MenuScale))
Font3% = LoadFont_Strict("GFX\font\DS-DIGI\DS-Digital.ttf", Int(22 * MenuScale))
Font4% = LoadFont_Strict("GFX\font\DS-DIGI\DS-Digital.ttf", Int(60 * MenuScale))
Font5% = LoadFont_Strict("GFX\font\Journal\Journal.ttf", Int(58 * MenuScale))

Global CreditsFont%,CreditsFont2%

ConsoleFont% = Font1

SetFont Font2

Global BlinkMeterIMG% = LoadImage_Strict("GFX\blinkmeter.jpg")
ScaleImage(BlinkMeterIMG, HUDScale, HUDScale)

DrawLoading(0, True)

; - -Viewport.
Global viewport_center_x% = RealGraphicWidth / 2, viewport_center_y% = RealGraphicHeight / 2

; -- Mouselook.
Global mouselook_x_inc# = 0.3 ; This sets both the sensitivity and direction (+/-) of the mouse on the X axis.
Global mouselook_y_inc# = 0.3 ; This sets both the sensitivity and direction (+/-) of the mouse on the Y axis.
Global mouse_x_speed_1#, mouse_y_speed_1#

Global MoveInputCancelling% = GetOptionInt("general", "move input cancelling")

Global KEY_RIGHT = GetOptionInt("binds", "Right key")
Global KEY_LEFT = GetOptionInt("binds", "Left key")
Global KEY_UP = GetOptionInt("binds", "Up key")
Global KEY_DOWN = GetOptionInt("binds", "Down key")

Global KEY_BLINK = GetOptionInt("binds", "Blink key")
Global KEY_SPRINT = GetOptionInt("binds", "Sprint key")
Global KEY_INV = GetOptionInt("binds", "Inventory key")
Global KEY_CROUCH = GetOptionInt("binds", "Crouch key")
Global KEY_SAVE = GetOptionInt("binds", "Save key")
Global KEY_CONSOLE = GetOptionInt("binds", "Console key")

Global MouseSmooth# = GetOptionFloat("controls", "mouse smoothing")

Global Mesh_MinX#, Mesh_MinY#, Mesh_MinZ#
Global Mesh_MaxX#, Mesh_MaxY#, Mesh_MaxZ#
Global Mesh_MagX#, Mesh_MagY#, Mesh_MagZ#

;player stats -------------------------------------------------------------------------------------------------------
Global KillTimer#, KillAnim%, FallTimer#, DeathTimer#
Global Sanity#, ForceMove#, ForceAngle#
Global RestoreSanity%

Global Playable% = True

Global BLINKFREQ#
Global BlinkTimer#, EyeIrritation#, EyeStuck#, BlinkEffect# = 1.0, BlinkEffectTimer#

Global Stamina#, StaminaEffect#=1.0, StaminaEffectTimer#

Global CameraShakeTimer#, Vomit%, VomitTimer#, Regurgitate%

Global SCP1025state#[6]

Global HeartBeatRate#, HeartBeatTimer#, HeartBeatVolume#

Global WearingGasMask%, WearingHazmat%, WearingVest%, Wearing714%, WearingNightVision%
Global NVTimer#

Global SuperMan%, SuperManTimer#

Global Injuries#, Bloodloss#, Infect#, HealTimer#

Global RefinedItems%

Include "Achievements.bb"

;player coordinates, angle, speed, movement etc ---------------------------------------------------------------------
Global DropSpeed#, HeadDropSpeed#, CurrSpeed#
Global user_camera_pitch#, side#
Global Crouch%, CrouchState#

Global PlayerZone%, PlayerRoom.Rooms

Global GrabbedEntity%

Global InvertMouse% = GetOptionInt("controls", "invert mouse y")
Global MouseHit1%, MouseDown1%, MouseHit2%, DoubleClick%, LastMouseHit1%, LastMouseHit1X%, LastMouseHit1Y%, MouseUp1%

Global GodMode%, NoClip%, NoClipSpeed# = 2.0

Global CoffinDistance# = 100.0

Global PlayerSoundVolume#

;camera/lighting effects (blur, camera shake, etc)-------------------------------------------------------------------
Global Shake#

Global ExplosionTimer#, ExplosionSFX%

Global LightsOn% = True

Global SoundTransmission%

;menus, GUI ---------------------------------------------------------------------------------------------------------
Global MainMenuOpen%, MenuOpen%, StopHidingTimer#, InvOpen%
Global OtherOpen.Items = Null

Global SelectedEnding$, EndingScreen%, EndingTimer#

Global MsgTimer#, Msg$, DeathMSG$

Global AccessCode%, KeypadInput$, KeypadTimer#, KeypadMSG$

Const HARPCODE% = 7816

Global DrawHandIcon%
Dim DrawArrowIcon%(4)

;misc ---------------------------------------------------------------------------------------------------------------

Include "Difficulty.bb"

Global MTFtimer#, MTFrooms.Rooms[10], MTFroomState%[10]

Dim RadioState#(10)
Dim RadioState3%(10)
Dim RadioState4%(9)
Dim RadioCHN%(8)

Dim OldAiPics%(5)

Global SpeedRunMode% = GetOptionInt("general", "speed run mode")
Global PlayTime%
; 0 = Running; 1 = Stopped; 2 = Pre-made save loaded; 3 = Ending reached
Global TimerStopped% = True
Global ConsoleFlush%
Global ConsoleFlushSnd% = 0, ConsoleMusFlush% = 0, ConsoleMusPlay% = 0

Global InfiniteStamina% = False
Global NVBlink%
Global IsNVGBlinking% = False

;[End block]


;----------------------------------------------  Console -----------------------------------------------------

Global ConsoleOpen%, ConsoleInput$
Global ConsoleScroll#,ConsoleScrollDragging%
Global ConsoleMouseMem%
Global ConsoleReissue.ConsoleMsg = Null
Global ConsoleR% = 255,ConsoleG% = 255,ConsoleB% = 255

Type ConsoleMsg
	Field txt$
	Field isCommand%
	Field r%,g%,b%
End Type

Function CreateConsoleMsg(txt$,r%=-1,g%=-1,b%=-1,isCommand%=False)
	Local c.ConsoleMsg = New ConsoleMsg
	Insert c Before First ConsoleMsg
	
	c\txt = txt
	c\isCommand = isCommand
	
	c\r = r
	c\g = g
	c\b = b
	
	If (c\r<0) Then c\r = ConsoleR
	If (c\g<0) Then c\g = ConsoleG
	If (c\b<0) Then c\b = ConsoleB
End Function

Function UpdateConsole()
	Local e.Events
	
	If CanOpenConsole = False Then
		ConsoleOpen = False
		Return
	EndIf
	
	If ConsoleOpen Then
		Local cm.ConsoleMsg
		
		SetFont ConsoleFont
		
		ConsoleR = 255 : ConsoleG = 255 : ConsoleB = 255
		
		Local x% = 0, y% = GraphicHeight-300*MenuScale, width% = GraphicWidth, height% = 300*MenuScale-30*MenuScale
		Local StrTemp$, temp%,  i%
		Local ev.Events, r.Rooms, it.Items
		
		DrawFrame x,y,width,height+30*MenuScale
		
		Local consoleHeight% = 0
		Local scrollbarHeight% = 0
		For cm.ConsoleMsg = Each ConsoleMsg
			consoleHeight = consoleHeight + 15*MenuScale
		Next
		scrollbarHeight = (Float(height)/Float(consoleHeight))*height
		If scrollbarHeight>height Then scrollbarHeight = height
		If consoleHeight<height Then consoleHeight = height
		
		Color 50,50,50
		inBar% = MouseOn(x+width-26*MenuScale,y,26*MenuScale,height)
		If inBar Then Color 70,70,70
		Rect x+width-26*MenuScale,y,26*MenuScale,height,True
		
		
		Color 120,120,120
		inBox% = MouseOn(x+width-23*MenuScale,y+height-scrollbarHeight+(ConsoleScroll*scrollbarHeight/height),20*MenuScale,scrollbarHeight)
		If inBox Then Color 200,200,200
		If ConsoleScrollDragging Then Color 255,255,255
		Rect x+width-23*MenuScale,y+height-scrollbarHeight+(ConsoleScroll*scrollbarHeight/height),20*MenuScale,scrollbarHeight,True
		
		If Not MouseDown(1) Then
			ConsoleScrollDragging=False
		ElseIf ConsoleScrollDragging Then
			ConsoleScroll = ConsoleScroll+((ScaledMouseY()-ConsoleMouseMem)*height/scrollbarHeight)
			ConsoleMouseMem = ScaledMouseY()
		EndIf
		
		If (Not ConsoleScrollDragging) Then
			If MouseHit1 Then
				If inBox Then
					ConsoleScrollDragging=True
					ConsoleMouseMem = ScaledMouseY()
				ElseIf inBar Then
					ConsoleScroll = ConsoleScroll+((ScaledMouseY()-(y+height))*consoleHeight/height+(height/2))
					ConsoleScroll = ConsoleScroll/2
				EndIf
			EndIf
		EndIf
		
		ConsoleScroll = ConsoleScroll - 15*MenuScale * MouseZSpeed()
		
		Local reissuePos%
		If KeyHit(200) Then
			reissuePos% = 0
			If (ConsoleReissue=Null) Then
				ConsoleReissue=First ConsoleMsg
				
				While (ConsoleReissue<>Null)
					If (ConsoleReissue\isCommand) Then
						Exit
					EndIf
					reissuePos = reissuePos - 15*MenuScale
					ConsoleReissue = After ConsoleReissue
				Wend
				
			Else
				cm.ConsoleMsg = First ConsoleMsg
				While cm<>Null
					If cm=ConsoleReissue Then Exit
					reissuePos = reissuePos-15*MenuScale
					cm = After cm
				Wend
				ConsoleReissue = After ConsoleReissue
				reissuePos = reissuePos-15*MenuScale
				
				While True
					If (ConsoleReissue=Null) Then
						ConsoleReissue=First ConsoleMsg
						reissuePos = 0
					EndIf
				
					If (ConsoleReissue\isCommand) Then
						Exit
					EndIf
					reissuePos = reissuePos - 15*MenuScale
					ConsoleReissue = After ConsoleReissue
				Wend
			EndIf
			
			If ConsoleReissue<>Null Then
				ConsoleInput = ConsoleReissue\txt
				ConsoleScroll = reissuePos+(height/2)
			EndIf
		EndIf
		
		If KeyHit(208) Then
			reissuePos% = -consoleHeight+15*MenuScale
			If (ConsoleReissue=Null) Then
				ConsoleReissue=Last ConsoleMsg
				
				While (ConsoleReissue<>Null)
					If (ConsoleReissue\isCommand) Then
						Exit
					EndIf
					reissuePos = reissuePos + 15*MenuScale
					ConsoleReissue = Before ConsoleReissue
				Wend
				
			Else
				cm.ConsoleMsg = Last ConsoleMsg
				While cm<>Null
					If cm=ConsoleReissue Then Exit
					reissuePos = reissuePos+15*MenuScale
					cm = Before cm
				Wend
				ConsoleReissue = Before ConsoleReissue
				reissuePos = reissuePos+15*MenuScale
				
				While True
					If (ConsoleReissue=Null) Then
						ConsoleReissue=Last ConsoleMsg
						reissuePos=-consoleHeight+15*MenuScale
					EndIf
				
					If (ConsoleReissue\isCommand) Then
						Exit
					EndIf
					reissuePos = reissuePos + 15*MenuScale
					ConsoleReissue = Before ConsoleReissue
				Wend
			EndIf
			
			If ConsoleReissue<>Null Then
				ConsoleInput = ConsoleReissue\txt
				ConsoleScroll = reissuePos+(height/2)
			EndIf
		EndIf
		
		If ConsoleScroll<-consoleHeight+height Then ConsoleScroll = -consoleHeight+height
		If ConsoleScroll>0 Then ConsoleScroll = 0
		
		Color 255, 255, 255
		
		SelectedInputBox = 2
		Local oldConsoleInput$ = ConsoleInput
		ConsoleInput = InputBox(x, y + height, width, 30*MenuScale, ConsoleInput, 2)
		If oldConsoleInput<>ConsoleInput Then
			ConsoleReissue = Null
		EndIf
		ConsoleInput = Left(ConsoleInput, 100)
		
		If KeyHit(28) And ConsoleInput <> "" Then
			UsedConsole = True
			ConsoleReissue = Null
			ConsoleScroll = 0
			CreateConsoleMsg(ConsoleInput,255,255,0,True)
			If Instr(ConsoleInput, " ") > 0 Then
				StrTemp$ = Lower(Left(ConsoleInput, Instr(ConsoleInput, " ") - 1))
			Else
				StrTemp$ = Lower(ConsoleInput)
			End If
			
			Select Lower(StrTemp)
				Case "help"
					;[Block]
					If Instr(ConsoleInput, " ")<>0 Then
						StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Else
						StrTemp$ = ""
					EndIf
					ConsoleR = 0 : ConsoleG = 255 : ConsoleB = 255
					
					Select Lower(StrTemp)
						Case "1",""
							CreateConsoleMsg("LIST OF COMMANDS - PAGE 1/3")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("- asd")
							CreateConsoleMsg("- status")
							CreateConsoleMsg("- noclip")
							CreateConsoleMsg("- noclipspeed")
							CreateConsoleMsg("- godmode")
							CreateConsoleMsg("- revive")
							CreateConsoleMsg("- injure [value]")
							CreateConsoleMsg("- infect [value]")
							CreateConsoleMsg("- heal")
							CreateConsoleMsg("- infinitestamina")
							CreateConsoleMsg("- sanic")
							CreateConsoleMsg("- notarget")
							CreateConsoleMsg("- teleport [room name] [index]")
							CreateConsoleMsg("- roomlist")
							CreateConsoleMsg("- spawnitem [item name]")
							CreateConsoleMsg("- itemlist")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Use "+Chr(34)+"help 2/3"+Chr(34)+" to find more commands.")
							CreateConsoleMsg("Use "+Chr(34)+"help [command name]"+Chr(34)+" to get more information about a command.")
							CreateConsoleMsg("******************************")
						Case "2"
							CreateConsoleMsg("LIST OF COMMANDS - PAGE 2/3")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("- spawn [npc type] [state]")
							CreateConsoleMsg("- 096state")
							CreateConsoleMsg("- reset096")
							CreateConsoleMsg("- 106state")
							CreateConsoleMsg("- 106speed")
							CreateConsoleMsg("- disable106")
							CreateConsoleMsg("- enable106")
							CreateConsoleMsg("- 173speed")
							CreateConsoleMsg("- 173state")
							CreateConsoleMsg("- disable173")
							CreateConsoleMsg("- enable173")
							CreateConsoleMsg("- halloween")
							CreateConsoleMsg("- scp-420-j")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Use "+Chr(34)+"help [command name]"+Chr(34)+" to get more information about a command.")
							CreateConsoleMsg("******************************")
						Case "3"
							CreateConsoleMsg("LIST OF COMMANDS - PAGE 3/3")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("- wireframe")
							CreateConsoleMsg("- showfps")
							CreateConsoleMsg("- debughud")
							CreateConsoleMsg("- camerafog [near] [far]")
							CreateConsoleMsg("- fov [value]")
							CreateConsoleMsg("- gamma [value]")
							CreateConsoleMsg("- playmusic [clip + .wav/.ogg]")
							CreateConsoleMsg("- camerapick")
							CreateConsoleMsg("- ending")
							CreateConsoleMsg("- unlockexits")
							CreateConsoleMsg("- omni")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Use "+Chr(34)+"help [command name]"+Chr(34)+" to get more information about a command.")
							CreateConsoleMsg("******************************")
						Case "asd"
							CreateConsoleMsg("HELP - asd")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Actives godmode, noclip, wireframe and")
							CreateConsoleMsg("sets fog distance to 20 near, 30 far")
							CreateConsoleMsg("******************************")
						Case "camerafog"
							CreateConsoleMsg("HELP - camerafog")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Sets the draw distance of the fog.")
							CreateConsoleMsg("The fog begins generating at 'CameraFogNear' units")
							CreateConsoleMsg("away from the camera and becomes completely opaque")
							CreateConsoleMsg("at 'CameraFogFar' units away from the camera.")
							CreateConsoleMsg("Example: camerafog 20 40")
							CreateConsoleMsg("******************************")
						Case "gamma"
							CreateConsoleMsg("HELP - gamma")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Sets the gamma correction.")
							CreateConsoleMsg("Should be set to a value between 0.0 and 2.0.")
							CreateConsoleMsg("Default is 1.0.")
							CreateConsoleMsg("******************************")
						Case "noclip","fly"
							CreateConsoleMsg("HELP - noclip")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Toggles noclip, unless a valid parameter")
							CreateConsoleMsg("is specified (on/off).")
							CreateConsoleMsg("Allows the camera to move in any direction while")
							CreateConsoleMsg("bypassing collision.")
							CreateConsoleMsg("******************************")
						Case "godmode","god"
							CreateConsoleMsg("HELP - godmode")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Toggles godmode, unless a valid parameter")
							CreateConsoleMsg("is specified (on/off).")
							CreateConsoleMsg("Prevents player death under normal circumstances.")
							CreateConsoleMsg("******************************")
						Case "wireframe"
							CreateConsoleMsg("HELP - wireframe")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Toggles wireframe, unless a valid parameter")
							CreateConsoleMsg("is specified (on/off).")
							CreateConsoleMsg("Allows only the edges of geometry to be rendered,")
							CreateConsoleMsg("making everything else transparent.")
							CreateConsoleMsg("******************************")
						Case "spawnitem"
							CreateConsoleMsg("HELP - spawnitem")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Spawns an item at the player's location.")
							CreateConsoleMsg("Any name that can appear in your inventory")
							CreateConsoleMsg("is a valid parameter.")
							CreateConsoleMsg("Example: spawnitem Key Card Omni")
							CreateConsoleMsg("******************************")
						Case "itemlist", "items"
							CreateConsoleMsg("HELP - itemlist")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Lists all currently loaded item templates.")
							CreateConsoleMsg("******************************")
						Case "spawn"
							CreateConsoleMsg("HELP - spawn")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Spawns an NPC at the player's location.")
							CreateConsoleMsg("Valid parameters are:")
							CreateConsoleMsg("008zombie / 049 / 049-2 / 066 / 096 / 106 / 173")
							CreateConsoleMsg("/ 178-1 / 372 / 513-1 / 966 / 1499-1 / class-d")
							CreateConsoleMsg("/ guard / mtf / apache / tentacle")
							CreateConsoleMsg("******************************")
						Case "revive","undead","resurrect"
							CreateConsoleMsg("HELP - revive")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Resets the player's death timer after the dying")
							CreateConsoleMsg("animation triggers.")
							CreateConsoleMsg("Does not affect injury, blood loss")
							CreateConsoleMsg("or 008 infection values.")
							CreateConsoleMsg("******************************")
						Case "teleport"
							CreateConsoleMsg("HELP - teleport")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Teleports the player to the first (or specified)")
							CreateConsoleMsg("instance of the specified room. Any room that")
							CreateConsoleMsg("appears in rooms.ini is a valid parameter.")
							CreateConsoleMsg("******************************")
						Case "roomlist", "rooms"
							CreateConsoleMsg("HELP - roomlist")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Lists all currently loaded room templates.")
							CreateConsoleMsg("Not all room templates may appear in the map.")
							CreateConsoleMsg("******************************")
						Case "stopsound", "stfu"
							CreateConsoleMsg("HELP - stopsound")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Stops all currently playing sounds.")
							CreateConsoleMsg("******************************")
						Case "camerapick"
							CreateConsoleMsg("HELP - camerapick")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Prints the texture name and coordinates of")
							CreateConsoleMsg("the model the camera is pointing at.")
							CreateConsoleMsg("******************************")
						Case "fov"
							CreateConsoleMsg("HELP - fov")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Field of view (FOV) is the amount of game view")
							CreateConsoleMsg("that is on display during a game.")
							CreateConsoleMsg("******************************")
						Case "status"
							CreateConsoleMsg("HELP - status")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Prints player, camera, and room information.")
							CreateConsoleMsg("******************************")
						Case "weed","scp-420-j","420"
							CreateConsoleMsg("HELP - 420")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Generates dank memes.")
							CreateConsoleMsg("******************************")
						Case "playmusic"
							CreateConsoleMsg("HELP - playmusic")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Will play tracks in .ogg/.wav format")
							CreateConsoleMsg("from "+Chr(34)+"SFX\Music\Custom\"+Chr(34)+".")
							CreateConsoleMsg("******************************")
						Case "omni"
							CreateConsoleMsg("HELP - omni")
							CreateConsoleMsg("******************************")
							CreateConsoleMsg("Toggles getting guaranteed Key Card Omnis")
							CreateConsoleMsg("from SCP-914, unless a valid parameter")
							CreateConsoleMsg("is specified (on/off).")
							CreateConsoleMsg("A Key Card Omni can be obtained from SCP-914")
							CreateConsoleMsg("by refining a Level 5 Key Card on Fine")
							CreateConsoleMsg("or any Key Card on Very Fine.")
							CreateConsoleMsg("By default, the chance of success is random")
							CreateConsoleMsg("and relies on the amount of achievements")
							CreateConsoleMsg("unlocked in the current save.")
							CreateConsoleMsg("******************************")
							
						Default
							CreateConsoleMsg("There is no help available for that command.",255,150,0)
					End Select
					
					;[End Block]
				Case "asd"
					;[Block]
					WireFrame 1
					WireframeState=1
					GodMode = 1
					NoClip = 1
					CameraFogNear = 15
					CameraFogFar = 20
					;[End Block]
				Case "status"
					;[Block]
					ConsoleR = 0 : ConsoleG = 255 : ConsoleB = 0
					CreateConsoleMsg("******************************")
					CreateConsoleMsg("Status: ")
					CreateConsoleMsg("Coordinates: ")
					CreateConsoleMsg("    - collider: "+EntityX(Collider)+", "+EntityY(Collider)+", "+EntityZ(Collider))
					CreateConsoleMsg("    - camera: "+EntityX(Camera)+", "+EntityY(Camera)+", "+EntityZ(Camera))
					
					CreateConsoleMsg("Rotation: ")
					CreateConsoleMsg("    - collider: "+EntityPitch(Collider)+", "+EntityYaw(Collider)+", "+EntityRoll(Collider))
					CreateConsoleMsg("    - camera: "+EntityPitch(Camera)+", "+EntityYaw(Camera)+", "+EntityRoll(Camera))
					
					CreateConsoleMsg("Room: "+PlayerRoom\RoomTemplate\Name)
					For ev.Events = Each Events
						If ev\room = PlayerRoom Then
							CreateConsoleMsg("Room event: "+ev\EventName)	
							CreateConsoleMsg("-    state: "+ev\EventState)
							CreateConsoleMsg("-    state2: "+ev\EventState2)	
							CreateConsoleMsg("-    state3: "+ev\EventState3)
							Exit
						EndIf
					Next
					
					CreateConsoleMsg("Room coordinates: "+Floor(EntityX(PlayerRoom\obj) / 8.0 + 0.5)+", "+ Floor(EntityZ(PlayerRoom\obj) / 8.0 + 0.5))
					CreateConsoleMsg("Stamina: "+Stamina)
					CreateConsoleMsg("Death timer: "+KillTimer)					
					CreateConsoleMsg("Blinktimer: "+BlinkTimer)
					CreateConsoleMsg("Injuries: "+Injuries)
					CreateConsoleMsg("Bloodloss: "+Bloodloss)
					CreateConsoleMsg("******************************")
					;[End Block]
				Case "camerapick"
					;[Block]
					ConsoleR = 0 : ConsoleG = 255 : ConsoleB = 0
					c = CameraPick(Camera,GraphicWidth/2, GraphicHeight/2)
					If c = 0 Then
						CreateConsoleMsg("******************************")
						CreateConsoleMsg("No entity  picked")
						CreateConsoleMsg("******************************")								
					Else
						CreateConsoleMsg("******************************")
						CreateConsoleMsg("Picked entity:")
						sf = GetSurface(c,1)
						b = GetSurfaceBrush( sf )
						t = GetBrushTexture(b,0)
						texname$ =  StripPath(TextureName(t))
						CreateConsoleMsg("Texture name: "+texname)
						CreateConsoleMsg("Coordinates: "+EntityX(c)+", "+EntityY(c)+", "+EntityZ(c))
						CreateConsoleMsg("******************************")							
					EndIf
					;[End Block]
				Case "fov"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					FOV = Int(StrTemp)
					;[End Block]
				Case "hidedistance"
					;[Block]
					HideDistance = Float(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					CreateConsoleMsg("Hidedistance set to "+HideDistance)
					;[End Block]
				Case "ending"
					;[Block]
					SelectedEnding = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					KillTimer = -0.1
					;EndingTimer = -0.1
					;[End Block]
				Case "noclipspeed"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					NoClipSpeed = Float(StrTemp)
					;[End Block]
				Case "injure"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Injuries = Float(StrTemp)
					;[End Block]
				Case "infect"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Infect = Float(StrTemp)
					;[End Block]
				Case "heal"
					;[Block]
					Injuries = 0
					Bloodloss = 0
					ResetDiseases()
					;[End Block]
				Case "teleport"
					;[Block]
					Local roomName$ = Piece(ConsoleInput, 2, " ")
					Local roomIndex% = Int(Piece(ConsoleInput, 3, " "))

					Select roomName
						Case "895", "scp-895", "room895"
							roomName = "coffin"
						Case "scp-914", "room914"
							roomName = "914"
						Case "offices", "office"
							roomName = "room2offices"
						Case "room372"
							roomName = "roompj"
						Case "room970"
							roomName = "room2storage"
					End Select
					
					Local roomFound% = False

					For r.Rooms = Each Rooms
						If r\RoomTemplate\Name = roomName Then
							If roomIndex <> 0 Then
								roomIndex = roomIndex - 1
							Else
								PositionEntity (Collider, EntityX(r\obj), EntityY(r\obj)+0.7, EntityZ(r\obj))
								ResetEntity(Collider)
								UpdateDoors()
								UpdateRooms()
								For it.Items = Each Items
									it\disttimer = 0
								Next
								PlayerRoom = r
								roomFound = True
								Exit
							EndIf
						EndIf
					Next
					
					If Not roomFound Then CreateConsoleMsg("Room not found.",255,150,0)
					;[End Block]
				Case "roomlist", "rooms"
					CreateConsoleMsg("Listing rooms:")
					For rt.RoomTemplates = Each RoomTemplates
						CreateConsoleMsg("- " + rt\Name)
					Next
				Case "spawnitem"
					;[Block]
					Local itt.ItemTemplates = FindItemTemplate(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					If itt = Null Then
						CreateConsoleMsg("Item not found.",255,150,0)
					Else
						CreateConsoleMsg(itt\displayname + " spawned.")
						it.Items = CreateItem(itt\name, EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
						EntityType(it\collider, HIT_ITEM)

						If itt\name = "snavulti" Lor itt\name = "fineradio" Lor itt\name = "veryfineradio" Then
							it\state = 101
						EndIf
					End If
					;[End Block]
				Case "itemlist", "items"
					CreateConsoleMsg("Listing items:")
					For itt.ItemTemplates = Each ItemTemplates
						CreateConsoleMsg("- " + itt\displayname + "(" + itt\name + ")")
					Next
				Case "wireframe"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							WireframeState = True							
						Case "off", "0", "false"
							WireframeState = False
						Default
							WireframeState = Not WireframeState
					End Select
					
					If WireframeState Then
						CreateConsoleMsg("WIREFRAME ON")
					Else
						CreateConsoleMsg("WIREFRAME OFF")	
					EndIf
					
					WireFrame WireframeState
					;[End Block]
				Case "173speed"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Curr173\Speed = Float(StrTemp)
					CreateConsoleMsg("173's speed set to " + StrTemp)
					;[End Block]
				Case "106speed"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Curr106\Speed = Float(StrTemp)
					CreateConsoleMsg("106's speed set to " + StrTemp)
					;[End Block]
				Case "173state"
					;[Block]
					CreateConsoleMsg("SCP-173")
					CreateConsoleMsg("Position: " + EntityX(Curr173\obj) + ", " + EntityY(Curr173\obj) + ", " + EntityZ(Curr173\obj))
					CreateConsoleMsg("Idle: " + Curr173\Idle)
					CreateConsoleMsg("State: " + Curr173\State)
					;[End Block]
				Case "106state"
					;[Block]
					CreateConsoleMsg("SCP-106")
					CreateConsoleMsg("Position: " + EntityX(Curr106\obj) + ", " + EntityY(Curr106\obj) + ", " + EntityZ(Curr106\obj))
					CreateConsoleMsg("Idle: " + Curr106\Idle)
					CreateConsoleMsg("State: " + Curr106\State)
					;[End Block]
				Case "reset096"
					;[Block]
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype096 Then
							n\State = 0
							If n\SoundChn<>0
								StopStream_Strict(n\SoundChn) : n\SoundChn=0 : n\SoundChn_isStream = False
							EndIf
							If n\SoundChn2<>0
								StopStream_Strict(n\SoundChn2) : n\SoundChn2=0 : n\SoundChn2_isStream = False
							EndIf
							Exit
						EndIf
					Next
					;[End Block]
				Case "disable173"
					;[Block]
					Curr173\Idle = 3 ;This phenominal comment is brought to you by PolyFox. His absolute wisdom in this fatigue of knowledge brought about a new era of 173 state checks.
					HideEntity Curr173\obj
					HideEntity Curr173\Collider
					;[End Block]
				Case "enable173"
					;[Block]
					Curr173\Idle = False
					ShowEntity Curr173\obj
					ShowEntity Curr173\Collider
					;[End Block]
				Case "disable106"
					;[Block]
					Curr106\Idle = True
					Curr106\State = 200000
					Contained106 = True
					;[End Block]
				Case "enable106"
					;[Block]
					Curr106\Idle = False
					Contained106 = False
					ShowEntity Curr106\Collider
					ShowEntity Curr106\obj
					;[End Block]
				Case "halloween"
					;[Block]
					HalloweenTex = Not HalloweenTex
					If HalloweenTex Then
						Local tex = LoadTexture_Strict("GFX\npcs\173h.pt", 1)
						EntityTexture Curr173\obj, tex, 0, 0
						FreeTexture tex
						CreateConsoleMsg("173 JACK-O-LANTERN ON")
					Else
						Local tex2 = LoadTexture_Strict("GFX\npcs\173texture.jpg", 1)
						EntityTexture Curr173\obj, tex2, 0, 0
						FreeTexture tex2
						CreateConsoleMsg("173 JACK-O-LANTERN OFF")
					EndIf
					;[End Block]
				Case "sanic"
					;[Block]
					SuperMan = Not SuperMan
					If SuperMan = True Then
						CreateConsoleMsg("GOTTA GO FAST")
					Else
						CreateConsoleMsg("WHOA SLOW DOWN")
					EndIf
					;[End Block]
				Case "scp-420-j","420","weed"
					;[Block]
					For i = 1 To 20
						If Rand(2)=1 Then
							it.Items = CreateItem("scp420j", EntityX(Collider,True)+Cos((360.0/20.0)*i)*Rnd(0.3,0.5), EntityY(Camera,True), EntityZ(Collider,True)+Sin((360.0/20.0)*i)*Rnd(0.3,0.5))
						Else
							it.Items = CreateItem("joint", EntityX(Collider,True)+Cos((360.0/20.0)*i)*Rnd(0.3,0.5), EntityY(Camera,True), EntityZ(Collider,True)+Sin((360.0/20.0)*i)*Rnd(0.3,0.5))
						EndIf
						EntityType (it\collider, HIT_ITEM)
					Next
					PlaySound_Strict LoadTempSound("SFX\Music\420J.ogg")
					;[End Block]
				Case "godmode", "god"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							GodMode = True						
						Case "off", "0", "false"
							GodMode = False
						Default
							GodMode = Not GodMode
					End Select	
					If GodMode Then
						CreateConsoleMsg("GODMODE ON")
					Else
						CreateConsoleMsg("GODMODE OFF")	
					EndIf
					;[End Block]
				Case "revive","undead","resurrect"
					;[Block]
					DropSpeed = -0.1
					HeadDropSpeed = 0.0
					Shake = 0
					CurrSpeed = 0
					
					HeartBeatVolume = 0
					
					CameraShake = 0
					Shake = 0
					LightFlash = 0
					BlurTimer = 0
					
					FallTimer = 0
					MenuOpen = False
					
					GodMode = 0
					NoClip = 0
					
					ShowEntity Collider
					TranslateEntity Collider, 0, 1.5, 0
					RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), 0
					
					KillTimer = 0
					KillAnim = 0
					;[End Block]
				Case "noclip","fly"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							NoClip = True
							Playable = True
						Case "off", "0", "false"
							NoClip = False	
							RotateEntity Collider, 0, EntityYaw(Collider), 0
						Default
							NoClip = Not NoClip
							If NoClip = False Then		
								RotateEntity Collider, 0, EntityYaw(Collider), 0
							Else
								Playable = True
							EndIf
					End Select
					
					If NoClip Then
						CreateConsoleMsg("NOCLIP ON")
					Else
						CreateConsoleMsg("NOCLIP OFF")
					EndIf
					
					DropSpeed = 0
					;[End Block]
				Case "showfps"
					;[Block]
					ShowFPS = Not ShowFPS
					CreateConsoleMsg("ShowFPS: "+Str(ShowFPS))
					;[End Block]
				Case "096state"
					;[Block]
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype096 Then
							CreateConsoleMsg("SCP-096")
							CreateConsoleMsg("Position: " + EntityX(n\obj) + ", " + EntityY(n\obj) + ", " + EntityZ(n\obj))
							CreateConsoleMsg("Idle: " + n\Idle)
							CreateConsoleMsg("State: " + n\State)
							Exit
						EndIf
					Next
					CreateConsoleMsg("SCP-096 has not spawned.")
					;[End Block]
				Case "debughud"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Select StrTemp
						Case "on", "1", "true"
							DebugHUD = True
						Case "off", "0", "false"
							DebugHUD = False
						Default
							DebugHUD = Not DebugHUD
					End Select
					
					If DebugHUD Then
						CreateConsoleMsg("Debug Mode On")
					Else
						CreateConsoleMsg("Debug Mode Off")
					EndIf
					;[End Block]
				Case "stopsound", "stfu"
					;[Block]
					KillSounds()
					
					For e.Events = Each Events
						If e\EventName = "alarm" Then 
							If e\room\NPC[0] <> Null Then RemoveNPC(e\room\NPC[0])
							If e\room\NPC[1] <> Null Then RemoveNPC(e\room\NPC[1])
							If e\room\NPC[2] <> Null Then RemoveNPC(e\room\NPC[2])
							
							FreeEntity e\room\Objects[0] : e\room\Objects[0]=0
							FreeEntity e\room\Objects[1] : e\room\Objects[1]=0
							PositionEntity Curr173\Collider, 0,0,0
							ResetEntity Curr173\Collider
							ShowEntity Curr173\obj
							RemoveEvent(e)
							Exit
						EndIf
					Next
					CreateConsoleMsg("Stopped all sounds.")
					;[End Block]
				Case "camerafog"
					;[Block]
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					CameraFogNear = Float(Left(args, Len(args) - Instr(args, " ")))
					CameraFogFar = Float(Right(args, Len(args) - Instr(args, " ")))
					CreateConsoleMsg("Near set to: " + CameraFogNear + ", far set to: " + CameraFogFar)
					;[End Block]
				Case "gamma"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					ScreenGamma = Int(StrTemp)
					CreateConsoleMsg("Gamma set to " + ScreenGamma)
					;[End Block]
				Case "spawn"
					;[Block]
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					StrTemp$ = Piece$(args$, 1)
					StrTemp2$ = Piece$(args$, 2)
					
					;Hacky fix for when the user doesn't input a second parameter.
					If (StrTemp <> StrTemp2) Then
						Console_SpawnNPC(StrTemp, StrTemp2)
					Else
						Console_SpawnNPC(StrTemp)
					EndIf
					;[End Block]
				;new Console Commands in SCP:CB 1.3 - ENDSHN
				Case "infinitestamina","infstam"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							InfiniteStamina% = True						
						Case "off", "0", "false"
							InfiniteStamina% = False
						Default
							InfiniteStamina% = Not InfiniteStamina%
					End Select
					
					If InfiniteStamina
						CreateConsoleMsg("INFINITE STAMINA ON")
					Else
						CreateConsoleMsg("INFINITE STAMINA OFF")	
					EndIf
					;[End Block]
				Case "asd2"
					;[Block]
					GodMode = 1
					InfiniteStamina = 1
					Curr173\Idle = 3
					Curr106\Idle = True
					Curr106\State = 200000
					Contained106 = True
					;[End Block]
				Case "toggle_warhead_lever"
					;[Block]
					For e.Events = Each Events
						If e\EventName = "room2nuke" Then
							e\EventState = (Not e\EventState)
							Exit
						EndIf
					Next
					;[End Block]
				Case "unlockexits"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "a"
							For e.Events = Each Events
								If e\EventName = "gateaentrance" Then
									e\EventState3 = 1
									e\room\RoomDoors[1]\open = True
									Exit
								EndIf
							Next
							CreateConsoleMsg("Gate A is now unlocked.")	
						Case "b"
							For e.Events = Each Events
								If e\EventName = "exit1" Then
									e\EventState3 = 1
									e\room\RoomDoors[4]\open = True
									Exit
								EndIf
							Next	
							CreateConsoleMsg("Gate B is now unlocked.")	
						Default
							For e.Events = Each Events
								If e\EventName = "gateaentrance" Then
									e\EventState3 = 1
									e\room\RoomDoors[1]\open = True
								ElseIf e\EventName = "exit1" Then
									e\EventState3 = 1
									e\room\RoomDoors[4]\open = True
								EndIf
							Next
							CreateConsoleMsg("Gate A and B are now unlocked.")	
					End Select
					
					RemoteDoorOn = True
					;[End Block]
				Case "kill","suicide"
					;[Block]
					KillTimer = -1
					DeathMSG = I_Loc\DeathMessage_Suicide[Rand(4)]
					;[End Block]
				Case "playmusic"
					;[Block]
					; I think this might be broken since the FMod library streaming was added. -Mark
					If Instr(ConsoleInput, " ")<>0 Then
						StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Else
						StrTemp$ = ""
					EndIf
					
					If StrTemp$ <> ""
						PlayCustomMusic% = True
						If CustomMusic <> 0 Then FreeSound_Strict CustomMusic : CustomMusic = 0
						If MusicCHN <> 0 Then StopChannel MusicCHN
						CustomMusic = LoadSound_Strict("SFX\Music\Custom\"+StrTemp$)
						If CustomMusic = 0
							PlayCustomMusic% = False
						EndIf
					Else
						PlayCustomMusic% = False
						If CustomMusic <> 0 Then FreeSound_Strict CustomMusic : CustomMusic = 0
						If MusicCHN <> 0 Then StopChannel MusicCHN
					EndIf
					;[End Block]
				Case "tp"
					;[Block]
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtypeMTF
							If n\MTFLeader = Null
								PositionEntity Collider,EntityX(n\Collider),EntityY(n\Collider)+5,EntityZ(n\Collider)
								ResetEntity Collider
								Exit
							EndIf
						EndIf
					Next
					;[End Block]
				Case "tele"
					;[Block]
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					StrTemp$ = Piece$(args$,1," ")
					StrTemp2$ = Piece$(args$,2," ")
					StrTemp3$ = Piece$(args$,3," ")
					PositionEntity Collider,Float(StrTemp$),Float(StrTemp2$),Float(StrTemp3$)
					PositionEntity Camera,Float(StrTemp$),Float(StrTemp2$),Float(StrTemp3$)
					ResetEntity Collider
					ResetEntity Camera
					CreateConsoleMsg("Teleported to coordinates (X|Y|Z): "+EntityX(Collider)+"|"+EntityY(Collider)+"|"+EntityZ(Collider))
					;[End Block]
				Case "notarget"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							NoTarget% = True						
						Case "off", "0", "false"
							NoTarget% = False	
						Default
							NoTarget% = Not NoTarget%
					End Select
					
					If NoTarget% = False Then
						CreateConsoleMsg("NOTARGET OFF")
					Else
						CreateConsoleMsg("NOTARGET ON")	
					EndIf
					;[End Block]
				Case "spawnradio"
					;[Block]
					it.Items = CreateItem("fineradio", EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
					EntityType(it\collider, HIT_ITEM)
					it\state = 101
					;[End Block]
				Case "spawnnvg"
					;[Block]
					it.Items = CreateItem("nvgoggles", EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
					EntityType(it\collider, HIT_ITEM)
					it\state = 1000
					;[End Block]
				Case "spawnpumpkin","pumpkin"
					;[Block]
					CreateConsoleMsg("What pumpkin?")
					;[End Block]
				Case "spawnnav"
					;[Block]
					it.Items = CreateItem("snavulti", EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
					EntityType(it\collider, HIT_ITEM)
					it\state = 101
					;[End Block]
				Case "teleport173"
					;[Block]
					PositionEntity Curr173\Collider,EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider)
					ResetEntity Curr173\Collider
					;[End Block]
				Case "seteventstate"
					;[Block]
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					StrTemp$ = Piece$(args$,1," ")
					StrTemp2$ = Piece$(args$,2," ")
					StrTemp3$ = Piece$(args$,3," ")
					Local pl_room_found% = False
					If StrTemp="" Or StrTemp2="" Or StrTemp3=""
						CreateConsoleMsg("Too few parameters. This command requires 3.",255,150,0)
					Else
						For e.Events = Each Events
							If e\room = PlayerRoom
								If Lower(StrTemp)<>"keep"
									e\EventState = Float(StrTemp)
								EndIf
								If Lower(StrTemp2)<>"keep"
									e\EventState2 = Float(StrTemp2)
								EndIf
								If Lower(StrTemp3)<>"keep"
									e\EventState3 = Float(StrTemp3)
								EndIf
								CreateConsoleMsg("Changed event states from current player room to: "+e\EventState+"|"+e\EventState2+"|"+e\EventState3)
								pl_room_found = True
								Exit
							EndIf
						Next
						If (Not pl_room_found)
							CreateConsoleMsg("The current room doesn't has any event applied.",255,150,0)
						EndIf
					EndIf
					;[End Block]
				Case "spawnparticles"
					;[Block]
					If Instr(ConsoleInput, " ")<>0 Then
						StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Else
						StrTemp$ = ""
					EndIf
					
					If Int(StrTemp) > -1 And Int(StrTemp) <= 1 ;<--- This is the maximum ID of particles by Devil Particle system, will be increased after time - ENDSHN
						SetEmitter(Collider,ParticleEffect[Int(StrTemp)])
						CreateConsoleMsg("Spawned particle emitter with ID "+Int(StrTemp)+" at player's position.")
					Else
						CreateConsoleMsg("Particle emitter with ID "+Int(StrTemp)+" not found.",255,150,0)
					EndIf
					;[End Block]
				Case "giveachievement"
					;[Block]
					If Instr(ConsoleInput, " ")<>0 Then
						StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					Else
						StrTemp$ = ""
					EndIf
					
					If Int(StrTemp)>=0 And Int(StrTemp)<MAXACHIEVEMENTS
						Achievements(Int(StrTemp))=True
						CreateConsoleMsg("Achievemt "+AchievementStrings(Int(StrTemp))+" unlocked.")
					Else
						CreateConsoleMsg("Achievement with ID "+Int(StrTemp)+" doesn't exist.",255,150,0)
					EndIf
					;[End Block]
				Case "427state"
					;[Block]
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					I_427\Timer = Float(StrTemp)*70.0
					;[End Block]
				Case "teleport106"
					;[Block]
					Curr106\State = 0
					Curr106\Idle = False
					;[End Block]
				Case "setblinkeffect"
					;[Block]
					args$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					BlinkEffect = Float(Left(args, Len(args) - Instr(args, " ")))
					BlinkEffectTimer = Float(Right(args, Len(args) - Instr(args, " ")))
					CreateConsoleMsg("Set BlinkEffect to: " + BlinkEffect + "and BlinkEffect timer: " + BlinkEffectTimer)
					;[End Block]
				Case "omni"
					StrTemp$ = Lower(Right(ConsoleInput, Len(ConsoleInput) - Instr(ConsoleInput, " ")))
					
					Select StrTemp
						Case "on", "1", "true"
							GuaranteedOmni% = True						
						Case "off", "0", "false"
							GuaranteedOmni% = False	
						Default
							GuaranteedOmni% = Not GuaranteedOmni%
					End Select
					
					If GuaranteedOmni% Then
						CreateConsoleMsg("GUARANTEED KEY CARD OMNI ON")	
					Else
						CreateConsoleMsg("GUARANTEED KEY CARD OMNI OFF")
					EndIf
				Case "mav"
					RuntimeErrorExt("Violation Access Memory")
				Case "jorge"
					;[Block]	
					CreateConsoleMsg(Chr(74)+Chr(79)+Chr(82)+Chr(71)+Chr(69)+Chr(32)+Chr(72)+Chr(65)+Chr(83)+Chr(32)+Chr(66)+Chr(69)+Chr(69)+Chr(78)+Chr(32)+Chr(69)+Chr(88)+Chr(80)+Chr(69)+Chr(67)+Chr(84)+Chr(73)+Chr(78)+Chr(71)+Chr(32)+Chr(89)+Chr(79)+Chr(85)+Chr(46))
;					Return
;					ConsoleFlush = True 
;					
;					If ConsoleFlushSnd = 0 Then
;						ConsoleFlushSnd = LoadSound(Chr(83)+Chr(70)+Chr(88)+Chr(92)+Chr(83)+Chr(67)+Chr(80)+Chr(92)+Chr(57)+Chr(55)+Chr(48)+Chr(92)+Chr(116)+Chr(104)+Chr(117)+Chr(109)+Chr(98)+Chr(115)+Chr(46)+Chr(100)+Chr(98))
;						;FMOD_Pause(MusicCHN)
;						;FSOUND_Stream_Stop()
;						ConsoleMusFlush% = LoadSound(Chr(83)+Chr(70)+Chr(88)+Chr(92)+Chr(77)+Chr(117)+Chr(115)+Chr(105)+Chr(99)+Chr(92)+Chr(116)+Chr(104)+Chr(117)+Chr(109)+Chr(98)+Chr(115)+Chr(46)+Chr(100)+Chr(98))
;						ConsoleMusPlay = PlaySound(ConsoleMusFlush)
;					Else
;						CreateConsoleMsg(Chr(74)+Chr(32)+Chr(79)+Chr(32)+Chr(82)+Chr(32)+Chr(71)+Chr(32)+Chr(69)+Chr(32)+Chr(32)+Chr(67)+Chr(32)+Chr(65)+Chr(32)+Chr(78)+Chr(32)+Chr(78)+Chr(32)+Chr(79)+Chr(32)+Chr(84)+Chr(32)+Chr(32)+Chr(66)+Chr(32)+Chr(69)+Chr(32)+Chr(32)+Chr(67)+Chr(32)+Chr(79)+Chr(32)+Chr(78)+Chr(32)+Chr(84)+Chr(32)+Chr(65)+Chr(32)+Chr(73)+Chr(32)+Chr(78)+Chr(32)+Chr(69)+Chr(32)+Chr(68)+Chr(46))
;					EndIf
					;[End Block]
				Default
					;[Block]
					CreateConsoleMsg("Command not found.",255,0,0)
					;[End Block]
			End Select
			
			ConsoleInput = ""
		End If
		
		Local TempY% = y + height - 25*MenuScale - ConsoleScroll
		Local count% = 0
		For cm.ConsoleMsg = Each ConsoleMsg
			count = count+1
			If count>1000 Then
				Delete cm
			Else
				If TempY >= y And TempY < y + height - 20*MenuScale Then
					If cm=ConsoleReissue Then
						Color cm\r/4,cm\g/4,cm\b/4
						Rect x+3*MenuScale,TempY-2*MenuScale,width-30*MenuScale,24*MenuScale,True
					EndIf
					Color cm\r,cm\g,cm\b
					If cm\isCommand Then
						Text(x + 20*MenuScale, TempY+5*MenuScale, "> "+cm\txt)
					Else
						Text(x + 20*MenuScale, TempY+5*MenuScale, cm\txt)
					EndIf
				EndIf
				TempY = TempY - 15*MenuScale
			EndIf
			
		Next
		
		Color 255,255,255
		
		If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
	End If
	
	SetFont Font1
	
End Function

ConsoleR = 0 : ConsoleG = 255 : ConsoleB = 255
CreateConsoleMsg("Console commands: ")
CreateConsoleMsg("  - teleport [room name]")
CreateConsoleMsg("  - godmode [on/off]")
CreateConsoleMsg("  - noclip [on/off]")
CreateConsoleMsg("  - noclipspeed [x] (default = 2.0)")
CreateConsoleMsg("  - wireframe [on/off]")
CreateConsoleMsg("  - debughud [on/off]")
CreateConsoleMsg("  - camerafog [near] [far]")
CreateConsoleMsg(" ")
CreateConsoleMsg("  - status")
CreateConsoleMsg("  - heal")
CreateConsoleMsg(" ")
CreateConsoleMsg("  - spawnitem [item name]")
CreateConsoleMsg(" ")
CreateConsoleMsg("  - 173speed [x] (default = 35)")
CreateConsoleMsg("  - disable173/enable173")
CreateConsoleMsg("  - disable106/enable106")
CreateConsoleMsg("  - 173state/106state/096state")
CreateConsoleMsg("  - spawn [npc type]")

;---------------------------------------------------------------------------------------------------

Global DebugHUD%

Global BlurVolume#, BlurTimer#

Global LightBlink#, LightFlash#

Global BumpEnabled% = GetOptionInt("graphics", "bump mapping enabled")
Global HUDenabled% = GetOptionInt("graphics", "HUD enabled")

Global Camera%, CameraShake#, CurrCameraZoom#

Global Brightness%
Global CameraFogNear#
Global CameraFogFar#

Global StoredCameraFogFar# = CameraFogFar

Global MouseSens# = GetOptionFloat("controls", "mouse sensitivity")

Global EnableVRam% = GetOptionInt("graphics", "enable vram")

Include "dreamfilter.bb"

Dim LightSpriteTex(10)

;----------------------------------------------  Sounds -----------------------------------------------------

;[Block]

Global SoundEmitter%
Global TempSounds%[10]
Global TempSoundCHN%
Global TempSoundIndex% = 0

;The Music now has to be pre-defined, as the new system uses streaming instead of the usual sound loading system Blitz3D has
Dim Music$(40)
Music(0) = "The Dread"
Music(1) = "HeavyContainment"
Music(2) = "EntranceZone"
Music(3) = "PD"
Music(4) = "079"
Music(5) = "GateB1"
Music(6) = "GateB2"
Music(7) = "Room3Storage"
Music(8) = "Room049"
Music(9) = "8601"
Music(10) = "106"
Music(11) = "Menu"
Music(12) = "8601Cancer"
Music(13) = "Intro"
Music(14) = "178"
Music(15) = "PDTrench"
Music(16) = "205"
Music(17) = "GateA"
Music(18) = "1499"
Music(19) = "1499Danger"
Music(20) = "049Chase"
Music(21) = "..\Ending\MenuBreath"
Music(22) = "914"
Music(23) = "Ending"
Music(24) = "Credits"
Music(25) = "SaveMeFrom"

Global MusicVolume# = GetOptionFloat("audio", "music volume")
;Global MusicCHN% = StreamSound_Strict("SFX\Music\"+Music(2)+".ogg", MusicVolume, CurrMusicStream)

Global CurrMusicStream, MusicCHN
MusicCHN = StreamSound_Strict("SFX\Music\"+Music(2)+".ogg",MusicVolume)

Global CurrMusicVolume# = 1.0, NowPlaying%=2, ShouldPlay%=11
Global CurrMusic% = 1

DrawLoading(10, True)

Dim OpenDoorSFX%(3,3), CloseDoorSFX%(3,3)

Global KeyCardSFX1 
Global KeyCardSFX2 
Global ButtonSFX2 
Global ScannerSFX1
Global ScannerSFX2 

Global OpenDoorFastSFX
Global CautionSFX% 

Global NuclearSirenSFX%

Global CameraSFX  

Global StoneDragSFX% 

Global GunshotSFX% 
Global Gunshot2SFX% 
Global Gunshot3SFX% 
Global BullethitSFX% 

Global TeslaIdleSFX 
Global TeslaActivateSFX 
Global TeslaPowerUpSFX 

Global MagnetUpSFX%, MagnetDownSFX
Global FemurBreakerSFX%
Global EndBreathCHN%
Global EndBreathSFX%

Dim DecaySFX%(5)

Global BurstSFX 

DrawLoading(20, True)

Dim RustleSFX%(3)

Global Use914SFX%
Global Death914SFX% 

Dim DripSFX%(4)

Global LeverSFX%, LightSFX% 
Global ButtGhostSFX% 

Dim RadioSFX(5,10) 

Global RadioSquelch 
Global RadioStatic 
Global RadioBuzz 

Global ElevatorBeepSFX, ElevatorMoveSFX  

Dim PickSFX%(10)

Global AmbientSFXCHN%, CurrAmbientSFX%
Dim AmbientSFXAmount(6)
;0 = light containment, 1 = heavy containment, 2 = entrance
AmbientSFXAmount(0)=8 : AmbientSFXAmount(1)=11 : AmbientSFXAmount(2)=12
;3 = general, 4 = pre-breach
AmbientSFXAmount(3)=15 : AmbientSFXAmount(4)=5
;5 = forest
AmbientSFXAmount(5)=10

Dim AmbientSFX%(6, 15)

Dim OldManSFX%(8)

Dim Scp173SFX%(3)

Dim HorrorSFX%(20)


DrawLoading(25, True)

Dim IntroSFX%(20)

;IntroSFX(13) = LoadSound_Strict("SFX\intro\shoot1.ogg")
;IntroSFX(14) = LoadSound_Strict("SFX\intro\shoot2.ogg")


Dim AlarmSFX%(5)

Dim CommotionState%(25)

Global HeartBeatSFX 

Global VomitSFX%

Dim BreathSFX(2,5)
Global BreathCHN%


Dim NeckSnapSFX(3)

Dim DamageSFX%(9)

Dim MTFSFX%(8)

Dim CoughSFX%(3)
Global CoughCHN%, VomitCHN%

Global MachineSFX% 
Global ApacheSFX
Global CurrStepSFX
Dim StepSFX%(5, 2, 8) ;(normal/metal, walk/run, id)

Dim Step2SFX(6)

DrawLoading(30, True)

;[End block]

;New Sounds and Meshes/Other things in SCP:CB 1.3 - ENDSHN
;[Block]
;Global NTF_1499EnterSFX% = LoadSound_Strict("SFX\SCP\1499\Enter.ogg")
;Global NTF_1499LeaveSFX% = LoadSound_Strict("SFX\SCP\1499\Exit.ogg")

Global PlayCustomMusic% = False, CustomMusic% = 0

Global Monitor2, Monitor3, MonitorTexture2, MonitorTexture3, MonitorTexture4, MonitorTextureOff
Global MonitorTimer# = 0.0, MonitorTimer2# = 0.0, UpdateCheckpoint1%, UpdateCheckpoint2%

;This variable is for when a camera detected the player
	;False: Player is not seen (will be set after every call of the Main Loop
	;True: The Player got detected by a camera
Global PlayerDetected%
Global PrevInjuries#,PrevBloodloss#
Global NoTarget% = False

Global GuaranteedOmni% = False

Global NVGImages = LoadAnimImage("GFX\battery.png",64,64,0,2)
MaskImage NVGImages,255,0,255

Global Wearing1499% = False
Global AmbientLight%, AmbientLightNVG%
Global AmbientLightRoomTex%, AmbientLightRoomVal%

Global EnableUserTracks% = GetOptionInt("audio", "enable user tracks")
Global UserTrackMode% = GetOptionInt("audio", "user track setting")
Global UserTrackCheck% = 0, UserTrackCheck2% = 0
Global UserTrackMusicAmount% = 0, CurrUserTrack%, UserTrackFlag% = False
Dim UserTrackName$(256)

Global NTF_1499PrevX#
Global NTF_1499PrevY#
Global NTF_1499PrevZ#
Global NTF_1499PrevRoom.Rooms
Global NTF_1499X#
Global NTF_1499Y#
Global NTF_1499Z#
Global NTF_1499Sky%

Global OptionsMenu% = 0
Global QuitMSG% = 0

Global InFacility% = True

Global PrevSFXVolume# = SFXVolume#
Global DeafPlayer% = False
Global DeafTimer# = 0.0

Global IsZombie% = False

Global room2gw_brokendoor% = False
Global room2gw_x# = 0.0
Global room2gw_z# = 0.0

Global menuroomscale# = 8.0 / 2048.0

Global ParticleAmount% = GetOptionInt("graphics","particle amount")

Dim NavImages(5)
For i = 0 To 3
	NavImages(i) = LoadImage_Strict("GFX\navigator\roomborder"+i+".png")
	MaskImage NavImages(i),255,0,255
Next
NavImages(4) = LoadImage_Strict("GFX\navigator\batterymeter.png")

Global NavBG = CreateImage(GraphicWidth,GraphicHeight)

Global LightConeModel

Global ParticleEffect[10]

Const MaxDTextures=9
Global DTextures[MaxDTextures]

Global NPC049OBJ, NPC0492OBJ
Global ClerkOBJ

Global IntercomStreamCHN%

Global ForestNPC,ForestNPCTex,ForestNPCData#[3]
;[End Block]

;-----------------------------------------  Images ----------------------------------------------------------

Global PauseMenuIMG%

Global SprintIcon%
Global BlinkIcon%
Global CrouchIcon%
Global HandIcon%
Global HandIcon2%

Global StaminaMeterIMG%

Global Panel294, Using294%, Input294$

DrawLoading(35, True)

;----------------------------------------------  Items  -----------------------------------------------------

Include "Items.bb"

;--------------------------------------- Particles ------------------------------------------------------------

Include "Particles.bb"

;-------------------------------------  Doors --------------------------------------------------------------

Global ClosestButton%, ClosestDoor.Doors
Global SelectedDoor.Doors, UpdateDoorsTimer#
Global DoorTempID%
Type Doors
	Field obj%, obj2%, frameobj%, buttons%[2]
	Field locked%, open%, angle%, openstate#, fastopen%
	Field dir%
	Field timer%, timerstate#
	Field KeyCard%
	Field room.Rooms
	
	Field DisableWaypoint%
	
	Field dist#
	
	Field SoundCHN%
	
	Field Code$
	
	Field ID%
	
	Field Level%
	Field LevelDest%
	
	Field AutoClose%
	
	Field LinkedDoor.Doors
	
	Field IsElevatorDoor% = False
	
	Field MTFClose% = True
	Field NPCCalledElevator% = False
	
	Field DoorHitOBJ%
End Type 

Dim BigDoorOBJ(2), HeavyDoorObj(2)
Dim OBJTunnel(7)

Function CreateDoor.Doors(lvl, x#, y#, z#, angle#, room.Rooms, dopen% = False,  big% = False, keycard% = False, code$="", useCollisionMesh% = False)
	Local d.Doors, parent, i%
	If room <> Null Then parent = room\obj
	
	Local d2.Doors
	
	d.Doors = New Doors
	If big=1 Then
		d\obj = CopyEntity(BigDoorOBJ(0))
		ScaleEntity(d\obj, 55 * RoomScale, 55 * RoomScale, 55 * RoomScale)
		d\obj2 = CopyEntity(BigDoorOBJ(1))
		ScaleEntity(d\obj2, 55 * RoomScale, 55 * RoomScale, 55 * RoomScale)
		
		d\frameobj = CopyEntity(DoorColl)	;CopyMesh				
		ScaleEntity(d\frameobj, RoomScale, RoomScale, RoomScale)
		EntityType d\frameobj, HIT_MAP
		EntityAlpha d\frameobj, 0.0
	ElseIf big=2 Then
		d\obj = CopyEntity(HeavyDoorObj(0))
		ScaleEntity(d\obj, RoomScale, RoomScale, RoomScale)
		d\obj2 = CopyEntity(HeavyDoorObj(1))
		ScaleEntity(d\obj2, RoomScale, RoomScale, RoomScale)
		
		d\frameobj = CopyEntity(DoorFrameOBJ)
	ElseIf big=3 Then
		For d2 = Each Doors
			If d2 <> d And d2\dir = 3 Then
				d\obj = CopyEntity(d2\obj)
				d\obj2 = CopyEntity(d2\obj2)
				ScaleEntity d\obj, RoomScale, RoomScale, RoomScale
				ScaleEntity d\obj2, RoomScale, RoomScale, RoomScale
				Exit
			EndIf
		Next
		If d\obj=0 Then
			d\obj = LoadMesh_Strict("GFX\map\elevatordoor.b3d")
			d\obj2 = CopyEntity(d\obj)
			ScaleEntity d\obj, RoomScale, RoomScale, RoomScale
			ScaleEntity d\obj2, RoomScale, RoomScale, RoomScale
		EndIf
		d\frameobj = CopyEntity(DoorFrameOBJ)
	Else
		d\obj = CopyEntity(DoorOBJ)
		ScaleEntity(d\obj, (204.0 * RoomScale) / MeshWidth(d\obj), 312.0 * RoomScale / MeshHeight(d\obj), 16.0 * RoomScale / MeshDepth(d\obj))
		
		d\frameobj = CopyEntity(DoorFrameOBJ)
		d\obj2 = CopyEntity(DoorOBJ)
		
		ScaleEntity(d\obj2, (204.0 * RoomScale) / MeshWidth(d\obj), 312.0 * RoomScale / MeshHeight(d\obj), 16.0 * RoomScale / MeshDepth(d\obj))
		;entityType d\obj2, HIT_MAP
	End If
	
	;scaleentity(d\obj, 0.1, 0.1, 0.1)
	PositionEntity d\frameobj, x, y, z	
	ScaleEntity(d\frameobj, (8.0 / 2048.0), (8.0 / 2048.0), (8.0 / 2048.0))
	EntityPickMode d\frameobj,2
	EntityType d\obj, HIT_MAP
	EntityType d\obj2, HIT_MAP
	
	d\ID = DoorTempID
	DoorTempID=DoorTempID+1
	
	d\KeyCard = keycard
	d\Code = code
	
	d\Level = lvl
	d\LevelDest = 66
	
	For i% = 0 To 1
		If code <> "" Then 
			d\buttons[i]= CopyEntity(ButtonCodeOBJ)
			EntityFX(d\buttons[i], 1)
		Else
			If keycard>0 Then
				d\buttons[i]= CopyEntity(ButtonKeyOBJ)
			ElseIf keycard<0
				d\buttons[i]= CopyEntity(ButtonScannerOBJ)	
			Else
				d\buttons[i] = CopyEntity(ButtonOBJ)
			End If
		EndIf
		
		ScaleEntity(d\buttons[i], 0.03, 0.03, 0.03)
	Next
	
	If big=1 Then
		PositionEntity d\buttons[0], x - 432.0 * RoomScale, y + 0.7, z + 192.0 * RoomScale
		PositionEntity d\buttons[1], x + 432.0 * RoomScale, y + 0.7, z - 192.0 * RoomScale
		RotateEntity d\buttons[0], 0, 90, 0
		RotateEntity d\buttons[1], 0, 270, 0
	Else
		PositionEntity d\buttons[0], x + 0.6, y + 0.7, z - 0.1
		PositionEntity d\buttons[1], x - 0.6, y + 0.7, z + 0.1
		RotateEntity d\buttons[1], 0, 180, 0		
	End If
	EntityParent(d\buttons[0], d\frameobj)
	EntityParent(d\buttons[1], d\frameobj)
	EntityPickMode(d\buttons[0], 2)
	EntityPickMode(d\buttons[1], 2)
	
	PositionEntity d\obj, x, y, z
	
	RotateEntity d\obj, 0, angle, 0
	RotateEntity d\frameobj, 0, angle, 0
	
	If d\obj2 <> 0 Then
		PositionEntity d\obj2, x, y, z
		If big=1 Then
			RotateEntity(d\obj2, 0, angle, 0)
		Else
			RotateEntity(d\obj2, 0, angle + 180, 0)
		EndIf
		EntityParent(d\obj2, parent)
	EndIf
	
	EntityParent(d\frameobj, parent)
	EntityParent(d\obj, parent)
	
	d\angle = angle
	d\open = dopen		
	
	EntityPickMode(d\obj, 2)
	If d\obj2 <> 0 Then
		EntityPickMode(d\obj2, 2)
	EndIf
	
	EntityPickMode d\frameobj,2
	
	If d\open And big = False And Rand(8) = 1 Then d\AutoClose = True
	d\dir=big
	d\room=room
	
	d\MTFClose = True
	
	If useCollisionMesh Then
		For d2.Doors = Each Doors
			If d2 <> d Then
				If d2\DoorHitOBJ <> 0 Then
					d\DoorHitOBJ = CopyEntity(d2\DoorHitOBJ,d\frameobj)
					EntityAlpha d\DoorHitOBJ,0.0
					EntityFX d\DoorHitOBJ,1
					EntityType d\DoorHitOBJ,HIT_MAP
					EntityColor d\DoorHitOBJ,255,0,0
					HideEntity d\DoorHitOBJ
					Exit
				EndIf
			EndIf
		Next
		If d\DoorHitOBJ=0 Then
			d\DoorHitOBJ = LoadMesh_Strict("GFX\doorhit.b3d",d\frameobj)
			EntityAlpha d\DoorHitOBJ,0.0
			EntityFX d\DoorHitOBJ,1
			EntityType d\DoorHitOBJ,HIT_MAP
			EntityColor d\DoorHitOBJ,255,0,0
			HideEntity d\DoorHitOBJ
		EndIf
	EndIf
	
	Return d
	
End Function

Function CreateButton(x#,y#,z#, pitch#,yaw#,roll#=0)
	Local obj = CopyEntity(ButtonOBJ)	
	
	ScaleEntity(obj, 0.03, 0.03, 0.03)
	
	PositionEntity obj, x,y,z
	RotateEntity obj, pitch,yaw,roll
	
	EntityPickMode(obj, 2)	
	
	Return obj
End Function

Function UpdateDoors()
	
	Local i%, d.Doors, x#, z#, dist#
	If UpdateDoorsTimer =< 0 Then
		For d.Doors = Each Doors
			Local xdist# = Abs(EntityX(Collider)-EntityX(d\obj,True))
			Local zdist# = Abs(EntityZ(Collider)-EntityZ(d\obj,True))
			
			d\dist = xdist+zdist
			
			If d\dist > HideDistance*2 Then
				If d\obj <> 0 Then HideEntity d\obj
				If d\frameobj <> 0 Then HideEntity d\frameobj
				If d\obj2 <> 0 Then HideEntity d\obj2
				If d\buttons[0] <> 0 Then HideEntity d\buttons[0]
				If d\buttons[1] <> 0 Then HideEntity d\buttons[1]				
			Else
				If d\obj <> 0 Then ShowEntity d\obj
				If d\frameobj <> 0 Then ShowEntity d\frameobj
				If d\obj2 <> 0 Then ShowEntity d\obj2
				If d\buttons[0] <> 0 Then ShowEntity d\buttons[0]
				If d\buttons[1] <> 0 Then ShowEntity d\buttons[1]
			EndIf
			
			If PlayerRoom\RoomTemplate\Name$ = "room2sl"
				If ValidRoom2slCamRoom(d\room)
					If d\obj <> 0 Then ShowEntity d\obj
					If d\frameobj <> 0 Then ShowEntity d\frameobj
					If d\obj2 <> 0 Then ShowEntity d\obj2
					If d\buttons[0] <> 0 Then ShowEntity d\buttons[0]
					If d\buttons[1] <> 0 Then ShowEntity d\buttons[1]
				EndIf
			EndIf
		Next
		
		UpdateDoorsTimer = 30
	Else
		UpdateDoorsTimer = Max(UpdateDoorsTimer-FPSfactor,0)
	EndIf
	
	ClosestButton = 0
	ClosestDoor = Null
	
	For d.Doors = Each Doors
		If d\dist < HideDistance*2 Or d\IsElevatorDoor>0 Then ;Make elevator doors update everytime because if not, this can cause a bug where the elevators suddenly won't work, most noticeable in room2tunnel - ENDSHN
			
			If (d\openstate >= 180 Or d\openstate <= 0) And GrabbedEntity = 0 Then
				For i% = 0 To 1
					If d\buttons[i] <> 0 Then
						If Abs(EntityX(Collider)-EntityX(d\buttons[i],True)) < 1.0 Then 
							If Abs(EntityZ(Collider)-EntityZ(d\buttons[i],True)) < 1.0 Then 
								dist# = Distance(EntityX(Collider, True), EntityZ(Collider, True), EntityX(d\buttons[i], True), EntityZ(d\buttons[i], True));entityDistance(collider, d\buttons[i])
								If dist < 0.7 Then
									Local temp% = CreatePivot()
									PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
									PointEntity temp,d\buttons[i]
									
									If EntityPick(temp, 0.6) = d\buttons[i] Then
										If ClosestButton = 0 Then
											ClosestButton = d\buttons[i]
											ClosestDoor = d
										Else
											If dist < EntityDistance(Collider, ClosestButton) Then ClosestButton = d\buttons[i] : ClosestDoor = d
										End If							
									End If
									
									FreeEntity temp
									
								EndIf							
							EndIf
						EndIf
						
					EndIf
				Next
			EndIf
			
			If d\open Then
				If d\openstate < 180 Then
					Select d\dir
						Case 0
							d\openstate = Min(180, d\openstate + FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * (d\fastopen*2+1) * FPSfactor / 80.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate)* (d\fastopen+1) * FPSfactor / 80.0, 0, 0)		
						Case 1
							d\openstate = Min(180, d\openstate + FPSfactor * 0.8)
							MoveEntity(d\obj, Sin(d\openstate) * FPSfactor / 180.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, -Sin(d\openstate) * FPSfactor / 180.0, 0, 0)
						Case 2
							d\openstate = Min(180, d\openstate + FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * (d\fastopen+1) * FPSfactor / 85.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate)* (d\fastopen*2+1) * FPSfactor / 120.0, 0, 0)
						Case 3
							d\openstate = Min(180, d\openstate + FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * (d\fastopen*2+1) * FPSfactor / 162.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate)* (d\fastopen*2+1) * FPSfactor / 162.0, 0, 0)
						Case 4 ;Used for 914 only
							d\openstate = Min(180, d\openstate + FPSfactor * 1.4)
							MoveEntity(d\obj, Sin(d\openstate) * FPSfactor / 114.0, 0, 0)
					End Select
				Else
					d\fastopen = 0
					ResetEntity(d\obj)
					If d\obj2 <> 0 Then ResetEntity(d\obj2)
					If d\timerstate > 0 Then
						d\timerstate = Max(0, d\timerstate - FPSfactor)
						If d\timerstate + FPSfactor > 110 And d\timerstate <= 110 Then d\SoundCHN = PlaySound2(CautionSFX, Camera, d\obj)
						;If d\timerstate = 0 Then d\open = (Not d\open) : PlaySound2(CloseDoorSFX(Min(d\dir,1),Rand(0, 2)), Camera, d\obj)
						Local sound%
						If d\dir = 1 Then sound% = Rand(0, 1) Else sound% = Rand(0, 2)
						If d\timerstate = 0 Then d\open = (Not d\open) : d\SoundCHN = PlaySound2(CloseDoorSFX(d\dir,sound%), Camera, d\obj)
					EndIf
					If d\AutoClose And RemoteDoorOn = True Then
						If EntityDistance(Camera, d\obj) < 2.1 Then
							If (Not Wearing714) Then PlaySound_Strict HorrorSFX(7)
							d\open = False : d\SoundCHN = PlaySound2(CloseDoorSFX(Min(d\dir,1), Rand(0, 2)), Camera, d\obj) : d\AutoClose = False
						EndIf
					EndIf				
				EndIf
			Else
				If d\openstate > 0 Then
					Select d\dir
						Case 0
							d\openstate = Max(0, d\openstate - FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor * (d\fastopen+1) / 80.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * (d\fastopen+1) * -FPSfactor / 80.0, 0, 0)	
						Case 1
							d\openstate = Max(0, d\openstate - FPSfactor*0.8)
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor / 180.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * FPSfactor / 180.0, 0, 0)
							If ParticleAmount=2 And d\openstate < 15 And d\openstate+FPSfactor => 15
								Local particles% = SetEmitter(d\frameobj, ParticleEffect[3])
								EntityOrder(particles, -1)
							EndIf
						Case 2
							d\openstate = Max(0, d\openstate - FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor * (d\fastopen+1) / 85.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * (d\fastopen+1) * -FPSfactor / 120.0, 0, 0)
						Case 3
							d\openstate = Max(0, d\openstate - FPSfactor * 2 * (d\fastopen+1))
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor * (d\fastopen+1) / 162.0, 0, 0)
							If d\obj2 <> 0 Then MoveEntity(d\obj2, Sin(d\openstate) * (d\fastopen+1) * -FPSfactor / 162.0, 0, 0)
						Case 4 ;Used for 914 only
							d\openstate = Min(180, d\openstate - FPSfactor * 1.4)
							MoveEntity(d\obj, Sin(d\openstate) * -FPSfactor / 114.0, 0, 0)
					End Select
					
					If d\angle = 0 Or d\angle=180 Then
						If Abs(EntityZ(d\frameobj, True)-EntityZ(Collider))<0.15 Then
							If Abs(EntityX(d\frameobj, True)-EntityX(Collider))<0.7*(d\dir*2+1) Then
								z# = CurveValue(EntityZ(d\frameobj,True)+0.15*Sgn(EntityZ(Collider)-EntityZ(d\frameobj, True)), EntityZ(Collider), 5)
								PositionEntity Collider, EntityX(Collider), EntityY(Collider), z
							EndIf
						EndIf
					Else
						If Abs(EntityX(d\frameobj, True)-EntityX(Collider))<0.15 Then	
							If Abs(EntityZ(d\frameobj, True)-EntityZ(Collider))<0.7*(d\dir*2+1) Then
								x# = CurveValue(EntityX(d\frameobj,True)+0.15*Sgn(EntityX(Collider)-EntityX(d\frameobj, True)), EntityX(Collider), 5)
								PositionEntity Collider, x, EntityY(Collider), EntityZ(Collider)
							EndIf
						EndIf
					EndIf
					
					If d\DoorHitOBJ <> 0 Then
						ShowEntity d\DoorHitOBJ
					EndIf
				Else
					d\fastopen = 0
					PositionEntity(d\obj, EntityX(d\frameobj, True), EntityY(d\frameobj, True), EntityZ(d\frameobj, True))
					If d\obj2 <> 0 Then PositionEntity(d\obj2, EntityX(d\frameobj, True), EntityY(d\frameobj, True), EntityZ(d\frameobj, True))
					If d\obj2 <> 0 And d\dir = 0 Then
						MoveEntity(d\obj, 0, 0, 8.0 * RoomScale)
						MoveEntity(d\obj2, 0, 0, 8.0 * RoomScale)
					EndIf
					If d\DoorHitOBJ <> 0 Then
						HideEntity d\DoorHitOBJ
					EndIf
				EndIf
			EndIf
			
		EndIf
		UpdateSoundOrigin(d\SoundCHN,Camera,d\frameobj)
		
		If d\DoorHitOBJ<>0 Then
			If DebugHUD Then
				EntityAlpha d\DoorHitOBJ,0.5
			Else
				EntityAlpha d\DoorHitOBJ,0.0
			EndIf
		EndIf
	Next
End Function

Global ElevatorButtonLastPressMillis%
Global ElevatorButtonSpamCount%

Function UseDoor(d.Doors, showmsg%=True, playsfx%=True)
	Local temp% = 0
	If d\KeyCard > 0 Then
		If SelectedItem = Null Then
			If showmsg = True Then
				If Not HasKeyCardMsg() Then
					Msg = I_Loc\MessageButton_KeyRequired
					MsgTimer = 70 * 7
					ActiveKeyCardMsgCooldown()
				EndIf
			EndIf
			Return
		Else
			Select SelectedItem\itemtemplate\name
				Case "key1"
					temp = 1
				Case "key2"
					temp = 2
				Case "key3"
					temp = 3
				Case "key4"
					temp = 4
				Case "key5"
					temp = 5
				Case "key6"
					temp = 6
				Default 
					temp = -1
			End Select
			
			If temp =-1 Then 
				If showmsg = True Then
					If Not HasKeyCardMsg() Then
						Msg = I_Loc\MessageButton_KeyRequired
						MsgTimer = 70 * 7
						ActiveKeyCardMsgCooldown()
					EndIf
				EndIf
				Return				
			ElseIf temp >= d\KeyCard 
				SelectedItem = Null
				If showmsg = True Then
					If d\locked Then
						PlaySound_Strict KeyCardSFX2
						Msg = I_Loc\MessageButton_KeyNothing
						MsgTimer = 70 * 7
						Return
					Else
						PlaySound_Strict KeyCardSFX1
						Msg = I_Loc\MessageButton_KeyInserted
						MsgTimer = 70 * 7	
					EndIf
					ActiveKeyCardMsgCooldown()
				EndIf
			Else
				SelectedItem = Null
				If showmsg = True Then 
					PlaySound_Strict KeyCardSFX2					
					If d\locked Then
						Msg = I_Loc\MessageButton_KeyNothing
					Else
						Msg = Format(I_Loc\MessageButton_KeyRequiredLevel, d\KeyCard)
					EndIf
					MsgTimer = 70 * 7					
					ActiveKeyCardMsgCooldown()
				EndIf
				Return
			End If
		EndIf	
	ElseIf d\KeyCard < 0
		;I can't find any way to produce short circuited boolean expressions so work around this by using a temporary variable - risingstar64
		If SelectedItem <> Null Then
			temp = (SelectedItem\itemtemplate\name = "hand" And d\KeyCard=-1) Or (SelectedItem\itemtemplate\name = "hand2" And d\KeyCard=-2)
		EndIf
		SelectedItem = Null
		If temp <> 0 Then
			PlaySound_Strict ScannerSFX1
			Msg = I_Loc\MessageButton_DnaSuccess
			MsgTimer = 70 * 10
			ActivateScannerMsgCooldown()
		Else
			If showmsg = True And (Not HasScannerMsg()) Then 
				PlaySound_Strict ScannerSFX2
				Msg = I_Loc\MessageButton_DnaSelf
				MsgTimer = 70 * 10
			EndIf
			Return			
		EndIf
	Else
		If d\locked Then
			If showmsg = True Then 
				If Not (d\IsElevatorDoor>0) Then
					PlaySound_Strict ButtonSFX2
					If PlayerRoom\RoomTemplate\Name <> "room2elevator" Then
                        If d\open Then
                            Msg = I_Loc\MessageButton_Nothing
                        Else    
                            Msg = I_Loc\MessageButton_DoorLocked
                        EndIf    
                    Else
                        Msg = I_Loc\MessageButton_ElevatorBroken
                    EndIf
					MsgTimer = 70 * 5
				Else
					If d\IsElevatorDoor <> 3 Then
						Local now% = MilliSecs()
						If now - ElevatorButtonLastPressMillis > 200 Then
							ElevatorButtonSpamCount = Max(0, ElevatorButtonSpamCount - (now - ElevatorButtonLastPressMillis) / 200)
						Else
							ElevatorButtonSpamCount = ElevatorButtonSpamCount + 1
							If ElevatorButtonSpamCount >= 30 Then api_MessageBox(api_GetActiveWindow(), "Memory Access Violation!" + Chr(10) + "The program attempted to read or write to a protected memory address.", "I warned you!", 0)
						EndIf
						ElevatorButtonLastPressMillis = now
					EndIf

					If d\IsElevatorDoor = 1 Then
						Msg = I_Loc\MessageButton_ElevatorCall
						MsgTimer = 70 * 5
					ElseIf d\IsElevatorDoor = 3 Then
						Msg = I_Loc\MessageButton_ElevatorFloor
						MsgTimer = 70 * 5
					ElseIf (Msg<>I_Loc\MessageButton_ElevatorCall)
						If (Msg=I_Loc\MessageButton_ElevatorAlready) Or (MsgTimer<70*3) Or (ElevatorButtonSpamCount > 20 And Msg <> I_Loc\MessageButton_ElevatorMav)
							Local rnd%
							If ElevatorButtonSpamCount > 20 Then
								rnd = 3
							Else
								rnd = Rand(10)
							EndIf
							Select rnd
								Case 1
									Msg = I_Loc\MessageButton_ElevatorStop
									MsgTimer = 70 * 7
								Case 2
									Msg = I_Loc\MessageButton_ElevatorFaster
									MsgTimer = 70 * 7
								Case 3
									Msg = I_Loc\MessageButton_ElevatorMav
									MsgTimer = 70 * 7
								Default
									Msg = I_Loc\MessageButton_ElevatorAlready
									MsgTimer = 70 * 7
							End Select
						EndIf
					Else
						Msg = I_Loc\MessageButton_ElevatorAlready
						MsgTimer = 70 * 7
					EndIf
				EndIf
				
			EndIf
			Return
		EndIf	
	EndIf
	
	d\open = (Not d\open)
	If d\LinkedDoor <> Null Then d\LinkedDoor\open = (Not d\LinkedDoor\open)
	
	Local sound = 0
	;If d\dir = 1 Then sound = 0 Else sound=Rand(0, 2)
	If d\dir = 1 Then sound=Rand(0, 1) Else sound=Rand(0, 2)
	
	If playsfx=True Then
		If d\open Then
			If d\LinkedDoor <> Null Then d\LinkedDoor\timerstate = d\LinkedDoor\timer
			d\timerstate = d\timer
			d\SoundCHN = PlaySound2 (OpenDoorSFX(d\dir, sound), Camera, d\obj)
		Else
			d\SoundCHN = PlaySound2 (CloseDoorSFX(d\dir, sound), Camera, d\obj)
		EndIf
		UpdateSoundOrigin(d\SoundCHN,Camera,d\obj)
	Else
		If d\open Then
			If d\LinkedDoor <> Null Then d\LinkedDoor\timerstate = d\LinkedDoor\timer
			d\timerstate = d\timer
		EndIf
	EndIf
	
End Function


Global KeyCardMsgCooldown% = 0
Function ActiveKeyCardMsgCooldown()
	KeyCardMsgCooldown = MilliSecs() + 4000
End Function

Function HasKeyCardMsg()
	Return MilliSecs() <= KeyCardMsgCooldown
End Function

Global ScannerMsgCooldown% = 0
Function ActivateScannerMsgCooldown()
	ScannerMsgCooldown = MilliSecs() + 7000
End Function

Function HasScannerMsg()
	Return MilliSecs() <= ScannerMsgCooldown
End Function

Function RemoveDoor(d.Doors)
	If d\buttons[0] <> 0 Then EntityParent d\buttons[0], 0
	If d\buttons[1] <> 0 Then EntityParent d\buttons[1], 0	
	
	If d\obj <> 0 Then FreeEntity d\obj
	If d\obj2 <> 0 Then FreeEntity d\obj2
	If d\frameobj <> 0 Then FreeEntity d\frameobj
	If d\buttons[0] <> 0 Then FreeEntity d\buttons[0]
	If d\buttons[1] <> 0 Then FreeEntity d\buttons[1]
	
	Delete d
End Function

DrawLoading(40,True)

Global DebugMapGen% = GetOptionInt("debug", "show map gen")
Global DebugForestGen% = GetOptionInt("debug", "show forest gen")

Include "MapSystem.bb"

DrawLoading(80,True)

Include "NPCs.bb"

;-------------------------------------  Events --------------------------------------------------------------

Type Events
	Field EventName$
	Field room.Rooms
	
	Field EventState#, EventState2#, EventState3#
	Field SoundCHN%, SoundCHN2%
	Field Sound, Sound2
	Field SoundCHN_isStream%, SoundCHN2_isStream%
	
	Field EventStr$
	
	Field img%
End Type 

Function CreateEvent.Events(eventname$, roomname$, id%, prob# = 0.0)
	;roomname = the name of the room(s) you want the event to be assigned to
	
	;the id-variable determines which of the rooms the event is assigned to,
	;0 will assign it to the first generated room, 1 to the second, etc
	
	;the prob-variable can be used to randomly assign events into some rooms
	;0.5 means that there's a 50% chance that event is assigned to the rooms
	;1.0 means that the event is assigned to every room
	;the id-variable is ignored if prob <> 0.0
	
	Local i% = 0, temp%, e.Events, e2.Events, r.Rooms
	
	If prob = 0.0 Then
		For r.Rooms = Each Rooms
			If (roomname = "" Or roomname = r\RoomTemplate\Name) Then
				temp = False
				For e2.Events = Each Events
					If e2\room = r Then temp = True : Exit
				Next
				
				i=i+1
				If i >= id And temp = False Then
					e.Events = New Events
					e\EventName = eventname					
					e\room = r
					Return e
				End If
			EndIf
		Next
	Else
		For r.Rooms = Each Rooms
			If (roomname = "" Or roomname = r\RoomTemplate\Name) Then
				temp = False
				For e2.Events = Each Events
					If e2\room = r Then temp = True : Exit
				Next
				
				If Rnd(0.0, 1.0) < prob And temp = False Then
					e.Events = New Events
					e\EventName = eventname					
					e\room = r
				End If
			EndIf
		Next		
	EndIf
	
	Return Null
End Function

Function InitEvents()
	Local e.Events
	
	CreateEvent("173", "173", 0)
	CreateEvent("alarm", "start", 0)
	
	CreateEvent("pocketdimension", "pocketdimension", 0)	
	
	;there's a 7% chance that 106 appears in the rooms named "tunnel"
	CreateEvent("tunnel106", "tunnel", 0, 0.07 + (0.1*SelectedDifficulty\aggressiveNPCs))
	
	;the chance for 173 appearing in the first lockroom is about 66%
	;there's a 30% chance that it appears in the later lockrooms
	If Rand(3)<3 Then CreateEvent("lockroom173", "lockroom", 0)
	CreateEvent("lockroom173", "lockroom", 0, 0.3 + (0.5*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("lockroom173", "lockroom_ez", 0, 0.3 + (0.5*SelectedDifficulty\aggressiveNPCs))
	
	CreateEvent("room2trick", "room2", 0, 0.15)	
	
	CreateEvent("1048a", "room2", 0, 1.0)	
	
	CreateEvent("room2storage", "room2storage", 0)	
	
	;096 spawns in the first (and last) lockroom2
	CreateEvent("lockroom096", "lockroom2", 0)
	
	CreateEvent("endroom106", "endroom", Rand(0,1))
	
	CreateEvent("room2poffices2", "room2poffices2", 0)
	
	CreateEvent("room2fan", "room2_2", 0, 1.0)
	
	CreateEvent("room2elevator2", "room2elevator", 0)
	CreateEvent("room2elevator", "room2elevator", Rand(1,2))
	
	CreateEvent("room3storage", "room3storage", 0, 0)
	
	CreateEvent("tunnel2smoke", "tunnel2", 0, 0.2)
	CreateEvent("tunnel2", "tunnel2", Rand(0,2), 0)
	CreateEvent("tunnel2", "tunnel2", 0, (0.2*SelectedDifficulty\aggressiveNPCs))
	
	;173 appears in half of the "room2doors" -rooms
	CreateEvent("room2doors173", "room2doors", 0, 0.5 + (0.4*SelectedDifficulty\aggressiveNPCs))
	
	;the anomalous duck in room2offices2-rooms
	CreateEvent("room2offices2", "room2offices2", 0, 0.7)
	
	CreateEvent("room2closets", "room2closets", 0)	
	
	CreateEvent("room2cafeteria", "room2cafeteria", 0)	
	
	CreateEvent("room3pitduck", "room3pit", 0)
	CreateEvent("room3pit1048", "room3pit", 1)
	
	;the event that causes the door to open by itself in room2offices3
	CreateEvent("room2offices3", "room2offices3", 0, 1.0)	
	
	CreateEvent("room2servers", "room2servers", 0)	
	
	CreateEvent("room3servers", "room3servers", 0)	
	CreateEvent("room3servers", "room3servers2", 0)
	
	;the dead guard
	CreateEvent("room3tunnel","room3tunnel", 0, 0.08)
	
	CreateEvent("room4","room4", 0)
	
	If Rand(5)<5 Then 
		Select Rand(3)
			Case 1
				CreateEvent("682roar", "tunnel", Rand(0,2), 0)	
			Case 2
				CreateEvent("682roar", "room3pit", Rand(0,2), 0)		
			Case 3
				;CreateEvent("682roar", "room2offices", 0, 0)
				CreateEvent("682roar", "room2z3", 0, 0)
		End Select 
	EndIf 
	
	CreateEvent("testroom173", "room2testroom2", 0, 1.0)	
	
	CreateEvent("room2tesla", "room2tesla", 0, 0.9)
	
	CreateEvent("room2nuke", "room2nuke", 0, 0)
	
	If Rand(5) < 5 Then 
		CreateEvent("coffin106", "coffin", 0, 0)
	Else
		CreateEvent("coffin", "coffin", 0, 0)
	EndIf 
	
	CreateEvent("checkpoint", "checkpoint1", 0, 1.0)
	CreateEvent("checkpoint", "checkpoint2", 0, 1.0)
	
	CreateEvent("room3door", "room3", 0, 0.1)
	CreateEvent("room3door", "room3tunnel", 0, 0.1)	
	
	If Rand(2)=1 Then
		CreateEvent("106victim", "room3", Rand(1,2))
		CreateEvent("106sinkhole", "room3_2", Rand(2,3))
	Else
		CreateEvent("106victim", "room3_2", Rand(1,2))
		CreateEvent("106sinkhole", "room3", Rand(2,3))
	EndIf
	CreateEvent("106sinkhole", "room4", Rand(1,2))
	
	CreateEvent("room079", "room079", 0, 0)	
	
	CreateEvent("room049", "room049", 0, 0)
	
	CreateEvent("room012", "room012", 0, 0)
	
	CreateEvent("room035", "room035", 0, 0)
	
	CreateEvent("008", "008", 0, 0)
	
	CreateEvent("room106", "room106", 0, 0)	
	
	CreateEvent("pj", "roompj", 0, 0)
	
	CreateEvent("914", "914", 0, 0)
	
	CreateEvent("buttghost", "room2toilets", 0, 0)
	CreateEvent("toiletguard", "room2toilets", 1, 0)
	
	CreateEvent("room2pipes106", "room2pipes", Rand(0, 3)) 
	
	CreateEvent("room2pit", "room2pit", 0, 0.4 + (0.4*SelectedDifficulty\aggressiveNPCs))
	
	CreateEvent("testroom", "testroom", 0)
	
	CreateEvent("room2tunnel", "room2tunnel", 0)
	
	CreateEvent("room2ccont", "room2ccont", 0)
	
	CreateEvent("gateaentrance", "gateaentrance", 0)
	CreateEvent("gatea", "gatea", 0)	
	CreateEvent("exit1", "exit1", 0)
	
	CreateEvent("room205", "room205", 0)
	
	CreateEvent("room860","room860", 0)
	
	CreateEvent("room966","room966", 0)
	
	CreateEvent("room1123", "room1123", 0, 0)
	
	CreateEvent("room2tesla", "room2tesla_lcz", 0, 0.9)
	CreateEvent("room2tesla", "room2tesla_hcz", 0, 0.9)
	
	;New Events in SCP:CB Version 1.3 - ENDSHN
	CreateEvent("room4tunnels","room4tunnels",0)
	CreateEvent("room_gw","room2gw",0,1.0)
	CreateEvent("dimension1499","dimension1499",0)
	CreateEvent("room1162","room1162",0)
	CreateEvent("room2scps2","room2scps2",0)
	CreateEvent("room_gw","room3gw",0,1.0)
	CreateEvent("room2sl","room2sl",0)
	CreateEvent("medibay","medibay",0)
	CreateEvent("room2shaft","room2shaft",0)
	CreateEvent("room1lifts","room1lifts",0)
	
	CreateEvent("room2gw_b","room2gw_b",Rand(0,1))
	
	CreateEvent("096spawn","room4pit",0,0.6+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room3pit",0,0.6+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room2pipes",0,0.4+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room2pit",0,0.5+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room3tunnel",0,0.6+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room4tunnels",0,0.7+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","tunnel",0,0.6+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","tunnel2",0,0.4+(0.2*SelectedDifficulty\aggressiveNPCs))
	CreateEvent("096spawn","room3z2",0,0.7+(0.2*SelectedDifficulty\aggressiveNPCs))
	
	CreateEvent("room2pit","room2_4",0,0.4 + (0.4*SelectedDifficulty\aggressiveNPCs))
	
	CreateEvent("room2offices035","room2offices",0)
	
	CreateEvent("room2pit106", "room2pit", 0, 0.07 + (0.1*SelectedDifficulty\aggressiveNPCs))
	
	CreateEvent("room1archive", "room1archive", 0, 1.0)
	
End Function

Include "UpdateEvents.bb"

Function RemoveEvent(e.Events)
	If e\Sound<>0 Then FreeSound_Strict e\Sound
	If e\Sound2<>0 Then FreeSound_Strict e\Sound2
	If e\img<>0 Then FreeImage e\img
	Delete e
End Function

Collisions HIT_PLAYER, HIT_MAP, 2, 2
Collisions HIT_PLAYER, HIT_PLAYER, 1, 3
Collisions HIT_ITEM, HIT_MAP, 2, 2
Collisions HIT_APACHE, HIT_APACHE, 1, 2
Collisions HIT_178, HIT_MAP, 2, 2
Collisions HIT_178, HIT_178, 1, 3
Collisions HIT_DEAD, HIT_MAP, 2, 2

DrawLoading(90, True)

;----------------------------------- meshes and textures ----------------------------------------------------------------

Global FogTexture%, Fog%
Global GasMaskTexture%, GasMaskOverlay%
Global InfectTexture%, InfectOverlay%
Global DarkTexture%, Dark%
Global Collider%, Head%

Global FogNVTexture%
Global NVTexture%, NVOverlay%

Global TeslaTexture%

Global LightTexture%, Light%
Dim LightSpriteTex%(5)
Global DoorOBJ%, DoorFrameOBJ%

Global LeverOBJ%, LeverBaseOBJ%

Global DoorColl%
Global ButtonOBJ%, ButtonKeyOBJ%, ButtonCodeOBJ%, ButtonScannerOBJ%

Dim DecalTextures%(20)

Global Monitor%, MonitorTexture%
Global CamBaseOBJ%, CamOBJ%

Global LiquidObj%,MTFObj%,GuardObj%,ClassDObj%
Global ApacheObj%,ApacheRotorObj%

Global UnableToMove% = False
Global ShouldEntitiesFall% = True
Global PlayerFallingPickDistance# = 10.0

Global Save_MSG$ = ""
Global Save_MSG_Timer# = 0.0
Global Save_MSG_Y# = 0.0

Global MTF_CameraCheckTimer# = 0.0
Global MTF_CameraCheckDetected% = False

;---------------------------------------------------------------------------------------------------

Include "menu.bb"
MainMenuOpen = True

;---------------------------------------------------------------------------------------------------

FlushKeys()
FlushMouse()

DrawLoading(100, True)

LoopDelay = MilliSecs()

Global UpdateParticles_Time# = 0.0

Global CurrTrisAmount%

Global Input_ResetTime# = 0

Type SCP427
	Field Using%
	Field Timer#
	Field Sound[2]
	Field SoundCHN[2]
End Type

Global I_427.SCP427 = New SCP427

Type MapZones
	Field Transition%[2]
	Field HasCustomForest%
	Field HasCustomMT%
End Type

Global I_Zone.MapZones = New MapZones


;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------       		MAIN LOOP                 ---------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------

Global TotalVidMem = TotalVidMem()
Global TotalPhysMem = TotalPhys()

While IsRunning
	SetErrorMsg(5, "GPU: " + GFXDriverName + " (" + (TotalVidMem - (AvailVidMem() / 1024)) + "MB/" + TotalVidMem + " MB)")
	SetErrorMsg(6, "Global memory status: (" + (TotalPhysMem - (AvailPhys() / 1024)) + "MB/" + TotalPhysMem + " MB)")

	Cls
	
	CurTime = MilliSecs()

	Local ElapsedTime% = CurTime - PrevTime
	PrevTime = CurTime
	If (SpeedRunMode Lor (Not (MainMenuOpen Lor MenuOpen))) And SelectedEnding="" And TimerStopped=0 Then PlayTime = PlayTime + ElapsedTime
	PrevFPSFactor = FPSfactor
	FPSfactor = Min(ElapsedTime / 1000.0 * 70, 5.0)
	FPSfactor2 = FPSfactor

	If IsPaused() Then FPSfactor = 0
	
	If Framelimit > 0 Then
	    ;Framelimit
		Local WaitingTime% = (1000.0 / Framelimit) - (MilliSecs() - LoopDelay)
		Delay WaitingTime%
		
		LoopDelay = MilliSecs()
	EndIf
	
	;Counting the fps
	If CheckFPS < MilliSecs() Then
		FPS = ElapsedLoops
		ElapsedLoops = 0
		CheckFPS = MilliSecs()+1000
	EndIf
	ElapsedLoops = ElapsedLoops + 1
	
	If Input_ResetTime<=0.0
		DoubleClick = False
		MouseHit1 = MouseHit(1)
		If MouseHit1 Then
			If MilliSecs() - LastMouseHit1 < 800 And Abs(MouseX() - LastMouseHit1X) < 4 And Abs(MouseY() - LastMouseHit1Y) < 4 Then DoubleClick = True
			LastMouseHit1 = MilliSecs()
			LastMouseHit1X = MouseX()
			LastMouseHit1Y = MouseY()
		EndIf
		
		Local prevmousedown1 = MouseDown1
		MouseDown1 = MouseDown(1)
		If prevmousedown1 = True And MouseDown1=False Then MouseUp1 = True Else MouseUp1 = False
		
		MouseHit2 = MouseHit(2)
		
		If (Not MouseDown1) And (Not MouseHit1) Then GrabbedEntity = 0
	Else
		Input_ResetTime = Max(Input_ResetTime-FPSfactor,0.0)
	EndIf
	
	UpdateMusic()
	If EnableSFXRelease Then AutoReleaseSounds()
	
	If MainMenuOpen Then
		If ShouldPlay = 21 Then
			EndBreathSFX = LoadSound(DetermineModdedPath("SFX\Ending\MenuBreath.ogg"))
			EndBreathCHN = PlaySound(EndBreathSFX)
			ShouldPlay = 66
		ElseIf ShouldPlay = 66
			If (Not ChannelPlaying(EndBreathCHN)) Then
				FreeSound(EndBreathSFX)
				ShouldPlay = 11
			EndIf
		Else
			ShouldPlay = 11
		EndIf
		UpdateMainMenu()
	Else
		UpdateStreamSounds()
		
		ShouldPlay = Min(PlayerZone,2)
		
		DrawHandIcon = False
		
		RestoreSanity = True
		ShouldEntitiesFall = True
		
		If FPSfactor > 0 And PlayerRoom\RoomTemplate\Name <> "dimension1499" Then UpdateSecurityCams()
		
		If PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "gatea" And PlayerRoom\RoomTemplate\Name <> "exit1" And (Not IsAnyMenuOpen()) Then 
			
			If Rand(1500) = 1 Then
				For i = 0 To 5
					If AmbientSFX(i,CurrAmbientSFX)<>0 Then
						If ChannelPlaying(AmbientSFXCHN)=0 Then FreeSound_Strict AmbientSFX(i,CurrAmbientSFX) : AmbientSFX(i,CurrAmbientSFX) = 0
					EndIf			
				Next
				
				PositionEntity (SoundEmitter, EntityX(Camera) + Rnd(-1.0, 1.0), 0.0, EntityZ(Camera) + Rnd(-1.0, 1.0))
				
				If Rand(3)=1 Then PlayerZone = 3
				
				If PlayerRoom\RoomTemplate\Name = "173" Then 
					PlayerZone = 4
				ElseIf PlayerRoom\RoomTemplate\Name = "room860"
					For e.Events = Each Events
						If e\EventName = "room860"
							If e\EventState = 1.0
								PlayerZone = 5
								PositionEntity (SoundEmitter, EntityX(SoundEmitter), 30.0, EntityZ(SoundEmitter))
							EndIf
							
							Exit
						EndIf
					Next
				EndIf
				
				CurrAmbientSFX = Rand(0,AmbientSFXAmount(PlayerZone)-1)
				
				Select PlayerZone
					Case 0,1,2
						If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Zone"+(PlayerZone+1)+"\ambient"+(CurrAmbientSFX+1)+".ogg")
					Case 3
						If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\General\ambient"+(CurrAmbientSFX+1)+".ogg")
					Case 4
						If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Pre-breach\ambient"+(CurrAmbientSFX+1)+".ogg")
					Case 5
						If AmbientSFX(PlayerZone,CurrAmbientSFX)=0 Then AmbientSFX(PlayerZone,CurrAmbientSFX)=LoadSound_Strict("SFX\Ambient\Forest\ambient"+(CurrAmbientSFX+1)+".ogg")
				End Select
				
				AmbientSFXCHN = PlaySound2(AmbientSFX(PlayerZone,CurrAmbientSFX), Camera, SoundEmitter)
			EndIf
			UpdateSoundOrigin(AmbientSFXCHN,Camera, SoundEmitter)
			
			If Rand(50000) = 3 Then
				Local RN$ = PlayerRoom\RoomTemplate\Name$
				If RN$ <> "room860" And RN$ <> "room1123" And RN$ <> "173" And RN$ <> "dimension1499" Then
					If FPSfactor > 0 Then LightBlink = Rnd(1.0,2.0)
					PlaySound_Strict  LoadTempSound("SFX\SCP\079\Broadcast"+Rand(1,7)+".ogg")
				EndIf 
			EndIf
		EndIf
		
		UpdateCheckpoint1 = False
		UpdateCheckpoint2 = False
		
		If (Not IsPaused()) And EndingTimer=>0 Then
			LightVolume = CurveValue(TempLightVolume, LightVolume, 50.0)
			CameraFogRange(Camera, CameraFogNear*LightVolume,CameraFogFar*LightVolume)
			CameraFogColor(Camera, 0,0,0)
			CameraFogMode Camera,1
			CameraRange(Camera, 0.05, Min(CameraFogFar*LightVolume*1.5,28))	
			If PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then
				CameraClsColor(Camera, 0,0,0)
			EndIf
			
			AmbientLight Brightness, Brightness, Brightness	
			PlayerSoundVolume = CurveValue(0.0, PlayerSoundVolume, 5.0)
			
			CanSave% = True
			UpdateDeafPlayer()
			UpdateEmitters()
			MouseLook()
			If PlayerRoom\RoomTemplate\Name = "dimension1499" And QuickLoadPercent > 0 And QuickLoadPercent < 100
				ShouldEntitiesFall = False
			EndIf
			MovePlayer()
			InFacility = CheckForPlayerInFacility()
			If PlayerRoom\RoomTemplate\Name = "dimension1499"
				If QuickLoadPercent = -1 Or QuickLoadPercent = 100
					UpdateDimension1499()
				EndIf
				UpdateLeave1499()
			ElseIf PlayerRoom\RoomTemplate\Name = "gatea" Or (PlayerRoom\RoomTemplate\Name="exit1" And EntityY(Collider)>1040.0*RoomScale)
				UpdateDoors()
				If QuickLoadPercent = -1 Or QuickLoadPercent = 100
					UpdateEndings()
				EndIf
				UpdateScreens()
				UpdateRoomLights(Camera)
			Else
				UpdateDoors()
				If QuickLoadPercent = -1 Or QuickLoadPercent = 100
					UpdateEvents()
				EndIf
				UpdateScreens()
				TimeCheckpointMonitors()
				UpdateRoomLights(Camera)
			EndIf
			Update294()
			UpdateDecals()
			UpdateMTF()
			UpdateNPCs()
			UpdateItems()
			UpdateParticles()
			Use427()
			UpdateMonitorSaving()
			UpdateParticles_Time# = UpdateParticles_Time#+FPSfactor
			If UpdateParticles_Time#=>1
				UpdateDevilEmitters()
				UpdateParticles_Devil()
				UpdateParticles_Time# = UpdateParticles_Time# - 1
			EndIf
		EndIf
		
		If InfiniteStamina% Then Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
		
		If FPSfactor=0
			UpdateWorld(0)
		Else
			UpdateWorld()
			ManipulateNPCBones()
		EndIf
		RenderWorld2()
		
		BlurVolume = Min(CurveValue(0.0, BlurVolume, 20.0),0.95)
		If BlurTimer > 0.0 Then
			BlurVolume = Max(Min(0.95, BlurTimer / 1000.0), BlurVolume)
			BlurTimer = Max(BlurTimer - FPSfactor, 0.0)
		End If
		
		UpdateBlur(BlurVolume)
		
		;[Block]
		
		Local darkA# = 0.0
		If (Not MenuOpen)  Then
			If Sanity < 0 Then
				If RestoreSanity Then Sanity = Min(Sanity + FPSfactor, 0.0)
				If Sanity < (-200) Then 
					darkA = Max(Min((-Sanity - 200) / 700.0, 0.6), darkA)
					If KillTimer => 0 Then 
						HeartBeatVolume = Min(Abs(Sanity+200)/500.0,1.0)
						HeartBeatRate = Max(70 + Abs(Sanity+200)/6.0,HeartBeatRate)
					EndIf
				EndIf
			End If
			
			If EyeStuck > 0 Then 
				BlinkTimer = BLINKFREQ
				EyeStuck = Max(EyeStuck-FPSfactor,0)
				
				If EyeStuck < 9000 Then BlurTimer = Max(BlurTimer, (9000-EyeStuck)*0.5)
				If EyeStuck < 6000 Then darkA = Min(Max(darkA, (6000-EyeStuck)/5000.0),1.0)
				If EyeStuck < 9000 And EyeStuck+FPSfactor =>9000 Then 
					Msg = I_Loc\MessageItem_EyedropsTear
					MsgTimer = 70*6
				EndIf
			EndIf
			
			If BlinkTimer < 0 Then
				If BlinkTimer > - 5 Then
					darkA = Max(darkA, Sin(Abs(BlinkTimer * 18.0)))
				ElseIf BlinkTimer > - 15
					darkA = 1.0
				Else
					darkA = Max(darkA, Abs(Sin(BlinkTimer * 18.0)))
				EndIf
				
				If BlinkTimer <= - 20 Then
					;Randomizes the frequency of blinking. Scales with difficulty.
					Select SelectedDifficulty\otherFactors
						Case EASY
							BLINKFREQ = Rnd(490,700)
						Case NORMAL
							BLINKFREQ = Rnd(455,665)
						Case HARD
							BLINKFREQ = Rnd(420,630)
					End Select 
					BlinkTimer = BLINKFREQ
				EndIf
				
				BlinkTimer = BlinkTimer - FPSfactor
			Else
				BlinkTimer = BlinkTimer - FPSfactor * 0.6 * BlinkEffect
				If EyeIrritation > 0 Then BlinkTimer=BlinkTimer-Min(EyeIrritation / 100.0 + 1.0, 4.0) * FPSfactor
				
				darkA = Max(darkA, 0.0)
			End If
			
			EyeIrritation = Max(0, EyeIrritation - FPSfactor)
			
			If BlinkEffectTimer > 0 Then
				BlinkEffectTimer = BlinkEffectTimer - (FPSfactor/70)
			Else
				If BlinkEffect <> 1.0 Then BlinkEffect = 1.0
			EndIf
			
			LightBlink = Max(LightBlink - (FPSfactor / 35.0), 0)
			If LightBlink > 0 Then darkA = Min(Max(darkA, LightBlink * Rnd(0.3, 0.8)), 1.0)
			
			If Using294 Then darkA=1.0
			
			If (Not WearingNightVision) Then darkA = Max((1.0-SecondaryLightOn)*0.9, darkA)
			
			If KillTimer < 0 Then
				InvOpen = False
				SelectedItem = Null
				SelectedScreen = Null
				SelectedMonitor = Null
				BlurTimer = Abs(KillTimer*5)
				KillTimer=KillTimer-(FPSfactor*0.8)
				If KillTimer < - 360 Then 
					MenuOpen = True 
					If SelectedEnding <> "" Then EndingTimer = Min(KillTimer,-0.1)
				EndIf
				darkA = Max(darkA, Min(Abs(KillTimer / 400.0), 1.0))
			EndIf
			
			If FallTimer < 0 Then
				If SelectedItem <> Null Then
					If SelectedItem\itemtemplate\group = "hazmat" Or SelectedItem\itemtemplate\group = "vest" Then
						If WearingHazmat=0 And WearingVest=0 Then
							DropItem(SelectedItem)
						EndIf
					EndIf
				EndIf
				InvOpen = False
				SelectedItem = Null
				SelectedScreen = Null
				SelectedMonitor = Null
				BlurTimer = Abs(FallTimer*10)
				FallTimer = FallTimer-FPSfactor
				darkA = Max(darkA, Min(Abs(FallTimer / 400.0), 1.0))				
			EndIf
			
			If SelectedScreen <> Null Lor (Not InvOpen) And SelectedItem <> Null And SelectedItem\itemtemplate\group = "nav" Then darkA = Max(darkA, 0.5)
			
			EntityAlpha(Dark, darkA)	
		EndIf
		
		If Not IsAnyMenuOpen() Then
			If LightFlash > 0 Then
				ShowEntity Light
				EntityAlpha(Light, Max(Min(LightFlash + Rnd(-0.2, 0.2), 1.0), 0.0))
				LightFlash = Max(LightFlash - (FPSfactor / 70.0), 0)
			Else
				HideEntity Light
				;EntityAlpha(Light, LightFlash)
			EndIf
		EndIf
		
		EntityColor Light,255,255,255
		
		;[End block]
		
		If KeyHit(KEY_INV) And VomitTimer >= 0 And (Not UnableToMove) And (Not IsZombie) And (Not Using294) And KillTimer >= 0 And (Not MenuOpen) Then
			Local W$ = ""
			Local V# = 0
			If SelectedItem<>Null
				W$ = SelectedItem\itemtemplate\group
				V# = SelectedItem\state
			EndIf
			If (W<>"vest" And W<>"hazmat") Or V=0 Or V=100
				InvOpen = Not InvOpen
				If OtherOpen<>Null Then OtherOpen=Null
				SelectedItem = Null
				SelectedScreen = Null
				UpdateMenuState()
			EndIf
		EndIf
		
		If KeyHit(KEY_SAVE) Then
			If SelectedDifficulty\saveType = SAVEANYWHERE Then
				RN$ = PlayerRoom\RoomTemplate\Name$
				If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
					Msg = I_Loc\MessageSave_DisabledLocation
					MsgTimer = 70 * 4
				ElseIf (Not CanSave) Or QuickLoadPercent > -1
					If QuickLoadPercent > -1
						Msg = I_Loc\MessageSave_DisabledLoading
					Else
						Msg = I_Loc\MessageSave_DisabledMoment
					EndIf
					MsgTimer = 70 * 4
				Else
					SaveGame(SavePath + CurrSave)
				EndIf
			ElseIf SelectedDifficulty\saveType = SAVEONSCREENS
				If SelectedScreen=Null And SelectedMonitor=Null Then
					Msg = I_Loc\MessageSave_Screens
					MsgTimer = 70 * 4
				Else
					RN$ = PlayerRoom\RoomTemplate\Name$
					If RN$ = "173" Or (RN$ = "exit1" And EntityY(Collider)>1040.0*RoomScale) Or RN$ = "gatea"
						Msg = I_Loc\MessageSave_DisabledLocation
						MsgTimer = 70 * 4
					ElseIf (Not CanSave) Or QuickLoadPercent > -1
						If QuickLoadPercent > -1
							Msg = I_Loc\MessageSave_DisabledLoading
						Else
							Msg = I_Loc\MessageSave_DisabledMoment
						EndIf
						MsgTimer = 70 * 4
					Else
						If SelectedScreen<>Null
							GameSaved = False
							Playable = True
							DropSpeed = 0
						EndIf
						SaveGame(SavePath + CurrSave)
					EndIf
				EndIf
			Else
				Msg = I_Loc\MessageSave_Disabled
				MsgTimer = 70 * 4
			EndIf
		Else If SelectedDifficulty\saveType = SAVEONSCREENS And (SelectedScreen<>Null Or SelectedMonitor<>Null)
			If (Msg<>I_Loc\MessageSave_Saved And Msg<>I_Loc\MessageSave_DisabledLocation And Msg<>I_Loc\MessageSave_DisabledMoment) Or MsgTimer<=0 Then
				Msg = Format(I_Loc\MessageSave_Anywhere, KeyName(KEY_SAVE))
				MsgTimer = 70*4
			EndIf
			
			If MouseHit2 Then SelectedMonitor = Null
		EndIf
		
		If KeyHit(KEY_CONSOLE) Then
			If CanOpenConsole
				ConsoleOpen = (Not ConsoleOpen)
				UpdateMenuState()
			EndIf
		EndIf
		
		DrawGUI()
		
		UpdateConsole()
		
		If PlayerRoom <> Null Then
			If PlayerRoom\RoomTemplate\Name = "173" Then
				For e.Events = Each Events
					If e\EventName = "173" Then
						If e\EventState3 => 40 And e\EventState3 < 50 Then
							If InvOpen Then
								Msg = I_Loc\MessageHelp_View
								MsgTimer=70*7
								e\EventState3 = 50
								Exit
							EndIf
						EndIf
					EndIf
				Next
			EndIf
		EndIf
		
		If MsgTimer > 0 Then
			;If temp = True -> move the message below
			Local temp% = False
			If (Not InvOpen And OtherOpen = Null) Then
				If SelectedItem <> Null
					If SelectedItem\itemtemplate\group = "paper" Or SelectedItem\itemtemplate\name = "oldpaper"
						temp% = True
					EndIf
				EndIf
			EndIf
			
			Local messageOpacity% = Min(MsgTimer / 2, 255)
			If (Not temp%)
				Color 0,0,0
				Text((GraphicWidth / 2)+1, (GraphicHeight / 2) + 201, Msg, True, False)
				Color messageOpacity, messageOpacity, messageOpacity
				Text((GraphicWidth / 2), (GraphicHeight / 2) + 200, Msg, True, False)
			Else
				Color 0,0,0
				Text((GraphicWidth / 2)+1, (GraphicHeight * 0.94) + 1, Msg, True, False)
				Color messageOpacity, messageOpacity, messageOpacity
				Text((GraphicWidth / 2), (GraphicHeight * 0.94), Msg, True, False)
			EndIf
			MsgTimer=MsgTimer-FPSfactor2 
		End If
		
		Color 255, 255, 255
		If ShowFPS Then SetFont ConsoleFont : Text 20, 20, Format(I_Loc\HUD_Fps, FPS) : SetFont Font1
		
		If EndingTimer < 0 Then
			If SelectedEnding <> "" Then DrawEnding()
		Else
			DrawMenu()			
		EndIf
		
		DrawQuickLoading()
		
		UpdateAchievementMsg()
		;UpdateSaveMSG()
	End If
	
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
	
	CatchErrors("Main loop / uncaught")

	If SteamActive Then Steam_Update()
	If DiscordActive Then
		If MilliSecs() > DiscordCooldown Then
			If MainMenuOpen Then
				If DiscordLastStatus <> -1 Then
					BlitzcordSetActivityDetails("Browsing the menus")
					BlitzcordSetLargeText("")
					BlitzcordSetSmallImage("")
					BlitzcordSetTimestampStart(BlitzcordGetCurrentTimestamp())
					BlitzcordUpdateActivity()
					DiscordCooldown = MilliSecs() + 5000
					DiscordLastStatus = -1
				EndIf
			Else
				If DiscordLastStatus = -1 Then
					BlitzcordSetLargeText(GetSeedString())
					BlitzcordSetSmallImage(Lower(SelectedDifficulty\name))
					BlitzcordSetSmallText("Difficulty: " + SelectedDifficulty\name)
					BlitzcordSetTimestampStart(BlitzcordGetCurrentTimestamp())
				EndIf

				Local newArea% = PlayerZone
				Select PlayerRoom\RoomTemplate\Name
					Case "173" newArea = 4
					Case "dimension1499" newArea = 100
					Case "pocketdimension" newArea = 101
					Case "gatea" newArea = 102
					Case "exit1" newArea = 103
					Case "room2tunnel" If EntityY(Collider,True)>4.0 Then newArea = 104
				End Select
				If newArea <> DiscordLastStatus Then
					Local newAreaStr$
					Select newArea
						Case 0 newAreaStr = "Roaming the Light Containment Zone"
						Case 1 newAreaStr = "Roaming the Heavy Containment Zone"
						Case 2 newAreaStr = "Roaming the Entrance Zone"
						Case 3 newAreaStr = "jorge has been expecting you"
						Case 4 newAreaStr = "Being tested on SCP-173"
						Case 5 newAreaStr = "Traversing a blue-hued forest"
						Case 100 newAreaStr = "Wearing a GP-5 Gas Mask"
						Case 101 newAreaStr = "Trapped in the Pocket Dimension"
						Case 102 newAreaStr = "Escaping through Gate A"
						Case 103 newAreaStr = "Escaping through Gate B"
						Case 104 newAreaStr = "Lost in the Maintenance Tunnels"
					End Select
					BlitzcordSetActivityDetails(newAreaStr)
					BlitzcordUpdateActivity()
					DiscordCooldown = MilliSecs() + 5000
					DiscordLastStatus = newArea
				EndIf
			EndIf
		EndIf
		BlitzcordRunCallbacks()
	EndIf

	If Vsync = 0 Then
		Flip 0
	Else 
		Flip 1
	EndIf
Wend

If ShouldRestart Then
	IsRestart = True
	Goto Start
EndIf

If SteamActive Then Steam_Shutdown()
If DiscordActive Then BlitzcordClearActivity()

Function Restart()
	Cls
	StopStream_Strict(MusicCHN)
	MusicCHN = 0
	ClearLoadedINIFiles()
	IsRunning = False
	ShouldRestart = True
End Function

;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------
;----------------------------------------------------------------------------------------------------------------------------------------------------

Function QuickLoadEvents()
	CatchErrors("Uncaught (QuickLoadEvents)")
	
	If QuickLoad_CurrEvent = Null Then
		QuickLoadPercent = -1
		Return
	EndIf
	
	Local e.Events = QuickLoad_CurrEvent
	
	Local r.Rooms,sc.SecurityCams,sc2.SecurityCams,scale#,pvt%,n.NPCs,tex%,i%,x#,z#
	
	;might be a good idea to use QuickLoadPercent to determine the "steps" of the loading process 
	;instead of magic values in e\eventState and e\eventStr

	Select e\EventName
		Case "room2sl"
			;[Block]
			If e\EventState = 0 And e\EventStr <> ""
				If e\EventStr <> "" And Left(e\EventStr,4) <> "load"
					QuickLoadPercent = QuickLoadPercent + 5
					If Int(e\EventStr) > 9
						e\EventStr = "load2"
					Else
						e\EventStr = Int(e\EventStr) + 1
					EndIf
				ElseIf e\EventStr = "load2"
					;For SCP-049
					Local skip = False
					If e\room\NPC[0]=Null Then
						For n.NPCs = Each NPCs
							If n\NPCtype = NPCtype049
								;e\room\NPC[0] = n
								skip = True
								Exit
							EndIf
						Next
						
						If (Not skip)
							e\room\NPC[0] = CreateNPC(NPCtype049,EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True)+5,EntityZ(e\room\Objects[7],True))
							e\room\NPC[0]\HideFromNVG = True
							PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True)+5,EntityZ(e\room\Objects[7],True)
							ResetEntity e\room\NPC[0]\Collider
							RotateEntity e\room\NPC[0]\Collider,0,e\room\angle+180,0
							e\room\NPC[0]\State = 0
							e\room\NPC[0]\PrevState = 2
							
							DebugLog(EntityX(e\room\Objects[7],True)+", "+EntityY(e\room\Objects[7],True)+", "+EntityZ(e\room\Objects[7],True))
						Else
							DebugLog "Skipped 049 spawning in room2sl"
						EndIf
					EndIf
					QuickLoadPercent = 80
					e\EventStr = "load3"
				ElseIf e\EventStr = "load3"
					;PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True)+5,EntityZ(e\room\Objects[7],True)
					;ResetEntity e\room\NPC[0]\Collider
					;RotateEntity e\room\NPC[0]\Collider,0,e\room\angle+180,0
					
					;DebugLog(EntityX(e\room\Objects[7],True)+", "+EntityY(e\room\Objects[7],True)+", "+EntityZ(e\room\Objects[7],True))
					
					;e\room\NPC[0]\State = 0
					;e\room\NPC[0]\PrevState = 2
					
					e\EventState = 1
					If e\EventState2 = 0 Then e\EventState2 = -(70*5)
					
					QuickLoadPercent = 100
				EndIf
			EndIf
			;[End Block]
		Case "room2closets"
			;[Block]
			If e\EventState = 0
				If e\EventStr = "load0"
					QuickLoadPercent = 10
					If e\room\NPC[0]=Null Then
						e\room\NPC[0] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True))
					EndIf
					
					ChangeNPCTextureID(e\room\NPC[0],4)
					e\EventStr = "load1"
				ElseIf e\EventStr = "load1"
					QuickLoadPercent = 20
					e\room\NPC[0]\Sound=LoadSound_Strict("SFX\Room\Storeroom\Escape1.ogg")
					e\EventStr = "load2"
				ElseIf e\EventStr = "load2"
					QuickLoadPercent = 35
					e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound, Camera, e\room\NPC[0]\Collider, 12)
					e\EventStr = "load3"
				ElseIf e\EventStr = "load3"
					QuickLoadPercent = 55
					If e\room\NPC[1]=Null Then
						e\room\NPC[1] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[1],True),EntityY(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True))
					EndIf
					
					ChangeNPCTextureID(e\room\NPC[1],2)
					e\EventStr = "load4"
				ElseIf e\EventStr = "load4"
					QuickLoadPercent = 80
					e\room\NPC[1]\Sound=LoadSound_Strict("SFX\Room\Storeroom\Escape2.ogg")
					e\EventStr = "load5"
				ElseIf e\EventStr = "load5"
					QuickLoadPercent = 100
					PointEntity e\room\NPC[0]\Collider, e\room\NPC[1]\Collider
					PointEntity e\room\NPC[1]\Collider, e\room\NPC[0]\Collider
					
					e\EventState=1
				EndIf
			EndIf
			;[End Block]
		Case "room3storage"
			;[Block]
			If e\room\NPC[0]=Null Then
				e\room\NPC[0]=CreateNPC(NPCtype939, 0,0,0)
				QuickLoadPercent = 20
			ElseIf e\room\NPC[1]=Null Then
				e\room\NPC[1]=CreateNPC(NPCtype939, 0,0,0)
				QuickLoadPercent = 50
			ElseIf e\room\NPC[2]=Null Then
				e\room\NPC[2]=CreateNPC(NPCtype939, 0,0,0)
				QuickLoadPercent = 100
			Else
				If QuickLoadPercent > -1 Then QuickLoadPercent = 100
			EndIf
			;[End Block]
		Case "room049"
			;[Block]
			If e\EventState = 0 Then
				If e\EventStr = "load0"
					n.NPCs = CreateNPC(NPCtypeZombie, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
					PointEntity n\Collider, e\room\obj
					TurnEntity n\Collider, 0, 190, 0
					QuickLoadPercent = 20
					e\EventStr = "load1"
				ElseIf e\EventStr = "load1"
					n.NPCs = CreateNPC(NPCtypeZombie, EntityX(e\room\Objects[5],True),EntityY(e\room\Objects[5],True),EntityZ(e\room\Objects[5],True))
					PointEntity n\Collider, e\room\obj
					TurnEntity n\Collider, 0, 20, 0
					QuickLoadPercent = 60
					e\EventStr = "load2"
				ElseIf e\EventStr = "load2"
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype049
							e\room\NPC[0]=n
							e\room\NPC[0]\State = 2
							e\room\NPC[0]\Idle = 1
							e\room\NPC[0]\HideFromNVG = True
							PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True)+3,EntityZ(e\room\Objects[4],True)
							ResetEntity e\room\NPC[0]\Collider
							Exit
						EndIf
					Next
					If e\room\NPC[0]=Null
						n.NPCs = CreateNPC(NPCtype049, EntityX(e\room\Objects[4],True), EntityY(e\room\Objects[4],True)+3, EntityZ(e\room\Objects[4],True))
						PointEntity n\Collider, e\room\obj
						n\State = 2
						n\Idle = 1
						n\HideFromNVG = True
						e\room\NPC[0]=n
					EndIf
					QuickLoadPercent = 100
					e\EventState=1
				EndIf
			EndIf
			;[End Block]
		Case "room205"
			;[Block]
			If e\EventState=0 Or e\EventStr <> "loaddone" Then
				If e\EventStr = "load0"
					e\room\Objects[3] = LoadAnimMesh_Strict("GFX\npcs\205_demon1.b3d")
					QuickLoadPercent = 10
					e\EventStr = "load1"
				ElseIf e\EventStr = "load1"
					e\room\Objects[4] = LoadAnimMesh_Strict("GFX\npcs\205_demon2.b3d")
					QuickLoadPercent = 20
					e\EventStr = "load2"
				ElseIf e\EventStr = "load2"
					e\room\Objects[5] = LoadAnimMesh_Strict("GFX\npcs\205_demon3.b3d")
					QuickLoadPercent = 30
					e\EventStr = "load3"
				ElseIf e\EventStr = "load3"
					e\room\Objects[6] = LoadAnimMesh_Strict("GFX\npcs\205_woman.b3d")
					QuickLoadPercent = 40
					e\EventStr = "load4"
				ElseIf e\EventStr = "load4"
					QuickLoadPercent = 50
					e\EventStr = "load5"
				ElseIf e\EventStr = "load5"
					For i = 3 To 6
						PositionEntity e\room\Objects[i], EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True), True
						RotateEntity e\room\Objects[i], -90, EntityYaw(e\room\Objects[0],True), 0, True
						ScaleEntity(e\room\Objects[i], 0.05, 0.05, 0.05, True)
					Next
					QuickLoadPercent = 70
					e\EventStr = "load6"
				ElseIf e\EventStr = "load6"
					;GiveAchievement(Achv205)
					
					HideEntity(e\room\Objects[3])
					HideEntity(e\room\Objects[4])
					HideEntity(e\room\Objects[5])
					QuickLoadPercent = 100
					e\EventStr = "loaddone"
					;e\EventState = 1
				EndIf
			EndIf
			;[End Block]
		Case "room860"
			;[Block]
			If e\EventStr = "load0"
				QuickLoadPercent = 15
				ForestNPC = CreateSprite()
				;0.75 = 0.75*(410.0/410.0) - 0.75*(width/height)
				ScaleSprite ForestNPC,0.75*(140.0/410.0),0.75
				SpriteViewMode ForestNPC,4
				EntityFX ForestNPC,1+8
				ForestNPCTex = LoadAnimTexture("GFX\npcs\AgentIJ.AIJ",1+2,140,410,0,4)
				ForestNPCData[0] = 0
				EntityTexture ForestNPC,ForestNPCTex,ForestNPCData[0]
				ForestNPCData[1]=0
				ForestNPCData[2]=0
				HideEntity ForestNPC
				e\EventStr = "load1"
			ElseIf e\EventStr = "load1"
				QuickLoadPercent = 40
				e\EventStr = "load2"
			ElseIf e\EventStr = "load2"
				QuickLoadPercent = 100
				If e\room\NPC[0]=Null Then e\room\NPC[0]=CreateNPC(NPCtype860, 0,0,0)
				e\EventStr = "loaddone"
			EndIf
			;[End Block]
		Case "room966"
			;[Block]
			If e\EventState = 1
				e\EventState2 = e\EventState2+FPSfactor
				If e\EventState2>30 Then
					If e\EventStr = ""
						CreateNPC(NPCtype966, EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True))
						QuickLoadPercent = 50
						e\EventStr = "load0"
					ElseIf e\EventStr = "load0"
						CreateNPC(NPCtype966, EntityX(e\room\Objects[2],True), EntityY(e\room\Objects[2],True), EntityZ(e\room\Objects[2],True))
						QuickLoadPercent = 100
						e\EventState=2
					EndIf
				Else
					QuickLoadPercent = Int(e\EventState2)
				EndIf
			EndIf
			;[End Block]
		Case "dimension1499"
			;[Block]
			If e\EventState = 0.0
				If e\EventStr = "load0"
					QuickLoadPercent = 10
					e\room\Objects[0] = LoadMesh_Strict("GFX\map\dimension1499\1499plane.b3d")
					;Local planetex% = LoadTexture_Strict("GFX\map\dimension1499\grit3.jpg")
					;ScaleTexture planetex%,0.5,0.5
					;EntityTexture e\room\Objects[0],planetex%
					;FreeTexture planetex%
					HideEntity e\room\Objects[0]
					e\EventStr = "load1"
				ElseIf e\EventStr = "load1"
					QuickLoadPercent = 30
					NTF_1499Sky = sky_CreateSky("GFX\map\sky\1499sky")
					e\EventStr = 1
				Else
					If Int(e\EventStr)<16
						QuickLoadPercent = QuickLoadPercent + 2
						e\room\Objects[Int(e\EventStr)] = LoadMesh_Strict("GFX\map\dimension1499\1499object"+(Int(e\EventStr))+".b3d")
						HideEntity e\room\Objects[Int(e\EventStr)]
						e\EventStr = Int(e\EventStr)+1
					ElseIf Int(e\EventStr)=16
						QuickLoadPercent = 90
						CreateChunkParts(e\room)
						e\EventStr = 17
					ElseIf Int(e\EventStr) = 17
						QuickLoadPercent = 100
						x# = EntityX(e\room\obj)
						z# = EntityZ(e\room\obj)
						Local ch.Chunk
						For i = -2 To 0 Step 2
							ch = CreateChunk(-1,x#*(i*2.5),EntityY(e\room\obj),z#,True)
							ch = CreateChunk(-1,x#*(i*2.5),EntityY(e\room\obj),z#-40,True)
						Next
						e\EventState = 2.0
						e\EventStr = 18
					EndIf
				EndIf
			EndIf
			;[End Block]
	End Select
	
	CatchErrors("QuickLoadEvents "+e\EventName)
	
End Function

Function Kill()
	If GodMode Then Return
	
	If BreathCHN <> 0 Then
		If ChannelPlaying(BreathCHN) Then StopChannel(BreathCHN)
	EndIf
	
	If KillTimer >= 0 Then
		KillAnim = Rand(0,1)
		PlaySound_Strict(DamageSFX(0))
		If SelectedDifficulty\permaDeath Then
			DeleteFile(CurrentDir() + SavePath + CurrSave+".cbsav")
			LoadSaveGames()
		End If
		
		KillTimer = Min(-1, KillTimer)
		ShowEntity Head
		PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
		ResetEntity (Head)
		RotateEntity(Head, 0, EntityYaw(Camera), 0)		
	EndIf
End Function

Function DrawEnding()
	
	ShowPointer()
	
	FPSfactor = 0
	;EndingTimer=EndingTimer-FPSfactor2
	If EndingTimer>-2000
		EndingTimer=Max(EndingTimer-FPSfactor2,-1111)
	Else
		EndingTimer=EndingTimer-FPSfactor2
	EndIf
	
	GiveAchievement(Achv055)
	If (Not UsedConsole) Then GiveAchievement(AchvConsole)
	If SelectedDifficulty = difficulties[KETER] Then GiveAchievement(AchvKeter)
	Local x,y,width,height, temp
	Local itt.ItemTemplates, r.Rooms
	
	Select Lower(SelectedEnding)
		Case "b2", "a1"
			ClsColor Max(255+(EndingTimer)*2.8,0), Max(255+(EndingTimer)*2.8,0), Max(255+(EndingTimer)*2.8,0)
		Default
			ClsColor 0,0,0
	End Select
	
	ShouldPlay = 66
	
	Cls
	
	If EndingTimer<-200 Then
		
		If BreathCHN <> 0 Then
			If ChannelPlaying(BreathCHN) Then StopChannel BreathCHN : Stamina = 100
		EndIf
		
		;If EndingTimer <-400 Then 
		;	ShouldPlay = 13
		;EndIf
		
		If EndingScreen = 0 Then
			EndingScreen = LoadImage_Strict("GFX\endingscreen.pt")
			
			ShouldPlay = 23
			CurrMusicVolume = MusicVolume
			
			CurrMusicVolume = MusicVolume
			StopStream_Strict(MusicCHN)
			MusicCHN = StreamSound_Strict("SFX\Music\"+Music(23)+".ogg",CurrMusicVolume,0)
			NowPlaying = ShouldPlay
			
			PlaySound_Strict LightSFX
		EndIf
		
		If EndingTimer > -700 Then 
			
			;-200 -> -700
			;Max(50 - (Abs(KillTimer)-200),0)    =    0->50
			If Rand(1,150)<Min((Abs(EndingTimer)-200),155) Then
				DrawImage EndingScreen, GraphicWidth/2-400, GraphicHeight/2-400
			Else
				Color 0,0,0
				Rect 100,100,GraphicWidth-200,GraphicHeight-200
				Color 255,255,255
			EndIf
			
			If EndingTimer+FPSfactor2 > -450 And EndingTimer <= -450 Then
				Select Lower(SelectedEnding)
					Case "a1", "a2"
						PlaySound_Strict LoadTempSound("SFX\Ending\GateA\Ending"+SelectedEnding+".ogg")
					Case "b1", "b2", "b3"
						PlaySound_Strict LoadTempSound("SFX\Ending\GateB\Ending"+SelectedEnding+".ogg")
				End Select
			EndIf			
			
		Else
			
			DrawImage EndingScreen, GraphicWidth/2-400, GraphicHeight/2-400
			
			If EndingTimer < -1000 And EndingTimer > -2000
				
				width = ImageWidth(PauseMenuIMG)
				height = ImageHeight(PauseMenuIMG)
				x = GraphicWidth / 2 - width / 2
				y = GraphicHeight / 2 - height / 2
				
				DrawImage PauseMenuIMG, x, y
				
				Color(255, 255, 255)
				SetFont Font2
				Text(x + width / 2 + 40*MenuScale, y + 20*MenuScale, I_Loc\Menu_End, True)
				SetFont Font1
				
				If AchievementsMenu=0 Then 
					x = x+132*MenuScale
					y = y+122*MenuScale
					
					Local roomamount = 0, roomsfound = 0
					For r.Rooms = Each Rooms
						Local RN$ = r\RoomTemplate\Name$
						If RN$ <> "dimension1499" And RN$ <> "gatea" And RN$ <> "pocketdimension" Then 
							roomamount = roomamount + 1
							roomsfound = roomsfound + r\found
						EndIf
					Next
					
					Local docamount=0, docsfound=0
					For itt.ItemTemplates = Each ItemTemplates
						If itt\group = "paper" Then
							docamount=docamount+1
							docsfound=docsfound+itt\found
						EndIf
					Next
					
					Local scpsEncountered=1
					For i = Achv008 To Achv1499
						scpsEncountered = scpsEncountered+Achievements(i)
					Next
					
					Local achievementsUnlocked =0
					For i = 0 To MAXACHIEVEMENTS-1
						achievementsUnlocked = achievementsUnlocked + Achievements(i)
					Next
					
					Text x, y, I_Loc\Menu_EndEnding+" " + Upper(SelectedEnding)
					Text x, y+20*MenuScale, I_Loc\Menu_EndTime+" " + FormatDuration(PlayTime, SpeedRunMode)
					Text x, y+40*MenuScale, I_Loc\Menu_EndScps+" " + scpsEncountered
					Text x, y+60*MenuScale, I_Loc\Menu_EndAchv+" " + achievementsUnlocked+"/"+(MAXACHIEVEMENTS)
					Text x, y+80*MenuScale, I_Loc\Menu_EndRooms+" " + roomsfound+"/"+roomamount
					Text x, y+100*MenuScale, I_Loc\Menu_EndDocs+" " +docsfound+"/"+docamount
					Text x, y+120*MenuScale, I_Loc\Menu_End914+" " +RefinedItems			
					
					x = GraphicWidth / 2 - width / 2
					y = GraphicHeight / 2 - height / 2
					x = x+width/2
					y = y+height-100*MenuScale
					
					If DrawButton(x-145*MenuScale,y-200*MenuScale,390*MenuScale,60*MenuScale,"ACHIEVEMENTS", True) Then
						AchievementsMenu = 1
					EndIf
					
;					If DrawButton(x-145*MenuScale,y-100*MenuScale,390*MenuScale,60*MenuScale,"MAIN MENU", True) Then
;						NullGame()
;						StopStream_Strict(MusicCHN)
;						;Music(21) = LoadSound_Strict("SFX\Ending\MenuBreath.ogg")
;						ShouldPlay = 21
;						MenuOpen = False
;						MainMenuOpen = True
;						MainMenuTab = 0
;						CurrSave = ""
;						FlushKeys()
;					EndIf
					
					If DrawButton(x-145*MenuScale,y-100*MenuScale,390*MenuScale,60*MenuScale,"MAIN MENU", True)
						ShouldPlay = 24
						NowPlaying = ShouldPlay
						For i=0 To 9
							If TempSounds[i]<>0 Then FreeSound_Strict TempSounds[i] : TempSounds[i]=0
						Next
						StopStream_Strict(MusicCHN)
						MusicCHN = StreamSound_Strict("SFX\Music\"+Music(NowPlaying)+".ogg",0.0)
						SetStreamVolume_Strict(MusicCHN,1.0*MusicVolume)
						FlushKeys()
						EndingTimer=-2000
						TimerStopped = True
						InitCredits()
					EndIf
				Else
					ShouldPlay = 23
					DrawMenu()
				EndIf
			;Credits
			ElseIf EndingTimer<=-2000
				ShouldPlay = 24
				DrawCredits()
			EndIf
			
		EndIf
		
	EndIf
	
	If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
	
	SetFont Font1
End Function

Function UpdateMenuState()
	If IsAnyMenuOpen() Then
		PauseSounds()
	Else
		ResumeSounds()
		MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
	EndIf
	FlushKeys()
End Function

Function IsAnyMenuOpen()
	Return MenuOpen Lor ConsoleOpen Lor InvOpen Lor OtherOpen<>Null Lor (SelectedItem <> Null And SelectedItem\Inventory <> Null) Lor Using294
End Function

Function IsPaused()
	Return IsAnyMenuOpen() Lor SelectedDoor <> Null Lor SelectedScreen <> Null
End Function

Type CreditsLine
	Field txt$
	Field id%
	Field stay%
End Type

Global CreditsTimer# = 0.0
Global CreditsScreen%

Function InitCredits()
	Local cl.CreditsLine
	Local file% = OpenFile("Credits.txt")
	Local l$
	
	CreditsFont% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(21 * (GraphicHeight / 1024.0)))
	CreditsFont2% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(35 * (GraphicHeight / 1024.0)))
	
	If CreditsScreen = 0
		CreditsScreen = LoadImage_Strict("GFX\creditsscreen.pt")
	EndIf
	
	Repeat
		l = ReadLine(file)
		cl = New CreditsLine
		cl\txt = l
	Until Eof(file)
	CloseFile(file)
	
	Delete First CreditsLine
	CreditsTimer = 0
	
End Function

Function DrawCredits()
    Local credits_Y# = (EndingTimer+2000)/2+(GraphicHeight+10)
    Local cl.CreditsLine
    Local id%
    Local endlinesamount%
	Local LastCreditLine.CreditsLine
	
    Cls
	
	If Rand(1,300)>1
		DrawImage CreditsScreen, GraphicWidth/2-400, GraphicHeight/2-400
	EndIf
	
	id = 0
	endlinesamount = 0
	LastCreditLine = Null
	Color 255,255,255
	For cl = Each CreditsLine
		cl\id = id
		If Left(cl\txt,1)="*"
			SetFont CreditsFont2
			If cl\stay=False
				Text GraphicWidth/2,credits_Y+(24*cl\id*MenuScale),Right(cl\txt,Len(cl\txt)-1),True
			EndIf
		ElseIf Left(cl\txt,1)="/"
			LastCreditLine = Before(cl)
		Else
			SetFont CreditsFont
			If cl\stay=False
				Text GraphicWidth/2,credits_Y+(24*cl\id*MenuScale),cl\txt,True
			EndIf
		EndIf
		If LastCreditLine<>Null
			If cl\id>LastCreditLine\id
				cl\stay = True
			EndIf
		EndIf
		If cl\stay
			endlinesamount=endlinesamount+1
		EndIf
		id=id+1
	Next
	If (credits_Y+(24*LastCreditLine\id*MenuScale))<-StringHeight(LastCreditLine\txt)
		CreditsTimer=CreditsTimer+(0.5*FPSfactor2)
		If CreditsTimer>=0.0 And CreditsTimer<255.0
			Color Max(Min(CreditsTimer,255),0),Max(Min(CreditsTimer,255),0),Max(Min(CreditsTimer,255),0)
		ElseIf CreditsTimer>=255.0
			Color 255,255,255
			If CreditsTimer>500.0
				CreditsTimer=-255.0
			EndIf
		Else
			Color Max(Min(-CreditsTimer,255),0),Max(Min(-CreditsTimer,255),0),Max(Min(-CreditsTimer,255),0)
			If CreditsTimer>=-1.0
				CreditsTimer=-1.0
			EndIf
		EndIf
		DebugLog CreditsTimer
	EndIf
	If CreditsTimer<>0.0
		For cl = Each CreditsLine
			If cl\stay
				SetFont CreditsFont
				If Left(cl\txt,1)="/"
					Text GraphicWidth/2,(GraphicHeight/2)+(endlinesamount/2)+(24*cl\id*MenuScale),Right(cl\txt,Len(cl\txt)-1),True
				Else
					Text GraphicWidth/2,(GraphicHeight/2)+(24*(cl\id-LastCreditLine\id)*MenuScale)-((endlinesamount/2)*24*MenuScale),cl\txt,True
				EndIf
			EndIf
		Next
	EndIf
	
	If GetKey() Then CreditsTimer=-1
	
	If CreditsTimer=-1
		FreeFont CreditsFont
		FreeFont CreditsFont2
		FreeImage CreditsScreen
		CreditsScreen = 0
		FreeImage EndingScreen
		EndingScreen = 0
		Delete Each CreditsLine
        NullGame(False)
        StopStream_Strict(MusicCHN)
        ShouldPlay = 21
        MenuOpen = False
        MainMenuOpen = True
        MainMenuTab = 0
		PrevSave = ""
        CurrSave = ""
        FlushKeys()
	EndIf
    
End Function

;--------------------------------------- player controls -------------------------------------------

Global MoveX%, MoveZ%

Function MovePlayer()
	CatchErrors("Uncaught (MovePlayer)")
	Local Sprint# = 1.0, Speed# = 0.018, i%, angle#
	
	If SuperMan Then
		Speed = Speed * 3
		
		SuperManTimer=SuperManTimer+FPSfactor
		
		CameraShake = Sin(SuperManTimer / 5.0) * (SuperManTimer / 1500.0)
		
		If SuperManTimer > 70 * 50 Then
			DeathMSG = I_Loc\DeathMessage_914Superman
			Kill()
			ShowEntity Fog
		Else
			BlurTimer = 500		
			HideEntity Fog
		EndIf
	End If
	
	If DeathTimer > 0 Then
		DeathTimer=DeathTimer-FPSfactor
		If DeathTimer < 1 Then DeathTimer = -1.0
	ElseIf DeathTimer < 0 
		Kill()
	EndIf
	
	If CurrSpeed > 0 Then
        Stamina = Min(Stamina + 0.15 * FPSfactor/1.25, 100.0)
    Else
        Stamina = Min(Stamina + 0.15 * FPSfactor*1.25, 100.0)
    EndIf
	
	If StaminaEffectTimer > 0 Then
		StaminaEffectTimer = StaminaEffectTimer - (FPSfactor/70)
	Else
		If StaminaEffect <> 1.0 Then StaminaEffect = 1.0
	EndIf
	
	Local temp#
	
	If PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then 
		If KeyDown(KEY_SPRINT) Then
			If Stamina < 5 Then
				temp = 0
				If WearingGasMask>0 Or Wearing1499>0 Then temp=1
				If ChannelPlaying(BreathCHN)=False Then BreathCHN = PlaySound_Strict(BreathSFX((temp), 0))
			ElseIf Stamina < 50
				If BreathCHN=0 Then
					temp = 0
					If WearingGasMask>0 Or Wearing1499>0 Then temp=1
					BreathCHN = PlaySound_Strict(BreathSFX((temp), Rand(1,3)))
					ChannelVolume BreathCHN, Min((70.0-Stamina)/70.0,1.0)*SFXVolume
				Else
					If ChannelPlaying(BreathCHN)=False Then
						temp = 0
						If WearingGasMask>0 Or Wearing1499>0 Then temp=1
						BreathCHN = PlaySound_Strict(BreathSFX((temp), Rand(1,3)))
						ChannelVolume BreathCHN, Min((70.0-Stamina)/70.0,1.0)*SFXVolume			
					EndIf
				EndIf
			EndIf
		EndIf
	EndIf
	
	For i = 0 To MaxItemAmount-1
		If Inventory(i)<>Null Then
			If Inventory(i)\itemtemplate\name = "finevest" Then
				Stamina = Min(Stamina, 60)
				Exit
			EndIf
		EndIf
	Next
	
	If Wearing714 Then
		Stamina = Min(Stamina, 10)
		Sanity = Max(-850, Sanity)
	EndIf
	
	If IsZombie Then Crouch = False
	
	If Abs(CrouchState-Crouch)<0.001 Then 
		CrouchState = Crouch
	Else
		CrouchState = CurveValue(Crouch, CrouchState, 10.0)
	EndIf
	
	If (Not NoClip) Then
		If ForceMove > 0 Lor Playable And (MoveX <> 0 Lor MoveZ <> 0) Then
			If Crouch = 0 And (KeyDown(KEY_SPRINT)) And Stamina > 0.0 And (Not IsZombie) Then
				Sprint = 2.5
				Stamina = Stamina - FPSfactor * 0.4 * StaminaEffect
				If Stamina <= 0 Then Stamina = -20.0
			End If
			
			If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then 
				If EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale Then
					Stamina = 0
					Speed = 0.015
					Sprint = 1.0					
				EndIf
			EndIf	
			
			If ForceMove>0 Then Speed=Speed*ForceMove
			
			If SelectedItem<>Null Then
				If SelectedItem\itemtemplate\name = "firstaid" Or SelectedItem\itemtemplate\name = "finefirstaid" Or SelectedItem\itemtemplate\name = "firstaid2" Then 
					Sprint = 0
				EndIf
			EndIf
			
			temp# = (Shake Mod 360)
			Local tempchn%
			If (Not UnableToMove%) Then Shake# = (Shake + FPSfactor * Min(Sprint, 1.5) * 7) Mod 720
			If temp < 180 And (Shake Mod 360) >= 180 And KillTimer>=0 Then
				If CurrStepSFX=0 Then
					temp = GetStepSound(Collider)
					
					If Sprint = 1.0 Then
						PlayerSoundVolume = Max(2.5-(Crouch*0.6),PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(temp, 0, Rand(0, 7)))
					Else
						PlayerSoundVolume = Max(4.0,PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(temp, 1, Rand(0, 7)))
					End If
				ElseIf CurrStepSFX=1
					tempchn% = PlaySound_Strict(Step2SFX(Rand(0, 2)))
				ElseIf CurrStepSFX=2
					tempchn% = PlaySound_Strict(Step2SFX(Rand(3,5)))
				ElseIf CurrStepSFX=3
					If Sprint = 1.0 Then
						PlayerSoundVolume = Max(2.5-(Crouch*0.6),PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(0, 0, Rand(0, 7)))
					Else
						PlayerSoundVolume = Max(4.0,PlayerSoundVolume)
						tempchn% = PlaySound_Strict(StepSFX(0, 1, Rand(0, 7)))
					End If
				EndIf
				If tempchn <> 0 Then ChannelVolume tempchn, (1.0-(Crouch*0.6))*SFXVolume#
			EndIf	
		EndIf
	Else ;noclip on
		If (KeyDown(KEY_SPRINT)) Then 
			Sprint = 2.5
		ElseIf KeyDown(KEY_CROUCH)
			Sprint = 0.5
		EndIf
	EndIf
	
	If KeyHit(KEY_CROUCH) And Playable Then Crouch = (Not Crouch)
	
	Local temp2# = (Speed * Sprint) / (1.0+CrouchState)
	
	If NoClip Then 
		Shake = 0
		CurrSpeed = 0
		CrouchState = 0
		Crouch = 0
		
		RotateEntity Collider, WrapAngle(EntityPitch(Camera)), WrapAngle(EntityYaw(Camera)), 0
		
		temp2 = temp2 * NoClipSpeed
		
		If KeyDown(KEY_DOWN) Then MoveEntity Collider, 0, 0, -temp2*FPSfactor
		If KeyDown(KEY_UP) Then MoveEntity Collider, 0, 0, temp2*FPSfactor
		
		If KeyDown(KEY_LEFT) Then MoveEntity Collider, -temp2*FPSfactor, 0, 0
		If KeyDown(KEY_RIGHT) Then MoveEntity Collider, temp2*FPSfactor, 0, 0	
		
		ResetEntity Collider
	Else
		temp2# = temp2 / Max((Injuries+3.0)/3.0,1.0)
		If Injuries > 0.5 Then 
			temp2 = temp2*Min((Sin(Shake/2)+1.2),1.0)
		EndIf
		
		temp = False
		If (Not IsZombie%)
			If MoveInputCancelling Then
				MoveZ = KeyDown(KEY_DOWN) - KeyDown(KEY_UP)
				MoveX = KeyDown(KEY_LEFT) - KeyDown(KEY_RIGHT)
			Else
				If KeyHit(KEY_DOWN) Then
					MoveZ = 1
				Else If KeyHit(KEY_UP)
					MoveZ = -1
				Else If MoveZ = 1 And (Not KeyDown(KEY_DOWN)) Then
					MoveZ = -KeyDown(KEY_UP)
				Else If MoveZ = -1 And (Not KeyDown(KEY_UP)) Then
					MoveZ = KeyDown(KEY_DOWN)
				Else If MoveZ = 0 Then
					MoveZ = KeyDown(KEY_DOWN) - KeyDown(KEY_UP)
				EndIf

				If KeyHit(KEY_LEFT) Then
					MoveX = 1
				Else If KeyHit(KEY_RIGHT)
					MoveX = -1
				Else If MoveX = 1 And (Not KeyDown(KEY_LEFT)) Then
					MoveX = -KeyDown(KEY_RIGHT)
				Else If MoveX = -1 And (Not KeyDown(KEY_RIGHT)) Then
					MoveX = KeyDown(KEY_LEFT)
				Else If MoveX = 0 Then
					MoveX = KeyDown(KEY_LEFT) - KeyDown(KEY_RIGHT)
				EndIf
			EndIf

			If moveZ > 0 And Playable Then
				temp = True
				If moveX = 0 Then angle = 180 Else angle = 135 * moveX
			ElseIf moveZ < 0 And Playable Then
				temp = True
				If moveX = 0 Then angle = 0 Else angle = 45 * moveX
			ElseIf ForceMove>0 Then
				temp=True
				angle = ForceAngle
			Else If Playable And moveX <> 0 Then
				temp = True
				angle = 90 * moveX
			EndIf
		Else
			temp=True
			angle = ForceAngle
		EndIf
		
		angle = WrapAngle(EntityYaw(Collider,True)+angle+90.0)
		
		If temp Then 
			CurrSpeed = CurveValue(temp2, CurrSpeed, 20.0)
		Else
			CurrSpeed = Max(CurveValue(0.0, CurrSpeed-0.1, 1.0),0.0)
		EndIf
		
		If (Not UnableToMove%) Then TranslateEntity Collider, Cos(angle)*CurrSpeed * FPSfactor, 0, Sin(angle)*CurrSpeed * FPSfactor, True
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Collider)
			If CollisionY(Collider, i) < EntityY(Collider) - 0.25 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			If DropSpeed# < - 0.07 Then 
				If CurrStepSFX=0 Then
					PlaySound_Strict(StepSFX(GetStepSound(Collider), 0, Rand(0, 7)))
				ElseIf CurrStepSFX=1
					PlaySound_Strict(Step2SFX(Rand(0, 2)))
				ElseIf CurrStepSFX=2
					PlaySound_Strict(Step2SFX(Rand(3, 5)))
				ElseIf CurrStepSFX=3
					PlaySound_Strict(StepSFX(0, 0, Rand(0, 7)))
				EndIf
				PlayerSoundVolume = Max(3.0,PlayerSoundVolume)
			EndIf
			DropSpeed# = 0
		Else
			;DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
			If PlayerFallingPickDistance#<>0.0
				Local pick = LinePick(EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,-PlayerFallingPickDistance,0)
				If pick
					DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
				Else
					DropSpeed# = 0
				EndIf
			Else
				DropSpeed# = Min(Max(DropSpeed - 0.006 * FPSfactor, -2.0), 0.0)
			EndIf
		EndIf
		PlayerFallingPickDistance# = 10.0
		
		If (Not UnableToMove%) And ShouldEntitiesFall Then TranslateEntity Collider, 0, DropSpeed * FPSfactor, 0
	EndIf
	
	ForceMove = False
	
	If Injuries > 1.0 Then
		temp2 = Bloodloss
		BlurTimer = Max(Max(Sin(MilliSecs()/100.0)*Bloodloss*30.0,Bloodloss*2*(2.0-CrouchState)),BlurTimer)
		If (Not I_427\Using And I_427\Timer < 70*360) Then
			Bloodloss = Min(Bloodloss + (Min(Injuries,3.5)/300.0)*FPSfactor,100)
		EndIf
		
		If temp2 <= 60 And Bloodloss > 60 Then
			Msg = I_Loc\Message_BloodlossFaint
			MsgTimer = 70*4
		EndIf
	EndIf
	
	UpdateInfect()
	
	If Bloodloss > 0 Then
		If Rnd(200)<Min(Injuries,4.0) Then
			pvt = CreatePivot()
			PositionEntity pvt, EntityX(Collider)+Rnd(-0.05,0.05),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.05,0.05)
			TurnEntity pvt, 90, 0, 0
			EntityPick(pvt,0.3)
			de.decals = CreateDecal(Rand(15,16), PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
			de\size = Rnd(0.03,0.08)*Min(Injuries,3.0) : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\size, de\size
			tempchn% = PlaySound_Strict (DripSFX(Rand(0,2)))
			ChannelVolume tempchn, Rnd(0.0,0.8)*SFXVolume
			ChannelPitch tempchn, Rand(20000,30000)
			
			FreeEntity pvt
		EndIf
		
		CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs())/20.0)+1.0)*Bloodloss*0.2)
		
		If Bloodloss > 60 Then Crouch = True
		If Bloodloss => 100 Then 
			Kill()
			HeartBeatVolume = 0.0
		ElseIf Bloodloss > 80.0
			HeartBeatRate = Max(150-(Bloodloss-80)*5,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, 0.75+(Bloodloss-80.0)*0.0125)	
		ElseIf Bloodloss > 35.0
			HeartBeatRate = Max(70+Bloodloss,HeartBeatRate)
			HeartBeatVolume = Max(HeartBeatVolume, (Bloodloss-35.0)/60.0)			
		EndIf
	EndIf
	
	If HealTimer > 0 Then
		DebugLog HealTimer
		HealTimer = HealTimer - (FPSfactor / 70)
		Bloodloss = Min(Bloodloss + (2 / 400.0) * FPSfactor, 100)
		Injuries = Max(Injuries - (FPSfactor / 70) / 30, 0.0)
	EndIf
		
	If Playable Then
		If KeyHit(KEY_BLINK) Then BlinkTimer = 0
		If KeyDown(KEY_BLINK) And BlinkTimer < - 10 Then BlinkTimer = -10
	EndIf
	
	
	If HeartBeatVolume > 0 Then
		If HeartBeatTimer <= 0 Then
			tempchn = PlaySound_Strict (HeartBeatSFX)
			ChannelVolume tempchn, HeartBeatVolume*SFXVolume#
			
			HeartBeatTimer = 70.0*(60.0/Max(HeartBeatRate,1.0))
		Else
			HeartBeatTimer = HeartBeatTimer - FPSfactor
		EndIf
		
		HeartBeatVolume = Max(HeartBeatVolume - FPSfactor*0.05, 0)
	EndIf
	
	CatchErrors("MovePlayer")
End Function

Function ZoomCamera(fov%)
	CameraZoom(Camera, Min(1.0+(CurrCameraZoom/400.0),1.1) / Tan((ATan(Tan(fov%/2.0)*RealGraphicWidth/RealGraphicHeight))))
End Function

Function MouseLook()
	Local i%
	
	CameraShake = Max(CameraShake - (FPSfactor / 10), 0)
	
	;CameraZoomTemp = CurveValue(CurrCameraZoom,CameraZoomTemp, 5.0)
	ZoomCamera(FOV)
	CurrCameraZoom = Max(CurrCameraZoom - FPSfactor, 0)
	
	If KillTimer >= 0 And FallTimer >=0 Then
		
		HeadDropSpeed = 0
		
		;fixing the black screen bug with some bubblegum code 
		If IsNaN(EntityX(Collider)) Then
			PositionEntity Collider, EntityX(Camera, True), EntityY(Camera, True) - 0.5, EntityZ(Camera, True), True
			Msg = "EntityX(Collider) = NaN, RESETTING COORDINATES    -    New coordinates: "+EntityX(Collider)
			MsgTimer = 300				
		EndIf
		;EndIf
		
		Local up# = (Sin(Shake) / (20.0+CrouchState*20.0))*0.6;, side# = Cos(Shake / 2.0) / 35.0		
		Local roll# = Max(Min(Sin(Shake/2)*2.5*Min(Injuries+0.25,3.0),8.0),-8.0)
		
		;käännetään kameraa sivulle jos pelaaja on vammautunut
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), Max(Min(up*30*Injuries,50),-50)
		PositionEntity Camera, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
		RotateEntity Camera, 0, EntityYaw(Collider), roll*0.5
		
		MoveEntity Camera, side, up + 0.6 + CrouchState * -0.3, 0
		
		;RotateEntity Collider, EntityPitch(Collider), EntityYaw(Collider), 0
		;moveentity player, side, up, 0	
		; -- Update the smoothing que To smooth the movement of the mouse.
		mouse_x_speed_1# = CurveValue(MouseXSpeed() * (MouseSens + 0.6) , mouse_x_speed_1, (6.0 / (MouseSens + 1.0))*MouseSmooth) 
		If IsNaN(mouse_x_speed_1) Then mouse_x_speed_1 = 0
		If InvertMouse Then
			mouse_y_speed_1# = CurveValue(-MouseYSpeed() * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		Else
			mouse_y_speed_1# = CurveValue(MouseYSpeed () * (MouseSens + 0.6), mouse_y_speed_1, (6.0/(MouseSens+1.0))*MouseSmooth) 
		EndIf
		If IsNaN(mouse_y_speed_1) Then mouse_y_speed_1 = 0
		
		Local the_yaw# = ((mouse_x_speed_1#)) * mouselook_x_inc# / (1.0+WearingVest)
		Local the_pitch# = ((mouse_y_speed_1#)) * mouselook_y_inc# / (1.0+WearingVest)
		
		TurnEntity Collider, 0.0, -the_yaw#, 0.0 ; Turn the user on the Y (yaw) axis.
		user_camera_pitch# = user_camera_pitch# + the_pitch#
		; -- Limit the user;s camera To within 180 degrees of pitch rotation. ;EntityPitch(); returns useless values so we need To use a variable To keep track of the camera pitch.
		If user_camera_pitch# > 70.0 Then user_camera_pitch# = 70.0
		If user_camera_pitch# < - 70.0 Then user_camera_pitch# = -70.0
		
		RotateEntity Camera, WrapAngle(user_camera_pitch + Rnd(-CameraShake, CameraShake)), WrapAngle(EntityYaw(Collider) + Rnd(-CameraShake, CameraShake)), roll ; Pitch the user;s camera up And down.
		
		If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
			If EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale Then
				RotateEntity Camera, WrapAngle(EntityPitch(Camera)),WrapAngle(EntityYaw(Camera)), roll+WrapAngle(Sin(MilliSecs()/150.0)*30.0) ; Pitch the user;s camera up And down.
			EndIf
		EndIf
		
	Else
		HideEntity Collider
		PositionEntity Camera, EntityX(Head), EntityY(Head), EntityZ(Head)
		
		Local CollidedFloor% = False
		For i = 1 To CountCollisions(Head)
			If CollisionY(Head, i) < EntityY(Head) - 0.01 Then CollidedFloor = True
		Next
		
		If CollidedFloor = True Then
			HeadDropSpeed# = 0
		Else
			
			If KillAnim = 0 Then 
				MoveEntity Head, 0, 0, HeadDropSpeed
				RotateEntity(Head, CurveAngle(-90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) - 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			Else
				MoveEntity Head, 0, 0, -HeadDropSpeed
				RotateEntity(Head, CurveAngle(90.0, EntityPitch(Head), 20.0), EntityYaw(Head), EntityRoll(Head))
				RotateEntity(Camera, CurveAngle(EntityPitch(Head) + 40.0, EntityPitch(Camera), 40.0), EntityYaw(Camera), EntityRoll(Camera))
			EndIf
			
			HeadDropSpeed# = HeadDropSpeed - 0.002 * FPSfactor
		EndIf
		
		If InvertMouse Then
			TurnEntity (Camera, -MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		Else
			TurnEntity (Camera, MouseYSpeed() * 0.05 * FPSfactor, -MouseXSpeed() * 0.15 * FPSfactor, 0)
		End If
		
	EndIf
	
	;pölyhiukkasia
	If ParticleAmount=2
		If Rand(35) = 1 Then
			Local pvt% = CreatePivot()
			PositionEntity(pvt, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True))
			RotateEntity(pvt, 0, Rnd(360), 0)
			If Rand(2) = 1 Then
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			Else
				MoveEntity(pvt, 0, Rnd(-0.5, 0.5), Rnd(0.5, 1.0))
			End If
			
			Local p.Particles = CreateParticle(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 2, 0.002, 0, 300)
			p\speed = 0.001
			RotateEntity(p\pvt, Rnd(-20, 20), Rnd(360), 0)
			
			p\SizeChange = -0.00001
			
			FreeEntity pvt
		End If
	EndIf
	
	MoveMouse viewport_center_x, viewport_center_y
	
	If WearingGasMask Or WearingHazmat Or Wearing1499 Then
		If Wearing714 = False Then
			If WearingGasMask = 2 Or Wearing1499 = 2 Or WearingHazmat = 2 Then
				Stamina = Min(100, Stamina + (100.0-Stamina)*0.01*FPSfactor)
			EndIf
		EndIf
		If WearingHazmat = 1 Then
			Stamina = Min(60, Stamina)
		EndIf
		
		ShowEntity(GasMaskOverlay)
	Else
		HideEntity(GasMaskOverlay)
	End If
	
	If (Not WearingNightVision=0) Then
		ShowEntity(NVOverlay)
		If WearingNightVision=2 Then
			EntityColor(NVOverlay, 0,100,255)
			AmbientLightRooms(AmbientLightNVG)
		ElseIf WearingNightVision=3 Then
			EntityColor(NVOverlay, 255,0,0)
			AmbientLightRooms(AmbientLightNVG)
		Else
			EntityColor(NVOverlay, 0,255,0)
			AmbientLightRooms(AmbientLightNVG)
		EndIf
		EntityTexture(Fog, FogNVTexture)
	Else
		AmbientLightRooms(AmbientLight)
		HideEntity(NVOverlay)
		EntityTexture(Fog, FogTexture)
	EndIf
	
	For i = 0 To 5
		If SCP1025state[i]>0 Then
			Select i
				Case 0 ;common cold
					If FPSfactor>0 Then 
						If Rand(1000)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.3
				Case 1 ;chicken pox
					If Rand(9000)=1 And Msg="" Then
						Msg=I_Loc\Message_1025ChickenpoxItchy
						MsgTimer =70*4
					EndIf
				Case 2 ;cancer of the lungs
					If FPSfactor>0 Then 
						If Rand(800)=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
					EndIf
					Stamina = Stamina - FPSfactor * 0.1
				Case 3 ;appendicitis
					;0.035/sec = 2.1/min
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.0005
					EndIf
					If SCP1025state[i]>20.0 Then
						If SCP1025state[i]-FPSfactor<=20.0 Then Msg=I_Loc\Message_1025Appendicitis2 : MsgTimer = 70*4
						Stamina = Stamina - FPSfactor * 0.3
					ElseIf SCP1025state[i]>10.0
						If SCP1025state[i]-FPSfactor<=10.0 Then Msg=I_Loc\Message_1025Appendicitis1 : MsgTimer = 70*4
					EndIf
				Case 4 ;asthma
					If Stamina < 35 Then
						If Rand(Int(140+Stamina*8))=1 Then
							If CoughCHN = 0 Then
								CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							Else
								If Not ChannelPlaying(CoughCHN) Then CoughCHN = PlaySound_Strict(CoughSFX(Rand(0, 2)))
							End If
						EndIf
						CurrSpeed = CurveValue(0, CurrSpeed, 10+Stamina*15)
					EndIf
				Case 5;cardiac arrest
					If (Not I_427\Using And I_427\Timer < 70*360) Then
						SCP1025state[i]=SCP1025state[i]+FPSfactor*0.35
					EndIf
					;35/sec
					If SCP1025state[i]>110 Then
						HeartBeatRate=0
						BlurTimer = Max(BlurTimer, 500)
						If SCP1025state[i]>140 Then 
							DeathMSG = I_Loc\DeathMessage_1025Cardiacarrest
							Kill()
						EndIf
					Else
						HeartBeatRate=Max(HeartBeatRate, 70+SCP1025state[i])
						HeartBeatVolume = 1.0
					EndIf
			End Select 
		EndIf
	Next
	
	
End Function

;--------------------------------------- GUI, menu etc ------------------------------------------------

Function DrawGUI()
	CatchErrors("Uncaught (DrawGUI)")
	
	Local temp%, x%, y%, z%, i%, yawvalue#, pitchvalue#
	Local x2#,y2#,z2#
	Local n%, xtemp, ytemp, strtemp$
	
	Local e.Events, it.Items
	
	If IsAnyMenuOpen() Lor SelectedDoor <> Null Lor EndingTimer < 0 Then
		ShowPointer()
	Else
		HidePointer()
	EndIf 	
	
	If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then
		For e.Events = Each Events
			If e\room = PlayerRoom Then
				If Float(e\EventStr)<1000.0 Then
					If e\EventState > 600 Then
						If BlinkTimer < -3 And BlinkTimer > -10 Then
							If e\img = 0 Then
								If BlinkTimer > -5 And Rand(30)=1 Then
									PlaySound_Strict DripSFX(0)
									If e\img = 0 Then e\img = LoadImage_Strict("GFX\npcs\106face.jpg")
								EndIf
							Else
								DrawImage e\img, GraphicWidth/2-Rand(390,310), GraphicHeight/2-Rand(290,310)
							EndIf
						Else
							If e\img <> 0 Then FreeImage e\img : e\img = 0
						EndIf
							
						Exit
					EndIf
				Else
					If BlinkTimer < -3 And BlinkTimer > -10 Then
						If e\img = 0 Then
							If BlinkTimer > -5 Then
								If e\img = 0 Then
									e\img = LoadImage_Strict("GFX\kneelmortal.pd")
									If (ChannelPlaying(e\SoundCHN)) Then
										StopChannel(e\SoundCHN)
									EndIf
									e\SoundCHN = PlaySound_Strict(e\Sound)
								EndIf
							EndIf
						Else
							DrawImage e\img, GraphicWidth/2-Rand(390,310), GraphicHeight/2-Rand(290,310)
						EndIf
					Else
						If e\img <> 0 Then FreeImage e\img : e\img = 0
						If BlinkTimer < -3 Then
							If (Not ChannelPlaying(e\SoundCHN)) Then
								e\SoundCHN = PlaySound_Strict(e\Sound)
							EndIf
						Else
							If (ChannelPlaying(e\SoundCHN)) Then
								StopChannel(e\SoundCHN)
							EndIf
						EndIf
					EndIf
					
					Exit
				EndIf
			EndIf
		Next
	EndIf
	
	
	If ClosestButton <> 0 And (Not IsPaused()) Then
		temp% = CreatePivot()
		PositionEntity temp, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
		PointEntity temp, ClosestButton
		yawvalue# = WrapAngle(EntityYaw(Camera) - EntityYaw(temp))
		If yawvalue > 90 And yawvalue <= 180 Then yawvalue = 90
		If yawvalue > 180 And yawvalue < 270 Then yawvalue = 270
		pitchvalue# = WrapAngle(EntityPitch(Camera) - EntityPitch(temp))
		If pitchvalue > 90 And pitchvalue <= 180 Then pitchvalue = 90
		If pitchvalue > 180 And pitchvalue < 270 Then pitchvalue = 270
		
		FreeEntity (temp)
		
		DrawImage(HandIcon, GraphicWidth / 2 + Sin(yawvalue) * (GraphicWidth / 3) - 32 * HUDScale, GraphicHeight / 2 - Sin(pitchvalue) * (GraphicHeight / 3) - 32 * HUDScale)
		
		If MouseUp1 Then
			MouseUp1 = False
			If ClosestDoor <> Null Then 
				If ClosestDoor\Code <> "" Then
					SelectedDoor = ClosestDoor
				ElseIf Playable Then
					PlaySound2(ButtonSFX, Camera, ClosestButton)
					UseDoor(ClosestDoor,True)				
				EndIf
			EndIf
		EndIf
	EndIf
	
	If ClosestItem <> Null Then
		yawvalue# = -DeltaYaw(Camera, ClosestItem\collider)
		If yawvalue > 90 And yawvalue <= 180 Then yawvalue = 90
		If yawvalue > 180 And yawvalue < 270 Then yawvalue = 270
		pitchvalue# = -DeltaPitch(Camera, ClosestItem\collider)
		If pitchvalue > 90 And pitchvalue <= 180 Then pitchvalue = 90
		If pitchvalue > 180 And pitchvalue < 270 Then pitchvalue = 270
		
		DrawImage(HandIcon2, GraphicWidth / 2 + Sin(yawvalue) * (GraphicWidth / 3) - 32 * HUDScale, GraphicHeight / 2 - Sin(pitchvalue) * (GraphicHeight / 3) - 32 * HUDScale)
	EndIf
	
	If DrawHandIcon Then DrawImage(HandIcon, GraphicWidth / 2 - 32 * HUDScale, GraphicHeight / 2 - 32 * HUDScale)
	For i = 0 To 3
		If DrawArrowIcon(i) Then
			x = GraphicWidth / 2 - 32 * HUDScale
			y = GraphicHeight / 2 - 32 * HUDScale	
			Select i
				Case 0
					y = y - 64 * HUDScale - 5
				Case 1
					x = x + 64 * HUDScale + 5
				Case 2
					y = y + 64 * HUDScale + 5
				Case 3
					x = x - 5 - 64 * HUDScale
			End Select
			DrawImage(HandIcon, x, y)
			Color 0, 0, 0
			Rect(x + 4, y + 4, 64 * HUDScale - 8, 64 * HUDScale - 8)
			DrawImage(ArrowIMG(i), x + 21 * HUDScale, y + 21 * HUDScale)
			DrawArrowIcon(i) = False
		End If
	Next
	
	If Using294 Then Use294()
	
	If SelectedScreen <> Null Then
		DrawImage SelectedScreen\img, GraphicWidth/2-ImageWidth(SelectedScreen\img)/2,GraphicHeight/2-ImageHeight(SelectedScreen\img)/2
		
		If MouseUp1 Or MouseHit2 Then
			FreeImage SelectedScreen\img : SelectedScreen\img = 0
			SelectedScreen = Null
			MouseUp1 = False
		EndIf
	EndIf
	
	Local PrevInvOpen% = InvOpen, MouseSlot% = 66
	
	Local shouldDrawHUD%=True
	If SelectedDoor <> Null Then
		SelectedItem = Null
		
		If shouldDrawHUD Then
			ZoomCamera(DEFAULT_FOV)
			pvt = CreatePivot()
			PositionEntity pvt, EntityX(ClosestButton,True),EntityY(ClosestButton,True),EntityZ(ClosestButton,True)
			RotateEntity pvt, 0, EntityYaw(ClosestButton,True)-180,0
			MoveEntity pvt, 0,0,0.22
			PositionEntity Camera, EntityX(pvt),EntityY(pvt),EntityZ(pvt)
			PointEntity Camera, ClosestButton
			FreeEntity pvt	
			
			CameraProject(Camera, EntityX(ClosestButton,True),EntityY(ClosestButton,True)+MeshHeight(ButtonOBJ)*0.015,EntityZ(ClosestButton,True))
			projY# = ProjectedY()
			CameraProject(Camera, EntityX(ClosestButton,True),EntityY(ClosestButton,True)-MeshHeight(ButtonOBJ)*0.015,EntityZ(ClosestButton,True))
			scale# = (ProjectedY()-projy)/462.0
			
			x = GraphicWidth/2-317*scale/2
			y = GraphicHeight/2-462*scale/2
			
			Select True
				Case WearingNightVision=1 Color 0,255,0
				Case WearingNightVision=2 Color 0,0,255
				Case WearingNightVision=3 Color 255,0,0
			End Select
			SetFont Font3
			If KeypadMSG <> "" Then 
				KeypadTimer = KeypadTimer-FPSfactor2
				
				If (KeypadTimer Mod 70) < 35 Then Text GraphicWidth/2, y+124*scale, KeypadMSG, True,True
				If KeypadTimer =<0 Then
					KeypadMSG = ""
					SelectedDoor = Null
					MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
				EndIf
			Else
				Text GraphicWidth/2, y+70*scale, I_Loc\HUD_KeypadCode,True,True	
				SetFont Font4
				Text GraphicWidth/2, y+124*scale, KeypadInput,True,True	
			EndIf

			SetFont Font1
			
			x = x+44*scale
			y = y+249*scale
			
			For n = 0 To 3
				For i = 0 To 2
					xtemp = x+Int(58.5*scale*n)
					ytemp = y+(67*scale)*i
					
					temp = False
					If MouseOn(xtemp,ytemp, 54*scale,65*scale) And KeypadMSG = "" Then
						If MouseUp1 Then 
							PlaySound_Strict ButtonSFX
							
							Select (n+1)+(i*4)
								Case 1,2,3
									KeypadInput=KeypadInput + ((n+1)+(i*4))
								Case 4
									KeypadInput=KeypadInput + "0"
								Case 5,6,7
									KeypadInput=KeypadInput + ((n+1)+(i*4)-1)
								Case 8 ;enter
									If KeypadInput = SelectedDoor\Code Then
										PlaySound_Strict ScannerSFX1
										
										If SelectedDoor\Code = Str(AccessCode) Then
											GiveAchievement(AchvMaynard)
										ElseIf SelectedDoor\Code = Str(HARPCODE)
											GiveAchievement(AchvHarp)
										EndIf									
										
										SelectedDoor\locked = 0
										UseDoor(SelectedDoor,True)
										SelectedDoor = Null
										MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
									Else
										PlaySound_Strict ScannerSFX2
										KeypadMSG = I_Loc\HUD_KeypadDenied
										KeypadTimer = 210
										KeypadInput = ""	
									EndIf
								Case 9,10,11
									KeypadInput=KeypadInput + ((n+1)+(i*4)-2)
								Case 12
									KeypadInput = ""
							End Select 
							
							If Len(KeypadInput)> 4 Then KeypadInput = Left(KeypadInput,4)
						EndIf
						
					Else
						temp = False
					EndIf
					
				Next
			Next
			
			If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
			
			If MouseHit2 Then
				SelectedDoor = Null
				MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
			EndIf
		Else
			SelectedDoor = Null
		EndIf
	Else
		KeypadInput = ""
		KeypadTimer = 0
		KeypadMSG = ""
	EndIf
	
	If KeyHit(1) And EndingTimer=0 And (Not Using294) Then
		If (MenuOpen Or InvOpen) And OptionsMenu <> 0 Then SaveOptionsINI()
		MenuOpen = (Not MenuOpen)
		UpdateMenuState()
		
		AchievementsMenu = 0
		OptionsMenu = 0
		QuitMSG = 0
		
		SelectedDoor = Null
		SelectedScreen = Null
		SelectedMonitor = Null
		If SelectedItem <> Null Then
			If SelectedItem\itemtemplate\group = "vest" Or SelectedItem\itemtemplate\group = "hazmat" Then
				If (Not WearingVest) And (Not WearingHazmat) Then
					DropItem(SelectedItem)
				EndIf
				SelectedItem = Null
			EndIf
		EndIf
	EndIf
	
	Local spacing%
	Local PrevOtherOpen.Items
	
	Local OtherSize%,OtherAmount%
	
	Local isEmpty%
	
	Local isMouseOn%
	
	Local closedInv%
	
	If OtherOpen<>Null Then
		;[Block]
		PrevOtherOpen = OtherOpen
		OtherSize=OtherOpen\Inventory\Size;Int(OtherOpen\state2)
		
		For i%=0 To OtherSize-1
			If OtherOpen\Inventory\Items[i] <> Null Then
				OtherAmount = OtherAmount+1
			EndIf
		Next
		
		;If OtherAmount > 0 Then
		;	OtherOpen\state = 1.0
		;Else
		;	OtherOpen\state = 0.0
		;EndIf
		InvOpen = False
		SelectedDoor = Null
		Local tempX% = 0
		
		width% = 70 * HUDScale
		height% = 70 * HUDScale
		spacing% = 35 * HUDScale
		
		x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
		y = GraphicHeight / 2 - (height * OtherSize /5 + height * (OtherSize / 5 - 1)) / 2;height
		
		ItemAmount = 0
		For  n% = 0 To OtherSize - 1
			isMouseOn% = False
			If ScaledMouseX() > x And ScaledMouseX() < x + width Then
				If ScaledMouseY() > y And ScaledMouseY() < y + height Then
					isMouseOn = True
				EndIf
			EndIf
			
			If isMouseOn Then
				MouseSlot = n
				Color 255, 0, 0
				Rect(x - 1, y - 1, width + 2, height + 2)
			EndIf
			
			DrawFrame(x, y, width, height, (x Mod 64 * HUDScale), (x Mod 64 * HUDScale))
			
			If OtherOpen = Null Then Exit
			
			If OtherOpen\Inventory\Items[n] <> Null Then
				If (SelectedItem <> OtherOpen\Inventory\Items[n] Or isMouseOn) Then DrawImage(OtherOpen\Inventory\Items[n]\invimg, x + width / 2 - 32 * HUDScale, y + height / 2 - 32 * HUDScale)
			EndIf
			If OtherOpen\Inventory\Items[n] <> Null And SelectedItem <> OtherOpen\Inventory\Items[n] Then
			;drawimage(OtherOpen\Inventory\Items[n].InvIMG, x + width / 2 - 32 * HUDScale, y + height / 2 - 32 * HUDScale)
				If isMouseOn Then
					SetFont Font1
					Color 0,0,0
					Text(x + width / 2 + 1, y + height + spacing - 15 + 1, OtherOpen\Inventory\Items[n]\itemtemplate\displayname, True)
					Color 255, 255, 255	
					Text(x + width / 2, y + height + spacing - 15, OtherOpen\Inventory\Items[n]\itemtemplate\displayname, True)
					If SelectedItem = Null Then
						If MouseHit1 Then
							SelectedItem = OtherOpen\Inventory\Items[n]
							MouseHit1 = False
							
							If DoubleClick Then
								If OtherOpen\Inventory\Items[n]\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(OtherOpen\Inventory\Items[n]\itemtemplate\sound))
								OtherOpen = Null
								closedInv=True
								InvOpen = False
								DoubleClick = False
							EndIf
							
						EndIf
					Else
						
					EndIf
				EndIf
				
				ItemAmount=ItemAmount+1
			Else
				If isMouseOn And MouseHit1 Then
					For z% = 0 To OtherSize - 1
						If OtherOpen\Inventory\Items[z] = SelectedItem Then
							OtherOpen\Inventory\Items[z] = Null
							Exit
						EndIf
					Next
					OtherOpen\Inventory\Items[n] = SelectedItem
				EndIf
				
			EndIf					
			
			x=x+width + spacing
			tempX=tempX + 1
			If tempX = 5 Then 
				tempX=0
				y = y + height*2 
				x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
			EndIf
		Next
		
		If SelectedItem <> Null Then
			If MouseDown1 Then
				If MouseSlot = 66 Then
					DrawImage(SelectedItem\invimg, ScaledMouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, ScaledMouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				ElseIf SelectedItem <> PrevOtherOpen\Inventory\Items[MouseSlot]
					DrawImage(SelectedItem\invimg, ScaledMouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, ScaledMouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				EndIf
			Else
				If MouseSlot = 66 Then
					If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
					
					ShowEntity(SelectedItem\collider)
					PositionEntity(SelectedItem\collider, EntityX(Camera), EntityY(Camera), EntityZ(Camera))
					RotateEntity(SelectedItem\collider, EntityPitch(Camera), EntityYaw(Camera), 0)
					MoveEntity(SelectedItem\collider, 0, -0.1, 0.1)
					RotateEntity(SelectedItem\collider, 0, Rand(360), 0)
					ResetEntity (SelectedItem\collider)
					;move the item so that it doesn't overlap with other items
					;For it.Items = Each Items
					;	If it <> SelectedItem And it\Picked = False Then
					;		x = Abs(EntityX(SelectedItem\collider, True)-EntityX(it\collider, True))
					;		If x < 0.2 Then 
					;			z = Abs(EntityZ(SelectedItem\collider, True)-EntityZ(it\collider, True))
					;			If z < 0.2 Then
					;				While (x+z)<0.25
					;					MoveEntity(SelectedItem\collider, 0, 0, 0.025)
					;					x = Abs(EntityX(SelectedItem\collider, True)-EntityX(it\collider, True))
					;					z = Abs(EntityZ(SelectedItem\collider, True)-EntityZ(it\collider, True))
					;				Wend
					;			EndIf
					;		EndIf
					;	EndIf
					;Next
					
					SelectedItem\DropSpeed = 0.0
					
					SelectedItem\Picked = False
					For z% = 0 To OtherSize - 1
						If OtherOpen\Inventory\Items[z] = SelectedItem Then
							OtherOpen\Inventory\Items[z] = Null
							Exit
						EndIf
					Next
					
					isEmpty=True
					If OtherOpen\itemtemplate\name = "wallet" Then
						If (Not isEmpty) Then
							For z% = 0 To OtherSize - 1
								If OtherOpen\Inventory\Items[z]<>Null
									Local name$=OtherOpen\Inventory\Items[z]\itemtemplate\name
									If name$<>"25ct" And name$<>"coin" And name$<>"key" And name$<>"scp860" And name$<>"scp714" Then
										isEmpty=False
										Exit
									EndIf
								EndIf
							Next
						EndIf
					Else
						For z% = 0 To OtherSize - 1
							If OtherOpen\Inventory\Items[z]<>Null
								isEmpty = False
								Exit
							EndIf
						Next
					EndIf
					
					If isEmpty Then
						Select OtherOpen\itemtemplate\name
							Case "clipboard"
								OtherOpen\invimg = OtherOpen\itemtemplate\invimg2
								SetAnimTime OtherOpen\model,17.0
							Case "wallet"
								SetAnimTime OtherOpen\model,0.0
						End Select
					EndIf
					
					SelectedItem = Null
					OtherOpen = Null
					closedInv=True
					
					MoveMouse viewport_center_x, viewport_center_y
				Else
					
					If PrevOtherOpen\Inventory\Items[MouseSlot] = Null Then
						For z% = 0 To OtherSize - 1
							If PrevOtherOpen\Inventory\Items[z] = SelectedItem Then
								PrevOtherOpen\Inventory\Items[z] = Null
								Exit
							EndIf
						Next
						PrevOtherOpen\Inventory\Items[MouseSlot] = SelectedItem
						SelectedItem = Null
					ElseIf PrevOtherOpen\Inventory\Items[MouseSlot] <> SelectedItem
						Msg = I_Loc\MessageItem_Cantcombine
						MsgTimer = 70 * 5
					EndIf
					
				EndIf
				SelectedItem = Null
			EndIf
		EndIf
		
		If Fullscreen Then DrawImage CursorIMG,ScaledMouseX(),ScaledMouseY()
		If (closedInv) And (Not InvOpen) Then 
			OtherOpen=Null
			UpdateMenuState()
		EndIf
		;[End Block]
		
	Else If InvOpen Then
		SelectedDoor = Null
		
		width% = 70 * HUDScale
		height% = 70 * HUDScale
		spacing% = 35 * HUDScale
		
		x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
		y = GraphicHeight / 2 - (height * MaxItemAmount /5 + height * (MaxItemAmount / 5 - 1)) / 2
		
		ItemAmount = 0
		For  n% = 0 To MaxItemAmount - 1
			isMouseOn% = False
			If ScaledMouseX() > x And ScaledMouseX() < x + width Then
				If ScaledMouseY() > y And ScaledMouseY() < y + height Then
					isMouseOn = True
				End If
			EndIf
			
			If Inventory(n) <> Null Then
				Color 200, 200, 200
				Select Inventory(n)\itemtemplate\name 
					Case "gasmask"
						If WearingGasMask=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "supergasmask"
						If WearingGasMask=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "gasmask3"
						If WearingGasMask=3 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "hazmatsuit"
						If WearingHazmat=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "hazmatsuit2"
						If WearingHazmat=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "hazmatsuit3"
						If WearingHazmat=3 Then Rect(x - 3, y - 3, width + 6, height + 6)	
					Case "vest"
						If WearingVest=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "finevest"
						If WearingVest=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "scp714"
						If Wearing714=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
						;BoH items
					;Case "ring"
					;	If Wearing714=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					;Case "scp178"
					;	If Wearing178=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					;Case "glasses"
					;	If Wearing178=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "nvgoggles"
						If WearingNightVision=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "supernv"
						If WearingNightVision=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "scp1499"
						If Wearing1499=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "super1499"
						If Wearing1499=2 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "finenvgoggles"
						If WearingNightVision=3 Then Rect(x - 3, y - 3, width + 6, height + 6)
					Case "scp427"
						If I_427\Using=1 Then Rect(x - 3, y - 3, width + 6, height + 6)
				End Select
			EndIf
			
			If isMouseOn Then
				MouseSlot = n
				Color 255, 0, 0
				Rect(x - 1, y - 1, width + 2, height + 2)
			EndIf
			
			Color 255, 255, 255
			DrawFrame(x, y, width, height, (x Mod 64 * HUDScale), (x Mod 64 * HUDScale))
			
			If Inventory(n) <> Null Then
				If (SelectedItem <> Inventory(n) Or isMouseOn) Then 
					DrawImage(Inventory(n)\invimg, x + width / 2 - 32 * HUDScale, y + height / 2 - 32 * HUDScale)
				EndIf
			EndIf
			
			If Inventory(n) <> Null And SelectedItem <> Inventory(n) Then
				;drawimage(Inventory(n).InvIMG, x + width / 2 - 32 * HUDScale, y + height / 2 - 32 * HUDScale)
				If isMouseOn Then
					If SelectedItem = Null Then
						If MouseHit1 Then
							SelectedItem = Inventory(n)
							MouseHit1 = False
							
							If DoubleClick Then
								If WearingHazmat > 0 And SelectedItem\itemtemplate\group <> "hazmat" Then
									Msg = I_Loc\MessageItem_HazmatNouseAny
									MsgTimer = 70*5
									SelectedItem = Null
									Return
								EndIf
								If Inventory(n)\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(Inventory(n)\itemtemplate\sound))
								InvOpen = False
								DoubleClick = False
							EndIf
							
						EndIf
						
						SetFont Font1
						Color 0,0,0
						Text(x + width / 2 + 1, y + height + spacing - 15 + 1, Inventory(n)\displayname, True)							
						Color 255, 255, 255	
						Text(x + width / 2, y + height + spacing - 15, Inventory(n)\displayname, True)	
						
					EndIf
				EndIf
				
				ItemAmount=ItemAmount+1
			Else
				If isMouseOn And MouseHit1 Then
					For z% = 0 To MaxItemAmount - 1
						If Inventory(z) = SelectedItem Then
							Inventory(z) = Null
							Exit
						EndIf
					Next
					Inventory(n) = SelectedItem
				End If
				
			EndIf					
			
			x=x+width + spacing
			If n = 4 Then 
				y = y + height*2 
				x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
			EndIf
		Next
		
		If SelectedItem <> Null Then
			If MouseDown1 Then
				If MouseSlot = 66 Then
					DrawImage(SelectedItem\invimg, ScaledMouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, ScaledMouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				ElseIf SelectedItem <> Inventory(MouseSlot)
					DrawImage(SelectedItem\invimg, ScaledMouseX() - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, ScaledMouseY() - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
				EndIf
			Else
				If MouseSlot = 66 Then
					Select SelectedItem\itemtemplate\name
						Case "vest","finevest","hazmatsuit","hazmatsuit2","hazmatsuit3"
							Msg = I_Loc\MessageHelp_Remove
							MsgTimer = 70*5
						Case "scp1499","super1499"
							If Wearing1499>0 Then
								Msg = I_Loc\MessageHelp_Remove
								MsgTimer = 70*5
							Else
								DropItem(SelectedItem)
								SelectedItem = Null
								InvOpen = False
							EndIf
						Default
							DropItem(SelectedItem)
							SelectedItem = Null
							InvOpen = False
					End Select
					
					MoveMouse viewport_center_x, viewport_center_y
				Else
					If Inventory(MouseSlot) = Null Then
						For z% = 0 To MaxItemAmount - 1
							If Inventory(z) = SelectedItem Then
								Inventory(z) = Null
								Exit
							EndIf
						Next
						Inventory(MouseSlot) = SelectedItem
						SelectedItem = Null
					ElseIf Inventory(MouseSlot) <> SelectedItem
						Local groupSelector$ = ""
						If SelectedItem\itemtemplate\group = "paper" Lor SelectedItem\itemtemplate\group = "misc" Then groupSelector = SelectedItem\itemtemplate\name
						Select SelectedItem\itemtemplate\name
							Case groupSelector,"key1","key2","key3","key4","key5","key6","oldpaper","badge","oldbadge","ticket","25ct","coin","key","scp860"
								;[Block]
								If Inventory(MouseSlot)\itemtemplate\name = "clipboard" Then
									;Add an item to clipboard
									Local added.Items = Null
									Local b$ = SelectedItem\itemtemplate\group
									Local b2$ = SelectedItem\itemtemplate\name
									If (b<>"misc" And b2<>"25ct" And b2<>"coin" And b2<>"key" And b2<>"scp860" And b2<>"scp714") Or (b2="playingcard" Or b2="mastercard") Then
										For c% = 0 To Inventory(MouseSlot)\Inventory\Size-1
											If (Inventory(MouseSlot)\Inventory\Items[c] = Null)
												If SelectedItem <> Null Then
													Inventory(MouseSlot)\Inventory\Items[c] = SelectedItem
													Inventory(MouseSlot)\state = 1.0
													SetAnimTime Inventory(MouseSlot)\model,0.0
													Inventory(MouseSlot)\invimg = Inventory(MouseSlot)\itemtemplate\invimg
													
													For ri% = 0 To MaxItemAmount - 1
														If Inventory(ri) = SelectedItem Then
															Inventory(ri) = Null
															PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
															Exit
														EndIf
													Next
													added = SelectedItem
													SelectedItem = Null : Exit
												EndIf
											EndIf
										Next
										If SelectedItem <> Null Then
											Msg = I_Loc\MessageItem_ClipboardFull
										Else
											If added\itemtemplate\group = "paper" Or added\itemtemplate\name = "oldpaper" Then
												Msg = I_Loc\MessageItem_ClipboardAddPaper
											ElseIf added\itemtemplate\name = "badge" Lor added\itemtemplate\name = "oldbadge"
												Msg = Format(I_Loc\MessageItem_ClipboardAddBadge, added\itemtemplate\displayname)
											Else
												Msg = Format(I_Loc\MessageItem_ClipboardAdd, added\itemtemplate\displayname)
											EndIf
											
										EndIf
										MsgTimer = 70 * 5
									Else
										Msg = I_Loc\MessageItem_Cantcombine
										MsgTimer = 70 * 5
									EndIf
								ElseIf Inventory(MouseSlot)\itemtemplate\name = "wallet" Then
									;Add an item to clipboard
									added.Items = Null
									b$ = SelectedItem\itemtemplate\group
									b2$ = SelectedItem\itemtemplate\name
									If (b<>"misc" And b<>"paper" And b2<>"oldpaper") Or (b2="playingcard" Or b2="mastercard") Then
										For c% = 0 To Inventory(MouseSlot)\Inventory\Size-1
											If (Inventory(MouseSlot)\Inventory\Items[c] = Null)
												If SelectedItem <> Null Then
													Inventory(MouseSlot)\Inventory\Items[c] = SelectedItem
													Inventory(MouseSlot)\state = 1.0
													If b2<>"25ct" And b2<>"coin" And b2<>"key" And b2<>"scp860"
														SetAnimTime Inventory(MouseSlot)\model,3.0
													EndIf
													Inventory(MouseSlot)\invimg = Inventory(MouseSlot)\itemtemplate\invimg
													
													For ri% = 0 To MaxItemAmount - 1
														If Inventory(ri) = SelectedItem Then
															Inventory(ri) = Null
															PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
															Exit
														EndIf
													Next
													added = SelectedItem
													SelectedItem = Null : Exit
												EndIf
											EndIf
										Next
										If SelectedItem <> Null Then
											Msg = I_Loc\MessageItem_WalletFull
										Else
											Msg = Format(I_Loc\MessageItem_WalletAdd, added\itemtemplate\displayname)
										EndIf
										
										MsgTimer = 70 * 5
									Else
										Msg = I_Loc\MessageItem_Cantcombine
										MsgTimer = 70 * 5
									EndIf
								Else
									Msg = I_Loc\MessageItem_Cantcombine
									MsgTimer = 70 * 5
								EndIf
								SelectedItem = Null
								
								;[End Block]
							Case "bat"
								;[Block]
								Select Inventory(MouseSlot)\itemtemplate\name
									Case "snav", "snav300", "snav310"
										If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))	
										RemoveItem (SelectedItem)
										SelectedItem = Null
										Inventory(MouseSlot)\state = 100.0
										Msg = I_Loc\MessageItem_NavBatReplace
										MsgTimer = 70 * 5
									Case "snavulti"
										Msg = I_Loc\MessageItem_NavBatNoplace
										MsgTimer = 70 * 5
									Case "radio"
										If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))	
										RemoveItem (SelectedItem)
										SelectedItem = Null
										Inventory(MouseSlot)\state = 100.0
										Msg = I_Loc\MessageItem_RadioBatReplace
										MsgTimer = 70 * 5
									Case "fineradio", "veryfineradio"
										Msg = I_Loc\MessageItem_RadioBatNoplace
										MsgTimer = 70 * 5
									Case "18vradio"
										Msg = I_Loc\MessageItem_RadioBatNofit
										MsgTimer = 70 * 5
									Case "supernv", "nvgoggles"
										If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))	
										RemoveItem (SelectedItem)
										SelectedItem = Null
										Inventory(MouseSlot)\state = 1000.0
										Msg = I_Loc\MessageItem_NvgBatReplace
										MsgTimer = 70 * 5
									Case "finenvgoggles"
										Msg = I_Loc\MessageItem_NvgBatNoplace
										MsgTimer = 70 * 5
									Default
										Msg = I_Loc\MessageItem_Cantcombine
										MsgTimer = 70 * 5	
								End Select
								;[End Block]
							Case "18vbat"
								;[Block]
								Select Inventory(MouseSlot)\itemtemplate\name
									Case "snav", "snav300", "snav310"
										Msg = I_Loc\MessageItem_NavBatNofit
										MsgTimer = 70 * 5
									Case "snavulti"
										Msg = I_Loc\MessageItem_NavBatNoplace
										MsgTimer = 70 * 5
									Case "radio"
										Msg = I_Loc\MessageItem_RadioBatNofit
										MsgTimer = 70 * 5
									Case "fineradio", "veryfineradio"
										Msg = I_Loc\MessageItem_RadioBatNoplace
										MsgTimer = 70 * 5
									Case "18vradio"
										If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))	
										RemoveItem (SelectedItem)
										SelectedItem = Null
										Inventory(MouseSlot)\state = 100.0
										Msg = I_Loc\MessageItem_RadioBatReplace
										MsgTimer = 70 * 5
									Default
										Msg = I_Loc\MessageItem_Cantcombine
										MsgTimer = 70 * 5
								End Select
								;[End Block]
							Default
								;[Block]
								Msg = I_Loc\MessageItem_Cantcombine
								MsgTimer = 70 * 5
								;[End Block]
						End Select					
					End If
					
				End If
				SelectedItem = Null
			End If
		End If
		
		If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
		
		If InvOpen = False Then 
			UpdateMenuState()
		EndIf
	Else ;invopen = False
		
		If SelectedItem <> Null Then
			Select SelectedItem\itemtemplate\name
				Case "nvgoggles"
					;[Block]
					If Wearing1499 = 0 And WearingHazmat=0 Then
						If WearingNightVision = 1 Then
							Msg = I_Loc\MessageItem_NvgOff
							CameraFogFar = StoredCameraFogFar
						Else
							Msg = I_Loc\MessageItem_NvgOn
							WearingGasMask = 0
							WearingNightVision = 0
							StoredCameraFogFar = CameraFogFar
							CameraFogFar = 30
						EndIf
						
						WearingNightVision = (Not WearingNightVision)
					ElseIf Wearing1499 > 0 Then
						Msg = I_Loc\MessageItem_NvgConflictScp1499
					Else
						Msg = I_Loc\MessageItem_NvgConflictHazmat
					EndIf
					SelectedItem = Null
					MsgTimer = 70 * 5
					;[End Block]
				Case "supernv"
					;[Block]
					If Wearing1499 = 0 And WearingHazmat=0 Then
						If WearingNightVision = 2 Then
							Msg = I_Loc\MessageItem_NvgOff
							CameraFogFar = StoredCameraFogFar
						Else
							Msg = I_Loc\MessageItem_NvgOn
							WearingGasMask = 0
							WearingNightVision = 0
							StoredCameraFogFar = CameraFogFar
							CameraFogFar = 30
						EndIf
						
						WearingNightVision = (Not WearingNightVision) * 2
					ElseIf Wearing1499 > 0 Then
						Msg = I_Loc\MessageItem_NvgConflictScp1499
					Else
						Msg = I_Loc\MessageItem_NvgConflictHazmat
					EndIf
					SelectedItem = Null
					MsgTimer = 70 * 5
					;[End Block]
				Case "finenvgoggles"
					;[Block]
					If Wearing1499 = 0 And WearingHazmat = 0 Then
						If WearingNightVision = 3 Then
							Msg = I_Loc\MessageItem_NvgOff
							CameraFogFar = StoredCameraFogFar
						Else
							Msg = I_Loc\MessageItem_NvgOn
							WearingGasMask = 0
							WearingNightVision = 0
							StoredCameraFogFar = CameraFogFar
							CameraFogFar = 30
						EndIf
						
						WearingNightVision = (Not WearingNightVision) * 3
					ElseIf Wearing1499 > 0 Then
						Msg = I_Loc\MessageItem_NvgConflictScp1499
					Else
						Msg = I_Loc\MessageItem_NvgConflictHazmat
					EndIf
					SelectedItem = Null
					MsgTimer = 70 * 5
					;[End Block]
				Case "scp1123"
					;[Block]
					If Not (Wearing714 = 1) Then
						If PlayerRoom\RoomTemplate\Name <> "room1123" Then
							ShowEntity Light
							LightFlash = 7
							PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Touch.ogg"))		
							DeathMSG = I_Loc\DeathMessage_1123
							Kill()
							Return
						EndIf
						For e.Events = Each Events
							If e\EventName = "room1123" Then 
								If e\EventState = 0 Then
									ShowEntity Light
									LightFlash = 3
									PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Touch.ogg"))		
								EndIf
								e\EventState = Max(1, e\EventState)
								Exit
							EndIf
						Next
					EndIf
					;[End Block]
				Case "key1", "key2", "key3", "key4", "key5", "key6", "keyomni", "scp860", "hand", "hand2", "25ct"
					;[Block]
					DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
					;[End Block]
				Case "scp513"
					;[Block]
					PlaySound_Strict LoadTempSound("SFX\SCP\513\Bell1.ogg")
					
					If Curr5131 = Null
						Curr5131 = CreateNPC(NPCtype5131, 0,0,0)
					EndIf	
					SelectedItem = Null
					;[End Block]
				Case "scp500"
					;[Block]
					If CanUseItem(False, False, True)
						GiveAchievement(Achv500)
						
						If Infect > 0 Then
							Msg = I_Loc\MessageItem_PillUseHealnausea
						Else
							Msg = I_Loc\MessageItem_PillUse
						EndIf
						MsgTimer = 70*7
						
						ResetDiseases()
						
						RemoveItem(SelectedItem)
						SelectedItem = Null
					EndIf	
					;[End Block]
				Case "veryfinefirstaid"
					;[Block]
					If CanUseItem(False, False, True)
						Select Rand(5)
							Case 1
								Injuries = 3.5
								Msg = I_Loc\MessageItem_VeryfinefirstaidUseHurt
								MsgTimer = 70*7
							Case 2
								Injuries = 0
								Bloodloss = 0
								Msg = I_Loc\MessageItem_VeryfinefirstaidUseFullheal
								MsgTimer = 70*7
							Case 3
								Injuries = Max(0, Injuries - Rnd(0.5,3.5))
								Bloodloss = Max(0, Bloodloss - Rnd(10,100))
								Msg = I_Loc\MessageItem_VeryfinefirstaidUseHeal
								MsgTimer = 70*7
							Case 4
								BlurTimer = 10000
								Bloodloss = 0
								Msg = I_Loc\MessageItem_VeryfinefirstaidUseNausea
								MsgTimer = 70*7
							Case 5
								BlinkTimer = -10
								Local roomname$ = PlayerRoom\RoomTemplate\Name
								If roomname = "dimension1499" Or roomname = "gatea" Or (roomname="exit1" And EntityY(Collider)>1040.0*RoomScale)
									Injuries = 2.5
									Msg = I_Loc\MessageItem_VeryfinefirstaidUseHurt
									MsgTimer = 70*7
								Else
									For r.Rooms = Each Rooms
										If r\RoomTemplate\Name = "pocketdimension" Then
											PositionEntity(Collider, EntityX(r\obj),0.8,EntityZ(r\obj))		
											ResetEntity Collider									
											UpdateDoors()
											UpdateRooms()
											PlaySound_Strict(Use914SFX)
											DropSpeed = 0
											Curr106\State = -2500
											Exit
										EndIf
									Next
									Msg = I_Loc\MessageItem_VeryfinefirstaidUsePocketdimension
									MsgTimer = 70*8
								EndIf
						End Select
						
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "firstaid", "finefirstaid", "firstaid2"
					;[Block]
					If Bloodloss = 0 And Injuries = 0 Then
						Msg = I_Loc\MessageItem_FirstaidUseFull
						MsgTimer = 70*5
						SelectedItem = Null
					Else
						If CanUseItem(False, True, True)
							CurrSpeed = CurveValue(0, CurrSpeed, 5.0)
							Crouch = True
							
							DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
							
							DrawBar(BlinkMeterIMG, GraphicWidth / 2, GraphicHeight / 2 + 80 * HUDScale, 300 * HUDScale, SelectedItem\state / 100.0, True)
							
							SelectedItem\state = Min(SelectedItem\state+(FPSfactor/5.0),100)			
							
							If SelectedItem\state = 100 Then
								If SelectedItem\itemtemplate\name = "finefirstaid" Then
									Bloodloss = 0
									Injuries = Max(0, Injuries - 2.0)
									If Injuries = 0 Then
										Msg = I_Loc\MessageItem_FinefirstaidUse1
									ElseIf Injuries > 1.0
										Msg = I_Loc\MessageItem_FinefirstaidUse2
									Else
										Msg = I_Loc\MessageItem_FinefirstaidUse3
									EndIf
									MsgTimer = 70*5
									RemoveItem(SelectedItem)
								Else
									Bloodloss = Max(0, Bloodloss - Rand(10,20))
									If Injuries => 2.5 Then
										Msg = I_Loc\MessageItem_FirstaidUse5
										Injuries = Max(2.5, Injuries-Rnd(0.3,0.7))
									ElseIf Injuries > 1.0
										Injuries = Max(0.5, Injuries-Rnd(0.5,1.0))
										If Injuries > 1.0 Then
											Msg = I_Loc\MessageItem_FirstaidUse4
										Else
											Msg = I_Loc\MessageItem_FirstaidUse3
										EndIf
									Else
										If Injuries > 0.5 Then
											Injuries = 0.5
											Msg = I_Loc\MessageItem_FirstaidUse2
										Else
											Injuries = 0.5
											Msg = I_Loc\MessageItem_FirstaidUse1
										EndIf
									EndIf
									
									If SelectedItem\itemtemplate\name = "firstaid2" Then 
										Select Rand(6)
											Case 1
												SuperMan = True
												Msg = I_Loc\MessageItem_BluefirstaidUseSuperman
											Case 2
												InvertMouse = (Not InvertMouse)
												Msg = I_Loc\MessageItem_BluefirstaidUseInvert
											Case 3
												BlurTimer = 5000
												Msg = I_Loc\MessageItem_BluefirstaidUseNausea
											Case 4
												BlinkEffect = 0.6
												BlinkEffectTimer = Rand(20,30)
											Case 5
												Bloodloss = 0
												Injuries = 0
												Msg = I_Loc\MessageItem_BluefirstaidUseHeal
											Case 6
												Msg = I_Loc\MessageItem_BluefirstaidUseHurt
												Injuries = 3.5
										End Select
									EndIf
									
									MsgTimer = 70*5
									RemoveItem(SelectedItem)
								EndIf							
							EndIf
						EndIf
					EndIf
					;[End Block]
				Case "eyedrops","redeyedrops"
					;[Block]
					If CanUseItem(False,False,False)
						If (Not (Wearing714=1)) Then ;wtf is this
							BlinkEffect = 0.6
							BlinkEffectTimer = Rand(20,30)
							BlurTimer = 200
						EndIf
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "fineeyedrops"
					;[Block]
					If CanUseItem(False,False,False)
						If (Not (Wearing714=1)) Then 
							BlinkEffect = 0.4
							BlinkEffectTimer = Rand(30,40)
							Bloodloss = Max(Bloodloss-1.0, 0)
							BlurTimer = 200
						EndIf
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "supereyedrops"
					;[Block]
					If CanUseItem(False,False,False)
						If (Not (Wearing714 = 1)) Then
							BlinkEffect = 0.0
							BlinkEffectTimer = 60
							EyeStuck = 10000
						EndIf
						BlurTimer = 1000
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "scp1025"
					;[Block]
					GiveAchievement(Achv1025) 
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\state = Rand(0,5)
						SelectedItem\itemtemplate\img=LoadImage_Strict("GFX\items\1025\1025_"+Int(SelectedItem\state)+".jpg")	
						SelectedItem\itemtemplate\img = ResizeImage2(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
						
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					If (Not Wearing714) Then SCP1025state[SelectedItem\state]=Max(1,SCP1025state[SelectedItem\state])
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
					;[End Block]
				Case "cup"
					;[Block]
					If CanUseItem(False,False,True)
						strtemp = SelectedItem\drinkName

						Local iniStr$ = "DATA\SCP-294.ini"
						Local loc% = -1
						Local hasOverride%
						For m.ActiveMods = Each ActiveMods
							Local modIniStr$ = m\Path + iniStr
							If FileType(modIniStr) = 1 Then
								Local sectionLocation = GetINISectionLocation(modIniStr, strtemp)
								If sectionLocation <> -1 Then
									iniStr = modIniStr
									loc = sectionLocation
									Exit
								EndIf
								If FileType(modIniStr + ".OVERRIDE") Then
									hasOverride = True
									Exit
								EndIf
							EndIf
						Next

						If loc = -1 And (Not hasOverride) Then
							loc% = GetINISectionLocation(iniStr, strtemp)
						EndIf

						;Stop
						
						strtemp = GetINIString2(iniStr, loc, "sound")
						If strtemp <> "" Then PlaySound_Strict LoadTempSound(strtemp)
						
						strtemp = GetINIString2(iniStr, loc, "message")
						If strtemp <> "" Then Msg = strtemp : MsgTimer = 70*6
						
						If GetINIInt2(iniStr, loc, "lethal") Or GetINIInt2(iniStr, loc, "deathtimer") Then 
							DeathMSG = GetINIString2(iniStr, loc, "deathmessage")
							If GetINIInt2(iniStr, loc, "lethal") Then Kill()
						EndIf
						
						BlurTimer = Max(BlurTimer + GetINIInt2(iniStr, loc, "blur")*70, 0);*temp
						CameraShakeTimer = Max(CameraShakeTimer + GetINIString2(iniStr, loc, "camerashake"), 0)
						
						temp = GetINIInt2(iniStr, loc, "vomit")
						If temp > 0 Then
							If VomitTimer = 0 Then
								VomitTimer = temp
							Else
								VomitTimer = Min(VomitTimer, temp)
							EndIf
						EndIf
						
						temp = GetINIInt2(iniStr, loc, "deathtimer")*70
						If temp > 0 Then
							If DeathTimer = 0 Then
								DeathTimer = temp
							Else
								DeathTimer = Min(DeathTimer, temp)
							EndIf
						EndIf
						
						Injuries = Max(Injuries + GetINIInt2(iniStr, loc, "damage"), 0);*temp
						Bloodloss = Max(Bloodloss + GetINIInt2(iniStr, loc, "blood loss"), 0);*temp
						
						If GetINIInt2(iniStr, loc, "stomachache") Then SCP1025state[3]=1
						
						;the state of refined drinks is more than 1.0 (fine setting increases it by 1, very fine doubles it)
						strtemp = GetINIString2(iniStr, loc, "blink effect")
						If strtemp <> "" Then BlinkEffect = Float(strtemp)^SelectedItem\state
						strtemp = GetINIString2(iniStr, loc, "blink effect timer")
						If strtemp <> "" Then BlinkEffectTimer = Float(strtemp)*SelectedItem\state
						strtemp = GetINIString2(iniStr, loc, "stamina effect")
						If strtemp <> "" Then StaminaEffect = Float(strtemp)^SelectedItem\state
						strtemp = GetINIString2(iniStr, loc, "stamina effect timer")
						If strtemp <> "" Then StaminaEffectTimer = Float(strtemp)*SelectedItem\state
						
						strtemp = GetINIString2(iniStr, loc, "refusemessage")
						If strtemp <> "" Then
							Msg = strtemp 
							MsgTimer = 70*6		
						Else
							it.Items = CreateItem("emptycup", 0,0,0)
							it\Picked = True
							For i = 0 To MaxItemAmount-1
								If Inventory(i)=SelectedItem Then Inventory(i) = it : Exit
							Next					
							EntityType (it\collider, HIT_ITEM)
							
							RemoveItem(SelectedItem)						
						EndIf
						
						SelectedItem = Null
					EndIf
					;[End Block]
				Case "syringe"
					;[Block]
					If CanUseItem(False,True,True)
						HealTimer = 30
						StaminaEffect = 0.5
						StaminaEffectTimer = 20
						
						Msg = I_Loc\MessageItem_SyringeUse
						MsgTimer = 70 * 8
						
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "finesyringe"
					;[Block]
					If CanUseItem(False,True,True)
						HealTimer = Rnd(20, 40)
						StaminaEffect = Rnd(0.5, 0.8)
						StaminaEffectTimer = Rnd(20, 30)
						
						Msg = I_Loc\MessageItem_FinesyringeUse
						MsgTimer = 70 * 8
						
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "veryfinesyringe"
					;[Block]
					If CanUseItem(False,True,True)
						Select Rand(3)
							Case 1
								HealTimer = Rnd(40, 60)
								StaminaEffect = 0.1
								StaminaEffectTimer = 30
								Msg = I_Loc\MessageItem_VeryfinesyringeUseHuge
							Case 2
								SuperMan = True
								Msg = I_Loc\MessageItem_VeryfinesyringeUseVeryhuge
							Case 3
								VomitTimer = 30
								Msg = I_Loc\MessageItem_VeryfinesyringeUseStomacheache
						End Select
						
						MsgTimer = 70 * 8
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "radio","18vradio","fineradio","veryfineradio"
					;[Block]
					If SelectedItem\state <= 100 Then SelectedItem\state = Max(0, SelectedItem\state - FPSfactor * 0.004)
					
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)	
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					;radiostate(5) = has the "use the number keys" -message been shown yet (true/false)
					;radiostate(6) = a timer for the "code channel"
					;RadioState(7) = another timer for the "code channel"
					
					If RadioState(5) = 0 And SelectedItem\state > 0 And SelectedItem\itemtemplate\name <> "veryfineradio" Then 
						Msg = I_Loc\MessageItem_RadioUse
						MsgTimer = 70 * 5
						RadioState(5) = 1
						RadioState(0) = -1
					EndIf
					
					strtemp$ = ""
					
					x = HUDEndX - ImageWidth(SelectedItem\itemtemplate\img) ;+ 120
					y = HUDEndY - ImageHeight(SelectedItem\itemtemplate\img) ;- 30
					
					DrawImage(SelectedItem\itemtemplate\img, x, y)
					
					If SelectedItem\state > 0 Then
						If PlayerRoom\RoomTemplate\Name = "pocketdimension" Or CoffinDistance < 4.0 Then
							ResumeChannel(RadioCHN(5))
							If ChannelPlaying(RadioCHN(5)) = False Then RadioCHN(5) = PlaySound_Strict(RadioStatic)	
						Else
							Select Int(SelectedItem\state2)
								Case 0 ;randomkanava
									ResumeChannel(RadioCHN(0))
									strtemp = "        " + I_Loc\HUD_RadioUsertrack + " - "
									If (Not EnableUserTracks)
										If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound_Strict(RadioStatic)
										strtemp = strtemp + I_Loc\HUD_RadioUsertrackDisabled + "     "
									ElseIf UserTrackMusicAmount<1
										If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound_Strict(RadioStatic)
										strtemp = strtemp + I_Loc\HUD_RadioUsertrackNotracks + "     "
									Else
										If (Not ChannelPlaying(RadioCHN(0)))
											If (Not UserTrackFlag%)
												If UserTrackMode
													If RadioState(0)<(UserTrackMusicAmount-1)
														RadioState(0) = RadioState(0) + 1
													Else
														RadioState(0) = 0
													EndIf
													UserTrackFlag = True
												Else
													RadioState(0) = Rand(0,UserTrackMusicAmount-1)
												EndIf
											EndIf
											If CurrUserTrack%<>0 Then FreeSound_Strict(CurrUserTrack%) : CurrUserTrack% = 0
											CurrUserTrack% = LoadSound_Strict("SFX\Radio\UserTracks\"+UserTrackName$(RadioState(0)))
											RadioCHN(0) = PlaySound_Strict(CurrUserTrack%)
											DebugLog "CurrTrack: "+RadioState(0)
											DebugLog UserTrackName$(RadioState(0))
										Else
											strtemp = strtemp + Upper(UserTrackName$(RadioState(0))) + "          "
											UserTrackFlag = False
										EndIf
										
										If KeyHit(2) Then
											PlaySound_Strict RadioSquelch
											If (Not UserTrackFlag%)
												If UserTrackMode
													If RadioState(0)<(UserTrackMusicAmount-1)
														RadioState(0) = RadioState(0) + 1
													Else
														RadioState(0) = 0
													EndIf
													UserTrackFlag = True
												Else
													RadioState(0) = Rand(0,UserTrackMusicAmount-1)
												EndIf
											EndIf
											If CurrUserTrack%<>0 Then FreeSound_Strict(CurrUserTrack%) : CurrUserTrack% = 0
											CurrUserTrack% = LoadSound_Strict("SFX\Radio\UserTracks\"+UserTrackName$(RadioState(0)))
											RadioCHN(0) = PlaySound_Strict(CurrUserTrack%)
											DebugLog "CurrTrack: "+RadioState(0)
											DebugLog UserTrackName$(RadioState(0))
										EndIf
									EndIf
								Case 1 ;hälytyskanava
									DebugLog RadioState(1) 
									
									ResumeChannel(RadioCHN(1))
									strtemp = "        " + I_Loc\HUD_RadioCb + "          "
									If ChannelPlaying(RadioCHN(1)) = False Then
										
										If RadioState(1) => 5 Then
											RadioCHN(1) = PlaySound_Strict(RadioSFX(1,1))	
											RadioState(1) = 0
										Else
											RadioState(1)=RadioState(1)+1	
											RadioCHN(1) = PlaySound_Strict(RadioSFX(1,0))	
										EndIf
										
									EndIf
									
								Case 2 ;scp-radio
									ResumeChannel(RadioCHN(2))
									strtemp = "        " + I_Loc\HUD_RadioRadio + "          "
									If ChannelPlaying(RadioCHN(2)) = False Then
										RadioState(2)=RadioState(2)+1
										If RadioState(2) = 17 Then RadioState(2) = 1
										If Floor(RadioState(2)/2)=Ceil(RadioState(2)/2) Then ;parillinen, soitetaan normiviesti
											RadioCHN(2) = PlaySound_Strict(RadioSFX(2,Int(RadioState(2)/2)))	
										Else ;pariton, soitetaan musiikkia
											RadioCHN(2) = PlaySound_Strict(RadioSFX(2,0))
										EndIf
									EndIf 
								Case 3
									ResumeChannel(RadioCHN(3))
									strtemp = "             " + I_Loc\HUD_RadioEmergency + "         "
									If ChannelPlaying(RadioCHN(3)) = False Then RadioCHN(3) = PlaySound_Strict(RadioStatic)
									
									If MTFtimer > 0 Then 
										RadioState(3)=RadioState(3)+Max(Rand(-10,1),0)
										Select RadioState(3)
											Case 40
												If Not RadioState3(0) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random1.ogg"))
													RadioState(3) = RadioState(3)+1	
													RadioState3(0) = True	
												EndIf											
											Case 400
												If Not RadioState3(1) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random2.ogg"))
													RadioState(3) = RadioState(3)+1	
													RadioState3(1) = True	
												EndIf	
											Case 800
												If Not RadioState3(2) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random3.ogg"))
													RadioState(3) = RadioState(3)+1	
													RadioState3(2) = True
												EndIf													
											Case 1200
												If Not RadioState3(3) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random4.ogg"))	
													RadioState(3) = RadioState(3)+1	
													RadioState3(3) = True
												EndIf
											Case 1600
												If Not RadioState3(4) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random5.ogg"))	
													RadioState(3) = RadioState(3)+1
													RadioState3(4) = True
												EndIf
											Case 2000
												If Not RadioState3(5) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random6.ogg"))	
													RadioState(3) = RadioState(3)+1
													RadioState3(5) = True
												EndIf
											Case 2400
												If Not RadioState3(6) Then
													RadioCHN(3) = PlaySound_Strict(LoadTempSound("SFX\Character\MTF\Random7.ogg"))	
													RadioState(3) = RadioState(3)+1
													RadioState3(6) = True
												EndIf
										End Select
									EndIf
								Case 4
									ResumeChannel(RadioCHN(6)) ;taustalle kohinaa
									If ChannelPlaying(RadioCHN(6)) = False Then RadioCHN(6) = PlaySound_Strict(RadioStatic)									
									
									ResumeChannel(RadioCHN(4))
									If ChannelPlaying(RadioCHN(4)) = False Then 
										If RemoteDoorOn = False And RadioState(8) = False Then
											RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\Chatter3.ogg"))	
											RadioState(8) = True
										Else
											RadioState(4)=RadioState(4)+Max(Rand(-10,1),0)
											
											Select RadioState(4)
												Case 10
													If (Not Contained106)
														If Not RadioState4(0) Then
															RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\OhGod.ogg"))
															RadioState(4) = RadioState(4)+1
															RadioState4(0) = True
														EndIf
													EndIf
												Case 100
													If Not RadioState4(1) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\Chatter2.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState4(1) = True
													EndIf		
												Case 158
													If MTFtimer = 0 And (Not RadioState4(2)) Then 
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\franklin1.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState(2) = True
													EndIf
												Case 200
													If Not RadioState4(3) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\Chatter4.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState4(3) = True
													EndIf		
												Case 260
													If Not RadioState4(4) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\SCP\035\RadioHelp1.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState4(4) = True
													EndIf		
												Case 300
													If Not RadioState4(5) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\Chatter1.ogg"))	
														RadioState(4) = RadioState(4)+1	
														RadioState4(5) = True
													EndIf		
												Case 350
													If Not RadioState4(6) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\franklin2.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState4(6) = True
													EndIf		
												Case 400
													If Not RadioState4(7) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\SCP\035\RadioHelp2.ogg"))
														RadioState(4) = RadioState(4)+1
														RadioState4(7) = True
													EndIf		
												Case 450
													If Not RadioState4(8) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\franklin3.ogg"))	
														RadioState(4) = RadioState(4)+1		
														RadioState4(8) = True
													EndIf		
												Case 600
													If Not RadioState4(9) Then
														RadioCHN(4) = PlaySound_Strict(LoadTempSound("SFX\radio\franklin4.ogg"))	
														RadioState(4) = RadioState(4)+1	
														RadioState4(9) = True
													EndIf		
											End Select
										EndIf
									EndIf
									
									
								Case 5
									ResumeChannel(RadioCHN(5))
									If ChannelPlaying(RadioCHN(5)) = False Then RadioCHN(5) = PlaySound_Strict(RadioStatic)
							End Select 
							
							x=x+66
							y=y+419
							
							Color (30,30,30)
							
							If SelectedItem\state <= 100 Then
								For i = 0 To 4
									Rect(x, y+8*i, 43 - i * 6, 4, Ceil(SelectedItem\state / 20.0) > 4 - i )
								Next
							EndIf	
							
							SetFont Font3
							Text(x+60, y, I_Loc\HUD_RadioChannel)						
							
							If SelectedItem\itemtemplate\name = "veryfineradio" Then ;"KOODIKANAVA"
								ResumeChannel(RadioCHN(0))
								If ChannelPlaying(RadioCHN(0)) = False Then RadioCHN(0) = PlaySound_Strict(RadioStatic)
								
								;radiostate(7)=kuinka mones piippaus menossa
								;radiostate(8)=kuinka mones access coden numero menossa
								RadioState(6)=RadioState(6) + FPSfactor
								temp = Mid(Str(AccessCode),RadioState(8)+1,1)
								If RadioState(6)-FPSfactor =< RadioState(7)*50 And RadioState(6)>RadioState(7)*50 Then
									PlaySound_Strict(RadioBuzz)
									RadioState(7)=RadioState(7)+1
									If RadioState(7)=>temp Then
										RadioState(7)=0
										RadioState(6)=-100
										RadioState(8)=RadioState(8)+1
										If RadioState(8)=4 Then RadioState(8)=0 : RadioState(6)=-200
									EndIf
								EndIf
								
								strtemp = ""
								For i = 0 To Rand(5, 30)
									strtemp = strtemp + Chr(Rand(1,100))
								Next
								
								SetFont Font4
								Text(x+97, y+16, Rand(0,9),True,True)
								
							Else
								For i = 2 To 6
									If KeyHit(i) Then
										If SelectedItem\state2 <> i-2 Then ;pausetetaan nykyinen radiokanava
											PlaySound_Strict RadioSquelch
											If RadioCHN(Int(SelectedItem\state2)) <> 0 Then PauseChannel(RadioCHN(Int(SelectedItem\state2)))
										EndIf
										SelectedItem\state2 = i-2
										;jos nykyistä kanavaa ollaan soitettu, laitetaan jatketaan toistoa samasta kohdasta
										If RadioCHN(SelectedItem\state2)<>0 Then ResumeChannel(RadioCHN(SelectedItem\state2))
									EndIf
								Next
								
								SetFont Font4
								Text(x+97, y+16, Int(SelectedItem\state2+1),True,True)
							EndIf
							
							SetFont Font3
							If strtemp <> "" Then
								strtemp = Right(Left(strtemp, (Int(MilliSecs()/300) Mod Len(strtemp))),10)
								Text(x+32, y+33, strtemp)
							EndIf
							
							SetFont Font1
							
						EndIf
						
					EndIf
					;[End Block]
				Case "cigarette"
					;[Block]
					If CanUseItem(False,False,True)
						If SelectedItem\state = 0 Then
							SelectedItem\state = 1
							Select Rand(6)
								Case 1
									Msg = I_Loc\MessageItem_CigaretteUse[1]
								Case 2
									Msg = I_Loc\MessageItem_CigaretteUseUnable
								Case 3
									Msg = I_Loc\MessageItem_CigaretteUse[2]
									RemoveItem(SelectedItem)
								Case 4
									Msg = I_Loc\MessageItem_CigaretteUse[3]
								Case 5
									Msg = I_Loc\MessageItem_CigaretteUse[4]
								Case 6
									Msg = I_Loc\MessageItem_CigaretteUse[5]
									RemoveItem(SelectedItem)
							End Select
						Else
							Msg = I_Loc\MessageItem_CigaretteUseUnable
						EndIf
						
						MsgTimer = 70 * 5
					EndIf
					;[End Block]
				Case "scp420j"
					;[Block]
					If CanUseItem(False,False,True)
						If Wearing714=1 Then
							Msg = I_Loc\MessageItem_420jUse714
						Else
							Msg = I_Loc\MessageItem_420jUse
							Injuries = Max(Injuries-0.5, 0)
							BlurTimer = 500
							GiveAchievement(Achv420)
							PlaySound_Strict LoadTempSound("SFX\Music\420J.ogg")
						EndIf
						MsgTimer = 70 * 5
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "smellyjoint", "joint"
					;[Block]
					If CanUseItem(False,False,True)
						If Wearing714=1 Then
							Msg = I_Loc\MessageItem_420jUse714
						Else
							DeathMSG = I_Loc\DeathMessage_420js
							Msg = I_Loc\MessageItem_420jUseNap
							KillTimer = -1						
						EndIf
						MsgTimer = 70 * 6
						RemoveItem(SelectedItem)
					EndIf
					;[End Block]
				Case "scp714"
					;[Block]
					If Wearing714=1 Then
						Msg = I_Loc\MessageItem_Scp714Off
						Wearing714 = False
					Else
						GiveAchievement(Achv714)
						Msg = I_Loc\MessageItem_Scp714On
						Wearing714 = True
					EndIf
					MsgTimer = 70 * 5
					SelectedItem = Null	
					;[End Block]
				Case "hazmatsuit", "hazmatsuit2", "hazmatsuit3"
					;[Block]
					If WearingVest = 0 Then
						CurrSpeed = CurveValue(0, CurrSpeed, 5.0)
						
						DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
						
						DrawBar(BlinkMeterIMG, GraphicWidth / 2, GraphicHeight / 2 + 80 * HUDScale, 300 * HUDScale, SelectedItem\state / 100.0, True)
						
						SelectedItem\state = Min(SelectedItem\state+(FPSfactor/4.0),100)
						
						If SelectedItem\state=100 Then
							If WearingHazmat>0 Then
								Msg = I_Loc\MessageItem_HazmatOff
								WearingHazmat = False
								DropItem(SelectedItem)
							Else
								If SelectedItem\itemtemplate\name="hazmatsuit" Then
									WearingHazmat = 1
								ElseIf SelectedItem\itemtemplate\name="hazmatsuit2" Then
									WearingHazmat = 2
								Else
									WearingHazmat = 3
								EndIf
								If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
								Msg = I_Loc\MessageItem_HazmatOn
								If WearingNightVision Then CameraFogFar = StoredCameraFogFar
								WearingGasMask = 0
								WearingNightVision = 0
							EndIf
							SelectedItem\state=0
							MsgTimer = 70 * 5
							SelectedItem = Null
						EndIf
					EndIf
					;[End Block]
				Case "vest","finevest"
					;[Block]
					CurrSpeed = CurveValue(0, CurrSpeed, 5.0)
					
					DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
					
					DrawBar(BlinkMeterIMG, GraphicWidth / 2, GraphicHeight / 2 + 80 * HUDScale, 300 * HUDScale, SelectedItem\state / 100.0, True)

					SelectedItem\state = Min(SelectedItem\state+(FPSfactor/(2.0+(0.5*(SelectedItem\itemtemplate\name="finevest")))),100)
					
					If SelectedItem\state=100 Then
						If WearingVest>0 Then
							Msg = I_Loc\MessageItem_VestOff
							WearingVest = False
							DropItem(SelectedItem)
						Else
							If SelectedItem\itemtemplate\name="vest" Then
								Msg = I_Loc\MessageItem_VestOn
								WearingVest = 1
							Else
								Msg = I_Loc\MessageItem_VestOnHeavy
								WearingVest = 2
							EndIf
							If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
						EndIf
						SelectedItem\state=0
						MsgTimer = 70 * 5
						SelectedItem = Null
					EndIf
					;[End Block]
				Case "gasmask", "supergasmask", "gasmask3"
					;[Block]
					If Wearing1499 = 0 And WearingHazmat = 0 Then
						If WearingGasMask Then
							Msg = I_Loc\MessageItem_GasmaskOff
						Else
							If SelectedItem\itemtemplate\name = "supergasmask"
								Msg = I_Loc\MessageItem_GasmaskOnEasy
							Else
								Msg = I_Loc\MessageItem_GasmaskOn
							EndIf
							If WearingNightVision Then CameraFogFar = StoredCameraFogFar
							WearingNightVision = 0
							WearingGasMask = 0
						EndIf
						If SelectedItem\itemtemplate\name="gasmask3" Then
							If WearingGasMask = 0 Then WearingGasMask = 3 Else WearingGasMask=0
						ElseIf SelectedItem\itemtemplate\name="supergasmask"
							If WearingGasMask = 0 Then WearingGasMask = 2 Else WearingGasMask=0
						Else
							WearingGasMask = (Not WearingGasMask)
						EndIf
					ElseIf Wearing1499 > 0 Then
						Msg = I_Loc\MessageItem_GasmaskConflictScp1499
					Else
						Msg = I_Loc\MessageItem_GasmaskConflictHazmat
					EndIf
					SelectedItem = Null
					MsgTimer = 70 * 5
					;[End Block]
				Case "snav", "snav300", "snav310", "snavulti"
					;[Block]
					
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)	
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					If SelectedItem\state <= 100 Then SelectedItem\state = Max(0, SelectedItem\state - FPSfactor * 0.005)
					
					x = HUDEndX - ImageWidth(SelectedItem\itemtemplate\img)*0.5+20
					y = HUDEndY - ImageHeight(SelectedItem\itemtemplate\img)*0.4-85
					width = 287
					height = 256
					
					Local PlayerX,PlayerZ
					
					DrawImage(SelectedItem\itemtemplate\img, x - ImageWidth(SelectedItem\itemtemplate\img) / 2, y - ImageHeight(SelectedItem\itemtemplate\img) / 2 + 85)
					
					SetFont Font3
					
					Local NavWorks% = True
					If PlayerRoom\RoomTemplate\Name$ = "pocketdimension" Or PlayerRoom\RoomTemplate\Name$ = "dimension1499" Then
						NavWorks% = False
					ElseIf PlayerRoom\RoomTemplate\Name$ = "room860" Then
						For e.Events = Each Events
							If e\EventName = "room860" Then
								If e\EventState = 1.0 Then
									NavWorks% = False
								EndIf
								Exit
							EndIf
						Next
					EndIf
					
					If (Not NavWorks) Then
						If (MilliSecs() Mod 1000) > 300 Then
							Color(200, 0, 0)
							Text(x, y + height / 2 - 80, I_Loc\HUD_NavError, True)
							Text(x, y + height / 2 - 60, I_Loc\HUD_NavErrorLocation, True)						
						EndIf
					Else
						
						If SelectedItem\state > 0 And (Rnd(CoffinDistance + 15.0) > 1.0 Or PlayerRoom\RoomTemplate\Name <> "coffin") Then
							
							PlayerX% = Floor((EntityX(PlayerRoom\obj)+8) / 8.0 + 0.5)
							PlayerZ% = Floor((EntityZ(PlayerRoom\obj)+8) / 8.0 + 0.5)
							
							SetBuffer ImageBuffer(NavBG)
							Local xx = x-ImageWidth(SelectedItem\itemtemplate\img)/2
							Local yy = y-ImageHeight(SelectedItem\itemtemplate\img)/2+85
							DrawImage(SelectedItem\itemtemplate\img, xx, yy)
							
							x = x - 12 + (((EntityX(Collider)-4.0)+8.0) Mod 8.0)*3
							y = y + 12 - (((EntityZ(Collider)-4.0)+8.0) Mod 8.0)*3
							For x2 = Max(0, PlayerX - 6) To Min(MapWidth, PlayerX + 6)
								For z2 = Max(0, PlayerZ - 6) To Min(MapHeight, PlayerZ + 6)
									
									If CoffinDistance > 16.0 Or Rnd(16.0)<CoffinDistance Then 
										If MapTemp(x2, z2)>0 And (MapFound(x2, z2) > 0 Or SelectedItem\itemtemplate\name = "snav310" Or SelectedItem\itemtemplate\name = "snavulti") Then
											Local drawx% = x + (PlayerX - 1 - x2) * 24 , drawy% = y - (PlayerZ - 1 - z2) * 24
											
											If x2+1<=MapWidth Then
												If MapTemp(x2+1,z2)=False
													DrawImage NavImages(3),drawx-12,drawy-12
												EndIf
											Else
												DrawImage NavImages(3),drawx-12,drawy-12
											EndIf
											If x2-1>=0 Then
												If MapTemp(x2-1,z2)=False
													DrawImage NavImages(1),drawx-12,drawy-12
												EndIf
											Else
												DrawImage NavImages(1),drawx-12,drawy-12
											EndIf
											If z2-1>=0 Then
												If MapTemp(x2,z2-1)=False
													DrawImage NavImages(0),drawx-12,drawy-12
												EndIf
											Else
												DrawImage NavImages(0),drawx-12,drawy-12
											EndIf
											If z2+1<=MapHeight Then
												If MapTemp(x2,z2+1)=False
													DrawImage NavImages(2),drawx-12,drawy-12
												EndIf
											Else
												DrawImage NavImages(2),drawx-12,drawy-12
											EndIf
										EndIf
									EndIf
									
								Next
							Next
							
							SetBuffer BackBuffer()
							DrawImageRect NavBG,xx+80,yy+70,xx+80,yy+70,270,230
							Color 30,30,30
							If SelectedItem\itemtemplate\name = "snav" Then Color(100, 0, 0)
							Rect xx+80,yy+70,270,230,False
							
							x = HUDEndX - ImageWidth(SelectedItem\itemtemplate\img)*0.5+20
							y = HUDEndY - ImageHeight(SelectedItem\itemtemplate\img)*0.4-85
							
							If SelectedItem\itemtemplate\name = "snav" Then 
								Color(100, 0, 0)
							Else
								Color (30,30,30)
							EndIf
							If (MilliSecs() Mod 1000) > 300 Then
								If SelectedItem\itemtemplate\name <> "snav310" And SelectedItem\itemtemplate\name <> "snavulti" Then
									Text(x - width/2 + 10, y - height/2 + 10, I_Loc\HUD_NavDatabase)
								EndIf
								
								yawvalue = EntityYaw(Collider)-90
								x1 = x+Cos(yawvalue)*6 : y1 = y-Sin(yawvalue)*6
								x2 = x+Cos(yawvalue-140)*5 : y2 = y-Sin(yawvalue-140)*5				
								x3 = x+Cos(yawvalue+140)*5 : y3 = y-Sin(yawvalue+140)*5
								
								Line x1,y1,x2,y2
								Line x1,y1,x3,y3
								Line x2,y2,x3,y3
							EndIf
							
							Local SCPs_found% = 0
							If SelectedItem\itemtemplate\name = "snavulti" And (MilliSecs() Mod 600) < 400 Then
								If Curr173<>Null Then
									Local dist# = EntityDistance(Camera, Curr173\obj)
									dist = Ceil(dist / 8.0) * 8.0
									If dist < 8.0 * 4 Then
										Color 100, 0, 0
										Oval(x - dist * 3, y - 7 - dist * 3, dist * 3 * 2, dist * 3 * 2, False)
										Text(x - width / 2 + 10, y - height / 2 + 30, I_Loc\NPC_173)
										SCPs_found% = SCPs_found% + 1
									EndIf
								EndIf
								If Curr106<>Null Then
									dist# = EntityDistance(Camera, Curr106\obj)
									If dist < 8.0 * 4 Then
										Color 100, 0, 0
										Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
										Text(x - width / 2 + 10, y - height / 2 + 30 + (20*SCPs_found), I_Loc\NPC_106)
										SCPs_found% = SCPs_found% + 1
									EndIf
								EndIf
								If Curr096<>Null Then 
									dist# = EntityDistance(Camera, Curr096\obj)
									If dist < 8.0 * 4 Then
										Color 100, 0, 0
										Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
										Text(x - width / 2 + 10, y - height / 2 + 30 + (20*SCPs_found), I_Loc\NPC_096)
										SCPs_found% = SCPs_found% + 1
									EndIf
								EndIf
								For np.NPCs = Each NPCs
									If np\NPCtype = NPCtype049
										dist# = EntityDistance(Camera, np\obj)
										If dist < 8.0 * 4 Then
											If (Not np\HideFromNVG) Then
												Color 100, 0, 0
												Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
												Text(x - width / 2 + 10, y - height / 2 + 30 + (20*SCPs_found), I_Loc\NPC_049)
												SCPs_found% = SCPs_found% + 1
											EndIf
										EndIf
										Exit
									EndIf
								Next
								If PlayerRoom\RoomTemplate\Name = "coffin" Then
									If CoffinDistance < 8.0 Then
										dist = Rnd(4.0, 8.0)
										Color 100, 0, 0
										Oval(x - dist * 1.5, y - 7 - dist * 1.5, dist * 3, dist * 3, False)
										Text(x - width / 2 + 10, y - height / 2 + 30 + (20*SCPs_found), I_Loc\NPC_895)
									EndIf
								EndIf
							End If
							
							Color (30,30,30)
							If SelectedItem\itemtemplate\name = "snav" Then Color(100, 0, 0)
							If SelectedItem\state <= 100 Then
								;Text (x - width/2 + 10, y - height/2 + 10, "BATTERY")
								;xtemp = x - width/2 + 10
								;ytemp = y - height/2 + 30		
								;Line xtemp, ytemp, xtemp+20, ytemp
								;Line xtemp, ytemp+100, xtemp+20, ytemp+100
								;Line xtemp, ytemp, xtemp, ytemp+100
								;Line xtemp+20, ytemp, xtemp+20, ytemp+100
								;
								;SetFont Font4
								;For i = 1 To Ceil(SelectedItem\state / 10.0)
								;	Text (xtemp+11, ytemp+i*10-26, "-", True)
								;	;Rect(x - width/2, y+i*15, 40 - i * 6, 5, Ceil(SelectedItem\state / 20.0) > 4 - i)
								;Next
								;SetFont Font3
								
								xtemp = x - width/2 + 196
								ytemp = y - height/2 + 10
								Rect xtemp,ytemp,80,20,False
								
								For i = 1 To Ceil(SelectedItem\state / 10.0)
									DrawImage NavImages(4),xtemp+i*8-6,ytemp+4
								Next
							EndIf
						EndIf
					EndIf

					SetFont Font1
					;[End Block]
				;new Items in SCP:CB 1.3
				Case "scp1499","super1499"
					;[Block]
					If WearingHazmat>0
						Msg = I_Loc\MessageItem_Scp1499ConflictHazmat
						MsgTimer = 70 * 5
						SelectedItem=Null
						Return
					EndIf
					
					CurrSpeed = CurveValue(0, CurrSpeed, 5.0)
					
					DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
					
					DrawBar(BlinkMeterIMG, GraphicWidth / 2, GraphicHeight / 2 + 80 * HUDScale, 300 * HUDScale, SelectedItem\state / 100.0, True)
					
					SelectedItem\state = Min(SelectedItem\state+(FPSfactor),100)
					
					If SelectedItem\state=100 Then
						If Wearing1499>0 Then
							SecondaryLightOn = PrevSecondaryLightOn
							Wearing1499 = False
							;DropItem(SelectedItem)
							If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
						Else
							If SelectedItem\itemtemplate\name="scp1499" Then
								Wearing1499 = 1
							Else
								Wearing1499 = 2
							EndIf
							If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
							GiveAchievement(Achv1499)
							PrevSecondaryLightOn = SecondaryLightOn
							SecondaryLightOn = True
							If WearingNightVision Then CameraFogFar = StoredCameraFogFar
							WearingGasMask = 0
							WearingNightVision = 0
							For r.Rooms = Each Rooms
								If r\RoomTemplate\Name = "dimension1499" Then
									BlinkTimer = -1
									NTF_1499PrevRoom = PlayerRoom
									NTF_1499PrevX# = EntityX(Collider)
									NTF_1499PrevY# = EntityY(Collider)
									NTF_1499PrevZ# = EntityZ(Collider)
									
									If NTF_1499X# = 0.0 And NTF_1499Y# = 0.0 And NTF_1499Z# = 0.0 Then
										PositionEntity (Collider, r\x+6086.0*RoomScale, r\y+304.0*RoomScale, r\z+2292.5*RoomScale)
										RotateEntity Collider,0,90,0,True
									Else
										PositionEntity (Collider, NTF_1499X#, NTF_1499Y#+0.05, NTF_1499Z#)
									EndIf
									ResetEntity(Collider)
									UpdateDoors()
									UpdateRooms()
									For it.Items = Each Items
										it\disttimer = 0
									Next
									PlayerRoom = r
									PlaySound_Strict (LoadTempSound("SFX\SCP\1499\Enter.ogg"))
									NTF_1499X# = 0.0
									NTF_1499Y# = 0.0
									NTF_1499Z# = 0.0
									If Curr096<>Null Then
										If Curr096\SoundChn<>0 Then
											SetStreamVolume_Strict(Curr096\SoundChn,0.0)
										EndIf
									EndIf
									For e.Events = Each Events
										If e\EventName = "dimension1499" Then
											If EntityDistance(e\room\obj,Collider)>8300.0*RoomScale Then
												If e\EventState2 < 5 Then
													e\EventState2 = e\EventState2 + 1
												EndIf
											EndIf
											Exit
										EndIf
									Next
									Exit
								EndIf
							Next
						EndIf
						SelectedItem\state=0
						;MsgTimer = 70 * 5
						SelectedItem = Null
					EndIf
					;[End Block]
				Case "badge", "oldbadge"
					;[Block]
					If SelectedItem\itemtemplate\img=0 Then
						SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)	
						;SelectedItem\itemtemplate\img = ResizeImage2(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
						
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
					
					If SelectedItem\state = 0 Then
						PlaySound_Strict LoadTempSound("SFX\SCP\1162\NostalgiaCancer"+Rand(6,10)+".ogg")
						If SelectedItem\itemtemplate\name = "oldbadge"
							Msg = I_Loc\MessageItem_1162UseBadge
							MsgTimer = 70*10
						EndIf
						
						SelectedItem\state = 1
					EndIf
					;[End Block]
				Case "key"
					;[Block]
					If SelectedItem\state = 0 Then
						PlaySound_Strict LoadTempSound("SFX\SCP\1162\NostalgiaCancer"+Rand(6,10)+".ogg")
						
						Msg = I_Loc\MessageItem_1162UseKey
						MsgTimer = 70*10						
					EndIf
					
					SelectedItem\state = 1
					SelectedItem = Null
					;[End Block]
				Case "oldpaper"
					;[Block]
					If SelectedItem\itemtemplate\img = 0 Then
						SelectedItem\itemtemplate\img = LoadImage_Strict(SelectedItem\itemtemplate\imgpath)	
						SelectedItem\itemtemplate\img = ResizeImage2(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
						
						MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
					EndIf
					
					DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
					
					If SelectedItem\state = 0
						BlurTimer = 1000
						
						Msg = I_Loc\MessageItem_1162UseHearing
						MsgTimer = 70*10
						PlaySound_Strict LoadTempSound("SFX\SCP\1162\NostalgiaCancer"+Rand(6,10)+".ogg")
						SelectedItem\state = 1
					EndIf
					;[End Block]
				Case "coin"
					;[Block]
					If SelectedItem\state = 0
						PlaySound_Strict LoadTempSound("SFX\SCP\1162\NostalgiaCancer"+Rand(1,5)+".ogg")
					EndIf
					
					Msg = ""
					
					SelectedItem\state = 1
					DrawImage(SelectedItem\itemtemplate\invimg, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\invimg) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\invimg) / 2)
					;[End Block]
				Case "scp427"
					;[Block]
					If I_427\Using=1 Then
						Msg = I_Loc\MessageItem_427Off
						I_427\Using = False
					Else
						GiveAchievement(Achv427)
						Msg = I_Loc\MessageItem_427On
						I_427\Using = True
					EndIf
					MsgTimer = 70 * 5
					SelectedItem = Null
					;[End Block]
				Case "pill"
					;[Block]
					If CanUseItem(False, False, True)
						Msg = I_Loc\MessageItem_PillUse
						MsgTimer = 70*7
						
						RemoveItem(SelectedItem)
						SelectedItem = Null
					EndIf	
					;[End Block]
				Case "scp500death"
					;[Block]
					If CanUseItem(False, False, True)
						Msg = I_Loc\MessageItem_PillUse
						MsgTimer = 70*7
						
						If I_427\Timer < 70*360 Then
							I_427\Timer = 70*360
						EndIf
						
						RemoveItem(SelectedItem)
						SelectedItem = Null
					EndIf
					;[End Block]
				Default
					If SelectedItem\itemtemplate\group = "paper" Lor SelectedItem\itemtemplate\name = "ticket" Then
						;[Block]
						If SelectedItem\itemtemplate\img = 0 Then
							Select SelectedItem\itemtemplate\name
								Case "burntnote" 
									SelectedItem\itemtemplate\img = LoadImage_Strict("GFX\items\bn.it")
									SetBuffer ImageBuffer(SelectedItem\itemtemplate\img)
									Color 0,0,0
									SetFont Font1
									Text 277, 469, AccessCode, True, True
									Color 255,255,255
									SetBuffer BackBuffer()
								Case "doc372"
									SelectedItem\itemtemplate\img = LoadImage_Strict(SelectedItem\itemtemplate\imgpath)
									SelectedItem\itemtemplate\img = ResizeImage2(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
									
									SetBuffer ImageBuffer(SelectedItem\itemtemplate\img)
									Color 37,45,137
									SetFont Font5
									temp = ((Int(AccessCode)*3) Mod 10000)
									If temp < 1000 Then temp = temp+1000
									Text 383*MenuScale, 734*MenuScale, temp, True, True
									Color 255,255,255
									SetBuffer BackBuffer()
								Case "ticket"
									;don't resize because it messes up the masking
									SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)
									
									If (SelectedItem\state = 0) Then
										Msg = I_Loc\MessageItem_1162UseTicket
										MsgTimer = 70*10
										PlaySound_Strict LoadTempSound("SFX\SCP\1162\NostalgiaCancer"+Rand(1,5)+".ogg")
										SelectedItem\state = 1
									EndIf
								Case "leaflet"
									;don't resize because it messes up the masking
									SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)
								Default 
									SelectedItem\itemtemplate\img=LoadImage_Strict(SelectedItem\itemtemplate\imgpath)
									SelectedItem\itemtemplate\img = ResizeImage2(SelectedItem\itemtemplate\img, ImageWidth(SelectedItem\itemtemplate\img) * MenuScale, ImageHeight(SelectedItem\itemtemplate\img) * MenuScale)
							End Select
							
							MaskImage(SelectedItem\itemtemplate\img, 255, 0, 255)
						EndIf
						
						DrawImage(SelectedItem\itemtemplate\img, GraphicWidth / 2 - ImageWidth(SelectedItem\itemtemplate\img) / 2, GraphicHeight / 2 - ImageHeight(SelectedItem\itemtemplate\img) / 2)
						;[End Block]
					Else
						;[Block]
						;check if the item is an inventory-type object
						DoubleClick = 0
						MouseHit1 = 0
						MouseDown1 = 0
						LastMouseHit1 = 0
						If SelectedItem\Inventory <> Null Then OtherOpen = SelectedItem
						SelectedItem = Null
						;[End Block]
					EndIf
			End Select
			
			If SelectedItem <> Null Then
				If SelectedItem\itemtemplate\img <> 0
					Local IN$ = SelectedItem\itemtemplate\group
					Local INN$ = SelectedItem\itemtemplate\name
					If IN$ = "paper" Or INN$ = "badge" Or INN$ = "oldbadge" Or INN$ = "oldpaper" Or INN$ = "ticket" Then
						For a_it.Items = Each Items
							If a_it <> SelectedItem
								Local IN2$ = a_it\itemtemplate\group
								Local INN2$ = a_it\itemtemplate\name
								If IN2$ = "paper" Or INN2$ = "badge" Or INN2$ = "oldbadge" Or INN2$ = "oldpaper" Or INN2$ = "ticket" Then
									If a_it\itemtemplate\img<>0
										If a_it\itemtemplate\img <> SelectedItem\itemtemplate\img
											FreeImage(a_it\itemtemplate\img) : a_it\itemtemplate\img = 0
											Exit
										EndIf
									EndIf
								EndIf
							EndIf
						Next
					EndIf
				EndIf			
			EndIf
			
			If MouseHit2 Then
				IN$ = SelectedItem\itemtemplate\name
				G$ = SelectedItem\itemtemplate\group
				If IN$ = "scp1025" Then
					If SelectedItem\itemtemplate\img<>0 Then FreeImage(SelectedItem\itemtemplate\img)
					SelectedItem\itemtemplate\img=0
				ElseIf IN$ = "firstaid" Or IN$="finefirstaid" Or IN$="firstaid2" Then
					SelectedItem\state = 0
				ElseIf G$ = "vest"
					SelectedItem\state = 0
					If (Not WearingVest)
						DropItem(SelectedItem,False)
					EndIf
				ElseIf G$="hazmat"
					SelectedItem\state = 0
					If (Not WearingHazmat)
						DropItem(SelectedItem,False)
					EndIf
				ElseIf IN$="scp1499" Or IN$="super1499"
					SelectedItem\state = 0
					;If (Not Wearing1499)
					;	DropItem(SelectedItem,False)
					;EndIf
				EndIf
				
				If SelectedItem\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(SelectedItem\itemtemplate\sound))
				SelectedItem = Null
			EndIf
		End If		
	EndIf
	
	If SelectedItem = Null Then
		For i = 0 To 6
			If RadioCHN(i) <> 0 Then 
				If ChannelPlaying(RadioCHN(i)) Then PauseChannel(RadioCHN(i))
			EndIf
		Next
	EndIf
	
	For it.Items = Each Items
		If it<>SelectedItem
			Select it\itemtemplate\name
				Case "firstaid","finefirstaid","firstaid2","vest","finevest","hazmatsuit","hazmatsuit2","hazmatsuit3","scp1499","super1499"
					it\state = 0
			End Select
		EndIf
	Next
	
	If PrevInvOpen And (Not InvOpen) Then MoveMouse viewport_center_x, viewport_center_y

	DrawHUD()
	
	CatchErrors("DrawGUI")
End Function

Function ResetDiseases()
	DeathTimer = 0
	Infect = 0
	Stamina = 100
	For i = 0 To 5
		SCP1025state[i]=0
	Next
	If StaminaEffect > 1.0 Then
		StaminaEffect = 1.0
		StaminaEffectTimer = 0.0
	EndIf
End Function

Function DrawHUD()
	If Not HUDenabled Then Return

	If SpeedRunMode Then DrawTimer()

	Local width% = 204 * HUDScale
	x% = HUDStartX + 80 * HUDScale
	y% = HUDEndY - 95 * HUDScale

	DrawBar(BlinkMeterIMG, x, y, width, BlinkTimer / BLINKFREQ)
	Color 0, 0, 0
	Rect(x - 50 * HUDScale, y, 30 * HUDScale, 30 * HUDScale)
	
	If EyeIrritation > 0 Then
		Color 200, 0, 0
		Rect(x - 50 * HUDScale - 3, y - 3, 30 * HUDScale + 6, 30 * HUDScale + 6)
	End If
	
	Color 255, 255, 255
	Rect(x - 50 * HUDScale - 1, y - 1, 30 * HUDScale + 2, 30 * HUDScale + 2, False)
	
	DrawImage BlinkIcon, x - 50 * HUDScale, y
	
	y = HUDEndY - 55 * HUDScale
	DrawBar(StaminaMeterIMG, x, y, width, Stamina / 100.0)
	
	Color 0, 0, 0
	Rect(x - 50 * HUDScale, y, 30 * HUDScale, 30 * HUDScale)
	
	Color 255, 255, 255
	Rect(x - 50 * HUDScale - 1, y - 1, 30 * HUDScale + 2, 30 * HUDScale + 2, False)
	If Crouch Then
		DrawImage CrouchIcon, x - 50 * HUDScale, y
	Else
		DrawImage SprintIcon, x - 50 * HUDScale, y
	EndIf

	If DebugHUD Then
		Color 255, 255, 255
		SetFont ConsoleFont
		
		;Text x + 250, 50, "Zone: " + (EntityZ(Collider)/8.0)
		Text x - 50, 50, "Player Position: (" + f2s(EntityX(Collider), 3) + ", " + f2s(EntityY(Collider), 3) + ", " + f2s(EntityZ(Collider), 3) + ")"
		Text x - 50, 70, "Camera Position: (" + f2s(EntityX(Camera), 3)+ ", " + f2s(EntityY(Camera), 3) +", " + f2s(EntityZ(Camera), 3) + ")"
		Text x - 50, 100, "Player Rotation: (" + f2s(EntityPitch(Collider), 3) + ", " + f2s(EntityYaw(Collider), 3) + ", " + f2s(EntityRoll(Collider), 3) + ")"
		Text x - 50, 120, "Camera Rotation: (" + f2s(EntityPitch(Camera), 3)+ ", " + f2s(EntityYaw(Camera), 3) +", " + f2s(EntityRoll(Camera), 3) + ")"
		Text x - 50, 150, "Room: " + PlayerRoom\RoomTemplate\Name
		For ev.Events = Each Events
			If ev\room = PlayerRoom Then
				Text x - 50, 170, "Room event: " + ev\EventName   
				Text x - 50, 190, "state: " + ev\EventState
				Text x - 50, 210, "state2: " + ev\EventState2   
				Text x - 50, 230, "state3: " + ev\EventState3
				Text x - 50, 250, "str: "+ ev\EventStr
				Exit
			EndIf
		Next
		Text x - 50, 280, "Room coordinates: (" + Floor(EntityX(PlayerRoom\obj) / 8.0 + 0.5) + ", " + Floor(EntityZ(PlayerRoom\obj) / 8.0 + 0.5) + ", angle: "+PlayerRoom\angle + ")"
		Text x - 50, 300, "Stamina: " + f2s(Stamina, 3)
		Text x - 50, 320, "Death timer: " + f2s(KillTimer, 3)               
		Text x - 50, 340, "Blink timer: " + f2s(BlinkTimer, 3)
		Text x - 50, 360, "Injuries: " + Injuries
		Text x - 50, 380, "Bloodloss: " + Bloodloss
		If Curr173 <> Null
			Text x - 50, 410, "SCP - 173 Position (collider): (" + f2s(EntityX(Curr173\Collider), 3) + ", " + f2s(EntityY(Curr173\Collider), 3) + ", " + f2s(EntityZ(Curr173\Collider), 3) + ")"
			Text x - 50, 430, "SCP - 173 Position (obj): (" + f2s(EntityX(Curr173\obj), 3) + ", " + f2s(EntityY(Curr173\obj), 3) + ", " + f2s(EntityZ(Curr173\obj), 3) + ")"
			;Text x - 50, 410, "SCP - 173 Idle: " + Curr173\Idle
			Text x - 50, 450, "SCP - 173 State: " + Curr173\State
		EndIf
		If Curr106 <> Null
			Text x - 50, 470, "SCP - 106 Position: (" + f2s(EntityX(Curr106\obj), 3) + ", " + f2s(EntityY(Curr106\obj), 3) + ", " + f2s(EntityZ(Curr106\obj), 3) + ")"
			Text x - 50, 490, "SCP - 106 Idle: " + Curr106\Idle
			Text x - 50, 510, "SCP - 106 State: " + Curr106\State
		EndIf
		offset% = 0
		For npc.NPCs = Each NPCs
			If npc\NPCtype = NPCtype096 Then
				Text x - 50, 530, "SCP - 096 Position: (" + f2s(EntityX(npc\obj), 3) + ", " + f2s(EntityY(npc\obj), 3) + ", " + f2s(EntityZ(npc\obj), 3) + ")"
				Text x - 50, 550, "SCP - 096 Idle: " + npc\Idle
				Text x - 50, 570, "SCP - 096 State: " + npc\State
				Text x - 50, 590, "SCP - 096 Speed: " + f2s(npc\currspeed, 5)
			EndIf
			If npc\NPCtype = NPCtypeMTF Then
				Text x - 50, 620 + 60 * offset, "MTF " + offset + " Position: (" + f2s(EntityX(npc\obj), 3) + ", " + f2s(EntityY(npc\obj), 3) + ", " + f2s(EntityZ(npc\obj), 3) + ")"
				Text x - 50, 640 + 60 * offset, "MTF " + offset + " State: " + npc\State
				Text x - 50, 660 + 60 * offset, "MTF " + offset + " LastSeen: " + npc\lastseen					
				offset = offset + 1
			EndIf
		Next
		x = x + 500 * MenuScale
		If PlayerRoom\RoomTemplate\Name$ = "dimension1499"
			Text x, 50, "Current Chunk X/Z: ("+(Int((EntityX(Collider)+20)/40))+", "+(Int((EntityZ(Collider)+20)/40))+")"
			Local CH_Amount% = 0
			For ch.Chunk = Each Chunk
				CH_Amount = CH_Amount + 1
			Next
			Text x, 70, "Current Chunk Amount: "+CH_Amount
		Else
			Text x, 50, "Current Room Position: ("+PlayerRoom\x+", "+PlayerRoom\y+", "+PlayerRoom\z+")"
		EndIf
		Text x, 90, "Triangles rendered: "+CurrTrisAmount
		Text x, 110, "Active textures: "+ActiveTextures()
		Text x, 130, "SCP-427 state (secs): "+Int(I_427\Timer/70.0)
		Text x, 150, "SCP-008 infection: "+Infect
		For i = 0 To 5
			Text x, 170+(20*i), "SCP-1025 State "+i+": "+SCP1025state[i]
		Next
		If SelectedMonitor <> Null Then
			Text x, 310, "Current monitor: "+SelectedMonitor\ScrObj
		Else
			Text x, 310, "Current monitor: NULL"
		EndIf
		
		SetFont Font1
	EndIf
End Function

Function DrawTimer()
	SetFont(Font2)
	Local durText$
	If TimerStopped = 0 Lor TimerStopped = 3 Then
		durText$ = FormatDuration(PlayTime)
	Else If TimerStopped = 1 Then
		durText = "The seventh seal has been broken"
	Else
		durText$ = I_Loc\HUD_SpeedrunSaveloaded
	EndIf
	Local x% = HUDEndX - StringWidth(durText) - 24 * HUDScale
	Local y% = HUDStartY + 24 * HUDScale
	Color 0, 0, 0
	Text(x + 3 * HUDScale, y + 3 * HUDScale, durText)
	If TimerStopped And TimerStopped<>3 Then
		Color 255, 0, 0
	Else
		If UsedConsole Then
			Color 150, 150, 150
		Else If First ActiveMods <> Null Then
			Color 200, 200, 200
		Else
			Color 255, 255, 255
		EndIf
	EndIf
	Text(x, y, durText)
	SetFont(Font1)
End Function

Function PadLeft$(txt$, padding$, targetLen%)
	Local req% = (targetLen - Len(txt)) / Len(padding)
	Return String(padding, req) + txt
End Function

Function FormatDuration$(totalMillis%, highPrecision%=True)
	Local ret$
	Local millis% = totalMillis Mod 1000
	Local totalSeconds% = totalMillis / 1000
	Local seconds% = totalSeconds Mod 60
	Local totalMinutes% = totalSeconds / 60
	Local minutes = totalMinutes Mod 60
	Local totalHours% = totalMinutes / 60
	Local hours% = totalHours Mod 24
	Local totalDays% = totalHours / 24
	If totalDays > 0 Then
		ret = ret + Str(totalDays) + ":"
	EndIf
	If totalHours > 0 Lor (Not highPrecision) Then
		ret = ret + PadLeft(Str(hours), "0", 2) + ":"
		hadLarger = True
	EndIf
	ret = ret + PadLeft(Str(minutes), "0", 2) + ":" + PadLeft(Str(seconds), "0", 2)
	If highPrecision Then
		Return ret + "." + PadLeft(Str(millis), "0", 3)
	Else
		Return ret
	EndIf
End Function

Function DrawMenu()
	CatchErrors("Uncaught (DrawMenu)")
	
	Local x%, y%, width%, height%
	Local steamOverlayActive = SteamActive And Steam_GetOverlayState()
	If api_GetFocus() = 0 Lor steamOverlayActive Then ;Game is out of focus -> pause the game
		If (Not Using294) Then
			MenuOpen = True
			UpdateMenuState()
		EndIf
		;Reduce the CPU take while game is not in focus, unless Steam overlay is active
		If Not steamOverlayActive Then Delay 1000
	EndIf
	If MenuOpen Then
		
		;DebugLog AchievementsMenu+"|"+OptionsMenu+"|"+QuitMSG
		
		If PlayerRoom\RoomTemplate\Name$ <> "exit1" And PlayerRoom\RoomTemplate\Name$ <> "gatea"
			If StopHidingTimer = 0 Then
				If EntityDistance(Curr173\Collider, Collider)<4.0 Or EntityDistance(Curr106\Collider, Collider)<4.0 Then 
					StopHidingTimer = 1
				EndIf	
			ElseIf StopHidingTimer < 40
				If KillTimer >= 0 Then 
					StopHidingTimer = StopHidingTimer+FPSfactor
					
					If StopHidingTimer => 40 Then
						PlaySound_Strict(HorrorSFX(15))
						Msg = I_Loc\Message_Stophiding
						MsgTimer = 6*70
						MenuOpen = False
						Return
					EndIf
				EndIf
			EndIf
		EndIf
		
		InvOpen = False
		OtherOpen = Null
		SelectedScreen = Null
		
		width = ImageWidth(PauseMenuIMG)
		height = ImageHeight(PauseMenuIMG)
		x = GraphicWidth / 2 - width / 2
		y = GraphicHeight / 2 - height / 2
		
		DrawImage PauseMenuIMG, x, y
		
		Color(255, 255, 255)
		
		x = x+132*MenuScale
		y = y+122*MenuScale	
		
		If (Not MouseDown1)
			OnSliderID = 0
		EndIf
		
		If AchievementsMenu > 0 Then
			SetFont Font2
			Text(x, y-(122-45)*MenuScale, I_Loc\Menu_AchievementsUpper,False,True)
			SetFont Font1
		ElseIf OptionsMenu > 0 Then
			SetFont Font2
			Text(x, y-(122-45)*MenuScale, I_Loc\Menu_OptionsUpper,False,True)
			SetFont Font1
		ElseIf QuitMSG > 0 Then
			SetFont Font2
			Text(x, y-(122-45)*MenuScale, I_Loc\Menu_QuitQuestion,False,True)
			SetFont Font1
		ElseIf KillTimer >= 0 Then
			SetFont Font2
			Text(x, y-(122-45)*MenuScale, I_Loc\Menu_Pause,False,True)
			SetFont Font1
		Else
			SetFont Font2
			Text(x, y-(122-45)*MenuScale, I_Loc\Menu_Dead,False,True)
			SetFont Font1
		End If		
		
		Local AchvXIMG% = (x + (22*MenuScale))
		Local scale# = GraphicHeight/768.0
		Local SeparationConst% = 76*scale
		Local imgsize% = 64
		
		If AchievementsMenu <= 0 And OptionsMenu <= 0 And QuitMSG <= 0
			SetFont Font1
			Text x, y, I_Loc\Menu_Difficulty+" "+SelectedDifficulty\localName
			Text x, y+20*MenuScale, I_Loc\Menu_Save+" "+CurrSave
			Text x, y+40*MenuScale, GetSeedString()
		ElseIf AchievementsMenu <= 0 And OptionsMenu > 0 And QuitMSG <= 0 And KillTimer >= 0
			If DrawButton(x + 101 * MenuScale, y + 390 * MenuScale, 230 * MenuScale, 60 * MenuScale, I_Loc\Menu_Back) Then
				AchievementsMenu = 0
				OptionsMenu = 0
				QuitMSG = 0
				MouseHit1 = False
				SaveOptionsINI()
				
				AntiAlias Opt_AntiAlias
				TextureLodBias TextureFloat#
			EndIf
			
			Color 0,255,0
			If OptionsMenu = 1
				Rect(x-10*MenuScale,y-5*MenuScale,110*MenuScale,40*MenuScale,True)
			ElseIf OptionsMenu = 2
				Rect(x+100*MenuScale,y-5*MenuScale,110*MenuScale,40*MenuScale,True)
			ElseIf OptionsMenu = 3
				Rect(x+210*MenuScale,y-5*MenuScale,110*MenuScale,40*MenuScale,True)
			ElseIf OptionsMenu = 4
				Rect(x+320*MenuScale,y-5*MenuScale,110*MenuScale,40*MenuScale,True)
			EndIf
			
			If DrawButton(x-5*MenuScale,y,100*MenuScale,30*MenuScale,I_Loc\Option_Graphics,False) Then OptionsMenu = 1
			If DrawButton(x+105*MenuScale,y,100*MenuScale,30*MenuScale,I_Loc\Option_Audio,False) Then OptionsMenu = 2
			If DrawButton(x+215*MenuScale,y,100*MenuScale,30*MenuScale,I_Loc\Option_Controls,False) Then OptionsMenu = 3
			If DrawButton(x+325*MenuScale,y,100*MenuScale,30*MenuScale,I_Loc\Option_Advanced,False) Then OptionsMenu = 4
			
			Local tx# = (GraphicWidth/2)+(width/2)
			Local ty# = y
			Local tw# = 400*MenuScale
			Local th# = 150*MenuScale
			
			Color 255,255,255
			Select OptionsMenu
				Case 1 ;Graphics
					SetFont Font1
					;[Block]
					y=y+50*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Vsync)
					Vsync% = DrawTick(x + 270 * MenuScale, y + MenuScale, Vsync%)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"vsync")
					EndIf
					
					y=y+30*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Antialias)
					Opt_AntiAlias = DrawTick(x + 270 * MenuScale, y + MenuScale, Opt_AntiAlias%)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"antialias")
					EndIf
					
					y=y+30*MenuScale
					
					ScreenGamma = (SlideBar(x + 270*MenuScale, y+6*MenuScale, 100*MenuScale, ScreenGamma*50.0, 1)/50.0)
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Gamma)
					If (MouseOn(x+270*MenuScale,y+6*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"gamma",ScreenGamma)
					EndIf
					
					y=y+50*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Texlod)
					TextureDetails = Slider5(x+270*MenuScale,y+6*MenuScale,100*MenuScale,TextureDetails,3,"0.8","0.4","0.0","-0.4","-0.8")
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
					If (MouseOn(x+270*MenuScale,y-6*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Or OnSliderID=3
						DrawOptionsTooltip(tx,ty,tw,th+100*MenuScale,"texquality")
					EndIf
					
					y=y+50*MenuScale
					Color 100,100,100
					Text(x, y, I_Loc\OptionName_Vram)	
					EnableVRam = DrawTick(x + 270 * MenuScale, y + MenuScale, EnableVRam, True)
					If MouseOn(x + 270 * MenuScale, y + MenuScale, 20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"vram")
					EndIf

					y=y+50*MenuScale

					HUDOffsetScale = SlideBar(x + 270*MenuScale, y+6*MenuScale,100*MenuScale, HUDOffsetScale*100, 5)/100
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Hudoffset)
					If (MouseOn(x+270*MenuScale,y+6*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=5
						DrawOptionsTooltip(tx,ty,tw,th,"hudoffset")
					EndIf
					UpdateHUDOffsets()

					y=y+50*MenuScale

					Local SlideBarFOV# = FOV-40
					SlideBarFOV = SlideBar(x + 270*MenuScale, y+6*MenuScale,100*MenuScale, SlideBarFOV*2.0, 4)/2.0
					FOV = Int(SlideBarFOV+40)
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_fov)
					Color 255,255,0
					Text(x + 5 * MenuScale, y + 25 * MenuScale, FOV+"°")
					If (MouseOn(x+270*MenuScale,y+6*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=4
						DrawOptionsTooltip(tx,ty,tw,th,"fov")
					EndIf
					ZoomCamera(FOV)
					;[End Block]
				Case 2 ;Audio
					SetFont Font1
					;[Block]
					y = y + 50*MenuScale
					
					MusicVolume = (SlideBar(x + 250*MenuScale, y-4*MenuScale, 100*MenuScale, MusicVolume*100.0, 1)/100.0)
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Musicvol)
					If (MouseOn(x+250*MenuScale,y-4*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"musicvol",MusicVolume)
					EndIf
					
					y = y + 30*MenuScale
					
					PrevSFXVolume = (SlideBar(x + 250*MenuScale, y-4*MenuScale, 100*MenuScale, SFXVolume*100.0, 2)/100.0)
					If (Not DeafPlayer) Then SFXVolume# = PrevSFXVolume#
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Soundvol)
					If (MouseOn(x+250*MenuScale,y-4*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=2
						DrawOptionsTooltip(tx,ty,tw,th,"soundvol",PrevSFXVolume)
					EndIf
					
					y = y + 30*MenuScale
					
					Color 100,100,100
					Text x, y, I_Loc\OptionName_Sfxautorelease
					EnableSFXRelease = DrawTick(x + 270 * MenuScale, y + MenuScale, EnableSFXRelease,True)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th+220*MenuScale,"sfxautorelease")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 100,100,100
					Text x, y, I_Loc\OptionName_Usertrack
					EnableUserTracks = DrawTick(x + 270 * MenuScale, y + MenuScale, EnableUserTracks,True)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"usertrack")
					EndIf
					
					If EnableUserTracks
						y = y + 30 * MenuScale
						Color 255,255,255
						Text x, y, I_Loc\OptionName_Usertrackmode
						UserTrackMode = DrawTick(x + 270 * MenuScale, y + MenuScale, UserTrackMode)
						If UserTrackMode
							Text x, y + 20 * MenuScale, I_Loc\OptionName_UsertrackmodeRepeat
						Else
							Text x, y + 20 * MenuScale, I_Loc\OptionName_UsertrackmodeRandom
						EndIf
						If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
							DrawOptionsTooltip(tx,ty,tw,th,"usertrackmode")
						EndIf
						;DrawButton(x, y + 30 * MenuScale, 190 * MenuScale, 25 * MenuScale, I_Loc\OptionName_Usertrackscan,False)
						;If MouseOn(x,y+30*MenuScale,190*MenuScale,25*MenuScale) And OnSliderID=0
						;	DrawOptionsTooltip(tx,ty,tw,th,"usertrackscan")
						;EndIf
					EndIf
					;[End Block]
				Case 3 ;Controls
					SetFont Font1
					;[Block]
					y = y + 50*MenuScale
					
					MouseSens = (SlideBar(x + 270*MenuScale, y-4*MenuScale, 100*MenuScale, (MouseSens+0.5)*100.0, 1)/100.0)-0.5
					Color(255, 255, 255)
					Text(x, y, I_Loc\OptionName_Mousesensitivity)
					If (MouseOn(x+270*MenuScale,y-4*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
						DrawOptionsTooltip(tx,ty,tw,th,"mousesensitivity",MouseSens)
					EndIf
					
					y = y + 30*MenuScale
					
					Color(255, 255, 255)
					Text(x, y, I_Loc\OptionName_Mouseinvert)
					InvertMouse = DrawTick(x + 270 * MenuScale, y + MenuScale, InvertMouse)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"mouseinvert")
					EndIf
					
					y = y + 40*MenuScale
					
					MouseSmooth = (SlideBar(x + 270*MenuScale, y-4*MenuScale, 100*MenuScale, (MouseSmooth)*50.0, 2)/50.0)
					Color(255, 255, 255)
					Text(x, y, I_Loc\OptionName_Mousesmoothing)
					If (MouseOn(x+270*MenuScale,y-4*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=2
						DrawOptionsTooltip(tx,ty,tw,th,"mousesmoothing",MouseSmooth)
					EndIf
					
					Color(255, 255, 255)
					
					y = y + 30*MenuScale
					Text(x, y, I_Loc\OptionName_Binds)
					y = y + 10*MenuScale
					
					Text(x, y + 20 * MenuScale, I_Loc\OptionName_BindMoveForward)
					InputBox(x + 200 * MenuScale, y + 20 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_UP,210)),5)		
					Text(x, y + 40 * MenuScale, I_Loc\OptionName_BindMoveLeft)
					InputBox(x + 200 * MenuScale, y + 40 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_LEFT,210)),3)	
					Text(x, y + 60 * MenuScale, I_Loc\OptionName_BindMoveBack)
					InputBox(x + 200 * MenuScale, y + 60 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_DOWN,210)),6)				
					Text(x, y + 80 * MenuScale, I_Loc\OptionName_BindMoveRight)
					InputBox(x + 200 * MenuScale, y + 80 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_RIGHT,210)),4)
					
					Text(x, y + 100 * MenuScale, I_Loc\OptionName_BindBlink)
					InputBox(x + 200 * MenuScale, y + 100 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_BLINK,210)),7)				
					Text(x, y + 120 * MenuScale, I_Loc\OptionName_BindSprint)
					InputBox(x + 200 * MenuScale, y + 120 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_SPRINT,210)),8)
					Text(x, y + 140 * MenuScale, I_Loc\OptionName_BindInv)
					InputBox(x + 200 * MenuScale, y + 140 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_INV,210)),9)
					Text(x, y + 160 * MenuScale, I_Loc\OptionName_BindCrouch)
					InputBox(x + 200 * MenuScale, y + 160 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_CROUCH,210)),10)
					Text(x, y + 180 * MenuScale, I_Loc\OptionName_BindSave)
					InputBox(x + 200 * MenuScale, y + 180 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_SAVE,210)),11)	
					Text(x, y + 200 * MenuScale, I_Loc\OptionName_BindConsole)
					InputBox(x + 200 * MenuScale, y + 200 * MenuScale,100*MenuScale,20*MenuScale,KeyName(Min(KEY_CONSOLE,210)),12)

					If MouseOn(x,y,300*MenuScale,220*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"controls")
					EndIf
					
					For i = 0 To 227
						If KeyHit(i) Then key = i : Exit
					Next
					If key <> 0 Then
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
				Case 4 ;Advanced
					SetFont Font1
					;[Block]
					y = y + 50*MenuScale
					
					Color 255,255,255				
					Text(x, y, I_Loc\OptionName_Showhud)	
					HUDenabled = DrawTick(x + 270 * MenuScale, y + MenuScale, HUDenabled)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"hud")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Console)
					CanOpenConsole = DrawTick(x +270 * MenuScale, y + MenuScale, CanOpenConsole)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"consoleenable")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Consoleerror)
					ConsoleOpening = DrawTick(x + 270 * MenuScale, y + MenuScale, ConsoleOpening)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"consoleerror")
					EndIf

					y = y + 30*MenuScale

					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Speedrunmode)
					SpeedRunMode = DrawTick(x + 270 * MenuScale, y + MenuScale, SpeedRunMode)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"speedrunmode")
					EndIf
					
					y = y + 30*MenuScale

					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Numericseeds)
					UseNumericSeeds = DrawTick(x + 270 * MenuScale, y + MenuScale, UseNumericSeeds)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"numericseeds")
					EndIf

					y = y + 50*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Achpopup)
					AchvMSGenabled% = DrawTick(x + 270 * MenuScale, y, AchvMSGenabled%)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"achpopup")
					EndIf
					
					y = y + 50*MenuScale

					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Launcher)
					LauncherEnabled% = DrawTick(x + 270 * MenuScale, y, LauncherEnabled%)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"launcher")
					EndIf
					
					y = y + 50*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Showfps)
					ShowFPS% = DrawTick(x + 270 * MenuScale, y, ShowFPS%)
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"showfps")
					EndIf
					
					y = y + 30*MenuScale
					
					Color 255,255,255
					Text(x, y, I_Loc\OptionName_Framelimit)
					
					Color 255,255,255
					If DrawTick(x + 270 * MenuScale, y, CurrFrameLimit > 0.0) Then
						;CurrFrameLimit# = (SlideBar(x + 150*MenuScale, y+30*MenuScale, 100*MenuScale, CurrFrameLimit#*50.0, 1)/50.0)
						;CurrFrameLimit = Max(CurrFrameLimit, 0.1)
						;Framelimit% = CurrFrameLimit#*100.0
						CurrFrameLimit# = (SlideBar(x + 150*MenuScale, y+30*MenuScale, 100*MenuScale, CurrFrameLimit#*99.0, 1)/99.0)
						CurrFrameLimit# = Max(CurrFrameLimit, 0.01)
						Framelimit% = 19+(CurrFrameLimit*100.0)
						Color 255,255,0
						Text(x + 5 * MenuScale, y + 25 * MenuScale, Format(I_Loc\OptionName_FramelimitFps, Framelimit%))
						If (MouseOn(x+150*MenuScale,y+30*MenuScale,100*MenuScale+14,20) And OnSliderID=0) Lor OnSliderID=1
							DrawOptionsTooltip(tx,ty,tw,th,"framelimit",Framelimit)
						EndIf
					Else
						CurrFrameLimit# = 0.0
						Framelimit = 0
					EndIf
					If MouseOn(x+270*MenuScale,y+MenuScale,20*MenuScale,20*MenuScale) And OnSliderID=0
						DrawOptionsTooltip(tx,ty,tw,th,"framelimit",Framelimit)
					EndIf
					;[End Block]
			End Select
		ElseIf AchievementsMenu <= 0 And OptionsMenu <= 0 And QuitMSG > 0 And KillTimer >= 0
			Local QuitButton% = 60 
			If SelectedDifficulty\saveType = SAVEONQUIT Or SelectedDifficulty\saveType = SAVEANYWHERE Then
				Local RN$ = PlayerRoom\RoomTemplate\Name$
				Local AbleToSave% = True
				If RN$ = "173" Or RN$ = "exit1" Or RN$ = "gatea" Then AbleToSave = False
				If (Not CanSave) Then AbleToSave = False
				If AbleToSave
					QuitButton = 140
					If DrawButton(x, y + 60*MenuScale, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Savequit) Then
						DropSpeed = 0
						SaveGame(SavePath + CurrSave)
						NullGame()
						MenuOpen = False
						MainMenuOpen = True
						MainMenuTab = 0
						PrevSave = CurrSave
						CurrSave = ""
						FlushKeys()
						Return
					EndIf
				EndIf
			EndIf
			
			If DrawButton(x, y + QuitButton*MenuScale, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Quit) Then
				NullGame()
				MenuOpen = False
				MainMenuOpen = True
				MainMenuTab = 0
				PrevSave = CurrSave
				CurrSave = ""
				FlushKeys()
				Return
			EndIf
			
			If DrawButton(x+101*MenuScale, y + 344*MenuScale, 230*MenuScale, 60*MenuScale, I_Loc\Menu_Back) Then
				AchievementsMenu = 0
				OptionsMenu = 0
				QuitMSG = 0
				MouseHit1 = False
			EndIf
		Else
			If DrawButton(x+101*MenuScale, y + 344*MenuScale, 230*MenuScale, 60*MenuScale, I_Loc\Menu_Back) Then
				AchievementsMenu = 0
				OptionsMenu = 0
				QuitMSG = 0
				MouseHit1 = False
			EndIf
			
			If AchievementsMenu>0 Then
				;DebugLog AchievementsMenu
				If AchievementsMenu <= Floor(Float(MAXACHIEVEMENTS-1)/12.0) Then 
					If DrawButton(x+341*MenuScale, y + 344*MenuScale, 50*MenuScale, 60*MenuScale, ">") Then
						AchievementsMenu = AchievementsMenu+1
					EndIf
				EndIf
				If AchievementsMenu > 1 Then
					If DrawButton(x+41*MenuScale, y + 344*MenuScale, 50*MenuScale, 60*MenuScale, "<") Then
						AchievementsMenu = AchievementsMenu-1
					EndIf
				EndIf
				
				For i=0 To 11
					If i+((AchievementsMenu-1)*12)<MAXACHIEVEMENTS Then
						DrawAchvIMG(AchvXIMG,y+((i/4)*120*MenuScale),i+((AchievementsMenu-1)*12))
					Else
						Exit
					EndIf
				Next
				
				For i=0 To 11
					If i+((AchievementsMenu-1)*12)<MAXACHIEVEMENTS Then
						If MouseOn(AchvXIMG+((i Mod 4)*SeparationConst),y+((i/4)*120*MenuScale),64*scale,64*scale) Then
							AchievementTooltip(i+((AchievementsMenu-1)*12))
							Exit
						EndIf
					Else
						Exit
					EndIf
				Next
				
			EndIf
		EndIf
		
		y = y+10
		
		If AchievementsMenu<=0 And OptionsMenu<=0 And QuitMSG<=0 Then
			If KillTimer >= 0 Then	
				
				y = y+ 72*MenuScale
				
				If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Resume, True, True) Then
					MenuOpen = False
					UpdateMenuState()
				EndIf
				
				y = y + 75*MenuScale
				If (Not SelectedDifficulty\permaDeath) Then
					If GameSaved Then
						If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Load) Then
							DrawLoading(0)
							
							MenuOpen = False
							LoadGameQuick(SavePath + CurrSave)
							
							MoveMouse viewport_center_x,viewport_center_y
							SetFont Font1
							HidePointer ()
							
							FlushKeys()
							FlushMouse()
							Playable=True
							
							UpdateRooms()
							
							For r.Rooms = Each Rooms
								x = Abs(EntityX(Collider) - EntityX(r\obj))
								z = Abs(EntityZ(Collider) - EntityZ(r\obj))
								
								If x < 12.0 And z < 12.0 Then
									MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = Max(MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)), 1)
									If x < 4.0 And z < 4.0 Then
										If Abs(EntityY(Collider) - EntityY(r\obj)) < 1.5 Then PlayerRoom = r
										MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = 1
									EndIf
								End If
							Next
							
							DrawLoading(100)
							
							DropSpeed=0
							
							UpdateWorld 0.0
							
							PrevTime = MilliSecs()
							FPSfactor = 0
							
							ResetInput()
						EndIf
					Else
						DrawFrame(x,y,390*MenuScale, 60*MenuScale)
						Color (100, 100, 100)
						SetFont Font2
						Text(x + (390*MenuScale) / 2, y + (60*MenuScale) / 2, I_Loc\Menu_Load, True, True)
					EndIf
					y = y + 75*MenuScale
			EndIf
				
				If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Achievements) Then AchievementsMenu = 1
				y = y + 75*MenuScale
				If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Options) Then OptionsMenu = 1 : OnSliderID = 66
				y = y + 75*MenuScale
			Else
				y = y+104*MenuScale
				If GameSaved And (Not SelectedDifficulty\permaDeath) Then
					If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Load) Then
						DrawLoading(0)
						
						MenuOpen = False
						LoadGameQuick(SavePath + CurrSave)
						
						MoveMouse viewport_center_x,viewport_center_y
						SetFont Font1
						HidePointer ()
						
						FlushKeys()
						FlushMouse()
						Playable=True
						
						UpdateRooms()
						
						For r.Rooms = Each Rooms
							x = Abs(EntityX(Collider) - EntityX(r\obj))
							z = Abs(EntityZ(Collider) - EntityZ(r\obj))
							
							If x < 12.0 And z < 12.0 Then
								MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = Max(MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)), 1)
								If x < 4.0 And z < 4.0 Then
									If Abs(EntityY(Collider) - EntityY(r\obj)) < 1.5 Then PlayerRoom = r
									MapFound(Floor(EntityX(r\obj) / 8.0), Floor(EntityZ(r\obj) / 8.0)) = 1
								EndIf
							End If
						Next
						
						DrawLoading(100)
						
						DropSpeed=0
						
						UpdateWorld 0.0
						
						PrevTime = MilliSecs()
						FPSfactor = 0
						
						ResetInput()
					EndIf
				Else
					DrawButton(x, y, 390*MenuScale, 60*MenuScale, "")
					Color 50,50,50
					Text(x + 185*MenuScale, y + 30*MenuScale, I_Loc\Menu_Load, True, True)
				EndIf
				If DrawButton(x, y + 80*MenuScale, 390*MenuScale, 60*MenuScale, I_Loc\Menu_QuitMenu) Then
					NullGame()
					MenuOpen = False
					MainMenuOpen = True
					MainMenuTab = 0
					PrevSave = CurrSave
					CurrSave = ""
					TimerStopped = True
					FlushKeys()
				EndIf
				y= y + 80*MenuScale
			EndIf
			
			If KillTimer >= 0 And (Not MainMenuOpen)
				If DrawButton(x, y, 390*MenuScale, 60*MenuScale, I_Loc\Menu_Quit) Then
					QuitMSG = 1
				EndIf
			EndIf
			
			SetFont Font1
			If KillTimer < 0 Then RowText(DeathMSG$, x, y + 80*MenuScale, 390*MenuScale, 600*MenuScale)
		EndIf
		
		If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
		
	End If
	
	SetFont Font1
	
	CatchErrors("DrawMenu")
End Function

Function GetSeedString$()
	If HasNumericSeed Then
		Return I_Loc\Menu_SeedNumeric+" "+Str(RandomSeedNumeric)
	Else
		Return I_Loc\Menu_Seed+" "+RandomSeed
	EndIf
End Function

Function MouseOn%(x%, y%, width%, height%)
	If ScaledMouseX() > x And ScaledMouseX() < x + width Then
		If ScaledMouseY() > y And ScaledMouseY() < y + height Then
			Return True
		End If
	End If
	Return False
End Function

;----------------------------------------------------------------------------------------------

Include "LoadAllSounds.bb"
Function LoadEntities()
	CatchErrors("Uncaught (LoadEntities)")
	DrawLoading(0)
	
	Local i%
	
	For i=0 To 9
		TempSounds[i]=0
	Next
	
	PauseMenuIMG% = LoadImage_Strict("GFX\menu\pausemenu.jpg")
	MaskImage PauseMenuIMG, 255,255,0
	ScaleImage PauseMenuIMG,MenuScale,MenuScale
	
	SprintIcon% = LoadImage_Strict("GFX\sprinticon.png")
	ScaleImage(SprintIcon, HUDScale, HUDScale)
	BlinkIcon% = LoadImage_Strict("GFX\blinkicon.png")
	ScaleImage(BlinkIcon, HUDScale, HUDScale)
	CrouchIcon% = LoadImage_Strict("GFX\sneakicon.png")
	ScaleImage(CrouchIcon, HUDScale, HUDScale)
	HandIcon% = LoadImage_Strict("GFX\handsymbol.png")
	ScaleImage(HandIcon, HUDScale, HUDScale)
	HandIcon2% = LoadImage_Strict("GFX\handsymbol2.png")
	ScaleImage(HandIcon2, HUDScale, HUDScale)

	StaminaMeterIMG% = LoadImage_Strict("GFX\staminameter.jpg")
	ScaleImage(StaminaMeterIMG, HUDScale, HUDScale)

	Panel294 = LoadImage_Strict("GFX\294panel.jpg")
	ScaleImage(Panel294, HUDScale, HUDScale)
	
	
	Brightness% = GetModdedINIFloat(MapOptions, "facility", "brightness")
	CameraFogNear# = GetModdedINIFloat(MapOptions, "facility", "camera fog near")
	CameraFogFar# = GetModdedINIFloat(MapOptions, "facility", "camera fog far")
	StoredCameraFogFar# = CameraFogFar
	
	;TextureLodBias
	
	AmbientLightRoomTex% = CreateTexture(2,2,257)
	AmbientLight = GetModdedINIInt(MapOptions, "facility", "ambient light")
	AmbientLightNVG = GetModdedINIInt(MapOptions, "facility", "ambient light nvg")

	TextureBlend AmbientLightRoomTex,5
	SetBuffer(TextureBuffer(AmbientLightRoomTex))
	ClsColor 0,0,0
	Cls
	SetBuffer BackBuffer()
	AmbientLightRoomVal = 0
	
	SoundEmitter = CreatePivot()
	
	Camera = CreateCamera()
	CameraViewport Camera,0,0,GraphicWidth,GraphicHeight
	CameraRange(Camera, 0.05, CameraFogFar)
	CameraFogMode (Camera, 1)
	CameraFogRange (Camera, CameraFogNear, CameraFogFar)
	AmbientLight Brightness, Brightness, Brightness
	
	ScreenTexs[0] = CreateTexture(512, 512, 1+256)
	ScreenTexs[1] = CreateTexture(512, 512, 1+256)
	
	CreateBlurImage()
	CameraProjMode ark_blur_cam,0
	;Listener = CreateListener(Camera)
	
	FogTexture = LoadTexture_Strict("GFX\fog.jpg", 1)
	
	Fog = CreateSprite(ark_blur_cam)
	ScaleSprite(Fog, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(Fog, FogTexture)
	EntityBlend (Fog, 2)
	EntityOrder Fog, -1000
	MoveEntity(Fog, 0, 0, 1.0)
	
	GasMaskTexture = LoadTexture_Strict("GFX\GasmaskOverlay.jpg", 1)
	GasMaskOverlay = CreateSprite(ark_blur_cam)
	ScaleSprite(GasMaskOverlay, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(GasMaskOverlay, GasMaskTexture)
	EntityBlend (GasMaskOverlay, 2)
	EntityFX(GasMaskOverlay, 1)
	EntityOrder GasMaskOverlay, -1003
	MoveEntity(GasMaskOverlay, 0, 0, 1.0)
	HideEntity(GasMaskOverlay)
	
	InfectTexture = LoadTexture_Strict("GFX\InfectOverlay.jpg", 1)
	InfectOverlay = CreateSprite(ark_blur_cam)
	ScaleSprite(InfectOverlay, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(InfectOverlay, InfectTexture)
	EntityBlend (InfectOverlay, 3)
	EntityFX(InfectOverlay, 1)
	EntityOrder InfectOverlay, -1003
	MoveEntity(InfectOverlay, 0, 0, 1.0)
	;EntityAlpha (InfectOverlay, 255.0)
	HideEntity(InfectOverlay)
	
	NVTexture = LoadTexture_Strict("GFX\NightVisionOverlay.jpg", 1)
	NVOverlay = CreateSprite(ark_blur_cam)
	ScaleSprite(NVOverlay, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(NVOverlay, NVTexture)
	EntityBlend (NVOverlay, 2)
	EntityFX(NVOverlay, 1)
	EntityOrder NVOverlay, -1003
	MoveEntity(NVOverlay, 0, 0, 1.0)
	HideEntity(NVOverlay)
	NVBlink = CreateSprite(ark_blur_cam)
	ScaleSprite(NVBlink, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityColor(NVBlink,0,0,0)
	EntityFX(NVBlink, 1)
	EntityOrder NVBlink, -1005
	MoveEntity(NVBlink, 0, 0, 1.0)
	HideEntity(NVBlink)
	
	FogNVTexture = LoadTexture_Strict("GFX\fogNV.jpg", 1)
	
	DrawLoading(5)
	
	DarkTexture = CreateTexture(1024, 1024, 1 + 2)
	SetBuffer TextureBuffer(DarkTexture)
	Cls
	SetBuffer BackBuffer()
	
	Dark = CreateSprite(ark_blur_cam)
	ScaleSprite(Dark, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(Dark, DarkTexture)
	EntityBlend (Dark, 1)
	EntityOrder Dark, -1002
	MoveEntity(Dark, 0, 0, 1.0)
	EntityAlpha Dark, 0.0
	
	LightTexture = CreateTexture(1024, 1024, 1 + 2+256)
	SetBuffer TextureBuffer(LightTexture)
	ClsColor 255, 255, 255
	Cls
	ClsColor 0, 0, 0
	SetBuffer BackBuffer()
	
	TeslaTexture = LoadTexture_Strict("GFX\map\tesla.jpg", 1+2)
	
	Light = CreateSprite(ark_blur_cam)
	ScaleSprite(Light, 1.0, Max(Float(GraphicHeight) / Float(GraphicWidth), 0.8))
	EntityTexture(Light, LightTexture)
	EntityBlend (Light, 1)
	EntityOrder Light, -1002
	MoveEntity(Light, 0, 0, 1.0)
	HideEntity Light
	
	Collider = CreatePivot()
	EntityRadius Collider, 0.15, 0.30
	EntityPickMode(Collider, 1)
	EntityType Collider, HIT_PLAYER
	
	Head = CreatePivot()
	EntityRadius Head, 0.15
	EntityType Head, HIT_PLAYER
	
	
	LiquidObj = LoadMesh_Strict("GFX\items\cupliquid.x") ;optimized the cups dispensed by 294
	HideEntity LiquidObj
	
	MTFObj = LoadAnimMesh_Strict("GFX\npcs\MTF2.b3d") ;optimized MTFs
	GuardObj = LoadAnimMesh_Strict("GFX\npcs\guard.b3d") ;optimized Guards
	;GuardTex = LoadTexture_Strict("GFX\npcs\body.jpg") ;optimized the guards even more
	
	;If BumpEnabled Then
	;	bump1 = LoadTexture_Strict("GFX\npcs\mtf_newnormal01.png")
	;	;TextureBlend bump1, FE_BUMP ;USE DOT3
	;		
	;	For i = 2 To CountSurfaces(MTFObj)
	;		sf = GetSurface(MTFObj,i)
	;		b = GetSurfaceBrush( sf )
	;		t1 = GetBrushTexture(b,0)
	;		
	;		Select Lower(StripPath(TextureName(t1)))
	;			Case "MTF_newdiffuse02.png"
	;				
	;				BrushTexture b, bump1, 0, 0
	;				BrushTexture b, t1, 0, 1
	;				PaintSurface sf,b
	;		End Select
	;		FreeBrush b
	;		FreeTexture t1
	;	Next
	;	FreeTexture bump1	
	;EndIf
	
	
	
	ClassDObj = LoadAnimMesh_Strict("GFX\npcs\classd.b3d") ;optimized Class-D's and scientists/researchers
	ApacheObj = LoadAnimMesh_Strict("GFX\apache.b3d") ;optimized Apaches (helicopters)
	ApacheRotorObj = LoadAnimMesh_Strict("GFX\apacherotor.b3d") ;optimized the Apaches even more
	
	HideEntity MTFObj
	HideEntity GuardObj
	HideEntity ClassDObj
	HideEntity ApacheObj
	HideEntity ApacheRotorObj
	
	;Other NPCs pre-loaded
	;[Block]
	NPC049OBJ = LoadAnimMesh_Strict("GFX\npcs\scp-049.b3d")
	HideEntity NPC049OBJ
	NPC0492OBJ = LoadAnimMesh_Strict("GFX\npcs\zombie1.b3d")
	HideEntity NPC0492OBJ
	ClerkOBJ = LoadAnimMesh_Strict("GFX\npcs\clerk.b3d")
	HideEntity ClerkOBJ	
	;[End Block]
	
;	For i=0 To 4
;		Select True
;			Case i=2
;				tempStr="2c"
;			Case i>2
;				tempStr=Str(i)
;			Default
;				tempStr=Str(i+1)
;		End Select
;		OBJTunnel(i)=LoadRMesh("GFX\map\mt"+tempStr+".rmesh",Null)
;		HideEntity OBJTunnel(i)
;	Next
	
;	OBJTunnel(0)=LoadRMesh("GFX\map\mt1.rmesh",Null)	
;	HideEntity OBJTunnel(0)				
;	OBJTunnel(1)=LoadRMesh("GFX\map\mt2.rmesh",Null)	
;	HideEntity OBJTunnel(1)
;	OBJTunnel(2)=LoadRMesh("GFX\map\mt2c.rmesh",Null)	
;	HideEntity OBJTunnel(2)				
;	OBJTunnel(3)=LoadRMesh("GFX\map\mt3.rmesh",Null)	
;	HideEntity OBJTunnel(3)	
;	OBJTunnel(4)=LoadRMesh("GFX\map\mt4.rmesh",Null)	
;	HideEntity OBJTunnel(4)				
;	OBJTunnel(5)=LoadRMesh("GFX\map\mt_elevator.rmesh",Null)
;	HideEntity OBJTunnel(5)
;	OBJTunnel(6)=LoadRMesh("GFX\map\mt_generator.rmesh",Null)
;	HideEntity OBJTunnel(6)
	
	LightSpriteTex(0) = LoadTexture_Strict("GFX\light1.jpg", 1)
	LightSpriteTex(1) = LoadTexture_Strict("GFX\light2.jpg", 1)
	LightSpriteTex(2) = LoadTexture_Strict("GFX\lightsprite.jpg",1)
	
	DrawLoading(10)
	
	DoorOBJ = LoadMesh_Strict("GFX\map\door01.x")
	HideEntity DoorOBJ
	DoorFrameOBJ = LoadMesh_Strict("GFX\map\doorframe.x")
	HideEntity DoorFrameOBJ
	
	HeavyDoorObj(0) = LoadMesh_Strict("GFX\map\heavydoor1.x")
	HideEntity HeavyDoorObj(0)
	HeavyDoorObj(1) = LoadMesh_Strict("GFX\map\heavydoor2.x")
	HideEntity HeavyDoorObj(1)
	
	DoorColl = LoadMesh_Strict("GFX\map\doorcoll.x")
	HideEntity DoorColl
	
	ButtonOBJ = LoadMesh_Strict("GFX\map\Button.x")
	HideEntity ButtonOBJ
	ButtonKeyOBJ = LoadMesh_Strict("GFX\map\ButtonKeycard.x")
	HideEntity ButtonKeyOBJ
	ButtonCodeOBJ = LoadMesh_Strict("GFX\map\ButtonCode.x")
	HideEntity ButtonCodeOBJ	
	ButtonScannerOBJ = LoadMesh_Strict("GFX\map\ButtonScanner.x")
	HideEntity ButtonScannerOBJ	
	
	BigDoorOBJ(0) = LoadMesh_Strict("GFX\map\ContDoorLeft.x")
	HideEntity BigDoorOBJ(0)
	BigDoorOBJ(1) = LoadMesh_Strict("GFX\map\ContDoorRight.x")
	HideEntity BigDoorOBJ(1)
	
	LeverBaseOBJ = LoadMesh_Strict("GFX\map\leverbase.x")
	HideEntity LeverBaseOBJ
	LeverOBJ = LoadMesh_Strict("GFX\map\leverhandle.x")
	HideEntity LeverOBJ
	
	;For i = 0 To 1
	;	HideEntity BigDoorOBJ(i)
	;	;If BumpEnabled And 0 Then
	;	If BumpEnabled
	;		
	;		Local bumptex = LoadTexture_Strict("GFX\map\containmentdoorsbump.jpg")
	;		;TextureBlend bumptex, FE_BUMP
	;		Local tex = LoadTexture_Strict("GFX\map\containment_doors.jpg")	
	;		EntityTexture BigDoorOBJ(i), bumptex, 0, 0
	;		EntityTexture BigDoorOBJ(i), tex, 0, 1
	;		
	;		;FreeEntity tex
	;		;FreeEntity bumptex
	;		FreeTexture tex
	;		FreeTexture bumptex
	;	EndIf
	;Next
	
	DrawLoading(15)
	
	For i = 0 To 5
		GorePics(i) = LoadTexture_Strict("GFX\895pics\pic" + (i + 1) + ".jpg")
	Next
	
	OldAiPics(0) = LoadTexture_Strict("GFX\AIface.jpg")
	OldAiPics(1) = LoadTexture_Strict("GFX\AIface2.jpg")	
	
	DrawLoading(20)
	
	For i = 0 To 6
		DecalTextures(i) = LoadTexture_Strict("GFX\decal" + (i + 1) + ".png", 1 + 2)
	Next
	DecalTextures(7) = LoadTexture_Strict("GFX\items\INVpaperstrips.jpg", 1 + 2)
	For i = 8 To 12
		DecalTextures(i) = LoadTexture_Strict("GFX\decalpd"+(i-7)+".jpg", 1 + 2)	
	Next
	For i = 13 To 14
		DecalTextures(i) = LoadTexture_Strict("GFX\bullethole"+(i-12)+".jpg", 1 + 2)	
	Next	
	For i = 15 To 16
		DecalTextures(i) = LoadTexture_Strict("GFX\blooddrop"+(i-14)+".png", 1 + 2)	
	Next
	DecalTextures(17) = LoadTexture_Strict("GFX\decal8.png", 1 + 2)	
	DecalTextures(18) = LoadTexture_Strict("GFX\decalpd6.dc", 1 + 2)	
	DecalTextures(19) = LoadTexture_Strict("GFX\decal19.png", 1 + 2)
	DecalTextures(20) = LoadTexture_Strict("GFX\decal427.png", 1 + 2)
	
	DrawLoading(24)
	
	Monitor = LoadMesh_Strict("GFX\map\monitor.b3d")
	HideEntity Monitor
	MonitorTexture = LoadTexture_Strict("GFX\monitortexture.jpg")
	
	CamBaseOBJ = LoadMesh_Strict("GFX\map\cambase.x")
	HideEntity(CamBaseOBJ)
	CamOBJ = LoadMesh_Strict("GFX\map\CamHead.b3d")
	HideEntity(CamOBJ)

	Monitor2 = LoadMesh_Strict("GFX\map\monitor_checkpoint.b3d")
	HideEntity Monitor2
	Monitor3 = LoadMesh_Strict("GFX\map\monitor_checkpoint.b3d")
	HideEntity Monitor3
	MonitorTexture2 = LoadTexture_Strict("GFX\map\LockdownScreen2.jpg")
	MonitorTexture3 = LoadTexture_Strict("GFX\map\LockdownScreen.jpg")
	MonitorTexture4 = LoadTexture_Strict("GFX\map\LockdownScreen3.jpg")
	MonitorTextureOff = CreateTexture(1,1)
	SetBuffer TextureBuffer(MonitorTextureOff)
	ClsColor 0,0,0
	Cls
	SetBuffer BackBuffer()
	LightConeModel = LoadMesh_Strict("GFX\lightcone.b3d")
	HideEntity LightConeModel
	
	For i = 2 To CountSurfaces(Monitor2)
		sf = GetSurface(Monitor2,i)
		b = GetSurfaceBrush(sf)
		If b<>0 Then
			t1 = GetBrushTexture(b,0)
			If t1<>0 Then
				name$ = StripPath(TextureName(t1))
				If Lower(name) <> "monitortexture.jpg"
					BrushTexture b, MonitorTextureOff, 0, 0
					PaintSurface sf,b
				EndIf
				If name<>"" Then FreeTexture t1
			EndIf
			FreeBrush b
		EndIf
	Next
	For i = 2 To CountSurfaces(Monitor3)
		sf = GetSurface(Monitor3,i)
		b = GetSurfaceBrush(sf)
		If b<>0 Then
			t1 = GetBrushTexture(b,0)
			If t1<>0 Then
				name$ = StripPath(TextureName(t1))
				If Lower(name) <> "monitortexture.jpg"
					BrushTexture b, MonitorTextureOff, 0, 0
					PaintSurface sf,b
				EndIf
				If name<>"" Then FreeTexture t1
			EndIf
			FreeBrush b
		EndIf
	Next
	
	UserTrackMusicAmount% = 0
	If EnableUserTracks Then
		Local dirPath$ = "SFX\Radio\UserTracks\"
		If FileType(dirPath)<>2 Then
			CreateDir(dirPath)
		EndIf
		
		Local Dir% = ReadDir("SFX\Radio\UserTracks\")
		Repeat
			file$=NextFile(Dir)
			If file$="" Then Exit
			If FileType("SFX\Radio\UserTracks\"+file$) = 1 Then
				test = LoadSound("SFX\Radio\UserTracks\"+file$)
				If test<>0
					UserTrackName$(UserTrackMusicAmount%) = file$
					UserTrackMusicAmount% = UserTrackMusicAmount% + 1
				EndIf
				FreeSound test
			EndIf
		Forever
		CloseDir Dir
	EndIf
	If EnableUserTracks Then DebugLog "User Tracks found: "+UserTrackMusicAmount
	
	DrawLoading(25)

	InitItemTemplates()
	
	DrawLoading(35)

	ParticleTextures(0) = LoadTexture_Strict("GFX\smoke.png", 1 + 2)
	ParticleTextures(1) = LoadTexture_Strict("GFX\flash.jpg", 1 + 2)
	ParticleTextures(2) = LoadTexture_Strict("GFX\dust.jpg", 1 + 2)
	ParticleTextures(3) = LoadTexture_Strict("GFX\npcs\hg.pt", 1 + 2)
	ParticleTextures(4) = LoadTexture_Strict("GFX\map\sun.jpg", 1 + 2)
	ParticleTextures(5) = LoadTexture_Strict("GFX\bloodsprite.png", 1 + 2)
	ParticleTextures(6) = LoadTexture_Strict("GFX\smoke2.png", 1 + 2)
	ParticleTextures(7) = LoadTexture_Strict("GFX\spark.jpg", 1 + 2)
	ParticleTextures(8) = LoadTexture_Strict("GFX\particle.png", 1 + 2)
	
	SetChunkDataValues()
	
	;NPCtypeD - different models with different textures (loaded using "CopyEntity") - ENDSHN
	;[Block]
	For i=1 To MaxDTextures-1
		DTextures[i] = CopyEntity(ClassDObj)
		HideEntity DTextures[i]
	Next
	;Gonzales
	tex = LoadTexture_Strict("GFX\npcs\gonzales.jpg")
	EntityTexture DTextures[1],tex
	FreeTexture tex
	;SCP-970 corpse
	tex = LoadTexture_Strict("GFX\npcs\corpse.jpg")
	EntityTexture DTextures[2],tex
	FreeTexture tex
	;scientist 1
	tex = LoadTexture_Strict("GFX\npcs\scientist.jpg")
	EntityTexture DTextures[3],tex
	FreeTexture tex
	;scientist 2
	tex = LoadTexture_Strict("GFX\npcs\scientist2.jpg")
	EntityTexture DTextures[4],tex
	FreeTexture tex
	;janitor
	tex = LoadTexture_Strict("GFX\npcs\janitor.jpg")
	EntityTexture DTextures[5],tex
	FreeTexture tex
	;106 Victim
	tex = LoadTexture_Strict("GFX\npcs\106victim.jpg")
	EntityTexture DTextures[6],tex
	FreeTexture tex
	;2nd ClassD
	tex = LoadTexture_Strict("GFX\npcs\classd2.jpg")
	EntityTexture DTextures[7],tex
	FreeTexture tex
	;035 victim
	tex = LoadTexture_Strict("GFX\npcs\035victim.jpg")
	EntityTexture DTextures[8],tex
	FreeTexture tex
	
	;[End Block]

	InitMaterials()
	
	OBJTunnel(0)=LoadRMesh("GFX\map\mt1.rmesh",Null)	
	HideEntity OBJTunnel(0)				
	OBJTunnel(1)=LoadRMesh("GFX\map\mt2.rmesh",Null)	
	HideEntity OBJTunnel(1)
	OBJTunnel(2)=LoadRMesh("GFX\map\mt2c.rmesh",Null)	
	HideEntity OBJTunnel(2)				
	OBJTunnel(3)=LoadRMesh("GFX\map\mt3.rmesh",Null)	
	HideEntity OBJTunnel(3)	
	OBJTunnel(4)=LoadRMesh("GFX\map\mt4.rmesh",Null)	
	HideEntity OBJTunnel(4)				
	OBJTunnel(5)=LoadRMesh("GFX\map\mt_elevator.rmesh",Null)
	HideEntity OBJTunnel(5)
	OBJTunnel(6)=LoadRMesh("GFX\map\mt_generator.rmesh",Null)
	HideEntity OBJTunnel(6)
	
	DrawLoading(37)

	;TextureLodBias TextureBias
	TextureLodBias TextureFloat#
	;Devil Particle System
	;ParticleEffect[] numbers:
	;	0 - electric spark
	;	1 - smoke effect
	
	Local t0

	InitParticles(Camera)
	
	;Spark Effect (short)
	ParticleEffect[0] = CreateTemplate()
	SetTemplateEmitterBlend(ParticleEffect[0], 3)
	SetTemplateInterval(ParticleEffect[0], 1)
	SetTemplateParticlesPerInterval(ParticleEffect[0], 6)
	SetTemplateEmitterLifeTime(ParticleEffect[0], 6)
	SetTemplateParticleLifeTime(ParticleEffect[0], 20, 30)
	SetTemplateTexture(ParticleEffect[0], "GFX\Spark.png", 2, 3)
	SetTemplateOffset(ParticleEffect[0], -0.1, 0.1, -0.1, 0.1, -0.1, 0.1)
	SetTemplateVelocity(ParticleEffect[0], -0.0375, 0.0375, -0.0375, 0.0375, -0.0375, 0.0375)
	SetTemplateAlignToFall(ParticleEffect[0], True, 45)
	SetTemplateGravity(ParticleEffect[0], 0.001)
	SetTemplateAlphaVel(ParticleEffect[0], True)
	;SetTemplateSize(ParticleEffect[0], 0.0625, 0.125, 0.7, 1)
	SetTemplateSize(ParticleEffect[0], 0.03125, 0.0625, 0.7, 1)
	SetTemplateColors(ParticleEffect[0], $0000FF, $6565FF)
	SetTemplateFloor(ParticleEffect[0], 0.0, 0.5)
	
	;Smoke effect (for some vents)
	ParticleEffect[1] = CreateTemplate()
	SetTemplateEmitterBlend(ParticleEffect[1], 1)
	SetTemplateInterval(ParticleEffect[1], 1)
	SetTemplateEmitterLifeTime(ParticleEffect[1], 3)
	SetTemplateParticleLifeTime(ParticleEffect[1], 30, 45)
	SetTemplateTexture(ParticleEffect[1], "GFX\smoke2.png", 2, 1)
	;SetTemplateOffset(ParticleEffect[1], -.3, .3, -.3, .3, -.3, .3)
	SetTemplateOffset(ParticleEffect[1], 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
	;SetTemplateVelocity(ParticleEffect[1], -.04, .04, .1, .2, -.04, .04)
	SetTemplateVelocity(ParticleEffect[1], 0.0, 0.0, 0.02, 0.025, 0.0, 0.0)
	SetTemplateAlphaVel(ParticleEffect[1], True)
	;SetTemplateSize(ParticleEffect[1], 3, 3, .5, 1.5)
	SetTemplateSize(ParticleEffect[1], 0.4, 0.4, 0.5, 1.5)
	SetTemplateSizeVel(ParticleEffect[1], .01, 1.01)
	
	;Smoke effect (for decontamination gas)
	ParticleEffect[2] = CreateTemplate()
	SetTemplateEmitterBlend(ParticleEffect[2], 1)
	SetTemplateInterval(ParticleEffect[2], 1)
	SetTemplateEmitterLifeTime(ParticleEffect[2], 3)
	SetTemplateParticleLifeTime(ParticleEffect[2], 30, 45)
	SetTemplateTexture(ParticleEffect[2], "GFX\smoke.png", 2, 1)
	SetTemplateOffset(ParticleEffect[2], -0.1, 0.1, -0.1, 0.1, -0.1, 0.1)
	SetTemplateVelocity(ParticleEffect[2], -0.005, 0.005, 0.0, -0.03, -0.005, 0.005)
	SetTemplateAlphaVel(ParticleEffect[2], True)
	SetTemplateSize(ParticleEffect[2], 0.4, 0.4, 0.5, 1.5)
	SetTemplateSizeVel(ParticleEffect[2], .01, 1.01)
	SetTemplateGravity(ParticleEffect[2], 0.005)
	t0 = CreateTemplate()
	SetTemplateEmitterBlend(t0, 1)
	SetTemplateInterval(t0, 1)
	SetTemplateEmitterLifeTime(t0, 3)
	SetTemplateParticleLifeTime(t0, 30, 45)
	SetTemplateTexture(t0, "GFX\smoke2.png", 2, 1)
	SetTemplateOffset(t0, -0.1, 0.1, -0.1, 0.1, -0.1, 0.1)
	SetTemplateVelocity(t0, -0.005, 0.005, 0.0, -0.03, -0.005, 0.005)
	SetTemplateAlphaVel(t0, True)
	SetTemplateSize(t0, 0.4, 0.4, 0.5, 1.5)
	SetTemplateSizeVel(t0, .01, 1.01)
	SetTemplateGravity(ParticleEffect[2], 0.005)
	SetTemplateSubTemplate(ParticleEffect[2], t0)

	;Big doors closing
	ParticleEffect[3] = CreateTemplate()
	SetTemplateEmitterBlend(ParticleEffect[3], 3)
	SetTemplateEmitterLifeTime(ParticleEffect[3], 0)
	SetTemplateParticlesPerInterval(ParticleEffect[3], 80)
	SetTemplateParticleLifeTime(ParticleEffect[3], 90, 100)
	SetTemplateTexture(ParticleEffect[3], "GFX\dust.jpg", 2, 1)
	SetTemplateOffset(ParticleEffect[3], -0.2, 0.2, 0.0, 1.2, -0.2, 0.2)
	SetTemplateVelocity(ParticleEffect[3], -0.005, 0.005, -0.0017, 0.0017, -0.005, 0.005)
	SetTemplateSizeVel(ParticleEffect[3], -0.000005, 1)
	SetTemplateSize(ParticleEffect[3], 0.005, 0.005)
	SetTemplateAlphaVel(ParticleEffect[3], True)
	
	Room2slCam = CreateCamera()
	CameraViewport(Room2slCam, 0, 0, 128, 128)
	CameraRange Room2slCam, 0.05, 6.0
	CameraZoom(Room2slCam, 0.8)
	HideEntity(Room2slCam)
	
	DrawLoading(40)
	
	CatchErrors("LoadEntities")
End Function

Function InitNewGame()
	CatchErrors("Uncaught (InitNewGame)")
	Local i%, de.Decals, d.Doors, it.Items, r.Rooms, sc.SecurityCams, e.Events

	DrawLoading(45)
	
	PlayTime = 0
	TimerStopped = False

	HideDistance# = 15.0
	
	HeartBeatRate = 70
	
	AccessCode = 0
	For i = 0 To 3
		AccessCode = AccessCode + (Rand(1,9)*(10^i))
	Next
	If AccessCode = HARPCODE Then AccessCode = AccessCode + 1
	
	If SelectedMap = "" Then
		CreateMap(50, 19)
	Else
		LoadMap("Map Creator\Maps\"+SelectedMap, 50, 19)
	EndIf
	DrawLoading(70)
	InitWayPoints(71, 9)
	
	DrawLoading(79)
	
	Curr173 = CreateNPC(NPCtype173, 0, -30.0, 0)
	Curr106 = CreateNPC(NPCtypeOldMan, 0, -30.0, 0)
	Curr106\State = 70 * 60 * Rand(12,17)
	
	For d.Doors = Each Doors
		EntityParent(d\obj, 0)
		If d\obj2 <> 0 Then EntityParent(d\obj2, 0)
		If d\frameobj <> 0 Then EntityParent(d\frameobj, 0)
		If d\buttons[0] <> 0 Then EntityParent(d\buttons[0], 0)
		If d\buttons[1] <> 0 Then EntityParent(d\buttons[1], 0)
		
		If d\obj2 <> 0 And d\dir = 0 Then
			MoveEntity(d\obj, 0, 0, 8.0 * RoomScale)
			MoveEntity(d\obj2, 0, 0, 8.0 * RoomScale)
		EndIf	
	Next
	
	For it.Items = Each Items
		EntityType (it\collider, HIT_ITEM)
		EntityParent(it\collider, 0)
	Next
	
	DrawLoading(81)
	For sc.SecurityCams= Each SecurityCams
		sc\angle = EntityYaw(sc\obj) + sc\angle
		EntityParent(sc\obj, 0)
	Next	
	
	For r.Rooms = Each Rooms
		For i = 0 To MaxRoomLights-1
			If r\Lights[i]<>0 Then
				EntityParent(r\Lights[i],0)
			Else
				Exit
			EndIf
		Next
		
		If (Not r\RoomTemplate\DisableDecals) Then
			If Rand(4) = 1 Then
				de.Decals = CreateDecal(Rand(2, 3), EntityX(r\obj)+Rnd(- 2,2), 0.003, EntityZ(r\obj)+Rnd(-2,2), 90, Rand(360), 0)
				de\Size = Rnd(0.1, 0.4) : ScaleSprite(de\obj, de\Size, de\Size)
				EntityAlpha(de\obj, Rnd(0.85, 0.95))
				SnapForward(de\obj, 5)
				TranslateEntity(de\obj, 0, 0.003, 0)
			EndIf
			
			If Rand(4) = 1 Then
				de.Decals = CreateDecal(0, EntityX(r\obj)+Rnd(- 2,2), 0.003, EntityZ(r\obj)+Rnd(-2,2), 90, Rand(360), 0)
				de\Size = Rnd(0.5, 0.7) : EntityAlpha(de\obj, 0.7) : de\ID = 1 : ScaleSprite(de\obj, de\Size, de\Size)
				EntityAlpha(de\obj, Rnd(0.7, 0.85))
				SnapForward(de\obj, 5)
				TranslateEntity(de\obj, 0, 0.003, 0)
			EndIf
		EndIf
		
		If (r\RoomTemplate\Name = "start" And IntroEnabled = False) Then 
			PositionEntity (Collider, EntityX(r\obj)+3584*RoomScale, 704*RoomScale, EntityZ(r\obj)+1024*RoomScale)
			PlayerRoom = r
			it = CreateItem("docORI", 1, 1, 1)
			it\Picked = True
			it\Dropped = -1
			it\itemtemplate\found=True
			Inventory(0) = it
			HideEntity(it\collider)
			EntityType (it\collider, HIT_ITEM)
			EntityParent(it\collider, 0)
			ItemAmount = ItemAmount + 1
			it = CreateItem("doc173", 1, 1, 1)
			it\Picked = True
			it\Dropped = -1
			it\itemtemplate\found=True
			Inventory(1) = it
			HideEntity(it\collider)
			EntityType (it\collider, HIT_ITEM)
			EntityParent(it\collider, 0)
			ItemAmount = ItemAmount + 1
		ElseIf (r\RoomTemplate\Name = "173" And IntroEnabled) Then
			PositionEntity (Collider, EntityX(r\obj), 1.0, EntityZ(r\obj))
			PlayerRoom = r
		EndIf
		
	Next
	
	ResetAllRMeshes()
	
	TurnEntity(Collider, 0, Rand(160, 200), 0)
	
	ResetEntity Collider
	
	If SelectedMap = "" Then InitEvents()
	
	For e.Events = Each Events
		If e\EventName = "room2nuke"
			e\EventState = 1
			DebugLog "room2nuke"
		EndIf
		If e\EventName = "room106"
			e\EventState2 = 1
			DebugLog "room106"
		EndIf	
		If e\EventName = "room2sl"
			e\EventState3 = 1
			DebugLog "room2sl"
		EndIf
	Next
		
	SetFont Font1
	
	HidePointer()
	
	BlinkTimer = -10
	BlurTimer = 100
	Stamina = 100
	
	Playable = False
	For i% = 0 To 70
		FPSfactor = 1.0
		FlushKeys()
		MovePlayer()
		UpdateDoors()
		UpdateNPCs()
		UpdateWorld()
		;Cls
		If (Int(Float(i)*0.27)<>Int(Float(i-1)*0.27)) Then
			DrawLoading(80+Int(Float(i)*0.27))
		EndIf
	Next
	Playable = True
	
	FreeTextureCache
	DrawLoading(100)

	MoveMouse viewport_center_x,viewport_center_y

	FlushKeys
	FlushMouse
	
	DropSpeed = 0
	
	PrevTime = MilliSecs()
	CatchErrors("InitNewGame")
End Function

Function InitLoadGame()
	CatchErrors("Uncaught (InitLoadGame)")
	Local d.Doors, sc.SecurityCams, rt.RoomTemplates, e.Events

	DrawLoading(80)
	
	For d.Doors = Each Doors
		EntityParent(d\obj, 0)
		If d\obj2 <> 0 Then EntityParent(d\obj2, 0)
		If d\frameobj <> 0 Then EntityParent(d\frameobj, 0)
		If d\buttons[0] <> 0 Then EntityParent(d\buttons[0], 0)
		If d\buttons[1] <> 0 Then EntityParent(d\buttons[1], 0)
		
	Next
	
	For sc.SecurityCams = Each SecurityCams
		sc\angle = EntityYaw(sc\obj) + sc\angle
		EntityParent(sc\obj, 0)
	Next
	
	ResetEntity Collider
	
	;InitEvents()
	
	DrawLoading(90)
		
	SetFont Font1
	
	HidePointer ()
	
	BlinkTimer = BLINKFREQ
	Stamina = 100
	
	ResetAllRMeshes()
	
	DropSpeed = 0.0
	
	For e.Events = Each Events
		;Loading the necessary stuff for dimension1499, but this will only be done if the player is in this dimension already
		If e\EventName = "dimension1499"
			If e\EventState = 2
				;[Block]
				DrawLoading(91)
				e\room\Objects[0] = LoadMesh_Strict("GFX\map\dimension1499\1499plane.b3d")
				HideEntity(e\room\Objects[0])
				DrawLoading(92)
				NTF_1499Sky = sky_CreateSky("GFX\map\sky\1499sky")
				DrawLoading(93)
				For i = 1 To 15
					e\room\Objects[i] = LoadMesh_Strict("GFX\map\dimension1499\1499object"+i+".b3d")
					HideEntity e\room\Objects[i]
				Next
				DrawLoading(96)
				CreateChunkParts(e\room)
				DrawLoading(97)
				x# = EntityX(e\room\obj)
				z# = EntityZ(e\room\obj)
				Local ch.Chunk
				For i = -2 To 2 Step 2
					ch = CreateChunk(-1,x#*(i*2.5),EntityY(e\room\obj),z#,True)
					ch = CreateChunk(-1,x#*(i*2.5),EntityY(e\room\obj),z#-40,True)
				Next
				DrawLoading(98)
				UpdateChunks(e\room,15,False)
				;MoveEntity Collider,0,10,0
				;ResetEntity Collider
				
				DebugLog "Loaded dimension1499 successful"
				
				Exit
				;[End Block]
			EndIf
		EndIf
	Next
	
	FreeTextureCache
	DrawLoading(100)

	MoveMouse viewport_center_x,viewport_center_y
	
	PrevTime = MilliSecs()
	FPSfactor = 0
	ResetInput()
	
	CatchErrors("InitLoadGame")
End Function

Function NullGame(playbuttonsfx%=True)
	CatchErrors("Uncaught (NullGame)")
	Local i%, x%, y%, lvl
	Local itt.ItemTemplates, s.Screens, lt.LightTemplates, d.Doors, m.Materials
	Local wp.WayPoints, twp.TempWayPoints, r.Rooms, it.Items
	
	KillSounds()
	If playbuttonsfx Then PlaySound_Strict ButtonSFX
	
	FreeParticles()
	
	ClearTextureCache
	
	If Not SpeedRunMode Then PlayTime = 0

	DebugHUD = False
	
	UnableToMove% = False
	
	QuickLoadPercent = -1
	QuickLoadPercent_DisplayTimer# = 0
	QuickLoad_CurrEvent = Null
	
	DeathMSG$=""
	
	SelectedMap = ""
	
	UsedConsole = False
	
	DoorTempID = 0
	RoomTempID = 0
	
	GameSaved = 0
	
	HideDistance# = 15.0
	
	For lvl = 0 To 0
		For x = 0 To MapWidth+1
			For y = 0 To MapHeight+1
				MapTemp(x, y) = 0
				MapFound(x, y) = 0
			Next
		Next
	Next
	
	For itt.ItemTemplates = Each ItemTemplates
		itt\found = False
	Next
	
	DropSpeed = 0
	Shake = 0
	CurrSpeed = 0
	
	DeathTimer=0
	
	HeartBeatVolume = 0
	
	StaminaEffect = 1.0
	StaminaEffectTimer = 0
	BlinkEffect = 1.0
	BlinkEffectTimer = 0
	
	Bloodloss = 0
	Injuries = 0
	Infect = 0
	
	For i = 0 To 5
		SCP1025state[i] = 0
	Next
	
	SelectedEnding = ""
	EndingTimer = 0
	ExplosionTimer = 0
	
	CameraShake = 0
	Shake = 0
	LightFlash = 0
	
	GodMode = 0
	NoClip = 0
	WireframeState = 0
	WireFrame 0
	WearingGasMask = 0
	WearingHazmat = 0
	WearingVest = 0
	Wearing714 = 0
	If WearingNightVision Then
		CameraFogFar = StoredCameraFogFar
		WearingNightVision = 0
	EndIf
	I_427\Using = 0
	I_427\Timer = 0.0
	
	ForceMove = 0.0
	ForceAngle = 0.0	
	Playable = True
	
	CoffinDistance = 100
	
	Contained106 = False
	If Curr173 <> Null Then Curr173\Idle = False
	
	MTFtimer = 0
	For i = 0 To 9
		MTFrooms[i]=Null
		MTFroomState[i]=0
	Next
	
	For s.Screens = Each Screens
		If s\img <> 0 Then FreeImage s\img : s\img = 0
		Delete s
	Next
	
	For i = 0 To MAXACHIEVEMENTS-1
		Achievements(i)=0
	Next
	RefinedItems = 0
	
	ConsoleInput = ""
	ConsoleOpen = False
	
	EyeIrritation = 0
	EyeStuck = 0
	
	ShouldPlay = 0
	
	KillTimer = 0
	FallTimer = 0
	Stamina = 100
	BlurTimer = 0
	SuperMan = False
	SuperManTimer = 0
	Sanity = 0
	RestoreSanity = True
	Crouch = False
	CrouchState = 0.0
	LightVolume = 0.0
	Vomit = False
	VomitTimer = 0.0
	SecondaryLightOn# = True
	PrevSecondaryLightOn# = True
	RemoteDoorOn = True
	SoundTransmission = False
	
	InfiniteStamina% = False
	
	Msg = ""
	MsgTimer = 0
	
	SelectedItem = Null
	
	For i = 0 To MaxItemAmount - 1
		Inventory(i) = Null
	Next
	SelectedItem = Null
	
	ClosestButton = 0
	
	room2gw_brokendoor = False
	room2gw_x = 0.0
	room2gw_z = 0.0

	Delete Each Doors
	
	;ClearWorld
	
	Delete Each LightTemplates
	Delete Each Materials
	Delete Each WayPoints
	Delete Each Rooms	
	Delete Each Inventories
	Delete Each Items
	Delete Each ItemTemplates
	Delete Each Props
	Delete Each Decals
	Delete Each NPCs

	Curr173 = Null
	Curr106 = Null
	Curr096 = Null
	For i = 0 To 6
		MTFrooms[i]=Null
	Next
	ForestNPC = 0
	ForestNPCTex = 0
	
	Local e.Events
	For e.Events = Each Events
		If e\Sound<>0 Then FreeSound_Strict e\Sound
		If e\Sound2<>0 Then FreeSound_Strict e\Sound2
		Delete e
	Next
	
	For sc.securitycams = Each SecurityCams
		Delete sc
	Next
	
	For em.emitters = Each Emitters
		Delete em
	Next	
	
	For p.particles = Each Particles
		Delete p
	Next
	
	ResetAllRMeshes()
	
	For i = 0 To 5
		If ChannelPlaying(RadioCHN(i)) Then StopChannel(RadioCHN(i))
	Next
	
	NTF_1499PrevX# = 0.0
	NTF_1499PrevY# = 0.0
	NTF_1499PrevZ# = 0.0
	NTF_1499PrevRoom = Null
	NTF_1499X# = 0.0
	NTF_1499Y# = 0.0
	NTF_1499Z# = 0.0
	Wearing1499% = False
	DeleteChunks()
	
	DeleteElevatorObjects()
	
	DeleteDevilEmitters()
	
	NoTarget% = False
	
	OptionsMenu% = -1
	QuitMSG% = -1
	AchievementsMenu% = -1
	
	SFXVolume# = PrevSFXVolume
	DeafPlayer% = False
	DeafTimer# = 0.0
	
	GuaranteedOmni% = False

	IsZombie% = False
	
	Delete Each AchievementMsg
	CurrAchvMSGID = 0
	
	SetErrorMsg(7, "")

	;DeInitExt
	
	CatchErrors("Clear World")
	ClearWorld
	CatchErrors("Uncaught (Clear World)")
	
	Camera = 0
	ark_blur_cam = 0
	Collider = 0
	Sky = 0
	InitFastResize()
	
	CatchErrors("NullGame")
End Function

Include "save.bb"

;--------------------------------------- music & sounds ----------------------------------------------

Function PlaySound2%(SoundHandle%, cam%, entity%, range# = 10, volume# = 1.0)
	range# = Max(range, 1.0)
	Local soundchn% = 0
	
	If volume > 0 Then 
		Local dist# = EntityDistance(cam, entity) / range#
		If 1 - dist# > 0 And 1 - dist# < 1
			Local panvalue# = Sin(-DeltaYaw(cam,entity))
			soundchn% = PlaySound_Strict (SoundHandle)
			
			ChannelVolume(soundchn, volume# * (1 - dist#)*SFXVolume#)
			ChannelPan(soundchn, panvalue)			
		EndIf
	EndIf
	
	Return soundchn
End Function

Function LoopSound2%(SoundHandle%, Chn%, cam%, entity%, range# = 10, volume# = 1.0)
	range# = Max(range,1.0)
	
	If volume>0 Then
		
		Local dist# = EntityDistance(cam, entity) / range#
		;If 1 - dist# > 0 And 1 - dist# < 1 Then
			
			Local panvalue# = Sin(-DeltaYaw(cam,entity))
			
			If Chn = 0 Then
				Chn% = PlaySound_Strict (SoundHandle)
			Else
				If (Not ChannelPlaying(Chn)) Then Chn% = PlaySound_Strict (SoundHandle)
			EndIf
			
			ChannelVolume(Chn, volume# * (1 - dist#)*SFXVolume#)
			ChannelPan(Chn, panvalue)
		;EndIf
	Else
		If Chn <> 0 Then
			ChannelVolume (Chn, 0)
		EndIf 
	EndIf
	
	Return Chn
End Function

Function LoadTempSound(file$)
	If TempSounds[TempSoundIndex]<>0 Then FreeSound_Strict(TempSounds[TempSoundIndex])
	TempSound = LoadSound_Strict(file)
	TempSounds[TempSoundIndex] = TempSound
	
	TempSoundIndex=(TempSoundIndex+1) Mod 10
	
	Return TempSound
End Function

Function LoadEventSound(e.Events,file$,num%=0)
	
	If num=0 Then
		If e\Sound<>0 Then FreeSound_Strict e\Sound : e\Sound=0
		e\Sound=LoadSound_Strict(file)
		Return e\Sound
	Else If num=1 Then
		If e\Sound2<>0 Then FreeSound_Strict e\Sound2 : e\Sound2=0
		e\Sound2=LoadSound_Strict(file)
		Return e\Sound2
	EndIf
End Function

Function UpdateMusic()
	
	If ConsoleFlush Then
		If Not ChannelPlaying(ConsoleMusPlay) Then ConsoleMusPlay = PlaySound(ConsoleMusFlush)
	ElseIf (Not PlayCustomMusic)
		If NowPlaying <> ShouldPlay ; playing the wrong clip, fade out
			CurrMusicVolume# = Max(CurrMusicVolume - (FPSfactor / 250.0), 0)
			If CurrMusicVolume = 0
				If NowPlaying<66
					StopStream_Strict(MusicCHN)
				EndIf
				NowPlaying = ShouldPlay
				MusicCHN = 0
				CurrMusic=0
			EndIf
		Else ; playing the right clip
			CurrMusicVolume = CurrMusicVolume + (MusicVolume - CurrMusicVolume) * (0.1*FPSfactor)
		EndIf
		
		If NowPlaying < 66
			If CurrMusic = 0
				MusicCHN = StreamSound_Strict("SFX\Music\"+Music(NowPlaying)+".ogg",0.0)
				CurrMusic = 1
			EndIf
			SetStreamVolume_Strict(MusicCHN,CurrMusicVolume)
		EndIf
	Else
		If FPSfactor > 0 Or OptionsMenu = 2 Then
			;CurrMusicVolume = 1.0
			If (Not ChannelPlaying(MusicCHN)) Then MusicCHN = PlaySound_Strict(CustomMusic)
			ChannelVolume MusicCHN,1.0*MusicVolume
		EndIf
	EndIf
	
End Function 

Function PauseSounds()
	For e.events = Each Events
		If e\soundchn <> 0 Then
			If (Not e\soundchn_isstream)
				PauseChannel(e\soundchn)
			Else
				SetStreamPaused_Strict(e\soundchn,True)
			EndIf
		EndIf
		If e\soundchn2 <> 0 Then
			If (Not e\soundchn2_isstream)
				PauseChannel(e\soundchn2)
			Else
				SetStreamPaused_Strict(e\soundchn2,True)
			EndIf
		EndIf		
	Next
	
	For n.npcs = Each NPCs
		If n\soundchn <> 0 Then
			If (Not n\soundchn_isstream)
				PauseChannel(n\soundchn)
			Else
				SetStreamPaused_Strict(n\soundchn,True)
			EndIf
		EndIf
		If n\soundchn2 <> 0 Then
			If (Not n\soundchn2_isstream)
				PauseChannel(n\soundchn2)
			Else
				SetStreamPaused_Strict(n\soundchn2,True)
			EndIf
		EndIf
	Next	
	
	For d.doors = Each Doors
		If d\soundchn <> 0 Then
			PauseChannel(d\soundchn)
		EndIf
	Next
	
	For dem.DevilEmitters = Each DevilEmitters
		If dem\soundchn <> 0 Then
			PauseChannel(dem\soundchn)
		EndIf
	Next
	
	If AmbientSFXCHN <> 0 Then
		PauseChannel(AmbientSFXCHN)
	EndIf
	
	If BreathCHN <> 0 Then
		PauseChannel(BreathCHN)
	EndIf
	
	If CoughCHN <> 0 Then
		PauseChannel(CoughCHN)
	EndIf
	
	If VomitCHN <> 0 Then
		PauseChannel(VomitCHN)
	EndIf
	
	If IntercomStreamCHN <> 0
		SetStreamPaused_Strict(IntercomStreamCHN,True)
	EndIf
End Function

Function ResumeSounds()
	For e.events = Each Events
		If e\soundchn <> 0 Then
			If (Not e\soundchn_isstream)
				ResumeChannel(e\soundchn)
			Else
				SetStreamPaused_Strict(e\soundchn,False)
			EndIf
		EndIf
		If e\soundchn2 <> 0 Then
			If (Not e\soundchn2_isstream)
				ResumeChannel(e\soundchn2)
			Else
				SetStreamPaused_Strict(e\soundchn2,False)
			EndIf
		EndIf	
	Next
	
	For n.npcs = Each NPCs
		If n\soundchn <> 0 Then
			If (Not n\soundchn_isstream)
				ResumeChannel(n\soundchn)
			Else
				SetStreamPaused_Strict(n\soundchn,False)
			EndIf
		EndIf
		If n\soundchn2 <> 0 Then
			If (Not n\soundchn2_isstream)
				ResumeChannel(n\soundchn2)
			Else
				SetStreamPaused_Strict(n\soundchn2,False)
			EndIf
		EndIf
	Next	
	
	For d.doors = Each Doors
		If d\soundchn <> 0 Then
			ResumeChannel(d\soundchn)
		EndIf
	Next
	
	For dem.DevilEmitters = Each DevilEmitters
		If dem\soundchn <> 0 Then
			ResumeChannel(dem\soundchn)
		EndIf
	Next
	
	If AmbientSFXCHN <> 0 Then
		ResumeChannel(AmbientSFXCHN)
	EndIf	
	
	If BreathCHN <> 0 Then
		ResumeChannel(BreathCHN)
	EndIf
	
	If CoughCHN <> 0 Then
		ResumeChannel(CoughCHN)
	EndIf
	
	If VomitCHN <> 0 Then
		ResumeChannel(VomitCHN)
	EndIf
	
	If IntercomStreamCHN <> 0
		SetStreamPaused_Strict(IntercomStreamCHN,False)
	EndIf
End Function

Function KillSounds()
	Local i%,e.Events,n.NPCs,d.Doors,dem.DevilEmitters,snd.Sound
	
	For i=0 To 9
		If TempSounds[i]<>0 Then FreeSound_Strict TempSounds[i] : TempSounds[i]=0
	Next
	For e.Events = Each Events
		If e\SoundCHN <> 0 Then
			If (Not e\SoundCHN_isStream)
				StopChannel(e\SoundCHN)
			Else
				StopStream_Strict(e\SoundCHN) : e\SoundCHN_isStream = False
			EndIf
			e\SoundCHN = 0
		EndIf
		If e\SoundCHN2 <> 0 Then
			If (Not e\SoundCHN2_isStream)
				StopChannel(e\SoundCHN2)
			Else
				StopStream_Strict(e\SoundCHN2) : e\SoundCHN2_isStream = False
			EndIf
			e\SoundCHN2 = 0
		EndIf		
	Next
	For n.NPCs = Each NPCs
		If n\SoundChn <> 0 Then
			If (Not n\SoundChn_IsStream)
				StopChannel(n\SoundChn)
			Else
				StopStream_Strict(n\SoundChn) : n\SoundChn_isStream = False
			EndIf
			n\SoundChn = 0
		EndIf
		If n\SoundChn2 <> 0 Then
			If (Not n\SoundChn2_IsStream)
				StopChannel(n\SoundChn2)
			Else
				StopStream_Strict(n\SoundChn2) : n\SoundChn2_isStream = False
			EndIf
			n\SoundChn2 = 0
		EndIf
	Next	
	For d.Doors = Each Doors
		If d\SoundCHN <> 0 Then
			StopChannel(d\SoundCHN) : d\SoundCHN = 0
		EndIf
	Next
	For dem.DevilEmitters = Each DevilEmitters
		If dem\SoundCHN <> 0 Then
			StopChannel(dem\SoundCHN) : dem\SoundCHN = 0
		EndIf
	Next
	If AmbientSFXCHN <> 0 Then
		StopChannel(AmbientSFXCHN) : AmbientSFXCHN = 0
	EndIf
	If BreathCHN <> 0 Then
		StopChannel(BreathCHN) : BreathCHN = 0
	EndIf
	If CoughCHN <> 0 Then
		StopChannel(CoughCHN) : CoughCHN = 0
	EndIf
	If VomitCHN <> 0 Then
		StopChannel(VomitCHN) : VomitCHN = 0
	EndIf
	For i = 0 To 6
		If RadioCHN(i) <> 0 Then
			StopChannel(RadioCHN(i)) : RadioCHN(i) = 0
		EndIf
	Next
	If IntercomStreamCHN <> 0
		StopStream_Strict(IntercomStreamCHN) : IntercomStreamCHN = 0
	EndIf
	If EnableSFXRelease
		For snd.Sound = Each Sound
			If snd\internalHandle <> 0 Then
				FreeSound snd\internalHandle
				snd\internalHandle = 0
				snd\releaseTime = 0
			EndIf
		Next
	EndIf
	
	For snd.Sound = Each Sound
		For i = 0 To 31
			If snd\channels[i]<>0 Then
				StopChannel snd\channels[i] : snd\channels[i] = 0
			EndIf
		Next
	Next
	
	DebugLog "Terminated all sounds"
	
End Function

Function GetStepSound(entity%)
    Local picker%,brush%,texture%,name$
    Local mat.Materials
    
    picker = LinePick(EntityX(entity),EntityY(entity),EntityZ(entity),0,-1,0)
    If picker <> 0 Then
        If GetEntityType(picker) <> HIT_MAP Then Return 0
        brush = GetSurfaceBrush(GetSurface(picker,CountSurfaces(picker)))
        If brush <> 0 Then
            texture = GetBrushTexture(brush,3)
            If texture <> 0 Then
                name = StripPath(TextureName(texture))
                If (name <> "") Then FreeTexture(texture)
				For mat.Materials = Each Materials
					If mat\name = name Then
						If mat\StepSound > 0 Then
							FreeBrush(brush)
							Return mat\StepSound-1
						EndIf
						Exit
					EndIf
				Next                
			EndIf
			texture = GetBrushTexture(brush,2)
			If texture <> 0 Then
				name = StripPath(TextureName(texture))
				If (name <> "") Then FreeTexture(texture)
				For mat.Materials = Each Materials
					If mat\name = name Then
						If mat\StepSound > 0 Then
							FreeBrush(brush)
							Return mat\StepSound-1
						EndIf
						Exit
					EndIf
				Next                
			EndIf
			texture = GetBrushTexture(brush,1)
			If texture <> 0 Then
				name = StripPath(TextureName(texture))
				If (name <> "") Then FreeTexture(texture)
				FreeBrush(brush)
				For mat.Materials = Each Materials
					If mat\name = name Then
						If mat\StepSound > 0 Then
							Return mat\StepSound-1
						EndIf
						Exit
					EndIf
				Next                
			EndIf
		EndIf
	EndIf
    
    Return 0
End Function

Function UpdateSoundOrigin2(Chn%, cam%, entity%, range# = 10, volume# = 1.0)
	If Chn <> 0 Then
		If ChannelPlaying(Chn) Then
			range# = Max(range,1.0)
			
			If volume>0 Then
				
				Local dist# = EntityDistance(cam, entity) / range#
				If 1 - dist# > 0 And 1 - dist# < 1 Then
					
					Local panvalue# = Sin(-DeltaYaw(cam,entity))
					
					ChannelVolume(Chn, volume# * (1 - dist#))
					ChannelPan(Chn, panvalue)
				Else
					ChannelVolume (Chn, 0)
				EndIf
			Else
				ChannelVolume (Chn, 0)
			EndIf
		EndIf
	EndIf
End Function

Function UpdateSoundOrigin(Chn%, cam%, entity%, range# = 10, volume# = 1.0)
	If Chn <> 0 Then
		If ChannelPlaying(Chn) Then
			range# = Max(range,1.0)
			
			If volume>0 Then
				
				Local dist# = EntityDistance(cam, entity) / range#
				If 1 - dist# > 0 And 1 - dist# < 1 Then
					
					Local panvalue# = Sin(-DeltaYaw(cam,entity))
					
					ChannelVolume(Chn, volume# * (1 - dist#)*SFXVolume#)
					ChannelPan(Chn, panvalue)
				Else
					ChannelVolume (Chn, 0)
				EndIf
			Else
				ChannelVolume (Chn, 0)
			EndIf
		EndIf
	EndIf
End Function
;--------------------------------------- random -------------------------------------------------------

Function f2s$(n#, count%)
	Return Left(n, Len(Int(Str(n)))+count+1)
End Function

Function AnimateNPC(n.NPCs, start#, quit#, speed#, loop=True)
	Local newTime#
	
	If speed > 0.0 Then 
		newTime = Max(Min(n\Frame + speed * FPSfactor,quit),start)
		
		If loop And newTime => quit Then
			newTime = start
		EndIf
	Else
		If start < quit Then
			temp% = start
			start = quit
			quit = temp
		EndIf
		
		If loop Then
			newTime = n\Frame + speed * FPSfactor
			
			If newTime < quit Then 
				newTime = start
			Else If newTime > start 
				newTime = quit
			EndIf
		Else
			newTime = Max(Min(n\Frame + speed * FPSfactor,start),quit)
		EndIf
	EndIf
	SetNPCFrame(n, newTime)
	
End Function

Function SetNPCFrame(n.NPCs, frame#)
	If (Abs(n\Frame-frame)<0.001) Then Return
	
	SetAnimTime n\obj, frame
	
	n\Frame = frame
End Function

Function Animate2#(entity%, curr#, start%, quit%, speed#, loop=True)
	
	Local newTime#
	
	If speed > 0.0 Then 
		newTime = Max(Min(curr + speed * FPSfactor,quit),start)
		
		If loop Then
			If newTime => quit Then 
				;SetAnimTime entity, start
				newTime = start
			Else
				;SetAnimTime entity, newTime
			EndIf
		Else
			;SetAnimTime entity, newTime
		EndIf
	Else
		If start < quit Then
			temp% = start
			start = quit
			quit = temp
		EndIf
		
		If loop Then
			newTime = curr + speed * FPSfactor
			
			If newTime < quit Then newTime = start
			If newTime > start Then newTime = quit
			
			;SetAnimTime entity, newTime
		Else
			;SetAnimTime (entity, Max(Min(curr + speed * FPSfactor,start),quit))
			newTime = Max(Min(curr + speed * FPSfactor,start),quit)
		EndIf
	EndIf
	
	SetAnimTime entity, newTime
	Return newTime
	
End Function 


Function Use914(item.Items, setting$, x#, y#, z#)
	
	RefinedItems = RefinedItems+1
	
	Local it2.Items
	Select item\itemtemplate\name
		Case "gasmask", "supergasmask", "gasmask3"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					RemoveItem(item)
				Case "1:1"
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
				Case "fine", "very fine"
					it2 = CreateItem("supergasmask", x, y, z)
					RemoveItem(item)
			End Select
		Case "scp1499", "super1499"
				Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					RemoveItem(item)
				Case "1:1"
					it2 = CreateItem("gasmask", x, y, z)
					RemoveItem(item)
				Case "fine"
					it2 = CreateItem("super1499", x, y, z)
					RemoveItem(item)
				Case "very fine"
					n.NPCs = CreateNPC(NPCtype1499,x,y,z)
					n\State = 1
					n\Sound = LoadSound_Strict("SFX\SCP\1499\Triggered.ogg")
					n\SoundChn = PlaySound2(n\Sound, Camera, n\Collider,20.0)
					n\State3 = 1
					RemoveItem(item)
			End Select
		Case "vest", "finevest", "veryfinevest"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					RemoveItem(item)
				Case "1:1"
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
				Case "fine"
					it2 = CreateItem("finevest", x, y, z)
					RemoveItem(item)
				Case "very fine"
					it2 = CreateItem("veryfinevest", x, y, z)
					RemoveItem(item)
			End Select
		Case "clipboard"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					For i% = 0 To 19
						If item\Inventory\Items[i]<>Null Then RemoveItem(item\Inventory\Items[i])
						item\Inventory\Items[i]=Null
					Next
					RemoveItem(item)
				Case "1:1"
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
				Case "fine"
					item\Inventory\Size = Max(item\state2,15)
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
				Case "very fine"
					item\Inventory\Size = Max(item\state2,20)
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
			End Select
		Case "supernv", "supernv", "finenvgoggles"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					RemoveItem(item)
				Case "1:1"
					PositionEntity(item\collider, x, y, z)
					ResetEntity(item\collider)
				Case "fine"
					it2 = CreateItem("finenvgoggles", x, y, z)
					RemoveItem(item)
				Case "very fine"
					it2 = CreateItem("supernv", x, y, z)
					it2\state = 1000
					RemoveItem(item)
			End Select
		Case "scp148", "scp148ingot"
			Select setting
				Case "rough", "coarse"
					it2 = CreateItem("scp148ingot", x, y, z)
					RemoveItem(item)
				Case "1:1", "fine", "very fine"
					it2 = Null
					For it.Items = Each Items
						If it<>item And it\collider <> 0 And it\Picked = False Then
							If Distance(EntityX(it\collider,True), EntityZ(it\collider,True), EntityX(item\collider, True), EntityZ(item\collider, True)) < (180.0 * RoomScale) Then
								it2 = it
								Exit
							ElseIf Distance(EntityX(it\collider,True), EntityZ(it\collider,True), x,z) < (180.0 * RoomScale)
								it2 = it
								Exit
							End If
						End If
					Next
					
					If it2<>Null Then
						Select it2\itemtemplate\name
							Case "gasmask", "supergasmask"
								RemoveItem (it2)
								RemoveItem (item)
								
								it2 = CreateItem("gasmask3", x, y, z)
							Case "vest"
								RemoveItem (it2)
								RemoveItem(item)
								it2 = CreateItem("finevest", x, y, z)
							Case "hazmatsuit","hazmatsuit2"
								RemoveItem (it2)
								RemoveItem(item)
								it2 = CreateItem("hazmatsuit3", x, y, z)
						End Select
					Else 
						If item\itemtemplate\name="scp148ingot" Then
							it2 = CreateItem("scp148", x, y, z)
							RemoveItem(item)
						Else
							PositionEntity(item\collider, x, y, z)
							ResetEntity(item\collider)							
						EndIf
					EndIf					
			End Select
		Case "hand", "hand2"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(3, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1", "fine", "very fine"
					If (item\itemtemplate\name = "hand")
						it2 = CreateItem("hand2", x, y, z)
					Else
						it2 = CreateItem("hand", x, y, z)
					EndIf
			End Select
			RemoveItem(item)
		Case "firstaidkit", "firstaid2" ; TODO Missing small and very fine
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
				If Rand(2)=1 Then
					it2 = CreateItem("firstaid2", x, y, z)
				Else
				    it2 = CreateItem("firstaid", x, y, z)
				EndIf
				Case "fine"
					it2 = CreateItem("finefirstaid", x, y, z)
				Case "very fine"
					it2 = CreateItem("veryfinefirstaid", x, y, z)
			End Select
			RemoveItem(item)
		Case "key1", "key2", "key3", "key4", "key5"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("playingcard", x, y, z)
				Case "fine"
					Select item\itemtemplate\name
						Case "key1"
							Select SelectedDifficulty\otherFactors
								Case EASY
									it2 = CreateItem("key2", x, y, z)
								Case NORMAL
									If Rand(5)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key2", x, y, z)
									EndIf
								Case HARD
									If Rand(4)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key2", x, y, z)
									EndIf
							End Select
						Case "key2"
							Select SelectedDifficulty\otherFactors
								Case EASY
									it2 = CreateItem("key3", x, y, z)
								Case NORMAL
									If Rand(4)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key3", x, y, z)
									EndIf
								Case HARD
									If Rand(3)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key3", x, y, z)
									EndIf
							End Select
						Case "key3"
							Select SelectedDifficulty\otherFactors
								Case EASY
									If Rand(10)=1 Then
										it2 = CreateItem("key4", x, y, z)
									Else
										it2 = CreateItem("playingcard", x, y, z)	
									EndIf
								Case NORMAL
									If Rand(15)=1 Then
										it2 = CreateItem("key4", x, y, z)
									Else
										it2 = CreateItem("playingcard", x, y, z)	
									EndIf
								Case HARD
									If Rand(20)=1 Then
										it2 = CreateItem("key4", x, y, z)
									Else
										it2 = CreateItem("playingcard", x, y, z)	
									EndIf
							End Select
						Case "key4"
							Select SelectedDifficulty\otherFactors
								Case EASY
									it2 = CreateItem("key5", x, y, z)
								Case NORMAL
									If Rand(4)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key5", x, y, z)
									EndIf
								Case HARD
									If Rand(3)=1 Then
										it2 = CreateItem("mastercard", x, y, z)
									Else
										it2 = CreateItem("key5", x, y, z)
									EndIf
							End Select
						Case "key5"	
							Local CurrAchvAmount%=0
							For i = 0 To MAXACHIEVEMENTS-1
								If Achievements(i)=True
									CurrAchvAmount=CurrAchvAmount+1
								EndIf
							Next
							
							DebugLog CurrAchvAmount
							
							Select SelectedDifficulty\otherFactors
								Case EASY
									If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*3)-((CurrAchvAmount-1)*3))=0
										it2 = CreateItem("key6", x, y, z)
									Else
										it2 = CreateItem("mastercard", x, y, z)
									EndIf
								Case NORMAL
									If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*4)-((CurrAchvAmount-1)*3))=0
										it2 = CreateItem("key6", x, y, z)
									Else
										it2 = CreateItem("mastercard", x, y, z)
									EndIf
								Case HARD
									If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*5)-((CurrAchvAmount-1)*3))=0
										it2 = CreateItem("key6", x, y, z)
									Else
										it2 = CreateItem("mastercard", x, y, z)
									EndIf
							End Select		
					End Select
				Case "very fine"
					CurrAchvAmount%=0
					For i = 0 To MAXACHIEVEMENTS-1
						If Achievements(i)=True
							CurrAchvAmount=CurrAchvAmount+1
						EndIf
					Next
					
					DebugLog CurrAchvAmount
					
					Select SelectedDifficulty\otherFactors
						Case EASY
							If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*3)-((CurrAchvAmount-1)*3))=0
								it2 = CreateItem("key6", x, y, z)
							Else
								it2 = CreateItem("mastercard", x, y, z)
							EndIf
						Case NORMAL
							If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*4)-((CurrAchvAmount-1)*3))=0
								it2 = CreateItem("key6", x, y, z)
							Else
								it2 = CreateItem("mastercard", x, y, z)
							EndIf
						Case HARD
							If GuaranteedOmni Lor Rand(0,((MAXACHIEVEMENTS-1)*5)-((CurrAchvAmount-1)*3))=0
								it2 = CreateItem("key6", x, y, z)
							Else
								it2 = CreateItem("mastercard", x, y, z)
							EndIf
					End Select
			End Select
			
			RemoveItem(item)
		Case "key6"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					If Rand(2)=1 Then
						it2 = CreateItem("mastercard", x, y, z)
					Else
						it2 = CreateItem("playingcard", x, y, z)			
					EndIf	
				Case "fine", "very fine"
					it2 = CreateItem("key6", x, y, z)
			End Select			
			
			RemoveItem(item)
		Case "playingcard", "coin", "25ct"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("key1", x, y, z)	
			    Case "fine", "very fine"
					it2 = CreateItem("key2", x, y, z)
			End Select
			RemoveItem(item)
		Case "mastercard"
			Select setting
				Case "rough"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "coarse"
					it2 = CreateItem("25ct", x, y, z)
					Local it3.Items,it4.Items,it5.Items
					it3 = CreateItem("25ct", x, y, z)
					it4 = CreateItem("25ct", x, y, z)
					it5 = CreateItem("25ct", x, y, z)
					EntityType (it3\collider, HIT_ITEM)
					EntityType (it4\collider, HIT_ITEM)
					EntityType (it5\collider, HIT_ITEM)
				Case "1:1"
					it2 = CreateItem("key1", x, y, z)	
			    Case "fine", "very fine"
					it2 = CreateItem("key2", x, y, z)
			End Select
			RemoveItem(item)
		Case "snav300", "snav310", "snav", "snavulti"
			Select setting
				Case "rough", "coarse"
					it2 = CreateItem("electronics", x, y, z)
				Case "1:1"
					it2 = CreateItem("snav", x, y, z)
					it2\state = 100
				Case "fine"
					it2 = CreateItem("snav310", x, y, z)
					it2\state = 100
				Case "very fine"
					it2 = CreateItem("snavulti", x, y, z)
					it2\state = 101
			End Select
			
			RemoveItem(item)
		Case "radio", "fineradio", "veryfineradio", "18vradio"
			Select setting
				Case "rough", "coarse"
					it2 = CreateItem("electronics", x, y, z)
				Case "1:1"
					it2 = CreateItem("18vradio", x, y, z)
					it2\state = 100
				Case "fine"
					it2 = CreateItem("fineradio", x, y, z)
					it2\state = 101
				Case "very fine"
					it2 = CreateItem("veryfineradio", x, y, z)
					it2\state = 101
			End Select
			
			RemoveItem(item)
		Case "scp513"
			Select setting
				Case "rough", "coarse"
					PlaySound_Strict LoadTempSound("SFX\SCP\513\914Refine.ogg")
					For n.npcs = Each NPCs
						If n\npctype = NPCtype5131 Then RemoveNPC(n)
					Next
					d.Decals = CreateDecal(0, x, 8*RoomScale+0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1", "fine", "very fine"
					it2 = CreateItem("scp513", x, y, z)
					
			End Select
			
			RemoveItem(item)
		Case "scp420j", "cigarette"
			Select setting
				Case "rough", "coarse"			
					d.Decals = CreateDecal(0, x, 8*RoomScale+0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("cigarette", x, y, z)
				Case "fine"
					it2 = CreateItem("join", x, y, z)
				Case "very fine"
					it2 = CreateItem("smellyjoint", x, y, z)
			End Select
			
			RemoveItem(item)
		Case "bat", "18vbat", "killbat"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("18vbat", x, y, z)
				Case "fine"
					it2 = CreateItem("killbat", x, y, z)
				Case "very fine"
					it2 = CreateItem("killbat", x, y, z)
			End Select
			
			RemoveItem(item)
		Case "eyedrops", "fineeyedrops", "supereyedrops", "redeyedrops"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("redeyedrops", x,y,z)
				Case "fine"
					it2 = CreateItem("fineeyedrops", x,y,z)
				Case "very fine"
					it2 = CreateItem("supereyedrops", x,y,z)
			End Select
			
			RemoveItem(item)		
		Case "hazmatsuit", "hazmatsuit2"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("hazmatsuit", x,y,z)
				Case "fine"
					it2 = CreateItem("hazmatsuit2", x,y,z)
				Case "very fine"
					it2 = CreateItem("hazmatsuit2", x,y,z)
			End Select
			
			RemoveItem(item)
			
		Case "syringe"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("finefirstaid", x, y, z)	
				Case "fine"
					it2 = CreateItem("finesyringe", x, y, z)
				Case "very fine"
					it2 = CreateItem("veryfinesyringe", x, y, z)
			End Select

			RemoveItem(item)
		Case "finesyringe"
			Select setting
				Case "rough"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
					d\Size = 0.07 : ScaleSprite(d\obj, d\Size, d\Size)
				Case "coarse"
					it2 = CreateItem("firstaid", x, y, z)
				Case "1:1"
					it2 = CreateItem("firstaid2", x, y, z)	
				Case "fine", "very fine"
					it2 = CreateItem("veryfinesyringe", x, y, z)
			End Select

			RemoveItem(item)
		Case "veryfinesyringe"
			Select setting
				Case "rough", "coarse", "1:1", "fine"
					it2 = CreateItem("electronics", x, y, z)	
				Case "very fine"
					n.NPCs = CreateNPC(NPCtype008,x,y,z)
					n\State = 2
			End Select
			
			RemoveItem(item)
		Case "scp500", "scp500death", "pill"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateItem("pill", x, y, z)
					RemoveItem(item)
				Case "fine"
					Local no427Spawn% = False
					For it3.Items = Each Items
						If it3\itemtemplate\name = "scp427" Then
							no427Spawn = True
							Exit
						EndIf
					Next
					If (Not no427Spawn) Then
						it2 = CreateItem("scp427", x, y, z)
					Else
						it2 = CreateItem("scp500death", x, y, z)
					EndIf
					RemoveItem(item)
				Case "very fine"
					it2 = CreateItem("scp500death", x, y, z)
					RemoveItem(item)
			End Select
			
		Case "cup"
			Select setting
				Case "rough", "coarse"
					d.Decals = CreateDecal(0, x, 8 * RoomScale + 0.010, z, 90, Rand(360), 0)
					d\Size = 0.2 : EntityAlpha(d\obj, 0.8) : ScaleSprite(d\obj, d\Size, d\Size)
				Case "1:1"
					it2 = CreateCup(item\drinkName, x,y,z, 255-item\r,255-item\g,255-item\b,item\a)
					it2\state = item\state
				Case "fine"
					it2 = CreateCup(item\drinkName, x,y,z, Min(item\r*Rnd(0.9,1.1),255),Min(item\g*Rnd(0.9,1.1),255),Min(item\b*Rnd(0.9,1.1),255),item\a)
					it2\state = item\state+1.0
				Case "very fine"
					it2 = CreateCup(item\drinkName, x,y,z, Min(item\r*Rnd(0.5,1.5),255),Min(item\g*Rnd(0.5,1.5),255),Min(item\b*Rnd(0.5,1.5),255),item\a)
					it2\state = item\state*2
					If Rand(5)=1 Then
						ExplosionTimer = 135
					EndIf
			End Select	
			
			RemoveItem(item)
		Default
			
			If item\itemtemplate\group = "paper" Then
				Select setting
					Case "rough", "coarse"
						d.Decals = CreateDecal(7, x, 8 * RoomScale + 0.005, z, 90, Rand(360), 0)
						d\Size = 0.12 : ScaleSprite(d\obj, d\Size, d\Size)
					Case "1:1"
						Select Rand(6)
							Case 1
								it2 = CreateItem("doc106", x, y, z)
							Case 2
								it2 = CreateItem("doc079", x, y, z)
							Case 3
								it2 = CreateItem("doc173", x, y, z)
							Case 4
								it2 = CreateItem("doc895", x, y, z)
							Case 5
								it2 = CreateItem("doc682", x, y, z)
							Case 6
								it2 = CreateItem("doc860", x, y, z)
						End Select
					Case "fine", "very fine"
						it2 = CreateItem("origami", x, y, z)
				End Select
				
				RemoveItem(item)
			Else
				PositionEntity(item\collider, x, y, z)
				ResetEntity(item\collider)	
			EndIf
			
	End Select
	
	If it2 <> Null Then EntityType (it2\collider, HIT_ITEM)
End Function

Function Use294()
	Local x#,y#, xtemp%,ytemp%, strtemp$, temp%
	
	x = GraphicWidth/2 - (ImageWidth(Panel294)/2)
	y = GraphicHeight/2 - (ImageHeight(Panel294)/2)
	DrawImage Panel294, x, y
	If Fullscreen Then DrawImage CursorIMG, ScaledMouseX(),ScaledMouseY()
	
	temp = True
	If PlayerRoom\SoundCHN<>0 Then temp = False
	
	Color 255, 255, 255
	Text x+903*HUDScale, y+185*HUDScale, Right(Input294,13), True,True
	
	If temp Then
		If MouseHit1 Then
			xtemp = Floor((ScaledMouseX()-x-228*HUDScale) / 35.5 / HUDScale)
			ytemp = Floor((ScaledMouseY()-y-342*HUDScale) / 36.5 / HUDScale)
			
			temp = False
			
			If ytemp => 0 And ytemp < 5 Then
				If xtemp => 0 And xtemp < 10 Then
					PlaySound_Strict ButtonSFX
					
					strtemp = ""
					
					Select ytemp
						Case 0
							strtemp = (xtemp + 1) Mod 10
						Case 1
							Select xtemp
								Case 0
									strtemp = "Q"
								Case 1
									strtemp = "W"
								Case 2
									strtemp = "E"
								Case 3
									strtemp = "R"
								Case 4
									strtemp = "T"
								Case 5
									strtemp = "Y"
								Case 6
									strtemp = "U"
								Case 7
									strtemp = "I"
								Case 8
									strtemp = "O"
								Case 9
									strtemp = "P"
							End Select
						Case 2
							Select xtemp
								Case 0
									strtemp = "A"
								Case 1
									strtemp = "S"
								Case 2
									strtemp = "D"
								Case 3
									strtemp = "F"
								Case 4
									strtemp = "G"
								Case 5
									strtemp = "H"
								Case 6
									strtemp = "J"
								Case 7
									strtemp = "K"
								Case 8
									strtemp = "L"
								Case 9 ;dispense
									temp = True
							End Select
						Case 3
							Select xtemp
								Case 0
									strtemp = "Z"
								Case 1
									strtemp = "X"
								Case 2
									strtemp = "C"
								Case 3
									strtemp = "V"
								Case 4
									strtemp = "B"
								Case 5
									strtemp = "N"
								Case 6
									strtemp = "M"
								Case 7
									strtemp = "-"
								Case 8
									strtemp = " "
								Case 9
									Input294 = Left(Input294, Max(Len(Input294)-1,0))
							End Select
						Case 4
							strtemp = " "
					End Select
				EndIf
			EndIf
			
			Input294 = Input294 + strtemp
			
			If temp And Input294<>"" Then ;dispense
				Input294 = Trim(Lower(Input294))
				For i = 1 To 2
					Local prefix$ = I_Loc\Cup_OfPrefix[i] + " "
					If Left(Input294, Min(Len(prefix),Len(Input294))) = prefix Then
						Input294 = Right(Input294, Len(Input294)-Len(prefix))
					EndIf
				Next
				
				Local iniStr$ = "DATA\SCP-294.ini"
				Local loc% = -1
				Local hasOverride%
				If Input294<>""
					For m.ActiveMods = Each ActiveMods
						Local modIniStr$ = m\Path + iniStr
						If FileType(modIniStr) = 1 Then
							Local sectionLocation = GetINISectionLocation(modIniStr, Input294)
							If sectionLocation <> -1 Then
								iniStr = modIniStr
								loc = sectionLocation
								Exit
							EndIf
							If FileType(modIniStr + ".OVERRIDE") Then
								hasOverride = True
								Exit
							EndIf
						EndIf
					Next

					If loc = -1 And (Not hasOverride) Then
						loc = GetINISectionLocation(iniStr, Input294)
					EndIf
				EndIf
				
				If loc <> -1 Then
					GiveAchievement(Achv294)

					strtemp$ = GetINIString2(iniStr, loc, "dispensesound")
					If strtemp="" Then
						PlayerRoom\SoundCHN = PlaySound_Strict (LoadTempSound("SFX\SCP\294\dispense1.ogg"))
					Else
						PlayerRoom\SoundCHN = PlaySound_Strict (LoadTempSound(strtemp))
					EndIf
					
					If GetINIInt2(iniStr, loc, "explosion")=True Then 
						ExplosionTimer = 135
						DeathMSG = GetINIString2(iniStr, loc, "deathmessage")
					EndIf
					
					strtemp$ = GetINIString2(iniStr, loc, "color")
					
					sep1 = Instr(strtemp, ",", 1)
					sep2 = Instr(strtemp, ",", sep1+1)
					r% = Trim(Left(strtemp, sep1-1))
					g% = Trim(Mid(strtemp, sep1+1, sep2-sep1-1))
					b% = Trim(Right(strtemp, Len(strtemp)-sep2))
					
					alpha# = Float(GetINIString2(iniStr, loc, "alpha",1.0))
					glow = GetINIInt2(iniStr, loc, "glow")
					;If alpha = 0 Then alpha = 1.0
					If glow Then alpha = -alpha
					
					it.items = CreateCup(Input294, EntityX(PlayerRoom\Objects[1],True),EntityY(PlayerRoom\Objects[1],True),EntityZ(PlayerRoom\Objects[1],True), r,g,b,alpha)
					EntityType (it\collider, HIT_ITEM)
					
				Else
					;out of range
					Input294 = I_Loc\HUD_294Range
					PlayerRoom\SoundCHN = PlaySound_Strict (LoadTempSound("SFX\SCP\294\outofrange.ogg"))
				EndIf
				
			EndIf
			
		EndIf ;if mousehit1
		
		If MouseHit2 Or (Not Using294) Then 
			HidePointer()
			Using294 = False
			Input294 = ""
			MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
		EndIf
		
	Else ;playing a dispensing sound
		If Input294 <> I_Loc\HUD_294Range Then Input294 = I_Loc\HUD_294Dispense
		
		If Not ChannelPlaying(PlayerRoom\SoundCHN) Then
			If Input294 <> I_Loc\HUD_294Range Then
				HidePointer()
				Using294 = False
				MouseXSpeed() : MouseYSpeed() : MouseZSpeed() : mouse_x_speed_1#=0.0 : mouse_y_speed_1#=0.0
				Local e.Events
				For e.Events = Each Events
					If e\room = PlayerRoom
						e\EventState2 = 0
						Exit
					EndIf
				Next
			EndIf
			Input294=""
			PlayerRoom\SoundCHN=0
		EndIf
	EndIf
	
End Function

Function Use427()
	Local i%,pvt%,de.Decals,tempchn%
	Local prevI427Timer# = I_427\Timer
	
	If I_427\Timer < 70*360
		If I_427\Using=True Then
			I_427\Timer = I_427\Timer + FPSfactor
			If Injuries > 0.0 Then
				Injuries = Max(Injuries - 0.0005 * FPSfactor,0.0)
			EndIf
			If Bloodloss > 0.0 And Injuries <= 1.0 Then
				Bloodloss = Max(Bloodloss - 0.001 * FPSfactor,0.0)
			EndIf
			If Infect > 0.0 Then
				Infect = Max(Infect - 0.001 * FPSfactor,0.0)
			EndIf
			For i = 0 To 5
				If SCP1025state[i]>0.0 Then
					SCP1025state[i] = Max(SCP1025state[i] - 0.001 * FPSfactor,0.0)
				EndIf
			Next
			If I_427\Sound[0]=0 Then
				I_427\Sound[0] = LoadSound_Strict("SFX\SCP\427\Effect.ogg")
			EndIf
			If (Not ChannelPlaying(I_427\SoundCHN[0])) Then
				I_427\SoundCHN[0] = PlaySound_Strict(I_427\Sound[0])
			EndIf
			If I_427\Timer => 70*180 Then
				If I_427\Sound[1]=0 Then
					I_427\Sound[1] = LoadSound_Strict("SFX\SCP\427\Transform.ogg")
				EndIf
				If (Not ChannelPlaying(I_427\SoundCHN[1])) Then
					I_427\SoundCHN[1] = PlaySound_Strict(I_427\Sound[1])
				EndIf
			EndIf
			If prevI427Timer < 70*60 And I_427\Timer => 70*60 Then
				Msg = I_Loc\Message_427_1
				MsgTimer = 70*5
			ElseIf prevI427Timer < 70*180 And I_427\Timer => 70*180 Then
				Msg = I_Loc\Message_427_2
				MsgTimer = 70*5
			EndIf
		Else
			For i = 0 To 1
				If I_427\SoundCHN[i]<>0 Then
					If ChannelPlaying(I_427\SoundCHN[i]) Then
						StopChannel(I_427\SoundCHN[i])
					EndIf
				EndIf
			Next
		EndIf
	Else
		If prevI427Timer-FPSfactor < 70*360 And I_427\Timer => 70*360 Then
			Msg = I_Loc\Message_427_3
			MsgTimer = 70*5
		ElseIf prevI427Timer-FPSfactor < 70*390 And I_427\Timer => 70*390 Then
			Msg = I_Loc\Message_427_4
			MsgTimer = 70*5
		EndIf
		I_427\Timer = I_427\Timer + FPSfactor
		If I_427\Sound[0]=0 Then
			I_427\Sound[0] = LoadSound_Strict("SFX\SCP\427\Effect.ogg")
		EndIf
		If I_427\Sound[1]=0 Then
			I_427\Sound[1] = LoadSound_Strict("SFX\SCP\427\Transform.ogg")
		EndIf
		For i = 0 To 1
			If (Not ChannelPlaying(I_427\SoundCHN[i])) Then
				I_427\SoundCHN[i] = PlaySound_Strict(I_427\Sound[i])
			EndIf
		Next
		If Rnd(200)<2.0 Then
			pvt = CreatePivot()
			PositionEntity pvt, EntityX(Collider)+Rnd(-0.05,0.05),EntityY(Collider)-0.05,EntityZ(Collider)+Rnd(-0.05,0.05)
			TurnEntity pvt, 90, 0, 0
			EntityPick(pvt,0.3)
			de.Decals = CreateDecal(20, PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
			de\Size = Rnd(0.03,0.08)*2.0 : EntityAlpha(de\obj, 1.0) : ScaleSprite de\obj, de\Size, de\Size
			tempchn% = PlaySound_Strict (DripSFX(Rand(0,2)))
			ChannelVolume tempchn, Rnd(0.0,0.8)*SFXVolume
			ChannelPitch tempchn, Rand(20000,30000)
			FreeEntity pvt
			BlurTimer = 800
		EndIf
		If I_427\Timer >= 70*420 Then
			Kill()
			DeathMSG = I_Loc\DeathMessage_427
		ElseIf I_427\Timer >= 70*390 Then
			Crouch = True
		EndIf
	EndIf
	
End Function


Function UpdateMTF%()
	If PlayerRoom\RoomTemplate\Name = "gateaentrance" Then Return
	
	Local r.Rooms, n.NPCs
	Local dist#, i%
	
	;mtf ei vielä spawnannut, spawnataan jos pelaaja menee tarpeeksi lähelle gate b:tä
	If MTFtimer = 0 Then
		If Rand(30)=1 And PlayerRoom\RoomTemplate\Name$ <> "dimension1499" Then
			
			Local entrance.Rooms = Null
			For r.Rooms = Each Rooms
				If Lower(r\RoomTemplate\Name) = "gateaentrance" Then entrance = r : Exit
			Next
			
			If entrance <> Null Then 
				If Abs(EntityZ(entrance\obj)-EntityZ(Collider))<30.0 Then
					;If PlayerRoom\RoomTemplate\Name<>"room860" And PlayerRoom\RoomTemplate\Name<>"pocketdimension" Then
					If PlayerInReachableRoom()
						;PlaySound_Strict LoadTempSound("SFX\Character\MTF\Announc.ogg")
						PlayAnnouncement("SFX\Character\MTF\Announc.ogg")
					EndIf
					
					MTFtimer = FPSfactor
					Local leader.NPCs
					For i = 0 To 2
						n.NPCs = CreateNPC(NPCtypeMTF, EntityX(entrance\obj)+0.3*(i-1), 1.0,EntityZ(entrance\obj)+8.0)
						
						If i = 0 Then 
							leader = n
						Else
							n\MTFLeader = leader
						EndIf
						
						n\PrevX = i
					Next
				EndIf
			EndIf
		EndIf
	Else
		If MTFtimer <= 70*120 ;70*120
			MTFtimer = MTFtimer + FPSfactor
		ElseIf MTFtimer > 70*120 And MTFtimer < 10000
			If PlayerInReachableRoom()
				PlayAnnouncement("SFX\Character\MTF\AnnouncAfter1.ogg")
			EndIf
			MTFtimer = 10000
		ElseIf MTFtimer >= 10000 And MTFtimer <= 10000+(70*120) ;70*120
			MTFtimer = MTFtimer + FPSfactor
		ElseIf MTFtimer > 10000+(70*120) And MTFtimer < 20000
			If PlayerInReachableRoom()
				PlayAnnouncement("SFX\Character\MTF\AnnouncAfter2.ogg")
			EndIf
			MTFtimer = 20000
		ElseIf MTFtimer >= 20000 And MTFtimer <= 20000+(70*60) ;70*120
			MTFtimer = MTFtimer + FPSfactor
		ElseIf MTFtimer > 20000+(70*60) And MTFtimer < 25000
			If PlayerInReachableRoom()
				;If the player has an SCP in their inventory play special voice line.
				For i = 0 To MaxItemAmount-1
					If Inventory(i) <> Null Then
						If Instr(Inventory(i)\itemtemplate\name, "scp") = 1 Then
							PlayAnnouncement("SFX\Character\MTF\ThreatAnnouncPossession.ogg")
							MTFtimer = 25000
							Return
							Exit
						EndIf
					EndIf
				Next
				
				PlayAnnouncement("SFX\Character\MTF\ThreatAnnounc"+Rand(1,3)+".ogg")
			EndIf
			MTFtimer = 25000
			
		ElseIf MTFtimer >= 25000 And MTFtimer <= 25000+(70*60) ;70*120
			MTFtimer = MTFtimer + FPSfactor
		ElseIf MTFtimer > 25000+(70*60) And MTFtimer < 30000
			If PlayerInReachableRoom()
				PlayAnnouncement("SFX\Character\MTF\ThreatAnnouncFinal.ogg")
			EndIf
			MTFtimer = 30000
			
		EndIf
	EndIf
	
End Function


Function UpdateInfect()
	Local temp#, i%, r.Rooms
	
	Local teleportForInfect% = Not GodMode
	
	If PlayerRoom\RoomTemplate\Name = "room860"
		For e.Events = Each Events
			If e\EventName = "room860"
				If e\EventState = 1.0
					teleportForInfect = False
				EndIf
				Exit
			EndIf
		Next
	ElseIf PlayerRoom\RoomTemplate\Name = "dimension1499" Or PlayerRoom\RoomTemplate\Name = "pocketdimension" Or PlayerRoom\RoomTemplate\Name = "gatea"
		teleportForInfect = False
	ElseIf PlayerRoom\RoomTemplate\Name = "exit1" And EntityY(Collider)>1040.0*RoomScale
		teleportForInfect = False
	EndIf
	
	If Infect>0 Then
		ShowEntity InfectOverlay
		
		If Infect < 93.0 Then
			temp=Infect
			If (Not I_427\Using And I_427\Timer < 70*360) Then
				Infect = Min(Infect+FPSfactor*0.002,100)
			EndIf
			
			BlurTimer = Max(Infect*3*(2.0-CrouchState),BlurTimer)
			
			HeartBeatRate = Max(HeartBeatRate, 100)
			HeartBeatVolume = Max(HeartBeatVolume, Infect/120.0)
			
			EntityAlpha InfectOverlay, Min(((Infect*0.2)^2)/1000.0,0.5) * (Sin(MilliSecs()/8.0)+2.0)
			
			For i = 0 To 6
				If Infect>i*15+10 And temp =< i*15+10 Then
					PlaySound_Strict LoadTempSound("SFX\SCP\008\Voices"+i+".ogg")
				EndIf
			Next
			
			If Infect > 20 And temp =< 20.0 Then
				Msg = I_Loc\Message_008_1
				MsgTimer = 70*6
			ElseIf Infect > 40 And temp =< 40.0
				Msg = I_Loc\Message_008_2
				MsgTimer = 70*6
			ElseIf Infect > 60 And temp =< 60.0
				Msg = I_Loc\Message_008_3
				MsgTimer = 70*6
			ElseIf Infect > 80 And temp =< 80.0
				Msg = I_Loc\Message_008_4
				MsgTimer = 70*6
			ElseIf Infect =>91.5
				BlinkTimer = Max(Min(-10*(Infect-91.5),BlinkTimer),-10)
				IsZombie = True
				UnableToMove = True
				If Infect >= 92.7 And temp < 92.7 Then
					If teleportForInfect
						For r.Rooms = Each Rooms
							If r\RoomTemplate\Name="008" Then
								PositionEntity Collider, EntityX(r\Objects[7],True),EntityY(r\Objects[7],True),EntityZ(r\Objects[7],True),True
								ResetEntity Collider
								r\NPC[0] = CreateNPC(NPCtypeD, EntityX(r\Objects[6],True),EntityY(r\Objects[6],True)+0.2,EntityZ(r\Objects[6],True))
								r\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\008\KillScientist1.ogg")
								r\NPC[0]\SoundChn = PlaySound_Strict(r\NPC[0]\Sound)
								tex = LoadTexture_Strict("GFX\npcs\scientist2.jpg")
								EntityTexture r\NPC[0]\obj, tex
								FreeTexture tex
								r\NPC[0]\State=6
								PlayerRoom = r
								UnableToMove = False
								Exit
							EndIf
						Next
					EndIf
				EndIf
			EndIf
		Else
			
			temp=Infect
			Infect = Min(Infect+FPSfactor*0.004,100)
			
			If teleportForInfect
				If Infect < 94.7 Then
					EntityAlpha InfectOverlay, 0.5 * (Sin(MilliSecs()/8.0)+2.0)
					BlurTimer = 900
					
					If Infect > 94.5 Then BlinkTimer = Max(Min(-50*(Infect-94.5),BlinkTimer),-10)
					PointEntity Collider, PlayerRoom\NPC[0]\Collider
					PointEntity PlayerRoom\NPC[0]\Collider, Collider
					PointEntity Camera, PlayerRoom\NPC[0]\Collider,EntityRoll(Camera)
					ForceMove = 0.75
					Injuries = 2.5
					Bloodloss = 0
					UnableToMove = False
					
					Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 357, 381, 0.3)
				ElseIf Infect < 98.5
					
					EntityAlpha InfectOverlay, 0.5 * (Sin(MilliSecs()/5.0)+2.0)
					BlurTimer = 950
					
					ForceMove = 0.0
					UnableToMove = True
					PointEntity Camera, PlayerRoom\NPC[0]\Collider
					
					If temp < 94.7 Then 
						PlayerRoom\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\008\KillScientist2.ogg")
						PlayerRoom\NPC[0]\SoundChn = PlaySound_Strict(PlayerRoom\NPC[0]\Sound)
						
						DeathMSG = I_Loc\DeathMessage_008
						
						Kill()
						de.Decals = CreateDecal(3, EntityX(PlayerRoom\NPC[0]\Collider), 544*RoomScale + 0.01, EntityZ(PlayerRoom\NPC[0]\Collider),90,Rnd(360),0)
						de\Size = 0.8
						ScaleSprite(de\obj, de\Size,de\Size)
					ElseIf Infect > 96
						BlinkTimer = Max(Min(-10*(Infect-96),BlinkTimer),-10)
					Else
						KillTimer = Max(-350, KillTimer)
					EndIf
					
					If PlayerRoom\NPC[0]\State2=0 Then
						Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 13, 19, 0.3,False)
						If AnimTime(PlayerRoom\NPC[0]\obj) => 19 Then PlayerRoom\NPC[0]\State2=1
					Else
						Animate2(PlayerRoom\NPC[0]\obj, AnimTime(PlayerRoom\NPC[0]\obj), 19, 13, -0.3)
						If AnimTime(PlayerRoom\NPC[0]\obj) =< 13 Then PlayerRoom\NPC[0]\State2=0
					EndIf
					
					If ParticleAmount>0
						If Rand(50)=1 Then
							p.Particles = CreateParticle(EntityX(PlayerRoom\NPC[0]\Collider),EntityY(PlayerRoom\NPC[0]\Collider),EntityZ(PlayerRoom\NPC[0]\Collider), 5, Rnd(0.05,0.1), 0.15, 200)
							p\speed = 0.01
							p\SizeChange = 0.01
							p\A = 0.5
							p\Achange = -0.01
							RotateEntity p\pvt, Rnd(360),Rnd(360),0
						EndIf
					EndIf
					
					PositionEntity Head, EntityX(PlayerRoom\NPC[0]\Collider,True), EntityY(PlayerRoom\NPC[0]\Collider,True)+0.65,EntityZ(PlayerRoom\NPC[0]\Collider,True),True
					RotateEntity Head, (1.0+Sin(MilliSecs()/5.0))*15, PlayerRoom\angle-180, 0, True
					MoveEntity Head, 0,0,-0.4
					TurnEntity Head, 80+(Sin(MilliSecs()/5.0))*30,(Sin(MilliSecs()/5.0))*40,0
				EndIf
			Else
				Kill()
				BlinkTimer = Max(Min(-10*(Infect-96),BlinkTimer),-10)
				If PlayerRoom\RoomTemplate\Name = "dimension1499" Then
					DeathMSG = I_Loc\DeathMessage_1499
				ElseIf PlayerRoom\RoomTemplate\Name = "gatea" Or PlayerRoom\RoomTemplate\Name = "exit1" Then
					Local deathGate$
					If PlayerRoom\RoomTemplate\Name = "gatea" Then
						deathGate = I_Loc\DeathMessage_008Gatea
					Else
						deathGate = I_Loc\DeathMessage_008Gateb
					EndIf
					DeathMSG = Format(I_Loc\DeathMessage_008Gate, deathGate)
				Else
					DeathMSG = ""
				EndIf
			EndIf
		EndIf
		
		
	Else
		HideEntity InfectOverlay
	EndIf
End Function

;--------------------------------------- math -------------------------------------------------------

Function GenerateSeedNumber(seed$)
 	Local temp% = 0
 	Local shift% = 0
 	For i = 1 To Len(seed)
 		temp = temp Xor (Asc(Mid(seed,i,1)) Shl shift)
 		shift=(shift+1) Mod 24
	Next
 	Return temp
End Function

Function Distance#(x1#, y1#, x2#, y2#)
	Local x# = x2 - x1, y# = y2 - y1
	Return(Sqr(x*x + y*y))
End Function


Function CurveValue#(number#, old#, smooth#)
	If FPSfactor = 0 Then Return old
	
	If number < old Then
		Return Max(old + (number - old) * (1.0 / smooth * FPSfactor), number)
	Else
		Return Min(old + (number - old) * (1.0 / smooth * FPSfactor), number)
	EndIf
End Function

Function CurveAngle#(val#, old#, smooth#)
	If FPSfactor = 0 Then Return old
	
   Local diff# = WrapAngle(val) - WrapAngle(old)
   If diff > 180 Then diff = diff - 360
   If diff < - 180 Then diff = diff + 360
   Return WrapAngle(old + diff * (1.0 / smooth * FPSfactor))
End Function




Function WrapAngle#(angle#)
	If angle = INFINITY Then Return 0.0
	While angle < 0
		angle = angle + 360
	Wend 
	While angle >= 360
		angle = angle - 360
	Wend
	Return angle
End Function

Function point_direction#(x1#,z1#,x2#,z2#)
	Local dx#, dz#
	dx = x1 - x2
	dz = z1 - z2
	Return ATan2(dz,dx)
End Function

Function point_distance#(x1#,z1#,x2#,z2#)
	Local dx#,dy#
	dx = x1 - x2
	dy = z1 - z2
	Return Sqr((dx*dx)+(dy*dy)) 
End Function

Function angleDist#(a0#,a1#)
	Local b# = a0-a1
	Local bb#
	If b<-180.0 Then
		bb = b+360.0
	Else If b>180.0 Then
		bb = b-360.0
	Else
		bb = b
	EndIf
	Return bb
End Function

;--------------------------------------- decals -------------------------------------------------------

Type Decals
	Field obj%
	Field SizeChange#, Size#, MaxSize#
	Field AlphaChange#, Alpha#
	Field blendmode%
	Field fx%
	Field ID%
	Field Timer#
	
	Field lifetime#
	
	Field x#, y#, z#
	Field pitch#, yaw#, roll#
End Type

Function CreateDecal.Decals(id%, x#, y#, z#, pitch#, yaw#, roll#)
	Local d.Decals = New Decals
	
	d\x = x
	d\y = y
	d\z = z
	d\pitch = pitch
	d\yaw = yaw
	d\roll = roll
	
	d\MaxSize = 1.0
	
	d\Alpha = 1.0
	d\Size = 1.0
	d\obj = CreateSprite()
	d\blendmode = 1
	
	EntityTexture(d\obj, DecalTextures(id))
	EntityFX(d\obj, 0)
	SpriteViewMode(d\obj, 2)
	PositionEntity(d\obj, x, y, z)
	RotateEntity(d\obj, pitch, yaw, roll)
	
	d\ID = id
	
	If DecalTextures(id) = 0 Or d\obj = 0 Then Return Null
	
	Return d
End Function

Function UpdateDecals()
	Local d.Decals
	For d.Decals = Each Decals
		If d\SizeChange <> 0 Then
			d\Size=d\Size + d\SizeChange * FPSfactor
			ScaleSprite(d\obj, d\Size, d\Size)
			
			Select d\ID
				Case 0
					If d\Timer <= 0 Then
						Local angle# = Rand(360)
						Local temp# = Rnd(d\Size)
						Local d2.Decals = CreateDecal(1, EntityX(d\obj) + Cos(angle) * temp, EntityY(d\obj) - 0.0005, EntityZ(d\obj) + Sin(angle) * temp, EntityPitch(d\obj), Rnd(360), EntityRoll(d\obj))
						d2\Size = Rnd(0.1, 0.5) : ScaleSprite(d2\obj, d2\Size, d2\Size)
						PlaySound2(DecaySFX(Rand(1, 3)), Camera, d2\obj, 10.0, Rnd(0.1, 0.5))
						;d\Timer = d\Timer + Rand(50,150)
						d\Timer = Rand(50, 100)
					Else
						d\Timer= d\Timer-FPSfactor
					End If
				;Case 6
				;	EntityBlend d\obj, 2
			End Select
			
			If d\Size >= d\MaxSize Then d\SizeChange = 0 : d\Size = d\MaxSize
		End If
		
		If d\AlphaChange <> 0 Then
			d\Alpha = Min(d\Alpha + FPSfactor * d\AlphaChange, 1.0)
			EntityAlpha(d\obj, d\Alpha)
		End If
		
		If d\lifetime > 0 Then
			d\lifetime=Max(d\lifetime-FPSfactor,5)
		EndIf
		
		If d\Size <= 0 Or d\Alpha <= 0 Or d\lifetime=5.0  Then
			FreeEntity(d\obj)
			Delete d
		End If
	Next
End Function


;--------------------------------------- INI-functions -------------------------------------------------------

Type INIFile
	Field name$
	Field bank%
	Field bankOffset% = 0
	Field size%
End Type

Function ReadINILine$(file.INIFile)
	Local rdbyte%
	Local firstbyte% = True
	Local offset% = file\bankOffset
	Local bank% = file\bank
	Local retStr$ = ""
	rdbyte = PeekByte(bank,offset)
	While ((firstbyte) Or ((rdbyte<>13) And (rdbyte<>10))) And (offset<file\size)
		rdbyte = PeekByte(bank,offset)
		If ((rdbyte<>13) And (rdbyte<>10)) Then
			firstbyte = False
			retStr=retStr+Chr(rdbyte)
		EndIf
		offset=offset+1
	Wend
	file\bankOffset = offset
	Return retStr
End Function

Function UpdateINIFile$(filename$, file.INIFile)
	CatchErrors("Uncaught (UpdateINIFile) " + filename)

	If file\bank<>0 Then FreeBank file\bank
	Local f% = ReadFile(filename)
	Local fleSize% = 1
	While fleSize<FileSize(filename)
		fleSize=fleSize*2
	Wend
	file\bank = CreateBank(fleSize)
	file\size = 0
	While Not Eof(f)
		PokeByte(file\bank,file\size,ReadByte(f))
		file\size=file\size+1
	Wend
	CloseFile(f)

	CatchErrors("UpdateINIFile " + filename)
End Function

Function GetINIString$(file$, section$, parameter$, defaultvalue$="")
	Local TemporaryString$ = ""
	
	Local fileLower$ = Lower(file)
	Local lfile.INIFile = Null
	For k.INIFile = Each INIFile
		If k\name = fileLower Then
			lfile = k
			Exit
		EndIf
	Next
	
	If lfile = Null Then
		DebugLog "CREATE BANK FOR "+file
		lfile = New INIFile
		lfile\name = fileLower
		lfile\bank = 0
		UpdateINIFile(file, lfile)
	EndIf
	
	lfile\bankOffset = 0
	
	section = Lower(section)
	
	;While Not Eof(f)
	While lfile\bankOffset<lfile\size
		Local strtemp$ = ReadINILine(lfile)
		If Left(strtemp,1) = "[" Then
			strtemp$ = Lower(strtemp)
			If Mid(strtemp, 2, Len(strtemp)-2)=section Then
				Repeat
					TemporaryString = ReadINILine(lfile)
					If Lower(Trim(Left(TemporaryString, Max(Instr(TemporaryString, "=") - 1, 0)))) = Lower(parameter) Then
						;CloseFile f
						Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
					EndIf
				Until (Left(TemporaryString, 1) = "[") Or (lfile\bankOffset>=lfile\size)
				
				;CloseFile f
				Return defaultvalue
			EndIf
		EndIf
	Wend
	
	Return defaultvalue
End Function

Function ClearLoadedINIFiles()
	For ini.INIFile = Each INIFile
		If ini\bank <> 0 Then FreeBank ini\bank
		Delete ini
	Next
End Function

Function ParseINIInt%(txt$)
	txt = Lower(Trim(txt))
	If txt = "true" Then
		Return 1
	ElseIf txt = "false"
		Return 0
	Else
		Return Int(txt)
	EndIf
End Function

Function GetINIInt%(file$, section$, parameter$, defaultvalue% = 0)
	Local txt$ = GetINIString(file$, section$, parameter$, defaultvalue)
	Return ParseINIInt(txt)
End Function

Function GetINIFloat#(file$, section$, parameter$, defaultvalue# = 0.0)
	Return Float(GetINIString(file$, section$, parameter$, defaultvalue))
End Function


Function GetINIString2$(file$, start%, parameter$, defaultvalue$="")
	Local TemporaryString$ = ""
	Local f% = ReadFile(file)
	
	Local n%=0
	While Not Eof(f)
		Local strtemp$ = ReadLine(f)
		n=n+1
		If n=start Then 
			Repeat
				TemporaryString = ReadLine(f)
				If Lower(Trim(Left(TemporaryString, Max(Instr(TemporaryString, "=") - 1, 0)))) = Lower(parameter) Then
					CloseFile f
					Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
				EndIf
			Until Left(TemporaryString, 1) = "[" Or Eof(f)
			CloseFile f
			Return defaultvalue
		EndIf
	Wend
	
	CloseFile f	
	
	Return defaultvalue
End Function

Function GetINIInt2%(file$, start%, parameter$, defaultvalue$="")
	Local txt$ = GetINIString2(file$, start%, parameter$, defaultvalue$)
	If Lower(txt) = "true" Then
		Return 1
	ElseIf Lower(txt) = "false"
		Return 0
	Else
		Return Int(txt)
	EndIf
End Function


Function GetINISectionLocation%(file$, section$)
	Local Temp%
	Local f% = ReadFile(file)
	
	section = Lower(section)
	
	Local n%=0
	While Not Eof(f)
		Local strtemp$ = ReadLine(f)
		n=n+1
		If Left(strtemp,1) = "[" Then
			strtemp$ = Lower(strtemp)
			Temp = Instr(strtemp, section)
			While Temp>0
				If (Mid(strtemp, Temp-1, 1)="[" Or Mid(strtemp, Temp-1, 1)="|") And (Mid(strtemp, Temp+Len(section), 1)="]" Or Mid(strtemp, Temp+Len(section), 1)="|") Then
					CloseFile f
					Return n
				EndIf
				Temp = Instr(strtemp, section, Temp+Len(section)+1)
			Wend
		EndIf
	Wend
	
	CloseFile f
	Return -1
End Function



Function PutINIValue%(file$, INI_sSection$, INI_sKey$, INI_sValue$)
	
	; Returns: True (Success) Or False (Failed)
	
	INI_sSection = "[" + Trim$(INI_sSection) + "]"
	Local INI_sUpperSection$ = Upper$(INI_sSection)
	INI_sKey = Trim$(INI_sKey)
	INI_sValue = Trim$(INI_sValue)
	Local INI_sFilename$ = file$
	
	; Retrieve the INI Data (If it exists)
	
	Local INI_sContents$ = INI_FileToString(INI_sFilename)
	
		; (Re)Create the INI file updating/adding the SECTION, KEY And VALUE
	
	Local INI_bWrittenKey% = False
	Local INI_bSectionFound% = False
	Local INI_sCurrentSection$ = ""
	
	Local INI_lFileHandle% = WriteFile(INI_sFilename)
	If INI_lFileHandle = 0 Then Return False ; Create file failed!
	
	Local INI_lOldPos% = 1
	Local INI_lPos% = Instr(INI_sContents, Chr$(0))
	
	While (INI_lPos <> 0)
		
		Local INI_sTemp$ = Mid$(INI_sContents, INI_lOldPos, (INI_lPos - INI_lOldPos))
		
		If (INI_sTemp <> "") Then
			
			If Left$(INI_sTemp, 1) = "[" And Right$(INI_sTemp, 1) = "]" Then
				
					; Process SECTION
				
				If (INI_sCurrentSection = INI_sUpperSection) And (INI_bWrittenKey = False) Then
					INI_bWrittenKey = INI_CreateKey(INI_lFileHandle, INI_sKey, INI_sValue)
				End If
				INI_sCurrentSection = Upper$(INI_CreateSection(INI_lFileHandle, INI_sTemp))
				If (INI_sCurrentSection = INI_sUpperSection) Then INI_bSectionFound = True
				
			Else
				If Left(INI_sTemp, 1) = ":" Then
					WriteLine INI_lFileHandle, INI_sTemp
				Else
						; KEY=VALUE				
					Local lEqualsPos% = Instr(INI_sTemp, "=")
					If (lEqualsPos <> 0) Then
						If (INI_sCurrentSection = INI_sUpperSection) And (Upper$(Trim$(Left$(INI_sTemp, (lEqualsPos - 1)))) = Upper$(INI_sKey)) Then
							If (INI_sValue <> "") Then INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
							INI_bWrittenKey = True
						Else
							WriteLine INI_lFileHandle, INI_sTemp
						End If
					End If
				EndIf
				
			End If
			
		End If
		
			; Move through the INI file...
		
		INI_lOldPos = INI_lPos + 1
		INI_lPos% = Instr(INI_sContents, Chr$(0), INI_lOldPos)
		
	Wend
	
		; KEY wasn;t found in the INI file - Append a New SECTION If required And create our KEY=VALUE Line
	
	If (INI_bWrittenKey = False) Then
		If (INI_bSectionFound = False) Then INI_CreateSection INI_lFileHandle, INI_sSection
		INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
	End If
	
	CloseFile INI_lFileHandle
	
	Return True ; Success
	
End Function

Function INI_FileToString$(INI_sFilename$)
	
	Local INI_sString$ = ""
	Local INI_lFileHandle%= ReadFile(INI_sFilename)
	If INI_lFileHandle <> 0 Then
		While Not(Eof(INI_lFileHandle))
			INI_sString = INI_sString + ReadLine$(INI_lFileHandle) + Chr$(0)
		Wend
		CloseFile INI_lFileHandle
	End If
	Return INI_sString
	
End Function

Function INI_CreateSection$(INI_lFileHandle%, INI_sNewSection$)
	
	If FilePos(INI_lFileHandle) <> 0 Then WriteLine INI_lFileHandle, "" ; Blank Line between sections
	WriteLine INI_lFileHandle, INI_sNewSection
	Return INI_sNewSection
	
End Function

Function INI_CreateKey%(INI_lFileHandle%, INI_sKey$, INI_sValue$)
	
	WriteLine INI_lFileHandle, INI_sKey + " = " + INI_sValue
	Return True
	
End Function

;Save options to .ini.
Function SaveOptionsINI()
	
	PutINIValue(OptionFile, "controls", "mouse sensitivity", MouseSens)
	PutINIValue(OptionFile, "controls", "invert mouse y", InvertMouse)
	PutINIValue(OptionFile, "graphics", "HUD enabled", HUDenabled)
	PutINIValue(OptionFile, "graphics", "screengamma", ScreenGamma)
	PutINIValue(OptionFile, "graphics", "antialias", Opt_AntiAlias)
	PutINIValue(OptionFile, "graphics", "vsync", Vsync)
	PutINIValue(OptionFile, "graphics", "show FPS", ShowFPS)
	PutINIValue(OptionFile, "graphics", "framelimit", Framelimit%)
	PutINIValue(OptionFile, "general", "achievement popup enabled", AchvMSGenabled%)
	PutINIValue(OptionFile, "launcher", "launcher enabled", LauncherEnabled%)
	PutINIValue(OptionFile, "graphics", "texture details", TextureDetails%)
	PutINIValue(OptionFile, "console", "enabled", CanOpenConsole%)
	PutINIValue(OptionFile, "console", "auto opening", ConsoleOpening%)
	PutINIValue(OptionFile, "general", "speed run mode", SpeedRunMode%)
	PutINIValue(OptionFile, "general", "numeric seeds", UseNumericSeeds%)
	PutINIValue(OptionFile, "graphics", "enable vram", EnableVRam)
	PutINIValue(OptionFile, "controls", "mouse smoothing", MouseSmooth)
	PutINIValue(OptionFile, "graphics", "hud offset", HUDOffsetScale)
	PutINIValue(OptionFile, "graphics", "fov", FOV)
	
	PutINIValue(OptionFile, "audio", "music volume", MusicVolume)
	PutINIValue(OptionFile, "audio", "sound volume", PrevSFXVolume)
	PutINIValue(OptionFile, "audio", "sfx release", EnableSFXRelease)
	PutINIValue(OptionFile, "audio", "enable user tracks", EnableUserTracks%)
	PutINIValue(OptionFile, "audio", "user track setting", UserTrackMode%)
	
	PutINIValue(OptionFile, "binds", "Right key", KEY_RIGHT)
	PutINIValue(OptionFile, "binds", "Left key", KEY_LEFT)
	PutINIValue(OptionFile, "binds", "Up key", KEY_UP)
	PutINIValue(OptionFile, "binds", "Down key", KEY_DOWN)
	PutINIValue(OptionFile, "binds", "Blink key", KEY_BLINK)
	PutINIValue(OptionFile, "binds", "Sprint key", KEY_SPRINT)
	PutINIValue(OptionFile, "binds", "Inventory key", KEY_INV)
	PutINIValue(OptionFile, "binds", "Crouch key", KEY_CROUCH)
	PutINIValue(OptionFile, "binds", "Save key", KEY_SAVE)
	PutINIValue(OptionFile, "binds", "Console key", KEY_CONSOLE)
	
End Function

;--------------------------------------- MakeCollBox -functions -------------------------------------------------------


; Create a collision box For a mesh entity taking into account entity scale
; (will not work in non-uniform scaled space)
Function MakeCollBox(mesh%)
	Local sx# = EntityScaleX(mesh, 1)
	Local sy# = Max(EntityScaleY(mesh, 1), 0.001)
	Local sz# = EntityScaleZ(mesh, 1)
	GetMeshExtents(mesh)
	EntityBox mesh, Mesh_MinX * sx, Mesh_MinY * sy, Mesh_MinZ * sz, Mesh_MagX * sx, Mesh_MagY * sy, Mesh_MagZ * sz
End Function

; Find mesh extents
Function GetMeshExtents(Mesh%)
	Local s%, surf%, surfs%, v%, verts%, x#, y#, z#
	Local minx# = INFINITY
	Local miny# = INFINITY
	Local minz# = INFINITY
	Local maxx# = -INFINITY
	Local maxy# = -INFINITY
	Local maxz# = -INFINITY
	
	surfs = CountSurfaces(Mesh)
	
	For s = 1 To surfs
		surf = GetSurface(Mesh, s)
		verts = CountVertices(surf)
		
		For v = 0 To verts - 1
			x = VertexX(surf, v)
			y = VertexY(surf, v)
			z = VertexZ(surf, v)
			
			If (x < minx) Then minx = x
			If (x > maxx) Then maxx = x
			If (y < miny) Then miny = y
			If (y > maxy) Then maxy = y
			If (z < minz) Then minz = z
			If (z > maxz) Then maxz = z
		Next
	Next
	
	Mesh_MinX = minx
	Mesh_MinY = miny
	Mesh_MinZ = minz
	Mesh_MaxX = maxx
	Mesh_MaxY = maxy
	Mesh_MaxZ = maxz
	Mesh_MagX = maxx-minx
	Mesh_MagY = maxy-miny
	Mesh_MagZ = maxz-minz
	
End Function

Function EntityScaleX#(entity%, globl% = False)
	If globl Then TFormVector 1, 0, 0, entity, 0 Else TFormVector 1, 0, 0, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 

Function EntityScaleY#(entity%, globl% = False)
	If globl Then TFormVector 0, 1, 0, entity, 0 Else TFormVector 0, 1, 0, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 

Function EntityScaleZ#(entity%, globl% = False)
	If globl Then TFormVector 0, 0, 1, entity, 0 Else TFormVector 0, 0, 1, entity, GetParent(entity)
	Return Sqr(TFormedX() * TFormedX() + TFormedY() * TFormedY() + TFormedZ() * TFormedZ())
End Function 

Function Graphics3DExt%(width%,height%,depth%=32,mode%=2)
	;If FE_InitExtFlag = 1 Then DeInitExt() ;prevent FastExt from breaking itself
	Graphics3D width,height,depth,mode
	InitFastResize()
	;InitExt()
	AntiAlias GetOptionInt("graphics","antialias")
	;TextureAnisotropy% (GetOptionInt("graphics","anisotropy"),-1)
End Function

Function ResizeImage2(image%,width%,height%)
    img% = CreateImage(width,height)
	
	oldWidth% = ImageWidth(image)
	oldHeight% = ImageHeight(image)
	CopyRect 0,0,oldWidth,oldHeight,1024-oldWidth/2,1024-oldHeight/2,ImageBuffer(image),TextureBuffer(fresize_texture)
	SetBuffer BackBuffer()
	ScaleRender(0,0,2048.0 / Float(RealGraphicWidth) * Float(width) / Float(oldWidth), 2048.0 / Float(RealGraphicWidth) * Float(height) / Float(oldHeight))
	;might want to replace Float(GraphicWidth) with Max(GraphicWidth,GraphicHeight) if portrait sizes cause issues
	;everyone uses landscape so it's probably a non-issue
	CopyRect RealGraphicWidth/2-width/2,RealGraphicHeight/2-height/2,width,height,0,0,BackBuffer(),ImageBuffer(img)
	
    FreeImage image
    Return img
End Function


Function RenderWorld2()
	CameraProjMode ark_blur_cam,0
	CameraProjMode Camera,1
	
	If WearingNightVision>0 And WearingNightVision<3 Then
		AmbientLight Min(Brightness*2,255), Min(Brightness*2,255), Min(Brightness*2,255)
	ElseIf WearingNightVision=3
		AmbientLight 255,255,255
	ElseIf PlayerRoom<>Null
		If (PlayerRoom\RoomTemplate\Name<>"173") And (PlayerRoom\RoomTemplate\Name<>"exit1") And (PlayerRoom\RoomTemplate\Name<>"gatea") Then
			AmbientLight Brightness, Brightness, Brightness
		EndIf
	EndIf
	
	IsNVGBlinking% = False
	HideEntity NVBlink
	
	CameraViewport Camera,0,0,GraphicWidth,GraphicHeight
	
	Local hasBattery% = 2
	Local power% = 0
	If (WearingNightVision=1) Or (WearingNightVision=2)
		For i% = 0 To MaxItemAmount - 1
			If (Inventory(i)<>Null) Then
				If (WearingNightVision = 1 And Inventory(i)\itemtemplate\name = "nvgoggles") Or (WearingNightVision = 2 And Inventory(i)\itemtemplate\name = "supernv") Then
					Inventory(i)\state = Inventory(i)\state - (FPSfactor * (0.02 * WearingNightVision))
					power%=Int(Inventory(i)\state)
					If Inventory(i)\state<=0.0 Then ;this nvg can't be used
						hasBattery = 0
						Msg = I_Loc\MessageItem_NvgBatDead
						BlinkTimer = -1.0
						MsgTimer = 350
						Exit
					ElseIf Inventory(i)\state<=100.0 Then
						hasBattery = 1
					EndIf
				EndIf
			EndIf
		Next
		
		If (hasBattery) Then
			RenderWorld()
		EndIf
	Else
		RenderWorld()
	EndIf
	
	CurrTrisAmount = TrisRendered()

	If hasBattery=0 And WearingNightVision<>3
		IsNVGBlinking% = True
		ShowEntity NVBlink%
	EndIf
	
	If BlinkTimer < - 16 Or BlinkTimer > - 6
		If WearingNightVision=2 And hasBattery<>0 Then ;show a HUD
			NVTimer=NVTimer-FPSfactor
			
			If NVTimer<=0.0 Then
				For np.NPCs = Each NPCs
					np\NVX = EntityX(np\Collider,True)
					np\NVY = EntityY(np\Collider,True)
					np\NVZ = EntityZ(np\Collider,True)
				Next
				IsNVGBlinking% = True
				ShowEntity NVBlink%
				If NVTimer<=-10
				NVTimer = 600.0
			EndIf
			EndIf
			
			Color 255,255,255
			
			SetFont Font3
			
			Local plusY% = 0
			If hasBattery=1 Then plusY% = 40
			
			Text GraphicWidth/2,HUDStartY+(20+plusY)*MenuScale,I_Loc\HUD_NvgRefresh,True,False
			
			Text GraphicWidth/2,HUDStartY+(60+plusY)*MenuScale,Max(f2s(NVTimer/60.0,1),0.0),True,False
			Text GraphicWidth/2,HUDStartY+(100+plusY)*MenuScale,I_Loc\HUD_NvgRefreshSeconds,True,False
			
			temp% = CreatePivot() : temp2% = CreatePivot()
			PositionEntity temp, EntityX(Collider), EntityY(Collider), EntityZ(Collider)
			
			Color 255,255,255;*(NVTimer/600.0)
			
			For np.NPCs = Each NPCs
				If np\NVName<>"" And (Not np\HideFromNVG) Then ;don't waste your time if the string is empty
					PositionEntity temp2,np\NVX,np\NVY,np\NVZ
					dist# = EntityDistance(temp2,Collider)
					If dist<23.5 Then ;don't draw text if the NPC is too far away
						PointEntity temp, temp2
						yawvalue# = WrapAngle(EntityYaw(Camera) - EntityYaw(temp))
						xvalue# = 0.0
						If yawvalue > 90 And yawvalue <= 180 Then
							xvalue# = Sin(90)/90*yawvalue
						Else If yawvalue > 180 And yawvalue < 270 Then
							xvalue# = Sin(270)/yawvalue*270
						Else
							xvalue = Sin(yawvalue)
						EndIf
						pitchvalue# = WrapAngle(EntityPitch(Camera) - EntityPitch(temp))
						yvalue# = 0.0
						If pitchvalue > 90 And pitchvalue <= 180 Then
							yvalue# = Sin(90)/90*pitchvalue
						Else If pitchvalue > 180 And pitchvalue < 270 Then
							yvalue# = Sin(270)/pitchvalue*270
						Else
							yvalue# = Sin(pitchvalue)
						EndIf
						
						If (Not IsNVGBlinking%)
						Text GraphicWidth / 2 + xvalue * (GraphicWidth / 2),GraphicHeight / 2 - yvalue * (GraphicHeight / 2),np\NVName,True,True
						Text GraphicWidth / 2 + xvalue * (GraphicWidth / 2),GraphicHeight / 2 - yvalue * (GraphicHeight / 2) + 30.0 * MenuScale,Format(I_Loc\HUD_NvgMeters, f2s(dist,1)),True,True
					EndIf
				EndIf
				EndIf
			Next
			
			SetFont Font1
			
			FreeEntity (temp) : FreeEntity (temp2)
			
			Color 0,0,55
			For k=0 To 10
				Rect HUDStartX+45,GraphicHeight*0.5-(k*20),54,10,True
			Next
			Color 0,0,255
			For l=0 To Floor((power%+50)*0.01)
				Rect HUDStartX+45,GraphicHeight*0.5-(l*20),54,10,True
			Next
			DrawImage NVGImages,HUDStartX+40,GraphicHeight*0.5+30,1
			
			Color 255,255,255
		ElseIf WearingNightVision=1 And hasBattery<>0
			Color 0,55,0
			For k=0 To 10
				Rect HUDStartX+45,GraphicHeight*0.5-(k*20),54,10,True
			Next
			Color 0,255,0
			For l=0 To Floor((power%+50)*0.01)
				Rect HUDStartX+45,GraphicHeight*0.5-(l*20),54,10,True
			Next
			DrawImage NVGImages,HUDStartX+40,GraphicHeight*0.5+30,0
		EndIf
	EndIf
	
	;render sprites
	CameraProjMode ark_blur_cam,2
	CameraProjMode Camera,0
	RenderWorld()
	CameraProjMode ark_blur_cam,0

	If BlinkTimer < - 16 Or BlinkTimer > - 6
		If (WearingNightVision=1 Or WearingNightVision=2) And (hasBattery=1) And ((MilliSecs() Mod 800) < 400) Then
			Color 255,0,0
			SetFont Font3
			
			Text GraphicWidth/2,20*MenuScale,I_Loc\HUD_NvgBatlow,True,False
			Color 255,255,255
			SetFont Font1
		EndIf
	EndIf
End Function


Function ScaleRender(x#,y#,hscale#=1.0,vscale#=1.0)
	If Camera<>0 Then HideEntity Camera
	WireFrame 0
	ShowEntity fresize_image
	ScaleEntity fresize_image,hscale,vscale,1.0
	PositionEntity fresize_image, x, y, 1.0001
	ShowEntity fresize_cam
	RenderWorld()
	HideEntity fresize_cam
	HideEntity fresize_image
	WireFrame WireframeState
	If Camera<>0 Then ShowEntity Camera
End Function

Function InitFastResize()
    ;Create Camera
	Local cam% = CreateCamera()
	CameraProjMode cam, 2
	CameraZoom cam, 0.1
	CameraClsMode cam, 0, 0
	CameraRange cam, 0.1, 1.5
	MoveEntity cam, 0, 0, -10000
	
	fresize_cam = cam
	
    ;ark_sw = GraphicsWidth()
    ;ark_sh = GraphicsHeight()
	
    ;Create sprite
	Local spr% = CreateMesh(cam)
	Local sf% = CreateSurface(spr)
	AddVertex sf, -1, 1, 0, 0, 0
	AddVertex sf, 1, 1, 0, 1, 0
	AddVertex sf, -1, -1, 0, 0, 1
	AddVertex sf, 1, -1, 0, 1, 1
	AddTriangle sf, 0, 1, 2
	AddTriangle sf, 3, 2, 1
	EntityFX spr, 17
	ScaleEntity spr, 2048.0 / Float(RealGraphicWidth), 2048.0 / Float(RealGraphicHeight), 1
	PositionEntity spr, 0, 0, 1.0001
	EntityOrder spr, -100001
	EntityBlend spr, 1
	fresize_image = spr
	
    ;Create texture
	fresize_texture = CreateTexture(2048, 2048, 1+256)
	fresize_texture2 = CreateTexture(2048, 2048, 1+256)
	TextureBlend fresize_texture2,3
	SetBuffer(TextureBuffer(fresize_texture2))
	ClsColor 0,0,0
	Cls
	SetBuffer(BackBuffer())
	;TextureAnisotropy(fresize_texture)
	EntityTexture spr, fresize_texture,0,0
	EntityTexture spr, fresize_texture2,0,1
	
	HideEntity fresize_cam
End Function

;--------------------------------------- Some new 1.3 -functions -------------------------------------------------------

Function UpdateLeave1499()
	Local r.Rooms, it.Items,r2.Rooms,i%
	Local r1499.Rooms
	
	If (Not Wearing1499) And PlayerRoom\RoomTemplate\Name$ = "dimension1499"
		For r.Rooms = Each Rooms
			If r = NTF_1499PrevRoom
				BlinkTimer = -1
				NTF_1499X# = EntityX(Collider)
				NTF_1499Y# = EntityY(Collider)
				NTF_1499Z# = EntityZ(Collider)
				PositionEntity (Collider, NTF_1499PrevX#, NTF_1499PrevY#+0.05, NTF_1499PrevZ#)
				ResetEntity(Collider)
				PlayerRoom = r
				UpdateDoors()
				UpdateRooms()
				If PlayerRoom\RoomTemplate\Name = "room3storage"
					If EntityY(Collider)<-4600*RoomScale
						For i = 0 To 2
							PlayerRoom\NPC[i]\State = 2
							PositionEntity(PlayerRoom\NPC[i]\Collider, EntityX(PlayerRoom\Objects[PlayerRoom\NPC[i]\State2],True),EntityY(PlayerRoom\Objects[PlayerRoom\NPC[i]\State2],True)+0.2,EntityZ(PlayerRoom\Objects[PlayerRoom\NPC[i]\State2],True))
							ResetEntity PlayerRoom\NPC[i]\Collider
							PlayerRoom\NPC[i]\State2 = PlayerRoom\NPC[i]\State2 + 1
							If PlayerRoom\NPC[i]\State2 > PlayerRoom\NPC[i]\PrevState Then PlayerRoom\NPC[i]\State2 = (PlayerRoom\NPC[i]\PrevState-3)
						Next
					EndIf
				ElseIf PlayerRoom\RoomTemplate\Name = "pocketdimension"
					CameraFogColor Camera, 0,0,0
					CameraClsColor Camera, 0,0,0
				EndIf
				For r2.Rooms = Each Rooms
					If r2\RoomTemplate\Name = "dimension1499"
						r1499 = r2
						Exit
					EndIf
				Next
				For it.Items = Each Items
					it\disttimer = 0
					If it\itemtemplate\name = "scp1499" Or it\itemtemplate\name = "super1499"
						If EntityY(it\collider) >= EntityY(r1499\obj)-5
							PositionEntity it\collider,NTF_1499PrevX#,NTF_1499PrevY#+(EntityY(it\collider)-EntityY(r1499\obj)),NTF_1499PrevZ#
							ResetEntity it\collider
							Exit
						EndIf
					EndIf
				Next
				r1499 = Null
				ShouldEntitiesFall = False
				PlaySound_Strict (LoadTempSound("SFX\SCP\1499\Exit.ogg"))
				NTF_1499PrevX# = 0.0
				NTF_1499PrevY# = 0.0
				NTF_1499PrevZ# = 0.0
				NTF_1499PrevRoom = Null
				Exit
			EndIf
		Next
	EndIf
	
End Function

Function CheckForPlayerInFacility()
	;False (=0): NPC is not in facility (mostly meant for "dimension1499")
	;True (=1): NPC is in facility
	;2: NPC is in tunnels (maintenance tunnels/049 tunnels/939 storage room, etc...)
	
	If EntityY(Collider)>100.0
		Return False
	EndIf
	If EntityY(Collider)< -10.0
		Return 2
	EndIf
	If EntityY(Collider)> 7.0 And EntityY(Collider)<=100.0
		Return 2
	EndIf
	
	Return True
End Function

Function IsItemGoodFor1162(itt.ItemTemplates)
	If itt\group = "paper" Then
		;if the item is a paper, only allow spawning it if the name contains the word "note" or "log"
		;(because those are items created recently, which D-9341 has most likely never seen)
		Return ((Not Instr(itt\name, "note")) And (Not Instr(itt\name, "log"))) And (Not Instr(itt\name, "docL")) And itt\name <> "docDan" And itt\name <> "docStrange" And itt\name <> "doc106_2" And itt\name <> "leaflet"
	EndIf
	Select itt\name
		Case "key1", "key2", "key3"
			Return True
		Case "misc", "scp420j", "cigarette"
			Return True
		Case "vest", "finevest","gasmask"
			Return True
		Case "radio","18vradio"
			Return True
		Case "clipboard","eyedrops","redeyedrops","nvgoggles"
			Return True
		Case "drawing"
			If itt\img<>0 Then FreeImage itt\img	
			itt\img = LoadImage_Strict("GFX\items\1048\1048_"+Rand(1,20)+".jpg") ;Gives a random drawing.
			Return True
	End Select
	Return False
End Function

Function ControlSoundVolume()
	Local snd.Sound,i
	
	For snd.Sound = Each Sound
		For i=0 To 31
			;If snd\channels[i]<>0 Then
			;	ChannelVolume snd\channels[i],SFXVolume#
			;Else
				ChannelVolume snd\channels[i],SFXVolume#
			;EndIf
		Next
	Next
	
End Function

Function UpdateDeafPlayer()
	
	If DeafTimer > 0
		DeafTimer = DeafTimer-FPSfactor
		SFXVolume# = 0.0
		If SFXVolume# > 0.0
			ControlSoundVolume()
		EndIf
		DebugLog DeafTimer
	Else
		DeafTimer = 0
		;If SFXVolume# < PrevSFXVolume#
		;	SFXVolume# = Min(SFXVolume# + (0.001*PrevSFXVolume)*FPSfactor,PrevSFXVolume#)
		;	ControlSoundVolume()
		;Else
			SFXVolume# = PrevSFXVolume#
			If DeafPlayer Then ControlSoundVolume()
			DeafPlayer = False
		;EndIf
	EndIf
	
End Function

Function CheckTriggers$()
	Local i%,sx#,sy#,sz#
	Local inside% = -1
	
	If PlayerRoom\TriggerboxAmount = 0
		Return ""
	Else
		For i = 0 To PlayerRoom\TriggerboxAmount-1
			EntityAlpha PlayerRoom\Triggerbox[i],1.0
			sx# = EntityScaleX(PlayerRoom\Triggerbox[i], 1)
			sy# = Max(EntityScaleY(PlayerRoom\Triggerbox[i], 1), 0.001)
			sz# = EntityScaleZ(PlayerRoom\Triggerbox[i], 1)
			GetMeshExtents(PlayerRoom\Triggerbox[i])
			If DebugHUD
				EntityColor PlayerRoom\Triggerbox[i],255,255,0
				EntityAlpha PlayerRoom\Triggerbox[i],0.2
			Else
				EntityColor PlayerRoom\Triggerbox[i],255,255,255
				EntityAlpha PlayerRoom\Triggerbox[i],0.0
 			EndIf
			If EntityX(Collider)>((sx#*Mesh_MinX)+PlayerRoom\x) And EntityX(Collider)<((sx#*Mesh_MaxX)+PlayerRoom\x)
				If EntityY(Collider)>((sy#*Mesh_MinY)+PlayerRoom\y) And EntityY(Collider)<((sy#*Mesh_MaxY)+PlayerRoom\y)
					If EntityZ(Collider)>((sz#*Mesh_MinZ)+PlayerRoom\z) And EntityZ(Collider)<((sz#*Mesh_MaxZ)+PlayerRoom\z)
						inside% = i%
						Exit
					EndIf
				EndIf
			EndIf
		Next
		
		If inside% > -1 Then Return PlayerRoom\TriggerboxName[inside%]
	EndIf
	
End Function

Function ScaledMouseX%()
	Return Float(MouseX()-(RealGraphicWidth*0.5*(1.0-AspectRatioRatio)))*Float(GraphicWidth)/Float(RealGraphicWidth*AspectRatioRatio)
End Function

Function ScaledMouseY%()
	Return Float(MouseY())*Float(GraphicHeight)/Float(RealGraphicHeight)
End Function

Function PlayAnnouncement(file$) ;This function streams the announcement currently playing
	
	If IntercomStreamCHN <> 0 Then
		StopStream_Strict(IntercomStreamCHN) : IntercomStreamCHN = 0
	EndIf
	
	IntercomStreamCHN = StreamSound_Strict(file$,SFXVolume,0)
	
End Function

Function UpdateStreamSounds()
	Local e.Events
	
	If FPSfactor > 0 Then
		If IntercomStreamCHN <> 0 Then
			SetStreamVolume_Strict(IntercomStreamCHN,SFXVolume)
		EndIf
		For e = Each Events
			If e\SoundCHN<>0 Then
				If e\SoundCHN_isStream
					SetStreamVolume_Strict(e\SoundCHN,SFXVolume)
				EndIf
			EndIf
			If e\SoundCHN2<>0 Then
				If e\SoundCHN2_isStream
					SetStreamVolume_Strict(e\SoundCHN2,SFXVolume)
				EndIf
			EndIf
		Next
	EndIf
	
	If (Not PlayerInReachableRoom()) Then
		If PlayerRoom\RoomTemplate\Name <> "exit1" And PlayerRoom\RoomTemplate\Name <> "gatea" Then
			If IntercomStreamCHN <> 0 Then
				StopStream_Strict(IntercomStreamCHN) : IntercomStreamCHN = 0
			EndIf
			If PlayerRoom\RoomTemplate\Name$ <> "dimension1499" Then
				For e = Each Events
					If e\SoundCHN<>0 Then
						If e\SoundCHN_isStream Then
							StopStream_Strict(e\SoundCHN) : e\SoundCHN = 0 : e\SoundCHN_isStream = False
						EndIf
					EndIf
					If e\SoundCHN2<>0 Then
						If e\SoundCHN2_isStream Then
							StopStream_Strict(e\SoundCHN2) : e\SoundCHN2 = 0 : e\SoundCHN2_isStream = False
						EndIf
					EndIf
				Next
			EndIf
		EndIf
	EndIf
	
End Function

Function TeleportEntity(entity%,x#,y#,z#,customradius#=0.3,isglobal%=False,pickrange#=2.0,dir%=0)
	Local pvt,pick
	;dir = 0 - towards the floor (default)
	;dir = 1 - towrads the ceiling (mostly for PD decal after leaving dimension)
	
	pvt = CreatePivot()
	PositionEntity(pvt, x,y+0.05,z,isglobal)
	If dir%=0
		RotateEntity pvt,90,0,0
	Else
		RotateEntity pvt,-90,0,0
	EndIf
	pick = EntityPick(pvt,pickrange)
	If pick<>0
		If dir%=0
			PositionEntity(entity, x,PickedY()+customradius#+0.02,z,isglobal)
		Else
			PositionEntity(entity, x,PickedY()+customradius#-0.02,z,isglobal)
		EndIf
		DebugLog "Entity teleported successfully"
	Else
		PositionEntity(entity,x,y,z,isglobal)
		DebugLog "Warning: no ground found when teleporting an entity"
	EndIf
	FreeEntity pvt
	ResetEntity entity
	DebugLog "Teleported entity to: "+EntityX(entity)+"/"+EntityY(entity)+"/"+EntityZ(entity)
	
End Function

Function PlayMovie(moviefile$)

	Local ScaledGraphicHeight%
	Local Ratio# = Float(RealGraphicWidth)/Float(RealGraphicHeight)
	If Ratio>1.76 And Ratio<1.78
		ScaledGraphicHeight = RealGraphicHeight
		DebugLog "Not Scaled"
	Else
		ScaledGraphicHeight% = Float(RealGraphicWidth)/(16.0/9.0)
		DebugLog "Scaled: "+ScaledGraphicHeight
	EndIf

	Local SplashScreenVideo = OpenMovie(moviefile$+".avi")
	If SplashScreenVideo = 0 Then Return

	DebugLog(RealGraphicHeight)

	Local SplashScreenAudio = StreamSound_Strict(moviefile$+".ogg",SFXVolume,0)
	Repeat
		Cls
		DrawMovie(SplashScreenVideo, 0, (RealGraphicHeight/2-ScaledGraphicHeight/2), RealGraphicWidth, ScaledGraphicHeight)
		Flip
	Until (GetKey() Or (Not IsStreamPlaying_Strict(SplashScreenAudio)))
	StopStream_Strict(SplashScreenAudio)
	CloseMovie(SplashScreenVideo)
	
	Cls
	Flip

End Function

Function PlayStartupVideos()

	If GetOptionInt("general","play startup video") = 0 Lor IsRestart Then Return

	PlayMovie("GFX\menu\startup_Undertow")
	PlayMovie("GFX\menu\startup_TSS")

End Function

Function CanUseItem(canUseWithHazmat%, canUseWithGasMask%, canUseWithEyewear%)
	If (canUseWithHazmat = False And WearingHazmat) Then
		Msg = I_Loc\MessageItem_HazmatNouse
		MsgTimer = 70*5
		Return False
	ElseIf (canUseWithGasMask = False And (WearingGasMask Or Wearing1499))
		Msg = I_Loc\MessageItem_GasmaskNouse
		MsgTimer = 70*5
		Return False
	ElseIf (canUseWithEyewear = False And (WearingNightVision))
		Msg = I_Loc\MessageItem_NvgNouse
		MsgTimer = 70*5
		Return False
	EndIf
	
	Return True
End Function

Function ResetInput()
	
	FlushKeys()
	FlushMouse()
	MouseHit1 = 0
	MouseHit2 = 0
	MouseDown1 = 0
	MouseUp1 = 0
	MouseHit(1)
	MouseHit(2)
	MouseDown(1)
	GrabbedEntity = 0
	Input_ResetTime# = 10.0
	
End Function

Function Update096ElevatorEvent#(e.Events,EventState#,d.Doors,elevatorobj%)
	Local prevEventState# = EventState#
	
	If EventState < 0 Then
		EventState = 0
		prevEventState = 0
	EndIf
	
	If d\openstate = 0 And d\open = False Then
		If Abs(EntityX(Collider)-EntityX(elevatorobj%,True))<=280.0*RoomScale+(0.015*FPSfactor) Then
			If Abs(EntityZ(Collider)-EntityZ(elevatorobj%,True))<=280.0*RoomScale+(0.015*FPSfactor) Then
				If Abs(EntityY(Collider)-EntityY(elevatorobj%,True))<=280.0*RoomScale+(0.015*FPSfactor) Then
					d\locked = True
					If EventState = 0 Then
						TeleportEntity(Curr096\Collider,EntityX(d\frameobj),EntityY(d\frameobj)+1.0,EntityZ(d\frameobj),Curr096\CollRadius)
						PointEntity Curr096\Collider,elevatorobj
						RotateEntity Curr096\Collider,0,EntityYaw(Curr096\Collider),0
						MoveEntity Curr096\Collider,0,0,-0.5
						ResetEntity Curr096\Collider
						Curr096\State = 6
						SetNPCFrame(Curr096,0)
						e\Sound = LoadSound_Strict("SFX\SCP\096\ElevatorSlam.ogg")
						EventState = EventState + FPSfactor * 1.4
					EndIf
				EndIf
			EndIf
		EndIf
	EndIf
	
	If EventState > 0 Then
		If prevEventState = 0 Then
			e\SoundCHN = PlaySound_Strict(e\Sound)
		EndIf
		
		If EventState > 70*1.9 And EventState < 70*2+FPSfactor
			CameraShake = 7
		ElseIf EventState > 70*4.2 And EventState < 70*4.25+FPSfactor
			CameraShake = 1
		ElseIf EventState > 70*5.9 And EventState < 70*5.95+FPSfactor
			CameraShake = 1
		ElseIf EventState > 70*7.25 And EventState < 70*7.3+FPSfactor
			CameraShake = 1
			d\fastopen = True
			d\open = True
			Curr096\State = 4
			Curr096\LastSeen = 1
		ElseIf EventState > 70*8.1 And EventState < 70*8.15+FPSfactor
			CameraShake = 1
		EndIf
		
		If EventState <= 70*8.1 Then
			d\openstate = Min(d\openstate,20)
		EndIf
		EventState = EventState + FPSfactor * 1.4
	EndIf
	Return EventState
	
End Function





;~IDEal Editor Parameters:
;~F#39#D8#DCD#162D#242C#2B2A
;~B#11E0#145E#1C07
;~C#Blitz3D