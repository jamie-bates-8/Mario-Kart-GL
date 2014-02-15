package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.util.Gradient;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

/* TODO Improve quality of fire particles and add dynamic lighting to boost effect
 * 
 * Quality may be improved by one or more of the following:
 * 
 * - creating a larger quantity of smaller particles
 * - discarding particles fragments randomly to create a dynamic dissolving pattern
 * - distorting the current effect using bloom, refraction and directional blur
 * - set particle colour using a gradient for more colour variation (possibly in a shader)
 */

public class FireParticle extends Particle
{
	private int lifespan;
	private int sourceID;
	private float scale = 1;
	private boolean spark = false;
	
	private Car car;
	
	private Vec3 source;
	private Vec3 direction;
	private Vec3 offset;
	
	private Texture texture;
	
	private static Gradient gradient;
	
	static
	{	
		gradient = new Gradient(new float[] {252, 253, 187}, new float[] {120, 120, 120});
		gradient.addStop( 5, RGB.BRIGHT_YELLOW);
		gradient.addStop(10, new float[] {252, 211, 103});
		gradient.addStop(20, new float[] {255, 171,  80});
		gradient.addStop(30, RGB.ORANGE);
		gradient.addStop(50, new float[] {255, 117,  58});
		gradient.addStop(60, new float[] {255,  83,  33});
		gradient.addStop(61, new float[] {30, 30, 30});
		gradient.addStop(70, new float[] {60, 60, 60});
		
//		gradient = new Gradient(new float[] {226, 254, 253}, new float[] {120, 120, 120});
//		gradient.addStop( 5, new float[] {180, 235, 255});
//		gradient.addStop(10, new float[] {68, 208, 255});
//		gradient.addStop(20, RGB.BLUE);
//		gradient.addStop(30, RGB.INDIGO);
//		gradient.addStop(40, new float[] {48, 104, 231});
//		gradient.addStop(50, new float[] {56, 80, 255});
//		gradient.addStop(60, new float[] {39, 39, 234});
//		gradient.addStop(61, new float[] {30, 30, 30});
//		gradient.addStop(70, new float[] {60, 60, 60});
	}

	public FireParticle(Vec3 c, Vec3 t, Vec3 dir, float rotation, int duration,
			int textureID, float scale, boolean spark, Car car, int sourceID)
	{
		super(c, t, rotation, duration);
		
		source = c;
		this.car = car;
		direction = dir;
		
		lifespan = duration;
		
		this.spark = spark;
		this.scale = scale;
		this.sourceID = sourceID;
		
		if(textureID == 0) texture = whiteFlare; 
		if(textureID == 1) texture = fire_alpha_1; 
		if(textureID == 2) texture = fire_alpha_2; 
		if(textureID == 3) texture = fire_alpha_3; 
		if(textureID == 4) texture = fire_alpha_4;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{		
		Shader shader = Shader.get("fire");
		shader.enable(gl);
		shader.setSampler(gl, "texture", 0);
		
		gl.glEnable(GL2.GL_POINT_SPRITE);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
		
		gl.glPushMatrix();
		{	
			float  age = (float) duration / lifespan;
			float halflife = lifespan / 2;
			float size = Math.abs(((float) duration - halflife) / lifespan);
			size = 1.0f - size;
			size = 2.0f * (size - 0.5f);
			
			gl.glPointSize((30 * size * scale) + (20 * age * scale));
			if(spark) gl.glPointSize(5 + 10 * age);
			
			float[] color = gradient.interpolate(1.0 - age);
			if(spark) color = gradient.interpolate(0.5);
			
			float[][] colors = gradient.getColors((int) ((1.0 - age) * 100));
			if(spark) colors = gradient.getColors(30);
			
			shader.setUniform(gl, "color2", colors[0]);
			shader.setUniform(gl, "color1", colors[1]);
			shader.setUniform(gl, "spark", spark);
			shader.setUniform(gl, "smoke", age < 0.2);
			
			gl.glColor4f(color[0], color[1], color[2], age);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			offset = car != null ? car.getBoostVectors()[sourceID] : source;
			
			Vec3 position = car != null ? offset.add(c).add(car.bound.u.zAxis.multiply(10.0f * (1 - age))) :
										  offset.add(c).add(direction.multiply(10.0f * (1 - age)));
			
			texture.bind(gl);
			
			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3f(position.x, position.y, position.z);
			}
			gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_POINT_SPRITE);
		
		Shader.disable(gl);
	}
	
	public static void renderList(GL2 gl, List<Particle> particles)
	{
		Shader shader = Shader.get("fire");
		shader.enable(gl);
		shader.setSampler(gl, "texture", 0);
		
		gl.glEnable(GL2.GL_POINT_SPRITE);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
		
		gl.glDepthMask(false);
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{	
			for(Particle particle : particles)
			{	
				FireParticle p = (FireParticle) particle;
				p.renderSingle(gl, shader);
			}
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
		gl.glDepthMask(true);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glDisable(GL2.GL_POINT_SPRITE);
		
		Shader.disable(gl);
	}
	
	private void renderSingle(GL2 gl, Shader shader)
	{
		float  age = (float) duration / lifespan;
		float halflife = lifespan / 2;
		float size = Math.abs(((float) duration - halflife) / lifespan);
		size = 1.0f - size;
		size = 2.0f * (size - 0.5f);
		
		gl.glPointSize((30 * size * scale) + (20 * age * scale));
		if(spark) gl.glPointSize(5 + 10 * age);
		
		float[] color = gradient.interpolate(1.0 - age);
		if(spark) color = gradient.interpolate(0.5);
		
		float[][] colors = gradient.getColors((int) ((1.0 - age) * 100));
		if(spark) colors = gradient.getColors(30);
		
		shader.setUniform(gl, "color2", colors[0]);
		shader.setUniform(gl, "color1", colors[1]);
		shader.setUniform(gl, "spark", spark);
		shader.setUniform(gl, "smoke", age < 0.2);
		
		gl.glColor4f(color[0], color[1], color[2], age * 1.00f);
		
		
		
		offset = car != null ? car.getBoostVectors()[sourceID] : source;
		
		Vec3 position = car != null ? offset.add(c).add(car.bound.u.zAxis.multiply(10.0f * (1 - age))) :
									  offset.add(c).add(direction.multiply(10.0f * (1 - age)));
		
		texture.bind(gl);
		
		gl.glBegin(GL_POINTS);
		{
			gl.glVertex3f(position.x, position.y, position.z);
		}
		gl.glEnd();
	}
	
	@Override
	public void update()
	{
		super.update();
		
//		t = t.add(car.getForwardVector().multiply(0.02f));
		t = t.multiply(duration > 0 ? (float) duration / lifespan : 0.002f);
	}
}
