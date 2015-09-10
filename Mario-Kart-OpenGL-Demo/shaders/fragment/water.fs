
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

vec2 windDir = vec2(0.5, -0.8); //wind direction XY
float windSpeed = 0.2; //wind speed

float visibility = 18.0;

float scale = 0.5; //overall wave scale

vec2 bWaves = vec2(0.30, 0.30); //strength of big waves
vec2 mWaves = vec2(0.30, 0.15); //strength of middle sized waves
vec2 sWaves = vec2(0.15, 0.10); //strength of small waves

vec3 waterColor = vec3(0.2, 0.4, 0.5); //color of the water
float waterDensity = 0.0; //water density (0.0 - 1.0)
    
float choppy = 0.05; //wave choppyness
float aberration = 0.000; //chromatic aberration amount
float bump = 1.5; //overall water surface bumpyness
float reflBump = 0.05; //reflection distortion amount
float refrBump = 0.05; //refraction distortion amount

uniform int renderMode;

vec3 sunPos = vec3(gl_ModelViewMatrixInverse * gl_LightSource[0].position);
float sunSpec = 1000.0; //Sun specular hardness

float scatterAmount = 3.0; //amount of sunlight scattering of waves
vec3 scatterColor = vec3(0.0, 1.0, 0.95);// color of the sunlight scattering

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

float fresnel_dielectric(vec3 Incoming, vec3 Normal, float eta)
{
    /* compute fresnel reflectance without explicitly computing
       the refracted direction */
    float c = abs(dot(Incoming, Normal));
    float g = eta * eta - 1.0 + c * c;
    float result;

    if(g > 0.0)
    {
        g = sqrt(g);
        float A  = (g - c) / (g + c);
        float B  = (c * (g + c) - 1.0) / (c * (g - c) + 1.0);
        result = 0.5 * A * A * (1.0 + B * B);
    }
    else result = 1.0;  /* TIR (no refracted component) */

    return result;
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
   
    vec3 nVec = tangentSpace(normal * bump); //converting normals to tangent space    
    vec3 vVec = normalize(viewPos);
    vec3 lVec = normalize(sunPos);
    
    //normal for light scattering
	vec3 lNormal = normalize(normal0 * bWaves.x * 0.5 + normal1 * bWaves.y * 0.5 +
                             normal2 * mWaves.x * 0.2 + normal3 * mWaves.y * 0.2 +
						     normal4 * sWaves.x * 0.1 + normal5 * sWaves.y * 0.1);
						     
    lNormal = tangentSpace(lNormal * bump);
    vec3 pNormal = tangentSpace(vec3(0.0));
    
	vec3 lR  = reflect(lVec, lNormal);
    vec3 llR = reflect(lVec, pNormal);
    
    float sunFade = clamp((sunPos.y + 50.0) / 300.0, 0.0, 1.0);
    vec3 sunext = vec3(0.45, 0.55, 0.68); //sunlight extinction
    
	float s = clamp((dot(lR, vVec) * 2.0 - 1.2), 0.0, 1.0);
    float lightScatter = clamp((clamp(dot(-lVec, lNormal) * 0.7 + 0.3, 0.0, 1.0) * s) * scatterAmount, 0.0, 1.0) * sunFade * clamp(1.0 - exp(-(sunPos.y / 500.0)), 0.0, 1.0);
    scatterColor = mix(vec3(scatterColor) * vec3(1.0, 0.4, 0.0), scatterColor, clamp(1.0 - exp(-(sunPos.y / 500.0) * sunext), 0.0, 1.0));
    
    //texture edge bleed removal
    float fade = 12.0;
    
    vec2 distortFade = vec2(0.0);
    
    distortFade.s  = clamp(fragCoord.s * fade, 0.0, 1.0);
    distortFade.t  = clamp(fragCoord.t * fade, 0.0, 1.0);
    distortFade.s -= clamp(1.0 - (1.0 - fragCoord.s) * fade, 0.0, 1.0);
    distortFade.t -= clamp(1.0 - (1.0 - fragCoord.t) * fade, 0.0, 1.0); 
    
    vec3 reflection = texture2D(reflectionSampler, fragCoord + (nVec.xz * reflBump * distortFade)).rgb;
    
    vec3 luminosity = vec3(0.30, 0.59, 0.11);
	float reflectivity = pow(dot(luminosity, reflection.rgb * 2.0), 3.0);
    
    vec3 R = reflect(vVec, nVec);

    float specular = pow(max(dot(R, lVec), 0.0), sunSpec) * reflectivity;
    vec3 specColor = mix(vec3(1.0, 0.5, 0.2), vec3(1.0, 1.0, 1.0), clamp(1.0 - exp(-(sunPos.y * 2.0) * sunext), 0.0, 1.0));

    vec2 rcoord = reflect(vVec, nVec).xz;
    vec3 refraction = vec3(0.0);
    
    refraction.r = texture2D(refractionSampler, (fragCoord - (nVec.xz * refrBump * distortFade)) * 1.0).r;
    refraction.g = texture2D(refractionSampler, (fragCoord - (nVec.xz * refrBump * distortFade)) * 1.0 - (rcoord * aberration)).g;
    refraction.b = texture2D(refractionSampler, (fragCoord - (nVec.xz * refrBump * distortFade)) * 1.0 - (rcoord * aberration * 2.0)).b;
    
    //fresnel term
    float ior = 1.33;
    ior = (cameraPos.y > 0.0) ? (1.333 / 1.0) : (1.0 / 1.333); //air to water; water to air
    float eta = max(ior, 0.00001);
    float fresnel = fresnel_dielectric(-vVec, nVec, eta);
      
    float waterSunGradient = dot(normalize(cameraPos - worldPos), -normalize(sunPos));
    waterSunGradient = clamp(pow(waterSunGradient * 0.7 + 0.3, 2.0), 0.0, 1.0);  
    vec3 waterSunColor = vec3(0.0, 1.0, 0.85) * waterSunGradient;
    waterSunColor = (cameraPos.y < 0.0) ? waterSunColor * 0.5 : waterSunColor * 0.25; //below or above water?
   
    float waterGradient = dot(normalize(cameraPos - worldPos), vec3(0.0, -1.0, 0.0));
    waterGradient = clamp((waterGradient * 0.5 + 0.5), 0.2, 1.0);
    vec3 watercolor = (vec3(0.0078, 0.5176, 0.700) + waterSunColor) * waterGradient * 2.0;
    vec3 waterext = vec3(0.6, 0.9, 1.0); //water extinction
    
    watercolor = mix(watercolor * 0.3 * sunFade, watercolor, clamp(1.0 - exp(-(sunPos.y / 500.0) * sunext), 0.0, 1.0));
    
    float fog = length(cameraPos - worldPos) / visibility; 
    fog = (cameraPos.y < 0.0) ? fog : 1.0;
    fog = clamp(fog, 0.0, 1.0);
    
    float darkness = visibility * 2.0;
    darkness = clamp((cameraPos.y + darkness) / darkness, 0.0, 1.0);
    
    fresnel = clamp(fresnel, 0.0, 1.0);
        
    vec3 color = mix(mix(refraction, scatterColor, lightScatter), reflection, fresnel);
    //color = (cameraPos.y < 0.0) ? mix(clamp(refraction * 1.5, 0.0, 1.0), reflection, fresnel) : color;   
    //color = (cameraPos.y < 0.0) ? mix(color, watercolor * darkness, clamp(fog / waterext, 0.0, 1.0)) : color;
    
    if(frozen) color = mix(color, texture2D(iceSampler, worldPos.xz * 0.05).rgb, 0.25);

	vec3 final = color + (specColor * specular);

    if(renderMode == 1) gl_FragData[0] = vec4(final, 1.0);
    if(renderMode == 2) gl_FragData[0] = texture2D(reflectionSampler, fragCoord + (nVec.xz * reflBump * distortFade));
    if(renderMode == 3) gl_FragData[0] = texture2D(reflectionSampler, fragCoord);
    if(renderMode == 4) gl_FragData[0] = texture2D(refractionSampler, fragCoord);
    
    vec3 brightColor = max(final - vec3(bloomLimit), vec3(0.0));
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    gl_FragData[1] = vec4(mix(vec3(0.0), final, bright), 1.0);
}
