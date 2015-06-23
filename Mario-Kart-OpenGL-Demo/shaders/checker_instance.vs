#version 120
#extension GL_ARB_gpu_shader5 : enable

varying vec4 shadowCoord;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

varying vec2 gridScale;
uniform vec3 scaleVec;

uniform vec3 block_colors[4];

attribute vec4 instance_data;
attribute mat4 instance_matrix;

vec2 getTextureScale()
{
	vec3 normal = abs(gl_Normal);

	float maximum = normal.x < normal.y ? normal.y : normal.x;
	if(normal.z > maximum) return scaleVec.xy;
	
	if(maximum == normal.x) return scaleVec.zy;
	else return scaleVec.xz;	
}

void main(void) 
{ 
	vec4 offset = instance_data;
	
	vec3 model_vertex = (instance_matrix * gl_Vertex).xyz;
	vec4 world_vertex = vec4(model_vertex + offset.xyz, gl_Vertex.a);

	vec4 vertex = gl_ModelViewMatrix           * world_vertex;
	gl_Position = gl_ModelViewProjectionMatrix * world_vertex;
	
	gl_ClipVertex = vertex;
	
	mat3 normal_matrix = mat3(gl_ModelViewMatrix * instance_matrix);
	normal_matrix = transpose(inverse(normal_matrix));
	
    vertexNormal = normalize(normal_matrix * gl_Normal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = -vertex.xyz;
	
	// Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
    gl_FrontColor = vec4(block_colors[int(offset.a)], 1.0);
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[6] * world_vertex;
	
	gridScale = getTextureScale();
}
