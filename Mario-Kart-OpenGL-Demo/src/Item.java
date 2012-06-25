import static graphics.util.Matrix.getRotationMatrix33;
import static graphics.util.Vector.getAngle;
import static graphics.util.Vector.multiply;
import static graphics.util.Vector.orient2D;
import static graphics.util.Vector.subtract;
import static graphics.util.Vector.dot;
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
	public boolean falling = false;
	public float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 5.0;
	
	public float velocity = 0;
	public float[][] u;
	public float trajectory = 0;
	
	public Bound bound;
	public boolean colliding = false;
	public List<Bound> detected = new ArrayList<Bound>();
	
	public boolean thrown = false;
	public boolean held = true;
	public boolean dead = false;
	
	public static void toggleBoundWireframes() { enableBoundWireframes = !enableBoundWireframes; }
	public static void toggleBoundSolids() { enableBoundSolids = !enableBoundSolids; }
	
	public abstract void render(GL2 gl);
	
	public void displayBoundVisuals(GL2 gl, GLUT glut, float[] color)
	{
		if(enableBoundWireframes) bound.displayWireframe(gl, glut, color);
		if(enableBoundSolids) bound.displaySolid(gl, glut, color);
	}
	
	public float getMaximumExtent() { return bound.getMaximumExtent(); }
	
	public boolean isDead() { return dead; }
	
	public abstract void hold();
	
	public void destroy() { dead = true; }
	
	public Bound getBound() { return bound; };
	
	public float[] getPosition() { return bound.c; }
	
	public void setPosition(float x, float y, float z) { bound.setPosition(x, y, z); }
	
	public void setPosition(float[] p) { bound.setPosition(p); }
	
	public void setRotation(float x, float y, float z) { u = getRotationMatrix33(x, y, z); }
	
	public void setRotation(float[] angles) { u = getRotationMatrix33(angles[0], angles[1], angles[2]); }
	
	public abstract void collide(Item item);
	
	public void fall()
	{
		if(falling)
		{
			if(fallRate < TOP_FALL_RATE) fallRate += gravity;
			bound.c[1] -= fallRate;
		}
	}
	
	public boolean outOfBounds()
	{
		float[] p = getPosition();
		return dot(p, p) > GLOBAL_RADIUS * GLOBAL_RADIUS;
	} 
	
	public void update(List<Bound> bounds)
	{
		boolean _colliding = false;

		if(!detected.isEmpty())
		{
			for(Bound bound : detected)
			{
				if(bound instanceof OBB)
				{
					OBB b = (OBB) bound;

					float[] face = b.getFaceVector(getPosition());

					if(b.isValidCollision(face))
					{
						if(!Arrays.equals(face, b.getUpVector(1)) && !Arrays.equals(face, b.getDownVector(1)))
						{
							if(!colliding) rebound(bound);
							_colliding = true;
						}
						else if(Arrays.equals(face, b.getDownVector(1)))
						{
							velocity = 0;
							_colliding = true;
						}
					}
				}
			}
			if(_colliding) colliding = true;
		}
		if(!_colliding) colliding = false;
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
	}
	
	public float[] getPositionVector() { return subtract(bound.c, multiply(u[0], velocity)); }
}
