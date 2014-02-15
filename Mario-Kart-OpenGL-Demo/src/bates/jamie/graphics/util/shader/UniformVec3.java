package bates.jamie.graphics.util.shader;

import bates.jamie.graphics.util.Vec3;

public class UniformVec3 extends Uniform
{
	private Vec3 value = new Vec3();

	public Vec3 getValue() { return value; }

	public void setValue(Vec3 value) { this.value = value; }
}
