varying vec3 vertexNormal;
varying vec3 eyeDir;

uniform vec3  rim_color;
uniform float rim_power;

uniform sampler2D sceneSampler;

uniform float screenHeight;
uniform float screenWidth;

vec3 rimLight()
{
    float d = dot(normalize(vertexNormal), normalize(-eyeDir));
	vec3 normal = (d < 0.0) ? -vertexNormal : vertexNormal;
    float f = 1.0 - dot(normalize(normal), normalize(-eyeDir));

    f = smoothstep(0.0, 1.0, f);
    f = pow(f, rim_power);
    
    float x = gl_FragCoord.x / screenWidth;
    float y = gl_FragCoord.y / screenHeight;
    
    vec3 sample = texture2D(sceneSampler, vec2(x, y)).rgb;

    return (f * rim_color) + sample;
}

void main(void)
{     						          
  	gl_FragData[0] = vec4(rimLight().rgb, 1.0);
    gl_FragData[1] = vec4(0.0);
    gl_FragData[2] = vec4(normalize(vertexNormal), eyeDir.z);
}
