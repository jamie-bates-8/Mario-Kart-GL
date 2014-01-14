package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec2;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.common.nio.Buffers;

public class PlaneMesh
{
	public TimeQuery timeQuery = new TimeQuery(TimeQuery.NULL_ID);
	
	FloatBuffer vertexBuffer;
	FloatBuffer tcoordBuffer;
	
	IntBuffer indexBuffer;
	int       indexCount;
	
	private boolean bufferCreated = false;
	
	int[] bufferIDs = new int[3];
	
	private static final int VERTEX_BUFFER = 0;
	private static final int TCOORD_BUFFER = 1;
	
	private static final int INDEX_BUFFER = 2;
	
	private int height_map;
	
	private float[][] heights;
	
	int rows    = 1;
	int columns = 1;
	
	Random generator = new Random();
	
	
	public PlaneMesh(Vec3 centre, float width, float height, int columns, int rows, boolean textured)
	{
		indexCount = (2 * (rows + 1) + 1) * columns;
		indexBuffer = Buffers.newDirectIntBuffer(indexCount);
		
		this.rows    = rows;
		this.columns = columns;
		
		generateIndices();
		generateVertices(centre, width, height);
		
		if(textured) generateTexCoords();
	}

	private void generateTexCoords()
	{
		tcoordBuffer = Buffers.newDirectFloatBuffer((columns + 1) * (rows + 1) * 2);
		
		Vec2 tcoord = new Vec2();
		
		float sInc = 1.0f / columns;
		float tInc = 1.0f / rows;
		
		for(int i = 0; i <= columns; i++)
		{
			for(int j = 0; j <= rows; j++)
			{
				tcoordBuffer.put(tcoord.toArray());
				tcoord.y += tInc;
			}
			tcoord.y  = 0.0f;
			tcoord.x += sInc;
		}
		tcoordBuffer.position(0);
	}

	private void generateVertices(Vec3 centre, float width, float height)
	{
		vertexBuffer = Buffers.newDirectFloatBuffer((columns + 1) * (rows + 1) * 3);
		
		Vec3 vertex = new Vec3(centre);
		vertex.x -= width  / 2;
		vertex.z -= height / 2;
		
		Vec3 origin = new Vec3(vertex);
		
		float xInc = width  / columns;
		float zInc = height / rows;
		
		for(int i = 0; i <= columns; i++)
		{
			for(int j = 0; j <= rows; j++)
			{
				vertexBuffer.put(vertex.toArray());
				vertex.z += zInc;
			}
			vertex.z  = origin.z;
			vertex.x += xInc;
		}
		vertexBuffer.position(0);
	}

	private void generateIndices()
	{
		int index = 0;
		
		for(int i = 0; i < columns; i++) // one triangle strip (column) at a time
		{
			for(int j = 0; j <= rows; j++)
			{
				index = i * (rows + 1) + j;
				
				indexBuffer.put(index);
				indexBuffer.put(index + (rows + 1));
			}
			indexBuffer.put(indexCount);
		}
		indexBuffer.position(0);
	}
	
	public void createTexture(GL2 gl)
	{
		int[] id = new int[1];
		gl.glGenTextures(1, id, 0);
		height_map = id[0];

		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, height_map);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, columns + 1, rows + 1, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		
		heights = new float[columns + 1][rows + 1];
	}
	
	private void createBuffers(GL2 gl)
	{
		gl.glGenBuffers(bufferIDs.length, bufferIDs, 0);
	    
	    // Vertex coordinates
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * vertexBuffer.capacity(), vertexBuffer, GL2.GL_STATIC_DRAW);
	    
	    // Texture coordinates
		if(tcoordBuffer != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * tcoordBuffer.capacity(), tcoordBuffer, GL2.GL_STATIC_DRAW);
		}
	    
	    // Indices
		if(indexBuffer != null)
		{
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
			gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, Integer.SIZE / 8 * indexCount, indexBuffer, GL2.GL_STATIC_DRAW);
		}
		
		unbindArrays(gl);
		
		bufferCreated = true;
	}
	
	private void unbindArrays(GL2 gl)
	{
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER        , 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private FloatBuffer arrayToBuffer()
	{
		FloatBuffer heightBuffer = Buffers.newDirectFloatBuffer((rows + 1) * (columns + 1) * 4);
		
		for(int i = 0; i <= columns; i++)
		{
			for(int j = 0; j <= rows; j++)
			{
				float h = heights[i][j];
				heightBuffer.put(new float[] {h, h, h, h});
			}
		}
		heightBuffer.position(0);
		
		return heightBuffer;
	}
	
	private void updateHeights()
	{
		for(int k = 0; k < 10; k++)
		{
			int radius = 10 + generator.nextInt(20);
			float peak = 0.2f + generator.nextFloat() * 1.8f * (generator.nextBoolean() ? +1 : -1);
			
			int row    = generator.nextInt(rows    + 1);			
			int column = generator.nextInt(columns + 1);
			
			for(int i = 0; i < radius * 2; i++)
				for(int j = 0; j < radius * 2; j++)
				{
					float x = Math.abs(i - radius);
					float z = Math.abs(j - radius);
					
					double d = Math.sqrt(x * x + z * z);
					if(d > radius) continue;
					
					if(row    - radius + i > rows    || row    - radius + i < 0) continue;
					if(column - radius + j > columns || column - radius + j < 0) continue;
					
					heights[row - radius + i][column - radius + j] += peak * 0.5f * (Math.cos(d / radius * Math.PI) + 1);
				}
		}
	}

	private void levelHeights()
	{
		float min_max_height = 10;
		
		for(int i = 0; i <= columns; i++)
			for(int j = 0; j <= rows; j++)
			{
//				heights[i][j] -= 0.1;
				if(heights[i][j] < -min_max_height) heights[i][j] = -min_max_height;
				if(heights[i][j] > +min_max_height) heights[i][j] = +min_max_height;
			}
	}
	
	public void render(GL2 gl)
	{
		if(!bufferCreated)
		{
			createBuffers(gl);
			createTexture(gl);
		}
		
		timeQuery.getResult(gl);
		timeQuery.begin(gl);
		
		Shader shader = Shader.get("height_map");
		shader.enable(gl);
		
		updateHeights();
		levelHeights();
			
		shader.setSampler(gl, "heightMap", 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, height_map);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, columns + 1, rows + 1, 0, GL2.GL_RGBA, GL2.GL_FLOAT, arrayToBuffer());
		
		enableState(gl);
		
		bindArrays(gl);
		
		gl.glDrawElements(GL2.GL_TRIANGLE_STRIP, indexCount, GL2.GL_UNSIGNED_INT, 0);
		
		unbindArrays(gl);
		
		disableState(gl);
		
		Shader.disable(gl);
		
		timeQuery.end(gl);
	}
	
	private void bindArrays(GL2 gl)
	{
		                           gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER]); gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, 0);
		if(tcoordBuffer != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD_BUFFER]); gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0); }

		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
	}

	private void disableState(GL2 gl)
	{
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		if(tcoordBuffer != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDisable(GL2.GL_PRIMITIVE_RESTART);
	}

	private void enableState(GL2 gl)
	{
		                         gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		if(tcoordBuffer != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glEnable(GL2.GL_PRIMITIVE_RESTART);
		gl.glPrimitiveRestartIndex(indexCount);
	}
}
