package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TRUE;
import static javax.media.opengl.GL2ES1.GL_COORD_REPLACE;
import static javax.media.opengl.GL2ES1.GL_POINT_SPRITE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.common.nio.Buffers;

public class ParticleEngine
{
	float timer = 0.0f;
	
	FloatBuffer velocities;
	FloatBuffer startTimes;
	
	int[] bufferIDs = new int[2];
	
	private static final int VELOCITY_BUFFER = 0;
	private static final int START_TIME_BUFFER = 1;
	
	int count;
	
	public ParticleEngine(int count)
	{
		this.count = count;
	}
	
	private void createBuffers(GL2 gl)
	{
		velocities = Buffers.newDirectFloatBuffer(count * 3);
		startTimes = Buffers.newDirectFloatBuffer(count * 2);
		
		Random generator = new Random();
		
		Vec3 v = new Vec3();
		float velocity, theta, phi;
		
		for(int i = 0; i < count; i++)
		{
			// Pick the direction of the velocity
			theta = (float) (Math.PI /  8.0f) * generator.nextFloat();
			phi   = (float) (Math.PI *  2.0f) * generator.nextFloat();
			
			v.x = sinf(theta) * cosf(phi);
			v.y = cosf(theta) * 3.0f;
			v.z = sinf(theta) * sinf(phi);
			
			// Scale to set the magnitude of the velocity (speed)
			velocity = 1.25f + .25f * generator.nextFloat();
			v = v.multiply(velocity);
			
			velocities.put(v.toArray());
		}
		velocities.rewind();
		
		float time = 0.0f, rate = 0.25f;
		for(int i = 0; i < count; i++)
		{
			startTimes.put(time);
			startTimes.put(generator.nextBoolean() ? 0.0f : 1.0f);
			time += rate;
		}
		startTimes.rewind();
		
		createVBOs(gl);
	}
	
	private float sinf(float radians) { return (float) Math.sin(radians); }
	private float cosf(float radians) { return (float) Math.cos(radians); }
	
	private void createVBOs(GL2 gl)
	{
		gl.glGenBuffers(bufferIDs.length, bufferIDs, 0);
	    
	    // Velocity data
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VELOCITY_BUFFER]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * velocities.capacity(), velocities, GL2.GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[START_TIME_BUFFER]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, Float.SIZE / 8 * startTimes.capacity(), startTimes, GL2.GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	public void render(GL2 gl)
	{
		if(Scene.enableAnimation) timer += 0.10;
		
		if(velocities == null) createBuffers(gl);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable (GL_BLEND);
		gl.glDisable(GL2.GL_DEPTH_TEST);
//		gl.glEnable (GL2.GL_ALPHA_TEST);
		gl.glEnable (GL_POINT_SPRITE);
		
//		gl.glAlphaFunc(GL2.GL_GREATER, 0.25f);
		gl.glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
		
		gl.glPointSize(10.0f);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		
		Shader shader = Shader.get("smoke"); shader.enable(gl);
		
		shader.setSampler(gl, "texture", 0);
		shader.setSampler(gl, "cloudSampler", 1);
		
		Particle.fire.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE1); Particle.fire2.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		shader.setUniform(gl, "timer", timer);
		shader.setUniform(gl, "gravity", new float[] {0.0f, -0.05f, 0.0f});
		shader.setUniform(gl, "duration", 20.0f);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY       );
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[VELOCITY_BUFFER  ]); gl.glVertexPointer  (3, GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[START_TIME_BUFFER]); gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
		
		gl.glDrawArrays(GL2.GL_POINTS, 0, count);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY       );
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		Shader.disable(gl);
		
		gl.glDisable(GL_BLEND);
		gl.glEnable (GL_LIGHTING);
		gl.glDisable(GL2.GL_ALPHA_TEST);
		gl.glEnable (GL2.GL_DEPTH_TEST);
	}
}
