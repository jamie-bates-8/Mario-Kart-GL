#extension GL_ARB_gpu_shader5 : enable

uniform mat4 cameraMatrix;

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec3 vertexPosition;

varying mat4 inverseMatrix;
varying mat3 tangentMatrix;

attribute vec3 tangent;

varying vec2 gridScale;
uniform vec3 scaleVec; // determines the number of cells

// Returns the relative dimensions of the current face / plane using the maximum
// value of the face normal. For example, if the face normal is (0, 1, 0), and
// hence maximal along the y-axis, the x and z dimensions of the face are returned.
vec2 getTextureScale()
{
	vec3 normal = abs(gl_Normal);

	float maximum = normal.x < normal.y ? normal.y : normal.x;
	if(normal.z > maximum) return scaleVec.xy;
	
	if(maximum == normal.x) return scaleVec.zy;
	else return scaleVec.xz;	
}

void main(void) 
{ 
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
	
    vec3 n = normalize(gl_NormalMatrix * gl_Normal);
	vec3 t = normalize(gl_NormalMatrix * tangent);
	vec3 b = cross(n, t);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    vertexPosition = vertex.xyz;
    
    eyeDir = vertex.xyz;
	
	vec3 light, v;
	
	for(int i = 0; i < 8; i++)
	{
		light = gl_LightSource[i].position.xyz - position;
	
		v.x = dot(light, t); // tangent
		v.y = dot(light, b); // bitangent
		v.z = dot(light, n); // normal
	
		lightDir[i] = v;
	}
	
	v.x = dot(eyeDir, t);
	v.y = dot(eyeDir, b);
	v.z = dot(eyeDir, n);
	
	eyeDir = v;
	
    inverseMatrix = inverse(cameraMatrix);
    tangentMatrix = mat3(t, b, n);
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    gridScale = getTextureScale();
}
