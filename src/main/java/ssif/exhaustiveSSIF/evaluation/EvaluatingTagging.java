package ssif.exhaustiveSSIF.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
* @author Rashmie Abeysinghe
*/
public class EvaluatingTagging {
	
	List<String[]> allTaggedTerms;
	
	public void loadTaggedTerms(String input) throws IOException
	{
		CSVReader cr = new CSVReader(new FileReader(input));
		allTaggedTerms = cr.readAll();
		cr.close();
	}
	
	private List<String[]> randomlyChose(int amount)
	{
		Collections.shuffle(allTaggedTerms);
		return allTaggedTerms.subList(0, amount);
	}
	
	private void writeSamplesToCSV(List<String[]> eval_sample, String outputPath) throws IOException
	{
		CSVWriter cw = new CSVWriter(new FileWriter(outputPath));
		cw.writeAll(eval_sample);
		cw.close();
	}
	
	private void writeSamplesToLatexFile(List<String[]> eval_samples, String outputPath) throws IOException
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputPath)));
		w.write("\\documentclass{article}\n\\usepackage[hscale=0.9,vscale=0.9]{geometry}\n\\usepackage{pgfplots, pgfplotstable}\n\\begin{document}");
		w.newLine();
		w.write("\\raggedright");
		w.newLine();
		w.write("\\title{Evaluation Samples}");
		w.newLine();
		w.write("\\maketitle");
		w.newLine();
		
		w.write("This document contains 200 randomly chosen samples for sequence-based representation of GO terms. \\linebreak \\linebreak");
		int i=1;
		for(String[] sample: eval_samples)
		{
			w.newLine();
			w.newLine();
			w.write("("+i+")"+" "+ sample[0]);
			w.newLine();
			w.write("\\linebreak \\linebreak");
			w.newLine();

			String[] elementlists = sample[1].split(" \\| ");
			
			for(String elementlist: elementlists)
			{
				//System.out.println(elementlist);
				w.write(elementlist);
				w.write("\\linebreak");
				w.newLine();
			}

			w.write("\\linebreak \\linebreak \\linebreak");
			i++;
		}
		w.write("\n\\end{document}");
		w.close();
	}
	
	private void writeSamplesToTxtFile(List<String[]> eval_samples, String outputPath) throws IOException
	{
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputPath)));
		
		w.write("This document contains 200 randomly chosen samples for sequence-based representation of GO terms.");
		w.newLine();
		w.newLine();
		
		int i=1;
		for(String[] sample: eval_samples)
		{
			w.write("("+i+")"+" "+ sample[0]);
			w.newLine();
			w.newLine();

			String[] elementlists = sample[1].split(" \\| ");
			
			for(String elementlist: elementlists)
			{
				w.write(elementlist);
				w.newLine();
				w.newLine();
			}
			
			w.newLine();
			w.newLine();
			w.newLine();
			i++;
		}
		w.close();
	}
	
	
	public void writeSamples(int num_samples, String output_latex, String output_CSV, String output_txt) throws IOException
	{
		List<String[]> eval_samples = randomlyChose(num_samples);
		writeSamplesToCSV(eval_samples, output_CSV);
		writeSamplesToLatexFile(eval_samples, output_latex);
		writeSamplesToTxtFile(eval_samples, output_txt);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		EvaluatingTagging et = new EvaluatingTagging();
		et.loadTaggedTerms("Evaluation/EvaluatingTagging/tags.csv");
		
		et.writeSamples(200, "Evaluation/EvaluatingTagging/TaggingEvalSamples_latex.tex", "Evaluation/EvaluatingTagging/TaggingEvalSamples_csv.csv", "Evaluation/EvaluatingTagging/TaggingEvalSamples_txt.txt");

	}

}
