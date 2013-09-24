// ADS Point Lighting Shader (Phong)

varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform bool enableAttenuation;

void main(void)
{ 
		float distanceToLight, attenuation;
    
    distanceToLight = length(lightDir);
    
    attenuation = enableAttenuation ? 1.0 / (gl_LightSource[0].constantAttenuation  +
             						         gl_LightSource[0].linearAttenuation    * distanceToLight +
             						         gl_LightSource[0].quadraticAttenuation * distanceToLight * distanceToLight) : 1.0;
    
     // Add in ambient light
    vec4 ambient = gl_LightSource[0].ambient * gl_Color;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(vNormal), normalize(lightDir)));
    vec4 diffuse = diffuseCoefficient * gl_LightSource[0].diffuse * gl_Color;

    // Specular Lighting
    vec4 specular = vec4(0.0);
    
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(normalize(-lightDir), normalize(vNormal));
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular = specularCoefficient * gl_LightSource[0].specular;
    }
	
	vec4 linearColor = ambient + attenuation * (diffuse + specular);
	
	gl_FragColor = vec4(linearColor.rgb, 1.0);
}
