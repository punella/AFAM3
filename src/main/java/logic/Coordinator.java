package logic;

import AI.FingeringRunner;
import GUI.FileChoosingWindow;
import GUI.ProcessingWindow;
import com.formdev.flatlaf.FlatLightLaf;

import java.io.File;
import java.util.List;

public class Coordinator {

    private static Coordinator coordinator;
    private static MusicSheetFileHandler fileHandler;
    private static ProcessingWindow processingWindow;
    private static FileChoosingWindow fileChoosingWindow;

    public static void main(String[] args){

        //Look and feel
        FlatLightLaf.setup();

        coordinator = new Coordinator();
        fileChoosingWindow = new FileChoosingWindow(coordinator);
        fileChoosingWindow.setVisible(true);
    }

    public void startFileProcessing(File file){

        processingWindow = new ProcessingWindow(coordinator);
        processingWindow.setVisible(true);
        try {
            fileHandler = new MusicSheetFileHandler(file);
        } catch (NotImplementedYetException e){
            processingWindow.notifyNotImplementedFeature("Il programma non è (ancora) in grado di leggere le alterazioni in chiave.");
        }

        //processingWindow.notifyStatus("Parsing del file XML...");
        //processingWindow.notifyStatus("Calcolo della diteggiatura...");

        FingeringRunner fr = new FingeringRunner(coordinator, fileHandler.getSheetFromFile());
        fr.start();
    }

    public void manageSolution(List<Integer> solution){
        //processingWindow.notifyStatus("Diteggiatura ottenuta.");
        //processingWindow.notifyStatus("Scrittura della diteggiatura su file...");
        fileHandler.writeFingeringOnFile(solution);
        //processingWindow.notifyStatus("File annotato.");
        processingWindow.completeProcess();
    }

    public void uploadAnotherSheet(){
        fileChoosingWindow.setVisible(true);
    }
}
