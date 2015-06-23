uniform sampler2D texture;

varying float type;

uniform vec3 color1;
uniform vec3 color2;

uniform bool spark;
uniform bool smoke;

void main(void)
{ 
	vec4 textureColor = texture2D(texture, gl_TexCoord[0].st);
	textureColor = vec4(mix(color2, color1, textureColor.a), gl_Color.a * textureColor.a);
	textureColor.rgb = mix(textureColor.rgb, gl_Color.rgb, 0.75);
	
	//textureColor.rgb = clamp(pow(textureColor.rgb * 1.1, vec3(textureColor.r * 1.1)), 0.0, 1.0);
	
	gl_FragData[0] = textureColor;
	
	vec3 brightColor = max(textureColor.rgb - vec3(0.90), vec3(0.0));
    float bright = dot(brightColor, vec3(1.0));
    bright = (1.0 - texture2D(texture, gl_TexCoord[0].st).a);
    bright = length(gl_TexCoord[0].st * 2.0 - 1.0) * 0.5 * bright;
    bright = smoothstep(0.0, 0.5, bright);
    
    //gl_FragData[0] = vec4(vec3(bright), textureColor.a);
    
    if(spark) gl_FragData[1] = textureColor;
    else if(smoke) gl_FragData[1] = vec4(vec3(0.0), textureColor.a);
    else gl_FragData[1] = vec4(mix(vec3(0.0), textureColor.rgb, bright), textureColor.a);
}
