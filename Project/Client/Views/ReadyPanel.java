package Project.Client.Views;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import Project.Client.Client;

public class ReadyPanel extends JPanel {
    @SuppressWarnings("unused")
    public ReadyPanel() {
        JButton readyButton = new JButton();
        readyButton.setText("Ready");
        readyButton.addActionListener(event -> {
            try {
                Client.INSTANCE.sendReady();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.add(readyButton);
    }
}