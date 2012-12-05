import graphics.util.MultiTexFace;

import static graphics.util.Renderer.displayWireframeObject;
import static graphics.util.Renderer.displayLines;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class HeightMap
{
	private static final int ITERATIONS = 2000;
	private static final float PEAK_INC = 0.30f;
	private static final float SIDE_INC = 0.25f;
	
	private static final float MAX_HEIGHT = 60.0f;
	
	public static final float X_SCALE = 10.0f;
	public static final float Y_SCALE =  3.0f;
	public static final float Z_SCALE = 10.0f;
	
	private static final int LIGHT_MAP_SIZE = 128;
	
	private boolean createLightMap = false;
	
	public int terrainList;
	public boolean enableWireframe = false;
	
	float[][] heights;
	int length;
	
	float[][] vertices;
	
	float[][] textureVertices1;
	float[][] textureVertices2;
	float[][] textureVertices3;
	
	Texture texture1;
	Texture texture2;
	Texture texture3;
	
	float[][] q = new float[16][3];
	float[][] r = new float[12][3];
	
	int qBuffer = 0;
	int rBuffer = 0;
	
	public HeightMap(GL2 gl, int length)
	{
		setHeights(length);
		this.length = length;
		
		loadTextures();
		
		if(createLightMap) createLightMap();
		
		createGeometry(10, 20);
		
		displayList(gl);
	}

	public HeightMap(GL2 gl, String fileName)
	{
		importMap(fileName);
		
		loadTextures();
		
		if(createLightMap) createLightMap();
		
		createGeometry(10, 20);
		
		displayList(gl);
	}

	/**
	 * This method uses a hill-raising algorithm to randomly generate the heights
	 * of the map; this map is of the length passed as a parameter
	 */
	private void setHeights(int length)
	{
		float[][] heights = new float[length + 1][length + 1];
		
		Random generator = new Random();
		
		int x, z;
		
		for (int i = 0; i < ITERATIONS; i++)
		{
			x = (int) (generator.nextDouble() * length);
			z = (int) (generator.nextDouble() * length);
			
			if(increaseHeight(x, z, generator))
			{
				heights[x][z] += PEAK_INC;
				
				if(x > 0)            heights[x - 1][z] += SIDE_INC; //left 
				if(x < (length - 1)) heights[x + 1][z] += SIDE_INC; //right
				if(z > 0)            heights[x][z - 1] += SIDE_INC; //back 
				if(x > (length - 1)) heights[x][z + 1] += SIDE_INC; //front 
			}
		}
		
		this.heights = heights;
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

	private void loadTextures()
	{
		try
		{
			texture1 = TextureIO.newTexture(new File("tex/grass.jpg"), true);
			texture2 = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
			texture3 = TextureIO.newTexture(new File("tex/lightMap.png"), true);
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
		BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < length; j++)
			{
				float height = heights[i][j] / MAX_HEIGHT;
				int intensity = (int) (height * 255);
				Color color = new Color(intensity, intensity, intensity);
				
				image.setRGB(i, j, color.getRGB());
			}
		}
		
		try { ImageIO.write(image, "jpg", new File("tex/heightMap.jpg")); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void displayMap()
	{
		BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < length; j++)
			{
				float height = heights[i][j] / MAX_HEIGHT;
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
		float x = (p[0] / X_SCALE) + length / 2; //x-coordinate translated to map scale
		float z = (p[2] / Z_SCALE) + length / 2; //z-coordinate translated to map scale
		
		int x1 = (int) Math.floor(x);
		int z1 = (int) Math.floor(z);
		int x2 = x1 + 1;
		int z2 = z1 + 1;
		
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
		
		return h * Y_SCALE;
	}
	
	private boolean increaseHeight(int x, int z, Random generator)
	{
		if(z < length /  5.0f) return (generator.nextDouble() < 0.5);
		if(x < length / 10.0f) return (generator.nextDouble() < 0.5);
		if((length - x) < length / 10.0f) return (generator.nextDouble() < 0.5);
		
		return (generator.nextDouble() < 0.05f);
	}
	
	public void createGeometry(int texLen1, int texLen2)
	{
		List<MultiTexFace> faces = new ArrayList<MultiTexFace>();
		
		vertices = createVertices();
		
		textureVertices1 = createTextureVertices(vertices, texLen1);
		textureVertices2 = createTextureVertices(vertices, texLen2);
		textureVertices3 = createTextureVertices(vertices, length);
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
		
		vertices[i    ] = new float[] {_x,     heights[x    ][z + 1], _z + 1}; //bottom-right
		vertices[i + 1] = new float[] {_x + 1, heights[x + 1][z + 1], _z + 1}; //bottom-left
		vertices[i + 2] = new float[] {_x + 1, heights[x + 1][z    ], _z    }; //top-right
		vertices[i + 3] = new float[] {_x,     heights[x    ][z    ], _z    }; //top-left
	}
	
	public int coordToIndex(float x, float z)
	{
		int _x = (int) (x / 10) + length / 2;
		int _z = (int) (z / 10) + length / 2;
		
		return 3 + (4 * _z) + (4 * _x * length);
	}
	
	public float[][] createTextureVertices(float[][] vertices, int texLen)
	{
		int n = vertices.length;
		float[][] textureVertices = new float[n][2];
		
		float[] _tv = {-1, -1};
		
		for (int i = 0; i < n; i += 4)
		{
			createTexturedQuad(textureVertices, i, texLen, vertices, _tv);
		}
		
		return textureVertices;
	}
	
	private void createTexturedQuad(float[][] textureVertices, int i, int texLen, float[][] vertices, float[] _tv)
	{
		textureVertices[i] = makeTextureVertex(vertices[i], texLen, _tv);
		
		for(int j = 1; j < 4; j++)
			textureVertices[i + j] = makeTextureVertex(vertices[i + j], texLen, textureVertices[i]);
	}
	
	private float[] makeTextureVertex(float[] vertex, int texLen, float[] tv1)
	{
		float s, t;
		
		if(texLen > 1)
		{
			s = ((float) ((vertex[0] + length / 2) % texLen)) / texLen;
			t = ((float) ((length / 2 - vertex[2]) % texLen)) / texLen;
		}
		else
		{
			s = ((float) (vertex[0] + length / 2)) / texLen;
			t = ((float) (length / 2 - vertex[2])) / texLen;
		}
		
		if(s < tv1[0]) s = 1.0f - s;
		if(t < tv1[1]) t = 1.0f - t;
		
		return new float[] {s, t};
	}
	
	public void render(GL2 gl, GLUT glut)
	{
		if(enableWireframe)
		{
			renderWireframe(gl, glut);
		}
		else
		{
			gl.glScalef(X_SCALE, Y_SCALE, Z_SCALE);
			gl.glCallList(terrainList);
		}
	}

	private void renderWireframe(GL2 gl, GLUT glut)
	{
		gl.glPushMatrix();
		{
			gl.glScalef(X_SCALE, Y_SCALE, Z_SCALE);
			
			displayWireframeObject(gl, vertices, 4, RGB.BLACK_3F);
			displayWireframeObject(gl, q, 4, RGB.RED_3F);
			displayLines(gl, r, 3, RGB.BLUE_3F);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(0, 1, 0); //Pure Green
		
		for (int i = 1; i < r.length; i += 3)
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(r[i][0] * X_SCALE, r[i][1] * Y_SCALE, r[i][2] * Z_SCALE);
				glut.glutSolidSphere(0.1, 6, 6);
			}
			gl.glPopMatrix();
		}
		
		gl.glColor3f(1, 1, 1);
	}
	
	public void prerender(GL2 gl)
	{	
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);

		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);

		gl.glActiveTexture(GL2.GL_TEXTURE0);

		for(int i = 0; i < vertices.length; i += 4)
			renderMultiTexQuad(gl, i);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glDisable(GL2.GL_TEXTURE_2D);

		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	}
	
	public void renderMultiTexQuad(GL2 gl, int i)
	{
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		//8 Texture Units are supported

		/* BEGIN TEXTURE BLENDING SECTION: */

		/* TEXTURE0 is the alpha map; a texture of type GL_ALPHA8, no rgb channels */
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		texture3.bind(gl);
		texture3.enable(gl);

		/* TEXTURE1 is the 'bottom' texture */
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		texture1.bind(gl);
		texture1.enable(gl);
		// use the rgb from the bottom texture
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB,  GL2.GL_DECAL    );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB,  GL2.GL_TEXTURE  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
		//------------------------
		// use the alpha value from the alphaMap
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA,  GL2.GL_DECAL    );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA,  GL2.GL_PREVIOUS );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_ALPHA, GL2.GL_SRC_ALPHA);

		/* TEXTURE2 is the 'top' texture */
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		texture2.bind(gl);
		texture2.enable(gl);
		
		// interpolate between texture1 and texture2's colors, using the alpha values
		// from texture1 (which were taken from the alphamap)
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB,  GL2.GL_INTERPOLATE);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB,  GL2.GL_PREVIOUS   );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE1_RGB,  GL2.GL_TEXTURE    );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE2_RGB,  GL2.GL_PREVIOUS   );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_RGB, GL2.GL_SRC_COLOR  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND2_RGB, GL2.GL_SRC_ALPHA  );
		//------------------------
		// interpolate the alphas (this doesn't really matter, neither of the textures
		// really have alpha values)
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA,  GL2.GL_INTERPOLATE);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA,  GL2.GL_PREVIOUS   );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE1_ALPHA,  GL2.GL_TEXTURE    );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE2_ALPHA,  GL2.GL_PREVIOUS   );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_ALPHA, GL2.GL_SRC_ALPHA  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_ALPHA, GL2.GL_SRC_ALPHA  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND2_ALPHA, GL2.GL_SRC_ALPHA  );

		gl.glBegin(GL2.GL_QUADS);
		{
			for(int j = i; j < i + 4; j++)
			{
				gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, textureVertices3[j][0], textureVertices3[j][1]);
				gl.glMultiTexCoord2f(GL2.GL_TEXTURE1, textureVertices1[j][0], textureVertices1[j][1]);
				gl.glMultiTexCoord2f(GL2.GL_TEXTURE2, textureVertices2[j][0], textureVertices2[j][1]);
				
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
