uniform float     dissolveFactor;
uniform sampler2D cloudSampler;

void main(void)
{ 
    vec4 cloudSample = texture2D(cloudSampler, gl_TexCoord[0].st);

    if(cloudSample.r < dissolveFactor) discard;
    
    vec3 final = vec3(0.118, 0.565, 1.000);
    final = mix(vec3(0.5, 0.5, 0.8), final, cloudSample.g);
    
	gl_FragColor = vec4(final, 1.0);
}