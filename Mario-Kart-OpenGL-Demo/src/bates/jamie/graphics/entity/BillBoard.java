package bates.jamie.graphics.entity;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Sphere;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class BillBoard
{
	private Texture texture;
	public Sphere sphere;
	private float size;
	
	private static Texture[] textures;
	
	static
	{
		try
		{
			textures = new Texture[4];
			
			textures[0] = TextureIO.newTexture(new File("tex/plant1.png"), true);
			textures[1] = TextureIO.newTexture(new File("tex/plant2.png"), true);
			textures[2] = TextureIO.newTexture(new File("tex/plant3.png"), true);
			textures[3] = TextureIO.newTexture(new File("tex/plant4.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public BillBoard(float[] c)
	{	
		Random generator = new Random();
		
		size = 1 + generator.nextFloat() * 3;
		
		sphere = new Sphere(c, size);
		
		this.texture = textures[generator.nextInt(textures.length)];
	}
	
	public BillBoard(float[] c, int texture)
	{		
		Random generator = new Random();
		
		size = 1 + generator.nextFloat() * 3;
		
		sphere = new Sphere(c, size);
		
		this.texture = textures[texture];
	}

	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDepthMask(false);
			
			float[] c = sphere.c;

			gl.glTranslatef(c[0], c[1] + size * 0.9f, c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glScalef(size * 2, size * 2, size * 2);
			
			texture.bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);
	}
}
