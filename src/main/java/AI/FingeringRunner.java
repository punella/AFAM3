package AI;

import logic.Coordinator;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.impl.NPointCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

import java.util.List;

public class FingeringRunner extends Thread{

    private Coordinator coordinator;
    private List<Integer> sheet;

    public FingeringRunner(Coordinator coordinator, List<Integer> sheet){
        this.coordinator = coordinator;
        this.sheet = sheet;
    }

    public void run(){

        final int MAX_EVALS = 100000;
        final int POPULATION_SIZE = 100;

        //CORREZIONE: L'ALGORITMO VIENE CHIAMATO DOPO OGNI PAUSA LETTA NELLO SPARTITO

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

        JMetalLogger.logger.info(String.format("Computing time: %s", runner.getComputingTime()));

        IntegerSolution best = algorithm.result();
        JMetalLogger.logger.info(String.format("Best individual: %s", best));

        coordinator.manageSolution(best.variables());
    }
}