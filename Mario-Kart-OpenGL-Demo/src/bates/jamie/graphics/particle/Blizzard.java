package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;

import com.jogamp.common.nio.Buffers;

public class Blizzard
{
	private Random generator = new Random();
	
	public List<SnowParticle> flakes;
	public FloatBuffer vBuffer;
	public int flakeLimit;
	
	public int timer = 0;
	
	public Blizzard(int flakeLimit)
	{
		flakes = new ArrayList<SnowParticle>();
		this.flakeLimit = flakeLimit;
	}
	
	public void update()
	{
		if(flakes.size() < flakeLimit - 5)
		{
			int i = generator.nextInt(6);
			
			while(i > 0)
			{
				flakes.add(new SnowParticle(getSource(), getRandomVector()));
				i--;
			}
		}
		
		for(SnowParticle flake : flakes)
		{
			flake.update();
			if(Scene.outOfBounds(flake.c)) flake.c = getSource();
		}
	}
	
	private float[] getSource()
	{
		float x = generator.nextFloat() * (generator.nextBoolean() ? 150 : -150);
		float y = generator.nextFloat() * (generator.nextBoolean() ?  10 : - 10);
		float z = generator.nextFloat() * (generator.nextBoolean() ? 150 : -150);
		
		return new float[] {x, y + 200, z};
	}
	
	private float[] getRandomVector()
	{
		float xVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float yVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float zVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		
		return new float[] {xVel, yVel, zVel};
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{	
			gl.glDisable(GL2.GL_TEXTURE_2D);
						
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
				
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(6);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(flakes.size() * 3);
			
			for(Particle particle : flakes) vertices.put(particle.c);
			vertices.position(0);  
			
			gl.glColor3f(1, 1, 1);
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glDrawArrays(GL_POINTS, 0, flakes.size() - 1);
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
					
			gl.glEnable(GL2.GL_TEXTURE_2D);
		}
		gl.glPopMatrix();
	}
}
