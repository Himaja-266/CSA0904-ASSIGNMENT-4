import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public final class ActionRegistrationBenchmark {
    private ActionRegistrationBenchmark() {
    }

    private void convert(ActionEvent event) {
    }

    private static long setupMethodReferences(int triggers) {
        long start = System.nanoTime();
        JPanel panel = new JPanel();
        ActionRegistrationBenchmark benchmark = new ActionRegistrationBenchmark();
        Action shortcut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                benchmark.convert(event);
            }
        };
        for (int i = 0; i < triggers; i++) {
            if (i % 2 == 0) {
                JButton button = new JButton();
                button.addActionListener(benchmark::convert);
                panel.add(button);
            } else {
                JMenuItem menuItem = new JMenuItem();
                menuItem.addActionListener(benchmark::convert);
                panel.add(menuItem);
            }
        }
        panel.getActionMap().put("convert", shortcut);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl ENTER"), "convert");
        return System.nanoTime() - start;
    }

    private static long setupSharedAction(int triggers) {
        long start = System.nanoTime();
        JPanel panel = new JPanel();
        ActionRegistrationBenchmark benchmark = new ActionRegistrationBenchmark();
        Action shared = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                benchmark.convert(event);
            }
        };
        for (int i = 0; i < triggers; i++) {
            if (i % 2 == 0) {
                panel.add(new JButton(shared));
            } else {
                panel.add(new JMenuItem(shared));
            }
        }
        panel.getActionMap().put("convert", shared);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl ENTER"), "convert");
        return System.nanoTime() - start;
    }

    private static long median(long[] values) {
        java.util.Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long measure(boolean shared, int triggers) {
        if (shared) {
            setupSharedAction(triggers);
        } else {
            setupMethodReferences(triggers);
        }
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = shared ? setupSharedAction(triggers) : setupMethodReferences(triggers);
        }
        return median(runs);
    }

    public static void main(String[] args) {
        System.out.println("triggers,method_ref_setup_ns,shared_action_setup_ns");
        for (int triggers : new int[]{1, 3, 10}) {
            System.out.printf("%d,%d,%d%n",
                    triggers, measure(false, triggers), measure(true, triggers));
        }
    }
}
