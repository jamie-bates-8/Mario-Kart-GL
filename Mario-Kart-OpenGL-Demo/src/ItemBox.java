import static graphics.util.Matrix.getRotationMatrix33;
import static graphics.util.Renderer.*;
import graphics.util.Face;

import java.util.List;

import javax.media.opengl.GL2;

import static javax.media.opengl.GL2.*;


public class ItemBox
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/itemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;
	public static final int RESPAWN_TIME = 60;
	
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
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			displayTexturedObject(gl, BOX_FACES);
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}
	
	public void destroy() { respawnTimer = RESPAWN_TIME; }
	
	public boolean isDead() { return respawnTimer > 0; }
}