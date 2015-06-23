attribute vec3 tangent;

varying vec3 worldPos, eyePos;
varying vec3 lightDir;
varying vec3 vNormal, vTangent, vBinormal;
varying float timer;

uniform mat4 model_matrix;
uniform float clock;

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

void main()
{	
	worldPos = vec3(model_matrix * gl_Vertex);  
	eyePos = gl_ModelViewMatrixInverse[3].xyz;
	lightDir = vec3(gl_ModelViewMatrixInverse * gl_LightSource[0].position);
	
	vTangent  = getRotation(model_matrix) * tangent;
	vBinormal = getRotation(model_matrix) * cross(gl_Normal, tangent);
	vNormal   = getRotation(model_matrix) * gl_Normal; 
	
    gl_TexCoord[0] = gl_MultiTexCoord0;
	
	gl_Position = ftransform();
    gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
}