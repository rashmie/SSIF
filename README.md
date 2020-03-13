# Subsumption-based Sub-term Inference Framework (SSIF).

## About

Java implementation of SSIF. The paper can be found at: (https://academic.oup.com/bioinformatics/advance-article/doi/10.1093/bioinformatics/btaa106/5739437)

SSIF is used to audit Gene Ontology (GO) by leveraging its underlying graph structure and a novel term-algebra.

Consists of three main components.

(1) A sequence-based representation of GO concept terms by using part-of-speech parsing and sub-concept matching.

(2) The formulation of algebraic operations for the development of a term-algebra based on this sequence-based representation, using antonyms and subsumption-based longest subsequence alignment

(3) The construction of a set of conditional rules (similar to default rules) for backward subsumption inference aimed at uncovering semantic inconsistencies in GO and other ontological structures.

In the code, we refer to the three conditional rules as follows:
-Sub-concept rule: R2
-Monotonicity rule: R3
-Intersection rule: R4


## Inputs

SSIF takes 5 inputs:
1. Ontology concept labels file: concept ID and label seperated by a tab
2. Ontology hierarchy file: direct child and parent seperated by a tab
3. WordNet antonyms file: antonyms identified through WordNet (https://wordnet.princeton.edu/)
4. Other antonyms file: other antonyms
5. Stanford POS tagger model file: We used the model 'english-left3words-distsim.tagger' available at: (https://nlp.stanford.edu/software/tagger.shtml#Download)'

Sample files are given for 1-4 in the 'inputs' folder.


## How to run

Compile the project with maven: 
`mvn clean compile assembly:single`

This will create a runnable jar file 'SSIF-0.0.1-jar-with-dependencies.jar' in folder 'target'

The jar file could be run by providing the above mentioned inputs as arguments: 
`java -jar SSIF-0.0.1-jar-with-dependencies <labelsFile> <hierarchyFile> <WordNetAntonyms> <OtherAntonyms> <POSTaggerModel>`

The program will create a csv file for each conditional rule with the inconsistencies obtained. The format of the each file is as follows: 

-Column 1: Potential descendent
-Column 2: Potential ancestor
-Column 3: A description of how the rule was obtained

