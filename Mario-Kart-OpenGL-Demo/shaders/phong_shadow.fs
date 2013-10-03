// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

varying vec3 vertexNormal;
varying vec3 lightDir;
varying vec3 eyeDir;
varying vec4 shadowCoord;

uniform sampler2D texture;
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
	float distanceToLight, attenuation;
    
    distanceToLight = length(lightDir);
    
    attenuation = 1.0 / (gl_LightSource[0].constantAttenuation  +
             		     gl_LightSource[0].linearAttenuation    * distanceToLight +
             			 gl_LightSource[0].quadraticAttenuation * distanceToLight * distanceToLight);
             						        
    float shadowCoefficient = enableShadow ? shadowIntensity() : 1.0;
             						         
    vec4 textureColor = texture2D(texture, gl_TexCoord[0].st);
    
     // Add in ambient light
    vec4 ambient = gl_LightSource[0].ambient * textureColor;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(vertexNormal), normalize(lightDir)));
    vec4 diffuse = diffuseCoefficient * gl_LightSource[0].diffuse * textureColor;

    // Specular Lighting
    vec4 specular = vec4(0.0);
    
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(normalize(-lightDir), normalize(vertexNormal));
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular = specularCoefficient * gl_LightSource[0].specular;
    }
	
	vec4 linearColor = ambient + attenuation * shadowCoefficient * (diffuse + specular);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
}