
uniform sampler2D sceneSampler;
uniform sampler2D bloomSampler;

uniform int samples;

uniform float decay;
uniform float exposure;
uniform float weight;
uniform float density;

varying vec4 lightPosition;
varying vec4 fragPosition;

void main(void)  
{  
	vec2 texCoord = gl_TexCoord[0].st;

	vec2 deltaCoord = fragPosition.st - (lightPosition.st / lightPosition.q); 	// Calculate vector from pixel to light source in screen space.
	deltaCoord *= 1.0 / float(samples) * density;  								// Divide by number of samples and scale by control factor.
	deltaCoord = clamp(deltaCoord, -0.01, 0.01);
 
	vec3 color = texture2D(sceneSampler, texCoord).rgb; // Store initial sample.
	float illuminationDecay = 1.0;  					// Set up illumination decay factor.
  
  	// Evaluate summation from Equation 3 for 'samples' iterations. 
   	for (int i = 0; i < samples; i++)  
  	{  	 
    	texCoord -= deltaCoord;	// Step sample location along ray. 
    	
    	vec3 bloom = texture2D(bloomSampler, texCoord).rgb;
    	
    	if(bloom.r > 0.1 && bloom.g > 0.1 && bloom.b > 0.1)
    	{
   			vec3 sample = texture2D(sceneSampler, texCoord).rgb; // Retrieve sample at new location. 
    		sample *= illuminationDecay * weight;  				 // Apply sample attenuation scale/decay factors. 
    		color += sample;									 // Accumulate combined color.
    	}								   
    	 
    	illuminationDecay *= decay;  // Update exponential decay factor. 
  	}  
  	// Output final color with a further scale control factor.
   	gl_FragData[0] = vec4(color * exposure, 1.0);
}  
