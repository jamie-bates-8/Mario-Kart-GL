// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

uniform mat4 ModelMatrix;

varying vec4 shadowCoord;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

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
    
    eyeDir = vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
	
	gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[2] * (ModelMatrix * gl_Vertex);
}