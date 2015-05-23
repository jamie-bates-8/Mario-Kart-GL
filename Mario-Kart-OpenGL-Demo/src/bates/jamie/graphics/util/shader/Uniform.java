package bates.jamie.graphics.util.shader;

import bates.jamie.graphics.util.Vec3;


public abstract class Uniform
{
	private String identifier;

	public String getIdentifier() { return identifier; }

	public void setIdentifier(String identifier) { this.identifier = identifier; }
	
	public static Uniform getUniform(String type, String identifier)
	{
		Uniform uniform;
		
		     if(type.equals("bool"   )) uniform = new UniformBool();
		else if(type.equals("vec3"   )) uniform = new UniformVec3();
		else if(type.equals("float"  )) uniform = new UniformFloat();
		else if(type.equals("int"    )) uniform = new UniformInt();
		else if(type.equals("sampler")) uniform = new UniformSampler();   
		     
		else uniform = new UniformFloat(); 
		     
		uniform.setIdentifier(identifier);
		
		return uniform;
	}
	
	public static Uniform getUniform(String identifier, float value)
	{
		UniformFloat uniform = new UniformFloat();
		
		uniform.setIdentifier(identifier);
		uniform.setValue(value);
		
		return uniform;
	}
	
	public static Uniform getUniform(String identifier, Vec3 value)
	{
		UniformVec3 uniform = new UniformVec3();
		
		uniform.setIdentifier(identifier);
		uniform.setValue(value);
		
		return uniform;
	}
	
	public static Uniform getSampler(String identifier, int sampler)
	{
		UniformSampler uniform = new UniformSampler();
		
		uniform.setIdentifier(identifier);
		uniform.setValue(sampler);
		
		return uniform;
	}
	
	public String toString() { return identifier; }
}
