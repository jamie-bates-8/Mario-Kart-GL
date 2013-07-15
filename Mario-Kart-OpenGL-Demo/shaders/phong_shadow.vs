// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

uniform mat4 ModelMatrix;

varying vec4 shadowCoord;
varying vec3 vNormal;
varying vec3 lightDir;

void main(void) 
{ 
    gl_Position = ftransform();
	
    // Get surface normal in eye coordinates
    vNormal = gl_NormalMatrix * gl_Normal;
	vNormal = normalize(vNormal);

    // Get vertex position in eye coordinates
    vec4 position4 = gl_ModelViewMatrix * gl_Vertex;
    vec3 position3 = vec3(position4) / position4.w;

    // Get vector to light source
    lightDir = normalize(gl_LightSource[0].position - position3);
	
	gl_FrontColor = gl_Color;
	
	shadowCoord = gl_TextureMatrix[2] * (ModelMatrix * gl_Vertex);

    gl_TexCoord[0] = gl_MultiTexCoord0;
}