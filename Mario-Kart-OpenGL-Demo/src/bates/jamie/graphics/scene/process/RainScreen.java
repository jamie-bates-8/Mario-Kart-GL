package bates.jamie.graphics.scene.process;

import static javax.media.opengl.GL.GL_CLAMP_TO_EDGE;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.util.Arrays;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.particle.RainDrop;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.common.nio.Buffers;

public class RainScreen
{
	public  int textureIDs[] = new int[3];
	private int pboID;
	
	private int width;
	private int height;
	
	private boolean initialized = false;
	
	RainDrop[] drops = new RainDrop[40];
	
	public RainScreen()
	{
		width  = Scene.singleton.getWidth();
		height = Scene.singleton.getHeight();
		
		for(int i = 0; i < drops.length; i++)
			drops[i] = new RainDrop();
	}
	
	public void changeSize()
	{
		width  = Scene.singleton.getWidth();
		height = Scene.singleton.getHeight();
		
		initialized = false;
	}
	
	private void createBuffers(GL2 gl)
	{
		gl.glGenTextures(3, textureIDs, 0);
		
		byte[] base_texture = new byte[width * height * 4];
		Arrays.fill(base_texture, (byte) 128);
		
		for(int i = 0; i < textureIDs.length; i++)
		{
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIDs[i]);
	
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, width, height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, Buffers.newDirectByteBuffer(base_texture));
		}
		
		int[] pboIDs = new int[1];
		gl.glGenBuffers(1, pboIDs, 0);
		pboID = pboIDs[0];
		
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
		gl.glBufferData(GL2.GL_PIXEL_PACK_BUFFER, width * height * 4, null, GL2.GL_DYNAMIC_COPY);
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);
		
		initialized = true;
	}
	
	private void ortho2DBegin(GL2 gl)
	{	
		GLU glu = new GLU();
		
	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, width, height, 0);
	    
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glDisable(GL_DEPTH_TEST);
	}
	

	private void ortho2DEnd(GL2 gl)
	{
		Scene.singleton.resetView(gl);
		
		gl.glEnable(GL_DEPTH_TEST);
	}
	
	public void render(GL2 gl)
	{
		if(!initialized) createBuffers(gl);
		
		ortho2DBegin(gl);
		
		gl.glEnable(GL2.GL_POINT_SPRITE);
	    gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
		
		Shader shader = Shader.get("rain_drop");
		shader.enable(gl);
		
		gl.glColor3f(1, 1, 1);
		
		for(RainDrop drop : drops)
		{
			drop.update();
			if(drop.shouldRender()) drop.render(gl);
		}
		
		gl.glDisable(GL3.GL_PROGRAM_POINT_SIZE);
		gl.glDisable(GL2.GL_BLEND);
		
		ortho2DEnd(gl);
		
		// read pixels displaying the rain pattern into the pixel buffer
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
		gl.glReadPixels(0, 0, width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);
		
		// push the pixels from the buffer straight into the texture
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, pboID);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIDs[2]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, width, height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
		
		shader = Shader.get("rainy_scene");
		shader.enable(gl);
		
		shader.setSampler(gl, "rainSampler" , 0);
		shader.setSampler(gl, "sceneSampler", 1);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIDs[Scene.sceneTimer % 2 == 0 ? 0 : 1]);
		
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
		}
		gl.glEnd();
		
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pboID);
		gl.glReadPixels(0, 0, width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);
		
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, pboID);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIDs[Scene.sceneTimer % 2 == 0 ? 1 : 0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, width, height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
		gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
		
		if(Scene.testMode)
		{
			shader = Shader.get("show_texture");
			shader.enable(gl);

			shader.setSampler(gl, "sampler0", 0);
			
			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIDs[Scene.sceneTimer % 2 == 0 ? 1 : 0]);
			
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
				gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
			}
			gl.glEnd();
		}
		else gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		
		Shader.disable(gl);
	}
	
	public int getHeightMap()
	{
		return textureIDs[Scene.sceneTimer % 2 == 0 ? 1 : 0];
	}
}
