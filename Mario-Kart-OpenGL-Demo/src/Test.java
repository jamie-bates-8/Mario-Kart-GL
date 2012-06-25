import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class Test
{
	private BufferedImage heightMap;
	private static final float MAX_HEIGHT = 0.0f;
	
	private BufferedImage motionLog;
	private static final int LOG_SCALE = 1;
	
	private OBB b;
	
	private boolean colliding;
	
	public float[] color = {255, 0, 0};
	public static final int COLOR_INCREMENT = 17; //1, 3, 5, 15, 17, 51, 85, 255
	
	public void testCollisionDetection()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		float[] p = b.getPosition();
		
		for(int a = 0; a < 360; a += 5)
		{
			System.out.println(a);
			
			for(double i = -100; i < 100; i += 0.5)
			{
				for(double j = -100; j < 100; j += 0.5)
				{
					b.c[0] = (float) i;
					b.c[2] = (float) j;
					b.setRotation(0, a, 0);
					
					colliding = false;
					
					for(Bound bound : bounds)
						if(b.testBound(bound)) colliding = true;

					recordCollision();
				}
			}
		}
		
		for(Bound bound : bounds)
		{
			List<float[]> vertices = bound.getPixelMap();
			
			for(float[] v : vertices)
			{	
				int xCentre = (heightMap.getHeight() / 2);
				int zCentre = (heightMap.getWidth() / 2);
					
				int x = (int) (xCentre - ((v[0] / 120) * xCentre));
				int z = (int) (zCentre - ((v[2] / 120) * zCentre));
		
				try { motionLog.setRGB(x / LOG_SCALE, z / LOG_SCALE, Color.GREEN.getRGB()); }
				catch(Exception e) {}
			}
		}
	}
	
	
	public void recordCollision()
	{		
		int xCentre = (heightMap.getHeight() / 2);
		int zCentre = (heightMap.getWidth() / 2);
			
		int x = (int) (xCentre - ((b.c[0] / 120) * xCentre));
		int z = (int) (zCentre - ((b.c[2] / 120) * zCentre));

		try
		{
			if(motionLog.getRGB(x / LOG_SCALE, z / LOG_SCALE) != Color.RED.getRGB())
			{
				if(colliding) motionLog.setRGB(x / LOG_SCALE, z / LOG_SCALE, Color.RED.getRGB());
				else motionLog.setRGB(x / LOG_SCALE, z / LOG_SCALE, Color.BLACK.getRGB());
			}
		}
		catch(Exception e) {}
	}
	
	private void displayMotionLog()
	{
		JFrame record = new JFrame();
		
		int height = heightMap.getHeight() / LOG_SCALE;
		int width = heightMap.getWidth() / LOG_SCALE;
		
		ImageIcon image = new ImageIcon(motionLog);
		JLabel label = new JLabel(image);
		
		record.setPreferredSize(new Dimension(width, height));
		record.add(label);
		record.pack();
		record.setVisible(true);
	}
	
	private void cycleColor()
	{
		     if(color[0] == 255 && color[1] != 255 && color[2] ==   0) color[1] += COLOR_INCREMENT; //255,   0,   0
		else if(color[0] !=   0 && color[1] == 255 && color[2] ==   0) color[0] -= COLOR_INCREMENT; //255, 255,   0
		else if(color[0] ==   0 && color[1] == 255 && color[2] != 255) color[2] += COLOR_INCREMENT; //  0, 255,   0
		else if(color[0] ==   0 && color[1] !=   0 && color[2] == 255) color[1] -= COLOR_INCREMENT; //  0, 255, 255
		else if(color[0] != 255 && color[1] ==   0 && color[2] == 255) color[0] += COLOR_INCREMENT; //  0,   0, 255
		else if(color[0] == 255 && color[1] ==   0 && color[2] !=   0) color[2] -= COLOR_INCREMENT; //255,   0, 255
	}
}
