package bates.jamie.graphics.item;
import static bates.jamie.graphics.util.Renderer.displayWildcardObject;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class GreenShell extends Shell
{	
	public static final int ID = 0;
	
	private static int shellList = -1;
	
	private static Texture[] textures;
	private static Texture shellTop;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "greenShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static final float MAX_VELOCITY = 2.0f;
	public static final float MIN_VELOCITY = 1.0f;
	public static final float ACCELERATION = 0.0025f;
	public static final int MAX_DURABILITY = 10;
	
	public int durability;
	
	public GreenShell(GL2 gl, Scene scene, Car car, float trajectory, boolean orbiting)
	{
		super(gl, scene, car, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
		}
	    
		velocity = MAX_VELOCITY;
		durability = MAX_DURABILITY;
		
		this.orbiting = orbiting;
		
		boundColor = RGB.toRGBAi(RGB.GREEN, BOUND_ALPHA);
	}
	
	public GreenShell(Scene scene, Vec3 c, float trajectory, boolean modelOnly)
	{	
		super(null, scene, null, trajectory);
		
		bound = new Sphere(c, RADIUS);
		boundColor = RGB.toRGBAi(RGB.GREEN, BOUND_ALPHA);
		
		velocity = modelOnly ? 0 : MAX_VELOCITY;
		durability = MAX_DURABILITY;
		
		this.trajectory = trajectory;
		setRotation(0, trajectory, 0);
	}
	
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			gl.glRotatef(rotation, 0, -1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			Shader shader = Shader.enabled ? Shader.getLightModel("texture") : null;
			if(shader != null) shader.enable(gl);
			
			gl.glCallList(shellList);
			
			shader = Shader.enabled ? Shader.getLightModel("phong") : null;
			if(shader != null) shader.enable(gl);
			
			gl.glCallList(rimList);
		}
		gl.glPopMatrix();
		
		Shader.disable(gl);
	}
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		durability--;
	}
	
	public void decelerate() { if(velocity > MIN_VELOCITY) velocity -= ACCELERATION; }
	
	@Override
	public void update()
	{
		setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();

		decelerate();
		
		float[] heights = scene.enableTerrain ? getHeights(scene.getTerrain()) : getHeights();

		setRotation(getRotationAngles(heights));
		rotation += 10 * velocity;
		
		if(durability < 1) destroy();
	}
	
	@Override
	public void collide(Car car) { car.spin(); destroy(); }
}
