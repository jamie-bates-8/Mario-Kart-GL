

void main() 
{
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = ftransform();
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}