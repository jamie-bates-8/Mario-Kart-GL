uniform sampler2D alphaMask;

void main(void)
{
	vec4 maskColor = texture2D(alphaMask, gl_TexCoord[1].st);

	if(maskColor.a < 0.25) discard;

	gl_FragColor = vec4(1.0);
}