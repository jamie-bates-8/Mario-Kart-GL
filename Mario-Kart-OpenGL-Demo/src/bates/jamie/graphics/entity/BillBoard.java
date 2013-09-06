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
import bates.jamie.graphics.util.Vec3;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class BillBoard
{
	public static final String TEXTURE_DIRECTORY = "tex/foliage/";
	
	public int texture;
	public Sphere sphere;
	
	private float size   = 3;
	private float width  = 1;
	private float height = 1;
	
	private float timer    = 0;
	private float rotation = 0;
	
	private static Texture[] textures;
	
	static
	{
		try
		{
			textures = new Texture[10];
			
			textures[0] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant1.png"), true);
			textures[1] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant2.png"), true);
			textures[2] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant3.png"), true);
			textures[3] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant4.png"), true);
			
			textures[4] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree1.png"), true);
			textures[5] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree2.png"), true);
			textures[6] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree3.png"), true);
			textures[7] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "tree4.png"), true);
			
			textures[8] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "bush1.png"), true);
			textures[9] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "bush2.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private static int current = 0;
	
	public BillBoard(Vec3 c, float baseSize)
	{	
		Random generator = new Random();
		
		size = baseSize + generator.nextFloat() * 0.25f;
		rotation = generator.nextInt(90);
		
		sphere = new Sphere(c, size);
		timer = generator.nextInt(90);
		
		this.texture = generator.nextInt(textures.length);
	}
	
	public BillBoard(Vec3 c, float baseSize, int texture)
	{		
		Random generator = new Random();
		
		size = baseSize + generator.nextFloat() * 0.25f;
		rotation = generator.nextInt(90);
		
		if(baseSize < 15 && baseSize > 3) width = 1.5f;
		
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
			
			Vec3 c = sphere.c;
			
			if(texture != current)
			{
				textures[texture].bind(gl);
				current = texture;
			}

			gl.glTranslatef(c.x, c.y + size * 0.75f, c.z);
			if(size > 15) gl.glRotatef(trajectory, 0, -1, 0);
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
			
			float w = 0.5f * width;
			float h = 0.5f * height;

			gl.glBegin(GL_QUADS);
			{
				gl.glNormal3f(0.0f, 0.0f, 1.0f);
				
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-w, -h, 0);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-w + xoffset, h, zoffset);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( w + xoffset, h, zoffset);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( w, -h, 0);
				
				if(size < 15)
				{
					gl.glNormal3f(0.25f, 0.0f, 1.0f);
					
					gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(0, -h, -w);
					gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(xoffset, h, -w + zoffset);
					gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(xoffset, h,  w + zoffset);
					gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(0, -h,  w);
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
			
			for(BillBoard board : boards) vertices.put(board.sphere.c.add(new Vec3(0, 0.75f, 0)).toArray());
			vertices.position(0); 
			
			FloatBuffer colors = Buffers.newDirectFloatBuffer(boards.size() * 4);
			
			for(BillBoard board : boards)
			{
				Vec3 c = board.sphere.c;
				
				float x = (c.x + 200) / 400;
				float z = (c.z + 200) / 400;
				
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
			
			Vec3[] offsets =
			{
				new Vec3(-.5f, -.5f, 0),
				new Vec3(-.5f,  .5f, 0),
				new Vec3( .5f,  .5f, 0),
				new Vec3( .5f, -.5f, 0)
			};
			
			for(BillBoard board : boards)
			{
				Vec3 c = board.sphere.c;
				Vec3 p = new Vec3(c.x, c.y + board.size * 0.9f, c.z);
				
				vertices.put(p.add(offsets[0].multiply(board.size * 2)).toArray());
				vertices.put(p.add(offsets[1].multiply(board.size * 2)).toArray());
				vertices.put(p.add(offsets[2].multiply(board.size * 2)).toArray());
				vertices.put(p.add(offsets[3].multiply(board.size * 2)).toArray());
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
