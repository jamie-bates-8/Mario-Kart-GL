uniform sampler2D sampler0;

void main(void)
{
	vec3 normal = vec3(0.0, 0.0, 1.0 / 8.0);
	
	float xInc = 1.0 / 640.0;
	float zInc = 1.0 / 860.0;
	
	float left  = texture2D(sampler0, gl_TexCoord[0].st + vec2(-xInc, 0.0)).r;
	float right = texture2D(sampler0, gl_TexCoord[0].st + vec2(+xInc, 0.0)).r;
	float up    = texture2D(sampler0, gl_TexCoord[0].st + vec2(0.0, +zInc)).r;
	float down  = texture2D(sampler0, gl_TexCoord[0].st + vec2(0.0, -zInc)).r;
	
	float xDiff = left - right;
	float zDiff = up - down;
	
	normal.x = xDiff / 2.0;
	normal.y = zDiff / 2.0;
	
	normal = normalize(normal);
	
	normal = (normal * 0.5) + 0.5; 

    gl_FragColor = vec4(normal, 1.0);
    gl_FragColor = texture2D(sampler0, gl_TexCoord[0].st);
}
