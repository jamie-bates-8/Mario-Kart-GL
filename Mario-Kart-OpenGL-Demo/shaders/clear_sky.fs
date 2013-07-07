
varying vec3 worldPos;

void main(void)
{ 
    float h = worldPos.y / 10.0;
    h = clamp(h, 0.0, 1.0);
	
	vec3 horizon = vec3(1.0);
	vec3 skyColor = vec3(0.00, 0.75, 1.00);
	
	gl_FragColor = vec4(mix(horizon, skyColor, h), 1.0);
}
