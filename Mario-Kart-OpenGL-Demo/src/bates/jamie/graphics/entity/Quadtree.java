package bates.jamie.graphics.entity;

import static bates.jamie.graphics.util.Renderer.displayWireframeObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

import bates.jamie.graphics.util.RGB;

public class Quadtree
{
	public Quadtree north_west;
	public Quadtree north_east;
	public Quadtree south_west;
	public Quadtree south_east;
	
	float length;

	float[][] vertices  = new float[4][3];
	float[][] texCoords = new float[4][2];
	
	Texture texture;
	boolean textured = false;
	
	public boolean containsPoint(float[] p)
	{
		float x = p[0];
		float z = p[2];
		
		float _x = vertices[3][0];
		float _z = vertices[3][2];
		float x_ = vertices[1][0];
		float z_ = vertices[1][2];
		
		return ((x >= _x) && (x <= x_) && (z >= _z) && (z <= z_)); 
	}

	public boolean insert(float[] p)
	{
		// Ignore objects which do not belong in this quad tree
		if(containsPoint(p)) return false;

		// Otherwise, we need to subdivide then add the point to whichever node will accept it
		if(isLeaf()) subdivide();

		if(north_west.insert(p)) return true;
		if(north_east.insert(p)) return true;
		if(south_west.insert(p)) return true;
		if(south_east.insert(p)) return true;

		// Otherwise, the point cannot be inserted for some unknown reason (which should never happen)
		return false;
	}
	
	public void subdivide()
	{
		float _x = vertices[3][0];
		float _z = vertices[3][2];
		float x_ = vertices[1][0];
		float z_ = vertices[1][2];
		
		float _x_ = (_x + x_) / 2;
		float _z_ = (_z + z_) / 2;
		
		float[] north  = {_x_, 0, _z }; north [1] = getHeight(north );
		float[] east   = { x_, 0, _z_}; east  [1] = getHeight(east  );
		float[] south  = {_x_, 0,  z_}; south [1] = getHeight(south );
		float[] west   = {_x , 0, _z_}; west  [1] = getHeight(west  );
		float[] centre = {_x_, 0, _z_}; centre[1] = getHeight(centre);
		
		if(textured)
		{
			float _s = texCoords[3][0];
			float _t = texCoords[3][1];
			float s_ = texCoords[1][0];
			float t_ = texCoords[1][1];
			
			float _s_ = (_s + s_) / 2;
			float _t_ = (_t + t_) / 2;
			
			float[] tNorth  = {_s_, _t };
			float[] tEast   = { s_, _t_};
			float[] tSouth  = {_s_,  t_};
			float[] tWest   = {_s , _t_};
			float[] tCentre = {_s_, _t_};
			
			north_west = new Quadtree(new float[][] {west, centre, north, vertices[3]}, new float[][] {tWest, tCentre, tNorth, texCoords[3]});
			north_east = new Quadtree(new float[][] {centre, east, vertices[2], north}, new float[][] {tCentre, tEast, texCoords[2], tNorth});
			south_west = new Quadtree(new float[][] {vertices[0], south, centre, west}, new float[][] {texCoords[0], tSouth, tCentre, tWest});
			south_east = new Quadtree(new float[][] {south, vertices[1], east, centre}, new float[][] {tSouth, texCoords[1], tEast, tCentre});
		}
		else
		{
			north_west = new Quadtree(new float[][] {west, centre, north, vertices[3]});
			north_east = new Quadtree(new float[][] {centre, east, vertices[2], north});
			south_west = new Quadtree(new float[][] {vertices[0], south, centre, west});
			south_east = new Quadtree(new float[][] {south, vertices[1], east, centre});
		}	
	}
	
	public Quadtree(float[][] vertices)
	{
		this.vertices = vertices;
	}
	
	public Quadtree(float[][] vertices, float[][] texCoords)
	{
		this.vertices = vertices;
		this.texCoords = texCoords;
		
		textured = true;
	}
	
	public Quadtree(float[][] vertices, int levels)
	{
		this.vertices = vertices;
		
		subdivide(levels);
	}
	
	public Quadtree(float[][] vertices, int levels, Random generator)
	{
		this.vertices = vertices;
		
		subdivide(levels, generator);
	}
	
	public Quadtree(float[][] vertices, float[][] texCoords, Texture texture, int levels, Random generator)
	{
		this.vertices = vertices;
		this.texCoords = texCoords;
		
		this.texture = texture;
		textured = true;
		
		subdivide(levels, generator);
	}
	
	public void subdivide(int level)
	{
		subdivide();
		
		level--;
		
		if(level > 0)
		{
			north_west.subdivide(level);
			north_east.subdivide(level);
			south_west.subdivide(level);
			south_east.subdivide(level);
		}
	}
	
	public void subdivide(int level, Random generator)
	{
		subdivide();
		
		level--;
		
		if(level > 0)
		{
			if(generator.nextFloat() > 0.3) north_west.subdivide(level, generator);
			if(generator.nextFloat() > 0.3) north_east.subdivide(level, generator);
			if(generator.nextFloat() > 0.3) south_west.subdivide(level, generator);
			if(generator.nextFloat() > 0.3) south_east.subdivide(level, generator);
		}
	}
	
	public float getHeight(float[] p)
	{	
		float x = p[0];
		float z = p[2];
		
		float x1 = vertices[3][0];
		float z1 = vertices[3][2];
		float x2 = vertices[1][0];
		float z2 = vertices[1][2];
		
		float q11 = vertices[3][1];
		float q12 = vertices[0][1];
		float q21 = vertices[2][1];
		float q22 = vertices[1][1];

		float r1 = ((x2 - x) / (x2 - x1)) * q11 + ((x - x1) / (x2 - x1)) * q21;
		float r2 = ((x2 - x) / (x2 - x1)) * q12 + ((x - x1) / (x2 - x1)) * q22;
		
		return r1 * ((z2 -  z) / (z2 - z1)) + r2 * ((z  - z1) / (z2 - z1));
	}
	
	public void getVertices(List<float[]> _vertices)
	{
		if(isLeaf())
		{
			_vertices.add(vertices[0]);
			_vertices.add(vertices[1]);
			_vertices.add(vertices[2]);
			_vertices.add(vertices[3]);
		}
		else
		{
			north_west.getVertices(_vertices);
			north_east.getVertices(_vertices);
			south_west.getVertices(_vertices);
			south_east.getVertices(_vertices);
		}
	}
	
	public void getTexCoords(List<float[]> _texCoords)
	{
		if(isLeaf())
		{
			_texCoords.add(texCoords[0]);
			_texCoords.add(texCoords[1]);
			_texCoords.add(texCoords[2]);
			_texCoords.add(texCoords[3]);
		}
		else
		{
			north_west.getTexCoords(_texCoords);
			north_east.getTexCoords(_texCoords);
			south_west.getTexCoords(_texCoords);
			south_east.getTexCoords(_texCoords);
		}
	}
	
	public boolean isLeaf() { return north_west == null; }
	
	public void subdivideAll()
	{
		if(north_west.isLeaf()) north_west.subdivide(); else north_west.subdivideAll();
		if(north_east.isLeaf()) north_east.subdivide(); else north_east.subdivideAll();
		if(south_west.isLeaf()) south_west.subdivide(); else south_west.subdivideAll();
		if(south_east.isLeaf()) south_east.subdivide(); else south_east.subdivideAll();
	}
	
	public void decimateAll()
	{
		if(north_west.isLeaf() && north_east.isLeaf() && south_west.isLeaf() && south_east.isLeaf()) decimate();
		else
		{
		    if(!north_west.isLeaf()) north_west.decimateAll();
			if(!north_east.isLeaf()) north_east.decimateAll();
			if(!south_west.isLeaf()) south_west.decimateAll();
			if(!south_east.isLeaf()) south_east.decimateAll();
		}
			
//	    if(north_west.isLeaf())	north_west.decimate(); else north_west.decimateAll();
//		if(north_east.isLeaf()) north_east.decimate(); else north_east.decimateAll();
//		if(south_west.isLeaf()) south_west.decimate(); else south_west.decimateAll();
//		if(south_east.isLeaf()) south_east.decimate(); else south_east.decimateAll();
	}
	
	public void decimate()
	{
		north_west = null;
		north_east = null;
		south_west = null;
		south_east = null;
	}
	
	public static boolean wireframe = true;
	public static boolean display = true;
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			texture.bind(gl);
			
			List<float[]> _vertices = new ArrayList<float[]>();
			getVertices(_vertices);
			
			List<float[]> _texCoords = new ArrayList<float[]>();
			getTexCoords(_texCoords);
			
			gl.glColor3f(1, 1, 1);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			
			for(int i = 0; i < _vertices.size(); i += 4)
			{
				gl.glBegin(GL2.GL_QUADS);
				{
					for(int j = i; j < i + 4; j++)
					{
						gl.glTexCoord2f(_texCoords.get(j)[0], _texCoords.get(j)[1]);
						gl.glVertex3f(_vertices.get(j)[0], _vertices.get(j)[1], _vertices.get(j)[2]);
					}
				}
				gl.glEnd();
			}
		}
		gl.glPopMatrix();
	}
	
	public void renderWireframe(GL2 gl)
	{
		gl.glPushMatrix();
		{
			List<float[]> _vertices = new ArrayList<float[]>();
			getVertices(_vertices);
			
			float[][] __vertices = new float[_vertices.size()][3];
			
			for(int i = 0; i < __vertices.length; i++)
			{
				__vertices[i] = _vertices.get(i);
			}
			
			gl.glColor3f(0, 0, 0);
			gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			
			displayWireframeObject(gl, __vertices, 4, RGB.BLACK_3F);
			
			gl.glColor3f(1, 1, 1);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		}
		gl.glPopMatrix();
	}
}
