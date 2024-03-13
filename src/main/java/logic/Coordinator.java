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

    public static void main(String[] args) throws ClassNotFoundException {

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
            FingeringRunner fr = new FingeringRunner(coordinator, fileHandler.getSheetFromFile());
            fr.start();
        } catch (NotImplementedYetException e){
            processingWindow.notifyNotImplementedFeature(e.getMessage());
        }
    }

    public void manageSolution(List<Integer> solution){
        fileHandler.writeFingeringOnFile(solution);
        processingWindow.completeProcess();
    }

    public void uploadAnotherSheet(){
        fileChoosingWindow.setVisible(true);
    }
}
