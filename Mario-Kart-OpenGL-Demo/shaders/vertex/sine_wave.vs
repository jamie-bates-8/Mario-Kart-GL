// ADS Point Lighting Shader (Phong)

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

uniform float timer;

void main(void) 
{ 
	vec4 vertex = gl_Vertex;
	
	float K = 6.2831 / 20.0;
	float Amp = 1.0;
	
	float u = K * (vertex.x - 1.0 * timer); // Translate the y coordinate
	vertex.y = Amp * sin( u );

	vec3 n = vec3(0.0);
	n.xy = normalize(vec2(-K * Amp * cos( u ), 1.0));

	vec4 position = gl_ModelViewMatrix * vertex;
	
	gl_ClipVertex = position;
    gl_Position   = gl_ModelViewProjectionMatrix * vertex;
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * n);
    
    eyeDir = -position.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}
