package bates.jamie.graphics.particle;

import static bates.jamie.graphics.util.Vector.add;
import static bates.jamie.graphics.util.Vector.multiply;

import javax.media.opengl.GL2;

public class WeatherParticle extends Particle
{	
	public float alpha;
	public boolean falling = true;
	
	public WeatherParticle(float[] c, float[] t, float alpha)
	{
		super(c, t, 0, 0);
		
		this.alpha = alpha;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		
	}
	
	public void update(float[] wind)
	{
		c = add(add(c, t), wind);
	}
	
	public float[] getDirectionVector(float[] wind, float scalar)
	{
		return add(add(c, multiply(t, scalar)), multiply(wind, scalar));
	}
}
