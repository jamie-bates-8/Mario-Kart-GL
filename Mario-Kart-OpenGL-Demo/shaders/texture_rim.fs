// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

uniform sampler2D texture;

uniform vec3  rim_color;
uniform float rim_power;

vec3 rimLight()
{
	float d = dot(normalize(vertexNormal), normalize(-eyeDir));
	vec3 normal = (d < 0.0) ? -vertexNormal : vertexNormal;
    float f = 1.0 - dot(normalize(normal), normalize(-eyeDir));

    f = smoothstep(0.0, 1.0, f);
    f = pow(f, rim_power);

    return f * rim_color;
}

void pointLight(in int i, in vec3 normal, in vec4 textureColor, inout vec4 ambient, inout vec4 diffuse, inout vec4 specular)
{
	float distanceToLight, attenuation;
    
    distanceToLight = length(lightDir[i]);
    
    attenuation = 1.0 / (gl_LightSource[i].constantAttenuation  +
             		     gl_LightSource[i].linearAttenuation    * distanceToLight +
             			 gl_LightSource[i].quadraticAttenuation * distanceToLight * distanceToLight);
    
     // Add in ambient light
    ambient += gl_LightSource[i].ambient * textureColor * attenuation;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(normal), normalize(lightDir[i])));
    diffuse += diffuseCoefficient * gl_LightSource[i].diffuse * textureColor * attenuation;

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
    vec4 textureColor = texture2D(texture, gl_TexCoord[0].st);
    
    vec4 ambient, diffuse, specular;
    ambient  = vec4(0.0);
    diffuse  = vec4(0.0);
    specular = vec4(0.0);
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, vertexNormal, textureColor, ambient, diffuse, specular);
    }
             						         
	vec4 linearColor = ambient + diffuse + specular + vec4(rimLight(), 1.0);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
    gl_FragData[2] = vec4(normalize(vertexNormal), eyeDir.z);
}
