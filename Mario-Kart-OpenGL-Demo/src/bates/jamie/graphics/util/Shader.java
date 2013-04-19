package bates.jamie.graphics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import java.util.Scanner;

import javax.media.opengl.GL2;

public class Shader
{
	public int shaderID;
	 
	public String[] vertSource;
	public String[] fragSource;
	
	public int vertProgram;
	public int fragProgram;
	
	public Shader(GL2 gl, String vShader, String fShader)
	{	
		attachPrograms(gl, vShader, fShader);
	}

	public void attachPrograms(GL2 gl, String vShader, String fShader)
	{
		vertProgram = gl.glCreateShader(GL2.GL_VERTEX_SHADER  );
		fragProgram = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		vertSource = parseSource(vShader);
	
		gl.glShaderSource (vertProgram, 1, vertSource, null, 0);
		gl.glCompileShader(vertProgram);
	
		fragSource = parseSource(fShader);
		
		gl.glShaderSource (fragProgram, 1, fragSource, null, 0);
		gl.glCompileShader(fragProgram);
		
		int[] success = new int[1];
		
		gl.glGetShaderiv(vertProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1) System.err.println("Shader: " + vShader + ", cannot be compiled");
		
		gl.glGetShaderiv(fragProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1) System.err.println("Shader: " + fShader + ", cannot be compiled");
	
	    shaderID = gl.glCreateProgram();
	    
	    gl.glAttachShader(shaderID, vertProgram);
	    gl.glAttachShader(shaderID, fragProgram);
	    
	    gl.glLinkProgram(shaderID);

	    validate(gl);  
	}
	
	/** 
	 * This function is called when you want to activate the shader.
     * Once activated, it will be used to render anything that is drawn until the
     * <code>disable</code> function is called.
     */
    public int useShader(GL2 gl)
    {
        gl.glUseProgram(shaderID);
        return shaderID;
    }

    public void disable(GL2 gl) { gl.glUseProgram(0); }

	private void validate(GL2 gl)
	{
		gl.glValidateProgram(shaderID);
        
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(shaderID, GL2.GL_LINK_STATUS, intBuffer);
        
        if(intBuffer.get(0) != 1)
        {
            gl.glGetProgramiv(shaderID, GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            
            System.err.println("Shader linking error:");
            
            if(size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(shaderID, size, intBuffer, byteBuffer);
                
                for (byte b : byteBuffer.array()) System.err.print((char) b);
            }
            else System.err.println("Unknown");
        }
	}
	
	public String[] parseSource(String filename)
	{
		try
		{
			Scanner scanner = new Scanner(new File("shaders/" + filename));
			
			StringBuffer str = new StringBuffer();
			String line;
			
			while(scanner.hasNextLine())
			{
				line = scanner.nextLine() + "\n";
				str.append(line);
			}
			
			scanner.close();
			
			return new String[] {str.toString()};
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Shader: " + filename + ", cannot be parsed");
			return null;
		}
	}
}
