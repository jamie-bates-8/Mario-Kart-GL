package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Model
{
	private List<float[]> _vertices;
	
	private FloatBuffer vertices;
	private FloatBuffer normals;
	private FloatBuffer texCoords;
	private FloatBuffer colors;
	private FloatBuffer tangents;
	private FloatBuffer positions;
	
	IntBuffer indices;
	
	int polygon;
	int indexCount;
	
	
	public Texture colourMap;
	public Texture normalMap;
	public Texture heightMap;
	
	
	public static boolean enableVBO = true;
	public boolean bufferCreated = false;
	
	private int[] bufferIDs = new int[7];
	
	private static final int  VERTEX_BUFFER = 0;
	private static final int  NORMAL_BUFFER = 1;
	private static final int  TCOORD_BUFFER = 2;
	private static final int  COLOUR_BUFFER = 3;
	private static final int  OFFSET_BUFFER = 4;
	private static final int TANGENT_BUFFER = 5;
	private static final int   INDEX_BUFFER = 6;
	
	private static final int  TANGENT_ID = 1;
	private static final int POSITION_ID = 4; 
	
	public Model(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] positions, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		indexCount = vertices.length / 3; // three values per coordinate (x, y, z)
		
		this.vertices  = Buffers.newDirectFloatBuffer(vertices);
		this.normals   = Buffers.newDirectFloatBuffer(normals); 
		
		if(texCoords != null) this.texCoords = Buffers.newDirectFloatBuffer(texCoords); 
		if(tangents  != null) this.tangents  = Buffers.newDirectFloatBuffer(tangents);
		if(positions != null) this.positions = Buffers.newDirectFloatBuffer(positions);
		
	}
	
	public Model(float[] vertices, float[] normals, float[] texCoords, float[] colors, float[] tangents, float[] positions, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		indexCount = vertices.length / 3; // three values per coordinate (x, y, z)
		
		this.vertices  = Buffers.newDirectFloatBuffer(vertices);
		this.normals   = Buffers.newDirectFloatBuffer(normals); 
		
		if(colors    != null) this.colors    = Buffers.newDirectFloatBuffer(colors);
		if(texCoords != null) this.texCoords = Buffers.newDirectFloatBuffer(texCoords); 
		if(tangents  != null) this.tangents  = Buffers.newDirectFloatBuffer(tangents);
		if(positions != null) this.positions = Buffers.newDirectFloatBuffer(positions);
		
	}
	
	public Model(String fileName)
	{
		long start = System.currentTimeMillis();
		
		polygon = 3;
		load(fileName);
		
		System.out.println("Model Loader: " + fileName + ", " + (System.currentTimeMillis() - start));
	}
	
	public Model(List<float[]> vertices, List<float[]> normals, int[] vIndices, int[] nIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		List<float[]> vertexData = new ArrayList<float[]>();
		List<float[]> normalData = new ArrayList<float[]>();
		List<Integer> offsetData = new ArrayList<Integer>();
		
		int index = 0;
		
		for(int i = 0; i < vIndices.length; i++)
		{
			float[] vertex = vertices.get(vIndices[i]);
			float[] normal = normals.get(nIndices[i]);
			
			boolean unique = true;
			int k = index;
			
			for(int j = 0; j < vertexData.size(); j++)
			{
				if( Vector.equal(vertex, vertexData.get(j)) &&
					Vector.equal(normal, normalData.get(j))    )
				{
					k = j;
					unique = false;
					break;
				}
			}
			
			if(unique)
			{
				offsetData.add(k);
				vertexData.add(vertex);
				normalData.add(normal);
				index++;
			}
			else offsetData.add(k);
		}
		
		_vertices = vertexData;
		
		indexCount = vIndices.length;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertexData.size() * 3);
		for(float[] vertex : vertexData) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
		for(float[] normal : normalData) this.normals.put(normal);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(offsetData.size());
		for(int i : offsetData) indices.put(i);
		indices.position(0);
		
		System.out.printf("Indexed Model:\n{\n\tIndices:  %d\n\tVertices: %d\n\tNormals:  %d\n}\n", indexCount, vertices.size(), normals.size());
	}
	
	public Model(List<float[]> vertices, List<float[]> texCoords, List<float[]> normals, int[] vIndices, int[] tIndices, int[] nIndices, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		List<float[]> vertexData = new ArrayList<float[]>();
		List<float[]> tcoordData = new ArrayList<float[]>();
		List<float[]> normalData = new ArrayList<float[]>();
		List<Integer> offsetData = new ArrayList<Integer>();
		
		int index = 0;
		
		for(int i = 0; i < vIndices.length; i++)
		{
			float[] vertex = vertices .get(vIndices[i]);
			float[] normal = normals  .get(nIndices[i]);
			float[] tcoord = texCoords.get(tIndices[i]);
			
			boolean unique = true;
			int k = index;
			
			for(int j = 0; j < vertexData.size(); j++)
			{
				if( Vector.equal(vertex, vertexData.get(j)) &&
					Vector.equal(normal, normalData.get(j)) &&
					Vector.equal(tcoord, tcoordData.get(j)))
				{
					k = j;
					unique = false;
					break;
				}
			}
			
			if(unique)
			{
				offsetData.add(k);
				vertexData.add(vertex);
				normalData.add(normal);
				tcoordData.add(tcoord);
				index++;
			}
			else offsetData.add(k);
		}
		
		_vertices = vertexData;
		
		indexCount = vIndices.length;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertexData.size() * 3);
		for(float[] vertex : vertexData) this.vertices.put(vertex);
		this.vertices.position(0); 
		
		this.texCoords = Buffers.newDirectFloatBuffer(tcoordData.size() * 2);
		for(float[] tcoord : tcoordData) this.texCoords.put(tcoord);
		this.texCoords.position(0);
		
		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
		for(float[] normal : normalData) this.normals.put(normal);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(offsetData.size());
		for(int i : offsetData) indices.put(i);
		indices.position(0);
		
		System.out.printf("Indexed Model:\n{\n\tIndices:  %d\n\tVertices: %d\n\tTexture Coordinates:  %d\n\tNormals:  %d\n}\n",
				          indexCount, vertices.size(), texCoords.size(), normals.size());
	}
	
	public void calculateTangents()
	{
		float[][] _tangents = new float[indices.capacity()][3];
		
		for(int i = 0; i < indices.capacity(); i += 3)
		{
			int i1 = indices.get();
			int i2 = indices.get();
			int i3 = indices.get();
			
			float[] v1 = new float[3]; vertices.position(i1 * 3); vertices.get(v1);
			float[] v2 = new float[3]; vertices.position(i2 * 3); vertices.get(v2);
			float[] v3 = new float[3]; vertices.position(i3 * 3); vertices.get(v3);
			
			float[] t1 = new float[2]; texCoords.position(i1 * 2); texCoords.get(t1);
			float[] t2 = new float[2]; texCoords.position(i2 * 2); texCoords.get(t2);
			float[] t3 = new float[2]; texCoords.position(i3 * 2); texCoords.get(t3);
			
			_tangents[i1] = Vector.add(_tangents[i1], Vector.tangent(v1, v2, v3, t1, t2, t3));
			_tangents[i2] = Vector.add(_tangents[i2], Vector.tangent(v2, v3, v1, t2, t3, t1));
			_tangents[i3] = Vector.add(_tangents[i3], Vector.tangent(v3, v1, v2, t3, t1, t2));
			
		}
		vertices .position(0);
		texCoords.position(0);
		indices  .position(0);
		
		tangents = Buffers.newDirectFloatBuffer(indices.capacity() * 3);
		for(float[] tangent : _tangents) tangents.put(Vector.normalize(tangent));
		tangents.position(0); 
	}
	
	public boolean save(String fileName)
	{
        try
        {
            FileOutputStream   fos = new FileOutputStream(new File("obj/" + fileName + ".model"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            List<Float>   vertexData = new ArrayList<Float>();
            List<Float>   normalData = new ArrayList<Float>();
            List<Integer> offsetData = new ArrayList<Integer>();
            
            for(int i = 0; i < vertices.capacity(); i++) vertexData.add(vertices.get(i));
            for(int i = 0; i <  normals.capacity(); i++) normalData.add(normals.get(i));
            for(int i = 0; i <  indices.capacity(); i++) offsetData.add(indices.get(i));
            
            oos.writeObject(vertexData);
            oos.writeObject(normalData);
            oos.writeObject(offsetData);
            oos.writeObject(indexCount);
            
            oos.close();
            fos.close(); 
        }
        catch (IOException e) { e.printStackTrace(); }
        
        return true;
    }
	
	@SuppressWarnings("unchecked")
	public boolean load(String fileName)
	{
        try
        {
            FileInputStream   fis = new FileInputStream(new File("obj/" + fileName + ".model"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            List<Float>   vertexData = (ArrayList<Float>)   ois.readObject();
            List<Float>   normalData = (ArrayList<Float>)   ois.readObject();
            List<Integer> offsetData = (ArrayList<Integer>) ois.readObject();
            indexCount = (Integer) ois.readObject();
            
            this.vertices = Buffers.newDirectFloatBuffer(vertexData.size() * 3);
    		for(float v : vertexData) this.vertices.put(v);
    		this.vertices.position(0);  
    		
    		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
    		for(float n : normalData) this.normals.put(n);
    		this.normals.position(0);
    		
    		indices = Buffers.newDirectIntBuffer(offsetData.size());
    		for(int i : offsetData) indices.put(i);
    		indices.position(0);

            ois.close();
            fis.close();
        }
        catch (Exception e) { e.printStackTrace(); }
        return true;
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
		this.colourMap = texture;
		
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) this.vertices.put(vertex);
		this.vertices.position(0);  
		
		indexCount = vIndices.length;
		
		this.texCoords = Buffers.newDirectFloatBuffer(vertices.size() * 2);
		for(float[] texCoord : texCoords) this.texCoords.put(texCoord);
		this.texCoords.position(0);
		
		System.out.printf("Indexed Model:\n{\n\tIndices:  %d\n\tVertices: %d\n\tTexture Coordinates: %d\n}\n", indexCount, vertices.size(), texCoords.size());
		
		indices = Buffers.newDirectIntBuffer(vIndices);
	}
	
	public void export(String fileName)
	{
		try
		{	
			FileWriter writer = new FileWriter("iva/" + fileName + ".iva");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("vertices ");
			float[] vertexArray = vertices.array();
			for(int v = 0; v < vertexArray.length; v++) out.write(vertexArray[v] + " ");
			out.write("\r\n");
			
			out.write("normals ");
			float[] normalArray = vertices.array();
			for(int n = 0; n < normalArray.length; n++) out.write(normalArray[n] + " ");
			out.write("\r\n");
			
			out.write("indices ");
			float[] indexArray = vertices.array();
			for(int i = 0; i < indexArray.length; i++) out.write(indexArray[i] + " ");
			out.write("\r\n");
	
			out.close();
		}
		
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public List<float[]> getVertices() { return _vertices; }
	
	public void setColorArray(List<float[]> colors)
	{
		this.colors = Buffers.newDirectFloatBuffer(colors.size() * 3);
		for(float[] color : colors) this.colors.put(color);
		this.colors.position(0);
	}
	
	public void setInstanceData(float[]     positions) { this.positions = Buffers.newDirectFloatBuffer(positions); }
	public void setInstanceData(FloatBuffer positions) { this.positions = positions; }
	
	public void render(GL2 gl)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(texCoords == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		if(heightMap != null) { gl.glActiveTexture(GL2.GL_TEXTURE2); heightMap.bind(gl); }
		if(normalMap != null) { gl.glActiveTexture(GL2.GL_TEXTURE1); normalMap.bind(gl); }
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		if(colourMap != null) colourMap.bind(gl);
		
		enableState(gl);
		
		if(enableVBO)
		{
			bindArrays(gl);
			if(indices != null) gl.glDrawElements(polygon, indexCount, GL2.GL_UNSIGNED_INT, 0);
			else gl.glDrawArrays(polygon, 0, indexCount);
			unbindArrays(gl);
		}
		else
		{
			setupPointers(gl);
			if(indices != null) gl.glDrawElements(polygon, indexCount, GL2.GL_UNSIGNED_INT, indices);
			else gl.glDrawArrays(polygon, 0, indexCount);
		}
		
		disableState(gl);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}

	private void unbindArrays(GL2 gl)
	{
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER        , 0);
		if(indices != null) gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void setupPointers(GL2 gl)
	{
						      gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, vertices );
		if(normals   != null) gl.glNormalPointer  (   GL2.GL_FLOAT, 0, normals  );
		if(colors    != null) gl.glColorPointer   (3, GL2.GL_FLOAT, 0, colors   );
		if(texCoords != null) gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, texCoords);
		if(tangents  != null) gl.glVertexAttribPointer(TANGENT_ID , 3, GL2.GL_FLOAT, true , 0, tangents );
		if(positions != null) gl.glVertexAttribPointer(POSITION_ID, 4, GL2.GL_FLOAT, false, 0, positions);	
	}

	private void bindArrays(GL2 gl)
	{
		                        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER ]); gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, 0);
		if(normals   != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER ]); gl.glNormalPointer  (   GL2.GL_FLOAT, 0, 0); }
		if(colors    != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[COLOUR_BUFFER ]); gl.glColorPointer   (3, GL2.GL_FLOAT, 0, 0); }
		if(texCoords != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD_BUFFER ]); gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0); }
		if(tangents  != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TANGENT_BUFFER]); gl.glVertexAttribPointer(TANGENT_ID , 3, GL2.GL_FLOAT, true , 0, 0); }
		if(positions != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[OFFSET_BUFFER ]); gl.glVertexAttribPointer(POSITION_ID, 4, GL2.GL_FLOAT, false, 0, 0); }

		if(indices != null) gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
	}

	private void disableState(GL2 gl)
	{
							  gl.glDisableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY       );
		if(texCoords != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glDisableClientState(GL2.GL_COLOR_ARRAY        );
		if(tangents  != null) gl.glDisableVertexAttribArray(TANGENT_ID );
		if(positions != null) gl.glDisableVertexAttribArray(POSITION_ID);
	}

	private void enableState(GL2 gl)
	{
		                      gl.glEnableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY       );
		if(texCoords != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glEnableClientState(GL2.GL_COLOR_ARRAY        );
		if(tangents  != null) gl.glEnableVertexAttribArray(TANGENT_ID );
		if(positions != null) gl.glEnableVertexAttribArray(POSITION_ID);
		
		gl.getGL3().glVertexAttribDivisor(POSITION_ID, 1);
	}
	
	public boolean hasInstanceData() { return positions != null; }
	
	public void renderInstanced(GL2 gl, int count)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(texCoords == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		if(colourMap != null) colourMap.bind(gl);
		
		enableState(gl);
		
		if(enableVBO)
		{
			bindArrays(gl);
			if(indices != null) gl.glDrawElementsInstanced(polygon, indexCount, GL2.GL_UNSIGNED_INT, null, count);
			else gl.glDrawArraysInstanced(polygon, 0, indexCount, count);
			unbindArrays(gl);
		}
		else
		{
			setupPointers(gl);
			if(indices != null) gl.glDrawElementsInstanced(polygon, indexCount, GL2.GL_UNSIGNED_INT, indices, count);
			else gl.glDrawArraysInstanced(polygon, 0, indexCount, count);
		}
		
		disableState(gl);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void renderNormals(GL2 gl, boolean smooth, float scale)
	{	
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_TEXTURE_CUBE_MAP);
		
		float[] c = RGB.BLUE;
		gl.glColor3f(c[0]/255, c[1]/255, c[2]/255);

		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		
		gl.glBegin(GL2.GL_LINES);
		
		for(int i = 0; i < _vertices.size(); i++)
		{
			float[] p1 = _vertices.get(i);
			float[] normal = new float[3]; normals.get(normal, 0, 3);
			float[] p2 = Vector.add(p1, Vector.multiply(normal, scale));
			
			gl.glVertex3f(p1[0], p1[1], p1[2]);
			gl.glVertex3f(p2[0], p2[1], p2[2]);
		}
		gl.glEnd();
		
		normals.position(0);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void renderTangents(GL2 gl, boolean smooth, float scale)
	{	
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glActiveTexture(GL2.GL_TEXTURE3); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_TEXTURE_CUBE_MAP);
		
		float[] c = RGB.GREEN;
		gl.glColor3f(c[0]/255, c[1]/255, c[2]/255);

		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		
		gl.glBegin(GL2.GL_LINES);
		
		for(int i = 0; i < _vertices.size(); i++)
		{
			float[] p1 = _vertices.get(i);
			float[] tangent = new float[3]; tangents.get(tangent);
			float[] p2 = Vector.add(p1, Vector.multiply(tangent, scale));
			
			gl.glVertex3f(p1[0], p1[1], p1[2]);
			gl.glVertex3f(p2[0], p2[1], p2[2]);
		}
		gl.glEnd();
		
		tangents.position(0);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void createBuffers(GL2 gl)
	{
		gl.glGenBuffers(bufferIDs.length, bufferIDs, 0);
	    
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
		
		// Color data
		if(colors != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[COLOUR_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 *  colors.capacity(), colors, GL2.GL_STATIC_DRAW);
		}
		
		// Tangent vectors for normal mapping
		if(tangents != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TANGENT_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * tangents.capacity(), tangents, GL2.GL_STATIC_DRAW);
		}
		
		// Positional data for instanced rendering
		if(positions != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[OFFSET_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * positions.capacity(), positions, GL2.GL_STATIC_DRAW);
		}
	    
	    // Indices
		if(indices != null)
		{
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
			gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, Integer.SIZE / 8 * indexCount, indices, GL2.GL_STATIC_DRAW);
		}
		
		unbindArrays(gl);
		
		bufferCreated = true;
	}
	
	public void renderGlass(GL2 gl, float[] color)
	{
		gl.glDisable(GL_TEXTURE_2D);
		gl.glColor4f(color[0], color[1], color[2], 0.25f);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glFrontFace(GL2.GL_CW ); render(gl);
		gl.glFrontFace(GL2.GL_CCW); render(gl);
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);	
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
}
