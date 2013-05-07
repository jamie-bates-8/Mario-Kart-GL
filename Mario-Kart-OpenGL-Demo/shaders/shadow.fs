uniform sampler2DShadow shadowMap;
const float epsilon = 0.01;
varying vec4 shadowCoord;

float lookup(float x, float y)
{
	float depth = shadow2DProj(shadowMap, shadowCoord + vec4(x, y, 0, 0) * epsilon);
	return depth != 1.0 ? 0.75 : 1.0;
}

void main()
{
	float shadeFactor = lookup(0.0, 0.0);
	gl_FragColor = vec4(shadeFactor * gl_Color.rgb, gl_Color.a);
	//gl_FragColor = shadowCoord;
}