package ssif.utilityFunctions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ssif.model.Term;

/**
* @author Rashmie Abeysinghe
*/
public class LoadFromFile {
	
	public static Map<String, Term> LoadConceptMap(String labelsFile) throws IOException
	{
		Map<String, Term> concept_map = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(labelsFile));
		String line;
		String[] tokens;
		Term newConcept;
		
		while((line=br.readLine())!=null)
		{
			tokens = line.split("\t");
			if(tokens.length>1)				//getting rid of deprecated concepts. They do not have a label.
			{
				newConcept = new Term(tokens[0], tokens[1].toLowerCase());	//toLowerCase() of concept label remomved
				concept_map.put(tokens[0], newConcept);
			}
		}
		br.close();
		
		return concept_map;
	}
	
	public static Set<String> loadStopWords(String stopWordsFile) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(stopWordsFile));
		Set<String> stop_words = new HashSet<String>();
		String line;
		while((line=br.readLine())!=null)
		{
			stop_words.add(line);
		}
		br.close();
		return stop_words;
	}
}
