package bates.jamie.graphics.util;

import static java.lang.Math.*;

public class Matrix
{
	public static final float EPSILON = 0.0001f;
	
	public static final float[] IDENTITY_MATRIX_9 =
	{1, 0, 0, 0, 1, 0, 0, 0, 1};
	
	public static final float[][] IDENTITY_MATRIX_33 =
	{
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
	};
	
	public static final float[] IDENTITY_MATRIX_16 =
	{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
	
	public static final float[][] IDENTITY_MATRIX_44 =
	{
		{1, 0, 0, 0},
		{0, 1, 0, 0},
		{0, 0, 1, 0},
		{0, 0, 0, 1}
	};
	
	public static final float[][] BIAS_MATRIX =
		{
			{.5f,   0,   0, .5f},
			{  0, .5f,   0, .5f},
			{  0,   0, .5f, .5f},
			{  0,   0,   0,   1}
		};
	
	public static float[][] multiply(float[][] a, float[][] b)
	{
		int rows = a.length;
		int columns = b[0].length;
		int n = a[0].length;
		
		float[][] c = new float[columns][rows];
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				c[i][j] = 0;
				
				for(int k = 0; k < n; k++)
					c[i][j] += (a[i][k] * b[k][j]);
			}
		}
		
		return c;
	}
	
	public static void scale(float[][] m, float x, float y, float z)
	{
		m[0][0] += x; m[1][1] *= y; m[2][2] *= z; 
	}
	
	public static void scale(float[] m, float x, float y, float z)
	{
		m[0] *= x; m[5] *= y; m[10] *= z; 
	}
	
	public static void translate(float[][] m, float x, float y, float z)
	{
		m[0][3] += x; m[1][3] += y; m[2][3] += z;
	}
	
	public static void translate(float[] m, float x, float y, float z)
	{
		m[12] += x; m[13] += y; m[14] += z;
	}
	
	public static void transpose(float[] dst, float[] src)
	{                              
		for (int j = 0; j < 4; j++)          
		{                                    
		    for (int i = 0; i < 4; i++)      
		    {                                
		        dst[(j * 4) + i] = src[(i * 4) + j]; 
		    }                               
		}                                    
	}
			
	private static float cell(float[] src, int row, int col) { return src[(col << 2) + row]; }

	// Multiply two 4x4 matricies
	public static void multiply(float[] product, float[] a, float[] b)
	{
		for (int i = 0; i < 4; i++)
		{
			float ai0 = cell(a,i,0),  ai1 = cell(a,i,1),  ai2 = cell(a,i,2),  ai3 = cell(a,i,3);
			product[(0 << 2) + i] = ai0 * cell(b,0,0) + ai1 * cell(b,1,0) + ai2 * cell(b,2,0) + ai3 * cell(b,3,0);
			product[(1 << 2) + i] = ai0 * cell(b,0,1) + ai1 * cell(b,1,1) + ai2 * cell(b,2,1) + ai3 * cell(b,3,1);
			product[(2 << 2) + i] = ai0 * cell(b,0,2) + ai1 * cell(b,1,2) + ai2 * cell(b,2,2) + ai3 * cell(b,3,2);
			product[(3 << 2) + i] = ai0 * cell(b,0,3) + ai1 * cell(b,1,3) + ai2 * cell(b,2,3) + ai3 * cell(b,3,3);
		}
	}
	
	public static float[] multiply(float[] a, float[][] b)
	{
		int rows = a.length;
		int columns = b[0].length;
		
		float[] c = new float[rows];
		
		for(int i = 0; i < rows; i++)
		{
			c[i] = 0;
			
			for(int j = 0; j < columns; j++)
				c[i] += (a[j] * b[i][j]);
		}
		
		return c;
	}
	
	public static float[][] transpose(float[][] A)
	{
		int rows = A.length;
		int columns = A[0].length;
		
		float[][] AT = new float[columns][rows];
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = i; j < columns; j++)
			{
				AT[j][i] = A[i][j];
				AT[i][j] = A[j][i];
			}
		}
		
		return AT;
	}

	public static float[][] getRotationMatrix(float x, float y, float z)
	{
		x = (float) toRadians(x);
		y = (float) toRadians(y);
		z = (float) toRadians(z);
		
		float[][] Rx =
			{{    1    ,    0    ,    0    },
			 {    0    ,  cosf(x), -sinf(x)},
			 {    0    ,  sinf(x),  cosf(x)}};
		
		float[][] Ry =
			{{  cosf(y),    0    ,  sinf(y)},
			 {    0    ,    1    ,    0    },
			 { -sinf(y),    0    ,  cosf(y)}};
		
		float[][] Rz =
			{{  cosf(z), -sinf(z),    0    },
			 {  sinf(z),  cosf(z),    0    },
			 {    0    ,    0    ,    1    }};
		
		float[][] R = multiply(multiply(Ry, Rx), Rz);
		
		return transpose(R);
	}
	
	public static float[][] getRotationMatrix(float[] u, float theta)
	{
		float c = cosf(toRadians(theta));
		float _c = 1 - c;
		float s = sinf(toRadians(theta));
		
		float u0 = u[0] * u[0];
		float u1 = u[1] * u[1];
		float u2 = u[2] * u[2];
		
		return new float[][]
			{{                u0 * _c + c, u[0] * u[1] * _c + u[2] * s, u[0] * u[2] * _c - u[1] * s},
			 {u[0] * u[1] * _c - u[2] * s,                 u1 * _c + c, u[1] * u[2] * _c + u[0] * s},
			 {u[0] * u[2] * _c + u[1] * s, u[1] * u[2] * _c - u[0] * s,                 u2 * _c + c}};
	}
	
	public static float getDeterminant(float[][] A)
	{
		return A[0][0] * A[1][1] * A[2][2] +
			   A[0][1] * A[1][2] * A[2][0] +
			   A[0][2] * A[1][0] * A[2][1] -
			   A[0][2] * A[1][1] * A[2][0] -
			   A[0][1] * A[1][0] * A[2][2] -
			   A[0][0] * A[1][2] * A[2][1];
	}
	
	public static float[] getEulerAngles(float[][] R)
	{
		float[] a = new float[3];
 		
		if(R[2][1] < 1)
		{
			if(R[2][1] > -1)
			{
				a[0] = (float) toDegrees(asin(-R[2][1]));
				a[1] = (float) toDegrees(atan2(R[2][0], R[2][2]));
				a[2] = (float) toDegrees(atan2(R[0][1], R[1][1]));
			}
			else
			{
				a[0] = 90.0f;
				a[1] = (float) -toDegrees(atan2(-R[1][0], R[0][0]));
				a[2] = 0.0f;
			}
		}
		else
		{
			a[0] = -90.0f;
			a[1] = (float) toDegrees(atan2(-R[1][0], R[0][0]));
			a[2] = 0.0f;
		}
		
		return a;
	}
	
	public static float[] getRotationMatrix(float[][] M)
	{		
		float[][] _M =
			{{M[0][0], M[0][1], M[0][2],   0   },
			 {M[1][0], M[1][1], M[1][2],   0   },
		     {M[2][0], M[2][1], M[2][2],   0   },
			 {   0   ,    0   ,    0   ,   1   }};
		
		float[] R = new float[16];
		
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				R[(i * 4) + j] = _M[i][j];
		
		return R;
	}
	
	/**
	 * This method converts a 4x4 row-major Matrix into a 16 value column-major
	 * Vector that matches OpenGL's internal format 
	 */
	public static float[] toVector(float[][] m)
	{
		float[] R = new float[16];
		
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				R[(j * 4) + i] = m[i][j];
		
		return R;
	}
	
	public static void main(String[] args)
	{
		float[][] M =
		{
			{ 0,  1,  2,  3},
			{ 4,  5,  6,  7},
			{ 8,  9, 10, 11},
			{12, 13, 14, 15}
		};
		
		System.out.println(print(M, 0));
		
		float[] m = toVector(M);
		float[] t = toVector(transpose(M));
		
		M = toMatrix(m);
		float[][] T = toMatrix(t);
		
		System.out.println(Vector.print(m, 0));
		System.out.println(Vector.print(t, 0));
		System.out.println();
		System.out.println(print(M, 0));
		System.out.println(print(T, 0));
	}
	
	/**
	 * This method converts a 16 value column-major Vector that matches OpenGL's
	 * internal format into a 4x4 row-major Matrix.
	 */
	public static float[][] toMatrix(float[] m)
	{
		float[][] R = new float[4][4];
		
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				R[i][j] = m[(j * 4) + i];
		
		return R;
	}
	
	public static boolean isRotationMatrix(float[][] A)
	{
		float det = getDeterminant(A);
		return (1 - EPSILON < det && det < 1 + EPSILON);
	}
	
	public static String print(float[][] M, int precision)
	{
		String mat = "";
		
		for(int i = 0; i < M.length; i++)
		{
			mat += "[";
			
			for(int j = 0; j < M[0].length - 1; j++)
				mat += String.format("%." + precision + "f, ", M[i][j]);
			    mat += String.format("%." + precision + "f] ", M[i][M[0].length - 1]);
			    mat += "\n";
		}
		
		return mat;
	}
	
	public static float sinf(double a) { return (float) sin(a); }
	
	public static float cosf(double a) { return (float) cos(a); }
	
	public static float tanf(double a) { return (float) tan(a); }
}
