Function CheckRoomOverlap(r1.Rooms, r2.Rooms)
	Return (CalcOverlapArea(r1, r2) > 0)
End Function

Function CalcOverlapArea#(r1.Rooms, r2.Rooms)
	If (r1\MaxX	<= r2\MinX Or r1\MaxY <= r2\MinY Or r1\MaxZ <= r2\MinZ) Then Return 0
	If (r1\MinX	>= r2\MaxX Or r1\MinY >= r2\MaxY Or r1\MinZ >= r2\MaxZ) Then Return 0
	Return (Min(r1\MaxX, r2\MaxX) - Max(r1\MinX, r2\MinX)) * (Min(r1\MaxZ, r2\MaxZ) - Max(r1\MinZ, r2\MinZ))
End Function

Function CalcAllRoomOverlaps#(r.Rooms)
	Local overlapArea# = 0
	Local r2.Rooms
	For r2 = Each Rooms
		If Not r2\RoomTemplate\DisableOverlapCheck Then
			If r2<>r Then
				overlapArea = overlapArea + CalcOverlapArea(r, r2)
			EndIf
		EndIf
	Next
	Return overlapArea
End Function

Function ReportOverlaps()
	Local r1.Rooms,r2.Rooms
	For r1 = Each Rooms
		For r2 = Each Rooms
			If r2<>r1 Then
				If CheckRoomOverlap(r1, r2) Then
					DebugLog "Overlapping rooms: " + r1\RoomTemplate\Name + " / " + r2\RoomTemplate\Name
				EndIf
			EndIf
		Next
	Next
End Function

Function RotateRoom(r.Rooms)
	RotateEntity r\obj,0,r\angle,0
	CalculateRoomExtents(r)
End Function

Function MoveRoom(r.Rooms)
	PositionEntity r\obj,r\x,r\y,r\z
	RotateEntity r\obj,0,r\angle,0
	CalculateRoomExtents(r)
End Function

Function PreventRoomOverlap(r.Rooms)
	If r\RoomTemplate\DisableOverlapCheck Then Return
	
	; Just skip it when it would try to check for the checkpoints
	If r\RoomTemplate\Name = "checkpoint1" Then Return
	If r\RoomTemplate\Name = "checkpoint2" Then Return
	If r\RoomTemplate\Name = "start" Then Return
	
	Local r2.Rooms,r3.Rooms
	Local overlapAreaBefore1#,overlapAreaAfter1#
	Local overlapAreaBefore2#,overlapAreaAfter2#
	
	DebugLog "PreventRoomOverlap: " + r\RoomTemplate\Name
	
	; First, check if the room is actually intersecting at all
	overlapAreaBefore1 = CalcAllRoomOverlaps(r)
	If overlapAreaBefore1 = 0 Then Return
	
	DebugLog "Room is intersecting, overlapping area = " + overlapAreaBefore1
	
	; Room is intersecting: First, check if the given room is a ROOM2, so we could potentially just turn it by 180 degrees
	If r\RoomTemplate\Shape = ROOM2 Then
		;Room is a ROOM2, let's check if turning it 180 degrees fixes the overlapping issue
		r\angle = r\angle + 180
		RotateRoom(r)
		
		overlapAreaAfter1 = CalcAllRoomOverlaps(r)
		
		; room is ROOM2 and was able to be turned by 180 degrees
		If overlapAreaAfter1 = 0
			DebugLog "ROOM2 turning succesful! " + r\RoomTemplate\Name
			Return
		EndIf
		
		; room still overlaps after rotate. Check if new position any better
		If overlapAreaBefore1 > overlapAreaAfter1 Then
			DebugLog "Better position after ROOM2 rotate: " + overlapAreaBefore1 + " -> " + overlapAreaAfter1
			overlapAreaBefore1 = overlapAreaAfter1;
		Else
			; didn't work -> rotate the room back and move to the next step
			DebugLog "rollback ROOM2 rotate"
			r\angle = r\angle - 180
			RotateRoom(r)
		EndIf
	EndIf
	
	;Room is either not a ROOM2 or the ROOM2 is still intersecting, now trying to swap the room with another of the same type
	Local oldX1%,oldZ1%,oldAngle1%
	Local oldX2%,oldZ2%,oldAngle2%
	
	For r2 = Each Rooms
		If r2 <> r Then
			If Not r2\RoomTemplate\DisableOverlapCheck Then
				If r\RoomTemplate\Shape = r2\RoomTemplate\Shape Then
					If r\zone = r2\zone Then
						If r2\RoomTemplate\Name <> "start" Then
							If r2\RoomTemplate\Name <> "checkpoint1" Then
								If r2\RoomTemplate\Name <> "checkpoint2" Then
									overlapAreaBefore2 = CalcAllRoomOverlaps(r2)
									DebugLog "Swap overlap area: " + overlapAreaBefore2
	
									; remember original position before swap
									oldX1 = r\x
									oldZ1 = r\z
									oldAngle1 = r\angle
		
									oldX2 = r2\x
									oldZ2 = r2\z
									oldAngle2 = r2\angle
	
									; swap itself
									r\x = oldX2
									r\z = oldZ2
									r\angle = oldAngle2
									MoveRoom(r)

									r2\x = oldX1
									r2\z = oldZ1
									r2\angle = oldAngle1
									MoveRoom(r2)
				
									;make sure neither room overlaps with anything after the swap
									overlapAreaAfter1 = 0;
									overlapAreaAfter2 = 0;
									For r3 = Each Rooms
										If Not r3\RoomTemplate\DisableOverlapCheck Then
											If r3 <> r Then 
												overlapAreaAfter1 = overlapAreaAfter1 + CalcOverlapArea(r, r3);
											EndIf
											If r3 <> r2 Then 
												overlapAreaAfter2 = overlapAreaAfter2 + CalcOverlapArea(r2, r3);
											EndIf
										EndIf
									Next
									DebugLog "Overlaps after swap: MAIN " + overlapAreaBefore1 + " -> " + overlapAreaAfter1 + ", SWAP " + overlapAreaBefore2 + " -> " + overlapAreaAfter2
				
									; Check if no overlaps after swap - then problem solved, return
									If overlapAreaAfter1 = 0 And overlapAreaAfter2 = 0 Then
										DebugLog "Successful rooms swap: " + r\RoomTemplate\Name + " / " + r2\RoomTemplate\Name
										Return
									EndIf
		
									; If we still have overlap - check if overlapping area is smaller than before and continue search
									If (overlapAreaAfter1 + overlapAreaAfter2) < (overlapAreaBefore1 + overlapAreaBefore2) Then
										DebugLog "Partial overlap fix, rooms swap: " + r\RoomTemplate\Name + " / " + r2\RoomTemplate\Name
										overlapAreaBefore1 = overlapAreaAfter1
									Else
										; overlap area is bigger than before - then rollback this change and continue search
										DebugLog "Cancel this swap"
										r\x = oldX1
										r\z = oldZ1
										r\angle = oldAngle1
										MoveRoom(r)

										r2\x = oldX2
										r2\z = oldZ2
										r2\angle = oldAngle2
										MoveRoom(r2)
									EndIf
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf
			EndIf
		EndIf
	Next
	
	DebugLog "Unable to resolve overlap problem for room " + r\RoomTemplate\Name
End Function