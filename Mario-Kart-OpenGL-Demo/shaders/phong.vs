// ADS Point Lighting Shader (Phong)

varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

void main(void) 
{ 
	// Don't forget to transform the geometry!
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = ftransform();
	
    // Get surface normal in eye coordinates
    vNormal = gl_NormalMatrix * gl_Normal;
	vNormal = normalize(vNormal);

    // Get vertex position in eye coordinates
    vec4 position4 = gl_ModelViewMatrix * gl_Vertex;
    vec3 position3 = (vec3(position4)) / position4.w;
    
    eyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);

    // Get vector to light source
    lightDir = gl_LightSource[0].position.xyz - position3;
	
	gl_FrontColor = gl_Color;
}
