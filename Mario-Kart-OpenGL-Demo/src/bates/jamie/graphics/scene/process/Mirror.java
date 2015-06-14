package bates.jamie.graphics.scene.process;

import static javax.media.opengl.GL.GL_CLAMP_TO_EDGE;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec3;

public class Mirror
{
	private int textureID;
	
	private int frameBuffer;
	private int renderBuffer;
	
	private int mapSize = 320; // based on canvas height
	private int maxSize;
	
	private Scene scene;
	
	private Vec3 planeCentre = new Vec3();
	private Vec3 planeNormal = new Vec3(0, 0, 1);
	
	public Mirror(Vec3 centre, Vec3 normal)
	{
		scene = Scene.singleton;
		
		planeCentre = centre;
		planeNormal = normal;
	}
	
	public void setup(GL2 gl)
	{
		int[] sizes = new int[2];
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE     , sizes, 0);
	    gl.glGetIntegerv(GL2.GL_MAX_RENDERBUFFER_SIZE, sizes, 1);
	    maxSize = (sizes[1] > sizes[0]) ? sizes[0] : sizes[1];
		
		createTexture(gl);
		createBuffer (gl);
		
		System.out.println("Mirror : Texture ID (" + textureID + ")");
		
		update(gl);
	}
	
	public int getTexture() { return textureID; }

	private void createBuffer(GL2 gl)
	{		
		int[] rboID = new int[1];
		gl.glGenRenderbuffers(1, rboID, 0);
		renderBuffer = rboID[0];
		
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderBuffer);
		
		gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT, mapSize, mapSize);
		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, renderBuffer);
		
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
		
		int[] fboID = new int[1];
		gl.glGenFramebuffers(1, fboID, 0);
		frameBuffer = fboID[0];
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);
		
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, textureID, 0);
		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, renderBuffer);
		
		int bufferStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if(bufferStatus != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("Mirror : " + checkFramebufferError(bufferStatus));
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
	}

	private void createTexture(GL2 gl)
	{
		int[] texID = new int[1];
	    gl.glGenTextures(1, texID, 0);
	    textureID = texID[0];
	    
	    gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
	}
	
	public void enable(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE0  );
		gl.glEnable       (GL2.GL_TEXTURE_2D);
		gl.glBindTexture  (GL2.GL_TEXTURE_2D, textureID);
	}
	
	public void update(GL2 gl)
	{
		GLUgl2 glu = new GLUgl2();
		
//		enable(gl);

		if( updateSize ) changeSize(gl);
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(70.0f, 1.0f, 1.0f, 1000.0f);
		gl.glViewport(0, 0, mapSize, mapSize);

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderBuffer);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		float[] mirrorMatrix = getMirrorMatrix(planeCentre, planeNormal);
//		gl.glLoadMatrixf(mirrorMatrix, 0);
		
		Vec3 from = planeCentre.add(new Vec3(0, Math.cos(Scene.sceneTimer / 30.0), 0).multiply(15));
		Vec3 direction = planeCentre.add(planeNormal.multiply(20));
		
		glu.gluLookAt(from.x, from.y, from.z, direction.x, direction.y, direction.z, 0, 0, 1);
			
		for(Light l : scene.lights) l.setup(gl);
		for(Light l : scene.getCars().get(0).driftLights) l.setup(gl);
		
		Scene.beginRenderLog("MIRROR MODE");
			
		Scene.environmentMode = true;
		scene.renderWorld(gl);
		Scene.environmentMode = false;
		
		Scene.endRenderLog();
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		scene.resetView(gl);
	}
	
	private String checkFramebufferError(int status)
	{
		switch(status)
		{
			case GL2.GL_FRAMEBUFFER_UNDEFINED                     : return "Frame Buffer Undefined : No Window?";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT         : return "Frame Buffer Incomplete Attachment : Check status of each attachment";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT : return "Attach at least one buffer to the FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        : return "Frame Buffer Incomplete Draw Buffer : Check that all attachments enabled" +
					                                                       "via glDrawBuffers exists in FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER        : return "Frame Buffer Incomplete Read Buffer : Check that all attachments enabled" +
            															   "via glReadBuffer exists in FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS         : return "Framw Buffer Incomplete Dimensions";
			
			default : return "Error undefined";	
		}
	}
	
	public void changeSize(GL2 gl)
	{		
//		gl.glBindFramebuffer (GL2.GL_FRAMEBUFFER , frameBuffer );
//		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderBuffer);
//		
//		gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_STENCIL, mapSize, mapSize);
//		
//		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT  , GL2.GL_RENDERBUFFER, renderBuffer);
//			
//		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
//		
//		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		
		updateSize = false;
	}
	
	public static float[] getMirrorMatrix(Vec3 p, Vec3 v) // point on the plane, plane perpendicular direction
	{
		float dot = p.dot(v);
		float[] matrix = new float[16];
			 
		matrix[ 0] = 1 - 2 * v.x * v.x;
		matrix[ 1] =   - 2 * v.x * v.y;
		matrix[ 2] =   - 2 * v.x * v.z;
		matrix[ 3] = 2 * dot *v.x;
		matrix[ 4] = - 2 * v.y * v.x;
		matrix[ 5] = 1 - 2 * v.y * v.y;
		matrix[ 6] = - 2 * v.y * v.z;
		matrix[ 7] = 2 * dot * v.y;
		matrix[ 8] = - 2 * v.z * v.x;
		matrix[ 9] = - 2 * v.z * v.y;
		matrix[10] = 1 - 2 * v.z * v.z;
		matrix[11] = 2 * dot * v.z;
		matrix[12] = 0;
		matrix[13] = 0;
		matrix[14] = 0;
		matrix[15] = 1;
		
		return matrix;
	}

	
	private boolean updateSize = false;
	

	public void updateSize(int size)
	{
		// environment map is limited by max supported renderbuffer size
		if(size > maxSize) mapSize = maxSize;
		mapSize = size;
		
		updateSize = true;
	}
}
