#extension GL_EXT_gpu_shader4 : enable

uniform sampler1D dataMap;

uniform int count;

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

void main(void) 
{ 
	vec4 offset = texture1D(dataMap, float(gl_InstanceID) / count);
	
	vec3 model_vertex = gl_Vertex.xyz * offset.a;
	vec4 world_vertex = vec4(model_vertex + offset.xyz, gl_Vertex.a);

	vec4 vertex = gl_ModelViewMatrix           * world_vertex;
	gl_Position = gl_ModelViewProjectionMatrix * world_vertex;
	
	gl_ClipVertex = vertex;
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * gl_Normal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = -vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
}
