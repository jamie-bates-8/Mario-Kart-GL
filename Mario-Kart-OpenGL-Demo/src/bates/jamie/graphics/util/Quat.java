package bates.jamie.graphics.util;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class Quat
{
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public float w = 0;
	
	public static final float EPSILON = 1E-5f;
	public static final int   DEFAULT_PRECISION = 2;
	
	public Quat() {}
	
	public Quat(Vec3 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public Quat(Vec3 v, float scalar)
	{
		x = v.x;
		y = v.y;
		z = v.z;
		
		w = scalar;
	}
	
	public Quat(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Quat(double x, double y, double z)
	{
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
	
	public Quat(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quat(double x, double y, double z, double w)
	{
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
		this.w = (float) w;
	}
	
	public Quat(RotationMatrix mat)
	{
		float tr = mat.xAxis.x + mat.yAxis.y + mat.zAxis.z;

		if (tr > 0)
		{ 
			float s = (float) (sqrt(tr + 1.0) * 2);  // S = 4 * w
			
			w = 0.25f * s;
			x = (mat.zAxis.y - mat.yAxis.z) / s;
			y = (mat.xAxis.z - mat.zAxis.x) / s; 
			z = (mat.yAxis.x - mat.xAxis.y) / s; 
		}
		else if ((mat.xAxis.x > mat.yAxis.y) && (mat.xAxis.x > mat.zAxis.z))
		{ 
			float s = (float) (sqrt(1.0 + mat.xAxis.x - mat.yAxis.y - mat.zAxis.z) * 2);
			
			w = (mat.zAxis.y - mat.yAxis.z) / s;
			x = 0.25f * s;
			y = (mat.xAxis.y + mat.yAxis.x) / s; 
			z = (mat.xAxis.z + mat.zAxis.x) / s; 
		}
		else if (mat.yAxis.y > mat.zAxis.z)
		{ 
			float s = (float) (sqrt(1.0 + mat.yAxis.y - mat.xAxis.x - mat.zAxis.z) * 2);
			
			w = (mat.xAxis.z - mat.zAxis.x) / s;
			x = (mat.xAxis.y + mat.yAxis.x) / s; 
			y = 0.25f * s;
			z = (mat.yAxis.z + mat.zAxis.y) / s; 
		}
		else
		{ 
			float s = (float) (sqrt(1.0 + mat.zAxis.z - mat.xAxis.x - mat.yAxis.y) * 2);
			
			w = (mat.yAxis.x - mat.xAxis.y) / s;
			x = (mat.xAxis.z + mat.zAxis.x) / s;
			y = (mat.yAxis.z + mat.zAxis.y) / s;
			z = 0.25f * s;
		}
	}
	
	public Vec3 getVector() { return new Vec3(x, y, z); }
	
	public float getScalar() { return w; }
	
	public static Quat constructFromAxis(Vec3 axis, float angle)
	{
		float theta = (float) (toRadians(angle) / 2.0f);
		axis = axis.normalize();

		return new Quat(axis.multiply(sinf(theta)), cosf(theta));
	}
	
	public float dot()
	{
		float dot = 0;
		
		dot += (x * x);
		dot += (y * y);
		dot += (z * z);
		dot += (w * w);
		
		return dot;
	}
	
	public float dot(Quat q)
	{
		float dot = 0;
		
		dot += (x * q.x);
		dot += (y * q.y);
		dot += (z * q.z);
		dot += (w * q.w);
		
		return dot;
	}
	
	public Quat normalize()
	{
		Quat result = new Quat();
		
		float dot = dot();
		// detect badness
		assert(dot > 0.1f);
		
		float inv = (float) (1.0f / sqrt(dot));
		
		result.x = x * inv;
		result.y = y * inv;
		result.z = z * inv;
		result.w = w * inv;
		
		return result;
	}
	
	
	public static void main(String[] args)
	{
		float xr = 40;
		float yr = 50;
		float zr = 60;
		
		Quat qx = constructFromAxis(new Vec3(+1,  0,  0), xr);
		Quat qy = constructFromAxis(new Vec3( 0, +1,  0), yr);
		Quat qz = constructFromAxis(new Vec3( 0,  0, +1), zr);
		
		Quat q = new Quat(new Vec3(), 1);
		q = q.multiply(qz);
		q = q.multiply(qx);
		q = q.multiply(qy);
		
		RotationMatrix result = new RotationMatrix(xr, yr, zr);
		System.out.println(result.toString() + "\n");
		
		System.out.println(new RotationMatrix(q) + "\n");	
		
		RotationMatrix mat1 = new RotationMatrix(40, 50, 60);
		
		System.out.println(mat1 + "\n");
		
		Quat quat = new Quat(mat1);
		
		RotationMatrix mat2 = new RotationMatrix(quat);
		
		System.out.println(mat2);
	}
	
	public Quat multiply(Quat q)
	{
	     Quat result = new Quat();
	     
	     result.w = w * q.w - x * q.x - y * q.y - z * q.z;
	     result.x = w * q.x + x * q.w + y * q.z - z * q.y;
	     result.y = w * q.y + y * q.w + z * q.x - x * q.z;
	     result.z = w * q.z + z * q.w + x * q.y - y * q.x;
	     
	     return result;
	}
	
	public Quat nlerp(Quat q, float blend)
	{
		Quat result = new Quat();
		
		float dot = dot(q);
		float blend_ = 1.0f - blend;
		
		if(dot < 0.0f)
		{
			Quat temp = new Quat(-q.x, -q.y, -q.z, -q.w);
			
			result.w = blend_ * w + blend * temp.w;
			result.x = blend_ * x + blend * temp.x;
			result.y = blend_ * y + blend * temp.y;
			result.z = blend_ * z + blend * temp.z;
		}
		else
		{
			result.w = blend_ * w + blend * q.w;
			result.x = blend_ * x + blend * q.x;
			result.y = blend_ * y + blend * q.y;
			result.z = blend_ * z + blend * q.z;
		}
		
		return result.normalize();
	}
	
	public static float sinf(double a) { return (float) sin(a); }
	
	public static float cosf(double a) { return (float) cos(a); }
	
	public static float tanf(double a) { return (float) tan(a); }
	
	public String toString(int precision)
	{
		String vec = "(";
		
		vec += String.format("%+." + precision + "f, ", x);
		vec += String.format("%+." + precision + "f, ", y);
		vec += String.format("%+." + precision + "f, ", z);
		vec += String.format("%+." + precision + "f) ", w);
		
		return vec;
	}
	
	@Override
	public String toString()
	{
		return toString(DEFAULT_PRECISION);
	}
}
