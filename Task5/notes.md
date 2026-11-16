# Task 5 — Currency converter handler styles

## 5.1 Four handler versions

| File | Handler style |
|---|---|
| `source/CurrencyConverterActionListener.java` | Frame implements `ActionListener` |
| `source/CurrencyConverterNamedInner.java` | Named inner `ConvertHandler` |
| `source/CurrencyConverterAnonymous.java` | Anonymous inner listener |
| `source/CurrencyConverterLambda.java` | Lambda listener |

All four handle close events with `WindowAdapter`. They convert USD using fixed demonstration rates for EUR, GBP, and JPY.

## 5.2 Enter conversion and result dialog

Each final version converts on both the Convert button and Enter in the amount field, updates the result label, and displays the result (or invalid-amount message) in a `JOptionPane`.

| Version | Commit | `git diff --stat` |
|---|---|---|
| Button-only baseline | `2c14dac` | 4 files changed, 272 insertions(+) |
| Enter + dialog update | second Task5 commit | 4 files changed, 26 insertions(+), 13 deletions(-) |

The second row is the working-tree `git diff --stat` from the baseline commit, captured before the update commit.

## 5.3 Handler comparison

| Style | Readability | Field access | Reuse | Ease of change |
|---|---|---|---|---|
| Frame implements `ActionListener` | Clear for a tiny frame; event source branching can grow | Direct instance access | Frame itself can be registered on multiple sources | Easy initially, increasingly coupled as actions grow |
| Named inner class | Explicit and easy to find | Direct private access | Good; one object can serve several triggers | Best of these for adding substantial handler logic |
| Anonymous inner class | Handler stays near registration but is verbose | Lexical access | Limited; reusable only if stored | Easy for one small, local handler |
| Lambda | Concise for one operation | Lexical access | Reusable when stored in a variable | Easy for simple behavior; extract a class as logic grows |

For large applications, prefer a named `Action` or named handler: it can carry command name, enabled state, icon, shortcut, and behavior in one reusable unit.

## Alternative: method reference vs shared Swing `Action`

```java
button.addActionListener(this::convert);
amount.addActionListener(this::convert);
```

```java
Action convertAction = new AbstractAction("Convert") {
    @Override public void actionPerformed(ActionEvent e) { convert(); }
};
button.setAction(convertAction);
menuItem.setAction(convertAction);
getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("ctrl ENTER"), "convert");
getRootPane().getActionMap().put("convert", convertAction);
```

The setup benchmark uses `System.nanoTime()`, one warm-up and five runs, and reports median nanoseconds for 1, 3, and 10 trigger controls. It includes control creation and a keyboard binding.

| Triggers | Method-reference setup (ns) | Shared-Action setup (ns) | Handler objects |
|---:|---:|---:|---|
| 1 | 373,800 | 435,000 | 1 listener vs 1 Action |
| 3 | 447,700 | 460,000 | 3 listeners vs 1 Action |
| 10 | 941,700 | 966,800 | 10 listeners vs 1 Action |

Both require O(k) setup time and O(k) trigger bindings/components. Method-reference handlers use O(k) listener objects; a shared Action uses O(1) action objects plus O(k) bindings. Small timing differences are JVM/UI-component overhead, not a meaningful speed claim. Raw output: `output.txt`.
