// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

uniform sampler2D texture;

void main(void)
{            						                  						         
    vec4 textureColor = texture2D(texture, gl_TexCoord[0].st);
    
     // Add in ambient light
    vec4 ambient = gl_LightSource[0].ambient * textureColor;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normalize(vertexNormal), normalize(lightDir)));
    vec4 toonColor = vec4(1.0);
    
    if(diffuseCoefficient < 0.80) toonColor = vec4(0.8);
    if(diffuseCoefficient < 0.60) toonColor = vec4(0.6);
    if(diffuseCoefficient < 0.40) toonColor = vec4(0.4);
    if(diffuseCoefficient < 0.20) toonColor = vec4(0.2);
    
    vec4 diffuse = toonColor * gl_LightSource[0].diffuse * textureColor;
	
	vec4 linearColor = ambient + diffuse;
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
    gl_FragData[1] = vec4(vec3(0.0), 1.0);	
}
