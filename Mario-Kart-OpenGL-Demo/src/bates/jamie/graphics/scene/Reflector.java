package bates.jamie.graphics.scene;

import java.nio.ByteBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.Vector;

public class Reflector
{
	private Scene scene;
	
	private int textureID;
	private int mappingID;
	
	private int frameBuffer;
	private int renderBuffer;
	
	private int mapSize = 640; // based on canvas height
	private int maxSize;
	
	public float reflectivity;
	
	public Reflector(Scene scene, float reflectivity)
	{
		this.scene = scene;
		this.reflectivity = reflectivity;
	}
	
	public void setup(GL2 gl)
	{
		int[] sizes = new int[2];
		gl.glGetIntegerv(GL2.GL_MAX_CUBE_MAP_TEXTURE_SIZE, sizes, 0);
	    gl.glGetIntegerv(GL2.GL_MAX_RENDERBUFFER_SIZE    , sizes, 1);
	    maxSize = (sizes[1] > sizes[0]) ? sizes[0] : sizes[1];
	    
	    System.out.println("Reflector\n{");
	    System.out.println("\tMax Cube Map Texture Size: " + sizes[0]);
	    System.out.println("\tMax Render Buffer Size: " + sizes[1]);
	    System.out.println("}\n");
		
		createTexture(gl);
		createBuffer (gl);
	}

	private void createBuffer(GL2 gl)
	{
		int[] fboID = new int[1];
		gl.glGenFramebuffers(1, fboID, 0);
		frameBuffer = fboID[0];
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);
		
		int[] rboID = new int[1];
		gl.glGenRenderbuffers(1, rboID, 0);
		renderBuffer = rboID[0];
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderBuffer);
		
		gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_STENCIL, mapSize, mapSize);
		
		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT  , GL2.GL_RENDERBUFFER, renderBuffer);
		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_STENCIL_ATTACHMENT, GL2.GL_RENDERBUFFER, renderBuffer);
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	}

	private void createTexture(GL2 gl)
	{
		int[] texID = new int[2];
	    gl.glGenTextures(2, texID, 0);
	    textureID = texID[0];
	    mappingID = texID[1];
	    gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureID);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	    
	    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
	    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
	    gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);

	    // this may change with window size changes
	    int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		
		for (int i = j; i < j + 6; i++)
	        gl.glTexImage2D(i, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, mappingID);
		
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_GENERATE_MIPMAP, 1);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
	    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	}
	
	public void enable(GL2 gl)
	{
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glEnable (GL2.GL_TEXTURE_CUBE_MAP);
		gl.glEnable (GL2.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureID);
		
		gl.glEnable(GL2.GL_TEXTURE_GEN_S);
		gl.glEnable(GL2.GL_TEXTURE_GEN_T);
		gl.glEnable(GL2.GL_TEXTURE_GEN_R);

		/* We need to put the eye-space reflection vector back into world space
		 * by multiplying by the transpose of the view matrix.
		 */
		float[] normalMatrix = new float[16];
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, normalMatrix, 0);
		Matrix.getNormalMatrix(normalMatrix, normalMatrix);
		
		gl.glMatrixMode(GL2.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadMatrixf(normalMatrix, 0);
			
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	public void disable(GL2 gl)
	{
		gl.glDisable(GL2.GL_TEXTURE_CUBE_MAP);
		gl.glDisable(GL2.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		gl.glDisable(GL2.GL_TEXTURE_GEN_S);
		gl.glDisable(GL2.GL_TEXTURE_GEN_T);
		gl.glDisable(GL2.GL_TEXTURE_GEN_R);
		
		gl.glMatrixMode(GL2.GL_TEXTURE);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
	}
	
	public void update(GL2 gl, float[] p)
	{
		GLUgl2 glu = new GLUgl2();
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(90.0f, 1.0f, 1.0f, 1000.0f);
		gl.glViewport(0, 0, mapSize, mapSize);

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);

		int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		
		for (int i = j; i < j + 6; i++)
		{
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			
			float[] q = new float[3];
			
			switch(i)
			{
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X: q = Vector.add(p, new float[] {+1,  0,  0}); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X: q = Vector.add(p, new float[] {-1,  0,  0}); break;
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y: q = Vector.add(p, new float[] { 0, +1,  0}); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y: q = Vector.add(p, new float[] { 0, -1,  0}); break;
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z: q = Vector.add(p, new float[] { 0,  0, +1}); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z: q = Vector.add(p, new float[] { 0,  0, -1}); break;
					
				default: assert(false); break;
			}

			switch(i)
			{
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X:
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X: 
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z:
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z: glu.gluLookAt(p[0], p[1], p[2], q[0], q[1], q[2], 0.0f, -1.0f,  0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y: glu.gluLookAt(p[0], p[1], p[2], q[0], q[1], q[2], 0.0f,  0.0f,  1.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y: glu.gluLookAt(p[0], p[1], p[2], q[0], q[1], q[2], 0.0f,  0.0f, -1.0f); break;

				default: assert(false); break;
			}

			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, i, textureID, 0);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

			scene.renderObstacles(gl);
			scene.renderSkybox(gl);
			scene.renderWalls(gl);
			scene.renderFloor(gl, false);
		}
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		scene.resetView(gl);
	}
	
	public void displayMap(GL2 gl)
	{
		// Display environment map for debugging purposes
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, mappingID);
		gl.glDisable(GL2.GL_LIGHTING);

		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		ByteBuffer texels = ByteBuffer.allocate(mapSize * mapSize * 4);

		int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		
		for (int i = j; i < j + 6; i++)
		{
			// Grab the cubemap face
			gl.glGetTexImage(i, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, texels);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, texels);

			gl.glPushMatrix();

			// position the cube face for display
			switch (i)
			{
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X: gl.glTranslatef(+0.25f,  0.0f, 0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X: gl.glTranslatef(-0.75f,  0.0f, 0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y: gl.glTranslatef(-0.25f, -0.5f, 0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y: gl.glTranslatef(-0.25f, +0.5f, 0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z: gl.glTranslatef(-0.25f,  0.0f, 0.0f); break;
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z: gl.glTranslatef(+0.75f,  0.0f, 0.0f); break;
				
				default: assert(false); break;
			}

			gl.glScalef(0.25f, 0.25f, 0.25f);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			}
			gl.glEnd();

			gl.glPopMatrix();
		}

		gl.glEnable(GL2.GL_LIGHTING);

		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	public void changeSize(GL2 gl)
	{
		int size = mapSize;
		// environment map is limited by max supported renderbuffer size
		mapSize = maxSize;

		if (size != mapSize)
		{
			gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT32, mapSize, mapSize);

			int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
			
			for (int i = j; i < j + 6; i++)
				gl.glTexImage2D(i, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		}
	}
}
