package com.overkill.live.pony;

import com.overkill.live.pony.Pony.Directions;

public class ToolSet {
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
    public static Directions getDirection(String setting) throws Exception {
		if (setting.trim().equalsIgnoreCase("top"))
			return Directions.top;
		if (setting.trim().equalsIgnoreCase("bottom"))
			return Directions.bottom;
		if (setting.trim().equalsIgnoreCase("left"))
			return Directions.left;
		if (setting.trim().equalsIgnoreCase("right"))
			return Directions.right;
		if (setting.trim().equalsIgnoreCase("bottom_right"))
			return Directions.bottom_right;
		if (setting.trim().equalsIgnoreCase("bottom_left"))
			return Directions.bottom_left;
		if (setting.trim().equalsIgnoreCase("top_right"))
			return Directions.top_right;
		if (setting.trim().equalsIgnoreCase("top_left"))
			return Directions.top_left;
		if (setting.trim().equalsIgnoreCase("center"))
			return Directions.center;
		if (setting.trim().equalsIgnoreCase("any"))
			return Directions.random;
		if (setting.trim().equalsIgnoreCase("any_notcenter"))
			return Directions.random_not_center;
		
		// If not a valid direction, throw excepion
		throw new Exception("Invalid placement direction or centering for effect.");
	}
    
    public static String formatBytes(float bytes) {
	    String units[] = {"B", "KB", "MB", "GB", "TB"};
	  
	    bytes = Math.max(bytes, 0);
	    int pow = (int) Math.floor(((bytes != 0) ? Math.log(bytes) : 0) / Math.log(1024));
	    pow = Math.min(pow, units.length - 1);
	  
	    bytes /= Math.pow(1024, pow);
	  
	    return  String.format("%.2f", bytes) + ' ' + units[pow];
	}
    
    public static Pony.Directions getRandomDirection(boolean IncludeCentered) {
		int dice;
		if (IncludeCentered)
			dice = MyLittleWallpaperService.rand.nextInt(9);
		else
			dice = MyLittleWallpaperService.rand.nextInt(8);
		
		switch(dice) {
			case 0:
				return Pony.Directions.bottom;
			case 1:
				return Pony.Directions.bottom_left;
			case 2:
				return Pony.Directions.bottom_right;
			case 3:
				return Pony.Directions.left;
			case 4:
				return Pony.Directions.right;
			case 5:
				return Pony.Directions.top;
			case 6:
				return Pony.Directions.top_left;
			case 7:
				return Pony.Directions.top_right;
			case 8:
			default:
				return Pony.Directions.center;
		}
	}
}
