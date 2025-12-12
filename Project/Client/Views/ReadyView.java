package Project.Client.Views;

import Project.Client.Client;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReadyView extends JPanel {

    // These static flags let PlayView know what options to use.
    // They're simple and local to the client – no server changes required for now.
    public static boolean useExtendedOptions = false;  // RPS-5 vs RPS-3
    public static boolean cooldownEnabled = false;     // same-option cooldown

    public ReadyView() {
        // basic stacked layout
        setLayout(new GridLayout(0, 1, 5, 5));

        // --- RPS mode toggle (3 vs 5 options) ---
        add(new JLabel("Game Options:"));

        JCheckBox extendedOptionsCheck = new JCheckBox("Use extra options (RPS-5)");
        extendedOptionsCheck.addActionListener(e -> {
            useExtendedOptions = extendedOptionsCheck.isSelected();
        });
        add(extendedOptionsCheck);

        // --- Cooldown toggle ---
        JCheckBox cooldownCheck = new JCheckBox("Enable cooldown (no same pick twice in a row)");
        cooldownCheck.addActionListener(e -> {
            cooldownEnabled = cooldownCheck.isSelected();
        });
        add(cooldownCheck);

        // --- Existing Ready button (unchanged behavior) ---
        JButton readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            try {
                Client.INSTANCE.sendReady();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        add(readyButton);
    }
}
