varying vec3 vertexNormal;
varying vec3 eyeDir;

void main() 
{
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = ftransform();
    
    vertexNormal = gl_NormalMatrix * normalize(gl_Vertex.xyz);
	vertexNormal = normalize(vertexNormal);
	
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	vec3 position = (vertex / vertex.w).xyz;
    eyeDir = vertex.xyz;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}