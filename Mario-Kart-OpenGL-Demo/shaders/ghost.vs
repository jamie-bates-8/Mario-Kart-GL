#extension GL_ARB_gpu_shader5 : enable

const float ETA = 0.97;
const float FRESNEL_POWER = 5.0;
const float REFLECTANCE = ((1.0 - ETA) * (1.0 - ETA)) / ((1.0 + ETA) * (1.0 + ETA));

varying vec3 reflectDir;
varying vec3 refractDir;

varying float ratio;

uniform mat4 cameraMatrix;

uniform float eta;
uniform float reflectance;


void main()
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	vec3 position = (vertex / vertex.w).xyz;
	
	vec3 eyeDir = normalize(vertex.xyz);
	vec3 vertexNormal = normalize(gl_NormalMatrix * gl_Normal);
	
	ratio = reflectance + (1.0 - reflectance) * pow((1.0 - dot(-eyeDir, vertexNormal)), FRESNEL_POWER);
	
	vec4 coord = vec4(reflect(eyeDir, vertexNormal), 1.0);
    coord = inverse(cameraMatrix) * coord;
    reflectDir = normalize(coord.xyz);
    
    coord = vec4(refract(eyeDir, vertexNormal, eta), 1.0);
    coord = inverse(cameraMatrix) * coord;
    refractDir = normalize(coord.xyz);

	gl_Position = ftransform();
}