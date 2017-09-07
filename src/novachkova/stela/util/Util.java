package novachkova.stela.util;

public class Util {
	public static StringBuilder deleteLastChar(StringBuilder sb) {
		//deleting the last character in StringBuilder
		if(sb.length() > 0) {
			int lastIndex = sb.length() - 1;
			sb.deleteCharAt(lastIndex);
		}
		return sb;	
	}
	
	public static StringBuilder deleteLast(StringBuilder sb, String toDelete) {
		if(sb.length() > 0) {
			int beginingToDelete = sb.lastIndexOf(toDelete);
			if(beginingToDelete != -1) {
				sb.delete(beginingToDelete, beginingToDelete + toDelete.length());
			} 
		}
		return sb;
	}
	
	public static String buildBuyRequest(String requestType, String destination, int seat, String buyMethod, String separator) {
		return requestType + separator + destination + separator + seat + separator + buyMethod;
	}
	
	public static String[] parseBuyRequest(String request, String separator) {
		return request.split(separator);
	}
	
	public static String prettyPrintPrep(String string, String separator) {
		if(separator != ""){
			String[] lines = string.split(separator);
			StringBuilder sb = new StringBuilder();
			for(String line: lines) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} else {
			return string;
		}
	}
}
