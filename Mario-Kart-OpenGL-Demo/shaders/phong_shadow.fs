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
    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vNormal), normalize(lightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vec4 vFragColor = diff * gl_LightSource[0].diffuse;

    // Add in ambient light
    vFragColor += gl_LightSource[0].ambient;
	
	vFragColor *= texture2D(texture, gl_TexCoord[0].st);

    // Specular Light
	vec3 vReflection = normalize(reflect(-normalize(lightDir), normalize(vNormal)));
    float spec = max(0.0, dot(normalize(vNormal), vReflection));
    if(diff != 0)
	{
        float fSpec = pow(spec, 128.0);
        vFragColor.rgb += gl_LightSource[0].specular * fSpec;
    }
	
	vFragColor.rgb *= gl_Color.rgb;
	
	float shadeFactor = lookup(0.0, 0.0);
	gl_FragColor = vec4(shadeFactor * vFragColor.rgb, vFragColor.a);
}