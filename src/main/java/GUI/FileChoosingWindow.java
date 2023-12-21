package GUI;

import logic.Coordinator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class FileChoosingWindow extends JFrame {

    private final Coordinator coordinator;
    private final JTextField path;

    public FileChoosingWindow(Coordinator coordinator){

        this.coordinator = coordinator;

        setTitle("AFAM3");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/iconLogo.png")));

        JLabel label = new JLabel("File selezionato:");
        path = new JTextField(20);
        JButton chooseFileButton = new JButton("Scegli...");
        JButton processButton = new JButton("Aggiungi diteggiatura");

        chooseFileButton.addActionListener(e -> chooseFile());
        processButton.addActionListener(e -> processFile());

        JPanel chooserPanel = new JPanel();

        chooserPanel.setLayout(new FlowLayout());
        chooserPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //statusNotifier.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //processButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //chooserPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
       // statusNotifier.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        //statusNotifier.setBorder(new EmptyBorder(10, 10, 10, 10));
        //statusNotifier.setMargin(new Insets(10,10,10,10));

        chooserPanel.add(label);
        chooserPanel.add(path);
        chooserPanel.add(chooseFileButton);
        chooserPanel.add(processButton);


        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.PAGE_AXIS));
        containerPanel.add(chooserPanel);

        add(containerPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void chooseFile(){

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Scegli il file");
        fileChooser.setFileFilter(new FileNameExtensionFilter("MusicXML", "xml"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            path.setText(selectedFile.getAbsolutePath());
        }
    }

    private void processFile(){

        File file = new File(path.getText());

        if(verifyFile(file)) {
            setVisible(false);
            coordinator.startFileProcessing(file);
        }
    }

    private boolean verifyFile(File file){

        if (file.exists() && file.isFile()) {

            String fileName = file.getName();
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot != -1 && lastDot < fileName.length() - 1){

                String extension = fileName.substring(lastDot + 1).toLowerCase();

                if ("xml".equals(extension))
                    return true;
                else
                   JOptionPane.showMessageDialog(this, "Il file deve essere nel formato MusicXML.", "Errore", JOptionPane.ERROR_MESSAGE);

            }
        } else
            JOptionPane.showMessageDialog(this, "File non trovato.", "Errore", JOptionPane.ERROR_MESSAGE);

        return false;
    }
}
