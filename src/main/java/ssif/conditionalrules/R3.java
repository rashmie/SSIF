package ssif.conditionalrules;

import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */

//R3: If <a is_a b> & <x is_a y> then, <ax is_a by>. Also considers <a is_a a> & <b is_a y> => <ab is_a ay> and <a is_a x> & <b is_a b> => <ab is_a xb>
public abstract class R3 extends ConditionalRule {
	
	public R3(Tagging tagged_terms) {
		super(tagged_terms);
		
	}
	
	public Term concatenationExist(Term t1, Term t2)
	{
		Term concat=null;
		concat = label_terms.get(t1.getLabel()+" "+t2.getLabel());
		return concat;
	}

	
	//el1: ElementList of the child, el2: ElementList of the parent
	//checks whether two ElementLists have equal elements or elements in a subsumption relation.
	//i.e e1 = abc &  e2 = def, if a=d, <b is_a e> & <c is_a f>, then this method returns true. Note that the positional correspondence is considered
	public boolean isR3Type(ElementList el1, ElementList el2)		
	{
		boolean r3 = false;
		//boolean antonyms = false;
		
		//HashMap<String, Antonym> anto_pairs = tagged_terms.getGO_antonym_pairs();
		String element1_name, element2_name;
		
		for(int i=0; i<el1.getSize();i++)
		{
			Element element1 = el1.getElement(i);
			Element element2 = el2.getElement(i);
			
			element1_name = element1.getElementName();
			element2_name = element2.getElementName();
			
			//if elements are equal
			if(element1_name.equals(element2_name))	
			{
				r3 = true;
				continue;
			}

			//if the elements have a subsumption relation in between
			else if(element1.isSubconcept() && element2.isSubconcept())	
			{
				if(term_map.get(element1_name).isParent(term_map.get(element2_name)))
				{
					r3 = true;
					continue;
				}
				else
				{
					r3 = false;
					break;
				}
			}
			else
			{
				r3 = false;
				break;
			}
		}
		return r3;
	}
}
