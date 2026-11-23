Type BodyRegions
	Field Name$
	Field Slots%
	
	; When this flag is set, equipping an item with this specific region will not add its slot to the occupied slots.
	; This does NOT extend to regions that include this region, those will set the occupied slot, and are thus the only way to do so.
	Field AllowMultiple%
End Type

Global OccupiedBodySlots%
Global LastBodyRegionSlot%

Const BODY_REGIONS_DATA_PATH$ = "Data/body_regions.ini"

Function InitBodyRegionsFromFile(file$)
	If FileType(file) <> 1 Then Return

	Local name$, slots%, allowMultiple%
	Local f% = OpenFile(file)
	While Not Eof(f)
		Local l$ = Trim(ReadLine(f))
		If l <> "" And Instr(l, "#") <> 1 And Instr(l, ";") <> 1 Then
			Local splitterPos = Instr(l, "=")
			If splitterPos = 0 And Instr(l, "[") = 1 Then
				If name <> "" Then
					Local eqr.BodyRegions = FindBodyRegion(name)
                    If eqr = Null Then eqr = New BodyRegions
					eqr\Name = name
                    If slots = 0 Then
                        slots = 1 Shl LastBodyRegionSlot
                        LastBodyRegionSlot = LastBodyRegionSlot + 1
                    EndIf
					eqr\Slots = slots
					eqr\AllowMultiple = allowMultiple
				EndIf

				name = Trim(Mid(l, 2, Len(l) - 2))
				If name = "" then
					RuntimeErrorExt("Empty body region name.")
				EndIf
				slots = 0
				allowMultiple = False
			Else
				If splitterPos = 0 Then Continue
				Local key$ = Trim(Left(l, splitterPos - 1))
				Local value$ = Trim(Right(l, Len(l) - splitterPos))
				Select key
					Case "includes"
                        Local eqr2.BodyRegions = FindBodyRegion(value)
                        If eqr2 = Null Then RuntimeErrorExt("Body region "+Chr(34)+name+Chr(34)+" includes unknown body region "+Chr(34)+value+Chr(34)+".")
                        slots = slots Or eqr2\Slots
					Case "allow multiple"
						allowMultiple = ParseINIInt(value)
					Default
						RuntimeErrorExt("Unknown key "+Chr(34)+key+Chr(34)+" in body region "+Chr(34)+name+Chr(34)+".")
				End Select
			EndIf
		EndIf
	Wend

    If name <> "" Then
        eqr.BodyRegions = New BodyRegions
        eqr\Name = name
        If slots = 0 Then
            slots = 1 Shl LastBodyRegionSlot
            LastBodyRegionSlot = LastBodyRegionSlot + 1
        EndIf
        eqr\Slots = slots
        eqr\AllowMultiple = allowMultiple
    EndIf
	CloseFile(f)
End Function

Function InitBodyRegions()
	Delete Each BodyRegions
    LastBodyRegionSlot = 0

	Local modRooms$
	Local hasOverride%
	Local lowest.ActiveMods = Last ActiveMods
	For m.ActiveMods = Each ActiveMods
		modRooms$ = m\Path + BODY_REGIONS_DATA_PATH
		If FileType(m\Path + BODY_REGIONS_DATA_PATH) = 1 And FileType(m\Path + BODY_REGIONS_DATA_PATH + ".OVERRIDE") = 1 Then
			hasOverride = True
			lowest = m
			Exit
		EndIf
	Next
	; We load the vanilla regions first, so that they will be able to be included by modded ones.
	If Not hasOverride Then InitBodyRegionsFromFile(BODY_REGIONS_DATA_PATH)
	While lowest<>Null
		modRooms$ = lowest\Path + BODY_REGIONS_DATA_PATH
		InitBodyRegionsFromFile(modRooms)
		lowest = Before lowest
	Wend
End Function

Function FindBodyRegion.BodyRegions(name$)
	For eqr.BodyRegions = Each BodyRegions
		If eqr\Name = name Then Return eqr
	Next
	Return Null
End Function

Function TryEquipItem%(item.Items)
    If CanUseItem(item) Then EquipItem(item) : Return True
    Return False
End Function

Function EquipItem(item.Items)
    If item\itemtemplate\bodyregion = Null Then RuntimeErrorExt("Item "+Chr(34)+item\itemtemplate\name+Chr(34)+" has no body region set.")

    item\Picked = 2

	If item\itemtemplate\bodyregion\AllowMultiple Then Return

    OccupiedBodySlots = OccupiedBodySlots Or item\itemtemplate\bodyregion\Slots
End Function

Function UnequipItem(item.Items)
    If item\itemtemplate\bodyregion = Null Then RuntimeErrorExt("Item "+Chr(34)+item\itemtemplate\name+Chr(34)+" has no body region set.")

    item\Picked = 1

	If item\itemtemplate\bodyregion\AllowMultiple Then Return

    OccupiedBodySlots = OccupiedBodySlots And (item\itemtemplate\bodyregion\Slots Xor -1)
End Function

Function CanUseItem%(item.Items)
	Local br.BodyRegions = item\itemtemplate\bodyregion
    If br = Null Then RuntimeErrorExt("Item "+Chr(34)+item\itemtemplate\name+Chr(34)+" has no body region set.")

    If br\Name = "finger" And LostHands Then
        Msg = I_Loc\MessageItem_Scp714Nohands
		MsgTimer = 70 * 5
        Return False
    EndIf

    Local conflict% = br\Slots And OccupiedBodySlots
    If conflict = 0 Then
        Return True
    EndIf

    For n% = 0 To MaxItemAmount-1
        Local it.Items = Inventory(n)
        If it <> Null And it\Picked = 2 And it\itemtemplate\bodyregion <> Null And (it\itemtemplate\bodyregion\Slots And conflict) <> 0 And it\itemtemplate\bodyregion\AllowMultiple = False Then
			If it\itemtemplate = item\itemtemplate Lor (it\itemtemplate\group <> "" And it\itemtemplate\group = item\itemtemplate\group) Then
				Msg = Format(I_Loc\MessageItemConflictTemplates_Self, GetConflictString(it, ".self"))
			Else
            	Msg = Format(I_Loc\MessageItemConflictTemplates_Takeoff, GetConflictString(it, ".off"), GetConflictString(item, ".on"))
			EndIf
			MsgTimer = 70 * 5
            Return False
        EndIf
    Next

    Return False
End Function

Function GetConflictString$(item.Items, suffix$)
	Local ret$ = GetModdedINIString(StringsFile, "Message Item Conflict", item\itemtemplate\name + suffix)
	If ret <> "" Then Return ret
	If item\itemtemplate\group <> "" Then
		ret = GetModdedINIString(StringsFile, "Message Item Conflict", item\itemtemplate\group + suffix)
		If ret <> "" Then Return ret
	EndIf
	Return GetModdedINIString(StringsFile, "Message Item Conflict", "fallback" + suffix)
End Function
