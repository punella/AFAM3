package AI;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.List;
import java.util.stream.IntStream;

public class SolutionRepair {

    //Garantisce che non venga designata la fila supplementare per una nota che non ce l'ha
    public static void repair(IntegerSolution solution, List<Integer> sheet) {

        IntStream.range(0, solution.variables().size())
                .filter(i -> sheet.get(i) % 3 == 0 && solution.variables().get(i) > 5)
                .forEach(i -> {
                    int newValue = solution.variables().get(i) % 6 + 1;
                    solution.variables().set(i, newValue);
                });
    }
}
