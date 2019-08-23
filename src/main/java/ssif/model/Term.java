

package ssif.model;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class Term implements Serializable{
	private String ID;
	private String label;
	
	private boolean validTerm;		//whether this term is a term which is considered in SSIF. Only terms with alphanumeric characters are considered. However to calculate transitive closure, all the terms are needed. To differentiate between valid and invalid, this variable is used.
	
	private Set<ElementList> elements_with_tags;	//contains a list of elements for each subconcept ID replacement.

	private Set<Term> subconcepts;
	private Set<String> label_with_subconcept_IDs;		//the label of the term with different subconcept ID replacements.
	
	private Set<Term> immediate_parents;
	private Set<Term> all_parents;
	private Set<Term> all_children;
	private Term subhierarchyRoot;	//root of the subhierarchy. ex:- for GO roots are cellular_component, biological_process, molecular_function
	
	public Term(String iD, String label, Set<ElementList> elements_with_tags) //constructor for valid terms
	{
		super();
		ID = iD;
		this.label = label;
		this.elements_with_tags = elements_with_tags;
		immediate_parents = new HashSet<Term>();
		all_parents = new HashSet<Term>();
		all_children = new HashSet<Term>();
		subconcepts = new HashSet<Term>();
		label_with_subconcept_IDs = new HashSet<String>();
		validTerm = true;
	}
	
	
	
	public Term(String iD, String label) {		//constructor for invalid terms
		super();
		ID = iD;
		this.label = label;
		immediate_parents = new HashSet<Term>();
		all_parents = new HashSet<Term>();
		all_children = new HashSet<Term>();
		validTerm = false;
	}


	

	public Set<ElementList> getElements_with_tags() {
		return elements_with_tags;
	}



	public void setElements_with_tags(Set<ElementList> elements_with_tags) {
		this.elements_with_tags = elements_with_tags;
	}


	public ElementList getFirstElementListOf_elements_with_tags()
	{
		ElementList first = null;
		for(ElementList el: elements_with_tags)
		{
			first = el;
			break;
		}
		return first;
	}

	public Set<Term> getImmediate_parents() {
		return immediate_parents;
	}


	public void setImmediate_parents(Set<Term> immediate_parents) {
		this.immediate_parents = immediate_parents;
	}

	public Set<Term> getAll_parents() {
		return all_parents;
	}

	public void setAll_parents(Set<Term> all_parents) {
		this.all_parents = all_parents;
	}
	
	public void addChild(Term child)
	{
		this.all_children.add(child);
	}
	
	public Set<Term> getAll_children() {
		return all_children;
	}
	
	public boolean isParent(Term t)
	{
		return all_parents.contains(t);
	}
	
	public boolean isChild(Term t)
	{
		return all_children.contains(t);
	}

	public Term getSubhierarchyRoot() {
		return subhierarchyRoot;
	}

	public void setSubhierarchyRoot(Term subhierarchyRoot) {
		this.subhierarchyRoot = subhierarchyRoot;
	}
	
	public String getID() 
	{
		return ID;
	}

	public String getLabel() 
	{
		return label;
	}
	
	public String getIDLabel()
	{
		return ID+": "+label;
	}
	
	public boolean isValidTerm()
	{
		return this.validTerm;
	}
	
	public Set<Term> getSubconcepts() 
	{
		return subconcepts;
	}
	
	public void addSubconcept(Term t)
	{
		this.subconcepts.add(t);
	}
	
	public ArrayList<String> getSubconceptIDList()
	{
		ArrayList<String> subconceptIDList = new ArrayList<String>();
		
		for(Term t: this.subconcepts)
		{
			subconceptIDList.add(t.getID());
		}
		
		return subconceptIDList;
	}
	
	public Term getSubconceptByID(String ID)
	{
		Term t=null;
		
		for(Term sub: this.getSubconcepts())
		{
			if(sub.getID().equals(ID))
				t = sub;
		}
		return t;
	}

	public Set<String> getLabel_with_subconcept_IDs() 
	{
		return label_with_subconcept_IDs;
	}
	
	public void addLabel_with_subconcept_ID(String label_with_subconcept_ID) 
	{
		this.label_with_subconcept_IDs.add(label_with_subconcept_ID);
	}
	
	public void bulkAdd_label_with_subconcept_IDs(Set<String> label_with_subconcept_ids)
	{
		label_with_subconcept_IDs.addAll(label_with_subconcept_ids);
	}
	
	public Set<Term> retrieveAllParents()	//recursive method to retrieve all the parents of a Term. The parents of parents etc. found along the way are also saved and tracked so that recalculation is not needed.
	{
		Set<Term> parent_set = new HashSet<Term>();
		Set<Term> temp;
		
		if(this.getImmediate_parents().isEmpty())
			return parent_set;
		else
		{
			for(Term c: this.getImmediate_parents())
			{
				parent_set.add(c);
				if(c.getAll_parents().isEmpty())				//if all the transitive parents have not been found before
				{
					temp = c.retrieveAllParents();
					if(!temp.isEmpty()) //only if there are parents
					{
						c.setAll_parents(temp);				//setting all parents here so that no need to calculate again for this concept.
						parent_set.addAll(temp);
					}
				}
				else
					parent_set.addAll(c.getAll_parents());
				
				//parent_set.addAll(retrieveAllParents(c));
			}
			return parent_set;
		}
	}
	
	public Set<Term> getCommonChildren(Term t)
	{
		Set<Term> commonChildren = new HashSet<Term>(this.all_children);
		commonChildren.retainAll(t.all_children);
		
		if(commonChildren.isEmpty())
			return null;
		return commonChildren;
	}
	
	public Set<Term> getCommonParents(Term t)
	{
		Set<Term> commonParents = new HashSet<Term>(this.all_parents);
		commonParents.retainAll(t.all_parents);
		
		if(commonParents.isEmpty())
			return null;
		return commonParents;
	}
	
	public boolean isBelongToSameSubheirarchy(Term t)
	{
		if(t.getSubhierarchyRoot()!=null && t.getSubhierarchyRoot().equals(this))		//if this is the subhierarchy root
			return true;
		else if(t.getSubhierarchyRoot()==null && this.subhierarchyRoot.equals(t))		//if t is the subhierarchy root
			return true;
		else if(this.subhierarchyRoot==null || t.getSubhierarchyRoot()==null)	//probably obsolete concepts
			return false;
		else
			return this.subhierarchyRoot.equals(t.getSubhierarchyRoot());		//compare subhierarchy roots
	}
	
    public static void serializeItemList(ArrayList<Term> terms, String output)
    {
	    	try{
	    		FileOutputStream fos = new FileOutputStream(output);
	    		ObjectOutputStream oos = new ObjectOutputStream(fos);
	    		oos.writeObject(terms);
	    		oos.close();
	    		fos.close();
	    	}catch(IOException ioe){
	    		ioe.printStackTrace();
	    	}
    }
    
    public static ArrayList<Term> deserializeItemList(String input)
    {
	    	ArrayList<Term> terms = new ArrayList<Term>();
	    	try{
	    		FileInputStream fis = new FileInputStream(input); 
	    		ObjectInputStream ois = new ObjectInputStream(fis);
	    		terms = (ArrayList)ois.readObject();
	    		ois.close();
	    		fis.close();
	    	}
	    	catch(IOException ioe){
	    		ioe.printStackTrace();
	    	}
	    	catch(ClassNotFoundException c){
	    		System.out.println("Class not found");
	            c.printStackTrace();
	    	}
	    	return terms;
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		return true;
	}
    
    
}
