// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

varying vec3 vNormal;
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
	float dist, att;
    
    dist = length(lightDir);
    
    att = 1.0 / (gl_LightSource[0].constantAttenuation  +
                 gl_LightSource[0].linearAttenuation    * dist +
                 gl_LightSource[0].quadraticAttenuation * dist * dist);
                 
    att = 1.0;

    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vNormal), normalize(lightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 vFragColor = diff * gl_LightSource[0].diffuse * att;

    // Add in ambient light
    vFragColor += gl_LightSource[0].ambient;

    // Specular Light
    if(diff > 0.0)
	{
		vec3 vReflection = reflect(normalize(-lightDir), normalize(vNormal));
    	float spec = max(0.0, dot(normalize(-eyeDir), vReflection));
    	
        float fSpec = pow(spec, gl_FrontMaterial.shininess);
        
        vFragColor.rgb += gl_LightSource[0].specular.rgb * fSpec * att;
    }

	vFragColor.rgb *= gl_Color.rgb;
	
	if(enableShadow)
	{
		float sIntensity = shadowIntensity();
		gl_FragColor = vec4(sIntensity * vFragColor.rgb, 1.0);
	}
	else gl_FragColor = vFragColor;
}