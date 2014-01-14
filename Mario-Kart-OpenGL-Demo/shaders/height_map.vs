uniform sampler2D heightMap;

void main(void) 
{ 
	float height = texture2D(heightMap, gl_MultiTexCoord0.st).r;
	
	vec3 model_vertex = gl_Vertex.xyz;
	vec4 world_vertex = vec4(model_vertex + vec3(0.0, height, 0.0), gl_Vertex.a);

	vec4 vertex = gl_ModelViewMatrix           * world_vertex;
	gl_Position = gl_ModelViewProjectionMatrix * world_vertex;
	
	gl_ClipVertex = vertex;
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
