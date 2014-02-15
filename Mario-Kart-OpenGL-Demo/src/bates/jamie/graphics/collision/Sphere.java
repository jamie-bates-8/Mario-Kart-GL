package bates.jamie.graphics.collision;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINE_LOOP;

import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import static bates.jamie.graphics.util.Vec3.*;

import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.gl2.GLUT;


public class Sphere extends Bound
{
	public static final float EPSILON = 1E-4f;
	
	public float r;
	
	public Sphere(float x, float y, float z, float r)
	{
		setPosition(x, y, z);
		this.r = r;
	}
	
	public Sphere(Vec3 c, float r)
	{
		setPosition(c);
		this.r = r;
	}
	
	@Override
	public float getHeight() { return r * 2; }
	
	@Override
	public boolean testSphere(Sphere a)
	{
		Vec3 t = a.c.subtract(c);
		
		return t.dot() <= (a.r + r) * (a.r + r);
	}
	
	@Override
	public boolean testOBB(OBB b)
	{
		Vec3 p = b.closestPointToPoint(c);
		
		p = p.subtract(c);
		
		return p.dot() <= r * r;
	}

	@Override
	public Vec3 getFaceVector(Vec3 p) { return p; }
	
	@Override
	public Vec3 closestPointOnPerimeter(Vec3 p)
	{
		p = p.subtract(c); // translation vector
		p = p.normalize(); // unit vector
		p = p.multiply(r); // scaled by radius
		
		return c.add(p);	
	}
	
	@Override
	public Vec3 closestPointToPoint(Vec3 p)
	{
		p = p.subtract(c); // translation vector
		
		float dist = p.magnitude();
		if(dist > r) dist = r;
		
		p = p.normalize(); // unit vector
		p = p.multiply(dist); // scaled by radius
		
		return c.add(p);	
	}
	
	@Override
	public float getMaximumExtent() { return r; }
	
	public void renderSolid(GL2 gl, float[] color)
	{
		if(color.length > 3)
			 gl.glColor4fv(color, 0);
		else gl.glColor3fv(color, 0);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glScalef(r, r, r);
			
			GLUT glut = new GLUT();
			if(Scene.occludeSphere) glut.glutSolidSphere(1, 12, 12);
			else glut.glutSolidCube(2);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
	
	public void renderWireframe(GL2 gl, float[] color, boolean smooth)
	{
		int v = 24;
		
		if(color.length > 3)
			 gl.glColor4fv(color, 0);
		else gl.glColor3fv(color, 0);
		
		if(smooth)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_LINE_SMOOTH);
		}
		
		float[][] vertices = new float[v][3];
		
		for(int i = 0; i < v; i ++)
		{
			double theta = toRadians(i * (360.0 / v));
			vertices[i] = new float[] {(float) (r * cos(theta)), 0, (float) (r * sin(theta))};
		}
		
		for(int a = 0; a < 3; a++)
		{
			int[] rot = {0, 0, 0};
			rot[a] = 1;
		
			gl.glPushMatrix();
			{
				gl.glTranslatef(c.x, c.y, c.z);
				gl.glRotatef(90, rot[0], rot[1], rot[2]);
				
				gl.glBegin(GL_LINE_LOOP);
				{
					for(int i = 0; i < v; i ++)
						gl.glVertex3fv(vertices[i], 0);
				}
				gl.glEnd();
			}
			gl.glPopMatrix();
		}
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_LINE_SMOOTH);
	}
	
	public Vec3 randomPointInside()
	{
		Random random = new Random();
		
		Vec3 p = new Vec3();
		
		do { p = getRandomVector(r); }
		while(p.dot(p) > r * r);
		
		return p.add(c);
	}
}
