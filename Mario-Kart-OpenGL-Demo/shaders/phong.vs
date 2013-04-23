// ADS Point Lighting Shader (Phong)

// Color to fragment program
varying vec3 vNormal;
varying vec3 lightDir;

void main(void) 
{ 
	// Don't forget to transform the geometry!
    gl_Position = ftransform();
	
    // Get surface normal in eye coordinates
    vNormal = gl_NormalMatrix * gl_Normal;
	vNormal = normalize(vNormal);

    // Get vertex position in eye coordinates
    vec4 position4 = gl_ModelViewMatrix * gl_Vertex;
    vec3 position3 = (vec3(position4)) / position4.w;

    // Get vector to light source
    lightDir = normalize(gl_LightSource[0].position - position3);
	
	gl_FrontColor = gl_Color;
}
