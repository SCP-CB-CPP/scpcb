Const MaxItemAmount% = 10
Global ItemAmount%
Dim Inventory.Items(MaxItemAmount + 1)
Global SelectedItem.Items

Global ClosestItem.Items

Global LastItemID%

Type ItemTemplates
	Field displayname$
	Field name$
	Field group$
	
	Field sound%
	
	Field found%
	
	Field obj%, objpath$
	Field invimg%,invimg2%,invimgpath$
	Field imgpath$, img%
	
	Field isAnim%
	
	Field scale#
	;Field bumptex%
	Field tex%, texpath$
End Type 

Function CreateItemTemplate.ItemTemplates(name$, group$, displayname$, objpath$, invimgpath$, imgpath$, scale#, texturepath$ = "",invimgpath2$="",Anim%=0, texflags%=9)
	Local it.ItemTemplates = New ItemTemplates, n
	
	
	;if another item shares the same object, copy it
	For it2.itemtemplates = Each ItemTemplates
		If it2\objpath = objpath And it2\obj <> 0 Then it\obj = CopyEntity(it2\obj) : Exit
	Next
	
	If it\obj = 0 Then
		If Anim<>0 Then
			it\obj = LoadAnimMesh_Strict(objpath)
			it\isAnim=True
		Else
			it\obj = LoadMesh_Strict(objpath)
			it\isAnim=False
		EndIf
		it\objpath = objpath
	EndIf
	it\objpath = objpath
	
	Local texture%
	
	If texturepath <> "" Then
		For it2.itemtemplates = Each ItemTemplates
			If it2\texpath = texturepath And it2\tex<>0 Then
				texture = it2\tex
				Exit
			EndIf
		Next
		If texture=0 Then texture=LoadTexture_Strict(texturepath,texflags%) : it\texpath = texturepath; : DebugLog texturepath
		EntityTexture it\obj, texture
		it\tex = texture
	EndIf  
	
	it\scale = scale
	ScaleEntity it\obj, scale, scale, scale, True
	
	;if another item shares the same object, copy it
	For it2.itemtemplates = Each ItemTemplates
		If it2\invimgpath = invimgpath And it2\invimg <> 0 Then
			it\invimg = it2\invimg ;CopyImage()
			If it2\invimg2<>0 Then
				it\invimg2=it2\invimg2 ;CopyImage()
			EndIf
			Exit
		EndIf
	Next
	If it\invimg=0 Then
		it\invimg = LoadImage_Strict(invimgpath)
		it\invimgpath = invimgpath
		ResizeImage(it\invimg, 64*HUDScale, 64*HUDScale)
	EndIf
	
	If (invimgpath2 <> "") Then
		If it\invimg2=0 Then
			it\invimg2 = LoadImage_Strict(invimgpath2)
			ResizeImage(it\invimg2, 64*HUDScale, 64*HUDScale)
		EndIf
	Else
		it\invimg2 = 0
	EndIf
	
	it\imgpath = imgpath
	
	;If imgpath<>"" Then
	;	it\img=LoadImage(imgpath)
	;	
	;	;DebugLog imgpath
	;	
	;	If it\img<>0 Then ScaleImage(it\img, MenuScale, MenuScale)
	;EndIf
	
	it\group = group
	it\name = name
	it\displayname = GetModdedINIString(StringsFile, "Item", name)
	If it\displayname = "" Then
		it\displayname = displayname
		If it\displayname = "" Then
			If DebugResourcePacks Then
				RuntimeErrorExt("Item " + Chr(34) + name + Chr(34) + " is missing a localized string!")
			Else
				it\displayname = name
			EndIf
		EndIf
	EndIf
	
	it\sound = 1

	HideEntity it\obj
	
	Return it
	
End Function

Function InitItemTemplatesFromFile(file$)
	If FileType(file) <> 1 Then Return

	Local name$, group$, displayname$
	Local model$, textureoverride$, textureflags%, anim%
	Local invicon$, invicon2$
	Local document$
	Local sound%, scale#
	Local hasCustomColor%, r%, g%, b%

	Local f% = OpenFile(file)
	While Not Eof(f)
		Local l$ = Trim(ReadLine(f))
		If l <> "" And Instr(l, "#") <> 1 And Instr(l, ";") <> 1 Then
			Local splitterPos = Instr(l, "=")
			If splitterPos = 0 And Instr(l, "[") = 1 Then
				If name <> "" Then
					Local itt.ItemTemplates = CreateItemTemplate(name, group, displayname, model, invicon, document, scale, textureoverride, invicon2, anim, textureflags)
					If hasCustomColor Then
						EntityColor(itt\obj, r, g, b)
					EndIf
					itt\sound = sound
				EndIf

				name = Trim(Mid(l, 2, Len(l) - 2))
				If name = "" then
					RuntimeErrorExt("Empty item name.")
				EndIf
				group = ""
				displayname = ""
				model = ""
				textureoverride = ""
				textureflags = 9
				anim = 0
				invicon = ""
				invicon2 = ""
				document = ""
				sound = 1
				scale = 1.0
				hasCustomColor = False
			Else
				Local key$ = Trim(Left(l, splitterPos - 1))
				Local value$ = Trim(Right(l, Len(l) - splitterPos))
				Select key
					Case "group"
						group = value
					Case "display name"
						displayname = value
					Case "model"
						model = value
					Case "invicon"
						invicon = value
					Case "document"
						document = value
					Case "scale"
						scale = Float(value)
					Case "texture override"
						textureoverride = value
					Case "invicon2"
						invicon2 = value
					Case "anim"
						anim = Int(value)
					Case "texture flags"
						textureflags = Int(value)
					Case "sound"
						sound = Int(value)
					Case "color"
						hasCustomColor = True
						r = Int(Piece(value,1,","))
						g = Int(Piece(value,2,","))
						b = Int(Piece(value,3,","))
					Default
						RuntimeErrorExt("Unknown key "+Chr(34)+key+Chr(34)+" in item "+Chr(34)+name+Chr(34)+".")
				End Select
			EndIf
		EndIf
	Wend
	CloseFile(f)
	
	If name <> "" Then
		itt.ItemTemplates = CreateItemTemplate(name, group, displayname, model, invicon, document, scale, textureoverride, invicon2, anim, textureflags)
		If hasCustomColor Then
			EntityColor(itt\obj, r, g, b)
		EndIf
		itt\sound = sound
	EndIf

End Function

Const ITEMS_DATA_PATH$ = "Data\items.ini"

Function InitItemTemplates()
	Local hasOverride%
	For m.ActiveMods = Each ActiveMods
		Local modPath$ = m\Path + ITEMS_DATA_PATH
		InitItemTemplatesFromFile(modPath)
		If FileType(modPath + ".OVERRIDE") = 1 Then
			hasOverride = True
			Exit
		EndIf
	Next

	If Not hasOverride Then InitItemTemplatesFromFile(ITEMS_DATA_PATH)

	Local it.ItemTemplates,it2.ItemTemplates
	For it = Each ItemTemplates
		If (it\tex<>0) Then
			If (it\texpath<>"") Then
				For it2=Each ItemTemplates
					If (it2<>it) And (it2\tex=it\tex) Then
						it2\tex = 0
					EndIf
				Next
			EndIf
			FreeTexture it\tex : it\tex = 0
		EndIf
	Next
	
End Function 

Function FindItemTemplate.ItemTemplates(name$)
	name$ = Lower(name$)
	For itt.Itemtemplates = Each ItemTemplates
		If Lower(itt\name) = name Lor Lower(itt\displayname) = name Then
			Return itt
		End If
	Next
End Function

Type Inventories
	Field Items.Items[20]
	Field Size%
End Type

Type Items
	Field displayname$
	Field collider%,model%
	Field itemtemplate.ItemTemplates
	Field DropSpeed#
	
	Field r%,g%,b%,a#
	
	Field SoundChn%
	
	Field dist#, disttimer#
	
	Field state#, state2#
	
	Field Picked%,Dropped%
	
	Field invimg%
	Field xspeed#,zspeed#
	Field Inventory.Inventories
	Field ID%

	Field drinkName$
End Type 

Function CreateItem.Items(name$, x#, y#, z#)
	CatchErrors("Uncaught (CreateItem)")
	
	Local i.Items = New Items
	Local it.ItemTemplates
	
	name = Lower(name)
	
	For it.ItemTemplates = Each ItemTemplates
		If Lower(it\name) = name Then
			i\itemtemplate = it
			i\collider = CreatePivot()			
			EntityRadius i\collider, 0.01
			EntityPickMode i\collider, 1, False
			i\model = CopyEntity(it\obj,i\collider)
			i\displayname = it\displayname
			ShowEntity i\collider
			ShowEntity i\model
			Exit
		EndIf
	Next
	
	If i\itemtemplate = Null Then RuntimeErrorExt("Item template not found ("+name+")")
	
	ResetEntity i\collider		
	PositionEntity(i\collider, x, y, z, True)
	RotateEntity (i\collider, 0, Rand(360), 0)
	i\dist = EntityDistance(Collider, i\collider)
	i\DropSpeed = 0.0
	
	i\invimg = i\itemtemplate\invimg
	If name="clipboard" Then
		i\Inventory = New Inventories
		i\Inventory\Size = 10
		SetAnimTime i\model,17.0
		i\invimg = i\itemtemplate\invimg2
	ElseIf name="wallet" Then
		i\Inventory = New Inventories
		i\Inventory\Size = 10
		SetAnimTime i\model,0.0
	EndIf
	
	i\ID=LastItemID+1
	LastItemID=i\ID
	
	CatchErrors("CreateItem")
	Return i
End Function

Function CreateCup.Items(drinkName$, x#, y#, z#, r%, g%, b%, a#=1.0)
	CatchErrors("Uncaught (CreateCup)")

	Local i.Items = CreateItem("cup", x, y, z)

	i\drinkName = drinkName
	i\displayname = Format(I_Loc\Cup_Of, drinkName)

	i\state = 1.0
		
	i\r=r
	i\g=g
	i\b=b
	i\a=a
	
	Local liquid = CopyEntity(LiquidObj)
	ScaleEntity liquid, i\itemtemplate\scale,i\itemtemplate\scale,i\itemtemplate\scale,True
	PositionEntity liquid, EntityX(i\collider,True),EntityY(i\collider,True),EntityZ(i\collider,True)
	EntityParent liquid, i\model
	EntityColor liquid, r,g,b
	
	If a < 0 Then 
		EntityFX liquid, 1
		EntityAlpha liquid, Abs(a)
	Else
		EntityAlpha liquid, Abs(a)
	EndIf
	
	EntityShininess liquid, 1.0
	
	CatchErrors("CreateCup")
	Return i
End Function

Function RemoveItem(i.Items, inGame%=True)
	CatchErrors("Uncaught (RemoveItem)")
	Local n
	FreeEntity(i\model) : FreeEntity(i\collider) : i\collider = 0
	
	If i\Picked And inGame Then
		For n% = 0 To MaxItemAmount - 1
			If Inventory(n) = i
				DebugLog "Removed "+i\itemtemplate\name+" from slot "+n
				Inventory(n) = Null
				ItemAmount = ItemAmount-1
				Exit
			EndIf
		Next
		
		If RemoveItem\Subscribers > 0 Then
			PrepareFunction(1)
			Local ii.Items = i
			SetArgObj(0, &ii)
			CallHook(RemoveItem)
		EndIf

		Select i\itemtemplate\name 
			Case "nvgoggles", "finenvgoggles", "supernv"
				WearingNightVision = False
			Case "gasmask", "supergasmask", "gasmask2", "gasmask3"
				WearingGasMask = False
			Case "vest", "finevest", "veryfinevest"
				WearingVest = False
			Case "hazmatsuit","hazmatsuit2","hazmatsuit3"
				WearingHazmat = False	
			Case "scp714"
				Wearing714 = False
			Case "scp1499","super1499"
				Wearing1499 = False
			Case "scp427"
				I_427\Using = False
		End Select
	EndIf
	If i\itemtemplate\img <> 0
		FreeImage i\itemtemplate\img
		i\itemtemplate\img = 0
	EndIf
	If i\Inventory <> Null Then Delete i\Inventory
	Delete i
	
	CatchErrors("RemoveItem")
End Function


Function UpdateItems()
	CatchErrors("Uncaught (UpdateItems)")
	Local n, i.Items, i2.Items
	Local xtemp#, ytemp#, ztemp#
	Local temp%, np.NPCs
	Local pick%
	
	Local HideDist = HideDistance*0.5
	Local deletedItem% = False
	
	ClosestItem = Null
	For i.Items = Each Items
		i\Dropped = 0
		
		If (Not i\Picked) Then
			If i\disttimer < MilliSecs() Then
				i\dist = EntityDistance(Camera, i\collider)
				i\disttimer = MilliSecs() + 700
			EndIf
			
			If i\dist < HideDist Then
				; ShowEntity resets the collisions.
				Local hasCollided% = EntityCollided(i\collider, HIT_MAP)

				ShowEntity i\collider
				
				If i\dist < 1.2 Then
					If ClosestItem = Null Then
						If EntityInView(i\model, Camera) Then
							If EntityVisible(i\collider,Camera) Then
								ClosestItem = i
							EndIf
						EndIf
					ElseIf ClosestItem = i Or i\dist < EntityDistance(Camera, ClosestItem\collider) Then 
						If EntityInView(i\model, Camera) Then
							If EntityVisible(i\collider,Camera) Then
								ClosestItem = i
							EndIf
						EndIf
					EndIf
				EndIf
				
				If hasCollided Then
					i\DropSpeed = 0
					i\xspeed = 0.0
					i\zspeed = 0.0
				Else
					If ShouldEntitiesFall
						pick = LinePick(EntityX(i\collider),EntityY(i\collider),EntityZ(i\collider),0,-10,0)
						If pick
							i\DropSpeed = i\DropSpeed - 0.0004 * FPSfactor
							TranslateEntity i\collider, i\xspeed*FPSfactor, i\DropSpeed * FPSfactor, i\zspeed*FPSfactor
						Else
							i\DropSpeed = 0
							i\xspeed = 0.0
							i\zspeed = 0.0
						EndIf
					Else
						i\DropSpeed = 0
						i\xspeed = 0.0
						i\zspeed = 0.0
					EndIf
				EndIf
				
				If i\dist<HideDist*0.2 Then
					For i2.Items = Each Items
						If i<>i2 And (Not i2\Picked) And i2\dist<HideDist*0.2 Then
							
							xtemp# = (EntityX(i2\collider,True)-EntityX(i\collider,True))
							ytemp# = (EntityY(i2\collider,True)-EntityY(i\collider,True))
							ztemp# = (EntityZ(i2\collider,True)-EntityZ(i\collider,True))
							
							ed# = (xtemp*xtemp+ztemp*ztemp)
							If ed<0.07 And Abs(ytemp)<0.25 Then
								;items are too close together, push away
								If PlayerRoom\RoomTemplate\Name	<> "room2storage" Then
									xtemp = xtemp*(0.07-ed)
									ztemp = ztemp*(0.07-ed)
									
									While Abs(xtemp)+Abs(ztemp)<0.001
										xtemp = xtemp+Rnd(-0.002,0.002)
										ztemp = ztemp+Rnd(-0.002,0.002)
									Wend
									
									TranslateEntity i2\collider,xtemp,0,ztemp
									TranslateEntity i\collider,-xtemp,0,-ztemp
								EndIf
							EndIf
						EndIf
					Next
				EndIf
				
				If EntityY(i\collider) < - 35.0 Then DebugLog "remove: " + i\itemtemplate\name:RemoveItem(i):deletedItem=True
			Else
				HideEntity i\collider
			EndIf
		Else
			i\DropSpeed = 0
			i\xspeed = 0.0
			i\zspeed = 0.0
		EndIf
		
		If Not deletedItem Then
			CatchErrors(Chr(34)+i\itemtemplate\name+Chr(34)+" item")
		EndIf
		deletedItem = False
	Next
	
	If ClosestItem <> Null Then
		;DrawHandIcon = True
		
		If MouseHit1 Then PickItem(ClosestItem)
	EndIf
	
End Function

Function PickItem(item.Items)
	Local n% = 0
	Local canpickitem = True
	Local fullINV% = True
	
	For n% = 0 To MaxItemAmount - 1
		If Inventory(n)=Null
			fullINV = False
			Exit
		EndIf
	Next
	
	If WearingHazmat > 0 Then
		Msg = I_Loc\MessageItem_HazmatNopickup
		MsgTimer = 70*5
		Return
	EndIf
	
	CatchErrors("Uncaught (PickItem)")
	If (Not fullINV) Then
		For n% = 0 To MaxItemAmount - 1
			If Inventory(n) = Null Then
				If PickItem\Subscribers > 0 Then
					PrepareFunction(1)
					Local ii.Items = item
					SetArgObj(0, &ii)
					CallHook(PickItem)
				EndIf

				Select item\itemtemplate\name
					Case "scp1123"
						If Not (Wearing714 = 1) Then
							If PlayerRoom\RoomTemplate\Name <> "room1123" Then
								ShowEntity Light
								LightFlash = 7
								PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Touch.ogg"))		
								DeathMSG = I_Loc\DeathMessage_1123
								Kill()
							EndIf
							For e.Events = Each Events
								If e\eventname = "room1123" Then 
									If e\eventstate = 0 Then
										ShowEntity Light
										LightFlash = 3
										PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Touch.ogg"))
									EndIf
									e\eventstate = Max(1, e\eventstate)
									
									Exit
								EndIf
							Next
						EndIf
						
						Return
					Case "killbat"
						ShowEntity Light
						LightFlash = 1.0
						PlaySound_Strict(IntroSFX(11))
						DeathMSG = I_Loc\DeathMessage_Killbat
						Kill()
					Case "scp148"
						GiveAchievement(Achv148)	
					Case "scp513"
						GiveAchievement(Achv513)
					Case "scp860"
						GiveAchievement(Achv860)
					Case "key6"
						GiveAchievement(AchvOmni)
					Case "veryfinevest"
						Msg = I_Loc\MessageItem_VestHeavy
						MsgTimer = 70*6
						Exit
					Case "firstaid", "finefirstaid", "veryfinefirstaid", "firstaid2"
						item\state = 0
					Case "snavulti"
						GiveAchievement(AchvSNAV)
					Case "hazmatsuit", "hazmatsuit2", "hazmatsuit3"
						canpickitem = True
						For z% = 0 To MaxItemAmount - 1
							If Inventory(z) <> Null Then
								If Inventory(z)\itemtemplate\group="hazmat" Then
									canpickitem% = False
									Exit
								ElseIf Inventory(z)\itemtemplate\group="vest" Then
									canpickitem% = 2
									Exit
								EndIf
							EndIf
						Next
						
						If canpickitem=False Then
							Msg = I_Loc\MessageItem_HazmatConflictHazmat
							MsgTimer = 70 * 5
							Return
						ElseIf canpickitem=2 Then
							Msg = I_Loc\MessageItem_VestConflictHazmat
							MsgTimer = 70 * 5
							Return
						Else
							;TakeOffStuff(1+16)
							SelectedItem = item
						EndIf
					Case "vest","finevest"
						canpickitem = True
						For z% = 0 To MaxItemAmount - 1
							If Inventory(z) <> Null Then
								If Inventory(z)\itemtemplate\group="vest" Then
									canpickitem% = False
									Exit
								ElseIf Inventory(z)\itemtemplate\group="hazmat" Then
									canpickitem% = 2
									Exit
								EndIf
							EndIf
						Next
						
						If canpickitem=False Then
							Msg = I_Loc\MessageItem_VestConflictVest
							MsgTimer = 70 * 5
							Return
						ElseIf canpickitem=2 Then
							Msg = I_Loc\MessageItem_VestConflictHazmat
							MsgTimer = 70 * 5
							Return
						Else
							;TakeOffStuff(2)
							SelectedItem = item
						EndIf
				End Select
				
				If item\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(item\itemtemplate\sound))
				item\Picked = True
				item\Dropped = -1
				
				item\itemtemplate\found=True
				ItemAmount = ItemAmount + 1
				
				Inventory(n) = item
				HideEntity(item\collider)
				Exit
			EndIf
		Next
	Else
		Msg = I_Loc\MessageItem_Full
		MsgTimer = 70 * 5
	EndIf
	CatchErrors("PickItem")
End Function

Function DropItem(item.Items,playdropsound%=True)
	If WearingHazmat > 0 Then
		Msg = I_Loc\MessageItem_HazmatNodrop
		MsgTimer = 70*5
		Return
	EndIf
	
	CatchErrors("Uncaught (DropItem)")
	If playdropsound Then
		If item\itemtemplate\sound <> 66 Then PlaySound_Strict(PickSFX(item\itemtemplate\sound))
	EndIf
	
	item\Dropped = 1
	
	ShowEntity(item\collider)
	PositionEntity(item\collider, EntityX(Camera), EntityY(Camera), EntityZ(Camera))
	RotateEntity(item\collider, EntityPitch(Camera), EntityYaw(Camera)+Rnd(-20,20), 0)
	MoveEntity(item\collider, 0, -0.1, 0.1)
	RotateEntity(item\collider, 0, EntityYaw(Camera)+Rnd(-110,110), 0)
	
	ResetEntity (item\collider)
	
	item\Picked = False
	For z% = 0 To MaxItemAmount - 1
		If Inventory(z) = item Then
			Inventory(z) = Null
			Exit
		EndIf
	Next

	If DropItem\Subscribers > 0 Then
		PrepareFunction(1)
		Local ii.Items = item
		SetArgObj(0, &ii)
		CallHook(DropItem)
	EndIf

	Select item\itemtemplate\name
		Case "gasmask", "supergasmask", "gasmask3"
			WearingGasMask = False
		Case "hazmatsuit",  "hazmatsuit2", "hazmatsuit3"
			WearingHazmat = False
		Case "vest", "finevest"
			WearingVest = False
		Case "nvgoggles"
			If WearingNightVision = 1 Then CameraFogFar = StoredCameraFogFar : WearingNightVision = False
		Case "supernv"
			If WearingNightVision = 2 Then CameraFogFar = StoredCameraFogFar : WearingNightVision = False
		Case "finenvgoggles"
			If WearingNightVision = 3 Then CameraFogFar = StoredCameraFogFar : WearingNightVision = False
		Case "scp714"
			Wearing714 = False
		Case "scp1499","super1499"
			Wearing1499 = False
		Case "scp427"
			I_427\Using = False
	End Select
	
	CatchErrors("DropItem")
	
End Function

;Update any ailments inflicted by SCP-294 drinks.
Function Update294()
	CatchErrors("Uncaught (Update294)")
	
	If CameraShakeTimer > 0 Then
		CameraShakeTimer = Max(CameraShakeTimer - (FPSfactor/70), 0)
		CameraShake = 2
	EndIf
	
	If VomitTimer > 0 Then
		DebugLog VomitTimer
		VomitTimer = VomitTimer - (FPSfactor/70)
		
		If (MilliSecs() Mod 1600) < Rand(200, 400) Then
			If BlurTimer = 0 Then BlurTimer = Rnd(10, 20)*70
			CameraShake = Rnd(0, 2)
		EndIf
		
;		If (MilliSecs() Mod 1000) < Rand(1200) Then 
		
		If Rand(50) = 50 And (MilliSecs() Mod 4000) < 200 Then PlaySound_Strict(CoughSFX(Rand(0,2)))
		
		;Regurgitate when timer is below 10 seconds. (ew)
		If VomitTimer < 10 And Rnd(0, 500 * VomitTimer) < 2 Then
			If (Not ChannelPlaying(VomitCHN)) And (Not Regurgitate) Then
				VomitCHN = PlaySound_Strict(LoadTempSound("SFX\SCP\294\Retch" + Rand(1, 2) + ".ogg"))
				Regurgitate = MilliSecs() + 50
			EndIf
		EndIf
		
		If Regurgitate > MilliSecs() And Regurgitate <> 0 Then
			mouse_y_speed_1 = mouse_y_speed_1 + 1.0
		Else
			Regurgitate = 0
		EndIf
		
	ElseIf VomitTimer < 0 Then ;vomit
		VomitTimer = VomitTimer - (FPSfactor/70)
		
		If VomitTimer > -5 Then
			If (MilliSecs() Mod 400) < 50 Then CameraShake = 4 
			mouse_x_speed_1 = 0.0
			Playable = False
		Else
			Playable = True
		EndIf
		
		If (Not Vomit) Then
			BlurTimer = 40 * 70
			VomitSFX = LoadSound_Strict("SFX\SCP\294\Vomit.ogg")
			VomitCHN = PlaySound_Strict(VomitSFX)
			PrevInjuries = Injuries
			PrevBloodloss = Bloodloss
			Injuries = Injuries + 1.5
			Bloodloss = Bloodloss + 70
			EyeIrritation = 9 * 70
			
			pvt = CreatePivot()
			PositionEntity(pvt, EntityX(Camera), EntityY(Collider) - 0.05, EntityZ(Camera))
			TurnEntity(pvt, 90, 0, 0)
			EntityPick(pvt, 0.3)
			de.decals = CreateDecal(5, PickedX(), PickedY() + 0.005, PickedZ(), 90, 180, 0)
			de\Size = 0.001 : de\SizeChange = 0.001 : de\MaxSize = 0.6 : EntityAlpha(de\obj, 1.0) : EntityColor(de\obj, 0.0, Rnd(200, 255), 0.0) : ScaleSprite de\obj, de\size, de\size
			FreeEntity pvt
			Vomit = True
		EndIf
		
		UpdateDecals()
		
		mouse_y_speed_1 = mouse_y_speed_1 + Max((1.0 + VomitTimer / 10), 0.0)
		
		If VomitTimer < -15 Then
			FreeSound_Strict(VomitSFX)
			VomitTimer = 0
			If KillTimer >= 0 Then
				PlaySound_Strict(BreathSFX(0,0))
			EndIf
			Injuries = PrevInjuries
			Bloodloss = PrevBloodloss
			Vomit = False
		EndIf
	EndIf
	
	CatchErrors("Update294")
End Function








;~IDEal Editor Parameters:
;~F#B#1E
;~C#Blitz3D