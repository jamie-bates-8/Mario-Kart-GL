#extension GL_EXT_gpu_shader4 : enable

uniform sampler1D positionMap;
uniform sampler2D texture;

uniform int   length;
uniform float timer;

vec2 windDir = vec2(0.2, 0.1);

mat4 get_Y_rotation(float angle)
{
    float st = sin(angle);
    float ct = cos(angle);

    return mat4(vec4( ct, 0.0,  st, 0.0),
                vec4(0.0, 1.0, 0.0, 0.0),
                vec4(-st, 0.0,  ct, 0.0),
                vec4(0.0, 0.0, 0.0, 1.0));
}

void main(void)
{
	vec2 texCoord = vec2(0.0);

	if(gl_Vertex.y > 0.0) texCoord.t = 1.0;
    if(gl_Vertex.x > 0.0) texCoord.s = 1.0;
    if(gl_Vertex.z > 0.0) texCoord.s = 1.0;
    
    gl_TexCoord[0] = vec4(texCoord, 0.0, 1.0);

    
    vec4 position  = texture1D(positionMap, float(gl_InstanceID) / float(length));
    mat4 yRotation = get_Y_rotation(position.a);
    
    vec4 windOffset = vec4(sin(timer + float(gl_InstanceID) / float(length)) * windDir, 0.0, 0.0);
    if(gl_Vertex.y <= 0.0) windOffset = vec4(0.0);
    
 	gl_FrontColor = vec4(1.0);
    gl_Position   = gl_ModelViewProjectionMatrix * (vec4(position.xyz, 0.0) + (yRotation * (gl_Vertex + windOffset)));
}
