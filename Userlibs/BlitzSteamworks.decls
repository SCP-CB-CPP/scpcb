.lib "BlitzSteamworks.dll"

Steam_Init%():"_Init@0"
Steam_RestartAppIfNecessary%(appID%):"_RestartAppIfNecessary@4"
Steam_Update():"_Update@0"
Steam_Shutdown():"_Shutdown@0"

Steam_Achieve%(ID$):"_Achieve@4"
Steam_UnAchieve%(ID$):"_UnAchieve@4"

Steam_GetGameLanguage$():"_GetGameLanguage@0"

Steam_GetOverlayState%():"_GetOverlayState@0"
Steam_GetOverlayUpdated%():"_GetOverlayUpdated@0"

Steam_PublishItem(name$, desc$, path$, imgPath$):"_PublishItem@16"
Steam_UpdateItem(fileid$, name$, desc$, path$, imgPath$, changelog$, updateTags%):"_UpdateItem@28"
Steam_QueryUpdateItemStatus%():"_QueryUpdateItemStatus@0"
Steam_ClearItemTags():"_ClearItemTags@0"
Steam_AddItemTag(tag$):"_AddItemTag@4"
Steam_RemoveItemTag(tag$):"_RemoveItemTag@4"
Steam_GetPublishedItemID$():"_GetPublishedItemID@0"
Steam_LoadSubscribedItems():"_LoadSubscribedItems@0"
Steam_GetSubscribedItemCount%():"_GetSubscribedItemCount@0"
Steam_GetSubscribedItemID$(id%):"_GetSubscribedItemID@4"
Steam_GetSubscribedItemPath$(id%):"_GetSubscribedItemPath@4"

Steam_StringToIDUpper%(cid$):"_StringToIDUpper@4"
Steam_StringToIDLower%(cid$):"_StringToIDLower@4"

Steam_IDToString$(upperID%, lowerID%):"_IDToString@8"

Steam_GetPlayerIDUpper%():"_GetPlayerIDUpper@0"
Steam_GetPlayerIDLower%():"_GetPlayerIDLower@0"
Steam_GetPlayerName$():"_GetPlayerName@0"

Steam_PushByte(b%):"_PushByte@4"
Steam_PushShort(s%):"_PushShort@4"
Steam_PushInt%(i%):"_PushInt@4"
Steam_PushFloat#(f#):"_PushFloat@4"
Steam_PushString$(s$):"_PushString@4"

Steam_PullByte%():"_PullByte@0"
Steam_PullShort%():"_PullShort@0"
Steam_PullInt%():"_PullInt@0"
Steam_PullFloat#():"_PullFloat@0"
Steam_PullString$():"_PullString@0"

Steam_GetSenderIDUpper%():"_GetSenderIDUpper@0"
Steam_GetSenderIDLower%():"_GetSenderIDLower@0"

Steam_LoadPacket%():"_LoadPacket@0"
Steam_SendPacketToUser%(upperID%, lowerID%):"_SendPacketToUser@8"
Steam_CloseConnection%(upperID%, lowerID%):"_CloseConnection@8"

Steam_OpenOnScreenKeyboard(mode%, x%, y%, width%, height%):"_OpenOnScreenKeyboard@20"
Steam_CloseOnScreenKeyboard():"_CloseOnScreenKeyboard@0"

Steam_EE$(cid$):"_EE@4"