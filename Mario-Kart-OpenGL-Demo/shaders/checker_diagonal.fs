varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec3 vertexPosition;

uniform float shininess;

varying vec2  texScale;
uniform float minScale;

uniform sampler2D   bumpmap;
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
	vec2  borderSize = 0.1 / texScale;
	vec2 _borderSize = 1.0 - borderSize;

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
	
	vec2 gridCoord = texCoord * texScale * minScale;
	
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
    
    vec3 normal = texture2D(bumpmap, gl_TexCoord[0].st * 5.0).rgb;
    normal *= 2.0; normal -= 1.0;
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, normal, ambient, diffuse, specular);
    }
             						         
	vec4 linearColor = ambient + diffuse + specular;
	
	vec4 checkerColor = vec4(0.0);
	
	float pixWidth = 0.0005;
	float boxWidth = 2.0;
	
	float x, y;
	
	for (y = -pixWidth * boxWidth; y <= pixWidth * boxWidth; y += pixWidth)
  			for (x = -pixWidth * boxWidth; x <= pixWidth * boxWidth; x += pixWidth)
    			checkerColor += getCheckerColor(gl_TexCoord[0].st + vec2(x, y));
    			
    checkerColor /= pow(2.0 * boxWidth + 1.0, 2.0);
    
    vec4 reflectDir = vec4(reflect(normalize(vertexPosition), normalize(tangentMatrix * normal)), 1.0);
    reflectDir = inverseMatrix * vec4(reflectDir.xyz, 1.0);
    vec3 envColor = vec3(textureCube(cubeMap, normalize(reflectDir.xyz)));
    
    linearColor.rgb *= checkerColor.rgb;
    
    linearColor = vec4(mix(envColor, linearColor.rgb, shininess), 1.0);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
    gl_FragData[2] = vec4(normalize(vertexNormal), eyeDir.z);
}
