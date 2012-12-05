import javax.media.opengl.GL2;

public class Test
{
	public void startGameLoop(final GL2 gl)
	{
		//TODO Note: has concurrency issues (particle list) + objects tend to 'bounce' slightly
		
		new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
					//TODO GAME LOGIC
					
					try { Thread.sleep(17); /*Should be approximately 16.7 milliseconds per frame*/ }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
			}
		}).start();
	}
}
