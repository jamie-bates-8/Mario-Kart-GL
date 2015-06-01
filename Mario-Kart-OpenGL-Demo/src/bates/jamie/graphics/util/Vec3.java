package bates.jamie.graphics.util;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;

import java.util.Collection;
import java.util.Random;

public class Vec3
{	
	public static final Vec3 POSITIVE_X_AXIS = new Vec3(+1,  0,  0);
	public static final Vec3 NEGATIVE_X_AXIS = new Vec3(-1,  0,  0);
	public static final Vec3 POSITIVE_Y_AXIS = new Vec3( 0, +1,  0);
	public static final Vec3 NEGATIVE_Y_AXIS = new Vec3( 0, -1,  0);
	public static final Vec3 POSITIVE_Z_AXIS = new Vec3( 0,  0, +1);
	public static final Vec3 NEGATIVE_Z_AXIS = new Vec3( 0,  0, -1);
	
	public static final float EPSILON = 1E-5f;
	public static final int   DEFAULT_PRECISION = 2;
	
	public float x, y, z;
	
	public Vec3() {}
	
	public Vec3(float f)
	{
		this.x = f;
		this.y = f;
		this.z = f;
	}
	
	public Vec3(double f)
	{
		float _f = (float) f;
		
		this.x = _f;
		this.y = _f;
		this.z = _f;
	}
	
	public Vec3(Vec3 v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vec3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3 normalizeScale() { return this.multiply(1.0f / min()); }
	
	public Vec3(double x, double y, double z)
	{
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
	
	public Vec3(float[] v)
	{
		x = v[0];
		y = v[1];
		z = v[2];
	}
	
	public float get(int i)
	{
		switch(i)
		{
			case 0: return x;
			case 1: return y;
			case 2: return z;
		}
		
		return 0;
	}
	
	public float min()
	{
		float minimum = x < y ? x : y;
		if(z < minimum) minimum = z;
		
		return minimum;
	}
	
	public float max()
	{
		float maximum = x > y ? x : y;
		if(z > maximum) maximum = z;
		
		return maximum;
	}
	
	public float[] toArray()
	{
		return new float[] {x, y, z};
	}
	
	public static float[][] toArray2D(Vec3[] points)
	{
		float[][] array = new float[points.length][3];
		
		for(int i = 0; i < points.length; i++)
			array[i] = points[i].toArray();
		
		return array;
	}
	
	public static float[] toArray1D(Vec3[] points)
	{
		float[] array = new float[3 * points.length];
		
		for(int i = 0; i < points.length; i++)
		{
			array[i * 3 + 0] = points[i].x;
			array[i * 3 + 1] = points[i].y;
			array[i * 3 + 2] = points[i].z;
		}
		
		return array;
	}
	
	public static Vec3[] toArrayVec3(float[] points)
	{
		Vec3[] array = new Vec3[points.length / 3];
		
		for(int i = 0; i < array.length; i++)
			array[i] = new Vec3(points[i * 3], points[i * 3 + 1], points[i * 3 + 2]);
		
		return array;
	}
	
	public static Vec3[] toArrayVec3(float[][] points)
	{
		Vec3[] array = new Vec3[points.length];
		
		for(int i = 0; i < array.length; i++)
			array[i] = new Vec3(points[i]);
			
		return array;
	}
	
	public static Vec3 getRandomVector(float k)
	{
		Random g = new Random();
		
		float x = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();
		float y = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();
		float z = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();

		Vec3 v = new Vec3(x, y, z);
		
		return v.multiply(k);
	}
	
	public static Vec3 getRandomVector(Vec3 dir, float angle, float scale)
	{
		Vec3 v = getRandomVector();
			
		if (v.dot(dir) < 0.0)   v = v.negate();
		if (v.dot(dir) < angle) v = v.add(dir).normalize();
		
		return v.multiply(scale);
	}
	
	public static Vec3 getRandomVector() { return getRandomVector(1); }
	
	public Vec3 add(Vec3 v) { return new Vec3(x + v.x, y + v.y, z + v.z); }
	
	public Vec3 subtract(Vec3 v) { return new Vec3(x - v.x, y - v.y, z - v.z); }
	
	public Vec3 multiply(Vec3 v) { return new Vec3(x * v.x, y * v.y, z * v.z); }
	
	public Vec3 multiply(float  k) { return new Vec3(x * k, y * k, z * k); }
	
	public Vec3 multiply(double k) { return new Vec3(x * k, y * k, z * k); }
	
	public Vec3 divide(Vec3 v) { return new Vec3(x / v.x, y / v.y, z / v.z); }
	
	public Vec3 divide(float k) { return new Vec3(x / k, y / k, z / k); }
	
	public Vec3 absolute() { return new Vec3(abs(x), abs(y), abs(z)); }
	
	public Vec3 fract()
	{
		long _x = (long) x;
		long _y = (long) y;
		long _z = (long) z;
		
		return new Vec3(x - _x, y - _y, z - _z);
	}
	
	public Vec3 clamp(float lower, float upper)
	{
		float _x =  Math.min(Math.max(x, lower), upper);
		float _y =  Math.min(Math.max(y, lower), upper);
		float _z =  Math.min(Math.max(z, lower), upper);
		
		return new Vec3(_x, _y, _z);
	}
	
	public Vec3 multiply(RotationMatrix matrix)
	{
		Vec3 v = new Vec3();
		
		Vec3 xAxis = matrix.xAxis;
		Vec3 yAxis = matrix.yAxis;
		Vec3 zAxis = matrix.zAxis;
		
		v.x = xAxis.x * x + xAxis.y * y + xAxis.z * z;
		v.y = yAxis.x * x + yAxis.y * y + yAxis.z * z;
		v.z = zAxis.x * x + zAxis.y * y + zAxis.z * z;
		
		return v;
	}
	
	public float dot(Vec3 v)
	{
		float dot = 0;
		
		dot += (x * v.x);
		dot += (y * v.y);
		dot += (z * v.z);
		
		return dot;
	}
	
	public float dot()
	{
		float dot = 0;
		
		dot += (x * x);
		dot += (y * y);
		dot += (z * z);
		
		return dot;
	}
	
	public float magnitude()
	{
		return (float) sqrt(dot());
	}
	
	public float length(Vec3 v)
	{
		v = v.subtract(this);
		return v.magnitude();
	}
	
	public static Vec3 mix(Vec3 u, Vec3 v, float factor)
	{
		float  f = factor < 0 ? 0 : (factor > 1 ? 1 : factor);
		float _f = 1 - f;
		
		u = u.multiply(_f);
		v = v.multiply( f);
		
		return u.add(v);
		
	}
	
	public double getAngle(Vec3 v)
	{
		double cos = dot(v) / (magnitude() * v.magnitude());
		
		     if(cos < -1) cos = -1;
		else if(cos >  1) cos =  1;
		
		return acos(cos);
	}
	
	public double getCompassAngle()
	{
		return Math.toDegrees(atan2(z, x)) + 90.0;
	}

	
	// TODO
	public static float orient2D(float[] a, float[] b, float[] c)
	{
		return (a[0] - c[0]) * (b[1] - c[1]) - (a[1] - c[1]) * (b[0] - c[0]);
	}
	
	public Vec3 normalize()
	{
		return this.multiply(1.0f / magnitude());
	}
	
	public Vec3 negate()
	{
		return new Vec3(-x, -y, -z);
	}
	
	public Vec3 cross(Vec3 v)
	{
		Vec3 w = new Vec3();
		
		w.x = y * v.z - z * v.y;
		w.y = z * v.x - x * v.z;
		w.z = x * v.y - y * v.x;
		
		return w;
	}
	
	public static Vec3 normal(Vec3 p1, Vec3 p2, Vec3 p3)
	{
		Vec3 v1 = p2.subtract(p1);
		Vec3 v2 = p3.subtract(p1);
		Vec3 v3 = v1.cross(v2);
		
		return v3.normalize();
	}
	
	public static Vec3 average(Vec3[] vectors)
	{
		Vec3 v = new Vec3();
		
		for(int i = 0; i < vectors.length; i++)
		{
			v.x += vectors[i].x;
			v.y += vectors[i].y;
			v.z += vectors[i].z;
		}
		
		return v.multiply(1.0f / vectors.length);
	}
	
	public static Vec3 average(Collection<Vec3> vectors)
	{
		Vec3 n = new Vec3();
		
		for(Vec3 normal : vectors)
		{
			n.x += normal.x;
			n.y += normal.y;
			n.z += normal.z;
		}
		
		return n.multiply(1.0f / vectors.size());
	}
	
	public boolean equals(Vec3 v)
	{
		if(abs(v.x - x) > EPSILON) return false;
		if(abs(v.y - y) > EPSILON) return false;
		if(abs(v.z - z) > EPSILON) return false;
		
		return true;
	}
	
	public boolean isZeroVector()
	{
		if(abs(x) > EPSILON) return false;
		if(abs(y) > EPSILON) return false;
		if(abs(z) > EPSILON) return false;
		
		return true;
	}
	
	public String toString(int precision)
	{
		String vec = "(";
		
		vec += String.format("%+." + precision + "f, ", x);
		vec += String.format("%+." + precision + "f, ", y);
		vec += String.format("%+." + precision + "f) ", z);
		
		return vec;
	}
	
	@Override
	public String toString()
	{
		return toString(DEFAULT_PRECISION);
	}
}
