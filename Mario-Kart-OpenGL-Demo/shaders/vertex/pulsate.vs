#version 120

varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

uniform float timer;

void main(void) 
{ 
	
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * gl_Normal);
    
    float dist = distance(vec3(0.0), gl_Vertex.xyz);
    
    vec4 vertex = gl_ModelViewMatrix * vec4(gl_Vertex.xyz + gl_Normal * abs(sin(timer + dist * 10.0)) * 0.1, 1.0);
    //vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
	
	//vec4 ripple = gl_ProjectionMatrix * vertex;
	//ripple.y += sin(timer + dist * 10.0) * 0.2;
    
    //gl_Position = ripple;
    gl_Position = gl_ProjectionMatrix * vertex;
    

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = -vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
}
