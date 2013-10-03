package bates.jamie.graphics.scene;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Shader;

public class SceneGraph
{
	private SceneNode root;
	
	public SceneGraph(SceneNode root)
	{
		this.setRoot(root);
	}

	public SceneNode getRoot() { return root; }

	public void setRoot(SceneNode root) { this.root = root; }
	
	public void render(GL2 gl) { root.render(gl); }
	
	public void renderGhost(GL2 gl, float fade, Shader shader) { root.renderGhost(gl, fade, shader); }
	
	public void renderColor(GL2 gl, float[] color, Reflector reflector) { root.renderColor(gl, color, reflector); }
}
