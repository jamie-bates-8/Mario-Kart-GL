package bates.jamie.graphics.scene;

import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Model
{
	FloatBuffer vertices;
	FloatBuffer normals;
	FloatBuffer texCoords;
	
	int polygon;
	
	Texture texture;
	
	IntBuffer indices;
	
	int indexCount;
	
	public Model(List<float[]> vertices, List<float[]> normals, int[] vIndices, int[] nIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		float[] _normals = reorderNormals(vertices.size(), normals, vIndices, nIndices);
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		this.normals = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		this.normals.put(_normals);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	public Model(List<float[]> vertices, int[] vIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	public Model(List<float[]> vertices, List<float[]> texCoords, int[] vIndices, int[] tIndices, Texture texture, int type)
	{	
		this.texture = texture;
		
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		this.texCoords = Buffers.newDirectFloatBuffer(vertices.size() * 2);
		for(float[] texCoord : texCoords) this.texCoords.put(texCoord);
		this.texCoords.position(0);
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	private float[] reorderNormals(int vertices, List<float[]> normals, int[] vIndices, int[] nIndices)
	{
		//each vertex has a normal that requires 3 components
		float[] _normals = new float[vertices * 3];
		
		//for each vertex
		for(int i = 0; i < nIndices.length; i++)
		{
			float[] normal = normals.get(nIndices[i]);
			
			int offset = vIndices[i] * 3;
			
			_normals[offset    ] = normal[0];
			_normals[offset + 1] = normal[1];
			_normals[offset + 2] = normal[2];
		}
		
		return _normals;
	}
	
	public float[] reorderTexCoords(int vertices, List<float[]> texCoords, int[] vIndices, int[] tIndices)
	{
		//each vertex has a texture coordinate that requires 2 components
		float[] _texCoords = new float[vertices * 2];
		
		for(int i = 0; i < vertices; i++)
		{
			float[] texCoord = texCoords.get(tIndices[i]);
			
			int offset = i * 2;
			
			_texCoords[offset    ] = texCoord[0];
			_texCoords[offset + 1] = texCoord[1];
		}
		
		return _texCoords;
	}
	
	public void render(GL2 gl)
	{
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		if(normals != null) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		if(texCoords != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertices);
		
		if(normals != null) gl.glNormalPointer(GL2.GL_FLOAT, 0, normals);
		if(texCoords != null)
		{
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, texCoords);
			texture.bind(gl);
		}
		
		gl.glDrawElements(polygon, indexCount, GL2.GL_UNSIGNED_INT, indices);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(normals != null) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		if(texCoords != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
}
