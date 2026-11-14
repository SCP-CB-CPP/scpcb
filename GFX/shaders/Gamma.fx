#include "Common.fx"

float4x4 ViewProj 		: MATRIX_VIEWPROJ;

uniform float Gamma = 0.5f;
static const float correction = 1.0 / Gamma;

DeclareSampler(ColorMap, 0, BLITZ_FILTER_POINT, BLITZ_ADDR_CLAMP, BLITZ_ADDR_CLAMP, 0.0, 1);

void VS_Gamma(VS_INPUT input, out float4 Pos : OUT_POSITION, out float2 TexCoord : TEXCOORD0)
{ 
	Pos = mul(input.Pos, ViewProj);
	TexCoord = input.TexCoords;
}

float4 PS_Gamma(float4 Pos : OUT_POSITION, float2 TexCoord : TEXCOORD0) : OUTPUT(0)
{
	return pow(Sample2D(ColorMap, TexCoord), correction);
}

technique Main
{
	pass p0
	{
		Vertex(VS_Gamma);
		Pixel(PS_Gamma);

		#ifndef D3D11
			ZWriteEnable = false;
			ClipPlaneEnable = false;
			Lighting = false;
		#endif
	}
}