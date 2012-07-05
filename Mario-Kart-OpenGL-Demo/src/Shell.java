
import static graphics.util.Renderer.displayColoredObject;
import static graphics.util.Vector.add;
import static graphics.util.Vector.multiply;
import graphics.util.Face;
import static graphics.util.Matrix.*;

import java.util.List;

import javax.media.opengl.GL2;

public abstract class Shell extends Item
{
	protected static final List<Face> SHELL_FACES = OBJParser.parseTriangles("obj/shell.obj");
	protected static final List<Face> RIM_FACES   = OBJParser.parseTriangles("obj/shellRim.obj");
	
	protected static int rimList = -1;
	
	protected static final float RADIUS = 2.0f;
	
	public boolean orbiting;
	public float rotation;
	
	public Shell(GL2 gl, Car car, float[] c, float trajectory)
	{ 
		if(rimList == -1)
		{
			rimList = gl.glGenLists(1);
			gl.glNewList(rimList, GL2.GL_COMPILE);
		    displayColoredObject(gl, RIM_FACES, new float[] {1, 1, 1});
		    gl.glEndList();
		}
		
	    this.car = car;
	    
		bound = new Sphere(c, RADIUS);
		
		setRotation(0, trajectory, 0);
		this.trajectory = trajectory;
		
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
			float radius = car.bound.e[0] * 1.1f + Shell.RADIUS;
			
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
