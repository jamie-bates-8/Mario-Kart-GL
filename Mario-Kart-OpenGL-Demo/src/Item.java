
import static graphics.util.Matrix.getRotationMatrix;
import static graphics.util.Vector.add;
import static graphics.util.Vector.getAngle;
import static graphics.util.Vector.multiply;
import static graphics.util.Vector.orient2D;
import static graphics.util.Vector.subtract;
import static graphics.util.Vector.dot;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public abstract class Item
{
	public static final float[] ORIGIN = {0, 0, 0};
	public static final float GLOBAL_RADIUS = 300;
	
	public static boolean enableBoundWireframes = false;
	public static boolean enableBoundSolids = false;
	
	public Car car;
	
	public double gravity = 0.05;
	public boolean falling = true;
	public float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 2.5;
	
	public float velocity = 0;
	public float[][] u;
	public float trajectory = 0;
	
	public Bound bound;
	public boolean colliding = false;
	public List<Bound> collisions = new ArrayList<Bound>();
	protected float[] heights = {0, 0, 0, 0};
	
	public boolean thrown = true;
	
	public boolean dead = false;
	
	public static void toggleBoundWireframes() { enableBoundWireframes = !enableBoundWireframes; }
	public static void toggleBoundSolids() { enableBoundSolids = !enableBoundSolids; }
	
	public abstract void render(GL2 gl, float trajectory);
	
	public void displayBoundVisuals(GL2 gl, GLUT glut, float[] color)
	{
		if(enableBoundWireframes) bound.displayWireframe(gl, glut, color);
		if(enableBoundSolids) bound.displaySolid(gl, glut, color);
	}
	
	public float getMaximumExtent() { return bound.getMaximumExtent(); }
	
	public boolean isDead() { return dead; }
	
	public abstract void hold();
	
	public void throwForwards()
	{
		trajectory = car.trajectory;
		setPosition(car.getForwardItemVector(this));
		setRotation(0, trajectory, 0);
	}
	
	public void throwBackwards()
	{
		trajectory = car.trajectory - 180;
		setPosition(car.getBackwardItemVector(this));
		setRotation(0, trajectory - 180, 0);
	}
	
	public void throwUpwards()
	{
		trajectory = car.trajectory;
		setPosition(car.getUpItemVector(this));
		setRotation(0, trajectory, -45);
		
		velocity = car.getThrowVelocity();
	}
	
	public void destroy() { dead = true; }
	
	public Bound getBound() { return bound; };
	
	public float[] getPosition() { return bound.c; }
	
	public void setPosition(float x, float y, float z) { bound.setPosition(x, y, z); }
	
	public void setPosition(float[] p) { bound.setPosition(p); }
	
	public void setRotation(float x, float y, float z) { u = getRotationMatrix(x, y, z); }
	
	public void setRotation(float[] angles) { u = getRotationMatrix(angles[0], angles[1], angles[2]); }
	
	public abstract void collide(Item item);
	
	public void fall()
	{
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		bound.c[1] -= fallRate;
	}
	
	public boolean outOfBounds()
	{
		float[] p = getPosition();
		return dot(p, p) > GLOBAL_RADIUS * GLOBAL_RADIUS;
	} 
	
	public abstract void update(List<Bound> bounds);
	
	public void update(Car car)
	{
		if(car.bound.testBound(bound))
		{
			if(!car.hasStarPower() && !car.isInvisible())
			{
				if(this instanceof Banana) car.slipOnBanana();
				else if(this instanceof FakeItemBox) car.curse();
				
				destroy();
			}
			else if(car.hasStarPower()) destroy();
		}
		else if(outOfBounds()) destroy();
	}
	
	public void resolveCollisions()
	{
		boolean _colliding = false;

		for(Bound bound : collisions)
		{
			if(bound instanceof OBB) _colliding = resolveOBB((OBB) bound);
		}
		colliding = _colliding;
	}
	
	private boolean resolveOBB(OBB bound)
	{
		float[] face = bound.getFaceVector(getPosition());

		if(bound.isValidCollision(face))
		{
			if(Arrays.equals(face, bound.getDownVector(1)))
			{
				if(this instanceof Shell) destroy();
				else velocity = 0;
				
				return true;
			}
			else if(!Arrays.equals(face, bound.getUpVector(1)))
			{
				if(!colliding) rebound(bound);
				
				return true;
			}	
		}
		return false;
	}
	
	public float[] getRotationAngles(float[] heights)
	{
		float diameter = getMaximumExtent() * 2;

		float xrot = (float) -toDegrees(atan((heights[2] - heights[3]) / diameter));
		float yrot = trajectory;
		float zrot = (float) -toDegrees(atan((heights[0] - heights[1]) / diameter));
		
		return new float[] {xrot, yrot, zrot};
	}
	
	public float[][] getAxisVectors()
	{
		float radius = getMaximumExtent();
		
		return new float[][]
		{
			subtract(bound.c, multiply(u[0],  radius)), //front
				 add(bound.c, multiply(u[0],  radius)), //back
				 add(bound.c, multiply(u[2],  radius)), //left
			subtract(bound.c, multiply(u[2],  radius)), //right
		};
	}
	 
	public void rebound(Bound b)
	{
		colliding = true;
		
		float[]  n = b.getFaceVector(getPosition()); //normal position vector
		float[] _n = subtract(n, b.c); //normal translation vector
		
		float[]  t = getPositionVector(); //item position vector
		float[] _t = subtract(t, getPosition()); //item translation vector
		
		float[] _i = subtract(ORIGIN, multiply(_t, 15)); //incident translation vector
		_i[1] = 0;
		
		double theta = toDegrees(getAngle(_n, _i));
		
		float orient = orient2D(ORIGIN, new float[] {_n[0], _n[2]},
			new float[] {_t[0], _t[2]});
		
		double angle = (90 - theta) * 2;
		
		trajectory += (orient > 0) ? angle : -angle;
		trajectory %= 360;
	}
	
	public float[] getHeights()
	{
		float[] _heights = heights;
		heights = new float[] {0, 0, 0, 0};
		
		falling = true;
		
		for(Bound collision : collisions)
		{
			if(collision instanceof OBB) setHeights((OBB) collision);
		}
		
		if(collisions.isEmpty()) heights = _heights;
		
		return heights;
	}
	
	private void setHeights(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());
		
		if(Arrays.equals(face, obb.getUpVector(1)))
		{
			float[][] vertices = getAxisVectors();

			for(int i = 0; i < 4; i++)
			{
				float h = obb.closestPointOnPerimeter(vertices[i])[1];
				if(h > heights[i]) heights[i] = h;
			}

			float h = obb.closestPointOnPerimeter(getPosition())[1]
					+ getMaximumExtent();
			
			if(h > bound.c[1]) bound.c[1] = h;

			falling = thrown = false;
			fallRate = 0;
		}
	}
	
	public void detectCollisions(List<Bound> bounds)
	{
		collisions.clear();

		for(Bound bound : bounds)
			if(bound.testBound(this.bound))
				collisions.add(bound);
	}
	
	public float[] getPositionVector() { return subtract(bound.c, multiply(u[0], velocity)); }
}
