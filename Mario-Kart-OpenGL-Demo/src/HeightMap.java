import graphics.util.MultiTexFace;

import java.awt.Color;
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

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class HeightMap
{
	private static final int ITERATIONS = 2000;
	private static final float PEAK_INC = 0.30f;
	private static final float SIDE_INC = 0.25f;
	
	private static final int LIGHT_MAP_SIZE = 128;
	
	private boolean createLightMap = false;
	
	public int terrainList;
	
	float[][] heights;
	int length;
	
	float[][] vertices;
	
	float[][] textureVertices1;
	float[][] textureVertices2;
	float[][] textureVertices3;
	
	Texture texture1;
	Texture texture2;
	Texture texture3;
	
	public HeightMap(GL2 gl, int length)
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
				heights[z][x] += PEAK_INC;
				
				if(x > 0)            heights[z][x - 1] += SIDE_INC; //left 
				if(x < (length - 1)) heights[z][x + 1] += SIDE_INC; //right
				if(z > 0)            heights[z - 1][x] += SIDE_INC; //back 
				if(x > (length - 1)) heights[z + 1][z] += SIDE_INC; //front 
			}
		}
		
		this.heights = heights;
		this.length = length;
		
		try
		{
			texture1 = TextureIO.newTexture(new File("tex/grass.jpg"), true);
			texture2 = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
			texture3 = TextureIO.newTexture(new File("tex/lightMap.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		if(createLightMap) createLightMap();
		
		createGeometry(10, 20);
		
		terrainList = gl.glGenLists(1);
		gl.glNewList(terrainList, GL2.GL_COMPILE);
		render(gl);
	    gl.glEndList();
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
		
		for(int z = 0; z <= length - 1; z++)
		{
			for(int x = 0; x <= length - 1; x++)
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
		
		vertices[i    ] = new float[] {_x,     heights[z + 1][x    ], _z + 1};
		vertices[i + 1] = new float[] {_x + 1, heights[z + 1][x + 1], _z + 1};
		vertices[i + 2] = new float[] {_x + 1, heights[z    ][x + 1], _z    };
		vertices[i + 3] = new float[] {_x,     heights[z    ][x    ], _z    };
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
	
	public void render(GL2 gl)
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
