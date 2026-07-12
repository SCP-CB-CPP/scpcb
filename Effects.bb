Global GammaEffect%, FXAAEffect%

Global PostEffectQuad%, QuadCamera%, PostEffect%

Global ScreenTexture%

Global PixelWidth# = 0, PixelHeight# = 0
PixelWidth# = 0.5 / GraphicWidth
PixelHeight# = 0.5 / GraphicHeight

Function InitPostProcess()
    ScreenTexture = CreateTexture(GraphicWidth, GraphicHeight, 1 + 1024)

    GammaEffect = LoadEffect_Strict("GFX\shaders\Gamma.fx")
    FXAAEffect = LoadEffect_Strict("GFX\shaders\FXAA.fx")

    PostEffectQuad = CreateFullscreenQuad()
    EntityOrder(PostEffectQuad, 10000000)
    EntityBlend(PostEffectQuad, 1)
    HideEntity(PostEffectQuad)
	
	GammaEffect = LoadEffect_Strict("GFX\shaders\Gamma.fx")

	FXAAEffect = LoadEffect_Strict("GFX\shaders\FXAA.fx")
	
	PostEffectQuad = CreateFullscreenQuad()
	EntityTexture(PostEffectQuad, ScreenTexture, 0, 0)
	EntityOrder(PostEffectQuad, 10000000)
	EntityFX(PostEffectQuad, 8)
	HideEntity(PostEffectQuad)
	
	QuadCamera = CreateCamera()
	CameraProjMode(QuadCamera, 2)
	CameraZoom(QuadCamera, 0.1)
	CameraClsMode(QuadCamera, 0, 0)
	CameraRange(QuadCamera, 0.1, 1.5)
	MoveEntity(QuadCamera, 0, 0, 10000)
	HideEntity(QuadCamera)
	
	PostEffect = 0
End Function


    Local aspect# = Float(GraphicWidth) / Float(GraphicHeight)
    Local scale# = SMALLEST_POWER_TWO / Float(GraphicWidth)
    ScaleEntity(PostEffectQuad, scale, scale / aspect, 1.0)
    PositionEntity(PostEffectQuad, 0, 0, 1.0001)

Function ProcessGammaEffect(gamma#)
	SetEffectFloat(GammaEffect, "Gamma", Lerp(gamma, 1.0, 0.3)) ; Limit gamma
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
	;EffectTechnique(effect, technique)
	CameraViewport(QuadCamera, 0, 0, GraphicWidth, GraphicHeight)
	RenderEntity(PostEffectQuad, QuadCamera)
	HideEntity(PostEffectQuad)

End Function

Function CreateFullscreenQuad%(Parent% = 0)
    Local Quad% = CreateMesh(Parent)
    Local SF% = CreateSurface(Quad)
    AddVertex(SF, -1.0,  1.0, 0.0, 0.0, 0.0)
    AddVertex(SF,  1.0,  1.0, 0.0, 1.0, 0.0)
    AddVertex(SF,  1.0, -1.0, 0.0, 1.0, 1.0)
    AddVertex(SF, -1.0, -1.0, 0.0, 0.0, 1.0)
    AddTriangle(SF, 0, 1, 2)
    AddTriangle(SF, 0, 2, 3)
    UpdateNormals(Quad)
    EntityFX(Quad, 1 + 32)
    Return Quad
End Function

Function RenderEffectQuad(effect%, buffer%, technique$, blend% = 0)
    CopyRect(0, 0, GraphicWidth, GraphicHeight, 0, 0, BackBuffer(), TextureBuffer(ScreenTexture))

    SetEffectTexture(effect, "SceneTex", ScreenTexture)

    SetQuadEffect(effect)
    ShowEntity(PostEffectQuad)
    EntityBlend(PostEffectQuad, blend)

    SetBuffer(buffer)

    CameraViewport(QuadCamera, 0, 0, GraphicWidth, GraphicHeight)
    RenderEntity(PostEffectQuad, QuadCamera)

    HideEntity(PostEffectQuad)
End Function

Function SetQuadEffect(effect%)
	if PostEffect = effect Then Return
	SetEntityEffect PostEffectQuad, effect
	PostEffect = effect
End Function

Function ProcessGammaEffect(gamma#)
    SetEffectFloat(GammaEffect, "gammaValue", Lerp(gamma, 1.0, 0.3))
    RenderEffectQuad(GammaEffect, BackBuffer(), "Main")
End Function

Function ProcessFXAAEffect()
    RenderEffectQuad(FXAAEffect, BackBuffer(), "Main")
End Function

Function UpdatePostProcess()
    ProcessGammaEffect(ScreenGamma)
    If Opt_AntiAlias Then ProcessFXAAEffect()
End Function