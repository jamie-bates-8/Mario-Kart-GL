// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir;
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
    lightDir = gl_LightSource[0].position.xyz - position;
	
	gl_FrontColor = gl_Color;
}
