package ssif.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

//This class holds an element and its tags. Antonyms have the ANT tag in addition to their POS tag.
/**
 * @author Rashmie Abeysinghe
 *
 */
public class Element implements Serializable {
	
	private String name;
	private Set<String> tags; //= new HashSet<String>();	//an element can have multiple tags (two, if it is an antonym)
	
	public Element(String element, Set<String> tags) {
		super();
		this.name = element;
		this.tags = tags;
	}
	public String getElementName() {
		return name;
	}
	public void setElementName(String element) {
		this.name = element;
	}
	public Set<String> getTags() {
		return tags;
	}
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	public boolean isTagAvailable(String tag)
	{
		return tags.contains(tag);
	}
	
	public String getElementTag()
	{
		return this.name+"/"+this.tags;
	}
	
	//this method checks whether the element contains any tag in the Set passed as parameter
	public boolean isAnyTagAvailable(Set<String> tags)
	{
		for(String tag: tags)
		{
			if(isTagAvailable(tag))
				return true;
		}
		return false;
	}
	
	public boolean isSubconcept()
	{
		return isTagAvailable("SC");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
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
		Element other = (Element) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		return true;
	}
	
}
