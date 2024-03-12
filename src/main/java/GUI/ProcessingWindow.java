package GUI;

import logic.Coordinator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProcessingWindow extends JFrame {
    private final Coordinator coordinator;
    private JProgressBar progressBar;
    private JLabel label;
    private JButton uploadAnotherSheetButton, exitButton;

    public ProcessingWindow(Coordinator coordinator){

        this.coordinator = coordinator;

        setTitle("AFAM3");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/iconLogo.png")));

        Box box = new Box(BoxLayout.Y_AXIS);
        EmptyBorder border = new EmptyBorder(20,20,20,20);
        box.setBorder(border);
        add(box);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        progressBar.setBorder(border);
        label = new JLabel("Sto processando...");
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        buttonPanel.setBorder(border);

        uploadAnotherSheetButton = new JButton("Carica un altro spartito");
        exitButton = new JButton("Esci");
        uploadAnotherSheetButton.setEnabled(false);
        exitButton.setEnabled(false);
        uploadAnotherSheetButton.addActionListener(e -> uploadAnotherSheet());
        exitButton.addActionListener(e -> exit());

        buttonPanel.add(uploadAnotherSheetButton);
        buttonPanel.add(exitButton);

        box.add(Box.createVerticalGlue());
        box.add(progressBar);
        box.add(label);
        box.add(buttonPanel);
        box.add(Box.createVerticalGlue());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public void completeProcess(){
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressBar.setForeground(Color.GREEN);
        label.setForeground(Color.GREEN);
        label.setText("Processo completato!");
        uploadAnotherSheetButton.setEnabled(true);
        exitButton.setEnabled(true);
    }

    public void notifyNotImplementedFeature(String missingFeature){
        setVisible(false);
        String[] options = {"Scegli un altro spartito", "Esci"};
        int choice = JOptionPane.showOptionDialog(this,
                "Il programma non è (ancora) in grado di " + missingFeature, "Feature mancante",
                0,0, null, options, options[1]);
        if(choice==0){
            uploadAnotherSheet();
        }
        if(choice==1){
            exit();
        }
    }

    private void uploadAnotherSheet(){
        setVisible(false);
        coordinator.uploadAnotherSheet();
    }

    private void exit(){
        System.exit(0);
    }
}
