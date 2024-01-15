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
            upperBound.add(5);
        }
        variableBounds(lowerBound, upperBound);
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution integerSolution) {

        integerSolution.objectives()[0] = computeRepetitions(integerSolution.variables());
        int[] uncomfortablePositions = computeUncomfortablePositions(integerSolution.variables());
        integerSolution.objectives()[1] = uncomfortablePositions[0];
        integerSolution.objectives()[2] = uncomfortablePositions[1];
        integerSolution.objectives()[3] = computeFingerDistance(integerSolution.variables());

        return integerSolution;
    }

    public int computeRepetitions(List<Integer> fingering){
        int repetitions = 0;
        for(int i=1; i<sheet.size(); i++){
            if(fingering.get(i) == fingering.get(i-1))
                repetitions++;
        }
        return repetitions;
    }

    public int computeFingerDistance(List<Integer> fingering){
        int totalDistance = 0;
        for(int i=0; i<sheet.size()-1; i++){
            totalDistance += FINGER_DISTANCE_MATRIX[fingering.get(i)-1][fingering.get(i+1)-1];
        }
        return totalDistance;
    }

    public int[] computeUncomfortablePositions(List<Integer> fingering){
        int[] uncomfortablePositions = new int[2];
        int unreachableButtons = 0;
        int fingerCrossings = 0;

        List<Button> startingButtons = findButtons(sheet.get(0));

        for(int i=0; i<sheet.size()-1; i++){

            int nextNote = sheet.get(i+1);
            int prevFing = fingering.get(i);
            int nextFing = fingering.get(i+1);

            int maxFingerDistance = FINGER_DISTANCE_MATRIX[prevFing-1][nextFing-1];

            List<Button> landingButtons = findButtons(nextNote);
            List<Button> comfortableButtons = new ArrayList<>();

            for(Button starting : startingButtons){
                for(Button landing : landingButtons){
                    if(starting.computeDistance(landing) <= maxFingerDistance){
                        if(!comfortableButtons.contains(landing))
                            comfortableButtons.add(landing);
                        if(isFingerCrossing(prevFing, nextFing, starting, landing))
                            fingerCrossings++;
                    }
                }
            }

            if(comfortableButtons.isEmpty()){
                unreachableButtons++;
                startingButtons = landingButtons;
            } else {
                startingButtons = comfortableButtons;
            }
        }

        uncomfortablePositions[0] = unreachableButtons;
        uncomfortablePositions[1] = fingerCrossings;
        return uncomfortablePositions;
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

    private Button findButton(Integer note){
        switch(note % 3){
            case 0: return new Button((note/3)-1, 2);
            case 1: return new Button(note/3, 0);
            case 2: return new Button(note/3, 1);
            default: return null;
        }
    }

    private List<Button> findButtons(Integer note){
        List<Button> buttons = new ArrayList<>();
        Button button = findButton(note);
        buttons.add(button);
        if(button.hasDouble())
            buttons.add(button.getDouble());
        return buttons;
    }
}
