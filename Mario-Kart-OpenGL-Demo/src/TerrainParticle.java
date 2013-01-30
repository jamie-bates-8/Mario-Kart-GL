import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

public class TerrainParticle extends Particle
{
	private double gravity = 0.1;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 10.0;
	
	private float[] texCoords;
	private Texture texture;
	
	public TerrainParticle(float[] c, float[] t, float rotation, int duration,
			float[] texCoords, Texture texture)
	{
		super(c, t, rotation, duration);
		
		this.texCoords = texCoords;
		this.texture = texture;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{	
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(3);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
			gl.glDisable(GL2.GL_LIGHTING);
			
			texture.bind(gl);
			
			gl.glBegin(GL2.GL_POINTS);
			{
				gl.glTexCoord2f(texCoords[0], texCoords[1]);
				gl.glVertex3f(c[0], c[1], c[2]);
			}
			gl.glEnd();

			gl.glDisable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
			gl.glDisable(GL2.GL_POINT_SMOOTH);
			gl.glEnable(GL2.GL_LIGHTING);
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

