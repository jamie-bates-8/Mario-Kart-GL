package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.ArrayList;
import java.util.Random;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.gl2.GLUgl2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.Vec3;

public class LightningStrike
{
	private boolean useNURBS;
	private boolean animated = true;
	private float[] color = RGB.BRIGHT_BLUE;
	private float   width;
	
	public  int duration = -30;
	private RenderStyle style;
	private BoltType type;
			
	private Vec3 origin; // the start point of the lightning bolt
	private Vec3 target; // the end point of the lightning bolt
	private Vec3 offset; // the direction in which the bolt deviates from the line segment (origin -> target)
	
	private float[] majorControl; // u values along the line segment, used to generate the major control structure
	private float[] minorControl; // u values evaluated by the major control structure
	
	private float[] minorOffsets; // deviation of minor points from the major control structure
	private float[] majorOffsets; // deviation of major points from the line segment (origin -> target)
	
	public float offsetScale = 1; // high values cause the curve to bend more dramatically
	
	private int majorIntervals;
	private int minorIntervals;
	
	private Vec3[] major_ctrl_pts; // used to store the control points of the previous frame
	private Vec3[] minor_ctrl_pts;
	
	private int level;
	private LightningStrike parent;
	private ArrayList<LightningStrike> children = new ArrayList<LightningStrike>();
	
	private float originControl; // u value evaluated by the parent curve to determine the start point of this curve
	private float targetControl; // u value evaluated by the parent curve to determine the end point of this curve
	
	private Random generator;
	
	public LightningStrike(Vec3 start, Vec3 end, float width, boolean useNURBS, boolean animated, RenderStyle style)
	{
		this.level = 0; parent = null;
		
		this.useNURBS = useNURBS;
		this.animated = animated;
		this.style = style;
		this.width = width;
		
		origin = start;
		target = end;
		offset = end.subtract(start).cross(Vec3.POSITIVE_Z_AXIS);
		offset = offset.normalize();
		
		majorIntervals = 10;
		minorIntervals = 64;
		
		generator = new Random();
		
		setup();
		update();
	}
	
	private void setup()
	{
		if(useNURBS) setupMajorNURBSPoints ();
		else         setupMajorBezierPoints();
		
		setupMinorPoints();
	}
	
	private void setupMajorNURBSPoints()
	{
		majorControl  = new float[majorIntervals + 2];
		majorOffsets  = new float[majorIntervals + 2];
		float[] stops = new float[majorIntervals];
		float   stop  = 0;
		
		for(int i = 0; i < stops.length; i++)
		{
			stop += 0.25 + generator.nextFloat() * 0.75;
			stops[i] = stop;
		}
		
		for(int i = 1; i <= majorIntervals; i++)
		{	    
//			float dist = stops[i - 1] / stops[stops.length - 1];
//			majorControl[i] = dist * 0.975f;
			
			float dist = (float) i / (majorIntervals + 1);
			majorControl[i] = dist;
			     
			dist = (float) (generator.nextFloat() * Math.PI * 2);
			majorOffsets[i] = dist;
		}
	
		majorControl[0] = 0; // insert start point
		majorControl[majorControl.length - 1] = 1; // insert end point
		
		majorOffsets[0] = 0;
		majorOffsets[majorControl.length - 1] = 0;
	}
	
	private void setupMinorPoints()
	{
		minorControl = new float[minorIntervals + 2];
		minorOffsets = new float[minorIntervals + 2];
		
		for(int i = 1; i <= minorIntervals; i++)
		{	    
			float dist = (float) i / (minorIntervals + 1);  
			minorControl[i] = dist;
			     
			dist = (float) (generator.nextFloat() * Math.PI * 2);	
			if(generator.nextFloat() < 0.10) dist = minorOffsets[i - 1];
			minorOffsets[i] = dist;
		}
	
		minorControl[0] = 0; // insert start point
		minorControl[minorControl.length - 1] = 1; // insert end point
		
		minorOffsets[0] = 0;
		minorOffsets[minorOffsets.length - 1] = 0;
	}
	
	private void setupMajorBezierPoints()
	{
		majorControl = new float[4];
		majorOffsets = new float[4];
		
		for(int i = 1; i <= 2; i++)
		{
			float dist = (float) i / 3;    
			majorControl[i] = dist;
				  
			dist = (float) (generator.nextFloat() * Math.PI * 2);	
			majorOffsets[i] = dist;
		}
		
		majorControl[0] = 0; // insert start point
		majorControl[3] = 1; // insert end point
		
		majorOffsets[0] = 0;
		majorOffsets[3] = 0;
	}
	
	/** 
	 * This method generates the four control points of the major control structure
	 * when the structure used is a bezier curve (hence, there are only 4 points).
	 */
	private void updateMajorBezierPoints()
	{	
		major_ctrl_pts = new Vec3[4];
		Vec3 direct = target.subtract(origin);
		
		for(int i = 1; i <= 2; i++)
		{
			if(animated) majorOffsets[i] += generator.nextFloat() > 0.01 ? 0.05 : Math.PI * (2.0 / 3.0);
			double dist = Math.cos(majorOffsets[i]) * 4.00 * offsetScale;
			if(animated && generator.nextFloat() < 0.01) dist *= 2 + 2 * generator.nextFloat();
			
			Vec3 p = origin.add(direct.multiply(majorControl[i]));
				
			major_ctrl_pts[i] = p.add(offset.multiply(dist));
		}
		
		major_ctrl_pts[0] = origin;
		major_ctrl_pts[3] = target;
	}
	
	/**
	 * This method generates the control points of the NURBS curve (minor control
	 * structure) used to render the actual lightning strike.
	 */
	private void updateMinorNURBSPoints()
	{	
		minor_ctrl_pts = new Vec3[minorIntervals + 2];
		
		for(int i = 1; i <= minorIntervals; i++)
		{
			if(animated) minorOffsets[i] += generator.nextFloat() > 0.005 ? 0.05 : Math.PI;
			double dist = Math.cos(minorOffsets[i]) * 0.30;
			if(animated && generator.nextFloat() < 0.01) dist *= 1 + 1 * generator.nextFloat();
			
			float[] knots = getKnotVector(majorIntervals, false);
			
			Vec3 point = !useNURBS ? Renderer.bezierCurveCubic(major_ctrl_pts, minorControl[i]): 
							  Renderer.evaluateNURBSCurveCubic(major_ctrl_pts, knots, minorControl[i]);
				
//			if(animated) point = point.add(Vec3.getRandomVector((float) dist));
			
			minor_ctrl_pts[i] = point.add(offset.multiply(dist));
		}
		
		minor_ctrl_pts[0] = origin;
		minor_ctrl_pts[minor_ctrl_pts.length - 1] = target;
	}
	
	/** 
	 * This method generates the control points of the major control structure when the
	 * structure used is a NURBS curve (hence, there is an arbitrary number of points).
	 */
	private void updateMajorNURBSPoints()
	{
		major_ctrl_pts = new Vec3[majorIntervals + 2];
		Vec3 direct = target.subtract(origin);
		
		for(int i = 1; i <= majorIntervals; i++)
		{
			if(animated) majorOffsets[i] += 0.075;
			double dist = Math.cos(majorOffsets[i]) * 1.20 * offsetScale;
			if(animated && generator.nextFloat() < 0.1) dist *= 2 + 2 * generator.nextFloat();
			
			Vec3 p = origin.add(direct.multiply(majorControl[i]));
			major_ctrl_pts[i] = p.add(offset.multiply(dist));
		}
		
		major_ctrl_pts[0] = origin;
		major_ctrl_pts[major_ctrl_pts.length - 1] = target;
	}
	
	/**
	 * This method returns a knot vector of evenly spaced knots for use in NURBS curve evaluation
	 * 
	 * @param intervals - This parameter demetermines the length of the knot vector, which is equal
	 * to the number of control points (intervals + 2) plus the degree of the curve.
	 *
	 * @param openGL - specifies whether or not the returned knot vector should be compatible
	 * with the internal implementation of NURBS within OpenGL (specifically GLU).
	 * 
	 * @return the knot vector
	 */
	private static float[] getKnotVector(int intervals, boolean openGL)
	{
		float[] knots = new float[intervals + 2 + (openGL ? 4 : 3)];
		
		int knot = 0;
		
		for(int i = 0; i < knots.length; i++)
		{
			if(i > (openGL ? 3 : 2) && i <= knots.length - 4) knot++;
			knots[i] = knot;
		}
		
		return knots;
	}
	
	public void render(GL2 gl, Vec3 eye_direction)
	{
		gl.glPushMatrix();
		{		
			boolean multisample = gl.glIsEnabled(GL2.GL_MULTISAMPLE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glDisable(GL_TEXTURE_2D);
			
			gl.glLineWidth(width);
			
			if(!multisample) gl.glEnable(GL2.GL_LINE_SMOOTH);
			
			gl.glEnable(GL_BLEND);
			
			// the start and end points of the curve must be updated before the new control points
			// can be calculated
			if(Scene.enableAnimation && level > 0)
			{
				Vec3 [] parent_ctrl_pts = parent.minor_ctrl_pts;
				float[] evaluator_knots = getKnotVector(parent.minorIntervals, false);
				
				if(type == BoltType.SELF_ARCH)
					target = Renderer.evaluateNURBSCurveCubic(parent_ctrl_pts, evaluator_knots, targetControl);
				// for other types of bolt, the target must be updated elsewhere by the application
				
				origin = Renderer.evaluateNURBSCurveCubic(parent_ctrl_pts, evaluator_knots, originControl);
				
			}
			
			if(animated && Scene.enableAnimation && useNURBS && Scene.sceneTimer % 30 == 0 && generator.nextFloat() < 0.5)
			{
				// reset the major control structure to randomize the appearance of the bolt
				if(useNURBS) setupMajorNURBSPoints ();
				else         setupMajorBezierPoints();
			}
			
			offset = target.subtract(origin).cross(eye_direction).normalize();
			
			if(Scene.enableAnimation) update();
			
			float[] knots = getKnotVector(minorIntervals, true);
			
			if(debugMode)
			{
				boolean useHDR = BloomStrobe.end(gl); // disable HDR bloom for the debug visuals
				
				Renderer.displayPoints(gl, Vec3.toArray1D(minor_ctrl_pts), minor_debug_colors[level], 3);
				Renderer.displayLines (gl, Vec3.toArray1D(minor_ctrl_pts), minor_debug_colors[level], false);
				
//				Renderer.displayNURBSCurve(gl, major_ctrl_pts, getKnotVector(majorIntervals, false), major_debug_colors[level], 500);
//				Renderer.displayNURBSCurve(gl, minor_ctrl_pts, getKnotVector(minorIntervals, false), minor_debug_colors[level], 500);
				
				Renderer.displayPoints(gl, Vec3.toArray1D(major_ctrl_pts), major_debug_colors[level], 6);
				Renderer.displayLines (gl, Vec3.toArray1D(major_ctrl_pts), major_debug_colors[level], false);
				
				if(useHDR) BloomStrobe.begin(gl);
			}
			else
			{
				GLUgl2 glu = new GLUgl2();
				GLUnurbs nurb = glu.gluNewNurbsRenderer();
				
				duration++;
				float alpha = 1;
				
				switch(style)
				{
					case CONTINUOUS : break;
					case PERIODIC_FLASH : alpha = (float) Math.abs(Math.cos(Math.PI * 2 * (Scene.sceneTimer % 300) / 300)); break;
					case SINGLE_FLASH : alpha = 1.0f - ((float) Math.abs(duration) / 120); break;
				}

				gl.glColor4fv(RGB.toRGBA(color, alpha), 0);
				
				if(style != RenderStyle.SINGLE_FLASH || duration < 120)
				{
					glu.gluBeginCurve(nurb);
					glu.gluNurbsCurve(nurb, knots.length, knots, 3, Vec3.toArray1D(minor_ctrl_pts), 4, GL2.GL_MAP1_VERTEX_3);
					glu.gluEndCurve(nurb);
				}
			}

			gl.glDepthMask(true);
			
			gl.glEnable (GL2.GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable (GL_LIGHTING);
			
			if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);
			
			gl.glLineWidth(1);
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
		
		for(LightningStrike bolt : children) bolt.render(gl, eye_direction);
	}

	private void update()
	{
		if(useNURBS) updateMajorNURBSPoints();
		else         updateMajorBezierPoints();
		
		updateMinorNURBSPoints();
	}
	
	public void addChild(LightningStrike bolt) { children.add(bolt); }
	
	public ArrayList<LightningStrike> getChildren() { return children; }
	
	public LightningStrike generateBolt(RenderStyle style, BoltType type, float width, Vec3 target, int majorIntervals, int minorIntervals)
	{
		Vec3 start = new Vec3();
		Vec3 end   = new Vec3();
		
		float originControl = 0;
		float targetControl = 1;
		
		float[] knots = getKnotVector(minorIntervals, false);
		
		switch(type)
		{
			case SELF_ARCH:
			{
				originControl = 0.05f + generator.nextFloat() * 0.15f;
				targetControl = originControl + 0.55f + generator.nextFloat() * 0.25f;
			
				start = Renderer.evaluateNURBSCurveCubic(minor_ctrl_pts, knots, originControl);
				end   = Renderer.evaluateNURBSCurveCubic(minor_ctrl_pts, knots, targetControl);
				
				break;
			}
			case ARCH:
			{
				originControl = 0.25f + generator.nextFloat() * 0.50f;
				
				start = Renderer.evaluateNURBSCurveCubic(minor_ctrl_pts, knots, originControl);
				end   = target;
				
				break;
			}
			case END_ARCH:
			{
				originControl = 1.00f;
				
				start = this.target;
				end   = target;
				
				break;
			}
			case FORK:
			{
				originControl = 0.25f + generator.nextFloat() * 0.50f; 
				
				Vec3 dir = target.subtract(origin);
				RotationMatrix rot = new RotationMatrix(dir.cross(Vec3.getRandomVector()), 10 + generator.nextInt(35));
				
				start = Renderer.evaluateNURBSCurveCubic(minor_ctrl_pts, knots, originControl);
				end   = dir.multiply(0.3 + generator.nextFloat() * 0.2f); // length of bolt
				end   = start.add(end.multiply(rot));	
				
				break;
			}
		}
		
		LightningStrike bolt = new LightningStrike(start, end, width, true, animated, style);
		
		bolt.level = level + 1;
		bolt.parent = this;
		
		bolt.type = type;
		
		bolt.originControl = originControl;
		bolt.targetControl = targetControl;
		
		bolt.minorIntervals = minorIntervals;
		bolt.majorIntervals = majorIntervals;
		
		bolt.offset = offset;
		
		if(useNURBS) bolt.setupMajorNURBSPoints ();
		else         bolt.setupMajorBezierPoints();
		bolt.setupMinorPoints();
			
		return bolt;
	}
	
	public void setIntervals(int major, int minor)
	{
		minorIntervals = minor;
		majorIntervals = major;
		
		setup();
	}
	
	public void setOrigin(Vec3 origin) { this.origin = origin; }
	public void setTarget(Vec3 target) { this.target = target; }
	
	public void setColor(float[] color) { this.color = color; }
	
	public boolean isDead() { return duration > 120; }
	
	private static float[][] minor_debug_colors = new float[][] {RGB.BRIGHT_GREEN, RGB.BRIGHT_BLUE, RGB.VIOLET, RGB.BRIGHT_RED};
	private static float[][] major_debug_colors = new float[][] {RGB.GREEN, RGB.BLUE, RGB.PLUM, RGB.RED};
	
	public static boolean debugMode = false;
	
	public enum RenderStyle
	{
		CONTINUOUS,     // always visable
		SINGLE_FLASH,   // fades in visability over time
		PERIODIC_FLASH; // dades in and out of visability periodically
	}
	
	public enum BoltType
	{
		ARCH,      // the start of the bolt is connected to its parent whilst the end is connected to another object
		FORK,      // the start (only) of this bolt is connected to its parent bolt
		SELF_ARCH, // bolt shoots out of the parent bolt in an arch and then reconnects with the parent bolt
		END_ARCH;  // a bolt that arches from the end of its parent bolt to an object
	}
}
