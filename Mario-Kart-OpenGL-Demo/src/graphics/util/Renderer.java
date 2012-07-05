package graphics.util;


import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;


public class Renderer
{
	public Renderer() {}
	
	/**
     * Converts a list of faces representing a 3D model into OpenGL commands to
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
	
	public static void displayWildcardObject(GL2 gl, List<Face> objectFaces, Texture[] textures)
	{
		
		Face f = objectFaces.get(0);
		Texture current = f.getTexture();
		
		if(f.hasWildcard()) current = textures[f.getWildcard()];
		else current = f.getTexture();
		
		current.bind(gl);
		
		for(Face face : objectFaces)
		{	
			if(!current.equals(face.getTexture()))
			{
				if(face.hasWildcard()) current = textures[face.getWildcard()];
				else current = face.getTexture();
				
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
		gl.glEnable(GL_COLOR_MATERIAL);
		
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
		
		for(Face face : objectFaces)
		{
			if(!face.hasTexture())
			{
				if(gl.glIsEnabled(GL_TEXTURE_2D)) gl.glDisable(GL_TEXTURE_2D);
				gl.glColor3f(color[0], color[1], color[2]);
				if(!gl.glIsEnabled(GL_COLOR_MATERIAL)) gl.glEnable(GL_COLOR_MATERIAL);

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

			if(!face.hasTexture())
			{
				gl.glEnable(GL_TEXTURE_2D);
			}
		}
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
