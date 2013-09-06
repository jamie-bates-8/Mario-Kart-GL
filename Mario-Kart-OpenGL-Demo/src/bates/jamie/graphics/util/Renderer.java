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
				gl.glVertex3fv(point, 0);
				gl.glEnd();
			}
			else
			{
				gl.glPushMatrix();
				{
					gl.glTranslatef(point[0], point[1], point[2]);
					glut.glutSolidSphere(0.1, 6, 6);
				}
				gl.glPopMatrix();
			}
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

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
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
	
	public static void displayTexturedCuboid(GL2 gl, float x, float y, float z,
			float xScale, float yScale, float zScale, float rotation, Texture[] t)
	{
		gl.glPushMatrix();
		{
			gl.glTranslated(x, y, z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScaled(xScale, yScale, zScale);

			t[0].bind(gl);

			gl.glBegin(GL_QUADS);
			{	
				gl.glNormal3f(+1, 0, 0);
				
				gl.glTexCoord2f(1, 0); gl.glVertex3f(+1, -1, -1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(+1, +1, -1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(+1, +1, +1);
				gl.glTexCoord2f(0, 0); gl.glVertex3f(+1, -1, +1);
				
				gl.glNormal3f(-1, 0, 0);

				gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, +1, -1);
			}
			gl.glEnd();

			t[1].bind(gl);

			gl.glBegin(GL_QUADS);
			{		 
				gl.glNormal3f(0, +1, 0);
				
				gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, +1, +1);
				gl.glTexCoord2f(1, 0); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f( 1, +1, -1);
				
				gl.glNormal3f(0, -1, 0); 

				gl.glTexCoord2f(1, 1); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f( 1, -1, -1);
				gl.glTexCoord2f(0, 0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(1, 0); gl.glVertex3f(-1, -1, +1);
			}
			gl.glEnd();

			t[2].bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glNormal3f(0, 0, +1);
				
				gl.glTexCoord2f(0, 0); gl.glVertex3f(-1, -1, +1);
				gl.glTexCoord2f(1, 0); gl.glVertex3f( 1, -1, +1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f( 1, +1, +1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f(-1, +1, +1);
				
				gl.glNormal3f(0, 0, -1);
				
				gl.glTexCoord2f(1, 0); gl.glVertex3f(-1, -1, -1);
				gl.glTexCoord2f(1, 1); gl.glVertex3f(-1, +1, -1);
				gl.glTexCoord2f(0, 1); gl.glVertex3f( 1, +1, -1);
				gl.glTexCoord2f(0, 0); gl.glVertex3f( 1, -1, -1);
			}    
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
}
