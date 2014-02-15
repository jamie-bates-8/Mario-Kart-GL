//#extension GL_ARB_gpu_shader5 : enable

uniform sampler2D   rainMap;
uniform sampler2D   colourMap;
uniform sampler2D   heightmap;

uniform sampler2D   bumpmap;
uniform samplerCube cubeMap;

varying mat3 tangentMatrix;

uniform float shininess;

varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec4 shadowCoord;
varying vec3 vertexPosition;

varying mat4 inverseMatrix;

uniform float timer;


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
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular += specularCoefficient * gl_LightSource[i].specular * attenuation;
    }
}

void main(void)
{
	vec2 texCoord = gl_TexCoord[0].st;
	
	float height, scale = 0.05, bias = 0.0;
	
	height = scale * (texture2D(heightmap, texCoord).r) - bias;
	texCoord -= height * normalize(eyeDir).xy;
	
    vec3 rain_normal = texture2D(rainMap, gl_TexCoord[0].st + vec2(0.0, timer * 0.01)).rgb;
    rain_normal *= 2.0; rain_normal -= 1.0; // map texel from [0,1] to [-1,1]
    
    vec3 normal = texture2D(bumpmap, texCoord).rgb;
    normal *= 2.0; normal -= 1.0; // map texel from [0,1] to [-1,1]
          			
    vec4 textureColor = texture2D(colourMap, texCoord);					         
    vec4 ambient, diffuse, specular;
    ambient  = vec4(0.0);
    diffuse  = vec4(0.0);
    specular = vec4(0.0);
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, normal, textureColor, ambient, diffuse, specular);
    }
    
    vec4 reflectDir = vec4(reflect(normalize(vertexPosition), normalize(tangentMatrix * (normal + rain_normal * 0.1))), 1.0);
    reflectDir = inverseMatrix * vec4(reflectDir.xyz, 1.0);
    vec3 envColor = vec3(textureCube(cubeMap, normalize(reflectDir.xyz)));

             						         
	vec4 linearColor = ambient + diffuse + specular;
	linearColor = vec4(mix(envColor, linearColor.rgb, shininess), 1.0);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
    gl_FragData[2] = vec4(normal, -eyeDir.z);
}