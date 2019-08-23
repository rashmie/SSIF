package ssif.exhaustiveSSIF.tagging;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class SubConceptTagging extends POSTagging{
	
	public SubConceptTagging()
	{
		
	}
	protected SubConceptTagging(String labels_file) {
		super(labels_file);
	}

	
//	protected SubConceptTagging() {
//		
//	}

	private void subconceptSearch() throws IOException		//does not consider antonyms at the moment
    {
	    	Term concept, potentialSubconcept;
	    	String cons;		//concept
	    	String potSubcons;		//potential subconcept
	    	
	    	for(int i=0;i<terms.size();i++)
	    	{
	    		concept = terms.get(i);
	    		if(!concept.isValidTerm())
	    			continue;
	    		
	    		cons=" "+concept.getLabel()+" ";
	    		
	    		for(int j=0;j<terms.size();j++)
	    		{
	    			potentialSubconcept = terms.get(j);
	    			if(!potentialSubconcept.isValidTerm())
	    				continue;
	    			
				potSubcons= " "+potentialSubconcept.getLabel()+" ";
	    			//implement: once a subconcept "y" of a term "x" is found, if the subconcepts of term "y" is already found, add them all as subconcepts of "x": This is more efficient! 
				if(i!=j && concept.getLabel().length()>1 && potentialSubconcept.getLabel().length()>1 && concept.getLabel().length() > potentialSubconcept.getLabel().length() && cons.contains(potSubcons))	//isSubconcept(concept.getLabel(), potentialSubconcept.getLabel())
					concept.addSubconcept(potentialSubconcept);
	    		}
	    	}
    }
	
	//counts the occurences of String "sub_string" in String "original_string"
	public int countOccurences(String original_string, String sub_string)
	{
		int count=0;
		Pattern p = Pattern.compile(sub_string);
		Matcher m = p.matcher(original_string);
		while(m.find())
			count++;

		return count;
	}
	
	//For a particular term "t", finds all overlapping subconcepts and returns a map: key= subconcept; value= set of subconcepts overlapping with the key
	public Map<Term, Set<Term>> findOverLappingSubconcepts(Term t)
	{
		ArrayList<Term> subconcepts = new ArrayList<>(t.getSubconcepts());
		String original_label = " "+t.getLabel()+" ";
		
		Map<Term, Set<Term>> overLappingSubconcepts = new HashMap<>();
		
		for(int i=0; i< subconcepts.size(); i++)
		{
			String id_replaced_label = original_label.replaceAll("(?i)" + Pattern.quote(" "+subconcepts.get(i).getLabel()+" "), " "+subconcepts.get(i).getID()+" ");
			
			for(int j=0; j< subconcepts.size(); j++)
			{
//				if(subcon1.equals(subcon2))
//					continue;
				
				//if number of occurences of the subcon2 label is different between the original_label and id_replaced_label of subcon1, then an overlap has occured.
				if((countOccurences(original_label, subconcepts.get(j).getLabel()) - countOccurences(id_replaced_label, subconcepts.get(j).getLabel()))!=0)
				{
					if(overLappingSubconcepts.containsKey(subconcepts.get(i)))
						overLappingSubconcepts.get(subconcepts.get(i)).add(subconcepts.get(j));
					else
					{
						Set<Term> overLap = new HashSet<>();
						overLap.add(subconcepts.get(j));
						overLappingSubconcepts.put(subconcepts.get(i), overLap);
					}
					
					if(overLappingSubconcepts.containsKey(subconcepts.get(j)))
						overLappingSubconcepts.get(subconcepts.get(j)).add(subconcepts.get(i));
					else
					{
						Set<Term> overLap = new HashSet<>();
						overLap.add(subconcepts.get(i));
						overLappingSubconcepts.put(subconcepts.get(j), overLap);
					}
				}
			}	
		}
		return overLappingSubconcepts;
	}
	
	//arr: list of subconcepts needed to be permuted
	//label: label of the original term whose subconcepts exists in "arr"
	//id_replaced_labels: The "label" is replaced by ids of terms in "arr" in the order of permutations generated. The resulting replaced labels are put into this set.
	//Permutes the subconcepts in the list "arr" so that different subconcept taggings could be obtained. This is especially necessary when there are overlapping subconcepts.
	//the original label is replaced with subconcept IDs in this method.
	//different permutations change the order of the original label is replaced by the subconcept ID.
	public void permute(ArrayList<Term> arr, int index, String label, Set<String> id_replaced_labels)
	{
		if(index >= arr.size() - 1) //If at the last element - nothing left to permute
		{      
			String id_replaced_label = label;
	        for(Term subcons: arr)	//replace the label with subconcept IDs in the order specified in this permutation.
	        		id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+subcons.getLabel()+" "), " "+subcons.getID()+" ");
	        
	        id_replaced_labels.add(id_replaced_label);
	        return;
		}
		
        for(int i = index; i < arr.size(); i++)	//For each index in the sub array arr[index...end]
        { 
            //Swap the elements at indices index and i
            Term t = arr.get(index);
            arr.set(index, arr.get(i));
            arr.set(i, t);
            
            //Recurse on the sub array arr[index+1...end]
            permute(arr, index+1, label, id_replaced_labels);
            
            //Swap the elements back
            t = arr.get(index);
            arr.set(index, arr.get(i));
            arr.set(i, t);
        }
	}
	
	//For a term "t", this method replaces the words in the label of its subconcepts in "t" with their id. For example: GO_1901265: nucleoside phosphate binding =  nucleoside phosphate GO_0005488, where GO_0005488: phosphate binding is a subconcept of GO_1901265 
	//Note that there might me multiple subconcepts some of which are overlapping and multiple representations of this replacement will be maintained in such scenarios
	public void labelWithSubconceptID_2()
	{
		Set<Term> allSubconcepts;
		
		int i=0;
		for(Term t: terms)
		{
			if(!t.isValidTerm())
				continue;
			
    			allSubconcepts = t.getSubconcepts();
    			Map<Term, Set<Term>> overLappingSubconcepts = findOverLappingSubconcepts(t);
    			    			
    			for(Term sc1: allSubconcepts)
    			{
    				String id_replaced_label= " "+t.getLabel()+" ";
    				Set<Term> nonOverLappingSubconcepts_of_sc1 = new HashSet<Term>(allSubconcepts);
        			nonOverLappingSubconcepts_of_sc1.removeAll(overLappingSubconcepts.get(sc1));	//concepts that don't overlap with "sc1"
    				
        			Set<Term> already_replaced_label = new HashSet<>();	//stores sub concepts whose ids are allready replaced in term "t"
    				id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+sc1.getLabel()+" "), " "+sc1.getID()+" ");	//replacing the subconcept label with its id
    				already_replaced_label.add(sc1);
    				
    				for(Term nol_sc: nonOverLappingSubconcepts_of_sc1)
    				{
    					if(overLappingSubconcepts.get(nol_sc).size()<=1)	//if nol_sc has no overlapping subconcepts. i.e the only overlapping soncept of nol_sc is itself
    					{
    						id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+nol_sc.getLabel()+" "), " "+nol_sc.getID()+" ");
    						already_replaced_label.add(nol_sc);
    					}
    				}
    				
    				
    				ArrayList<Term> concepts_to_permute = new ArrayList<Term>(nonOverLappingSubconcepts_of_sc1);
    				concepts_to_permute.removeAll(already_replaced_label);
    				
    				if(concepts_to_permute.size()>0)
    				{
    					Set<String> replaced_labels = new HashSet<>();
    					permute(concepts_to_permute, 0, id_replaced_label, replaced_labels);
    					t.bulkAdd_label_with_subconcept_IDs(replaced_labels);
    				}
    				else
    					t.addLabel_with_subconcept_ID(id_replaced_label);
    			}
    			i++;
    			//System.out.println("# of Terms considered: "+i+"\n");
		}
	}
	
	
	
//	//for a particular term "cons", permutes the subconcepts in the list "arr" so that different subconcept taggings could be obtained. This is especially necessary when there are overlapping subconcepts.
//	//the original label is replaced with subconcept IDs in this method.
//	//different permutations change the order of the original label is replaced by the subconcept ID.
//	private void permute_and_replace_label_with_subconcept_ID(ArrayList<Term> arr, int index, Term cons)
//	{
//        if(index >= arr.size() - 1){ //If at the last element - nothing left to permute
//            
//            String id_replaced_label = " "+cons.getLabel()+" ";
//            for(Term subcons: arr)
//            		id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+subcons.getLabel()+" "), " "+subcons.getID()+" ");
//            
//            cons.addLabel_with_subconcept_ID(id_replaced_label.trim());
//            return;
//        }
//
//        for(int i = index; i < arr.size(); i++)	//For each index in the sub array arr[index...end]
//        { 
//            //Swap the elements at indices index and i
//            Term t = arr.get(index);
//            arr.set(index, arr.get(i));
//            arr.set(i, t);
//
//            //Recurse on the sub array arr[index+1...end]
//            permute_and_replace_label_with_subconcept_ID(arr, index+1, cons);
//            //Swap the elements back
//            t = arr.get(index);
//            arr.set(index, arr.get(i));
//            arr.set(i, t);
//        }
//    }
	
//	private void labelWithSubconceptID()
//	{
//		Set<Term> subconcepts;
//		String id_replaced_label;
//		
//		for(Term t: terms)
//		{
//			if(!t.isValidTerm())
//				continue;
//			
//    			subconcepts = t.getSubconcepts();
//    				
//    			ArrayList<Term> subcons = new ArrayList<>(subconcepts);
//    			
//    			//permute_and_replace_label_with_subconcept_ID(subcons, 0, t);
//			for(Term sc1: subconcepts)
//			{
//		    		id_replaced_label= " "+t.getLabel()+" ";
//		    		id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+sc1.getLabel()+" "), " "+sc1.getID()+" ");
//		    		for(Term sc2: subconcepts)
//		    		{ 
//		    			if(!sc1.equals(sc2))
//		    				id_replaced_label= id_replaced_label.replaceAll("(?i)" + Pattern.quote(" "+sc2.getLabel()+" "), " "+sc2.getID()+" ");
//		    		}
//		    		t.addLabel_with_subconcept_ID(id_replaced_label.trim());
//			}
//		}
//	}
	
	//This method fills the "elements_with_tags" of each "Term", so that SC tag will be used for subconcepts.
	private void addSubConceptTag()
	{	
		ElementList POS_tags = null;
		ElementList POS_Sub_tags;			//An ElementList with elements containing POS and SUB tags.
		Element newElement;
		Set<ElementList> elements_with_subconcept_tags;
		
		Set<String> tags;
		Term subcons;
		int subcons_size, words_index;
		String element;
		for(Term t: terms)
		{	
			if(!t.isValidTerm())
				continue;
			
			if(t.getLabel_with_subconcept_IDs().size()>0)	//if subconcepts exist
			{
				elements_with_subconcept_tags = new HashSet<ElementList>();
				elements_with_subconcept_tags.add(t.getFirstElementListOf_elements_with_tags());	//adding the already found POS tags
				
				for(String s: t.getLabel_with_subconcept_IDs())
				{	
					//elements_with_POS_tags = t.getElements_with_POS_tags();
					
					POS_Sub_tags = new ElementList();
					
					List<CoreLabel> tokens = tokenize(s);
					words_index=0;	//index in words ArrayList. Used to iterate.
					for(CoreLabel cl: tokens)
					{
						element = cl.originalText();
						if((subcons = t.getSubconceptByID((element)))!=null)	//if this token is a subconcept. i.e GO_****** for Gene Ontology
						{
							tags = new HashSet<String>();
							tags.add("SC");	//subconcept tag
							
							newElement = new Element(element, tags);
							
							subcons_size = tokenize(subcons.getLabel()).size(); //the size of tokens of the subconcept.
								
							words_index += subcons_size;		//increase the word_index by the size of the subconcept so that the next element could be obtained.
						}
						else		
						{
							POS_tags = t.getFirstElementListOf_elements_with_tags();
							
							tags = new HashSet<String>(POS_tags.getElement(words_index).getTags());
							newElement = new Element(element, tags);
							words_index++;
						}
						POS_Sub_tags.putElements(newElement);
					}
					elements_with_subconcept_tags.add(POS_Sub_tags);
				}
				t.setElements_with_tags(elements_with_subconcept_tags);
			}
		}
	}
	
	public void runSubconceptTagging() throws IOException
	{
		subconceptSearch();
		labelWithSubconceptID_2();
		//labelWithSubconceptID();
		addSubConceptTag();
	}
	
	public void writeSubconceptsToFile(String outputFile) throws IOException
    {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
	    	Set<Term> subconcepts;
	    	String line_to_write;
	    	for(Term t: terms)
	    	{
	    		line_to_write = "";
	    		line_to_write+= t.getID()+":\t";
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
		
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
    	
//		SubConceptTagging st = new SubConceptTagging("GO_labels_test.txt");
//		st.runPOSTaggin();
//		st.runSubconceptTagging();
		
		
//		st.writeTags("test/SubConceptTagging.txt");
		
//		String a= " mesenchymal stem cell differentiation involved in nephron morphogenesis stem cell differentiation ";
//		String b= " mesenchymal stem cell differentiation ";
//		
//		System.out.println(countOccurences(a, b));
		
		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed Time: "+(endTime-startTime)/1000+" seconds");
	}
}
