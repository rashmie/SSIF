package ssif.exhaustiveSSIF.tagging;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.PropertiesUtils;
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
	protected ArrayList<Term> terms;

	public POSTagging()
	{
		
	}
	protected POSTagging(String labels_file) {
		super();
		this.labels_file = labels_file;
	}
	
//	protected POSTagging() {
//		
//	}
	
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
	    	
	    	terms = new ArrayList<Term>();
	    	
	    	String line, label;
	    	String[] tokens;
	    	Tree tree, parent;
	    	List<Tree> leaves;
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
	        			tree = parse(label); 
	        			leaves = tree.getLeaves();
	    		        for (Tree leaf : leaves) 
	    		        { 
	    		            parent = leaf.parent(tree);	//parent=POS tag, leaf = element.
	    		            tag = new HashSet<String>();
	    		            tag.add(parent.label().value());
	    		            newElement = new Element(leaf.label().value().toLowerCase(), tag);
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
    
    protected void runPOSTaggin2() throws IOException
    {
	    	BufferedReader br = new BufferedReader(new FileReader(labels_file));
	    	MaxentTagger tagger = new MaxentTagger("parsers/english-left3words-distsim.tagger");
	    	
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
    
    public void writeTags(String output) throws IOException
    {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		for(Term t: terms)
		{ 
			if(!t.isValidTerm())
				continue;
			
			String line_to_write = "";
	    		line_to_write+= t.getIDLabel()+"\t**  ";
	    		
	    		for(ElementList el: t.getElements_with_tags())
	    		{
	    			for(Element e: el)
	    				line_to_write += e.getElementName()+"/"+e.getTags()+" ";
	    			line_to_write += " | ";
	    		}
	    		line_to_write = line_to_write.replaceAll(" \\| $", "");	//removing the trailing pipe sign.
	    		bw.write(line_to_write);
	    		bw.newLine();;
		}
		bw.close();
    }
    
    public void testPOSTagging2(String sample)
    {
    		//"If you are tagging English, you should almost certainly choose the model english-left3words-distsim.tagger.": https://nlp.stanford.edu/software/pos-tagger-faq.html#train
    		MaxentTagger tagger = new MaxentTagger("parsers/english-left3words-distsim.tagger");
    		MaxentTagger tagger2 = new MaxentTagger("parsers/wsj-0-18-bidirectional-distsim.tagger");
    		MaxentTagger tagger3 = new MaxentTagger("parsers/english-bidirectional-distsim.tagger");
    	
    		
    		// The sample string
    		 
//    		String sample = "mitochondrial outer membrane permeabilization involved in programmed cell death";
    		
    		//String sample = "negative regulation of cellular protein catabolic process";
    		 
    		// The tagged string
    		 
    		String tagged = tagger.tagString(sample);
    		 
    		// Output the result
    		 
    		System.out.println(tagged);
    }
    
    public void testPOSTagging(String sentence2)
    {
    		String sentence = "My dog also likes eating sausage.";
    	
	    	String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";  
	    	
	    	LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);
	    	
	    	TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
	    	
	    	Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(new StringReader(sentence));    
	   
	    	List<CoreLabel> tokens = tokenizer.tokenize();
	    
	    	Tree tree = parser.apply(tokens);
	    
		List<Tree> leaves = tree.getLeaves();
		
		Tree parent;
        for (Tree leaf : leaves) 
        { 
            parent = leaf.parent(tree);	//parent=POS tag, leaf = word.
            System.out.println(leaf.label().value()+"\t :"+parent.label().value());
	    }	
    }
    
    public void testPOSTagging3(String sentence)
    {
	    	StanfordCoreNLP pipeline = new StanfordCoreNLP(
	                PropertiesUtils.asProperties(
	                        "annotators",
	                        "tokenize, ssplit, pos, depparse, parse",
	                        "ssplit.isOneSentence",
	                        "true",
	                        "tokenize.language",
	                        "en"
	                )
		);
		
		//String sentence = "My dog also likes eating sausage.";
		     
		CoreDocument document = new CoreDocument(sentence);
		     
		pipeline.annotate(document);
		
		for(CoreSentence sent: document.sentences())
		{
			System.out.println(sent.posTags());
		}

    	
    }
    
   
    public static void main(String[] args) throws IOException
    {
    	
//	    	long startTime = System.currentTimeMillis();
//	    	
//    		POSTagging pt = new POSTagging("GO_labels.txt");
//    		pt.runPOSTaggin();
//    		pt.writeTags("test/POSTagging.txt");
//			
//		long endTime = System.currentTimeMillis();
//		System.out.println("Elapsed Time: "+(endTime-startTime)/1000+" seconds");
    		
    		POSTagging pt = new POSTagging(null);
//    		String label = "protein tyrosine kinase binding";	
//    		String label2 = "negative regulation of cellular protein catabolic process";
    		String label3 = "My dog also likes eating sausage.";
    		//pt.testPOSTagging2(label3);
    		pt.testPOSTagging(label3);
    		//pt.testPOSTagging3(label3);
    		
    	
//    		ArrayList<String> arr = new ArrayList<>();
//    		arr.add("A");
//    		arr.add("B");
//    		arr.add("C");
//    		arr.add("D");
//    		
//    		permute(arr, 0);
    		
    		//permute(arr);
    	
    }
}
