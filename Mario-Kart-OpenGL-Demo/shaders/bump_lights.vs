// bump.vs

uniform mat4 ModelMatrix;

varying vec4 shadowCoord;
varying vec3 lightDir[4];
varying vec3 eyeDir;

attribute vec3 tangent;

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
    
    eyeDir = vertex.xyz;
	
	vec3 light, v;
	
	for(int i = 0; i < 4; i++)
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
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
	shadowCoord = gl_TextureMatrix[2] * (ModelMatrix * gl_Vertex);
}