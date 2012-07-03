import static graphics.util.Matrix.getRotationMatrix;
import static graphics.util.Renderer.*;

import graphics.util.Face;

import java.util.List;

import javax.media.opengl.GL2;


public class Banana extends Item
{
	private static final List<Face> BANANA_FACES = OBJParser.parseTriangles("obj/banana.obj");
	
	private static int bananaList = -1;
	
	public static final float RADIUS = 1.8f;
	
	private int bananaID = 0;
	
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
			if(thrown) gl.glRotatef(trajectory, 0, 1, 0);
			else gl.glMultMatrixf(getRotationMatrix(u), 0);
			gl.glScalef(1.0f, 1.0f, 1.0f);
			
			gl.glCallList(bananaList);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update(List<Bound> bounds)
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions(bounds);
		resolveCollisions();

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
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
}
