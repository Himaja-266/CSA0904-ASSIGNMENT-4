# Task 2 — Hospital appointment desk

## 2.1 Event table

| Source | Event object | Listener | Action |
|---|---|---|---|
| Department `Choice` | `ItemEvent` | `ItemListener` | Reload doctors, then available slots |
| Doctor `Choice` | `ItemEvent` | `ItemListener` | Reload the doctor's slots |
| Slot `List` | `ItemEvent` | `ItemListener` (optional selection feedback) | Track selected appointment time |
| Book `Button` | `ActionEvent` | `ActionListener` | Validate selections and confirm booking |
| Window | `WindowEvent` | `WindowAdapter` | Dispose the frame on close |

## 2.2 Containment tree

```text
AppointmentDesk (java.awt.Frame, BorderLayout)
├── selectors (java.awt.Panel)
│   ├── java.awt.Label + departmentChoice (java.awt.Choice)
│   └── java.awt.Label + doctorChoice (java.awt.Choice)
├── center (java.awt.Panel, BorderLayout)
│   ├── java.awt.Label (NORTH)
│   └── slotList (java.awt.List, CENTER)
└── footer (java.awt.Panel, BorderLayout)
    ├── bookButton (java.awt.Button, WEST)
    └── status (java.awt.Label, CENTER)
```

## 2.3 Pseudocode

```text
1. START
2. INPUT department selection event
3. CALL refreshDoctors
4. CLEAR doctor choices
5. FOR each doctor in Map[department]
6.   ADD doctor to doctor Choice
7. ENDFOR
8. CALL refreshSlots
9. END
```

```text
1. START
2. INPUT Book action
3. READ selected doctor and slot
4. IF doctor is missing OR slot is missing
5.   OUTPUT selection error
6. ELSE
7.   OUTPUT booking confirmation for doctor and slot
8.   ENDIF
9. END
```

## 2.4 Window event trace and applet comparison

1. Constructor creates controls and registers listeners.
2. `setVisible(true)` requests display.
3. `windowOpened` fires after the frame is shown.
4. `windowActivated` fires when the frame becomes the active window.
5. `windowClosing` fires when the user requests closing.
6. `dispose()` releases the native peer; `windowClosed` fires afterward.

- `init()` initializes an applet once; an application constructs its frame directly.
- `start()` begins/resumes applet activity; a frame is shown with `setVisible(true)`.
- `stop()`/`destroy()` manage applet shutdown; a frame handles `windowClosing` and `dispose()`.

## Alternative: database fetch on every change vs loaded Map

The benchmark simulated a 2 ms fetch delay for each of 8 department changes, using `System.nanoTime()`, one warm-up, and five measured runs (median). The preloaded dataset contains 4 departments and 40 doctors.

| Approach | Median time per 8 changes | Retained doctor records |
|---|---:|---:|
| Fetch on each change | 23.24 ms | 0 cached (temporary query result only) |
| Loaded once into `Map` | 0.022 ms | 40 |

Fetching costs O(L) per change for simulated latency L; lookup from a Map is O(1) average. The cache uses O(D) memory for D doctors; fetching does not retain a cache. Times are machine/JVM dependent. Raw output: `output.txt`.
