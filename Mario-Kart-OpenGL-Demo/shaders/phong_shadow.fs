// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

varying vec3 vNormal;
varying vec3 lightDir;
varying vec4 shadowCoord;

uniform sampler2D texture;
uniform sampler2DShadow shadowMap;

const float epsilon = 0.01;

float lookup(float x, float y)
{
	float depth = shadow2DProj(shadowMap, shadowCoord + vec4(x, y, 0, 0) * epsilon);
	return depth != 1.0 ? 0.75 : 1.0;
}

void main(void)
{ 
	vec4 color = vec4(1.0);
	

	// Diffuse Light
    float diffuse = max(0.0, dot(normalize(vNormal), normalize(lightDir)));
    color = diffuse * gl_LightSource[0].diffuse;
    color *= texture2D(texture, gl_TexCoord[0].st);
    

    // Ambient light
    color += gl_LightSource[0].ambient;


    // Specular Light
	vec3 vReflection = normalize(reflect(-normalize(lightDir), normalize(vNormal)));
    float specular = max(0.0, dot(normalize(vNormal), vReflection));
    
    if(diffuse != 0)
	{
        specular = pow(specular, 128.0);
        color.rgb += gl_LightSource[0].specular * specular;
    }
	
	
	color.rgb *= gl_Color.rgb;
	
	
	float shade = lookup(0.0, 0.0);
	gl_FragColor = vec4(shade * color.rgb, color.a);
}