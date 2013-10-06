// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

void main(void) 
{ 
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * gl_Normal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}
