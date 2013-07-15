// bump.fs
//
// per-pixel bumpmapped Phong lighting

uniform sampler2D texture;
uniform sampler2D bumpmap;

varying vec3 lightDir;
varying vec3 eyeDir;

void main(void)
{
    vec3 normal = texture2D(bumpmap, gl_TexCoord[0].st).rgb;
    normal *= 2.0; normal -= 1.0; // map texel from [0,1] to [-1,1]


    // Diffuse Lighting
    float diffuse = max(0.0, dot(normalize(normal), normalize(lightDir)));
    vec4 color = diffuse * gl_LightSource[0].diffuse;


    // Ambient Light
    color += gl_LightSource[0].ambient;
	color *= texture2D(texture, gl_TexCoord[0].st);


    // Specular Light
	vec3 vReflection = normalize(reflect(normalize(lightDir), normalize(normal)));
    float specular = max(0.0, dot(normalize(eyeDir), vReflection));
    if(diffuse != 0)
	{
        specular = pow(specular, 128.0);
        color.rgb += gl_LightSource[0].specular * specular;
    }
	
	
	color.rgb *= gl_Color.rgb;
	gl_FragColor = color;
}