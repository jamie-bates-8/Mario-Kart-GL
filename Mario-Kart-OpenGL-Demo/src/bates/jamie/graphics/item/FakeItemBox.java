package bates.jamie.graphics.item;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

//TODO Fake Item Boxes should rebound off slopes

public class FakeItemBox extends Item
{
	public static final int ID = 7;
	
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.8f;
	
	private static Texture questionMark;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/items/fakeQuestionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public ParticleGenerator generator;
	
	private int bounces = 3;
	
	public Reflector reflector;
	
	SceneNode boxNode;
	SceneNode symbolNode;
	
	private boolean simplify = false;
	
	public FakeItemBox(GL2 gl, Scene scene, Vehicle car)
	{
		this.scene = scene;
		this.car = car;
		
		bound = new Sphere(new Vec3(), 2.5f);
		boundColor = RGB.toRGBA(RGB.RED, BOUND_ALPHA);
		
		generator = new ParticleGenerator();
		
		gravity = 0.025f;
		
		setupGraph(new Vec3());
	}
	
	public FakeItemBox(Scene scene, Vec3 c, float trajectory)
	{
		this.scene = scene;
		
		bound = new Sphere(c, 2.5f);
		boundColor = RGB.toRGBA(RGB.RED, BOUND_ALPHA);
		
		generator = new ParticleGenerator();
		
		gravity = 0.025f;
		
		this.trajectory = trajectory;
		setRotation(0, trajectory, 0);
		
		velocity = 1.0f;
		
		setupGraph(c);
	}
	
	private void setupGraph(Vec3 c)
	{
		reflector = new Reflector(1.0f, 160, true);
		
		boxNode = new SceneNode(ItemBox.item_box);
		boxNode.setMatrixOrder(MatrixOrder.NONE);
		boxNode.setReflector(reflector);
		boxNode.setReflectivity(0.75f);
		boxNode.setRenderMode(RenderMode.REFLECT);
		
		symbolNode = new SceneNode(ItemBox.question_mark);
		symbolNode.setTranslation(c);
		symbolNode.setScale(new Vec3(-1.25, -1.25, 1.25));
		symbolNode.setRenderMode(RenderMode.BLOOM_COLOR);
		symbolNode.setColor(new float[] {1, 0.4f, 0.4f});
		symbolNode.enableBloom(true);
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
		if(simplify) renderSymbol(gl, trajectory);
		else if(!Scene.depthMode)
		{
			symbolNode.setTranslation(getPosition());
			symbolNode.setRotation(new Vec3(0, -trajectory, 0));
			symbolNode.render(gl);
		}
		
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			gl.glRotated(rotation, 0.0, Math.cos(Scene.sceneTimer * (Math.PI / 90.0)),
					                   -Math.sin(Scene.sceneTimer * (Math.PI / 90.0)));
			gl.glScalef(SCALE, SCALE, SCALE);
			
			Shader shader = Shader.get("fake_box");
			shader.enable(gl);
			
			shader.setUniform(gl, "timer", (float) Scene.sceneTimer % 1000);
			
			boxNode.renderGhost(gl, 1.0f, shader);
			
			Shader.disable(gl);
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}

	private void renderSymbol(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDepthMask(false);
			
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			gl.glRotatef(trajectory, 0, -1, 0);
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
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		scene.addParticles(generator.generateFakeItemBoxParticles(getPosition(), 24, false, null));
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
		Vec3 face = obb.getFaceVector(getPosition());

		if(face.equals(obb.getUpVector(1)))
		{
			float h = obb.closestPointOnPerimeter(getPosition()).y
					+ bound.getMaximumExtent();
			
			if(h > bound.c.y) bound.c.y = h;

			bounce();
		}
	}

	private void bounce()
	{
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
	
	@Override
	public float[] getHeights(Terrain map)
	{
		float h = map.getHeight(getPosition().toArray());
		
		if(bound.c.y - bound.getMaximumExtent() <= h) bounce();

		return heights;
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
		
		reflector.isDynamic(thrown || falling);
		
		detectCollisions();
		resolveCollisions();

		if(falling)
		{
			if(scene.enableTerrain) getHeights(scene.getTerrain());
			else getHeights();
		}
		
		if(thrown) setRotation(-45, trajectory, 0);
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
	public void collide(Vehicle car)
	{
		car.curse();
		destroy();
	}
}

