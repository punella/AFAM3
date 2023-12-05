import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.impl.NPointCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

import java.util.ArrayList;
import java.util.List;

public class FingeringRunner {

    public static void main(String[] args){

        final int MAX_EVALS = 100000;
        final int POPULATION_SIZE = 1000;

        //sheet mock
        List<Integer> fakeSheet = new ArrayList<>();
        fakeSheet.add(1);
        fakeSheet.add(2);
        fakeSheet.add(3);
        fakeSheet.add(4);
        fakeSheet.add(5);
        fakeSheet.add(6);
        fakeSheet.add(7);

        FingeringProblem problem = new FingeringProblem(fakeSheet);
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

        //JMetalLogger.logger.info(String.format("Objectives: %s", best.objectives()));
    }
}