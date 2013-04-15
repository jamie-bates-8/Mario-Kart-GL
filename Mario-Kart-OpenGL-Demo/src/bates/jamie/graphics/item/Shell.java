package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Matrix.*;
import static bates.jamie.graphics.util.Renderer.displayColoredObject;
import static bates.jamie.graphics.util.Vector.add;
import static bates.jamie.graphics.util.Vector.multiply;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.scene.OBJParser;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.RGB;

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
	    
		bound = new Sphere(new float[] {0, 0, 0}, RADIUS);
		
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
			float radius = car.bound.e[0] * 1.2f + Shell.RADIUS;
			
			float[] p = multiply(car.bound.u[0], radius);
			
			p = multiply(p, getRotationMatrix(car.bound.u[1], rotation));
			
			setPosition(add(car.getPosition(), p));
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
