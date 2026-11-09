/* 
instanceshader.fx - textured effect with an instanced technique variant
Used together with EntityInstance: linked entities are drawn in one
hardware call through the "<Technique>::Instanced" technique below.
Each instance carries its own transform (TEXCOORD4..6) and tint (COLOR1),
filled automatically by the engine. The entity's brush texture is
automatically available in texture register 0.
*/

int Anisotropy : ANISOTROPY;

sampler2D Diffuse : register(s0) = sampler_state { Filter = Anisotropic; AddressU = Wrap; AddressV = Wrap; MipLODBias = -0.3; MaxAnisotropy = Anisotropy; };

float4x4 ViewProj      : MATRIX_VIEWPROJ;
float4   EntityColor   : ENTITY_COLOR;
float4   FogColor        : FOG_COLOR;
float4   FogParams       : FOG_PARAMS;

struct VS_OUT
{
	float4 Pos    : POSITION;
	float3 Normal : TEXCOORD0;
	float2 Uv     : TEXCOORD1;
	float4 Color  : TEXCOORD2;
	float  Fog    : TEXCOORD3;
};

inline float4x3 GetInstanceTransform(float4 M1, float4 M2, float4 M3)
{
	float4x3 Mat;
	Mat[0] = float3(M1.x, M2.x, M3.x);
	Mat[1] = float3(M1.y, M2.y, M3.y);
	Mat[2] = float3(M1.z, M2.z, M3.z);
	Mat[3] = float3(M1.w, M2.w, M3.w);
	return Mat;
}

// Instanced technique variant - one draw call for all linked entities
VS_OUT VS_Instanced(
	float4 Pos         : POSITION,
	float3 Normal      : NORMAL,
	float2 Uv          : TEXCOORD0,
	float4 VertexColor : COLOR0,
	float4 IM1         : TEXCOORD4,  // instance matrix, rows 1-2
	float4 IM2         : TEXCOORD5,  // instance matrix, rows 3-4
	float4 IM3         : TEXCOORD6,  // instance matrix, row 5
	float4 Tint        : COLOR1 )    // per-instance tint (instance brush color)
{
	VS_OUT o;
	float4x3 Mat = GetInstanceTransform( IM1, IM2, IM3 );

	// The instance matrix already contains the full world transform
	o.Pos    = mul( float4( mul( Pos, Mat ), 1 ), ViewProj );
	o.Normal = normalize( mul( Normal, (float3x3)Mat ) );
	o.Uv     = Uv;
	o.Color  = VertexColor * Tint;
    o.Fog = saturate((FogParams.y - o.Pos.w) * FogParams.w);
	return o;
}

float4 PS( VS_OUT i ) : COLOR0
{
	float4 color = tex2D( Diffuse, i.Uv ) * i.Color;
	return float4(lerp(FogColor.rgb, color.rgb, i.Fog), color.a); 
}

technique Default
{
	// Don't render model itself
}

technique Default::Instanced
{
	pass P0
	{
		VertexShader = compile vs_3_0 VS_Instanced();
		PixelShader = compile ps_3_0 PS();
	}
}
