
uniform float timer;

uniform sampler2D normalSampler;
uniform sampler2D sceneSampler;

uniform vec2 offsets_3x3[ 9];
uniform vec2 offsets_5x5[25];
uniform vec2 offsets_7x7[49];

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

void main(void)
{
    vec2 texCoord = gl_TexCoord[0].st;
    
    vec3 normal = vec3(0.0, 0.0, 1.0 / 8.0);
	
	float xInc = 1.0 / 640.0;
	float zInc = 1.0 / 860.0;
	
	float left  = texture2D(normalSampler, gl_TexCoord[0].st + vec2(-xInc, 0.0)).r;
	float right = texture2D(normalSampler, gl_TexCoord[0].st + vec2(+xInc, 0.0)).r;
	float up    = texture2D(normalSampler, gl_TexCoord[0].st + vec2(0.0, +zInc)).r;
	float down  = texture2D(normalSampler, gl_TexCoord[0].st + vec2(0.0, -zInc)).r;
	
	float xDiff = left - right;
	float zDiff = up - down;
	
	normal.x = xDiff / 2.0;
	normal.y = zDiff / 2.0;
	
	normal = normalize(normal);
    
    //normal = texture2D(normalSampler, texCoord * 4.0 + vec2(0.0, timer * 0.02)).rgb;
    //normal *= 2.0; normal -= 1.0;
    
    if(length(normal.z) < 1.0) texCoord += normal.xy * 0.075;
    
    vec4 final = texture2D(sceneSampler, texCoord);
	
	if(length(normal.z) < 1.0) final = guassian3x3(texCoord);

    gl_FragColor = vec4(final.rgb, 1.0);
}
