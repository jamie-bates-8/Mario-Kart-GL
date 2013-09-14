package bates.jamie.graphics.util;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;

public class OccludeQuery
{
	private int queryID = -1;
	
	public OccludeQuery() {}
	
	public boolean getResult(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode) return true;
		
		int[] results = new int[1];
		
		if(queryID != -1)
		{
			gl.glGetQueryObjectuiv(queryID, GL2.GL_QUERY_RESULT, results, 0);
			gl.glDeleteQueries(1, new int[] {queryID}, 0);
			
			return results[0] == 1;
		}
		
		return true;
	}
	
	public void begin(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode) return;
		
		int[] queries = new int[1];
		gl.glGenQueries(1, queries, 0);
		queryID = queries[0];
		
		gl.glBeginQuery(GL2.GL_ANY_SAMPLES_PASSED, queryID);
	}
	
	public void end(GL2 gl)
	{
		if(Scene.depthMode || Scene.shadowMode || Scene.environmentMode) return;
		gl.glEndQuery(GL2.GL_ANY_SAMPLES_PASSED);
	}
}
