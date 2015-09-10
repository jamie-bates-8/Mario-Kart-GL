

void main(void) 
{ 
    gl_Position   = ftransform();
	gl_PointSize  = gl_Point.size;
	gl_FrontColor = gl_Color;
}
