// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform sampler2D texture;

void main(void)
{ 
	float distanceToLight, attenuation;
    
    distanceToLight = length(lightDir);
    
    attenuation = 1.0 / (gl_LightSource[0].constantAttenuation  +
             		     gl_LightSource[0].linearAttenuation    * distanceToLight +
             			 gl_LightSource[0].quadraticAttenuation * distanceToLight * distanceToLight);
             						        
             						         
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
	
	vec4 linearColor = ambient + attenuation * (diffuse + specular);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);	
}
