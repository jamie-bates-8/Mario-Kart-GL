import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

public class DriftParticle extends Particle
{
	public static final float[] RED    = {237.0f,  28.0f,  36.0f};
	public static final float[] YELLOW = {255.0f, 228.0f,   0.0f};
	public static final float[] BLUE   = {  0.0f, 173.0f, 239.0f};
	
	public static float[][] colors = {YELLOW, RED, BLUE};
	
	private int color;
	private boolean flat;
	
	public DriftParticle(float[] c, float rotation, int color, boolean flat)
	{
		super(c, new float[] {0, 0, 0}, rotation, 0);
		
		this.color = color;
		this.flat = flat;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{	
			gl.glTranslatef(c[0], c[1], c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glRotatef((flat) ? -90 : 0, 1, 0, 0);
			gl.glRotatef(rotation, 0, 0, 1);
			
			gl.glScalef(1.5f, 5.0f, 1.5f);
			gl.glTranslatef(0, 0.2f, 0);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			whiteSpark.bind(gl);
			
			gl.glColor3f(colors[color][0]/255, colors[color][1]/255, colors[color][2]/255);

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
