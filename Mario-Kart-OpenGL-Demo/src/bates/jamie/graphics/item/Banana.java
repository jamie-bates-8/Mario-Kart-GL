package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Renderer.displayGradientObject;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Gradient;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class Banana extends Item
{
	public static final int ID = 8;
	
	private static final List<Face> BANANA_FACES = OBJParser.parseTriangles("banana");
	
	private static final float[] BROWN  = {100,  50,   0};
	private static final float[] YELLOW = {255, 255,  75};
	private static final float[] GREEN  = {100, 255,   0};
	
	private static int bananaList = -1;
	
	private static Texture face;
	private static Gradient gradient;
	
	static
	{
		try { face = TextureIO.newTexture(new File("tex/items/bananaFace.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
		
		gradient = new Gradient(YELLOW, BROWN);
		gradient.addStop(30, YELLOW);
		gradient.addStop(90, GREEN);
	}
	
	public static final float RADIUS = 1.6f;
	
	private int bananaID = 0;
	
	public Banana(GL2 gl, Scene scene, Car car, int id)
	{
		if(bananaList == -1)
		{
			bananaList = gl.glGenLists(1);
			gl.glNewList(bananaList, GL2.GL_COMPILE);
			displayGradientObject(gl, BANANA_FACES, gradient, -1.35f, 1.41f);
		    gl.glEndList();
		}
		
		this.scene = scene;
	    this.car = car;
	    
	    bananaID = id;
		
		bound = new Sphere(new Vec3(), RADIUS);
		boundColor = RGB.toRGBAi(RGB.YELLOW, BOUND_ALPHA);
	}
	
	public Banana(Scene scene, Vec3 c)
	{
		this.scene = scene;
		
		bound = new Sphere(c, RADIUS);
		boundColor = RGB.toRGBAi(RGB.YELLOW, BOUND_ALPHA);
	}
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		velocity /= 2;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		timeQuery.getResult(gl);
		timeQuery.begin(gl);
		
		if(Scene.enableOcclusion)
		{
			boolean visible = occludeQuery.getResult(gl);
			occludeQuery.begin(gl);

			if(!visible)
			{
				renderFacade(gl);
				
				timeQuery.end(gl);
				occludeQuery.end(gl);
				
				return;
			}
		}
		Scene.bananasRendered++;
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			if(thrown) gl.glRotatef(trajectory, 0, -1, 0);
			else gl.glMultMatrixf(u.toArray(), 0);
			
			Shader shader = Shader.enabled ? Shader.getLightModel("phong") : null;
			if(shader != null) shader.enable(gl);
			
			gl.glCallList(bananaList);
			
			Shader.disable(gl);
			
			renderFace(gl);
		}
		gl.glPopMatrix();
		
		timeQuery.end(gl);
		occludeQuery.end(gl);
	}

	private void renderFace(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glEnable(GL2.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL2.GL_GREATER, 0.25f);
			
			gl.glTranslatef(0, 0, 0.325f);
			
			face.bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.35f, -0.35f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.35f,  0.35f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.35f,  0.35f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.35f, -0.35f, 0.0f);
			}
			gl.glEnd();
			
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL2.GL_ALPHA_TEST);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();
		
		float[] heights = scene.enableTerrain ? getHeights(scene.getTerrain()) : getHeights();

		setRotation(getRotationAngles(heights));
		if(thrown) setRotation(-45, trajectory, 0);
	}
	
	@Override
	public void hold()
	{
		setPosition(car.getBackwardItemVector(this, bananaID));
		trajectory = car.trajectory;
	}
	
	@Override
	public boolean canCollide(Item item)
	{
		if(item instanceof Shell) return true;
		
		return false;
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
	public void collide(Car car)
	{
		car.spin();
		destroy();
	}
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
}
