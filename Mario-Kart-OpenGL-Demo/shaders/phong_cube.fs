varying vec3 reflectDir;
varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform float shininess;
uniform samplerCube cubeMap;

void main(void)
{ 
    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vNormal), normalize(lightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 vFragColor = diff * gl_LightSource[0].diffuse;

    // Add in ambient light
    vFragColor += gl_LightSource[0].ambient;
    
    // Look up environment map value in cube map
	vec3 envColor = vec3(textureCube(cubeMap, reflectDir));
	vFragColor = vec4(mix(envColor, vFragColor.rgb, shininess), 1.0);

    // Specular Light
    if(diff > 0.0)
	{
		vec3 vReflection = reflect(normalize(-lightDir), normalize(vNormal));
    	float spec = max(0.0, dot(normalize(-eyeDir), vReflection));
	
        float fSpec = pow(spec, 128.0);
        vFragColor.rgb += gl_LightSource[0].specular.rgb * fSpec;
    }
	
	vFragColor.rgba *= gl_Color.rgba;
	
	//gl_FragColor = vFragColor;
	gl_FragData[0] = vFragColor;
	
	vec3 brightColor = max(vFragColor.rgb - vec3(1.0), vec3(0.0));
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    gl_FragData[1] = vec4(mix(vec3(0.0), vFragColor.rgb, bright), 1.0);
}