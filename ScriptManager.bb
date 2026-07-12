Include "AngelBB.bb"
Include "AngelCB.bb"
Include "AngelSteam.bb"

Type Hooks
    Field Subscribers%
    Field ID%
    Field HookType%
    Field FuncName$
End Type

Global HookCount% = 0
Const HOOK_TYPE_RUN_ALL% = 0
Const HOOK_TYPE_OVERRIDABLE% = 1
Const HOOK_TYPE_RETURN_INT% = 2
Const HOOK_TYPE_RETURN_STR% = 3

Delete Each Hooks
Global InitializeLauncher.Hooks = CreateHook("Hook_InitializeLauncher")
Global Initialize.Hooks = CreateHook("Hook_Initialize")
Global Shutdown.Hooks = CreateHook("Hook_Shutdown")
Global SaveOptions.Hooks = CreateHook("Hook_SaveOptions")
Global Update.Hooks = CreateHook("Hook_Update")
Global DrawHUD.Hooks = CreateHook("Hook_DrawHUD", HOOK_TYPE_OVERRIDABLE)
Global MovePlayer.Hooks = CreateHook("Hook_MovePlayer", HOOK_TYPE_OVERRIDABLE)
Global MouseLook.Hooks = CreateHook("Hook_MouseLook", HOOK_TYPE_OVERRIDABLE)
Global KillPlayer.Hooks = CreateHook("Hook_KillPlayer", HOOK_TYPE_OVERRIDABLE)
Global ExecuteConsoleCommand.Hooks = CreateHook("Hook_ExecuteConsoleCommand", HOOK_TYPE_OVERRIDABLE)
Global LoadEntities.Hooks = CreateHook("Hook_LoadEntities")
Global NullGame.Hooks = CreateHook("Hook_NullGame")
Global DrawLoading.Hooks = CreateHook("Hook_DrawLoading", HOOK_TYPE_OVERRIDABLE)

Global MapInitializeDimensions.Hooks = CreateHook("Hook_MapInitializeDimensions", HOOK_TYPE_OVERRIDABLE)
Global MapCreateLayout.Hooks = CreateHook("Hook_MapCreateLayout", HOOK_TYPE_OVERRIDABLE)
Global MapForceRooms.Hooks = CreateHook("Hook_MapForceRooms", HOOK_TYPE_OVERRIDABLE)
Global MapSetRooms.Hooks = CreateHook("Hook_MapSetRooms", HOOK_TYPE_OVERRIDABLE)
Global MapCreateRooms.Hooks = CreateHook("Hook_MapCreateRooms", HOOK_TYPE_OVERRIDABLE)
Global MapPreventRoomOverlaps.Hooks = CreateHook("Hook_MapPreventRoomOverlaps", HOOK_TYPE_OVERRIDABLE)
Global MapCreateDoors.Hooks = CreateHook("Hook_MapCreateDoors", HOOK_TYPE_OVERRIDABLE)
Global MapConnectAdjacentRooms.Hooks = CreateHook("Hook_MapConnectAdjacentRooms", HOOK_TYPE_OVERRIDABLE)

Global InitializeEvents.Hooks = CreateHook("Hook_InitializeEvents", HOOK_TYPE_OVERRIDABLE)
Global UpdateEvent.Hooks = CreateHook("Hook_UpdateEvent", HOOK_TYPE_OVERRIDABLE)
Global FillRoom.Hooks = CreateHook("Hook_FillRoom", HOOK_TYPE_OVERRIDABLE)
Global PostFillRoom.Hooks = CreateHook("Hook_PostFillRoom")
Global LoadRoomTemplateEntity.Hooks = CreateHook("Hook_LoadRoomTemplateEntity", HOOK_TYPE_OVERRIDABLE)
Global PostLoad.Hooks = CreateHook("Hook_PostLoad")

Global CreateItem.Hooks = CreateHook("Hook_CreateItem", HOOK_TYPE_OVERRIDABLE)
Global UpdateItem.Hooks = CreateHook("Hook_UpdateItem", HOOK_TYPE_OVERRIDABLE)
Global RemoveItem.Hooks = CreateHook("Hook_RemoveItem")
Global PickItem.Hooks = CreateHook("Hook_PickItem", HOOK_TYPE_OVERRIDABLE)
Global DropItem.Hooks = CreateHook("Hook_DropItem", HOOK_TYPE_OVERRIDABLE)
Global SelectItem.Hooks = CreateHook("Hook_SelectItem")
Global CombineItems.Hooks = CreateHook("Hook_CombineItems")

Global CreateNPC.Hooks = CreateHook("Hook_CreateNPC")
Global PostCreateNPC.Hooks = CreateHook("Hook_PostCreateNPC")
Global RemoveNPC.Hooks = CreateHook("Hook_RemoveNPC", HOOK_TYPE_OVERRIDABLE)
Global UpdateNPC.Hooks = CreateHook("Hook_UpdateNPC", HOOK_TYPE_OVERRIDABLE)
Global ConsoleNPCNameToType.Hooks = CreateHook("Hook_ConsoleNPCNameToType", HOOK_TYPE_RETURN_INT)
Global ConsoleNPCTypeToName.Hooks = CreateHook("Hook_ConsoleNPCTypeToName", HOOK_TYPE_RETURN_STR)
Global ConsoleSpawnNPC.Hooks = CreateHook("Hook_ConsoleSpawnNPC", HOOK_TYPE_OVERRIDABLE)
Global ConsoleCheckCanChangeNPCSpeed.Hooks = CreateHook("Hook_ConsoleCheckCanChangeNPCSpeed", HOOK_TYPE_RETURN_INT)
Global ConsoleCheckCanToggleNPC.Hooks = CreateHook("Hook_ConsoleCheckCanToggleNPC", HOOK_TYPE_RETURN_INT)
Global EnableNPC.Hooks = CreateHook("Hook_EnableNPC", HOOK_TYPE_OVERRIDABLE)
Global DisableNPC.Hooks = CreateHook("Hook_DisableNPC", HOOK_TYPE_OVERRIDABLE)

Global CanUseDoor.Hooks = CreateHook("Hook_CanUseDoor", HOOK_TYPE_RETURN_INT)
Global UseDoor.Hooks = CreateHook("Hook_UseDoor", HOOK_TYPE_OVERRIDABLE)
Global Use914.Hooks = CreateHook("Hook_Use914", HOOK_TYPE_OVERRIDABLE)

Dim HookFuncs%(HookCount, 0)

Function CreateHook.Hooks(funcName$, hookType%=HOOK_TYPE_RUN_ALL)
    Local h.Hooks = New Hooks
    h\HookType = hookType%
    h\FuncName = funcName$
    h\ID = HookCount
    HookCount = HookCount + 1
    Return h
End Function

Function InitializeHooks(moduleCount%)
    For h.Hooks = Each Hooks
        h\Subscribers = 0
    Next
    Dim HookFuncs%(HookCount, moduleCount)
End Function

Function SubscribeModuleHooks(m%)
    For h.Hooks = Each Hooks
        Local f% = GetModuleFunction(m, h\FuncName)
        If f <> 0 Then
            HookFuncs%(h\ID, h\Subscribers) = f
            h\Subscribers = h\Subscribers + 1
        End If
    Next
End Function

Function CallHook%(h.Hooks)
    Select h\HookType
        Case HOOK_TYPE_RUN_ALL
            For i = 0 To h\Subscribers-1
                ExecuteFunction(HookFuncs%(h\ID, i))
            Next
        Case HOOK_TYPE_OVERRIDABLE
            Local ret%
            For i = 0 To h\Subscribers-1
                ExecuteFunction(HookFuncs%(h\ID, i), &ret)
                If ret Then Return True
            Next
        Case HOOK_TYPE_RETURN_INT
            For i = 0 To h\Subscribers-1
                ExecuteFunction(HookFuncs%(h\ID, i), &ret)
                Return ret
            Next
        Case HOOK_TYPE_RETURN_STR
            RuntimeErrorExt("Incorrect call hook function called for return type string!")
    End Select
    Return False
End Function

Function CallHookStr$(h.Hooks)
    If h\HookType <> HOOK_TYPE_RETURN_STR Then
        RuntimeErrorExt("Incorrect call hook function called for non-string return type!")
        Return ""
    End If
    Local ret$
    For i = 0 To h\Subscribers-1
        ExecuteFunction(HookFuncs%(h\ID, i), &ret)
        If ret <> "" Then Return ret
    Next
    Return ""
End Function

Function CreateModule%(name$, file$)
    BeginModule(name)
    ModuleAddFile(file$)
    EndModule()
    Return GetModule(name$)
End Function
