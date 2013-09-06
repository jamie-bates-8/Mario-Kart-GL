package bates.jamie.graphics.collision;

import static bates.jamie.graphics.util.Vec3.getRandomVector;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.Arrays;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.gl2.GLUT;


public class OBB extends Bound
{	
	public static final float EPSILON = 1E-5f;
	
	private static final int LEFT  = 0;
	private static final int RIGHT = 1;
	private static final int DOWN  = 2;
	private static final int UP    = 3;
	private static final int FRONT = 4;
	private static final int BACK  = 5;
	
	// rotation matrix used to describe orientation of bound
	public RotationMatrix u = new RotationMatrix();
	
	// half-extents of bound in the form {width, height, depth}
	public Vec3 e;
	
	// flags to determine whether collisions with certain faces should be considered 
	public boolean[] validFaces = new boolean[6];
	

	public OBB(float c0, float c1, float c2,
			   float u0, float u1, float u2,
			   float e0, float e1, float e2)
	{
		 setPosition(c0, c1, c2);
		 setRotation(u0, u1, u2);
		e = new Vec3(e0, e1, e2);
		
		Arrays.fill(validFaces, true);
	}
	
	public OBB(float x, float y, float z, float rx, float ry, float rz, float halfWidth, float halfHeight, float halfDepth, boolean[] validFaces)
	{
		setPosition(x, y, z);
		setRotation(rx, ry, rz);
		e = new Vec3(halfWidth, halfHeight, halfDepth);	
		this.validFaces = validFaces;
	}
	
	/**
	 *
	 *
	 * @param c - the centre point of the bound
	 * @param u - the {x, y, z} rotations of the OBB
	 * @param e - the half-extents of the box, {half-width, half-height, half-depth}
	 * @param v - flags to determine whether collisions with certain faces should be considered 
	 */
	public OBB(float[] c, float[] u, float[] e, boolean[] v)
	{
		setPosition(c);
		setRotation(u[0], u[1], u[2]);
		this.e = new Vec3(e);
		validFaces = v;
	}

	public boolean isValidCollision(Vec3 face)
	{
		Vec3[] faces = getAxisVectors();
		
		for(int i = 0; i < faces.length; i++)
			if(face.equals(faces[i])) return validFaces[i];
		
		return false;
	}
	
	public void setRotation(float x, float y, float z) { u = new RotationMatrix(x, y, z); }
	
	public float getHeight() { return e.y * 2; }
	
	@Override
	public Vec3 getFaceVector(Vec3 p)
	{	
		Vec3[] normals = getAxisVectors();
		Vec3 q = closestPointToPoint(p).subtract(c);
		
		float xScale = q.dot(u.xAxis) / e.x;
		float yScale = q.dot(u.yAxis) / e.y;
		float zScale = q.dot(u.zAxis) / e.z;
		
		// vertical collisions are resolved differently so the top and bottom faces are prioritized 
		if(abs(yScale) >= abs(xScale) && abs(yScale) >= abs(zScale))
		{
			     if(yScale < 0 && p.y < c.y - e.y) return normals[DOWN];
			else if(yScale > 0) return normals[UP];
		}
		
		// prioritize side (left and right) faces over front and back faces
		if(abs(xScale) > abs(zScale))
		{
			return xScale < 0 ? normals[LEFT] : normals[RIGHT];
		}
		
		// if the x and z components of the translation vector are equal, the faces to prioritize
		// are calculated using a number of techniques...
		if(abs(xScale) == abs(zScale))
		{
			if(e.x < e.z) // side faces are prioritized if the x extent of the OBB is greater 
			{
				 return xScale < 0 ? normals[LEFT] : normals[RIGHT];
			}
			else if(e.x == e.z)
			{
				if(q.dot(u.xAxis) < q.dot(u.zAxis))
					 return xScale < 0 ? normals[LEFT ] : normals[RIGHT];
				else return zScale < 0 ? normals[FRONT] : normals[BACK ];
			}
			else return zScale < 0 ? normals[FRONT] : normals[BACK];
		}   else return zScale < 0 ? normals[FRONT] : normals[BACK];		
	}
	
	public float getPenetration(OBB b)
	{
		float p = Integer.MAX_VALUE;
		
		OBB a = this;
		
		float ra, rb, r;
		
		float[][] R    = new float[3][3];
		float[][] AbsR = new float[3][3];
		
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
			{
				   R[j][i] = b.u.getAxis(i).dot(u.getAxis(j));
				AbsR[j][i] = abs(R[j][i]) + EPSILON;
			}
		
		Vec3 t = a.c.subtract(b.c);
		t = new Vec3(t.dot(b.u.xAxis), t.dot(b.u.yAxis), t.dot(b.u.zAxis));		
		
		for(int i = 0; i < 3; i++)
		{
			ra = b.e.get(i);
			rb = e.dot(new Vec3(AbsR[i]));
			r  = abs(t.get(i));

			if(p > abs(rb - (r - ra))) p = abs(rb - (r - ra));
		}

		for(int i = 0; i < 3; i++)
		{
			ra = b.e.x * AbsR[0][i] + b.e.y * AbsR[1][i] + b.e.z * AbsR[2][i];
			rb = a.e.get(i);
			r  = abs(t.x * R[0][i] + t.y * R[1][i] + t.z * R[2][i]);

			if(p > abs(rb - (r - ra))) p = abs(rb - (r - ra));
		}
		
		return p;
	}
	
	public float getPenetration(Sphere s)
	{
		Vec3 q = closestPointOnPerimeter(s.c);
		q = q.subtract(s.c);
		
		double ra = sqrt(q.x * q.x + q.z * q.z);
		double rb = s.r;
		
		return (float) abs(rb - ra);
	}
	
	@Override
	public float getMaximumExtent()
	{
		return e.magnitude();
	}
	
	/**
	 * An even index indicates that the vertex is on front of the OBB.
	 * Conversely, an odd index indicates that the vertex is on the back.
	 * <P>
	 * The first four indices store the bottom vertices while the last
	 * four store the top vertices.
	 * <P>
	 * If the modolo 4 of the index is 0 or 1, the vertex is on the left,
	 * else if the modolo 4 is 2 or 3, the vertex is on the right.
	 */
	public Vec3[] getVertices()
	{ 	
		Vec3[] vertices = new Vec3[8];
		
		Vec3 eu0 = u.xAxis.multiply(e.x);
		Vec3 eu1 = u.yAxis.multiply(e.y);
		Vec3 eu2 = u.zAxis.multiply(e.z);
		
		vertices[0] = c.     add(eu0).subtract(eu1).subtract(eu2); // right bottom front 
		vertices[1] = c.subtract(eu0).subtract(eu1).subtract(eu2); // left  bottom front
		vertices[2] = c.     add(eu0).subtract(eu1).     add(eu2); // right bottom back
		vertices[3] = c.subtract(eu0).subtract(eu1).     add(eu2); // left  bottom back
		vertices[4] = c.     add(eu0).     add(eu1).subtract(eu2); // right top    front
		vertices[5] = c.subtract(eu0).     add(eu1).subtract(eu2); // left  top    front
		vertices[6] = c.     add(eu0).     add(eu1).     add(eu2); // right top    back
		vertices[7] = c.subtract(eu0).     add(eu1).     add(eu2); // left  top    back
		
		return vertices;
	}
	
	@Override
	public boolean testOBB(OBB b)
	{
		OBB a = this;
		
		float ra, rb;
		
		float[][]    R = new float[3][3];
		float[][] AbsR = new float[3][3];
		
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
			{
				   R[j][i] = b.u.getAxis(i).dot(a.u.getAxis(j));
				AbsR[j][i] = abs(R[j][i]) + EPSILON;
			}
		
		Vec3 t = a.c.subtract(b.c);
		t = new Vec3(t.dot(b.u.xAxis), t.dot(b.u.yAxis), t.dot(b.u.zAxis));	
		
		for(int i = 0; i < 3; i++)
		{
			ra = b.e.get(i);
			rb = a.e.x * AbsR[i][0] + a.e.y * AbsR[i][1] + a.e.z * AbsR[i][2];
			if(abs(t.get(i)) > ra + rb) return false;
		}
		
		for(int i = 0; i < 3; i++)
		{
			ra = b.e.x * AbsR[0][i] + b.e.y * AbsR[1][i] + b.e.z * AbsR[2][i];
			rb = a.e.get(i);
			if(abs(t.x * R[0][i] + t.y * R[1][i] + t.z * R[2][i]) > ra + rb) return false;
		}
		
		ra = b.e.y * AbsR[2][0] + b.e.z * AbsR[1][0];
		rb = a.e.y * AbsR[0][2] + a.e.z * AbsR[0][1];
		if(abs(t.z * R[1][0] - t.y * R[2][0]) > ra + rb) return false;
		
		ra = b.e.y * AbsR[2][1] + b.e.z * AbsR[1][1];
		rb = a.e.x * AbsR[0][2] + a.e.z * AbsR[0][0];
		if(abs(t.z * R[1][1] - t.y * R[2][1]) > ra + rb) return false;
		
		ra = b.e.y * AbsR[2][2] + b.e.z * AbsR[1][2];
		rb = a.e.x * AbsR[0][1] + a.e.y * AbsR[0][0];
		if(abs(t.z * R[1][2] - t.y * R[2][2]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[2][0] + b.e.z * AbsR[0][0];
		rb = a.e.y * AbsR[1][2] + a.e.z * AbsR[1][1];
		if(abs(t.x * R[2][0] - t.z * R[0][0]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[2][1] + b.e.z * AbsR[0][1];
		rb = a.e.x * AbsR[1][2] + a.e.z * AbsR[1][0];
		if(abs(t.x * R[2][1] - t.z * R[0][1]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[2][2] + b.e.z * AbsR[0][2];
		rb = a.e.x * AbsR[1][1] + a.e.y * AbsR[1][0];
		if(abs(t.x * R[2][2] - t.z * R[0][2]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[1][0] + b.e.y * AbsR[0][0];
		rb = a.e.y * AbsR[2][2] + a.e.z * AbsR[2][1];
		if(abs(t.y * R[0][0] - t.x * R[1][0]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[1][1] + b.e.y * AbsR[0][1];
		rb = a.e.x * AbsR[2][2] + a.e.z * AbsR[2][0];
		if(abs(t.y * R[0][1] - t.x * R[1][1]) > ra + rb) return false;
		
		ra = b.e.x * AbsR[1][2] + b.e.y * AbsR[0][2];
		rb = a.e.x * AbsR[2][1] + a.e.y * AbsR[2][0];
		if(abs(t.y * R[0][2] - t.x * R[1][2]) > ra + rb) return false;
		
		return true;
	}
	
	@Override
	public boolean testSphere(Sphere s)
	{
		Vec3 p = closestPointToPoint(s.c);
		
		p = p.subtract(s.c);
		
		return p.dot() <= s.r * s.r;
	}

	public boolean testRay(Vec3 p0, Vec3 p1)
	{
		p0.subtract(c);
		p0 = new Vec3(p0.dot(u.xAxis), p0.dot(u.yAxis), p0.dot(u.zAxis));
		
		Vec3 min = c.subtract(e);
		Vec3 max = c.add(e);
		
		Vec3 d = p1.subtract(p0);
		Vec3 m = p0.add(p1).subtract(min).subtract(max);
		
		float adx = abs(d.x); if(abs(m.x) > e.x + adx) return false;
		float ady = abs(d.y); if(abs(m.y) > e.y + ady) return false;
		float adz = abs(d.z); if(abs(m.z) > e.z + adz) return false;
		
		adx += EPSILON; ady += EPSILON; adz += EPSILON;
		
		if(abs(m.y * d.z - m.z * d.y) > e.y * adz + e.z * ady) return false;
		if(abs(m.z * d.x - m.x * d.z) > e.x * adz + e.z * adx) return false;
		if(abs(m.x * d.y - m.y * d.x) > e.x * ady + e.y * adx) return false;
		
		return true;	
	}

	@Override
	public Vec3 closestPointToPoint(Vec3 p)
	{	
		Vec3 d = p.subtract(c);
		Vec3 q = new Vec3(c);
		
		float[] e = this.e.toArray();
		
		for(int i = 0; i < 3; i++)
		{
			float dist = d.dot(u.getAxis(i));
			
			if(dist >  e[i]) dist =  e[i];
			if(dist < -e[i]) dist = -e[i];
			
			q = q.add(u.getAxis(i).multiply(dist));
		}
		
		return q;
	}
	
	public Vec3 closestPointOnPerimeter(Vec3 p)
	{
		Vec3 d = p.subtract(c);
		Vec3 q = new Vec3(c);
		
		int n = getFaceIndex(getFaceVector(p));
		
		float[] e = this.e.toArray();

		for(int i = 0; i < 3; i++)
		{
			float dist = d.dot(u.getAxis(i));
			
			if(dist >  e[i] || (i * 2) + 1 == n) dist =  e[i];
			if(dist < -e[i] || (i * 2)     == n) dist = -e[i];
			
			q = q.add(u.getAxis(i).multiply(dist));
		}
		
		return q;
	}
	
	/**
	 * Return the index of the face with the normal passed as a parameter,
	 * or an invalid index of -1 if no face has that normal. 
	 */
	public int getFaceIndex(Vec3 normal)
	{
		Vec3[] normals = getAxisVectors();
		
		for(int i = 0; i < 6; i++)
			if(normals[i].equals(normal)) return i;
		
		return -1;
	}
	
	/**
	 * Returns the normals of each face of the OBB, multiplied by the parameter
	 * <code>scale</code>. These normals can be used to render the local axes
	 * of the bound.
	 */
	public Vec3[] getAxisVectors(float scale)
	{
		return new Vec3[]
		{
			c.subtract(u.xAxis.multiply(scale)), // 0 left
			c.	   add(u.xAxis.multiply(scale)), // 1 right
			c.subtract(u.yAxis.multiply(scale)), // 2 down
			c.	   add(u.yAxis.multiply(scale)), // 3 up	 
			c.subtract(u.zAxis.multiply(scale)), // 4 front 
			c.	   add(u.zAxis.multiply(scale)), // 5 back	
		};
	}
	
	public Vec3[] getAxisVectors() { return getAxisVectors(1); }
	
	public Vec3 closestPointToOBB(OBB b)
	{
		Vec3 p = closestPointToPoint(b.getPosition());
		
		for(int i = 0; i < 10; i++)
		{
			p = b.closestPointToPoint(p);
			p = closestPointToPoint(p);
		}
		
		return p;
	}
	
	public Vec3 getUpVector  (float scale) { return c.     add(u.yAxis.multiply(scale)); }
	public Vec3 getDownVector(float scale) { return c.subtract(u.yAxis.multiply(scale)); }
	
	public void displaySolid(GL2 gl, GLUT glut, float[] color)
	{
		gl.glColor4fv(color, 0);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);

		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glMultMatrixf(u.toArray(), 0);
			gl.glScalef(e.x * 2, e.y * 2, e.z * 2);
			
			glut.glutSolidCube(1);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
		
	public void displayWireframe(GL2 gl, GLUT glut, float[] color, boolean smooth)
	{
		if(color.length > 3)
			 gl.glColor4fv(color, 0);
		else gl.glColor3fv(color, 0);
		
		if(smooth)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_LINE_SMOOTH);
		}
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glMultMatrixf(u.toArray(), 0);
			gl.glScalef(e.x * 2, e.y * 2, e.z * 2);
			
			glut.glutWireCube(1);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_LINE_SMOOTH);
	}
	
	public void displayVertices(GL2 gl, GLUT glut, float[] color, boolean smooth)
	{
		float[][] vertices = new float[8][3];
		
		int i = 0;
		
		for(Vec3 vertex : getVertices())
		{
			vertices[i][0] = vertex.x;
			vertices[i][1] = vertex.y;
			vertices[i][2] = vertex.z;
			
			i++;
		}
		
		Renderer.displayPoints(gl, glut, vertices, color, 5, smooth);
	}
		
	public void displayAxes(GL2 gl, float scale)
	{
		Vec3[] axes = getAxisVectors(scale);
		
		for(int a = 0; a < 3; a++)
		{
			float[] c = {0, 0, 0, 1};
			
			c[a] = 1;
			
			gl.glColor4fv(c, 0);
			
			int i = a * 2;
			int j = i + 1;
			float[] u = axes[i].toArray();
			float[] v = axes[j].toArray();
			
			gl.glBegin(GL_LINES);
			
			gl.glVertex3fv(u, 0);
			gl.glVertex3fv(v, 0);
			
			gl.glEnd();
		}
	}
	
	public void displayPerimeterPtToPt(GL2 gl, GLUT glut, float[] p)
	{
		gl.glColor4f(0, 1, 1, 1);
		
		gl.glPushMatrix();
		{
			Vec3 vertex = closestPointOnPerimeter(new Vec3(p));

			gl.glTranslatef(vertex.x, vertex.y, vertex.z);
			glut.glutSolidSphere(0.2, 12, 12);
		}
		gl.glPopMatrix();
	}
	
	/**
	 * Returns a randomly calculated point that is within the bound.
	 */
	public Vec3 randomPointInside()
	{
		Vec3 r = getRandomVector();
	
		Vec3 x = u.xAxis.multiply(e.x * r.x);
		Vec3 y = u.yAxis.multiply(e.y * r.y);
		Vec3 z = u.zAxis.multiply(e.z * r.z);
		
		return c.add(x).add(y).add(z);
	}
}
