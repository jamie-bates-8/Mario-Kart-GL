package graphics.util;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINE_LOOP;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import static java.lang.Math.abs;

import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;


public class Renderer
{
	public Renderer() {}
	
	/**
     * Converts a list of faces representing a textured 3D model into OpenGL commands to
     * display the model in a scene
     * @param gl - the OpenGL context
     * @param objectFaces - the faces of the model to be displayed
     */
	public static void displayTexturedObject(GL2 gl, List<Face> objectFaces)
	{
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
	
	public static void displayGradientObject(GL2 gl, List<Face> objectFaces, Gradient gradient, float lower, float upper)
	{
		gl.glDisable(GL_TEXTURE_2D);
		
		for(Face face : objectFaces)
		{
			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				float ratio = (abs(lower) + face.getVy(i)) / (abs(lower) + abs(upper));
				
				float[] color = gradient.getColor(ratio);
				
				gl.glColor3f(color[0], color[1], color[2]);
				
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
		
		gl.glColor3f(1, 1, 1);
		gl.glEnable(GL_TEXTURE_2D);
	}
	
	public static void displayWildcardObject(GL2 gl, List<Face> objectFaces, Texture[] textures)
	{
		
		Face f = objectFaces.get(0);
		Texture current = f.hasWildcard() ?
				textures[f.getWildcard()] : f.getTexture();
		
		current.bind(gl);
		
		for(Face face : objectFaces)
		{	
			if(face.hasWildcard())
			{
				if(!current.equals(textures[face.getWildcard()]))
				{
					current = textures[face.getWildcard()];
					current.bind(gl);
				}
			}
			else if(!current.equals(face.getTexture()))
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
	
	public static void displayColoredObject(GL2 gl, List<Face> objectFaces, float[] color)
	{
		gl.glDisable(GL_TEXTURE_2D);
		gl.glColor3f(color[0], color[1], color[2]);
		
		for(Face face : objectFaces)
		{
			gl.glBegin(GL_TRIANGLES);

			for(int i = 0; i < face.getVertices().length; i++)
			{
				gl.glNormal3f(face.getNx(i), face.getNy(i), face.getNz(i));
				gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}

			gl.glEnd();
		}
		
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
		
		displayColoredObject(gl, objectFaces, color);
		
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

		for(Face face : objectFaces)
		{
			gl.glColor3f(color[0], color[1], color[2]);

			gl.glBegin(GL_LINE_LOOP);
			{
				for(int i = 0; i < face.getVertices().length; i ++)
					gl.glVertex3f(face.getVx(i), face.getVy(i), face.getVz(i));
			}
			gl.glEnd();
		}

		gl.glEnable(GL_LIGHTING);	
		gl.glEnable(GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public static void displayTexturedCuboid(GL2 gl, double x, double y, double z,
			double xScale, double yScale, double zScale, float rotation, Texture[] t)
	{
		gl.glPushMatrix();
		{
			gl.glTranslated(x, y, z);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScaled(xScale, yScale, zScale);

			t[0].bind(gl);

			gl.glBegin(GL_QUADS);
			{	
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);

				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
			}
			gl.glEnd();

			t[1].bind(gl);

			gl.glBegin(GL_QUADS);
			{		     
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f,  1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);

				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f,  1.0f);
			}
			gl.glEnd();

			t[2].bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-1.0f,  1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);

				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 1.0f,  1.0f,  1.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 1.0f, -1.0f,  1.0f);
			}    
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
}
