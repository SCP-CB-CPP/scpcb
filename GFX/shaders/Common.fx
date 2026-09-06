struct VS_INPUT
{ 
	float4 Pos : POSITION; 
	float2 TexCoords : TEXCOORD0;
};

inline float2 GetScreenTexCoords(float4 ScreenCoords)
{
	ScreenCoords.xy /= ScreenCoords.w;
	return 0.5f * (float2(ScreenCoords.x, -ScreenCoords.y) + 1.0f);
}

float4 ViewportSize		: VIEWPORT_SIZE;
static const float2 ScreenSize = ViewportSize.zw;

#ifdef D3D11
static const float2 halfPixel = float2(0.0, 0.0);
#else
static const float2 halfPixel = 0.5 / ScreenSize;
#endif
