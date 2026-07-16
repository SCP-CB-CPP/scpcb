Function RegisterCommon()
    RegisterGlobalProperty("float FPSFactor", &FPSFactor)

    RegisterGlobalProperty("bool HasNumericSeed", &HasNumericSeed)
    RegisterGlobalProperty("string RandomSeed", &RandomSeed)
    RegisterGlobalProperty("int RandomSeedNumeric", &RandomSeedNumeric)

    RegisterGlobalFunction("bool IsPaused()", @IsPaused)

    RegisterGlobalProperty("int TimerStopped", &TimerStopped)
    RegisterGlobalProperty("int PlayTime", &PlayTime)

    RegisterGlobalFunction("string FormatDuration(int ms, bool highPrecision=true)", @FormatDuration)
End Function

Function RegisterMenu()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Menu") Else SetDefaultNamespace("Menu")

    RegisterGlobalProperty("const float Scale", &MenuScale)

    RegisterGlobalProperty("const bool IsMainMenuOpen", &MainMenuOpen)
    RegisterGlobalProperty("int MainMenuTab", &MainMenuTab)
    
    RegisterGlobalFunction("string InputBox(int x, int y, int width, int height, string text, int id=0, int virtualKeyboardMode=0)", @InputBox)
    RegisterGlobalFunction("void DrawFrame(int x, int y, int width, int height, int xOffset=0, int yOffset=0, bool scrollY=true)", @DrawFrame)
    RegisterGlobalFunction("bool DrawButton(int x, int y, int width, int height, string text, bool bigFont=true, bool waitForMouseUp=false, bool disabled=false)", @DrawButton)
    RegisterGlobalFunction("bool DrawTick(int x, int y, bool selected, bool locked=false)", @DrawTick)

    RegisterGlobalProperty("int CurrentOpen", &MenuOpen)
    RegisterGlobalProperty("float StopHidingTimer", &StopHidingTimer)

    RegisterGlobalFunction("bool IsAnyOpen()", @IsAnyMenuOpen)

    RegisterGlobalFunction("void StartNewGame()", @StartNewGame)

    SetDefaultNamespace(ns)
End Function

Function RegisterLoadingScreens()
    RegisterTypeFromPtr("LoadingScreen", %LoadingScreens)

    RegisterTypeField("LoadingScreen", "string ImagePath", %LoadingScreens\imgpath)
    RegisterTypeField("LoadingScreen", "B3D::Image@ Image", %LoadingScreens\img)
    RegisterTypeField("LoadingScreen", "int ID", %LoadingScreens\ID)
    RegisterTypeField("LoadingScreen", "int Title", %LoadingScreens\Title)
    RegisterTypeField("LoadingScreen", "int AlignX", %LoadingScreens\alignx)
    RegisterTypeField("LoadingScreen", "int AlignY", %LoadingScreens\aligny)
    RegisterTypeField("LoadingScreen", "bool DisableBackground", %LoadingScreens\DisableBackground)
    RegisterTypeField("LoadingScreen", "carray<string> Text", %LoadingScreens\txt)
    RegisterTypeField("LoadingScreen", "int TextAmount", %LoadingScreens\txtamount)
    RegisterTypeField("LoadingScreen", "carray<int> TextDelay", %LoadingScreens\txtDelay)
    RegisterTypeField("LoadingScreen", "int TotalTextDelay", %LoadingScreens\totalTxtDelay)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::LoadingScreen") Else SetDefaultNamespace("LoadingScreen")

    RegisterGlobalFunction("void Initialize()", @InitLoadingScreens)
    RegisterGlobalFunction("void LoadFromFile(string file)", @LoadLoadingScreens)
    RegisterGlobalFunction("void Draw(int percent, bool shortLoading=false)", @DrawLoading)

    RegisterGlobalProperty("int StartTime", &LoadingScreenStartTime)
    RegisterGlobalProperty("LoadingScreen@ Selected", &SelectedLoadingScreen)

    RegisterGlobalProperty("B3D::Channel@ CWMChannel", &LoadingScreenCHN)
    RegisterGlobalProperty("int CWMState", &LoadingScreenCWM)
    RegisterGlobalFunction("string GetRandomDefaultWidthCharacter(int minCharCode, int maxCharCode, string def)", @RandomDefaultWidthChar)

    SetDefaultNamespace(ns)
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

Type ScriptMusic
    Field File$
End Type

Const SCRIPT_MUSIC_START% = 128

Function RegisterCustomMusic%(file$)
    Local m.ScriptMusic = New ScriptMusic
    m\File = file
    Local hdl% = Handle(m) + SCRIPT_MUSIC_START
    If hdl => 0 And hdl < SCRIPT_MUSIC_START Then
        RuntimeErrorExt("Failed to register custom music.")
    End If
    Return hdl
End Function

Function RegisterCBAudio()
    RegisterType("Sound")

    RegisterObjectMethod("Sound", "void Free()", @FreeSound_Strict)
    
    RegisterObjectMethod("Sound", "B3D::Channel@ Play()", @PlaySound_Strict)


    RegisterType("Stream")
    RegisterObjectMethod("Stream", "void Stop()", @StopStream_Strict)
    
    RegisterObjectMethod("Stream", "void SetVolume(float volume, bool isSFX=false)", @SetStreamVolume_Strict)
    RegisterObjectMethod("Stream", "void SetPan(float pan)", @SetStreamPan_Strict)
    RegisterObjectMethod("Stream", "void UpdateOrigin(B3D::Camera@ cam, B3D::Entity@ entity, float range=10, float volume=1)", @UpdateStreamSoundOrigin)

    RegisterObjectMethod("Stream", "void SetPaused(bool paused)", @SetStreamPaused_Strict)
    RegisterObjectMethod("Stream", "bool get_IsPlaying() property", @IsStreamPlaying_Strict)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Sound") Else SetDefaultNamespace("Sound")
    RegisterGlobalFunction("CB::Sound@ Load(string file)", @LoadSound_Strict)
    RegisterGlobalFunction("CB::Sound@ LoadTemporary(string file)", @LoadTempSound)
    RegisterGlobalFunction("CB::Stream@ Stream(string file, float volume=1, int customMode=2)", @StreamSound_Strict)
    SetDefaultNamespace(ns)


    RegisterEnum("Music")
    RegisterEnumValue("Music", "LightContainmentZone", 0)
    RegisterEnumValue("Music", "HeavyContainmentZone", 1)
    RegisterEnumValue("Music", "EntranceZone", 2)
    RegisterEnumValue("Music", "PocketDimension", 3)
    RegisterEnumValue("Music", "Room079", 4)
    RegisterEnumValue("Music", "GateB1", 5)
    RegisterEnumValue("Music", "GateB2", 6)
    RegisterEnumValue("Music", "Room3Storage", 7)
    RegisterEnumValue("Music", "Room049", 8)
    RegisterEnumValue("Music", "Forest860", 9)
    RegisterEnumValue("Music", "SCP106Chase", 10)
    RegisterEnumValue("Music", "Menu", 11)
    RegisterEnumValue("Music", "Forest860Chase", 12)
    RegisterEnumValue("Music", "Intro", 13)
    RegisterEnumValue("Music", "SCP178", 14)
    RegisterEnumValue("Music", "PocketDimensionTrench", 15)
    RegisterEnumValue("Music", "Room205", 16)
    RegisterEnumValue("Music", "GateA", 17)
    RegisterEnumValue("Music", "Dimension1499", 18)
    RegisterEnumValue("Music", "Dimension1499Danger", 19)
    RegisterEnumValue("Music", "SCP049Chase", 20)
    RegisterEnumValue("Music", "EndingMenuBreath", 21)
    RegisterEnumValue("Music", "Room914", 22)
    RegisterEnumValue("Music", "Ending", 23)
    RegisterEnumValue("Music", "Credits", 24)
    RegisterEnumValue("Music", "AfterCredits", 25)
    RegisterEnumValue("Music", "Invalid", 66)

    If ns <> "" Then SetDefaultNamespace(ns + "::Music") Else SetDefaultNamespace("Music")
    RegisterGlobalFunction("Music RegisterCustom(string file)", @RegisterCustomMusic)
    RegisterGlobalProperty("Music NowPlaying", &NowPlaying)
    RegisterGlobalProperty("Music ShouldPlay", &ShouldPlay)

    RegisterGlobalProperty("float CurrentVolume", &CurrMusicVolume)
    RegisterGlobalProperty("CB::Stream@ Channel", &MusicCHN)
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

Function SetParticleSystemEmitter(template, owner, fixed = False)
    Return SetEmitter(owner, template, fixed)
End Function

Function RegisterParticleSystem()
    ; Technically they are meshes, but I don't think they should be treated as such.
    RegisterB3DEntitySubtype("ParticleEmitter", False)
    RegisterTypeInheritance("CB::ParticleEmitter", "B3D::Entity")

    RegisterObjectMethod("ParticleEmitter", "void Free(bool deleteParticles=true)", @FreeEmitter)
    RegisterObjectMethod("ParticleEmitter", "void Freeze()", @FreezeEmitter)
    RegisterObjectMethod("ParticleEmitter", "void Unfreeze()", @UnfreezeEmitter)
    

    RegisterType("ParticleTemplate")
    RegisterTypeConstructor("ParticleTemplate", "ParticleTemplate@ f()", @CreateTemplate)

    RegisterObjectMethod("ParticleTemplate", "void Free()", @FreeTemplate)

    RegisterObjectMethod("ParticleTemplate", "void set_EmitterBlend(int value) property", @SetTemplateEmitterBlend)
    RegisterObjectMethod("ParticleTemplate", "void set_Interval(int value) property", @SetTemplateInterval)
    RegisterObjectMethod("ParticleTemplate", "void set_ParticlesPerInterval(int value) property", @SetTemplateParticlesPerInterval)
    RegisterObjectMethod("ParticleTemplate", "void set_MaxParticles(int value) property", @SetTemplateMaxParticles)
    RegisterObjectMethod("ParticleTemplate", "void set_EmitterLifeTime(int value) property", @SetTemplateEmitterLifeTime)
    RegisterObjectMethod("ParticleTemplate", "void set_Gravity(float value) property", @SetTemplateGravity)
    RegisterObjectMethod("ParticleTemplate", "void set_Alpha(float value) property", @SetTemplateAlpha)
    RegisterObjectMethod("ParticleTemplate", "void set_AlphaVel(bool value) property", @SetTemplateAlphaVel)
    RegisterObjectMethod("ParticleTemplate", "void set_Brightness(int value) property", @SetTemplateBrightness)
    RegisterObjectMethod("ParticleTemplate", "void set_Yaw(float value) property", @SetTemplateYaw)

    RegisterObjectMethod("ParticleTemplate", "void SetParticleLifeTime(int minTime, int maxTime)", @SetTemplateParticleLifeTime)
    RegisterObjectMethod("ParticleTemplate", "void SetTexture(string path, int mode=0, int blend=1)", @SetTemplateTexture)
    RegisterObjectMethod("ParticleTemplate", "void SetAnimatedTexture(string path, int mode, int blend, int width, int height, int maxFrames, float speed=1)", @SetTemplateAnimTexture)
    RegisterObjectMethod("ParticleTemplate", "void SetOffset(float minX, float maxX, float minY, float maxY, float minZ, float maxZ)", @SetTemplateOffset)
    RegisterObjectMethod("ParticleTemplate", "void SetVelocity(float minX, float maxX, float minY, float maxY, float minZ, float maxZ)", @SetTemplateVelocity)
    RegisterObjectMethod("ParticleTemplate", "void SetRotation(float minRotationSpeed, float maxRotationSpeed)", @SetTemplateRotation)
    RegisterObjectMethod("ParticleTemplate", "void SetAlignToFall(bool alignToFall, int alignOffset=0)", @SetTemplateAlignToFall)
    RegisterObjectMethod("ParticleTemplate", "void SetSize(float x, float y, float minMultiplier=1, float maxMultiplier=1)", @SetTemplateSize)
    RegisterObjectMethod("ParticleTemplate", "void SetSizeVelocity(float add, float mult)", @SetTemplateSizeVel)
    RegisterObjectMethod("ParticleTemplate", "void SetColors(int col1, int col2)", @SetTemplateColors)
    RegisterObjectMethod("ParticleTemplate", "void SetFloor(float y, float bounce=0.5)", @SetTemplateFloor)
    RegisterObjectMethod("ParticleTemplate", "void SetFixAngles(int pitch, int yaw)", @SetTemplateFixAngles)
    RegisterObjectMethod("ParticleTemplate", "void AddSubTemplate(ParticleTemplate@ template)", @SetTemplateSubTemplate)

    RegisterObjectMethod("ParticleTemplate", "ParticleEmitter@ Emit(B3D::Entity@ owner, bool fixed=false)", @SetParticleSystemEmitter)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Particles") Else SetDefaultNamespace("Particles")
    RegisterGlobalFunction("void Initialize(B3D::Camera@ camera)", @InitParticles)
    RegisterGlobalFunction("void Update()", @UpdateParticles_Devil)
    RegisterGlobalFunction("void FreeAll()", @FreeParticles)
    SetDefaultNamespace(ns)
End Function

Function RegisterDoor()
    RegisterTypeFromPtr("Door", %Doors)

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
    RegisterTypeField("Door", "B3D::Mesh@ HitObject", %Doors\DoorHitOBJ)

    RegisterObjectMethod("Door", "void Remove()", @RemoveDoor)
    RegisterObjectMethod("Door", "bool CanUse(bool showMsg=true, bool playSFX=true)", @CanUseDoor)
    RegisterObjectMethod("Door", "void Use(bool showMsg=true, bool playSFX=true)", @UseDoor)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Door") Else SetDefaultNamespace("Door")
    RegisterGlobalFunction("Door@+ Create(int lvl, float x, float y, float z, float yawAngle, Room@ room, bool startOpen=false, int big=0, int keycard=0, string code=" + Chr(34) + Chr(34) + ", bool useCollisionMesh=false)", @CreateDoor)

    RegisterGlobalFunction("void UpdateAll()", @UpdateDoors)

    RegisterGlobalProperty("Door@ Closest", &ClosestDoor)
    RegisterGlobalProperty("B3D::Mesh@ ClosestButton", &ClosestButton)

    RegisterGlobalFunction("void ActivateKeyCardMessageCooldown()", @ActivateKeyCardMsgCooldown)
    RegisterGlobalFunction("void HasKeyCardMessage()", @HasKeyCardMsg)
    RegisterGlobalProperty("int KeyCardMessageCooldownUntilMillis", &KeyCardMsgCooldown)

    RegisterGlobalFunction("void ActivateScannerMessageCooldown()", @ActivateScannerMsgCooldown)
    RegisterGlobalFunction("void HasScannerMessage()", @HasScannerMsg)
    RegisterGlobalProperty("int ScannerMessageCooldownUntilMillis", &ScannerMsgCooldown)
    SetDefaultNamespace(ns)
End Function

Function RegisterRoomTemplate()
    RegisterTypeFromPtr("TempTriggerbox", %TempTriggerboxes)
    RegisterTypeField("TempTriggerbox", "B3D::Mesh@ Object", %TempTriggerboxes\Obj)
    RegisterTypeField("TempTriggerbox", "string Name", %TempTriggerboxes\Name)
    RegisterTypeField("TempTriggerbox", "TempTriggerbox@ Successor", %TempTriggerboxes\Successor)

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
    RegisterTypeField("TempDoor", "int Index", %TempDoors\Index)
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

    RegisterTypeField("RoomTemplate", "TempTriggerbox@ FirstTempTriggerbox", %RoomTemplates\FirstTempTriggerbox)
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

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Waypoint") Else SetDefaultNamespace("Waypoint")
    RegisterGlobalFunction("Waypoint@+ f(float x, float y, float z, Door@ door, Room@ room)", @CreateWaypoint)
    SetDefaultNamespace(ns)
End Function

Function RegisterTriggerbox()
    RegisterTypeFromPtr("Triggerbox", %Triggerboxes)
    RegisterTypeField("Triggerbox", "B3D::Mesh@ Object", %Triggerboxes\Obj)
    RegisterTypeField("Triggerbox", "float MinX", %Triggerboxes\MinX)
    RegisterTypeField("Triggerbox", "float MinY", %Triggerboxes\MinY)
    RegisterTypeField("Triggerbox", "float MinZ", %Triggerboxes\MinZ)
    RegisterTypeField("Triggerbox", "float MaxX", %Triggerboxes\MaxX)
    RegisterTypeField("Triggerbox", "float MaxY", %Triggerboxes\MaxY)
    RegisterTypeField("Triggerbox", "float MaxZ", %Triggerboxes\MaxZ)
    RegisterTypeField("Triggerbox", "string Name", %Triggerboxes\Name)
    RegisterTypeField("Triggerbox", "Triggerbox@ Successor", %Triggerboxes\Successor)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Triggerbox") Else SetDefaultNamespace("Triggerbox")
    RegisterGlobalFunction("string Check(Room@ room, float x, float y, float z)", @CheckTriggers)
    SetDefaultNamespace(ns)
End Function

Function RegisterForest()
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

Function RegisterMapGen()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Map") Else SetDefaultNamespace("Map")

    RegisterGlobalProperty("int Width", &MapWidth)
    RegisterGlobalProperty("int Height", &MapHeight)

    RegisterGlobalProperty("dim2<int> Temporary", &MapTemp)
    RegisterGlobalProperty("dim2<int> Found", &MapFound)
    RegisterGlobalProperty("dim2<string> Name", &MapName)
    RegisterGlobalProperty("dim1<int> RoomID", &MapRoomID)
    RegisterGlobalProperty("dim2<string> Room", &MapRoom)

    RegisterGlobalProperty("dim2<int> RoomAmounts", &RoomAmounts)
    RegisterGlobalProperty("dim2<int> MinPositions", &MinPositions)
    RegisterGlobalProperty("dim2<int> MaxPositions", &MaxPositions)

    RegisterGlobalFunction("void Create(int loadingStart, float loadingCount)", @CreateMap)
    RegisterGlobalFunction("void CreateLayout()", @CreateMapLayout)
    RegisterGlobalFunction("void CountRooms()", @CountRooms)
    RegisterGlobalFunction("void ForceRooms()", @ForceRooms)
    RegisterGlobalFunction("void ForceRoom1s(int desiredCountPerZone)", @ForceRoom1s)
    RegisterGlobalFunction("void ForceRoom2CsAnd4s(int desired2CCountPerZone, int desired4CountPerZone)", @ForceRoom2CsAnd4s)
    RegisterGlobalFunction("void InitializeRoomRanges()", @InitializeRoomRanges)
    RegisterGlobalFunction("void SetRooms()", @SetRooms)
    RegisterGlobalFunction("void CreateRooms(int loadingStart, float loadingCount)", @CreateRooms)
    RegisterGlobalFunction("void PreventRoomOverlaps()", @PreventRoomOverlaps)
    RegisterGlobalFunction("void DrawDebug()", @DrawDebugMap)
    RegisterGlobalFunction("void CreateDoors()", @CreateDoors)
    RegisterGlobalFunction("void ConnectAdjacentRooms()", @ConnectAdjacentRooms)

    SetDefaultNamespace(ns)
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

    RegisterGlobalFunction("NPC@+ Create(Type, float x, float y, float z)", @CreateNPC)
    RegisterGlobalFunction("int FindFreeID()", @FindFreeNPCID)

    RegisterGlobalFunction("void UpdateAll()", @UpdateNPCs)

    RegisterGlobalFunction("void Shoot(float x, float y, float z, float hitProb=1.0, bool impactParticles=true, bool instaKill=false)", @Shoot)

    RegisterGlobalFunction("void ManipulateBones()", @ManipulateNPCBones)
    RegisterGlobalFunction("string GetBoneManipulationValue(string npcName, string boneName, string section, int valueType=0)", @GetNPCManipulationValue)
    RegisterGlobalFunction("float ChangeAngleValueForCorrectBoneAssignment(float value)", @ChangeAngleValueForCorrectBoneAssigning)

    SetDefaultNamespace(ns)

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

    RegisterTypeField("NPC", "Sound@ Sound", %NPCs\Sound)
    RegisterTypeField("NPC", "B3D::Channel@ Channel", %NPCs\SoundChn)
    RegisterTypeField("NPC", "float SoundTimer", %NPCs\SoundTimer)

    RegisterTypeField("NPC", "Sound@ Sound2", %NPCs\Sound2)
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

    RegisterObjectMethod("NPC", "void PlayMTFSound(CB::Sound@)", @PlayMTFSound, True)
    ;RegisterObjectMethod("NPC", "void UpdateMTFUnit()", @UpdateMTFUnit)

    RegisterObjectMethod("NPC", "void Animate(float startFrame, float endFrame, float speed, bool loop=true)", @AnimateNPC)
    RegisterObjectMethod("NPC", "void SetNPCFrame(float frame)", @SetNPCFrame)
    RegisterObjectMethod("NPC", "void FinishWalking(float startFrame, float endFrame, float speed)", @FinishWalking)
    RegisterObjectMethod("NPC", "void ChangeNPCTexture(int textureID)", @ChangeNPCTextureID)

    RegisterObjectMethod("NPC", "void ForceSetID(int newID)", @ForceSetNPCID)
    RegisterObjectMethod("NPC", "float Find860Angle(Forest@)", @Find860Angle)

    RegisterObjectMethod("NPC", "void EnableNPC()", @EnableNPC)
    RegisterObjectMethod("NPC", "void DisableNPC()", @DisableNPC)

    RegisterObjectMethod("NPC", "void ApplyDifficultySpeedScale()", @NPCSpeedChange)
End Function

Function RegisterMap()
    RegisterTypeFromPtr("Room", %Rooms)

    RegisterDoor()
    RegisterRoomTemplate()
    RegisterWayPoint()
    RegisterTriggerbox()
    RegisterForest()
    RegisterMaintenanceTunnel()
    RegisterMapGen()

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Room") Else SetDefaultNamespace("Room")
    RegisterGlobalFunction("Room@+ Create(int zone, int shape, float x, float y, float z, int angle, string name)", @CreateRoom)
    SetDefaultNamespace(ns)

    If ns <> "" Then SetDefaultNamespace(ns + "::Map") Else SetDefaultNamespace("Map")
    RegisterGlobalFunction("void SetupTriggerBoxes(Room@ room)", @SetupTriggerBoxes)
    SetDefaultNamespace(ns)

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
    RegisterTypeField("Room", "carray<B3D::Pivot@> SoundEmitterObject", %Rooms\SoundEmitterObj)
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

    RegisterTypeField("Room", "Triggerbox@ FirstTriggerbox", %Rooms\FirstTriggerbox)

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

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Decal") Else SetDefaultNamespace("Decal")
    ; TODO: Ability to register custom decal textures.
    RegisterGlobalFunction("Decal@+ Create(int id, float x, float y, float z, float pitch, float yaw, float roll)", @CreateDecal)
    SetDefaultNamespace(ns)

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

Function RegisterSkybox()
    RegisterB3DMeshSubtype("Skybox", False, False)
    RegisterTypeInheritance("CB::Skybox", "B3D::Mesh")
    RegisterTypeInheritance("CB::Skybox", "B3D::Model")
    RegisterTypeInheritance("CB::Skybox", "B3D::Entity")
    RegisterObjectMethod("Skybox", "void Update()", @sky_Update)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Skybox") Else SetDefaultNamespace("Skybox")
    RegisterGlobalFunction("Skybox@ Create(string filename, B3D::Entity@ parent=null)", @sky_CreateSky)
    RegisterGlobalProperty("Skybox@ Sky", &Sky)
    RegisterGlobalProperty("Skybox@ Sky1499", &NTF_1499Sky)
    SetDefaultNamespace(ns)
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

    RegisterObjectMethod("Item", "void Remove(bool inGame=true)", @RemoveItem)
    RegisterObjectMethod("Item", "void Pick()", @PickItem)
    RegisterObjectMethod("Item", "void Drop(bool playDropSound=true)", @DropItem)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Item") Else SetDefaultNamespace("Item")
    RegisterGlobalFunction("Item@+ Create(string name, float x, float y, float z)", @CreateItem)
    RegisterGlobalFunction("Item@+ CreateCup(string drinkName, float x, float y, float z, int r, int g, int b, float a=1)", @CreateCup)
    RegisterGlobalFunction("ItemTemplate@+ FindTemplate(string name)", @FindItemTemplate)
    RegisterGlobalProperty("int LastItemID", &LastItemID)
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

    RegisterGlobalProperty("int CurrentZone", &PlayerZone)
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

    RegisterGlobalFunction("void MoveToPocketDimension()", @MoveToPocketDimension)

    RegisterGlobalFunction("bool IsInReachableRoom(bool is049ChamberReachable=false)", @PlayerInReachableRoom)

    SetDefaultNamespace(ns)
End Function

Function RegisterDifficulty()
    RegisterTypeFromPtr("Difficulty", %Difficulty)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Difficulty") Else SetDefaultNamespace("Difficulty")
    RegisterEnum("SaveType")
    RegisterEnumValue("SaveType", "Anywhere", 0)
    RegisterEnumValue("SaveType", "OnQuit", 1)
    RegisterEnumValue("SaveType", "OnScreens", 2)

    RegisterEnum("OtherFactors")
    RegisterEnumValue("OtherFactors", "Easy", 0)
    RegisterEnumValue("OtherFactors", "Normal", 1)
    RegisterEnumValue("OtherFactors", "Hard", 2)

    RegisterTypeField("Difficulty", "string Name", %Difficulty\name)
    RegisterTypeField("Difficulty", "string LocalName", %Difficulty\localName)
    RegisterTypeField("Difficulty", "string Description", %Difficulty\description)
    RegisterTypeField("Difficulty", "bool PermaDeath", %Difficulty\permaDeath)
    RegisterTypeField("Difficulty", "bool AggressiveNPCs", %Difficulty\aggressiveNPCs)
    RegisterTypeField("Difficulty", "Difficulty::SaveType Save", %Difficulty\saveType)
    RegisterTypeField("Difficulty", "Difficulty::OtherFactors Other", %Difficulty\otherFactors)

    RegisterTypeField("Difficulty", "int R", %Difficulty\r)
    RegisterTypeField("Difficulty", "int G", %Difficulty\g)
    RegisterTypeField("Difficulty", "int B", %Difficulty\b)

    RegisterTypeField("Difficulty", "bool Customizable", %Difficulty\customizable)

    RegisterGlobalProperty("Difficulty@ Current", &SelectedDifficulty)
    Local safe.Difficulty = Difficulties[SAFE]
    Local euclid.Difficulty = Difficulties[EUCLID]
    Local keter.Difficulty = Difficulties[KETER]
    Local custom.Difficulty = Difficulties[CUSTOM]
    RegisterGlobalProperty("Difficulty@ Safe", &safe)
    RegisterGlobalProperty("Difficulty@ Euclid", &euclid)
    RegisterGlobalProperty("Difficulty@ Keter", &keter)
    RegisterGlobalProperty("Difficulty@ Custom", &custom)
    SetDefaultNamespace(ns)
End Function

Function RegisterDreamfilter()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Dreamfilter") Else SetDefaultNamespace("Dreamfilter")

    RegisterGlobalProperty("B3D::Texture@ Texture", &ark_blur_texture)
    RegisterGlobalProperty("B3D::Image@ Image", &ark_blur_image)
    RegisterGlobalProperty("B3D::Camera@ Camera", &ark_blur_cam)

    RegisterGlobalProperty("float Volume", &BlurVolume)
    RegisterGlobalProperty("float Timer", &BlurTimer)

    RegisterGlobalFunction("void Update(float power)", @UpdateBlur)

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

    RegisterGlobalFunction("CB::NPC::Type NPCNameToType(string name)", @Console_NPCNameToType)
    RegisterGlobalFunction("string NPCTypeToName(CB::NPC::Type, bool plural=false)", @Console_NPCTypeToName)
    RegisterGlobalFunction("void ListNPCTypes()", @Console_ListNPCTypes)
    RegisterGlobalFunction("void SpawnNPC(CB::NPC::Type, string stateStr=" + Chr(34) + Chr(34) + ")", @Console_SpawnNPC)
    RegisterGlobalFunction("void ChangeNPCSpeed(CB::NPC::Type, float speed)", @Console_ChangeNPCSpeed)
    RegisterGlobalFunction("void PrintNPCState(CB::NPC::Type)", @Console_PrintNPCState)
    RegisterGlobalFunction("bool CheckCanToggleNPC(CB::NPC::Type)", @Console_CheckCanToggleNPC)
    RegisterGlobalFunction("void EnableNPC(CB::NPC::Type)", @Console_EnableNPC)
    RegisterGlobalFunction("void DisableNPC(CB::NPC::Type)", @Console_DisableNPC)

    SetDefaultNamespace(ns)
End Function

Function EventGetChannel%(e.Events)
    If e\SoundCHN_isStream Then Return 0
    Return e\SoundCHN
End Function

Function EventGetChannel2%(e.Events)
    If e\SoundCHN2_isStream Then Return 0
    Return e\SoundCHN2
End Function

Function EventGetChannelStream%(e.Events)
    If Not e\SoundCHN_isStream Then Return 0
    Return e\SoundCHN
End Function

Function EventGetChannelStream2%(e.Events)
    If Not e\SoundCHN2_isStream Then Return 0
    Return e\SoundCHN2
End Function

Function EventSetChannel(e.Events, chn%)
    e\SoundCHN = chn
    e\SoundCHN_isStream = 0
End Function

Function EventSetChannel2(e.Events, chn%)
    e\SoundCHN2 = chn
    e\SoundCHN2_isStream = 0
End Function

Function EventSetChannelStream(e.Events, chn%)
    e\SoundCHN = chn
    e\SoundCHN_isStream = 1
End Function

Function EventSetChannelStream2(e.Events, chn%)
    e\SoundCHN2 = chn
    e\SoundCHN2_isStream = 1
End Function

Function RegisterEvent()
    RegisterTypeFromPtr("Event", %Events)
    RegisterTypeField("Event", "string Name", %Events\EventName)
    RegisterTypeField("Event", "Room@ Room", %Events\room)
    RegisterTypeField("Event", "float State", %Events\EventState)
    RegisterTypeField("Event", "float State2", %Events\EventState2)
    RegisterTypeField("Event", "float State3", %Events\EventState3)
    RegisterObjectMethod("Event", "B3D::Channel@ get_Channel() property", @EventGetChannel)
    RegisterObjectMethod("Event", "B3D::Channel@ get_Channel2() property", @EventGetChannel2)
    RegisterObjectMethod("Event", "CB::Stream@ get_ChannelStream() property", @EventGetChannelStream)
    RegisterObjectMethod("Event", "CB::Stream@ get_ChannelStream2() property", @EventGetChannelStream2)
    RegisterObjectMethod("Event", "void set_Channel(B3D::Channel@) property", @EventSetChannel)
    RegisterObjectMethod("Event", "void set_Channel2(B3D::Channel@) property", @EventSetChannel2)
    RegisterObjectMethod("Event", "void set_ChannelStream(CB::Stream@) property", @EventSetChannelStream)
    RegisterObjectMethod("Event", "void set_ChannelStream2(CB::Stream@) property", @EventSetChannelStream2)
    RegisterTypeField("Event", "CB::Sound@ Sound", %Events\Sound)
    RegisterTypeField("Event", "CB::Sound@ Sound2", %Events\Sound2)
    RegisterTypeField("Event", "int ChannelIsStream", %Events\SoundCHN_isStream)
    RegisterTypeField("Event", "int Channel2IsStream", %Events\SoundCHN2_isStream)
    RegisterTypeField("Event", "string String", %Events\EventStr)
    RegisterTypeField("Event", "B3D::Image@ Image", %Events\img)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Event") Else SetDefaultNamespace("Event")

    RegisterGlobalFunction("Sound@ LoadSound(Event@ event, string file, int num=0)", @LoadEventSound)

    RegisterGlobalFunction("Event@+ Create(string eventName, string roomName, int id, float probability=0)", @CreateEvent)

    RegisterGlobalFunction("float UpdateElevator(float state, Door@ door1, Door@ door2, B3D::Entity@ room1, B3D::Entity@ room2, Event@ event, bool ignoreRotation = true)", @UpdateElevators)

    RegisterGlobalFunction("void UpdateAll()", @UpdateEvents)
    RegisterGlobalFunction("void UpdateDimension1499()", @UpdateDimension1499)
    RegisterGlobalFunction("void UpdateEndings()", @UpdateEndings)
    RegisterGlobalFunction("void IsIn914Input(Room@ room, float x, float y, float z)", @IsIn914Input)

    SetDefaultNamespace(ns)
End Function

Function RegisterSubtitles()
    RegisterTypeFromPtr("SubtitleBox", %SubtitleBox)
    RegisterTypeField("SubtitleBox", "float ScreenWidth", %SubtitleBox\screenWidth)
    RegisterTypeField("SubtitleBox", "float ScreenLeft", %SubtitleBox\screenLeft)
    RegisterTypeField("SubtitleBox", "float ScreenTop", %SubtitleBox\screenTop)
    RegisterTypeField("SubtitleBox", "float CurrentTop", %SubtitleBox\curTop)
    RegisterTypeField("SubtitleBox", "float CurrentHeight", %SubtitleBox\curHeight)
    RegisterTypeField("SubtitleBox", "float TargetTop", %SubtitleBox\targetTop)
    RegisterTypeField("SubtitleBox", "float TargetHeight", %SubtitleBox\targetHeight)
    RegisterTypeField("SubtitleBox", "float Alpha", %SubtitleBox\alpha)
    RegisterTypeField("SubtitleBox", "int Lines", %SubtitleBox\lines)
    RegisterTypeField("SubtitleBox", "B3D::Camera@ Camera", %SubtitleBox\cam)
    RegisterTypeField("SubtitleBox", "B3D::Sprite@ Sprite", %SubtitleBox\sprite)

    RegisterTypeFromPtr("SubtitleColor", %SubtitleColor)
    RegisterTypeField("SubtitleColor", "string VoiceKey", %SubtitleColor\voiceKey)
    RegisterTypeField("SubtitleColor", "string Name", %SubtitleColor\name)
    RegisterTypeField("SubtitleColor", "int R", %SubtitleColor\r)
    RegisterTypeField("SubtitleColor", "int G", %SubtitleColor\g)
    RegisterTypeField("SubtitleColor", "int B", %SubtitleColor\b)
    RegisterTypeField("SubtitleColor", "bool IsItalic", %SubtitleColor\isItalic)
    RegisterTypeField("SubtitleColor", "bool IsBold", %SubtitleColor\isBold)
    RegisterTypeField("SubtitleColor", "float CooldownLength", %SubtitleColor\cooldownLength)
    RegisterTypeField("SubtitleColor", "float MinVolume", %SubtitleColor\minVolume)

    RegisterTypeFromPtr("SubtitleEntry", %SubtitleEntry)
    RegisterTypeField("SubtitleEntry", "SubtitleEntry@ NextEntry", %SubtitleEntry\nextEntry)
    RegisterTypeField("SubtitleEntry", "string Text", %SubtitleEntry\txt)
    RegisterTypeField("SubtitleEntry", "SubtitleColor@ Color", %SubtitleEntry\col)
    RegisterTypeField("SubtitleEntry", "int EntryType", %SubtitleEntry\entryType)
    RegisterTypeField("SubtitleEntry", "float Time", %SubtitleEntry\time)
    RegisterTypeField("SubtitleEntry", "float Length", %SubtitleEntry\length)
    RegisterTypeField("SubtitleEntry", "float Cooldown", %SubtitleEntry\cooldown)

    RegisterTypeFromPtr("SubtitleToken", %SubtitleToken)
    RegisterTypeField("SubtitleToken", "string SoundPath", %SubtitleToken\soundPath)
    RegisterTypeField("SubtitleToken", "int FromFile", %SubtitleToken\fromFile)
    RegisterTypeField("SubtitleToken", "SubtitleEntry@ Entry", %SubtitleToken\entry)

    RegisterTypeFromPtr("QueuedSubtitleMsg", %QueuedSubtitleMsg)
    RegisterTypeField("QueuedSubtitleMsg", "SubtitleToken@ Token", %QueuedSubtitleMsg\token)
    RegisterTypeField("QueuedSubtitleMsg", "SubtitleEntry@ Entry", %QueuedSubtitleMsg\entry)
    RegisterTypeField("QueuedSubtitleMsg", "int SoundHandle", %QueuedSubtitleMsg\sndHandle)
    RegisterTypeField("QueuedSubtitleMsg", "B3D::Channel@ Channel", %QueuedSubtitleMsg\chn)
    RegisterTypeField("QueuedSubtitleMsg", "bool IsStream", %QueuedSubtitleMsg\isStream)
    RegisterTypeField("QueuedSubtitleMsg", "float Volume", %QueuedSubtitleMsg\volume)
    RegisterTypeField("QueuedSubtitleMsg", "int Paused", %QueuedSubtitleMsg\paused)
    RegisterTypeField("QueuedSubtitleMsg", "float TimeStart", %QueuedSubtitleMsg\timeStart)

    RegisterTypeFromPtr("SubtitleMsg", %SubtitleMsg)
    RegisterTypeField("SubtitleMsg", "SubtitleEntry@ Entry", %SubtitleMsg\entry)
    RegisterTypeField("SubtitleMsg", "string Text", %SubtitleMsg\txt)
    RegisterTypeField("SubtitleMsg", "float Y", %SubtitleMsg\curYPos)
    RegisterTypeField("SubtitleMsg", "float TimeLeft", %SubtitleMsg\timeLeft)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Subtitles") Else SetDefaultNamespace("Subtitles")

    RegisterGlobalProperty("float TextHeight", &SubtitleTextHeight)
    RegisterGlobalProperty("float EarlyDelay", &SubtitleEarlyDelay)
    RegisterGlobalProperty("float FadeLength", &SubtitleFadeLength)

    RegisterGlobalProperty("bool Initialized", &SubtitlesInitialized)

    RegisterGlobalProperty("B3D::Font@ Font1Bold", &Font1Bold)
    RegisterGlobalProperty("B3D::Font@ Font1Italic", &Font1Italic)
    RegisterGlobalProperty("B3D::Font@ Font1BoldItalic", &Font1BoldItalic)

    ; TODO: Cache?

    RegisterGlobalProperty("SubtitleBox@ Box", &SubBox)

    RegisterGlobalFunction("void Load()", @LoadSubtitles)
    RegisterGlobalFunction("void LoadEntities()", @LoadSubtitleEntities)
    RegisterGlobalFunction("void Queue(string soundPath, int soundHandle, B3D::Channel@ channel, bool isStream=false)", @QueueSubtitle)
    RegisterGlobalFunction("void RemoveQueued(int soundHandle)", @RemoveQueuedSubtitle)
    RegisterGlobalFunction("void RemoveQueuedByChannel(B3D::Channel@ channel, bool isStream=false)", @RemoveQueuedSubtitleByChannel)
    RegisterGlobalFunction("void UpdateChannelVolume(B3D::Channel@ channel, float volume, bool isStream=false, bool isSFX=true)", @UpdateChannelVolumeWithSubtitles)
    RegisterGlobalFunction("void UpdateQueuedVolume(B3D::Channel@ channel, float volume, bool isStream=false)", @UpdateQueuedSubtitleVolume)
    RegisterGlobalFunction("void ResumeChannel(B3D::Channel@ channel)", @ResumeChannelWithSubtitles)
    RegisterGlobalFunction("void PauseChannel(B3D::Channel@ channel)", @PauseChannelWithSubtitles)
    RegisterGlobalFunction("void SetQueuedPause(B3D::Channel@ channel, bool paused)", @SetQueuedSubtitlePause)
    RegisterGlobalFunction("void Clear()", @ClearSubtitles)
    RegisterGlobalFunction("bool ShouldShow(SubtitleEntry@ entry)", @ShouldShowSubtitle)
    RegisterGlobalFunction("SubtitleMsg@+ TryCreate(QueuedSubtitleMsg@ queue, string txt=" + Chr(34) + Chr(34) + ")", @TryCreateSubtitleMsg)
    RegisterGlobalFunction("void TryCreateSplit(QueuedSubtitleMsg@ queue, string txtLine, int padding)", @TryCreateSplitSubtitleMsg)
    RegisterGlobalFunction("void RecalculateBoxTarget()", @RecalculateSubtitleBoxTarget)
    RegisterGlobalFunction("void BumpUp(SubtitleMsg@ msg)", @BumpSubtitleUp)
    RegisterGlobalFunction("void Update(float factor)", @UpdateSubtitles)
    RegisterGlobalFunction("void Draw()", @DrawSubtitles)
    RegisterGlobalFunction("void Render()", @RenderSubtitles)

    RegisterGlobalFunction("string StripComments(string line)", @SubtitleStripComments)
    RegisterGlobalFunction("void LoadVoices(string path)", @LoadSubtitleVoices)
    RegisterGlobalFunction("void LoadTokens(string path, int tokenType)", @LoadSubtitleTokens)
    RegisterGlobalFunction("SubtitleEntry@+ CreateEntry(string key, string value, SubtitleColor@ color, int tokenType, bool isCaptionDefault)", @CreateSubtitleEntry)
    RegisterGlobalFunction("void CreateToken(SubtitleEntry@ entry, string soundPathGroup, int tokenType)", @CreateSubtitleToken)
    RegisterGlobalFunction("SubtitleToken@+ GetToken(string soundPath)", @GetSubtitleToken)
    RegisterGlobalFunction("string ParseSettings(SubtitleEntry@ entry, string txt, bool isCaption=false)", @ParseSubtitleSettings)
    RegisterGlobalFunction("bool ApplyDataSetting(string key, string value, SubtitleEntry@ entry)", @ApplySubtitleDataSetting)
    RegisterGlobalFunction("bool ApplyColorSetting(string key, string value, SubtitleColor@ color)", @ApplySubtitleColorSetting)
    RegisterGlobalFunction("SubtitleColor@+ FindOrAddVoice(string voice)", @FindOrAddSubtitleVoice)
    RegisterGlobalFunction("void CopyColor(SubtitleColor@ fromColor, SubtitleColor@ toColor, bool keepVoiceKey=false, bool keepBools=true)", @CopySubtitleColor)
    RegisterGlobalFunction("bool ColorsMatch(SubtitleColor@ firstColor, SubtitleColor@ secondColor)", @SubtitleColorsMatch)
    RegisterGlobalFunction("SubtitleColor@+ InternColor(SubtitleColor@ color)", @InternSubtitleColor)
    RegisterGlobalFunction("void CleanupColors()", @CleanupColors)

    SetDefaultNamespace(ns)
End Function

Function GiveAchievementNoSteam(achv.Achievements, showMessage%=True)
    GiveAchievement(achv, showMessage, False)
End Function

Function RegisterAchievements()
    RegisterTypeFromPtr("Achievement", %Achievements)
    RegisterTypeField("Achievement", "string Name", %Achievements\Name)
    RegisterTypeField("Achievement", "bool IsSCP", %Achievements\IsSCP)
    RegisterTypeField("Achievement", "string DisplayName", %Achievements\LocalName)
    RegisterTypeField("Achievement", "string Description", %Achievements\Description)
    RegisterTypeField("Achievement", "B3D::Image@ Image", %Achievements\Img)
    RegisterTypeField("Achievement", "bool Unlocked", %Achievements\Unlocked)

    RegisterTypeFromPtr("AchievementMessage", %AchievementMsg)
    RegisterTypeField("AchievementMessage", "Achievement@ Achievement", %AchievementMsg\achv)
    RegisterTypeField("AchievementMessage", "float X", %AchievementMsg\msgx)
    RegisterTypeField("AchievementMessage", "float Time", %AchievementMsg\msgtime)
    RegisterTypeField("AchievementMessage", "int ID", %AchievementMsg\msgID)

    RegisterObjectMethod("Achievement", "void Award(bool showMessage=true)", @GiveAchievementNoSteam)
    RegisterObjectMethod("Achievement", "void DrawTooltip()", @AchievementTooltip)
    RegisterObjectMethod("Achievement", "void DrawImage(int x, int y)", @DrawAchvIMG, True)
    RegisterObjectMethod("Achievement", "AchievementMessage@+ CreateMessage()", @CreateAchievementMsg)

    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Achievement") Else SetDefaultNamespace("Achievement")

    RegisterGlobalProperty("int TotalCount", &AchievementCount)

    RegisterGlobalProperty("Achievement@ Achv008", &Achv008)
    RegisterGlobalProperty("Achievement@ Achv012", &Achv012)
    RegisterGlobalProperty("Achievement@ Achv035", &Achv035)
    RegisterGlobalProperty("Achievement@ Achv049", &Achv049)
    RegisterGlobalProperty("Achievement@ Achv055", &Achv055)
    RegisterGlobalProperty("Achievement@ Achv066", &Achv066)
    RegisterGlobalProperty("Achievement@ Achv079", &Achv079)
    RegisterGlobalProperty("Achievement@ Achv096", &Achv096)
    RegisterGlobalProperty("Achievement@ Achv106", &Achv106)
    RegisterGlobalProperty("Achievement@ Achv148", &Achv148)
    RegisterGlobalProperty("Achievement@ Achv205", &Achv205)
    RegisterGlobalProperty("Achievement@ Achv294", &Achv294)
    RegisterGlobalProperty("Achievement@ Achv372", &Achv372)
    RegisterGlobalProperty("Achievement@ Achv420", &Achv420)
    RegisterGlobalProperty("Achievement@ Achv427", &Achv427)
    RegisterGlobalProperty("Achievement@ Achv500", &Achv500)
    RegisterGlobalProperty("Achievement@ Achv513", &Achv513)
    RegisterGlobalProperty("Achievement@ Achv714", &Achv714)
    RegisterGlobalProperty("Achievement@ Achv789", &Achv789)
    RegisterGlobalProperty("Achievement@ Achv860", &Achv860)
    RegisterGlobalProperty("Achievement@ Achv895", &Achv895)
    RegisterGlobalProperty("Achievement@ Achv914", &Achv914)
    RegisterGlobalProperty("Achievement@ Achv939", &Achv939)
    RegisterGlobalProperty("Achievement@ Achv966", &Achv966)
    RegisterGlobalProperty("Achievement@ Achv970", &Achv970)
    RegisterGlobalProperty("Achievement@ Achv1025", &Achv1025)
    RegisterGlobalProperty("Achievement@ Achv1048", &Achv1048)
    RegisterGlobalProperty("Achievement@ Achv1123", &Achv1123)
    RegisterGlobalProperty("Achievement@ Achv1162", &Achv1162)
    RegisterGlobalProperty("Achievement@ Achv1499", &Achv1499)

    RegisterGlobalProperty("Achievement@ AchvMaynard", &AchvMaynard)
    RegisterGlobalProperty("Achievement@ AchvHarp", &AchvHarp)
    RegisterGlobalProperty("Achievement@ AchvSNAV", &AchvSNAV)
    RegisterGlobalProperty("Achievement@ AchvOmni", &AchvOmni)
    RegisterGlobalProperty("Achievement@ AchvTesla", &AchvTesla)
    RegisterGlobalProperty("Achievement@ AchvPD", &AchvPD)

    RegisterGlobalProperty("Achievement@ AchvConsole", &AchvConsole)
    RegisterGlobalProperty("Achievement@ AchvKeter", &AchvKeter)

    RegisterGlobalProperty("bool UsedConsole", &UsedConsole)
    RegisterGlobalProperty("int CurrentPage", &AchievementsMenu) ; TODO: Move to Menu namespace?
    RegisterGlobalProperty("B3D::Image@ LockedImage", &AchvLocked)
    RegisterGlobalProperty("int CurrentAchievementMessageID", &CurrAchvMSGID)

    RegisterGlobalFunction("Achievement@+ Create(string name, bool isSCP=false)", @CreateAchievement)
    RegisterGlobalFunction("void UpdateMessages()", @UpdateAchievementMsg)

    SetDefaultNamespace(ns)
End Function

Function Register914()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::SCP914") Else SetDefaultNamespace("SCP914")

    RegisterEnum("Setting")
    RegisterEnumValue("Setting", "Rough", 0)
    RegisterEnumValue("Setting", "Coarse", 1)
    RegisterEnumValue("Setting", "OneToOne", 2)
    RegisterEnumValue("Setting", "Fine", 3)
    RegisterEnumValue("Setting", "VeryFine", 4)

    RegisterGlobalFunction("void Use(Item@ item, Setting setting, float x, float y, float z)", @Use914)

    RegisterGlobalProperty("int RefinedItems", &RefinedItems)

    SetDefaultNamespace(ns)
End Function

Function RegisterEnding()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Ending") Else SetDefaultNamespace("Ending")

    RegisterGlobalProperty("float Timer", &EndingTimer)
    RegisterGlobalProperty("string Selected", &SelectedEnding)

    SetDefaultNamespace(ns)
End Function

Global HasRegisteredCB%
Function RegisterCB()
    If HasRegisteredCB Then Return
    HasRegisteredCB = True

    SetDefaultNamespace("CB")
    RegisterCommon()
    RegisterMenu()
    RegisterLoadingScreens()
    RegisterOptions()
    RegisterIO()
    RegisterCBAudio()
    RegisterCBInput()
    RegisterParticleSystem()
    ; Fucking ugly but the dependencies have a ton of cycles.
    RegisterTypeFromPtr("Waypoint", %WayPoints)
    RegisterTypeFromPtr("Forest", %Forest)
    RegisterNPC()
    RegisterMap()
    RegisterDecal()
    RegisterSkybox()
    RegisterItem()
    RegisterPlayer()
    RegisterDifficulty()
    RegisterDreamfilter()
    RegisterConsole()
    RegisterEvent()
    RegisterSubtitles()
    RegisterAchievements()
    Register914()
    RegisterEnding()
    SetDefaultNamespace("")
End Function

RegisterCB()
