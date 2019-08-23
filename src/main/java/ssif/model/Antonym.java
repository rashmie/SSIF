package ssif.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class Antonym implements Serializable {
	
	private String name;
	private Set<String> opposites;
	
	public Antonym(String name) {
		super();
		this.name = name;
		this.opposites = new HashSet<String>();
	}
	
	public Antonym(String name, String opposite) {
		super();
		this.name = name;
		this.opposites = new HashSet<String>();
		this.opposites.add(opposite);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<String> getOpposites() {
		return opposites;
	}
	public void setOpposites(Set<String> opposites) {
		this.opposites = opposites;
	}
	
	public void putOpposite(String opposite)
	{
		this.opposites.add(opposite);
	}
	public boolean isOpposite(String s)
	{
		return this.opposites.contains(s);
	}

}
