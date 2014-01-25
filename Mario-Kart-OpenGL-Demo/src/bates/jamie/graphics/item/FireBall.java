package bates.jamie.graphics.item;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;

public class FireBall extends Item
{
	public static final int ID = 15;
	
	public static final float RADIUS = 1.7f;
	public static final float MAX_VELOCITY = 1.0f;
	
	private Vec3 bounceVector = new Vec3();
	private ParticleGenerator flameGenerator;
	
	public FireBall(GL2 gl, Scene scene, Car car, int id)
	{
		this.scene = scene;
	    this.car = car;
		
		bound = new Sphere(new Vec3(), RADIUS);
		boundColor = RGB.toRGBAi(RGB.RED, BOUND_ALPHA);
		
		flameGenerator = new ParticleGenerator(1, 5, ParticleGenerator.GeneratorType.FIRE, new Vec3());
		scene.generators.add(flameGenerator);
	}
	
	public FireBall(Scene scene, Vec3 c, float trajectory)
	{
		this.scene = scene;
		
		bound = new Sphere(c, RADIUS);
		boundColor = RGB.toRGBAi(RGB.RED, BOUND_ALPHA);
		
		flameGenerator = new ParticleGenerator(1, 5, ParticleGenerator.GeneratorType.FIRE, new Vec3(c));
		scene.generators.add(flameGenerator);
		
		velocity = MAX_VELOCITY;
		
		this.trajectory = trajectory;
		setRotation(0, trajectory, 0);
	}
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
//		velocity *= 0.75;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		
	}
	
	@Override
	public void update()
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		bound.c = bound.c.add(bounceVector);
		
		flameGenerator.setSource(getPosition());
		
		detectCollisions();
		resolveCollisions();
		
		if(scene.enableTerrain) getHeights(scene.getTerrain());
		else getHeights();

		if(thrown) setRotation(0, trajectory, 0);
	}
	
	@Override
	public float[] getHeights()
	{
		falling = true;
		
		for(Bound collision : collisions)
		{
			if(collision instanceof OBB)
			{		
				setHeights((OBB) collision);
			}
		}
		return heights;
	}

	private void setHeights(OBB obb)
	{
		Vec3 face = obb.getFaceVector(getPosition());

		if(face.equals(obb.getUpVector(1)))
		{
			float h = obb.closestPointOnPerimeter(getPosition()).y
					+ bound.getMaximumExtent();
			
			if(h > bound.c.y) bound.c.y = h;

			bounce();
		}
	}

	private void bounce()
	{
		fallRate = 0;
		bounceVector = new Vec3(0, 0.66, 0);
	}
	
	@Override
	public float[] getHeights(Terrain map)
	{
		float h = map.getHeight(getPosition().toArray());
		
		if(bound.c.y - bound.getMaximumExtent() <= h) bounce();

		return heights;
	}
	
	@Override
	public void hold()
	{
		
	}
	
	@Override
	public boolean canCollide(Item item)
	{
		if(item instanceof Shell    ||
		   item instanceof Banana   ||
		   item instanceof FireBall   ) return true;
		
		return false;
	}

	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell    ||
		   item instanceof Banana   ||
		   item instanceof FireBall   )
		{
			this.destroy();
			item.destroy();
		}
	}
	
	@Override
	public void collide(Car car)
	{
		car.spin();
		destroy();
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		scene.generators.remove(flameGenerator);
	}
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
}
