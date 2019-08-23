
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
import au.com.bytecode.opencsv.CSVWriter;
import ssif.conditionalrules.Inconsistency;
import ssif.exhaustiveSSIF.conditionalrules.R2_exhaustive;
import ssif.exhaustiveSSIF.conditionalrules.R3_exhaustive;
import ssif.exhaustiveSSIF.conditionalrules.R4_exhaustive;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Element;
import ssif.model.ElementList;
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
	protected Map<String, Term> term_map;		//term id to Term map
	protected Tagging tagged_terms;			//result after all the tagging is done
		
	public SSIF_exhaustive(String labels_file, String wordnetAntonymFile, String antoFile, String hierarchy_file) {
		super();
		this.labels_file = labels_file;
		this.wordnetAntonymFile = wordnetAntonymFile;
		this.antoFile = antoFile;
		this.hierarchy_file = hierarchy_file;
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
//		Term child;
//		for(String con: term_map.keySet())
//		{
//			child = term_map.get(con);
//			child.setAll_parents(child.retrieveAllParents());
//		}
		
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
	
	//For Dr.Cui's report
	protected void numOfValidTerms()
	{
		System.out.println("Total number of terms: "+tagged_terms.getTerms().size());
		int valid = 0;
		for(Term t: tagged_terms.getTerms())
		{
			if(t.isValidTerm())
				valid++;
		}
		System.out.println("Total number of valid terms: "+ valid);
	}
	
	//For Dr.Cui's report. Obtains the number of terms with a given tag
	protected void numOfTermsWithTag(String tag)
	{
		int count = 0;
		loop1:
		for(Term t: tagged_terms.getTerms())
		{
			if(!t.isValidTerm())
				continue;
			loop2:
			for(ElementList el_list: t.getElements_with_tags())
			{
				for(Element el: el_list)
				{
					if(el.isTagAvailable(tag))
					{
						count++;
						break loop2;
					}
				}
			}
		}
		System.out.println("Number of terms with "+tag+": "+count);
	}
	
	public void findClosure() throws IOException 		//finds transtivie closure to get all parents for each Term.
	{
		this.getTermMapFromList();
    		this.fillImmediateParents();
    		this.fillAllParents();
    		this.fillAllChildren();
	}
	
	public void printTagsOfTerms(String output) throws IOException
	{
		CSVWriter cw = new CSVWriter(new FileWriter(output));
		String[] line = new String[2];
		for(Term t: tagged_terms.getTerms())
		{ 
			if(!t.isValidTerm())
				continue;
			
			line[0] = t.getIDLabel();
			String line_to_write = "";
	    		//line_to_write+= t.getIDLabel()+"\t**  ";
	    		
	    		for(ElementList el: t.getElements_with_tags())
	    		{
	    			for(Element e: el)
	    				line_to_write += e.getElementName()+"/"+e.getTags()+" ";
	    			line_to_write += " | ";
	    		}
	    		line_to_write = line_to_write.replaceAll(" \\| $", "");	//removing the trailing pipe sign.
	    		line[1] = line_to_write;
	    		cw.writeNext(line);
	    		//cw.write(line_to_write);
	    		//cw.newLine();
		}
		cw.close();
	}
	
	public void printSubConceptReplacedTerms(String output) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for(Term t: tagged_terms.getTerms())
		{ 
			if(!t.isValidTerm())
				continue;
			
			String line_to_write = "";
	    		line_to_write+= t.getIDLabel()+"\t**  ";
	    		
	    		for(String el: t.getLabel_with_subconcept_IDs())
	    		{
	    			line_to_write += el;
	    			line_to_write += " | ";
	    		}
	    		line_to_write = line_to_write.replaceAll(" \\| $", "");	//removing the trailing pipe sign.
	    		bw.write(line_to_write);
	    		bw.newLine();;
		}
		bw.close();
	}
	
	public void writeSubconceptsToFile(String outputFile) throws IOException
    {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
	    	Set<Term> subconcepts;
	    	String line_to_write;
	    	for(Term t: tagged_terms.getTerms())
	    	{
	    		line_to_write = "";
	    		line_to_write+= t.getIDLabel()+":\t";
	    		subconcepts = t.getSubconcepts();
	    		
	    		for(Term sc: subconcepts)
	    		{
	    			line_to_write+= sc.getID()+" ";
	    		}
	    		
	    		bw.write(line_to_write.trim());
	    		bw.write("\n");
	    	}
	    	bw.close();
    }
	
	
	public void printChildren(String output) throws IOException
	{
		CSVWriter bw = new CSVWriter(new FileWriter(output));
		
		for(Term t: tagged_terms.getTerms())
		{
			if(!t.isValidTerm())
				continue;
			
			String[] line = new String[3];
			line[0] = t.getIDLabel();
			line[1] = Integer.toString(t.getAll_children().size());
			line[2] = t.getAll_children().toString();
			
			bw.writeNext(line);
		}
		
		bw.close();
	}
	
	public void printParents(String output) throws IOException
	{
		CSVWriter bw = new CSVWriter(new FileWriter(output));
		
		for(Term t: tagged_terms.getTerms())
		{
			if(!t.isValidTerm())
				continue;
			
			String[] line = new String[3];
			line[0] = t.getIDLabel();
			line[1] = Integer.toString(t.getAll_parents().size());
			line[2] = t.getAll_parents().toString();
			
			bw.writeNext(line);
		}
		
		bw.close();
	}
	
	public void setTagged_terms_FromSerialized(String inputSerialFile) throws IOException
	{
		Tagging tagging1 = Tagging.deserializeTagging(inputSerialFile);
		this.tagged_terms = tagging1;
	}
	
	public void setTagged_terms_FromBegining(String outputSerialFile) throws IOException
	{
		Tagging tagging1 = new Tagging(labels_file, wordnetAntonymFile, antoFile);
	    	tagging1.runTagging();
	    	Tagging.serializeTagging(tagging1, outputSerialFile);
	    	this.tagged_terms = tagging1;
	}
	
	public void runConditionalRules(String serialized_tagging_file) throws IOException
	{
//		setTagged_terms_FromSerialized("serialized/tagging/tagging7_parser.ser");
//		Tagging tagging1 = Tagging.deserializeTagging("serialized/tagging/tagging7_parser.ser");
		setTagged_terms_FromSerialized(serialized_tagging_file);
		Tagging tagging1 = Tagging.deserializeTagging(serialized_tagging_file);
	    	this.tagged_terms = tagging1;
	    	
	    	//UNCOMMENT WHEN OBTAINING RULES
	    findClosure();
	    	
	    	//UNCOMMENT WHEN OBTAINING RULES
	    	FindRootsOfEachTerm();
	    	


	    		R2_exhaustive r2 = new R2_exhaustive(tagging1);
	    		Set<String> tgs = new HashSet<String>();
	    		tgs.add("NN");
	    		tgs.add("JJ");
	    		tgs.add("SC");
	    		
	    		r2.runR2("NewRules/R2/r2_plus_modified2_check.csv", tgs, false, wordnetAntonymFile, antoFile, labels_file);	    	
	
	    		
		    	R3_exhaustive r3 = new R3_exhaustive(tagging1);
		    	r3.runR3("NewRules/R3/r3_plus_modified_check.csv", false, labels_file, wordnetAntonymFile, antoFile);	//"OntologyInputs/uberon_import_labels_02-07-2019.txt", "OntologyInputs/GO_partOf_10-04-2018.txt"

	    	
	    	
		    	R4_exhaustive r4_exhaustive = new R4_exhaustive(tagging1);
		    	Set<Inconsistency> obtainedRules_r4 = r4_exhaustive.runR4("NewRules/R4/r4_one_modified2_check.csv", false, labels_file, wordnetAntonymFile, antoFile);
		    	
	}
	
	public void serializeTagging(String output) throws IOException
	{
		Tagging tagging1 = new Tagging(labels_file, wordnetAntonymFile, antoFile);
	    	tagging1.runTagging();
	    	Tagging.serializeTagging(tagging1, output);
	}
	
	
	public static void main(String[] args) throws IOException 
	{
		long startTime = System.currentTimeMillis();
    	
	    	SSIF_exhaustive ssif1 = new SSIF_exhaustive("OntologyInputs/GO_labels_10-03-2018.txt", "AntonymInputs/wordnet_antonyms.txt", "AntonymInputs/other_antonyms.txt", "OntologyInputs/GO_hierarchy_10-03-2018.txt");
	    //	ssif1.serializeTagging("serialized/tagging/tagging21.ser");
	    	ssif1.runConditionalRules("serialized/tagging/tagging21.ser");
		
		    			
	    	long endTime = System.currentTimeMillis();
	    	System.out.println("Elapsed Time: "+(endTime-startTime)/1000+" seconds");
	}
}
