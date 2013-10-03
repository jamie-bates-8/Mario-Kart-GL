varying vec3 reflectDir;
varying vec3 vertexNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform float shininess;
uniform samplerCube cubeMap;

void main(void)
{  
    // Ambient Lighting
    vec4 ambient = gl_LightSource[0].ambient * gl_Color;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(vertexNormal), normalize(lightDir)));
    vec4 diffuse = diffuseCoefficient * gl_LightSource[0].diffuse * gl_Color;
    
    vec4 fragColor = ambient + diffuse;
    
    // Environment Mapping
    vec3 envColor = vec3(textureCube(cubeMap, reflectDir));
	fragColor = vec4(mix(envColor, fragColor.rgb, shininess), 1.0);

    // Specular Lighting
    vec4 specular = vec4(0.0);
    
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(normalize(-lightDir), normalize(vertexNormal));
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular = specularCoefficient * gl_LightSource[0].specular;
    }
	
	fragColor += specular;
	
	gl_FragData[0] = vec4(fragColor.rgb, 1.0);
	
	vec3 brightColor = max(fragColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), fragColor.rgb, bright), 1.0);
}