package bates.jamie.graphics.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bates.jamie.graphics.scene.Model;


public class PLYParser
{
	public static Model parseColoredMesh(String fileName)
	{
		long startTime = System.nanoTime();
		
		List<float[]> vertices  = new ArrayList<float[]>();
		List<float[]> normals   = new ArrayList<float[]>();
		List<float[]> colors    = new ArrayList<float[]>();
		
		List<Integer> indices  = new ArrayList<Integer>();
		
		int vertexCount = 0;
		int   faceCount = 0;

		try
		{
			Scanner fs = new Scanner(new File("ply/" + fileName + ".ply")); // file scanner
			String line = "";
			
			do
			{
				line = fs.nextLine();
				
				if (line.startsWith("element vertex"))
				{
					Scanner ls = new Scanner(line.replaceAll("element vertex", "").trim()); // line scanner
					vertexCount = ls.nextInt();
					ls.close();
				}
				if (line.startsWith("element face"))
				{
					Scanner ls = new Scanner(line.replaceAll("element face", "").trim());
					faceCount = ls.nextInt();
					ls.close();
				}	
			}
			while(!line.startsWith("end_header"));
			
			for(int i = 0; i < vertexCount; i++)
			{
				line = fs.nextLine();
				
				Scanner ls = new Scanner(line.trim());
				
				float[] vertex = {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()};
				float[] normal = {ls.nextFloat(), ls.nextFloat(), ls.nextFloat()};
				float[] colour = {ls.nextFloat() / 255, ls.nextFloat() / 255, ls.nextFloat() / 255};
				
				vertices.add(vertex);
				normals.add(normal);
				colors.add(colour);
				
				ls.close();
			}
			
			for(int i = 0; i < faceCount; i++)
			{
				line = fs.nextLine();
				
				Scanner ls = new Scanner(line.trim());
				
				ls.nextInt(); // pass value that states the number of sides
	
				indices.add(ls.nextInt());
				indices.add(ls.nextInt());
				indices.add(ls.nextInt());
				
				ls.close();
			}
			
			fs.close();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
		
		long endTime = System.nanoTime();
		
		System.out.printf("OBJ Parser: %-13s (%5d) %8.3f ms" + "\n", fileName, faceCount, (endTime - startTime) / 1E6);
		
		Model model = new Model(vertices, normals, colors, indices);
		Model.model_set.put(fileName, model);
		
		return model;
	}
}
