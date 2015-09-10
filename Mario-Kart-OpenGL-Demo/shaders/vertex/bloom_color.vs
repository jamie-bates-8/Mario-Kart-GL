

void main(void) 
{ 
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
	
	gl_FrontColor = gl_Color;
}
