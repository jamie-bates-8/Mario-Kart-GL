package bates.jamie.graphics.entity;

import java.nio.ByteBuffer;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

import com.jogamp.common.nio.Buffers;

public class Volume
{
	private int imageWidth, imageHeight, imageDepth;
	private int textureID;
	
	private float orthoSize = 50.0f;
	
	public Volume(GL2 gl, int width, int height, int depth)
	{
		imageWidth  = width;
		imageHeight = height;
		imageDepth  = depth;
		
		createTexture(gl);
	}
	
	private void createTexture(GL2 gl)
	{
		ByteBuffer iBuffer = Buffers.newDirectByteBuffer(imageWidth * imageHeight * imageDepth);
		ByteBuffer cBuffer = Buffers.newDirectByteBuffer(imageWidth * imageHeight * imageDepth * 4);
		
		Vec3 centre = new Vec3(imageWidth, imageHeight, imageDepth);
		centre = centre.multiply(0.5f);
		float maximumDistance = centre.magnitude() * 0.5f;
		
		Random generator = new Random();

	    for(int i = 0; i < imageWidth; i++)
	    	for(int j = 0; j < imageHeight; j++)
	    		for(int k = 0; k < imageDepth; k++)
	    		{
	    			Vec3 position = new Vec3(i, j, k);
	    			float distance = centre.length(position);
	    			
	    			float alpha = 1.0f - (distance / maximumDistance);
	    			
	    			if(alpha < 0) alpha = 0;
	    			
	    			int  a = (int ) (alpha * generator.nextFloat() * 0.25f * 255);
	    			byte b = (byte) a;
	    			
	    			iBuffer.put(b);
	    		}

	    // Convert the data to RGBA data.
	    // Here we are simply putting the same value to R, G, B and A channels.
	    // Usually for raw data, the alpha value will be constructed by a threshold value given by the user 

	    for(int index = 0; index < imageWidth * imageHeight * imageDepth; ++index)
	    {
	    	cBuffer.put(index * 4 + 0, iBuffer.get(index));
	    	cBuffer.put(index * 4 + 1, iBuffer.get(index));
	    	cBuffer.put(index * 4 + 2, iBuffer.get(index));
	    	cBuffer.put(index * 4 + 3, iBuffer.get(index));
	    }

	    int[] textureIDs = new int[1];
	    gl.glGenTextures(1, textureIDs, 0);
	    textureID = textureIDs[0];

	    gl.glBindTexture(GL2.GL_TEXTURE_3D, textureID);
	    
	    gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
	    gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
	    gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_BORDER);
	    
	    gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	    gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);


	    gl.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_RGBA, imageWidth, imageHeight, imageDepth, 0,
	    				GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, cBuffer);
	    
	    gl.glBindTexture(GL2.GL_TEXTURE_3D, 0);
	}
	
	public void render(GL2 gl)
	{
		gl.glEnable(GL2.GL_ALPHA_TEST);
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_TEXTURE_3D);
		
	    gl.glAlphaFunc(GL2.GL_GREATER, 0.05f);
	    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
//	    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

	    gl.glMatrixMode(GL2.GL_TEXTURE);
	    gl.glLoadIdentity();

	    // Translate and make 0.5f as the center 
	    // (texture co ordinate is from 0 to 1. so center of rotation has to be 0.5f)
	    gl.glTranslatef(0.5f, 0.5f, 0.5f);

	    // A scaling applied to normalize the axis 
	    // (Usually the number of slices will be less so if this is not - 
	    // normalized then the z axis will look bulky)
	    // Flipping of the y axis is done by giving a negative value in y axis.
	    // This can be achieved either by changing the y co ordinates in -
	    // texture mapping or by negative scaling of y axis
	    
	    int minimumDimension = imageWidth < imageHeight ? imageWidth : imageHeight;
	        minimumDimension = imageDepth < minimumDimension ? imageDepth : minimumDimension;
	    
	    gl.glScalef( (float) imageWidth  / (float) minimumDimension, 
	         -1.0f * (float) imageHeight / (float) minimumDimension, 
	                 (float) imageDepth  / (float) minimumDimension );
	    
	    gl.glTranslatef(-0.5f,-0.5f, -0.5f);
	    
	    gl.glMatrixMode(GL2.GL_MODELVIEW_MATRIX);
	    
	    gl.glPushMatrix();
	    {
//	    	gl.glTranslatef(0, 25, 0);

	    	gl.glBindTexture(GL2.GL_TEXTURE_3D, textureID);

	    	for (float index = -1.0f; index <= 1.0f; index += 0.01f)
	    	{
	    		gl.glBegin(GL2.GL_QUADS);
	    		{
	    			gl.glTexCoord3f(0.0f, 0.0f, ((float) index + 1.0f) / 2.0f);
	    			gl.glVertex3f  (-orthoSize, -orthoSize, index * orthoSize);
	    			gl.glTexCoord3f(1.0f, 0.0f, ((float) index + 1.0f) / 2.0f);
	    			gl.glVertex3f  ( orthoSize,-orthoSize, index * orthoSize);
	    			gl.glTexCoord3f(1.0f, 1.0f, ((float) index + 1.0f) / 2.0f);
	    			gl.glVertex3f  ( orthoSize, orthoSize, index * orthoSize);
	    			gl.glTexCoord3f(0.0f, 1.0f, ((float) index + 1.0f) / 2.0f);
	    			gl.glVertex3f  (-orthoSize, orthoSize, index * orthoSize);
	    		}
	    		gl.glEnd();
	    	}
	    }
	    gl.glPopMatrix();
	    
	    gl.glBindTexture(GL2.GL_TEXTURE_3D, 0);
	    
	    gl.glMatrixMode(GL2.GL_TEXTURE);
	    gl.glLoadIdentity();
	    gl.glMatrixMode(GL2.GL_MODELVIEW_MATRIX);
	    
	    gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	    
	    gl.glDisable(GL2.GL_ALPHA_TEST);
	    gl.glDisable(GL2.GL_BLEND);
	    gl.glDisable(GL2.GL_TEXTURE_3D);
	}
}
