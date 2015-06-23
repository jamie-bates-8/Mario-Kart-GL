package bates.jamie.graphics.particle;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import bates.jamie.graphics.particle.ParticleGenerator.GeneratorType;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec3;

public class FirePit
{
	ParticleGenerator generator;
	
	ArrayList<Particle> particles = new ArrayList<Particle>();
	
	public FirePit(Vec3 position)
	{
		generator = new ParticleGenerator(0, 5, GeneratorType.FIRE, position);
	}
	
	public void update()
	{
		particles.addAll(generator.generate());
		for(Particle p : particles) p.update();
		Particle.removeParticles(particles);
	}
	
	public void render(GL2 gl)
	{
		if(Scene.testMode) FireParticle.renderList(gl, particles);
		else for(Particle p : particles) p.render(gl, 0);
	}
}
