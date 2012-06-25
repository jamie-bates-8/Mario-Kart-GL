import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.GL2.GL_LINE_LOOP;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static graphics.util.Vector.add;

import javax.media.opengl.GL2;


public class ItemBoxParticle extends Particle
{
	public float[] color;
	public boolean fill;
	
	public ItemBoxParticle(float[] c, float[] t, float rotation, int duration, float[] color, boolean fill)
	{
		super(c, t, rotation, duration);
		
		this.color = color;
		this.fill = fill;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			float[] c = add(getPosition(), t);

			gl.glTranslatef(c[0], c[1], c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glRotatef(rotation, 0, 0, 1);
			gl.glScalef(0.75f, 0.75f, 0.75f);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			gl.glColor4f(color[0], color[1], color[2], 1);

			if(fill)
			{
				gl.glBegin(GL_QUADS);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}
			else
			{
				gl.glBegin(GL_LINE_LOOP);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}
			

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}
}
