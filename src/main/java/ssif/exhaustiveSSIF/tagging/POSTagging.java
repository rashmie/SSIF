package ssif.exhaustiveSSIF.tagging;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import ssif.model.Element;
import ssif.model.ElementList;
import ssif.model.Term;

/**
 * @author Rashmie Abeysinghe
 *
 */
public class POSTagging implements Serializable {
	
	private final transient String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        
	private final transient TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
	private final transient LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);
    
	protected String labels_file;
	protected String taggerModel_file;
	protected ArrayList<Term> terms;

	public POSTagging()
	{
		
	}
	protected POSTagging(String labels_file, String taggerModel_file) {
		super();
		this.labels_file = labels_file;
		this.taggerModel_file = taggerModel_file;
	}
	
	public void setTerms(ArrayList<Term> terms) {
		this.terms = terms;
	}
	

	public ArrayList<Term> getTerms() {
		return terms;
	}

	protected Tree parse(String str) {                
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    protected List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(new StringReader(str));    
        return tokenizer.tokenize();
    }
     
    protected void runPOSTaggin() throws IOException
    {
	    	BufferedReader br = new BufferedReader(new FileReader(labels_file));
	    	MaxentTagger tagger = new MaxentTagger(taggerModel_file);
	    	
	    	terms = new ArrayList<Term>();
	    	
	    	String line, label, tagged;
	    	String[] tokens;
	    	Element newElement;
	    	ElementList elements_with_POS_tags;
	    	Set<ElementList> elements_with_tags;
	    	Set<String> tag;
	    	Term newTerm;
	    	
	    	Pattern p = Pattern.compile("[^a-zA-Z0-9\\s\\-\\_]");		//do not match alphanumeric characters, spaces, dashes and underscores
	    	Matcher m;
	    	    	
	    	while((line=br.readLine())!=null)
	    	{
	    		tokens=line.split("\t");
	    		if(tokens.length>1)
	    		{
	    			m = p.matcher(tokens[1]);
	    			label = tokens[1].replace("_", " ");		//roots of GO sub-hierarchies are specified as cellular_compenent, biological_process, molecular_function (with underscores). We are splitting by spaces, hence, they need to be converted.
	    			if(!m.find())	//valid term
	    			{
	    				elements_with_POS_tags = new ElementList();
	    				
	    				tagged = tagger.tagString(label);
	    				String[] tokens2 = tagged.split(" ");	//each token after splitting is a word_tag pair: protein_NN
	    				
	    				for(String word_tag: tokens2)
	    				{
	    					String[] tokens3 = word_tag.split("_");
	    					tag = new HashSet<String>();
	    					tag.add(tokens3[1]);
	    					
	    					newElement = new Element(tokens3[0].toLowerCase(), tag);
	    		            elements_with_POS_tags.putElements(newElement);
	    				}
	    				
	    		        elements_with_tags = new HashSet<ElementList>();	
	    		        elements_with_tags.add(elements_with_POS_tags);
	    		        newTerm = new Term(tokens[0], label, elements_with_tags);
	    			}
	    			else
	    				newTerm = new Term(tokens[0],label);	//invalid term. added just to calculate transitive closure
	    			
	    			terms.add(newTerm);
	    		}		
	    	}
	    	br.close();
    }
}
