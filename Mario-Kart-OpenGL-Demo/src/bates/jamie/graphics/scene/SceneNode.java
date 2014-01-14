package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;


public class SceneNode
{
	private List<SceneNode> children;
	
	private SceneNode parent;
	private SceneNode root;
	
	private List<Face> geometry;
	private Model model;
	private int displayList;
	
	private float[] color = {1, 1, 1};
	
	private Vec3 t = new Vec3(0); // translation
	private Vec3 r = new Vec3(0); // rotation
	private Vec3 s = new Vec3(1); // scale
	
	private float[] orientation;
	
	private MatrixOrder order;
	private RenderMode renderMode;
	
	private Material material;
	private Reflector reflector;
	private float reflectivity = 1.0f;
	
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
			gl.glColor3fv(color, 0);
			
			switch(renderMode)
			{
				case TEXTURE:
				{
					Shader shader = Shader.enabled ? Shader.getLightModel("texture") : null;
					if(shader != null)
					{
						shader.enable(gl);
						shader.setSampler(gl, "texture", 0);
					}
					break;      
				}
				case BUMP_RAIN:
				{
					Shader shader = Shader.enabled ? Shader.get("bump_rain") : null;
					if(shader != null)
					{
						shader.enable(gl);
						shader.setSampler(gl, "rainMap", 4);
						shader.setSampler(gl, "heightmap", 3);
						
						shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
						shader.setUniform(gl, "shininess", reflectivity);
						
						shader.setSampler(gl, "colourMap", 0);
						shader.setSampler(gl, "bumpmap", 1);
						
						gl.glActiveTexture(GL2.GL_TEXTURE4); Scene.singleton.rain_normal.bind(gl);
						gl.glActiveTexture(GL2.GL_TEXTURE0);
						
						shader.setUniform(gl, "timer", (float) Scene.sceneTimer);
						
						float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
						shader.loadMatrix(gl, "cameraMatrix", camera);
					}
					break;      
				}
				case BUMP_REFLECT:
				{
					Shader shader = Shader.enabled ? Shader.get("bump_cube") : null;
					if(shader != null)
					{
						shader.enable(gl);
						
						shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
						shader.setUniform(gl, "shininess", reflectivity);
						
						shader.setSampler(gl, "bumpmap", 1);
						
						float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
						shader.loadMatrix(gl, "cameraMatrix", camera);
					}
					break;      
				}
				case BUMP_COLOR:
				{
					Shader shader = Shader.enabled ? Shader.get("bump_phong") : null;
					if(shader != null)
					{
						shader.enable(gl);
				
						shader.setSampler(gl, "bumpmap", 1);
					}
					break;      
				}
				case COLOR  :
				{
					Shader shader = Shader.enabled ? Shader.getLightModel("phong") : null;
					if(shader != null) shader.enable(gl);
					break;
				}
				case REFLECT:
				{
					Shader shader = Shader.enabled ? Shader.getLightModel("cube") : null;
					if(shader != null)
					{
						shader.enable(gl);
						shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
						shader.setUniform(gl, "shininess", reflectivity);
						
						float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
						shader.loadMatrix(gl, "cameraMatrix", camera);
					}
					break;      
				}
				case GLASS  : break;
			}
			
			int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
			if(!Scene.reflectMode && Scene.singleton.enableBloom && renderMode != RenderMode.GLASS) gl.glDrawBuffers(2, attachments, 0);
			
			if(model != null)
			{
				switch(renderMode)
				{
					case TEXTURE    : 
					case BUMP_COLOR :
					case COLOR      : model.render(gl); break;
					case BUMP_RAIN :
					case BUMP_REFLECT:
					case REFLECT:
					{
						if(reflector != null) reflector.enable(gl);
						model.render(gl);
						if(reflector != null) reflector.disable(gl);
						break;
					}
					case GLASS : model.renderGlass(gl, color); break;
				}
			}
			else if(displayList != -1) gl.glCallList(displayList);
			else
			{
				switch(renderMode)
				{
					case BUMP_RAIN    :
					case TEXTURE      : Renderer.displayTexturedObject(gl, geometry);        break;
					case REFLECT      :
					case BUMP_REFLECT :
					case BUMP_COLOR   :
					case COLOR        : Renderer.displayColoredObject (gl, geometry, color); break;
					case GLASS        : Renderer.displayGlassObject   (gl, geometry, color); break;
				}
			}
			
			if(!Scene.reflectMode && Scene.singleton.enableBloom && renderMode != RenderMode.GLASS) gl.glDrawBuffers(1, attachments, 0);
			Shader.disable(gl);
			
			for(SceneNode child : children) child.render(gl);
		}
		gl.glPopMatrix();
	}

	public void renderGhost(GL2 gl, float fade, Shader shader, float[] color)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			gl.glColor3fv(color, 0);
			
			if(Shader.enabled && shader != null)
			{	
				Reflector reflector = this.reflector == null ? root.getReflector() : this.reflector;
				
				shader.enable(gl);
				shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
				
				shader.setUniform(gl, "eta", reflector.eta);
				shader.setUniform(gl, "reflectance", reflector.reflectance);
				
				float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
				shader.loadMatrix(gl, "cameraMatrix", camera);
				
				if(model != null)
				{
					if(reflector != null) reflector.enable(gl);
					model.render(gl);
					if(reflector != null) reflector.disable(gl);
				}
				else Renderer.displayColoredObject(gl, geometry, fade);
				
				Shader.disable(gl);
			}
			else
			{
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
			}
			
			for(SceneNode child : children) child.renderGhost(gl, fade, shader, color);
		}
		gl.glPopMatrix();
	}
	
	public void renderGhost(GL2 gl, float fade, Shader shader)
	{
		renderGhost(gl, fade, shader, RGB.WHITE_3F);
	}
	
	public void renderColor(GL2 gl, float[] color, Reflector reflector)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			
			Shader shader = Shader.enabled ? (reflector != null ? Shader.get("star_power") : Shader.get("phong_rim")) : null;
			if(shader != null)
			{
				shader.enable(gl);
				
				if(reflector != null)
				{
					shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
					shader.setUniform(gl, "shininess", reflectivity);
					
					float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
					shader.loadMatrix(gl, "cameraMatrix", camera);
				}
				else
				{
					shader.setUniform(gl, "rim_color", new float[] {1, 1, 1});
					shader.setUniform(gl, "rim_power", 3.0f);
				}
			}
			
			int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
			if(!Scene.reflectMode && Scene.singleton.enableBloom) gl.glDrawBuffers(2, attachments, 0);
			
			if(model != null)
			{
				gl.glColor3fv(color, 0);
				if(reflector != null) reflector.enable(gl);
				model.render(gl);
				if(reflector != null) reflector.disable(gl);
			}
			else Renderer.displayColoredObject(gl, geometry, color);
			
			if(!Scene.reflectMode && Scene.singleton.enableBloom) gl.glDrawBuffers(2, attachments, 0);
			Shader.disable(gl);
			
			for(SceneNode child : children) child.renderColor(gl, color, reflector);
		}
		gl.glPopMatrix();
	}
	
	public void setRenderMode(RenderMode mode) { renderMode = mode; }
	
	public List<SceneNode> getChildren() { return children; }
	
	public void setChildren(List<SceneNode> children) { this.children = children; }
	
	public void addChild(SceneNode child)
	{
		child.setParent(this);
		child.setRoot(root);
		children.add(child);
	}

	public List<Face> getGeometry() { return geometry; }

	public void setGeometry(List<Face> geometry) { this.geometry = geometry; }

	public int getDisplayList() { return displayList; }

	public void setDisplayList(int displayList) { this.displayList = displayList; }
	
	public void setColor(float[] color) { this.color = color; }
	
	public void setTranslation(Vec3 c) { this.t = c; }
	
	public void setRotation(Vec3 r) { this.r = r; }
	
	public void setScale(Vec3 s) { this.s = s; }
	
	public void setupMatrix(GL2 gl)
	{
		switch(order)
		{
			case T : gl.glTranslatef(t.x, t.y, t.z); break;
			
			case RX: gl.glRotatef(r.x, 1, 0, 0); break;
			case RY: gl.glRotatef(r.y, 0, 1, 0); break;
			case RZ: gl.glRotatef(r.z, 0, 0, 1); break;
			
			case S:  gl.glScalef(s.x, s.y, s.z); break;
			
			case T_RY_RX_RZ_S:
			{
				gl.glTranslatef(t.x, t.y, t.z); 
				
				gl.glRotatef(r.y, 0, 1, 0);
				gl.glRotatef(r.x, 1, 0, 0); 
				gl.glRotatef(r.z, 0, 0, 1);
				
				gl.glScalef(s.x, s.y, s.z);
				
				break;
			}
			
			case T_S:
			{
				gl.glTranslatef(t.x, t.y, t.z);
				gl.glScalef    (s.x, s.y, s.z);
				
				break;
			}
			
			case T_M:
			{
				gl.glTranslatef(t.x, t.y, t.z);
				gl.glMultMatrixf(orientation, 0);
				
				break;
			}
			
			case T_M_S:
			{
				gl.glTranslatef(t.x, t.y, t.z);
				gl.glMultMatrixf(orientation, 0);
				gl.glScalef(s.x, s.y, s.z);
				
				break;
			}
			
			default : break;
		}
	}
	
	public float[] getModelMatrix()
	{
		float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		
		switch(order)
		{
			case T : Matrix.translate(model, t.x, t.y, t.z); break;
			
			case RX: Matrix.multiply (model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(r.x,   0,   0))); break;
			case RY: Matrix.multiply (model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(  0, r.y,   0))); break;
			case RZ: Matrix.multiply (model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(  0,   0, r.z))); break;
			
			case S:  Matrix.scale    (model, s.x, s.y, s.z); break;
			
			case T_RY_RX_RZ_S:
			{
				Matrix.translate(model, t.x, t.y, t.z);
				Matrix.multiply (model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(r.x, r.y, r.z)));
				Matrix.scale    (model, s.x, s.y, s.z);
				
				break;
			}
			
			case T_S:
			{
				Matrix.translate(model, t.x, t.y, t.z);
				Matrix.scale    (model, s.x, s.y, s.z);
				
				break;
			}
			
			case T_M:
			{
				Matrix.translate(model, t.x, t.y, t.z);
				Matrix.multiply (model, model, orientation);
				
				break;
			}
			
			case T_M_S:
			{
				Matrix.translate(model, t.x, t.y, t.z);
				Matrix.multiply (model, model, orientation);
				Matrix.scale    (model, s.x, s.y, s.z);
				
				break;
			}
			
			default : break;
		}
		
		return model;
	}
	
	public float[] getOrientation() { return orientation; }

	public void setOrientation(float[] orientation) { this.orientation = orientation; }

	public Material getMaterial() { return material; }

	public void setMaterial(Material material) { this.material = material; }
	
	public Reflector getReflector() { return reflector; }
	
	public void setReflector(Reflector reflector) { this.reflector = reflector; }
	
	public Vec3 getPosition() { return t; }

	public void setReflectivity(float reflectivity) { this.reflectivity = reflectivity; }

	public SceneNode getParent() { return parent; }

	public void setParent(SceneNode parent) { this.parent = parent; }
	
	public SceneNode getRoot() { return root; }

	public void setRoot(SceneNode root) { this.root = root; }

	public enum MatrixOrder
	{
		NONE,
		T,
		RX, RY, RZ,
		S,
		T_RY_RX_RZ_S,
		T_S,
		T_M, T_M_S;
	}
	
	public enum RenderMode
	{
		TEXTURE,
		COLOR,
		BUMP_COLOR,
		BUMP_REFLECT,
		BUMP_RAIN,
		REFLECT,
		GLASS;
	}

	public void setModel(Model model) { this.model = model; }
}
