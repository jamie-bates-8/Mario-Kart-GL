package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Renderer.displayPartiallyTexturedObject;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class ItemBox
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("item_box");
	private static float rotation = 45.0f; 
//	public static final float SCALE = 1.75f;
	public static final float SCALE = 3.0f;
	public static final int RESPAWN_TIME = 60;
	
	static Model item_box = OBJParser.parseTriangleMesh("item_box_2");
	
	SceneNode boxNode;
	
	public Reflector reflector;
	
	public boolean initialized = false;
	
	private static Texture questionMark;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/items/questionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private int respawnTimer = 0;
	private Sphere bound;
	
	private ParticleGenerator generator = new ParticleGenerator();
	private List<Particle> particles;
	
	public ItemBox(Vec3 c, List<Particle> particles)
	{
		c.y += 3;
		
		bound = new Sphere(c, 2.5f);
		
		reflector = new Reflector(1.0f);
		
		boxNode = new SceneNode(null, -1, item_box, MatrixOrder.NONE, new Material(new float[] {1, 1, 1}));
		boxNode.setTranslation(c);
		boxNode.setScale(new Vec3(0.75));
		boxNode.setReflector(reflector);
		boxNode.setReflectivity(0.75f);
		boxNode.setRenderMode(RenderMode.REFLECT);
		boxNode.setColor(new float[] {1, 1, 1});
		
		this.particles = particles;
	}
	
	public ItemBox(float x, float y, float z, List<Particle> particles)
	{
		bound = new Sphere(x, y + 3, z, 2.5f);
		
		reflector = new Reflector(1.0f);
		
		boxNode = new SceneNode(null, -1, item_box, MatrixOrder.NONE, new Material(new float[] {1, 1, 1}));
		boxNode.setTranslation(new Vec3(x, y, z));
		boxNode.setScale(new Vec3(4.0));
		boxNode.setReflector(reflector);
		boxNode.setReflectivity(0.75f);
		boxNode.setRenderMode(RenderMode.REFLECT);
		boxNode.setColor(new float[] {1, 1, 1});
		
		this.particles = particles;
	}
	
	public Vec3 getPosition() { return bound.c; }
	
	public static void increaseRotation() { rotation += 4; }
	
	public void render(GL2 gl, float trajectory)
	{
//		renderSymbol(gl, trajectory);
		
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
//			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
			
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			boxNode.renderGhost(gl, 1.0f, Shader.get("aberration"), new float[] {1, 0.7f, 0.7f});

//			displayPartiallyTexturedObject(gl, BOX_FACES, new float[] {0.5f, 0.5f, 0.5f});
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
		
		renderSymbol(gl, trajectory);
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
			gl.glRotatef(180, 0, 0, 1);
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
	
	public void destroy() { respawnTimer = RESPAWN_TIME; }
	
	public boolean isDead() { return respawnTimer > 0; }
	
	public static List<ItemBox> generateSquare(float x, float y, List<Particle> particles)
	{
		List<ItemBox> boxes = new ArrayList<ItemBox>();
		
		boxes.add(new ItemBox( x,  y,  x, particles));
		boxes.add(new ItemBox(-x,  y,  x, particles));
		boxes.add(new ItemBox(-x,  y, -x, particles));
		boxes.add(new ItemBox( x,  y, -x, particles));
		
		return boxes;
	}
	
	public static List<ItemBox> generateDiamond(float x, float y, List<Particle> particles)
	{
		List<ItemBox> boxes = new ArrayList<ItemBox>();
		
		boxes.add(new ItemBox( x,  y,  0, particles));
		boxes.add(new ItemBox(-x,  y,  0, particles));
		boxes.add(new ItemBox( 0,  y,  x, particles));
		boxes.add(new ItemBox( 0,  y, -x, particles));
		
		return boxes;
	}
	
	public void update(List<Car> cars)
	{
		if(!isDead())
		{
			for(Car car : cars)
			{
				if(car.bound.testSphere(bound))
				{
					destroy();
					
					ItemRoulette roulette = car.getRoulette();
					
					if(!roulette.isAlive() && !car.isCursed())
					{
						roulette.spin();
						roulette.secondary = car.hasItem();
					}
					
					particles.addAll(generator.generateItemBoxParticles(getPosition(), 64));
				}
			}
		}
		else respawnTimer--;
	}
}
