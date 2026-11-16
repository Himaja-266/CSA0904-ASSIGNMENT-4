import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

public final class LayoutBenchmark {
    private LayoutBenchmark() {
    }

    private static JPanel gridBagPanel(int count) {
        JPanel panel = new JPanel(new GridBagLayout());
        for (int i = 0; i < count; i++) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = i % 50;
            constraints.gridy = i / 50;
            panel.add(new JPanel(), constraints);
        }
        panel.setSize(1000, 1000);
        return panel;
    }

    private static JPanel nullLayoutPanel(int count) {
        JPanel panel = new JPanel(null);
        for (int i = 0; i < count; i++) {
            panel.add(new JPanel());
        }
        panel.setSize(1000, 1000);
        setNullBounds(panel);
        return panel;
    }

    private static void setNullBounds(JPanel panel) {
        Component[] children = panel.getComponents();
        for (int i = 0; i < children.length; i++) {
            children[i].setBounds((i % 50) * 20, (i / 50) * 20, 18, 18);
        }
    }

    private static long timeGridBag(JPanel panel) {
        long start = System.nanoTime();
        panel.doLayout();
        return System.nanoTime() - start;
    }

    private static long timeNullLayout(JPanel panel) {
        long start = System.nanoTime();
        setNullBounds(panel);
        return System.nanoTime() - start;
    }

    private static long median(long[] values) {
        java.util.Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long measureGridBag(JPanel panel) {
        timeGridBag(panel);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeGridBag(panel);
        }
        return median(runs);
    }

    private static long measureNullLayout(JPanel panel) {
        timeNullLayout(panel);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeNullLayout(panel);
        }
        return median(runs);
    }

    public static void main(String[] args) {
        System.out.println("components,gridbag_ns,null_manual_bounds_ns");
        for (int count : new int[]{100, 1_000, 5_000}) {
            JPanel gridBag = gridBagPanel(count);
            JPanel nullLayout = nullLayoutPanel(count);
            System.out.printf("%d,%d,%d%n",
                    count, measureGridBag(gridBag), measureNullLayout(nullLayout));
        }
    }
}
