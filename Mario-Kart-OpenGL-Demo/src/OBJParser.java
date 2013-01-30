import graphics.util.Face;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	public static List<Face> parseTriangles(String filename)
	{
		long startTime = System.nanoTime();
		
		List<Face> faces = new ArrayList<Face>();
		
		List<float[]> vertices        = new ArrayList<float[]>();
		List<float[]> textureVertices = new ArrayList<float[]>();
		List<float[]> normals         = new ArrayList<float[]>();

		try
		{
			Texture defaultTexture = TextureIO.newTexture(new File("tex/default.jpg"), true);
			String current = "default.jpg";
			Texture currentTexture = defaultTexture;
			
			boolean hasTexture = false;
			
			int wildcard  = -1;
			int wildcards =  0; 

			Scanner fs = new Scanner(new File(filename));
			
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
					textureVertices.add(new float[] {ls.nextFloat(), ls.nextFloat()});
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
					Scanner ls = new Scanner(line.replaceAll("f", "").trim().replaceAll("/", " "));

					int[] v1 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					int[] v2 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};
					int[] v3 = new int[] {ls.nextInt(), ls.nextInt(), ls.nextInt()};

					float[][] vs = new float[][]
					{
						vertices.get(v1[0] - 1),
						vertices.get(v2[0] - 1),
						vertices.get(v3[0] - 1)
					};
					float[][] ts = new float[][]
					{
						textureVertices.get(v1[1] - 1),
						textureVertices.get(v2[1] - 1),
						textureVertices.get(v3[1] - 1)
					};
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
		
		System.out.printf("Parsed \"" + filename + "\" in %.3f ms" + "\n", (endTime - startTime) / 1E6);

		return faces;
	}
}
