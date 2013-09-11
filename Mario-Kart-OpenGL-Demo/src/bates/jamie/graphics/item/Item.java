package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Vector.orient2D;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import java.util.ArrayList;
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
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.Vec3;

public abstract class Item
{
	public static final String TEXTURE_DIRECTORY = "tex/items/";
	
	public static final float[] ORIGIN = {0, 0, 0};
	public static final float GLOBAL_RADIUS = 300;
	
	protected static final float BOUND_ALPHA = 0.25f;
	
	public int occludeQuery = -1;
	
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
	public RotationMatrix u = new RotationMatrix();
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
	
	public void renderBound(GL2 gl)
	{
		if(boundFrames) bound.displayWireframe(gl, RGB.BLACK_3F, smooth);
		if(boundSolids) bound.displaySolid(gl, boundColor);
	}
	
	/**
	 * This method renders a simplified version of this object so that an occlusion
	 * query can be performed. If <code>false</code> is returned, this means that
	 * the simplified object was not visible; consequently, the original object does
	 * not need to be rendered in the final scene.
	 */
	public void renderFacade(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glColorMask(false, false, false, false);
			gl.glDepthMask(false);
			
			bound.displaySolid(gl, RGB.WHITE_3F);
			
			if(!Scene.depthMode) gl.glColorMask(true, true, true, true);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
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
		setRotation(-45, trajectory, 0);
		
		velocity = car.getThrowVelocity();
	}
	
	public void destroy() { dead = true; }
	
	public Bound getBound() { return bound; };
	
	public Vec3 getPosition() { return bound.c; }
	
	public void setPosition(float x, float y, float z) { bound.setPosition(x, y, z); }
	
	public void setPosition(Vec3 p) { bound.setPosition(p); }
	
	public void setRotation(float x, float y, float z) { u = new RotationMatrix(x, y, z); }
	
	public void setRotation(float[] angles) { u = new RotationMatrix(angles[0], angles[1], angles[2]); }
	
	public abstract boolean canCollide(Item item);
	
	public abstract void collide(Item item);
	
	public abstract void collide(Car car);
	
	public void fall()
	{
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		bound.c.y -= fallRate;
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
		if(Scene.outOfBounds(getPosition())) destroy();
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
		Vec3 face = bound.getFaceVector(getPosition());

		if(bound.isValidCollision(face))
		{
			if(face.equals(bound.getDownVector(1)))
			{
				if(this instanceof Shell) destroy();
				else velocity = 0;
				
				return true;
			}
			else if(!face.equals(bound.getUpVector(1)))
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

		float yrot = trajectory;
		
		float xrot = (float) toDegrees(atan((heights[2] - heights[3]) / diameter));
		float zrot = (float) toDegrees(atan((heights[0] - heights[1]) / diameter));
		
		return new float[] {xrot, yrot, zrot};
	}
	
	public Vec3[] getAxisVectors()
	{
		float radius = getMaximumExtent();
		
		return new Vec3[]
		{
			bound.c.subtract(u.xAxis.multiply(radius)), // left
			bound.c.     add(u.xAxis.multiply(radius)), // right
			bound.c.     add(u.zAxis.multiply(radius)), // back
			bound.c.subtract(u.zAxis.multiply(radius)), // front
		};
	}
	 
	public void rebound(Bound b)
	{
		colliding = true;
		
		Vec3  n = b.getFaceVector(getPosition()); //normal position vector
		Vec3 _n = n.subtract(b.c); //normal translation vector
		
		Vec3  t = getPositionVector(); //item position vector
		Vec3 _t = t.subtract(getPosition()); //item translation vector
		
		Vec3 _i = new Vec3().subtract(_t.multiply(15)); //incident translation vector
		_i.y = 0;
		
		double theta = toDegrees(_n.getAngle(_i));
		
		float orient = orient2D(ORIGIN, new float[] {_n.x, _n.z},
			new float[] {_t.x, _t.z});
		
		double angle = (90 - theta) * 2;

		trajectory += (orient < 0) ? angle : -angle;
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
		Vec3 face = obb.getFaceVector(getPosition());
		
		if(face.equals(obb.getUpVector(1)))
		{
			Vec3[] vertices = getAxisVectors();

			for(int i = 0; i < 4; i++)
			{
				float h = obb.closestPointOnPerimeter(vertices[i]).y;
				if(h > heights[i]) heights[i] = h;
			}

			float h = obb.closestPointOnPerimeter(getPosition()).y
					+ getMaximumExtent();
			
			if(h > bound.c.y) bound.c.y = h;

			falling = thrown = false;
			fallRate = 0;
		}
	}
	
	public float[] getHeights(Terrain map)
	{
		if(map.enableQuadtree) return getHeights(map.trees.values());
		
		Vec3[] vertices = getAxisVectors();

		for(int i = 0; i < 4; i++)
		{
			float[] vertex = vertices[i].toArray();
			
			float h = map.getHeight(vertex);
			heights[i] = h;
		}

		setHeight();

		return heights;
	}
	
	private void setHeight()
	{
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		
		if(bound.c.y - bound.getMaximumExtent() <= h)
		{
			h += bound.getMaximumExtent();
			bound.c.y = h;
			
			falling = thrown = false;
			fallRate = 0;
		}
	}
	
	public float[] getHeights(Collection<Quadtree> trees)
	{
		Vec3[] vertices = getAxisVectors();
		
		Quadtree[] _trees = new Quadtree[4];
		
		for(int i = 0; i < 4; i++)
		{
			float max = 0;
			float[] vertex = vertices[i].toArray();
			
			for(Quadtree tree : trees)
			{
				Quadtree cell = tree.getCell(vertex, tree.detail);
				float h = (cell != null) ? cell.getHeight(vertex) : 0;
				if(h > max && !tree.enableBlending) { max = h; _trees[i] = tree; }
			}

			heights[i] = max;
		}
		
		setHeight();
		
		return heights;
	}
	
	public float[] getHeights(Quadtree tree, int lod)
	{
		Vec3[] vertices = getAxisVectors();
		
		for(int i = 0; i < 4; i++)
		{
			float[] vertex = vertices[i].toArray();
			
			Quadtree cell = tree.getCell(vertex, lod);
			float h = (cell == null) ? 0 : cell.getHeight(vertex);
			heights[i] = h;
		}
		
		setHeight();
		
		return heights;
	}
	
	public void detectCollisions()
	{
		collisions.clear();

		for(Bound bound : scene.getBounds())
			if(bound.testBound(this.bound))
				collisions.add(bound);
	}
	
	public Vec3 getPositionVector() { return bound.c.subtract(u.zAxis.multiply(velocity)); }
}