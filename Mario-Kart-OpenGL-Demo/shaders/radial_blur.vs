
varying vec4 focalCentre;
varying vec4 fragPosition;

uniform vec3 focalPosition;

void main(void)
{ 
    gl_Position  = gl_Vertex; // no transform
    fragPosition = gl_Vertex;
    
	focalCentre = gl_ModelViewProjectionMatrix * vec4(focalPosition, 1.0);
	
    gl_TexCoord[0].st = gl_MultiTexCoord0.st;
}