#version 120
#extension GL_ARB_gpu_shader5 : enable

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
}
