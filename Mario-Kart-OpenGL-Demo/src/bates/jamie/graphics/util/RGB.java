package bates.jamie.graphics.util;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class RGB
{
	public static final float[] DARK_RED    = {153,   0,   0};
	public static final float[] RED         = {237,  28,  36};
	public static final float[] ORANGE      = {242, 101,  34};
	public static final float[] YELLOW      = {255, 228,   0};
	public static final float[] LIME_GREEN  = { 50, 205,  50};
	public static final float[] GREEN       = { 57, 180,  74};
	public static final float[] BLUE        = {  0, 173, 239};
	public static final float[] INDIGO      = {  0, 114, 188};
	public static final float[] VIOLET      = {102,  45, 145};
	public static final float[] PLUM        = {221, 160, 221};
	public static final float[] WHITE       = {255, 255, 255};
	public static final float[] GRAY        = {127, 127, 127};
	public static final float[] DARK_GRAY   = { 64,  64,  64};
	public static final float[] BLACK       = {  0,   0,   0};
	public static final float[] SKY_BLUE    = { 30, 144,   1};

	public static final float[] SKY_BLUE_3F = {0.118f, 0.565f, 1.000f};

	public static final float[] DARK_BROWN  = { 44,  29,   9};
	public static final float[] LIGHT_BROWN = { 93,  63,  19};

	public static final float[] BLACK_3F      = {0, 0, 0};
	public static final float[] PURE_RED_3F   = {1, 0, 0};
	public static final float[] PURE_GREEN_3F = {0, 1, 0};
	public static final float[] PURE_BLUE_3F  = {0, 0, 1};
	public static final float[] WHITE_3F      = {1, 1, 1};

	public static void main(String[] args)
	{
		System.out.println("vec3" + Vector.print(Vector.multiply(DARK_RED, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(RED, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(ORANGE, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(YELLOW, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(LIME_GREEN, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(GREEN, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(BLUE, 1.0f / 255)) + ",");
		System.out.println("vec3" + Vector.print(Vector.multiply(VIOLET, 1.0f / 255)) + ",");
	}

	public static float[] toRGBAi(float[] color, float alpha)
	{
		return new float[] {color[0]/255, color[1]/255, color[2]/255, alpha};
	}

	public static float[] toRGBAf(float[] color, float alpha)
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