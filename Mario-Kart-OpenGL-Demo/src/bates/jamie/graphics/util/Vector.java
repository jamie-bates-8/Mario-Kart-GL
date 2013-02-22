package bates.jamie.graphics.util;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

public class Vector
{
	public static final float EPSILON = 0.00001f;
	public static final int DEFAULT_PRECISION = 2;
	
	public float[] u;
	
	public Vector(float[] u) { this.u = u; }
	
	public Vector(float u0, float u1, float u2) { u = new float[] {u0, u1, u2}; }
	
	public void add(Vector v)
	{
		for(int i = 0; i < u.length; i++) u[i] += v.u[i];
	}
	
	public void add(float[] v)
	{
		for(int i = 0; i < u.length; i++) u[i] += v[i];
	}
	
	public static float dot(float[] u, float[] v)
	{
		float dot = 0;
		int n = u.length;
		
		for(int i = 0; i < n; i++) dot += (u[i] * v[i]);
		
		return dot;
	}
	
	public static float[] add(float[] u, float[] v)
	{
		int n = u.length;
		float[] w = new float[n];
		
		for(int i = 0; i < n; i++) w[i] = u[i] + v[i];
		
		return w;
	}
	
	public static float[] subtract(float[] u, float[] v)
	{
		int n = u.length;
		float[] w = new float[n];
		
		for(int i = 0; i < n; i++) w[i] = u[i] - v[i];
		
		return w;
	}
	
	public static float[] multiply(float[] u, float k)
	{
		int n = u.length;
		float[] w = new float[n];
		
		for(int i = 0; i < n; i++) w[i] = k * u[i];
		
		return w;
	}
	
	public static float[] multiply(float[] u, float[] v)
	{
		int n = u.length;
		float[] w = new float[n];
		
		for(int i = 0; i < n; i++) w[i] = u[i] * v[i];
		
		return w;
	}
	
	public static double getAngle(float[] u, float[] v)
	{
		double cos = dot(u, v) / (sqrt(dot(u, u)) * sqrt(dot(v, v)));
		
		     if(cos < -1) cos = -1;
		else if(cos >  1) cos =  1;
		
		return acos(cos);
	}
	
	public static float orient2D(float[] a, float[] b, float[] c)
	{
		return (a[0] - c[0]) * (b[1] - c[1]) - (a[1] - c[1]) * (b[0] - c[0]);
	}
	
	public static float[] normalize(float[] u)
	{
		int n = u.length;
		float[] w = new float[n];
		
		float magnitude = (float) sqrt(dot(u, u));
		
		for(int i = 0; i < n; i++)
			w[i] = u[i] / magnitude;
		
		return w;
	}
	
	public static float[] cross(float[] u, float[] v)
	{
		float[] w = new float[3];
		
		w[0] = u[1] * v[2] - u[2] * v[1];
		w[1] = u[2] * v[0] - u[0] * v[2];
		w[2] = u[0] * v[1] - u[1] * v[0];
		
		return w;
	}
	
	public static float[] normal(float[] p1, float[] p2, float[] p3)
	{
		float[] v1 = subtract(p2, p1);
		float[] v2 = subtract(p3, p1);
		float[] v3 = cross(v1, v2);
		
		return normalize(v3);
	}
	
	public static boolean equal(float[] u, float[] v)
	{
		for(int i = 0; i < u.length; i++)
		{
			if(abs(v[i] - u[i]) > EPSILON) return false;
		}
		
		return true;
	}
	
	public static String print(float[] u, int precision)
	{
		String vec = "(";
		
		for(int i = 0; i < u.length - 1; i++) vec += String.format("%." + precision + "f, ", u[i]);
		
		vec += String.format("%." + precision + "f)", u[u.length - 1]);
		
		return vec;
	}
	
	public static String print(float[] u)
	{
		return print(u, DEFAULT_PRECISION);
	}
}
