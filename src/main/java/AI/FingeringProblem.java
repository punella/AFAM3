package AI;

import model.Button;
import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import java.util.ArrayList;
import java.util.List;

public class FingeringProblem extends AbstractIntegerProblem {

    private List<Integer> sheet;
    private final int[][] FINGER_DISTANCE_MATRIX = {
            {0, 12, 14, 16, 16},
            {12, 0, 6, 7, 11},
            {14, 6, 0, 5, 8},
            {16, 7, 5, 0, 5},
            {16, 11, 8, 5, 0}
    };

    public FingeringProblem(List<Integer> sheet){
        if(sheet.isEmpty())
            throw new IllegalArgumentException("Spartito vuoto.");
        this.sheet = sheet;
        int numberOfVariables = sheet.size();

        numberOfObjectives(4);

        List<Integer> lowerBound = new ArrayList<>();
        List<Integer> upperBound = new ArrayList<>();
        for(int i=0; i<numberOfVariables; i++){
            lowerBound.add(1);
            upperBound.add(10);
        }
        variableBounds(lowerBound, upperBound);
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution integerSolution) {

        SolutionRepair.repair(integerSolution, sheet);

        Button[] buttonPath = getButtonPath(integerSolution.variables());

        integerSolution.objectives()[0] = computeRepetitions(integerSolution.variables());
        integerSolution.objectives()[1] = computeUncomfortablePositions(buttonPath, integerSolution.variables());
        integerSolution.objectives()[2] = computeHandShiftings(buttonPath, integerSolution.variables());
        integerSolution.objectives()[3] = computeTotalDistance(buttonPath);

        return integerSolution;
    }

    //Il terzo obiettivo potrebbe rendere inutile il primo:
    // la distanza tra due bottoni non sarà mai minore di quella tra un dito e se stesso
    public int computeRepetitions(List<Integer> fingering){
        int repetitions = 0;
        for(int i=1; i<sheet.size(); i++){
            if((fingering.get(i) % 5) == (fingering.get(i-1) % 5))
                repetitions++;
        }
        return repetitions;
    }

    public int computeUncomfortablePositions(Button[] buttonPath, List<Integer> fingering){
        int uncomfortablePositions = 0;
        for(int i = 1; i < buttonPath.length; i++) {
            int prevFinger = toRealFinger(fingering.get(i - 1));
            int nextFinger = toRealFinger(fingering.get(i));
            if(isFingerCrossing(prevFinger, nextFinger, buttonPath[i-1], buttonPath[i]))
                uncomfortablePositions++;
        }
        return uncomfortablePositions;
    }

    public int computeHandShiftings(Button[] buttonPath, List<Integer> fingering){
        int handShiftings = 0;
        for(int i = 1; i < buttonPath.length; i++) {
            double distance = buttonPath[i - 1].computeDistance(buttonPath[i]);
            int prevFinger = toRealFinger(fingering.get(i - 1)) - 1;
            int nextFinger = toRealFinger(fingering.get(i)) - 1;
            if(distance > FINGER_DISTANCE_MATRIX[prevFinger][nextFinger])
                handShiftings++;
        }
        return handShiftings;
    }

    public double computeTotalDistance(Button[] buttonPath){
        double totalDistance = 0;
        for(int i = 1; i < buttonPath.length; i++)
            totalDistance += buttonPath[i-1].computeDistance(buttonPath[i]);
        return totalDistance;
    }

    private boolean isFingerCrossing(int prevFing, int nextFing, Button prevBut, Button nextBut){
        if(prevBut.getRow()==nextBut.getRow()){
            if(prevBut.getCol()<nextBut.getCol()&&prevFing>nextFing)
                return true;
            if(prevBut.getCol()>nextBut.getCol()&&prevFing<nextFing)
                return true;
        }
        if(nextBut.getRow()<prevBut.getRow()){
            return prevFing != 1 && nextFing > prevFing;
        }
        return false;
    }

    private Button[] getButtonPath(List<Integer> fingering){
        Button[] buttonPath = new Button[sheet.size()];
        for(int i = 0; i < sheet.size(); i++)
            buttonPath[i] = getButton(sheet.get(i), fingering.get(i));
        return buttonPath;
    }

    private Button getButton(Integer note, Integer finger){
        Button button;
        switch(note % 3){
            case 0:
                button = new Button((note/3)-1, 2);
                break;
            case 1:
                button = new Button(note/3, 0);
                break;
            case 2:
                button = new Button(note/3, 1);
                break;
            default: return null;
        }
        if(finger > 5){
            if(button.hasDouble())
                return button.getDouble();
            throw new RuntimeException("Trovata soluzione inammissibile!");
        }
        return button;
    }

    private int toRealFinger(Integer finger){
        if(finger > 5)
            return finger % 6 + 1;
        return finger;
    }

}
