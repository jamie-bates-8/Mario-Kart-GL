package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.Random;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.Vector;

public class LightningStrike
{
	private Vec3  origin;
	private Vec3  target;
	
	private float[] controlPoints;
	
	public int timer    = 0;
	public int duration = 60;
	
	public int size;
	public int intervals;
	
	
	public LightningStrike(Vec3 start, Vec3 end)
	{
		intervals = 50;
		
		origin = start;
		target = end;
		
		controlPoints = setupPoints();
	}
	
	private float[] setupPoints()
	{
		Random generator = new Random();

		Vec3       dir = target.subtract(origin).divide(intervals + 1);
		float[] points = new float[intervals * 3 + (2 * 3)];
		
		float  length = origin.length(target);
		float[] stops = new float[intervals];
		
		float stop = 0;
		
		for(int i = 0; i < stops.length; i++)
		{
			stop += generator.nextFloat();
			stops[i] = stop;
		}
		
		for(int i = 1; i <= intervals; i++)
		{
			Vec3 point  = origin.add(dir.multiply(i));
			     point  =  point.add(dir.multiply(generator.nextFloat()));
			     
			     float dist = stops[i - 1] * (length / stops[intervals - 1]);
			     
			     point = origin.add(dir.normalize().multiply(dist));
			     
			Vec3 offset = dir.cross(Vec3.getRandomVector());
			     offset = offset.normalize();
			     
			dist = generator.nextFloat() < 0.1 ? 15 : 0.5f;
			
			point = point.add(offset.multiply(generator.nextFloat() * dist));
			
			points[i * 3 + 0] = point.x;
			points[i * 3 + 1] = point.y;
			points[i * 3 + 2] = point.z;
		}
		
		int l = points.length;
		
		points[    0] = origin.x; points[    1] = origin.y; points[    2] = origin.z;
		points[l - 3] = target.x; points[l - 2] = target.y; points[l - 1] = target.z; 
		
		return points;
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{		
			boolean multisample = gl.glIsEnabled(GL2.GL_MULTISAMPLE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glDisable(GL_TEXTURE_2D);
			
			gl.glLineWidth(2);
			
			if(!multisample)
			{
				gl.glEnable(GL_BLEND);
				gl.glEnable(GL2.GL_LINE_SMOOTH);
			}
			
			
//			controlPoints = setupPoints();
			
//			gl.glMap1f(GL2.GL_MAP1_VERTEX_3, 0, 100, 3, intervals + 2, controlPoints, 0);
//			
//			gl.glEnable(GL2.GL_MAP1_VERTEX_3);
//			
//			gl.glMapGrid1d(100, 0.0, 100.0);
//			
//			gl.glEvalMesh1(GL2.GL_LINE, 0, 100);
			
			if(Scene.testMode)
			{
				Renderer.displayPoints(gl, controlPoints, intervals + 2, RGB.SKY_BLUE_3F, 10);
				Renderer.displayLines (gl, controlPoints, RGB.SKY_BLUE_3F, true);
			}
			
			GLUgl2 glu = new GLUgl2();
			GLUnurbs nurb = glu.gluNewNurbsRenderer();
			
			float[] knots = new float[intervals + 2 + 4];
			
			int knot = 0;
			
			for(int i = 0; i < knots.length; i++)
			{
				if(i > 3 && i <= knots.length - 4) knot++;
				knots[i] = knot;
			}
			
//			System.out.println("Controls Points: " + (intervals + 2) + ", + Curve Order: " + "4, equals: " +
//			(intervals + 2 + 4) + ", Knots: " + knots.length);
//			
//			System.out.println(Vector.print(knots, 0));
			
			gl.glColor3f(1.0f, 1.0f, 0.5f);
			
			glu.gluBeginCurve(nurb);
			{
				glu.gluNurbsCurve(nurb, knots.length, knots, 3, controlPoints, 4, GL2.GL_MAP1_VERTEX_3);
			}
			glu.gluEndCurve(nurb);

			gl.glDepthMask(true);
			
			gl.glEnable (GL2.GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable (GL_LIGHTING);
			
			if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);
			
			gl.glLineWidth(1);
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
	}
}
