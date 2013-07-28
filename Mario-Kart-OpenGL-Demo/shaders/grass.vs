#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D heightMap;
uniform sampler2D perturbMap;

uniform vec3 origin;
uniform int length;
uniform float spread;
uniform float timer;

const float pi = 3.141592;

int random(int seed, int iterations)
{
    int value = seed;
    int n;

    for (n = 0; n < iterations; n++)
 		value = ((value >> 7) ^ (value << 9)) * 15485863;

    return value;
}

vec4 random_vector(int seed)
{
    int r = random(gl_InstanceID, 4);
    int g = random(r, 2);
    int b = random(g, 2);
    int a = random(b, 2);
    
    float l = float(length); 

    return vec4(float(r & (length - 1)) / l,
                float(g & (length - 1)) / l,
                float(b & (length - 1)) / l,
                float(a & (length - 1)) / l);
}

mat4 construct_rotation_matrix(float angle)
{
    float st = sin(angle);
    float ct = cos(angle);

    return mat4(vec4( ct, 0.0,  st, 0.0),
                vec4(0.0, 1.0, 0.0, 0.0),
                vec4(-st, 0.0,  ct, 0.0),
                vec4(0.0, 0.0, 0.0, 1.0));
}

void main(void)
{
	vec4 offset = vec4(origin, 0.0);
	
	float l = float(length);
	
	float xoffset = float(gl_InstanceID >> int(log2(float(length))));
	float zoffset = float(gl_InstanceID  & (length - 1));
	
    offset += vec4(xoffset * spread, 0.0,
                   zoffset * spread, 0.0);

    vec2 texcoord = offset.xz / l;
    vec4 perturb  = texture2D(perturbMap, texcoord);
    
    vec2 hcoord = clamp(vec2(xoffset, zoffset) / l, 0.0, 1.0);
    float height = texture2D(heightMap, hcoord).r;

    float bend_factor = perturb.r * 1.0 * abs(cos(timer));
    float bend_amount = cos(gl_Vertex.y / 3.3);

    float angle = perturb.g * 2.0 * pi;
    mat4 rot = construct_rotation_matrix(angle);
    vec4 position = (rot * (gl_Vertex + vec4(0.0, 0.0, bend_amount * bend_factor, 0.0))) + offset;
    position *= vec4(1.0, perturb.b * 0.9 + 0.3, 1.0, 1.0);
    position += vec4(0, height, 0, 0);
    
    float avg1 = (perturb.r + perturb.g + perturb.b) / 3.0; 
    float avg2 = (perturb.r + perturb.g) / 2.0;
    float avg3 = (perturb.r + perturb.b) / 2.0;
    float avg4 = (perturb.b + perturb.g) / 2.0;
    
    float h = -avg2;
    vec2 d = vec2(0.5) - hcoord;
    float dist = dot(d, d);
    h -= dist * 10.0;
    
    position += vec4(4.5 * spread * avg3, h, 10.5 * spread * avg4, 0.0);
    
    vec3 deadGrass = vec3(0.651, 0.494, 0.290);
    vec3 lushGrass = vec3(0.647, 0.859, 0.173);
    vec3 goodGrass = vec3(0.200, 0.800, 0.200);
    
    vec3 color = mix(goodGrass, deadGrass, avg1);
    color = mix(color, lushGrass, avg2);
    
 	gl_FrontColor = vec4(color, 1.0) * 0.3;
    gl_Position   = gl_ModelViewProjectionMatrix * position;
}
