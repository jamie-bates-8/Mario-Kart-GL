package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

public class ShadowCaster
{
	public static final double[] SHADOW_BIAS =
	 {	
		0.5, 0.0, 0.0, 0.0, 
		0.0, 0.5, 0.0, 0.0,
		0.0, 0.0, 0.5, 0.0,
		0.5, 0.5, 0.5, 1.0
	};
	
	private Scene scene;
	private Light light;
	
	public float shadowRadius = 300.0f;
	public float shadowOffset =   2.0f;
	
	private int shadowTexture;
	private int shadowBuffer ; // framebuffer ID
	
	private int shadowQuality = 12;
	private boolean enableBuffer = true;
	
	public static SampleMode sampleMode = SampleMode.SIXTEEN_SAMPLES;
	
	public enum ShadowQuality
	{
		LOW,
		MED,
		HIGH,
		BEST;
	}
	
	public enum SampleMode
	{
		ONE_SAMPLE,
		FOUR_DITHERED,
		SIXTEEN_SAMPLES;
		
		public static SampleMode cycle(SampleMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}
	
	public ShadowCaster(Scene scene, Light light)
	{
		this.scene = scene;
		this.light = light;
	}
	
	public void setQuality(ShadowQuality quality)
	{
		switch(quality)
		{
			case LOW : enableBuffer = false; break;
			
			case MED : enableBuffer = true; shadowQuality =  4; break;
			case HIGH: enableBuffer = true; shadowQuality =  8; break;
			case BEST: enableBuffer = true; shadowQuality = 12; break;
		}
	}
	
	public void setup(GL2 gl)
	{
		if(enableBuffer) createBuffer(gl);
		else createTexture(gl);
	    
	    update(gl);
	}

	private void createTexture(GL2 gl)
	{
		int[] id = new int[1];
		gl.glGenTextures(1, id, 0);
		shadowTexture = id[0];

		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTexture);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_DEPTH_TEXTURE_MODE, GL2.GL_INTENSITY);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	private void createBuffer(GL2 gl)
	{
		int shadowWidth  = scene.getWidth () * shadowQuality;
		int shadowHeight = scene.getHeight() * shadowQuality;
	
		int bufferStatus = 0;
	
		// Try to use a texture depth component
		int[] texID = new int[1];
	    gl.glGenTextures(1, texID, 0);
	    shadowTexture = texID[0];
	    
	    gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTexture);
	
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_DEPTH_TEXTURE_MODE, GL2.GL_INTENSITY);
	
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, shadowWidth, shadowHeight, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_UNSIGNED_BYTE, null);
		gl.glBindTexture(GL_TEXTURE_2D, 0);
	
		// create a framebuffer object
		int[] fboID = new int[1];
		gl.glGenFramebuffers(1, fboID, 0);
		shadowBuffer = fboID[0];
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, shadowBuffer);
	
		// Instruct OpenGL that we won't bind a color texture with the currently bound FBO
		gl.glDrawBuffer(GL2.GL_NONE);
		gl.glReadBuffer(GL2.GL_NONE);
	
		// attach the texture to FBO depth attachment point
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTexture, 0);
	
		// check FBO status
		bufferStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if(bufferStatus != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("Frame Buffer failure!");
	
		// switch back to window-system-provided framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	}

	public int getTexture() { return shadowTexture; }
	
	public void update(GL2 gl)
	{
		GLUgl2 glu = new GLUgl2();
		
		int width  = scene.getWidth();
		int height = scene.getHeight();
		
	    float distance, near, fov;
	    
	    float[] modelview  = new float[16];
	    float[] projection = new float[16];
	    
	    float radius = shadowRadius; // based on objects in scene

	    Vec3 p = light.getPosition();
	    
	    // Euclidian distance from light source to origin
	    distance = p.magnitude();

	    near = distance - radius;
	    // Keep the scene filling the depth texture
	    fov = (float) Math.toDegrees(2.0f * Math.atan(radius / distance));

	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluPerspective(fov, 1.0f, near, near + (2.0f * radius));
	    
	    gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
	    
	    // Switch to light's point of view
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    glu.gluLookAt(p.x, p.y, p.z, 0, 0, 0, 0, 1, 0);
	    
	    gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);

	    gl.glViewport(0, 0, width, height);
	    
	    if(enableBuffer)
	    {
	    	gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, shadowBuffer); // rendering offscreen.
	    	gl.glViewport(0, 0, width * shadowQuality, height * shadowQuality); // need larger viewport
	    }

	    depthMode(gl, true);
	    
	    renderCasters(gl);
	    
	    gl.glActiveTexture(GL2.GL_TEXTURE2);

	    // Copy depth values into depth texture
	    if(!enableBuffer)
	    {
	    	gl.glBindTexture(GL_TEXTURE_2D, shadowTexture);
	    	gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, 0, 0, width, height, 0);
	    }

	    depthMode(gl, false);

	    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);

	    loadShadowMatrix(gl, modelview, projection); 
	    gl.glActiveTexture(GL2.GL_TEXTURE0);

	    scene.resetView(gl);
	}

	private void loadShadowMatrix(GL2 gl, float[] modelview, float[] projection)
	{
		gl.glMatrixMode(GL2.GL_TEXTURE);
			
		gl.glLoadIdentity();	
		gl.glLoadMatrixd(SHADOW_BIAS, 0);
			
		// concatating all matrices into one.
		gl.glMultMatrixf(projection, 0);
		gl.glMultMatrixf(modelview , 0);
	 	
	 	gl.glActiveTexture(GL2.GL_TEXTURE0);
	}

	private void renderCasters(GL2 gl)
	{
		Car car = scene.getCars().get(0);
		
		scene.renderVehicles (gl, car, true);
	    scene.renderItems    (gl, car);
	    scene.renderFoliage  (gl, car);
	    scene.renderObstacles(gl);
	}
	
	private void depthMode(GL2 gl, boolean enable)
	{
		boolean enableShaders = Shader.enabled;
		
		if(enable) // Only need depth values
		{
			Shader.enabled = false;
			// Clear the depth buffer only
		    gl.glClear(GL_DEPTH_BUFFER_BIT);
		    
		    gl.glShadeModel(GL2.GL_FLAT);
		    
		    gl.glDisable(GL2.GL_LIGHTING      );
		    gl.glDisable(GL2.GL_COLOR_MATERIAL);
		    gl.glDisable(GL2.GL_NORMALIZE     );
		    
		    gl.glColorMask(false, false, false, false);
		    
		    gl.glEnable(GL2.GL_CULL_FACE);
		    gl.glCullFace(GL2.GL_FRONT);
	
		    // Overcome imprecision
		    gl.glPolygonOffset(shadowOffset, 1.0f);
		    gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
		}
		else // Restore normal drawing state
		{ 
		    gl.glShadeModel(GL2.GL_SMOOTH);
		    
		    gl.glEnable(GL2.GL_LIGHTING      );
		    gl.glEnable(GL2.GL_COLOR_MATERIAL);
		    gl.glEnable(GL2.GL_NORMALIZE     );
		    
		    gl.glColorMask(true, true, true, true);
		    
		    gl.glDisable(GL2.GL_CULL_FACE);
		    gl.glCullFace(GL2.GL_BACK);
		    
		    gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
		}
		
		Shader.enabled = enableShaders;
	}
	
	public void displayShadow(GL2 gl)
	{
		enable(gl);
		
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL2.GL_LEQUAL);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}

	public void disable(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}

	public void enable(GL2 gl)
	{
		// fixed-functionality shadows no longer supported
		if(Shader.enabled)
		{
			gl.glActiveTexture(GL2.GL_TEXTURE2);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, shadowTexture);
			
			gl.glActiveTexture(GL2.GL_TEXTURE0);
		}
	}

	public static void cycle()
	{
		sampleMode = SampleMode.cycle(sampleMode);
	}
}
