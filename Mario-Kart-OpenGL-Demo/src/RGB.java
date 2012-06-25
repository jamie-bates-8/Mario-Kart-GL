import java.awt.color.ColorSpace;

public class RGB
{
	public static int toRGB(int red, int green, int blue)
	{
		int color = red;
	    color = (color << 8) + green;
	    color = (color << 8) + blue;
	    
	    return color;
	}
	
	public static int getRed(int color) { return (color >> 16) & 0xFF; }
	
	public static int getGreen(int color) { return (color >> 8) & 0xFF; }
	
	public static int getBlue(int color) { return color & 0xFF; }
	
	public static int getIntensity(int color)
	{
		float red   = (float)   getRed(color) / 255;
		float green = (float) getGreen(color) / 255;
		float blue  = (float)  getBlue(color) / 255;
		
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
		
		float[] _color = cs.fromRGB(new float[] {red, green, blue});
		
		return (int) ((_color[0] + _color[1] + _color[2]) * 100 / 3);
	}
}