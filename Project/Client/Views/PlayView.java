package Project.Client.Views;

import Project.Client.Client;
import Project.Common.Phase;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PlayView extends JPanel {
    private final JPanel buttonPanel = new JPanel();
    private final JLabel statusLabel = new JLabel("Waiting for game...", SwingConstants.CENTER);

    // Keep buttons in a map so we can enable/disable them by choice string
    private final Map<String, JButton> optionButtons = new LinkedHashMap<>();
    private String lastChoice = null;  // used for cooldown

    public PlayView(String name){
        this.setName(name);

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.NORTH);

        // 5 options: R, P, S + 2 extras (Lizard, Spock)
        buttonPanel.setLayout(new GridLayout(1, 5, 10, 10));

        createOptionButton("rock",    "Rock");
        createOptionButton("paper",   "Paper");
        createOptionButton("scissors","Scissors");
        createOptionButton("lizard",  "Lizard");
        createOptionButton("spock",   "Spock");

        add(buttonPanel, BorderLayout.CENTER);

        // Initially hidden until Phase.IN_PROGRESS
        buttonPanel.setVisible(false);
    }

    private void createOptionButton(String key, String label) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> sendChoice(key));
        optionButtons.put(key, btn);
        buttonPanel.add(btn);
    }

    private void sendChoice(String choice) {
        // Enforce cooldown if enabled: same option can't be chosen twice in a row
        if (ReadyView.cooldownEnabled && choice.equals(lastChoice)) {
            statusLabel.setText("Cooldown active: you can't pick " + choice + " twice in a row.");
            return;
        }

        try {
            Client.INSTANCE.sendDoTurn(choice);
            statusLabel.setText("You picked: " + choice + " — waiting...");
            lastChoice = choice;
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error sending choice.");
        }

        // Immediately disable that option if cooldown is enabled
        if (ReadyView.cooldownEnabled) {
            JButton btn = optionButtons.get(choice);
            if (btn != null) {
                btn.setEnabled(false);
            }
        }
    }

    public void changePhase(Phase phase){
        if (phase == Phase.READY) {
            // Between rounds / pre-session
            buttonPanel.setVisible(false);
            statusLabel.setText("Waiting for players to be ready...");
        } 
        else if (phase == Phase.IN_PROGRESS) {
            // New round starting
            buttonPanel.setVisible(true);
            statusLabel.setText("Choose your option!");

            // Enable/disable buttons based on settings:

            // 1) Reset all buttons first
            optionButtons.values().forEach(b -> b.setEnabled(true));

            // 2) Hide or show extra options based on ReadyView.useExtendedOptions
            boolean showExtras = ReadyView.useExtendedOptions;
            // Base RPS = rock, paper, scissors
            optionButtons.get("rock").setVisible(true);
            optionButtons.get("paper").setVisible(true);
            optionButtons.get("scissors").setVisible(true);
            // Extras = lizard, spock
            optionButtons.get("lizard").setVisible(showExtras);
            optionButtons.get("spock").setVisible(showExtras);

            // 3) Re-apply cooldown on lastChoice if needed
            if (ReadyView.cooldownEnabled && lastChoice != null) {
                JButton lastBtn = optionButtons.get(lastChoice);
                if (lastBtn != null) {
                    lastBtn.setEnabled(false);
                }
            }

            // Make sure layout updates
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }
    }
}
