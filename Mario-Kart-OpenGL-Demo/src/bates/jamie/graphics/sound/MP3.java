package bates.jamie.graphics.sound;

import javax.media.*;

import bates.jamie.graphics.scene.Scene;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MP3 extends Thread
{
	private Scene c;
	private URL url;
	private MediaLocator mediaLocator;
	private Player playMP3;
	
	public MP3(Scene c, String file)
	{
		this.c = c;
		
		try { this.url = new URL(file); }
		catch(MalformedURLException e) { e.printStackTrace(); }
	}
	
	public void run()
	{
		try
		{
			mediaLocator = new MediaLocator(url);
			playMP3 = Manager.createPlayer(mediaLocator);
		}
		catch(IOException e) { e.printStackTrace(); }
		catch(NoPlayerException e) { e.printStackTrace(); }
		
		playMP3.addControllerListener(new ControllerListener()
		{
			public void controllerUpdate(ControllerEvent e)
			{
				if (e instanceof EndOfMediaEvent)
				{
					playMP3.stop();
					playMP3.close();
					c.stopMusic();
				}
			}
		});
		playMP3.realize();
		playMP3.start();
	}
}
