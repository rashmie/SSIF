package ssif.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

//This class holds a list of elements. A label of a particular term contains many elements and this class is used to hold them.
/**
 * @author Rashmie Abeysinghe
 *
 */
public class ElementList implements Iterable<Element>, Serializable {
	
	private ArrayList<Element> list_of_elements;// = new ArrayList<Element>();

	public ElementList() {
		super();
		this.list_of_elements = new ArrayList<Element>();	
	}
	
	public ElementList(ArrayList<Element> list_of_elements) {
		super();
		this.list_of_elements = new ArrayList<Element>(list_of_elements);
		//this.list_of_elements = list_of_elements;
	}
	

	public ArrayList<Element> getList_of_elements() {
		return list_of_elements;
	}

	public void setList_of_elements(ArrayList<Element> list_of_elements) {
		this.list_of_elements = list_of_elements;
	}
	
	public void putElements(Element e)
	{
		this.list_of_elements.add(e);
	}
	public void putAll(ElementList el)
	{
		this.list_of_elements.addAll(el.getList_of_elements());
	}
	public Element getElement(int index)
	{
		return this.list_of_elements.get(index);
	}
	
	public int getSize()
	{
		return this.list_of_elements.size();
	}
	public boolean contains(Element e)
	{
		return this.list_of_elements.contains(e);
	}

	public Iterator<Element> iterator() {
		
		return list_of_elements.iterator();
	}
	
	public String ElementListAsAString()
	{
		String elementList="";
		for(Element e:list_of_elements)
		{
			elementList += e.getElementName()+" ";
		}
		return elementList.trim();
	}
	
	public String ElementListWithTagsAsAString()
	{
		String elementList="";
		for(Element e:list_of_elements)
		{
			elementList += e.getElementTag()+", ";
		}
		return elementList.replaceAll(", $", "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list_of_elements == null) ? 0 : list_of_elements.hashCode());
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
		ElementList other = (ElementList) obj;
		if (list_of_elements == null) {
			if (other.list_of_elements != null)
				return false;
		} else if (!list_of_elements.equals(other.list_of_elements))
			return false;
		return true;
	}
	
	

}
