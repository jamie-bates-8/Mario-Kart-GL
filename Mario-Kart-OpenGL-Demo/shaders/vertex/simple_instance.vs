#extension GL_EXT_gpu_shader4 : enable

attribute vec4 instance_data;

void main(void) 
{ 
	vec4 offset = instance_data;
	
	vec3 model_vertex = gl_Vertex.xyz * offset.a;
	vec4 world_vertex = vec4(model_vertex + offset.xyz, gl_Vertex.a);

	vec4 vertex = gl_ModelViewMatrix           * world_vertex;
	gl_Position = gl_ModelViewProjectionMatrix * world_vertex;
	
	gl_ClipVertex = vertex;
}
