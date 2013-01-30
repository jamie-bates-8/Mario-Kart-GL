import static graphics.util.Vector.subtract;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_POINTS;
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
	private boolean miniature;

	public BoostParticle(float[] c, float[] t, float rotation, int duration,
			float scale, boolean special, boolean miniature)
	{
		super(c, t, rotation, duration);
		
		this.scale = scale;
		this.special = special;
		this.miniature = miniature;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glEnable(GL2.GL_POINT_SPRITE);
		gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
		
		gl.glPushMatrix();
		{	
			Random generator = new Random();
			
			double rotation = Math.toRadians(trajectory);
			
			float r = generator.nextInt(2) + generator.nextFloat();
			if(special) r *= 1.5;
			if(miniature) r *= 0.5;
			
			float[] _t = {(float) (-r * Math.cos(rotation)), 0, (float) (r * Math.sin(rotation))};
			float[] _c = subtract(c, _t);
			
			gl.glPointSize(30 * scale);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);

			int offset   = (special) ? 4 : 0;
			int spectrum = (special) ? 3 : 3;
			
			textures[offset + generator.nextInt(spectrum)].bind(gl);
			
			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3f(_c[0], _c[1], _c[2]);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_POINT_SPRITE);
	}
	
	@Override
	public void update()
	{
		super.update();
		scale /= 2;
	}
}
