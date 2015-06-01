// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec4 shadowCoord;

varying vec2  gridScale;
uniform float minScale;

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

bool insideCircle(vec2 circle, float radius, vec2 point)
{
	return pow(point.x - circle.x, 2.0) + pow(point.y - circle.y, 2.0) < pow(radius, 2.0);
}

bool insideEllipse(vec2 circle, vec2 radius, vec2 point)
{
	return pow(point.x - circle.x, 2.0) / pow(radius.x, 2.0) + pow(point.y - circle.y, 2.0) / pow(radius.y, 2.0) <= 1.0;
}

vec4 getCheckerColor(vec2 texCoord)
{
	vec2  borderSize  = vec2(0.25);
	      borderSize /= gridScale * 2.0;
	vec2 _borderSize  = 1.0 - borderSize;

	bool exterior = texCoord.s <       borderSize.s || texCoord.t <       borderSize.t ||
	                texCoord.s > 1.0 - borderSize.s || texCoord.t > 1.0 - borderSize.t;
	
	if(!exterior)
	{
		vec2 interiorCoord = (texCoord - borderSize) * (1.0 / (1.0 - 2.0 * borderSize));
		
		bool insideCircles[4];
		
	 	     if(interiorCoord.s <  borderSize.s && interiorCoord.t <  borderSize.t) exterior = !insideEllipse(vec2( borderSize), borderSize, interiorCoord);
		else if(interiorCoord.s > _borderSize.s && interiorCoord.t <  borderSize.t) exterior = !insideEllipse(vec2(_borderSize.s,  borderSize.t), borderSize, interiorCoord);
		else if(interiorCoord.s <  borderSize.s && interiorCoord.t > _borderSize.t) exterior = !insideEllipse(vec2( borderSize.s, _borderSize.t), borderSize, interiorCoord);
		else if(interiorCoord.s > _borderSize.s && interiorCoord.t > _borderSize.t) exterior = !insideEllipse(vec2(_borderSize), borderSize, interiorCoord);
	}
	
	vec2 gridCoord = texCoord * gridScale * minScale;
	
	gridCoord = gridCoord * 2.0 - 1.0;
	gridCoord = fract(gridCoord * 0.5);
	gridCoord = (gridCoord * 2.0 - 1.0) * 0.5;
	gridCoord = abs(gridCoord);
	
	bool darken = (gridCoord.x + gridCoord.y) <= 0.5;
	
	if(exterior) return gl_Color;
	else if(darken) return gl_Color - vec4(0.1);
	else return gl_Color + vec4(0.1);	
}

void pointLight(in int i, in vec3 normal, inout vec4 ambient, inout vec4 diffuse, inout vec4 specular)
{
	float distanceToLight, attenuation;
    
    distanceToLight = length(lightDir[i]);
    
    attenuation = 1.0 / (gl_LightSource[i].constantAttenuation  +
             		     gl_LightSource[i].linearAttenuation    * distanceToLight +
             			 gl_LightSource[i].quadraticAttenuation * distanceToLight * distanceToLight);
    
     // Add in ambient light
    ambient += gl_LightSource[i].ambient * attenuation;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(normal), normalize(lightDir[i])));
    diffuse += diffuseCoefficient * gl_LightSource[i].diffuse * attenuation;

    // Specular Lighting
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(normalize(-lightDir[i]), normalize(normal));
		
    	float specularCoefficient = max(0.0, dot(normalize(eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular += specularCoefficient * gl_LightSource[i].specular * attenuation;
    }
}

void main(void)
{ 
	float shadowCoefficient = enableShadow ? shadowIntensity() : 1.0;
          						         
    vec4 ambient, diffuse, specular;
    ambient  = vec4(0.0);
    diffuse  = vec4(0.0);
    specular = vec4(0.0);
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, vertexNormal, ambient, diffuse, specular);
    }
             						         
	vec4 linearColor = shadowCoefficient * (ambient + diffuse + specular);
	
	vec4 checkerColor = vec4(0.0);
	
	float pixWidth = 0.0005;
	float boxWidth = 2.0;
	
	float x, y;
	
	for (y = -pixWidth * boxWidth; y <= pixWidth * boxWidth; y += pixWidth)
  			for (x = -pixWidth * boxWidth; x <= pixWidth * boxWidth; x += pixWidth)
    			checkerColor += getCheckerColor(gl_TexCoord[0].st + vec2(x, y));
    			
    checkerColor /= pow(2.0 * boxWidth + 1.0, 2.0);
    
    linearColor.rgb *= checkerColor.rgb;
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
    gl_FragData[2] = vec4(normalize(vertexNormal), eyeDir.z);
}