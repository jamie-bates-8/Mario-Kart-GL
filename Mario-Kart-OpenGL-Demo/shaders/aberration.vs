const float EtaR = 0.65;
const float EtaG = 0.67; // Ratio of indices of refraction
const float EtaB = 0.69;
const float FresnelPower = 5.0;
const float F = ((1.0 - EtaG) * (1.0 - EtaG)) / ((1.0 + EtaG) * (1.0 + EtaG));

varying vec3 Reflect;

varying vec3 RefractR;
varying vec3 RefractG;
varying vec3 RefractB;

varying float Ratio;

void main()
{
	vec4 ecPosition = gl_ModelViewMatrix * gl_Vertex;
	vec3 ecPosition3 = ecPosition.xyz / ecPosition.w;
	
	vec3 i = normalize(ecPosition3);
	vec3 n = normalize(gl_NormalMatrix * gl_Normal);
	
	Ratio = F + (1.0 - F) * pow((1.0 - dot(-i, n)), FresnelPower);
	
	RefractR = refract(i, n, EtaR);
	RefractG = refract(i, n, EtaG);
	RefractB = refract(i, n, EtaB);

	Reflect = reflect(i, n);

	gl_Position = ftransform();
}