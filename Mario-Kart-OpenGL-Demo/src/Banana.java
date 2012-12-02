import static graphics.util.Matrix.getRotationMatrix;
import static graphics.util.Renderer.displayGradientObject;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import graphics.util.Face;
import graphics.util.Gradient;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class Banana extends Item
{
	private static final List<Face> BANANA_FACES = OBJParser.parseTriangles("obj/banana.obj");
	
	private static final float[] BROWN  = {100,  50,   0};
	private static final float[] YELLOW = {255, 255,  75};
	private static final float[] GREEN  = {100, 255,   0};
	
	private static int bananaList = -1;
	
	private static Texture face;
	private static Gradient gradient;
	
	static
	{
		try { face = TextureIO.newTexture(new File("tex/bananaFace.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
		
		gradient = new Gradient(YELLOW, BROWN);
		gradient.addStop(30, YELLOW);
		gradient.addStop(90, GREEN);
	}
	
	public static final float RADIUS = 1.8f;
	
	private int bananaID = 0;
	
	public Banana(GL2 gl, Scene scene, Car car, int id)
	{
		if(bananaList == -1)
		{
			bananaList = gl.glGenLists(1);
			gl.glNewList(bananaList, GL2.GL_COMPILE);
			displayGradientObject(gl, BANANA_FACES, gradient, -1.35f, 1.41f);
		    gl.glEndList();
		    
		    System.out.println("Banana: " + BANANA_FACES.size() + " faces");
		}
		
		this.scene = scene;
	    this.car = car;
	    
	    bananaID = id;
		
		bound = new Sphere(new float[] {0, 0, 0}, RADIUS);
	}
	
	public Banana(Scene scene, float[] c)
	{
		this.scene = scene;
		
		bound = new Sphere(c, RADIUS);
	}
	
	@Override
	public void rebound(Bound b)
	{
		super.rebound(b);
		velocity /= 2;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			if(thrown) gl.glRotatef(trajectory, 0, 1, 0);
			else gl.glMultMatrixf(getRotationMatrix(u), 0);
			gl.glScalef(1.0f, 1.0f, 1.0f);
			
			gl.glCallList(bananaList);
			
			gl.glPushMatrix();
			{
				gl.glDisable(GL_LIGHTING);
				gl.glEnable(GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
				
				gl.glTranslatef(0.325f, 0, 0);
				gl.glRotatef(90, 0, 1, 0);
				
				face.bind(gl);

				gl.glBegin(GL_QUADS);
				{
					gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.35f, -0.35f, 0.0f);
					gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.35f,  0.35f, 0.0f);
					gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.35f,  0.35f, 0.0f);
					gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.35f, -0.35f, 0.0f);
				}
				gl.glEnd();
				
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
				gl.glDisable(GL_BLEND);
				gl.glEnable(GL_LIGHTING);
			}
			gl.glPopMatrix();
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		if(thrown && falling) setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();

		setRotation(getRotationAngles(getHeights()));
		if(thrown) setRotation(0, trajectory, -45);
	}
	
	@Override
	public void hold()
	{
		setPosition(car.getBackwardItemVector(this, bananaID));
		trajectory = car.trajectory;
	}
	
	@Override
	public boolean canCollide(Item item)
	{
		if(item instanceof Shell) return true;
		
		return false;
	}
	
	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell)
		{
			this.destroy();
			item.destroy();
		}
	}
	
	@Override
	public void collide(Car car)
	{
		car.spin();
		destroy();
	}
	
	@Override
	public float getMaximumExtent() { return bound.getMaximumExtent() * 0.85f; }
}
