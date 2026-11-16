public final class DispatchBenchmark {
    private static volatile int sink;

    private DispatchBenchmark() {
    }

    private static int chainDispatch(int action, int count) {
        for (int candidate = 0; candidate < count; candidate++) {
            if (action == candidate) {
                return candidate;
            }
        }
        return -1;
    }

    private static int listenerDispatch(int action, int[] listeners) {
        return listeners[action];
    }

    private static long timeChain(int[] actions, int count) {
        long start = System.nanoTime();
        int value = 0;
        for (int action : actions) {
            value += chainDispatch(action, count);
        }
        sink = value;
        return System.nanoTime() - start;
    }

    private static long timeListener(int[] actions, int[] listeners) {
        long start = System.nanoTime();
        int value = 0;
        for (int action : actions) {
            value += listenerDispatch(action, listeners);
        }
        sink = value;
        return System.nanoTime() - start;
    }

    private static long medianOfFive(long[] values) {
        java.util.Arrays.sort(values);
        return values[2];
    }

    private static double benchmarkChain(int[] actions, int count) {
        timeChain(actions, count);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeChain(actions, count);
        }
        return (double) medianOfFive(runs) / actions.length;
    }

    private static double benchmarkListener(int[] actions, int[] listeners) {
        timeListener(actions, listeners);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeListener(actions, listeners);
        }
        return (double) medianOfFive(runs) / actions.length;
    }

    public static void main(String[] args) {
        System.out.println("k,iterations,if_else_ns_per_dispatch,listener_ns_per_dispatch");
        for (int count : new int[]{10, 1_000, 10_000}) {
            int[] listeners = new int[count];
            for (int i = 0; i < count; i++) {
                listeners[i] = i;
            }
            int iterations = Math.max(2_000, 2_000_000 / count);
            int[] actions = new int[iterations];
            java.util.Random random = new java.util.Random(2026L + count);
            for (int i = 0; i < actions.length; i++) {
                actions[i] = random.nextInt(count);
            }
            System.out.printf("%d,%d,%.2f,%.2f%n",
                    count,
                    iterations,
                    benchmarkChain(actions, count),
                    benchmarkListener(actions, listeners));
        }
    }
}
