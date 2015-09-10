
void main() 
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_TexCoord[1] = gl_MultiTexCoord1;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
}