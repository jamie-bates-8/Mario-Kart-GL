package bates.jamie.graphics.collision;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.gl2.GLUT;

public abstract class Bound
{
	public Vec3 c;
	
	public void setPosition(float x, float y, float z) { c = new Vec3(x, y, z); }
	public void setPosition(float[] p)                 { c = new Vec3(p);       }
	public void setPosition(Vec3 v)                    { c = v;                 }
	
	public Vec3 getPosition() { return c; }
	
	/**
	 * Returns the normal of the bound's surface with reference to the point
	 * <code>p</code>.
	 */
	public abstract Vec3 getFaceVector(Vec3 p);
	
	/**
	 * Returns the closest point within the bound to the point <code>p</code>
	 * passed as a parameter. If the point <code>p</code> is within the bound,
	 * the point itself is returned.
	 */
	public abstract Vec3 closestPointToPoint(Vec3 p);
	
	public abstract Vec3 closestPointOnPerimeter(Vec3 p);
	
	public abstract boolean testSphere(Sphere s);
	
	public abstract boolean testOBB(OBB b);
	
	/**
	 * Returns the maximum length from the centre of the bound to any other point
	 * on the bound.
	 */
	public abstract float getMaximumExtent();
	
	public abstract float getHeight();
	
	public abstract Vec3 randomPointInside();
	
	public void displayClosestPtToPt(GL2 gl, GLUT glut, Vec3 p, boolean smooth)
	{
		gl.glColor4f(1, 1, 1, 1);
		
		if(smooth)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		}
		
		gl.glPushMatrix();
		{
			Vec3 vertex = closestPointOnPerimeter(p);
			
			if(smooth)
			{
				gl.glBegin(GL2.GL_POINTS);
				gl.glVertex3fv(vertex.toArray(), 0);
				gl.glEnd();
			}
			else
			{			
				gl.glTranslatef(vertex.x, vertex.y, vertex.z);
				glut.glutSolidSphere(0.2, 12, 12);
			}
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
	}
	
	public abstract void displaySolid(GL2 gl, float[] color);
	
	public abstract void displayWireframe(GL2 gl, float[] color, boolean smooth);
	
	public boolean testBound(Bound b)
	{
		if(b instanceof Sphere) return testSphere((Sphere) b);
		else if(b instanceof OBB) return testOBB((OBB) b);
		
		return false;	
	}
}
