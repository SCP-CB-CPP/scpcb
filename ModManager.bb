Type ActiveMods
    Field Path$
    Field IsLocale%
End Type

Type Mods
    Field Id$
    Field Path$
    Field Name$
    Field Description$
    Field Author$
    Field IconPath$, Icon%, DisabledIcon%
    Field RequiresReload%
    Field IsNew%, IsActive%
    Field SteamWorkshopId$
    Field IsUserOwner%
End Type

Global ModCount%

Const STEAM_ITEM_ID_FILENAME$ = "steam_itemid.txt"

Function InstantiateMod.Mods(id$, path$)
    m.Mods = new Mods
    m\IsNew = True
    m\Id = id
    m\Path = path
    Local modIni$ = m\Path + "info.ini"
    If FileType(modIni) <> 1 Then RuntimeErrorExt("Mod at " + Chr(34) + m\Path + Chr(34) + " is missing an info.ini file.")
    Local ini% = OpenFile(modIni)
    While Not Eof(ini)
        Local l$ = Trim(ReadLine(ini))
        If l <> "" And Instr(l, "#") <> 1 And Instr(l, ";") <> 1 Then
            Local splitterPos = Instr(l, "=")
            Local key$ = Trim(Left(l, splitterPos - 1))
            Local value$ = Trim(Right(l, Len(l) - splitterPos))
            Select key
                Case "name"
                    m\Name = value
                Case "desc"
                    m\Description = value
                Case "author"
                    m\Author = value
                Case "requires reload"
                    m\RequiresReload = ParseINIInt(value)
            End Select
        EndIf
    Wend
    CloseFile(ini)

    If m\Name = "" Then RuntimeErrorExt("Mod at " + Chr(34) + m\Path + Chr(34) + " is missing a name in its info.ini file.")
    For m2.Mods = Each Mods
        ; ID collisions should be impossible.
        If m2 <> m And m2\Id = m\Id Then RuntimeErrorExt("Mod at " + Chr(34) + m\Path + Chr(34) + " and mod at " + Chr(34) + m\Path + Chr(34) + " share a mod ID.")
    Next

    m\IconPath = DetermineIcon(m, False)

    ModCount = ModCount + 1
    Return m
End Function

Function ReloadMods()
    For m.Mods = Each Mods
        If Icon <> 0 Then FreeImage(Icon)
    Next
    Delete Each Mods
    ModCount = 0
    d% = ReadDir("Mods")
    Repeat
        f$=NextFile(d)
        If f$="" Then Exit
        If f$<>"." And f$<>".." And FileType("Mods\"+f) = 2 Then
            m.Mods = InstantiateMod("local " + f, "Mods\"+f+"\")

            ; These files will find their way onto the workshop on the first update, so we only set the owner for local mods.
            If FileType(m\Path + STEAM_ITEM_ID_FILENAME) = 1 Then
                Local idFile% = OpenFile(m\Path + STEAM_ITEM_ID_FILENAME)
                m\SteamWorkshopId = ReadLine(idFile)
                m\IsUserOwner = True
                CloseFile(idFile)
            EndIf
        EndIf
    Forever
    CloseDir(d)

    If SteamActive
        Steam_LoadSubscribedItems()
        Local itemCount% = Steam_GetSubscribedItemCount()
        For i = 0 To itemCount-1
            Local id$ = Steam_GetSubscribedItemID(i)
            Local itemPath$ = Steam_GetSubscribedItemPath(i)
            If id <> "" And itemPath <> ""
                m.Mods = InstantiateMod(id, itemPath + "\")
                m\SteamWorkshopId = id
            EndIf
        Next
    EndIf

    If FileType(ModsFile) = 1 Then
        Local mods% = OpenFile(ModsFile)
        Local firstSorted.Mods = First Mods
        While Not Eof(mods)
            l$ = Trim(ReadLine(mods))
            If l <> "" And Instr(l, "#") <> 1 And Instr(l, ";") <> 1 Then
                splitterPos = Instr(l, "=")
                key$ = Trim(Left(l, splitterPos - 1))
                value$ = Trim(Right(l, Len(l) - splitterPos))
                For m.Mods = Each Mods
                    If key = m\Id Then
                        Insert m After firstSorted
                        firstSorted = m
                        m\IsActive = ParseINIInt(value)
                        m\IsNew = False
                        Exit
                    EndIf
                Next
            EndIf
        Wend
        CloseFile(mods)
    EndIf
    
    UpdateActiveMods()
End Function

Function SerializeMods()
    Local f% = WriteFile(ModsFile)
    For m.Mods = Each Mods
        WriteLine(f, m\Id + "=" + Str(m\IsActive))
    Next
    CloseFile(f)
End Function

Const LOCALIZATIONS_DIR$ = "Localization\"

Function UpdateActiveMods()
    HasDubbedAudio = False

    Local txt$
    Delete Each ActiveMods
    Local mm.ActiveMods
    Local locale$ = GetCLIString("locale", GetOptionString("general", "locale"))
    If locale = "" And SteamActive Then locale = Steam_GetGameLanguage()
    If locale <> "" And FileType(LOCALIZATIONS_DIR + locale) = 2 Then
        mm = New ActiveMods
        mm\Path = LOCALIZATIONS_DIR + locale + "\"
        mm\IsLocale = True
        If FileType(mm\Path + "SFX") = 2 Then HasDubbedAudio = True
        txt = "locale " + locale
    EndIf
    For m.Mods = Each Mods
        If m\IsActive Then
            If locale <> "" And FileType(m\Path + LOCALIZATIONS_DIR + locale) = 2 Then
                mm.ActiveMods = New ActiveMods
                mm\Path = m\Path + LOCALIZATIONS_DIR + locale + "\"
                mm\IsLocale = True
                If FileType(mm\Path + "SFX") = 2 Then HasDubbedAudio = True
                If txt <> "" Then txt = txt + ", "
                txt = txt + m\Id + " locale " + locale
            EndIf
            mm.ActiveMods = New ActiveMods
            mm\Path = m\Path
            If txt <> "" Then txt = txt + ", "
            txt = txt + m\Id
        EndIf
    Next
    SetErrorMsg(10, "Active mods: " + txt)

    UsesDubbedAudio = HasDubbedAudio And DubbedAudio
End Function

Function DetermineModdedTexturePath$(file$)
    If Instr(file, "Mods\") = 1 Then file = Right(file, Len(file) - Instr(file, "\", 6))
    If Instr(file, ":") = 2 Then
        ; Could be a mod load path.. Check if any of them fit.
        For m.ActiveMods = Each ActiveMods
            If Instr(file, m\Path) = 1 Then
                file = Right(file, Len(file) - Len(m\Path))
                Exit
            EndIf
        Next
    EndIf

    Local ext$ = File_GetExtension(File)
	Local fileNoExt$ = Left(File, Len(File) - Len(ext))

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
				Return modPath
			EndIf
		Next
	Next

    Return file
End Function

SetTextureLoadPathMutator(@DetermineModdedTexturePath)

Function LoadModdedMeshNonStrict%(File$, parent%=0)
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
				tmp = LoadMesh(modPath, parent)
				If tmp <> 0 Then
					Return tmp
				Else
					RuntimeErrorExt("Failed to load 3D Mesh " + Chr(34) + modPath + Chr(34) + ".")
				EndIf
			EndIf
		Next
	Next

    Return LoadMesh(File, parent)
End Function

Function DetermineModdedPath$(f$)
    For m.ActiveMods = Each ActiveMods
        modPath$ = m\Path + f
        If FileType(modPath) = 1 Then Return modPath
    Next
    Return f
End Function

Function DetermineIcon$(m.Mods, allowGif% = True)
    If allowGif And FileType(m\Path + "icon.gif") = 1 Then Return m\Path + "icon.gif"
    If FileType(m\Path + "icon.png") = 1 Then Return m\Path + "icon.png"
    If FileType(m\Path + "icon.jpg") = 1 Then Return m\Path + "icon.jpg"
    Return ""
End Function

Global UpdatingMod.Mods
Global UpdateModErrorCode%
Function UploadMod(m.Mods)
    If UpdatingMod <> Null Then Return
    UpdatingMod = m
    Steam_PublishItem(m\Name, m\Description, m\Path, DetermineIcon(m))
End Function

Function UpdateMod(m.Mods, changelog$)
    If UpdatingMod <> Null Then Return
    UpdatingMod = m
    Local desc$ = ""
    If Not ShouldKeepModDescription Then
        desc = m\Description
    EndIf
    Steam_UpdateItem(m\SteamworkshopId, m\Name, desc, m\Path, DetermineIcon(m), changelog, True)
End Function

Function UpdateUpdatingMod()
    If UpdatingMod = Null Then Return

    Local status% = Steam_QueryUpdateItemStatus()
    If status = 3 Then
        UpdatingMod\SteamWorkshopId = Steam_GetPublishedItemID()
        UpdatingMod\IsUserOwner = True
        Local f% = WriteFile(UpdatingMod\Path + STEAM_ITEM_ID_FILENAME)
        WriteLine(f, UpdatingMod\SteamWorkshopId)
        CloseFile(f)
        VisitModPage(UpdatingMod)
        UpdatingMod = Null
    EndIf
    If status => 100 Then
        UpdateModErrorCode = status
        UpdatingMod = Null
    EndIf
End Function

Function VisitModPage(m.Mods)
    If m\SteamWorkshopId = "" Then Return

    ExecFile("https://steamcommunity.com/sharedfiles/filedetails/?id=" + m\SteamworkshopId)
End Function

Function GetWorkshopErrorCodeStr$(err%)
    Local txt$ = ""
    Select err Mod 1000
        Case 2 txt = "Generic failure. Try changing your Steam download region or your connection type."
        Case 24 txt = "You are restricted from uploading content. Please contact Steam support."
        Case 17 txt = "You are currently VAC or game banned."
        Case 16 txt = "The operation timed out. Please try again."
        Case 21 txt = "You are not currently logged into Steam."
        Case 20 txt = "The workshop server is currently unavailable. Please try again later."
        Case 8 txt = "One of the submissions data fields is invalid."
        Case 15 txt = "Access denied."
        Case 25 txt = "The preview image is too large, it must be less than 1 MB or you have exceeded your Steam Cloud quota."
        Case 9 txt = "An uploaded file could not be found."
        Case 14 txt = "You already have a workshop item with that name."
        Case 44 txt = "Due to a recent password or email change you are not currently allowed to upload new content."
    End Select
    If txt <> "" Then
        Return "Code " + Str(err) + ": " + txt
    Else
        Return "Code " + Str(err)
    EndIf
End Function

Function GetModdedINIString$(file$, section$, key$)
    For m.ActiveMods = Each ActiveMods
        Local moddedPath$ = m\Path + file
        If FileType(moddedPath) = 1
            Local ret$ = GetINIString(moddedPath, section, key)
            If ret <> "" Then Return ret
        EndIf
    Next
    Return GetINIString(file, section, key)
End Function

Function GetModdedINIInt%(file$, section$, key$)
    Return Int(GetModdedINIString(file, section, key))
End Function

Function GetModdedINIFloat#(file$, section$, key$)
    Return Float(GetModdedINIString(file, section, key))
End Function

Function Format$(txt$, arg1$)
    Return Replace(txt, "{0}", arg1)
End Function

Function Format$(txt$, arg1$, arg2$)
    Return Replace(Format(txt, arg1), "{1}", arg2)
End Function

Function Format$(txt$, arg1$, arg2$, arg3$)
    Return Replace(Format(txt, arg1, arg2), "{2}", arg3)
End Function
