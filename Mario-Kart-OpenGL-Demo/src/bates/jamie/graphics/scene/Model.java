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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.Vector;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.common.nio.Buffers;

public class Model
{
	public static HashMap<String, Model> model_set = new HashMap<String, Model>();
	
	private List<float[]> _vertices;
	
	private FloatBuffer vertices;
	private FloatBuffer normals;
	private FloatBuffer tcoords0;
	private FloatBuffer tcoords1;
	private FloatBuffer colors;
	private FloatBuffer tangents;
	private FloatBuffer positions;
	private FloatBuffer matrices;
	
	IntBuffer indices;
	
	int polygon;
	int indexCount;
	
	public static boolean enableVBO = true;
	public boolean bufferCreated = false;
	
	private int[] bufferIDs = new int[9];
	
	private static final int   VERTEX_BUFFER = 0;
	private static final int   NORMAL_BUFFER = 1;
	private static final int  TCOORD0_BUFFER = 2;
	private static final int  TCOORD1_BUFFER = 3;
	private static final int   COLOUR_BUFFER = 4;
	private static final int POSITION_BUFFER = 5;
	private static final int  TANGENT_BUFFER = 6;
	private static final int    INDEX_BUFFER = 7;
	private static final int   MATRIX_BUFFER = 8;
	
	private static final int  TANGENT_ID =  1;
	private static final int POSITION_ID =  4;
	private static final int   MATRIX_ID = 11;
	
	public int matrixDivisor = 1;
	
	public Model(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] positions, int type)
	{
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		indexCount = vertices.length / 3; // three values per coordinate (x, y, z)
		
		this.vertices  = Buffers.newDirectFloatBuffer(vertices);
		this.normals   = Buffers.newDirectFloatBuffer(normals); 
		
		if(texCoords != null) this.tcoords0  = Buffers.newDirectFloatBuffer(texCoords); 
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
		if(texCoords != null) this.tcoords0  = Buffers.newDirectFloatBuffer(texCoords); 
		if(tangents  != null) this.tangents  = Buffers.newDirectFloatBuffer(tangents);
		if(positions != null) this.positions = Buffers.newDirectFloatBuffer(positions);
		
	}
	
	public Model(List<float[]> vertices, List<float[]> normals, int[] vIndices, int[] nIndices, int type)
	{
		long startTime = System.nanoTime();
		
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
		
		long endTime = System.nanoTime();
		
		System.out.printf("Indexed Model: %8.3f ms \n{\n\tIndices:  %d\n\tVertices: %d\n\tNormals:  %d\n}\n",
				(endTime - startTime) / 1E6, indexCount, vertices.size(), normals.size());
	}
	
	public Model(List<float[]> vertices, List<float[]> texCoords, List<float[]> normals, int[] vIndices, int[] tIndices, int[] nIndices, int type)
	{
		long startTime = System.nanoTime();
		
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
		
		this.tcoords0 = Buffers.newDirectFloatBuffer(tcoordData.size() * 2);
		for(float[] tcoord : tcoordData) this.tcoords0.put(tcoord);
		this.tcoords0.position(0);
		
		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
		for(float[] normal : normalData) this.normals.put(normal);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(offsetData.size());
		for(int i : offsetData) indices.put(i);
		indices.position(0);
		
		long endTime = System.nanoTime();
		
		System.out.printf("Indexed Model: %8.3f ms \n{\n\tIndices:  %d\n\tVertices: %d\n\tTexture Coordinates:  %d\n\tNormals:  %d\n}\n",
				(endTime - startTime) / 1E6, indexCount, vertices.size(), texCoords.size(), normals.size());
	}
	
	public Model(List<float[]> vertices, List<float[]> tcoords0, List<float[]> tcoords1, List<float[]> normals,
			int[] vIndices, int[] t0_indices, int[] t1_indices, int[] nIndices, int type)
	{
		long startTime = System.nanoTime();
		
		if(type == 3) polygon = GL2.GL_TRIANGLES;
		if(type == 4) polygon = GL2.GL_QUADS;
		
		List<float[]> vertexData = new ArrayList<float[]>();
		List<float[]> tex_0_Data = new ArrayList<float[]>();
		List<float[]> tex_1_Data = new ArrayList<float[]>();
		List<float[]> normalData = new ArrayList<float[]>();
		List<Integer> offsetData = new ArrayList<Integer>();
		
		int index = 0;
		
		for(int i = 0; i < vIndices.length; i++)
		{
			float[] vertex  = vertices.get(vIndices[i]);
			float[] normal  = normals .get(nIndices[i]);
			float[] tcoord0 = tcoords0.get(t0_indices[i]);
			float[] tcoord1 = tcoords1.get(t1_indices[i]);
			
			boolean unique = true;
			int k = index;
			
			for(int j = 0; j < vertexData.size(); j++)
			{
				if( Vector.equal(vertex , vertexData.get(j)) &&
					Vector.equal(normal , normalData.get(j)) &&
					Vector.equal(tcoord0, tex_0_Data.get(j)) &&
					Vector.equal(tcoord1, tex_1_Data.get(j)))
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
				tex_0_Data.add(tcoord0);
				tex_1_Data.add(tcoord1);
				index++;
			}
			else offsetData.add(k);
		}
		
		_vertices = vertexData;
		
		indexCount = vIndices.length;
		
		this.vertices = Buffers.newDirectFloatBuffer(vertexData.size() * 3);
		for(float[] vertex : vertexData) this.vertices.put(vertex);
		this.vertices.position(0); 
		
		this.tcoords0 = Buffers.newDirectFloatBuffer(tex_0_Data.size() * 2);
		for(float[] tcoord : tex_0_Data) this.tcoords0.put(tcoord);
		this.tcoords0.position(0);
		
		this.tcoords1 = Buffers.newDirectFloatBuffer(tex_1_Data.size() * 2);
		for(float[] tcoord : tex_1_Data) this.tcoords1.put(tcoord);
		this.tcoords1.position(0);
		
		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
		for(float[] normal : normalData) this.normals.put(normal);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(offsetData.size());
		for(int i : offsetData) indices.put(i);
		indices.position(0);
		
		long endTime = System.nanoTime();
		
		System.out.printf("Indexed Model: %8.3f ms \n{\n\tIndices:  %d\n\tVertices: %d\n\tTex Coords (0): %d\n\tTex Coords (1): %d\n\tNormals:  %d\n}\n",
				(endTime - startTime) / 1E6, indexCount, vertices.size(), tcoords0.size(), tcoords1.size(), normals.size());
	}
	
	public Model(List<float[]> vertices, List<float[]> tcoords0, List<float[]> tcoords1, List<float[]> normals, List<int[]> points)
	{
		long startTime = System.nanoTime();
		
		polygon = GL2.GL_TRIANGLES;
		
		List<float[]> vertexData = new ArrayList<float[]>();
		List<float[]> tex_0_Data = new ArrayList<float[]>();
		List<float[]> tex_1_Data = new ArrayList<float[]>();
		List<float[]> normalData = new ArrayList<float[]>();
		List<Integer> offsetData = new ArrayList<Integer>();
		
		List<int[]> pointData = new ArrayList<int[]>();
		
		int index = 0;
		
		for(int i = 0; i < points.size(); i++)
		{
			int[] pIndices = points.get(i);
			
			float[] vertex = vertices.get(pIndices[0]);
			float[] normal = normals .get(pIndices[1]);
			float[] texUV0 = tcoords0.get(pIndices[2]);
			float[] texUV1 = tcoords1.get(pIndices[2]);
			
			boolean unique = true;
			int k = index;
			
			for(int j = 0; j < pointData.size(); j++)
			{
				int[] stored_point = pointData.get(j);
				
				if(pIndices[0] == stored_point[0] &&
				   pIndices[1] == stored_point[1] && 
				   pIndices[2] == stored_point[2]   )
				{
					k = j;
					unique = false;
					break;
				}
			}
			
			if(unique)
			{
				pointData.add(pIndices);
				
				offsetData.add(k);
				vertexData.add(vertex);
				normalData.add(normal);
				tex_0_Data.add(texUV0);
				tex_1_Data.add(texUV1);
				index++;
			}
			else offsetData.add(k);
		}
		
		_vertices = vertexData;
		
		indexCount = points.size();
		
		this.vertices = Buffers.newDirectFloatBuffer(vertexData.size() * 3);
		for(float[] vertex : vertexData) this.vertices.put(vertex);
		this.vertices.position(0); 
		
		this.tcoords0 = Buffers.newDirectFloatBuffer(tex_0_Data.size() * 2);
		for(float[] tcoord : tex_0_Data) this.tcoords0.put(tcoord);
		this.tcoords0.position(0);
		
		this.tcoords1 = Buffers.newDirectFloatBuffer(tex_1_Data.size() * 2);
		for(float[] tcoord : tex_1_Data) this.tcoords1.put(tcoord);
		this.tcoords1.position(0);
		
		this.normals = Buffers.newDirectFloatBuffer(normalData.size() * 3);
		for(float[] normal : normalData) this.normals.put(normal);
		this.normals.position(0);
		
		indices = Buffers.newDirectIntBuffer(offsetData.size());
		for(int i : offsetData) indices.put(i);
		indices.position(0);
		
		long endTime = System.nanoTime();
		
		System.out.printf("Indexed Model: %8.3f ms \n{\n\tIndices:  %d\n\tVertices: %d\n\tTexture Coordinates:  %d\n\tNormals:  %d\n}\n",
				(endTime - startTime) / 1E6, indexCount, vertices.size(), tcoords0.size(), normals.size());
	}
	
	public void calculateTangents()
	{
		float[][] tan_0 = new float[vertices.capacity() / 3][3];
		float[][] tan_1 = new float[vertices.capacity() / 3][3];
		
		for(int i = 0; i < indices.capacity(); i += 3) // each loop evaluates one triangle or polygon
		{
			int i1 = indices.get();
			int i2 = indices.get();
			int i3 = indices.get();
			
			float[] v1 = new float[3]; vertices.position(i1 * 3); vertices.get(v1);
			float[] v2 = new float[3]; vertices.position(i2 * 3); vertices.get(v2);
			float[] v3 = new float[3]; vertices.position(i3 * 3); vertices.get(v3);
			
			float[] t1 = new float[2]; tcoords0.position(i1 * 2); tcoords0.get(t1);
			float[] t2 = new float[2]; tcoords0.position(i2 * 2); tcoords0.get(t2);
			float[] t3 = new float[2]; tcoords0.position(i3 * 2); tcoords0.get(t3);
			
			float[][] tangent = Vector.tangent(v1, v2, v3, t1, t2, t3);
			
			tan_0[i1] = Vector.add(tan_0[i1], tangent[0]);
			tan_0[i2] = Vector.add(tan_0[i2], tangent[0]);
			tan_0[i3] = Vector.add(tan_0[i3], tangent[0]);
			
			tan_1[i1] = Vector.add(tan_1[i1], tangent[1]);
			tan_1[i2] = Vector.add(tan_1[i2], tangent[1]);
			tan_1[i3] = Vector.add(tan_1[i3], tangent[1]);
			
		}
		vertices.position(0);
		tcoords0.position(0);
		indices .position(0);
		
		tangents = Buffers.newDirectFloatBuffer(vertices.capacity());
		
		for(int i = 0; i < tan_0.length; i++)
		{
			float[] normal  = new float[3]; normals.position(i * 3); normals.get(normal);
			float[] tangent = tan_0[i];
			
			Vec3 n = new Vec3(normal);
			Vec3 t = new Vec3(tangent);
			
			tangents.put(t.subtract(n.multiply(n.dot(t))).normalize().toArray());
		}
		normals.position(0);
		tangents.position(0);
		
		bufferCreated = false;
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
	
	public void export(String fileName)
	{
		try
		{	
			FileWriter writer = new FileWriter("iva/" + fileName + ".iva");
			BufferedWriter out = new BufferedWriter(writer);
			
			if(vertices != null)
			{
				out.write("VERTEX ");
				for(int i = 0; i < vertices.capacity(); i++) out.write(vertices.get(i) + " ");
				out.write("\r\n");
			}
			
			if(normals != null)
			{
				out.write("NORMAL ");
				for(int i = 0; i < normals.capacity(); i++) out.write(normals.get(i) + " ");
				out.write("\r\n");
			}
			
			if(tcoords0 != null)
			{
				out.write("TEXCOORD0 ");
				for(int i = 0; i < tcoords0.capacity(); i++) out.write(tcoords0.get(i) + " ");
				out.write("\r\n");
			}
			
			if(tcoords1 != null)
			{
				out.write("TEXCOORD1 ");
				for(int i = 0; i < tcoords1.capacity(); i++) out.write(tcoords1.get(i) + " ");
				out.write("\r\n");
			}
			
			if(tangents != null)
			{
				out.write("TANGENT ");
				for(int i = 0; i < tangents.capacity(); i++) out.write(tangents.get(i) + " ");
				out.write("\r\n");
			}
			
			if(indices != null)
			{
				out.write("INDEX ");
				for(int i = 0; i < indices.capacity(); i++) out.write(indices.get(i) + " ");
				out.write("\r\n");
			}
	
			out.close();
		}
		
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public Model(String fileName)
	{
		 polygon = GL2.GL_TRIANGLES;
		
		long startTime = System.nanoTime();

		try
		{
			Scanner fs = new Scanner(new File("iva/" + fileName + ".iva"));
			
			while (fs.hasNextLine())
			{
				String line = fs.nextLine();
				
				if (line.startsWith("VERTEX"))
				{
					line = line.substring(6).trim();
					String[] coords = line.split(" ");
					
					vertices = Buffers.newDirectFloatBuffer(coords.length);
					
					for(int i = 0; i < coords.length; i++)
						vertices.put(Float.parseFloat(coords[i]));
					
					vertices.position(0);
					
				}
				if (line.startsWith("NORMAL"))
				{
					line = line.substring(6).trim();
					String[] coords = line.split(" ");
					
					normals = Buffers.newDirectFloatBuffer(coords.length);
					
					for(int i = 0; i < coords.length; i++)
						normals.put(Float.parseFloat(coords[i]));
					
					normals.position(0);
				}
				if (line.startsWith("TEXCOORD0"))
				{
					line = line.substring(9).trim();
					String[] coords = line.split(" ");
					
					tcoords0 = Buffers.newDirectFloatBuffer(coords.length);
					
					for(int i = 0; i < coords.length; i++)
						tcoords0.put(Float.parseFloat(coords[i]));
					
					tcoords0.position(0);
				}
				if (line.startsWith("TEXCOORD1"))
				{
					line = line.substring(9).trim();
					String[] coords = line.split(" ");
					
					tcoords1 = Buffers.newDirectFloatBuffer(coords.length);
					
					for(int i = 0; i < coords.length; i++)
						tcoords1.put(Float.parseFloat(coords[i]));
					
					tcoords1.position(0);
				}
				if (line.startsWith("TANGENT"))
				{
					line = line.substring(7).trim();
					String[] coords = line.split(" ");
					
					tangents = Buffers.newDirectFloatBuffer(coords.length);
					
					for(int i = 0; i < coords.length; i++)
						tangents.put(Float.parseFloat(coords[i]));
					
					tangents.position(0);
				}
				if (line.startsWith("INDEX"))
				{	
					line = line.substring(5).trim();
					String[] indices = line.split(" ");
					
					indexCount = indices.length;
					
					this.indices = Buffers.newDirectIntBuffer(indices.length);
					
					for(int i = 0; i < indices.length; i++)
						this.indices.put(Integer.parseInt(indices[i]));
					
					this.indices.position(0);		
				}
			}
			fs.close();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		long endTime = System.nanoTime();
		
		System.out.printf("Indexed Model: %-13s (%5d) %8.3f ms" + "\n", fileName, indexCount / 3, (endTime - startTime) / 1E6);
		
		model_set.put(fileName, this);
	}
	
	public List<float[]> getVertices() { return _vertices; }
	
	public void setColorArray(List<float[]> colors)
	{
		this.colors = Buffers.newDirectFloatBuffer(colors.size() * 3);
		for(float[] color : colors) this.colors.put(color);
		this.colors.position(0);
	}
	
	public void setPositionData(float[]     positions) { this.positions = Buffers.newDirectFloatBuffer(positions); }
	public void setPositionData(FloatBuffer positions) { this.positions = positions; }
	
	public void setMatrixData(float[]     matrices) { this.matrices = Buffers.newDirectFloatBuffer(matrices); }
	public void setMatrixData(FloatBuffer matrices) { this.matrices = matrices; }
	
	public void render(GL2 gl)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(tcoords0 == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
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
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		if(indices != null) gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void setupPointers(GL2 gl)
	{
						      gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, vertices );
		if(normals   != null) gl.glNormalPointer  (   GL2.GL_FLOAT, 0, normals  );
		if(colors    != null) gl.glColorPointer   (3, GL2.GL_FLOAT, 0, colors   );
		if(tcoords0  != null) gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, tcoords0 );
		if(tangents  != null) gl.glVertexAttribPointer(TANGENT_ID , 3, GL2.GL_FLOAT, true , 0, tangents );
		if(positions != null) gl.glVertexAttribPointer(POSITION_ID, 4, GL2.GL_FLOAT, false, 0, positions);
		
		if(tcoords1  != null)
		{
			gl.glClientActiveTexture(GL2.GL_TEXTURE1);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, tcoords1);
			gl.glClientActiveTexture(GL2.GL_TEXTURE0);
		}
	}

	private void bindArrays(GL2 gl)
	{
		                        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER  ]); gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, 0);
		if(normals   != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER  ]); gl.glNormalPointer  (   GL2.GL_FLOAT, 0, 0); }
		if(colors    != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[COLOUR_BUFFER  ]); gl.glColorPointer   (3, GL2.GL_FLOAT, 0, 0); }
		if(tcoords0  != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD0_BUFFER ]); gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0); }
		
		if(tangents  != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TANGENT_BUFFER ]); gl.glVertexAttribPointer(TANGENT_ID , 3, GL2.GL_FLOAT, true , 0, 0); }
		if(positions != null) { gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[POSITION_BUFFER]); gl.glVertexAttribPointer(POSITION_ID, 4, GL2.GL_FLOAT, false, 0, 0); }

		if(matrices  != null)
		{
			 gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[MATRIX_BUFFER]);
			
			int stride = Float.SIZE / 8 * 4 * 4;
			
			gl.glVertexAttribPointer(MATRIX_ID + 0, 4, GL2.GL_FLOAT, false, stride, 0);
			gl.glVertexAttribPointer(MATRIX_ID + 1, 4, GL2.GL_FLOAT, false, stride, Float.SIZE / 8 *  4);
			gl.glVertexAttribPointer(MATRIX_ID + 2, 4, GL2.GL_FLOAT, false, stride, Float.SIZE / 8 *  8);
			gl.glVertexAttribPointer(MATRIX_ID + 3, 4, GL2.GL_FLOAT, false, stride, Float.SIZE / 8 * 12);
		}
		
		if(tcoords1  != null)
		{
			gl.glClientActiveTexture(GL2.GL_TEXTURE1);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD1_BUFFER]);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
			gl.glClientActiveTexture(GL2.GL_TEXTURE0);
		}
		
		if(indices != null) gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
	}

	private void disableState(GL2 gl)
	{
							  gl.glDisableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY       );
		if(tcoords0  != null) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glDisableClientState(GL2.GL_COLOR_ARRAY        );
		
		if(tangents  != null) gl.glDisableVertexAttribArray(TANGENT_ID );
		if(positions != null) gl.glDisableVertexAttribArray(POSITION_ID);
		
		if(matrices  != null)
		{
			gl.glDisableVertexAttribArray(MATRIX_ID + 0);
			gl.glDisableVertexAttribArray(MATRIX_ID + 1);
			gl.glDisableVertexAttribArray(MATRIX_ID + 2);
			gl.glDisableVertexAttribArray(MATRIX_ID + 3);
		}
		
		if(tcoords1  != null)
		{
			gl.glClientActiveTexture(GL2.GL_TEXTURE1);
			gl.glDisableClientState (GL2.GL_TEXTURE_COORD_ARRAY);
			gl.glClientActiveTexture(GL2.GL_TEXTURE0);
		}
	}

	private void enableState(GL2 gl)
	{
		                      gl.glEnableClientState(GL2.GL_VERTEX_ARRAY       );
		if(normals   != null) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY       );
		if(tcoords0  != null) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		if(colors    != null) gl.glEnableClientState(GL2.GL_COLOR_ARRAY        );
		
		if(tangents  != null) gl.glEnableVertexAttribArray(TANGENT_ID );
		if(positions != null) gl.glEnableVertexAttribArray(POSITION_ID);
		
		if(matrices  != null)
		{
			gl.glEnableVertexAttribArray(MATRIX_ID + 0);
			gl.glEnableVertexAttribArray(MATRIX_ID + 1);
			gl.glEnableVertexAttribArray(MATRIX_ID + 2);
			gl.glEnableVertexAttribArray(MATRIX_ID + 3);
		}
		
		if(tcoords1  != null)
		{
			gl.glClientActiveTexture(GL2.GL_TEXTURE1);
			gl.glEnableClientState  (GL2.GL_TEXTURE_COORD_ARRAY);
			gl.glClientActiveTexture(GL2.GL_TEXTURE0);
		}
		
		gl.getGL3().glVertexAttribDivisor(POSITION_ID, 1);
		
		gl.getGL3().glVertexAttribDivisor(MATRIX_ID + 0, matrixDivisor);
		gl.getGL3().glVertexAttribDivisor(MATRIX_ID + 1, matrixDivisor);
		gl.getGL3().glVertexAttribDivisor(MATRIX_ID + 2, matrixDivisor);
		gl.getGL3().glVertexAttribDivisor(MATRIX_ID + 3, matrixDivisor);
	}
	
	public boolean hasInstanceData() { return positions != null; }
	public boolean hasTangentData () { return tangents  != null; }
	public boolean hasMatrixData  () { return matrices  != null; }
	
	public void renderInstanced(GL2 gl, int count)
	{
		if(!bufferCreated && enableVBO) createBuffers(gl);
		
		if(tcoords0 == null) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
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
		gl.glColor3f(c[0], c[1], c[2]);

		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		
		gl.glBegin(GL2.GL_LINES);
		
		for(int i = 0; i < vertices.capacity(); i += 3)
		{
			float[] p1     = new float[3]; vertices.get(p1);
			float[] normal = new float[3]; normals.get(normal);
			float[] p2 = Vector.add(p1, Vector.multiply(normal, scale));
			
			gl.glVertex3f(p1[0], p1[1], p1[2]);
			gl.glVertex3f(p2[0], p2[1], p2[2]);
		}
		gl.glEnd();
		
		vertices.position(0);
		normals.position(0);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	public void renderTangents(GL2 gl, boolean smooth, float scale)
	{	
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glEnable(GL2.GL_BLEND);
		
		float[] c = RGB.GREEN;
		gl.glColor3f(c[0], c[1], c[2]);

		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		
		gl.glBegin(GL2.GL_LINES);
		
		for(int i = 0; i < vertices.capacity(); i += 3)
		{
			float[] p1      = new float[3]; vertices.get(p1);
			float[] tangent = new float[3]; tangents.get(tangent);
			float[] p2 = Vector.add(p1, Vector.multiply(tangent, scale));
			
			gl.glVertex3f(p1[0], p1[1], p1[2]);
			gl.glVertex3f(p2[0], p2[1], p2[2]);
		}
		gl.glEnd();
		
		vertices.position(0);
		tangents.position(0);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glDisable(GL2.GL_BLEND);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	public void createBuffers(GL2 gl)
	{
		gl.glGenBuffers(bufferIDs.length, bufferIDs, 0);
	    
	    // Vertex data
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VERTEX_BUFFER]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * vertices.capacity(), vertices, GL2.GL_STATIC_DRAW);
		// the division by 8 is required to convert bits to bytes
	    
	    // Normal data
		if(normals != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[NORMAL_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * normals.capacity(), normals, GL2.GL_STATIC_DRAW);
		}
	    
	    // Texture coordinates
		if(tcoords0 != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD0_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * tcoords0.capacity(), tcoords0, GL2.GL_STATIC_DRAW);
		}
		
		// Additional texture coordinates for complex texture mapping (i.e. using an alpha map)
		if(tcoords1 != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[TCOORD1_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * tcoords1.capacity(), tcoords1, GL2.GL_STATIC_DRAW);
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
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[POSITION_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * positions.capacity(), positions, GL2.GL_STATIC_DRAW);
		}
	    
	    // Indices
		if(indices != null)
		{
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[INDEX_BUFFER]);
			gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, Integer.SIZE / 8 * indexCount, indices, GL2.GL_STATIC_DRAW);
		}
		
		// Transformation matrices for instanced rendering 
		if(matrices != null)
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[MATRIX_BUFFER]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * matrices.capacity(), matrices, GL2.GL_STATIC_DRAW);
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
	
	@Override
	public String toString()
	{
		String str = "";
		
		                      str += "Face Count    : " + indexCount / 3 + "\n";
		if(indices   != null) str += "Indices       : " +   indices.capacity() + "\n";
		if(vertices  != null) str += "Vertices      : " +  vertices.capacity() / 3 + "\n";
		if(normals   != null) str += "Normals       : " +   normals.capacity() / 3 + "\n";
		if(tcoords0  != null) str += "Tex Coords (0): " +  tcoords0.capacity() / 2 + "\n";
		if(tcoords1  != null) str += "Tex Coords (1): " +  tcoords1.capacity() / 2 + "\n";
		if(colors    != null) str += "Colors        : " +    colors.capacity() / 3 + "\n";
		if(tangents  != null) str += "Tangents      : " +  tangents.capacity() / 3 + "\n";
		if(positions != null) str += "Instance Data : " + positions.capacity() / 4 + "\n";
		
		return str;
	}
}
