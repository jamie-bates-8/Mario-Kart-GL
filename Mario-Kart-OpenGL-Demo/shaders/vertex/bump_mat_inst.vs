#version 120
#extension GL_ARB_gpu_shader5 : enable


uniform mat4 model_matrix;
uniform vec3 camera_position;

varying vec4 shadowCoord;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec3 cameraVec;

attribute vec3 tangent;
attribute vec4 instance_data;
attribute mat4 instance_matrix;

flat varying int alt_material;

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
	
    vec3 n = normalize(normal_matrix * gl_Normal);
	vec3 t = normalize(normal_matrix * tangent);
	vec3 b = cross(n, t);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = vertex.xyz;
    cameraVec = camera_position - world_vertex.xyz;
	
	vec3 light, v;
	
	for(int i = 0; i < 8; i++)
	{
		light = gl_LightSource[i].position.xyz - position;
	
		v.x = dot(light, t); // tangent
		v.y = dot(light, b); // bitangent
		v.z = dot(light, n); // normal
	
		lightDir[i] = v;
	}
	
	v.x = dot(eyeDir, t);
	v.y = dot(eyeDir, b);
	v.z = dot(eyeDir, n);
	
	eyeDir = v;
	
	v.x = dot(cameraVec, t);
	v.y = dot(cameraVec, b);
	v.z = dot(cameraVec, n);
	
	cameraVec = v;
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[6] * world_vertex;
	
	alt_material = (offset.a == 1.0) ? 1 : 0;
}
