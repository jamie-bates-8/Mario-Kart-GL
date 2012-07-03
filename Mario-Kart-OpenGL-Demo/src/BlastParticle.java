import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import static graphics.util.Vector.multiply;


public class BlastParticle extends Particle
{
	public BlastParticle(float[] c, float[] t, float rotation, int duration)
	{
		super(c, t, rotation, duration);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{	
			gl.glTranslatef(c[0], c[1], c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glScalef(15, 15, 15);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			float c = 2.0f / (duration + 1);
			c = (1 - c) * 0.9f;

			gl.glColor4f(c, c, c, c);
			
			indigoFlare.bind(gl);

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
	}
	
	@Override
	public void update()
	{
		super.update();
		t = multiply(t, 0.9f);
	}
}
