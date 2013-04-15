package bates.jamie.graphics.entity;

import static bates.jamie.graphics.util.Renderer.displayLines;
import static bates.jamie.graphics.util.Renderer.displayWireframeObject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.util.Gradient;
import bates.jamie.graphics.util.MultiTexFace;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vector;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

public class Terrain
{
	private static final float WORLD_LENGTH = 420;
	
	private static final float PEAK_INC = 0.15f;
	private static final float HILL_INC = 3.0f;
	
	private float peak_inc = PEAK_INC;
	private float hill_inc = HILL_INC;
	
	private static final int MIN_RADIUS =  4;
	private static final int MAX_RADIUS = 12;
	
	private int min_radius = MIN_RADIUS;
	private int max_radius = MAX_RADIUS;
	
	private static final float MAX_HEIGHT = 60.0f;
	
	private static final int TEXTURE_LENGTH = 4;
	
	public static float sx = 6.0f;
	public static float sy = 3.0f;
	public static float sz = 6.0f;
	
	private static final int LIGHT_MAP_SIZE = 128;
	
	private boolean createLightMap = false;
	
	public int terrainList;
	public int renderMode = 1;
	
	public float[][] heights;
	public int length;
	
	float[][] vertices;
	float[][] normals;
	float[][] texCoords;
	
	public List<Texture > textures  = new ArrayList<Texture >();
	public List<Gradient> gradients = new ArrayList<Gradient>();
	
	public int  textureID = 0;
	public int gradientID = 0;
	
	public Texture baseTexture;
	
	float[][] q = new float[16][3];
	float[][] r = new float[12][3];
	
	int qBuffer = 0;
	int rBuffer = 0;
	
	Model model;
	
	public HashMap<String, Quadtree> trees = new HashMap<String, Quadtree>();
	public Quadtree tree;
	
	public boolean enableQuadtree = false;
	public boolean enableWater = false;
	
	public Terrain(GL2 gl, int length, int i)
	{	
		setHeights(length, i);
		
		this.length = length;
		
		sx = sz = WORLD_LENGTH / length;
		
		loadTextures(gl);
		
		if(createLightMap) createLightMap();
		
		createGeometry(TEXTURE_LENGTH);
		
		displayList(gl);
	}
	
	public Terrain(GL2 gl, int length, int i, int r0, int r1, float p, float h)
	{
		min_radius = r0;
		max_radius = r1;
		
		peak_inc = p;
		hill_inc = h;
		
		loadTextures(gl);
		
		System.out.println("Terrain:\n{");
		
		if(!enableQuadtree)
		{
			long start = System.nanoTime();
			
			setHeights(length, i);
			
			long end = System.nanoTime();
			
			System.out.printf("\tDeformation: %.3f ms\n", (end - start) / 1E6);
			
			this.length = length;
			
			sx = sz = WORLD_LENGTH / length;
			
			if(createLightMap) createLightMap();
			
			start = System.currentTimeMillis();
			
			createGeometry(TEXTURE_LENGTH);
			
			end = System.currentTimeMillis();
			
			System.out.println("\tGeometry: " + (System.currentTimeMillis() - start) + " ms\n}");
			
			displayList(gl);
			
			toModel();
		}
		
		generateQuadtree();
	}
	
	public void generateQuadtree()
	{	
		gradients.add(Gradient.MUD);
		gradients.add(Gradient.GRAYSCALE);
		
		
		Quadtree base = new Quadtree(210, 32, textures.get(1), 7);
		base.setHeights(1000);
		base.specular = new float[] {0.3f, 0.3f, 0.3f, 1};
		
		tree = base;
		
		
		Quadtree pond = new Quadtree(210, 9);
		pond.enableShading  = false;
		pond.enableColoring = false;
		pond.enableBlending = true;
		pond.offsetHeights(0.5f);
		
		
		Quadtree road = new Quadtree(210, 32, textures.get(3), 7);
		road.setHeights(1000);
		road.malleable = false;
		road.specular = new float[] {1, 1, 1, 1};
		
		
		trees.put("Base", base);
		trees.put("Pond", pond);
		trees.put("Road", road);
		
	}
	
	public void selectQuadtree(String key)
	{
		tree = trees.get(key);
	}
	
	public void toModel()
	{
		long start = System.currentTimeMillis();
		
		int[] indices = new int[length * length * 4];
		
		List<float[]> _vertices  = new ArrayList<float[]>();
		List<float[]> _texCoords = new ArrayList<float[]>();
		
		int index = 0;
		
		for(int i = 0; i < indices.length; i++)
		{
			float[] vertex   = vertices[i];
			float[] texCoord = texCoords[i];
			
			boolean equal = false;
			
			for(int j = 0; j < _vertices.size(); j++)
			{
				if(Vector.equal(vertex, _vertices.get(j)) &&
				   Vector.equal(texCoord, _texCoords.get(j)))
				{
					equal = true;
					indices[i] = j;
	                break;
				}
			}
		
			if(!equal)
			{
				indices[i] = index;
				index++;
				
		        _vertices.add(vertex);
		        _texCoords.add(texCoord);
			}
		}
		
		for(int i = 0; i < _vertices.size(); i++)
		{
			float[] vertex = _vertices.get(i);
			_vertices.set(i, new float[] {vertex[0] * sx, vertex[1] * sy, vertex[2] * sz});
		}
		
		model = new Model(_vertices, _texCoords, indices, indices, baseTexture, 4);
		
		System.out.println("Terrain Model Indexed: " + (System.currentTimeMillis() - start) + " ms");
	}

	public Terrain(GL2 gl, String fileName)
	{
		importMap(fileName);
		
		loadTextures(gl);
		
		if(createLightMap) createLightMap();
		
		createGeometry(TEXTURE_LENGTH);
		
		displayList(gl);
	}

	/**
	 * This method uses a hill-raising algorithm to randomly generate the heights
	 * of the map; this map is of the length passed as a parameter
	 */
	private void setHeights(int length, int iterations)
	{
		float[][] heights = new float[length + 1][length + 1];
		
		Random generator = new Random();
		
		int x, z;
		
		double[][] distances = new double[max_radius + 1][max_radius + 1];
		
		for(int i = 0; i <= max_radius; i++)
			for(int j = 0; j <= max_radius ; j++)
				distances[i][j] = Math.sqrt((i * i) + (j * j));
		
		for (int i = 0; i < iterations; i++)
		{
			x = (int) (generator.nextDouble() * length);
			z = (int) (generator.nextDouble() * length);

			if(generator.nextBoolean())
			{
				 increaseRadius(x, z, 2, peak_inc, heights, length, distances);
			}
			else increaseRadius(x, z, generator, heights, length, distances);
		}
		
		this.heights = heights;
	}
	
	private void increaseRadius(int x, int z, int radius, float peak, float[][] heights, int length, double[][] distances)
	{
		/*
		 * calculate extent of the deformation mask in terms of the top-left (_x, _z),
		 * bottom-right (x_, z_), and centre (x, z) vertices 
		 */
		int _x = (x - radius) < 0 ? 0 : x - radius;
		int _z = (z - radius) < 0 ? 0 : z - radius;
		int x_ = (x + radius) > length ? length : x + radius;
		int z_ = (z + radius) > length ? length : z + radius;
		
		float offset = 0.5f / radius;
		
		for(int a = _x; a <= x_; a++)
		{
			for(int b = _z; b <= z_; b++)
			{
				int _x_ = Math.abs(x - a);
				int _z_ = Math.abs(z - b);
				
				double d = distances[_x_][_z_];
				d = (d > radius) ? radius : d;
				
				if(a == x && b == z) heights[a][b] += peak * (1 - offset);
				else heights[a][b] += peak * (1 - (d / radius));
			}
		}
	}
	
	private void increaseRadius(int x, int z, Random generator, float[][] heights, int length, double[][] distances)
	{	
		int radius = min_radius + generator.nextInt(max_radius - min_radius + 1);
		float peak = generator.nextFloat() * hill_inc;
		
		increaseRadius(x, z, radius, peak, heights, length, distances);
	}

	private void importMap(String fileName)
	{
		try
		{
			BufferedImage map = ImageIO.read(new File("tex/" + fileName));
			
			int width  = map.getWidth();
			int height = map.getHeight();
			
			float[][] heights = new float[width + 1][height + 1];
			
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					float intensity = RGB.getIntensity(map.getRGB(i, j));
					heights[i][j] = intensity * MAX_HEIGHT;
				}
			}
			
			this.heights = heights;
			this.length = width; //TODO works with square images only
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	private void loadTextures(GL2 gl)
	{
		try
		{
			textures.add(TextureLoader.load(gl, "tex/grass.jpg"  ));
			textures.add(TextureLoader.load(gl, "tex/sand.jpg"   ));
			textures.add(TextureLoader.load(gl, "tex/snow.jpg"   ));
			textures.add(TextureLoader.load(gl, "tex/cobbles.jpg"));
			
			baseTexture = textures.get(0);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private void displayList(GL2 gl)
	{
		terrainList = gl.glGenLists(1);
		gl.glNewList(terrainList, GL2.GL_COMPILE);
		prerender(gl);
	    gl.glEndList();
	}
	
	public void export()
	{
		float max_height = getMaxHeight();
		
		BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < length; j++)
			{
				float height = heights[i][j] / max_height;
				int intensity = (int) (height * 255);
				Color color = new Color(intensity, intensity, intensity);
				
				image.setRGB(i, j, color.getRGB());
			}
		}
		
		try { ImageIO.write(image, "jpg", new File("tex/heightMap.jpg")); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public float getMaxHeight()
	{
		float max_height = 0;
		
		for(int i = 0; i < heights.length; i++)
			for(int j = 0; j < heights[0].length; j++)
				if(heights[i][j] > max_height) max_height = heights[i][j];
		
		return max_height;
	}
	
	public void displayMap()
	{
		float max_height = getMaxHeight();
		
		BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < length; j++)
			{
				float height = heights[i][j] / max_height;
				int intensity = (int) (height * 255);
				Color color = new Color(intensity, intensity, intensity);
				
				image.setRGB(i, j, color.getRGB());
			}
		}
		
		JFrame record = new JFrame();
		
		int height = image.getHeight();
		int width = image.getWidth();
		
		ImageIcon _image = new ImageIcon(image);
		JLabel label = new JLabel(_image);
		
		record.setPreferredSize(new Dimension(width, height));
		record.add(label);
		record.pack();
		record.setVisible(true);
	}
	
	/**
	 * This method calculates the height of the map at the point p passed as a
	 * parameter; p is translated to a cell on the map and bilinear interpolation
	 * is used to estimate the result based on the heights of its four corners.
	 */
	public float getHeight(float[] p)
	{
		float x = (p[0] / sx) + length / 2; //x-coordinate translated to map scale
		float z = (p[2] / sz) + length / 2; //z-coordinate translated to map scale
		
		int x1 = (int) Math.floor(x);
		int z1 = (int) Math.floor(z);
		int x2 = x1 + 1;
		int z2 = z1 + 1;
		
		//checks whether the point p can be sampled within the bounds of the array
		if(x1 < 0 || z1 < 0 || x1 >= length || z1 >= length) return 0;
		
		float q11 = heights[x1][z1]; //Q11 = (x1, z1), one of the known points
		float q12 = heights[x1][z2];
		float q21 = heights[x2][z1];
		float q22 = heights[x2][z2];

		float _x1 = (x1 - length / 2); // x1 translated to world coordinates
		float _z1 = (z1 - length / 2);
		float  _x = ( x - length / 2);
		float  _z = ( z - length / 2);
		
		/*
		 * The bilinear interpolation performed below is optimised for a terrain made
		 * of quads equal in x and z length, meaning that (x2 - x1) = (z2 - z1) = 1.
		 * It also means that intermediate heights r1 and r2 can be calculated for use
		 * in debugging visuals without requiring excessive variables
		 */
		float r1 = (x2 - x) * q11 + (x - x1) * q21;
		float r2 = (x2 - x) * q12 + (x - x1) * q22;
		
		float h = r1 * (z2 -  z) + r2 * (z  - z1);
		
	    q[qBuffer    ] = new float[] {_x1,     q11, _z1    };
		q[qBuffer + 1] = new float[] {_x1 + 1, q21, _z1    };
		q[qBuffer + 3] = new float[] {_x1,     q12, _z1 + 1};
		q[qBuffer + 2] = new float[] {_x1 + 1, q22, _z1 + 1};
		
		qBuffer += 4;
		if(qBuffer == 16) qBuffer = 0;
		
		r[rBuffer    ] = new float[] {_x, r1, _z1    };
		r[rBuffer + 1] = new float[] {_x,  h,  _z    };
		r[rBuffer + 2] = new float[] {_x, r2, _z1 + 1};
		
		rBuffer += 3;
		if(rBuffer == 12) rBuffer = 0;
		
		return h * sy;
	}
	
	public void createGeometry(int textureLength)
	{
		List<MultiTexFace> faces = new ArrayList<MultiTexFace>();
		
		vertices  = createVertices();
		normals   = createNormals(vertices);
		texCoords = createTexCoords(vertices, textureLength);
	}
	
	public float[][] createVertices()
	{
		float[][] vertices = new float[length * length * 4][3];
		
		int i = 0;
		
		for(int x = 0; x <= length - 1; x++)
		{
			for(int z = 0; z <= length - 1; z++)
			{
				createQuad(vertices, i, x, z);
				i += 4;
			}
		}
		
		return vertices;
	}
	
	public void createQuad(float[][] vertices, int i, int x, int z)
	{		
		float _x = x - length / 2;
		float _z = z - length / 2;
		
		//construct vertices anti-clockwise from bottom-left
		vertices[i    ] = new float[] {_x,     heights[x    ][z + 1], _z + 1}; //bottom-left
		vertices[i + 1] = new float[] {_x + 1, heights[x + 1][z + 1], _z + 1}; //bottom-right
		vertices[i + 2] = new float[] {_x + 1, heights[x + 1][z    ], _z    }; //top-right
		vertices[i + 3] = new float[] {_x,     heights[x    ][z    ], _z    }; //top-left
	}
	
	public float[][] createNormals(float[][] vertices)
	{	
		float[][] normals = new float[length * length * 4][3];
		
		for(int i = 0; i < vertices.length; i += 4)
		{
			normals[i    ] = Vector.normal(vertices[i    ], vertices[i + 1], vertices[i + 3]);
			normals[i + 1] = Vector.normal(vertices[i + 1], vertices[i    ], vertices[i + 2]);
			normals[i + 2] = Vector.normal(vertices[i + 2], vertices[i + 1], vertices[i + 3]);
			normals[i + 3] = Vector.normal(vertices[i + 3], vertices[i + 2], vertices[i    ]);
		}
		
		return normals;
	}
	
	public int coordToIndex(int x, int z)
	{
		int index = 3 + (4 * x) + (4 * z * length);
		if(x == length && z == length) index -= 6 + (4 * length);
		else if(x == length) index -= 5;
		else if(z == length) index -= 3 + (4 * length);
		
		return index;
	}
	
	public float[][] createTexCoords(float[][] vertices, int tLength)
	{	
		int n = vertices.length;
		float[][] texCoords = new float[n][2];
		
		for (int i = 0; i < n; i += 4)
		{
			createTexturedQuad(texCoords, i, tLength, vertices);
		}
		
		return texCoords;
	}
	
	public void scaleTexCoords(int tLength)
	{
		int n = vertices.length;
		float[][] _texCoords = new float[n][2];
		
		for (int i = 0; i < n; i += 4)
		{
			createTexturedQuad(_texCoords, i, tLength, vertices);
		}
		
		texCoords = _texCoords;
	}
	
	private void createTexturedQuad(float[][] texCoords, int i, int tLength, float[][] vertices)
	{
		texCoords[i] = makeTexCoord(vertices[i], tLength);
		
		for(int j = 1; j < 4; j++)
		{
			texCoords[i + j] = makeTexCoord(vertices[i + j], tLength);
		}
	}
	
	private float[] makeTexCoord(float[] vertex, int texLen)
	{
		float s, t;
		
		float x = vertex[0] + length / 2;
		float z = length / 2 - vertex[2];
			
		s = x / texLen;
		t = z / texLen;
		
		return new float[] {s, t};
	}
	
	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_EQUALS       : tree.increaseDetail(); break;
			case KeyEvent.VK_MINUS        : tree.decreaseDetail(); break;
			case KeyEvent.VK_OPEN_BRACKET : tree.decimateAll() ; tree.updateIndices(tree.detail); break;
			case KeyEvent.VK_CLOSE_BRACKET: tree.subdivideAll(); break;
			
			case KeyEvent.VK_QUOTE      : trees.get("Pond").decreaseDetail(); break;
			case KeyEvent.VK_NUMBER_SIGN: trees.get("Pond").increaseDetail(); break;
			
			case KeyEvent.VK_COMMA : enableQuadtree = !enableQuadtree; break;
			
			case KeyEvent.VK_PERIOD:
			{
				break;
			}
			
			case KeyEvent.VK_P:  tree.flatten(); break;
			
			case KeyEvent.VK_J:
			{
				textureID++;
				textureID %= textures.size();
				tree.texture = textures.get(textureID);
				break;
			}
			
			case KeyEvent.VK_K:
			{
				gradientID++;
				gradientID %= gradients.size();
				tree.setGradient(gradients.get(gradientID));
				break;
			}
		}
	}
	
	public void render(GL2 gl, GLUT glut)
	{
		if(enableQuadtree)
		{
			gl.glPushMatrix();
			{
				for(Quadtree surface : trees.values())
					if(!surface.enableBlending) surface.render(gl);
			}
			gl.glPopMatrix();
		}
		else
		{
			switch(renderMode)
			{
				case 0: renderWireframe(gl, glut); break;
				case 1: if(model != null)
				{
					gl.glColor3f(1, 1, 1);
					model.render(gl); break;
				}
				case 2:
				{
					gl.glScalef(sx, sy, sz);
					prerender(gl);
					break;
				}
				case 3:
				{
					gl.glScalef(sx, sy, sz);
					gl.glCallList(terrainList);
					break;
				}
			}
		}
	}
	
	public void renderWater(GL2 gl)
	{
		if(enableQuadtree)
		{
			gl.glPushMatrix();
			{		
				for(Quadtree surface : trees.values())
					if(surface.enableBlending) surface.renderAlpha(gl);
			}
			gl.glPopMatrix();
		}
	}

	private void renderWireframe(GL2 gl, GLUT glut)
	{
		gl.glPushMatrix();
		{
			gl.glScalef(sx, sy, sz);
			
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_LINE_SMOOTH);
		
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			
			displayWireframeObject(gl, vertices, 4, RGB.BLACK_3F);
			displayWireframeObject(gl, q, 4, RGB.PURE_RED_3F);
			displayLines(gl, r, 3, RGB.PURE_BLUE_3F, true);
			
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			
			gl.glDisable(GL2.GL_BLEND);
			gl.glDisable(GL2.GL_LINE_SMOOTH);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(0, 1, 0);
		
		for (int i = 1; i < r.length; i += 3)
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(r[i][0] * sx, r[i][1] * sy, r[i][2] * sz);
				glut.glutSolidSphere(0.1, 6, 6);
			}
			gl.glPopMatrix();
		}
		
		gl.glColor3f(1, 1, 1);
	}
	
	public void prerender(GL2 gl)
	{	
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		for(int i = 0; i < vertices.length; i += 4)
			renderMultiTexQuad(gl, i);
	}
	
	public void renderMultiTexQuad(GL2 gl, int i)
	{	
		baseTexture.bind(gl);
		
		gl.glEnable(GL2.GL_LIGHTING);

		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glNormal3f(normals[i][0], normals[i][1], normals[i][2]);
			
			for(int j = i; j < i + 4; j++)
			{
				gl.glTexCoord2f(texCoords[j][0], texCoords[j][1]);
				gl.glVertex3f(vertices[j][0], vertices[j][1], vertices[j][2]);
			}
		}
		gl.glEnd();
	}
	
	private void createLightMap()
	{
		BufferedImage map = new BufferedImage(LIGHT_MAP_SIZE, LIGHT_MAP_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = map.createGraphics();
		
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								  RenderingHints.VALUE_ANTIALIAS_ON);
		
		graphics.setColor(Color.GRAY);
		graphics.fillRect(0, 0, LIGHT_MAP_SIZE, LIGHT_MAP_SIZE);
		
		int[] centre = {LIGHT_MAP_SIZE / 2, LIGHT_MAP_SIZE / 2};
		
		int diameter = (LIGHT_MAP_SIZE * 7) / 10;
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillOval(centre[0] - (diameter / 2), centre[1] - (diameter / 2), diameter, diameter);
		
		diameter = (LIGHT_MAP_SIZE * 4) / 10;
		graphics.setColor(Color.WHITE);
		graphics.fillOval(centre[0] - (diameter / 2), centre[1] - (diameter / 2), diameter, diameter);
		
		diameter = (LIGHT_MAP_SIZE * 2) / 10;
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillOval(centre[0] - (diameter / 2), centre[1] - (diameter / 2), diameter, diameter);
		
		diameter = (LIGHT_MAP_SIZE * 15) / 100;
		graphics.setColor(Color.GRAY);
		graphics.fillOval(centre[0] - (diameter / 2), centre[1] - (diameter / 2), diameter, diameter);
		
		graphics.dispose();
		
		try { ImageIO.write(map, "png", new File("tex/lightMap.png")); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
}
