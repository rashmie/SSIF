package ssif.exhaustiveSSIF.conditionalrules;

import java.io.IOException;
import java.util.ArrayList;
import ssif.conditionalrules.R3;
import ssif.exhaustiveSSIF.tagging.AntonymTagging;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */

//R3: If <a is_a b> & <x is_a y> then, <ax is_a by>. Also considers <a is_a a> & <b is_a y> => <ab is_a ay> and <a is_a x> & <b is_a b> => <ab is_a xb>
public class R3_exhaustive extends R3 {
	
	public R3_exhaustive(Tagging tagged_terms) {
		super(tagged_terms);
		
	}

	public void runR3(String output, boolean obtainExistingRules, String labels_file, String antonym_input, String other_antonyms_input) throws IOException		//String input_externalOntologyConceptLabels, String partOf_inputFile
	{
		AntonymTagging at = new AntonymTagging(labels_file, antonym_input, other_antonyms_input);
		at.loadWordnetAntonyms();
		at.findAntonymPairsInGO();
		
		ArrayList<Term> taggedTerms = this.tagged_terms.getTerms();
		
		for(int i=0; i< taggedTerms.size(); i++)
		{
			Term term1 = taggedTerms.get(i);
			
			if(!term1.isValidTerm())
				continue;
			
			for(ElementList term1EL: term1.getElements_with_tags())
			{
				loop_j:
				for(int j=i+1; j< taggedTerms.size(); j++)
				{
					Term term2 = taggedTerms.get(j);
					
					if(!term2.isValidTerm())
						continue;
						
					for(ElementList term2EL: term2.getElements_with_tags())
					{
						if(term1EL.getSize()==term2EL.getSize() && isR3Type(term1EL, term2EL) && !term1.isParent(term2))// && !isOverlapping(term1, term1EL, term2, term2EL, input_externalOntologyConceptLabels))	//ElementList should be of same size
						{
							if(!isAntonymPairExistsInElementLists(term1EL, term2EL, at))	
								addRule(term1, term2, term1EL.ElementListAsAString()+" IS-A "+term2EL.ElementListAsAString(), "R3_plus", obtainExistingRules);
							else
								System.out.println(term1.getIDLabel()+"\t"+ term2.getIDLabel());
							continue loop_j;
						}
						
						if(term1EL.getSize()==term2EL.getSize() && isR3Type(term2EL, term1EL) && !term2.isParent(term1))// && !isOverlapping(term2, term2EL, term1, term1EL, input_externalOntologyConceptLabels))	//ElementList should be of same size
						{
							if(!isAntonymPairExistsInElementLists(term2EL, term1EL, at))		
								addRule(term2, term1, term2EL.ElementListAsAString()+" IS-A "+term1EL.ElementListAsAString(), "R3_plus", obtainExistingRules);
							else
								System.out.println(term2.getIDLabel()+"\t"+ term1.getIDLabel());
							continue loop_j;
						}
					}
				}
			}
		}
		restrictRulesToGeneralOnes();
		writeObtainedRulesToCSVFile(output);
	}
}
