Global GammaEffect%, FXAAEffect%

Global PostEffectQuad%, QuadCamera%, PostEffect%

Global ScreenTexture%

Global PixelWidth# = 0, PixelHeight# = 0
If GetGraphicsLevel() < 100
	PixelWidth# = 0.5 / GraphicWidth
	PixelHeight# = 0.5 / GraphicHeight
EndIf

Function InitPostProcess()
	ScreenTexture = CreateTexture(GraphicWidth, GraphicHeight, 1 + 1024)
	
	GammaEffect = LoadEffect_Strict("GFX\shaders\Gamma.fx")

	FXAAEffect = LoadEffect_Strict("GFX\shaders\FXAA.fx")
	
	PostEffectQuad = CreateFullscreenQuad()
	EntityTexture(PostEffectQuad, ScreenTexture, 0, 0)
	EntityOrder(PostEffectQuad, 10000000)
	EntityFX(PostEffectQuad, 8)
	HideEntity(PostEffectQuad)
	
	QuadCamera = CreateCamera()
	CameraClsMode(QuadCamera, 0, 0)
	HideEntity(QuadCamera)
	
	PostEffect = 0
End Function

Function UpdatePostProcess()
	ProcessGammaEffect(ScreenGamma)
	If Opt_AntiAlias Then ProcessFXAAEffect()
End Function

Function ProcessGammaEffect(gamma#)
	EffectFloat(GammaEffect, "Gamma", Lerp(gamma, 1.0, 0.3)) ; Limit gamma
	RenderEffectQuad(GammaEffect, BackBuffer(), "Main")
End Function

Function ProcessFXAAEffect()
	RenderEffectQuad(FXAAEffect, BackBuffer(), "Main")
End Function

Function RenderEffectQuad(effect%, buffer%, technique$, blend% = 0)
	CopyRect(0, 0, GraphicWidth, GraphicHeight, 0, 0, BackBuffer(), TextureBuffer(ScreenTexture))

	SetQuadEffect(effect)
	ShowEntity(PostEffectQuad)
	EntityBlend(PostEffectQuad, blend)
	SetBuffer(buffer)
	EffectTechnique(effect, technique)
	CameraViewport(QuadCamera, 0, 0, GraphicWidth, GraphicHeight)
	RenderEntity(QuadCamera, PostEffectQuad)
	HideEntity(PostEffectQuad)
End Function

Function CreateFullscreenQuad%(Parent% = 0)
	Quad = CreateSprite(Parent)
	ScaleSprite(Quad, 1.0, (Float(GraphicHeight) / Float(GraphicWidth)))
	
	MoveEntity(Quad, -PixelWidth, PixelHeight, 1.0001)
	Return(Quad)
End Function

Function SetQuadEffect(effect%)
	if PostEffect = effect Then Return
	EntityEffect PostEffectQuad, effect
	PostEffect = effect
End Function
