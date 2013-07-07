
varying vec3 worldPos;

void main() 
{
    worldPos = vec3(gl_Vertex);
    gl_Position = ftransform();
}