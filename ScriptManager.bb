Include "AngelBB.bb"
Include "AngelCB.bb"

Type Hooks
    Field Subscribers%
    Field ID%
    Field HookType%
    Field FuncName$
End Type

Global HookCount% = 0
Const HOOK_TYPE_RUN_ALL = 0
Const HOOK_TYPE_OVERRIDABLE = 1

Delete Each Hooks
Global Initialize.Hooks = CreateHook("Hook_Initialize")
Global SaveOptions.Hooks = CreateHook("Hook_SaveOptions")
Global Update.Hooks = CreateHook("Hook_Update")
Global DrawHUD.Hooks = CreateHook("Hook_DrawHUD", HOOK_TYPE_OVERRIDABLE)
Global MovePlayer.Hooks = CreateHook("Hook_MovePlayer", HOOK_TYPE_OVERRIDABLE)
Global MouseLook.Hooks = CreateHook("Hook_MouseLook", HOOK_TYPE_OVERRIDABLE)
Global KillPlayer.Hooks = CreateHook("Hook_KillPlayer", HOOK_TYPE_OVERRIDABLE)
Global InitializeEvents.Hooks = CreateHook("Hook_InitializeEvents", HOOK_TYPE_OVERRIDABLE)
Global UpdateEvent.Hooks = CreateHook("Hook_UpdateEvent", HOOK_TYPE_OVERRIDABLE)
Global FillRoom.Hooks = CreateHook("Hook_FillRoom", HOOK_TYPE_OVERRIDABLE)
Global PostFillRoom.Hooks = CreateHook("Hook_PostFillRoom")
Global UpdateItem.Hooks = CreateHook("Hook_UpdateItem", HOOK_TYPE_OVERRIDABLE)
Global RemoveItem.Hooks = CreateHook("Hook_RemoveItem")
Global PickItem.Hooks = CreateHook("Hook_PickItem", HOOK_TYPE_OVERRIDABLE)
Global DropItem.Hooks = CreateHook("Hook_DropItem", HOOK_TYPE_OVERRIDABLE)
Global SelectItem.Hooks = CreateHook("Hook_SelectItem")
Global CombineItems.Hooks = CreateHook("Hook_CombineItems")
Global CreateNPC.Hooks = CreateHook("Hook_CreateNPC")
Global PostCreateNPC.Hooks = CreateHook("Hook_PostCreateNPC")
Global UpdateNPC.Hooks = CreateHook("Hook_UpdateNPC", HOOK_TYPE_OVERRIDABLE)

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
    End Select
    Return False
End Function

Function CreateModule%(name$, file$)
    BeginModule(name)
    ModuleAddFile(file$)
    EndModule()
    Return GetModule(name$)
End Function
