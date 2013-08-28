package bates.jamie.graphics.entity;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.GL.GL_TRUE;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.GL2ES1.GL_COORD_REPLACE;
import static javax.media.opengl.GL2ES1.GL_POINT_SPRITE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class BillBoard
{
	public static final String TEXTURE_DIRECTORY = "tex/foliage/";
	
	public int texture;
	public Sphere sphere;
	
	private float size;
	
	private float timer    = 0;
	private float rotation = 0;
	
	private static Texture[] textures;
	
	static
	{
		try
		{
			textures = new Texture[8];
			
			textures[0] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant1.png"), true);
			textures[1] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant2.png"), true);
			textures[2] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant3.png"), true);
			textures[3] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant4.png"), true);
			
			textures[4] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree1.png"), true);
			textures[5] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree2.png"), true);
			textures[6] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree3.png"), true);
			textures[7] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree4.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private static int current = 0;
	
	public BillBoard(float[] c, float baseSize)
	{	
		Random generator = new Random();
		
		size = baseSize + generator.nextFloat() * 0.25f;
		rotation = generator.nextInt(90);
		
		sphere = new Sphere(c, size);
		timer = generator.nextInt(90);
		
		this.texture = generator.nextInt(textures.length);
	}
	
	public BillBoard(float[] c, float baseSize, int texture)
	{		
		Random generator = new Random();
		
		size = baseSize + generator.nextFloat() * 0.25f;
		rotation = generator.nextInt(90);
		
		sphere = new Sphere(c, size);
		timer = generator.nextInt(90);
		
		this.texture = texture;
	}

	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glEnable(GL2.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL2.GL_GREATER, 0.25f);
			
			float[] c = sphere.c;
			
			if(texture != current)
			{
				textures[texture].bind(gl);
				current = texture;
			}

			gl.glTranslatef(c[0], c[1] + size * 0.75f, c[2]);
			if(size > 15) gl.glRotatef(trajectory - 90, 0, 1, 0);
			else gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(size * 2, size * 2, size * 2);
			
			timer += 0.75;
			float xoffset = (float) Math.sin(Math.toRadians(timer)) * 0.10f;
			float zoffset = (float) Math.sin(Math.toRadians(timer + rotation)) * 0.05f;
			
			if(size > 15)
			{
				xoffset *= 0.1f;
				zoffset *= 0.1f;
			}

			gl.glBegin(GL_QUADS);
			{
				gl.glNormal3f(0.0f, 0.0f, 1.0f);
				
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f,  0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f + xoffset,  0.5f,  0.0f + zoffset);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f + xoffset,  0.5f,  0.0f + zoffset);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f,  0.0f);
				
				if(size < 15)
				{
					gl.glNormal3f(0.25f, 0.0f, 1.0f);
					
					gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 0.0f, -0.5f, -0.5f);
					gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 0.0f + xoffset,  0.5f, -0.5f + zoffset);
					gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.0f + xoffset,  0.5f,  0.5f + zoffset);
					gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.0f, -0.5f,  0.5f);
				}
			}
			gl.glEnd();
			
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL2.GL_ALPHA_TEST);
			
		}
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void renderPoints(GL2 gl, List<BillBoard> boards)
	{
		gl.glPushMatrix();
		{	
			gl.glColor3f(1, 0, 0);
			
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			
//			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glEnable(GL2.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL2.GL_GREATER, 0.25f);
				
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(100);
				
			gl.glEnable(GL_POINT_SPRITE);
			gl.glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
			gl.glPointParameteri(GL2.GL_POINT_SPRITE_COORD_ORIGIN, GL2.GL_LOWER_LEFT);
			
			textures[3].bind(gl);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(boards.size() * 3);
			
			for(BillBoard board : boards) vertices.put(Vector.add(board.sphere.c, new float[] {0, 0.75f, 0}));
			vertices.position(0); 
			
			FloatBuffer colors = Buffers.newDirectFloatBuffer(boards.size() * 4);
			
			for(BillBoard board : boards)
			{
				float[] c = board.sphere.c;
				
				float x = (c[0] + 200) / 400;
				float z = (c[2] + 200) / 400;
				
				colors.put(new float[] {x, z, x, 1});
			}
			colors.position(0); 
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glColorPointer (4, GL_FLOAT, 0, colors);
			gl.glDrawArrays(GL_POINTS, 0, boards.size() - 1);
			
//			gl.glDepthMask(true);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDisable(GL2.GL_ALPHA_TEST);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
			
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
	}
	
	public static void renderQuads(GL2 gl, List<BillBoard> boards, float trajectory)
	{
		gl.glPushMatrix();
		{	
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			gl.glColor3f(1, 1, 1);
			
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			textures[3].bind(gl);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(boards.size() * 4 * 3);
			
			float[][] offsets =
			{
				{-0.5f, -0.5f, 0.0f},
				{-0.5f,  0.5f, 0.0f},
				{ 0.5f,  0.5f, 0.0f},
				{ 0.5f, -0.5f, 0.0f}
			};
			
			for(BillBoard board : boards)
			{
				float[] c = board.sphere.c;
				float[] p = {c[0], c[1] + board.size * 0.9f, c[2]};
				
				vertices.put(Vector.add(p, Vector.multiply(offsets[0], board.size * 2)));
				vertices.put(Vector.add(p, Vector.multiply(offsets[1], board.size * 2)));
				vertices.put(Vector.add(p, Vector.multiply(offsets[2], board.size * 2)));
				vertices.put(Vector.add(p, Vector.multiply(offsets[3], board.size * 2)));
			}
			vertices.position(0);
			
			FloatBuffer texCoords = Buffers.newDirectFloatBuffer(boards.size() * 4 * 2);
			float[] texCoord = {1, 0, 1, 1, 0, 1, 0, 0};
			
			for(BillBoard board : boards) texCoords.put(texCoord);
			texCoords.position(0); 
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
			
			gl.glDrawArrays(GL_QUADS, 0, boards.size() - 1);
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		}
		gl.glPopMatrix();
	}
}
