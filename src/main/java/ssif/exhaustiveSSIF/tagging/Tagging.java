package ssif.exhaustiveSSIF.tagging;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class Tagging extends AntonymTagging implements Serializable {


	public Tagging(String labels_file, String wordnetAntonymFile, String antoFile, String taggerModel_file) {
		super(labels_file, wordnetAntonymFile, antoFile, taggerModel_file);
		// TODO Auto-generated constructor stub
	}

	public void runTagging() throws IOException
	{
		runPOSTaggin();
		System.out.println("POS Tagging Done!");
		runSubconceptTagging();
		System.out.println("Subconcept Tagging Done!");
		runAntonymTagging();
		System.out.println("Antonym Tagging Done!");
 
	}
	
   public static void serializeTagging(Tagging tagged, String output)
    {
	    	try{
	    		FileOutputStream fos = new FileOutputStream(output);
	    		ObjectOutputStream oos = new ObjectOutputStream(fos);
	    		oos.writeObject(tagged);
	    		oos.close();
	    		fos.close();
	    	}catch(IOException ioe){
	    		ioe.printStackTrace();
	    	}
    }
    
    public static Tagging deserializeTagging(String input)
    {
	    	Tagging tagged = null;
	    	try{
	    		FileInputStream fis = new FileInputStream(input); 
	    		ObjectInputStream ois = new ObjectInputStream(fis);
	    		tagged = (Tagging)ois.readObject();
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
	    	return tagged;
    }
}
