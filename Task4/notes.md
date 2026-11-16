# Task 4 — DrawPad forensics

## 4.1 Defects

- `mouseclicked` is misspelled; it does not override `mouseClicked`, so click handling never runs.
- The click count and label are never updated, leaving the user without feedback.
- Drawing with `getGraphics()` paints only to the current screen surface and is not retained for later repaints.

## 4.2 Minimize/restore failure

1. Click the canvas; the dot is drawn directly to the current graphics surface.
2. Minimize the window; the operating system may discard that surface.
3. Restore the window or resize it; the component is repainted.
4. `paint()` has no saved point data to redraw, so the dot disappears.

## 4.3 Repaired version

`source/DrawPad.java` stores each click as a `Point`, updates `Clicks: n`, and repaints. Its `Canvas.paint(Graphics)` redraws all stored points with:

```java
for (Point p : points) {
    g.fillOval(p.x - 3, p.y - 3, 6, 6);
}
```

The listener uses `@Override public void mouseClicked(MouseEvent e)`, records the point, updates the label, and requests repaint.

## 4.4 `@Override` demonstration

`OverrideWithoutAnnotation.java` compiles successfully, but `mouseclicked` is only an unrelated overload and is never called for mouse clicks. Adding `@Override` in `OverrideWithAnnotation.java` makes javac report that the method does not override a supertype method. Both compiler outcomes are recorded in `output.txt`.

## Alternative: point list vs off-screen image

The benchmark renders fixed 1000 x 1000 images. It reports median timings from `System.nanoTime()` after one warm-up and five runs. Point storage is estimated at 28 bytes per point (24-byte `Point` plus a 4-byte compressed reference); the image uses 4 bytes per pixel.

| Points n | Point repaint (ns) | Build cached image (ns) | Cached-image repaint (ns) | Point estimate | Image memory |
|---:|---:|---:|---:|---:|---:|
| 1,000 | 2,591,900 | 1,175,000 | 736,500 | 28,000 B | 4,000,000 B |
| 100,000 | 123,928,300 | 120,930,000 | 2,191,400 | 2,800,000 B | 4,000,000 B |
| 1,000,000 | 1,232,034,000 | 1,114,985,700 | 1,615,500 | 28,000,000 B | 4,000,000 B |

Point redraw is O(n) time and O(n) space. A cached image has O(n) one-time build time, then O(1) in n per repaint (copying a fixed W x H image), with O(W x H) pixel space. Image rendering trades fixed pixel memory for much faster repaint at high point counts. Raw compiler and timing output: `output.txt`.
