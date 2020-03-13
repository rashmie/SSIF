package ssif.conditionalrules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import au.com.bytecode.opencsv.CSVWriter;
import ssif.exhaustiveSSIF.tagging.AntonymTagging;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Antonym;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */
public abstract class ConditionalRule {

	protected Tagging tagged_terms;
	protected Map<String, Term> term_map;
	protected Map<String, Term> label_terms;	//hashmap containing the label of a term as the key and the term as the value.
	protected Set<Inconsistency> obtainedRules;
	
	
	public ConditionalRule(Tagging tagged_terms) {
		super();
		this.tagged_terms = tagged_terms;
		//this.term_map = term_map;
		fillTermMapFromList();
		fillLabelTerms();
		obtainedRules = new HashSet<Inconsistency>();
	}
	
	public ConditionalRule()
	{
		obtainedRules = new HashSet<Inconsistency>();
	}
	
	protected void fillLabelTerms()
	{
		label_terms = new HashMap<String, Term>();
		for(Term t: tagged_terms.getTerms())
			label_terms.put(t.getLabel(), t);
	}
	
	public void fillTermMapFromList()
	{
		term_map = new HashMap<String, Term>();
		for(Term t: tagged_terms.getTerms())
			this.term_map.put(t.getID(), t);
	}
	
	public void writeObtainedRulesToTextFile(String output) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		
		for(Inconsistency or : obtainedRules)
		{
			bw.write(or.getChild().getIDLabel() +"\nIS-A\n"+ or.getParent().getIDLabel());
			bw.write(or.getDescription());
			bw.newLine();
			bw.newLine();
		}
		
		bw.close();
	}
	
	public void writeObtainedRulesToCSVFile(String output) throws IOException
	{
		CSVWriter bw = new CSVWriter(new FileWriter(output));
		String[] line = {"Left", "Right", "Description"};
		for(Inconsistency or : obtainedRules)
		{
			line[0] = or.getChild().getIDLabel();
			line[1] = or.getParent().getIDLabel();
			line[2] = or.getDescription();
			bw.writeNext(line);
		}
		bw.close();
	}
	
	//If A->B and C->B are suggested where A->C is given, then A->B is already implied transitively. So, only the more general rule C->B needs to be kept. 
	//Also if C->A and C->B are suggested where A->B is given, then C->B is already implied transitively (C->A->B). So, the more general rule C->A only have to suggested.
	public void restrictRulesToGeneralOnes()		
	{
		Inconsistency temp;
		Set<Inconsistency> rulesToRemove = new HashSet<Inconsistency>();
		
		//If A->B and C->B are suggested where A->C is given, then A->B is already implied transitively. So, only the more general rule C->B needs to be kept. 
		for(Inconsistency or : obtainedRules)
		{
			for(Term left_parent: or.getChild().getAll_parents())	
			{
				temp = new Inconsistency(left_parent, or.getParent());
				if(!rulesToRemove.contains(temp) && obtainedRules.contains(temp))
				{
					rulesToRemove.add(or);
					break;
				}
			}
			
			//If C->A and C->B are suggested where A->B is given, then C->B is already implied transitively (C->A->B). So, the more general rule C->A only have to suggested.
			for(Term right_parent: or.getParent().getAll_parents()) 
			{
				temp = new Inconsistency(or.getChild(), right_parent);
				if(!rulesToRemove.contains(temp) && obtainedRules.contains(temp))
					rulesToRemove.add(temp);
			}	
		}
		obtainedRules.removeAll(rulesToRemove);
		System.out.println("Total rules obtained: "+obtainedRules.size());
	}

	//SLCS gets the specific LCS: ABCDEF and PQRCDES, SLCS = CDEF if F is-a S
	public ElementList findSLCS(ElementList el1, ElementList el2)			// ABCDEF, PBQDF. LCS = BDF
	{
		Table<Integer, Integer, ElementList>  lcs = HashBasedTable.create();
		ElementList temp, current, lcs1, lcs2;
		Element element1, element2;
			
		//System.out.println(el1.ElementListAsAString()+": "+el2.ElementListAsAString());
		for(int i=el1.getSize()-1; i>=0; i--)
		{
			for(int j=el2.getSize()-1; j>=0; j--)
			{
//				element1 = el1.getElement(i);
//				element2 = el2.getElement(j);
				if((element1 = el1.getElement(i))==null || (element2 = el2.getElement(j))==null)
					continue;
				
				else if(element1.getElementName().equals(element2.getElementName()))		//if two elements are equal
				{
					//System.out.println("Equal == "+element1.getElementName()+": "+element2.getElementName());
					current = new ElementList();
					current.putElements(element1);
					if((temp=lcs.get(i+1, j+1))!=null)
					{
						current.putAll(temp);
//						lcs.put(i, j, current);
					}
					lcs.put(i, j, current);
				}
				else if(element1.isSubconcept() && element2.isSubconcept())			//if two elements have a subsumption relationship
				{
					//System.out.println("Subsumption == "+element1.getElementName()+": "+element2.getElementName());
					if(term_map.get(element1.getElementName()).isParent(term_map.get(element2.getElementName())))
					{
						current = new ElementList();
						current.putElements(element1);	//consider the child
						if((temp = lcs.get(i+1, j+1))!=null)
							current.putAll(temp);
						
						lcs.put(i, j, current);
						
					}
					else if(term_map.get(element2.getElementName()).isParent(term_map.get(element1.getElementName())))
					{
						current = new ElementList();
						current.putElements(element2);	//consider the child
						if((temp = lcs.get(i+1, j+1))!=null)
							current.putAll(temp);
						
						lcs.put(i, j, current);
					}
				}
				else		//if elements are not equal and does not have a subsumption relationship.
				{
					if((lcs1 = lcs.get(i, j+1))!=null && (lcs2 = lcs.get(i+1, j))!=null)
					{
						if((lcs1.getSize() >= lcs2.getSize()))
							lcs.put(i, j, lcs1);
						else
							lcs.put(i, j, lcs2);
					}
					else if(lcs1!=null)
						lcs.put(i, j, lcs1);
					else if((lcs2 = lcs.get(i+1, j))!=null)
						lcs.put(i, j, lcs2);
				}
			}
		}
		return lcs.get(0, 0);
	}
	
	//returns the table that is constructed during LCS calculation
	public Table<Integer, Integer, ElementList> GLCS_table(ElementList el1, ElementList el2)
	{
		Table<Integer, Integer, ElementList>  lcs = HashBasedTable.create();
		ElementList temp, current, lcs1, lcs2;
		Element element1, element2;
			
		for(int i=el1.getSize()-1; i>=0; i--)
		{
			for(int j=el2.getSize()-1; j>=0; j--)
			{
//				element1 = el1.getElement(i);
//				element2 = el2.getElement(j);
				if((element1 = el1.getElement(i))==null || (element2 = el2.getElement(j))==null)
					continue;
				
				else if(element1.getElementName().equals(element2.getElementName()))		//if two elements are equal
				{
					current = new ElementList();
					current.putElements(element1);
					if((temp=lcs.get(i+1, j+1))!=null)
					{
						current.putAll(temp);
						//lcs.put(i, j, current);
					}
					lcs.put(i, j, current);
				}
				else if(element1.isSubconcept() && element2.isSubconcept())			//if two elements have a subsumption relationship
				{
					//System.out.println("Subsumption == "+element1.getElementName()+": "+element2.getElementName());
					if(term_map.get(element1.getElementName()).isParent(term_map.get(element2.getElementName())))
					{
						current = new ElementList();
						current.putElements(element2);	//consider the parent
						if((temp = lcs.get(i+1, j+1))!=null)
							current.putAll(temp);
						
						lcs.put(i, j, current);
						
					}
					else if(term_map.get(element2.getElementName()).isParent(term_map.get(element1.getElementName())))
					{
						current = new ElementList();
						current.putElements(element1);	//consider the parent
						if((temp = lcs.get(i+1, j+1))!=null)
							current.putAll(temp);
						
						lcs.put(i, j, current);
					}
				}
				else		//if elements are not equal and does not have a subsumption relationship.
				{
					if((lcs1 = lcs.get(i, j+1))!=null && (lcs2 = lcs.get(i+1, j))!=null)
					{
						if((lcs1.getSize() >= lcs2.getSize()))
							lcs.put(i, j, lcs1);
						else
							lcs.put(i, j, lcs2);
					}
					else if(lcs1!=null)
						lcs.put(i, j, lcs1);
					else if((lcs2 = lcs.get(i+1, j))!=null)
						lcs.put(i, j, lcs2);
				}
			}
		}
		//System.out.println(lcs.size());
		return lcs;
	}
	
	//GLCS gets the general LCS: ABCDEF and PQRCDES, GLCS = CDES if F is-a S
	public ElementList findGLCS(ElementList el1, ElementList el2)			// ABCDEF, PBQDF. LCS = BDF
	{
		Table<Integer, Integer, ElementList>  lcs = GLCS_table(el1, el2);
		return lcs.get(0, 0);
	}
	
	public Element intersectionOfTwoElements(Element e1, Element e2)
	{	
		if(e1.getElementName().equals(e2.getElementName()))
			return e1;
		else if(e1.isSubconcept() && e2.isSubconcept())
		{
			if(term_map.get(e1.getElementName()).isParent(term_map.get(e2.getElementName())))
				return e1;
			else if(term_map.get(e2.getElementName()).isParent(term_map.get(e1.getElementName())))
				return e2;
			else
				return null;
		}
		else
			return null;
		
	}
	
	public ElementList intersection(ElementList x, ElementList y)
	{
		ElementList intersect = new ElementList();
		Element intersectOfTwoElements;
		ElementList slcs;
		
		//System.out.println(x.ElementListAsAString()+": "+y.ElementListAsAString());
		
		if((slcs = findSLCS(x,y))!=null)
		{
			if((x.getSize() == y.getSize()) && (x.getSize() == slcs.getSize()))	//lengths of x and y and their LCS is equal
			{
				for(int i=0; i<x.getSize();i++)
				{
					if((intersectOfTwoElements = intersectionOfTwoElements(x.getElement(i), y.getElement(i)))!=null)	//this always happens. When x=y=lcs(x,y), each element of x and y are equal or they have a subsumtion relationship between them.
					{
						//System.out.println(x.getElement(i).getElementName()+" ** "+y.getElement(i).getElementName()+" ** "+intersectOfTwoElements.getElementName());
						intersect.putElements(intersectOfTwoElements);
					}
//					else
//						return null;
				}
			}
			else if(slcs.getSize() == x.getSize() && slcs.getSize() < y.getSize())
			{
				boolean parent_found = false;
				for(Element y_element: y)
				{
					if(slcs.contains(y_element))
						intersect.putElements(y_element);
					
					else if(y_element.isSubconcept())
					{
						for(Element lcs_element:slcs)
						{
							if(lcs_element.isSubconcept() && term_map.get(lcs_element.getElementName()).isParent(term_map.get(y_element.getElementName())))
							{
								intersect.putElements(lcs_element);
								parent_found = true;
								break;
							}
						}
						if(!parent_found)
						{
							intersect.putElements(y_element);
						}
					}
					else 
					{
						intersect.putElements(y_element);
					}
				}
			}
			else if(slcs.getSize() == y.getSize() && slcs.getSize() < x.getSize())
			{
				boolean parent_found = false;
				for(Element x_element: x)
				{
					if(slcs.contains(x_element))
						intersect.putElements(x_element);
					
					else if(x_element.isSubconcept())
					{
						for(Element lcs_element:slcs)
						{
							if(lcs_element.isSubconcept() && term_map.get(lcs_element.getElementName()).isParent(term_map.get(x_element.getElementName())))
							{
								intersect.putElements(lcs_element);
								parent_found = true;
								break;
							}
						}
						if(!parent_found)
						{
							intersect.putElements(x_element);
						}
					}
					else 
					{
						intersect.putElements(x_element);
					}
				}
			}
			else if(slcs.getSize() != y.getSize() && slcs.getSize() != x.getSize())
			{
				//System.out.println(x.ElementListAsAString()+": "+y.ElementListAsAString()+" ** "+lcs.ElementListAsAString());
			}
			else
				return null;
		}
		if (intersect.getSize()==0)
			return null;
		return intersect;
	}
	
	public Element unionOfTwoElements(Element e1, Element e2)
	{	
		if(e1.getElementName().equals(e2.getElementName()))
			return e1;
		else if(e1.isSubconcept() && e2.isSubconcept())
		{
			if(term_map.get(e1.getElementName()).isParent(term_map.get(e2.getElementName())))
				return e2;
			else if(term_map.get(e2.getElementName()).isParent(term_map.get(e1.getElementName())))
				return e1;
			else
				return null;
		}
		else
			return null;
		
	}
	
	public ElementList union(ElementList x, ElementList y)
	{
		ElementList union = new ElementList();
		Element unionOfTwoElements;
		ElementList glcs;
		
		if((glcs = findGLCS(x,y))!=null)
		{
			if((x.getSize() == y.getSize()) && (x.getSize() == glcs.getSize()))	//lengths of x and y and their GLCS is equal
			{
				for(int i=0; i<x.getSize();i++)
				{
					if((unionOfTwoElements = unionOfTwoElements(x.getElement(i), y.getElement(i)))!=null)	//this always happens. When x=y=lcs(x,y), each element of x and y are equal or they have a subsumption relationship between them.
					{
						//System.out.println(x.getElement(i).getElementName()+" ** "+y.getElement(i).getElementName()+" ** "+intersectOfTwoElements.getElementName());
						union.putElements(unionOfTwoElements);
					}
//					else
//						return null;
				}
			}
			else if((glcs.getSize() == x.getSize() && glcs.getSize() < y.getSize()) || (glcs.getSize() == y.getSize() && glcs.getSize() < x.getSize()))
				union.putAll(glcs);

			else if(glcs.getSize() != y.getSize() && glcs.getSize() != x.getSize())
				return null;
			else
				return null;
		}
		if (union.getSize()==0)
			return null;
		return union;
	}
	
	public void addRule(Term child, Term parent, String desc, String type, boolean obtainExistingRules)
	{	
		if(!obtainExistingRules && !child.equals(parent) && !child.isParent(parent) && child.isBelongToSameSubheirarchy(parent))
		{
			Inconsistency newRule;
			newRule = new Inconsistency(child, parent, desc, type);
			if(!obtainedRules.contains(newRule))
				obtainedRules.add(newRule);
		}
		else if(obtainExistingRules && child.isParent(parent))
		{
			Inconsistency newRule;
			newRule = new Inconsistency(child, parent, desc, type);
			if(!obtainedRules.contains(newRule))
				obtainedRules.add(newRule);
		}
	}
	
	public int getStartIndexOfSubConcept(ElementList el_t, int subconcept_element_index)
	{
		String term_label_with_subconceptID = "";
		for(int i=0; i< el_t.getSize(); i++)
		{
			if(!el_t.getElement(i).isSubconcept() || i==subconcept_element_index)
				term_label_with_subconceptID += el_t.getElement(i).getElementName()+" ";
			else
				term_label_with_subconceptID += term_map.get(el_t.getElement(i).getElementName()).getLabel()+" ";
		}
		term_label_with_subconceptID = term_label_with_subconceptID.trim();
		//System.out.println(term_label_with_subconceptID);
		//if(el_t.getElement(subconcept_element_index).getElementName().equals("GO_0016020"))
		//	System.out.println(el_t.ElementListAsAString()+"\t|| "+subconcept_element_index+"\t||"+term_label_with_subconceptID+"\t||"+term_label_with_subconceptID.indexOf(el_t.getElement(subconcept_element_index).getElementName()));
		//starting and ending string indexes of the specific subconcept ID (specified by subconcept_element_index of the ElementList el_t)
		return term_label_with_subconceptID.indexOf(el_t.getElement(subconcept_element_index).getElementName());
	}
	

	//this method return true if there exists an antonym pair in the terms passed as parameters
	public boolean isAntonymPairExistsInTerms(Term t1, Term t2, AntonymTagging at) throws IOException
	{
		for(ElementList el1: t1.getElements_with_tags())
		{
			for(ElementList el2: t2.getElements_with_tags())
			{
				//if(t1.getID().equals("GO_1902879") && t2.getID().equals("GO_1902880"))
					//System.out.println(el1.ElementListAsAString()+"\t:"+el2.ElementListAsAString()+"\t***"+isAntonymPairExistsInElementLists(el1, el2, at));
				if(isAntonymPairExistsInElementLists(el1, el2, at))
					return true;
			}
		}
		return false;
	}
	
	//this method return true if there exists an antonym pair in the elementlists passed as parameters
	public boolean isAntonymPairExistsInElementLists(ElementList el1, ElementList el2, AntonymTagging at) throws IOException
	{
		Set<String> antonyms_found_el1 = new HashSet<>();
		for(Element e: el1)
		{
			if(e.isTagAvailable("ANT"))
				antonyms_found_el1.add(e.getElementName());
		}
		
		Set<String> antonyms_found_el2 = new HashSet<>();
		for(Element e: el2)
		{
			if(e.isTagAvailable("ANT"))
				antonyms_found_el2.add(e.getElementName());
		}
		
		if(antonyms_found_el1.isEmpty() || antonyms_found_el2.isEmpty())
			return false;
		
		Map<String, Antonym> anto_pairs = at.getGO_antonym_pairs();
		for(String ant: antonyms_found_el1)
		{
			for(String opposite: anto_pairs.get(ant).getOpposites())
			{
				if(antonyms_found_el2.contains(opposite))
				{
					//System.out.println(ant+": "+opposite+"***"+el1.ElementListAsAString()+"\t | "+el2.ElementListAsAString());
					return true;
				}
			}
		}
		return false;
	}
}
