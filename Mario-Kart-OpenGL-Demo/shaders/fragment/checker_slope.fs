varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec3 vertexPosition;
varying vec3 reflectDir;

uniform float shininess;

varying vec2  gridScale;
uniform float minScale;

uniform sampler2D   patternMask;
uniform samplerCube cubeMap;

varying mat3 tangentMatrix;
varying mat4 inverseMatrix;

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

vec4 getCheckerColor(vec2 texCoord)
{
	texCoord = vec2(texCoord.x, 1.0 - texCoord.y);

	bool exterior = texture2D(patternMask, texCoord).r > 0.5;
	
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

void main(void)
{        						         
    vec4 ambient, diffuse, specular;
    ambient  = vec4(0.0);
    diffuse  = vec4(0.0);
    specular = vec4(0.0);
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, vertexNormal, ambient, diffuse, specular);
    }
             						         
	vec4 linearColor = ambient + diffuse;
    
    vec3 envColor = vec3(textureCube(cubeMap, reflectDir));
	linearColor = vec4(mix(envColor, linearColor.rgb, shininess), 1.0);	
	
 	linearColor += specular;
	
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
