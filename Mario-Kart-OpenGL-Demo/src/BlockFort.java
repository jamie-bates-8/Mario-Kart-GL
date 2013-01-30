import static graphics.util.Renderer.displayWildcardObject;
import static graphics.util.Renderer.displayWireframeObject;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import graphics.util.Face;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class BlockFort
{
	private static final List<Face> FORT_FACES = OBJParser.parseTriangles("obj/blockFort.obj");
	
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
	
	static
	{
		try
		{
			greenGranite  = TextureIO.newTexture(new File("tex/greenGranite.jpg"), true);
			greenMetal    = TextureIO.newTexture(new File("tex/greenMetal.jpg"), true);
			blueGranite   = TextureIO.newTexture(new File("tex/blueGranite.jpg"), true);
			blueMetal     = TextureIO.newTexture(new File("tex/blueMetal.jpg"), true);
			redGranite    = TextureIO.newTexture(new File("tex/redGranite.jpg"), true);
			redMetal      = TextureIO.newTexture(new File("tex/redMetal.jpg"), true);
			yellowGranite = TextureIO.newTexture(new File("tex/yellowGranite.jpg"), true);
			yellowMetal   = TextureIO.newTexture(new File("tex/yellowMetal.jpg"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private List<OBB> bounds;
	
	public BlockFort(GL2 gl)
	{
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
	    
		System.out.println("\n" + "Block Fort: " + FORT_FACES.size() * 4 + " faces" + "\n");
	    
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void render(GL2 gl)
	{
		if(!displayModel) return;
		
		if(renderMode == 2) gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, 90);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK_3F);
			else if(renderMode == 3)
			{
				gl.glDisable(GL_LIGHTING);
				gl.glEnable(GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
				
				gl.glColor3f(0.1f, 0.1f, 0.1f);
				
				gl.glCallList(fortList);
				
				gl.glColor3f(1, 1, 1);
				
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
				gl.glDisable(GL_BLEND);
				gl.glEnable(GL_LIGHTING);
			}
			else gl.glCallList(fortList);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, 90);
			gl.glRotatef(-90, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK_3F);
			else gl.glCallList(fortList + 1);

		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, -90);
			gl.glRotatef(-180, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK_3F);
			else gl.glCallList(fortList + 2);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, -90);
			gl.glRotatef(-270, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(renderMode == 1) displayWireframeObject(gl, FORT_FACES, RGB.BLACK_3F);
			else gl.glCallList(fortList + 3);
		}	
		gl.glPopMatrix();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public List<OBB> getBounds() { return bounds; }
}
