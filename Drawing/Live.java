package Drawing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Live extends JPanel {
    static List<Point> coords = new ArrayList<>();
    private static boolean pressed = false;
    private static Point current = new Point();

    public static void main(String[] args) {
        Live drawingPanel = new Live();
        JFrame frame = new JFrame("Drawing Examples Test");
        SwingUtilities.invokeLater(() -> {

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);

            
            frame.add(drawingPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Handle mouse moved event
                int x = e.getX();
                int y = e.getY();
                System.out.println("Mouse moved to: (" + x + ", " + y + ")");
                current.setLocation(x, y);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Handle mouse dragged event (button pressed and moving)
                int x = e.getX();
                int y = e.getY();
                current.setLocation(x, y);
                
            }
        });
        drawingPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Mouse pressed");
                pressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Mouse released");
                pressed = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (pressed) {
                            coords.add(new Point((int) current.getX(), (int) current.getY()));
                         
                        }
                        drawingPanel.repaint();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());
        int cx = (int) (getWidth() * 0.5f);
        int cy = (int) (getHeight() * 0.5f);
        g.setColor(new Color(0,0,0,255));// set alpha back to visible
        g.setColor(Color.BLACK);
        System.out.println("Coords len" + coords.size());
        /*coords.forEach(coord -> {
            g.fillOval((int) (coord.getX() - 1), (int) (coord.getY() - 1), 5,
                    5);
        });*/
        for(int i = 0; i < coords.size(); i++){
            int next = i++;
            if(next > coords.size()){
                next = 0;
            }
            Point p1 = coords.get(i);
            Point p2 = coords.get(next);
            g.drawLine((int)p1.getX(), (int)p1.getY(),
        (int)p2.getX(), (int)p2.getY() );
        }
    }

}
