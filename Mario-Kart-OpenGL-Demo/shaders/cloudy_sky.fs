#version 120
#extension GL_EXT_gpu_shader4 : enable

varying vec3 worldPos;

varying vec3 eyeDir;
varying vec3 vertexNormal;

uniform vec3 horizon;
uniform vec3 skyColor;

uniform float timer;
uniform float screenWidth;
uniform float screenHeight;
uniform float noiseResolution;

uniform sampler2D noiseSampler;

float calculateDitherPattern1()
{
	vec2 texCoord = gl_TexCoord[0].st;

	const int[16] ditherPattern = int[16] ( 0,  9,  3, 11,
									 	   13,  5, 15,  7,
									 	    4, 12,  2, 10,
									 	   16,  8, 14,  6 );

	vec2 count = vec2(0.0);
	     //count.x = floor(mod(texCoord.s *  screenWidth, 4.0));
		 //count.y = floor(mod(texCoord.t * screenHeight, 4.0));
		 
		 count.x = floor(mod(texCoord.s * 860.0, 4.0));
		 count.y = floor(mod(texCoord.t * 640.0, 4.0));

	int dither = ditherPattern[int(count.x) + int(count.y) * 4];

	return float(dither) / 17.0;
}

float calculateDitherPattern2()
{
	vec2 texCoord = gl_TexCoord[0].st;

	const int[64] ditherPattern = int[64] ( 1, 49, 13, 61,  4, 52, 16, 64,
										   33, 17, 45, 29, 36, 20, 48, 32,
										    9, 57,  5, 53, 12, 60,  8, 56,
										   41, 25, 37, 21, 44, 28, 40, 24,
										    3, 51, 15, 63,  2, 50, 14, 62,
										   35, 19, 47, 31, 34, 18, 46, 30,
										   11, 59,  7, 55, 10, 58,  6, 54,
										   43, 27, 39, 23, 42, 26, 38, 22);

	vec2 count = vec2(0.0);
	     //count.x = floor(mod(texCoord.s *  screenWidth, 8.0));
		 //count.y = floor(mod(texCoord.t * screenHeight, 8.0));
		 
		 count.x = floor(mod(texCoord.s * 860.0, 8.0));
		 count.y = floor(mod(texCoord.t * 640.0, 8.0));

	int dither = ditherPattern[int(count.x) + int(count.y) * 8];

	return float(dither) / 65.0;
}

float calculateSunglow()
{
	float curve = 4.0;

	//vec3 npos = normalize(surface.screenSpacePosition.xyz);
	//vec3 halfVector2 = normalize(-surface.lightVector + npos);
	//float factor = 1.0f - dot(halfVector2, npos);

	//return factor * factor * factor * factor;
	
	return 0.5;
}

float get3DNoise(vec3 position)
{
	position.z += 0.0;
	
	vec3 p = floor(position);
	vec3 f = fract(position);

	vec2 uv =  (p.xy + p.z * vec2(17.0f)) + f.xy;
	vec2 uv2 = (p.xy + (p.z + 1.0f) * vec2(17.0f)) + f.xy;
	
	vec2 coord  = (uv  + 0.5f) / 64.0;
	vec2 coord2 = (uv2 + 0.5f) / 64.0;
	
	float xy1 = texture2D(noiseSampler, coord ).x;
	float xy2 = texture2D(noiseSampler, coord2).x;
	
	return mix(xy1, xy2, f.z);
}

vec4 getWorldSpacePosition(vec2 coord, float depth)
{
	//vec4 pos = GetScreenSpacePosition(coord, depth);
    //pos = gbufferModelViewInverse * pos;
	//pos.xyz += cameraPosition.xyz;

	return vec4(0.0);
}

vec4 cloudColour(vec3 worldPosition, float sunglow)
{
	float cloudHeight = 140.0;
	float cloudDepth  = 25.0;
	float cloudUpperHeight = cloudHeight + (cloudDepth / 2.0);
	float cloudLowerHeight = cloudHeight - (cloudDepth / 2.0);

	if (worldPosition.y < cloudLowerHeight || worldPosition.y > cloudUpperHeight) return vec4(0.0);

	vec3  p = worldPosition.xyz / 40.0;
	float t = timer;
	
	p.x += t * 0.005;
		
		
	float noise  = 			   get3DNoise(p) 			   * 1.0;	p *= 4.0;	p.x  += t * 0.07;
		  noise += (1.0f - abs(get3DNoise(p) * 3.0 - 1.0)) * 0.30;	p *= 3.0;	p.xz += t * 0.15;
		  noise += (1.0f - abs(get3DNoise(p) * 3.0 - 1.0)) * 0.085;	p *= 2.0;	p.xz += t * 0.15;
		  noise += (1.0f - abs(get3DNoise(p) * 3.0 - 1.0)) * 0.06;
		  noise /= 1.2;


	float cloudAltitudeWeight = 1.0 - clamp(distance(worldPosition.y, cloudHeight) / (cloudDepth / 2.0), 0.0, 1.0);
		  cloudAltitudeWeight = pow(cloudAltitudeWeight, 0.15);

	noise *= cloudAltitudeWeight;

	//cloud edge
	float coverage = 0.4;
	float density = 0.05;
		
	noise = clamp(noise - (1.0 - coverage), 0.0, 1.0 - density) / (1.0 - density);

	float sunProximity = pow(sunglow, 1.0);
	float propigation = mix(3.0, 18.0, sunProximity);

	float heightGradient = clamp(( - (cloudLowerHeight - worldPosition.y) / cloudDepth), 0.0, 1.0);

	float directLightFalloff  = pow(heightGradient, propigation);
		  directLightFalloff *= mix(clamp(pow(noise, 0.85) * 2.5, 0.0, 1.0), clamp(pow(1.0 - noise, 10.3), 0.0, 0.5), pow(sunglow, 1.2));

	vec3 colorDirect = skyColor;
	     colorDirect *= 1.0 + pow(sunglow, 10.0) * 100.0;
		 colorDirect *= 1.0 + pow(sunglow,  2.0) * 100.0;


	vec3 colorAmbient = mix(horizon, skyColor, 0.15) * 0.075;
		 colorAmbient *= 1.0f + clamp(pow(noise, 1.0) * 1.0, 0.0, 1.0);
		 colorAmbient *= heightGradient + 0.75f;

	vec3 color  = mix(colorAmbient, colorDirect, vec3(min(1.0, directLightFalloff * 4.0)));
		 color *= clamp(pow(noise, 0.1), 0.0, 1.0);

	vec4 result = vec4(color.rgb, noise);

	return result;
}

vec3 calculateClouds(vec3 color)
{
	vec2 texCoord = gl_TexCoord[0].st;

	vec4 worldPosition = vec4(worldPos, 1.0);
		 worldPosition.xyz += eyeDir.xyz;

	float cloudHeight  = 140.0;
	float cloudDepth   =  25.0;
	float cloudDensity =   1.8;

	float rayDepth = 1.0f;
		  rayDepth += calculateDitherPattern1() * 0.0001;
		  rayDepth += calculateDitherPattern2() * 0.09;
	float rayIncrement = 0.08f;

	float previousRayDistance = 0.1;
	int i = 0;

	vec3 cloudColor = skyColor;
	vec4 cloudSum = vec4(0.0);
		 cloudSum.rgb = horizon * 0.2;
		 cloudSum.rgb = color.rgb;

		float sunglow = calculateSunglow();

		while (rayDepth > 0.0)
		{
			//determine worldspace ray position
			vec4 rayPosition = getWorldSpacePosition(texCoord.st, pow(rayDepth, 0.0005 * (1.0 - rayDepth)));

			if (rayPosition.y > cloudHeight - (cloudHeight / 2.0) && rayPosition.y < cloudHeight + (cloudHeight / 2.0))
			{

				//determine screen space ray position 
				float rayDistance = length(rayPosition.xyz - eyeDir.xyz);

				float surfaceDistance = length(worldPosition.xyz - eyeDir.xyz);

				//if ray is past surface, don't accumulate
				if (rayDistance < surfaceDistance)
				{
					//add cloud density. Multiply by ray distance traveled
					if (i > 0)
					{
						float stepDistance = abs(rayDistance - previousRayDistance);
						
						vec4 proximity    = cloudColour(rayPosition.xyz, sunglow);
							 proximity.a *= min(stepDistance * cloudDensity, cloudDensity);

						cloudSum.rgb = mix(cloudSum.rgb, proximity.rgb, vec3(pow(min(1.0, proximity.a * 1.2), 0.1)));
						cloudSum.a += proximity.a * 1.0;
					} 
					else //add cloud density for first step
					{
						float stepDistance = 0.0f;
						
						vec4 proximity =  cloudColour(rayPosition.xyz, sunglow);
							 proximity.a *= min(stepDistance * 0.9, 1.0);

						cloudSum.rgb = mix(cloudSum.rgb, proximity.rgb, vec3(min(1.0, proximity.a * 2.0)));
						cloudSum.a += proximity.a;
					}

					//record previous ray position
					previousRayDistance = rayDistance;
				}

			}

			//increment ray
			rayDepth -= rayIncrement;
			i++;
		}

		return mix(color.rgb, cloudSum.rgb, vec3(min(1.0, cloudSum.a * 20.0)));
}

void main (void)
{ 
    float h = worldPos.y / 500.0;
    h = clamp(h, -1.0, 1.0);
	
	vec3 background = mix(horizon, skyColor, abs(h));
	background = vec3(timer * 0.0001);
	//background = calculateClouds(background);
	
	gl_FragData[0] = vec4(background, 1.0);
	//gl_FragData[1] = vec4(background, 1.0);
	
	vec3 brightColor = max(background - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), background, bright), 1.0);
	gl_FragData[2] = vec4(vertexNormal, eyeDir.z);
}