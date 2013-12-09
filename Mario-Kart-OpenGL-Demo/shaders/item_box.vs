#extension GL_ARB_gpu_shader5 : enable

const float FRESNEL_POWER = 5.0;

varying vec3 reflectDir;

varying vec3 refractRed;
varying vec3 refractGreen;
varying vec3 refractBlue;

varying float ratio;

uniform mat4 cameraMatrix;

uniform float eta;
uniform float reflectance;

varying vec3 vertexNormal;
varying vec3 eyeDir;


void main()
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	vec3 position = (vertex / vertex.w).xyz;
	
	vec3 viewDir = normalize(vertex.xyz);
	vec3 normal  = normalize(gl_NormalMatrix * gl_Normal);
	
	eyeDir = -viewDir;
	vertexNormal = normal;
	
	ratio = reflectance + (1.0 - reflectance) * pow((1.0 - dot(-viewDir.xyz, normal.xyz)), FRESNEL_POWER);
	
	viewDir = (inverse(cameraMatrix) * vec4(viewDir, 1.0)).xyz;
	normal  = (inverse(cameraMatrix) * vec4(normal, 0.0)).xyz;
	
	reflectDir = reflect(viewDir, normal);
	
	refractRed   = refract(viewDir, normal, eta - 0.01);
	refractGreen = refract(viewDir, normal, eta);
	refractBlue  = refract(viewDir, normal, eta + 0.01);
	
	gl_FrontColor = gl_Color;
	gl_Position = ftransform();
}