#version 120
#extension GL_EXT_gpu_shader4 : enable

// Samplers for pre-rendered color, normal and depth
uniform sampler2D colourSampler;
uniform sampler2D normalSampler; // normals stored in RGB channels, depth stored in alpha channel
uniform sampler2D randomSampler; // Uniform sampler containing completely random vectors

// Various uniforms controling SSAO effect
uniform float area;
uniform float falloff;
uniform float radius;

uniform float angle;
uniform float offset;
uniform float strength;

uniform vec3 sample_sphere[16];

const int samples = 16;

void main(void)
{
 	vec2 texCoord = gl_TexCoord[0].st;
  
  	float depth = texture2D(normalSampler, texCoord).a;
  
  	vec3 random = normalize(texture2D(randomSampler, texCoord * offset).rgb);
  	//random = abs(random);
  	//random = normalize((random * 2.0) - vec3(1.0));
 
  	vec3 position = vec3(texCoord, depth);
  	vec3 normal = texture2D(normalSampler, texCoord).xyz;
  
  	float radius_depth = radius * (1.0 - clamp(depth, 0.0, 10.0));
  	float occlusion = 0.0;
  	
  	for(int i = 0; i < samples; i++)
  	{
    	vec3 ray = radius_depth * reflect(sample_sphere[i], random);
    	if (dot(normalize(normal), ray) < 0.0) ray = -ray;
    	vec3 hemi_ray = position + ray;
    
    	vec4 occ_sample = texture2D(normalSampler, hemi_ray.xy);
    	
    	float occ_depth = occ_sample.a;
    	float difference = (depth - occ_depth);
    
    	float normDiff = (1.0 - dot(occ_sample.xyz, normal));
    	if(dot(ray, normal) > angle) occlusion += step(falloff, difference) * (1.0 - smoothstep(falloff, area, difference));
  	}
  	
  	float ao = 1.0 - strength * occlusion * (1.0 / samples);
  	vec4 object_color = texture2D(colourSampler, texCoord);
  	
  	gl_FragColor = vec4(ao * object_color);
}