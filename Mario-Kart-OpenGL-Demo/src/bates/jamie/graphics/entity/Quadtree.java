package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Quadtree
{
	private static final float PEAK_INC = 0.15f;
	private static final float HILL_INC = 3.0f;
	
	private static final float MIN_RADIUS =  8;
	private static final float MAX_RADIUS = 40;
	
	int lod;
	public static final int MAXIMUM_LOD = 10;
	
	private static final float EPSILON = 0.005f;
	private static final float VECTOR_OFFSET = 0.005f;
	
	public Quadtree root;
	
	public Quadtree north_west;
	public Quadtree north_east;
	public Quadtree south_west;
	public Quadtree south_east;
	
	int[] indices = new int[4];
	
	List<float[]> vertices;
	List<float[]> texCoords;
	
	IntBuffer iBuffer;
	FloatBuffer vBuffer;
	FloatBuffer tBuffer;
	
	int indexCount;
	
	Texture texture;
	boolean textured = false;
	
	public Quadtree(int lod, List<float[]> vertices, List<float[]> texCoords, int[] indices)
	{
		this.lod = lod;
		
		this.vertices = vertices;
		this.texCoords = texCoords;
		this.indices = indices;
		
		textured = true;
	}
	
	public Quadtree(int lod, List<float[]> vertices, int[] indices)
	{
		this.lod = lod;
		
		this.vertices = vertices;
		this.indices = indices;
	}
	
	public Quadtree(int lod, List<float[]> vertices, int[] indices, int iterations)
	{
		this.lod = lod;
		
		this.vertices = vertices;
		this.indices = indices;
		
		subdivide(iterations);
	}
	
	public Quadtree(int lod, List<float[]> vertices, int[] indices, int iterations, Random generator)
	{
		this.lod = lod;
		
		this.vertices = vertices;
		this.indices = indices;
		
		subdivide(iterations, generator);
	}
	
	public Quadtree(int lod, List<float[]> vertices, List<float[]> texCoords, int[] indices, Texture texture, int iterations, Random generator)
	{
		long start = System.nanoTime();
		
		root = this;
		
		this.lod = lod;
		
		this.vertices = vertices;
		this.texCoords = texCoords;
		this.indices = indices;
		
		this.texture = texture;
		textured = true;
		
		if(generator != null) subdivide(iterations, generator);
		else subdivide(iterations);
		
		System.out.printf("Quadtree, Construction: %.3f ms\n", (System.nanoTime() - start) / 1E6);
		
		System.out.printf("Quadtree, Update: %.3f ms\n", updateBuffers() / 1E6);
	}
	
	public void flatten()
	{
		if(isLeaf()) return;
		
		if(north_west.isLeaf() && north_east.isLeaf() &&
		   south_west.isLeaf() && south_east.isLeaf())
		{
			float[] northwest = vertices.get(north_west.indices[3]);
			float[] northeast = vertices.get(north_east.indices[2]);
			float[] southwest = vertices.get(south_west.indices[0]);
			float[] southeast = vertices.get(south_east.indices[1]);
			
			float[] north  = vertices.get(north_west.indices[2]);
			float[] east   = vertices.get(south_east.indices[2]);
			float[] south  = vertices.get(south_east.indices[0]);
			float[] west   = vertices.get(north_west.indices[0]);
			float[] centre = vertices.get(north_west.indices[1]);
			
			if(gradient(southeast, east , northeast) &&
			   gradient(southwest, west , northwest) &&
			   gradient(southwest, south, southeast) &&
			   gradient(northwest, north, northeast) &&
			   gradient(west , centre, east ) &&
			   gradient(north, centre, south)) decimate();
		}
		else
		{
			north_west.flatten();
			north_east.flatten();
			south_west.flatten();
			south_east.flatten();
		}
	}
	
	public void divideAtPoint(int index, int lod)
	{
		divideAtPoint(vertices.get(index), lod);
	}
	
	public void divideAtPoint(float[] p, int lod)
	{
		Quadtree cell = this;
		
		while(cell.lod < lod)
		{
			cell.subdivide();
			cell = getCell(p, MAXIMUM_LOD);
		}
	}
	
	public void repairCrack(int index)
	{
		Quadtree[] cells = neighbourhood(index);
		
		int lod = 0;
		
		for(int i = 0; i < 4; i++)
			if(cells[i] != null)
				lod = (cells[i].lod > lod) ? cells[i].lod : lod;
		
		for(int i = 0; i < 4; i++)
		{
			if(cells[i] != null)
			{
				cells[i].divideAtPoint(index, lod);
				cells = neighbourhood(index);
			}
		}
	}
	
	public Quadtree[] neighbourhood(float[] p)
	{
		Quadtree[] neighbours = new Quadtree[4];
		
		neighbours[0] = getCell(Vector.add(p, new float[] {-VECTOR_OFFSET, 0, -VECTOR_OFFSET}), MAXIMUM_LOD);
		neighbours[1] = getCell(Vector.add(p, new float[] {+VECTOR_OFFSET, 0, -VECTOR_OFFSET}), MAXIMUM_LOD);
		neighbours[2] = getCell(Vector.add(p, new float[] {-VECTOR_OFFSET, 0, +VECTOR_OFFSET}), MAXIMUM_LOD);
		neighbours[3] = getCell(Vector.add(p, new float[] {+VECTOR_OFFSET, 0, +VECTOR_OFFSET}), MAXIMUM_LOD);
		
		return neighbours;
	}
	
	public Quadtree[] neighbourhood(int index)
	{
		return neighbourhood(vertices.get(index));
	}
	
	public static boolean gradient(float[] a, float[] b, float[] c)
	{
		float ba = b[1] - a[1];
		float cb = c[1] - b[1];
		
		return cb - ba < EPSILON;
	}
	
	public long updateBuffers()
	{
		return updateBuffers(MAXIMUM_LOD);
	}
	
	public long updateBuffers(int lod)
	{
		long start = System.nanoTime();
		
		vBuffer = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) vBuffer.put(vertex);
		vBuffer.position(0);
		
		tBuffer = Buffers.newDirectFloatBuffer(texCoords.size() * 2);
		for(float[] texCoord : texCoords) tBuffer.put(texCoord);
		tBuffer.position(0);
		
		List<int[]> indices = new ArrayList<int[]>(); getIndices(indices, lod);
		
		iBuffer = Buffers.newDirectIntBuffer(indices.size() * 4);
		for(int[] index : indices) iBuffer.put(index);
		iBuffer.position(0);
		
		indexCount = indices.size() * 4;
		
		return System.nanoTime() - start;
	}
	
	public int containsVertex(float[] p)
	{
		int index = pointOnCell(p);
		if(index != -1) return index;
		
		int nw, ne, sw, se;
		nw = ne = sw = se = -1;
		
		if(north_west != null && north_west.pointInCell(p)) nw = north_west.containsVertex(p); if(nw != -1) return nw;
		if(north_east != null && north_east.pointInCell(p)) ne = north_east.containsVertex(p); if(ne != -1) return ne;
		if(south_west != null && south_west.pointInCell(p)) sw = south_west.containsVertex(p); if(sw != -1) return sw;
		if(south_east != null && south_east.pointInCell(p)) se = south_east.containsVertex(p); if(se != -1) return se;
		
		return -1;
	}
	
	public int pointOnCell(float[] p)
	{
		if(Vector.equal(p, vertices.get(indices[0]))) return indices[0];
		if(Vector.equal(p, vertices.get(indices[1]))) return indices[1];
		if(Vector.equal(p, vertices.get(indices[2]))) return indices[2];
		if(Vector.equal(p, vertices.get(indices[3]))) return indices[3];
		
		else return -1;
	}
	
	public boolean pointInCell(float[] p)
	{
		float x = p[0];
		float z = p[2];
		
		float _x = vertices.get(indices[3])[0];
		float _z = vertices.get(indices[3])[2];
		float x_ = vertices.get(indices[1])[0];
		float z_ = vertices.get(indices[1])[2];
		
		return ((x >= _x) && (x <= x_) && (z >= _z) && (z <= z_)); 
	}

	public boolean insert(float[] p)
	{
		// Ignore objects which do not belong in this quad tree
		if(pointInCell(p)) return false;

		// Otherwise, we need to subdivide then add the point to whichever node will accept it
		if(isLeaf()) subdivide();

		if(north_west.insert(p)) return true;
		if(north_east.insert(p)) return true;
		if(south_west.insert(p)) return true;
		if(south_east.insert(p)) return true;

		// Otherwise, the point cannot be inserted for some unknown reason (which should never happen)
		return false;
	}
	
	public boolean subdivide()
	{
		if(lod + 1 > MAXIMUM_LOD) return false;
		
		float _x = vertices.get(indices[3])[0];
		float _z = vertices.get(indices[3])[2];
		float x_ = vertices.get(indices[1])[0];
		float z_ = vertices.get(indices[1])[2];
		
		float _x_ = (_x + x_) / 2;
		float _z_ = (_z + z_) / 2;
		
		float[] vNorth  = {_x_, 0, _z }; vNorth [1] = getHeight(vNorth ); 
		float[] vEast   = { x_, 0, _z_}; vEast  [1] = getHeight(vEast  );
		float[] vSouth  = {_x_, 0,  z_}; vSouth [1] = getHeight(vSouth );
		float[] vWest   = {_x , 0, _z_}; vWest  [1] = getHeight(vWest  );
		float[] vCentre = {_x_, 0, _z_}; vCentre[1] = getHeight(vCentre);
		
		int north, east, south, west, centre;
		north = east = south = west = centre = -1;
		
		int iNorth  = north  = root.containsVertex(vNorth ); if(iNorth  == -1) { north  = vertices.size(); vertices.add(vNorth ); }
		int iEast   = east   = root.containsVertex(vEast  ); if(iEast   == -1) { east   = vertices.size(); vertices.add(vEast  ); }
		int iSouth  = south  = root.containsVertex(vSouth ); if(iSouth  == -1) { south  = vertices.size(); vertices.add(vSouth ); }
		int iWest   = west   = root.containsVertex(vWest  ); if(iWest   == -1) { west   = vertices.size(); vertices.add(vWest  ); }
		int iCentre = centre = root.containsVertex(vCentre); if(iCentre == -1) { centre = vertices.size(); vertices.add(vCentre); }

		if(textured)
		{
			float _s = texCoords.get(indices[3])[0];
			float _t = texCoords.get(indices[3])[1];
			float s_ = texCoords.get(indices[1])[0];
			float t_ = texCoords.get(indices[1])[1];
			
			float _s_ = (_s + s_) / 2;
			float _t_ = (_t + t_) / 2;
			
			float[] tNorth  = {_s_, _t }; if(iNorth  == -1) texCoords.add(tNorth );
			float[] tEast   = { s_, _t_}; if(iEast   == -1) texCoords.add(tEast  );
			float[] tSouth  = {_s_,  t_}; if(iSouth  == -1) texCoords.add(tSouth );
			float[] tWest   = {_s , _t_}; if(iWest   == -1) texCoords.add(tWest  );
			float[] tCentre = {_s_, _t_}; if(iCentre == -1) texCoords.add(tCentre);
			
			north_west = new Quadtree(lod + 1, vertices, texCoords, new int[] {west, centre, north, indices[3]}); north_west.root = root;
			north_east = new Quadtree(lod + 1, vertices, texCoords, new int[] {centre, east, indices[2], north}); north_east.root = root;
			south_west = new Quadtree(lod + 1, vertices, texCoords, new int[] {indices[0], south, centre, west}); south_west.root = root;
			south_east = new Quadtree(lod + 1, vertices, texCoords, new int[] {south, indices[1], east, centre}); south_east.root = root;
		}
		else
		{
			north_west = new Quadtree(lod + 1, vertices, new int[] {west, centre, north, indices[3]}); north_west.root = root;
			north_east = new Quadtree(lod + 1, vertices, new int[] {centre, east, indices[2], north}); north_east.root = root;
			south_west = new Quadtree(lod + 1, vertices, new int[] {indices[0], south, centre, west}); south_west.root = root;
			south_east = new Quadtree(lod + 1, vertices, new int[] {south, indices[1], east, centre}); south_east.root = root;
		}
		
		return true;
	}
	
	public boolean subdivide(int iterations)
	{
		boolean divisible = subdivide();
		
		iterations--;
		
		if(iterations > 0)
		{
			north_west.subdivide(iterations);
			north_east.subdivide(iterations);
			south_west.subdivide(iterations);
			south_east.subdivide(iterations);
		}
		
		return divisible;
	}
	
	public void subdivide(int iteration, Random generator)
	{
		subdivide();
		
		iteration--;
		
		if(iteration > 0)
		{
			if(generator.nextFloat() > 0.3) north_west.subdivide(iteration, generator);
			if(generator.nextFloat() > 0.3) north_east.subdivide(iteration, generator);
			if(generator.nextFloat() > 0.3) south_west.subdivide(iteration, generator);
			if(generator.nextFloat() > 0.3) south_east.subdivide(iteration, generator);
		}
	}
	
	public Quadtree getCell(float[] p, int lod)
	{
		if((isLeaf() || this.lod == lod) && pointInCell(p)) return this;
		
		if(north_west != null && north_west.pointInCell(p)) return north_west.getCell(p, lod);
		if(north_east != null && north_east.pointInCell(p)) return north_east.getCell(p, lod);
		if(south_west != null && south_west.pointInCell(p)) return south_west.getCell(p, lod);
		if(south_east != null && south_east.pointInCell(p)) return south_east.getCell(p, lod);
		
		return null;
	}
	
	public float getHeight(float[] p)
	{	
		float x = p[0];
		float z = p[2];
		
		float x1 = vertices.get(indices[3])[0];
		float z1 = vertices.get(indices[3])[2];
		float x2 = vertices.get(indices[1])[0];
		float z2 = vertices.get(indices[1])[2];
		
		float q11 = vertices.get(indices[3])[1];
		float q12 = vertices.get(indices[0])[1];
		float q21 = vertices.get(indices[2])[1];
		float q22 = vertices.get(indices[1])[1];

		float r1 = ((x2 - x) / (x2 - x1)) * q11 + ((x - x1) / (x2 - x1)) * q21;
		float r2 = ((x2 - x) / (x2 - x1)) * q12 + ((x - x1) / (x2 - x1)) * q22;
		
		return r1 * ((z2 -  z) / (z2 - z1)) + r2 * ((z  - z1) / (z2 - z1));
	}
	
	public void getIndices(List<int[]> _indices)
	{
		if(isLeaf()) _indices.add(indices);
		else
		{
			north_west.getIndices(_indices);
			north_east.getIndices(_indices);
			south_west.getIndices(_indices);
			south_east.getIndices(_indices);
		}
	}
	
	public void getIndices(List<int[]> _indices, int lod)
	{
		if(isLeaf() || this.lod == lod) _indices.add(indices);
		else
		{
			north_west.getIndices(_indices, lod);
			north_east.getIndices(_indices, lod);
			south_west.getIndices(_indices, lod);
			south_east.getIndices(_indices, lod);
		}
	}

	public void setHeights(int iterations)
	{
		Random generator = new Random();
		
		int x, z;
		
		float _x = vertices.get(root.indices[3])[0];
		float _z = vertices.get(root.indices[3])[2];
		
		float length = root.getLength();
		
		for (int i = 0; i < iterations; i++)
		{
			x = (int) (generator.nextDouble() * length);
			z = (int) (generator.nextDouble() * length);

			if(generator.nextBoolean())
			{
				float radius = MIN_RADIUS + generator.nextFloat() * (MAX_RADIUS - MIN_RADIUS);
				float peak = generator.nextFloat() * HILL_INC;
				
				increaseRadius(new float[] {_x + x, 0, _z + z}, radius, peak);
			}
			else increaseRadius(new float[] {_x + x, 0, _z + z}, 8, PEAK_INC);
		}
	}
	
	public void increaseRadius(float[] p, float radius, float peak)
	{	
		float offset = 0.5f / radius;
		
		for(int i = 0; i < vertices.size(); i++)
		{	
			float[] vertex = vertices.get(i);
			
			float x = Math.abs(vertex[0] - p[0]);
			float z = Math.abs(vertex[2] - p[2]);
			
			double d = Math.sqrt(x * x + z * z);
			
			if(d < radius && d > EPSILON)
			{
				repairCrack(i);
				
				if(d == 0) vertex[1] += peak * (1 - offset);
				else vertex[1] += peak * (1 - (d / radius));
			}
		}
	}
	
	public int getMaximumLOD()
	{
		if(isLeaf()) return lod;
		
		int max_lod = 0;
		int lod = 0;
		
		if(north_west != null) { lod = north_west.getMaximumLOD(); if(lod > max_lod) max_lod = lod; }
		if(north_east != null) { lod = north_east.getMaximumLOD(); if(lod > max_lod) max_lod = lod; }
		if(south_west != null) { lod = south_west.getMaximumLOD(); if(lod > max_lod) max_lod = lod; }
		if(south_east != null) { lod = south_east.getMaximumLOD(); if(lod > max_lod) max_lod = lod; }
		
		return max_lod;
	}
	
	public float getIncrement()
	{
		int max_lod = root.getMaximumLOD();
		float width = root.getLength();
		
		for(int i = 0; i < max_lod; i++) width /= 2;
		
		return width;
	}
	
	public float getLength()
	{
		float _x = vertices.get(indices[3])[0];
		float x_ = vertices.get(indices[1])[0];
		
		return Math.abs(_x - x_);
	}
	
	public boolean isLeaf()
	{
		return north_west == null && north_east == null &&
			   south_west == null && south_east == null;
	}
	
	public void subdivideAll()
	{
		if(isLeaf()) subdivide();
		else
		{
			if(north_west.isLeaf()) north_west.subdivide(); else north_west.subdivideAll();
			if(north_east.isLeaf()) north_east.subdivide(); else north_east.subdivideAll();
			if(south_west.isLeaf()) south_west.subdivide(); else south_west.subdivideAll();
			if(south_east.isLeaf()) south_east.subdivide(); else south_east.subdivideAll();
		}
	}
	
	public void decimateAll()
	{
		if(isLeaf()) return;
		
		if(north_west.isLeaf() && north_east.isLeaf() && south_west.isLeaf() && south_east.isLeaf()) decimate();
		else
		{
		    if(!north_west.isLeaf()) north_west.decimateAll();
			if(!north_east.isLeaf()) north_east.decimateAll();
			if(!south_west.isLeaf()) south_west.decimateAll();
			if(!south_east.isLeaf()) south_east.decimateAll();
		}
	}
	
	public void decimate()
	{
		north_west = null;
		north_east = null;
		south_west = null;
		south_east = null;
	}
	
	public static boolean frame = true;
	public static boolean solid = true;
	
	public int vertexCount()
	{
		return vertices.size();
	}
	
	public void render(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor3f(1, 1, 1);
		
		if(!textured) gl.glDisable(GL2.GL_TEXTURE_2D);
		else gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		if(textured) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		if(textured)
		{
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, tBuffer);
			texture.bind(gl);
		}
		
		gl.glDrawElements(GL2.GL_QUADS, indexCount, GL2.GL_UNSIGNED_INT, iBuffer);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(textured) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void renderWireframe(GL2 gl)
	{
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glColor3f(0, 0, 0);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		
		gl.glDrawElements(GL2.GL_QUADS, indexCount, GL2.GL_UNSIGNED_INT, iBuffer);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);	
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public int testIndex = 0;
	
	public void nextTest()
	{
		testIndex++;
		testIndex %= indexCount;
	}
	
	public void renderNeighbourhood(GL2 gl)
	{
		Quadtree[] neighbours = neighbourhood(testIndex);
		
		List<float[]> _vertices = new ArrayList<float[]>();
		
		for(int i = 0; i < neighbours.length; i++)
		{
			if(neighbours[i] != null)
			{
				_vertices.add(vertices.get(neighbours[i].indices[0]));
				_vertices.add(vertices.get(neighbours[i].indices[1]));
				_vertices.add(vertices.get(neighbours[i].indices[2]));
				_vertices.add(vertices.get(neighbours[i].indices[3]));
			}
		}
		
		float[][] _vBuffer = new float[_vertices.size()][3];
		
		for(int i = 0; i < _vBuffer.length; i++) _vBuffer[i] = _vertices.get(i);
		
		Renderer.displayQuads(gl, _vBuffer, RGB.PURE_RED_3F);
		Renderer.displayWireframeObject(gl, _vBuffer, 4, RGB.WHITE_3F);
	}
}

