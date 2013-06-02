package bates.jamie.graphics.scene;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

public class Reflector
{
	private Scene scene;
	
	private int textureID;
	
	private int frameBuffer;
	private int renderBuffer;
	
	private int mapSize = 640; // based on canvas height
	
	public Reflector(Scene scene)
	{
		this.scene = scene;
	}
	
	public void setup(GL2 gl)
	{
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
		int[] texID = new int[1];
	    gl.glGenTextures(1, texID, 0);
	    textureID = texID[0];
	    gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, textureID);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_GENERATE_MIPMAP, 1);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
	    gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	    
	    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
	    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
	    gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);

	    // this may change with window size changes
	    int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		
		for (int i = j; i < j + 6; i++)
	        gl.glTexImage2D(i, 0, GL2.GL_RGBA8, mapSize, mapSize, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
	}
	
	public void update(GL2 gl)
	{
		GLUgl2 glu = new GLUgl2();
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(90.0f, 1.0f, 1.0f, 400.0f);
		gl.glViewport(0, 0, mapSize, mapSize);

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBuffer);

		int j = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
		
		for (int i = j; i < j + 6; i++)
		{
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();

			switch(i)
			{
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						 1.0f,  0.0f,  0.0f,
						 0.0f, -1.0f,  0.0f
				    );
					break;
				}
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						-1.0f,  0.0f,  0.0f,
						 0.0f, -1.0f,  0.0f
					);
					break;
				}
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						 0.0f,  1.0f,  0.0f,
						 0.0f,  0.0f,  1.0f
					);
					break;
				}
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						 0.0f, -1.0f,  0.0f,
						 0.0f,  0.0f, -1.0f
					);
					break;
				}
				case GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						 0.0f,  0.0f,  1.0f,
						 0.0f, -1.0f,  0.0f
					);
					break;
				}
				case GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z:
				{
					glu.gluLookAt
					(
						 0.0f,  0.0f,  0.0f, 
						 0.0f,  0.0f, -1.0f,
						 0.0f, -1.0f,  0.0f
					);
					break;
				}
				
				default: assert(false); break;
			}

			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, i, textureID, 0);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

			scene.renderObstacles(gl);
		}
	}
}
