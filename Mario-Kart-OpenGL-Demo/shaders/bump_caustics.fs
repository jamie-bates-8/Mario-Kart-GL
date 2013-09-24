varying vec3 vNormal, vTangent, vBinormal;
varying vec3 worldPos, eyePos;
varying vec3 lightDir;
varying vec3 lightVec;
varying vec3 eyeDir;
varying vec4 shadowCoord;

uniform float timer;
uniform vec2 texScale;

uniform sampler2D texture;
uniform sampler2D normalMap;
uniform sampler2D bumpmap;

uniform sampler2DShadow shadowMap;

bool enableFringe = true;

uniform bool enableShadow;
uniform bool magma;
uniform int sampleMode;

float choppy = 0.8;
float refractAmount = 0.3;
float aberration = 0.2;
float visibility = 32.0; // water visibility

float waterLevel = 0.0;

vec2 windDir = vec2(0.5, -0.8); // wind direction (x, z)
float windSpeed = 0.2; // wind speed

float scale = 1.0; // overall wave scale

vec2 bWaves = vec2(0.30, 0.30); // strength of big waves
vec2 mWaves = vec2(0.30, 0.15); // strength of middle sized waves
vec2 sWaves = vec2(0.15, 0.10); // strength of small waves

const float epsilon = 0.05;
float illumination = 0.5;

float lookup(float x, float y)
{
	vec2 offset = vec2(mod(floor(gl_FragCoord.xy), 2.0));
	x += offset.x;
	y += offset.y;

	float depth = shadow2DProj(shadowMap, shadowCoord + vec4(x, y, 0, 0) * epsilon).x;
	return depth != 1.0 ? illumination : 1.0;
}

float lookup(sampler2DShadow map, vec4 coord, vec2 offset)
{
	float depth = shadow2DProj(shadowMap, vec4(coord.xy + offset * texScale * coord.w, coord.z, coord.w)).x;
	return depth != 1.0 ? illumination : 1.0;
}

float shadowIntensity()
{
	if(sampleMode == 0)
	{	
		return lookup(0.0, 0.0);
	}
	else if(sampleMode == 1)
	{
		float sum = 0.0;
		
		sum += lookup(-1.0, -1.0);
		sum += lookup(+1.0, -1.0);
		sum += lookup(-1.0, +1.0);
		sum += lookup(+1.0, +1.0);
			
		return sum * 0.25;
	}
	else if(sampleMode == 2)
	{
		float sum = 0.0;
		float x, y;

		for (y = -1.5; y <= 1.5; y += 1.0)
  			for (x = -1.5; x <= 1.5; x += 1.0)
    			sum += lookup(shadowMap, shadowCoord, vec2(x, y));
    				
    	return sum / 16.0;
	}	
}

vec3 tangentSpace(vec3 v)
{
	vec3 vec;
	vec.xz  = v.xz;
	vec.y   = sqrt(1.0 - dot(vec.xz, vec.xz));
	vec.xyz = normalize(vec.x * vTangent + vec.y * vNormal + vec.z * vBinormal);
	return vec;
}

vec3 intercept(vec3 lineP, vec3 lineN, vec3 planeN, float  planeD)
{
	float distance = (planeD - dot(planeN, lineP)) / dot(lineN, planeN);
	return lineP + lineN * distance;
}

vec3 perturb(sampler2D tex, vec2 coords, float bend)
{
	bend *= choppy;
	vec3 color = vec3(0.0);
	vec2 coord = vec2(0.0); // normal coords
	
	coord = coords * (scale * 0.025) + windDir * timer * (windSpeed * 0.03);
	color += texture2D(tex, coord + vec2(-timer * 0.005, -timer * 0.010)).rgb * 0.20;
	coord = coords * (scale * 0.100) + windDir * timer * (windSpeed * 0.05) - (color.xy / color.z) * bend;
	color += texture2D(tex, coord + vec2(+timer * 0.010, +timer * 0.005)).rgb * 0.20;

	coord = coords * (scale * 0.2) + windDir * timer * (windSpeed * 0.1) - (color.xy / color.z) * bend;
	color += texture2D(tex, coord + vec2(-timer * 0.02, -timer * 0.03)).rgb * 0.20;
	coord = coords * (scale * 0.5) + windDir * timer * (windSpeed * 0.2) - (color.xy / color.z) * bend;
	color += texture2D(tex, coord + vec2(+timer * 0.03, +timer * 0.02)).rgb * 0.15;
	
	coord = coords * (scale * 0.8) + windDir * timer * (windSpeed * 1.0) - (color.xy / color.z) * bend;
	color += texture2D(tex, coord + vec2(-timer * 0.06, +timer * 0.08)).rgb * 0.15;
	coord = coords * (scale * 1.0) + windDir * timer * (windSpeed * 1.3) - (color.xy / color.z) * bend;
	color += texture2D(tex, coord + vec2(+timer * 0.08, -timer * 0.06)).rgb * 0.10;
	
	return color;
}

void main()
{
    float waterSunGradient = dot(normalize(eyePos - worldPos), -normalize(lightDir));
    waterSunGradient = clamp(pow(waterSunGradient * 0.7 + 0.3, 2.0), 0.0, 1.0);
    
    float waterGradient = dot(normalize(eyePos - worldPos), vec3(0.0, -1.0, 0.0));
    waterGradient = clamp((waterGradient * 0.5 + 0.5), 0.2, 1.0);
    
    vec3 waterSunColor = vec3(0.0, 1.0, 0.85) * waterSunGradient;
    waterSunColor = (eyePos.y - waterLevel < 0.0) ? waterSunColor * 0.5 : waterSunColor * 0.25; // below or above water?
    
    vec3 waterColor = (vec3(0.0078, 0.5176, 0.700) + waterSunColor) * waterGradient * 2.0;
    
    vec3 waterext = vec3(0.60, 0.90, 1.00); // water extinction
    vec3 sunext   = vec3(0.45, 0.55, 0.70); // sunlight extinction

	vec3 normal = tangentSpace(vec3(0.0));
	
	vec3 EV = normalize(eyePos   - worldPos);
	vec3 LV	= normalize(lightDir - worldPos);
	
	vec3 waterEyePos = intercept(worldPos, eyePos - worldPos, vec3(0.0, 1.0, 0.0), waterLevel);     

	float NdotL = max(dot(normal, LV), 0.0);


    float sunFade = clamp((lightDir.y + 50.0) / 300.0, 0.0, 1.0);
    vec3 sunLight = mix(vec3(1.0, 0.5, 0.2), vec3(1.0), clamp(1.0 - exp(-(lightDir.y / 500.0) * sunext), 0.0, 1.0));
    sunLight *= NdotL * sunFade;
    
    
    //sky illumination
    float skyBright = max(dot(normal, vec3(0.0, 1.0, 0.0)) * 0.5 + 0.5, 0.0);   
    vec3 skyLight = mix(vec3(1.0, 0.5, 0.0) * 0.05, vec3(0.2, 0.5, 1.0), clamp(1.0 - exp(-(lightDir.y / 500.0) * sunext), 0.0, 1.0));
    skyLight *= skyBright;

    //ground illumination
    float groundBright = max(dot(normal, vec3(0.0, -1.0, 0.0)) * 0.5 + 0.5, 0.0);   
    vec3 groundLight = vec3(0.3) * 1.0 * clamp(1.0 - exp(-(lightDir.y / 500.0)), 0.0, 1.0);
    groundLight *= groundBright;
 
    float underwaterFresnel = pow(clamp(1.0 - dot(normal, EV), 0.0, 1.0), 2.0);
 
    // water fogging
       
	float topfog = length(waterEyePos - worldPos) / visibility;
    topfog = clamp(topfog, 0.0, 1.0);
    
    float underfog = length(eyePos - worldPos) / visibility;
    underfog = clamp(underfog, 0.0, 1.0);

	float depth = waterEyePos.y - worldPos.y; // water depth

    float shorecut    = smoothstep(-0.001, 0.0, depth);
    float shorewetcut = magma ? smoothstep(-0.400, 0.0, -abs(depth) + 0.4) : smoothstep(-0.200, 0.0, depth + 0.2);
    
    depth /= visibility;  
    depth = clamp(depth, 0.0, 1.0);
    
    float fog = (eyePos.y - waterLevel < 0.0) ? underfog : topfog; // below or above water?
    fog = fog * shorecut;
    
    float darkness = visibility * 1.5;
    float foginess = visibility * 2.0;
    
    darkness = mix(1.0, clamp((eyePos.y + darkness) / darkness, 0.0, 1.0), shorecut);
    foginess = mix(1.0, clamp((eyePos.y + foginess) / foginess, 0.0, 1.0), shorecut);
    
    //caustics
    vec3 causticPos = intercept(worldPos, lightDir - worldPos, vec3(0.0, 1.0, 0.0), waterLevel);
    float causticdepth = length(causticPos - worldPos); // caustic depth
    causticdepth = 1.0 - clamp(causticdepth / visibility, 0.0, 1.0);
    causticdepth = clamp(causticdepth, 0.0, 1.0);
 
    float causticR = 1.0 - perturb(normalMap, causticPos.xz, causticdepth).z;
        
    vec3 caustics = clamp(pow(vec3(causticR) * 5.5, vec3(5.5 * causticdepth)), 0.0, 1.0) * NdotL * sunFade * causticdepth;
    
    if(enableFringe)
    {
    	float causticG = 1.0 - perturb(normalMap, causticPos.xz + (1.0 - causticdepth) * aberration, causticdepth).z;
    	float causticB = 1.0 - perturb(normalMap, causticPos.xz + (1.0 - causticdepth) * aberration * 2.0, causticdepth).z;
    	caustics = clamp(pow(vec3(causticR, causticG, causticB) * 5.5, vec3(5.5 * causticdepth)), 0.0, 1.0) * NdotL * sunFade * causticdepth;
    }
    
    vec3 underwaterSunLight = clamp((sunLight + 0.8) - (1.0 - caustics), 0.0, 1.0) * causticdepth + (sunLight * caustics);
    
    underwaterSunLight = mix(underwaterSunLight, underwaterSunLight * waterColor, clamp((1.0 - causticdepth) / waterext, 0.0, 1.0));
    
    skyLight    = mix(skyLight,    skyLight    * waterColor, clamp((depth) / waterext, 0.0, 1.0));
    groundLight = mix(groundLight, groundLight * waterColor, clamp((depth) / waterext, 0.0, 1.0));

    sunLight = mix(sunLight, mix(underwaterSunLight, (waterColor * 0.8 + 0.4) * sunFade, underwaterFresnel), shorecut);

    vec3 color = vec3(sunLight + skyLight + groundLight) * darkness;

    waterColor = mix(waterColor * 0.3 * sunFade, waterColor, clamp(1.0 - exp(-(lightDir.y / 500.0) * sunext), 0.0, 1.0));
    
    
    normal = texture2D(bumpmap, gl_TexCoord[0].st).rgb;
    normal *= 2.0; normal -= 1.0; // map texel from [0,1] to [-1,1]

	//float distanceToLight = length(gl_LightSource[0].position.xyz - worldPos);
	//float attenuation = 1.0 / (1.0 + 0.1 * pow(distanceToLight, 2.0));
	//attenuation = 1.0;

    // Diffuse Lighting
    float diffuse = max(0.0, dot(normalize(normal), normalize(lightVec)));
    vec3 fcolor = diffuse * gl_LightSource[0].diffuse.rgb;


    // Ambient Light
    fcolor += gl_LightSource[0].ambient.rgb;
    fcolor *= texture2D(texture, gl_TexCoord[0].st).rgb;

    vec3 fogging = mix(fcolor * mix(vec3(1.0), vec3(0.8), shorewetcut) * color, underwaterSunLight * foginess, clamp(fog / waterext, 0.0, 1.0)); // adding water color fog
    if(magma) fogging = mix(fogging, vec3(1.0, 0.8, 0.6), shorewetcut);

    // Specular Light
    if(diffuse != 0.0)
	{
		vec3 vReflection = normalize(reflect(normalize(-lightVec), normalize(normal)));
    	float specular = max(0.0, dot(normalize(-eyeDir), vReflection));
	
        specular = pow(specular, 128.0);
        fogging.rgb += gl_LightSource[0].specular.rgb * specular;
    }
	
	
	fogging.rgb *= gl_Color.rgb;
	
	
	if(magma)
	{
		vec3 brightColor = max(fogging.rgb - vec3(0.8), vec3(0.0));
    	float bright = dot(brightColor, vec3(1.0));
    	bright = smoothstep(0.0, 0.5, bright);
    	gl_FragData[1] = vec4(mix(vec3(0.0), fogging.rgb, bright), 1.0);
	}
	
	if(enableShadow)
	{
		float sIntensity = shadowIntensity();
		gl_FragData[0] = vec4(sIntensity * fogging.rgb, 1.0);
	}
	else gl_FragData[0] = vec4(fogging, 1.0);
}