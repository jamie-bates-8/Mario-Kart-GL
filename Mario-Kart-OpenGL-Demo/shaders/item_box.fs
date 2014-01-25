varying vec3 reflectDir;
varying vec3 refractDir;

varying float ratio;

uniform samplerCube cubeMap;

varying vec3 vertexNormal;
varying vec3 eyeDir;

uniform float timer;

vec3 hsv_to_rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 rimLight()
{
    float d = dot(normalize(vertexNormal), normalize(-eyeDir));
	vec3 normal = (d < 0.0) ? -vertexNormal : vertexNormal;
    float f = 1.0 - dot(normalize(normal), normalize(-eyeDir));

    f = smoothstep(0.0, 1.0, f);
    f = pow(f, 2.0);

    return f * vec3(0.66);
}

bool isPolkaDotLocation()
{
	bool skipDot = true;
	
	for(float i = 0.17; i < 0.88; i += 0.11)
	{
		for(float j = 0.17; j < 0.88; j += 0.11)
		{
			skipDot = !skipDot;
			if(skipDot) continue;
		
			vec2 p = vec2(i, j);
			
			float dist1 = length(gl_TexCoord[0].xy - p);
			float dist2 = length(gl_TexCoord[0].xz - p);
			float dist3 = length(gl_TexCoord[0].yz - p);
			
			if(dist1 < 0.05 || dist2 < 0.05 || dist3 < 0.05) return true;
		}
	}
	
	return false;
}

void main()
{
	vec3 refractColor, reflectColor;
	
	reflectColor = vec3(textureCube(cubeMap, reflectDir));
	refractColor = vec3(textureCube(cubeMap, refractDir));
	
	bool drawDot = isPolkaDotLocation();
	
	vec3 hsv = gl_Color.rgb;
	
	hsv.r += 0.001 * timer;
	if(drawDot) hsv.r += 0.0333;
	
	vec3 rgb = hsv_to_rgb(vec3(hsv.rgb));
	
	vec3 color = mix(refractColor, reflectColor, ratio);
	color = mix(color, rgb, drawDot ? 0.66 : 0.33);
	
	gl_FragData[0] = vec4(color + rimLight(), 1.0);
	gl_FragData[1] = vec4(0.0);
}