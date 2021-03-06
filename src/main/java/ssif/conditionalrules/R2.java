package ssif.conditionalrules;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Table;
import au.com.bytecode.opencsv.CSVWriter;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */

//R2: if x is an adjective or noun (or any part of speech specified), then <xy is_a y>
public abstract class R2 extends ConditionalRule {

	public R2(Tagging tagged_terms) {
		super(tagged_terms);
	}
	
	public R2()
	{
		
	}
	
	//checks whether the elements are equal or e1 is a subtype of e2
	private boolean isSubsume(Element e1, Element e2)
	{
		if(e1.equals(e2) || (e1.isSubconcept() && e2.isSubconcept() && term_map.get(e1.getElementName()).isParent(term_map.get(e2.getElementName()))))
			return true;
		return false;
	}

	//the most recent version of R2
	protected boolean R2_plus_modified_type(ElementList el, Set<String> modifier_tags)
	{
		//The last element should be a subconcept
		if(!el.getElement(el.getSize()-1).isSubconcept())
			return false;
		
		//The modifiers should contain any of the required tags
		for(int i=0; i< el.getSize()-1; i++)
		{
			if(modifier_tags!=null && !el.getElement(i).isAnyTagAvailable(modifier_tags))
				return false;
		}
		return true;
	}
	
	//checks whether two elements falls into the category mentioned in R2_plus_plus
	protected boolean R2_plus_plus_type(ElementList el1, ElementList el2, Set<String> modifier_tags)
	{
		Element lastElem_el1 = el1.getElement(el1.getSize()-1), lastElem_el2 = el2.getElement(el2.getSize()-1);
		
		//child ElementList should be longer in length than parent ElementList
		if(el1.getSize() <= el2.getSize())
			return false;
		//if the two last elements are not equal and el1 is not the child of el2.
		if(!isSubsume(lastElem_el1, lastElem_el2))
			return false;
		
		Table<Integer, Integer, ElementList>  glcs_table = GLCS_table(el1, el2);
		ElementList glcs = glcs_table.get(0, 0);
		if(!el2.equals(glcs))
			return false;

		
		boolean valid_uniq_found = false;
		for(int i=el1.getSize()-2; i>=0; i--)
		{
			if(glcs_table.contains(i, 0) && glcs_table.contains(i+1, 0) && glcs_table.get(i, 0).equals(glcs_table.get(i+1, 0)))
			{
				if(el1.getElement(i).isAnyTagAvailable(modifier_tags))
					valid_uniq_found = true;
				else
				{
					valid_uniq_found = false;
					break;
				}
			}
		}
		if(valid_uniq_found)
			return true;
		return false;
	}
		
	public void addRule(Term child, Term parent, String desc, String type, ArrayList<String> modifier, ArrayList<String> tags, boolean obtainExistingRules)
	{
		Inconsistency newRule;
		
		if(!obtainExistingRules && !child.isParent(parent) && child.isBelongToSameSubheirarchy(parent))
		{
			newRule = new Inconsistency(child, parent, desc, type, modifier, tags);
			if(!obtainedRules.contains(newRule))
				obtainedRules.add(newRule);
		}
		else if(obtainExistingRules && child.isParent(parent))
		{
			newRule = new Inconsistency(child, parent, desc, type, modifier, tags);
			if(!obtainedRules.contains(newRule))
				obtainedRules.add(newRule);
		}
	}
	
	public void writeToCSVFile(String output) throws IOException
	{
		CSVWriter cw = new CSVWriter(new FileWriter(output));
		String[] line = {"Left", "Right", "Description", "Modifier", "Tag"};
		cw.writeNext(line);
		for(Inconsistency or: obtainedRules)
		{
			line[0] = or.getChild().getIDLabel();
			line[1] = or.getParent().getIDLabel();
			line[2] = or.getDescription();
			line[3] = or.getModifier().toString();
			line[4] = or.getTags().toString();
			cw.writeNext(line);
		}
		cw.close();
	}
	
}
