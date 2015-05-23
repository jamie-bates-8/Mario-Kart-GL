package bates.jamie.graphics.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bates.jamie.graphics.scene.Model;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class OBJParser
{
	/**
	 * @author Matthew Bates (modified by Jamie Bates)
	 * @version 1.0
	 * 
	 * This method parses a Wavefront (.obj) file for use in OpenGL.
	 * Note that the faces represented by the file must be triangles.
	 */
	public static List<Face> parseTriangles(String fileName)
	{
		long startTime = System.nanoTime();
		
		List<Face> faces = new ArrayList<Face>();
		
		List<float[]> vertices  = new ArrayList<float[]>();
		List<float[]> texCoords = new ArrayList<float[]>();
		List<float[]> normals   = new ArrayList<float[]>();

		try
		{
			Texture defaultTexture = TextureIO.newTexture(new File("tex/default.jpg"), true);
			String current = "default.jpg";
			Texture currentTexture = defaultTexture;
			
			boolean hasTexture = false;
			
			int wildcard  = -1;
			int wildcards =  0; 

			Scanner fs = new Scanner(new File("obj/" + fileName + ".obj"));
			
			while (fs.hasNextLine())
			{
				String line = fs.nextLine();
				
				if (line.startsWith("v "))
				{
					Scanner ls = new Scanner(line.replaceAll("v", "").trim());
					vertices.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("vt"))
				{
					Scanner ls = new Scanner(line.replaceAll("vt", "").trim());
					texCoords.add(new float[] {ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("vn"))
				{
					Scanner ls = new Scanner(line.replaceAll("vn", "").trim());
					normals.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("usemtl"))
				{
					wildcard = -1;
					String texture = line.replaceAll("usemtl", "").trim();
					
					if (texture.equals("Material"))
					{
						currentTexture = defaultTexture;
						current = "default.jpg";
						hasTexture = false;
					}
					else if(texture.equals("Material_"))
					{
						currentTexture = defaultTexture;
						current = "default.jpg";
						hasTexture = true;
						wildcard = wildcards++;
					}
					else
					{
						texture = texture.replaceAll("Material_", "");
						if(!current.equals(texture))
						{
							current = texture;
							currentTexture = TextureIO.newTexture(new File("tex/" + texture), true);
						}
						
						hasTexture = true;
					}

				}
				if (line.startsWith("f"))
				{
					boolean textured = !texCoords.isEmpty(); 
					
					Scanner ls = new Scanner(line.replaceAll("f", "").trim().replaceAll("/", " "));

					int[] v1, v2, v3;
					
					if(textured)
					{
						v1 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
						v2 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
						v3 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					}
					else
					{
						v1 = new int[] {ls.nextInt(), 0, ls.nextInt()};
						v2 = new int[] {ls.nextInt(), 0, ls.nextInt()};
						v3 = new int[] {ls.nextInt(), 0, ls.nextInt()};
					}

					float[][] vs = new float[][]
					{
						vertices.get(v1[0] - 1),
						vertices.get(v2[0] - 1),
						vertices.get(v3[0] - 1)
					};
					
					float[][] ts = new float[3][2];
					                         
					if(textured)
					{
						ts = new float[][]
						{
							texCoords.get(v1[1] - 1),
							texCoords.get(v2[1] - 1),
							texCoords.get(v3[1] - 1)
						};
					}
					
					float[][] ns = new float[][]
					{
						normals.get(v1[2] - 1),
						normals.get(v2[2] - 1),
						normals.get(v3[2] - 1)
					};
					
					ls.close();

					faces.add(new Face(vs, ns, ts, currentTexture, hasTexture, wildcard));
				}
			}
			fs.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		
		long endTime = System.nanoTime();
		
		System.out.printf("OBJ Parser: %-13s (%5d) %8.3f ms" + "\n", fileName, faces.size(), (endTime - startTime) / 1E6);

		return faces;
	}
	
	public static Model parseTriangleMesh(String fileName)
	{
		long startTime = System.nanoTime();
		
		int polygonCount = 0;
		
		List<float[]> vertices  = new ArrayList<float[]>();
		List<float[]> normals   = new ArrayList<float[]>();
		
		List<Integer> vIndices  = new ArrayList<Integer>();
		List<Integer> nIndices  = new ArrayList<Integer>();

		try
		{
			Scanner fs = new Scanner(new File("obj/" + fileName + ".obj"));
			
			while (fs.hasNextLine())
			{
				String line = fs.nextLine();
				
				if (line.startsWith("v "))
				{
					Scanner ls = new Scanner(line.replaceAll("v", "").trim());
					vertices.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("vn"))
				{
					Scanner ls = new Scanner(line.replaceAll("vn", "").trim());
					normals.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("f"))
				{
					polygonCount++;
					
					Scanner ls = new Scanner(line.replaceAll("f", "").trim().replaceAll("/", " "));

					int[] v1 = new int[] {ls.nextInt(), ls.nextInt()};
					int[] v2 = new int[] {ls.nextInt(), ls.nextInt()};
					int[] v3 = new int[] {ls.nextInt(), ls.nextInt()};
					
					vIndices.add(v1[0] - 1);
					vIndices.add(v2[0] - 1);
					vIndices.add(v3[0] - 1);
					
					nIndices.add(v1[1] - 1);
					nIndices.add(v2[1] - 1);
					nIndices.add(v3[1] - 1);
					
					ls.close();
				}
			}
			fs.close();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		int[] _vIndices = new int[vIndices.size()];
		for(int i = 0; i < vIndices.size(); i++) _vIndices[i] = vIndices.get(i);
		
		int[] _nIndices = new int[nIndices.size()];
		for(int i = 0; i < nIndices.size(); i++) _nIndices[i] = nIndices.get(i);
		
		long endTime = System.nanoTime();
		
		System.out.printf("OBJ Parser: %-13s (%5d) %8.3f ms" + "\n", fileName, polygonCount, (endTime - startTime) / 1E6);
		
		Model model = new Model(vertices, normals, _vIndices, _nIndices, 3);
		Model.model_set.put(fileName, model);
		
		return model;
	}
	
	public static Model parseTexturedTriangleMesh(String fileName)
	{
		long startTime = System.nanoTime();
		
		int polygonCount = 0;
		
		List<float[]> vertices  = new ArrayList<float[]>();
		List<float[]> normals   = new ArrayList<float[]>();
		List<float[]> texCoords = new ArrayList<float[]>(); 
		
		List<Integer> vIndices  = new ArrayList<Integer>();
		List<Integer> nIndices  = new ArrayList<Integer>();
		List<Integer> tIndices  = new ArrayList<Integer>();

		try
		{
			Scanner fs = new Scanner(new File("obj/" + fileName + ".obj"));
			
			while (fs.hasNextLine())
			{
				String line = fs.nextLine();
				
				if (line.startsWith("v "))
				{
					Scanner ls = new Scanner(line.replaceAll("v", "").trim());
					vertices.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("vt"))
				{
					Scanner ls = new Scanner(line.replaceAll("vt", "").trim());
					texCoords.add(new float[] {ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("vn"))
				{
					Scanner ls = new Scanner(line.replaceAll("vn", "").trim());
					normals.add(new float[] {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()});
					ls.close();
				}
				if (line.startsWith("f"))
				{
					polygonCount++;
					
					Scanner ls = new Scanner(line.replaceAll("f", "").trim().replaceAll("/", " "));

					int[] v1 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					int[] v2 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					int[] v3 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					
					vIndices.add(v1[0] - 1);
					vIndices.add(v2[0] - 1);
					vIndices.add(v3[0] - 1);
					
					tIndices.add(v1[1] - 1);
					tIndices.add(v2[1] - 1);
					tIndices.add(v3[1] - 1);
					
					nIndices.add(v1[2] - 1);
					nIndices.add(v2[2] - 1);
					nIndices.add(v3[2] - 1);
					
					ls.close();
				}
			}
			fs.close();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		int[] _vIndices = new int[vIndices.size()];
		for(int i = 0; i < vIndices.size(); i++) _vIndices[i] = vIndices.get(i);
		
		int[] _tIndices = new int[tIndices.size()];
		for(int i = 0; i < tIndices.size(); i++) _tIndices[i] = tIndices.get(i);
		
		int[] _nIndices = new int[nIndices.size()];
		for(int i = 0; i < nIndices.size(); i++) _nIndices[i] = nIndices.get(i);
		
		long endTime = System.nanoTime();
		
		System.out.printf("OBJ Parser: %-13s (%5d) %8.3f ms" + "\n", fileName, polygonCount, (endTime - startTime) / 1E6);
		
		Model model = new Model(vertices, texCoords, normals, _vIndices, _tIndices, _nIndices, 3);
		Model.model_set.put(fileName, model);
		
		return model;
	}
	
	public static void main(String[] args)
	{
		parseIndexedArrays("grass_top_upper");
		parseTexturedTriangleMesh("grass_top_upper");
		
	}
	
	public static Model parseIndexedArrays(String fileName)
	{
		long startTime = System.nanoTime();
		
		int polygonCount = 0;
		
		List<float[]> vertices = new ArrayList<float[]>();
		List<float[]> normals  = new ArrayList<float[]>();
		List<float[]> tcoords0 = new ArrayList<float[]>();
		List<float[]> tcoords1 = new ArrayList<float[]>(); 
		
		List<int[]> points  = new ArrayList<int[]>();

		try
		{
			Scanner fs = new Scanner(new File("obj/" + fileName + ".model"));
			
			while (fs.hasNextLine())
			{
				String line = fs.nextLine();
				
				if (line.startsWith("VERTEX"))
				{
					line = line.substring(6).trim();
					String[] coords = line.split(" ");
					
					for(int i = 0; i < coords.length; i += 3)
					{
						float x = Float.parseFloat(coords[i + 0]);
						float y = Float.parseFloat(coords[i + 1]);
						float z = Float.parseFloat(coords[i + 2]);
						vertices.add(new float[] {x, y, z});
					}
				}
				if (line.startsWith("NORMAL"))
				{
					line = line.substring(6).trim();
					String[] coords = line.split(" ");
					
					for(int i = 0; i < coords.length; i += 3)
					{
						float x = Float.parseFloat(coords[i + 0]);
						float y = Float.parseFloat(coords[i + 1]);
						float z = Float.parseFloat(coords[i + 2]);
						normals.add(new float[] {x, y, z});
					}
				}
				if (line.startsWith("TEXCOORD0"))
				{
					line = line.substring(9).trim();
					String[] coords = line.split(" ");
					
					for(int i = 0; i < coords.length; i += 2)
					{
						float s = Float.parseFloat(coords[i + 0]);
						float t = Float.parseFloat(coords[i + 1]);
						tcoords0.add(new float[] {s, t});
					}
				}
				if (line.startsWith("TEXCOORD1"))
				{
					line = line.substring(9).trim();
					String[] coords = line.split(" ");
					
					for(int i = 0; i < coords.length; i += 2)
					{
						float s = Float.parseFloat(coords[i + 0]);
						float t = Float.parseFloat(coords[i + 1]);
						tcoords1.add(new float[] {s, t});
					}
				}
				if (line.startsWith("INDEX"))
				{	
					line = line.substring(5).trim();
					String[] indices = line.split(" ");
					
					polygonCount = indices.length;
					
					for(int i = 0; i < indices.length; i += 3)
					{
						int vi = Integer.parseInt(indices[i + 0]);
						int ni = Integer.parseInt(indices[i + 1]);
						int ti = Integer.parseInt(indices[i + 2]);
						points.add(new int[] {vi, ni, ti});
					}
				}
			}
			fs.close();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		long endTime = System.nanoTime();
		
		System.out.printf("OBJ Parser: %-13s (%5d) %8.3f ms" + "\n", fileName, polygonCount / 9, (endTime - startTime) / 1E6);
		
		Model model = new Model(vertices, tcoords0, tcoords1, normals, points);
		Model.model_set.put(fileName, model);
		
		return model;
	}
}
