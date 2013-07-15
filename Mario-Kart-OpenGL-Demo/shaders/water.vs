
attribute vec4 tangent;

varying vec4 fragPos;
varying vec3 T, B, N; // tangent binormal normal
varying vec3 viewPos, worldPos;
varying float timer;

uniform mat4 ModelMatrix;

void main() 
{
	vec3 pos = vec3(gl_Vertex);
	
	T = tangent.xyz;
	B = cross(gl_Normal, tangent.xyz);
	N = gl_Normal; 

    worldPos = vec3(ModelMatrix * gl_Vertex);
    fragPos = ftransform();
    viewPos = pos - gl_ModelViewMatrixInverse[3].xyz;
    gl_Position = fragPos;
    timer = gl_Color.r * 2.0;
}
