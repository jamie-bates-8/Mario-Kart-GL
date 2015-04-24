package bates.jamie.graphics.util;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class RGB
{
	public static final float[] BLACK         = {0.000f, 0.000f, 0.000f};
	public static final float[] DARK_GRAY     = {0.251f, 0.251f, 0.251f};
	public static final float[] GRAY          = {0.498f, 0.498f, 0.498f};
	public static final float[] WHITE         = {1.000f, 1.000f, 1.000f};
	
	public static final float[] DARK_RED      = {0.600f, 0.000f, 0.000f};
	public static final float[] RED           = {0.929f, 0.110f, 0.141f};
	public static final float[] ORANGE        = {0.949f, 0.396f, 0.133f};
	public static final float[] YELLOW        = {1.000f, 0.894f, 0.000f};
	public static final float[] LIME_GREEN    = {0.196f, 0.804f, 0.196f};
	public static final float[] GREEN         = {0.224f, 0.706f, 0.290f};
	public static final float[] SKY_BLUE      = {0.118f, 0.565f, 1.000f};
	public static final float[] BLUE          = {0.000f, 0.678f, 0.937f};
	public static final float[] INDIGO        = {0.000f, 0.447f, 0.737f};
	public static final float[] VIOLET        = {0.400f, 0.176f, 0.569f};
	public static final float[] PLUM          = {0.867f, 0.627f, 0.867f};

	public static final float[] DARK_BROWN    = {0.173f, 0.114f, 0.035f};
	public static final float[] LIGHT_BROWN   = {0.365f, 0.247f, 0.075f};
	
	public static final float[] BRIGHT_RED    = {1.000f, 0.275f, 0.275f};
	public static final float[] BRIGHT_YELLOW = {1.000f, 1.000f, 0.500f};
	public static final float[] BRIGHT_GREEN  = {0.631f, 0.918f, 0.392f};
	public static final float[] BRIGHT_BLUE   = {0.541f, 0.925f, 1.000f};
	
	public static final float[] PURE_RED      = {1.000f, 0.000f, 0.000f};
	public static final float[] PURE_GREEN    = {0.000f, 1.000f, 0.000f};
	public static final float[] PURE_BLUE     = {0.000f, 0.000f, 1.000f};

	public static float[] toRGBA(float[] color, float alpha)
	{
		return new float[] {color[0], color[1], color[2], alpha};
	}

	public static int toRGB(int red, int green, int blue)
	{
		int color = red;
		color = (color << 8) + green;
		color = (color << 8) + blue;

		return color;
	}

	public static float[] toRGBA(Color color)
	{
		float r = color.getRed();
		float g = color.getGreen();
		float b = color.getBlue();
		float a = color.getAlpha();

		float[] _color = {r/255, g/255, b/255, a/255};

		return _color;
	}

	public static float[] toRGB(Color color)
	{
		float r = color.getRed();
		float g = color.getGreen();
		float b = color.getBlue();

		float[] _color = {r/255, g/255, b/255};

		return _color;
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
	
	public static float[] HSVToRGB(float[] hsv)
	{
		Vec3 k = new Vec3(1.0f, 2.0f / 3.0f, 1.0f / 3.0f);
		
		Vec3 p = new Vec3(hsv[0]).add(k);
		p = p.fract().multiply(6).subtract(new Vec3(3.0));
		p = p.absolute();
		
		p = p.subtract(new Vec3(k.x)).clamp(0, 1);
		
		return Vec3.mix(new Vec3(k.x), p, hsv[1]).multiply(hsv[2]).toArray();
	}

	public static float[] HSLtoRGB(float hue, float saturation, float luminosity)
	{
		float v;
		float r,g,b;

		r = luminosity; // default to gray
		g = luminosity;
		b = luminosity;

		v = (luminosity <= 0.5f) ? (luminosity * (1.0f + saturation)) :
			(luminosity + saturation - luminosity * saturation);

		if (v > 0)
		{
			float m;
			float sv;
			int sextant;
			float fract, vsf, mid1, mid2;

			m = luminosity + luminosity - v;
			sv = (v - m ) / v;
			hue *= 6.0;
			sextant = (int) hue;
			fract = hue - sextant;
			vsf = v * sv * fract;
			mid1 = m + vsf;
			mid2 = v - vsf;
			
			switch (sextant)
			{
				case 0:
					r = v;
					g = mid1;
					b = m;
					break;
				case 1:
					r = mid2;
					g = v;
					b = m;
					break;
				case 2:
					r = m;
					g = v;
					b = mid1;
					break;
				case 3:
					r = m;
					g = mid2;
					b = v;
					break;
				case 4:
					r = mid1;
					g = m;
					b = v;
					break;
				case 5:
					r = v;
					g = m;
					b = mid2;
					break;
			}
		}

		return new float[] {r, g, b};
	}
}