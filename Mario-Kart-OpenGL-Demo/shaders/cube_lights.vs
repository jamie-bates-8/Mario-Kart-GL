#extension GL_ARB_gpu_shader5 : enable

varying vec3 reflectDir;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

uniform mat4 cameraMatrix;

void main()
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * gl_Normal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    vec4 coord = vec4(reflect(position, vertexNormal), 1.0);
    coord = inverse(cameraMatrix) * coord;
    reflectDir = normalize(coord.xyz);
    
    eyeDir = vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
}