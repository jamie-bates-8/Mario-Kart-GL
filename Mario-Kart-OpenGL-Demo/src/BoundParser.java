
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class BoundParser
{
	public static final float[] DEFAULT_POSITION = {0, 0, 0};
	public static final float[] DEFAULT_ROTATION = {0, 0, 0};
	public static final float[] DEFAULT_EXTENTS  = {1, 1, 1};
	
	public static final boolean[] DEFAULT_VALID_FACES
		= {true, true, true, true, true, true};
	
	public static List<OBB> parseOBBs(String fileName)
	{
		List<OBB> bounds = new ArrayList<OBB>();
		
		List<float[]>   positions  = new ArrayList<float[]>();
		List<float[]>   rotations  = new ArrayList<float[]>();
		List<float[]>   extents    = new ArrayList<float[]>();
		
		List<boolean[]> validFaces = new ArrayList<boolean[]>();
		
		try
		{
			Scanner sc = new Scanner(new File(fileName));
			
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				
				if(line.startsWith("c"))
				{
					Scanner _sc = new Scanner(line.replaceAll("c", "").trim());
					positions.add(new float[] {_sc.nextFloat(), _sc.nextFloat(), _sc.nextFloat()});
					_sc.close();
				}
				if(line.startsWith("u"))
				{
					Scanner _sc = new Scanner(line.replaceAll("u", "").trim());
					rotations.add(new float[] {_sc.nextFloat(), _sc.nextFloat(), _sc.nextFloat()});
					_sc.close();
				}
				if(line.startsWith("e"))
				{
					Scanner _sc = new Scanner(line.replaceAll("e", "").trim());
					extents.add(new float[] {_sc.nextFloat(), _sc.nextFloat(), _sc.nextFloat()});
					_sc.close();
				}
				if(line.startsWith("v"))
				{
					Scanner _sc = new Scanner(line.replaceAll("v", "").trim());
					validFaces.add(new boolean[] {_sc.nextBoolean(), _sc.nextBoolean(), _sc.nextBoolean(),
							                      _sc.nextBoolean(), _sc.nextBoolean(), _sc.nextBoolean()});
					_sc.close();
				}
				if(line.startsWith("obb"))
				{
					Scanner _sc = new Scanner(line.replaceAll("obb", "").trim().replaceAll("/", " "));
					
					int positionID   = _sc.nextInt();
					int rotationID   = _sc.nextInt();
					int extentID     = _sc.nextInt();
					int validFacesID = _sc.nextInt();
					
					bounds.add(new OBB
					(
						(positionID   == 0) ? DEFAULT_POSITION    : positions.get(positionID - 1),
						(rotationID   == 0) ? DEFAULT_ROTATION    : rotations.get(rotationID - 1),
						(extentID     == 0) ? DEFAULT_EXTENTS     : extents.get(extentID - 1),
						(validFacesID == 0) ? DEFAULT_VALID_FACES : validFaces.get(validFacesID - 1)
					));
					
					_sc.close();
				}
			}
			sc.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		
		return bounds;
	}
}
