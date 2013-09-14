
varying vec4 fragPos; // fragment coordinates
varying vec3 T, B, N; // tangent, bitangent, normal
varying vec3 viewPos;
varying vec3 worldPos;
varying float timer;

uniform sampler2D reflectionSampler;
uniform sampler2D refractionSampler;
uniform sampler2D normalSampler;
uniform sampler2D iceSampler;

uniform bool frozen;

uniform vec3 cameraPos;

//----------------
//tweakables

vec2 windDir = vec2(0.5, -0.8); // wind direction XY
float windSpeed = 0.2; // wind speed

float scale = 1.0; //overall wave scale

vec2 bWaves = vec2(0.30, 0.30); // strength of big waves
vec2 mWaves = vec2(0.30, 0.15); // strength of middle sized waves
vec2 sWaves = vec2(0.15, 0.10); // strength of small waves
    
float choppy = 0.15; // wave choppyness
float bump = 1.5; // overall water surface bumpyness
float reflBump = 0.20; // reflection distortion amount

float bloomLimit = 0.66;
//----------------

vec3 tangentSpace(vec3 v)
{
	vec3 vec;
	vec.xy = v.xy;
	vec.z = sqrt(1.0 - dot(vec.xy, vec.xy));
	vec.xyz = normalize(vec.x * T + vec.y * B + vec.z * N);
	return vec;
}

void main()
{
    vec2 fragCoord = (fragPos.st / fragPos.q) * 0.5 + 0.5;
    fragCoord = clamp(fragCoord, 0.002, 0.998);

	//normal map
	vec2 nCoord = vec2(0.0); //normal coords

  	nCoord = worldPos.xz * (scale * 0.05) + windDir * timer * (windSpeed * 0.04);
	vec3 normal0 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.015, -timer * 0.005)).rgb - 1.0;
	nCoord = worldPos.xz * (scale * 0.1) + windDir * timer * (windSpeed * 0.08) - (normal0.xy / normal0.z) * choppy;
	vec3 normal1 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.020, +timer * 0.015)).rgb - 1.0;
 
 	nCoord = worldPos.xz * (scale * 0.25) + windDir * timer * (windSpeed * 0.07) - (normal1.xy / normal1.z) * choppy;
	vec3 normal2 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.04, -timer * 0.03)).rgb - 1.0;
	nCoord = worldPos.xz * (scale * 0.5) + windDir * timer * (windSpeed * 0.09) - (normal2.xy / normal2.z) * choppy;
	vec3 normal3 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.03, +timer * 0.04)).rgb - 1.0;
  
  	nCoord = worldPos.xz * (scale * 1.0) + windDir * timer * (windSpeed * 0.4) - (normal3.xy / normal3.yz) * choppy;
	vec3 normal4 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.02, +timer * 0.1)).rgb - 1.0;  
    nCoord = worldPos.xz * (scale * 2.0) + windDir * timer * (windSpeed * 0.7) - (normal4.xy / normal4.yz) * choppy;
    vec3 normal5 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.1, -timer * 0.06)).rgb - 1.0;

	vec3 normal = normalize(normal0 * bWaves.x + normal1 * bWaves.y +
                            normal2 * mWaves.x + normal3 * mWaves.y +
						    normal4 * sWaves.x + normal5 * sWaves.y);
   
    vec3 nVec = tangentSpace(normal * bump); // converting normals to tangent space
    
    //texture edge bleed removal
    float fade = 12.0;
    
    vec2 distortFade = vec2(0.0);
    
    distortFade.s  = clamp(fragCoord.s * fade, 0.0, 1.0);
    distortFade.t  = clamp(fragCoord.t * fade, 0.0, 1.0);
    distortFade.s -= clamp(1.0 - (1.0 - fragCoord.s) * fade, 0.0, 1.0);
    distortFade.t -= clamp(1.0 - (1.0 - fragCoord.t) * fade, 0.0, 1.0); 

    vec3 final = texture2D(iceSampler, worldPos.xz * (scale * 0.01) + windDir * timer * 2.0 * (windSpeed * 0.04) + (nVec.xz * reflBump * distortFade)).rgb;

    gl_FragData[0] = vec4(final, 1.0);
    
    vec3 brightColor = max(final - vec3(bloomLimit), vec3(0.0));
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    gl_FragData[1] = vec4(mix(vec3(0.0), final, bright), 1.0);
}
