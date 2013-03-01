package bates.jamie.graphics.particle;

import static bates.jamie.graphics.util.Vector.multiply;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.GL.GL_TRUE;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.GL2ES1.GL_COORD_REPLACE;
import static javax.media.opengl.GL2ES1.GL_POINT_SPRITE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

public class BlastParticle extends Particle
{
	public static boolean pointSprite = true;
	
	public static HashMap<Integer, Float> colorMap = new HashMap<Integer, Float>(60);
	
	static
	{
		for(int i = 0; i < 60; i++)
		{
			float c = 2.0f / (i + 1);
			c = (1 - c) * 0.9f;

			colorMap.put(i, c);
		}
	}
	
	public BlastParticle(float[] c, float[] t, float rotation, int duration)
	{
		super(c, t, rotation, duration);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{		
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			if(pointSprite)
			{
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glPointSize(60);
				
				gl.glEnable(GL_POINT_SPRITE);
				gl.glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
				
				float[] p = this.c;
				
				float c = colorMap.get(duration);

				gl.glColor4f(c, c, c, c);
					
				if(!current.equals(indigoFlare))
				{
					indigoFlare.bind(gl);
					current = indigoFlare;
				}
				
				gl.glBegin(GL2.GL_POINTS);
				gl.glVertex3f(p[0], p[1], p[2]);
				gl.glEnd();
				
				gl.glDisable(GL2.GL_POINT_SPRITE);
				
				gl.glColor4f(1, 1, 1, 1);
			}
			else
			{
				gl.glTranslatef(c[0], c[1], c[2]);
				gl.glRotatef(trajectory - 90, 0, 1, 0);
				gl.glScalef(15, 15, 15);
				
				float c = 2.0f / (duration + 1);
				c = (1 - c) * 0.9f;

				gl.glColor4f(c, c, c, c);
				
				if(!current.equals(indigoFlare))
				{
					indigoFlare.bind(gl);
					current = indigoFlare;
				}
				
				gl.glBegin(GL_QUADS);
				{
					gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
		}
		gl.glPopMatrix();
	}
	
	public static void renderList(GL2 gl, List<Particle> particles)
	{
		gl.glPushMatrix();
		{	
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
			
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL_COLOR_ARRAY);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
				
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(60);
				
			gl.glEnable(GL_POINT_SPRITE);
			gl.glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
			
			indigoFlare.bind(gl);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(particles.size() * 3);
			
			for(Particle particle : particles) vertices.put(particle.c);
			vertices.position(0);  
			
			FloatBuffer colors = Buffers.newDirectFloatBuffer(particles.size() * 4);
			
			for(Particle particle : particles)
			{
				float c = colorMap.get(particle.duration);
				
				colors.put(new float[] {c, c, c, c});
			}
			colors.position(0);  
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glColorPointer (4, GL_FLOAT, 0, colors);
			gl.glDrawArrays(GL_POINTS, 0, particles.size() - 1);
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL_COLOR_ARRAY);
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		super.update();
		t = multiply(t, 0.9f);
	}
}
