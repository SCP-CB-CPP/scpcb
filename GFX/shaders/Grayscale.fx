float4x4 World : WORLD;
float4x4 View : VIEW;
float4x4 Projection : PROJECTION;

texture tex0 : TEX0;
sampler2D samp = sampler_state
{
    Texture = <tex0>;
    MinFilter = LINEAR;
    MagFilter = LINEAR;
    MipFilter = LINEAR;
    AddressU = CLAMP;
    AddressV = CLAMP;
};

float GrayscaleEnable = 1.0;

struct VS_IN
{
    float4 pos : POSITION;
    float2 tex : TEXCOORD0;
};

struct VS_OUT
{
    float4 pos : POSITION;
    float2 tex : TEXCOORD0;
};

VS_OUT VS(VS_IN input)
{
    VS_OUT output;
    output.pos = mul(input.pos, World);
    output.pos = mul(output.pos, View);
    output.pos = mul(output.pos, Projection);
    output.tex = input.tex;
    return output;
}

float4 PS(VS_OUT input) : COLOR
{
    float4 color = tex2D(samp, input.tex);
    float grey = dot(color.rgb, float3(0.299, 0.587, 0.114));
    float3 mixed = lerp(color.rgb, grey.xxx, GrayscaleEnable);
    // TODO: Any alpha that is returned here seems to be ignored.
    // Meanwhile, manually blending with the alpha we sample doesn't seem to work either!
    // Something is fucked in the shader system, fix your shit Krim!
    if (color.a == 0) return float4(0, 0, 0, 0);
    return float4(mixed, 1);
}

technique Greyscale
{
    pass P0
    {
        VertexShader = compile vs_2_0 VS();
        PixelShader  = compile ps_2_0 PS();
    }
}