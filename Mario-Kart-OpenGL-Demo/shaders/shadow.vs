varying vec4 shadowCoord;

void main()
{
	vec4 ecPosition = gl_ModelViewMatrix * gl_Vertex;
	vec3 ecPosition3 = (vec3(ecPosition)) / ecPosition.w;
	vec3 VP = vec3(gl_LightSource[0].position) - ecPosition3;
	
	VP = normalize(VP);
	vec3 normal = normalize(gl_NormalMatrix * gl_Normal);
	float diffuse = max(0.0, dot(normal, VP));
	diffuse = diffuse + 0.25;
	
	vec4 texCoord = gl_ModelViewMatrix * (gl_TextureMatrix[2] * gl_Vertex);
	shadowCoord = texCoord / texCoord.w;
	
	gl_FrontColor = vec4(diffuse * gl_Color.rgb, gl_Color.a);
	gl_Position = ftransform();
}