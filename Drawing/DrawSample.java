package Drawing;

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DrawSample extends JPanel {
    private int circleX = 0;

    public void setCircleX(int x) {
        this.circleX = x;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillOval(circleX, getHeight() / 2, 50, 50);
    }

    public DrawSample() {
        Thread drawingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                circleX = 0;
                while (true) {
                    circleX += 5;
                    if (circleX > getWidth()) {
                        circleX = 0;
                    }
                    SwingUtilities.invokeLater(() -> repaint());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        drawingThread.start();
        setSize(400, 400);
        setVisible(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Draw Sample");
        DrawSample ds = new DrawSample();
        frame.add(ds);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}