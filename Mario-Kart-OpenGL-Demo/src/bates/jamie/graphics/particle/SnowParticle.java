package bates.jamie.graphics.particle;

import static bates.jamie.graphics.util.Vector.add;

import javax.media.opengl.GL2;

public class SnowParticle extends Particle
{	
	public static float[] wind = {0.1f, -2.0f, 0.2f};
	
	public SnowParticle(float[] c, float[] t)
	{
		super(c, t, 0, 0);
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		
	}
	
	public void update()
	{
		c = add(add(c, t), wind);
	}
}
