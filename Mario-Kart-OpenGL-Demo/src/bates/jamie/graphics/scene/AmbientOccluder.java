package bates.jamie.graphics.scene;


import static javax.media.opengl.GL.GL_CLAMP_TO_EDGE;
import static javax.media.opengl.GL.GL_FRAMEBUFFER;
import static javax.media.opengl.GL.GL_RENDERBUFFER;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

public class AmbientOccluder
{
	private Scene scene;

	private int fboWidth  = 0;
	private int fboHeight = 0;

	private int[] textureID = new int[3]; // original scene, bright pass, and bloom results (4)
	private int[] fboID     = new int[2]; // FBO names for 1st pass (1) and 2nd pass (4)  
	
	private int randomTexture;

	private int rboID; // render buffer object name
	
	private float[][] offsets = new float[4][5 * 5 * 2]; // 5 based on size of gaussian

	private DisplayMode mode = DisplayMode.FULL_SCENE;

	public AmbientOccluder(GL2 gl, Scene scene)
	{
		this.scene = scene;

		fboWidth  = scene.getWidth();
		fboHeight = scene.getHeight();
		
		createTexture(gl);
		createBuffers(gl);
		
		changeSize(gl);
	}
	
	public int getTexture(int texture) { return textureID[texture]; }
	
	public void setupShaders(GL2 gl)
	{
		Shader gaussian = Shader.get("gaussian");
		Shader show2D   = Shader.get("show_texture");
		Shader occlude  = Shader.get("ssao");
	    
	    gaussian.enable(gl);
	    gaussian.setSampler(gl, "sampler0", 0);
	    
	    show2D.enable(gl);
	    show2D.setSampler(gl, "sampler0", 0);
	    
	    occlude.enable(gl);
	    occlude.setSampler(gl, "colourSampler", 0);
	    occlude.setSampler(gl, "normalSampler", 1);
	    occlude.setSampler(gl, "randomSampler", 6);
	    
	    occlude.setUniform(gl, "ssao_level", 1.0f);
	    occlude.setUniform(gl, "object_level", 1.0f);
	    occlude.setUniform(gl, "ssao_radius", 0.0025f);
	    occlude.setUniform(gl, "weight_by_angle", true);
	    occlude.setUniform(gl, "point_count", 10);
	    occlude.setUniform(gl, "randomize_points", true);

	    Shader.disable(gl);
	}

	public void render(GL2 gl)
	{
		setupShaders(gl);
	
		// Original Scene + Bright Pass
		firstPass(gl);
	
		// Generate mipmaps of the bright pass results:
		gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);
	
		secondPass(gl);
	
		finalPass(gl);
	
		// Reset state
		Shader.disable(gl);
	}

	/*
	 * This method renders the objects in the scene that can create the bloom
	 * lighting effect. Shaders enabled while rendering these objects must
	 * send bright fragments to an additional color attachment.
	 */
	public void firstPass(GL2 gl)
	{
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[0]);
		gl.glViewport(0, 0, fboWidth, fboHeight);

		int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);

		if(status != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("Frame Buffer Error : First Rendering Pass");

		// Clear the frame buffer with current clearing color
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		gl.glDrawBuffers(1, attachments, 0); // standard rendering
		
		Car car = scene.getCars().get(0);
		Terrain terrain = scene.getTerrain();
		
		if(terrain != null && terrain.enableWater) scene.renderWater(gl, car);
		
		scene.renderWorld(gl);
		scene.render3DModels(gl, car);
		
		if(scene.displayLight) for(Light l : scene.lights) l.render(gl);
		
		gl.glDrawBuffers(2, attachments, 0);
		
		scene.renderParticles(gl, car);
		Particle.resetTexture();
		
		if(scene.enableTerrain) scene.renderFoliage(gl, car);
		
		if(terrain != null && terrain.enableWater) 
		{
			scene.water.setRefraction(gl);
			
			gl.glDrawBuffers(2, attachments, 0);
			scene.water.render(gl, car.camera.getPosition());
		}
		
		gl.glDrawBuffers(2, attachments, 0);
	}

	public void secondPass(GL2 gl)
	{
		Shader shader = Shader.get("show_texture");
		if(shader != null) shader.enable(gl);

		gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[1]);
		gl.glViewport(0, 0, fboWidth, fboHeight);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);

		if(status != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("Frame Buffer Error : Second Rendering Pass");

		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
		}
		gl.glEnd();
	}
	
	public static float object_level = 1.0f;
	public static int point_count = 10;
	
	public static float falloff = 0.000001f;
	public static float area = 0.00f;
	public static float radius = 0.002f;
	public static float angle = 0.00f;
	public static float offset = 1.0f;
	public static float strength = 1.0f;
	
	public static final float[] OFFSETS =
	{
		 0.5381f, 0.1856f,-0.4319f,  0.1379f, 0.2486f, 0.4430f,
      	 0.3371f, 0.5679f,-0.0057f, -0.6999f,-0.0451f,-0.0019f,
     	 0.0689f,-0.1598f,-0.8547f,  0.0560f, 0.0069f,-0.1843f,
     	-0.0146f, 0.1402f, 0.0762f,  0.0100f,-0.1924f,-0.0344f,
      	-0.3577f,-0.5301f,-0.4358f, -0.3169f, 0.1063f, 0.0158f,
     	 0.0103f,-0.5869f, 0.0046f, -0.0897f,-0.4940f, 0.3287f,
     	 0.7119f,-0.0154f,-0.0918f, -0.0533f, 0.0596f,-0.5411f,
     	 0.0352f,-0.0631f, 0.5460f, -0.4776f, 0.2847f,-0.0271f
     };

	// final pass:
	// output the final image to the backbuffer
	public void finalPass(GL2 gl)
	{
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[1]);
		gl.glViewport(0, 0, fboWidth, fboHeight);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glClampColor(GL2.GL_CLAMP_FRAGMENT_COLOR, GL2.GL_FALSE);
		gl.glClampColor(GL2.GL_CLAMP_VERTEX_COLOR, GL2.GL_FALSE);
		gl.glClampColor(GL2.GL_CLAMP_READ_COLOR, GL2.GL_FALSE);
		
		gl.glActiveTexture(GL_TEXTURE0);
		
//		generateRandomVectors(gl);

		switch(mode)
		{
			case ORIGINAL_SCENE:
			{
				Shader shader = Shader.get("show_texture");
				if(shader != null) shader.enable(gl);
				gl.glBindTexture(GL_TEXTURE_2D, textureID[0]);
				break;
			}
			case BRIGHT_PASS:
			{
				Shader shader = Shader.get("show_texture");
				if(shader != null) shader.enable(gl);
				gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);
				break;
			}
			case FULL_SCENE:
			{
				Shader shader = Shader.get("ssao");
				if(shader != null) shader.enable(gl);
				
				shader.setUniform(gl, "radius", radius);
				shader.setUniform(gl, "area", area);
				shader.setUniform(gl, "falloff", falloff);
				shader.setUniform(gl, "angle", angle);
				shader.setUniform(gl, "offset", offset);
				shader.setUniform(gl, "strength", strength);
				
				shader.setSampler(gl, "colourSampler", 0); gl.glActiveTexture(GL2.GL_TEXTURE0); gl.glBindTexture(GL_TEXTURE_2D, textureID[0]);
				shader.setSampler(gl, "normalSampler", 1); gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);
				shader.setSampler(gl, "randomSampler", 6); gl.glActiveTexture(GL2.GL_TEXTURE6); gl.glBindTexture(GL_TEXTURE_2D, randomTexture);
				
				int offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "sample_sphere");
				gl.glUniform3fv(offsetsLoc, 16, OFFSETS, 0);
				
				gl.glActiveTexture(GL2.GL_TEXTURE0);
				
				gl.glBindTexture(GL_TEXTURE_2D, textureID[0]);

				break;
			}

			default: assert(false); break;
		}
		
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
		}
		gl.glEnd();
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, scene.getWidth(), scene.getHeight());
		
		Shader shader = Scene.enableParallax ? Shader.get("gaussian") : Shader.get("show_texture");
		if(shader != null) shader.enable(gl);
		
		gl.glBindTexture(GL_TEXTURE_2D, textureID[2]);

		int offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "tc_offset");
		gl.glUniform2fv(offsetsLoc, 25, offsets[0], 0);

		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
		}
		gl.glEnd();
	}

	public void createTexture(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		gl.glGenTextures(3, textureID, 0);

		for(int i = 0; i < 3; i++) // original + normal pass
		{
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID[i]);

			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		}
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		int[] textureID = new int[1];
		gl.glGenTextures(1, textureID, 0);
		randomTexture = textureID[0];
		
		generateRandomVectors(gl);
	}
	
	private void generateRandomVectors(GL2 gl)
	{	
		gl.glActiveTexture(GL2.GL_TEXTURE6);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, randomTexture);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		int length = 2048;
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, length, length, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		
		FloatBuffer positions = FloatBuffer.allocate(length * length * 4);
		
		Random generator = new Random();
		
		for(int i = 0; i < length * length; i++)
		{
			positions.put(getRandomDirection());
			positions.put(0);	
		}
		positions.position(0);
		
		gl.glTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, length, length, GL2.GL_RGBA, GL2.GL_FLOAT, positions);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	private float[] getRandomDirection()
	{
		Random g = new Random();
		
		Vec3 v = new Vec3();
		
		do
		{
			float x = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();
			float y = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();
			float z = g.nextFloat();
			
			v = new Vec3(x, y, z);
			
		} while (v.magnitude() > 1.0);
		
		v = v.normalize();
		
		return v.toArray(); 
	}

	public void createBuffers(GL2 gl)
	{
		// Set up a PBO for afterglow
		int[] buffers = new int[2];

		gl.glGenBuffers(1, buffers, 0);

		// Set up some renderbuffer state
		gl.glGenRenderbuffers(1, buffers, 1);
		rboID = buffers[1];

		gl.glBindRenderbuffer(GL_RENDERBUFFER, rboID);
		gl.glRenderbufferStorage(GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32, fboWidth, fboHeight);

		gl.glGenFramebuffers(2, fboID, 0);

		// in 1st pass we'll render to two 2D textures simultaneously
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[0]);

		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		gl.glDrawBuffers(2, attachments, 0);

		gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID);

		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[0], GL_TEXTURE_2D, textureID[0], 0); // original pass
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[1], GL_TEXTURE_2D, textureID[1], 0); // bright pass

		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[1]);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[0], GL_TEXTURE_2D, textureID[2], 0);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void changeSize(GL2 gl)
	{
	    int width  = fboWidth;
	    int height = fboHeight;
	    
	    fboWidth  = scene.getWidth();
	    fboHeight = scene.getHeight();
	    
	    fboWidth  = 2048;
	    fboHeight = 2048;

	    gl.glRenderbufferStorage(GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32, fboWidth, fboHeight);

	    for(int i = 0; i < 3; i++)
	    {
	    	gl.glBindTexture(GL_TEXTURE_2D, textureID[i]); // GL_RGB16
	    	gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
	    }

	    setGuassian();
	}

	private void setGuassian()
	{
		float xInc, yInc;
	    
	    for(int k = 0; k < 4; k++)
	    {
	    	xInc = 1.0f / (float) (fboWidth  >> k);
	    	yInc = 1.0f / (float) (fboHeight >> k);

	    	for(int i = 0; i < 5; i++)
	    	{
	    		for(int j = 0; j < 5; j++)
	    		{
	    			int index = ((i * 5) + j) * 2;
	    			
	    			offsets[k][index + 0] = (-2.0f * xInc) + ((float) i * xInc);
	    			offsets[k][index + 1] = (-2.0f * yInc) + ((float) j * yInc);
	    		}
	    	}
	    }
	}
	
	private enum DisplayMode
	{
		ORIGINAL_SCENE,
		BRIGHT_PASS,
		FULL_SCENE;

		public static DisplayMode cycle(DisplayMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
		
		@Override
		public String toString()
		{
			switch(this)
			{
				case BRIGHT_PASS:     return "Bright Pass";
				case FULL_SCENE :     return "Full Scene";
				case ORIGINAL_SCENE:  return "Original Scene";
				
				default: return "";
			}
		}
	}
	
	public void cycleMode() { mode = DisplayMode.cycle(mode); }
	
	public String getDisplayMode() { return mode.toString(); }
}
