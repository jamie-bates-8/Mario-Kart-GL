
import java.awt.color.ColorSpace;

public class RGB
{
	public static final float[] DARK_RED   = {153.0f,   0.0f,   0.0f};
	public static final float[] RED        = {237.0f,  28.0f,  36.0f};
	public static final float[] ORANGE     = {242.0f, 101.0f,  34.0f};
	public static final float[] YELLOW     = {255.0f, 228.0f,   0.0f};
	public static final float[] LIME_GREEN = { 50.0f, 205.0f,  50.0f};
	public static final float[] GREEN      = { 57.0f, 180.0f,  74.0f};
	public static final float[] BLUE       = {  0.0f, 173.0f, 239.0f};
	public static final float[] INDIGO     = {  0.0f, 114.0f, 188.0f};
	public static final float[] VIOLET     = {102.0f,  45.0f, 145.0f};
	public static final float[] PLUM       = {221.0f, 160.0f, 221.0f};
	public static final float[] WHITE      = {255.0f, 255.0f, 255.0f};
	public static final float[] GRAY       = {127.0f, 127.0f, 127.0f};
	public static final float[] DARK_GRAY  = { 64.0f,  64.0f,  64.0f};
	public static final float[] BLACK      = {  0.0f,   0.0f,   0.0f};
	
	public static final float[] BLACK_3F = {0, 0, 0};
	public static final float[] RED_3F   = {1, 0, 0};
	public static final float[] GREEN_3F = {0, 1, 0};
	public static final float[] BLUE_3F  = {0, 0, 1};
	public static final float[] WHITE_3F = {1, 1, 1};
	
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
	
	public static float getIntensity(int color)
	{
		float red   = (float)   getRed(color) / 255;
		float green = (float) getGreen(color) / 255;
		float blue  = (float)  getBlue(color) / 255;
		
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
		
		float[] _color = cs.fromRGB(new float[] {red, green, blue});
		
		return (_color[0] + _color[1] + _color[2]) / 3;
	}
}