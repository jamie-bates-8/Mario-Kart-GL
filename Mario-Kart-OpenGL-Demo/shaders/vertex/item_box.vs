#extension GL_ARB_gpu_shader5 : enable

const float FRESNEL_POWER = 5.0;

varying vec3 reflectDir;
varying vec3 refractDir;

varying float ratio;

uniform mat4 cameraMatrix;

uniform float eta;
uniform float reflectance;

varying vec3 vertexNormal;
varying vec3 eyeDir;
varying float eyeDist;


void main()
{
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	vec3 position = (vertex / vertex.w).xyz;
	
	vec3 viewDir = normalize(vertex.xyz);
	vec3 normal  = normalize(gl_NormalMatrix * gl_Normal);
	
	eyeDist = length(vertex.xyz);
	
	eyeDir = -viewDir;
	vertexNormal = normal;
	
	ratio = reflectance + (1.0 - reflectance) * pow((1.0 - dot(-viewDir.xyz, normal.xyz)), FRESNEL_POWER);
	
	viewDir = (inverse(cameraMatrix) * vec4(viewDir, 1.0)).xyz;
	normal  = (inverse(cameraMatrix) * vec4(normal, 0.0)).xyz;
	
	reflectDir = reflect(viewDir, normal);
	refractDir = refract(viewDir, normal, eta);
	
	vec4 texCoord = (gl_Vertex + 1.0) * 0.5;
	gl_TexCoord[0].xyz = texCoord.xyz;
	float dist = (texCoord.x + texCoord.y + texCoord.z) / 3.0;
	
	gl_FrontColor = vec4(dist * 0.167, 1.0, 1.0, 1.0);

	gl_Position = ftransform();
}