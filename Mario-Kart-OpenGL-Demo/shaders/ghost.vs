const float Eta = 0.66;
const float FresnelPower = 5.0;
const float F = ((1.0 - Eta) * (1.0 - Eta)) / ((1.0 + Eta) * (1.0 + Eta));

varying vec3 Reflect;
varying vec3 Refract;

varying float Ratio;

void main()
{
	vec4 ecPosition = gl_ModelViewMatrix * gl_Vertex;
	vec3 ecPosition3 = ecPosition.xyz / ecPosition.w;
	
	vec3 i = normalize(ecPosition3);
	vec3 n = normalize(gl_NormalMatrix * gl_Normal);
	
	Ratio = F + (1.0 - F) * pow((1.0 - dot(-i, n)), FresnelPower);
	
	Refract = refract(i, n, Eta);
	Reflect = reflect(i, n);

	gl_Position = ftransform();
}