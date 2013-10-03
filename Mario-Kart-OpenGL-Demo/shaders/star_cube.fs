varying vec3 reflectDir;
varying vec3 vertexNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform float shininess;
uniform samplerCube cubeMap;

void main(void)
{
    float diffuseCoefficient = max(0.0, dot(normalize(vertexNormal), normalize(lightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 color = vec4(diffuseCoefficient);

    // Add in ambient light
    color += vec4(0.5);
    
    // Look up environment map value in cube map
	vec3 envColor = vec3(textureCube(cubeMap, reflectDir));
	color = vec4(mix(envColor, color.rgb, shininess), 1.0);

    // Specular Light
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(normalize(-lightDir), normalize(vertexNormal));
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        color.rgb += vec3(specularCoefficient);
    }
	
	color.rgba *= gl_Color.rgba;
	
	gl_FragData[0] = color;
	
	vec3 brightColor = max(color.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), color.rgb, bright), 1.0);
}