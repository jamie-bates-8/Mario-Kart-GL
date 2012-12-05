import static graphics.util.Vector.multiply;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

public class BlastParticle extends Particle
{
	public BlastParticle(float[] c, float[] t, float rotation, int duration)
	{
		super(c, t, rotation, duration);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glEnable(GL2.GL_POINT_SPRITE);
		gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
		
		gl.glPushMatrix();
		{	
			gl.glPointSize(60);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			float c = 2.0f / (duration + 1);
			c = (1 - c) * 0.9f;

			gl.glColor4f(c, c, c, c);
			
			if(!current.equals(indigoFlare))
			{
				indigoFlare.bind(gl);
				current = indigoFlare;
			}
			
			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3f(this.c[0], this.c[1], this.c[2]);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_POINT_SPRITE);
	}
	
	@Override
	public void update()
	{
		super.update();
		t = multiply(t, 0.9f);
	}
}
