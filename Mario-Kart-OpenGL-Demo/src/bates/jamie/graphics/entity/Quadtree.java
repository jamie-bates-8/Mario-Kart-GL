package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.util.Gradient;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;

public class Quadtree
{
	private static final float HILL_INC = 2.0f;
	
	private static final float MIN_RADIUS = 10;
	private static final float MAX_RADIUS = 60;
	
	private static final float MAX_TROUGH = 1.00f;
	private static final float MAX_RIPPLE = 0.25f;
	
	private static final float MAX_HEIGHT = 40.0f;
	
	public static final int MAXIMUM_LOD = 10;
	public int detail = MAXIMUM_LOD;
	public int lod;
	
	private static final float EPSILON = 0.0005f;
	private static final float VECTOR_OFFSET = 0.005f;
	
	public Quadtree root;
	
	public Quadtree north_west;
	public Quadtree north_east;
	public Quadtree south_west;
	public Quadtree south_east;
	
	int[] indices = new int[4];
	
	List<float[]> vertices;
	List<float[]> normals;
	List<float[]> texCoords;
	List<float[]> colors;
	
	List<Float> heights;
	public float height = 0;
	
	int offset;
	
	IntBuffer   iBuffer;
	FloatBuffer vBuffer;
	FloatBuffer nBuffer;
	FloatBuffer tBuffer;
	FloatBuffer cBuffer;
	
	int indexCount;
	
	public Texture texture;
	public Gradient gradient = Gradient.GRAYSCALE;
	
	public FallOff falloff = FallOff.SMOOTH;
	public boolean malleable = true;
	
	public boolean enableShading = true;
	public boolean smoothShading = true;
	public boolean enableTexture = true;
	
	public boolean enableColoring = true;
	public boolean enableBlending = false;
	public boolean enableSpecular = false;
	
	public float[] specular = {0, 0, 0, 1};
	
	public boolean frame = false;
	public boolean solid = true;
	public boolean vNormals = false;
	
	public boolean reliefMap = false;

	public float[] lineColor = RGB.WHITE_3F;
	
	/**
	 * This method constructs a Quadtree data structure that maintains an indexed
	 * list of textured geometry
	 * 
	 * @param root - A reference to the root of the quadtree hierarchy.
	 * @param lod - Specifies the level of detail (LOD), that is, the level at which
	 * this subtree is located in the quadtree hierarchy.
	 * @param indices - The array of indices to be used by the indexed vertex buffer
	 * to reference vertices, texture coordinates and colors appropriately.
	 * @param textured - If <code>true</code>, the geometry will be textured using
	 * the texture stored by the root.
	 */
	public Quadtree(Quadtree root, int lod, int[] indices, boolean textured)
	{
		this.root = root;
		this.lod  = lod;
		
		heights = root.heights;
		
		vertices  = root.vertices;
		normals   = root.normals;
		texCoords = root.texCoords;
		colors    = root.colors;
		
		vBuffer = root.vBuffer;
		nBuffer = root.nBuffer;
		tBuffer = root.tBuffer;
		cBuffer = root.cBuffer;
		iBuffer = root.iBuffer;
		
		this.indices = indices;
		
		this.enableTexture = textured;
	}
	
	/**
	 * This method constructs the root of a Quadtree data structure that maintains an
	 * indexed list of colored geometry
	 * 
	 * @param vertices - A list of (hopefully) unique vertices used to render the
	 * geometric model maintained by this data structure. 
	 * @param iterations - The desired level of detail (LOD).
	 * @param generator - if this parameter is non-null, an aspect of randomness
	 * will be added to the quadtree's construction, subdividing randomly rather
	 * than uniformly.
	 */
	public Quadtree(List<float[]> vertices, int iterations, Random generator)
	{
		root = this;
		lod = 0;
		
		this.vertices  = vertices;
		this.indices   = new int[] {0, 1, 2, 3};
		
		texCoords = new ArrayList<float[]>();
		
		createLists(vertices);
		createBuffers();
		
		enableTexture = false;
		
		// a random generator is passed to each subdivision rather than created per call
		if(generator != null) subdivide(iterations, generator);
		else subdivide(iterations);
	}
	
	public Quadtree(float vScale, int iterations)
	{
		root = this;
		lod = 0;
		
		List<float[]> vertices = new ArrayList<float[]>();
		vertices.add(new float[] {-vScale, 0,  vScale});
		vertices.add(new float[] { vScale, 0,  vScale});
		vertices.add(new float[] { vScale, 0, -vScale});
		vertices.add(new float[] {-vScale, 0, -vScale});
		
		this.vertices  = vertices;
		this.indices   = new int[] {0, 1, 2, 3};
		
		texCoords = new ArrayList<float[]>();
		
		createLists(vertices);
		createBuffers();
		
		enableTexture = false;
		
		subdivide(iterations);
	}
	
	/**
	 * This method constructs the root of a Quadtree data structure that maintains an
	 * indexed list of textured geometry
	 * 
	 * @param vertices - A list of (hopefully) unique vertices used to render the
	 * geometric model maintained by this data structure. 
	 * @param texCoords - A list of (hopefully) unique texture coordinates.
	 * @param texture - The base texture used to render the structure's geometry.
	 * @param iterations - The desired level of detail (LOD).
	 * @param generator - if this parameter is non-null, an aspect of randomness
	 * will be added to the quadtree's construction, subdividing randomly rather
	 * than uniformly.
	 */
	public Quadtree(List<float[]> vertices, List<float[]> texCoords, Texture texture, int iterations, Random generator)
	{
		long start = System.nanoTime();
		
		System.out.print("Quadtree:\n{\n");
		
		root = this;
		lod = 0;
		
		offset = 0;
		
		this.indices   = new int[] {0, 1, 2, 3};
		
		this.vertices  = vertices;
		this.texCoords = texCoords;
		
		createLists(vertices);
		createBuffers();
		
		this.texture = texture;
		enableTexture = true;
		
		// a random generator is passed to each subdivision rather than created per call
		if(generator != null) subdivide(iterations, generator);
		else subdivide(iterations);
		
		System.out.printf("\tConstructor: %7.3f ms\n", (System.nanoTime() - start) / 1E6);
		System.out.printf("\tVertices   : %7d\n", vertexCount());
		System.out.printf("\tCells      : %7d\n", cellCount());
		System.out.println("}");
	}
	
	public Quadtree(float vScale, float tScale, Texture texture, int iterations)
	{	
		long start = System.nanoTime();
		
		System.out.print("Quadtree:\n{\n");
		
		root = this;
		lod = 0;
		
		offset = 0;
		
		this.indices = new int[] {0, 1, 2, 3};
		
		List<float[]> vertices = new ArrayList<float[]>();
		vertices.add(new float[] {-vScale, 0,  vScale});
		vertices.add(new float[] { vScale, 0,  vScale});
		vertices.add(new float[] { vScale, 0, -vScale});
		vertices.add(new float[] {-vScale, 0, -vScale});
		
		List<float[]> texCoords = new ArrayList<float[]>();
		texCoords.add(new float[] {     0,      0});
		texCoords.add(new float[] {tScale,      0});
		texCoords.add(new float[] {tScale, tScale});
		texCoords.add(new float[] {     0, tScale});
		
		this.vertices  = vertices;
		this.texCoords = texCoords;
		
		createLists(vertices);
		createBuffers();
		
		this.texture = texture;
		enableTexture = true;
		
		subdivide(iterations);
		
		System.out.printf("\tConstructor: %7.3f ms\n", (System.nanoTime() - start) / 1E6);
		System.out.printf("\tVertices   : %7d\n", vertexCount());
		System.out.printf("\tCells      : %7d\n", cellCount());
		System.out.println("}");
	}

	private void createLists(List<float[]> vertices)
	{
		normals = new ArrayList<float[]>();
		float[] normal = Vector.normal(vertices.get(0), vertices.get(1), vertices.get(3));
		for(int i = 0; i < 4; i++) normals.add(normal);
		
		colors = new ArrayList<float[]>();
		for(int i = 0; i < 4; i++) colors.add(RGB.WHITE_3F);
		
		heights = new ArrayList<Float>();
		for(int i = 0; i < 4; i++) heights.add(vertices.get(i)[1]);
	}
	
	public void translate(float[] vector)
	{
		if(enableBlending) height += vector[1];
		
		int position = vBuffer.position();
		
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] vertex = vertices.get(i);

			vertex[0] += vector[0];
			vertex[1] += vector[1];
			vertex[2] += vector[2];
			
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);
			
			heights.set(i, heights.get(i) + vector[1]);
		}
		
		vBuffer.position(position);
	}
	
	/**
	 * This method decimates a quadtree that has leaf nodes if the leaves do not
	 * provide any additional detail. This can be determined by checking the gradient
	 * of all 6 lines that constitute the 4 leaves.
	 * 
	 * For example, if the line that connects the north-west and north-east points runs
	 * through the north point, the gradient is considered smooth. Therefore, the north
	 * point is not required to represent the curvature of that line.  
	 */
	public void flatten()
	{
		// cannot remove detail from this quadtree
		if(isLeaf()) return;
		
		// if this quadtree has leaf nodes exclusively
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
			   gradient(west , centre, east )  && // horizontal centre
			   gradient(north, centre, south)) decimate(); // vertical centre
		}
		else // traverse the tree to a suitible level
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
	
	/**
	 * This method ensures that all of the cells neighbouring the vertex at the
	 * index specified are of the same detail (LOD). By doing so, this vertex can
	 * be deformed without introducing any cracking artifacts.
	 * 
	 * This is done by finding the maximum LOD of the cells, and then subdividing
	 * the less detailed cells until they reach this desired level.  
	 */
	public void repairCrack(int index)
	{	
		Quadtree[] cells = getAdjacent(index);
		
		int lod = 0;
		
		// find the maximum level of detail required
		for(int i = 0; i < 4; i++)
			if(cells[i] != null)
				lod = (cells[i].lod > lod) ? cells[i].lod : lod;
		
		for(int i = 0; i < 4; i++)
		{
			if(cells[i] != null)
			{
				cells[i].divideAtPoint(index, lod);
				// subdivide may have introduced new neighbours
				cells = getAdjacent(index);
			}
		}
	}
	
	/**
	 * This method finds the leaf nodes that reference vertex <code>p</code>.
	 * For example, the north-east leaf node can be located by finding a cell
	 * that contains a vertex <code>p</code> with a small diagonal (-x, -z)
	 * vector added.
	 * 
	 * While getting the cell at <code>p</code> may yield one of the four neighbours
	 * depending on how the tree is traversed, the vector offset allows the traversal
	 * done by the getCell() method to accurately distinct each neighbouring cell.
	 */
	public Quadtree[] getAdjacent(float[] p)
	{
		Quadtree[] neighbours = new Quadtree[4];
		
		neighbours[0] = getCell(Vector.add(p, new float[] {-VECTOR_OFFSET, 0, -VECTOR_OFFSET}), MAXIMUM_LOD); // north-west
		neighbours[1] = getCell(Vector.add(p, new float[] {+VECTOR_OFFSET, 0, -VECTOR_OFFSET}), MAXIMUM_LOD); // south-west
		neighbours[2] = getCell(Vector.add(p, new float[] {-VECTOR_OFFSET, 0, +VECTOR_OFFSET}), MAXIMUM_LOD); // north-east
		neighbours[3] = getCell(Vector.add(p, new float[] {+VECTOR_OFFSET, 0, +VECTOR_OFFSET}), MAXIMUM_LOD); // south-east
		
		return neighbours;
	}
	
	public Quadtree[] getAdjacent(int index)
	{
		return getAdjacent(vertices.get(index));
	}
	
	public static boolean gradient(float[] a, float[] b, float[] c)
	{
		float ba = b[1] - a[1];
		float cb = c[1] - b[1];
		
		return Math.abs(cb - ba) < EPSILON;
	}
	
	public static int getVertexCapacity()
	{
		int c = (int) Math.pow(2, MAXIMUM_LOD) + 1;
		
		return c * c;
	}
	
	public static int getCellCapacity()
	{
		int c = (int) Math.pow(2, MAXIMUM_LOD);
		
		return c * c;
	}
	
	public long createBuffers()
	{
		long start = System.nanoTime();
		
		int capacity = getVertexCapacity();
		
		vBuffer = Buffers.newDirectFloatBuffer(capacity * 3);
		for(float[] vertex : vertices) vBuffer.put(vertex);
		
		nBuffer = Buffers.newDirectFloatBuffer(capacity * 3);
		for(float[] normal : normals) nBuffer.put(normal);
		
		tBuffer = Buffers.newDirectFloatBuffer(capacity * 2);
		for(float[] texCoord : texCoords) tBuffer.put(texCoord);
		
		cBuffer = Buffers.newDirectFloatBuffer(capacity * 3);
		for(float[] color : colors) cBuffer.put(color);
		
		iBuffer = Buffers.newDirectIntBuffer(getCellCapacity() * 4);
		for(int index : indices) iBuffer.put(index);
		
		indexCount = 4;
		
		return System.nanoTime() - start;
	}
	
	public void updateIndices(int lod)
	{
		iBuffer.position(0);
		indexCount = getIndices(lod);
	}
	
	public int getIndices(int lod)
	{
		int count = 0;
		
		if(isLeaf() || this.lod == lod)
		{
			// re-assign offsets for new index buffer
			offset = iBuffer.position();
			iBuffer.put(indices);
			count += 4;
		}
		else
		{
			count += north_west.getIndices(lod);
			count += north_east.getIndices(lod);
			count += south_west.getIndices(lod);
			count += south_east.getIndices(lod);
		}
		
		return count;
	}
	
	/**
	 * This method returns the index of a vertex <code>p</code> if it is stored by this
	 * quadtree's hierarchy, otherwise, an invalid index of -1 is returned.
	 */
	public int storesVertex(float[] p)
	{
		int index = pointOnCell(p);
		if(index != -1) return index;
		
		int nw, ne, sw, se;
		nw = ne = sw = se = -1;
		
		if(north_west != null && north_west.pointInCell(p)) nw = north_west.storesVertex(p); if(nw != -1) return nw;
		if(north_east != null && north_east.pointInCell(p)) ne = north_east.storesVertex(p); if(ne != -1) return ne;
		if(south_west != null && south_west.pointInCell(p)) sw = south_west.storesVertex(p); if(sw != -1) return sw;
		if(south_east != null && south_east.pointInCell(p)) se = south_east.storesVertex(p); if(se != -1) return se;
		
		return -1;
	}
	
	/**
	 * This method returns the index of a vertex <code>p</code> if it is equal to any of the four
	 * vertices stored by this quadtree, otherwise, an invalid index of -1 is returned. 
	 */
	public int pointOnCell(float[] p)
	{
		if(Vector.equal(p, vertices.get(indices[0]))) return indices[0];
		if(Vector.equal(p, vertices.get(indices[1]))) return indices[1];
		if(Vector.equal(p, vertices.get(indices[2]))) return indices[2];
		if(Vector.equal(p, vertices.get(indices[3]))) return indices[3];
		
		else return -1;
	}
	
	/**
	 * This method can be used to determine whether of not a given vertex 'p' is
	 * located within the implicit bounds of this quadtree.
	 */
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
	
	public void deallocateBuffers()
	{
		vBuffer = null;
		nBuffer = null;
		tBuffer = null;
		cBuffer = null;
		iBuffer = null;
		
		if(north_west != null) north_west.deallocateBuffers();
		if(north_east != null) north_east.deallocateBuffers();
		if(south_west != null) south_west.deallocateBuffers();
		if(south_east != null) south_east.deallocateBuffers();
	}
	
	/**
	 * This method splits the cell into four by halving it across both the horizontal
	 * and vertical centres. This is accomplished by extending the quadtree hierarchy
	 * with four children nodes.
	 * 
	 * @return <code>true</code> if the subdivision is possible based on the restriction
	 * set by the maximum level of detail.
	 */
	public boolean subdivide()
	{
		// do not subdivide if cell will exceed maximum lod
		if(lod + 1 > MAXIMUM_LOD) return false;
		
		vBuffer.limit(vBuffer.capacity());
		nBuffer.limit(nBuffer.capacity());
		tBuffer.limit(tBuffer.capacity());
		cBuffer.limit(cBuffer.capacity());
		
		float _x = vertices.get(indices[3])[0];
		float _z = vertices.get(indices[3])[2];
		float x_ = vertices.get(indices[1])[0];
		float z_ = vertices.get(indices[1])[2];
		
		float _x_ = (_x + x_) / 2; // horizontal centre of cell
		float _z_ = (_z + z_) / 2; // vertical centre of cell
		
		float[] vNorth  = {_x_, 0, _z }; vNorth [1] = getHeight(vNorth ); // north vertex
		float[] vEast   = { x_, 0, _z_}; vEast  [1] = getHeight(vEast  );
		float[] vSouth  = {_x_, 0,  z_}; vSouth [1] = getHeight(vSouth );
		float[] vWest   = {_x , 0, _z_}; vWest  [1] = getHeight(vWest  );
		float[] vCentre = {_x_, 0, _z_}; vCentre[1] = getHeight(vCentre);
		
		int north, east, south, west, centre; // the indices of the 5 new vertices
		north = east = south = west = centre = -1; // -1 means invalid
		
		/*
		 * Each iVertex variable acts as a placeholder to store whether or not the
		 * corresponding index is already stored within the entire quadtree hierarchy.
		 * If possible, indices are reused, otherwise a new index is created.
		 */
		int iNorth  = north  = root.storesVertex(vNorth ); 
		int iEast   = east   = root.storesVertex(vEast  ); 
		int iSouth  = south  = root.storesVertex(vSouth ); 
		int iWest   = west   = root.storesVertex(vWest  ); 
		int iCentre = centre = root.storesVertex(vCentre); 
		
		if(iNorth  == -1) { north  = vertices.size(); vertices.add(vNorth ); vBuffer.put(vNorth ); }
		if(iEast   == -1) { east   = vertices.size(); vertices.add(vEast  ); vBuffer.put(vEast  ); }
		if(iSouth  == -1) { south  = vertices.size(); vertices.add(vSouth ); vBuffer.put(vSouth ); }
		if(iWest   == -1) { west   = vertices.size(); vertices.add(vWest  ); vBuffer.put(vWest  ); }
		if(iCentre == -1) { centre = vertices.size(); vertices.add(vCentre); vBuffer.put(vCentre); }
		
		// set default color to white so that final color is sampled using the texture
		if(iNorth  == -1) { colors.add(RGB.WHITE_3F); cBuffer.put(RGB.WHITE_3F); }
		if(iEast   == -1) { colors.add(RGB.WHITE_3F); cBuffer.put(RGB.WHITE_3F); }
		if(iSouth  == -1) { colors.add(RGB.WHITE_3F); cBuffer.put(RGB.WHITE_3F); }
		if(iWest   == -1) { colors.add(RGB.WHITE_3F); cBuffer.put(RGB.WHITE_3F); }
		if(iCentre == -1) { colors.add(RGB.WHITE_3F); cBuffer.put(RGB.WHITE_3F); }
		
		// record original heights for use in color and deformation calculations 
		if(iNorth  == -1) { heights.add(vNorth [1]); }
		if(iEast   == -1) { heights.add(vEast  [1]); }
		if(iSouth  == -1) { heights.add(vSouth [1]); }
		if(iWest   == -1) { heights.add(vWest  [1]); }
		if(iCentre == -1) { heights.add(vCentre[1]); }

		if(enableTexture)
		{
			float _s = texCoords.get(indices[3])[0];
			float _t = texCoords.get(indices[3])[1];
			float s_ = texCoords.get(indices[1])[0];
			float t_ = texCoords.get(indices[1])[1];
			
			float _s_ = (_s + s_) / 2;
			float _t_ = (_t + t_) / 2;
			
			// if an index was invalid, a new texture coordinate must also be added 
			float[] tNorth  = {_s_, _t }; if(iNorth  == -1) { texCoords.add(tNorth ); tBuffer.put(tNorth ); };
			float[] tEast   = { s_, _t_}; if(iEast   == -1) { texCoords.add(tEast  ); tBuffer.put(tEast  ); };
			float[] tSouth  = {_s_,  t_}; if(iSouth  == -1) { texCoords.add(tSouth ); tBuffer.put(tSouth ); };
			float[] tWest   = {_s , _t_}; if(iWest   == -1) { texCoords.add(tWest  ); tBuffer.put(tWest  ); };
			float[] tCentre = {_s_, _t_}; if(iCentre == -1) { texCoords.add(tCentre); tBuffer.put(tCentre); };
		}

		// set children nodes; indices supplied with counter-clockwise winding starting at the bottom-left corner
		north_west = new Quadtree(root, lod + 1, new int[] {west, centre, north, indices[3]}, enableTexture);
		north_east = new Quadtree(root, lod + 1, new int[] {centre, east, indices[2], north}, enableTexture);
		south_west = new Quadtree(root, lod + 1, new int[] {indices[0], south, centre, west}, enableTexture);
		south_east = new Quadtree(root, lod + 1, new int[] {south, indices[1], east, centre}, enableTexture);
		
		if(enableShading)
		{
			float[] nNorth  = getNormal(north ); if(iNorth  == -1) { normals.add(nNorth ); nBuffer.put(nNorth ); };
			float[] nEast   = getNormal(east  ); if(iEast   == -1) { normals.add(nEast  ); nBuffer.put(nEast  ); };
			float[] nSouth  = getNormal(south ); if(iSouth  == -1) { normals.add(nSouth ); nBuffer.put(nSouth ); };
			float[] nWest   = getNormal(west  ); if(iWest   == -1) { normals.add(nWest  ); nBuffer.put(nWest  ); };
			float[] nCentre = getNormal(centre); if(iCentre == -1) { normals.add(nCentre); nBuffer.put(nCentre); };
		}
		
		if(lod < root.detail)
		{
			int position = iBuffer.position();
			iBuffer.position(offset); // use offset to overwrite parent cell indices
			
			north_west.offset = offset; iBuffer.put(north_west.indices);
			iBuffer.position(position); // set position to end of buffer
			
			north_east.offset = iBuffer.position(); iBuffer.put(north_east.indices);
			south_west.offset = iBuffer.position(); iBuffer.put(south_west.indices);
			south_east.offset = iBuffer.position(); iBuffer.put(south_east.indices);
			
			root.indexCount += 12; // one cell (4 indices) is replaced with four (16) 16 - 4 = 12
		}
		
		// subdivide was successful
		return true;
	}
	
	public float[] getNormal()
	{
		float[] p1 = vertices.get(indices[0]);
		float[] p2 = vertices.get(indices[1]);
		float[] p3 = vertices.get(indices[3]);
		
		return Vector.normal(p1, p2, p3);
	}
	
	public float[] getNormal(int index)
	{
		Quadtree[] cells = getAdjacent(index);
		
		List<float[]> normals = new ArrayList<float[]>();
		
		for(Quadtree cell : cells)
			if(cell != null) normals.add(cell.getNormal());
		
		return Vector.average(normals);
	}
	
	public void increaseDetail()
	{
		if(detail < Quadtree.MAXIMUM_LOD) detail++;
		updateIndices(detail); 
	}
	
	public void decreaseDetail()
	{
		if(detail > 0) detail--;
		updateIndices(detail); 
	}
	
	/**
	 * This method splits the cell into four by halving it across both the horizontal
	 * and vertical centres. If the argument <code>i</code> is greater than 0, the
	 * algorithm will be applied recursively to the four new cells until the original
	 * cell has been split into <code>2</code><sup><code>2i</code></sup> cells.
	 */
	public boolean subdivide(int i)
	{
		boolean divisible = subdivide();
		
		i--;
		
		if(i > 0)
		{
			north_west.subdivide(i);
			north_east.subdivide(i);
			south_west.subdivide(i);
			south_east.subdivide(i);
		}

		return divisible;
	}
	
	public void subdivide(int i, Random generator)
	{
		subdivide();
		
		i--;
		
		if(i > 0)
		{
			if(generator.nextFloat() > 0.3) north_west.subdivide(i, generator);
			if(generator.nextFloat() > 0.3) north_east.subdivide(i, generator);
			if(generator.nextFloat() > 0.3) south_west.subdivide(i, generator);
			if(generator.nextFloat() > 0.3) south_east.subdivide(i, generator);
		}
	}
	
	/**
	 * This method traverses the quadtree's hierarchy using depth-first search to the level
	 * specified by the <code>lod</code> argument and returns a node that contains the vertex
	 * <code>p</code>.
	 */
	public Quadtree getCell(float[] p, int lod)
	{
		if((isLeaf() || this.lod == lod) && pointInCell(p)) return this;
		
		if(north_west != null && north_west.pointInCell(p)) return north_west.getCell(p, lod);
		if(north_east != null && north_east.pointInCell(p)) return north_east.getCell(p, lod);
		if(south_west != null && south_west.pointInCell(p)) return south_west.getCell(p, lod);
		if(south_east != null && south_east.pointInCell(p)) return south_east.getCell(p, lod);
		
		return null;
	}
	
	// perform bilinear interpolate to get the height of the quadtree at a point 'p'
	public float getHeight(float[] p)
	{	
		float x = p[0];
		float z = p[2];
		
		float x1 = vertices.get(indices[3])[0];
		float z1 = vertices.get(indices[3])[2];
		float x2 = vertices.get(indices[1])[0];
		float z2 = vertices.get(indices[1])[2];
		
		float q11 = root.malleable ? vertices.get(indices[3])[1] : heights.get(indices[3]);
		float q12 = root.malleable ? vertices.get(indices[0])[1] : heights.get(indices[0]);
		float q21 = root.malleable ? vertices.get(indices[2])[1] : heights.get(indices[2]);
		float q22 = root.malleable ? vertices.get(indices[1])[1] : heights.get(indices[1]);

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
	
	public void rippleSurface()
	{
		Random generator = new Random();
		
		int position = vBuffer.position();
		
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] vertex = vertices.get(i);
			
			     if(vertex[1] >  MAX_RIPPLE + height) heights.set(i,  MAX_RIPPLE + height);
			else if(vertex[1] < -MAX_RIPPLE + height) heights.set(i, -MAX_RIPPLE + height);
			
			vertex[1] += (heights.get(i) > height) ? -0.01f : 0.01f;
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);	
		}
		
		vBuffer.position(position);
	}
	
	public void offsetHeights(float trough)
	{
		Random generator = new Random();
		
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] vertex = vertices.get(i);
			float depression = -generator.nextFloat() * trough;
			
			vertex[1] += depression * (generator.nextBoolean() ? 1 : -1);
//			float[] color = gradient.interpolate((heights.get(i) - vertex[1]) / MAX_TROUGH);
//			colors.set(i, color);
			
			int position = cBuffer.position();
//			cBuffer.position(i * 3); cBuffer.put(color);
//			cBuffer.position(position);
				
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);
			vBuffer.position(position);
		}
		
		setHeights();
	}

	/**
	 * This method executes a hill-raising algorithm for a number of iterations
	 * to deform the geometric surface stored by the quadtree.
	 */
	public void setHeights(int iterations)
	{
		Random generator = new Random();
		
		int x, z;
		
		float _x = vertices.get(root.indices[3])[0]; // left-most x-coordinate
		float _z = vertices.get(root.indices[3])[2]; // bottom-most z-coordinate
		
		float length = root.getLength();
		
		for (int i = 0; i < iterations; i++)
		{
			x = (int) (generator.nextDouble() * length);
			z = (int) (generator.nextDouble() * length);

			float radius = MIN_RADIUS + generator.nextFloat() * (MAX_RADIUS - MIN_RADIUS);
			float peak = generator.nextFloat() * HILL_INC;
				
			// select random point within the boundaries of the quadtree
			deformAll(new float[] {_x + x, 0, _z + z}, radius, peak);
		}
		
		setHeights();
	}
	
	public void setHeights(Quadtree tree)
	{
		int position = vBuffer.position();
		
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] vertex = vertices.get(i);
			
			Quadtree cell = tree.getCell(vertex, getMaximumLOD());
			vertex[1] = cell.getHeight(vertex);
			
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);
			
			heights.set(i, vertex[1]);
		}
		
		vBuffer.position(position);
	}

	public void setHeights()
	{
		for(int i = 0; i < vertices.size(); i++) heights.set(i, vertices.get(i)[1]);
	}
	
	public float[] getVerticalRange()
	{
		float min = vertices.get(0)[1];
		float max = min;
		
		for(float[] vertex : vertices)
		{
			float h = vertex[1];
			
			if(h < min) min = h;
			if(h > max) max = h; 
		}
		
		return new float[] {min, max};
	}
	
	public void resetHeights()
	{
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] vertex = vertices.get(i);
			vertex[1] = heights.get(i);
			
			colors.set(i, RGB.WHITE_3F);
			
			int position = vBuffer.position();
				
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);
			vBuffer.position(position);
				
			cBuffer.position(i * 3); cBuffer.put(RGB.WHITE_3F);
			cBuffer.position(position);
		}
	}
	
	public Set<Integer> getIndices(float[][] vertices)
	{
		Quadtree region = getRegion(vertices);
		
		Set<Integer> _indices = new HashSet<Integer>();
		region.getIndices( _indices);
		
		return _indices;
	}
	
	public void getIndices(Set<Integer> _indices)
	{
		if(isLeaf())
		{
			_indices.add(indices[0]);
			_indices.add(indices[1]);
			_indices.add(indices[2]);
			_indices.add(indices[3]);
		}
		else
		{
			north_west.getIndices(_indices);
			north_east.getIndices(_indices);
			south_west.getIndices(_indices);
			south_east.getIndices(_indices);
		}
	}
	
	public Quadtree getRegion(float[][] vertices)
	{
		if(north_west != null && north_west.pointsInCell(vertices)) return north_west.getRegion(vertices);
		if(north_east != null && north_east.pointsInCell(vertices)) return north_east.getRegion(vertices);
		if(south_west != null && south_west.pointsInCell(vertices)) return south_west.getRegion(vertices);
		if(south_east != null && south_east.pointsInCell(vertices)) return south_east.getRegion(vertices);
		
		return this;
	}
	
	public boolean pointsInCell(float[][] vertices)
	{
		for(float[] vertex : vertices)
			if(!pointInCell(vertex)) return false;
		
		return true;
	}
	
	/**
	 * This method creates a hill in the geometric surface stored by the quadtree
	 * at the point <code>p</code>. The extent of the hill is determined by the
	 * argument <code>radius</code>, while the height of the hill is determined by
	 * the argument <code>peak</code>. It should be noted that if <code>peak</code>
	 * is negative, a depression is created instead of a hill.
	 */
	public long deformAll(float[] p, float radius, float peak)
	{		
		long start = System.nanoTime();
		
		for(int i = 0; i < vertices.size(); i++)
			deformVertex(i, p, radius, peak);
		
		return System.nanoTime() - start;
	}
	
	public void deformVertex(int i, float[] p, float radius, float peak)
	{
		float[] vertex = vertices.get(i);
		
		float x = Math.abs(vertex[0] - p[0]); if(x > radius) return;
		float z = Math.abs(vertex[2] - p[2]); if(z > radius) return;
		
		// calculate distance from vertex to centre of deformation
		double d = Math.sqrt(x * x + z * z);
		
		if(d <= radius)
		{
			// ensure the deformation will not cause cracks
			repairCrack(i);
			
			switch(falloff)
			{
				case LINEAR: vertex[1] += peak * (1 - (d / radius)); break;
				case SMOOTH: vertex[1] += peak * 0.5f * (Math.cos(d / radius * Math.PI) + 1); break;
			}
			
			if(vertex[1] < heights.get(i) - MAX_TROUGH)
			   vertex[1] = heights.get(i) - MAX_TROUGH;
			
			if(vertex[1] > MAX_HEIGHT) vertex[1] = MAX_HEIGHT;
			
			updateBuffers(i, vertex);
		}
	}

	private void updateBuffers(int i, float[] vertex)
	{
		int position = vBuffer.position();
		
		if(malleable)
		{
			vBuffer.position(i * 3 + 1); vBuffer.put(vertex[1]);
			vBuffer.position(position);
		}
		
		if(vertex[1] < heights.get(i))
		{
			float[] color = gradient.interpolate((heights.get(i) - vertex[1]) / MAX_TROUGH);
			colors.set(i, color);
			
			cBuffer.position(i * 3); cBuffer.put(color);
			cBuffer.position(position);
		}
		
		if(malleable)
		{
			Set<Integer> indices = getSurface(i);
				
			for(Integer index : indices)
			{
				float[] normal = getNormal(index);
				normals.set(index, normal);
				
				nBuffer.position(index * 3); nBuffer.put(normal);
			}
			
			nBuffer.position(position);
		}
	}
	
	public Set<Integer> getSurface(int i)
	{
		Set<Integer> indices = new HashSet<Integer>();
		
		Quadtree[] cells = getAdjacent(i);
		
		for(Quadtree cell : cells)
		{
			if(cell != null)
			{
				indices.add(cell.indices[0]);
				indices.add(cell.indices[1]);
				indices.add(cell.indices[2]);
				indices.add(cell.indices[3]);
			}
		}
		
		return indices;
	}
	
	public void recalculateNormals()
	{
		int position = nBuffer.position();
		
		for(int i = 0; i < normals.size(); i++)
		{	
			float[] normal = getNormal(i);
			
			normals.set(i, normal);
			nBuffer.position(i * 3); nBuffer.put(normal);
		}
		
		nBuffer.position(position);
	}
	
	public Set<Integer> getIndices(float[] p, float radius)
	{	
		Set<Integer> indices = new HashSet<Integer>();
		
		float increment = (float) (root.getLength() / Math.pow(2, MAXIMUM_LOD));
		int steps = (int) Math.ceil((2 * radius) / increment);
		
		float[] v = Vector.add(p, new float[] {-radius, 0, -radius});
		
		for(int i = 0; i <= steps; i++)
		{
			for(int j = 0; j <= steps; j++)
			{
				v[0] += increment;
				
				Quadtree cell = getCell(v, MAXIMUM_LOD);
				if(cell == null) continue;
				int[] _indices = cell.indices;
				
				indices.add(_indices[0]);
				indices.add(_indices[1]);
				indices.add(_indices[2]);
				indices.add(_indices[3]);
			}
			
			v[0] = p[0] - radius;
			v[2] += increment;
		}
		
		return indices;
	}
	
	public long deform(float[] p, float radius, float peak)
	{		
		long start = System.nanoTime();
		
		int size = vertices.size();
		
		Set<Integer> indices = getIndices(p, radius);
		
		for(int i : indices)
			deformVertex(i, p, radius, peak);
		
		while(vertices.size() - size > 0)
		{
			size = vertices.size();
			
			for(int i = size; i < vertices.size(); i++)
				deformVertex(i, p, radius, peak);
		}
		
		return System.nanoTime() - start;
	}
	
	public void setGradient(Gradient gradient)
	{
		this.gradient = gradient;
		
		int position = cBuffer.position();
		
		for(int i = 0; i < vertices.size(); i++)
		{	
			float[] vertex = vertices.get(i);
			float[] color = gradient.interpolate((heights.get(i) - vertex[1]) / MAX_TROUGH);
				
			cBuffer.position(i * 3); cBuffer.put(color);
	
			colors.set(i, color);
		}
		
		cBuffer.position(position);
	}
	
	public void scaleTexture(float scale)
	{
		float length = root.getLength();
		
		float _x = root.vertices.get(indices[3])[0];
		float _z = root.vertices.get(indices[3])[2];
		
		int position = tBuffer.position();
		
		for(int i = 0; i < vertices.size(); i++)
		{	
			float[] vertex = vertices.get(i);
			
			float s = Math.abs(vertex[0] - _x) / length;
			float t = Math.abs(vertex[2] - _z) / length;
			
			float[] texCoord = {s * scale, t * scale};
				
			tBuffer.position(i * 2);
			tBuffer.put(texCoord);
				
			texCoords.set(i, texCoord);
		}
		
		tBuffer.position(position);
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
	
	// a node is classed as a leaf if all of it's children are null
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
	
	public int cellCount() { return indexCount / 4; }
	
	public int vertexCount() { return vertices.size(); }
	
	public void render(GL2 gl)
	{
		if(solid)
		{
			if(reliefMap) renderElevation(gl);
			else          renderGeometry(gl);
		}
		
		if(frame   ) renderWireframe(gl);
		if(vNormals) renderNormals(gl, true, 1);
	}
	
	public void renderAlpha(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_BLEND);
		
		gl.glColor4f(0.75f, 0.75f, 0.75f, 0.20f);
		
		renderGeometry(gl);
		rippleSurface();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LIGHTING);
		
		gl.glColor4f(1, 1, 1, 1);
	}
	
	public void renderGeometry(GL2 gl)
	{
		Light.globalSpecular(gl, specular);
		
		if(!enableShading) gl.glDisable(GL2.GL_LIGHTING);
		
		if(!enableTexture) gl.glDisable(GL2.GL_TEXTURE_2D);
		else               gl.glEnable (GL2.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		if(enableShading ) gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		if(enableColoring) gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		if(enableTexture ) gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		vBuffer.flip(); // read data from start of buffer
		nBuffer.flip();
		cBuffer.flip();
		tBuffer.flip();
		iBuffer.flip();
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		if(enableShading ) gl.glNormalPointer(   GL2.GL_FLOAT, 0, nBuffer);
		if(enableColoring) gl.glColorPointer (3, GL2.GL_FLOAT, 0, cBuffer);
		if(enableTexture )
		{
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, tBuffer);
			texture.bind(gl);
		}
		
		gl.glDrawElements(GL2.GL_QUADS, indexCount, GL2.GL_UNSIGNED_INT, iBuffer);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		if(enableShading ) gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		if(enableColoring) gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		if(enableTexture ) gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		vBuffer.position(vBuffer.limit()); vBuffer.limit(vBuffer.capacity());
		nBuffer.position(nBuffer.limit()); nBuffer.limit(nBuffer.capacity());
		tBuffer.position(tBuffer.limit()); tBuffer.limit(tBuffer.capacity());
		cBuffer.position(cBuffer.limit()); cBuffer.limit(cBuffer.capacity());
		iBuffer.position(iBuffer.limit()); iBuffer.limit(iBuffer.capacity());
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_TEXTURE_2D);	
		
		Light.globalSpecular(gl, new float[] {1, 1, 1, 1});
	}
	
	public void renderWireframe(GL2 gl)
	{
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
		gl.glTranslatef(0, EPSILON, 0);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glColor3f(lineColor[0], lineColor[1], lineColor[2]);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		
		vBuffer.flip();
		iBuffer.flip();
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		
		gl.glDrawElements(GL2.GL_QUADS, indexCount, GL2.GL_UNSIGNED_INT, iBuffer);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);	
		gl.glEnable(GL2.GL_LIGHTING);
		
		vBuffer.position(vBuffer.limit()); vBuffer.limit(vBuffer.capacity());
		iBuffer.position(iBuffer.limit()); iBuffer.limit(iBuffer.capacity());
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public void renderElevation(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		
		FloatBuffer _colors = Buffers.newDirectFloatBuffer(colors.size() * 3);
		
		for(float[] vertex : vertices)
		{
			float[] color = gradient.interpolate(1 - (vertex[1] / MAX_HEIGHT));
			_colors.put(color);
		}
		_colors.position(0);
		
		vBuffer.flip();
		iBuffer.flip();
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		gl.glColorPointer (3, GL2.GL_FLOAT, 0, _colors);
		
		gl.glDrawElements(GL2.GL_QUADS, indexCount, GL2.GL_UNSIGNED_INT, iBuffer);
		
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		
		vBuffer.position(vBuffer.limit()); vBuffer.limit(vBuffer.capacity());
		iBuffer.position(iBuffer.limit()); iBuffer.limit(iBuffer.capacity());
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public void renderNormals(GL2 gl, boolean smooth, float scale)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		float[] c = RGB.BLUE;
		
		gl.glColor3f(c[0]/255, c[1]/255, c[2]/255);

		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		
		gl.glBegin(GL2.GL_LINES);
		
		for(int i = 0; i < vertices.size(); i++)
		{
			float[] p1 = vertices.get(i);
			float[] p2 = Vector.add(p1, Vector.multiply(normals.get(i), scale));
			
			gl.glVertex3f(p1[0], p1[1], p1[2]);
			gl.glVertex3f(p2[0], p2[1], p2[2]);
		}
		gl.glEnd();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public Set<Integer> selected = new HashSet<Integer>();
	
	public void renderSelected(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glColor3f(1, 0, 0);

		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glPointSize(10);
		gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, new float[] {1, 0, 0}, 0);
		
		gl.glBegin(GL2.GL_POINTS);
		
		for(int i : selected)
		{
			float[] point = vertices.get(i);
			gl.glVertex3f(point[0], point[1], point[2]);
		}
		gl.glEnd();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
		
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public enum FallOff
	{
		LINEAR,
		SMOOTH;
		
		public static FallOff cycle(FallOff mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}
}

