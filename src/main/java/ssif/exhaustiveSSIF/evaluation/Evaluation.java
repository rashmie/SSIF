package ssif.exhaustiveSSIF.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import ssif.conditionalrules.Inconsistency;
import ssif.exhaustiveSSIF.SSIF_exhaustive;
import ssif.exhaustiveSSIF.tagging.Tagging;
import ssif.model.Term;


/**
* @author Rashmie Abeysinghe
*/
public class Evaluation extends SSIF_exhaustive {
	
	private Set<Inconsistency> r1_rules;
	private Set<Inconsistency> r2_rules;
	private Set<Inconsistency> r3_rules;
	private Set<Inconsistency> r4_rules;
	private Set<Inconsistency> r5_rules;
	private Set<Inconsistency> r6_rules;
	
	private Tagging tagged_terms;
	private Map<String, Term> label_terms;	//hashmap containing the label of a term as the key and the term as the value.
	private Map<String, Term> id_terms;	//hashmap containing the id of a term as the key and the term as the value.
	
	public Evaluation(String labels_file, String wordnetAntonymFile, String antoFile, String hierarchy_file, String serialized_tagging_file) throws IOException
	{
		super(antoFile, antoFile, antoFile, antoFile);
		setTagged_terms_FromSerialized(serialized_tagging_file);
		Tagging tagging1 = Tagging.deserializeTagging(serialized_tagging_file);
	    	this.tagged_terms = tagging1;
		fill_label_terms();
		fill_iD_terms();
		r1_rules = new HashSet<Inconsistency>();
		r2_rules = new HashSet<Inconsistency>();
		r3_rules = new HashSet<Inconsistency>();
		r4_rules = new HashSet<Inconsistency>();
		r5_rules = new HashSet<Inconsistency>();
	}
	

	
	private void fill_label_terms()
	{
		label_terms = new HashMap<String, Term>();
		for(Term t: tagged_terms.getTerms())
		{
			label_terms.put(t.getLabel(), t);
		}
	}
	
	private void fill_iD_terms()
	{
		id_terms = new HashMap<String, Term>();
		for(Term t: tagged_terms.getTerms())
		{
			id_terms.put(t.getID(), t);
		}
	}
	
	private Set<Inconsistency> loadRules(String inputCSV) throws IOException
	{
		Set<Inconsistency> rules = new HashSet<Inconsistency>();
		CSVReader csvrdr = new CSVReader(new FileReader(inputCSV));
		String[] line;
		
		//rules = new HashSet<ObtainedRule>();
		String child_id, parent_id;
		while((line=csvrdr.readNext())!=null)
		{
			child_id = line[0].split(": ")[1];
			parent_id = line[1].split(": ")[1];
			rules.add(new Inconsistency(label_terms.get(child_id), label_terms.get(parent_id)));
		}
		csvrdr.close();
		return rules;
	}
	
	//check whether common defects exists between defects obtained by two conditional rules
	private void commonDefectsExist(Set<Inconsistency> cr1, Set<Inconsistency> cr2)
	{
//		System.out.println("cr1 size:"+ cr1.size());
//		System.out.println("cr2 size:"+ cr2.size());
		Set<Inconsistency> cr1_copy = new HashSet<>(cr1);
		cr1_copy.retainAll(cr2);
		System.out.println("No of original defects  "+cr1.size()+"\t and \t"+cr2.size());
		if(cr1_copy.isEmpty())
			System.out.println("No commonly obtained defects!!");
		else
		{
			System.out.println("Has commonly obtained defects!!: "+cr1_copy.size());
//			for(Inconsistency i: cr1_copy)
//			{
//				System.out.println(i.getChild().getIDLabel()+"** is_a **"+i.getParent().getIDLabel());
//			}
			
//			Set<Inconsistency> cr1_copy2 = new HashSet<>(cr1);
//			cr1_copy2.removeAll(cr2);
//			
//			for(Inconsistency i: cr1_copy2)
//			{
//				System.out.println(i.getChild().getIDLabel()+"** is_a **"+i.getParent().getIDLabel());
//			}
			
		}
	}
	
	
	public Term getTermFromIDLabel(String idLabel)
	{
		return id_terms.get(idLabel.split(": ")[0]);
	}
	
	//this method loads a set of inconsistencies to a given array list
	//used to load all random samples of different conditional rules
	public void loadEvalSample(ArrayList<Inconsistency> random_sample, String input) throws IOException
	{
		CSVReader cr = new CSVReader(new FileReader(input));
		
		String[] line;
		while((line=cr.readNext())!=null)
		{
			Inconsistency incon = new Inconsistency(getTermFromIDLabel(line[0]), getTermFromIDLabel(line[1]), line[2]);
			random_sample.add(incon);
		}
		cr.close();
	}
	
	public Set<Inconsistency> loadResults(String input) throws IOException
	{
		Set<Inconsistency> results = new HashSet<Inconsistency>();
		CSVReader cr = new CSVReader(new FileReader(input));
		
		String[] line;
		while((line=cr.readNext())!=null)
		{
			Inconsistency incon = new Inconsistency(getTermFromIDLabel(line[0]), getTermFromIDLabel(line[1]), line[2]);
			results.add(incon);
		}
		cr.close();
		return results;
	}
	
	
	public Term getTermFromIDLabel_GODIFF1(String idLabel)
	{
		return id_terms.get(idLabel.split("\\{")[0]);
	}
	
	public Set<Inconsistency> loadGODIFF1Results(String input) throws IOException
	{
		Set<Inconsistency> results = new HashSet<Inconsistency>();
		CSVReader cr = new CSVReader(new FileReader(input));
		
		String[] line;
		while((line=cr.readNext())!=null)
		{
			Inconsistency incon = new Inconsistency(getTermFromIDLabel_GODIFF1(line[0]), getTermFromIDLabel_GODIFF1(line[1]));
			results.add(incon);
		}
		cr.close();
		return results;
	}
	
	public Set<Inconsistency> randomSelection(Set<Inconsistency> cr, int num_needed)
	{
		List<Inconsistency> cr_list = new ArrayList<>(cr);
		Collections.shuffle(cr_list);
		
		Set<Inconsistency> random_set = new HashSet<>();
		
		for(int i=0; i< num_needed; i++)
			random_set.add(cr_list.get(i));
		
		return random_set;
	}
	
	public void commonRules() throws IOException
	{
		//Set<Inconsistency> R1 = loadResults("/Users/rashmie/Documents/workspace/SSIF/NewRules/R1/r1.csv");
//		Set<Inconsistency> R2 = loadResults("NewRules/R2/r2_plus_modified.csv");
//		Set<Inconsistency> R3 = loadResults("NewRules/R3/r3_plus.csv");
//		Set<Inconsistency> R4 = loadResults("NewRules/R4/r4_one_modified2.csv");
		
		Set<Inconsistency> R2 = loadResults("NLS_based/R2_NLS/r2_plus_modified_nls.csv");
		Set<Inconsistency> R3 = loadResults("NLS_based/R3_NLS/r3_plus_modified_nls.csv");
		Set<Inconsistency> R4 = loadResults("NLS_based/R4_NLS/r4_nls.csv");
		
		
		System.out.println("R2: R3");
		commonDefectsExist(R2, R3);
		
		System.out.println("\n\nR2: R4");
		commonDefectsExist(R2, R4);
		
		System.out.println("\n\nR3: R4");
		commonDefectsExist(R3, R4);
		
		R2.retainAll(R3);
		R2.retainAll(R4);
		
		System.out.println("\n\nR2, R3 & R4 common rules: "+R2.size());		
	}
	
	public void writeEvalSampleToCSVFile(Set<Inconsistency> eval_sample_for_rule, String output) throws IOException
	{
		CSVWriter bw = new CSVWriter(new FileWriter(output));
		String[] line = {"Left", "Right", "Description"};
		for(Inconsistency or : eval_sample_for_rule)
		{
			line[0] = or.getChild().getIDLabel();
			line[1] = or.getParent().getIDLabel();
			line[2] = or.getDescription();
			bw.writeNext(line);
		}
		bw.close();
	}
	
	public void randomSampleGeneration() throws IOException
	{
		Set<Inconsistency> R1 = loadResults("/Users/rashmie/Documents/workspace/SSIF/NewRules/R1/r1.csv");
		Set<Inconsistency> R2 = loadResults("/Users/rashmie/Documents/workspace/SSIF/NewRules/R2/r2_plus_plus.csv");
		Set<Inconsistency> R3 = loadResults("/Users/rashmie/Documents/workspace/SSIF/NewRules/R3/r3_plus.csv");
		Set<Inconsistency> R4 = loadResults("/Users/rashmie/Documents/workspace/SSIF/NewRules/R4/r4.csv");
		
		Set<Inconsistency> R2_R3_R4 = new HashSet<>(R2);
		R2_R3_R4.retainAll(R3);
		R2_R3_R4.retainAll(R4);
		
		Set<Inconsistency> R2_R3 = new HashSet<>(R2);
		R2_R3.retainAll(R3);
		R2_R3.removeAll(R2_R3_R4);
		
		Set<Inconsistency> R2_R4 = new HashSet<>(R2);
		R2_R4.retainAll(R4);
		R2_R4.removeAll(R2_R3_R4);
		
		Set<Inconsistency> R3_R4 = new HashSet<>(R3);
		R3_R4.retainAll(R4);
		R3_R4.removeAll(R2_R3_R4);
		
		R2.removeAll(R2_R3);
		R2.removeAll(R2_R4);
		R2.removeAll(R2_R3_R4);
		
		R3.removeAll(R2_R3);
		R3.removeAll(R3_R4);
		R3.removeAll(R2_R3_R4);
		
		R4.removeAll(R3_R4);
		R4.removeAll(R2_R4);
		R4.removeAll(R2_R3_R4);
		
//		System.out.println(R1.size());
//		System.out.println(R2.size());
//		System.out.println(R3.size());
//		System.out.println(R4.size());
//		
//		System.out.println(R2_R3.size());
//		System.out.println(R2_R4.size());
//		System.out.println(R3_R4.size());
//		System.out.println(R2_R3_R4.size());
		
		writeEvalSampleToCSVFile(randomSelection(R1, 10), "Evaluation/R1_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R2, 171), "Evaluation/R2_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R3, 47), "Evaluation/R3_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R4, 60), "Evaluation/R4_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R2_R3, 17), "Evaluation/R2_R3_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R2_R4, 10), "Evaluation/R2_R4_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R3_R4, 25), "Evaluation/R3_R4_eval.csv");
		writeEvalSampleToCSVFile(randomSelection(R2_R3_R4, 10), "Evaluation/R2_R3_R4_eval.csv");
	}
	
	
	public void writeSamplesToLatex(String output, boolean description) throws IOException
	{
		ArrayList<Inconsistency> eval_sample = new ArrayList<>();
		loadEvalSample(eval_sample, "Evaluation/R1_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R2_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R3_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R4_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R2_R3_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R2_R4_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R3_R4_eval.csv");
		loadEvalSample(eval_sample, "Evaluation/R2_R3_R4_eval.csv");
		
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(output)));
		w.write("\\documentclass{article}\n\\usepackage[hscale=0.9,vscale=0.9]{geometry}\n\\usepackage{pgfplots, pgfplotstable}\n\\begin{document}");
		w.newLine();
		w.write("\\raggedright");
		w.newLine();
		w.write("\\title{Evaluation Samples}");
		w.newLine();
		w.write("\\maketitle");
		w.newLine();
		
		w.write("This document contains 340 potential missing relations and 10 incorrect existing relations (from 1-10) found in Gene Ontology. These relations are subsumption (IS-A) type relations. The format of each is as follows. \\linebreak \\linebreak GO:XXXXXX IS-A GO:YYYYYY means there is a subsumption relation between the two concepts GO:XXXXXX and GO:YYYYYY such that, GO:XXXXXX is the subclass (child) and GO:YYYYYY is the superclass (parent). \\linebreak \\linebreak");
		int i=1;
		for(Inconsistency incon: eval_sample)
		{
			w.newLine();
			w.newLine();
			w.write("("+i+")");
			w.newLine();
			w.write("\\linebreak");
			w.newLine();
			w.write(incon.getChild().getID().replaceAll("_", ":")+" "+ incon.getChild().getLabel());
			w.newLine();
			w.write("\\linebreak");
			w.newLine();
			if(i<=10)
				w.write("IS-NOT-A");
			else
				w.write("IS-A");
			w.newLine();
			w.write("\\linebreak");
			w.newLine();
			if(description)
				w.write(incon.getParent().getID().replaceAll("_", ":")+" "+ incon.getParent().getLabel()+ " \\linebreak \\linebreak "+incon.getDescription().replaceAll("_", ":").replaceAll("\\&\\&", ","));
			else
				w.write(incon.getParent().getID().replaceAll("_", ":")+" "+ incon.getParent().getLabel());
			w.write("\\linebreak \\linebreak \\linebreak \\linebreak \\linebreak \\linebreak \\linebreak \\linebreak");
			i++;
		}
		w.write("\n\\end{document}");
		w.close();
	}
	
	//this method compares the results of SSIF with GODiff1
	public void compareSSIFWithGODiff1() throws IOException
	{
		Set<Inconsistency> goDiff1_results = loadGODIFF1Results("GO_diff_allResults.csv");
		Set<Inconsistency> ssif_results = loadResults("NewRules/R2/r2_plus_modified.csv");
		ssif_results.addAll(loadResults("NewRules/R3/r3_plus.csv"));
		ssif_results.addAll(loadResults("NewRules/R4/r4_one_modified2.csv"));
		
		System.out.println("goDiff1_results size: "+goDiff1_results.size());
		System.out.println("ssif_results size: "+ssif_results.size());
		goDiff1_results.retainAll(ssif_results);
		
		System.out.println("Common results size: "+goDiff1_results.size());
		
	}
	
	public static void main(String[] args) throws IOException {
		
		Evaluation e = new Evaluation("GO_labels.txt", "wordnet_antonyms.txt", "other_antonyms.txt", "GO_hierarchy.txt", "serialized/tagging/tagging21.ser");
		
		//e.randomSampleGeneration();
		
		e.commonRules();
		//e.compareSSIFWithGODiff1();
		
		//e.writeSamplesToLatex("Evaluation/Eval_doc/eval.tex", true);
	}
}