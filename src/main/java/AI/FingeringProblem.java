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

    private final int[][] BUTTONS_MATRIX = new int[23][5];

    private void instantiateButtonsMatrix(){
        int noteValue = 1;
        for(int i = 0; i < 23; i++){
            for(int j = 0; j < 3; j++){
                BUTTONS_MATRIX[i][j] = noteValue++;
            }
        }
        noteValue = 4;
        for(int i = 0; i < 23; i++){
            for(int j = 3; j < 5; j++){
                BUTTONS_MATRIX[i][j] = noteValue++;
            }
            noteValue++;
        }
    }

    public FingeringProblem(List<Integer> sheet){
        if(sheet.isEmpty())
            throw new IllegalArgumentException("Spartito vuoto.");
        this.sheet = sheet;
        int numberOfVariables = sheet.size();

        instantiateButtonsMatrix();

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

    //Obiettivo modificato rispetto alla progettazione del problema
    //Invece che calcolare la distanza tra i tasti, minimizziamo le occorrenze di soluzioni "non confortevoli"
    //Calcoliamo la distanza tra i bottoni (con entrambe le alternative per i tasti doppioni)
    //Se la soluzione eccede la distanza, incrementiamo il numero di posizioni non confortevoli (da minimizzare)

    public int[] computeUncomfortablePositions(List<Integer> fingering){
        int[] uncomfortablePositions = new int[2];
        int unreachableButtons = 0;
        int fingerCrossings = 0;

        //Stabilisco il primo bottone
        List<Button> startingButtons = findButtons(sheet.get(0));

        for(int i=0; i<sheet.size()-1; i++){

            //int prevNote = sheet.get(i);
            int nextNote = sheet.get(i+1);

            int prevFing = fingering.get(i);
            int nextFing = fingering.get(i+1);

            int maxFingerDistance = FINGER_DISTANCE_MATRIX[prevFing-1][nextFing-1];

            List<Button> landingButtons = findButtons(nextNote);
            List<Button> comfortableButtons = new ArrayList<>();

            for(Button starting : startingButtons){
                for(Button landing : landingButtons){
                    if(starting.computeDistance(landing) <= maxFingerDistance){
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
            if(prevFing!=1 && nextFing>prevFing)
                return true;
        }
        return false;
    }

    private List<Button> findButtons(Integer note){
        List<Button> buttons = new ArrayList<>();
        if(note%3 == 0){
            for(int i = 0; i < 23; i++){
                if(BUTTONS_MATRIX[i][2] == note){
                    buttons.add(new Button(i, 2));
                    break;
                }
            }
        }else {
            for (int i = 0; i < 23; i++) {
                for (int j = 0; j < 5; j++) {
                    if (BUTTONS_MATRIX[i][j] == note) {
                        buttons.add(new Button(i, j));
                        if(buttons.size()==2)
                            break;
                    }
                }
            }
        }
        return buttons;
    }
}
