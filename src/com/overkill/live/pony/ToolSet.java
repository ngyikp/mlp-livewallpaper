package com.overkill.live.pony;

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
}
