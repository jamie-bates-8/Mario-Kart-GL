package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;


public class DriftParticle extends Particle
{
	public static float[][] colors = {RGB.BRIGHT_YELLOW, RGB.RED, RGB.SKY_BLUE};
	
	private int color;
	private boolean flat;
	private boolean miniature;
	
	public DriftParticle(Vec3 c, float rotation, int color, boolean flat, boolean miniature)
	{
		super(c, new Vec3(), rotation, 0);
		
		this.color = color;
		this.flat = flat;
		this.miniature = miniature;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
//		flat = true;
		
		gl.glPushMatrix();
		{	
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glRotatef(trajectory, 0, -1, 0);
			gl.glRotatef((flat) ? -90 : 0, 1, 0, 0);
			gl.glRotatef(rotation, 0, 0, 1);
			
			Vec3 scale = new Vec3(0.75f, 2.0f, 0);
			if(miniature) scale = scale.multiply(0.5f);
			scale = scale.multiply(0.8f);
			
			gl.glScalef(scale.x, scale.y, scale.z);
			gl.glTranslatef(0, 0.2f, 0);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable (GL_BLEND);
			gl.glEnable (GL_TEXTURE_2D);
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

			whiteSpark.bind(gl);
			
			gl.glColor3f(colors[color][0], colors[color][1], colors[color][2]);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
	}
}
