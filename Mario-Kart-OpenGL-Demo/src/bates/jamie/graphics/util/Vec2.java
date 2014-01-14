package bates.jamie.graphics.util;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

import java.util.Random;

public class Vec2
{		
	public static final float EPSILON = 1E-5f;
	public static final int   DEFAULT_PRECISION = 2;
	
	public float x, y;
	
	public Vec2() {}
	
	public Vec2(float f)
	{
		this.x = f;
		this.y = f;
	}
	
	public Vec2(double f)
	{
		float _f = (float) f;
		
		this.x = _f;
		this.y = _f;
	}
	
	public Vec2(Vec2 v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vec2(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vec2(double x, double y)
	{
		this.x = (float) x;
		this.y = (float) y;
	}
	
	public Vec2(float[] v)
	{
		x = v[0];
		y = v[1];
	}
	
	public float get(int i)
	{
		switch(i)
		{
			case 0: return x;
			case 1: return y;
		}
		
		return 0;
	}
	
	public float[] toArray()
	{
		return new float[] {x, y};
	}
	
	public static Vec2 getRandomVector(float k)
	{
		Random g = new Random();
		
		float x = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();
		float y = g.nextBoolean() ? g.nextFloat() : -g.nextFloat();

		Vec2 v = new Vec2(x, y);
		
		return v.multiply(k);
	}
	
	public static Vec2 getRandomVector() { return getRandomVector(1); }
	
	public Vec2 add(Vec2 v)
	{
		return new Vec2(x + v.x, y + v.y);
	}
	
	public Vec2 subtract(Vec2 v)
	{
		return new Vec2(x - v.x, y - v.y);
	}
	
	public Vec2 multiply(Vec2 v)
	{
		return new Vec2(x * v.x, y * v.y);
	}
	
	public Vec2 multiply(float k)
	{
		return new Vec2(x * k, y * k);
	}
	
	public float dot(Vec2 v)
	{
		float dot = 0;
		
		dot += (x * v.x);
		dot += (y * v.y);
		
		return dot;
	}
	
	public float dot()
	{
		float dot = 0;
		
		dot += (x * x);
		dot += (y * y);
		
		return dot;
	}
	
	public float magnitude()
	{
		return (float) sqrt(dot());
	}
	
	public float length(Vec2 v)
	{
		v = v.subtract(this);
		return v.magnitude();
	}
	
	public Vec2 mix(Vec2 u, Vec2 v, float factor)
	{
		float  f = factor < 0 ? 0 : (factor > 1 ? 1 : factor);
		float _f = 1 - f;
		
		u = u.multiply( f);
		v = v.multiply(_f);
		
		return u.add(v);
		
	}
	
	public double getAngle(Vec2 v)
	{
		double cos = dot(v) / (magnitude() * v.magnitude());
		
		     if(cos < -1) cos = -1;
		else if(cos >  1) cos =  1;
		
		return acos(cos);
	}
	
	// TODO
	public static float orient2D(float[] a, float[] b, float[] c)
	{
		return (a[0] - c[0]) * (b[1] - c[1]) - (a[1] - c[1]) * (b[0] - c[0]);
	}
	
	public Vec2 normalize()
	{
		return this.multiply(1.0f / magnitude());
	}
	
	public Vec2 negate()
	{
		return new Vec2(-x, -y);
	}
	
	public boolean equals(Vec2 v)
	{
		if(abs(v.x - x) > EPSILON) return false;
		if(abs(v.y - y) > EPSILON) return false;
		
		return true;
	}
	
	public boolean isZeroVector()
	{
		if(abs(x) > EPSILON) return false;
		if(abs(y) > EPSILON) return false;
		
		return true;
	}
	
	public String toString(int precision)
	{
		String vec = "(";
		
		vec += String.format("%." + precision + "f, ", x);
		vec += String.format("%." + precision + "f) ", y);
		
		return vec;
	}
	
	@Override
	public String toString()
	{
		return toString(DEFAULT_PRECISION);
	}
}
