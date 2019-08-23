package ssif.utilityFunctions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
* @author Rashmie Abeysinghe
*/
public class UtilityFunctions {
	
	
	public static Set<String> getSetFromString(String label)
	{
		String[] tokens = label.split(" ");
		return new HashSet<String>(Arrays.asList(tokens));
	}
	
	public static void main(String[] args)
	{
		System.out.println(getSetFromString("Abnormality of dorsoventral patterning of the limbs"));
	}

}
