// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 eyeDir;

uniform vec3  rim_color;
uniform float rim_power;

uniform sampler2D normalSampler;
uniform sampler2D runeSampler;

uniform bool enableRunes;

uniform float timer;

float rimLight()
{
    float d = dot(normalize(vertexNormal), normalize(-eyeDir));
	vec3 normal = (d < 0.0) ? -vertexNormal : vertexNormal;
    float f = 1.0 - dot(normalize(normal), normalize(-eyeDir));

    f = smoothstep(0.0, 1.0, f);
    f = pow(f, rim_power);

    return f;
}

vec3 perturb(vec2 coords)
{
	float bend  = 0.4;
	float scale = 1.0;
	
	vec2  windDir   = vec2(0.5, -0.0); // wind direction (x, z)
	float windSpeed = 0.2;             // wind speed
	
	vec3 color = vec3(0.0);
	vec2 coord = vec2(0.0); // normal coords
	
	coord = coords * (scale * 0.025) + windDir * timer * (windSpeed * 0.03);
	color += texture2D(normalSampler, coord + vec2(-timer * 0.005, -timer * 0.010)).rgb * 0.20;
	coord = coords * (scale * 0.100) + windDir * timer * (windSpeed * 0.05) - (color.xy / color.z) * bend;
	color += texture2D(normalSampler, coord + vec2(+timer * 0.010, +timer * 0.005)).rgb * 0.20;

	coord = coords * (scale * 0.2) + windDir * timer * (windSpeed * 0.1) - (color.xy / color.z) * bend;
	color += texture2D(normalSampler, coord + vec2(-timer * 0.02, -timer * 0.03)).rgb * 0.20;
	coord = coords * (scale * 0.5) + windDir * timer * (windSpeed * 0.2) - (color.xy / color.z) * bend;
	color += texture2D(normalSampler, coord + vec2(+timer * 0.03, +timer * 0.02)).rgb * 0.15;
	
	coord = coords * (scale * 0.8) + windDir * timer * (windSpeed * 1.0) - (color.xy / color.z) * bend;
	color += texture2D(normalSampler, coord + vec2(-timer * 0.06, +timer * 0.08)).rgb * 0.15;
	coord = coords * (scale * 1.0) + windDir * timer * (windSpeed * 1.3) - (color.xy / color.z) * bend;
	color += texture2D(normalSampler, coord + vec2(+timer * 0.08, -timer * 0.06)).rgb * 0.10;
	
	return color;
}

void main(void)
{        	
				
	float textureColor = 1.0 - perturb(gl_TexCoord[0].st * 50.0).z;
	vec3    fieldColor = clamp(pow(vec3(textureColor) * 5.5, vec3(5.5 * 1.0)), 0.0, 1.0);
	
	vec4 runeColor = texture2D(runeSampler, gl_TexCoord[0].st * 10.0 + 0.1 * timer);
	
	float rimFactor = rimLight();
             						         
	vec4 linearColor1 = vec4((fieldColor + rimFactor) * rim_color, 0.1 + fieldColor.r);
	vec4 linearColor2 = vec4(runeColor.rgb * rim_color, rimFactor * runeColor.a);
	
	vec4 linearColor = mix(linearColor1, linearColor2, 0.5);
	
	gl_FragData[0] = enableRunes ? linearColor : linearColor1;
    gl_FragData[1] = enableRunes ? linearColor : linearColor1;
    gl_FragData[2] = vec4(normalize(vertexNormal), eyeDir.z);
}
