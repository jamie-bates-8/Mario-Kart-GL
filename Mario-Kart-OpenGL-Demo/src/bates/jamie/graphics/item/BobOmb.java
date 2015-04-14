package bates.jamie.graphics.item;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.particle.BlastParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

public class BobOmb extends Item
{
	private static final float RADIUS = 1.6f;

	public static final int ID = 14;
	
	private static Texture noiseSampler;
	
	static Model bob_omb_body = OBJParser.parseTriangleMesh("bob_omb_body");
	static Model bob_omb_eyes = OBJParser.parseTriangleMesh("bob_omb_eyes");
	static Model bob_omb_cap  = OBJParser.parseTriangleMesh("bob_omb_cap");
	static Model bob_omb_fuse = OBJParser.parseTriangleMesh("bob_omb_fuse");
	static Model bob_omb_legs = OBJParser.parseTriangleMesh("bob_omb_legs");
	static Model bob_omb_key  = OBJParser.parseTriangleMesh("bob_omb_key");
	
	SceneNode bodyNode;
	SceneNode eyeNode;
	SceneNode capNode;
	SceneNode fuseNode;
	SceneNode legsNode;
	SceneNode keyNode;
	
	public Reflector reflector;
	
	float rotation = 0;
	
	public static Light blastLight;
	
	public ParticleGenerator generator;
	
	private int   blastDuration = 0;
	private float blastRadius   = 0;
	private float blastSpeed    = 2.5f;
	
	private List<Particle> blast = new ArrayList<Particle>();
	
	private boolean dud;
	
	public BobOmb(Vec3 p, Vehicle car, boolean dud)
	{
		bound = new Sphere(p, RADIUS);
		
		this.car = car;
		this.dud = dud;
		
		reflector = new Reflector(1.0f);
		
		bodyNode = new SceneNode(null, -1, bob_omb_body, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		bodyNode.setTranslation(p);
		bodyNode.setScale(new Vec3(0.60));
		bodyNode.setReflector(reflector);
		bodyNode.setReflectivity(0.9f);
		bodyNode.setRenderMode(RenderMode.REFLECT);
		bodyNode.setColor(new float[] {0.013f, 0.037f, 0.077f});
		
		eyeNode = new SceneNode(null, -1, bob_omb_eyes, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		eyeNode.setScale(new Vec3(1.0));
		eyeNode.setRenderMode(RenderMode.COLOR);
		eyeNode.setColor(RGB.WHITE_3F);
		
		capNode = new SceneNode(null, -1, bob_omb_cap, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		capNode.setScale(new Vec3(1.0));
		capNode.setReflector(reflector);
		capNode.setReflectivity(0.9f);
		capNode.setRenderMode(RenderMode.REFLECT);
		capNode.setColor(new float[] {0.272f, 0.426f, 0.467f});
		
		fuseNode = new SceneNode(null, -1, bob_omb_fuse, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		fuseNode.setScale(new Vec3(1.0));
		fuseNode.setRenderMode(RenderMode.COLOR);
		fuseNode.setColor(RGB.WHITE_3F);
		
		legsNode = new SceneNode(null, -1, bob_omb_legs, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		legsNode.setScale(new Vec3(1.0));
		legsNode.setRenderMode(RenderMode.COLOR);
		legsNode.setColor(new float[] {1.000f, 0.412f, 0.019f});
		
		keyNode = new SceneNode(null, -1, bob_omb_key, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		keyNode.setScale(new Vec3(1.0));
		keyNode.setReflector(reflector);
		keyNode.setReflectivity(0.75f);
		keyNode.setRenderMode(RenderMode.REFLECT);
		keyNode.setColor(new float[] {1, 1, 0.2f});
		
		bodyNode.addChild(eyeNode);
		bodyNode.addChild(capNode);
		bodyNode.addChild(fuseNode);
		bodyNode.addChild(legsNode);
		bodyNode.addChild(keyNode);
		
		generator = new ParticleGenerator();
		
		this.scene = Scene.singleton;
	}
	
	private void renderBlast(GL2 gl)
	{
		GLU glu = new GLU();
		
		Shader shader = Shader.get("dissolve");
		if(shader != null)
		{
			shader.enable(gl);
			
			shader.setSampler(gl, "cloudSampler", 0);
			shader.setUniform(gl, "dissolveFactor", 1.0f - ((float) blastDuration / 60.0f));
		}
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		GLUquadric sphere = glu.gluNewQuadric();
		
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		glu.gluQuadricTexture  (sphere, true);
		
		gl.glPushMatrix();
		{
			noiseSampler.bind(gl);
			noiseSampler.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			
			glu.gluSphere(sphere, blastRadius, 24, 24);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{	
		if(noiseSampler == null)
		{
			noiseSampler = TextureLoader.load(gl, "tex/blast_noise.png");
			
			float[] blastColor = {RGB.INDIGO[0]/255, RGB.INDIGO[1]/255, RGB.INDIGO[2]/255};
			
			blastLight = new Light(gl, new Vec3(), blastColor, blastColor, blastColor);
		    
			blastLight.setConstantAttenuation(0.5f);
			blastLight.setLinearAttenuation(0.001f);
			blastLight.setQuadraticAttenuation(0.001f);
			blastLight.enableAttenuation = true;
			blastLight.disable(gl);
		}
		
		if(Scene.environmentMode) return;
		
		if(!dead)
		{
			bodyNode.setTranslation(bound.c);
			bodyNode.setRotation(new Vec3(0, rotation, 0));
			bodyNode.render(gl);
		}
		else if(!blast.isEmpty())
		{
			boolean useHDR = BloomStrobe.isEnabled();
			
			if(Scene.reflectMode) BloomStrobe.end(gl);

			if(useHDR) renderBlast(gl);
			else BlastParticle.renderList(gl, blast);
			
			if(useHDR) BloomStrobe.begin(gl);
			
			Shader.disable(gl);
		}
		
		if(blastDuration <= 1) blastLight.disable(gl); 
		else blastLight.enable(gl);
		
		gl.glColor3f(1, 1, 1);
	}

	@Override
	public void hold()
	{
		setPosition(car.getBackwardItemVector(this, 1));
		rotation = car.trajectory;
	}
	
	@Override
	public boolean canCollide(Item item)
	{
		if(item instanceof Shell  ||
		   item instanceof Banana ||
		   item instanceof BobOmb   ) return true;
		
		return false;
	}

	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell  ||
		   item instanceof Banana ||
		   item instanceof BobOmb   )
		{
			this.destroy();
			item.destroy();
		}
	}
	
	@Override
	public boolean isDead() { return dead && blastDuration < 1; }
	
	@Override
	public void collide(Vehicle car)
	{
		if(!dead) destroy();
		else if(blastDuration > 50)
		{
			car.velocity = 0;
			car.spin();
		}
		else if(blastDuration >  0) car.spin();
	}

	@Override
	public void update()
	{
		if(!dead)
		{
			setPosition(getPositionVector());
			if(falling) fall();
			
			if(!dud)
			{
				for(Bound bound : scene.getBounds())
					if(bound.testBound(this.bound))
						{ destroy(); break; }
				
				if(scene.enableTerrain)
				{
					Terrain terrain = scene.getTerrain();
					float h = terrain.getHeight(terrain.trees.values(), getPosition().toArray());
				
					if(bound.c.y - bound.getMaximumExtent() <= h) destroy();
				}
			}
			else
			{
				detectCollisions();
				resolveCollisions();
				
				float[] heights = scene.enableTerrain ? getHeights(scene.getTerrain()) : getHeights();

				setRotation(getRotationAngles(heights));
				if(thrown) setRotation(-45, trajectory, 0);
			}	
		}
		else if(blastDuration > 0)
		{
			bound = new Sphere(getPosition(), blastRadius);
			blastRadius += blastSpeed;
			blastSpeed *= 0.9f;
			blastDuration--;
			
			for(Particle particle : blast) particle.update();
			Particle.removeParticles(blast);
			
			blastLight.setPosition(getPosition());
			scene.focalBlur.blurCentre = blastLight.getPosition();
			scene.focalBlur.enableRadial = true;
			scene.focalBlur.blurFactor = (float) blastDuration / 60.0f;
		}
	}
	
	@Override
	public void destroy()
	{
		if(!dead)
		{
			blastDuration = 60;
			blast = generator.generateBlastParticles(getPosition(), 1800);
		}
		
		dead = true;
	}
}
