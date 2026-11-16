import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;

public final class RegistrationPagingBenchmark {
    private static final int ROW_COUNT = 100_000;
    private static final int PAGE_SIZE = 100;

    private record Row(long id, String studentName) {
    }

    private RegistrationPagingBenchmark() {
    }

    private static List<Row> generateRows(int offset, int count) {
        List<Row> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long id = offset + i + 1L;
            rows.add(new Row(id, new String("Student " + id)));
        }
        return rows;
    }

    private static List<Row> loadAllRows() {
        return generateRows(0, ROW_COUNT);
    }

    private static List<Row> loadFirstPage() {
        return generateRows(0, PAGE_SIZE);
    }

    private static long timeLoadAll() {
        long start = System.nanoTime();
        List<Row> rows = loadAllRows();
        long elapsed = System.nanoTime() - start;
        if (rows.size() != ROW_COUNT) {
            throw new IllegalStateException("Incorrect all-rows result.");
        }
        return elapsed;
    }

    private static long timeFirstPage() {
        long start = System.nanoTime();
        List<Row> rows = loadFirstPage();
        long elapsed = System.nanoTime() - start;
        if (rows.size() != PAGE_SIZE) {
            throw new IllegalStateException("Incorrect page result.");
        }
        return elapsed;
    }

    private static long median(long[] values) {
        java.util.Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long measure(boolean paged) {
        if (paged) {
            timeFirstPage();
        } else {
            timeLoadAll();
        }
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = paged ? timeFirstPage() : timeLoadAll();
        }
        return median(runs);
    }

    private static long usedHeap() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long measureRetainedHeap(boolean paged) throws InterruptedException {
        System.gc();
        Thread.sleep(100);
        long before = usedHeap();
        List<Row> rows = paged ? loadFirstPage() : loadAllRows();
        System.gc();
        Thread.sleep(100);
        long retained = Math.max(0, usedHeap() - before);
        Reference.reachabilityFence(rows);
        return retained;
    }

    public static void main(String[] args) throws InterruptedException {
        long allTime = measure(false);
        long pageTime = measure(true);
        long allMemory = measureRetainedHeap(false);
        long pageMemory = measureRetainedHeap(true);
        System.out.println("strategy,rows_materialized,median_time_to_first_page_ns,retained_heap_delta_bytes");
        System.out.printf("all_rows,%d,%d,%d%n", ROW_COUNT, allTime, allMemory);
        System.out.printf("paged_LIMIT_OFFSET,%d,%d,%d%n", PAGE_SIZE, pageTime, pageMemory);
    }
}
