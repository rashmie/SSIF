package ssif.exhaustiveSSIF.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import ssif.conditionalrules.ConditionalRule;
import ssif.conditionalrules.Inconsistency;
import ssif.model.Term;
import ssif.utilityFunctions.LoadFromFile;

/**
* @author Rashmie Abeysinghe
*/

//This class is used to process the evaluation results done by domain experts.
public class EvaluationResults {
	
	Map<String, Term> label_terms;

	public EvaluationResults(String labelsFile) throws IOException {
		// TODO Auto-generated constructor stub
		label_terms = LoadFromFile.LoadConceptMap(labelsFile);
	}
	
//	public Map<String, ArrayList<Integer>> readEvalResults_EWH(String input) throws IOException
//	{
//		Map<String, ArrayList<Integer>> results = new HashMap<>();
//		BufferedReader br = new BufferedReader(new FileReader(input));
//		String line;
//		while((line=br.readLine())!=null)
//		{
//			String spaceremoved = line.trim();
//			if(!spaceremoved.isEmpty())
//			{
//				//System.out.println(line);
//				String[] tokens = line.split("\\.");
//				String[] tokens2 = tokens[0].split("\\)");
//				
//				String number = tokens2[0];
//				String type = tokens2[1].trim();
//				
//				if(results.containsKey(type))
//					results.get(type).add(Integer.parseInt(number));
//				else
//				{
//					ArrayList<Integer> temp = new ArrayList<>();
//					temp.add(Integer.parseInt(number));
//					results.put(type, temp);
//				}
//			}
//		}
//		br.close();
//		return results;
//	}
//	
//	public Map<String, ArrayList<Integer>> readEvalResults_HNBM(String input) throws IOException
//	{
//		Map<String, ArrayList<Integer>> results = new HashMap<>();
//		CSVReader br = new CSVReader(new FileReader(input));
//		String[] line;
//		while((line=br.readNext())!=null)
//		{
//			String number = line[0];
//			String type = line[1];
//			if(type.isEmpty())
//				type = "disagree";
//			
//			if(results.containsKey(type))
//				results.get(type).add(Integer.parseInt(number));
//			else
//			{
//				ArrayList<Integer> temp = new ArrayList<>();
//				temp.add(Integer.parseInt(number));
//				results.put(type, temp);
//			}
//		}
//		br.close();
//		return results;
//	}
//	
//	//convert key=type, value=list(number) Map to: key=number, value=type Map
//	public Map<Integer, String> convertMap(Map<String, ArrayList<Integer>> map1)
//	{
//		Map<Integer, String> converted = new HashMap<>();
//		for(Entry<String, ArrayList<Integer>> entry: map1.entrySet())
//		{
//			for(int number: entry.getValue())
//			{
//				converted.put(number, entry.getKey());
//			}
//		}
//		return converted;
//	}
//	
//	public void writeBothReviews(Map<Integer, String> HNBM, Map<Integer, String> EWH, String output) throws IOException
//	{
//		CSVWriter cw = new CSVWriter(new FileWriter(output));
//		String[] line = new String[3];
//		for(Entry<Integer, String> entry: HNBM.entrySet())
//		{
//			line[0] = Integer.toString(entry.getKey());
//			line[1] = entry.getValue();
//			line[2] = EWH.get(entry.getKey());
//			cw.writeNext(line);
//		}
//		cw.close();
//	}
//	
//	public void displayDistribution(Map<String, ArrayList<Integer>> results)
//	{
//		for(Entry<String, ArrayList<Integer>> entry: results.entrySet())
//		{
//			System.out.println(entry.getKey()+"\t :"+entry.getValue());
//		}
//	}
//	
//	public void displayDistributionCounts(Map<String, ArrayList<Integer>> results)
//	{
//		for(Entry<String, ArrayList<Integer>> entry: results.entrySet())
//		{
//			System.out.println(entry.getKey()+"\t :"+entry.getValue().size());
//		}
//	}
	
	//hnbm: true for HNBM, false for EWH
	public Map<Inconsistency, String> loadEvaluationFromCombined(String input, boolean hnbm) throws IOException
	{
		CSVReader cr = new CSVReader(new FileReader(input));
		String[] line;
		Map<Inconsistency, String> reviews = new HashMap<>();
		cr.readNext();//title line
		while((line=cr.readNext())!=null)
		{
			if(hnbm)
				reviews.put(new Inconsistency(label_terms.get(line[1].split(": ")[0]), label_terms.get(line[2].split(": ")[0])) , line[4].toLowerCase());
			else
				reviews.put(new Inconsistency(label_terms.get(line[1].split(": ")[0]), label_terms.get(line[2].split(": ")[0])), line[5].toLowerCase());
		}
		cr.close();
		return reviews;
	}

//	//load the evaluation of HNBM from the csv file containing both HNBM's and EWH's evaluations
//	public ArrayList<String> loadEvaluationOfHNBMfromCombined(String input) throws IOException
//	{
//		CSVReader cr = new CSVReader(new FileReader(input));
//		String[] line;
//		ArrayList<String> reviews = new ArrayList<>();
//		cr.readNext();//title line
//		while((line=cr.readNext())!=null)
//			reviews.add(line[1]);
//		cr.close();
//		return reviews;
//	}
//	
//	//load the evaluation of EWH from the csv file containing both HNBM's and EWH's evaluations
//	public ArrayList<String> loadEvaluationOfEWHfromCombined(String input) throws IOException
//	{
//		CSVReader cr = new CSVReader(new FileReader(input));
//		String[] line;
//		ArrayList<String> reviews = new ArrayList<>();
//		cr.readNext();//title line
//		while((line=cr.readNext())!=null)
//			reviews.add(line[2]);
//		cr.close();
//		return reviews;
//	}
	
	public Set<Inconsistency> loadResults(String inputCSV) throws IOException
	{
		Set<Inconsistency> rules = new HashSet<>();
		CSVReader csvrdr = new CSVReader(new FileReader(inputCSV));
		String[] line;
		
		//rules = new HashSet<ObtainedRule>();
		String child_id, parent_id, desc;
		while((line=csvrdr.readNext())!=null)
		{
			child_id = line[0].split(": ")[0];
			parent_id = line[1].split(": ")[0];
			desc = line[2];
			rules.add(new Inconsistency(label_terms.get(child_id), label_terms.get(parent_id), desc));
		}
		csvrdr.close();
		return rules;
	}
	
	public ArrayList<Inconsistency> loadResultsToList(String inputCSV) throws IOException
	{
		ArrayList<Inconsistency> rules = new ArrayList<>();
		CSVReader csvrdr = new CSVReader(new FileReader(inputCSV));
		String[] line;
		
		//rules = new HashSet<ObtainedRule>();
		String child_id, parent_id, desc;
		while((line=csvrdr.readNext())!=null)
		{
			child_id = line[0].split(": ")[0];
			parent_id = line[1].split(": ")[0];
			desc = line[2];
			rules.add(new Inconsistency(label_terms.get(child_id), label_terms.get(parent_id), desc));
		}
		csvrdr.close();
		return rules;
	}
	
	//after modification of the algorithm, this method allows to obtain the sample numbers of the results corresponding to that rule
	//e.g. if the rule is R2, this method obtains the sample numbers of the inconsistencies obtained by R2 in the evaluation sample
	public ArrayList<Integer> getEvaluationSampleNumbersOfResults(Set<Inconsistency> results, ArrayList<Inconsistency> evaluationSample)
	{
		ArrayList<Integer> sampleNumbers = new ArrayList<>();
		for(int i=0; i< evaluationSample.size(); i++)
		{
			if(results.contains(evaluationSample.get(i)))
				sampleNumbers.add(i+1);
		}
		
		if(!sampleNumbers.isEmpty())
		{
			System.out.println("Number of samples: "+sampleNumbers.size());
			System.out.println(sampleNumbers);
			return sampleNumbers;
		}
		else
			System.out.println("No evaluation samples!!");
		return null;
	}
	
	public void compareNewResultsWithEvaluationSample() throws IOException
	{
		ArrayList<Inconsistency> evaluationSample = loadResultsToList("Evaluation/EvaluatingRules-domainExperts/eval_allRules.csv");
		
		Set<Inconsistency> R2 = loadResults("NewRules/R2/r2_plus_modified.csv");
		Set<Inconsistency> R3 = loadResults("NewRules/R3/r3_plus.csv");
		Set<Inconsistency> R4 = loadResults("NewRules/R4/r4_one_modified2.csv");
	
		Set<Inconsistency> R2_R3 = new HashSet<>(R2);
		R2_R3.retainAll(R3);
		
		Set<Inconsistency> R2_R4 = new HashSet<>(R2);
		R2_R4.retainAll(R4);
		
		Set<Inconsistency> R3_R4 = new HashSet<>(R3);
		R3_R4.retainAll(R4);
		
		Set<Inconsistency> R2_R3_R4 = new HashSet<>(R2_R3);
		R2_R3_R4.retainAll(R4);
		
		//R2 unique
		R2.removeAll(R2_R3); R2.removeAll(R2_R4); R2.removeAll(R2_R3_R4);
		
		//R3 unique
		R3.removeAll(R2_R3); R3.removeAll(R3_R4); R3.removeAll(R2_R3_R4);
		
		//R4 unique
		R4.removeAll(R2_R4); R4.removeAll(R3_R4); R3.removeAll(R2_R3_R4);
		
		
		//R2 & R3 unique
		R2_R3.removeAll(R2_R3_R4);
		
		//R2 & R4 unique
		R2_R4.removeAll(R2_R3_R4);
		
		//R3 & R4 unique
		R3_R4.removeAll(R2_R3_R4);
		
		Set<Integer> removedSamples = new HashSet<>();
		for(int i=1; i<351; i++)
			removedSamples.add(i);
		
		System.out.println("R2 unique");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R2, evaluationSample));
		System.out.println("\n\n");
		
		System.out.println("R3 unique");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R3, evaluationSample));
		System.out.println("\n");
		
		System.out.println("R4 unique");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R4, evaluationSample));
		System.out.println("\n");
		
		System.out.println("R2 and R3");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R2_R3, evaluationSample));
		System.out.println("\n");
		
		System.out.println("R2 and R4");	//no samples here!!!
		getEvaluationSampleNumbersOfResults(R2_R4, evaluationSample);
		System.out.println("\n");
		
		System.out.println("R3 and R4");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R3_R4, evaluationSample));
		System.out.println("\n");
		
		System.out.println("R2, R3 and R4");
		removedSamples.removeAll(getEvaluationSampleNumbersOfResults(R2_R3_R4, evaluationSample));
		System.out.println("\n");
		
		System.out.println("Removed Samples:");
		System.out.println("Number of samples:"+removedSamples.size());
		System.out.println(removedSamples);
		
	}
	
	//this method is used to compare the results of an improved rule against the evaluation and report the precision.
	//eval_start: the start of range corresponding to the results of the rule in the evaluation sample 
	//eval_end: //eval_start: the end of range corresponding to the results of the rule in the evaluation sample
	//this method considers evaluation done by HNBM
	//
	public void compareResultsWithEvaluation_HNBM(String input_new_results, String input_evaluation_sample, String output) throws IOException
	{
		Map<Inconsistency, String> eval = loadEvaluationFromCombined(input_evaluation_sample, true);
		Set<Inconsistency> results = loadResults(input_new_results);
		
		//int res_agree, res_disagree, res_maybe, eval_agree, res_disagree, res_maybe
		
		Map<String, Integer> eval_eistribution_original = new HashMap<>();	//the distribution of agree, disagree and maybe in the original evaluation
		Map<String, Integer> eval_eistribution_modified = new HashMap<>();	//the distribution of agree, disagree and maybe in the results obtained by modified algorithm
		for(Entry<Inconsistency, String> e: eval.entrySet())
		{
			if(results.contains(e.getKey()))
			{
				if(eval_eistribution_modified.containsKey(e.getValue()))
					eval_eistribution_modified.put(e.getValue(),  eval_eistribution_modified.get(e.getValue())+1);
				else
					eval_eistribution_modified.put(e.getValue(), 1);
			}
			if(eval_eistribution_original.containsKey(e.getValue()))
				eval_eistribution_original.put(e.getValue(),  eval_eistribution_original.get(e.getValue())+1);
			else
				eval_eistribution_original.put(e.getValue(), 1);
		}
//		System.out.println("Original Evaluation Distribution:");
//		displayDistributionInMap(eval_eistribution_original);
		
		//System.out.println("\nDistribution after modifying algorithm:");
		System.out.println("Distribution:");
		displayDistributionInMap(eval_eistribution_modified);
		if(output!=null)
		{
			CSVWriter cw = new CSVWriter(new FileWriter(output));
			String[] line = new String[3];
			for(Inconsistency or : results)
			{
				if(eval.containsKey(or))
				{
					line[0] = or.getChild().getIDLabel();
					line[1] = or.getParent().getIDLabel();
					line[2] = eval.get(or);
					cw.writeNext(line);
				}
			}
			cw.close();
		}
	}
	
	public void writeInconsistenciesToFile(Set<Inconsistency> incons, String output) throws IOException
	{
		if(output!=null)
		{
			CSVWriter cw = new CSVWriter(new FileWriter(output));
			String[] line = new String[3];
			for(Inconsistency or : incons)
			{
				
					line[0] = or.getChild().getIDLabel();
					line[1] = or.getParent().getIDLabel();
					line[2] = or.getDescription();
					cw.writeNext(line);
			}
			cw.close();
		}
	}
	
	public void displayDistributionInMap(Map<String, Integer> dist_map)
	{
		for(Entry<String, Integer> e: dist_map.entrySet())
			System.out.println(e.getKey()+ ":\t"+ e.getValue());
	}
	
	//compares the newly obtained results with the evaluation sample and writes the common ones to the output
	public void intersectResultsWithEvaluationSample(String input_results, String input_eval, String output) throws IOException
	{
		Set<Inconsistency> results = loadResults(input_results);
		Set<Inconsistency> eval = loadResults(input_eval);
		eval.retainAll(results);
		if(!eval.isEmpty())
			writeInconsistenciesToFile(eval, output);
		else
			System.out.println("Intersection is an empty set!!");
	}

	public void R2eval(String input, String output) throws IOException
	{
		CSVReader cr = new CSVReader(new FileReader(input));
		BufferedWriter cw = new BufferedWriter(new FileWriter(output));
		String[] line;
		
		String suggestion, how_derived;
		int j = 1;
		while((line=cr.readNext())!=null)
		{
			suggestion = line[0] + " IS-A "+ line[1];
			String[] tokens = line[2].split(" IS-A ");
			String[] left = tokens[0].split(" ");
			String[] right = tokens[1].split(" ");
			
			how_derived="";
			for(int i=0; i< left.length; i++)
			{
				if(left[i].equals(right[i]))
					continue;
				how_derived += label_terms.get(left[i]).getIDLabel()+ "\nIS-A\n" +label_terms.get(right[i]).getIDLabel()+"\n\n";
			}
			cw.write("("+j+") ");
			cw.write(suggestion);
			cw.newLine();
			cw.newLine();
			cw.write(line[2]);
			cw.newLine();
			cw.newLine();
			cw.write(how_derived);
			cw.newLine();
			cw.newLine();
			cw.newLine();
			j++;
		}
		cr.close();
		cw.close();
	}
	
//	public void checkEval(String input) throws IOException
//	{
//		CSVReader csvrdr = new CSVReader(new FileReader(input));
//		String[] line;
//		
//		csvrdr.readNext();	//title line
//		Set<String> st = new HashSet<>();
//		while((line=csvrdr.readNext())!=null)
//		{
//			st.add(line[5].toLowerCase());
//			if(!line[5].equalsIgnoreCase("agree") && !line[5].equalsIgnoreCase("disagree") && !line[5].equalsIgnoreCase("maybe"))
//				System.out.println(line[0]+": "+ line[5]);
//		}
//		System.out.println(st);
//		csvrdr.close();
//	}
	
	public static void main(String[] args) throws IOException {
		
		EvaluationResults er = new EvaluationResults("OntologyInputs/GO_labels_10-04-2018.txt");
		//er.displayDistributionCounts();
		//er.displayDistribution(er.readEvalResults_EWH("Evaluation/EvaluatingRules-domain experts/Eval-Results/350-eval-sample-response_EWH_harmonized.txt"));
		//er.displayDistribution(er.readEvalResults_HNBM("Evaluation/EvaluatingRules-domain experts/Eval-Results/HNBM-harmonized.csv"));
		
//		Map<Integer, String> HNBM = er.convertMap(er.readEvalResults_HNBM("Evaluation/EvaluatingRules-domain experts/Eval-Results/HNBM-harmonized.csv"));
//		Map<Integer, String> EWH = er.convertMap(er.readEvalResults_EWH("Evaluation/EvaluatingRules-domain experts/Eval-Results/350-eval-sample-response_EWH_harmonized.txt"));
//		er. writeBothReviews(HNBM, EWH, "Evaluation/EvaluatingRules-domain experts/Eval-Results/harmonized_combined.csv");

		//System.out.println(er.loadEvaluationOfEWHfromCombined("Evaluation/EvaluatingRules-domain experts/Eval-Results/harmonized_combined.csv").size());
		
		//test();
		
		//er.R2eval("Evaluation/EvaluatingRules-domainExperts/R3_eval.csv", "Evaluation/EvaluatingRules-domainExperts/R3_eval.txt");
		
		//er.checkEval("Evaluation/EvaluatingRules-domainExperts/Eval-Results/harmonized_combined.csv");
		
		//er.compareResultsWithEvaluation_HNBM("Evaluation/EvaluatingRules-domainExperts/R2_eval.csv", "Evaluation/EvaluatingRules-domainExperts/Eval-Results/harmonized_combined.csv", null);
		//er.compareResultsWithEvaluation_HNBM("NewRules/R2/r2_plus_modified.csv", "Evaluation/EvaluatingRules-domainExperts/Eval-Results/harmonized_combined.csv", "Evaluation/EvaluatingRules-domainExperts/Eval-Results/R2_eval_afterMod.csv");
		//er.compareResultsWithEvaluation_HNBM("NewRules/R2/r2_plus_modified.csv", "Evaluation/EvaluatingRules-domainExperts/Eval-Results/harmonized_combined.csv", null); //"Evaluation/EvaluatingRules-domainExperts/Eval-Results/R3_eval_afterMod.csv"
	
		
		//er.intersectResultsWithEvaluationSample("NewRules/R2/r2_plus_modified.csv", "Evaluation/EvaluatingRules-domainExperts/R2_R3_R4_eval.csv", "Evaluation/EvaluatingRules-domainExperts/Eval-Results/R2_R3_R4_eval_afterMod.csv");
	
		//er.R2eval("NewRules/R3/R3_plus.csv", "NewRules/R3/R3_plus.txt");
		
		er.compareNewResultsWithEvaluationSample();
		
	}
}
