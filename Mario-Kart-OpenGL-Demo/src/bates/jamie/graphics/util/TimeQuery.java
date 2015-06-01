package bates.jamie.graphics.util;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;

public class TimeQuery
{
	public static final int TERRAIN_ID = 0;
	public static final int FOLIAGE_ID = 1;
	public static final int VEHICLE_ID = 2;
	public static final int ITEM_ID = 3;
	public static final int PARTICLE_ID = 4;
	public static final int BOUND_ID = 5;
	public static final int HUD_ID = 6;
	public static final int NULL_ID = 7;
	
	private static int cacheSize = 240;
	private static int[][] cache = new int[cacheSize][8]; 
	
	private int queryID = -1;
	private int queryType;
	
	private   int counter  = 0;
	private float average  = 0;
	private   int previous = 0;
	
	public TimeQuery(int type) { queryType = type; }
	
	public static int[][] getCache() { return cache; }
	
	public static void resetCache()
	{
		for(int i = 0; i < 7; i++)
			cache[Scene.frameIndex][i] = 0;
	}
	
	public int getResult(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode || Scene.reflectMode) return 0;
		
		int[] results = new int[1];
		
		if(queryID != -1)
		{
			gl.glGetQueryObjectuiv(queryID, GL2.GL_QUERY_RESULT, results, 0);
			gl.glDeleteQueries(1, new int[] {queryID}, 0);
			
			if(queryType < 8) cache[Scene.frameIndex][queryType] += results[0];
			
			average  = (float) (average * counter + results[0]) / ++counter;
			previous = results[0];
			
			return results[0];
		}
		
		return -1;
	}
	
	public void begin(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode || Scene.reflectMode) return;
		
		int[] queries = new int[1];
		gl.glGenQueries(1, queries, 0);
		queryID = queries[0];
		
		gl.glBeginQuery(GL2.GL_TIME_ELAPSED, queryID);
	}
	
	public void end(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode || Scene.reflectMode) return;
		gl.glEndQuery(GL2.GL_TIME_ELAPSED);
	}
	
	public int getPrevious() { return previous; }
	
	public float getAverage() { return average; }
	
	public void reset()
	{
		counter = 0;
		average = 0;
	}

	
}
