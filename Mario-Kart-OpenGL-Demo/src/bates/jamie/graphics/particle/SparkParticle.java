package bates.jamie.graphics.particle;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_LINES;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

public class SparkParticle extends Particle
{
	private double gravity = 0.1;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 10.0;
	
	private float[] color;
	private boolean miniature;
	
	public SparkParticle(float[] c, float[] t, float rotation, int duration,
			float[] color, boolean miniature)
	{
		super(c, t, rotation, duration);
		
		this.color = color;
		this.miniature = miniature;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(c[0], c[1], c[2]);
			if(miniature) gl.glScalef(0.25f, 0.25f, 0.25f);
			else gl.glScalef(0.5f, 0.5f, 0.5f);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDisable(GL_TEXTURE_2D);

			gl.glColor3f(color[0], color[1], color[2]);
			
			gl.glBegin(GL_LINES);
			{
				gl.glVertex3f(0, 0, 0);
				gl.glVertex3f(t[0], t[1] - fallRate, t[2]);
			}
			gl.glEnd();

			gl.glEnable(GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		c[1] -= fallRate;
	}
}
