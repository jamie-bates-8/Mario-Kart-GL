
uniform mat4 model_matrix;
uniform vec3 camera_position;

varying vec4 shadowCoord;
varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec3 cameraVec;

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
    cameraVec = camera_position - gl_Vertex.xyz;
	
	vec3 light, v;
	
	for(int i = 0; i < 8; i++)
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
	
	v.x = dot(cameraVec, t);
	v.y = dot(cameraVec, b);
	v.z = dot(cameraVec, n);
	
	cameraVec = v;
	
    gl_FrontColor = gl_Color;
    
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
	shadowCoord = gl_TextureMatrix[6] * (model_matrix * gl_Vertex);
}