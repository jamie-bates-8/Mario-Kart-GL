
varying vec2 lightPosition;

void main(void)
{ 
    gl_Position   = gl_Vertex; // no transform
    
	lightPosition  = (gl_ModelViewProjectionMatrix * gl_LightSource[0].position).st;
	lightPosition *= 2.0;
	lightPosition -= 1.0;
	
    gl_TexCoord[0].st = gl_MultiTexCoord0.st;
}