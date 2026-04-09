Function RegisterCommon()
    RegisterGlobalProperty("float FPSFactor", &FPSFactor)
End Function

Function RegisterOptions()
    Local ns$ = GetDefaultNamespace()
    If ns <> "" Then SetDefaultNamespace(ns + "::Options") Else SetDefaultNamespace("Options")

    RegisterGlobalProperty("int InvertMouse", &InvertMouse)

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

    ; Replace first argument with some other type to enforce having to register modded NPC types.
    RegisterTypeConstructor("NPC", "NPC@ f(int NPCtype, float x, float y, float z)", @CreateNPC)

    RegisterTypeField("NPC", "B3D::Entity@ Object", %NPCs\obj)
    RegisterTypeField("NPC", "B3D::Entity@ Object2", %NPCs\obj2)
    RegisterTypeField("NPC", "B3D::Entity@ Object3", %NPCs\obj3)
    RegisterTypeField("NPC", "B3D::Entity@ Object4", %NPCs\obj4)
    RegisterTypeField("NPC", "B3D::Pivot@ Collider", %NPCs\Collider)

    RegisterTypeField("NPC", "int Type", %NPCs\NPCtype)
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
End Function

Function RegisterMap()
    RegisterTypeFromPtr("Room", %Rooms)

    RegisterDoor()
    RegisterRoomTemplate()
    RegisterWayPoint()
    RegisterForest()
    RegisterMaintenanceTunnel()

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
    RegisterTypeField("Room", "carray<Door@> RoomDoors", %Rooms\RoomDoors)
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
    RegisterGlobalProperty("float CurrSpeed", &CurrSpeed)
    RegisterGlobalProperty("float UserCameraPitch", &user_camera_pitch)
    RegisterGlobalProperty("float Side", &side)
    RegisterGlobalProperty("bool Crouch", &Crouch)
    RegisterGlobalProperty("float CrouchState", &CrouchState)

    RegisterGlobalProperty("int PlayerZone", &PlayerZone)
    RegisterGlobalProperty("Room@ PlayerRoom", &PlayerRoom)

    RegisterGlobalProperty("int GrabbedEntity", &GrabbedEntity)

    RegisterGlobalProperty("bool GodMode", &GodMode)
    RegisterGlobalProperty("bool NoClip", &NoClip)
    RegisterGlobalProperty("float NoClipSpeed", &NoClipSpeed)

    RegisterGlobalProperty("float CoffinDistance", &CoffinDistance)

    RegisterGlobalProperty("float PlayerSoundVolume", &PlayerSoundVolume)

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

    RegisterGlobalFunction("float UpdateElevator(float state, Door@ door1, Door@ door2, B3D::Entity@ room1, B3D::Entity@ room2, Event@ event, bool ignorerotation = true)", @UpdateElevators)
End Function

Global HasRegisteredCB%
Function RegisterCB()
    If HasRegisteredCB Then Return
    HasRegisteredCB = True

    SetDefaultNamespace("CB")
    RegisterCommon()
    RegisterOptions()
    RegisterCBInput()
    ; Fucking ugly but the dependencies have a ton of circles.
    RegisterTypeFromPtr("Waypoint", %WayPoints)
    RegisterNPC()
    RegisterMap()
    RegisterDecal()
    RegisterPlayer()
    RegisterConsole()
    RegisterEvent()
    SetDefaultNamespace("")
End Function

RegisterCB()
