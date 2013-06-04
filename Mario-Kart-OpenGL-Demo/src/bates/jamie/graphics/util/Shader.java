package bates.jamie.graphics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.glsl.ShaderUtil;

public class Shader
{
	public static boolean enabled = true;
	
	public int shaderID;
	 
	public String[] vertSource;
	public String[] fragSource;
	
	public int vertProgram;
	public int fragProgram;
	
	private boolean valid = false;
	
	public Shader(GL2 gl, String vShader, String fShader)
	{	
		valid = attachPrograms(gl, vShader, fShader);
	}
	
	public boolean isValid() { return valid; }

	public boolean attachPrograms(GL2 gl, String vShader, String fShader)
	{
		vertProgram = gl.glCreateShader(GL2.GL_VERTEX_SHADER  );
		fragProgram = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		vertSource = parseSource(vShader + ".vs");
		if(vertSource == null) return false;
	
		gl.glShaderSource (vertProgram, 1, vertSource, null, 0);
		gl.glCompileShader(vertProgram);
	
		fragSource = parseSource(fShader + ".fs");
		if(fragSource == null) return false;
		
		gl.glShaderSource (fragProgram, 1, fragSource, null, 0);
		gl.glCompileShader(fragProgram);
		
		int[] success = new int[1];
		
		gl.glGetShaderiv(vertProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1)
		{
			System.err.println("Vertex Shader: " + vShader + ".vs, cannot be compiled");
			return false;
		}
		
		gl.glGetShaderiv(fragProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1)
		{
			System.err.println("Fragment Shader: " + fShader + ".fs, cannot be compiled");
			System.err.println(ShaderUtil.getShaderInfoLog(gl, fragProgram));
			return false;
		}
	
	    shaderID = gl.glCreateProgram();
	    
	    gl.glBindAttribLocation(shaderID, 1, "tangent");
	    
	    gl.glAttachShader(shaderID, vertProgram);
	    gl.glAttachShader(shaderID, fragProgram);
	    
	    gl.glLinkProgram(shaderID);
	    
	    System.out.println(ShaderUtil.getProgramInfoLog(gl, shaderID));

	    return validate(gl);  
	}
	
	/** 
	 * This function is called when you want to activate the shader.
     * Once activated, it will be used to render anything that is drawn until the
     * <code>disable</code> function is called.
     */
    public int enable(GL2 gl)
    {
        if(enabled) gl.glUseProgram(shaderID);
        return shaderID;
    }

    public static void disable(GL2 gl) { gl.glUseProgram(0); }

	private boolean validate(GL2 gl)
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
            
            return false;
        }
        
        return true;
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

	public void setSampler(GL2 gl, String sampler, int unit)
	{
		int samplerID = gl.glGetUniformLocation(shaderID, sampler);
		gl.glUniform1i(samplerID, unit);
	}
	
	public void setUniform(GL2 gl, String uniform, float value)
	{
		int uniformID = gl.glGetUniformLocation(shaderID, uniform);
		gl.glUniform1f(uniformID, value);
	}
}
