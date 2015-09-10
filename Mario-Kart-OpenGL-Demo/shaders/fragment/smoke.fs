uniform sampler2D texture;
uniform sampler2D cloudSampler;

varying float type;

void main(void)
{ 
	vec4 cloudSample = texture2D(cloudSampler, gl_PointCoord + gl_Color.a);

    //if(cloudSample.r < (1.0 - gl_Color.a)) discard;

	gl_FragColor = texture2D(type != 0.0 ? texture : cloudSampler, gl_PointCoord) * 1.0;
	gl_FragColor.a *= gl_Color.a;
}
