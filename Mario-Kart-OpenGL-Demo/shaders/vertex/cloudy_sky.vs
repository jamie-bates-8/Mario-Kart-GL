
varying vec3 worldPos;

varying vec3 eyeDir;
varying vec3 vertexNormal;

void main() 
{
    worldPos = vec3(gl_Vertex);
    gl_Position = ftransform();
    
    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	vertexNormal = gl_NormalMatrix * -gl_Vertex.xyz;
	vertexNormal = normalize(vertexNormal);
    
    eyeDir = -vertex.xyz;
}