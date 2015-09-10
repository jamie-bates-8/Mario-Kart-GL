package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

public class WarpPipe
{
	static Model pipe_model = new Model("pipe");
//	static Model pipe_model = OBJParser.parseTriangleMesh("glass_bottle");
	
	SceneNode pipeNode;
	
	public Reflector reflector;
	
	float rotation = 0;
	Vec3  position;
	
	boolean clear = false;
	
	public WarpPipe(Vec3 p)
	{
		position = p;
		
		reflector = new Reflector(1.0f);
		
		pipeNode = new SceneNode(pipe_model);
		pipeNode.setTranslation(p);
		pipeNode.setScale(new Vec3(2.0));
		pipeNode.setReflector(reflector);
		pipeNode.setReflectivity(0.75f);
		pipeNode.setRenderMode(RenderMode.COLOR);
		pipeNode.setColor(new float[] {0.4f, 1.0f, 0.4f});
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		pipeNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{		
		float   rimPower = Light.rimPower;
		float[] rimColor = Light.rimColor;
		
		Light.rimPower = 1.0f;
		Light.rimColor = new float[] {.7f, .7f, .7f};
		
		Light.setepRimLighting(gl);
		
		if(!(BloomStrobe.opaqueMode && clear))
		{
			pipeNode.setRotation(new Vec3(0, rotation, 0));
			
			if(clear) pipeNode.renderGhost(gl, 1, Shader.get("invisible"));
			else      pipeNode.render(gl);
		}
		
		Light.rimPower = rimPower;
		Light.rimColor = rimColor;
		
		Light.setepRimLighting(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}

	public boolean isClear() { return clear; }
	
	public void setClear(boolean clear) { this.clear = clear; }
	
	
}
