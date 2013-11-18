


uniform float timer;    // Animation time
uniform vec3  gravity;
uniform float duration; // Max particle lifetime

varying float type;

void main(void) 
{
	// Assume the initial position is (0,0,0).
	vec3 position = vec3(0.0);
	float alpha = 0.0;
	
	type = gl_MultiTexCoord0.t;
	
	float time = mod(gl_MultiTexCoord0.s, duration);
	//float time = gl_MultiTexCoord0.s;
	
	// Particle doesn't exist until the start time
	if(mod(timer, duration) > time)
	{
		float t = mod(timer, duration) - time;
		
		if(t < duration)
		{
			position = gl_Vertex.xyz * t + gravity * t * t;
			alpha = 1.0 - t / duration;
		}
	}
	
	//position = gl_Vertex.xyz * timer + gravity * timer * timer;
	position.y += 5.0;
	
	float attenuation = (gl_ModelViewMatrix * vec4(position, 1.0)).z / -200.0;
	
	gl_PointSize = mix(100.0, 200.0, 1.0 - alpha) * clamp((1.0 - attenuation), 0.0, 1.0);
	
	gl_Position = gl_ModelViewProjectionMatrix * vec4(position, 1.0);
	gl_FrontColor = vec4(1.0, 1.0, 1.0, alpha);
}
