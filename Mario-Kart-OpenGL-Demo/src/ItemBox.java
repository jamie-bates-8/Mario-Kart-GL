import static graphics.util.Matrix.getRotationMatrix33;
import static graphics.util.Renderer.*;
import graphics.util.Face;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import static javax.media.opengl.GL2.*;


public class ItemBox
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/itemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;
	public static final int RESPAWN_TIME = 60;
	
	private static Texture questionMark;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/questionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public int respawnTimer;
	public OBB bound;
	public ParticleGenerator generator;
	
	public ItemBox(GL2 gl, float[] c)
	{
		respawnTimer = 0;
		
		bound = new OBB(
				c[0], c[1] + 3, c[2],
	    		0, 0, 0,
	    		SCALE, SCALE, SCALE);
		
		generator = new ParticleGenerator();
	}
	
	public void setRotation(float x, float y, float z)
	{	
		bound.u = getRotationMatrix33(x, y, z);
	}
	
	public List<Particle> generateParticles()
	{
		return generator.generateItemBoxParticles(getPosition(), 64);
	}
	
	public float[] getPosition() { return bound.c; }
	
	public static void increaseRotation() { rotation += 4; }
	
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			displayPartiallyTexturedObject(gl, BOX_FACES, new float[] {1, 1, 1});
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
		
//		gl.glPushMatrix();
//		{
//			gl.glBlendFunc(GL2.GL_DST_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//			gl.glDepthMask(false);
//			
//			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
//			gl.glRotatef(trajectory - 90, 0, 1, 0);
//			gl.glRotatef(180, 0, 0, 1);
//			gl.glScalef(5, 5, 5);
//			
//			questionMark.bind(gl);
//
//			gl.glBegin(GL_QUADS);
//			{
//				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
//				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
//				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
//				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
//			}
//			gl.glEnd();
//			
//			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
//			gl.glDisable(GL_BLEND);
//			gl.glEnable(GL_LIGHTING);
//			gl.glDepthMask(true);
//		}
//		gl.glPopMatrix();
	}
	
	public void destroy() { respawnTimer = RESPAWN_TIME; }
	
	public boolean isDead() { return respawnTimer > 0; }
}
