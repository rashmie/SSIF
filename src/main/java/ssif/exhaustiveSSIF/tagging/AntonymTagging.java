package ssif.exhaustiveSSIF.tagging;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
//import ssif.model.Antonym;
//import ssif.model.Element;
//import ssif.model.ElementList;
import ssif.model.*;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class AntonymTagging extends SubConceptTagging implements Serializable {
	
	protected String wordnetAntonymFile;
	protected String antoFile;				//other antonyms not in wordnet. Specific to the Ontology.
	private HashMap<String, Antonym> antonym_pairs;
	private HashMap<String, Antonym> GO_antonym_pairs;
	
	public AntonymTagging(String labels_file, String wordnetAntonymFile, String antoFile, String taggerModel_file) {
		super(labels_file, taggerModel_file);
		this.wordnetAntonymFile = wordnetAntonymFile;
		this.antoFile = antoFile;
	}

	public HashMap<String, Antonym> getGO_antonym_pairs() {
		return GO_antonym_pairs;
	}
	
	public HashMap<String, Antonym> getWordNet_antonym_pairs() {
		return antonym_pairs;
	}

	public void loadWordnetAntonyms() throws IOException
	{
		loadAntonymsFromFile(wordnetAntonymFile);
	}
	
	public void loadOtherAntonymsFromFile() throws IOException
	{
		loadAntonymsFromFile(antoFile);
	}
	
	private void loadAntonymsFromFile(String input) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line;
		String[] tokens;
		if(antonym_pairs==null)
			antonym_pairs = new HashMap<String, Antonym>();
		
		Antonym ant;
		while((line=br.readLine())!=null)
		{
			tokens = line.split(" ");
			if((ant = antonym_pairs.get(tokens[0]))!=null)
				ant.putOpposite(tokens[1]);
			else
				antonym_pairs.put(tokens[0], new Antonym(tokens[0], tokens[1]));
		}
		br.close();
	}
	
	public void findAntonymPairsInGO() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(labels_file));
		String line, antonym;
		//int count =0;
		String[] tokens;
		List<CoreLabel> labelTokens;
		loadWordnetAntonyms();
		if(this.antoFile!=null)
			loadOtherAntonymsFromFile();
		GO_antonym_pairs = new HashMap<String, Antonym>();
		
		while((line=br.readLine())!=null)
		{
			tokens = line.split("\t");
			if(tokens.length>1)
			{
				labelTokens = tokenize(tokens[1]);
				for(CoreLabel cl : labelTokens)
				{
					antonym = cl.originalText();
					if(antonym_pairs.containsKey(antonym))
						GO_antonym_pairs.put(antonym, antonym_pairs.get(antonym));
				}
			}
		}
		br.close();
	}
	
	public void runAntonymTagging() throws IOException
	{
		findAntonymPairsInGO();
		Set<String> tagListForWord;
		Set<ElementList> elements_with_POS_tags;
		ElementList POS_temp;
		Set<ElementList> elements_with_subconcept_tags;
		Set<ElementList> elements_with_antonym_tags;
		
		for(Term t: terms)
		{
			if(!t.isValidTerm())
				continue;
			
			if(t.getElements_with_tags().size()>0)	//if multiple subconcept patterns exist (having multiple subconcepts leads to different subconcept patterns. These might be different).
			{
				elements_with_subconcept_tags = t.getElements_with_tags();
				elements_with_antonym_tags = new HashSet<ElementList>(elements_with_subconcept_tags);
				//System.out.println("elements size: "+elements_with_subconcepts_and_antonyms.size()+"tags size: "+stanford_subconcept_antonym_tags.size());
				
				for(ElementList el: elements_with_antonym_tags)
				{
					for(Element e: el)
					{
						if(GO_antonym_pairs.containsKey(e.getElementName()))
						{
							tagListForWord = new HashSet<String>(e.getTags());		//multiple ANT tags if not for this
							//tagListForWord = e.getTags();
							tagListForWord.add("ANT");	
							e.setTags(tagListForWord);
						}
					}
				}
				t.setElements_with_tags(elements_with_antonym_tags);
			}
			else		//this is not really necessary as even when there are no subconcepts, the already found POS tags will be added to "elements_with_subconcept_tags"
			{
				POS_temp = new ElementList(t.getFirstElementListOf_elements_with_tags().getList_of_elements());
				for(Element e: POS_temp)
				{
					if(GO_antonym_pairs.containsKey(e.getElementName()))
					{
						tagListForWord = new HashSet<String>(e.getTags());
						//tagListForWord = e.getTags();
						tagListForWord.add("ANT");	
						e.setTags(tagListForWord);
					}
				}
				elements_with_POS_tags = new HashSet<ElementList>();
				elements_with_POS_tags.add(POS_temp);
				t.setElements_with_tags(elements_with_POS_tags);
			}
			
		}
	}	
}
