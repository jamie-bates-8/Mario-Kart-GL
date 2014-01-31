package bates.jamie.graphics.particle;

import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec2;

public class RainDrop
{
	private static final int RESPAWN_COUNTER = 120;
	private static final Vec2 GRAVITY_VECTOR = new Vec2(0.0, 4.0);
	private static Random generator = new Random();
	
	private Vec2 position = new Vec2();
	private int  duration;
	private Vec2 direction;
	
	private boolean stationary = false;
	
	private float acceleration = 1.0f;
	private float pointSize = 75.0f;
	
	public RainDrop()
	{
		recycle();
	}
	
	public float getSize() { return pointSize; }

	private void recycle()
	{
		position = new Vec2(Scene.singleton.getWidth()  * generator.nextFloat(),
				            Scene.singleton.getHeight() * generator.nextFloat() - 200);
		direction = Vec2.getRandomVector();
		
		stationary = generator.nextFloat() < 0.2;
		
		acceleration = 0.75f + generator.nextFloat() * 0.5f;
		
		pointSize = 20.0f + generator.nextFloat() * 10.0f;
		
		if(stationary)
		{
			duration = 2 + generator.nextInt(8);
			pointSize *= generator.nextFloat() * 2.0f;
		}
		else duration = 180 + generator.nextInt(60);
	}
	
	public void render(GL2 gl)
	{
		gl.glPointSize(pointSize - 5.0f + (generator.nextFloat() * 10.0f));
		
		gl.glBegin(GL2.GL_POINTS);
		{
			gl.glVertex2f(position.x, position.y);
		}
		gl.glEnd();
	}
	
	public Vec2 getPosition() { return position; }
	
	public boolean shouldRender() { return !stationary || !isDead(); }
	
	public boolean isDead() { return (duration < 1); }
	
	public void update()
	{
		if(generator.nextFloat() < 0.20) direction = Vec2.getRandomVector();
		if(generator.nextFloat() < 0.05) acceleration += 0.05;
		if(generator.nextFloat() < 0.05) acceleration -= 0.05;
		
		if(acceleration <= 0.0) acceleration = 0.05f;
		
		if(!stationary)
		{
			if(isDead()) acceleration *= 0.99;
			
			position = position.add(direction.multiply(acceleration));
		    position = position.add(GRAVITY_VECTOR.multiply(acceleration));
		}
		
		duration--;
		
		if(duration < -RESPAWN_COUNTER) recycle();
	}
	
	
}
