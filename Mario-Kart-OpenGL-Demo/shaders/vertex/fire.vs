void main(void) 
{
	vec4 eyeVec = gl_ModelViewMatrix * gl_Vertex;
	
  	float dist = distance(eyeVec, vec4(0.0, 0.0, 0.0, 1.0));
  	float att = sqrt(1.0 / (gl_Point.distanceConstantAttenuation  +
                           (gl_Point.distanceLinearAttenuation    +
                            gl_Point.distanceQuadraticAttenuation * dist) * dist));
                            
 	float size = clamp(gl_Point.size * att, gl_Point.sizeMin, gl_Point.sizeMax);
 	
 	gl_PointSize = max(size, gl_Point.fadeThresholdSize);

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	gl_FrontColor = gl_Color;
}
