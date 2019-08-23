

package ssif.exhaustiveSSIF.conditionalrules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import au.com.bytecode.opencsv.CSVWriter;
import ssif.conditionalrules.ConditionalRule;
import ssif.conditionalrules.Inconsistency;
import ssif.exhaustiveSSIF.tagging.AntonymTagging;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */

////R4 has two parts. 
//R4_1: If for all x such that <x is_a y> and <x is_a z>, <y intersect z> exists and <<y intersect z> is_a y> and <<y intersect z> is_a z>, then <x is_a <y intersect z>> should exist.
//R4_2: If for all x such that <x is_a y> and <x is_a z>, <y intersect z> exists and <x is_a <y intersect z>>, then <<y intersect z> is_a y> and <<y intersect z> is_a z> should exist.
public class R4_exhaustive extends ConditionalRule {

	public R4_exhaustive(Tagging tagged_terms) {
		super(tagged_terms);
	}
	
	public void test()
	{
		Term t = term_map.get("GO_1902880");
		for(ElementList el: t.getElements_with_tags())
		{
			System.out.println(el.ElementListWithTagsAsAString());
		}
	}
	
	//Efficient R4: takes around 40s
	public Set<Inconsistency> runR4(String output, boolean obtainExistingRules, String labels_file, String antonym_input, String other_antonyms_input) throws IOException	//String partOf_inputFile
	{
		AntonymTagging at = new AntonymTagging(labels_file, antonym_input, other_antonyms_input);
		at.loadWordnetAntonyms();
		at.findAntonymPairsInGO();
		
		ElementList intersect;
		String inters;
		Term intersect_term;
		Set<String> considered_y_z = new HashSet<>();	//stores already considered y and z pairs
		//Map<Term, Set<Term>> ISA_PartOf_relations = new HashMap<>();
		for(Term x: tagged_terms.getTerms())
		{
			if(!x.isValidTerm())
				continue;
			
			for(Term y: x.getAll_parents())
			{
				if(!y.isValidTerm())		
					continue;
				
				for(Term z: x.getAll_parents())
				{					
					//if an y & z pair is considered, no need to consider them again.
					if(!z.isValidTerm() || y.equals(z) || considered_y_z.contains(y.getID()+z.getID()) || considered_y_z.contains(z.getID()+y.getID()))
						continue;
					
					considered_y_z.add(y.getID()+z.getID());
					
					for(ElementList el_y:y.getElements_with_tags())
					{
						for(ElementList el_z:z.getElements_with_tags())
						{
							if((intersect = intersection(el_y, el_z))!=null)	//if intersection defined
							{
								inters ="";
								for(Element e: intersect)
								{
									if(e.isSubconcept())
										inters+=term_map.get(e.getElementName()).getLabel()+" ";
									else
										inters+=e.getElementName()+" ";
								}
							
								//if intersection exists as a term in the terminology
								if((intersect_term = label_terms.get(inters.trim())) != null && intersect_term.isValidTerm() && !y.equals(intersect_term) && !z.equals(intersect_term))	//if intersection exists as a term
								{
									Set<Term> commonChildren = y.getCommonChildren(z);							
									for(Term commonChild: commonChildren)
									{
										if(commonChild.isValidTerm() && !commonChild.equals(intersect_term))
										{
											if(commonChildren.contains(intersect_term))	//in other words intersect_term.isParent(y) && intersect_term.isParent(z)
											{
												if(!isAntonymPairExistsInTerms(commonChild, intersect_term, at))	
													addRule(commonChild, intersect_term, "R4_1 **** OBTAINED BY**** y= "+"<"+y.getIDLabel()+">"+" && z= "+"<"+z.getIDLabel()+">"+"IS_A relations: "+SLCS_ISA(el_y, el_z), "R4", obtainExistingRules);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		restrictRulesToGeneralOnes();
		if(output!=null)
		{
			writeObtainedRulesToCSVFile(output);
			//writeObtainedRulesToTXTFile(output);
		}
		return obtainedRules;
	}
	
	public String replaceUnderscoreWithColon(String inputString)
	{
		return inputString.replaceAll(":", "").replaceAll("_", ":");
	}
	
	public void writeObtainedRulesToTXTFile(String output) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		String line;
		for(Inconsistency or : obtainedRules)
		{
			line = "";
			line += replaceUnderscoreWithColon(or.getChild().getIDLabel())+" IS-A "+ replaceUnderscoreWithColon(or.getParent().getIDLabel()) +"\n\n"+or.getDescription();
			bw.write(line);
			bw.newLine();
			bw.newLine();
			bw.newLine();
		}
		bw.close();
	}
	
	//Returns the subsumption relations considered when obtaining SLCS.
	//SLCS gets the specific LCS: ABCDEF and PQRCDES, SLCS = CDEF if F is-a S
	public String SLCS_ISA(ElementList el1, ElementList el2)			// ABCDEF, PBQDF. LCS = BDF
	{
		Table<Integer, Integer, ElementList>  lcs = HashBasedTable.create();
		ElementList temp, current, lcs1, lcs2;
		Element element1, element2;
		
		String ISA_rels = "";
			
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
						
						ISA_rels += replaceUnderscoreWithColon(term_map.get(element1.getElementName()).getIDLabel())+" IS-A "+ replaceUnderscoreWithColon(term_map.get(element2.getElementName()).getIDLabel())+"****";
						
					}
					else if(term_map.get(element2.getElementName()).isParent(term_map.get(element1.getElementName())))
					{
						current = new ElementList();
						current.putElements(element2);	//consider the child
						if((temp = lcs.get(i+1, j+1))!=null)
							current.putAll(temp);
						
						lcs.put(i, j, current);
						
						ISA_rels += replaceUnderscoreWithColon((term_map.get(element2.getElementName()).getIDLabel())+" IS-A "+ replaceUnderscoreWithColon(term_map.get(element1.getElementName()).getIDLabel()))+ "****";
					}
				}
				else		//if elements are not equal and does not have a subsumption relationship.
				{
					//System.out.println("Not equal/subsumption == "+element1.getElementName()+": "+element2.getElementName());
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
		return ISA_rels;
	}
}
