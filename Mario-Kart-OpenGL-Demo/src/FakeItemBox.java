import static graphics.util.Renderer.displayPartiallyTexturedObject;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import graphics.util.Face;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

//TODO Fake Item Boxes should rebound off slopes

public class FakeItemBox extends Item
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/fakeItemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;
	
	private static Texture questionMark;
	
	private static int boxList = -1;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/fakeQuestionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public ParticleGenerator generator;
	
	private int bounces = 3;
	
	public FakeItemBox(GL2 gl, Scene scene, Car car)
	{
		this.scene = scene;
		this.car = car;
		
		bound = new Sphere(new float[] {0, 0, 0}, 2.5f);
		
		generator = new ParticleGenerator();
		
		gravity = 0.025;
		
		if(boxList == -1)
		{
			boxList = gl.glGenLists(1);
			gl.glNewList(boxList, GL2.GL_COMPILE);
			displayPartiallyTexturedObject(gl, BOX_FACES, new float[] {1.0f, 0.5f, 0.5f});
		    gl.glEndList();
		    
		    System.out.println("Fake Item Box: " + BOX_FACES.size() + " faces");
		}
	}
	
	public FakeItemBox(Scene scene, float[] c, float trajectory)
	{
		this.scene = scene;
		
		bound = new Sphere(c, 2.5f);
		
		generator = new ParticleGenerator();
		
		gravity = 0.025;
		
		this.trajectory = trajectory;
		setRotation(0, trajectory, 0);
		
		velocity = 1.0f;
	}
	
	public static void increaseRotation() { rotation -= 4; }
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		velocity *= 0.75f;
		if(bounces > 0) bounces--;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDepthMask(false);
			
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glScalef(3.0f, 3.0f, 3.0f);
			
			questionMark.bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			displayPartiallyTexturedObject(gl, BOX_FACES, new float[] {1.0f, 0.5f, 0.5f});
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		scene.addParticles(generator.generateFakeItemBoxParticles(getPosition(), 64, false));
	}
	
	@Override
	public float[] getHeights()
	{
		falling = true;
		
		for(Bound collision : collisions)
		{
			if(collision instanceof OBB)
			{		
				setHeights((OBB) collision);
			}
		}
		return heights;
	}

	private void setHeights(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());

		if(Arrays.equals(face, obb.getUpVector(1)))
		{
			float h = obb.closestPointOnPerimeter(getPosition())[1]
					+ bound.getMaximumExtent();
			
			if(h > bound.c[1]) bound.c[1] = h;

			if(bounces == 0)
			{
				falling = thrown = false;
				fallRate = 0;
			}
			else
			{
				bounces--;
				fallRate /= 2;
			}
		}
	}
	
	@Override
	public void throwUpwards()
	{
		super.throwUpwards();
		velocity *= 0.75f;
	}
	
	@Override
	public void update()
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();

		if(falling) getHeights();
		if(thrown) setRotation(0, trajectory, -45);
	}
	
	@Override
	public void hold()
	{
		setPosition(car.getBackwardItemVector(this, 1));
		trajectory = car.trajectory;
	}
	
	@Override
	public boolean canCollide(Item item) { return false; }
	
	@Override
	public void collide(Item item) {}
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
	
	@Override
	public void collide(Car car)
	{
		car.curse();
		destroy();
	}
}

