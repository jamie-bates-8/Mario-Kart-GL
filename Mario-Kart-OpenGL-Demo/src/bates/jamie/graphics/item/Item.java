package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Matrix.getRotationMatrix;
import static bates.jamie.graphics.util.Vector.add;
import static bates.jamie.graphics.util.Vector.dot;
import static bates.jamie.graphics.util.Vector.getAngle;
import static bates.jamie.graphics.util.Vector.multiply;
import static bates.jamie.graphics.util.Vector.orient2D;
import static bates.jamie.graphics.util.Vector.subtract;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Quadtree;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;

import com.jogamp.opengl.util.gl2.GLUT;

public abstract class Item
{
	public static final float[] ORIGIN = {0, 0, 0};
	public static final float GLOBAL_RADIUS = 300;
	
	protected static final float BOUND_ALPHA = 0.25f;
	
	public static int renderMode = 0;
	public static boolean smooth = true;
	
	public static boolean boundFrames = false;
	public static boolean boundSolids = false;
	
	protected Scene scene;
	protected Car car;
	
	protected float gravity = 0.05f;
	protected boolean falling = true;
	protected float fallRate = 0.0f;
	protected static final float TOP_FALL_RATE = 5.0f;
	
	public float velocity = 0;
	public float[][] u = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
	public float trajectory = 0;
	
	public Bound bound;
	public boolean colliding = false;
	public List<Bound> collisions = new ArrayList<Bound>();
	protected float[] heights = {0, 0, 0, 0};
	
	protected float[] boundColor;
	
	public boolean thrown = true;
	
	public boolean dead = false;
	
	public static void toggleBoundFrames() { boundFrames = !boundFrames; }
	public static void toggleBoundSolids() { boundSolids = !boundSolids; }
	
	public abstract void render(GL2 gl, float trajectory);
	
	public void renderWireframe(GL2 gl, float trajectory)
	{
		if(smooth)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_LINE_SMOOTH);
		}
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		 
		render(gl, trajectory);
		 
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_LINE_SMOOTH);
	}
	
	public void renderBound(GL2 gl, GLUT glut)
	{
		if(boundFrames) bound.displayWireframe(gl, glut, RGB.BLACK_3F, smooth);
		if(boundSolids) bound.displaySolid(gl, glut, boundColor);
	}
	
	public static int removeItems(Collection<Item> items)
	{
		List<Item> toRemove = new ArrayList<Item>();
		
		for(Item item : items)
			if(item.isDead()) toRemove.add((Item) item);
		
		items.removeAll(toRemove);
		
		return toRemove.size();
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
	
	public abstract boolean canCollide(Item item);
	
	public abstract void collide(Item item);
	
	public abstract void collide(Car car);
	
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
	
	public abstract void update();
	
	public void update(List<Car> cars)
	{
		for(Car car : cars)
		{
			if(car.bound.testBound(bound))
			{
				if(!car.hasStarPower() && !car.isInvisible()) collide(car);
				else if(car.hasStarPower()) destroy();
			}
		}
		if(outOfBounds()) destroy();
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
	
	public float[] getHeights(Terrain map)
	{
		if(map.enableQuadtree) return getHeights(map.tree, map.max_lod);
		
		float[][] vertices = getAxisVectors();

		for(int i = 0; i < 4; i++)
		{
			float h = map.getHeight(vertices[i]);
			heights[i] = h;
		}

		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		
		if(bound.c[1] - bound.getMaximumExtent() <= h)
		{
			h += bound.getMaximumExtent();
			bound.c[1] = h;
			
			falling = thrown = false;
			fallRate = 0;
		}

		return heights;
	}
	
	public float[] getHeights(Quadtree tree, int lod)
	{
		float[][] vertices = getAxisVectors();
		
		for(int i = 0; i < 4; i++)
		{
			Quadtree cell = tree.getCell(vertices[i], lod);
			float h = cell.getHeight(vertices[i]);
			heights[i] = h;
		}
		
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		
		if(bound.c[1] - bound.getMaximumExtent() <= h)
		{
			h += bound.getMaximumExtent();
			bound.c[1] = h;
			
			falling = thrown = false;
			fallRate = 0;
		}
		
		return heights;
	}
	
	public void detectCollisions()
	{
		collisions.clear();

		for(Bound bound : scene.getBounds())
			if(bound.testBound(this.bound))
				collisions.add(bound);
	}
	
	public float[] getPositionVector() { return subtract(bound.c, multiply(u[0], velocity)); }
}
