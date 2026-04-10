Function RegisterCommon()
    RegisterGlobalProperty("float FPSFactor", &FPSFactor)
    
    ; TODO: Probably move these to a "Menu" namespace.
    RegisterGlobalProperty("const bool IsMainMenuOpen", &MainMenuOpen)
    RegisterGlobalProperty("int OpenMenu", &MenuOpen)
    RegisterGlobalProperty("float StopHidingTimer", &StopHidingTimer)

    RegisterGlobalFunction("bool IsAnyMenuOpen()", @IsAnyMenuOpen)
    RegisterGlobalFunction("bool IsPaused()", @IsPaused)
End Function

Function RegisterOptions()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Options") Else SetDefaultNamespace("Options")

    RegisterGlobalFunction("int GetInt(string section, string key)", @GetOptionInt)
    RegisterGlobalFunction("float GetFloat(string section, string key)", @GetOptionFloat)
    RegisterGlobalFunction("string GetString(string section, string key)", @GetOptionString)

    RegisterGlobalFunction("bool PutINIValue(string section, string key, string value)", @SetOptionValue)

    RegisterGlobalProperty("const bool SteamEnabled", &SteamActive)
    RegisterGlobalProperty("const bool SteamRichPresenceEnabled", &SteamRichPresenceActive)
    RegisterGlobalProperty("const bool DiscordRichPresenceEnabled", &DiscordActive)
    RegisterGlobalProperty("const bool ModsEnabled", &ModsEnabled)

    RegisterGlobalProperty("bool IntroEnabled", &IntroEnabled)
    RegisterGlobalProperty("int LoadingScreenTimePerCharacter", &LoadingScreenTimePerCharacter)
    RegisterGlobalProperty("bool AchievementPopupEnabled", &AchvMSGenabled)

    ; TODO: Remove these completely.
    ;RegisterGlobalProperty("int LauncherWidth", &LauncherWidth)
    ;RegisterGlobalProperty("int LauncherHeight", &LauncherHeight)
    ;RegisterGlobalProperty("bool LauncherEnabled", &LauncherEnabled)

    RegisterGlobalProperty("const int GraphicWidth", &GraphicWidth)
    RegisterGlobalProperty("const int GraphicHeight", &GraphicHeight)
    RegisterGlobalProperty("const bool Fullscreen", &Fullscreen)
    RegisterGlobalProperty("bool ShowFPS", &ShowFPS)
    RegisterGlobalProperty("const bool BorderlessWindowed", &BorderlessWindowed)
    RegisterGlobalProperty("bool EnableRoomLights", &EnableRoomLights)
    RegisterGlobalProperty("int TextureDetails", &TextureDetails)
    RegisterGlobalProperty("bool BumpEnabled", &BumpEnabled)
    RegisterGlobalProperty("bool HUDEnabled", &HUDenabled)
    RegisterGlobalProperty("int Framelimit", &Framelimit)
    RegisterGlobalProperty("bool Vsync", &Vsync)
    RegisterGlobalProperty("int AntiAlias", &Opt_AntiAlias)
    RegisterGlobalProperty("float ScreenGamma", &ScreenGamma)
    RegisterGlobalProperty("float ViewBobScale", &ViewBobScale)
    RegisterGlobalProperty("bool InvertMouse", &InvertMouse)
    RegisterGlobalProperty("float HUDScaleFactor", &HUDScaleFactor)
    RegisterGlobalProperty("float HUDOffsetScale", &HUDOffsetScale)
    RegisterGlobalProperty("float FOV", &FOV)

    RegisterGlobalProperty("float MouseSensitivity", &MouseSens)
    RegisterGlobalProperty("float MouseSmoothing", &MouseSmooth)
    RegisterGlobalProperty("int KeyRight", &KEY_RIGHT)
    RegisterGlobalProperty("int KeyLeft", &KEY_LEFT)
    RegisterGlobalProperty("int KeyUp", &KEY_UP)
    RegisterGlobalProperty("int KeyDown", &KEY_DOWN)
    RegisterGlobalProperty("int KeyBlink", &KEY_BLINK)
    RegisterGlobalProperty("int KeySprint", &KEY_SPRINT)
    RegisterGlobalProperty("int KeyInventory", &KEY_INV)
    RegisterGlobalProperty("int KeyCrouch", &KEY_CROUCH)
    RegisterGlobalProperty("int KeySave", &KEY_SAVE)
    RegisterGlobalProperty("int KeyConsole", &KEY_CONSOLE)
    RegisterGlobalProperty("bool MoveInputCancelling", &MoveInputCancelling)

    RegisterGlobalProperty("float MusicVolume", &MusicVolume)
    RegisterGlobalProperty("float SoundVolume", &SFXVolume)
    RegisterGlobalProperty("bool EnableSFXRelease", &EnableSFXRelease)
    RegisterGlobalProperty("bool EnableUserTracks", &EnableUserTracks)
    RegisterGlobalProperty("int UserTrackMode", &UserTrackMode)
    RegisterGlobalProperty("bool DubbedAudio", &DubbedAudio)
    RegisterGlobalProperty("bool SubtitlesEnabled", &SubtitlesEnabled)
    RegisterGlobalProperty("bool ClosedCaptionsEnabled", &ClosedCaptionsEnabled)

    RegisterGlobalProperty("bool ConsoleEnabled", &CanOpenConsole)
    RegisterGlobalProperty("bool ConsoleAutoOpen", &ConsoleOpening)

    RegisterGlobalProperty("bool SpeedRunMode", &SpeedRunMode)
    RegisterGlobalProperty("bool NumericSeeds", &UseNumericSeeds)

    ; Don't forget to remove!!
    RegisterGlobalProperty("bool DebugResourcePackStrictLoad", &DebugResourcePacks)
    RegisterGlobalProperty("bool DebugMapGen", &DebugMapGen)
    RegisterGlobalProperty("bool DebugForestGen", &DebugForestGen)

    RegisterGlobalProperty("int ParticleAmount", &ParticleAmount)

    SetDefaultNamespace(ns)
End Function

Function RegisterIO()
    ; TODO: Sanitize paths (also in audio).
    ; TODO: Consider "hijacking" the standard B3D functions.
    RegisterGlobalFunction("B3D::Image@ LoadImage(string file, float scale=0, int flags=0)", @LoadImage_Strict)
    RegisterGlobalFunction("B3D::Mesh@ LoadMesh(string file, B3D::Entity@ parent)", @LoadMesh_Strict)
    RegisterGlobalFunction("B3D::Mesh@ LoadAnimMesh(string file, B3D::Entity@ parent)", @LoadAnimMesh_Strict)
    RegisterGlobalFunction("B3D::Texture@ LoadTexture(string file, int flags=1)", @LoadTexture_Strict)
    RegisterGlobalFunction("B3D::Texture@ LoadAnimTexture(string file, int flags, int columns, int rows, int start, int count)", @LoadAnimTexture_Strict)
    RegisterGlobalFunction("B3D::Brush@ LoadBrush(string file, int flags, float u=1, float v=1)", @LoadBrush_Strict)
    RegisterGlobalFunction("B3D::Font@ LoadFont(string file, int height, bool bold=false, bool italic=false)", @LoadFont_Strict)
    RegisterGlobalFunction("B3D::Effect@ LoadEffect(string file)", @LoadEffect_Strict)
    ; TODO: Sprite (used)? Terrain (unused)? Image grid (unused)?
End Function

Function RegisterCBAudio()
    RegisterType("Sound")

    RegisterTypeConstructor("Sound", "Sound@ f(string file)", @LoadSound_Strict)
    RegisterObjectMethod("Sound", "void Free()", @FreeSound_Strict)
    
    RegisterObjectMethod("Sound", "B3D::Channel@ Play()", @PlaySound_Strict)


    RegisterType("Stream")
    RegisterTypeConstructor("Stream", "Stream@ f(string file, float volume=1, int customMode=2)", @StreamSound_Strict)
    RegisterObjectMethod("Stream", "void Stop()", @StopStream_Strict)
    
    RegisterObjectMethod("Stream", "void SetVolume(float volume, bool isSFX=false)", @SetStreamVolume_Strict)
    RegisterObjectMethod("Stream", "void SetPan(float pan)", @SetStreamPan_Strict)
    RegisterObjectMethod("Stream", "void UpdateOrigin(B3D::Camera@ cam, B3D::Entity@ entity, float range=10, float volume=1)", @UpdateStreamSoundOrigin)

    RegisterObjectMethod("Stream", "void SetPaused(bool paused)", @SetStreamPaused_Strict)
    RegisterObjectMethod("Stream", "bool get_IsPlaying() property", @IsStreamPlaying_Strict)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Sound") Else SetDefaultNamespace("Sound")

    SetDefaultNamespace(ns)

End Function

Function RegisterCBInput()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Input") Else SetDefaultNamespace("Input")

    RegisterGlobalProperty("int MouseHit1", &MouseHit1)
    RegisterGlobalProperty("bool MouseDown1", &MouseDown1)
    RegisterGlobalProperty("int MouseHit2", &MouseHit2)
    RegisterGlobalProperty("bool DoubleClick", &DoubleClick)
    RegisterGlobalProperty("int LastMouseHit1", &LastMouseHit1)
    RegisterGlobalProperty("int LastMouseHit1X", &LastMouseHit1X)
    RegisterGlobalProperty("int LastMouseHit1Y", &LastMouseHit1Y)
    RegisterGlobalProperty("bool MouseUp1", &MouseUp1)

    SetDefaultNamespace(ns)
End Function

Function RegisterDoor()
    RegisterTypeFromPtr("Door", %Doors)

    RegisterTypeConstructor("Door", "Door@ f(int lvl, float x, float y, float z, float yawAngle, Room@ room, bool startOpen=false, int big=0, int keycard=0, string code=" + Chr(34) + Chr(34) + ", bool useCollisionMesh=false)", @CreateDoor)

    RegisterTypeField("Door", "B3D::Mesh@ Object", %Doors\obj)
    RegisterTypeField("Door", "B3D::Mesh@ Object2", %Doors\obj2)
    RegisterTypeField("Door", "B3D::Mesh@ FrameObject", %Doors\frameobj)
    RegisterTypeField("Door", "carray<B3D::Mesh@> Buttons", %Doors\buttons)

    RegisterTypeField("Door", "bool Locked", %Doors\locked)
    RegisterTypeField("Door", "bool Open", %Doors\open)
    RegisterTypeField("Door", "int Angle", %Doors\angle)
    RegisterTypeField("Door", "float OpenState", %Doors\openstate)
    RegisterTypeField("Door", "int FastOpen", %Doors\fastopen)

    RegisterTypeField("Door", "int Direction", %Doors\dir)
    RegisterTypeField("Door", "int Timer", %Doors\timer)
    RegisterTypeField("Door", "float TimerState", %Doors\timerstate)
    RegisterTypeField("Door", "int KeyCard", %Doors\KeyCard)
    RegisterTypeField("Door", "Room@ Room", %Doors\room)

    RegisterTypeField("Door", "bool DisableWaypoint", %Doors\DisableWaypoint)
    RegisterTypeField("Door", "float Distance", %Doors\dist)
    RegisterTypeField("Door", "B3D::Channel@ Channel", %Doors\SoundCHN)
    RegisterTypeField("Door", "string Code", %Doors\Code)
    RegisterTypeField("Door", "int ID", %Doors\ID)
    RegisterTypeField("Door", "int Level", %Doors\Level) ; TODO: Unused? (See function parameter)
    RegisterTypeField("Door", "bool AutoClose", %Doors\AutoClose)
    RegisterTypeField("Door", "Door@ LinkedDoor", %Doors\LinkedDoor)
    RegisterTypeField("Door", "bool IsElevatorDoor", %Doors\IsElevatorDoor)
    RegisterTypeField("Door", "bool MTFClose", %Doors\MTFClose)
    RegisterTypeField("Door", "bool NPCCalledElevator", %Doors\NPCCalledElevator) ; TODO: Dead code?
    RegisterTypeField("Door", "B3D::Mesh@ DoorHitObject", %Doors\DoorHitOBJ)
End Function

Function RegisterRoomTemplate()
RegisterTypeFromPtr("TempItem", %TempItems)
    RegisterTypeField("TempItem", "string Name", %TempItems\Name)
    RegisterTypeField("TempItem", "float X", %TempItems\X)
    RegisterTypeField("TempItem", "float Y", %TempItems\Y)
    RegisterTypeField("TempItem", "float Z", %TempItems\Z)
    RegisterTypeField("TempItem", "bool HasCustomAngle", %TempItems\HasCustomAngle)
    RegisterTypeField("TempItem", "float AngleX", %TempItems\AngleX)
    RegisterTypeField("TempItem", "float AngleY", %TempItems\AngleY)
    RegisterTypeField("TempItem", "float AngleZ", %TempItems\AngleZ)
    RegisterTypeField("TempItem", "float State", %TempItems\State)
    RegisterTypeField("TempItem", "float State2", %TempItems\State2)
    RegisterTypeField("TempItem", "float Chance", %TempItems\Chance)
    RegisterTypeField("TempItem", "TempItem@ Successor", %TempItems\Successor)

    RegisterTypeFromPtr("TempDoor", %TempDoors)
    RegisterTypeField("TempDoor", "int Dir", %TempDoors\Dir)
    RegisterTypeField("TempDoor", "int KeyCard", %TempDoors\KeyCard)
    RegisterTypeField("TempDoor", "string Code", %TempDoors\Code)
    RegisterTypeField("TempDoor", "float X", %TempDoors\X)
    RegisterTypeField("TempDoor", "float Y", %TempDoors\Y)
    RegisterTypeField("TempDoor", "float Z", %TempDoors\Z)
    RegisterTypeField("TempDoor", "float Angle", %TempDoors\Angle)
    RegisterTypeField("TempDoor", "bool SpawnOpen", %TempDoors\SpawnOpen)
    RegisterTypeField("TempDoor", "bool Locked", %TempDoors\Locked)
    RegisterTypeField("TempDoor", "bool DeleteHalf", %TempDoors\DeleteHalf)
    RegisterTypeField("TempDoor", "bool AllowRemoteControl", %TempDoors\AllowRemoteControl)
    RegisterTypeField("TempDoor", "float Button1PosX", %TempDoors\Button1PosX)
    RegisterTypeField("TempDoor", "float Button1PosY", %TempDoors\Button1PosY)
    RegisterTypeField("TempDoor", "float Button1PosZ", %TempDoors\Button1PosZ)
    RegisterTypeField("TempDoor", "float Button1RotX", %TempDoors\Button1RotX)
    RegisterTypeField("TempDoor", "float Button1RotY", %TempDoors\Button1RotY)
    RegisterTypeField("TempDoor", "float Button1RotZ", %TempDoors\Button1RotZ)
    RegisterTypeField("TempDoor", "float Button2PosX", %TempDoors\Button2PosX)
    RegisterTypeField("TempDoor", "float Button2PosY", %TempDoors\Button2PosY)
    RegisterTypeField("TempDoor", "float Button2PosZ", %TempDoors\Button2PosZ)
    RegisterTypeField("TempDoor", "float Button2RotX", %TempDoors\Button2RotX)
    RegisterTypeField("TempDoor", "float Button2RotY", %TempDoors\Button2RotY)
    RegisterTypeField("TempDoor", "float Button2RotZ", %TempDoors\Button2RotZ)
    RegisterTypeField("TempDoor", "TempDoor@ Successor", %TempDoors\Successor)

    RegisterTypeFromPtr("RoomTemplate", %RoomTemplates)

    RegisterTypeField("RoomTemplate", "B3D::Pivot@ Object", %RoomTemplates\obj)
    RegisterTypeField("RoomTemplate", "int ID", %RoomTemplates\id)
    RegisterTypeField("RoomTemplate", "string ObjectPath", %RoomTemplates\objPath)

    RegisterTypeField("RoomTemplate", "carray<int> Zone", %RoomTemplates\zone)

    RegisterTypeField("RoomTemplate", "carray<int> TempSoundEmitter", %RoomTemplates\TempSoundEmitter)
    RegisterTypeField("RoomTemplate", "carray<float> TempSoundEmitterX", %RoomTemplates\TempSoundEmitterX)
    RegisterTypeField("RoomTemplate", "carray<float> TempSoundEmitterY", %RoomTemplates\TempSoundEmitterY)
    RegisterTypeField("RoomTemplate", "carray<float> TempSoundEmitterZ", %RoomTemplates\TempSoundEmitterZ)
    RegisterTypeField("RoomTemplate", "carray<float> TempSoundEmitterRange", %RoomTemplates\TempSoundEmitterRange)

    RegisterTypeField("RoomTemplate", "int Shape", %RoomTemplates\Shape) ; TODO: Enum
    RegisterTypeField("RoomTemplate", "string Name", %RoomTemplates\Name)
    RegisterTypeField("RoomTemplate", "int Commonness", %RoomTemplates\Commonness)
    RegisterTypeField("RoomTemplate", "int Large", %RoomTemplates\Large)
    RegisterTypeField("RoomTemplate", "float SetRoom", %RoomTemplates\SetRoom)
    RegisterTypeField("RoomTemplate", "int SetRoomPriority", %RoomTemplates\SetRoomPriority)
    RegisterTypeField("RoomTemplate", "int DisableDecals", %RoomTemplates\DisableDecals)

    RegisterTypeField("RoomTemplate", "int R", %RoomTemplates\R)
    RegisterTypeField("RoomTemplate", "int G", %RoomTemplates\G)
    RegisterTypeField("RoomTemplate", "int B", %RoomTemplates\B)

    RegisterTypeField("RoomTemplate", "int TempTriggerboxAmount", %RoomTemplates\TempTriggerboxAmount)
    RegisterTypeField("RoomTemplate", "carray<B3D::Mesh@> TempTriggerbox", %RoomTemplates\TempTriggerbox)
    RegisterTypeField("RoomTemplate", "carray<string> TempTriggerboxName", %RoomTemplates\TempTriggerboxName)

    RegisterTypeField("RoomTemplate", "TempItem@ FirstTempItem", %RoomTemplates\FirstTempItem)
    RegisterTypeField("RoomTemplate", "TempDoor@ FirstTempDoor", %RoomTemplates\FirstTempDoor)

    RegisterTypeField("RoomTemplate", "bool UseLightCones", %RoomTemplates\UseLightCones)
    RegisterTypeField("RoomTemplate", "bool DisableOverlapCheck", %RoomTemplates\DisableOverlapCheck)

    RegisterTypeField("RoomTemplate", "float MinX", %RoomTemplates\MinX)
    RegisterTypeField("RoomTemplate", "float MinY", %RoomTemplates\MinY)
    RegisterTypeField("RoomTemplate", "float MinZ", %RoomTemplates\MinZ)
    RegisterTypeField("RoomTemplate", "float MaxX", %RoomTemplates\MaxX)
    RegisterTypeField("RoomTemplate", "float MaxY", %RoomTemplates\MaxY)
    RegisterTypeField("RoomTemplate", "float MaxZ", %RoomTemplates\MaxZ)

    RegisterTypeFromPtr("TempWaypoint", %TempWayPoints)
    RegisterTypeField("TempWaypoint", "float X", %TempWayPoints\x)
    RegisterTypeField("TempWaypoint", "float Y", %TempWayPoints\y)
    RegisterTypeField("TempWaypoint", "float Z", %TempWayPoints\z)
    RegisterTypeField("TempWaypoint", "RoomTemplate@ RoomTemplate", %TempWayPoints\roomtemplate)
End Function

Function RegisterWayPoint()
    RegisterTypeConstructor("Waypoint", "Waypoint@ f(float x, float y, float z, Door@ door, Room@ room)", @CreateWaypoint)
    RegisterTypeField("Waypoint", "B3D::Pivot@ Object", %WayPoints\obj)
    RegisterTypeField("Waypoint", "Door@ Door", %WayPoints\door)
    RegisterTypeField("Waypoint", "Room@ Room", %WayPoints\room)
    RegisterTypeField("Waypoint", "int State", %WayPoints\state)
    RegisterTypeField("Waypoint", "carray<Waypoint@> Connected", %WayPoints\connected)
    RegisterTypeField("Waypoint", "carray<float> Distance", %WayPoints\dist)
    RegisterTypeField("Waypoint", "float FCost", %WayPoints\Fcost)
    RegisterTypeField("Waypoint", "float GCost", %WayPoints\Gcost)
    RegisterTypeField("Waypoint", "float HCost", %WayPoints\Hcost)
    RegisterTypeField("Waypoint", "Waypoint@ Parent", %WayPoints\parent)
End Function

Function RegisterForest()
    RegisterTypeFromPtr("Forest", %Forest)

    ; TODO: These types are fucked up but this appears to be accurate to how they're assigned.
    RegisterTypeField("Forest", "carray<B3D::Mesh@> TileMesh", %Forest\TileMesh)
    RegisterTypeField("Forest", "carray<B3D::Entity@> DetailMesh", %Forest\DetailMesh)
    RegisterTypeField("Forest", "carray<int> Grid", %Forest\grid)
    RegisterTypeField("Forest", "carray<B3D::Mesh@> TileEntities", %Forest\TileEntities)
    RegisterTypeField("Forest", "B3D::Pivot@ Pivot", %Forest\Forest_Pivot)

    RegisterTypeField("Forest", "carray<B3D::Pivot@> Door", %Forest\Door)
    RegisterTypeField("Forest", "carray<B3D::Mesh@> DetailEntities", %Forest\DetailEntities)
End Function

Function RegisterMaintenanceTunnel()
    RegisterTypeFromPtr("MaintenanceTunnel", %Grids)

    RegisterTypeField("MaintenanceTunnel", "carray<int> Grid", %Grids\grid)
    RegisterTypeField("MaintenanceTunnel", "carray<int> Angles", %Grids\angles)
    RegisterTypeField("MaintenanceTunnel", "carray<B3D::Pivot@> Meshes", %Grids\Meshes)
    RegisterTypeField("MaintenanceTunnel", "carray<B3D::Pivot@> Entities", %Grids\Entities)
    RegisterTypeField("MaintenanceTunnel", "carray<Waypoint@> Waypoints", %Grids\waypoints)
End Function

Function RegisterNPC()
    RegisterTypeFromPtr("NPC", %NPCs)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::NPC") Else SetDefaultNamespace("NPC")

    RegisterEnum("Type")
    RegisterEnumValue("Type", "SCP173", 1)
    RegisterEnumValue("Type", "SCP106", 2)
    RegisterEnumValue("Type", "Guard", 3)
    RegisterEnumValue("Type", "ClassD", 4)
    RegisterEnumValue("Type", "SCP372", 6)
    RegisterEnumValue("Type", "Apache", 7)
    RegisterEnumValue("Type", "MTF", 8)
    RegisterEnumValue("Type", "SCP096", 9)
    RegisterEnumValue("Type", "SCP049", 10)
    RegisterEnumValue("Type", "Zombie", 11)
    RegisterEnumValue("Type", "SCP5131", 12)
    RegisterEnumValue("Type", "Tentacle", 13)
    RegisterEnumValue("Type", "SCP860", 14)
    RegisterEnumValue("Type", "SCP939", 15)
    RegisterEnumValue("Type", "SCP066", 16)
    RegisterEnumValue("Type", "PDPlane", 17)
    RegisterEnumValue("Type", "SCP966", 18)
    RegisterEnumValue("Type", "SCP1048a", 19)
    RegisterEnumValue("Type", "SCP1499", 20)
    RegisterEnumValue("Type", "SCP008", 21)
    RegisterEnumValue("Type", "Clerk", 22)

    ;RegisterGlobalFunction("Type RegisterNewNPCType()", @RegisterNewNPCType) ; TODO

    RegisterGlobalProperty("NPC@ Current173", &Curr173)
    RegisterGlobalProperty("NPC@ Current106", &Curr106)
    RegisterGlobalProperty("NPC@ Current096", &Curr096)
    RegisterGlobalProperty("NPC@ Current5131", &Curr5131)

    SetDefaultNamespace(ns)

    ; Replace first argument with some other type to enforce having to register modded NPC types.
    RegisterTypeConstructor("NPC", "NPC@ f(NPC::Type type, float x, float y, float z)", @CreateNPC)
    RegisterObjectMethod("NPC", "void Remove()", @RemoveNPC)

    RegisterTypeField("NPC", "B3D::Entity@ Object", %NPCs\obj)
    RegisterTypeField("NPC", "B3D::Entity@ Object2", %NPCs\obj2)
    RegisterTypeField("NPC", "B3D::Entity@ Object3", %NPCs\obj3)
    RegisterTypeField("NPC", "B3D::Entity@ Object4", %NPCs\obj4)
    RegisterTypeField("NPC", "B3D::Pivot@ Collider", %NPCs\Collider)

    RegisterTypeField("NPC", "NPC::Type NPCType", %NPCs\NPCtype)
    RegisterTypeField("NPC", "int ID", %NPCs\ID)

    RegisterTypeField("NPC", "float DropSpeed", %NPCs\DropSpeed)
    RegisterTypeField("NPC", "int Gravity", %NPCs\Gravity)

    RegisterTypeField("NPC", "float State", %NPCs\State)
    RegisterTypeField("NPC", "float State2", %NPCs\State2)
    RegisterTypeField("NPC", "float State3", %NPCs\State3)
    RegisterTypeField("NPC", "int PrevState", %NPCs\PrevState)

    RegisterTypeField("NPC", "int MakingNoise", %NPCs\MakingNoise)

    RegisterTypeField("NPC", "float Frame", %NPCs\Frame)

    RegisterTypeField("NPC", "float Angle", %NPCs\Angle)

    RegisterTypeField("NPC", "B3D::Sound@ Sound", %NPCs\Sound)
    RegisterTypeField("NPC", "B3D::Channel@ Channel", %NPCs\SoundChn)
    RegisterTypeField("NPC", "float SoundTimer", %NPCs\SoundTimer)

    RegisterTypeField("NPC", "B3D::Sound@ Sound2", %NPCs\Sound2)
    RegisterTypeField("NPC", "B3D::Channel@ Channel2", %NPCs\SoundChn2)

    RegisterTypeField("NPC", "float Speed", %NPCs\Speed)
    RegisterTypeField("NPC", "float CurrentSpeed", %NPCs\CurrSpeed)

    RegisterTypeField("NPC", "string Texture", %NPCs\texture)

    RegisterTypeField("NPC", "float Idle", %NPCs\Idle)

    RegisterTypeField("NPC", "float Reload", %NPCs\Reload)

    RegisterTypeField("NPC", "int LastSeen", %NPCs\LastSeen)
    RegisterTypeField("NPC", "float LastDistance", %NPCs\LastDist)

    RegisterTypeField("NPC", "float PrevX", %NPCs\PrevX)
    RegisterTypeField("NPC", "float PrevY", %NPCs\PrevY)
    RegisterTypeField("NPC", "float PrevZ", %NPCs\PrevZ)

    RegisterTypeField("NPC", "NPC@ Target", %NPCs\Target)
    RegisterTypeField("NPC", "int TargetID", %NPCs\TargetID)

    RegisterTypeField("NPC", "float EnemyX", %NPCs\EnemyX)
    RegisterTypeField("NPC", "float EnemyY", %NPCs\EnemyY)
    RegisterTypeField("NPC", "float EnemyZ", %NPCs\EnemyZ)

    RegisterTypeField("NPC", "carray<Waypoint@> Path", %NPCs\Path)
    RegisterTypeField("NPC", "int PathStatus", %NPCs\PathStatus)
    RegisterTypeField("NPC", "float PathTimer", %NPCs\PathTimer)
    RegisterTypeField("NPC", "int PathLocation", %NPCs\PathLocation)

    RegisterTypeField("NPC", "float NVX", %NPCs\NVX)
    RegisterTypeField("NPC", "float NVY", %NPCs\NVY)
    RegisterTypeField("NPC", "float NVZ", %NPCs\NVZ)
    RegisterTypeField("NPC", "string NVName", %NPCs\NVName)

    RegisterTypeField("NPC", "float GravityMultiplier", %NPCs\GravityMult)
    RegisterTypeField("NPC", "float MaxGravity", %NPCs\MaxGravity)

    RegisterTypeField("NPC", "int MTFVariant", %NPCs\MTFVariant)
    RegisterTypeField("NPC", "NPC@ MTFLeader", %NPCs\MTFLeader)
    RegisterTypeField("NPC", "int IsDead", %NPCs\IsDead)
    RegisterTypeField("NPC", "float BlinkTimer", %NPCs\BlinkTimer)
    RegisterTypeField("NPC", "int IgnorePlayer", %NPCs\IgnorePlayer)

    RegisterTypeField("NPC", "int ManipulateBone", %NPCs\ManipulateBone)
    RegisterTypeField("NPC", "int ManipulationType", %NPCs\ManipulationType)
    RegisterTypeField("NPC", "string BoneToManipulate", %NPCs\BoneToManipulate)
    RegisterTypeField("NPC", "float BonePitch", %NPCs\BonePitch)
    RegisterTypeField("NPC", "float BoneYaw", %NPCs\BoneYaw)
    RegisterTypeField("NPC", "float BoneRoll", %NPCs\BoneRoll)

    RegisterTypeField("NPC", "string NPCNameInSection", %NPCs\NPCNameInSection)
    RegisterTypeField("NPC", "int InFacility", %NPCs\InFacility)
    RegisterTypeField("NPC", "int CanUseElevator", %NPCs\CanUseElevator)
    ; TODO: Unused, remove?
    ;RegisterTypeField("NPC", "ElevatorObj@ CurrElevator", 60)
    RegisterTypeField("NPC", "int HP", %NPCs\HP)

    RegisterTypeField("NPC", "float PathX", %NPCs\PathX)
    RegisterTypeField("NPC", "float PathZ", %NPCs\PathZ)

    RegisterTypeField("NPC", "string Model", %NPCs\Model)

    RegisterTypeField("NPC", "float ModelScaleX", %NPCs\ModelScaleX)
    RegisterTypeField("NPC", "float ModelScaleY", %NPCs\ModelScaleY)
    RegisterTypeField("NPC", "float ModelScaleZ", %NPCs\ModelScaleZ)

    RegisterTypeField("NPC", "int HideFromNVG", %NPCs\HideFromNVG)
    RegisterTypeField("NPC", "int TextureID", %NPCs\TextureID)
    RegisterTypeField("NPC", "float CollRadius", %NPCs\CollRadius)
    RegisterTypeField("NPC", "float IdleTimer", %NPCs\IdleTimer)

    RegisterTypeField("NPC", "int ChannelIsStream", %NPCs\SoundChn_IsStream)
    RegisterTypeField("NPC", "int Channel2IsStream", %NPCs\SoundChn2_IsStream)

    RegisterTypeField("NPC", "float FallingPickDistance", %NPCs\FallingPickDistance)

    RegisterObjectMethod("NPC", "void TeleportCloser()", @TeleportCloser)
    RegisterObjectMethod("NPC", "void TeleportMTFGroup()", @TeleportMTFGroup)
    
    RegisterObjectMethod("NPC", "int FindPath(float x, float y, float z)", @FindPath)
    RegisterObjectMethod("NPC", "int IsInFacility()", @CheckForNPCInFacility) ; 2 means the NPC is in the tunnels
    RegisterObjectMethod("NPC", "void FindNextElevator()", @FindNextElevator)
    RegisterObjectMethod("NPC", "void GoToElevator()", @GoToElevator)
    
    RegisterObjectMethod("NPC", "bool SeesNPC(NPC@ other)", @OtherNPCSeesMeNPC)
    RegisterObjectMethod("NPC", "bool SeesPlayer(bool disableSoundOnCrouch=false)", @MeNPCSeesPlayer)
    RegisterObjectMethod("NPC", "bool PlayerSees096Face()", @Sees096Face)

    RegisterObjectMethod("NPC", "void Animate(float startFrame, float endFrame, float speed, bool loop=true)", @AnimateNPC)
    RegisterObjectMethod("NPC", "void SetNPCFrame(float frame)", @SetNPCFrame)
    RegisterObjectMethod("NPC", "void FinishWalking(float startFrame, float endFrame, float speed)", @FinishWalking)
    RegisterObjectMethod("NPC", "void ChangeNPCTexture(int textureID)", @ChangeNPCTextureID)
End Function

Function RegisterMap()
    RegisterTypeFromPtr("Room", %Rooms)

    RegisterDoor()
    RegisterRoomTemplate()
    RegisterWayPoint()
    RegisterForest()
    RegisterMaintenanceTunnel()

    RegisterTypeConstructor("Room", "Room@ f(int zone, int shape, float x, float y, float z, int angle, string name)", @CreateRoom)

    RegisterTypeField("Room", "int Zone", %Rooms\zone)
    RegisterTypeField("Room", "int Found", %Rooms\found)
    RegisterTypeField("Room", "B3D::Pivot@ Object", %Rooms\obj)
    RegisterTypeField("Room", "float X", %Rooms\x)
    RegisterTypeField("Room", "float Y", %Rooms\y)
    RegisterTypeField("Room", "float Z", %Rooms\z)
    RegisterTypeField("Room", "int Angle", %Rooms\angle)
    RegisterTypeField("Room", "RoomTemplate@ Template", %Rooms\RoomTemplate)
    RegisterTypeField("Room", "float Distance", %Rooms\dist)
    RegisterTypeField("Room", "B3D::Channel@ SoundCHN", %Rooms\SoundCHN)
    RegisterTypeField("Room", "Forest@ Forest", %Rooms\fr)

    RegisterTypeField("Room", "carray<int> SoundEmitter", %Rooms\SoundEmitter)
    RegisterTypeField("Room", "carray<B3D::Pivot@> SoundEmitterObj", %Rooms\SoundEmitterObj)
    RegisterTypeField("Room", "carray<float> SoundEmitterRange", %Rooms\SoundEmitterRange)
    RegisterTypeField("Room", "carray<B3D::Channel@> SoundEmitterCHN", %Rooms\SoundEmitterCHN)

    RegisterTypeField("Room", "carray<B3D::Light@> Lights", %Rooms\Lights)
    RegisterTypeField("Room", "carray<float> LightIntensity", %Rooms\LightIntensity)
    RegisterTypeField("Room", "carray<B3D::Sprite@> LightSprites", %Rooms\LightSprites)

    RegisterTypeField("Room", "carray<B3D::Entity@> Objects", %Rooms\Objects)
    RegisterTypeField("Room", "carray<B3D::Entity@> Levers", %Rooms\Levers)
    RegisterTypeField("Room", "carray<Door@> Doors", %Rooms\RoomDoors)
    RegisterTypeField("Room", "carray<NPC@> NPC", %Rooms\NPC)

    RegisterTypeField("Room", "MaintenanceTunnel@ MaintenanceTunnel", %Rooms\grid)

    RegisterTypeField("Room", "carray<Room@> Adjacent", %Rooms\Adjacent)
    RegisterTypeField("Room", "carray<Door@> AdjacentDoor", %Rooms\AdjDoor)

    RegisterTypeField("Room", "carray<B3D::Texture@> Textures", %Rooms\Textures)

    RegisterTypeField("Room", "int MaxLights", %Rooms\MaxLights)

    RegisterTypeField("Room", "carray<int> LightSpriteHidden", %Rooms\LightSpriteHidden)
    RegisterTypeField("Room", "carray<B3D::Pivot@> LightSpritesPivot", %Rooms\LightSpritesPivot)
    RegisterTypeField("Room", "carray<B3D::Sprite@> LightSprites2", %Rooms\LightSprites2)
    RegisterTypeField("Room", "carray<int> LightHidden", %Rooms\LightHidden)
    RegisterTypeField("Room", "carray<int> LightFlicker", %Rooms\LightFlicker)

    RegisterTypeField("Room", "int TriggerboxAmount", %Rooms\TriggerboxAmount)

    RegisterTypeField("Room", "carray<B3D::Mesh@> Triggerbox", %Rooms\Triggerbox)
    RegisterTypeField("Room", "carray<string> TriggerboxName", %Rooms\TriggerboxName)

    RegisterTypeField("Room", "float MaxWayPointY", %Rooms\MaxWayPointY)

    RegisterTypeField("Room", "carray<float> LightR", %Rooms\LightR)
    RegisterTypeField("Room", "carray<float> LightG", %Rooms\LightG)
    RegisterTypeField("Room", "carray<float> LightB", %Rooms\LightB)
    RegisterTypeField("Room", "carray<B3D::Mesh@> LightCone", %Rooms\LightCone)
    RegisterTypeField("Room", "carray<B3D::Sprite@> LightConeSpark", %Rooms\LightConeSpark)
    RegisterTypeField("Room", "carray<float> LightConeSparkTimer", %Rooms\LightConeSparkTimer)

    RegisterTypeField("Room", "float MinX", %Rooms\MinX)
    RegisterTypeField("Room", "float MinY", %Rooms\MinY)
    RegisterTypeField("Room", "float MinZ", %Rooms\MinZ)
    RegisterTypeField("Room", "float MaxX", %Rooms\MaxX)
    RegisterTypeField("Room", "float MaxY", %Rooms\MaxY)
    RegisterTypeField("Room", "float MaxZ", %Rooms\MaxZ)

    RegisterGlobalFunction("B3D::Pivot@ LoadRMesh(string file, RoomTemplate@ template=null)", @LoadRMesh)
End Function

Function RegisterDecal()
    RegisterTypeFromPtr("Decal", %Decals)

    ; TODO: Ability to register custom decal textures.
    RegisterTypeConstructor("Decal", "Decal@ f(int id, float x, float y, float z, float pitch, float yaw, float roll)", @CreateDecal)

    RegisterTypeField("Decal", "B3D::Sprite@ Object", %Decals\obj)
    RegisterTypeField("Decal", "float SizeChange", %Decals\SizeChange)
    RegisterTypeField("Decal", "float Size", %Decals\Size)
    RegisterTypeField("Decal", "float MaxSize", %Decals\MaxSize)
    RegisterTypeField("Decal", "float AlphaChange", %Decals\AlphaChange)
    RegisterTypeField("Decal", "float Alpha", %Decals\Alpha)
    RegisterTypeField("Decal", "int BlendMode", %Decals\blendmode)
    RegisterTypeField("Decal", "int FX", %Decals\fx)
    RegisterTypeField("Decal", "int ID", %Decals\ID)
    RegisterTypeField("Decal", "float Timer", %Decals\Timer)

    RegisterTypeField("Decal", "float Lifetime", %Decals\lifetime)

    RegisterTypeField("Decal", "float X", %Decals\x)
    RegisterTypeField("Decal", "float Y", %Decals\y)
    RegisterTypeField("Decal", "float Z", %Decals\z)
    RegisterTypeField("Decal", "float Pitch", %Decals\pitch)
    RegisterTypeField("Decal", "float Yaw", %Decals\yaw)
    RegisterTypeField("Decal", "float Roll", %Decals\roll)
End Function

Function RegisterItem()
    RegisterTypeFromPtr("ItemTemplate", %ItemTemplates)

    RegisterTypeField("ItemTemplate", "string DisplayName", %ItemTemplates\displayname)
    RegisterTypeField("ItemTemplate", "string Name", %ItemTemplates\name)
    RegisterTypeField("ItemTemplate", "string Group", %ItemTemplates\group)
    RegisterTypeField("ItemTemplate", "int Sound", %ItemTemplates\sound)
    RegisterTypeField("ItemTemplate", "bool Found", %ItemTemplates\found)
    RegisterTypeField("ItemTemplate", "B3D::Mesh@ Object", %ItemTemplates\obj)
    RegisterTypeField("ItemTemplate", "string ObjectPath", %ItemTemplates\objpath)
    RegisterTypeField("ItemTemplate", "B3D::Image@ InventoryImage", %ItemTemplates\invimg)
    RegisterTypeField("ItemTemplate", "B3D::Image@ InventoryImage2", %ItemTemplates\invimg2)
    RegisterTypeField("ItemTemplate", "string InventoryImagePath", %ItemTemplates\invimgpath)
    RegisterTypeField("ItemTemplate", "string ImagePath", %ItemTemplates\imgpath)
    RegisterTypeField("ItemTemplate", "B3D::Image@ Image", %ItemTemplates\img)
    RegisterTypeField("ItemTemplate", "bool IsAnimated", %ItemTemplates\isAnim)
    RegisterTypeField("ItemTemplate", "float Scale", %ItemTemplates\scale)
    RegisterTypeField("ItemTemplate", "B3D::Texture@ Texture", %ItemTemplates\tex)
    RegisterTypeField("ItemTemplate", "string TexturePath", %ItemTemplates\texpath)

    RegisterTypeFromPtr("Item", %Items)
    RegisterTypeFromPtr("Inventory", %Inventories)

    RegisterTypeField("Inventory", "carray<Item@> Items", %Inventories\Items)
    RegisterTypeField("Inventory", "int Size", %Inventories\Size)


    RegisterTypeField("Item", "string DisplayName", %Items\displayname)
    RegisterTypeField("Item", "B3D::Pivot@ Collider", %Items\collider)
    RegisterTypeField("Item", "B3D::Mesh@ Model", %Items\model)
    RegisterTypeField("Item", "ItemTemplate@ Template", %Items\itemtemplate)
    RegisterTypeField("Item", "float DropSpeed", %Items\DropSpeed)
    RegisterTypeField("Item", "int R", %Items\r)
    RegisterTypeField("Item", "int G", %Items\g)
    RegisterTypeField("Item", "int B", %Items\b)
    RegisterTypeField("Item", "float A", %Items\a)
    RegisterTypeField("Item", "B3D::Channel@ Channel", %Items\SoundChn)
    RegisterTypeField("Item", "float Distance", %Items\dist)
    RegisterTypeField("Item", "float DistanceTimer", %Items\disttimer)
    RegisterTypeField("Item", "float State", %Items\state)
    RegisterTypeField("Item", "float State2", %Items\state2)
    RegisterTypeField("Item", "bool Picked", %Items\Picked)
    RegisterTypeField("Item", "int Dropped", %Items\Dropped)
    RegisterTypeField("Item", "B3D::Image@ InventoryImage", %Items\invimg)
    RegisterTypeField("Item", "float XSpeed", %Items\xspeed)
    RegisterTypeField("Item", "float ZSpeed", %Items\zspeed)
    RegisterTypeField("Item", "Inventory@ Inventory", %Items\Inventory)
    RegisterTypeField("Item", "int ID", %Items\ID)
    RegisterTypeField("Item", "string DrinkName", %Items\drinkName)

    RegisterTypeConstructor("Item", "Item@ f(string name, float x, float y, float z)", @CreateItem)
    RegisterObjectMethod("Item", "void Remove(bool inGame=true)", @RemoveItem)
    RegisterObjectMethod("Item", "void Pick()", @PickItem)
    RegisterObjectMethod("Item", "void Drop(bool playDropSound=true)", @DropItem)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Item") Else SetDefaultNamespace("Item")
    RegisterGlobalProperty("int LastItemID", &LastItemID)
    RegisterGlobalFunction("ItemTemplate@ FindTemplate(string name)", @FindItemTemplate)
    RegisterGlobalFunction("Item@ CreateCup(string drinkName, float x, float y, float z, int r, int g, int b, float a=1)", @CreateCup)
    SetDefaultNamespace(ns)
End Function

Function RegisterPlayer()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Player") Else SetDefaultNamespace("Player")
    
    RegisterGlobalProperty("B3D::Pivot@ Collider", &Collider)
    RegisterGlobalProperty("B3D::Camera@ Camera", &Camera)
    RegisterGlobalProperty("B3D::Pivot@ Head", &Head)

    RegisterGlobalProperty("float KillTimer", &KillTimer)
    RegisterGlobalProperty("int KillAnimation", &KillAnim)
    RegisterGlobalProperty("float FallTimer", &FallTimer)
    RegisterGlobalProperty("float DeathTimer", &DeathTimer)

    RegisterGlobalProperty("float Sanity", &Sanity)
    RegisterGlobalProperty("float ForceMove", &ForceMove)
    RegisterGlobalProperty("float ForceAngle", &ForceAngle)
    RegisterGlobalProperty("int RestoreSanity", &RestoreSanity)

    RegisterGlobalProperty("bool Playable", &Playable)

    RegisterGlobalProperty("float BlinkFrequency", &BLINKFREQ)
    RegisterGlobalProperty("float BlinkTimer", &BlinkTimer)
    RegisterGlobalProperty("float EyeIrritation", &EyeIrritation)
    RegisterGlobalProperty("float EyeStuck", &EyeStuck)
    RegisterGlobalProperty("float BlinkEffect", &BlinkEffect)
    RegisterGlobalProperty("float BlinkEffectTimer", &BlinkEffectTimer)

    RegisterGlobalProperty("float Stamina", &Stamina)
    RegisterGlobalProperty("float StaminaEffect", &StaminaEffect)
    RegisterGlobalProperty("float StaminaEffectTimer", &StaminaEffectTimer)

    RegisterGlobalProperty("float CameraShakeTimer", &CameraShakeTimer)
    RegisterGlobalProperty("bool Vomit", &Vomit)
    RegisterGlobalProperty("float VomitTimer", &VomitTimer)
    RegisterGlobalProperty("int Regurgitate", &Regurgitate)

    RegisterGlobalProperty("carray<float> SCP1025State", &SCP1025state)

    RegisterGlobalProperty("float HeartBeatRate", &HeartBeatRate)
    RegisterGlobalProperty("float HeartBeatTimer", &HeartBeatTimer)
    RegisterGlobalProperty("float HeartBeatVolume", &HeartBeatVolume)

    RegisterGlobalProperty("int WearingGasMask", &WearingGasMask)
    RegisterGlobalProperty("int WearingHazmat", &WearingHazmat)
    RegisterGlobalProperty("int WearingVest", &WearingVest)
    RegisterGlobalProperty("int Wearing714", &Wearing714)
    RegisterGlobalProperty("int WearingNightVision", &WearingNightVision)
    RegisterGlobalProperty("float NVTimer", &NVTimer)

    RegisterGlobalProperty("bool SuperMan", &SuperMan)
    RegisterGlobalProperty("float SuperManTimer", &SuperManTimer)

    RegisterGlobalProperty("float Injuries", &Injuries)
    RegisterGlobalProperty("float Bloodloss", &Bloodloss)
    RegisterGlobalProperty("float Infect", &Infect)
    RegisterGlobalProperty("float HealTimer", &HealTimer)

    RegisterGlobalProperty("int RefinedItems", &RefinedItems)

    RegisterGlobalProperty("float DropSpeed", &DropSpeed)
    RegisterGlobalProperty("float HeadDropSpeed", &HeadDropSpeed)
    RegisterGlobalProperty("float CurrentSpeed", &CurrSpeed)
    RegisterGlobalProperty("float UserCameraPitch", &user_camera_pitch)
    RegisterGlobalProperty("float Side", &side)
    RegisterGlobalProperty("bool Crouch", &Crouch)
    RegisterGlobalProperty("float CrouchState", &CrouchState)

    RegisterGlobalProperty("int PlayerZone", &PlayerZone)
    RegisterGlobalProperty("Room@ CurrentRoom", &PlayerRoom)

    RegisterGlobalProperty("int GrabbedEntity", &GrabbedEntity)

    RegisterGlobalProperty("bool GodMode", &GodMode)
    RegisterGlobalProperty("bool NoClip", &NoClip)
    RegisterGlobalProperty("float NoClipSpeed", &NoClipSpeed)

    RegisterGlobalProperty("float CoffinDistance", &CoffinDistance)

    RegisterGlobalProperty("float PlayerSoundVolume", &PlayerSoundVolume)

    RegisterGlobalProperty("int ItemAmount", &ItemAmount)
    RegisterGlobalProperty("Item@ SelectedItem", &SelectedItem)
    RegisterGlobalProperty("Item@ ClosestItem", &ClosestItem)

    RegisterGlobalProperty("bool InventoryOpen", &InvOpen)
    RegisterGlobalProperty("Item@ OpenInventoryItem", &OtherOpen)

    RegisterGlobalProperty("float MessageTimer", &MsgTimer)
    RegisterGlobalProperty("string Message", &Msg)
    RegisterGlobalProperty("string DeathMessage", &DeathMsg)

    SetDefaultNamespace(ns)
End Function

Function RegisterConsole()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Console") Else SetDefaultNamespace("Console")

    RegisterTypeFromPtr("Msg", %ConsoleMsg)
    RegisterTypeField("Msg", "string Text", %ConsoleMsg\txt)
    RegisterTypeField("Msg", "bool IsCommand", %ConsoleMsg\isCommand)
    RegisterTypeField("Msg", "int R", %ConsoleMsg\r)
    RegisterTypeField("Msg", "int G", %ConsoleMsg\g)
    RegisterTypeField("Msg", "int B", %ConsoleMsg\b)

    RegisterGlobalProperty("bool Open", &ConsoleOpen)
    RegisterGlobalProperty("string Input", &ConsoleInput)
    RegisterGlobalProperty("float Scroll", &ConsoleScroll)
    RegisterGlobalProperty("bool ScrollDragging", &ConsoleScrollDragging)
    
    RegisterGlobalProperty("int MouseMemory", &ConsoleMouseMem)
    RegisterGlobalProperty("Msg@ Reissue", &ConsoleReissue)
    
    RegisterGlobalProperty("int R", &ConsoleR)
    RegisterGlobalProperty("int G", &ConsoleG)
    RegisterGlobalProperty("int B", &ConsoleB)

    RegisterGlobalFunction("void CreateMessage(string txt, int r=-1, int g=-1, int b=-1, bool isCommand=false)", @CreateConsoleMsg)

    SetDefaultNamespace(ns)
End Function

Function RegisterEvent()
    RegisterTypeFromPtr("Event", %Events)
    RegisterTypeField("Event", "string Name", %Events\EventName)
    RegisterTypeField("Event", "Room@ Room", %Events\room)
    RegisterTypeField("Event", "float State", %Events\EventState)
    RegisterTypeField("Event", "float State2", %Events\EventState2)
    RegisterTypeField("Event", "float State3", %Events\EventState3)
    RegisterTypeField("Event", "B3D::Channel@ Channel", %Events\SoundCHN)
    RegisterTypeField("Event", "B3D::Channel@ Channel2", %Events\SoundCHN2)
    RegisterTypeField("Event", "int ChannelIsStream", %Events\SoundCHN_isStream)
    RegisterTypeField("Event", "int Channel2IsStream", %Events\SoundCHN2_isStream)
    RegisterTypeField("Event", "string String", %Events\EventStr)
    RegisterTypeField("Event", "B3D::Image@ Image", %Events\img)

    RegisterGlobalFunction("Event@ CreateEvent(string eventName, string roomName, int id, float probability=0)", @CreateEvent)

    RegisterGlobalFunction("float UpdateElevator(float state, Door@ door1, Door@ door2, B3D::Entity@ room1, B3D::Entity@ room2, Event@ event, bool ignorerotation = true)", @UpdateElevators)
End Function

Global HasRegisteredCB%
Function RegisterCB()
    If HasRegisteredCB Then Return
    HasRegisteredCB = True

    SetDefaultNamespace("CB")
    RegisterCommon()
    RegisterOptions()
    RegisterIO()
    RegisterCBAudio()
    RegisterCBInput()
    ; Fucking ugly but the dependencies have a ton of circles.
    RegisterTypeFromPtr("Waypoint", %WayPoints)
    RegisterNPC()
    RegisterMap()
    RegisterDecal()
    RegisterItem()
    RegisterPlayer()
    RegisterConsole()
    RegisterEvent()
    SetDefaultNamespace("")
End Function

RegisterCB()
