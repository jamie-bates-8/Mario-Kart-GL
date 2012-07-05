
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


public class FakeItemBox extends Item
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/fakeItemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;
	
	private static Texture questionMark;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/fakeQuestionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public ParticleGenerator generator;

	private List<Particle> particles;
	
	private int bounces = 3;
	
	public FakeItemBox(GL2 gl, Car car, float[] c, float velocity, List<Particle> particles)
	{
		this.velocity = velocity;
		this.particles = particles;
		this.car = car;
		
		bound = new Sphere(c, 2.5f);
		
		generator = new ParticleGenerator();
		
		gravity = 0.025;
		
		setRotation(0, trajectory, 0);
	}
	
	public static void increaseRotation() { rotation -= 4; }
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		velocity *= 0.75f;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_DST_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
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
		particles.addAll(generator.generateFakeItemBoxParticles(getPosition(), 64));
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
	public void update(List<Bound> bounds)
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions(bounds);
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
	public void collide(Item item) {}
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
}

