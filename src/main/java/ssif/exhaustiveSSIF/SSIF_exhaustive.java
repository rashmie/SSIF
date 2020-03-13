
package ssif.exhaustiveSSIF;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ssif.exhaustiveSSIF.conditionalrules.R2_exhaustive;
import ssif.exhaustiveSSIF.conditionalrules.R3_exhaustive;
import ssif.exhaustiveSSIF.conditionalrules.R4_exhaustive;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Term;
/**
 * @author Rashmie Abeysinghe
 *
 */
public class SSIF_exhaustive{
	
	protected String labels_file;
	protected String wordnetAntonymFile;
	protected String antoFile;			//other antonyms not in wordnet
	protected String hierarchy_file;
	protected String taggerModel_file;
	protected Map<String, Term> term_map;		//term id to Term map
	protected Tagging tagged_terms;			//result after all the tagging is done
		
	public SSIF_exhaustive(String labels_file, String hierarchy_file, String wordnetAntonymFile, String antoFile, String taggerModel_file) {
		super();
		this.labels_file = labels_file;
		this.wordnetAntonymFile = wordnetAntonymFile;
		this.antoFile = antoFile;
		this.hierarchy_file = hierarchy_file;
		this.taggerModel_file = taggerModel_file;
	}
	
	public Map<String, Term> getTerm_map() {
		return term_map;
	}

	public Map<String, Term> getTermMapFromList()
	{
		term_map = new HashMap<String, Term>();
		//System.out.println(tagged_terms.getTerms());
		for(Term t: tagged_terms.getTerms())
		{
			this.term_map.put(t.getID(), t);
		}
		return term_map;
	}
	
	public void fillImmediateParents() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(hierarchy_file));
		String line;
		String[] tokens;
		Term child;
		Term parent;
		Set<Term> immediate_parents;
		
		while((line=br.readLine())!=null)
		{
			tokens = line.split("\t");
			if(tokens.length>1 && term_map.containsKey(tokens[0]) && term_map.containsKey(tokens[1]))
			{
				child = term_map.get(tokens[0]);
				parent = term_map.get(tokens[1]);
				immediate_parents = child.getImmediate_parents();
				immediate_parents.add(parent);		//object reference added to the ArrayList
			}
		}
		br.close();
	}
	
	protected void fillAllParents()
	{	
		for(Term child: term_map.values())
		{
			child.setAll_parents(child.retrieveAllParents());
		}
	}
	
	protected void fillAllChildren()
	{
		for(Term child: tagged_terms.getTerms())
	    	{
			for(Term parent : child.getAll_parents())
    				parent.addChild(child);
	    	}
	}
	
	protected void FindRootsOfEachTerm()
	{
		for(Term t: tagged_terms.getTerms())
	    	{
	    		for(Term parent : t.getAll_parents())
	    		{
	    			if(parent.getAll_parents().size()==0)		//if the term does not have any parents, then it is a root
	    			{
	    				t.setSubhierarchyRoot(parent);
	    				break;
	    			}
	    		}
	    	}
	}
	
	protected void writeTransitiveClosureFromSet(String output) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(labels_file));
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		String line, line_to_write;
		String[] tokens;
		Term child;
		
		while((line =br.readLine())!=null)
		{
			tokens = line.split("\t");
			if((child = term_map.get(tokens[0]))!=null)
			{
				line_to_write = child.getID()+": ";
				for(Term c: child.getAll_parents())
				{
					line_to_write += c.getID()+"|";
				}
 				bw.write(line_to_write.replaceAll("\\|$", ""));
				bw.write("\n");
			}
		}
		
		br.close();
		bw.close();
	}
		
	public void findClosure() throws IOException 		//finds transtivie closure to get all parents for each Term.
	{
		this.getTermMapFromList();
		this.fillImmediateParents();
		this.fillAllParents();
		this.fillAllChildren();
	}
		
	public void setTagged_terms_FromSerialized(String inputSerialFile) throws IOException
	{
		Tagging tagging1 = Tagging.deserializeTagging(inputSerialFile);
		this.tagged_terms = tagging1;
	}
	
	public void setTagged_terms_FromBegining(String outputSerialFile) throws IOException
	{
		Tagging tagging1 = new Tagging(labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
    	tagging1.runTagging();
    	Tagging.serializeTagging(tagging1, outputSerialFile);
    	this.tagged_terms = tagging1;
	}
	
	public void runConditionalRules() throws IOException
	{
		Tagging tagging1 = new Tagging(labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
		tagging1.runTagging();
		this.tagged_terms = tagging1;
		
		//if loading from serialized tagging
//		setTagged_terms_FromSerialized(serialized_tagging_file);
//		Tagging tagging1 = Tagging.deserializeTagging(serialized_tagging_file);
//	    	this.tagged_terms = tagging1;
	    	
    	//UNCOMMENT WHEN OBTAINING RULES
	    findClosure();
	    	
    	//UNCOMMENT WHEN OBTAINING RULES
    	FindRootsOfEachTerm();
    
		R2_exhaustive r2 = new R2_exhaustive(tagging1);
		Set<String> tgs = new HashSet<String>();
		tgs.add("NN");
		tgs.add("JJ");
		tgs.add("SC");
		
		r2.runR2("subconcept_rule_results.csv", tgs, false, wordnetAntonymFile, antoFile, labels_file, taggerModel_file);	    	

    	R3_exhaustive r3 = new R3_exhaustive(tagging1);
    	r3.runR3("monotonicity_rule_results.csv", false, labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
	
    	R4_exhaustive r4_exhaustive = new R4_exhaustive(tagging1);
    	r4_exhaustive.runR4("intersection_rule_results.csv", false, labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
	}
	
	public void serializeTagging(String output) throws IOException
	{
		Tagging tagging1 = new Tagging(labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
    	tagging1.runTagging();
    	Tagging.serializeTagging(tagging1, output);
	}
	
	
	public static void main(String[] args) throws IOException 
	{
		long startTime = System.currentTimeMillis();
    	
		//args[0]: labels file, 
		//args[1]: hierarchy file
		//args[2]: wordnet antonyms file
		//args[3]: other antonyms file
		//args[4]: POS tagger model file
		
	    SSIF_exhaustive ssif1 = new SSIF_exhaustive(args[0], args[1], args[2], args[3], args[4]);
    	ssif1.runConditionalRules();
	
    	long endTime = System.currentTimeMillis();
    	System.out.println("Elapsed Time: "+(endTime-startTime)/1000+" seconds");
	}
}
