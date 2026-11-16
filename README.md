# CO4 Java Assignment

**Name:** ______________________________  
**Register number:** ______________________________

**Java version:** Java 17 or newer. Verified with OpenJDK 25.0.4.1.

Run commands from the repository root in PowerShell. Each task compiles into its own output folder so default-package class names do not collide.

## Task 1 — FeeWindow

```powershell
javac -d out\Task1 Task1\source\*.java
java -cp out\Task1 FeeWindow
java -cp out\Task1 DispatchBenchmark
```

The UI defaults to the in-memory repository. `JdbcFeeRepository` is available for a configured `DataSource`.

## Task 2 — Appointment desk

```powershell
javac -d out\Task2 Task2\source\*.java
java -cp out\Task2 AppointmentDesk
java -cp out\Task2 AppointmentFetchBenchmark
```

## Task 3 — Library search

```powershell
javac -d out\Task3 Task3\source\*.java
java -cp out\Task3 LibrarySearch
java -cp out\Task3 LayoutBenchmark
```

## Task 4 — DrawPad

```powershell
javac -d out\Task4 Task4\source\DrawPad.java Task4\source\DrawPadRenderBenchmark.java Task4\source\OverrideWithoutAnnotation.java
java -cp out\Task4 DrawPad
java -cp out\Task4 DrawPadRenderBenchmark
```

`OverrideWithAnnotation.java` is intentionally expected not to compile; see `Task4\output.txt`.

## Task 5 — Currency converter

```powershell
javac -d out\Task5 Task5\source\*.java
java -cp out\Task5 CurrencyConverterLambda
```

Run `CurrencyConverterActionListener`, `CurrencyConverterNamedInner`, or `CurrencyConverterAnonymous` to compare the other handler styles. The baseline and updated versions are preserved in Git history.

## Task 6 — Event registration

```powershell
javac -d out\Task6 Task6\source\*.java
java -cp "out\Task6;mysql-connector-j.jar" MainFrame
java -cp out\Task6 RegistrationPagingBenchmark
```

From the repository root, copy `Task6\db.properties.example` to `db.properties`, set local credentials, and run `Task6\schema.sql` in MySQL. Add the MySQL Connector/J jar to the runtime classpath to use MySQL. Without a usable configuration/connection, the app reports that it is using in-memory data.
