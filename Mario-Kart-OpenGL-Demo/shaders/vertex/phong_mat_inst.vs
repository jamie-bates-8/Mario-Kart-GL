#version 120
#extension GL_ARB_gpu_shader5 : enable

varying vec4 shadowCoord;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

attribute vec4 instance_data;
attribute mat4 instance_matrix;


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
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[6] * world_vertex;
}
