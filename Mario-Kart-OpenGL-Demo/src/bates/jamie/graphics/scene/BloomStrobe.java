package bates.jamie.graphics.scene;


import static javax.media.opengl.GL.GL_CLAMP_TO_EDGE;
import static javax.media.opengl.GL.GL_FRAMEBUFFER;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_RENDERBUFFER;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;
import static javax.media.opengl.GL2GL3.GL_TEXTURE_BASE_LEVEL;
import static javax.media.opengl.GL2GL3.GL_TEXTURE_MAX_LEVEL;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.util.Shader;

import com.jogamp.opengl.util.gl2.GLUT;

public class BloomStrobe
{
	private Scene scene;

	private int fboWidth  = 0;
	private int fboHeight = 0;

	private int[] textureID = new int[7]; // original scene, bright pass, and bloom results (4)
	private int[] fboID     = new int[5]; // FBO names for 1st pass (1) and 2nd pass (4)                    

	private int rboID; // render buffer object name
	private int pboID; // pixel buffer object name 
	
	private float bloomLimit = 1.0f;
	private float[][] offsets = new float[4][5 * 5 * 2]; // 5 based on size of gaussian
	
	private boolean afterGlowValid = false;

	private DisplayMode mode = DisplayMode.FULL_SCENE;

	public BloomStrobe(GL2 gl, Scene scene)
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
		Shader ball     = Scene.shaders.get("ball");
		Shader gaussian = Scene.shaders.get("gaussian");
		Shader combine  = Scene.shaders.get("combine");
		Shader show2D   = Scene.shaders.get("show2D");
		
		ball.enable(gl);
	    ball.setUniform(gl, "bloomLimit", bloomLimit);
	    
	    gaussian.enable(gl);
	    gaussian.setSampler(gl, "sampler0", 0);
	    
	    combine.enable(gl);
	    combine.setSampler(gl, "sampler0", 0);
	    combine.setSampler(gl, "sampler1", 1);
	    combine.setSampler(gl, "sampler2", 2);
	    combine.setSampler(gl, "sampler3", 3);
	    combine.setSampler(gl, "sampler4", 4);
	    combine.setSampler(gl, "sampler5", 5);
	    
	    show2D.enable(gl);
	    show2D.setSampler(gl, "sampler0", 0);

	    Shader.disable(gl);
	}

	public void render(GL2 gl)
	{
		setupShaders(gl);
		
		// spin objects
		animationAngle += angleIncrement;
		if (animationAngle == 360.0f) animationAngle = 0.0f;
	
		// slow down over time
		angleIncrement *= 0.99f;
		if (Math.abs(angleIncrement) < 0.01f) angleIncrement = 0.0f;
	
		// Original Scene + Bright Pass
		firstPass(gl);
		
		gl.glEnable(GL2.GL_BLEND);
	
		// Generate mipmaps of the bright pass results:
		gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
	
		secondPass(gl);
	
		finalPass(gl);
	
		// Read back frame for afterglow effect
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
		gl.glReadPixels(0, 0, fboWidth, fboHeight, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0); 
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);
		afterGlowValid = true;
		
		gl.glDisable(GL2.GL_BLEND);
	
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
		
		scene.renderWater(gl, car, true);
		
		scene.renderWorld(gl);
		scene.render3DModels(gl, car);
		
		scene.water.setRefraction(gl);
		
		gl.glDrawBuffers(2, attachments, 0);
		scene.water.render(gl, car.getPosition());
		
		scene.renderParticles(gl, car);
		Particle.resetTexture();
		
		gl.glDrawBuffers(1, attachments, 0);
		scene.renderFoliage(gl, car);
		
		gl.glDrawBuffers(2, attachments, 0);
		// Draw objects in the scene
		drawModels(gl);
	}

	public void drawModels(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		Shader shader = Scene.shaders.get("ball");
		if(shader != null) shader.enable(gl);

		shader.setUniform(gl, "starIntensity", Math.abs(angleIncrement));
		
		// Draw sphere
		gl.glPushMatrix();
		{
			gl.glTranslatef(0.0f, 30.0f, 0.0f);
			gl.glRotatef(animationAngle / 5.0f, 0.0f, 1.0f, 0.0f);
			glut.glutSolidSphere(15.0f, 75, 75);
		}
		gl.glPopMatrix();
	}

	/*
	 * This second rendering pass uses a gaussian function to blur the bright
	 * fragments of the first pass; this create the bloom effect by causing
	 * light to bleed into the surrounding pixels.
	 * 
	 * 4 passes are actually performed, one for each LOD of the bright pass
	 */
	public void secondPass(GL2 gl)
	{
		Shader shader = Scene.shaders.get("gaussian");
		if(shader != null) shader.enable(gl);

		gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);

		for(int i = 0; i < 4; i++)
		{
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[i + 1]);
			gl.glViewport(0, 0, fboWidth >> i, fboHeight >> i);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

			int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);

			if(status != GL2.GL_FRAMEBUFFER_COMPLETE)
				System.out.println("Frame Buffer Error : Second Rendering Pass");

			int offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "tc_offset");
			gl.glUniform2fv(offsetsLoc, 25, offsets[i], 0);

			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, i);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , i);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
			}
			gl.glEnd();
		}

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 4);
	}

	// final pass:
	// output the final image to the backbuffer
	public void finalPass(GL2 gl)
	{
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, scene.getWidth(), scene.getHeight());

		// set up afterglow texture
		gl.glActiveTexture(GL2.GL_TEXTURE5);
		
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, pboID);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
		
//		for(int i = 2; i < 7; i++)
//		{
//			gl.glActiveTexture(GL2.GL_TEXTURE1 + i - 2);
//			gl.glBindTexture(GL_TEXTURE_2D, textureID[i]);
//		}
//		
		gl.glActiveTexture(GL_TEXTURE0);
		
		updateState(gl);

		switch(mode)
		{
			case ORIGINAL_SCENE:
			{
				Shader shader = Scene.shaders.get("show2D");
				if(shader != null) shader.enable(gl);
				gl.glBindTexture(GL_TEXTURE_2D, textureID[0]);
				break;
			}
			case BRIGHT_PASS:
			case PRE_BLUR:
			{
				Shader shader = Scene.shaders.get("show2D");
				if(shader != null) shader.enable(gl);
				gl.glBindTexture(GL_TEXTURE_2D, textureID[1]);
				break;
			}
			case POST_BLUR:
			{
				Shader shader = Scene.shaders.get("show2D");
				if(shader != null) shader.enable(gl);
				break;
			}
			case JUST_BLOOM:
			case NO_AFTER_GLOW:
			case JUST_AFTER_GLOW:
			case FULL_SCENE:
			{
				Shader shader = Scene.shaders.get("combine");
				if(shader != null) shader.enable(gl);

				gl.glBindTexture(GL_TEXTURE_2D, 
						((mode == DisplayMode.JUST_BLOOM) || (mode == DisplayMode.JUST_AFTER_GLOW)) ? 0 : textureID[0]);

				boolean afterGlow = mode.ordinal() >= DisplayMode.JUST_AFTER_GLOW.ordinal();
				shader.setUniform(gl, "afterGlow", afterGlow && afterGlowValid ? 1 : 0);
				break;
			}

			default: assert(false); break;
		}

		if((mode == DisplayMode.PRE_BLUR) || (mode == DisplayMode.POST_BLUR))
		{
			if(mode == DisplayMode.PRE_BLUR)
			{
				// show each LOD individually
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 0);
			}
			else gl.glBindTexture(GL_TEXTURE_2D, textureID[2]);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, +0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+0.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+0.0f, +0.0f);
			}
			gl.glEnd();

			if(mode == DisplayMode.PRE_BLUR)
			{
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 1);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 1);
			}
			else gl.glBindTexture(GL_TEXTURE_2D, textureID[3]);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(0.0f, 0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(0.0f, 1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(1.0f, 1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(1.0f, 0.0f);
			}
			gl.glEnd();

			if(mode == DisplayMode.PRE_BLUR)
			{
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 2);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 2);
			}
			else gl.glBindTexture(GL_TEXTURE_2D, textureID[4]);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+0.0f, +0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+0.0f, -1.0f);
			}
			gl.glEnd();

			if(mode == DisplayMode.PRE_BLUR)
			{
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 3);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 3);
			}
			else gl.glBindTexture(GL_TEXTURE_2D, textureID[5]);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(0.0f, -1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(0.0f,  0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(1.0f,  0.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(1.0f, -1.0f);
			}
			gl.glEnd();

			if(mode == DisplayMode.PRE_BLUR) // reset texture LODs
			{
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL , 4);
			}
		}
		else
		{
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
			}
			gl.glEnd();
		}
	}

	private float animationAngle = 0.0f;
	private float angleIncrement = 4.0f;
	
	public void increaseIncrement() { angleIncrement += 2.0f; }

	public void createTexture(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		// Set up the render textures: 2 for 1st pass, 4 for 2nd pass
		gl.glGenTextures(7, textureID, 0);

		for(int i = 0; i < 2; i++) // original + bright pass
		{
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID[i]);

			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		}

		for(int i = 2; i < 7; i++)
		{
			gl.glActiveTexture(GL2.GL_TEXTURE1 + i - 2);
			gl.glBindTexture(GL_TEXTURE_2D, textureID[i]);

			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

			if (i < 6)
			{
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, fboWidth >> (i - 2), fboHeight >> (i - 2), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			}
			else
			{
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			}
		}
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}

	public void createBuffers(GL2 gl)
	{
		// Set up a PBO for afterglow
		int[] buffers = new int[2];

		gl.glGenBuffers(1, buffers, 0);
		pboID = buffers[0];

		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
		gl.glBufferData(GL2.GL_PIXEL_PACK_BUFFER, 1, null, GL2.GL_STREAM_COPY);
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);

		// Set up some renderbuffer state
		gl.glGenRenderbuffers(1, buffers, 1);
		rboID = buffers[1];

		gl.glBindRenderbuffer(GL_RENDERBUFFER, rboID);
		gl.glRenderbufferStorage(GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32, fboWidth, fboHeight);

		gl.glGenFramebuffers(5, fboID, 0);

		// in 1st pass we'll render to two 2D textures simultaneously
		gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[0]);

		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		gl.glDrawBuffers(2, attachments, 0);

		gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID);

		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[0], GL_TEXTURE_2D, textureID[0], 0); // original pass
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[1], GL_TEXTURE_2D, textureID[1], 0); // bright pass

		// in 2nd pass (actually 4 passes) we'll render to one 2D texture at a time
		for (int i = 0; i < 4; i++)
		{
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fboID[i + 1]);
			gl.glFramebufferTexture2D(GL_FRAMEBUFFER, attachments[0], GL_TEXTURE_2D, textureID[i + 2], 0);
		}

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void changeSize(GL2 gl)
	{
	    int width  = fboWidth;
	    int height = fboHeight;
	    
	    fboWidth  = scene.getWidth();
	    fboHeight = scene.getHeight();

	    gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
	    gl.glBufferData(GL2.GL_PIXEL_PACK_BUFFER, fboHeight * fboWidth * 4, null, GL2.GL_STREAM_COPY);
	    gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);
	    afterGlowValid = false;

	    gl.glRenderbufferStorage(GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32, fboWidth, fboHeight);

	    for(int i = 0; i < 2; i++)
	    {
	    	gl.glBindTexture(GL_TEXTURE_2D, textureID[i]); // GL_RGB16
	    	gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
	    }

	    for(int i = 2; i < 7; i++)
	    {
	    	gl.glBindTexture(GL_TEXTURE_2D, textureID[i]);
	    	
	    	if (i < 6)
			{
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, fboWidth >> (i - 2), fboHeight >> (i - 2), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			}
			else
			{
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, fboWidth, fboHeight, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			}
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
	    			
	    			offsets[k][index + 0] = (-1.0f * xInc) + ((float) i * xInc);
	    			offsets[k][index + 1] = (-1.0f * yInc) + ((float) j * yInc);
	    		}
	    	}
	    }
	}
	
	private void updateState(GL2 gl)
	{
		if(mode == DisplayMode.JUST_AFTER_GLOW)
		{
			gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glBindTexture(GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE3); gl.glBindTexture(GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE4); gl.glBindTexture(GL_TEXTURE_2D, 0);
		}
		else
		{
			gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL_TEXTURE_2D, textureID[2]);
			gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glBindTexture(GL_TEXTURE_2D, textureID[3]);
			gl.glActiveTexture(GL2.GL_TEXTURE3); gl.glBindTexture(GL_TEXTURE_2D, textureID[4]);
			gl.glActiveTexture(GL2.GL_TEXTURE4); gl.glBindTexture(GL_TEXTURE_2D, textureID[5]);
		}
		gl.glActiveTexture(GL_TEXTURE0);
	}
	
	private enum DisplayMode
	{
		ORIGINAL_SCENE,
		BRIGHT_PASS,
		PRE_BLUR,
		POST_BLUR,
		JUST_BLOOM,
		NO_AFTER_GLOW,
		JUST_AFTER_GLOW,
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
				case JUST_AFTER_GLOW: return "Just After Glow";
				case JUST_BLOOM:      return "Just Bloom";
				case NO_AFTER_GLOW:   return "No After Glow";
				case ORIGINAL_SCENE:  return "Original Scene";
				case POST_BLUR:       return "Post Blur";
				case PRE_BLUR:        return "Pre Blur";
				
				default: return "";
			}
		}
	}
	
	public void cycleMode() { mode = DisplayMode.cycle(mode); }
	
	public String getDisplayMode() { return mode.toString(); }
}
