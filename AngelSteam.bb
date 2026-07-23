Global HasRegisteredSteamworks%
Function RegisterSteamworks()
	If HasRegisteredSteamworks Then Return
	HasRegisteredSteamworks = True

	Local ns$ = GetDefaultNamespace()
	If ns <> "" Then SetDefaultNamespace(ns + "::Steam") Else SetDefaultNamespace("Steam")

	RegisterGlobalFunction("bool Init()", @Steam_Init)
	RegisterGlobalFunction("bool RestartAppIfNecessary(int appID)", @Steam_RestartAppIfNecessary)
	RegisterGlobalFunction("void Update()", @Steam_Update)
	RegisterGlobalFunction("void Shutdown()", @Steam_Shutdown)

	;RegisterGlobalFunction("bool Achieve(cstr@ id)", @Steam_Achieve)
	;RegisterGlobalFunction("bool UnAchieve(cstr@ id)", @Steam_UnAchieve)

	RegisterGlobalFunction("cstr@ GetGameLanguage()", @Steam_GetGameLanguage)

	RegisterGlobalFunction("bool GetOverlayState()", @Steam_GetOverlayState)
	RegisterGlobalFunction("bool GetOverlayUpdated()", @Steam_GetOverlayUpdated)
	RegisterGlobalFunction("void SetOverlayNotificationInset(int x, int y)", @Steam_SetOverlayNotificationInset)
	RegisterGlobalFunction("void SetOverlayNotificationPosition(int pos)", @Steam_SetOverlayNotificationPosition)

	RegisterGlobalFunction("bool SetRichPresence(cstr@ key, cstr@ value)", @Steam_SetRichPresence)

	RegisterGlobalFunction("void PublishItem(cstr@ name, cstr@ desc, cstr@ path, cstr@ imgPath)", @Steam_PublishItem)
	RegisterGlobalFunction("void UpdateItem(cstr@ fileID, cstr@ name, cstr@ desc, cstr@ path, cstr@ imgPath, cstr@ changelog, bool updateTags)", @Steam_UpdateItem)
	RegisterGlobalFunction("int QueryUpdateItemStatus()", @Steam_QueryUpdateItemStatus)
	RegisterGlobalFunction("void ClearItemTags()", @Steam_ClearItemTags)
	RegisterGlobalFunction("void AddItemTag(cstr@ tag)", @Steam_AddItemTag)
	RegisterGlobalFunction("void RemoveItemTag(cstr@ tag)", @Steam_RemoveItemTag)
	RegisterGlobalFunction("cstr@ GetPublishedItemID()", @Steam_GetPublishedItemID)
	RegisterGlobalFunction("void LoadSubscribedItems()", @Steam_LoadSubscribedItems)
	RegisterGlobalFunction("int GetSubscribedItemCount()", @Steam_GetSubscribedItemCount)
	RegisterGlobalFunction("cstr@ GetSubscribedItemID(int id)", @Steam_GetSubscribedItemID)
	RegisterGlobalFunction("cstr@ GetSubscribedItemPath(int id)", @Steam_GetSubscribedItemPath)

	RegisterGlobalFunction("int StringToIDUpper(cstr@ cid)", @Steam_StringToIDUpper)
	RegisterGlobalFunction("int StringToIDLower(cstr@ cid)", @Steam_StringToIDLower)
	RegisterGlobalFunction("cstr@ IDToString(int upperID, int lowerID)", @Steam_IDToString)

	RegisterGlobalFunction("int GetPlayerIDUpper()", @Steam_GetPlayerIDUpper)
	RegisterGlobalFunction("int GetPlayerIDLower()", @Steam_GetPlayerIDLower)
	RegisterGlobalFunction("cstr@ GetPlayerName()", @Steam_GetPlayerName)
	RegisterGlobalFunction("cstr@ GetOtherPlayerName(int upperID, int lowerID)", @Steam_GetOtherPlayerName)

	RegisterGlobalFunction("void PushByte(int b)", @Steam_PushByte)
	RegisterGlobalFunction("void PushShort(int s)", @Steam_PushShort)
	RegisterGlobalFunction("void PushInt(int i)", @Steam_PushInt)
	RegisterGlobalFunction("void PushFloat(float f)", @Steam_PushFloat)
	RegisterGlobalFunction("void PushString(cstr@ s)", @Steam_PushString)

	RegisterGlobalFunction("int PullByte()", @Steam_PullByte)
	RegisterGlobalFunction("int PullShort()", @Steam_PullShort)
	RegisterGlobalFunction("int PullInt()", @Steam_PullInt)
	RegisterGlobalFunction("float PullFloat()", @Steam_PullFloat)
	RegisterGlobalFunction("cstr@ PullString()", @Steam_PullString)

	RegisterGlobalFunction("int GetSenderIDUpper()", @Steam_GetSenderIDUpper)
	RegisterGlobalFunction("int GetSenderIDLower()", @Steam_GetSenderIDLower)

	RegisterGlobalFunction("int LoadPacket()", @Steam_LoadPacket)
	RegisterGlobalFunction("bool SendPacketToUser(int upperID, int lowerID, bool reliable=false)", @Steam_SendPacketToUser)
	RegisterGlobalFunction("bool CloseConnection(int upperID, int lowerID)", @Steam_CloseConnection)

	RegisterGlobalFunction("bool CreateLobby(int lobbyType, int maxMembers)", @Steam_CreateLobby)
	RegisterGlobalFunction("bool JoinLobby(int lobbyIDUpper, int lobbyIDLower)", @Steam_JoinLobby)
	RegisterGlobalFunction("void LeaveLobby()", @Steam_LeaveLobby)
	RegisterGlobalFunction("int GetLobbyState()", @Steam_GetLobbyState)
	RegisterGlobalFunction("int GetLobbyIDUpper()", @Steam_GetLobbyIDUpper)
	RegisterGlobalFunction("int GetLobbyIDLower()", @Steam_GetLobbyIDLower)
	RegisterGlobalFunction("int GetNumLobbyMembers()", @Steam_GetNumLobbyMembers)
	RegisterGlobalFunction("int GetLobbyMemberIDUpper(int member)", @Steam_GetLobbyMemberIDUpper)
	RegisterGlobalFunction("int GetLobbyMemberIDLower(int member)", @Steam_GetLobbyMemberIDLower)
	RegisterGlobalFunction("int GetLobbyOwnerIDUpper()", @Steam_GetLobbyOwnerIDUpper)
	RegisterGlobalFunction("int GetLobbyOwnerIDLower()", @Steam_GetLobbyOwnerIDLower)
	RegisterGlobalFunction("void ActivateOverlayInviteDialog()", @Steam_ActivateOverlayInviteDialog)
	RegisterGlobalFunction("void SetAcceptLobbyInvites(bool accept)", @Steam_SetAcceptLobbyInvites)

	RegisterGlobalFunction("void OpenOnScreenKeyboard(int mode, int x, int y, int width, int height)", @Steam_OpenOnScreenKeyboard)
	RegisterGlobalFunction("void CloseOnScreenKeyboard()", @Steam_CloseOnScreenKeyboard)

	;RegisterGlobalFunction("cstr@ EE(cstr@ cid)", @Steam_EE)

	SetDefaultNamespace(ns)
End Function

RegisterSteamworks()
