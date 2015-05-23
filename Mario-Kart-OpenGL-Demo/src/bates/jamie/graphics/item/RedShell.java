package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Renderer.displayWildcardObject;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class RedShell extends Shell
{
	public static final int ID = 1;
	
	private static int shellList = -1;
	
	private static Texture[] textures;
	private static Texture shellTop;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "redShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private static final float INITIAL_VELOCITY = 2.0f;
	private static final float TOP_SPEED = 3.0f;
	
	private float acceleration = 0.0125f;
	
	private Vehicle target;
	private boolean locked = true;
	
	public RedShell(GL2 gl, Scene scene, Vehicle car, float trajectory, boolean orbiting)
	{
		super(gl, scene, car, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
		}
	    
		velocity = INITIAL_VELOCITY;
		
		this.orbiting = orbiting;
		
		boundColor = RGB.toRGBA(RGB.DARK_RED, BOUND_ALPHA);
		
		target = seekTarget();
	}

	public RedShell(Scene scene, Vec3 c, float trajectory, boolean modelOnly)
	{	
		super(null, scene, null, trajectory);
		
		bound = new Sphere(c, RADIUS);
		boundColor = RGB.toRGBA(RGB.DARK_RED, BOUND_ALPHA);
		
		velocity = modelOnly ? 0 : INITIAL_VELOCITY;
		
		this.trajectory = trajectory;
		setRotation(0, trajectory, 0);
		
		target = seekTarget();
	}
	
	private Vehicle seekTarget()
	{
		Vehicle target = null;
		
		float min_distance = Float.MAX_VALUE;
		
		for(Vehicle c : scene.getCars())
		{
			if(!c.equals(car))
			{
				float distance = c.getPosition().dot(getPosition());
				
				if(distance < min_distance)
				{
					min_distance = distance;
					target = c;
				}
			}
		}
		
		if(target == null) locked = false;
		
		return target;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			gl.glRotatef(rotation, 0, -1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			Shader shader = Shader.getLightModel("texture");
			if(shader != null) shader.enable(gl);
			
			gl.glCallList(shellList);
			
			rimNode.render(gl);
		}
		gl.glPopMatrix();
		
		Shader.disable(gl);
	}
	
	@Override
	public void update()
	{	
		if(target != null)
			if(target.isInvisible()) locked = false; 
		
		if(velocity < TOP_SPEED && !locked) velocity += acceleration;
		
		setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();
		
		if(!thrown && locked && target != null) trajectory = target.trajectory;
		
		float[] heights = scene.enableTerrain ? getHeights(scene.getTerrain()) : getHeights();
		
		setRotation(getRotationAngles(heights));
		rotation += 10 * velocity;	
	}
	
	@Override
	public Vec3 getPositionVector()
	{
		if(thrown || !locked)
			return getPosition().subtract(u.zAxis.multiply(velocity));
		else
		{
			Vec3  t = target.getPosition().subtract(getPosition());
			Vec3 _t = t.normalize();
			_t.y = 0;
			
			return getPosition().add(_t.multiply(velocity));
		}
	}
	
	@Override
	public void rebound(Bound b) { destroy(); }
	
	@Override
	public void collide(Vehicle car) { car.spin(); destroy(); }
}
