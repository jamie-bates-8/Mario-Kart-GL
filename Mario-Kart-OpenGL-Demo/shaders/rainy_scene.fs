
uniform sampler2D rainSampler;
uniform sampler2D sceneSampler;

void main(void)
{
	vec4 newColor = texture2D( rainSampler, gl_TexCoord[0].st);
	vec4 oldColor = texture2D(sceneSampler, gl_TexCoord[0].st);
	
	vec4 fragColor = oldColor * 0.99 + newColor;
    
    gl_FragColor = vec4(fragColor.rgb, 1.0);
}
