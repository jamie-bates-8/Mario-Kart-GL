
uniform sampler2D sceneSampler;

uniform int samples;

uniform float decay;
uniform float exposure;
uniform float weight;
uniform float density;

varying vec2 lightPosition;

void main(void)  
{  
	vec2 texCoord = gl_TexCoord[0].st;

	vec2 deltaTexCoord = (texCoord * 2.0 - 1.0);	// Calculate vector from pixel to light source in screen space.  
	deltaTexCoord *= 1.0 / samples * density;  	// Divide by number of samples and scale by control factor.  
 
	vec3 color = texture2D(sceneSampler, texCoord).rgb; // Store initial sample.
	float illuminationDecay = 1.0;  					// Set up illumination decay factor.
  
  	// Evaluate summation from Equation 3 NUM_SAMPLES iterations. 
   	for (int i = 0; i < samples; i++)  
  	{  	 
    	texCoord -= deltaTexCoord;	// Step sample location along ray. 
    	 
   		vec3 sample = texture2D(sceneSampler, texCoord).rgb; // Retrieve sample at new location. 
    	sample *= illuminationDecay * weight;  				 // Apply sample attenuation scale/decay factors. 
    	color += sample;  									 // Accumulate combined color.  
    	 
    	illuminationDecay *= decay;  // Update exponential decay factor. 
  	}  
  	// Output final color with a further scale control factor.  
   	gl_FragData[0] = vec4(color * exposure, 1.0);  
}  
