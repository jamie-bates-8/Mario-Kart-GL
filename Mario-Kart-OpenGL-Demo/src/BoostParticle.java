import static graphics.util.Vector.*;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.Random;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

public class BoostParticle extends Particle
{
	private static Texture[] textures =
		{redFlare, orangeFlare, yellowFlare, greenFlare,
	     blueFlare, indigoFlare, whiteFlare, violetFlare};
	
	private float scale;
	private boolean special;

	public BoostParticle(float[] c, float[] t, float rotation, int duration, float scale, boolean special)
	{
		super(c, t, rotation, duration);
		
		this.scale = scale;
		this.special = special;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{	
			Random generator = new Random();
			
			double rotation = Math.toRadians(trajectory);
			
			float r = generator.nextInt(2) + generator.nextFloat();
			if(special) r *= 1.5;
			
			float[] _t = {(float) (-r * Math.cos(rotation)), 0, (float) (r * Math.sin(rotation))};
			float[] _c = subtract(c, _t);

			gl.glTranslatef(_c[0], _c[1], _c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glScalef(scale, scale, scale);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			gl.glColor4f(1, 1, 1, 1);
			
			int offset   = (special) ? 4 : 0;
			int spectrum = (special) ? 3 : 3;
			
			textures[offset + generator.nextInt(spectrum)].bind(gl);

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
}
