import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalLogger;

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

        //numberOfConstraints(1);
        numberOfObjectives(3);

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
        integerSolution.objectives()[1] = computeUncomfortablePositions(integerSolution.variables());
        integerSolution.objectives()[2] = computeFingerDistance(integerSolution.variables());

        /*
        int fingerCrossing = computeFingerCrossing();
        integerSolution.getObjectives()[2] = fingerCrossing;*/

        //evaluateConstraints(integerSolution);
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

    public int computeUncomfortablePositions(List<Integer> fingering){
        int uncomfortablePositions = 0;

        //Stabilisco il primo bottone
        List<Button> startingButtons = findButtons(sheet.get(0));

        for(int i=0; i<sheet.size()-1; i++){

            int prevNote = sheet.get(i);
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
                    }
                }
            }

            if(comfortableButtons.isEmpty()){
                uncomfortablePositions++;
                startingButtons = landingButtons;
            } else {
                //startingButtons.clear();
                startingButtons = comfortableButtons;
            }
        }
        return uncomfortablePositions;
    }

    private List<Button> findButtons(Integer note){
        List<Button> buttons = new ArrayList<>();
        if(note%3 == 0){
            for(int i = 0; i < 23; i++){
                if(BUTTONS_MATRIX[i][2]==sheet.get(0)){
                    buttons.add(new Button(i, 2));
                    break;
                }
            }
        }else {
            for (int i = 0; i < 23; i++) {
                for (int j = 0; j < 5; j++) {
                    if (BUTTONS_MATRIX[i][j] == sheet.get(0)) {
                        buttons.add(new Button(i, j));
                        if(buttons.size()==2)
                            break;
                    }
                }
            }
        }
        return buttons;
    }

    /*
    public void evaluateConstraints(IntegerSolution integerSolution){

        List<Integer> fingering = integerSolution.variables();
        int repetitions = 0;
        for(int i=1; i<sheet.size(); i++){
            if(fingering.get(i) == fingering.get(i-1))
                repetitions++;
        }
        integerSolution.constraints()[0] = -1.0 * repetitions;
    }
    */
}
