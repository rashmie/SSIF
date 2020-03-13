package ssif.exhaustiveSSIF.conditionalrules;

import java.io.IOException;
import java.util.Set;

import ssif.conditionalrules.Inconsistency;
import ssif.conditionalrules.R2;
import ssif.exhaustiveSSIF.tagging.AntonymTagging;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */

//R2: if x is an adjective or noun (or any part of speech specified), then <xy is_a y>
public class R2_exhaustive extends R2 {

	public R2_exhaustive(Tagging tagged_terms) {
		super(tagged_terms);
	}
	
	//the most recent version of R2
	//xabcy is_a y when, x,a,b,c,.. has either of NN,JJ,SC tags
	public Set<Inconsistency> runR2(String output, Set<String> modifier_tags, boolean obtainExistingRules, String antonym_input, String other_antonyms_input, String labels_file, String taggerModel_file) throws IOException	//String input_externalOntologyConceptLabels, String partOf_inputFile
	{
		AntonymTagging at = new AntonymTagging(labels_file, antonym_input, other_antonyms_input, taggerModel_file);
		at.loadWordnetAntonyms();
		at.findAntonymPairsInGO();
		
		Term potential_parent;
		for(Term t:this.tagged_terms.getTerms())
		{
			if(t.isValidTerm())
			{
				for(ElementList el: t.getElements_with_tags())
				{
					potential_parent = term_map.get(el.getElement(el.getSize()-1).getElementName());
					if(R2_plus_modified_type(el, modifier_tags) && !t.isParent(potential_parent))// && !isOverlappingTerms_ExternalOntology2(t, el, el.getSize()-1, input_externalOntologyConceptLabels))// && !isOverlappingTerms_SameOntology2(t, el, el.getSize()-1))// && !isOverlappingTerms_ExternalOntology2(t, el, el.getSize()-1, input_externalOntologyConceptLabels))	//from the evaluation: if the potential parent has overlapping subconcepts, the suggested relation is more likely false
					{
						if(!isAntonymPairExistsInTerms(t, potential_parent, at))
							addRule(t, potential_parent, el.ElementListWithTagsAsAString(), "R2_plus_modified", obtainExistingRules);
						else
							System.out.println(t.getIDLabel()+"\t"+potential_parent.getIDLabel());
					}
				}
			}
		}
		restrictRulesToGeneralOnes();
		if(output!=null)
			writeObtainedRulesToCSVFile(output);
		return obtainedRules;
	}
}
