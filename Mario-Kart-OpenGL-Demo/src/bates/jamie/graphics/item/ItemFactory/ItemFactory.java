package bates.jamie.graphics.item.ItemFactory;

import javax.media.opengl.GL2;

import com.sun.tools.javac.util.List;

import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.entity.GrassPatch;
import bates.jamie.graphics.entity.Quadtree;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.BobOmb;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.FireBall;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.ItemBox;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.item.Shell;
import bates.jamie.graphics.particle.Blizzard;
import bates.jamie.graphics.particle.Blizzard.StormType;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.process.AmbientOccluder;
import bates.jamie.graphics.util.Vec3;

public class ItemFactory {

	
	public Item getItemType(String itemType, GL2 gl, Scene scene, Vehicle car, int id, float trajectory, boolean orbiting, Vec3 p, boolean dud) {
		
		if(itemType.equalsIgnoreCase("Banana")) {
			
			return new Banana(gl, scene, car, id);
		}
		
		
		else if(itemType.equalsIgnoreCase("BlueShell")) {
			
			return new BlueShell(gl, scene, car, trajectory);
		}
		
		
		else if(itemType.equalsIgnoreCase("BobOmb")) {
			
			return new BobOmb(p, car, dud);
		}

		else if(itemType.equalsIgnoreCase("FakeItemBox")) {
			
			return new FakeItemBox(gl, scene, car);
		}
		

		
		else if(itemType.equalsIgnoreCase("GreenShell")) {
			
			return new GreenShell(gl, scene, car, trajectory, orbiting);
		}
		
		
		else if(itemType.equalsIgnoreCase("RedShell")) {
			
			return new RedShell(gl, scene, car, trajectory, orbiting);
		}
		return null;

	}
	
	
	
}
