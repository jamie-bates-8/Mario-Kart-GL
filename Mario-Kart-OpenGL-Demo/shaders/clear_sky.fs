
varying vec3 worldPos;

varying vec3 eyeDir;
varying vec3 vertexNormal;

uniform vec3 horizon;
uniform vec3 skyColor;

void main(void)
{ 
    float h = worldPos.y / 500.0;
    h = clamp(h, -1.0, 1.0);
	
	vec3 background = mix(horizon, skyColor, abs(h));
	
	gl_FragData[0] = vec4(background, 1.0);
	gl_FragData[1] = vec4(vertexNormal, eyeDir.z);
}