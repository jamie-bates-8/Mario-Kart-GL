package bates.jamie.graphics.util;
import static java.lang.Math.*;

public class Quaternion
{
	public float x;
	public float y;
	public float z;
	public float w;
	
	public Quaternion(float pitch, float yaw, float roll)
	{
		double p = toRadians(pitch) / 2.0;
		double y = toRadians(yaw)   / 2.0;
		double r = toRadians(roll)  / 2.0;
	 
		double sinp = sin(p);
		double siny = sin(y);
		double sinr = sin(r);
		double cosp = cos(p);
		double cosy = cos(y);
		double cosr = cos(r);
	 
		this.x = (float) (sinr * cosp * cosy - cosr * sinp * siny);
		this.y = (float) (cosr * sinp * cosy + sinr * cosp * siny);
		this.z = (float) (cosr * cosp * siny - sinr * sinp * cosy);
		this.w = (float) (cosr * cosp * cosy + sinr * sinp * siny);
	}
	
	public float[] getMatrix()
	{
		float x2 = x * x;
		float y2 = y * y;
		float z2 = z * z;
		float xy = x * y;
		float xz = x * z;
		float yz = y * z;
		float wx = w * x;
		float wy = w * y;
		float wz = w * z;
	 
		float[] matrix =
			{1.0f - 2.0f * (y2 + z2), 2.0f * (xy - wz), 2.0f * (xz + wy), 0.0f,
			 2.0f * (xy + wz), 1.0f - 2.0f * (x2 + z2), 2.0f * (yz - wx), 0.0f,
			 2.0f * (xz - wy), 2.0f * (yz + wx), 1.0f - 2.0f * (x2 + y2), 0.0f,
			 0.0f, 0.0f, 0.0f, 1.0f};
		
		return matrix;
	}
}
