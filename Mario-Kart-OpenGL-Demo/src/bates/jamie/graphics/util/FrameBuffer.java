package bates.jamie.graphics.util;

import javax.media.opengl.GL2;

public class FrameBuffer
{
	public static String checkFramebufferError(int status)
	{
		switch(status)
		{
			case GL2.GL_FRAMEBUFFER_UNDEFINED                     : return "Frame Buffer Undefined : No Window?";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT         : return "Frame Buffer Incomplete Attachment : Check status of each attachment";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT : return "Attach at least one buffer to the FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER        : return "Frame Buffer Incomplete Draw Buffer : Check that all attachments enabled" +
					                                                       "via glDrawBuffers exists in FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER        : return "Frame Buffer Incomplete Read Buffer : Check that all attachments enabled" +
            															   "via glReadBuffer exists in FBO";
			case GL2.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS         : return "Framw Buffer Incomplete Dimensions";
			
			default : return "Error undefined";	
		}
	}
}
