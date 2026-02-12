.lib "Blitzcord.dll"

BlitzcordCreateCore%( id$ ):"_BlitzcordCreateCore@4"
BlitzcordRunCallbacks():"_BlitzcordRunCallbacks@0"
BlitzcordSetActivityState( state$ ):"_BlitzcordSetActivityState@4"
BlitzcordSetActivityDetails( details$ ):"_BlitzcordSetActivityDetails@4"
BlitzcordSetActivityType( type% ):"_BlitzcordSetActivityType@4"
BlitzcordClearActivity():"_BlitzcordClearActivity@0"
BlitzcordUpdateActivity():"_BlitzcordUpdateActivity@0"
BlitzcordGetUpdateActivityStatus%():"_BlitzcordGetUpdateActivityStatus@0"

BlitzcordSetSmallImage( image$ ):"_BlitzcordSetSmallImage@4"
BlitzcordSetSmallText( text$ ):"_BlitzcordSetSmallText@4"
BlitzcordGetSmallImage$():"_BlitzcordGetSmallImage@0"
BlitzcordGetSmallText$():"_BlitzcordGetSmallText@0"

BlitzcordSetLargeImage( image$ ):"_BlitzcordSetLargeImage@4"
BlitzcordSetLargeText( text$ ):"_BlitzcordSetLargeText@4"
BlitzcordGetLargeImage$():"_BlitzcordGetLargeImage@0"
BlitzcordGetLargeText$():"_BlitzcordGetLargeText@0"

BlitzcordSetTimestampStart( timestamp$ ):"_BlitzcordSetTimestampStart@4"
BlitzcordSetTimestampEnd( timestamp$ ):"_BlitzcordSetTimestampEnd@4"

BlitzcordGetTimestampStartUpper%():"_BlitzcordGetTimestampStartUpper@0"
BlitzcordGetTimestampStartLower%():"_BlitzcordGetTimestampStartLower@0"
BlitzcordGetTimestampEndUpper%():"_BlitzcordGetTimestampEndUpper@0"
BlitzcordGetTimestampEndLower%():"_BlitzcordGetTimestampEndLower@0"

BlitzcordGetCurrentTimestamp$():"_BlitzcordGetCurrentTimestamp@0"
BlitzcordGetTimestampPlus$( hours%, minutes%, seconds% ):"_BlitzcordGetTimestampPlus@12"
BlitzcordGetTimestampMinus$( hours%, minutes%, seconds% ):"_BlitzcordGetTimestampMinus@12"

StringToUpperLong%( stringLong$ ):"_StringToUpperLong@4"
StringToLowerLong%( stringLong$ ):"_StringToLowerLong@4"
LongToString$( upperLong%, lowerLong% ):"_LongToString@8"