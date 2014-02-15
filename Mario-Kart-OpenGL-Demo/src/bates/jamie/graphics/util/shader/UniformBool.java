package bates.jamie.graphics.util.shader;

public class UniformBool extends Uniform
{
	private boolean value = false;

	public boolean getValue() { return value; }

	public void setValue(boolean value) { this.value = value; }
}
