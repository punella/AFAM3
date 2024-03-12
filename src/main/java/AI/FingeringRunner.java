package AI;

import logic.Coordinator;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.impl.NPointCrossover;
import org.uma.jmetal.operator.crossover.impl.UniformCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class FingeringRunner extends Thread{
    private Coordinator coordinator;
    private List<List<Integer>> sheet;

    public FingeringRunner(Coordinator coordinator, List<List<Integer>> sheet){
        this.coordinator = coordinator;
        this.sheet = sheet;
    }

    public void run(){

        //Variabili usate per il confronto delle performance
        int numOfPhrases = sheet.size();
        List<Integer> phraseSize = new ArrayList<>();
        String algorithmName = "";
        double[] sums = new double[3];
        long computingTime = 0;

        final int MAX_EVALS = 17000;
        final int POPULATION_SIZE = 100;

        //Scelta dell'algoritmo di selezione
        BinaryTournamentSelection<IntegerSolution> selection = new BinaryTournamentSelection<>();
        //RankingAndCrowdingSelection selection = new RankingAndCrowdingSelection(POPULATION_SIZE);

        double crossoverProbability = 0.8;
        int nCrossoverPoints = 1;
        NPointCrossover crossover = new NPointCrossover(crossoverProbability, nCrossoverPoints);

        //Scelta dell'algoritmo di mutazione
        double mutationProbability = 0.01;
        IntegerPolynomialMutation mutation = new IntegerPolynomialMutation(mutationProbability, 0);
        //RandomResettingMutation mutation = new RandomResettingMutation(mutationProbability);

        List<Integer> solution = new ArrayList<>();

        for(List<Integer> phrase : sheet) {

            phraseSize.add(phrase.size());

            //Scelta dell'algoritmo di crossover: prove in base alla lunghezza della frase
            /*
            if(phrase.size()>2)
                nCrossoverPoints = 2;
            else
                nCrossoverPoints = 1;
            crossover = new NPointCrossover(crossoverProbability, nCrossoverPoints);
            */
            //nCrossoverPoints = phrase.size();
            //crossover = new NPointCrossover(crossoverProbability, nCrossoverPoints);

            FingeringProblem problem = new FingeringProblem(phrase);

            //Scelta della metaeuristica
            Algorithm<IntegerSolution> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, POPULATION_SIZE)
                    .setSelectionOperator(selection)
                    .setMaxEvaluations(MAX_EVALS)
                    .build();
            /*
            Algorithm<IntegerSolution> algorithm = new NSGAIIIBuilder<>(problem)
                    .setSelectionOperator(selection)
                    .setCrossoverOperator(crossover)
                    .setMutationOperator(mutation)
                    .build();
            */

            algorithmName = algorithm.name();

            AlgorithmRunner.Executor executor = new AlgorithmRunner.Executor(algorithm);
            AlgorithmRunner runner = executor.execute();

            //JMetalLogger.logger.info(String.format("Computing time: %s", runner.getComputingTime()));
            computingTime += runner.getComputingTime();

            List<IntegerSolution> paretoFront = (List<IntegerSolution>) algorithm.result();

            //JMetalLogger.logger.info("Dimensione del fronte di Pareto: " + paretoFront.size());

            IntegerSolution best = doPreferenceSorting(paretoFront);
            IntStream.range(0, 3).forEach(i -> sums[i] += best.objectives()[i]);
            solution.addAll(best.variables());
        }

        //Stampe per l'analisi dei risultati
        System.out.println("========== INPUT ==========");
        System.out.println("Executions: " + numOfPhrases + " Shortest phrase size: " + Collections.min(phraseSize)
                + " Longest phrase size: " + Collections.max(phraseSize));
        System.out.println("=== ALGORITHM PARAMETERS ===");
        System.out.println("Algorithm: " + algorithmName + " Population size: " + POPULATION_SIZE
                + " Stopping condition: " + MAX_EVALS + " evaluations");
        System.out.println("Selection: " + getOperatorName(selection.getClass()));
        System.out.println("Crossover: " + getOperatorName(crossover.getClass()) + " con N=" + nCrossoverPoints
                + " e con prob. " + crossover.crossoverProbability());
        System.out.println("Mutation: " + getOperatorName(mutation.getClass()) + " con prob. " + mutation.mutationProbability());
        System.out.println("========= RESULTS =========");
        System.out.println("Total uncomfortable positions: " + sums[0]);
        System.out.println("Total hand shiftings/repetitions: " + sums[1]);
        System.out.println("Total distance: " + sums[2]);
        System.out.println("Total computing time: " + computingTime);

        coordinator.manageSolution(solution);
    }

    private IntegerSolution doPreferenceSorting(List<IntegerSolution> paretoFront){

        IntegerSolution best = paretoFront.get(0);
        for(int i = 1; i < paretoFront.size(); i++){
            IntegerSolution candidate = paretoFront.get(i);
            if(candidate.objectives()[0] < best.objectives()[0])
                best = candidate;
            else if(candidate.objectives()[0] == best.objectives()[0]){
                if(candidate.objectives()[1] < best.objectives()[1])
                    best = candidate;
            }
        }
        JMetalLogger.logger.info(String.format("Migliore individuo del fronte: %s", best));
        return best;
    }

    private String getOperatorName(Class operator){
        String fullname = operator.getName();
        return fullname.substring(fullname.lastIndexOf(".")+1, fullname.length());
    }

    //Classe utilizzata per il confronto con la mutazione implementata da jMetal
    /*
    private class RandomResettingMutation implements MutationOperator<IntegerSolution> {
        private double mutationProbability;

        public RandomResettingMutation(double mutationProbability) {
            this.mutationProbability = mutationProbability;
        }

        @Override
        public IntegerSolution execute(IntegerSolution solution) {
            for (int i = 0; i < solution.variables().size(); i++) {
                if (JMetalRandom.getInstance().nextDouble() < mutationProbability) {
                    int newValue = getRandomValue(i, solution);
                    solution.variables().set(i, newValue);
                }
            }
            return solution;
        }

        @Override
        public double mutationProbability() {
            return mutationProbability;
        }

        private int getRandomValue(int index, IntegerSolution solution) {
            int lowerBound = solution.getBounds(index).getLowerBound();
            int upperBound = solution.getBounds(index).getUpperBound();
            return JMetalRandom.getInstance().nextInt(lowerBound, upperBound + 1);
        }
    }
    */
}