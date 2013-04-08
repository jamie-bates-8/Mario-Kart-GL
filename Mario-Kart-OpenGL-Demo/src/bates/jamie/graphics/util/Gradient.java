package bates.jamie.graphics.util;

import java.util.ArrayList;
import java.util.List;

import static bates.jamie.graphics.util.Vector.*;

public class Gradient
{
	private List<Stop> stops = new ArrayList<Stop>();
	
	public static final Gradient GRAYSCALE = new Gradient(RGB.WHITE_3F, RGB.GRAY);
	public static final Gradient MUD = new Gradient(new float[][] {RGB.WHITE_3F, RGB.LIGHT_BROWN, RGB.DARK_BROWN}); 
	
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
	
	public float[] interpolate(int location)
	{
		if(location > 100) location = 100;
		if(location <   0) location =   0;
		
		int index = 0;
		
		while(stops.get(index + 1).location < location) index++;
		
		Stop _stop = stops.get(index);
		Stop stop_ = stops.get(index + 1);
		
		int _location = _stop.location;
		int location_ = stop_.location;
		
		// index <-- lower -- location -- upper --> index + 1
		int lower = location  - _location;
		int upper = location_ -  location;
		
		float[] _color = _stop.color;
		float[] color_ = stop_.color;
		
		float[] color = {0, 0, 0};
		
		for(int i = 0; i < 3; i++)
		{
			color[i] += ((float) upper / (location_ - _location)) * _color[i];
			color[i] += ((float) lower / (location_ - _location)) * color_[i];
		}
		
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
