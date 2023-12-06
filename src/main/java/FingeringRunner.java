import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.impl.NPointCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FingeringRunner {

    public static void main(String[] args){

        final int MAX_EVALS = 100000;
        final int POPULATION_SIZE = 100;

        ArrayList<Integer> sheet = new ArrayList<>();
        //new ProgramGUI();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File("src/main/resources/scalaDoM.xml"));
            doc.getDocumentElement().normalize();
            NodeList notes = doc.getElementsByTagName("note");



            for(int i = 0; i < notes.getLength(); i++) {

                Node node = notes.item(i);

                if(node.getNodeType() == Node.ELEMENT_NODE) {

                    Element note = (Element) node;

                    Element pitch = (Element) note.getElementsByTagName("pitch").item(0);

                    if(pitch!=null){

                        String sstep = pitch.getElementsByTagName("step").item(0).getTextContent();
                        int step = 0;
                        switch(sstep){
                            case "A": step=10; break;
                            case "B": step=12; break;
                            case "C": step=1; break;
                            case "D": step=3; break;
                            case "E": step=5; break;
                            case "F": step=6; break;
                            case "G": step=8; break;
                        }
                        Node acc = pitch.getElementsByTagName("accidental").item(0);
                        if(acc!=null){
                            String sacc = acc.getTextContent();
                            if(sacc.equals("sharp"))
                                step++;
                            else if(sacc.equals("flat"))
                                step--;
                        }
                        int octave = Integer.parseInt(pitch.getElementsByTagName("octave").item(0).getTextContent());
                        sheet.add(step+12*(octave-2));
                    }

                }
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        FingeringProblem problem = new FingeringProblem(sheet);
        BinaryTournamentSelection<IntegerSolution> selection = new BinaryTournamentSelection<>();

        double crossoverProbability = 0.8;
        int nCrossoverPoints = 1;
        NPointCrossover crossover = new NPointCrossover(crossoverProbability, nCrossoverPoints);

        //La mutazione sugli interi implementata in questa versione di JMetal è solo di un tipo particolare
        //Si può scrivere una classe semplice che faccia la mutazione casuale di un solo intero della soluzione

        double mutationProbability = 0.01;
        IntegerPolynomialMutation mutation = new IntegerPolynomialMutation(mutationProbability, 0);

        Algorithm<IntegerSolution> algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
                .setSelectionOperator(selection)
                .setMaxEvaluations(MAX_EVALS)
                .setPopulationSize(POPULATION_SIZE)
                .build();

        AlgorithmRunner.Executor executor = new AlgorithmRunner.Executor(algorithm);
        AlgorithmRunner runner = executor.execute();

        //per stampare a video il tempo di calcolo
        JMetalLogger.logger.info(String.format("Computing time: %s", runner.getComputingTime()));

        IntegerSolution best = algorithm.result();
        JMetalLogger.logger.info(String.format("Best individual: %s", best));

    }
}