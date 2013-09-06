package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

public class SplashParticle extends Particle
{
	private double gravity = 0.1;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 10.0;
	
	private float[] color;
	
	public SplashParticle(Vec3 c, Vec3 t, float[] color)
	{
		super(c, t, 0, 6);
		
		this.color = color;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDisable(GL_TEXTURE_2D);

			gl.glColor4fv(color, 0);
			
			gl.glBegin(GL2.GL_POINTS);
			{
				gl.glVertex3f(0, 0, 0);
			}
			gl.glEnd();

			gl.glEnable(GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		c.y -= fallRate;
	}
}
