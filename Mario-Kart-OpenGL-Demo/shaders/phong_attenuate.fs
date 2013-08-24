// ADS Point Lighting Shader (Phong)

varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 worldPos;

uniform float factor;

uniform sampler2D texture;

void main(void)
{ 
    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vNormal), normalize(lightDir)));
    
    vec3 surfaceColor = texture2D(texture, gl_TexCoord[0].st).rgb;
    
    float distanceToLight = length(gl_LightSource[0].position.xyz - worldPos);
	float attenuation = 1.0 / (1.0 + factor * pow(distanceToLight, 2.0));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec3 diffuse = diff * gl_LightSource[0].diffuse.rgb * surfaceColor.rgb;

    // Add in ambient light
    vec3 ambient = gl_LightSource[0].ambient.rgb * surfaceColor.rgb;

    // Specular Light
	vec3 vReflection = normalize(reflect(-normalize(lightDir), normalize(vNormal)));
    float spec = max(0.0, dot(normalize(vNormal), vReflection));
    vec3 specular = vec3(0.0);
    if(diff != 0.0)
	{
        float fSpec = pow(spec, 128.0);
        specular = gl_LightSource[0].specular.rgb * fSpec;
    }
	
	vec3 vFragColor = ambient + attenuation * (diffuse + specular);
	
	gl_FragColor = vec4(vFragColor, 1.0);
}
