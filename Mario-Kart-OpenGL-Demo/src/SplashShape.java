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


public class SplashShape
{
	Texture texture1;
	Texture texture2;

	private int floorLength;

	private int splashSize;

	private float[][] vertices;
	private float[][] textureVertices;

	private float[][] heights;

	private static final int ALPHA_MAP_SIZE = 128;
	private boolean createAlphaMap = true;


	public SplashShape(Texture texture, float[][] heights, int splashSize)
	{
		this.texture1 = texture;
		floorLength = heights.length - 1;
		this.splashSize = splashSize;

		if(createAlphaMap) createAlphaMap();
		
		try
		{
			texture2 = TextureIO.newTexture(new File("tex/cloud1.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }

		createGeometry(heights);
	}

	private void createGeometry(float[][] heights)
	{
		int[] start = getStartPoint(heights);

		vertices = createVertices(start, heights);
		textureVertices = createTextureVertices(start);
	}

	private int[] getStartPoint(float[][] heights)
	{
		Random generator = new Random();

		int z = (int) (generator.nextDouble() * floorLength);
		int x = (int) (generator.nextDouble() * floorLength);

		if(z + splashSize > floorLength) z = floorLength - splashSize;
		if(x + splashSize > floorLength) x = floorLength - splashSize;

		return new int[] {x, 0, z};
	}

	private float[][] createVertices(int[] start, float[][] heights)
	{
		this.heights = getHeights(start, heights);

		float[][] vertices = new float[splashSize * splashSize * 4][3];

		int i = 0;

		for(int z = 0; z <= splashSize - 1; z++)
		{
			for(int x = 0; x <= splashSize - 1; x++)
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

		vertices[i    ] = new float[] {_x    , heights[z + 1][x    ], _z + 1};
		vertices[i + 1] = new float[] {_x + 1, heights[z + 1][x + 1], _z + 1};
		vertices[i + 2] = new float[] {_x + 1, heights[z    ][x + 1], _z    };
		vertices[i + 3] = new float[] {_x    , heights[z    ][x    ], _z    };
	}

	private float[][] getHeights(int[] start, float[][] heights)
	{
		float[][] _heights = new float[splashSize + 1][splashSize + 1];

		for(int z = 0; z <= splashSize; z++)
		{
			for(int x = 0; x <= splashSize; x++)
			{
				_heights[z][x] = heights[start[2] + z][start[0] + x];
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
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);

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
	}

	public void renderSplashQuad(GL2 gl, int i)
	{
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		/* BEGIN TEXTURE BLENDING SECTION: */

//		gl.glActiveTexture(GL2.GL_TEXTURE0);
//		texture1.bind(gl);
//		texture1.enable(gl);
//
//		gl.glActiveTexture(GL2.GL_TEXTURE1);
//		texture2.bind(gl);
//		texture2.enable(gl);
//		// use the rgb from the bottom texture
//
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB,  GL2.GL_COMBINE  );
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB,  GL2.GL_TEXTURE  );
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
//		//------------------------
//		// use the alpha value from the alphaMap
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA,  GL2.GL_DECAL    );
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA,  GL2.GL_PREVIOUS );
//		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_ALPHA, GL2.GL_SRC_ALPHA);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		texture2.bind(gl);
		texture2.enable(gl);
	    gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		texture1.bind(gl);
		texture1.enable(gl);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_INTERPOLATE);   //Interpolate RGB with RGB
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_RGB, GL2.GL_PREVIOUS);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE1_RGB, GL2.GL_TEXTURE);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE2_RGB, GL2.GL_PREVIOUS);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_RGB, GL2.GL_SRC_COLOR);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND2_RGB, GL2.GL_SRC_ALPHA);
		 //------------------------
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_INTERPOLATE);   //Interpolate ALPHA with ALPHA
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE0_ALPHA, GL2.GL_PREVIOUS);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE1_ALPHA, GL2.GL_TEXTURE);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SOURCE2_ALPHA, GL2.GL_PREVIOUS);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_ALPHA, GL2.GL_SRC_ALPHA);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_ALPHA, GL2.GL_SRC_ALPHA);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND2_ALPHA, GL2.GL_SRC_ALPHA);

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
}
