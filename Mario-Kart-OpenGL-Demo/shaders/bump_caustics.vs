
varying vec3 lightDir;
varying vec3 lightVec;
varying vec3 eyeDir;

varying vec3 worldPos, eyePos;
varying vec3 vNormal, vTangent, vBinormal;
varying float timer;

uniform mat4 ModelMatrix;
uniform float clock;

attribute vec3 tangent;

mat3 getRotation(mat4 m)
{
	mat3 result;
	
	result[0][0] = m[0][0]; 
	result[0][1] = m[0][1]; 
	result[0][2] = m[0][2]; 

	result[1][0] = m[1][0]; 
	result[1][1] = m[1][1]; 
	result[1][2] = m[1][2]; 
	
	result[2][0] = m[2][0]; 
	result[2][1] = m[2][1]; 
	result[2][2] = m[2][2]; 
	
	return result;
}

void main(void)
{
	worldPos = vec3(ModelMatrix * gl_Vertex);  
	
	vTangent  = getRotation(ModelMatrix) * tangent;
	vBinormal = getRotation(ModelMatrix) * cross(gl_Normal, tangent);
	vNormal   = getRotation(ModelMatrix) * gl_Normal; 

	eyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	
	vec3 n = normalize(gl_NormalMatrix * gl_Normal);
	vec3 t = normalize(gl_NormalMatrix * tangent);
	vec3 b = cross(n, t);
	
	vec3 light = gl_LightSource[0].position.xyz - gl_Vertex.xyz;
	
	vec3 v;
	
	v.x = dot(light, t); // tangent
	v.y = dot(light, b); // binormal
	v.z = dot(light, n); // normal
	
	lightVec = normalize(v);
	lightDir = vec3(gl_ModelViewMatrixInverse * gl_LightSource[0].position);
	
	v.x = dot(eyeDir, t);
	v.y = dot(eyeDir, b);
	v.z = dot(eyeDir, n);
	
	eyeDir = normalize(v);
	eyePos = gl_ModelViewMatrixInverse[3].xyz;
	
	gl_FrontColor = gl_Color;
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = ftransform();
}