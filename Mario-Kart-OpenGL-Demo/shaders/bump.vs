// bump.vs

uniform mat4 ModelMatrix;
uniform vec3 lightPosition;

varying vec4 shadowCoord;
varying vec3 lightDir;
varying vec3 eyeDir;

attribute vec3 tangent;

void main(void)
{
	eyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);
	
	gl_Position = ftransform();
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[2] * (ModelMatrix * gl_Vertex);
	
	vec3 n = normalize(gl_NormalMatrix * gl_Normal);
	vec3 t = normalize(gl_NormalMatrix * tangent);
	vec3 b = cross(n, t);
	
	vec3 light = gl_LightSource[0].position.xyz - eyeDir;
	
	vec3 v;
	
	v.x = dot(light, t); // tangent
	v.y = dot(light, b); // binormal
	v.z = dot(light, n); // normal
	
	lightDir = v;
	
	v.x = dot(eyeDir, t);
	v.y = dot(eyeDir, b);
	v.z = dot(eyeDir, n);
	
	eyeDir = v;
	
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_FrontColor = gl_Color;
}