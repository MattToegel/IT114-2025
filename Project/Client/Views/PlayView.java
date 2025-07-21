package Project.Client.Views;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import Project.Client.Client;
import Project.Common.Phase;

public class PlayView extends JPanel {
    private final JPanel buttonPanel = new JPanel();

    public PlayView(String name){
        this.setName(name);

        // example user interaction
        JButton doSomething = new JButton("Do Something");
        doSomething.addActionListener(_ -> {
            try {
                Client.INSTANCE.sendDoTurn("example");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        buttonPanel.add(doSomething);
        this.add(buttonPanel);
    }
    public void changePhase(Phase phase){
        if (phase == Phase.READY) {
            buttonPanel.setVisible(false);
        } else if (phase == Phase.IN_PROGRESS) {
            buttonPanel.setVisible(true);
        }
    }
    
}
