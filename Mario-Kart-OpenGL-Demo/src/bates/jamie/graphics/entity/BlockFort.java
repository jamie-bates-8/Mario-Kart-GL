package bates.jamie.graphics.entity;

import static bates.jamie.graphics.util.Renderer.displayWildcardObject;
import static bates.jamie.graphics.util.Renderer.displayWireframeObject;

import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.process.ShadowCaster;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;


public class BlockFort
{
	private static final List<Face> FORT_FACES = OBJParser.parseTriangles("block_fort");
	
	public int renderMode = 0;
	public boolean displayModel = false;
	
	private static Texture greenGranite;
	private static Texture greenMetal;
	private static Texture blueGranite;
	private static Texture blueMetal;
	private static Texture redGranite;
	private static Texture redMetal;
	private static Texture yellowGranite;
	private static Texture yellowMetal;
	
	private static int fortList = -1;
	
	private GreenFort green_fort;
	
	private List<OBB> bounds;
	
	public BlockFort(GL2 gl)
	{
		green_fort = new GreenFort(gl);
		
		loadTextures(gl);
		
		fortList = gl.glGenLists(4);

	    gl.glNewList(fortList, GL2.GL_COMPILE);
	    displayWildcardObject(gl, FORT_FACES, new Texture[] {greenMetal, greenGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 1, GL2.GL_COMPILE);
		displayWildcardObject(gl, FORT_FACES, new Texture[] {blueMetal, blueGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 2, GL2.GL_COMPILE);
		displayWildcardObject(gl, FORT_FACES, new Texture[] {redMetal, redGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 3, GL2.GL_COMPILE);
		displayWildcardObject(gl, FORT_FACES, new Texture[] {yellowMetal, yellowGranite});
	    gl.glEndList();
	    
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void loadTextures(GL2 gl)
	{
		try
		{
			greenGranite  = TextureLoader.load(gl, "tex/greenGranite.jpg" , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			greenMetal    = TextureLoader.load(gl, "tex/greenMetal.jpg"   , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			blueGranite   = TextureLoader.load(gl, "tex/blueGranite.jpg"  , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			blueMetal     = TextureLoader.load(gl, "tex/blueMetal.jpg"    , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			redGranite    = TextureLoader.load(gl, "tex/redGranite.jpg"   , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			redMetal      = TextureLoader.load(gl, "tex/redMetal.jpg"     , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			yellowGranite = TextureLoader.load(gl, "tex/yellowGranite.jpg", GL2.GL_LINEAR_MIPMAP_LINEAR, true);
			yellowMetal   = TextureLoader.load(gl, "tex/yellowMetal.jpg"  , GL2.GL_LINEAR_MIPMAP_LINEAR, true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void render(GL2 gl)
	{
		if(!displayModel) return;
		
//		if(renderMode == 2) gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
		green_fort.render(gl);
		
		Shader shader = Shader.getLightModel("shadow");
		if(shader != null)
		{
			shader.enable(gl);
			
			if(Scene.enableShadow)
			{
				shader.setUniform(gl, "enableShadow", true);
				shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
				shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
				
				shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);
			}
			else shader.setUniform(gl, "enableShadow", false);
		}
		
//		gl.glPushMatrix(); // Green Fort
//		{
//			gl.glTranslatef(90, 30, 90);
//			gl.glScalef(30.0f, 30.0f, 30.0f);
//			
//			if(shader != null)
//			{
//				float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
//				Matrix.translate(model, 90, 30, 90);
//				Matrix.scale    (model, 30, 30, 30);
//				
//				shader.loadmodel_matrix(gl, model);
//				//TODO seems to generate "OpenGL Error: invalid operation" for unknown reason
//			}
//
//			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK);
//			else if(renderMode == 3) displayWildcardObject(gl, FORT_FACES, new Texture[] {greenMetal, greenGranite});
//			else gl.glCallList(fortList);
//		}	
//		gl.glPopMatrix();

		gl.glPushMatrix(); // BLue Fort TODO shadows incorrect
		{	
			gl.glTranslatef(-90, 30, 90);
			gl.glRotatef(-90, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);
			
			if(shader != null)
			{
				float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
				Matrix.translate(model, -90, 30, 90);
				Matrix.scale(model, 30, 30, 30);
				float[] rotation = Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, -90, 0));
				Matrix.multiply(model, model, rotation);
				
				shader.setModelMatrix(gl, model);
			}

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK);
			else if(renderMode == 3) displayWildcardObject(gl, FORT_FACES, new Texture[] {greenMetal, greenGranite});
			else gl.glCallList(fortList + 1);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix(); // Red Fort
		{
			gl.glTranslatef(-90, 30, -90);
			gl.glRotatef(-180, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);
			
			if(shader != null)
			{
				float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
				Matrix.translate(model, -90, 30, -90);
				float[] rotation = Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, -180, 0));
				Matrix.multiply(model, model, rotation);
				Matrix.scale(model, 30, 30, 30);
				
				shader.setModelMatrix(gl, model);
			}

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK);
			else if(renderMode == 3) displayWildcardObject(gl, FORT_FACES, new Texture[] {greenMetal, greenGranite});
			else gl.glCallList(fortList + 2);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix(); // Yellow Fort TODO shadows incorrect
		{	
			gl.glTranslatef(90, 30, -90);
			gl.glRotatef(-270, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);
			
			if(shader != null)
			{
				float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
				Matrix.translate(model, 90, 30, -90);
				Matrix.scale(model, 30, 30, 30);
				float[] rotation = Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, -270, 0));
				Matrix.multiply(model, model, rotation);
				
				shader.setModelMatrix(gl, model);
			}

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK);
			else if(renderMode == 3) displayWildcardObject(gl, FORT_FACES, new Texture[] {greenMetal, greenGranite});
			else gl.glCallList(fortList + 3);
		}	
		gl.glPopMatrix();
		
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public List<OBB> getBounds() { return bounds; }
}
