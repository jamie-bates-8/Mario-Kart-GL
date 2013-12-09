varying vec3 reflectDir;

varying vec3 refractRed;
varying vec3 refractGreen;
varying vec3 refractBlue;

varying float ratio;

uniform samplerCube cubeMap;
uniform float opacity;

varying vec3 vertexNormal;
varying vec3 eyeDir;


vec3 rimLight()
{
    float d = dot(normalize(vertexNormal), normalize(-eyeDir));
	vec3 normal = (d < 0.0) ? -vertexNormal : vertexNormal;
    float f = 1.0 - dot(normalize(normal), normalize(-eyeDir));

    f = smoothstep(0.0, 1.0, f);
    f = pow(f, 2.0);

    return f * vec3(0.7);
}

void main()
{
	vec3 refractColor, reflectColor;
	
	refractColor.r = vec3(textureCube(cubeMap, refractRed)).r;
	refractColor.g = vec3(textureCube(cubeMap, refractGreen)).g;
	refractColor.b = vec3(textureCube(cubeMap, refractBlue)).b;
	
	reflectColor = vec3(textureCube(cubeMap, reflectDir));
	
	vec3 color = mix(refractColor, reflectColor, ratio) * gl_Color.rgb;
	
	gl_FragData[0] = vec4(color + rimLight(), 1.0);
	gl_FragData[1] = vec4(0.0);
}