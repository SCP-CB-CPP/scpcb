Function ExecuteCfg%(file$)
    If FileType(file) <> 1 Then Return 0

    Local f% = ReadFile(file)
    While (Not Eof(f))
        Local l$ = ReadLine(f)
        If l <> "" And Instr(l, ";") <> 1 Then ExecuteConsoleCommand(l, True)
    Wend
    CloseFile(f)
    Return 1
End Function

Function ExecuteConsoleCommand(cmd$, quiet% = False)
    ConsoleQuiet = quiet

    ; Bind and exec commands do not set this flag, as this allows assigning binds without invalidating all runs
    ; Actually using them will still set the flag
    Local prevUsedConsole% = UsedConsole
    UsedConsole = True
    ConsoleReissue = Null
    ConsoleScroll = 0
    CreateConsoleMsg(cmd,255,255,0,True)
    If Instr(cmd, " ") > 0 Then
        StrTemp$ = Lower(Left(cmd, Instr(cmd, " ") - 1))
    Else
        StrTemp$ = Lower(cmd)
    End If
    
    Select Lower(StrTemp)
        Case "help"
            ;[Block]
            If Instr(cmd, " ")<>0 Then
                StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
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
                    CreateConsoleMsg("- blinkeffect [strength] [timer]")
                    CreateConsoleMsg("- notarget")
                    CreateConsoleMsg("- teleport [room name] [index]")
                    CreateConsoleMsg("- roomlist")
                    CreateConsoleMsg("- spawnitem [item name]")
                    CreateConsoleMsg("- itemlist")
                    CreateConsoleMsg("- spawncup [drink name]")
                    CreateConsoleMsg("******************************")
                    CreateConsoleMsg("Use "+Chr(34)+"help 2/3"+Chr(34)+" to find more commands.")
                    CreateConsoleMsg("Use "+Chr(34)+"help [command name]"+Chr(34)+" to get more information about a command.")
                    CreateConsoleMsg("******************************")
                Case "2"
                    CreateConsoleMsg("LIST OF COMMANDS - PAGE 2/3")
                    CreateConsoleMsg("******************************")
                    CreateConsoleMsg("- spawn [npc type] [state]")
                    CreateConsoleMsg("- npcspeed [npc type] [speed]")
                    CreateConsoleMsg("- npcstate [npc type]")
                    CreateConsoleMsg("- enable [npc type]")
                    CreateConsoleMsg("- disable [npc type]")
                    CreateConsoleMsg("- reset096")
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
                    CreateConsoleMsg("- timescale [value]")
                    CreateConsoleMsg("- camerafog [near] [far]")
                    CreateConsoleMsg("- viewbob [value]")
                    CreateConsoleMsg("- fov [value]")
                    CreateConsoleMsg("- gamma [value]")
                    CreateConsoleMsg("- playmusic [clip + .wav/.ogg]")
                    CreateConsoleMsg("- camerapick")
                    CreateConsoleMsg("- ending")
                    CreateConsoleMsg("- unlockcheckpoints")
                    CreateConsoleMsg("- togglewarheadlever")
                    CreateConsoleMsg("- unlockexits")
                    CreateConsoleMsg("- omni")
                    CreateConsoleMsg("- showmap")
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
                    CreateConsoleMsg("Sets the screen gamma.")
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
                    Console_ListNPCTypes()
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
                    CreateConsoleMsg("from "+Chr(34)+"SFX\Radio\UserTracks\"+Chr(34)+".")
                    CreateConsoleMsg("******************************")
                Case "setblinkeffect", "blinkeffect"
                    CreateConsoleMsg("HELP - blinkeffect")
                    CreateConsoleMsg("******************************")
                    CreateConsoleMsg("Sets the blink effect and its timer.")
                    CreateConsoleMsg("The blink effect scales how fast the blink meter")
                    CreateConsoleMsg("depletes. A value of two makes it drain twice")
                    CreateConsoleMsg("as fast, while a value of 0 disables blinking.")
                    CreateConsoleMsg("The timer is specified in seconds and controls")
                    CreateConsoleMsg("how long the effect will last.")
                    CreateConsoleMsg("If a timer value is not specified, the effect")
                    CreateConsoleMsg("will last indefinitely.")
                    CreateConsoleMsg("******************************")
                Case "omni"
                    CreateConsoleMsg("HELP - omni")
                    CreateConsoleMsg("******************************")
                    CreateConsoleMsg("Toggles getting guaranteed Key Card Omnis")
                    CreateConsoleMsg("from SCP-914, unless a valid parameter")
                    CreateConsoleMsg("is specified (on/off).")
                    CreateConsoleMsg("Using " + Chr(34) + "omni 2" + Chr(34) + " allows")
                    CreateConsoleMsg("the option to persist between runs.")
                    CreateConsoleMsg("A Key Card Omni can be obtained from SCP-914")
                    CreateConsoleMsg("by refining a Level 5 Key Card on Fine")
                    CreateConsoleMsg("or any Key Card on Very Fine.")
                    CreateConsoleMsg("By default, the chance of success is random")
                    CreateConsoleMsg("and relies on the amount of achievements")
                    CreateConsoleMsg("unlocked in the current save.")
                    CreateConsoleMsg("******************************")
                Case "showmap"
                    CreateConsoleMsg("HELP - showmap")
                    CreateConsoleMsg("******************************")
                    CreateConsoleMsg("Toggles drawing a 2D version of the map")
                    CreateConsoleMsg("layout on screen unless a valid parameter")
                    CreateConsoleMsg("is specified (on/off).")
                    CreateConsoleMsg("Using " + Chr(34) + "showmap 2" + Chr(34) + " allows")
                    CreateConsoleMsg("the option to persist between runs.")
                    CreateConsoleMsg("")

                    For i% = 3 To 1 Step -1
                        ConsoleR = 255 : ConsoleG = 255 : ConsoleB = 255
                        Select i
                            Case 3 CreateConsoleMsg("Entrance Zone")
                            Case 2 CreateConsoleMsg("Heavy Containment Zone")
                            Case 1 CreateConsoleMsg("Light Containment Zone")
                        End Select

                        For rt.RoomTemplates = Each RoomTemplates
                            If rt\R <> 255 Lor rt\G <> 255 Or rt\B <> 255 Then
                                For j% = 0 To ZONEAMOUNT-1
                                    If rt\zone[j] = i Then
                                        ConsoleR = rt\R : ConsoleG = rt\G : ConsoleB = rt\B
                                        CreateConsoleMsg(rt\Name)
                                        ConsoleR = 255 : ConsoleG = 255 : ConsoleB = 255
                                        Exit
                                    EndIf
                                Next
                            EndIf
                        Next

                        If i <> 1 Then CreateConsoleMsg("")
                    Next

                    ConsoleR = 0 : ConsoleG = 255 : ConsoleB = 255
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
            Local c = CameraPick(Camera,GraphicWidth/2, GraphicHeight/2)
            If c = 0 Then
                CreateConsoleMsg("******************************")
                CreateConsoleMsg("No entity  picked")
                CreateConsoleMsg("******************************")								
            Else
                CreateConsoleMsg("******************************")
                CreateConsoleMsg("Picked entity:")
                Local sf = GetSurface(c,1)
                Local b = GetSurfaceBrush( sf )
                Local t = GetBrushTexture(b,0)
                Local texname$ =  StripPath(TextureName(t))
                CreateConsoleMsg("Texture name: "+texname)
                CreateConsoleMsg("Coordinates: "+EntityX(c)+", "+EntityY(c)+", "+EntityZ(c))
                CreateConsoleMsg("******************************")							
            EndIf
            ;[End Block]
        Case "hudscalefactor"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))

            UpdateHUDScaleFactor(Float(StrTemp))
            ;[End Block]
        Case "hudoffset"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            HUDOffsetScale = Float(StrTemp)
            UpdateHUDOffsets()
            ;[End Block]
        Case "viewbob"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))

            ViewBobScale = Float(StrTemp)
            ;[End Block]
        Case "fov"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            FOV = Int(StrTemp)
            ;[End Block]
        Case "hidedistance"
            ;[Block]
            HideDistance = Float(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            CreateConsoleMsg("Hidedistance set to "+HideDistance)
            ;[End Block]
        Case "ending"
            ;[Block]
            SelectedEnding = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            KillTimer = -0.1
            ;EndingTimer = -0.1
            ;[End Block]
        Case "noclipspeed"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            NoClipSpeed = Float(StrTemp)
            ;[End Block]
        Case "injure"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            Injuries = Float(StrTemp)
            ;[End Block]
        Case "infect"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            Infect = Float(StrTemp)
            ;[End Block]
        Case "heal"
            ;[Block]
            Injuries = 0
            Bloodloss = 0
            LostHands = False
            ResetDiseases()
            ;[End Block]
        Case "teleport"
            ;[Block]
            Local roomName$ = Piece(cmd, 2, " ")
            Local roomIndex% = Int(Piece(cmd, 3, " "))

            Select roomName
                Case "room008", "scp-008"
                    roomName = "008"
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
                        Teleport(r)
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
            Local itt.ItemTemplates = FindItemTemplate(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            If itt = Null Then
                CreateConsoleMsg("Item not found.",255,150,0)
            Else
                CreateConsoleMsg(itt\displayname + " spawned.")
                it.Items = CreateItem(itt\name, EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))

                If itt\name = "snavulti" Lor itt\name = "fineradio" Lor itt\name = "veryfineradio" Then
                    it\state = 101
                EndIf
            End If
            ;[End Block]
        Case "giveitem"
            ;[Block]
            If ItemAmount >= MaxItemAmount Then
                CreateConsoleMsg("No free inventory slot.",255,150,0)
            Else
                itt.ItemTemplates = FindItemTemplate(Right(cmd, Len(cmd) - Instr(cmd, " ")))
                If itt = Null Then
                    CreateConsoleMsg("Item not found.",255,150,0)
                Else
                    CreateConsoleMsg(itt\displayname + " given.")
                    it.Items = CreateItem(itt\name, EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
                    PickItem(it)
                End If
            EndIf
            ;[End Block]
        Case "itemlist", "items"
            CreateConsoleMsg("Listing items:")
            For itt.ItemTemplates = Each ItemTemplates
                CreateConsoleMsg("- " + itt\displayname + "(" + itt\name + ")")
            Next
        Case "spawncup"
            Local drinkName$ = Right(cmd, Len(cmd) - Instr(cmd, " "))
            If CreateCup(drinkName, EntityX(Collider), EntityY(Camera,True), EntityZ(Collider)) Then
                CreateConsoleMsg("Cup of " + drinkName + " spawned.")
            Else
                CreateConsoleMsg("Drink not found.", 255, 150, 0)
            EndIf
        Case "wireframe"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
        Case "npcspeed"
            ;[Block]
            Local args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            StrTemp$ = Piece$(args$, 1)
            StrTemp2$ = Piece$(args$, 2)

            Console_ChangeNPCSpeed(StrTemp$, Float(StrTemp2))
            ;[End Block]
        Case "173speed"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Curr173\Speed = Float(StrTemp)
            CreateConsoleMsg("173's speed set to " + StrTemp)
            ;[End Block]
        Case "106speed"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Curr106\Speed = Float(StrTemp)
            CreateConsoleMsg("106's speed set to " + StrTemp)
            ;[End Block]
        Case "npcstate"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Console_PrintNPCState(StrTemp)
            ;[End Block]
        Case "enable"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Console_EnableNPC(StrTemp)
            ;[End Block]
        Case "disable"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Console_DisableNPC(StrTemp)
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
            DisableNPC(Curr173)
            ;[End Block]
        Case "enable173"
            ;[Block]
            EnableNPC(Curr173)
            ;[End Block]
        Case "disable106"
            ;[Block]
            DisableNPC(Curr106)
            ;[End Block]
        Case "enable106"
            ;[Block]
            EnableNPC(Curr106)
            ;[End Block]
        Case "halloween"
            ;[Block]
            If Curr173\TextureID = 1 Then
                ChangeNPCTextureID(Curr173, 0)
                CreateConsoleMsg("173 JACK-O-LANTERN OFF")
            Else
                ChangeNPCTextureID(Curr173, 1)
                CreateConsoleMsg("173 JACK-O-LANTERN ON")
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
            Next
            PlaySound_Strict LoadTempSound("SFX\Music\420J.ogg")
            ;[End Block]
        Case "godmode", "god"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
            RotateEntity Collider, 0, EntityYaw(Collider), 0

            PositionEntity(Head, 0, 0, 0)
            ResetEntity Head

            KillTimer = 0
            KillAnim = 0
            ;[End Block]
        Case "noclip","fly"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
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

                For r.Rooms = Each Rooms
                    Local tb.Triggerboxes = r\FirstTriggerbox
                    While tb <> Null
                        ShowEntity(tb\Obj)
                        tb = tb\Successor
                    Wend
                Next
            Else
                CreateConsoleMsg("Debug Mode Off")
            
                For r.Rooms = Each Rooms
                    tb.Triggerboxes = r\FirstTriggerbox
                    While tb <> Null
                        HideEntity(tb\Obj)
                        tb = tb\Successor
                    Wend
                Next
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
            args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            CameraFogNear = Float(Left(args, Len(args) - Instr(args, " ")))
            CameraFogFar = Float(Right(args, Len(args) - Instr(args, " ")))
            CreateConsoleMsg("Near set to: " + CameraFogNear + ", far set to: " + CameraFogFar)
            ;[End Block]
        Case "gamma"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            ScreenGamma = Int(StrTemp)
            CreateConsoleMsg("Gamma set to " + ScreenGamma)
            ;[End Block]
        Case "spawn"
            ;[Block]
            args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
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
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
        Case "toggle_warhead_lever", "togglewarheadlever"
            ;[Block]
            For e.Events = Each Events
                If e\EventName = "room2nuke" Then
                    e\EventState = (Not e\EventState)
                    Exit
                EndIf
            Next
            ;[End Block]
        Case "unlockcheckpoints"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Select StrTemp
                Case "lcz"
                    For e.Events = Each Events
                        If e\EventName = "room2sl" Then
                            e\EventState3 = 0.0
                            UpdateLever(e\room\Levers[0])
                            RotateEntity(e\room\Levers[0], 0.0, EntityYaw(e\room\Levers[0]), 0.0)
                            Exit
                        EndIf
                    Next
                    CreateConsoleMsg("The Heavy Containment Zone is now unlocked.")	
                Case "hcz"
                    For e.Events = Each Events
                        If e\EventName = "008" Then
                            e\EventState = 2.0
                            UpdateLever(e\room\Objects[1])
                            RotateEntity(e\room\Objects[1], 0.0, EntityYaw(e\room\Objects[1]), 0.0)
                            Exit
                        EndIf
                    Next	
                    CreateConsoleMsg("The Entrance Zone is now unlocked.")	
                Default
                    For e.Events = Each Events
                        If e\EventName = "room2sl" Then
                            e\EventState3 = 0.0
                            UpdateLever(e\room\Levers[0])
                            RotateEntity(e\room\Levers[0], 0.0, EntityYaw(e\room\Levers[0]), 0.0)
                        ElseIf e\EventName = "008" Then
                            e\EventState = 2.0
                            UpdateLever(e\room\Objects[1])
                            RotateEntity(e\room\Objects[1], 0.0, EntityYaw(e\room\Objects[1]), 0.0)
                        EndIf
                    Next
                    CreateConsoleMsg("The Heavy Containment Zone and Entrance Zone are now unlocked.")	
            End Select
            ;[End Block]
        Case "unlockexits"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
            Kill()
            DeathMSG = I_Loc\DeathMessage_Suicide[Rand(4)]
            ;[End Block]
        Case "playmusic"
            ;[Block]
            ; I think this might be broken since the FMod library streaming was added. -Mark
            If Instr(cmd, " ")<>0 Then
                StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Else
                StrTemp$ = ""
            EndIf
            
            If StrTemp$ <> ""
                PlayCustomMusic% = True
                If CustomMusic <> 0 Then FreeSound_Strict CustomMusic : CustomMusic = 0
                If MusicCHN <> 0 Then StopChannel MusicCHN
                CustomMusic = LoadSound_Strict("SFX\Radio\UserTracks\"+StrTemp$)
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
            args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
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
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
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
            it\state = 101
            ;[End Block]
        Case "spawnnvg"
            ;[Block]
            it.Items = CreateItem("nvgoggles", EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
            it\state = 1000
            ;[End Block]
        Case "spawnpumpkin","pumpkin"
            ;[Block]
            CreateConsoleMsg("What pumpkin?")
            ;[End Block]
        Case "spawnnav"
            ;[Block]
            it.Items = CreateItem("snavulti", EntityX(Collider), EntityY(Camera,True), EntityZ(Collider))
            it\state = 101
            ;[End Block]
        Case "teleport173"
            ;[Block]
            PositionEntity Curr173\Collider,EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider)
            ResetEntity Curr173\Collider
            ;[End Block]
        Case "seteventstate"
            ;[Block]
            args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
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
                    CreateConsoleMsg("The current room doesn't have any event applied.",255,150,0)
                EndIf
            EndIf
            ;[End Block]
        Case "spawnparticles"
            ;[Block]
            If Instr(cmd, " ")<>0 Then
                StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Else
                StrTemp$ = ""
            EndIf

            Local maxParticlesIdx% = 2
            If IsBirthday Then maxParticlesIdx = 7

            If Int(StrTemp) >= 0 And Int(StrTemp) <= maxParticlesIdx% ;<--- This is the maximum ID of particles by Devil Particle system, will be increased after time - ENDSHN
                SetEmitter(Collider,ParticleEffect[Int(StrTemp)])
                CreateConsoleMsg("Spawned particle emitter with ID "+Int(StrTemp)+" at player's position.")
            Else
                CreateConsoleMsg("Particle emitter with ID "+Int(StrTemp)+" not found.",255,150,0)
            EndIf
            ;[End Block]
        Case "giveachievement"
            ;[Block]
            If Instr(cmd, " ")<>0 Then
                StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            Else
                StrTemp$ = ""
            EndIf
            
            i% = 0
            Local searched% = Int(StrTemp)
            For a.Achievements = Each Achievements
                If i = searched%
                    a\Unlocked = True
                    CreateConsoleMsg("Achievement "+a\LocalName+" unlocked.")
                    i = -1
                    Exit
                EndIf
                i = i + 1
            Next

            If i <> -1 Then CreateConsoleMsg("Achievement with ID "+searched+" doesn't exist.",255,150,0)
            ;[End Block]
        Case "427state"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            I_427\Timer = Float(StrTemp)*70.0
            ;[End Block]
        Case "teleport106"
            ;[Block]
            Curr106\State = 0
            Curr106\Idle = False
            ;[End Block]
        Case "setblinkeffect", "blinkeffect"
            ;[Block]
            args$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            BlinkEffect = Float(Piece(args, 1, " "))
            Local timerStr$ = Piece(args, 2, " ")
            If timerStr = "" Then
                If BlinkEffect = 1 Then BlinkEffectTimer = 0 Else BlinkEffectTimer = Infinity
            Else
                BlinkEffectTimer = Float(timerStr)
            EndIf
            CreateConsoleMsg("Set BlinkEffect to: " + BlinkEffect + " and BlinkEffect timer: " + BlinkEffectTimer)
            ;[End Block]
        Case "omni"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            
            Select StrTemp
                Case "on", "1", "true"
                    GuaranteedOmni% = True
                Case "off", "0", "false"
                    GuaranteedOmni% = False
                Case "2"
                    GuaranteedOmni% = 2
                Default
                    GuaranteedOmni% = Not GuaranteedOmni%
            End Select
            
            If GuaranteedOmni% = 2 Then
                CreateConsoleMsg("GUARANTEED KEY CARD OMNI ON (PERSISTENT)")	
            Else If GuaranteedOmni% Then
                CreateConsoleMsg("GUARANTEED KEY CARD OMNI ON")	
            Else
                CreateConsoleMsg("GUARANTEED KEY CARD OMNI OFF")
            EndIf
            ;[End Block]
        Case "showmap"
            ;[Block]
            StrTemp$ = Lower(Right(cmd, Len(cmd) - Instr(cmd, " ")))

            Select StrTemp
                Case "on", "1", "true"
                    ShowMap% = True						
                Case "off", "0", "false"
                    ShowMap% = False
                Case "2"
                    ShowMap% = 2
                Default
                    ShowMap% = Not ShowMap%
            End Select

            If ShowMap% = 2 Then
                CreateConsoleMsg("SHOW MAP ON (PERSISTENT)")
            Else If ShowMap% Then
                CreateConsoleMsg("SHOW MAP ON")
            Else
                CreateConsoleMsg("SHOW MAP OFF")
            EndIf
            If ShowMap% Then CreateConsoleMsg("TYPE " + Chr(34) + "help showmap" + Chr(34) + " FOR A LIST OF ROOM COLORS")
            ;[End Block]
        Case "timescale"
            TimeScale = Float(Lower(Right(cmd, Len(cmd) - Instr(cmd, " "))))
            CreateConsoleMsg("TIMESCALE SET TO " + TimeScale)
        Case "keylist"
            CreateConsoleMsg("Listing key codes:")
            For i = 0 To 255
                Local keyName$ = GetKeyName(i)
                If keyName <> "" Then CreateConsoleMsg(i + ": " + keyName)
            Next
        Case "bind"
            Local argStart% = Instr(cmd, " ") + 1
            Local cmdStart% = Instr(cmd, " ", argStart)
            Local bindingKey% = Int(Mid(cmd, argStart, cmdStart - argStart))
            Local bindingCmd$ = Right(cmd, Len(cmd) - cmdStart)
            Local verb$ = "Bound "
            If CreateBinding(bindingKey, bindingCmd) Then
                verb = "Rebound "
            EndIf
            CreateConsoleMsg(verb + Chr(34) + bindingCmd + Chr(34) + " to key " + bindingKey + " (" + GetKeyName(bindingKey) + ").")

            UsedConsole = prevUsedConsole
        Case "unbind"
            bindingKey% = Int(Right(cmd, Len(cmd) - Instr(cmd, " ")))
            If DeleteBinding(bindingKey) Then
                CreateConsoleMsg("Unbound key " + bindingKey + " (" + GetKeyName(bindingKey) + ").")
            Else
                CreateConsoleMsg("Key " + bindingKey + " (" + GetKeyName(bindingKey) + ") was not bound.",255,150,0)
            EndIf

            UsedConsole = prevUsedConsole
        Case "unbindall"
            UnbindAll()
            CreateConsoleMsg("Unbound all bindings.")
        
            UsedConsole = prevUsedConsole
        Case "exec"
            Local file$ = Right(cmd, Len(cmd) - Instr(cmd, " "))
            If ExecuteCfg(file) Lor ExecuteCfg(DataDir + file) Lor ExecuteCfg(DataDir + "cfg\" + file) Lor ExecuteCfg(file + ".cfg") Lor ExecuteCfg(DataDir +  file + ".cfg") Lor ExecuteCfg(DataDir + "cfg\" + file + ".cfg") Then
                CreateConsoleMsg("Executed configuration file.")
            Else
                CreateConsoleMsg("Configuration file not found.",255,150,0)
            EndIf
        Case "clear", "cls"
            Delete Each ConsoleMsg
        Case "mav"
            RuntimeErrorExt("Violation Access Memory")
        Case Chr($6A)+Chr($6F)+Chr($72)+Chr($67)+Chr($65)
            ;[Block]
            ConsoleFlush = True 
            
            If ConsoleFlushSnd = 0 Then
                ConsoleFlushSnd = LoadSound(Chr(83)+Chr(70)+Chr(88)+Chr(92)+Chr(83)+Chr(67)+Chr(80)+Chr(92)+Chr(57)+Chr(55)+Chr(48)+Chr(92)+Chr(116)+Chr(104)+Chr(117)+Chr(109)+Chr(98)+Chr(115)+Chr(46)+Chr(100)+Chr(98))
                ConsoleMusFlush% = LoadSound(Chr(83)+Chr(70)+Chr(88)+Chr(92)+Chr(77)+Chr(117)+Chr(115)+Chr(105)+Chr(99)+Chr(92)+Chr(116)+Chr(104)+Chr(117)+Chr(109)+Chr(98)+Chr(115)+Chr(46)+Chr(100)+Chr(98))
                ConsoleMusPlay = PlaySound(ConsoleMusFlush)
                CreateConsoleMsg(Chr(74)+Chr(79)+Chr(82)+Chr(71)+Chr(69)+Chr(32)+Chr(72)+Chr(65)+Chr(83)+Chr(32)+Chr(66)+Chr(69)+Chr(69)+Chr(78)+Chr(32)+Chr(69)+Chr(88)+Chr(80)+Chr(69)+Chr(67)+Chr(84)+Chr(73)+Chr(78)+Chr(71)+Chr(32)+Chr(89)+Chr(79)+Chr(85)+Chr(46))
            Else
                CreateConsoleMsg(Chr(74)+Chr(32)+Chr(79)+Chr(32)+Chr(82)+Chr(32)+Chr(71)+Chr(32)+Chr(69)+Chr(32)+Chr(32)+Chr(67)+Chr(32)+Chr(65)+Chr(32)+Chr(78)+Chr(32)+Chr(78)+Chr(32)+Chr(79)+Chr(32)+Chr(84)+Chr(32)+Chr(32)+Chr(66)+Chr(32)+Chr(69)+Chr(32)+Chr(32)+Chr(67)+Chr(32)+Chr(79)+Chr(32)+Chr(78)+Chr(32)+Chr(84)+Chr(32)+Chr(65)+Chr(32)+Chr(73)+Chr(32)+Chr(78)+Chr(32)+Chr(69)+Chr(32)+Chr(68)+Chr(46))
            EndIf
            ;[End Block]
        Default
            ;[Block]
            CreateConsoleMsg("Command not found.",255,0,0)
            ;[End Block]
    End Select
End Function
