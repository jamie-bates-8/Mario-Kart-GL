package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_TEXTURE_2D;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Model
{
	private List<float[]> _vertices;
	
	private FloatBuffer vertices;
	private FloatBuffer normals;
	private FloatBuffer texCoords;
	private FloatBuffer tangents;
	
	int indices;
	int polygon;
	
	Texture texture;
	
	public static boolean enableVBO = true;
	public boolean bufferCreated = false;
	
	private int[] bufferIDs = new int[5];
	
	private static final int VERTEX_BUFFER = 0;
	private static final int NORMAL_BUFFER = 1;
	private static final int TCOORD_BUFFER = 2;
	
	private static final int TANGENT_BUFFER = 3;
	
	
	public Model(float[] vertices, float[] normals, float[] texCoords, float[] tangents, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		indices = vertices.length / 3;
		
		this.vertices  = Buffers.newDirectFloatBuffer(vertices);
		this.normals   = Buffers.newDirectFloatBuffer(normals); 
		this.texCoords = Buffers.newDirectFloatBuffer(texCoords); 
		this.tangents  = Buffers.newDirectFloatBuffer(tangents);
	}
	
	public List<float[]> getVertices() { return _vertices; }
	
	public void render(GL2 gl)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(texCoords == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		if(texture != null) texture.bind(gl);
		
		enableState(gl);
		
		if(enableVBO)
		{
			bindArrays(gl);
			gl.glDrawArrays(polygon, 0, indices);
			unbindArrays(gl);
		}
		else
		{
			setupPointers(gl);
			gl.glDrawArrays(polygon, 0, indices);
		}
		
		disableState(gl);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}

	private void unbindArrays(GL2 gl)
	{
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}

	private void setupPointers(GL2 gl)
	{
						      gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, vertices );
		if(normals   != null) gl.glNormalPointer  (   GL2.GL_FLOAT, 0, normals  );
		if(texCoords != null) gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, texCoords);
		if(tangents  != null) gl.glVertexAttribPointer(1, 3, GL2.GL_FLOAT, true, 0, tangents);
		
	}

	private void bindArrays(GL2 gl)
	{
		                      gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER ]); gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, 0);
		if(normals   != null) gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER ]); gl.glNormalPointer  (   GL2.GL_FLOAT, 0, 0);
		if(texCoords != null) gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD_BUFFER ]); gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
		if(tangents  != null) gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TANGENT_BUFFER]); gl.glVertexAttribPointer(1, 3, GL2.GL_FLOAT, true, 0, 0);
	}

	private void disableState(GL2 gl)
	{
							  gl.glDisableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY       );
		if(texCoords != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(tangents  != null) gl.glDisableVertexAttribArray(1);
	}

	private void enableState(GL2 gl)
	{
		                      gl.glEnableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY       );
		if(texCoords != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(tangents  != null) gl.glEnableVertexAttribArray(1);
	}
	
	public void renderInstanced(GL2 gl, int count, int dataID)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(texCoords == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL2.GL_TEXTURE_1D, dataID);
		gl.glActiveTexture(GL2.GL_TEXTURE0); if(texture != null) texture.bind(gl);
		
		enableState(gl);
		
		if(enableVBO)
		{
			bindArrays(gl);
			gl.glDrawArraysInstanced(polygon, 0, indices, count);
			unbindArrays(gl);
		}
		else
		{
			setupPointers(gl);
			gl.glDrawArraysInstanced(polygon, 0, indices, count);
		}
		
		disableState(gl);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void createBuffers(GL2 gl)
	{
		gl.glGenBuffers(5, bufferIDs, 0);
	    
	    // Vertex data
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * vertices.capacity(), vertices, GL2.GL_STATIC_DRAW);
	    
	    // Normal data
		if(normals != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * normals.capacity(), normals, GL2.GL_STATIC_DRAW);
		}
	    
	    // Texture coordinates
		if(texCoords != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * texCoords.capacity(), texCoords, GL2.GL_STATIC_DRAW);
		}
	    
		// Texture coordinates
		if(tangents != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TANGENT_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * tangents.capacity(), tangents, GL2.GL_STATIC_DRAW);
		}
		
		unbindArrays(gl);
		
		bufferCreated = true;
	}
}
