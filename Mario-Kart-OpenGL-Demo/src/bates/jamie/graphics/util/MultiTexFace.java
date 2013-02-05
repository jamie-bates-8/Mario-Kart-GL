package bates.jamie.graphics.util;

import com.jogamp.opengl.util.texture.Texture;

public class MultiTexFace
{
	private float[][] vertices;
	private float[][] normals;
	private float[][][] textureVertices;
	
	private Texture[] textures;
	
	public MultiTexFace(float[][] vertices, float[][] normals, float[][][] textureVertices, Texture[] textures)
	{
		this.vertices = vertices;
		this.normals = normals;
		this.textureVertices = textureVertices;
		
		this.textures = textures;
	}

	public float[][] getVertices() { return vertices; }

	public Texture getTexture(int t) { return textures[t]; }

	public void setTexture(int t, Texture texture) { textures[t] = texture; }
	
	public float getVx(int vertex) { return vertices[vertex][0]; }
	public float getVy(int vertex) { return vertices[vertex][1]; }
	public float getVz(int vertex) { return vertices[vertex][2]; }
	
	public float getNx(int vertex) { return normals[vertex][0]; }
	public float getNy(int vertex) { return normals[vertex][1]; }
	public float getNz(int vertex) { return normals[vertex][2]; }
	
	public float getTu(int vertex, int t) { return textureVertices[vertex][t][0]; }
	public float getTv(int vertex, int t) { return textureVertices[vertex][t][1]; }
	
}
