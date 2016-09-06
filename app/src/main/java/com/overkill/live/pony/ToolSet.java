package com.overkill.live.pony;

import java.io.File;
import java.io.FileFilter;

import android.graphics.Point;

import com.overkill.live.pony.engine.EffectWindow;
import com.overkill.live.pony.engine.Pony;
import com.overkill.live.pony.engine.Pony.AllowedMoves;
import com.overkill.live.pony.engine.PonyWindow;
import com.overkill.live.pony.engine.Pony.Direction;


public class ToolSet {
	
	public static String formatFolderName(String value){
		value = value.toLowerCase();
		return value.replaceAll("[^A-Za-z0-9]", "");	
	}
	
	public static FileFilter fileOnlyFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			String filename = pathname.getName().toLowerCase();
			return pathname.isFile() && (filename.endsWith(".gif") || filename.endsWith(".png") || filename.endsWith(".ini"));
		}
	};
	
	public static FileFilter iniFileOnlyFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			String filename = pathname.getName().toLowerCase();
			return pathname.isFile() && filename.equalsIgnoreCase("pony.ini");
		}
	};
	
	public static FileFilter folderContainingINIFileFilter = new FileFilter() {		
		@Override
		public boolean accept(File pathname) {
			if(pathname.isDirectory() == false) return false;
			return pathname.listFiles(iniFileOnlyFilter).length > 0;
		}
	};
	
	public static long getFolderSize(File folder){
		File[] files = folder.listFiles(fileOnlyFilter);
		int size = 0;
		for(File f : files){
			size += f.length();
		}
		return size;
	}
	
	public static int getFolderItemCount(File folder){
		File[] files = folder.listFiles(fileOnlyFilter);
		return files.length;
	}
	
	public static String[] splitWithQualifiers(String SourceText, String TextDelimiter, String TextQualifier) {
		return splitWithQualifiers(SourceText, TextDelimiter, TextQualifier, "");
	}
    public static String[] splitWithQualifiers(String SourceText, String TextDelimiter, String TextQualifier, String ClosingTextQualifier) {
		String[] strTemp;
		String[] strRes; int I; int J; String A; String B; boolean blnStart = false;
		B = "";
		
		if (TextDelimiter != " ") SourceText = SourceText.trim();
		if (ClosingTextQualifier.length() > 0) SourceText = SourceText.replace(ClosingTextQualifier, TextQualifier);
		strTemp = SourceText.split(TextDelimiter);
		for (I = 0; I < strTemp.length; I++) {
		    J = strTemp[I].indexOf(TextQualifier, 0);
		    if (J > -1) {
		        A = strTemp[I].replace(TextQualifier, "").trim();
		        String C = strTemp[I].replace(TextQualifier, "");
		        if (strTemp[I].trim().equals(TextQualifier + A + TextQualifier)) {
		                B = B + A + " \n";
		                blnStart = false;
		        } else if (strTemp[I].trim().equals(TextQualifier + C + TextQualifier)) {
	                B = B + C + " \n";
	                blnStart = false;
		        } else if (strTemp[I].trim().equals(TextQualifier + A)) {
		                B = B + A + TextDelimiter;
		                blnStart = true;
		        } else if (strTemp[I].trim().equals(A)) {
		                B = B + A + TextDelimiter;
		                blnStart = false;
		        } else if (strTemp[I].trim().equals(A + TextQualifier)) {
		                B = B + A + "\n";
		                blnStart = false;
		        }
		    } else {
		        if (blnStart)
		            B = B + strTemp[I] + TextDelimiter;
		        else
		            B = B + strTemp[I] + "\n";
		    }
		}
		if (B.length() > 0) {
		    B = B.substring(0, B.length());
		    strRes = B.split("\n");
		} else {
		    strRes = new String[1];
		    strRes[0] = SourceText;
		}	
		return strRes;
	}
    
    /**
     * Converts a String to a {@link Direction}
     * @param string The String to convert
     * @return The {@link Direction} indicated by the String
     * @throws Exception If no match was found
     */
    public static Direction getDirectionByString(String string) throws Exception {
		if (string.trim().equalsIgnoreCase("top"))
			return Direction.top;
		if (string.trim().equalsIgnoreCase("bottom"))
			return Direction.bottom;
		if (string.trim().equalsIgnoreCase("left"))
			return Direction.left;
		if (string.trim().equalsIgnoreCase("right"))
			return Direction.right;
		if (string.trim().equalsIgnoreCase("bottom_right"))
			return Direction.bottom_right;
		if (string.trim().equalsIgnoreCase("bottom_left"))
			return Direction.bottom_left;
		if (string.trim().equalsIgnoreCase("top_right"))
			return Direction.top_right;
		if (string.trim().equalsIgnoreCase("top_left"))
			return Direction.top_left;
		if (string.trim().equalsIgnoreCase("center"))
			return Direction.center;
		if (string.trim().equalsIgnoreCase("any"))
			return Direction.random;
		if (string.trim().equalsIgnoreCase("any_notcenter") || string.trim().equalsIgnoreCase("any-not_center"))
			return Direction.random_not_center;
		
		// If not a valid direction, throw excepion
		throw new Exception("Invalid placement direction or centering for effect.");
	}
    
    public static AllowedMoves getMovementByString(String string){
    	if (string.trim().equalsIgnoreCase("none")) {
			return AllowedMoves.None;
		} else if (string.trim().equalsIgnoreCase("horizontal_only")) {
			return AllowedMoves.Horizontal_Only;
		} else if (string.trim().equalsIgnoreCase("vertical_only")) {
			return AllowedMoves.Vertical_Only;
		} else if (string.trim().equalsIgnoreCase("horizontal_vertical")) {
			return AllowedMoves.Horizontal_Vertical;
		} else if (string.trim().equalsIgnoreCase("diagonal_only")) {
			return AllowedMoves.Diagonal_Only;
		} else if (string.trim().equalsIgnoreCase("diagonal_horizontal")) {
			return AllowedMoves.Diagonal_Horizontal;
		} else if (string.trim().equalsIgnoreCase("diagonal_vertical")) {
			return AllowedMoves.Diagonal_Vertical;
		} else if (string.trim().equalsIgnoreCase("all")) {
			return AllowedMoves.All;
		} else if (string.trim().equalsIgnoreCase("mouseover")) {
			return AllowedMoves.MouseOver;
		} else if (string.trim().equalsIgnoreCase("sleep")) {
			return AllowedMoves.Sleep;
		}
    	return AllowedMoves.None;
    }
    
    /**
     * Format a byte value to a string ending with B, KB, MB, GB or TB
     * @param bytes The value to format
     * @return The given byte value with 2 decimal digits and B, KB, MB, GB or TB
     */
    public static String formatBytes(float bytes) {
	    String units[] = {"B", "KB", "MB", "GB", "TB"};
	  
	    bytes = Math.max(bytes, 0);
	    int pow = (int) Math.floor(((bytes != 0) ? Math.log(bytes) : 0) / Math.log(1024));
	    pow = Math.min(pow, units.length - 1);
	  
	    bytes /= Math.pow(1024, pow);
	    
	    return  String.format("%.2f", bytes) + ' ' + units[pow];
	}
    
    /**
     * Returns a random {@link Direction}.
     * @param IncludeCentered Include {@link Directions.center} as a possible value
     * @return A random {@link Direction}
     */
    public static Direction getRandomDirection(boolean IncludeCentered) {
		int dice;
		if (IncludeCentered)
			dice = MyLittleWallpaperService.rand.nextInt(9);
		else
			dice = MyLittleWallpaperService.rand.nextInt(8);
		
		switch(dice) {
			case 0:
				return Pony.Direction.bottom;
			case 1:
				return Pony.Direction.bottom_left;
			case 2:
				return Pony.Direction.bottom_right;
			case 3:
				return Pony.Direction.left;
			case 4:
				return Pony.Direction.right;
			case 5:
				return Pony.Direction.top;
			case 6:
				return Pony.Direction.top_left;
			case 7:
				return Pony.Direction.top_right;
			case 8:
			default:
				return Pony.Direction.center;
		}
	}
    
    /**
	 * Returns the position	of the effect depending on the target postionioning around the given Pony
	 * @param effectWindow Holds the size of the effect
	 * @param pony Pony to cast the effect
	 * @param direction Direction from the Pony
	 * @param centering Centering on the Pony
	 * @return
	 */
	public static Point getEffectLocation(EffectWindow effectWindow, Direction direction, PonyWindow ponyWindow, Direction centering) {
		Point point = new Point(0, 0);		
		switch(direction) {
			case bottom:
				point = new Point(ponyWindow.getLocation().x + (ponyWindow.getWidth() / 2), ponyWindow.getLocation().y + ponyWindow.getHeight());
				break;
			case bottom_left:
				point = new Point(ponyWindow.getLocation().x, ponyWindow.getLocation().y + ponyWindow.getHeight());
				break;
			case bottom_right:
				point = new Point(ponyWindow.getLocation().x + ponyWindow.getWidth(), ponyWindow.getLocation().y + ponyWindow.getHeight());
				break;
			case center:
				point = new Point(ponyWindow.getLocation().x + (ponyWindow.getWidth() / 2), ponyWindow.getLocation().y + (ponyWindow.getHeight() / 2));
				break;
			case left:
				point = new Point(ponyWindow.getLocation().x, ponyWindow.getLocation().y + (ponyWindow.getHeight() / 2));
				break;
			case right:
				point = new Point(ponyWindow.getLocation().x + ponyWindow.getWidth(), ponyWindow.getLocation().y + (ponyWindow.getHeight() / 2));
				break;
			case top:
				point = new Point(ponyWindow.getLocation().x + (ponyWindow.getWidth() / 2), ponyWindow.getLocation().y);
				break;
			case top_left:
				point = new Point(ponyWindow.getLocation().x, ponyWindow.getLocation().y);
				break;
			case top_right:
				point = new Point(ponyWindow.getLocation().x + ponyWindow.getWidth(), ponyWindow.getLocation().y);
				break;
		}
		
		switch(centering) {
			case bottom:
				point = new Point(point.x - (effectWindow.getImage().getSpriteWidth() / 2), point.y - effectWindow.getImage().getSpriteHeight());
				break;
	        case bottom_left:
				point = new Point(point.x, point.y - effectWindow.getImage().getSpriteHeight());
				break;
	        case bottom_right:
				point = new Point(point.x - effectWindow.getImage().getSpriteWidth(), point.y - effectWindow.getImage().getSpriteHeight());
				break;
	        case center:
				point = new Point(point.x - (effectWindow.getImage().getSpriteWidth() / 2), point.y - (effectWindow.getImage().getSpriteHeight() / 2));
				break;
	        case left:
				point = new Point(point.x, point.y - (effectWindow.getImage().getSpriteHeight() / 2));
				break;
	        case right:
				point = new Point(point.x - effectWindow.getImage().getSpriteWidth(), point.y - (effectWindow.getImage().getSpriteHeight() / 2));
				break;
	        case top:
				point = new Point(point.x - (effectWindow.getImage().getSpriteWidth() / 2), point.y);
				break;
	        case top_left:
				// no change
				break;
	        case top_right:
				point = new Point(point.x - effectWindow.getImage().getSpriteWidth(), point.y);
				break;
		}
		
		return point;
	}
}
