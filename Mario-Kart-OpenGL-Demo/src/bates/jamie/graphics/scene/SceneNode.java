package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Renderer;


public class SceneNode
{
	private List<SceneNode> children;
	
	private List<Face> geometry;
	private Model model;
	private int displayList;
	
	private float[] color = {1, 1, 1};
	
	private float[] t;
	private float[] r;
	private float[] s;
	
	private float[] orientation;
	
	private MatrixOrder order;
	private RenderMode renderMode;
	
	private Material material;
	
	public SceneNode(List<Face> geometry, int displayList, Model model, MatrixOrder order, Material material)
	{
		children = new ArrayList<SceneNode>();
		
		this.geometry = geometry;
		this.displayList = displayList;
		this.model = model;
		
		color = new float[4];
		
		this.order = order;
		
		this.material = material;
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			gl.glColor3f(color[0], color[1], color[2]);
			
			if(model != null)
			{	
				switch(renderMode)
				{
					case TEXTURE: model.render(gl); break;      
					case COLOR  : model.render(gl); break;
					case GLASS  : model.renderGlass(gl, color); break;
				}
			}
			else if(displayList != -1) gl.glCallList(displayList);
			else
			{
				switch(renderMode)
				{
					case TEXTURE: Renderer.displayTexturedObject(gl, geometry);        break;
					case COLOR  : Renderer.displayColoredObject (gl, geometry, color); break;
					case GLASS  : Renderer.displayGlassObject   (gl, geometry, color); break;
				}
			}
			
			for(SceneNode child : children) child.render(gl);
		}
		gl.glPopMatrix();
	}
	
	public void renderGhost(GL2 gl, float fade)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			if(model != null)
			{
				gl.glColor3f(fade, fade, fade);
				
				gl.glDisable(GL_LIGHTING);
				gl.glEnable(GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
				
				model.render(gl);
				
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
				gl.glDisable(GL_BLEND);
				gl.glEnable(GL_LIGHTING);
			}
			else Renderer.displayTransparentObject(gl, geometry, fade);
			
			for(SceneNode child : children) child.renderGhost(gl, fade);
		}
		gl.glPopMatrix();
	}
	
	public void renderColor(GL2 gl, float[] color)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			
			if(model != null)
			{
				gl.glColor3f(color[0], color[1], color[2]);
				model.render(gl);
			}
			else Renderer.displayColoredObject(gl, geometry, color);
			
			for(SceneNode child : children) child.renderColor(gl, color);
		}
		gl.glPopMatrix();
	}
	
	public void setRenderMode(RenderMode mode) { renderMode = mode; }
	
	public List<SceneNode> getChildren() { return children; }
	
	public void setChildren(List<SceneNode> children) { this.children = children; }
	
	public void addChild(SceneNode child) { children.add(child); }

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
			
			case T_M:
			{
				gl.glTranslatef(t[0], t[1], t[2]);
				gl.glMultMatrixf(orientation, 0);
				
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

	public Material getMaterial()
	{
		return material;
	}

	public void setMaterial(Material material)
	{
		this.material = material;
	}

	public enum MatrixOrder
	{
		NONE,
		T,
		RX, RY, RZ,
		S,
		T_RX_RY_RZ_S,
		T_S,
		T_M, T_M_S;
	}
	
	public enum RenderMode
	{
		TEXTURE,
		COLOR,
		GLASS;
	}
}
