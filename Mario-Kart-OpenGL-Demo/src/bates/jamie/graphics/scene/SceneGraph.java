package bates.jamie.graphics.scene;

import javax.media.opengl.GL2;

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
	
	public void renderGhost(GL2 gl, float fade) { root.renderGhost(gl, fade); }
	
	public void renderColor(GL2 gl, float[] color) { root.renderColor(gl, color); }
}
