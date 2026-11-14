Type Difficulty
	Field name$, localName$
	Field description$
	Field permaDeath%
	Field aggressiveNPCs
	Field saveType%
	Field otherFactors%
	
	Field r%
	Field g%
	Field b%
	
	Field customizable%
End Type

Global Difficulties.Difficulty[4]

Global SelectedDifficulty.Difficulty

Const SAFE=0, EUCLID=1, KETER=2, CUSTOM=3

Const SAVEANYWHERE = 0, SAVEONQUIT=1, SAVEONSCREENS=2

Const EASY = 0, NORMAL = 1, HARD = 2

Function CreateDifficulty.Difficulty(name$, localName$, description$, permaDeath%, aggresiveNPCs, saveType%, otherFactors%, r%, g%, b%, customizable% = False)
	Local d.Difficulty = new Difficulty
	d\name = name
	d\localName = localName
	d\description = description
	d\permaDeath = permaDeath
	d\aggressiveNPCs = aggressiveNPCs
	d\saveType = saveType
	d\otherFactors = otherFactors
	d\r = r 
	d\g = g
	d\b = b
	d\customizable = customizable
	Return d
End Function

difficulties[SAFE] = CreateDifficulty("Safe", I_Loc\Difficulty_Safe, I_Loc\Difficulty_SafeDesc, False, False, SAVEANYWHERE, EASY, 120, 150, 50)
difficulties[EUCLID] = CreateDifficulty("Euclid", I_Loc\Difficulty_Euclid,  I_Loc\Difficulty_EuclidDesc, False, False, SAVEONSCREENS, NORMAL, 200, 200, 0)
difficulties[KETER] = CreateDifficulty("Keter", I_Loc\Difficulty_Keter, I_Loc\Difficulty_KeterDesc, True, True, SAVEONQUIT, HARD, 200, 0, 0)
difficulties[CUSTOM] = CreateDifficulty("Esoteric", I_Loc\Difficulty_Esoteric, "", False, True, SAVEANYWHERE, EASY, 255, 255, 255, True)

SelectedDifficulty = difficulties[SAFE]

;~IDEal Editor Parameters:
;~F#0
;~C#Blitz3D
