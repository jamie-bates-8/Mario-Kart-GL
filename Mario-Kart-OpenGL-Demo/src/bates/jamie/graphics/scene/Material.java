package bates.jamie.graphics.scene;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;


public class Material
{
	// Material properties
	private float[] diffuse   = {1.0f, 1.0f, 1.0f};
	private float[] specular  = {0.4f, 0.4f, 0.4f};
	private int     shininess = 50;
	
	//Texture properties
	private Texture diffuseMap;
	private Texture normalMap;
	private Texture heightMap;
	private Texture alphaMask;
	private Texture specularMap;
	
	public Material() {}
	
	public Material(float[] diffuse, float[] specular, int shininess)
	{
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
	}
	
	public Material(Texture texture) { diffuseMap = texture; }
	
	public Material(Texture diffuse, Texture normal, Texture height, Texture specular)
	{
		diffuseMap = diffuse;
		normalMap = normal;
		heightMap = height;
		specularMap = specular;
	}
	
	public Material(Material material)
	{
		diffuse = material.getDiffuse();
		specular = material.getSpecular();
		shininess = material.getShininess();
		
		diffuseMap = material.diffuseMap;
		normalMap = material.normalMap;
		heightMap = material.heightMap;
		alphaMask = material.alphaMask;
		specularMap = material.specularMap;
	}

	public float[] getDiffuse() { return diffuse; }
	public float[] getSpecular() { return specular; }
	public int     getShininess() { return shininess; }
	
	public void setDiffuse(float[] diffuse) { this.diffuse = diffuse; }
	public void setSpecular(float[] specular) { this.specular = specular; }
	public void setShininess(int s)
	{
		shininess = s;
		
		if(shininess <   0) shininess =   0;
		if(shininess > 128) shininess = 128;
	}
	
	public void setDiffuseMap (Texture diffuseTexture ) { diffuseMap = diffuseTexture; }
	public void setNormalMap  (Texture normalTexture  ) { normalMap = normalTexture ; }
	public void setHeightMap  (Texture heightTexture  ) { heightMap = heightTexture ; }
	public void setAlphaMap   (Texture alphaTexture   ) { alphaMask = alphaTexture  ; }
	public void setSpecularMap(Texture specularTexture) { specularMap = specularTexture; }
	
	public Texture getDiffuseMap() { return diffuseMap; }
	
	public void load(GL2 gl)
	{
		gl.glColor3fv(diffuse, 0);
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);
		gl.glMateriali(GL_FRONT, GL_SHININESS, shininess);
		
		if(specularMap != null) { gl.glActiveTexture(GL2.GL_TEXTURE4); specularMap.bind(gl); }
		if(alphaMask != null) { gl.glActiveTexture(GL2.GL_TEXTURE3); alphaMask.bind(gl); }
		if(heightMap != null) { gl.glActiveTexture(GL2.GL_TEXTURE2); heightMap.bind(gl); }
		if(normalMap != null) { gl.glActiveTexture(GL2.GL_TEXTURE1); normalMap.bind(gl); }
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		if(diffuseMap != null) diffuseMap.bind(gl);
	}
	
	public static void loadDefault(GL2 gl)
	{
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, new float[] {.4f, .4f, .4f}, 0);
		gl.glMateriali(GL_FRONT, GL_SHININESS, 50);
	}
}
