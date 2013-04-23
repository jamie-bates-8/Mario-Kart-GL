// bump.fs
//
// per-pixel bumpmapped Phong lighting

uniform sampler2D texture;
uniform sampler2D bumpmap;

varying vec3 lightDir;
varying vec3 eyeDir;

void main(void)
{
    vec3 N = texture2D(bumpmap, gl_TexCoord[0].st).rgb;
    // map texel from [0,1] to [-1,1]
    N *= 2.0;
    N -= 1.0;

    // calculate diffuse lighting
    float diff = max(0.0, dot(normalize(N), normalize(lightDir)));
    
     // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 vFragColor = diff * gl_LightSource[0].diffuse;

    // Add in ambient light
    vFragColor += gl_LightSource[0].ambient;
	
	vFragColor *= texture2D(texture, gl_TexCoord[0].st);

    // Specular Light
	vec3 vReflection = normalize(reflect(normalize(lightDir), normalize(N)));
    float spec = max(0.0, dot(normalize(eyeDir), vReflection));
    if(diff != 0)
	{
        float fSpec = pow(spec, 128.0);
        vFragColor.rgb += gl_LightSource[0].specular * fSpec;
    }
	
	vFragColor.rgb *= gl_Color.rgb;
	
	gl_FragColor = vFragColor;
}