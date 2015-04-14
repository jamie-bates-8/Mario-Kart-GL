package bates.jamie.graphics.collision;

import static org.junit.Assert.*;

import org.junit.Test;

import bates.jamie.graphics.util.Quat;
import bates.jamie.graphics.util.Vec3;

public class OBBTest
{
	@Test
	public void testContains()
	{
		OBB obb = new OBB();
		
		Vec3 p = new Vec3(0); assertTrue(obb.containsPoint(p));
		
		float offset = 1E-4f;
		
		float e = 0.5f;
		float c = e + offset;
		float d = e - offset;
		
		p = new Vec3(e); assertTrue (obb.containsPoint(p));
		p = new Vec3(c); assertFalse(obb.containsPoint(p));
		p = new Vec3(d); assertTrue (obb.containsPoint(p));
		
		p = new Vec3(-e); assertTrue (obb.containsPoint(p));
		p = new Vec3(-c); assertFalse(obb.containsPoint(p));
		p = new Vec3(-d); assertTrue (obb.containsPoint(p));
		
		p = new Vec3(e, 0, 0); assertTrue(obb.containsPoint(p));
		p = new Vec3(0, e, 0); assertTrue(obb.containsPoint(p));
		p = new Vec3(0, 0, e); assertTrue(obb.containsPoint(p));
		
		p = new Vec3(c, 0, 0); assertFalse(obb.containsPoint(p));
		p = new Vec3(0, c, 0); assertFalse(obb.containsPoint(p));
		p = new Vec3(0, 0, c); assertFalse(obb.containsPoint(p));
		
		p = new Vec3(d, 0, 0); assertTrue(obb.containsPoint(p));
		p = new Vec3(0, d, 0); assertTrue(obb.containsPoint(p));
		p = new Vec3(0, 0, d); assertTrue(obb.containsPoint(p));
	}
	
	@Test
	public void testSegment()
	{
		Vec3 p0 = new Vec3(+15.21, +1.80, +189.55);
		Vec3 p1 = new Vec3(+15.77, +1.80, +209.55);
		
		OBB obb = new OBB(new Vec3(+0.00, +45.00, +206.25), new Quat(+0.00, +0.00, +0.00, +1.00), new Vec3(+202.50, +45.00, +3.75).multiply(2));
		
		assertTrue(obb.testSegmentOBB(p0, p1));
		assertTrue(obb.testSegment(p0, p1));
		assertTrue(obb.containsPoint(p1));
		
		p0 = new Vec3(+40.34, +31.85, +34.72);
		p1 = new Vec3(+30.34, +31.85, +52.04);
		
		obb = new OBB(new Vec3(+70.20, +28.80, +56.25), new Quat(+0.00, +0.00, -0.29, +0.96), new Vec3(+33.75, +15.00, +11.25).multiply(2));
	
		assertFalse(obb.testSegmentOBB(p0, p1));
		assertFalse(obb.testSegment(p0, p1));
		assertFalse(obb.containsPoint(p1));
		
		p0 = new Vec3(+128.35, +1.80, +164.61);
		p1 = new Vec3(+114.74, +1.80, +149.96);
		
		obb = new OBB(new Vec3(+109.80, -1.22, +146.25), new Quat(+0.00, +0.00, +0.29, +0.96), new Vec3(+33.75, +15.00, +11.25).multiply(2));
	
		assertTrue(obb.testSegmentOBB(p0, p1));
		assertTrue(obb.testSegment(p0, p1));
		assertTrue(obb.containsPoint(p1));
		
		p0 = new Vec3(+82.06, +1.80, +168.20);
		p1 = new Vec3(+82.75, +1.80, +148.21);
		
		obb = new OBB(new Vec3(+109.80, -1.22, +146.25), new Quat(+0.00, +0.00, +0.29, +0.96), new Vec3(+33.75, +15.00, +11.25).multiply(2));
	
		assertTrue(obb.testSegmentOBB(p0, p1));
		assertTrue(obb.testSegment(p0, p1));
		assertTrue(obb.containsPoint(p1));
	}
	
	@Test
	public void testOBB()
	{
		OBB a = new OBB(new Vec3(), new Vec3(), new Vec3(1));
		
		// both OBBs are in the same location
		OBB b = new OBB(new Vec3(0, 0, 0), new Vec3(), new Vec3(1)); 
		assertTrue(a.testOBB(b));
		
		float offset = 1E-4f; // must be less than the epsilon used by the OBB intersection test
		
		float c = 1.0f + offset;
		float d = 1.0f - offset;
		
		/***********************************************************************************/
		
		// B is translated along the positive x-axis but is still intersecting A
		b.setPosition(d, 0, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
		
		// B is translated along the positive x-axis but is still touching the perimeter of A
		b.setPosition(1, 0, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
		
		// B is translated along the positive x-axis so that it is no longer touching A
		b.setPosition(c, 0, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated along the negative x-axis but is still intersecting A
		b.setPosition(-d, 0, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
		
		// B is translated along the negative x-axis but is still touching the perimeter of A
		b.setPosition(-1, 0, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
				
		// B is translated along the negative x-axis so that it is no longer touching A
		b.setPosition(-c, 0, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated along the positive y-axis but is still intersecting A
		b.setPosition(0, d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
				
		// B is translated along the positive y-axis but is still touching the perimeter of A
		b.setPosition(0, 1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
				
		// B is translated along the positive y-axis so that it is no longer touching A
		b.setPosition(0, c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated along the negative y-axis but is still intersecting A
		b.setPosition(0, -d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
		
		// B is translated along the negative y-axis but is still touching the perimeter of A
		b.setPosition(0, -1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
						
		// B is translated along the negative y-axis so that it is no longer touching A
		b.setPosition(0, -c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated along the positive z-axis but is still intersecting A
		b.setPosition(0, 0, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
						
		// B is translated along the positive z-axis but is still touching the perimeter of A
		b.setPosition(0, 0, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
						
		// B is translated along the positive z-axis so that it is no longer touching A
		b.setPosition(0, 0, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated along the negative z-axis but is still intersecting A
		b.setPosition(0, 0, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
				
		// B is translated along the negative z-axis but is still touching the perimeter of A
		b.setPosition(0, 0, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated along the negative z-axis so that it is no longer touching A
		b.setPosition(0, 0, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated towards the right-top-back corner of A but is still intersecting A
		b.setPosition(d, d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
						
		// B is translated towards the right-top-back corner of A but is still touching the corner of A
		b.setPosition(1, 1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
										
		// B is translated beyond the right-top-back corner of A
		b.setPosition(c, c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the right-top-front corner of A but is still intersecting A
		b.setPosition(d, d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the right-top-front corner of A but is still touching the corner of A
		b.setPosition(1, 1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the right-top-front corner of A
		b.setPosition(c, c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the left-top-back corner of A but is still intersecting A
		b.setPosition(-d, d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the left-top-back corner of A but is still touching the corner of A
		b.setPosition(-1, 1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the left-top-back corner of A
		b.setPosition(-c, c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the left-top-front corner of A but is still intersecting A
		b.setPosition(-d, d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the left-top-front corner of A but is still touching the corner of A
		b.setPosition(-1, 1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the left-top-front corner of A
		b.setPosition(-c, c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated towards the right-bottom-back corner of A but is still intersecting A
		b.setPosition(d, -d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
						
		// B is translated towards the right-bottom-back corner of A but is still touching the corner of A
		b.setPosition(1, -1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
										
		// B is translated beyond the right-bottom-back corner of A
		b.setPosition(c, -c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the right-bottom-front corner of A but is still intersecting A
		b.setPosition(d, -d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the right-bottom-front corner of A but is still touching the corner of A
		b.setPosition(1, -1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the right-bottom-front corner of A
		b.setPosition(c, -c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the left-bottom-back corner of A but is still intersecting A
		b.setPosition(-d, -d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the left-bottom-back corner of A but is still touching the corner of A
		b.setPosition(-1, -1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the left-bottom-back corner of A
		b.setPosition(-c, -c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the left-bottom-front corner of A but is still intersecting A
		b.setPosition(-d, -d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
								
		// B is translated towards the left-bottom-front corner of A but is still touching the corner of A
		b.setPosition(-1, -1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated beyond the left-bottom-front corner of A
		b.setPosition(-c, -c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated towards the top-left edge of A but is still intersecting A
		b.setPosition(-d, d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
										
		// B is translated towards the top-left edge of A but is still touching the edge of A
		b.setPosition(-1, 1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
														
		// B is translated beyond the top-left edge of A
		b.setPosition(-c, c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the top-right edge of A but is still intersecting A
		b.setPosition(d, d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated towards the top-right edge of A but is still touching the edge of A
		b.setPosition(1, 1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																
		// B is translated beyond the top-right edge of A
		b.setPosition(c, c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the top-back edge of A but is still intersecting A
		b.setPosition(0, d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated towards the top-back edge of A but is still touching the edge of A
		b.setPosition(0, 1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																
		// B is translated beyond the top-back edge of A
		b.setPosition(0, c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the top-front edge of A but is still intersecting A
		b.setPosition(0, d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
														
		// B is translated towards the top-front edge of A but is still touching the edge of A
		b.setPosition(0, 1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																		
		// B is translated beyond the top-front edge of A
		b.setPosition(0, c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated towards the bottom-left edge of A but is still intersecting A
		b.setPosition(-d, -d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
										
		// B is translated towards the bottom-left edge of A but is still touching the edge of A
		b.setPosition(-1, -1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
														
		// B is translated beyond the bottom-left edge of A
		b.setPosition(-c, -c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the bottom-right edge of A but is still intersecting A
		b.setPosition(d, -d, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated towards the bottom-right edge of A but is still touching the edge of A
		b.setPosition(1, -1, 0); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																
		// B is translated beyond the bottom-right edge of A
		b.setPosition(c, -c, 0); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the bottom-back edge of A but is still intersecting A
		b.setPosition(0, -d, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
												
		// B is translated towards the bottom-back edge of A but is still touching the edge of A
		b.setPosition(0, -1, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																
		// B is translated beyond the bottom-back edge of A
		b.setPosition(0, -c, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the bottom-front edge of A but is still intersecting A
		b.setPosition(0, -d, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
														
		// B is translated towards the bottom-front edge of A but is still touching the edge of A
		b.setPosition(0, -1, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																		
		// B is translated beyond the bottom-front edge of A
		b.setPosition(0, -c, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
		
		// B is translated towards the left-front edge of A but is still intersecting A
		b.setPosition(-d, 0, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																
		// B is translated towards the left-front edge of A but is still touching the edge of A
		b.setPosition(-1, 0, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																				
		// B is translated beyond the left-front edge of A
		b.setPosition(-c, 0, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the right-front edge of A but is still intersecting A
		b.setPosition(d, 0, d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																		
		// B is translated towards the right-front edge of A but is still touching the edge of A
		b.setPosition(1, 0, 1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																						
		// B is translated beyond the right-front edge of A
		b.setPosition(c, 0, c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		
		// B is translated towards the left-back edge of A but is still intersecting A
		b.setPosition(-d, 0, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																		
		// B is translated towards the left-back edge of A but is still touching the edge of A
		b.setPosition(-1, 0, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																						
		// B is translated beyond the left-back edge of A
		b.setPosition(-c, 0, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
				
				
		// B is translated towards the right-back edge of A but is still intersecting A
		b.setPosition(d, 0, -d); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																				
		// B is translated towards the right-back edge of A but is still touching the edge of A
		b.setPosition(1, 0, -1); assertTrue(a.testOBB(b)); assertTrue(b.testOBB(a));
																								
		// B is translated beyond the right-back edge of A
		b.setPosition(c, 0, -c); assertFalse(a.testOBB(b)); assertFalse(b.testOBB(a));
		
		/***********************************************************************************/
	}

}
