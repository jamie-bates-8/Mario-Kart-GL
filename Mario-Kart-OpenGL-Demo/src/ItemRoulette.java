import static javax.media.opengl.GL.GL_BLEND;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class ItemRoulette
{
	private static final int ROTATIONS = 3;
	private static final float ROTATION_SPEED = 5.0f;
	
	private Texture greenShellIcon;
	private Texture tripleGreenShellIcon;
	private Texture redShellIcon;
	private Texture tripleRedShellIcon;
	private Texture mushroomIcon;
	private Texture doubleMushroomIcon;
	private Texture tripleMushroomIcon;
	private Texture goldenMushroomIcon;
	private Texture fakeItemBoxIcon;
	private Texture bananaIcon;
	private Texture tripleBananaIcon;
	private Texture lightningBoltIcon;
	private Texture starIcon;
	private Texture booIcon;
	private Texture blueShellIcon;
	
	private Texture rouletteBorder;
	private Texture noItemIcon;
	
	private Texture[] itemIcons;
	
	private enum State {DEAD, SPINNING, STORING, ON_TIMER}
	private State state = State.DEAD;
	
	private float offset;
	private int itemID;
	private int rouletteID;
	public ItemState itemState;
	
	public int duration = 0;
	
	public int curseDuration = 0;
	public boolean cursed = false;
	
	public boolean secondary = false;
	
	public ItemRoulette()
	{
		itemState = ItemState.NO_ITEM;
		
		try
		{
			greenShellIcon       = TextureIO.newTexture(new File("tex/greenShellIcon.jpg"), true);
			tripleGreenShellIcon = TextureIO.newTexture(new File("tex/tripleGreenShellIcon.jpg"), true);
			redShellIcon         = TextureIO.newTexture(new File("tex/redShellIcon.jpg"), true);
			tripleRedShellIcon   = TextureIO.newTexture(new File("tex/tripleRedShellIcon.jpg"), true);
			mushroomIcon         = TextureIO.newTexture(new File("tex/mushroomIcon.png"), true);
			doubleMushroomIcon   = TextureIO.newTexture(new File("tex/doubleMushroomIcon.jpg"), true);
			tripleMushroomIcon   = TextureIO.newTexture(new File("tex/tripleMushroomIcon.jpg"), true);
			goldenMushroomIcon   = TextureIO.newTexture(new File("tex/goldenMushroomIcon.jpg"), true);
			fakeItemBoxIcon      = TextureIO.newTexture(new File("tex/fakeItemBoxIcon.jpg"), true);
			bananaIcon           = TextureIO.newTexture(new File("tex/bananaIcon.jpg"), true);
			tripleBananaIcon     = TextureIO.newTexture(new File("tex/tripleBananaIcon.jpg"), true);
			lightningBoltIcon    = TextureIO.newTexture(new File("tex/lightningBoltIcon.jpg"), true);
			starIcon             = TextureIO.newTexture(new File("tex/starIcon.jpg"), true);
			booIcon              = TextureIO.newTexture(new File("tex/booIcon.jpg"), true);
			blueShellIcon        = TextureIO.newTexture(new File("tex/blueShellIcon.jpg"), true);
			
			rouletteBorder       = TextureIO.newTexture(new File("tex/rouletteBorder.png"), true);
			noItemIcon           = TextureIO.newTexture(new File("tex/noItemIcon.png"), true);
		}
		catch (IOException e) { e.printStackTrace(); }
		
		itemIcons = new Texture[]
			{greenShellIcon, tripleGreenShellIcon, redShellIcon, tripleRedShellIcon,
			 mushroomIcon, tripleMushroomIcon, goldenMushroomIcon, fakeItemBoxIcon,
			 bananaIcon, tripleBananaIcon, lightningBoltIcon, starIcon, booIcon,
			 blueShellIcon};
	}
	
	public void spin()
	{
		offset = 0;
		itemID = selectItem();
		rouletteID = itemID;
		state = State.SPINNING;
		itemState = ItemState.get(itemID);
	}
	
	public void next()
	{
		if(!isSpinning())
		{
			offset = 0;
			itemID = (itemID + 1) % itemIcons.length;
			rouletteID = ROTATIONS * itemIcons.length + itemID;
			state = State.STORING;
			itemState = ItemState.get(itemID);
		}
	}
	
	public void previous()
	{
		if(!isSpinning())
		{
			offset = 0;
			itemID--;
			if(itemID < 0) itemID = itemIcons.length - 1;
			rouletteID = ROTATIONS * itemIcons.length + itemID;
			state = State.STORING;
			itemState = ItemState.get(itemID);
		}
	}
	
	public void repeat()
	{
		if(!isSpinning())
		{
			offset = 0;
			rouletteID = ROTATIONS * itemIcons.length + itemID;
			state = State.STORING;
			itemState = ItemState.get(itemID);
		}
	}
	
	public int selectItem()
	{	
		Random generator = new Random();
		
		double[] weights = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
	
		double total = 0.0;
		
		for (double weight : weights) total += weight;
	
		int randomIndex = -1;
		double random = generator.nextDouble() * total;
	
		for (int j = 0; j < weights.length; j++)
		{
			random -= weights[j];
			if (random <= 0.0)
			{
				randomIndex = j;
				break;
			}
		}
		
		return randomIndex;
	}
	
	public void render(GL2 gl)
	{
		float yT = 1 - offset;
		float yV = 20 + 100 * offset;
		
		if(offset > 0)
		{
			itemIcons[(rouletteID + 1) % itemIcons.length].bind(gl);
			
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0, yT); gl.glVertex2f( 20, 20);
				gl.glTexCoord2f(1, yT); gl.glVertex2f(120, 20);
				gl.glTexCoord2f(1,  1); gl.glVertex2f(120, yV);
				gl.glTexCoord2f(0,  1); gl.glVertex2f( 20, yV);
			}
			gl.glEnd();
		}
			
		if(isSpinning()) itemIcons[rouletteID % itemIcons.length].bind(gl);
		else bindIcon(gl);

		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glTexCoord2f(0,  0); gl.glVertex2f( 20,  yV);
			gl.glTexCoord2f(1,  0); gl.glVertex2f(120,  yV);
			gl.glTexCoord2f(1, yT); gl.glVertex2f(120, 120);
			gl.glTexCoord2f(0, yT); gl.glVertex2f( 20, 120);
		}
		gl.glEnd();
		
		if(cursed)
		{
			noItemIcon.bind(gl);
			
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0, 0); gl.glVertex2f( 20,  20);
				gl.glTexCoord2f(1, 0); gl.glVertex2f(120,  20);
				gl.glTexCoord2f(1, 1); gl.glVertex2f(120, 120);
				gl.glTexCoord2f(0, 1); gl.glVertex2f( 20, 120);
			}
			gl.glEnd();
			
			gl.glDisable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		}
		
		rouletteBorder.bind(gl);
		
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glTexCoord2f(0, 0); gl.glVertex2f( 10,  10);
			gl.glTexCoord2f(1, 0); gl.glVertex2f(130,  10);
			gl.glTexCoord2f(1, 1); gl.glVertex2f(130, 130);
			gl.glTexCoord2f(0, 1); gl.glVertex2f( 10, 130);
		}
		gl.glEnd();
		
		gl.glDisable(GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		if(rouletteID < ROTATIONS * itemIcons.length + itemID)
		{
			offset += ROTATION_SPEED / (rouletteID + 1);
			if(offset > 1.0)
			{
				offset = 0;
				rouletteID++;
			}
		}
		else if(state != State.ON_TIMER) state = State.STORING;
		
		if(duration > 0) duration--;
		else if(state == State.ON_TIMER) state = State.DEAD;
	}
	
	public boolean isAlive()    { return state != State.DEAD;     }
	public boolean isSpinning() { return state == State.SPINNING; }
	public boolean hasItem()    { return state == State.STORING;  }
	
	public int getItem() { return itemID; }
	
	public void update()
	{	
		if(!ItemState.isMultipleUse(itemState)) state = State.DEAD;
		else itemState = ItemState.press(itemState);
	}
	
	public void bindIcon(GL2 gl)
	{
		switch(itemState)
		{
			case THREE_ORBITING_GREEN_SHELLS: tripleGreenShellIcon.bind(gl); break;
			case HOLDING_GREEN_SHELL: 		  greenShellIcon.bind(gl); break;
			case HOLDING_RED_SHELL:           redShellIcon.bind(gl); break;
			case THREE_ORBITING_RED_SHELLS:   tripleRedShellIcon.bind(gl); break;
			case THREE_MUSHROOMS: 			  tripleMushroomIcon.bind(gl); break;
			case TWO_MUSHROOMS: 			  doubleMushroomIcon.bind(gl); break;
			case ONE_MUSHROOM: 				  mushroomIcon.bind(gl); break;
			case GOLDEN_MUSHROOM:			  goldenMushroomIcon.bind(gl); break;
			case FAKE_ITEM_BOX: 			  fakeItemBoxIcon.bind(gl); break;
			case HOLDING_BANANA:			  bananaIcon.bind(gl); break;
			case THREE_BANANAS:				  tripleBananaIcon.bind(gl); break;
			case LIGHTNING_BOLT:			  lightningBoltIcon.bind(gl); break;
			case POWER_STAR:				  starIcon.bind(gl); break;
			case BOO:						  booIcon.bind(gl); break;
			case BLUE_SHELL:                  blueShellIcon.bind(gl); break;
				
			default: break; 
		}
	}
	
	public void destroy() { state = State.DEAD; }
	
	public void setTimer() { duration = 400; state = State.ON_TIMER; }
 }
