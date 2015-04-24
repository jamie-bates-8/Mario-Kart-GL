package bates.jamie.graphics.util;

import java.util.ArrayList;
import java.util.List;

import static bates.jamie.graphics.util.Vector.*;

public class Gradient
{
	private List<Stop> stops = new ArrayList<Stop>();
	
	public static final Gradient GRAYSCALE = new Gradient(RGB.WHITE, RGB.GRAY);
	public static final Gradient MUD       = new Gradient(new float[][] {RGB.WHITE, RGB.LIGHT_BROWN, RGB.DARK_BROWN}); 
	public static final Gradient TROPICAL  = new Gradient(new float[][] {RGB.WHITE, RGB.BLUE,        RGB.INDIGO    }); 
	
	public Gradient(float[] color1, float[] color2)
	{
		stops.add(new Stop(  0, color1));
		stops.add(new Stop(100, color2));
	}
	
	public Gradient(float[] color1, float[] color2, float[] color3)
	{
		stops.add(new Stop(  0, color1));
		stops.add(new Stop( 50, color2));
		stops.add(new Stop(100, color3));
	}
	
	public Gradient(float[][] colors)
	{
		float increment = 100 / (colors.length - 1);
		
		for(int i = 0; i < colors.length; i++)
			stops.add(new Stop((int) increment * i, colors[i]));
	}
	
	public void addStop(int location, float[] color)
	{
		int index = 0;
		
		while(stops.get(index).location < location) index++;
		
		stops.add(index, new Stop(location, color));
	}
	
	public float[] getColor(int location)
	{
		int index = 0;
		
		while(stops.get(index + 1).location < location) index++;
		
		// index <-- lower -- location -- upper --> index + 1
		int lower = location - stops.get(index).location;
		int upper = stops.get(index + 1).location - location;
		
		if(lower < upper) return stops.get(index).color;
		else return stops.get(index + 1).color;
	}
	
	public float[][] getColors(int location)
	{
		int index = 0;
		
		while(stops.get(index + 1).location < location) index++;
		
		return new float[][] {stops.get(index).color, stops.get(index + 1).color};
	}
	
	public float[] interpolate(int location)
	{
		if(location > 100) location = 100;
		if(location <   0) location =   0;
		
		int index = 0;
		
		while(stops.get(index + 1).location < location) index++;
		
		Stop lower_stop = stops.get(index);
		Stop upper_stop = stops.get(index + 1);
		
		int location0 = lower_stop.location; // x interpolation
		int location1 = upper_stop.location;
		
		float fraction = (float) (location - location0) / (location1 - location0);
		
		float[] color0 = lower_stop.color;
		float[] color1 = upper_stop.color;
		
		float[] color = {0, 0, 0};
		
		for(int i = 0; i < 3; i++)
			color[i] = color0[i] + (color1[i] - color0[i]) * fraction;
		
		return color;
	}
	
	public float[] interpolate(double location)
	{	
		return interpolate((int) (location * 100));
	}
	
	public float[] getColor(double location)
	{
		return getColor((int) (location * 100));
	}
	
	private class Stop
	{
		private int location;
		private float[] color;
		
		public Stop(int location, float[] color)
		{
			this.location = location;
			
			if(color[0] > 1 || color[1] > 1 || color[2] > 1)
				color = multiply(color, 1.0f / 255);
			
			this.color = color;	
		}
	}
}
