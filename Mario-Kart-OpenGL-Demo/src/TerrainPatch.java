import static graphics.util.Renderer.displayWireframeObject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import java.io.File;
import java.io.IOException;

import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TerrainPatch
{
	public Texture texture;
	private Texture alphaMap;
	
	public boolean colliding = false;
	
	public float friction = 1;
	
	private static Texture[] textures;
	private static Texture[] alphaMaps;
	
	static
	{
		try
		{
			textures = new Texture[3];
					
			textures[0] = TextureIO.newTexture(new File("tex/sand.jpg"), true);
//			textures[1] = TextureIO.newTexture(new File("tex/dirt1.jpg"), true);
//			textures[2] = TextureIO.newTexture(new File("tex/dirt2.jpg"), true);
			textures[1] = TextureIO.newTexture(new File("tex/dirt3.jpg"), true);
			textures[2] = TextureIO.newTexture(new File("tex/dirt4.jpg"), true);
//			textures[5] = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
//			textures[6] = TextureIO.newTexture(new File("tex/grass.jpg"), true);
			
			alphaMaps = new Texture[2];
			
			alphaMaps[0] = TextureIO.newTexture(new File("tex/splatter.png"), true);
			alphaMaps[1] = TextureIO.newTexture(new File("tex/splatter2.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private int floorLength;

	private int splashSize;

	private float[][] vertices;
	private float[][] textureVertices;

	private float[][] heights;

	private static final int ALPHA_MAP_SIZE = 128;
	private boolean createAlphaMap = false;


	public TerrainPatch(Texture texture, float[][] heights, int splashSize)
	{
		Random generator = new Random();
		
		if(texture == null)
		{
			int t = generator.nextInt(textures.length);
			texture = textures[t];
			friction = friction(t);
		}
		
		this.texture = texture;
		floorLength = heights.length - 1;
		this.splashSize = splashSize;

		if(createAlphaMap) createAlphaMap();
		
		int a = generator.nextInt(alphaMaps.length);
		alphaMap = alphaMaps[a];

		createGeometry(heights);
	}
	
	private float friction(int material)
	{
		switch(material)
		{
			case 0: return 1.00f;
			case 1: return 0.90f;
			case 2: return 0.80f;
			case 3: return 0.70f;
			case 4: return 0.60f;
			case 5: return 0.50f;
			case 6: return 0.90f;
			
			default: return 1.00f;
		}
	}
	
	public TerrainPatch(Texture texture, float[][] heights, int splashSize, int x, int z)
	{
		this.texture = texture;
		floorLength = heights.length - 1;
		this.splashSize = splashSize;

		if(createAlphaMap) createAlphaMap();
		
		try
		{
			alphaMap = TextureIO.newTexture(new File("tex/splatter.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		x -= splashSize / 2;
		z -= splashSize / 2;
		
		if(x + splashSize > floorLength) x = floorLength - splashSize;
		if(z + splashSize > floorLength) z = floorLength - splashSize;

		createGeometry(heights, new int[] {x, 0, z});
	}

	private void createGeometry(float[][] heights)
	{
		int[] start = getStartPoint(heights);

		vertices = createVertices(start, heights);
		textureVertices = createTextureVertices(start);
	}
	
	private void createGeometry(float[][] heights, int[] start)
	{
		vertices = createVertices(start, heights);
		textureVertices = createTextureVertices(start);
	}

	private int[] getStartPoint(float[][] heights)
	{
		Random generator = new Random();

		int x = (int) (generator.nextDouble() * floorLength);
		int z = (int) (generator.nextDouble() * floorLength);

		if(x + splashSize > floorLength) x = floorLength - splashSize;
		if(z + splashSize > floorLength) z = floorLength - splashSize;

		return new int[] {x, 0, z};
	}

	private float[][] createVertices(int[] start, float[][] heights)
	{
		this.heights = getHeights(start, heights);

		float[][] vertices = new float[splashSize * splashSize * 4][3];

		int i = 0;

		for(int x = 0; x <= splashSize - 1; x++)
		{
			for(int z = 0; z <= splashSize - 1; z++)
			{
				createQuad(vertices, i, x, z, start);
				i += 4;
			}
		}

		return vertices;
	}

	private void createQuad(float[][] vertices, int i, int x, int z, int[] start)
	{
		float _x = start[0] + x - floorLength / 2;
		float _z = start[2] + z - floorLength / 2;

		vertices[i    ] = new float[] {_x    , heights[x    ][z + 1], _z + 1};
		vertices[i + 1] = new float[] {_x + 1, heights[x + 1][z + 1], _z + 1};
		vertices[i + 2] = new float[] {_x + 1, heights[x + 1][z    ], _z    };
		vertices[i + 3] = new float[] {_x    , heights[x    ][z    ], _z    };
	}

	private float[][] getHeights(int[] start, float[][] heights)
	{
		float[][] _heights = new float[splashSize + 1][splashSize + 1];

		for(int x = 0; x <= splashSize; x++)
		{
			for(int z = 0; z <= splashSize; z++)
			{
				_heights[x][z] = heights[start[0] + x][start[2] + z];
			}
		}

		return _heights;
	}

	private float[][] createTextureVertices(int[] start)
	{
		int n = vertices.length;

		float[][] textureVertices = new float[n][2];

		for(int i = 0; i < n; i += 4)
			for(int j = 0; j < 4; j++)
				textureVertices[i + j] = makeTextureVertex(start, vertices[i + j]);

		return textureVertices;
	}

	private float[] makeTextureVertex(int[] start, float[] vertex)
	{
		float s = ((float) vertex[0] - start[0] + floorLength / 2) / splashSize; 
		float t = ((float) vertex[2] - start[2] + floorLength / 2) / splashSize; 

		return new float[] {s, t};
	}

	public void render(GL2 gl)
	{	
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);

		gl.glActiveTexture(GL2.GL_TEXTURE0);

		for(int i = 0; i < vertices.length; i += 4)
			renderSplashQuad(gl, i);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glDisable(GL2.GL_TEXTURE_2D);

		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		gl.glDisable(GL2.GL_BLEND);
		
//		if(colliding) renderWireframe(gl);
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}
	
	public void renderWireframe(GL2 gl)
	{
		float[][] _vertices = new float[4][3];
		
		float x1 = vertices[3][0];
		float z1 = vertices[3][2];
		float x2 = x1 + splashSize;
		float z2 = z1 + splashSize;
		
		_vertices[0] = new float[] {x1, 0.5f, z1};
		_vertices[1] = new float[] {x1, 0.5f, z2};
		_vertices[2] = new float[] {x2, 0.5f, z2};
		_vertices[3] = new float[] {x2, 0.5f, z1};
		
		gl.glPushMatrix();
		{
			displayWireframeObject(gl, _vertices, 4, RGB.PURE_RED_3F);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);
	}

	public void renderSplashQuad(GL2 gl, int i)
	{
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.1f);

		gl.glActiveTexture(GL2.GL_TEXTURE0);
		texture.bind(gl);
		texture.enable(gl);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		alphaMap.bind(gl);
		alphaMap.enable(gl);
		
//		int[] data = new int[4];
//		
//		data[0] = r;
//	    data[1] = g;
//	    data[2] = b;
//	    data[3] = a;
//	    
//	    Buffer b;
//	    
//	    int x = 0;
//	    int y = 0;
//		
//	    gl.glTexSubImage2D(GL2.GL_TEXTURE_2D,
//                0,
//                x,
//                y,
//                1,
//                1,
//                GL2.GL_RGBA,
//                GL2.GL_UNSIGNED_BYTE,
//                b);
	    
		//replace the color of the alpha map with the color of the texture
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB,  GL2.GL_REPLACE  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB,  GL2.GL_PREVIOUS );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);

		// use the alpha value from the alpha map
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA,  GL2.GL_REPLACE  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA,  GL2.GL_TEXTURE  );
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_ALPHA, GL2.GL_SRC_ALPHA);

		gl.glBegin(GL2.GL_QUADS);
		{
			for(int j = i; j < i + 4; j++)
			{
				gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, textureVertices[j][0], textureVertices[j][1]);
				gl.glMultiTexCoord2f(GL2.GL_TEXTURE1, textureVertices[j][0], textureVertices[j][1]);

				gl.glVertex3f(vertices[j][0], vertices[j][1], vertices[j][2]);
			}
		}
		gl.glEnd();
	}

	private void createAlphaMap()
	{
		Random generator = new Random();

		BufferedImage map = new BufferedImage(ALPHA_MAP_SIZE, ALPHA_MAP_SIZE, BufferedImage.TYPE_BYTE_GRAY);

		Graphics2D graphics = map.createGraphics();
		
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, ALPHA_MAP_SIZE, ALPHA_MAP_SIZE);
		
		int radius = 3;
		int offset = 8;
		graphics.setColor(new Color(0.3f, 0.3f, 0.3f));
		boxedCircles(offset, offset, ALPHA_MAP_SIZE - (offset * 2), radius, 100, graphics, generator);
		
		offset = 12;
		graphics.setColor(new Color(0.6f, 0.6f, 0.6f));
		boxedCircles(offset, offset, ALPHA_MAP_SIZE - (offset * 2), radius,  80, graphics, generator);
		
		offset = 16;
		graphics.setColor(Color.WHITE);
		boxedCircles(offset, offset, ALPHA_MAP_SIZE - (offset * 2), radius,  50, graphics, generator);
		
		Image _map = grayScaleToAlpha(map);
		
		try { ImageIO.write(applyAlpha(map), "png", new File("tex/alphaMap.png")); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private void boxedCircles(int x, int y, int lenght, int radius, int n, Graphics2D graphics, Random generator)
	{
		int _x, _y;
		
		for (int i = 0; i < n; i++)
		{
			_x = x + (int) (generator.nextDouble() * lenght) - radius;
			_y = y + (int) (generator.nextDouble() * lenght) - radius;
			
			graphics.fillOval(_x, _y, radius * 2, radius * 2);
		}
	}

	private Image grayScaleToAlpha(BufferedImage image)
	{
		ImageFilter filter = new RGBImageFilter()
		{
			public final int filterRGB(int x, int y, int rgb)
			{
				return (rgb << 8) & 0xFF000000;
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	private BufferedImage applyAlpha(Image mask)
	{
		BufferedImage map = new BufferedImage(ALPHA_MAP_SIZE, ALPHA_MAP_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = map.createGraphics();
		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f);
		graphics.setComposite(ac);
		
		graphics.drawImage(mask, 0, 0, null);
		graphics.dispose();
		return map;
	}
	
	public boolean isColliding(float[] p)
	{
		float x = p[0];
		float z = p[2];
		
		float[] v = vertices[3];
		
		float x1 = v[0] * Terrain.sx;
		float z1 = v[2] * Terrain.sz;
		float x2 = x1 + (splashSize * Terrain.sx);
		float z2 = z1 + (splashSize * Terrain.sz);
		
		return ((x >= x1) && (x <= x2) && (z >= z1) && (z <= z2)); 
	}
}
