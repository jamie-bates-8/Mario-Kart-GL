package bates.jamie.graphics.util;

import java.io.File;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureLoader
{
	private static int texture_ID = 0;
	
	public static Texture load(GL2 gl, String filename, int filter, boolean anistropic)
	{
		Texture texture = null;
		
		try
		{
			texture = TextureIO.newTexture(new File(filename), true);
			
			if(anistropic) texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, filter);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return texture;
	}
	
	public static Texture load(GL2 gl, String filename)
	{
		Texture texture = null;
		
		try
		{
			texture = TextureIO.newTexture(new File(filename), true);
			
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		}
		catch(Exception e) { e.printStackTrace(); }
		
		System.out.println("Texture ID : " + texture_ID++);
		
		return texture;
	}
	
	public static Texture load(GL2 gl, String filename, String fileType)
	{
		Texture texture = null;
		TextureData data = null;
		
		try
		{
			data = TextureIO.newTextureData(gl.getGLProfile(), new File(filename), true, fileType);
			texture = TextureIO.newTexture(data);
			
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
			texture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		}
		catch(Exception e) { e.printStackTrace(); }
		
		return texture;
	}
}
