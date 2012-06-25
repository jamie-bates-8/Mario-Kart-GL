import static graphics.util.Matrix.getRotationMatrix44;
import static graphics.util.Renderer.*;
import static graphics.util.Vector.add;
import static graphics.util.Vector.multiply;
import static graphics.util.Vector.subtract;

import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import graphics.util.Face;

import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;


public class Banana extends Item
{
	private static final List<Face> BANANA_FACES = OBJParser.parseTriangles("obj/banana.obj");
	
	private static int bananaList = -1;
	
	public static final float RADIUS = 2.0f;
	
	public int bananaID = 0;
	
	private float[] _heights = {0, 0, 0, 0};
	
	public Banana(GL2 gl, Car car, float[] c, float trajectory, int id)
	{
		if(bananaList == -1)
		{
			bananaList = gl.glGenLists(1);
			gl.glNewList(bananaList, GL2.GL_COMPILE);
		    displayPartiallyTexturedObject(gl, BANANA_FACES, new float[] {1, 0.976f, 0.306f});
		    gl.glEndList();
		}
		
	    this.car = car;
	    
	    bananaID = id;
		
		bound = new Sphere(c, RADIUS);
		setRotation(0, trajectory, 0);
		this.trajectory = trajectory;
	}
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		velocity /= 2;
	}
	
	@Override
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			if(thrown || held) gl.glRotatef(trajectory, 0, 1, 0);
			else gl.glMultMatrixf(getRotationMatrix44(u), 0);
			gl.glScalef(1.4f, 1.4f, 1.4f);
			
			gl.glCallList(bananaList);
		}
		gl.glPopMatrix();
	}
	
	public float[] getHeights()
	{
		float[] heights = {0, 0, 0, 0};
		
		falling = true;
		
		if(!detected.isEmpty())
		{
			for(Bound _bound : detected)
			{
				if(_bound instanceof OBB)
				{		
					OBB b = (OBB) _bound;
					
					float[] face = b.getFaceVector(getPosition());
	
					if(Arrays.equals(face, b.getUpVector(1)))
					{
						float[][] vertices = getAxisVectors();
						
						for(int i = 0; i < 4; i++)
						{
							float h = b.perimeterPointToPoint(vertices[i])[1];
							if(h > heights[i]) heights[i] = h;
						}
			
						float h = b.perimeterPointToPoint(getPosition())[1] + (getMaximumExtent() * 0.9f);
						if(h > bound.c[1]) bound.c[1] = h;
						
						falling = thrown = false;
						fallRate = 0;
					}
				}
			}
		}
		else return _heights;
		
		_heights = heights;
		
		return heights;
	}
	
	public float[][] getAxisVectors()
	{
		return new float[][]
			{
				subtract(bound.c, multiply(new float[] {u[0][0], u[0][1], u[0][2]},  RADIUS)), //front
					 add(bound.c, multiply(new float[] {u[0][0], u[0][1], u[0][2]},  RADIUS)), //back
					 add(bound.c, multiply(new float[] {u[2][0], u[2][1], u[2][2]},  RADIUS)), //left
				subtract(bound.c, multiply(new float[] {u[2][0], u[2][1], u[2][2]},  RADIUS)), //right
			};
	}
	
	public float[] getRotationAngles(float[] heights)
	{
		float diameter = Banana.RADIUS * 2;

		float xrot = (float) -toDegrees(atan((heights[2] - heights[3]) / diameter));
		float yrot = trajectory;
		float zrot = (float) -toDegrees(atan((heights[0] - heights[1]) / diameter));
		
		return new float[] {xrot, yrot, zrot};
	}
	
	@Override
	public void update(List<Bound> bounds)
	{
		if(thrown && falling) setPosition(getPositionVector());
		
		fall();
		
		detected.clear();
		
		for(Bound bound : bounds)
			if(bound.testBound(this.bound))
				detected.add(bound);
			
		super.update(bounds);

		setRotation(getRotationAngles(getHeights()));
		if(thrown) setRotation(0, trajectory, -45);
	}
	
	@Override
	public void hold()
	{
		setPosition(car.getBackwardItemVector(this, bananaID));
		trajectory = car.trajectory;
	}
	
	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell)
		{
			this.destroy();
			item.destroy();
		}
	}
}
