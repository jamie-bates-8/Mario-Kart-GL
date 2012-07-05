package graphics.util;


import com.jogamp.opengl.util.texture.Texture;


public class Face
{
	private float[][] vertices;
	private float[][] normals;
	private float[][] textureVertices;
	
	private Texture texture;
	private boolean hasTexture;
	private int wildcard;
	
	public Face(float[][] vertices, float[][] normals, float[][] textureVertices, Texture texture, boolean hasTexture, int wildcard)
	{
		this.vertices = vertices;
		this.normals = normals;
		this.textureVertices = textureVertices;
		
		this.texture = texture;
		this.hasTexture = hasTexture;
		this.wildcard = wildcard;
	}
	
	public boolean hasTexture() { return hasTexture; }
	
	public boolean hasWildcard() { return wildcard != -1; }
	
	public int getWildcard() { return wildcard; }

	public float[][] getVertices() { return vertices; }

	public Texture getTexture() { return texture; }

	public void setTexture(Texture texture) { this.texture = texture; }
	
	public float getVx(int vertex) { return vertices[vertex][0]; }
	public float getVy(int vertex) { return vertices[vertex][1]; }
	public float getVz(int vertex) { return vertices[vertex][2]; }
	
	public float getNx(int vertex) { return normals[vertex][0]; }
	public float getNy(int vertex) { return normals[vertex][1]; }
	public float getNz(int vertex) { return normals[vertex][2]; }
	
	public float getTu(int vertex) { return textureVertices[vertex][0]; }
	public float getTv(int vertex) { return textureVertices[vertex][1]; }
	
}
