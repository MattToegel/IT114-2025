package Drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class DrawingExamples extends JPanel {
    private int numCharacters = 0;
    private int type = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drawing Examples Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);

            DrawingExamples drawingPanel = new DrawingExamples();
            frame.add(drawingPanel, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(2, 2, 10, 10));

            JTextField numCharactersField = new JTextField("0");

            JButton updateCharactersButton = new JButton("Set Number of Characters");
            updateCharactersButton.addActionListener(e -> {
                try {
                    int num = Integer.parseInt(numCharactersField.getText());
                    drawingPanel.setNumberOfCharacters(num);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number.");
                }
            });

            String[] types = { "None", "Start Door", "End Door", "Wall" };
            JComboBox<String> typeComboBox = new JComboBox<>(types);

            JButton updateTypeButton = new JButton("Set Type");
            updateTypeButton.addActionListener(e -> {
                drawingPanel.setType(typeComboBox.getSelectedIndex());
            });

            controlPanel.add(new JLabel("Number of Characters:"));
            controlPanel.add(numCharactersField);
            controlPanel.add(updateCharactersButton);
            controlPanel.add(typeComboBox);
            controlPanel.add(updateTypeButton);

            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.setVisible(true);
        });
    }

    public void setNumberOfCharacters(int num) {
        numCharacters = num;
        this.repaint();
    }

    public void setType(int type) {
        this.type = type;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cx = (int) (getWidth() * 0.5f);
        int cy = (int) (getHeight() * 0.5f);

        if (numCharacters > 0) {
            for (int i = 0; i < numCharacters; i++) {
                int x = 50 + (i * 50);
                drawStickFigure(g, x, cy);
            }
        }

        if (type == 1 || type == 2) {
            drawArchedDoor(g, cx, cy, 100, 100, type == 1 ? Color.GREEN : Color.BLUE);
        } else if (type == 3) {
            drawWall(g, cx, cy, 100, 100);
        }
    }

    private void drawStickFigure(Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        g.fillOval(x - 15, y - 15, 30, 30);
        g.drawLine(x, y + 15, x, y + 55);
        g.drawLine(x, y + 25, x - 20, y + 35);
        g.drawLine(x, y + 25, x + 20, y + 35);
        g.drawLine(x, y + 55, x - 20, y + 85);
        g.drawLine(x, y + 55, x + 20, y + 85);
    }

    private void drawArchedDoor(Graphics g, int x, int y, int width, int height, Color doorColor) {
        int topLeftX = x - (int) (width * 0.5);
        int topLeftY = y - (int) (height * 0.5);

        g.setColor(Color.GRAY);
        g.fillRect(topLeftX, topLeftY, width, height);

        g.setColor(doorColor);
        g.fillArc(topLeftX, topLeftY, width, height, 0, 180);

        g.setColor(Color.RED);
        g.fillOval(topLeftX + (int) (width * 0.25) - 2, topLeftY + (int) (height * 0.5) - 2, 4, 4);
        g.fillOval(topLeftX + (int) (width * 0.75) - 2, topLeftY + (int) (height * 0.5) - 2, 4, 4);
    }

    private void drawWall(Graphics g, int x, int y, int width, int height) {
        g.setColor(new Color(139, 69, 19));
        int brickWidth = (int) (width * 0.1);
        int brickHeight = (int) (height * 0.05);
        int topLeftX = x - (int) (width * 0.5);
        int topLeftY = y - (int) (height * 0.5);

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                int brickX = topLeftX + j * brickWidth;
                int brickY = topLeftY + i * brickHeight;

                g.fillRect(brickX, brickY, brickWidth, brickHeight);

                g.setColor(Color.BLACK);
                g.drawRect(brickX, brickY, brickWidth, brickHeight);

                g.setColor(new Color(139, 69, 19));
            }
        }
    }
}
