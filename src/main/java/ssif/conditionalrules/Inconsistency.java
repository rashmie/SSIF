package ssif.conditionalrules;

import java.util.ArrayList;
import ssif.model.Term;

//this class holds a mined conditional rule
/**
 * @author Rashmie Abeysinghe
 *
 */
//This class is used to save a potential inconsistency: A missing or an incorrect subtype relation
public class Inconsistency {
	
	private Term child;
	private Term parent;
	private String description;		// stores a description of the rule. How it was generated etc. Used for manual inspection so generally this is written to a file
	private String ruleType;		//whether its R1, R2, R3 or R4
	private ArrayList<String> tags;		//this is specifically for R2 to store the modifier tags
	private ArrayList<String> modifier;			//this is specifically for R2 to store the modifier
	
	public Inconsistency(Term child, Term parent, String description, String ruleType) throws IllegalArgumentException {
		
		if(child.getSubhierarchyRoot().equals(parent.getSubhierarchyRoot()))
		{
			this.child = child;
			this.parent = parent;
			this.description = description;
			this.ruleType = ruleType;
		}
		else
			throw new IllegalArgumentException("Terms do not belong to the same subhierarchy!!");
			
	}
	
	//constructor for rules obtained by R2
	public Inconsistency(Term child, Term parent, String description, String ruleType, ArrayList<String> modifier, ArrayList<String> tags) throws IllegalArgumentException {
			this.child = child;
			this.parent = parent;
			this.description = description;
			this.ruleType = ruleType;
			this.modifier = modifier;
			this.tags = tags;
	}

	public Inconsistency(Term child, Term parent) {
		super();
		this.child = child;
		this.parent = parent;
	}
	
	public Inconsistency(Term child, Term parent, String description) {
		super();
		this.child = child;
		this.parent = parent;
		this.description = description;
	}

	public Term getChild() {
		return child;
	}

	public Term getParent() {
		return parent;
	}

	public String getDescription() {
		return description;
	}

	public String getRuleType() {
		return ruleType;
	}


	public ArrayList<String> getTags() {
		return tags;
	}

	public ArrayList<String> getModifier() {
		return modifier;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		Inconsistency other = (Inconsistency) obj;
		if (child == null) {
			if (other.child != null)
				return false;
		} else if (!child.equals(other.child))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}
}
