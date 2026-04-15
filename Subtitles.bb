Const SubtitleFilePath$ = "Data\subtitles.ini"
Const DubbedSubtitleFilePath$ = "Data\subtitles_dub.ini"
Const ClosedCaptionFilePath$ = "Data\subtitles_captions.ini"
Const VoiceFilePath$ = "Data\voices.ini"

Global Font1Bold% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(19 * MenuScale), True, False)
Global Font1Italic% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(19 * MenuScale), False, True)
Global Font1BoldItalic% = LoadFont_Strict("GFX\font\cour\Courier New.ttf", Int(19 * MenuScale), True, True)

Global SubtitleTextHeight# = Int(19 * MenuScale)*1.5
Global SubtitleEarlyDelay# = 0.15
Global SubtitleFadeLength# = 0.1

Global SubtitlesInitialized% = False

Const SubtitleCacheSize% = 15
Dim SubtitleCacheKey$(SubtitleCacheSize)
Dim SubtitleCacheToken%(SubtitleCacheSize)
Global SubtitleCacheNext% = 0

Type SubtitleBox
	field screenWidth#
	field screenLeft#
	field screenTop#

	field curTop#
	field curHeight#

	field targetTop#
	field targetHeight#
	field alpha#

	field lines%

	field cam%
	field sprite%
End Type
Global SubBox.SubtitleBox


Type SubtitleToken ; ~ A subtitle definition, contains the beginning of a linked list of SubtitleEntry.
	field soundPath$
	field fromFile% ; Bit 0 = From Subtitles File, Bit 1 = From Captions File, Bit 2 = From Dubbed Subtitles File

	field entry.SubtitleEntry
End Type

Type SubtitleEntry ; ~ A single subtitle line. Contains a timing and duration.
	field nextEntry.SubtitleEntry

	field txt$

	field col.SubtitleColor

	field entryType%
	field time#, length# 

	field cooldown#
End Type

Type SubtitleColor ; ~ Settings for subtitle lines. Creates instances for voice definitions and inline settings. Duplicates removed.
	field voiceKey$

	field name$
	field r%, g%, b%
	field isItalic%, isBold%
	field cooldownLength#
	field minVolume#
End Type

Type QueuedSubtitleMsg ; ~ Queued up subtitle. Uses a millisecond timestamp to prevent desync.
	field token.SubtitleToken
	field entry.SubtitleEntry

	field sndHandle%, chn%, isStream%
	field volume#
	field paused%

	field timeStart#
End Type

Type SubtitleMsg ; ~ Subtitle that is rendered on screen.
	field entry.SubtitleEntry

	field txt$

	field curYPos#
	field timeLeft#
End Type


; ----- DATA / CONFIG -----


Function SubtitleStripComments$(linestr$)
	Local strtemp$ = ""
	For i = 1 To Len(linestr)
		If Mid(linestr, i, 1)=";" Or Mid(linestr, i, 1)="#" Then ; Really hacky escape codes but I figure add these since some characters might be part of subtitle text
			If i = 1 Or (i > 1 And Mid(linestr, i-1, 1)<>"\") Exit
			strtemp = Left(strtemp, Len(strtemp)-1)
		EndIf
		strtemp = strtemp + Mid(linestr, i, 1)
	Next

	Return strtemp
End Function


Function LoadSubtitleVoices(path$)
	Local f% = OpenFile(path)

	local section$ = ""
	Local v.SubtitleColor
	While Not Eof(f)
		Local linestr$ = ReadLine(f)
		Local strtemp$ = Trim(SubtitleStripComments(linestr))

		Local equal% = Instr(strtemp, "=")

		If Left(strtemp, 1) = "[" And Right(strtemp, 1) = "]" And Len(strtemp)>2 Then
			section = Lower(Mid(strtemp, 2, Len(strtemp)-2))

			; Does this voice already exist?
			For clr.SubtitleColor = Each SubtitleColor 
				If clr\voiceKey = section Then
					section = ""
					Exit
				EndIf
			Next

			If section <> "" Then
				v.SubtitleColor = new SubtitleColor
				v\voiceKey = section
				v\r = 255
				v\g = 255
				v\b = 255
			EndIf
		ElseIf section<>"" And equal<>0 Then
			Local key$ = Lower(Trim(Left(strtemp, equal-1)))
			Local value$ = Trim(Right(strtemp, Len(strtemp)-equal))

			ApplySubtitleColorSetting(key, value, v)
		EndIf
	Wend

	CloseFile(f)
End Function

Function LoadSubtitleTokens(path$, tokenType)
	Local f% = OpenFile(path)

	Local section$ = ""
	Local used% = True
	Local c.SubtitleColor = Null ; Token color settings

	Local firstEntry.SubtitleEntry
	Local lastEntry.SubtitleEntry
	While Not Eof(f)
		Local linestr$ = ReadLine(f)
		Local strtemp$ = Trim(SubtitleStripComments(linestr))

		Local equal% = Instr(strtemp, "=")

		If Left(strtemp, 1) = "[" And Right(strtemp, 1) = "]" And Len(strtemp)>2 Then
			If section <> "" And firstEntry <> Null Then
				CreateSubtitleToken(firstEntry, section, tokenType)
				firstEntry = Null
				lastEntry = Null
			EndIf

			section = Lower(Mid(strtemp, 2, Len(strtemp)-2))

			If Not used Then Delete c

			used = False
			c = new SubtitleColor
			CopySubtitleColor(FindOrAddSubtitleVoice("default"), c)

		ElseIf section<>"" And equal<>0 Then
			Local key$ = Trim(Left(strtemp, equal-1))
			Local value$ = Trim(Right(strtemp, Len(strtemp)-equal))

			If Not ApplySubtitleColorSetting(key, value, c) Then
				Local e.SubtitleEntry = CreateSubtitleEntry(key, value, c, tokenType)
				If e\col = c Then used = True ; If the same color gets returned, this color is now being referenced directly by an entry and should not be deleted.
				If firstEntry = Null Then firstEntry = e
				If lastEntry <> Null Then lastEntry\nextEntry = e
				lastEntry = e
			EndIf
		EndIf
	Wend

	If section <> "" And firstEntry <> Null Then
		CreateSubtitleToken(firstEntry, section, tokenType)
		firstEntry = Null
		lastEntry = Null
	EndIf

	CloseFile(f)
End Function


Function CreateSubtitleEntry.SubtitleEntry(key$, value$, clr.SubtitleColor, tokenType)
	Local e.SubtitleEntry = new SubtitleEntry

	e\time = Float(key)
	e\length = (5.0+1.0) * 70.0

	e\col = clr
	e\txt = ParseSubtitleSettings(e, value, tokenType=1)

	; If not explicitly defined as a caption, apply based on load type.
	If e\entryType <> 1 Then e\entryType = tokenType

	Insert e Before First SubtitleEntry
	Return e
End Function

Function CreateSubtitleToken(entry.SubtitleEntry, soundPathGroup$, tokenType)
	Local offset% = 1
	Local toChar% = 0

	While offset <= Len(soundPathGroup)
		toChar = Instr(soundPathGroup, "|", offset)
		If toChar = 0 Then toChar = Len(soundPathGroup)+1

		Local soundPath$ = Trim(Mid(soundPathGroup, offset, toChar-offset))
		Local t.SubtitleToken = GetSubtitleToken(soundPath)

		If t <> Null And ((t\fromFile And (1 Shl tokenType)) <> 0) Then 
			DebugLog("Token already exists: "+soundPath)
		Else
			If t = Null Then
				t = New SubtitleToken

				t\soundPath = soundPath

				t\entry = entry

				Insert t Before First SubtitleToken

				t\fromFile = 1 Shl tokenType
			Else
				; Append entry to existing linked list
				Local e.SubtitleEntry = t\entry
				While e\nextEntry <> Null
					e = e\nextEntry
				Wend

				e\nextEntry = entry

				t\fromFile = t\fromFile Or (1 Shl tokenType)

				; All tokens in a group share the same entry. This means that appending to the first one is sufficient.
				; Appending to the others would actually create a loop in the linked list.
				Exit
			EndIf
		EndIf

		offset = toChar+1
	Wend
End Function


Function GetSubtitleToken.SubtitleToken(soundPath$)
	For token.SubtitleToken = Each SubtitleToken
		If token\soundPath = soundPath Then
			Return token
		EndIf
	Next

	Return Null 
End Function


; Example of inline Subtitle settings: <r=255,g=255,b=255,voice=classd,italic=true,bold=true,caption=true>
Function ParseSubtitleSettings$(e.SubtitleEntry, txt$, isCaption%=False)
	; If the string doesn't begin with settings definitions, ignore.
	If Mid(txt, 1, 1) <> "<" Or Instr(txt, ">") = 0 Then
		Return txt
	EndIf

	Local c.SubtitleColor = new SubtitleColor
	CopySubtitleColor(e\col, c, True)

	Local parsingKey% = True 
	Local key$ = ""
	Local value$ = ""

	Local temp%
	Local char$ = ""
	For temp=2 To Len(txt)
		char = Mid(txt, temp, 1)

		If char = ">" Then
			If (Not parsingKey) Then
				key = Trim(Lower(key))
				value = Trim(Lower(value))
				ApplySubtitleDataSetting(key, value, e)
				ApplySubtitleColorSetting(key, value, c)
			EndIf

			e\col = InternSubtitleColor(c)

			Return Right(txt, Len(txt)-temp)
		ElseIf parsingKey Then
			If char = "=" Then
				parsingKey = False
			Else
				key = key + char
			EndIf
		Else
			If char = "," Then
				key = Trim(Lower(key))
				value = Trim(Lower(value))
				ApplySubtitleDataSetting(key, value, e)
				ApplySubtitleColorSetting(key, value, c)

				parsingKey = True
				key = ""
				value = ""
			Else
				value = value + char
			EndIf
		EndIf
	Next

	e\col = InternSubtitleColor(c)
	Return txt
End Function

Function ApplySubtitleDataSetting%(key$, value$, e.SubtitleEntry)
	If key = "length" Then
		e\length = (Float(value)+1.0) * 70.0
		Return True
	EndIf

	If key = "caption" Then
		If value = "true" Then
			e\entryType = 1
		ElseIf value = "false" Then
			e\entryType = 0
		EndIf
		Return True
	EndIf

	Return False
End Function

Function ApplySubtitleColorSetting%(key$, value$, c.SubtitleColor)
	If key = "voice" Then
		For clr.SubtitleColor = Each SubtitleColor
			If clr\voiceKey = value And clr <> c Then
				CopySubtitleColor(clr, c, False, False)
			EndIf
		Next

		Return True
	EndIf

	If key = "name" Then c\name = value : Return True

	If key = "r" Then c\r = Int(value) : Return True
	If key = "g" Then c\g = Int(value) : Return True
	If key = "b" Then c\b = Int(value) : Return True
	
	If key = "bold" Then
		If value = "true" Then
			c\isBold = True
		ElseIf value = "false" Then
			c\isBold = False
		EndIf

		Return True
	EndIf
	If key = "italic" Then
		If value = "true" Then
			c\isItalic = True
		ElseIf value = "false" Then
			c\isItalic = False
		EndIf

		Return True
	EndIf

	If key = "minvolume" Then c\minVolume = Float(value) : Return True
	If key = "cooldown" Then c\cooldownLength = Float(value) : Return True

	Return False
End Function


Function FindOrAddSubtitleVoice.SubtitleColor(voice$)
	For clr.SubtitleColor = Each SubtitleColor
		If clr\voiceKey = voice Then
			Return clr
		EndIf
	Next

	Local c.SubtitleColor = new SubtitleColor
	c\voiceKey = voice
	c\r = 255
	c\g = 255
	c\b = 255
	Return c
End Function

Function CopySubtitleColor(fromColor.SubtitleColor, toColor.SubtitleColor, keepVoiceKey%=False, keepBools%=True)
	If keepVoiceKey Then toColor\voiceKey = fromColor\voiceKey
	toColor\name = fromColor\name
	toColor\r = fromColor\r
	toColor\g = fromColor\g
	toColor\b = fromColor\b
	If keepBools Or fromColor\isItalic Then toColor\isItalic = fromColor\isItalic
	If keepBools Or fromColor\isBold Then toColor\isBold = fromColor\isBold
	toColor\minVolume = fromColor\minVolume
	toColor\cooldownLength = fromColor\cooldownLength
End Function

Function SubtitleColorsMatch%(firstColor.SubtitleColor, secondColor.SubtitleColor)
	If firstColor\name <> secondColor\name Then Return False
	If firstColor\r <> secondColor\r Then Return False
	If firstColor\g <> secondColor\g Then Return False
	If firstColor\b <> secondColor\b Then Return False
	If firstColor\isItalic <> secondColor\isItalic Then Return False
	If firstColor\isBold <> secondColor\isBold Then Return False
	If firstColor\minVolume <> secondColor\minVolume Then Return False
	If firstColor\cooldownLength <> secondColor\cooldownLength Then Return False
	
	Return True
End Function

Function InternSubtitleColor.SubtitleColor(c.SubtitleColor)
	If c = Null Then
		Return Null
	EndIf

	For clr.SubtitleColor = Each SubtitleColor
		If c <> clr And SubtitleColorsMatch(c, clr) Then
			Delete c
			Return clr
		EndIf
	Next

	Return c
End Function

Function CleanupColors()
	Local c% = 0

	For clr.SubtitleColor = Each SubtitleColor
		Local referenced% = False
		For e.SubtitleEntry = Each SubtitleEntry
			If e\col = clr Then
				referenced = True
				Exit
			EndIf
		Next

		If Not referenced Then
			Delete clr
			c = c + 1
		EndIf
	Next

	DebugLog("Cleaned up "+Str(c)+" unused colors.")
End Function


; ----- SETUP -----


Function LoadSubtitles()
	SubtitlesInitialized = False

	Delete Each SubtitleToken
	Delete Each SubtitleEntry
	Delete Each SubtitleColor
	Dim SubtitleCacheKey$(SubtitleCacheSize)
	Dim SubtitleCacheToken%(SubtitleCacheSize)
	SubtitleCacheNext = 0

	DebugLog "Loading Subtitles"

	; Load all voices first so entries can reference them
	Local voicesUnlocked% = True
	For m.ActiveMods = Each ActiveMods
		If voicesUnlocked And FileType(m\Path + VoiceFilePath) = 1 Then
			LoadSubtitleVoices(m\Path + VoiceFilePath)
			If FileType(m\Path + VoiceFilePath + ".OVERRIDE") = 1 Then voicesUnlocked = False : Exit
		EndIf
	Next
	If voicesUnlocked Then LoadSubtitleVoices(VoiceFilePath)

	Local subtitlesUnlocked% = True
	Local captionsUnlocked% = True
	Local dubbedSubtitlesUnlocked% = True
	For m.ActiveMods = Each ActiveMods
		If subtitlesUnlocked And FileType(m\Path + SubtitleFilePath) = 1 Then
			LoadSubtitleTokens(m\Path + SubtitleFilePath, 0)
			If FileType(m\Path + SubtitleFilePath + ".OVERRIDE") = 1 Then subtitlesUnlocked = False
		EndIf

		If captionsUnlocked And FileType(m\Path + ClosedCaptionFilePath) = 1 Then
			LoadSubtitleTokens(m\Path + ClosedCaptionFilePath, 1)
			If FileType(m\Path + ClosedCaptionFilePath + ".OVERRIDE") = 1 Then captionsUnlocked = False
		EndIf

		If dubbedSubtitlesUnlocked And FileType(m\Path + DubbedSubtitleFilePath) = 1 Then
			LoadSubtitleTokens(m\Path + DubbedSubtitleFilePath, 2)
			If FileType(m\Path + DubbedSubtitleFilePath + ".OVERRIDE") = 1 Then dubbedSubtitlesUnlocked = False
		EndIf

		If (Not subtitlesUnlocked) And (Not captionsUnlocked) And (Not dubbedSubtitlesUnlocked) Then Exit
	Next

	If subtitlesUnlocked Then LoadSubtitleTokens(SubtitleFilePath, 0)
	If captionsUnlocked Then LoadSubtitleTokens(ClosedCaptionFilePath, 1)
	; Dubbed subtitles don't exist for the base game.

	CleanupColors()

	Local tokenCount% = 0
	Local entryCount% = 0
	Local colorCount% = 0
	For t.SubtitleToken = Each SubtitleToken tokenCount = tokenCount + 1 Next
	For e.SubtitleEntry = Each SubtitleEntry entryCount = entryCount + 1 Next
	For c.SubtitleColor = Each SubtitleColor colorCount = colorCount + 1 Next

	DebugLog "Tokens: "+Str(tokenCount)
	DebugLog "Entries: "+Str(entryCount)
	DebugLog "Colors: "+Str(colorCount)

	SubtitlesInitialized = True
	DebugLog "Loaded All Subtitles"
End Function

Function LoadSubtitleEntities()
	Delete Each QueuedSubtitleMsg
	Delete Each SubtitleMsg

	Delete SubBox
	SubBox = new SubtitleBox

	SubBox\cam = CreateCamera()
	CameraViewport(SubBox\cam, 0, 0, 10, 10)

	CameraZoom SubBox\cam, 0.1
	CameraClsMode(SubBox\cam, 0, 0)
	CameraRange(SubBox\cam, 0.1, 1.5)
	MoveEntity(SubBox\cam, 0, 0, -20000)
	CameraProjMode(SubBox\cam, 0)

	SubBox\sprite = CreateSprite(SubBox\cam)
	EntityColor(SubBox\sprite, 0, 0, 0)
	EntityAlpha(SubBox\sprite, 0)
	EntityFX(SubBox\sprite, 1)
	EntityBlend(SubBox\sprite, 1)
	EntityOrder(SubBox\sprite, -2000)
	MoveEntity(SubBox\sprite, 0.0, 0.0, 1.0)

	SubBox\screenWidth = GraphicWidth * 0.5
	SubBox\screenLeft = (GraphicWidth * 0.5)+1-(SubBox\screenWidth*0.5)
	SubBox\screenTop = GraphicHeight * 0.8

	SubBox\targetHeight = SubBox\screenTop + SubtitleTextHeight
	SubBox\targetTop = SubBox\screenTop

	SubBox\curHeight = SubBox\targetHeight
	SubBox\curTop = SubBox\targetTop

	SubBox\alpha = 0
	SubBox\lines = 0
End Function


; ----- QUEUING -----


Function QueueSubtitle(soundPath$, soundHandle%, soundChannel%, isStream%=False)
	If (Not SubtitlesEnabled) Or (Not SubtitlesInitialized) Then
		Return 
	EndIf

	Local lowerPath$ = Lower(soundPath)
	Local found.SubtitleToken = Null

	; Check cache first
	For i = 0 To SubtitleCacheSize-1
		If SubtitleCacheKey(i) = lowerPath Then
			found = Object.SubtitleToken(SubtitleCacheToken(i))
			Exit
		EndIf
	Next

	; Search tokens
	If found = Null Then
		For t.SubtitleToken = Each SubtitleToken
			If t\soundPath = Lower(soundPath) Then
				found = t
				SubtitleCacheKey(SubtitleCacheNext) = lowerPath
				SubtitleCacheToken(SubtitleCacheNext) = Handle(found)
				SubtitleCacheNext = (SubtitleCacheNext+1) Mod SubtitleCacheSize
				Exit
			EndIf
		Next
	EndIf

	If found = Null Then Return

	Local e.SubtitleEntry = found\entry
	While e <> Null
		If ShouldShowSubtitle(e) Then
			Local queue.QueuedSubtitleMsg = new QueuedSubtitleMsg

			queue\sndHandle = soundHandle
			queue\chn = soundChannel
			queue\isStream = isStream
			queue\volume = 1.0

			queue\token = found
			queue\entry = e
			queue\timeStart = Millisecs() + e\time*1000
		EndIf
		e = e\nextEntry
	Wend
End Function

Function ShouldShowSubtitle%(e.SubtitleEntry)
	Select e\entryType
		Case 0 Return Not UsesDubbedAudio
		Case 1 Return ClosedCaptionsEnabled
		Case 2 Return UsesDubbedAudio
		Default Return False
	End Select
End Function

Function RemoveQueuedSubtitle(soundHandle%)
	If Not SubtitlesEnabled Then
		Return 
	EndIf

	For queue.QueuedSubtitleMsg = Each QueuedSubtitleMsg
		If queue\sndHandle = soundHandle And (Not queue\isStream) Then
			Delete queue
		EndIf
	Next
End Function

Function RemoveQueuedSubtitleByChannel(soundChannel%, isStream%=False)
	If Not SubtitlesEnabled Then
		Return 
	EndIf

	For queue.QueuedSubtitleMsg = Each QueuedSubtitleMsg
		If queue\chn = soundChannel and queue\isStream = isStream Then
			Delete queue
		EndIf
	Next
End Function

Function UpdateChannelVolumeWithSubtitles(chn%, volume#, isStream%=False, isSFX%=True)
	Local usedVolume# = volume
	If isSFX Then usedVolume = usedVolume * SFXVolume
	ChannelVolume(chn, usedVolume)
	UpdateQueuedSubtitleVolume(chn, volume, isStream)
End Function

Function UpdateQueuedSubtitleVolume(soundChannel%, volume#, isStream%=False)
	If Not SubtitlesEnabled Then
		Return 
	EndIf

	For queue.QueuedSubtitleMsg = Each QueuedSubtitleMsg
		If queue\chn = soundChannel And queue\isStream = isStream Then
			queue\volume = volume
		EndIf
	Next
End Function

Function ResumeChannelWithSubtitles(chn%)
	ResumeChannel(chn)
	SetQueuedSubtitlePause(chn, False)
End Function

Function PauseChannelWithSubtitles(chn%)
	PauseChannel(chn)
	SetQueuedSubtitlePause(chn, True)
End Function

Function SetQueuedSubtitlePause(soundChannel%, paused%)
	If Not SubtitlesEnabled Then
		Return 
	EndIf

	For queue.QueuedSubtitleMsg = Each QueuedSubtitleMsg
		If queue\chn = soundChannel Then
			If paused And queue\paused = 0 Then
				queue\paused = Millisecs()
			ElseIf (Not paused) And queue\paused <> 0 Then
				queue\timeStart = queue\timeStart + (Millisecs() - queue\paused)
				queue\paused = 0
			EndIf
		EndIf
	Next
End Function

Function ClearSubtitles()
	Delete Each QueuedSubtitleMsg
	Delete Each SubtitleMsg

	SubBox\lines = 0
End Function


Function TryCreateSubtitleMsg.SubtitleMsg(queue.QueuedSubtitleMsg, txt$="")
	If (Not SubtitlesEnabled) Or DeafTimer > 0 Or (Not ShouldShowSubtitle(queue\entry)) Then
		Return Null
	EndIf

	; Check if cooldown active
	If queue\entry\col\cooldownLength > 0.0 Then
		If Millisecs() < queue\entry\cooldown Then Return Null
	EndIf

	if queue\chn <> 0 Then
		If queue\volume < queue\entry\col\minVolume Then
			Return Null
		EndIf

		If Not ChannelPlaying(queue\chn) Then
			Return Null
		EndIf
	EndIf

	Local c.SubtitleMsg = new SubtitleMsg

	c\entry = queue\entry
	c\txt = txt 
	c\timeLeft = queue\entry\length+(SubtitleEarlyDelay*70.0)

	If txt = "" Then
		c\txt = c\entry\txt
	EndIf
	
	c\curYPos = (SubBox\targetTop+SubBox\targetHeight)-SubtitleTextHeight+9.0

	Insert c After Last SubtitleMsg
	SubBox\lines = SubBox\lines + 1

	; Add cooldown
	If queue\entry\col\cooldownLength > 0.0 Then
		queue\entry\cooldown = Millisecs() + queue\entry\col\cooldownLength*1000.0
	EndIf

	Return c
End Function

Function TryCreateSplitSubtitleMsg(queue.QueuedSubtitleMsg, txtLine$, padding%)
	If Not SubtitlesEnabled Then
		Return 
	EndIf

	Local tstr$ = ""
	Local offset% = 1
	Local chars% = 0
	Local splits% = 0

	For i = 1 To Len(txtLine)
		Local ch$ = Mid(txtLine, i, 1)
		If ch=" " Or i=Len(txtLine) Then
			Local w$ = Mid(txtLine, offset, chars)
			If StringWidth(tstr+w) > SubBox\screenWidth-padding Then

				; Bump up our previously created lines. This helps reduce visual noise when the lines slide into position.
				Local sub.SubtitleMsg = TryCreateSubtitleMsg(queue, Trim(tstr))
				If sub<>Null And splits>0 Then
					For j = 1 To splits
						sub = Before sub
						BumpSubtitleUp(sub)
					Next
				EndIf

				splits = splits + 1
				tstr = ""
			EndIf

			tstr = tstr + w
			offset = i
			chars = 1
		Else
			chars = chars + 1
		EndIf
	Next
	
	; Create subtitle for the remaining text
	w$ = Mid(txtLine, offset, chars)
	If Trim(w)<>"" Then
		sub.SubtitleMsg = TryCreateSubtitleMsg(queue, Trim(tstr)+w)
		If sub<>Null Then
			For j = 1 to splits
				sub = Before sub
				BumpSubtitleUp(sub)
			Next
		EndIf
	EndIf
End Function


; ----- UPDATE/DRAW -----


Function RecalculateSubtitleBoxTarget()
	SubBox\targetTop = (SubBox\screenTop + SubtitleTextHeight) - SubtitleTextHeight * Max(SubBox\lines, 1)
	SubBox\targetHeight = SubtitleTextHeight * Max(SubBox\lines, 1)
End Function

Function BumpSubtitleUp(c.SubtitleMsg)
	c\curYPos = c\curYPos-SubtitleTextHeight
End Function


Function UpdateSubtitles(factor#)
	If Not SubtitlesEnabled Then
		Return 
	EndIf
	CatchErrors("Uncaught (UpdateSubtitles)")

	SetFont Font1

	For queue.QueuedSubtitleMsg = Each QueuedSubtitleMsg
		If queue\paused = 0 And Millisecs() > (queue\timeStart-SubtitleEarlyDelay*1000) Then
			Local txtLine$ = queue\entry\txt
			If queue\entry\col\name<>"" Then
				txtLine = queue\entry\col\name+": "+txtLine
			EndIf

			If SubBox\lines < 20 Then
				Local noLines% = SubBox\lines <= 0

				; If we don't load this into a local, the check will spuriously fail.
				; This is incredibly obscure and I currently believe it may be the result of some x86 asm generation bug.
				Local sw% = StringWidth(txtLine)
				; Split long lines of text into multiple subtitles
				If sw >= SubBox\screenWidth-10 Then
					TryCreateSplitSubtitleMsg(queue, txtLine, 10)
				Else ; No need to split
					TryCreateSubtitleMsg(queue, txtLine)
				EndIf

				If noLines Then ; Box should wrap around all initial subtitles
					RecalculateSubtitleBoxTarget()
					SubBox\curHeight = SubBox\targetHeight
					SubBox\curTop = SubBox\targetTop
				EndIf
			EndIf

			Delete queue
		EndIf
	Next

	Local lines% = -1
	For c.SubtitleMsg = Each SubtitleMsg
		c\timeLeft = c\timeLeft - factor

		If c\timeLeft <= 0.0 Then
			SubBox\lines = SubBox\lines - 1

			; Need to recalculate or subtitles lower than this one move upward slightly
			RecalculateSubtitleBoxTarget()

			Delete c
		Else
			lines = lines + 1
			Local yPos# = SubBox\targetTop+(SubtitleTextHeight*lines)+9.0
			c\curYPos = CurveValue(yPos, c\curYPos, 7.0, factor)
		EndIf
	Next

	RecalculateSubtitleBoxTarget()
	If SubBox\lines>0 Then
		SubBox\curHeight = CurveValue(SubBox\targetHeight, SubBox\curHeight, 7.0, factor)
		SubBox\curTop = (SubBox\screenTop + SubtitleTextHeight) - SubBox\curHeight

		SubBox\alpha = CurveValue(1.0, SubBox\alpha, 7.0, factor)
	Else
		SubBox\alpha = CurveValue(0.0, SubBox\alpha, 7.0, factor)
	EndIf
End Function

Function DrawSubtitles()
	If Not SubtitlesEnabled Then
		Return 
	EndIf
	CatchErrors("Uncaught (DrawSubtitles)")

	; Draw text
	For c.SubtitleMsg = Each SubtitleMsg
		Local alpha# = 0.0
		local t# = Max(c\entry\length-c\timeLeft, 0)
		If c\timeLeft < SubtitleEarlyDelay*70.0 Then
			alpha = c\timeLeft/(SubtitleFadeLength*70.0)
		ElseIf t > 0 Then
			alpha = t/(SubtitleFadeLength*70.0)
		EndIf
		alpha = Max(Min(alpha, 1.0), 0.0)


		Color c\entry\col\r * alpha,c\entry\col\g * alpha,c\entry\col\b * alpha

		If c\entry\col\isItalic And c\entry\col\isBold Then
			SetFont Font1BoldItalic
		ElseIf c\entry\col\isItalic Then
			SetFont Font1Italic
		ElseIf c\entry\col\isBold Then
			SetFont Font1Bold
		Else
			SetFont Font1
		EndIf

		If t > 0 Then
			Text(SubBox\screenLeft+5,c\curYPos,c\txt)
		EndIf
	Next

	Color 255,255,255
	SetFont Font1
End Function

Function RenderSubtitles()
	If Not SubtitlesEnabled Then Return
	CatchErrors("Uncaught (RenderSubtitles)")

	EntityAlpha(SubBox\sprite, 0.7*SubBox\alpha)
	CameraViewport(SubBox\cam, SubBox\screenLeft, SubBox\curTop, SubBox\screenWidth, SubBox\curHeight)

	If SubBox\alpha > 0 Then
		CameraProjMode(SubBox\cam, 2)
		RenderWorld()
	EndIf

	CameraProjMode(SubBox\cam, 0)
End Function
