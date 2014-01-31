
void main(void)
{ 
	float intensity = length(gl_TexCoord[0].st * 2.0 - 1.0);
	
	gl_FragColor = vec4(1.0 - intensity) * 0.33;
}
