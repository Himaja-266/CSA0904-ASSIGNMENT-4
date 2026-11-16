import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public final class DrawPad extends Frame {
    private final List<Point> points = new ArrayList<>();
    private final Label status = new Label("Clicks: 0");

    public DrawPad() {
        super("DrawPad");
        add(new DrawingCanvas(), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
        setSize(640, 480);
        setLocationRelativeTo(null);
    }

    private final class DrawingCanvas extends Canvas {
        private DrawingCanvas() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    points.add(e.getPoint());
                    status.setText("Clicks: " + points.size());
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.BLACK);
            for (Point p : points) {
                g.fillOval(p.x - 3, p.y - 3, 6, 6);
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new DrawPad().setVisible(true));
    }
}
