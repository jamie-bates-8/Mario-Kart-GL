package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.util.Arrays;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.Shader;

public class ShadowManipulator
{
	private Scene scene;
	private Light light;
	
	private float[] shadowMatrix = new float[16];
	
	public float shadowRadius = 300.0f;
	public float shadowOffset =   2.0f;
	
	private int shadowTexture;
	private int shadowBuffer ; // framebuffer ID
	
	private int shadowQuality = 12;
	private boolean enableBuffer = true;
	
	public enum ShadowQuality
	{
		LOW,
		MED,
		HIGH,
		BEST;
	}
	
	public ShadowManipulator(Scene scene, Light light)
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
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_DEPTH_TEXTURE_MODE, GL2.GL_INTENSITY);
		
		gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void update(GL2 gl)
	{
		GLUgl2 glu = new GLUgl2();
		
		int width  = scene.getWidth();
		int height = scene.getHeight();
		
	    float distance, near, fov;
	    
	    float[] modelview  = new float[16];
	    float[] projection = new float[16];
	    
	    float radius = shadowRadius; // based on objects in scene

	    float[] p = light.getPosition();
	    
	    // Euclidian distance from light source to origin
	    distance = (float) Math.sqrt(p[0] * p[0] +  p[1] * p[1] + p[2] * p[2]);

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
	    glu.gluLookAt(p[0], p[1], p[2], 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
	    
	    gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);

	    gl.glViewport(0, 0, width, height);
	    
	    if(enableBuffer)
	    {
	    	gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, shadowBuffer); // rendering offscreen.
	    	gl.glViewport(0, 0, width * shadowQuality, height * shadowQuality); // need larger viewport
	    }

	    depthMode(gl, true);
	    
	    scene.renderVehicles (gl, scene.getCars().get(0), true);
	    scene.renderItems    (gl, scene.getCars().get(0)); 
	    scene.renderObstacles(gl); 
	    
	    enable(gl);
	    gl.glActiveTexture(GL2.GL_TEXTURE2);

	    // Copy depth values into depth texture
	    if(!enableBuffer) gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, 0, 0, width, height, 0);

	    depthMode(gl, false);

	    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);

	    gl.glMatrixMode(GL2.GL_TEXTURE);
	    
	    if(!Shader.enabled)
	    {
		    // Set up texture matrix for shadow map projection,
		 	float[] tempMatrix = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		 	Matrix.translate(tempMatrix, 0.5f, 0.5f, 0.5f);
		 	Matrix.scale    (tempMatrix, 0.5f, 0.5f, 0.5f);
		 	
		 	Matrix.multiply(shadowMatrix, tempMatrix, projection);
		 	Matrix.multiply(tempMatrix, shadowMatrix, modelview );
		 	// transpose to get the s, t, r, and q rows for plane equations
		 	Matrix.transpose(shadowMatrix, tempMatrix);
		 	gl.glLoadMatrixf(shadowMatrix, 0);
	    }

	 	double[] bias =
	 	{	
			0.5, 0.0, 0.0, 0.0, 
			0.0, 0.5, 0.0, 0.0,
			0.0, 0.0, 0.5, 0.0,
			0.5, 0.5, 0.5, 1.0
		};
			
		gl.glLoadIdentity();	
		gl.glLoadMatrixd(bias, 0);
			
		// concatating all matrices into one.
		gl.glMultMatrixf(projection, 0);
		gl.glMultMatrixf(modelview , 0);
	 	
	 	gl.glActiveTexture(GL2.GL_TEXTURE0); 

	    scene.resetView(gl);
	}
	
	void createBuffer(GL2 gl)
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

		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

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

	private void depthMode(GL2 gl, boolean enable)
	{
		if(enable) // Only need depth values
		{
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
	}
	
	public void displayShadow(GL2 gl)
	{
		enable(gl);
		
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL2.GL_LEQUAL);

		// Set up the eye plane for projecting the shadow map on the scene
		gl.glEnable(GL2.GL_TEXTURE_GEN_S);
		gl.glEnable(GL2.GL_TEXTURE_GEN_T);
		gl.glEnable(GL2.GL_TEXTURE_GEN_R);
		gl.glEnable(GL2.GL_TEXTURE_GEN_Q);
		
		gl.glTexGenfv(GL2.GL_S, GL2.GL_EYE_PLANE, shadowMatrix,  0); 
		gl.glTexGenfv(GL2.GL_T, GL2.GL_EYE_PLANE, shadowMatrix,  4); 
		gl.glTexGenfv(GL2.GL_R, GL2.GL_EYE_PLANE, shadowMatrix,  8);
		gl.glTexGenfv(GL2.GL_Q, GL2.GL_EYE_PLANE, shadowMatrix, 12);
		
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
	
	public void displayMap(GL2 gl, int texture)
    {
        gl.glMatrixMode(GL_PROJECTION); gl.glLoadIdentity();
        gl.glMatrixMode(GL_MODELVIEW ); gl.glLoadIdentity();
        
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();
        {
	        gl.glLoadIdentity();
	           
	        gl.glEnable(GL_TEXTURE_2D);
	        gl.glBindTexture(GL_TEXTURE_2D, texture);
	        gl.glDisable(GL2.GL_LIGHTING);
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
	        gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_NONE);
	        // Show the shadow map at its actual size relative to window
	        gl.glBegin(GL2.GL_QUADS);
	        {
	            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
	            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f( 1.0f, -1.0f);
	            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f( 1.0f,  1.0f);
	            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(-1.0f,  1.0f);
	        }
	        gl.glEnd();
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	        gl.glEnable(GL2.GL_LIGHTING);
        }
        gl.glPopMatrix();
		
        scene.resetView(gl);
    }
}