// hdrball.vs
//
// Generic vertex transformation,
// copy object-space position and 
// lighting vectors out to interpolants

varying vec3 N, L, V;

void main(void)
{ 
    // normal MVP transform
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    // map object-space position onto unit sphere
    V = gl_Vertex.xyz;

    // eye-space normal
    N = gl_NormalMatrix * gl_Normal;

    // eye-space light vector
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    L = gl_LightSource[0].position.xyz - V.xyz;
}