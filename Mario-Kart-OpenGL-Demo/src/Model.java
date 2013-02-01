import graphics.util.Face;
import graphics.util.Renderer;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;


public class Model
{
	private List<Model> children;
	
	private List<Face> geometry;
	private int displayList;
	
	private float[] color;
	
	private float[] t;
	private float[] r;
	private float[] s;
	
	private float[] orientation;
	
	private MatrixOrder order;
	private RenderMode renderMode;
	
	public Model(List<Face> geometry, int displayList, MatrixOrder order)
	{
		children = new ArrayList<Model>();
		
		this.geometry = geometry;
		this.displayList = displayList;
		
		color = new float[4];
		
		this.order = order;
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			if(displayList != -1) gl.glCallList(displayList);
			else
			{
				switch(renderMode)
				{
					case TEXTURE    : Renderer.displayTexturedObject   (gl, geometry);        break;
					case COLOR      : Renderer.displayColoredObject    (gl, geometry, color); break;
					case TRANSPARENT: Renderer.displayTransparentObject(gl, geometry, color); break;
				}
			}
			
			for(Model child : children) child.render(gl);
		}
		gl.glPopMatrix();
	}
	
	public void renderGhost(GL2 gl, float fade)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			Renderer.displayTransparentObject(gl, geometry, fade);
			
			for(Model child : children) child.renderGhost(gl, fade);
		}
		gl.glPopMatrix();
	}
	
	public void renderColor(GL2 gl, float[] color)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			Renderer.displayColoredObject(gl, geometry, color);
			
			for(Model child : children) child.renderColor(gl, color);
		}
		gl.glPopMatrix();
	}
	
	public void setRenderMode(RenderMode mode) { renderMode = mode; }
	
	public List<Model> getChildren() { return children; }
	
	public void setChildren(List<Model> children) { this.children = children; }
	
	public void addChild(Model child) { children.add(child); }

	public List<Face> getGeometry() { return geometry; }

	public void setGeometry(List<Face> geometry) { this.geometry = geometry; }

	public int getDisplayList() { return displayList; }

	public void setDisplayList(int displayList) { this.displayList = displayList; }
	
	public void setColor(float[] color) { this.color = color; }
	
	public void setTranslation(float[] t) { this.t = t; }
	
	public void setRotation(float[] r) { this.r = r; }
	
	public void setScale(float[] s) { this.s = s; }
	
	public void setupMatrix(GL2 gl)
	{
		switch(order)
		{
			case T : gl.glTranslatef(t[0], t[1], t[2]); break;
			case RX: gl.glRotatef(r[0], 1, 0, 0); break;
			case RY: gl.glRotatef(r[1], 0, 1, 0); break;
			case RZ: gl.glRotatef(r[2], 0, 0, 1); break;
			case S:  gl.glScalef(s[0], s[1], s[2]); break;
			
			case T_RX_RY_RZ_S:
			{
				gl.glTranslatef(t[0], t[1], t[2]); 
				gl.glRotatef(r[0], 1, 0, 0); 
				gl.glRotatef(r[1], 0, 1, 0); 
				gl.glRotatef(r[2], 0, 0, 1);
				gl.glScalef(s[0], s[1], s[2]);
				
				break;
			}
			
			case T_S:
			{
				gl.glTranslatef(t[0], t[1], t[2]);
				gl.glScalef(s[0], s[1], s[2]);
				
				break;
			}
			
			case T_M_S:
			{
				gl.glTranslatef(t[0], t[1], t[2]);
				gl.glMultMatrixf(orientation, 0);
				gl.glScalef(s[0], s[1], s[2]);
				
				break;
			}
			
			default : break;
		}
	}
	
	public float[] getOrientation() { return orientation; }

	public void setOrientation(float[] orientation) { this.orientation = orientation; }

	public enum MatrixOrder
	{
		NONE,
		T,
		RX, RY, RZ,
		S,
		T_RX_RY_RZ_S,
		T_S,
		T_M_S;
	}
	
	public enum RenderMode
	{
		TEXTURE,
		COLOR,
		TRANSPARENT;
	}
}
