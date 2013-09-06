package bates.jamie.graphics.particle;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

public class WeatherParticle extends Particle
{	
	public float alpha;
	public boolean falling = true;
	
	public WeatherParticle(Vec3 c, Vec3 t, float alpha)
	{
		super(c, t, 0, 0);
		
		this.alpha = alpha;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		
	}
	
	public void update(Vec3 wind)
	{
		c = c.add(t).add(wind);
	}
	
	public Vec3 getDirectionVector(Vec3 wind, float scalar)
	{
		Vec3 _w = wind.multiply(scalar);
		Vec3 _t =    t.multiply(scalar);
		
		return c.add(_t).add(_w);
	}
}
