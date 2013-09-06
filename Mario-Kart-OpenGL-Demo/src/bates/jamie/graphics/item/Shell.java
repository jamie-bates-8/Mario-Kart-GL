package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Renderer.displayColoredObject;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.scene.OBJParser;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.Vec3;

//TODO Add Shell fragments when a Shell is destroyed

public abstract class Shell extends Item
{
	protected static final List<Face> SHELL_FACES = OBJParser.parseTriangles("shell");
	protected static final List<Face> RIM_FACES   = OBJParser.parseTriangles("shell_rim");
	
	protected static int rimList = -1;
	
	protected static final float RADIUS = 1.5f;
	
	public boolean orbiting;
	public float rotation;
	
	public Shell(GL2 gl, Scene scene, Car car, float trajectory)
	{ 
		if(rimList == -1)
		{
			rimList = gl.glGenLists(1);
			gl.glNewList(rimList, GL2.GL_COMPILE);
		    displayColoredObject(gl, RIM_FACES, RGB.WHITE_3F);
		    gl.glEndList();
		}
		
		this.scene = scene;
	    this.car = car;
	    
		bound = new Sphere(new Vec3(), RADIUS);
		
		rotation = trajectory;
	}

	@Override
	public void render(GL2 gl, float trajectory) {}

	@Override
	public void hold()
	{
		if(orbiting)
		{
			rotation -= 10;
			float radius = car.bound.e.z * 1.2f + Shell.RADIUS;
			
			Vec3 p = car.bound.u.zAxis.multiply(radius);
			
			p = p.multiply(new RotationMatrix(car.bound.u.yAxis, rotation));
			
			setPosition(car.getPosition().add(p));
		}
		else
		{	
			setPosition(car.getBackwardItemVector(this, 1));
			rotation = car.trajectory;
		}
	}
	
	@Override
	public boolean canCollide(Item item)
	{
		if(item instanceof Shell ||
		   item instanceof Banana  ) return true;
		
		return false;
	}

	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell  ||
		   item instanceof Banana   )
		{
			this.destroy();
			item.destroy();
		}
	}
}
