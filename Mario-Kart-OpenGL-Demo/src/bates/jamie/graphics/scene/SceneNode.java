package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.scene.process.ShadowCaster;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;
import bates.jamie.graphics.util.shader.Uniform;


public class SceneNode
{
	private List<SceneNode> children;
	
	private SceneNode parent;
	private SceneNode root;
	
	private List<Face> geometry;
	private Model model;
	private int displayList;
	
	private Shader shader;
	private List<Uniform> uniforms = new ArrayList<Uniform>();
	
	private Vec3 t = new Vec3(0); // translation
	private Vec3 r = new Vec3(0); // rotation
	private Vec3 s = new Vec3(1); // scale
	
	private float[] orientation;
	
	private MatrixOrder order;
	private RenderMode renderMode;
	
	private Material material;
	private Reflector reflector;
	private float reflectivity = 1.0f;
	
	private boolean enableParallax = false;
	private boolean enableBloom = true;
	
	public SceneNode(List<Face> geometry, int displayList, Model model, MatrixOrder order, Material material)
	{
		children = new ArrayList<SceneNode>();
		
		this.geometry = geometry;
		this.displayList = displayList;
		this.model = model;
		
		this.order = order;
		
		this.material = new Material(material);
		
		root = this;
	} 
	
	public SceneNode(Model model)
	{
		children = new ArrayList<SceneNode>();
		
		geometry = null;
		displayList = -1;
		this.model = model;
		
		order = MatrixOrder.T_RY_RX_RZ_S;
		
		material = new Material();
		
		root = this;
	} 
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			
			setupShader(gl);
			
			boolean useHDR = BloomStrobe.isEnabled();
			
			if(Scene.reflectMode || renderMode == RenderMode.GLASS || !enableBloom) BloomStrobe.end(gl);
			
			if(model != null)
			{
				switch(renderMode)
				{
					case BUMP_COLOR   :
					case BUMP_RAIN    :
					case BUMP_REFLECT :
					case BUMP_TEXTURE : if(!model.hasTangentData()) model.calculateTangents(); break;
				
					default: break;
				}
				
				switch(renderMode)
				{
					case TEXTURE      : 
					case BUMP_COLOR   :
					case COLOR        :
					case BLOOM_COLOR  :
					case BUMP_TEXTURE : model.render(gl); break;
					case BUMP_RAIN    :
					case BUMP_REFLECT :
					case REFLECT:
					{
						if(reflector != null) reflector.enable(gl);
						model.render(gl);
						if(reflector != null) reflector.disable(gl);
						break;
					}
					case GLASS : model.renderGlass(gl, material.getDiffuse()); break;
					case NORMAL: renderNormals(gl); break;
				}
			}
			else if(displayList != -1) gl.glCallList(displayList);
			else
			{
				switch(renderMode)
				{
					case BUMP_RAIN    :
					case BUMP_TEXTURE :
					case TEXTURE      : Renderer.displayTexturedObject(gl, geometry);        break;
					case REFLECT      :
					case BUMP_REFLECT :
					case BUMP_COLOR   :
					case BLOOM_COLOR  :
					case COLOR        : Renderer.displayColoredObject (gl, geometry, material.getDiffuse()); break;
					case GLASS        : Renderer.displayGlassObject   (gl, geometry, material.getDiffuse()); break;
					case NORMAL       : renderNormals(gl); break;
				}
			}
			if(useHDR) BloomStrobe.begin(gl);
			
			Shader.disable(gl);
			
			for(SceneNode child : children) child.render(gl);
		}
		gl.glPopMatrix();
	}

	private void setupShader(GL2 gl)
	{
		Shader shader = this.shader;
		
		switch(renderMode)
		{
			case TEXTURE:
			{
				if(shader == null) shader = Shader.getLightModel("texture");
				
				if(shader != null)
				{
					shader.enable(gl);
					shader.setSampler(gl, "texture", 0);
				}
				break;      
			}
			case BUMP_RAIN:
			{
				if(shader == null) shader = Shader.get("bump_rain");
				
				if(shader != null)
				{
					shader.enable(gl);
					shader.setSampler(gl, "rainMap", 3);
					shader.setSampler(gl, "heightmap", 2);
					
					shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
					shader.setUniform(gl, "shininess", reflectivity);
					
					shader.setSampler(gl, "colourMap", 0);
					shader.setSampler(gl, "bumpmap", 1);
					
					gl.glActiveTexture(GL2.GL_TEXTURE3); Scene.singleton.rain_normal.bind(gl);
					gl.glActiveTexture(GL2.GL_TEXTURE0);
					
					shader.setUniform(gl, "timer", (float) Scene.sceneTimer);
					
					float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
					shader.loadMatrix(gl, "cameraMatrix", camera);
				}
				break;      
			}
			case BUMP_REFLECT:
			{
				if(shader == null) shader = Shader.get("bump_cube");
				
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
				if(shader == null) shader = Shader.get("bump_phong");
				
				if(shader != null)
				{
					shader.enable(gl);
			
					shader.setSampler(gl, "bumpmap", 1);
				}
				break;      
			}
			case BUMP_TEXTURE:
			{
				if(shader == null) shader = (enableParallax && Scene.enableParallax) ? Shader.get("parallax_lights") : Shader.get("bump_lights");
				
				if(shader != null)
				{
					shader.enable(gl);
			
					shader.setSampler(gl, "texture"  , 0);
					shader.setSampler(gl, "bumpmap"  , 1);
					
					if(enableParallax)
					{
						shader.setSampler(gl, "heightmap", 2);
						shader.setUniform(gl, "camera_position", Scene.singleton.getCars().get(0).camera.getPosition());
					}
				}
				break;      
			}
			case BLOOM_COLOR:
			{
				if(shader == null) shader = Shader.get("bloom_color");
				if(shader != null) shader.enable(gl);
				break;
			}
			case COLOR:
			{
				if(shader == null) shader = Shader.getLightModel("phong");
				if(shader != null) shader.enable(gl);
				break;
			}
			case REFLECT:
			{
				if(shader == null) shader = Shader.getLightModel("cube");
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
			case GLASS :
			case NORMAL: break;
		}
		
		if(Scene.enableShadow && shader != null)
		{
			shader.setModelMatrix(gl, getModelMatrix());

			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

			shader.setUniform(gl, "enableShadow", true);
			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
		}
		else if(shader != null) shader.setUniform(gl, "enableShadow", false);
		
		for(Uniform uniform : uniforms) shader.setUniform(gl, uniform);
	}
	
	public List<Uniform> getUniforms() { return uniforms; }

	public void setUniforms(List<Uniform> uniforms) { this.uniforms = uniforms; }

	public void render(GL2 gl, Shader shader, Collection<Uniform> uniforms)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			
			if(shader != null)
			{	
				Reflector reflector = this.reflector == null ? root.getReflector() : this.reflector;
				
				shader.enable(gl);
				
				if(uniforms != null) for(Uniform uniform : uniforms) shader.setUniform(gl, uniform);
				
				if(Scene.enableShadow && shader != null)
				{
					shader.setModelMatrix(gl, getModelMatrix());

					shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

					shader.setUniform(gl, "enableShadow", true);
					shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
					shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
				}
				else if(shader != null) shader.setUniform(gl, "enableShadow", false);
				
				if(reflector != null) reflector.enable(gl);
				model.render(gl);
				if(reflector != null) reflector.disable(gl);
				
				Shader.disable(gl);
			}
			
			for(SceneNode child : children) child.render(gl, shader, uniforms);
		}
		gl.glPopMatrix();
	}

	public void renderGhost(GL2 gl, float fade, Shader shader, float[] color)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			gl.glColor3fv(color, 0);
			
			if(shader != null)
			{	
				Reflector reflector = this.reflector == null ? root.getReflector() : this.reflector;
				
				shader.enable(gl);
				
				if(!shader.equals(Shader.get("ghost_rim")))
				{
					shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
					
					shader.setUniform(gl, "eta", reflector.eta);
					shader.setUniform(gl, "reflectance", reflector.reflectance);
					
					float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
					shader.loadMatrix(gl, "cameraMatrix", camera);
				}
				
				if(shader.equals(Shader.get("invisible")) || shader.equals(Shader.get("ghost_rim")))
				{
					shader.setSampler(gl, "sceneSampler", 0);
					gl.glBindTexture(GL2.GL_TEXTURE_2D, Scene.singleton.bloom.getTexture(7));
					
					shader.setUniform(gl, "screenHeight", (float) Scene.singleton.getHeight());
					shader.setUniform(gl, "screenWidth" , (float) Scene.singleton.getWidth ());
						
					shader.setUniform(gl, "rim_color", new Vec3(0.3));
					shader.setUniform(gl, "rim_power", 3.0f);
				}
				
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
		renderGhost(gl, fade, shader, RGB.WHITE);
	}
	
	public void renderColor(GL2 gl, float[] color, Reflector reflector)
	{
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			if(material != null) material.load(gl);
			
			Shader shader = reflector != null ? Shader.get("star_power") : Shader.get("phong_rim");
			if(shader != null)
			{
				shader.enable(gl);
				
				if(reflector != null)
				{
					shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
					shader.setUniform(gl, "shininess", 0.75f);
					
					float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
					shader.loadMatrix(gl, "cameraMatrix", camera);
				}
				else
				{
					shader.setUniform(gl, "rim_color", new float[] {1, 1, 1});
					shader.setUniform(gl, "rim_power", 3.0f);
				}
			}
			
			boolean useHDR = BloomStrobe.isEnabled();
			
			if(Scene.reflectMode || !enableBloom) BloomStrobe.end(gl);
			
			if(model != null)
			{
				gl.glColor3fv(color, 0);
				if(reflector != null) reflector.enable(gl);
				model.render(gl);
				if(reflector != null) reflector.disable(gl);
			}
			else Renderer.displayColoredObject(gl, geometry, color);
			
			if(useHDR) BloomStrobe.begin(gl);
			
			Shader.disable(gl);
			
			for(SceneNode child : children) child.renderColor(gl, color, reflector);
		}
		gl.glPopMatrix();
	}
	
	public void updateReflection(GL2 gl)
	{
		if(reflector != null) reflector.update(gl, t);
		
		for(SceneNode child : children) child.updateReflection(gl);
	}
	
	public void useParallax(boolean enabled) { enableParallax = enabled; }
	
	public void enableBloom(boolean enabled) { enableBloom = enabled; }
	
	public void setRenderMode(RenderMode mode) { renderMode = mode; }
	
	public void setMatrixOrder(MatrixOrder order) { this.order = order; }
	
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
	
	public void setColor(float[] color) { material.setDiffuse(color); }
	
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
				Matrix.scale    (model, s.x, s.y, s.z);
				Matrix.multiply (model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(r.x, r.y, r.z)));
				
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
		BLOOM_COLOR,
		BUMP_COLOR,
		BUMP_REFLECT,
		BUMP_TEXTURE,
		BUMP_RAIN,
		REFLECT,
		GLASS,
		NORMAL;
	}

	public void setModel(Model model) { this.model = model; }

	public Vec3 getScale() { return s; }

	public Shader getShader() { return shader; }

	public void setShader(Shader shader) { this.shader = shader; }
	
	public void renderNormals(GL2 gl)
	{	
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			float scale = 1.0f / s.max();
			
			if(model != null) model.renderNormals(gl, true, scale);
		}
		gl.glPopMatrix();
	}
	
	public void renderTangents(GL2 gl)
	{	
		gl.glPushMatrix();
		{
			setupMatrix(gl);
			
			float scale = 1.0f / s.max();
			
			if(model != null) model.renderTangents(gl, true, scale);
		}
		gl.glPopMatrix();
	}
}
