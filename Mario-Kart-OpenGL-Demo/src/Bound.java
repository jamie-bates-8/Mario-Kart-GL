
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public abstract class Bound
{
	public float[] c;
	
	public void setPosition(float x, float y, float z) { c = new float[] {x, y, z}; }
	
	public void setPosition(float[] c) { this.c = c; }
	
	public float[] getPosition() { return c; }
	
	public abstract List<float[]> getPixelMap();
	
	public abstract float[] getFaceVector(float[] p);
	
	public abstract float[] closestPointToPoint(float[] p);
	
	public abstract boolean testSphere(Sphere s);
	
	public abstract boolean testOBB(OBB b);
	
	public abstract float getMaximumExtent();
	
	public abstract float getHeight();
	
	public void displayClosestPtToPt(GL2 gl, GLUT glut, float[] p)
	{
		gl.glColor4f(1, 1, 1, 1);
		
		gl.glPushMatrix();
		{
			float[] vertex = closestPointToPoint(p);
			
			gl.glTranslatef(vertex[0], vertex[1], vertex[2]);
			glut.glutSolidSphere(0.2, 12, 12);
		}
		gl.glPopMatrix();
	}
	
	public abstract void displaySolid(GL2 gl, GLUT glut, float[] color);
	
	public abstract void displayWireframe(GL2 gl, GLUT glut, float[] color);
	
	public boolean testBound(Bound b)
	{
		if(b instanceof Sphere) return testSphere((Sphere) b);
		else if(b instanceof OBB) return testOBB((OBB) b);
		
		return false;	
	}
}
