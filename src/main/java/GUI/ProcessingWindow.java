package GUI;

import logic.Coordinator;

import javax.swing.*;
import java.awt.*;

public class ProcessingWindow extends JFrame {
    private final Coordinator coordinator;
    private JTextArea statusNotifier;

    public ProcessingWindow(Coordinator coordinator){

        this.coordinator = coordinator;

        setTitle("AFAM3");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/iconLogo.png")));

        statusNotifier = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(statusNotifier);

        add(scrollPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void notifyStatus(String status){

        statusNotifier.append(status + "\n");
    }
}
