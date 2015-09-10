
varying vec4 lightPosition;
varying vec4 fragPosition;

void main(void)
{ 
    gl_Position  = gl_Vertex; // no transform
    fragPosition = gl_Vertex;
    
	lightPosition = (gl_ProjectionMatrix * gl_LightSource[0].position);
	
    gl_TexCoord[0].st = gl_MultiTexCoord0.st;
}