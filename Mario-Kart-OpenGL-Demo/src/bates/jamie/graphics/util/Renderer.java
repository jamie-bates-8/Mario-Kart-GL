package bates.jamie.graphics.util;

import static java.lang.Math.abs;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINE_LOOP;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Model;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;


public class Renderer
{
	public static boolean anisotropic = true;
	
	public Renderer() {}
	
	/**
     * Converts a list of faces representing a textured 3D model into OpenGL commands to
     * display the model in a scene
     * @param gl - the OpenGL context
     * @param objectFaces - the faces of the model to be displayed
     */
	public static void displayTexturedObject(GL2 gl, List<Face> objectFaces)
	{
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		Texture current = objectFaces.get(0).getTexture();
		current.bind(gl);
		
		for(Face face : objectFaces)
		{	
			if(!current.equals(face.getTexture()))
			{
				current = face.getTexture();
				current.bind(gl);
			}

			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f  (face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glTexCoord2f(face.getTu(i), face.getTv(i));
				gl.glVertex3f  (face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
	}
	
	public static void displayNormalMappedObject(GL2 gl, List<Face> objectFaces, Texture normalMap)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glEnable(GL2.GL_TEXTURE_2D); normalMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0); gl.glEnable(GL2.GL_TEXTURE_2D);
		
		Texture current = objectFaces.get(0).getTexture();
		current.bind(gl);
		
		for(Face face : objectFaces)
		{	
			if(!current.equals(face.getTexture()))
			{
				current = face.getTexture();
				current.bind(gl);
			}

			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f  (face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glTexCoord2f(face.getTu(i), face.getTv(i));
				gl.glVertex3f  (face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
	}
	
	public static void displayPoints(GL2 gl, GLUT glut, float[][] points, float[] color, float size, boolean smooth)
	{
		if(color.length > 3)
			 gl.glColor4fv(color, 0);
		else gl.glColor3fv(color, 0);
		
		if(smooth)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(size);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		}
		
		for(float[] point : points)
		{
			if(smooth)
			{
				gl.glBegin(GL2.GL_POINTS);
				gl.glVertex3f(point[0], point[1], point[2]);
				gl.glEnd();
			}
			else
			{
				gl.glPushMatrix();
				{
					gl.glTranslatef(point[0], point[1], point[2]);
					glut.glutSolidSphere(size, 6, 6);
				}
				gl.glPopMatrix();
			}
		}
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
	}
	
	public static void displayPoints(GL2 gl, float[] points, float[] color, float size)
	{
		if(color.length > 3)
			 gl.glColor4fv(color, 0);
		else gl.glColor3fv(color, 0);
		
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glPointSize(size);
		gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		
		for(int i = 0; i < points.length / 3; i++)
		{
			gl.glBegin(GL2.GL_POINTS);
			gl.glVertex3f(points[i * 3 + 0], points[i * 3 + 1], points[i * 3 + 2]);
			gl.glEnd();
		}
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_POINT_SMOOTH);
	}
	
	public static void displayGradientObject(GL2 gl, List<Face> objectFaces, Gradient gradient, float lower, float upper)
	{
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glBegin(GL_TRIANGLES);
		
		for(Face face : objectFaces)
		{
			for(int i = 0; i < face.getVertices().length; i++)
			{
				float ratio = (abs(lower) + face.getVy(i)) / (abs(lower) + abs(upper));
				
				float[] color = gradient.getColor(ratio);
				
				gl.glColor3f(color[0], color[1], color[2]);
				
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}	
		}
		
		gl.glEnd();
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);
	}
	
	public static float[][] getCellShades(float[] color, float increment, int shades)
	{
		float[][] colors = new float[1 + shades * 2][3];
		
		colors[shades] = color;
		
		for(int i = 1; i <= shades; i++)
		{
			float[] _color = new float[3];
			
			_color[0] = color[0] - (increment * i);
			_color[1] = color[1] - (increment * i);
			_color[2] = color[2] - (increment * i);
			
			colors[shades - i] = _color;
			
			float[] color_ = new float[3];
			
			color_[0] = color[0] + (increment * i);
			color_[1] = color[1] + (increment * i);
			color_[2] = color[2] + (increment * i);
			
			colors[shades + i] = color_;
		}
		
		return colors;
	}
	
	public static void cellShadeObject(GL2 gl, List<Face> objectFaces, float[] light, float[][] colors)
	{	
		gl.glEnable(GL2.GL_TEXTURE_1D);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);
		
	    FloatBuffer shader = FloatBuffer.allocate(colors.length * 3);
	
	    for(int i = 0; i < colors.length; i++)
	    {
	    	shader.put(colors[i][0]);
	    	shader.put(colors[i][1]);
	    	shader.put(colors[i][2]);
	    }
	    
	    shader.flip();
	    
	    gl.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGB, colors.length, 0, GL2.GL_RGB, 
	    GL2.GL_FLOAT, shader);
	    
	    light = Vector.normalize(light);
		
		for(Face face : objectFaces)
		{
			gl.glBegin(GL_TRIANGLES);
			
			for(int i = 0; i < face.getVertices().length; i++)
			{	
				float[] normal = {face.getNx(i), face.getNy(i), face.getNz(i)};
				normal = Vector.normalize(normal);
				
				float shade = Vector.dot(light, normal);
				shade = shade < 0 ? 0 : shade;
				shade = shade > 1 ? 1 : shade;
				
				gl.glTexCoord1f(shade);
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));	
			}
			
			gl.glEnd();
		}
		
		gl.glColor3f(1, 1, 1);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_1D);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);	
	}

	public static void displayWildcardObject(GL2 gl, List<Face> objectFaces, Texture[] textures)
	{
		Face f = objectFaces.get(0);
		Texture current = f.hasWildcard() ? textures[f.getWildcard()] : f.getTexture();
		
		current.bind(gl);
		
		for(Face face : objectFaces)
		{	
			if(face.hasWildcard())
			{
				if(!current.equals(textures[face.getWildcard()]))
				{
					current = textures[face.getWildcard()];
					current.bind(gl);
					
					if(anisotropic) current.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
					else current.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 1);
				}
			}
			else if(!current.equals(face.getTexture()))
			{
				current = face.getTexture();
				current.bind(gl);
				
				if(anisotropic) current.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
				else current.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 1);
			}

			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f  (face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glTexCoord2f(face.getTu(i), face.getTv(i));
				gl.glVertex3f  (face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
	}
	
	public static void displayColoredObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		gl.glDisable(GL_TEXTURE_2D);
		gl.glColor3f(color[0], color[1], color[2]);
		
		gl.glBegin(GL_TRIANGLES);
		
		for(Face face : objectFaces)
		{
			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}	
		}
		
		gl.glEnd();
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);	
	}
	
	public static void displayColoredObject(GL2 gl, List<Face> objectFaces, float intensity)
	{
		float[] color = {intensity, intensity, intensity};
		displayColoredObject(gl, objectFaces, color);
	}
	
	public static void displayTransparentObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		displayColoredObject(gl, objectFaces, color);
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
	
	public static void displayGlassObject(GL2 gl, List<Face> objectFaces, float opacity)
	{
		float[] color = {opacity, opacity, opacity};
		displayGlassObject(gl, objectFaces, color);
	}
	
	public static void displayGlassObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		gl.glDisable(GL_TEXTURE_2D);
		gl.glColor4f(color[0], color[1], color[2], 0.25f);
		
		gl.glDisable(GL_LIGHTING);
		gl.glEnable(GL_BLEND);
		
		gl.glFrontFace(GL2.GL_CW);
		
		gl.glBegin(GL_TRIANGLES);
		
		for(Face face : objectFaces)
		{
			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}	
		}
		
		gl.glEnd();
		
		gl.glFrontFace(GL2.GL_CCW);
		
		gl.glBegin(GL_TRIANGLES);
		
		for(Face face : objectFaces)
		{
			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}	
		}
		
		gl.glEnd();
		
		gl.glColor4f(1, 1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);	
		
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_LIGHTING);
	}
	
	public static void displayTransparentObject(GL2 gl, List<Face> objectFaces, float intensity)
	{
		float[] color = {intensity, intensity, intensity};
		displayTransparentObject(gl, objectFaces, color);
	}
	
	public static void displayPartiallyTexturedObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		Texture current = objectFaces.get(0).getTexture();
		current.bind(gl);
		
		boolean colorMode = false;
		
		for(Face face : objectFaces)
		{
			if(!face.hasTexture())
			{
				gl.glDisable(GL_TEXTURE_2D);
				gl.glColor3f(color[0], color[1], color[2]);
				
				colorMode = true;

			}
			else if(!current.equals(face.getTexture()))
			{
				gl.glEnable(GL_TEXTURE_2D);
				current = face.getTexture();
				current.bind(gl);
				
				colorMode = false;
			}

			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f  (face.getNx(i), face.getNy(i), face.getNz(i));
				if(!colorMode) gl.glTexCoord2f(face.getTu(i), face.getTv(i));
				gl.glVertex3f  (face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayWireframeObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glColor3f(color[0], color[1], color[2]);
		
		for(Face face : objectFaces)
		{
			gl.glBegin(GL_LINE_LOOP);
			
			for(int i = 0; i < face.getVertices().length; i++)
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			
			gl.glEnd();
		}

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayWireframeObject(GL2 gl, float[][] vertices, int n, float[] color)
	{
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glColor3f(color[0], color[1], color[2]);

		for(int i = 0; i < vertices.length; i += n)
		{
			gl.glBegin(GL_LINE_LOOP);
			{
				for(int j = 0; j < n; j++)
				{
					float[] v = vertices[i + j];
					gl.glVertex3f(v[0], v[1], v[2]);
				}
			}
			gl.glEnd();
		}

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayLines(GL2 gl, float[][] vertices, int n, float[] color, boolean stipple)
	{
		boolean texture  = gl.glIsEnabled(GL2.GL_TEXTURE_2D);
		boolean lighting = gl.glIsEnabled(GL2.GL_LIGHTING  );
		
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		if(stipple)
		{
			gl.glEnable(GL2.GL_LINE_STIPPLE);
			gl.glLineStipple(4, (short) 0xBBBB);
		}
		
		gl.glColor3f(color[0], color[1], color[2]);

		for(int i = 0; i < vertices.length; i += n)
		{
			gl.glBegin(GL2.GL_LINE_STRIP);
			{
				for(int j = 0; j < n; j++)
				{
					float[] v = vertices[i + j];
					gl.glVertex3f(v[0], v[1], v[2]);
				}
			}
			gl.glEnd();
		}
		
		gl.glDisable(GL2.GL_LINE_STIPPLE);

		if(lighting) gl.glEnable(GL_LIGHTING  );	
		if(texture ) gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayLines(GL2 gl, float[] vertices, float[] color, boolean stipple)
	{
		boolean texture  = gl.glIsEnabled(GL2.GL_TEXTURE_2D);
		boolean lighting = gl.glIsEnabled(GL2.GL_LIGHTING  );
		
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		if(stipple)
		{
			gl.glEnable(GL2.GL_LINE_STIPPLE);
			gl.glLineStipple(4, (short) 0xBBBB);
		}
		
		gl.glColor3f(color[0], color[1], color[2]);

		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			int offset = 0;
			
			while(offset < vertices.length)
			{
				gl.glVertex3fv(vertices, offset);
				offset += 3;
			}
		}
		gl.glEnd();
		
		gl.glDisable(GL2.GL_LINE_STIPPLE);

		if(lighting) gl.glEnable(GL_LIGHTING  );	
		if(texture ) gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displaySegment(GL2 gl, Vec3 p0, Vec3 p1, float[] color)
	{
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glColor3f(color[0], color[1], color[2]);

		gl.glBegin(GL2.GL_LINES);
		{	
			gl.glVertex3f(p0.x, p0.y, p0.z);
			gl.glVertex3f(p1.x, p1.y, p1.z);	
		}
		gl.glEnd();

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayNURBSCurve(GL2 gl, Vec3[] controlPoints, float[] knots, float[] color, int sampleRate)
	{
		boolean texture  = gl.glIsEnabled(GL2.GL_TEXTURE_2D);
		boolean lighting = gl.glIsEnabled(GL2.GL_LIGHTING  );
		
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glColor3f(color[0], color[1], color[2]);
		
		float increment = 1.0f / sampleRate;

		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			for(int i = 0; i <= sampleRate; i++)
			{
				Vec3 p = evaluateNURBSCurveCubic(controlPoints, knots, increment * i);
				gl.glVertex3fv(p.toArray(), 0);
			}
			System.out.println();
		}
		gl.glEnd();

		if(lighting) gl.glEnable(GL_LIGHTING  );	
		if(texture ) gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);	
	}
	
	public static Vec3 evaluateNURBSCurveCubic(Vec3[] controlPoints, float[] knots, float u)
	{	
		float[] R = new float[4];
		
		u *= knots[knots.length - 1];
		
		int i = 0;
		while((knots[i] > u || u >= knots[i + 1]) && knots[i + 1] != knots[knots.length - 1]) i++;
		
		if(i < 2) i = 2;
		if(i > knots.length - 4) i = knots.length - 4;
		if(i > controlPoints.length - 2) i = controlPoints.length - 2;
		
		float k0 = knots[i - 2];
		float k1 = knots[i - 1];
		float k2 = knots[i + 0];
		float k3 = knots[i + 1];
		float k4 = knots[i + 2];
		float k5 = knots[i + 3];
		
		Vec3 c0 = controlPoints[i - 2];
		Vec3 c1 = controlPoints[i - 1];
		Vec3 c2 = controlPoints[i + 0];
		Vec3 c3 = controlPoints[i + 1];
		
		float l = (k3 - u) / ((k3 - k2) * (k3 - k1) + 1E-5f);
		float r = (u - k2) / ((k3 - k2) * (k4 - k2) + 1E-5f);
		
		float a = ( r * (u - k2)) / (k5 - k2 + 1E-5f);
		float b = ((r * (k4 - u)) + (l * (u - k1))) / (k4 - k1 + 1E-5f);
		float c = ( l * (k3 - u)) / (k3 - k0 + 1E-5f);
		
		R[0] =  c * (k3 - u);
		R[1] = (b * (k4 - u)) + (c * (u - k0));
		R[2] = (a * (k5 - u)) + (b * (u - k1));
		R[3] =  a * (u - k2);
		
		float w = R[0] + R[1] + R[2] + R[3] + 1E-5f;
		
		Vec3 C = c0.multiply(R[0]).add(c1.multiply(R[1])).add(c2.multiply(R[2])).add(c3.multiply(R[3]));
		
		return C.divide(w);
	}
	
	public static Vec3 evaluateNURBSCurve(Vec3[] controlPoints, float[] knots, float u)
	{
		Vec3 c = new Vec3();
		
		u *= knots[knots.length - 1];
		
		for(int i = 0; i < controlPoints.length; i++)
		{
			float R = basis(i, 3, knots, u);
			float denom = 1E-5f;
			
			for(int j = 0; j < controlPoints.length; j++)
				denom += basis(j, 3, knots, u);
			
			R /= denom;
			
			c = c.add(controlPoints[i].multiply(R));
		}
		
		return c;
	}
	
	private static float basis(int i, int n, float[] knots, float u) 
	{	
		if(n == 0)
		{
			if(knots[i] <= u && u <= knots[i + 1]) return 1.0f;
			else return 0.0f;
		}
		
		float f = f(i    , n, knots, u);
		float g = g(i + 1, n, knots, u);
		
		return f * basis(i, n - 1, knots, u) + g * basis(i + 1, n - 1, knots, u);
	}
	
	private static float f(int i, int n, float[] knots, float u)
	{
		return (u - knots[i]) / (knots[i + n] - knots[i] + 1E-5f);
	}
	
	private static float g(int i, int n, float[] knots, float u)
	{
		return (knots[i + n] - u) / (knots[i + n] - knots[i] + 1E-5f);
	}
	
	public static void displayCubicBezierCurve(GL2 gl, Vec3[] controlPoints, float[] color, int sampleRate)
	{
		boolean texture  = gl.glIsEnabled(GL2.GL_TEXTURE_2D);
		boolean lighting = gl.glIsEnabled(GL2.GL_LIGHTING  );
		
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glColor3f(color[0], color[1], color[2]);
		
		float increment = 1.0f / sampleRate;

		gl.glBegin(GL2.GL_LINE_STRIP);
		{
			for(int i = 0; i <= sampleRate; i++)
			{
				Vec3 p = bezierCurveCubic(controlPoints, increment * i);
				gl.glVertex3fv(p.toArray(), 0);
			}
		}
		gl.glEnd();

		if(lighting) gl.glEnable(GL_LIGHTING  );	
		if(texture ) gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);	
	}
	
	public static Vec3 bezierCurveCubic(Vec3[] controlPoints, float u)
	{
		float v = 1 - u;
		
		Vec3 p =  controlPoints[0].multiply(v * v * v);
		p = p.add(controlPoints[1].multiply(v * v * u * 3));
		p = p.add(controlPoints[2].multiply(v * u * u * 3));
		p = p.add(controlPoints[3].multiply(u * u * u));
		
		return p;
	}
	
	public static Vec3 bezierCurveDerivative(Vec3[] controlPoints, float u)
	{
		float v = 1 - u;
		
		Vec3 p =  controlPoints[1].subtract(controlPoints[0]).multiply(v * v * 3);
		p = p.add(controlPoints[2].subtract(controlPoints[1]).multiply(v * u * 6));
		p = p.add(controlPoints[3].subtract(controlPoints[2]).multiply(u * u * 3));
		
		return p;
	}
	
	public static Vec3 bezierCurve(Vec3[] controlPoints, int order, float u)
	{
		Vec3 p = new Vec3();
		
		for(int i = 0; i < 4; i++)
			p = p.add(controlPoints[i].multiply(bernstein(3, i, u)));
		
		return p;
	}
	
	private static double bernstein(int n, int i, float u)
	{
		int binom = binomial(n, i);
		
		return binom * Math.pow(u, i) * Math.pow(1 - u, n  - i);
	}
	
	private static int binomial(int n, int k)
	{
		int c = 1;
		
		for(int i = 1; i <= k; i++)
			c *= (float) (n + 1 - i) / i; 
		
		return c;
	}
	
	public static void displayQuads(GL2 gl, float[][] vertices, float[] color)
	{
		gl.glDisable(GL_LIGHTING);
		gl.glDisable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		gl.glColor3f(color[0], color[1], color[2]);

		for(int i = 0; i < vertices.length; i += 4)
		{
			gl.glBegin(GL2.GL_QUADS);
			{
				for(int j = 0; j < 4; j++)
				{
					float[] v = vertices[i + j];
					gl.glVertex3f(v[0], v[1], v[2]);
				}
			}
			gl.glEnd();
		}

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		gl.glDisable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayTexturedCuboid(GL2 gl, Vec3 centre, Vec3 scale,
			float rotation, Texture[] textures, float textureScale)
	{
		gl.glPushMatrix();
		{
			gl.glTranslated(centre.x, centre.y, centre.z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScaled(scale.x, scale.y, scale.z);

			textures[0].bind(gl);
			
			Vec3 t = new Vec3((int) (scale.x / textureScale),
					          (int) (scale.y / textureScale),
					          (int) (scale.z / textureScale));

			gl.glBegin(GL_QUADS);
			{	
				gl.glNormal3f(+1, 0, 0); // right
				
				gl.glTexCoord2f(t.z,   0); gl.glVertex3f(+1, -1, -1);
				gl.glTexCoord2f(t.z, t.y); gl.glVertex3f(+1, +1, -1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(+1, +1, +1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(+1, -1, +1);
				
				gl.glNormal3f(-1, 0, 0); // left

				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(t.z,   0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(t.z, t.y); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(-1, +1, -1);
			}
			gl.glEnd();

			textures[1].bind(gl);

			gl.glBegin(GL_QUADS);
			{		 
				gl.glNormal3f(0, +1, 0); // up
				
				gl.glTexCoord2f(  0, t.z); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(t.x, t.z); gl.glVertex3f( 1, +1, -1);
				
				gl.glNormal3f(0, -1, 0); // down

				gl.glTexCoord2f(t.x, t.z); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(  0, t.z); gl.glVertex3f( 1, -1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f(-1, -1, +1);
			}
			gl.glEnd();

			textures[2].bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glNormal3f(0, 0, +1); // front
				
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(t.x, t.y); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(-1, +1, +1);
				
				gl.glNormal3f(0, 0, -1); // back
				 
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(t.x, t.y); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f( 1, +1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f( 1, -1, -1);
			}    
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
	
	public static void displayBumpMappedCuboid(GL2 gl, Vec3 centre, Vec3 scale, float rotation,
			Texture[] colourMaps, Texture[] normalMaps, Texture[] heightMaps, float textureScale)
	{
		Vec3 t = new Vec3((int) (scale.x / textureScale),
		          		  (int) (scale.y / textureScale),
		          		  (int) (scale.z / textureScale));
		
		gl.glPushMatrix();
		{
			gl.glTranslated(centre.x, centre.y, centre.z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(scale.x, scale.y, scale.z);
			
			gl.glActiveTexture(GL2.GL_TEXTURE2); heightMaps[0].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE1); normalMaps[0].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE0); colourMaps[0].bind(gl);

			gl.glBegin(GL_QUADS);
			{	
				gl.glVertexAttrib3f(1, 0, 0, -1);
				gl.glNormal3f(+1, 0, 0); // right
				
				gl.glTexCoord2f(t.z,   0); gl.glVertex3f(+1, -1, -1);
				gl.glTexCoord2f(t.z, t.y); gl.glVertex3f(+1, +1, -1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(+1, +1, +1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(+1, -1, +1);
				
				gl.glVertexAttrib3f(1, 0, 0, +1);
				gl.glNormal3f(-1, 0, 0); // left

				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(t.z,   0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(t.z, t.y); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(-1, +1, -1);
			}
			gl.glEnd();

			gl.glActiveTexture(GL2.GL_TEXTURE2); heightMaps[1].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE1); normalMaps[1].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE0); colourMaps[1].bind(gl);

			gl.glBegin(GL_QUADS);
			{		
				gl.glVertexAttrib3f(1, +1, 0, 0);
				gl.glNormal3f(0, +1, 0); // up
				
				gl.glTexCoord2f(  0, t.z); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(t.x, t.z); gl.glVertex3f( 1, +1, -1);
				
				gl.glVertexAttrib3f(1, -1, 0, 0);
				gl.glNormal3f(0, -1, 0); // down

				gl.glTexCoord2f(t.x, t.z); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(  0, t.z); gl.glVertex3f( 1, -1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f(-1, -1, +1);
			}
			gl.glEnd();

			gl.glActiveTexture(GL2.GL_TEXTURE2); heightMaps[2].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE1); normalMaps[2].bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE0); colourMaps[2].bind(gl);

			
			gl.glBegin(GL_QUADS);
			{
				gl.glVertexAttrib3f(1, +1, 0, 0);
				gl.glNormal3f(0, 0, +1); // front
				
				gl.glTexCoord2f(  0,   0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(t.x, t.y); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f(-1, +1, +1);
				
				gl.glVertexAttrib3f(1, -1, 0, 0);
				gl.glNormal3f(0, 0, -1); // back
				
				gl.glTexCoord2f(t.x,   0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(t.x, t.y); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(  0, t.y); gl.glVertex3f( 1, +1, -1);
				gl.glTexCoord2f(  0,   0); gl.glVertex3f( 1, -1, -1);
			}    
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
	
	private static final float ONE_THIRD = 1.0f / 3.0f;
	private static final float TWO_THIRD = 2.0f / 3.0f;
	
	public static void displayBumpMappedCube(GL2 gl, Vec3 centre, float scale, float rotation)
	{	
		float s = ONE_THIRD;
		float t = TWO_THIRD;
		
		gl.glPushMatrix();
		{
			gl.glTranslated(centre.x, centre.y, centre.z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(scale, scale, scale);

			gl.glBegin(GL_QUADS);
			{	
				gl.glVertexAttrib3f(1, 0, 0, -1);
				gl.glNormal3f(+1, 0, 0); // right
				
				gl.glTexCoord2f(s, 0); gl.glVertex3f(+1, -1, -1);
				gl.glTexCoord2f(s, 1); gl.glVertex3f(+1, +1, -1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(+1, +1, +1);
				gl.glTexCoord2f(0, 0); gl.glVertex3f(+1, -1, +1);
				
				gl.glVertexAttrib3f(1, 0, 0, +1);
				gl.glNormal3f(-1, 0, 0); // left

				gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(s, 0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(s, 1); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, +1, -1);
				
				//-----------------------------------------------
	
				gl.glVertexAttrib3f(1, +1, 0, 0);
				gl.glNormal3f(0, +1, 0); // up
				
				gl.glTexCoord2f(s, 1); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(s, 0); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(t, 0); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(t, 1); gl.glVertex3f( 1, +1, -1);
				
				gl.glVertexAttrib3f(1, -1, 0, 0);
				gl.glNormal3f(0, -1, 0); // down

				gl.glTexCoord2f(t, 1); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(s, 1); gl.glVertex3f( 1, -1, -1);
				gl.glTexCoord2f(s, 0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(t, 0); gl.glVertex3f(-1, -1, +1);
				
				//-----------------------------------------------

				gl.glVertexAttrib3f(1, +1, 0, 0);
				gl.glNormal3f(0, 0, +1); // front
				
				gl.glTexCoord2f(t, 0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(1, 0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(t, 1); gl.glVertex3f(-1, +1, +1);
				
				gl.glVertexAttrib3f(1, -1, 0, 0);
				gl.glNormal3f(0, 0, -1); // back
				
				gl.glTexCoord2f(1, 0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(t, 1); gl.glVertex3f( 1, +1, -1);
				gl.glTexCoord2f(t, 0); gl.glVertex3f( 1, -1, -1);
			}    
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
	
	public static void displayBumpMappedCubeAccelerated(GL2 gl, Vec3 centre, float scale, float rotation)
	{	
		gl.glPushMatrix();
		{
			gl.glTranslated(centre.x, centre.y, centre.z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(scale, scale, scale);

			multi_tex_cube_model.render(gl);
		}
		gl.glPopMatrix();
	}
	
	private static final float[] VERTEX_ARRAY =
	{
		+1, -1, -1, +1, +1, -1, +1, +1, +1, +1, -1, +1, // right
		-1, -1, -1, -1, -1, +1, -1, +1, +1, -1, +1, -1, // left
		-1, +1, -1, -1, +1, +1, +1, +1, +1, +1, +1, -1, // up
		-1, -1, -1, +1, -1, -1, +1, -1, +1, -1, -1, +1, // down
		-1, -1, +1, +1, -1, +1, +1, +1, +1, -1, +1, +1, // back
		-1, -1, -1, -1, +1, -1, +1, +1, -1, +1, -1, -1  // front
	};

	private static final float[] NORMAL_ARRAY =
	{
		+1, 0, 0, +1, 0, 0, +1, 0, 0, +1, 0, 0,
		-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
		0, +1, 0, 0, +1, 0, 0, +1, 0, 0, +1, 0,
		0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
		0, 0, +1, 0, 0, +1, 0, 0, +1, 0, 0, +1,
		0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1
	};

	private static final float[] TANGENT_ARRAY =
	{
		0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
		0, 0, +1, 0, 0, +1, 0, 0, +1, 0, 0, +1,
		+1, 0, 0, +1, 0, 0, +1, 0, 0, +1, 0, 0,
		-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
		+1, 0, 0, +1, 0, 0, +1, 0, 0, +1, 0, 0,
		-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
	};

	private static final float[] TCOORD_ARRAY_MULTI_TEX =
	{
		ONE_THIRD, 0, ONE_THIRD, 1, 0, 1, 0, 0,
		0, 0, ONE_THIRD, 0, ONE_THIRD, 1, 0, 1,
		ONE_THIRD, 1, ONE_THIRD, 0, TWO_THIRD, 0, TWO_THIRD, 1,
		TWO_THIRD, 1, ONE_THIRD, 1, ONE_THIRD, 0, TWO_THIRD, 0,
		TWO_THIRD, 0, 1, 0, 1, 1, TWO_THIRD, 1,
		1, 0, 1, 1, TWO_THIRD, 1, TWO_THIRD, 0
	};
	
	private static final float[] TCOORD_ARRAY =
	{
		1, 0, 1, 1, 0, 1, 0, 0,
		0, 0, 1, 0, 1, 1, 0, 1,
		0, 1, 0, 0, 1, 0, 1, 1,
		1, 1, 0, 1, 0, 0, 1, 0,
		0, 0, 1, 0, 1, 1, 0, 1,
		1, 0, 1, 1, 0, 1, 0, 0
	};

	public static Model cube_model = new Model(VERTEX_ARRAY, NORMAL_ARRAY, TCOORD_ARRAY, TANGENT_ARRAY, null, 4);
	public static Model multi_tex_cube_model = new Model(VERTEX_ARRAY, NORMAL_ARRAY, TCOORD_ARRAY_MULTI_TEX, TANGENT_ARRAY, null, 4);
	public static Model multi_tex_bevelled_cube_model = OBJParser.parseTexturedTriangleMesh("bevelled_block");
	public static Model bevelled_cube_model = OBJParser.parseTexturedTriangleMesh("checkered_block");
}
