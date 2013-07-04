package bates.jamie.graphics.io;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.scene.Scene;

import com.jogamp.common.nio.Buffers;

public class ModelSelecter
{
	private Scene scene;
	private GLU glu;
	
	public int selectX = -1;
	public int selectY = -1;
	public int selected = 0;
	private IntBuffer selectBuffer;
	private static final int BUFFER_SIZE = 512;
	
	public ModelSelecter(Scene scene, GLU glu)
	{
		this.scene = scene;
		this.glu = glu;
	}
	
	public void setSelection(int x, int y)
	{
		selectX = x;
		selectY = y;
	}
	
	public float[] select3DPoint(GL2 gl, GLU glu)
	{
		Point point = scene.canvas.getMousePosition();
		if(point == null) return null;
	
		int w = (int) point.getX();
		int h = (int) point.getY();
		
		int[] viewport = new int[4];
		float[] modelview  = new float[16];
		float[] projection = new float[16];

		FloatBuffer p = FloatBuffer.allocate(3);
		   
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv  (GL2.GL_MODELVIEW_MATRIX, modelview, 0);
		gl.glGetFloatv  (GL2.GL_PROJECTION_MATRIX, projection, 0);

		int _h = scene.getHeight();
		
		glu.gluUnProject(w, _h - h, 0.95f,
			Buffers.newDirectFloatBuffer(modelview ),
			Buffers.newDirectFloatBuffer(projection),
			Buffers.newDirectIntBuffer(viewport), p);
		
		return new float[] {p.get(0), p.get(1), p.get(2)};
	}
	
	//TODO does not work in bird's eye view
	public void selectModel(GL2 gl)
	{
		startPicking(gl);
		
		gl.glPushName(1);
		
		gl.glPushMatrix();
		{		
			
		}
		gl.glPopMatrix();
		
		gl.glPopName();
		
		endPicking(gl);
	}
	
	private void startPicking(GL2 gl)
	{
		selectBuffer = Buffers.newDirectIntBuffer(BUFFER_SIZE);
		gl.glSelectBuffer(BUFFER_SIZE, selectBuffer);
		
		gl.glRenderMode(GL2.GL_SELECT);
		
		gl.glInitNames();
		
		gl.glMatrixMode(GL_PROJECTION);
		
		gl.glPushMatrix();
		{
			gl.glLoadIdentity();
			
			int[] viewport = new int[4];
			gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
			
			glu.gluPickMatrix(selectX, viewport[3] - selectY, 5, 5, viewport, 0);
			
			scene.resetView(gl);
			
			gl.glMatrixMode(GL_MODELVIEW);
		}
	}

	private void endPicking(GL2 gl)
	{
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glFlush();
		
		int hits = gl.glRenderMode(GL2.GL_RENDER);
		
		selectX = selectY = -1;
		
		getSelection(gl, hits);
	}

	private void getSelection(GL2 gl, int hits)
	{
		selected++;
	}
}
