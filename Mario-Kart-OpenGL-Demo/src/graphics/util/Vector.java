package graphics.util;


import static java.lang.Math.*;

public class Vector
{
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
	
	public static float[] divide(float[] u, float k)
	{
		int n = u.length;
		float[] w = new float[n];
		
		for(int i = 0; i < n; i++) w[i] = u[i] / k;
		
		return w;
	}
	
	public static double getAngle(float[] u, float[] v)
	{
		return acos(dot(u, v) / (sqrt(dot(u, u)) * sqrt(dot(v, v))));
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
}
