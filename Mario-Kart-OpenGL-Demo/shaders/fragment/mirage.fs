
uniform float timer;
uniform sampler2D normalSampler;

uniform sampler2D sceneSampler;
uniform sampler2D depthSampler;

uniform vec2 offsets_3x3[ 9];
uniform vec2 offsets_5x5[25];
uniform vec2 offsets_7x7[49];

vec2 windDir = vec2(0.8, 0.0); // wind direction XY
float windSpeed = 0.2; // wind speed

float scale = 1.0; //overall wave scale

vec2 bWaves = vec2(0.30, 0.30); // strength of big waves
vec2 mWaves = vec2(0.30, 0.15); // strength of middle sized waves
vec2 sWaves = vec2(0.15, 0.10); // strength of small waves
    
float choppy = 0.15; // wave choppyness
float bump = 1.5; // overall water surface bumpyness
float reflBump = 0.20; // reflection distortion amount

vec4 guassian3x3(vec2 texCoord)
{
	 vec4 sample[9];

    for (int i = 0; i < 9; i++)
        sample[i] = texture2D(sceneSampler, texCoord + offsets_3x3[i]);

    return
    
    (0.0751 * (sample[0] + sample[2] + sample[6] + sample[8])) +
    (0.1238 * (sample[1] + sample[3] + sample[5] + sample[7])) +
    (0.2041 *  sample[4]);
}

vec4 guassian5x5(vec2 texCoord)
{
	 vec4 sample[25];

    for (int i = 0; i < 25; i++)
        sample[i] = texture2D(sceneSampler, texCoord + offsets_5x5[i]);

//   1  4  7  4 1
//   4 16 26 16 4
//   7 26 41 26 7 / 273
//   4 16 26 16 4
//   1  4  7  4 1

    return
    (
    	(1.0  * (sample[ 0] + sample[ 4] + sample[20] + sample[24])) +
        (4.0  * (sample[ 1] + sample[ 3] + sample[ 5] + sample[ 9]   +
                 sample[15] + sample[19] + sample[21] + sample[23])) +
        (7.0  * (sample[ 2] + sample[10] + sample[14] + sample[22])) +
        (16.0 * (sample[ 6] + sample[ 8] + sample[16] + sample[18])) +
        (26.0 * (sample[ 7] + sample[11] + sample[13] + sample[17])) +
        (41.0 *  sample[12])
    ) / 273.0;
}
                                       
vec4 guassian7x7(vec2 texCoord)
{
	 vec4 sample[49];

    for (int i = 0; i < 49; i++)
        sample[i] = texture2D(sceneSampler, texCoord + offsets_7x7[i]);

    return
    
    (0.0010 * (sample[ 2] + sample[ 4] + sample[14] + sample[20]   +
               sample[28] + sample[34] + sample[44] + sample[46])) +
    (0.0017 * (sample[ 3] + sample[21] + sample[27] + sample[45])) +
    (0.0029 * (sample[ 8] + sample[12] + sample[36] + sample[40])) +
    (0.0130 * (sample[ 9] + sample[11] + sample[15] + sample[19]   +
               sample[29] + sample[33] + sample[37] + sample[39])) +
    (0.0215 * (sample[10] + sample[22] + sample[26] + sample[38])) +
    (0.0585 * (sample[16] + sample[18] + sample[30] + sample[32])) +  
    (0.0965 * (sample[17] + sample[23] + sample[25] + sample[31])) +
    (0.1592 *  sample[24]);
}

vec3 tangentSpace(vec3 v)
{
	vec3 vec;
	vec.xy = v.xy;
	vec.z = sqrt(1.0 - dot(vec.xy, vec.xy));
	vec.xyz = normalize(vec.x * vec3(1.0, 0.0, 0.0) + vec.y * vec3(0.0, 1.0, 0.0) + vec.z * vec3(0.0, 0.0, 1.0));
	return vec;
}

void main(void)
{
	vec2 fragCoord = gl_TexCoord[0].st;

	//normal map
	vec2 nCoord = vec2(0.0); //normal coords

  	nCoord = fragCoord * (scale * 0.05) + windDir * timer * (windSpeed * 0.04);
	vec3 normal0 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.015, -timer * 0.005)).rgb - 1.0;
	nCoord = fragCoord * (scale * 0.1) + windDir * timer * (windSpeed * 0.08) - (normal0.xy / normal0.z) * choppy;
	vec3 normal1 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.020, +timer * 0.015)).rgb - 1.0;
 
 	nCoord = fragCoord * (scale * 0.25) + windDir * timer * (windSpeed * 0.07) - (normal1.xy / normal1.z) * choppy;
	vec3 normal2 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.04, -timer * 0.03)).rgb - 1.0;
	nCoord = fragCoord * (scale * 0.5) + windDir * timer * (windSpeed * 0.09) - (normal2.xy / normal2.z) * choppy;
	vec3 normal3 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.03, +timer * 0.04)).rgb - 1.0;
  
  	nCoord = fragCoord * (scale * 1.0) + windDir * timer * (windSpeed * 0.4) - (normal3.xy / normal3.yz) * choppy;
	vec3 normal4 = 2.0 * texture2D(normalSampler, nCoord + vec2(-timer * 0.02, +timer * 0.1)).rgb - 1.0;  
    nCoord = fragCoord * (scale * 2.0) + windDir * timer * (windSpeed * 0.7) - (normal4.xy / normal4.yz) * choppy;
    vec3 normal5 = 2.0 * texture2D(normalSampler, nCoord + vec2(+timer * 0.1, -timer * 0.06)).rgb - 1.0;

	vec3 normal = normalize(normal0 * bWaves.x + normal1 * bWaves.y +
                            normal2 * mWaves.x + normal3 * mWaves.y +
						    normal4 * sWaves.x + normal5 * sWaves.y);
   
    vec3 nVec = tangentSpace(normal * bump); // converting normals to tangent space
    
    float depth = texture2D(depthSampler, gl_TexCoord[0].st).x;

    vec2 texCoord = gl_TexCoord[0].st + (nVec.xz * reflBump * (0.05 + depth / 10.0));
    texCoord.y -= 0.03;
    
    vec4 final = texture2D(sceneSampler, texCoord);
	
	if(depth  > 0.980) final = guassian3x3(texCoord);
	if(depth  > 0.990) final = guassian5x5(texCoord);
	if(depth  > 0.995) final = guassian7x7(texCoord);

    gl_FragColor = vec4(final.rgb, 1.0);
}
