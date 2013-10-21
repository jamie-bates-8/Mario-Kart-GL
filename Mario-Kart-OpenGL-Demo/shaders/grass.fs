uniform sampler2D texture;

void main(void)
{
	vec4 textureColor = texture2D(texture, gl_TexCoord[0].st);
	if(textureColor.a < 0.25) discard;
	
    gl_FragData[0] = textureColor;
    gl_FragData[1] = vec4(0.0);
}