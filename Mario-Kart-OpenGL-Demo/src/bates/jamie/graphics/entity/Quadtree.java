package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Quadtree
{
	int lod;
	public static final int MAXIMUM_LOD = 8;
	
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
	
	public long updateBuffers()
	{
		long start = System.nanoTime();
		
		vBuffer = Buffers.newDirectFloatBuffer(vertices.size() * 3);
		for(float[] vertex : vertices) vBuffer.put(vertex);
		vBuffer.position(0);
		
		tBuffer = Buffers.newDirectFloatBuffer(texCoords.size() * 2);
		for(float[] texCoord : texCoords) tBuffer.put(texCoord);
		tBuffer.position(0);
		
		List<int[]> indices = new ArrayList<int[]>(); getIndices(indices, 7);
		
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
		float _z = vertices.get(indices[1])[2];
		float x_ = vertices.get(indices[1])[0];
		float z_ = vertices.get(indices[3])[2];
		
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
	
	public void subdivide()
	{
		if(lod + 1 > MAXIMUM_LOD) return;
		
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
	}
	
	public void subdivide(int iterations)
	{
		subdivide();
		
		iterations--;
		
		if(iterations > 0)
		{
			north_west.subdivide(iterations);
			north_east.subdivide(iterations);
			south_west.subdivide(iterations);
			south_east.subdivide(iterations);
		}
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
	
	public boolean isLeaf() { return north_west == null; }
	
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
	
	public void render(GL2 gl, int lod)
	{
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
	
	public void renderWireframe(GL2 gl, int lod)
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
}

