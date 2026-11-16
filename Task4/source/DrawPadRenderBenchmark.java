import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class DrawPadRenderBenchmark {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;

    private DrawPadRenderBenchmark() {
    }

    private static Point[] makePoints(int count) {
        Point[] points = new Point[count];
        Random random = new Random(41L + count);
        for (int i = 0; i < count; i++) {
            points[i] = new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        return points;
    }

    private static long drawPoints(Point[] points) {
        BufferedImage target = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setColor(Color.BLACK);
        long start = System.nanoTime();
        for (Point point : points) {
            graphics.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
        long elapsed = System.nanoTime() - start;
        graphics.dispose();
        return elapsed;
    }

    private static long buildCache(Point[] points) {
        BufferedImage cache = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cache.createGraphics();
        graphics.setColor(Color.BLACK);
        long start = System.nanoTime();
        for (Point point : points) {
            graphics.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
        long elapsed = System.nanoTime() - start;
        graphics.dispose();
        return elapsed;
    }

    private static BufferedImage renderCache(Point[] points) {
        BufferedImage cache = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cache.createGraphics();
        graphics.setColor(Color.BLACK);
        for (Point point : points) {
            graphics.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
        graphics.dispose();
        return cache;
    }

    private static long copyCache(BufferedImage cache) {
        BufferedImage target = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D targetGraphics = target.createGraphics();
        long start = System.nanoTime();
        targetGraphics.drawImage(cache, 0, 0, null);
        long elapsed = System.nanoTime() - start;
        targetGraphics.dispose();
        return elapsed;
    }

    private static long median(long[] values) {
        java.util.Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long measure(RenderOperation operation) {
        operation.run();
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = operation.run();
        }
        return median(runs);
    }

    @FunctionalInterface
    private interface RenderOperation {
        long run();
    }

    public static void main(String[] args) {
        System.out.println("points,direct_ns_per_repaint,cache_build_ns,cache_copy_ns_per_repaint,point_est_bytes,image_bytes");
        for (int count : new int[]{1_000, 100_000, 1_000_000}) {
            Point[] points = makePoints(count);
            long direct = measure(() -> drawPoints(points));
            long build = measure(() -> buildCache(points));
            BufferedImage cache = renderCache(points);
            long copy = measure(() -> copyCache(cache));
            long estimatedPointBytes = count * 28L;
            long imageBytes = WIDTH * (long) HEIGHT * Integer.BYTES;
            System.out.printf("%d,%d,%d,%d,%d,%d%n",
                    count, direct, build, copy, estimatedPointBytes, imageBytes);
        }
    }
}
