#include "Common.fx"

float4x4 ViewProj 		: MATRIX_VIEWPROJ;

uniform float Gamma = 0.5f;

sampler ColorMap : register(s0) = sampler_state
{
	MinFilter = Point;
	MagFilter = Point;
	MipFilter = Point;
	AddressU = Clamp;
	AddressV = Clamp;
	AddressW = Clamp;
};

void VS_Gamma(VS_INPUT input, out float4 Pos : POSITION, out float2 TexCoord : TEXCOORD0)
{ 
	Pos = mul(input.Pos, ViewProj);
	TexCoord = input.TexCoords;
}

float4 PS_Gamma(float4 Pos : POSITION, float2 TexCoord : TEXCOORD0) : COLOR
{
	return tex2D(ColorMap, TexCoord) * Gamma;
}

technique Main
{
	pass p0
	{
		VertexShader = compile vs_3_0 VS_Gamma();
		PixelShader = compile ps_3_0 PS_Gamma();
		ZWriteEnable = false;
		ClipPlaneEnable = false;
		Lighting = false;
	}
}