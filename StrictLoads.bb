; ID: 2975
; Author: RifRaf, further modified by MonocleBios
; Date: 2012-09-11 11:44:22
; Title: Safe Loads (b3d) ;strict loads sounds more appropriate IMO
; Description: Get the missing filename reported

;safe loads for mav trapping media issues


Const ImageExtensionCount = 2
Global ImageExtensions$[ImageExtensionCount]
ImageExtensions[0] = "png"
ImageExtensions[1] = "jpg"

Const ModelExtensionCount = 5
Global ModelExtensions$[ModelExtensionCount]
ModelExtensions[0] = "b3d"
ModelExtensions[1] = "x"
ModelExtensions[2] = "fbx"
ModelExtensions[3] = "glb"
ModelExtensions[4] = "obj"

Const SoundExtensionCount = 3
Global SoundExtensions$[SoundExtensionCount]
SoundExtensions[0] = "ogg"
SoundExtensions[1] = "wav"
SoundExtensions[2] = "mp3"

;basic wrapper functions that check to make sure that the file exists before attempting to load it, raises an RTE if it doesn't
;more informative alternative to MAVs outside of debug mode, makes it immiediately obvious whether or not someone is loading resources
;likely to cause more crashes than 'clean' CB, as this prevents anyone from loading any assets that don't exist, regardless if they are ever used
;added zero checks since blitz load functions return zero sometimes even if the filetype exists
Function LoadImage_Strict%(file$, scale#=0, flags%=0)
	Local ext$ = File_GetExtension(file)
	Local fileNoExt$ = Left(file, Len(file) - Len(ext))
	Local tmp%

	For m.ActiveMods = Each ActiveMods
		For i = 0 To ImageExtensionCount
			Local usedExtension$
			If i = ImageExtensionCount Then
				usedExtension = ext
			Else
				usedExtension = ImageExtensions[i]
			EndIf
			Local modPath$ = m\Path + fileNoExt + usedExtension
			If FileType(modPath) = 1 Then
				tmp = LoadImage(modPath, flags)
				If tmp <> 0 Then
					If scale <> 0 Then ScaleImageFromFile(tmp, m\Path + fileNoExt, scale)
					Return tmp
				Else
					RuntimeErrorExt("Failed to load image " + Chr(34) + modPath + Chr(34) + ".")
				EndIf
			EndIf
		Next
	Next

	If FileType(file$) = 1 Then
		tmp = LoadImage(file$, flags)
		If scale <> 0 Then ScaleImageFromFile(tmp, fileNoExt, scale)
		Return tmp
	Else
		For i = 0 To ImageExtensionCount-1
			usedExtension$ = ImageExtensions[i]
			Local path$ = fileNoExt + usedExtension
			If FileType(path) = 1 Then
				tmp = LoadImage(path, flags)
				If scale <> 0 Then ScaleImageFromFile(tmp, fileNoExt, scale)
				Return tmp
			EndIf
		Next
	EndIf

	RuntimeErrorExt "Image " + Chr(34) + file$ + Chr(34) + " missing."
End Function

Function ScaleImageFromFile(img%, path$, scale#)
	path = path + "SCALE"
	If FileType(path) = 1 Then
		Local f% = OpenFile(path)
		Local val# = Float(ReadLine(f))
		CloseFile f
		scale = scale * val
	EndIf
	If scale <> 1.0 Then
		ScaleImage img, scale, scale
	EndIf
End Function


Type Sound
	Field internalHandle%
	Field name$
	Field channels%[32]
	Field releaseTime%
End Type

Function AutoReleaseSounds()
	Local snd.Sound
	For snd.Sound = Each Sound
		Local tryRelease% = True
		For i = 0 To 31
			If snd\channels[i] <> 0 Then
				If ChannelPlaying(snd\channels[i]) Then
					tryRelease = False
					snd\releaseTime = MilliSecs()+5000
					Exit
				EndIf
			EndIf
		Next
		If tryRelease Then
			If snd\releaseTime < MilliSecs() Then
				If snd\internalHandle <> 0 Then
					FreeSound snd\internalHandle
					snd\internalHandle = 0
				EndIf
			EndIf
		EndIf
	Next
End Function

Function PlaySound_Strict%(sndHandle%)
	Local snd.Sound = Object.Sound(sndHandle)
	If snd <> Null Then
		Local shouldPlay% = True
		For i = 0 To 31
			If snd\channels[i] = 0 Lor (Not ChannelPlaying(snd\channels[i])) Then
				If snd\internalHandle = 0 Then
					Local usedPath$ = DetermineModdedSoundPath(snd\name)
					If FileType(usedPath) <> 1 Then
						CreateConsoleMsg("Sound " + Chr(34) + snd\name + Chr(34) + " not found.")
						If ConsoleOpening
							ConsoleOpen = True
						EndIf
					Else
						If EnableSFXRelease Then snd\internalHandle = LoadSound(usedPath)
					EndIf
						
					If snd\internalHandle = 0 Then
						CreateConsoleMsg("Failed to load Sound: " + Chr(34) + snd\name + Chr(34))
						If ConsoleOpening
							ConsoleOpen = True
						EndIf
					EndIf
				EndIf
				If ConsoleFlushSnd Then
					snd\channels[i] = PlaySound(ConsoleFlushSnd)
				Else
					snd\channels[i] = PlaySound(snd\internalHandle)
				EndIf
				ChannelVolume snd\channels[i],SFXVolume#
				QueueSubtitle(snd\name, snd\internalHandle, snd\channels[i])
				snd\releaseTime = MilliSecs()+5000 ;release after 5 seconds
				Return snd\channels[i]
			EndIf
		Next
	EndIf
	
	Return 0
End Function

Function DetermineModdedSoundPath$(File$)
	Local ext$ = File_GetExtension(File)
	Local fileNoExt$ = Left(File, Len(File) - Len(ext))
	Local tmp%

	For m.ActiveMods = Each ActiveMods
		If (Not m\IsLocale) Lor UsesDubbedAudio Then
			For i = 0 To SoundExtensionCount
				Local usedExtension$
				If i = SoundExtensionCount Then
					usedExtension = ext
				Else
					usedExtension = SoundExtensions[i]
				EndIf
				Local modPath$ = m\Path + fileNoExt + usedExtension
				If FileType(modPath) = 1 Then
					Return modPath
				EndIf
			Next
		EndIf
	Next

	Return File
End Function

Function LoadSound_Strict(file$)
	Local snd.Sound = New Sound
	snd\name = file
	snd\internalHandle = 0
	snd\releaseTime = 0
	If (Not EnableSFXRelease) Then
		If snd\internalHandle = 0 Then 
			snd\internalHandle = LoadSound(DetermineModdedSoundPath(snd\name))
		EndIf
	EndIf
	
	Return Handle(snd)
End Function

Function FreeSound_Strict(sndHandle%)
	Local snd.Sound = Object.Sound(sndHandle)
	If snd <> Null Then
		If snd\internalHandle <> 0 Then
			RemoveQueuedSubtitle(snd\internalHandle)
			FreeSound snd\internalHandle
			snd\internalHandle = 0
		EndIf
		Delete snd
	EndIf
End Function

Type Stream
	Field chn%
End Type

Function StreamSound_Strict(file$,volume#=1.0,custommode=2)
	Local vanillaFile$ = file
	file = DetermineModdedSoundPath(file)
	If FileType(file$)<>1
		CreateConsoleMsg("Sound " + Chr(34) + file$ + Chr(34) + " not found.")
		If ConsoleOpening
			ConsoleOpen = True
		EndIf
		Return 0
	EndIf
	
	Local st.Stream = New Stream
	
	st\chn = PlayMusic(file$,custommode)
	
	If st\chn = -1
		CreateConsoleMsg("Failed to stream Sound (returned -1): " + Chr(34) + file$ + Chr(34))
		If ConsoleOpening
			ConsoleOpen = True
		EndIf
		Return -1
	EndIf
	QueueSubtitle(vanillaFile, 0, st\chn, True)
	UpdateChannelVolumeWithSubtitles(st\chn, volume, True, False)
	Return Handle(st)
End Function

Function StopStream_Strict(streamHandle%)
	Local st.Stream = Object.Stream(streamHandle)
	
	If st = Null
		CreateConsoleMsg("Failed to stop stream Sound: Unknown Stream")
		Return
	EndIf
	If st\chn=0 Or st\chn=-1
		CreateConsoleMsg("Failed to stop stream Sound: Return value "+st\chn)
		Return
	EndIf
	StopChannel(st\chn)
	RemoveQueuedSubtitleByChannel(st\chn, True)
	Delete st
	
End Function

Function SetStreamVolume_Strict(streamHandle%,volume#,isSFX%=False)
	Local st.Stream = Object.Stream(streamHandle)
	
	If st = Null
		CreateConsoleMsg("Failed to set stream Sound volume: Unknown Stream")
		Return
	EndIf
	If st\chn=0 Or st\chn=-1
		CreateConsoleMsg("Failed to set stream Sound volume: Return value "+st\chn)
		Return
	EndIf
	
	UpdateChannelVolumeWithSubtitles(st\chn, volume, True, isSFX)
	
End Function

Function SetStreamPaused_Strict(streamHandle%,paused%)
	Local st.Stream = Object.Stream(streamHandle)
	
	If st = Null
		CreateConsoleMsg("Failed to pause/unpause stream Sound: Unknown Stream")
		Return
	EndIf
	If st\chn=0 Or st\chn=-1
		CreateConsoleMsg("Failed to pause/unpause stream Sound: Return value "+st\chn)
		Return
	EndIf
	
	If paused Then
		PauseChannel(st\chn)
	Else
		ResumeChannel(st\chn)
	EndIf
	SetQueuedSubtitlePause(st\chn, paused)
	
End Function

Function IsStreamPlaying_Strict(streamHandle%)
	Local st.Stream = Object.Stream(streamHandle)
	
	If st = Null
		CreateConsoleMsg("Failed to find stream Sound: Unknown Stream")
		Return
	EndIf
	If st\chn=0 Or st\chn=-1
		CreateConsoleMsg("Failed to find stream Sound: Return value "+st\chn)
		Return
	EndIf
	
	Return ChannelPlaying(st\chn)
	
End Function

Function SetStreamPan_Strict(streamHandle%,pan#)
	Local st.Stream = Object.Stream(streamHandle)
	
	If st = Null
		CreateConsoleMsg("Failed to find stream Sound: Unknown Stream")
		Return
	EndIf
	If st\chn=0 Or st\chn=-1
		CreateConsoleMsg("Failed to find stream Sound: Return value "+st\chn)
		Return
	EndIf
	
	;-1 = Left
	;0 = Middle
	;1 = Right
	ChannelPan(st\chn,pan)
	
End Function

Function UpdateStreamSoundOrigin(streamHandle%,cam%,entity%,range#=10,volume#=1.0)
	If streamHandle <> 0 Then
		If IsStreamPlaying_Strict(streamHandle) Then
			;Local st.Stream = Object.Stream(streamHandle)
			range# = Max(range,1.0)
			
			If volume>0 Then
				
				Local dist# = EntityDistance(cam, entity) / range#
				If 1 - dist# > 0 And 1 - dist# < 1 Then
					
					Local panvalue# = Sin(-DeltaYaw(cam,entity))
					
					SetStreamVolume_Strict(streamHandle,volume#*(1-dist#), True)
					SetStreamPan_Strict(streamHandle,panvalue)
				Else
					SetStreamVolume_Strict(streamHandle,0.0)
				EndIf
			Else
				SetStreamVolume_Strict(streamHandle,0.0)
			EndIf
		EndIf
	EndIf
End Function

Function LoadMesh_Strict(File$,parent=0)
	Local tmp% = LoadModdedMeshNonStrict(File, parent)
	If tmp <> 0 Then Return tmp

	Local err$
	If FileType(File$) <> 1 Then err = "3D Mesh " + File$ + " not found." Else err = "Failed to load 3D Mesh: " + File$ + "."
	RuntimeErrorExt(err)
End Function

Function LoadAnimMesh_Strict(File$,parent=0)
	Local ext$ = File_GetExtension(File)
	Local fileNoExt$ = Left(File, Len(File) - Len(ext))
	Local tmp%

	For m.ActiveMods = Each ActiveMods
		For i = 0 To ModelExtensionCount
			Local usedExtension$
			If i = ModelExtensionCount Then
				usedExtension = ext
			Else
				usedExtension = ModelExtensions[i]
			EndIf
			Local modPath$ = m\Path + fileNoExt + usedExtension
			If FileType(modPath) = 1 Then
				tmp = LoadAnimMesh(modPath, parent)
				If tmp <> 0 Then
					Return tmp
				Else
					RuntimeErrorExt("Failed to load 3D Animated Mesh " + Chr(34) + modPath + Chr(34) + ".")
				EndIf
			EndIf
		Next
	Next

	If FileType(File$) <> 1 Then RuntimeErrorExt "3D Animated Mesh " + File$ + " not found."
	tmp = LoadAnimMesh(File$, parent)
	If tmp = 0 Then RuntimeErrorExt "Failed to load 3D Animated Mesh: " + File$ 
	Return tmp
End Function

;don't use in LoadRMesh, as Reg does this manually there. If you wanna fuck around with the logic in that function, be my guest 
Function LoadTexture_Strict(File$,flags=1)
	Local tmp% = LoadModdedTextureNonStrict(File, flags)
	If tmp <> 0 Then Return tmp

	Local err$
	If FileType(File$) <> 1 Then err = "Texture " + File$ + " not found." Else err = "Failed to load Texture: " + File$ + "."
	RuntimeErrorExt(err)
End Function

Function LoadAnimTexture_Strict(File$,flags%,columns%,rows%,start%,count%)
	Local ext$ = File_GetExtension(File)
	Local fileNoExt$ = Left(File, Len(File) - Len(ext))
	Local tmp%

	For m.ActiveMods = Each ActiveMods
		For i = 0 To ImageExtensionCount
			Local usedExtension$
			If i = ImageExtensionCount Then
				usedExtension = ext
			Else
				usedExtension = ImageExtensions[i]
			EndIf
			Local modPath$ = m\Path + fileNoExt + usedExtension
			If FileType(modPath) = 1 Then
				tmp = LoadAnimTextureGrid(modPath, flags, columns, rows, start, count)
				If tmp <> 0 Then
					Return tmp
				Else If DebugResourcePacks Then
					RuntimeErrorExt("Failed to load animated texture " + Chr(34) + modPath + Chr(34) + ".")
				EndIf
			EndIf
		Next
	Next

	Return LoadAnimTextureGrid(file, flags, columns, rows, start, count)
End Function

Function LoadBrush_Strict(file$,flags,u#=1.0,v#=1.0)
	File = DetermineModdedPath(File)
	If FileType(file$)<>1 Then RuntimeErrorExt "Brush Texture " + file$ + "not found."
	tmp = LoadBrush(file$, flags, u, v)
	If tmp = 0 Then RuntimeErrorExt "Failed to load Brush: " + file$ 
	Return tmp 
End Function 

Function LoadFont_Strict(file$, height, bold%=False, italic%=False)
	File = DetermineModdedPath(File)
	If FileType(file$)<>1 Then RuntimeErrorExt "Font " + file$ + " not found."
	tmp = LoadFont(file, height, bold, italic)
	If tmp = 0 Then RuntimeErrorExt "Failed to load Font: " + file$ 
	Return tmp
End Function

Function LoadEffect_Strict%(file$)
	file = DetermineModdedPath(file)
	If FileType(file$)<>1 Then RuntimeErrorExt "Shader " + file$ + " not found."
	tmp = LoadEffect(file)
	If tmp = 0 Then RuntimeErrorExt "Failed to load Shader: " + file$ + ". Error: " + GetEffectError() + Chr(10)
	Return tmp
End Function









;~IDEal Editor Parameters:
;~F#F#34#3B
;~C#Blitz3D