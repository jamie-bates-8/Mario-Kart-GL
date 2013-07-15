
varying vec3 worldPos;

void main(void)
{ 
    float h = worldPos.y / 500.0;
    h = clamp(h, -1.0, 1.0);
	
	vec3 horizon  = vec3(0.88, 1.00, 1.00);
	vec3 skyColor = vec3(0.18, 0.56, 1.00);
	
	vec3 background = mix(horizon, skyColor, abs(h));
	
	gl_FragColor = vec4(background, 1.0);
}