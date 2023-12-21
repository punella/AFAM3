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

    public static void main(String[] args){

        //Look and feel
        FlatLightLaf.setup();

        coordinator = new Coordinator();
        new FileChoosingWindow(coordinator);
    }

    public void startFileProcessing(File file){

        processingWindow = new ProcessingWindow(coordinator);
        fileHandler = new MusicSheetFileHandler(file);
        processingWindow.notifyStatus("Parsing del file XML...");
        processingWindow.notifyStatus("Calcolo della diteggiatura...");

        FingeringRunner fr = new FingeringRunner(coordinator, fileHandler.getSheetFromFile());
        fr.start();
    }

    public void manageSolution(List<Integer> solution){
        processingWindow.notifyStatus("Diteggiatura ottenuta.");
        processingWindow.notifyStatus("Scrittura della diteggiatura su file...");
        fileHandler.writeFingeringOnFile(solution);
        processingWindow.notifyStatus("File annotato.");
    }
}
