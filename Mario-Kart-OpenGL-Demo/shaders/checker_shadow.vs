#extension GL_ARB_gpu_shader5 : enable

uniform mat4 ModelMatrix;

varying vec4 shadowCoord;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

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
	
    // Get surface normal in eye coordinates
    vertexNormal = gl_NormalMatrix * gl_Normal;
	vertexNormal = normalize(vertexNormal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = -vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[6] * (ModelMatrix * gl_Vertex);
    
    gridScale = getTextureScale();
}
