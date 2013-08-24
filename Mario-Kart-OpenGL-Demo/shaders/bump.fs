// bump.fs
//
// per-pixel bumpmapped Phong lighting

uniform sampler2D texture;
uniform sampler2D bumpmap;

varying vec3 lightDir;
varying vec3 eyeDir;
varying vec4 shadowCoord;

uniform sampler2DShadow shadowMap;

uniform bool enableShadow;
uniform int sampleMode;

const float epsilon = 0.05;
float illumination = 0.5;

uniform vec2 texScale;

float lookup(float x, float y)
{
	vec2 offset = vec2(mod(floor(gl_FragCoord.xy), 2.0));
	x += offset.x;
	y += offset.y;

	float depth = shadow2DProj(shadowMap, shadowCoord + vec4(x, y, 0, 0) * epsilon).x;
	return depth != 1.0 ? illumination : 1.0;
}

float lookup(sampler2DShadow map, vec4 coord, vec2 offset)
{
	float depth = shadow2DProj(shadowMap, vec4(coord.xy + offset * texScale * coord.w, coord.z, coord.w)).x;
	return depth != 1.0 ? illumination : 1.0;
}

float shadowIntensity()
{
	if(sampleMode == 0)
	{	
		return lookup(0.0, 0.0);
	}
	else if(sampleMode == 1)
	{
		float sum = 0.0;
		
		sum += lookup(-1.0, -1.0);
		sum += lookup(+1.0, -1.0);
		sum += lookup(-1.0, +1.0);
		sum += lookup(+1.0, +1.0);
			
		return sum * 0.25;
	}
	else if(sampleMode == 2)
	{
		float sum = 0.0;
		float x, y;

		for (y = -1.5; y <= 1.5; y += 1.0)
  			for (x = -1.5; x <= 1.5; x += 1.0)
    			sum += lookup(shadowMap, shadowCoord, vec2(x, y));
    				
    	return sum / 16.0;
	}	
}

void main(void)
{
    vec3 normal = texture2D(bumpmap, gl_TexCoord[0].st).rgb;
    normal *= 2.0; normal -= 1.0; // map texel from [0,1] to [-1,1]


    // Diffuse Lighting
    float diffuse = max(0.0, dot(normalize(normal), normalize(lightDir)));
    vec4 color = diffuse * gl_LightSource[0].diffuse;


    // Ambient Light
    color += gl_LightSource[0].ambient;
	color *= texture2D(texture, gl_TexCoord[0].st);


    // Specular Light
	vec3 vReflection = normalize(reflect(normalize(lightDir), normalize(normal)));
    float specular = max(0.0, dot(normalize(eyeDir), vReflection));
    if(diffuse != 0.0)
	{
        specular = pow(specular, 128.0);
        color.rgb += gl_LightSource[0].specular.rgb * specular;
    }
	
	color.rgb *= gl_Color.rgb;
	
	if(enableShadow)
	{
		float sIntensity = shadowIntensity();
		gl_FragColor = vec4(sIntensity * color.rgb, 1.0);
	}
	else gl_FragColor = color;
}