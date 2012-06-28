import static graphics.util.Renderer.displayTexturedObject;
import static graphics.util.Renderer.displayColoredObject;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import graphics.util.Face;

import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;


public class FakeItemBox extends Item
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/fakeItemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;

	private float _height = 0;
	
	public ParticleGenerator generator;

	private List<Particle> particles;
	
	private float fade = 0.5f;
	
	private int bounces = 3;
	
	public FakeItemBox(GL2 gl, Car car, float[] c, float velocity, List<Particle> particles)
	{
		this.velocity = velocity;
		this.particles = particles;
		this.car = car;
		
		bound = new OBB(
				c[0], c[1], c[2],
	    		0, 0, 0,
	    		SCALE, SCALE, SCALE);
		
		generator = new ParticleGenerator();
		
		setRotation(0, trajectory, 0);
	}
	
	public List<Particle> generateParticles()
	{
		return generator.generateFakeItemBoxParticles(getPosition(), 64);
	}
	
	public static void increaseRotation() { rotation -= 4; }
	
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
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			if(thrown) displayTexturedObject(gl, BOX_FACES);
			else
			{
				displayColoredObject(gl, BOX_FACES, fade);
				if(fade > 0) fade -= 0.0125f;
			}
			
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
		particles.addAll(generateParticles());
	}
	
	public float getHeights()
	{
		float height = 0;
		
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
						float h = b.perimeterPointToPoint(getPosition())[1] + (bound.getHeight() * 0.45f);
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
			}
		}
		else return _height;
		
		_height = height;
		
		return height;
	}
	
	@Override
	public void update(List<Bound> bounds)
	{
		if(thrown && falling) setPosition(getPositionVector());

		if(falling) fall();
		
		detected.clear();

		for(Bound bound : bounds)
			if(this.bound.testBound(bound))
				detected.add(bound);

		super.update(bounds);

		getHeights();
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
}

