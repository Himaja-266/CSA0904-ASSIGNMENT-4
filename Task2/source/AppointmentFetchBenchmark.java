import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AppointmentFetchBenchmark {
    private static final int DEPARTMENTS = 4;
    private static final int DOCTORS_PER_DEPARTMENT = 10;
    private static final int CHANGES_PER_RUN = 8;

    private AppointmentFetchBenchmark() {
    }

    private static Map<String, List<String>> makeDoctors() {
        Map<String, List<String>> doctors = new LinkedHashMap<>();
        for (int department = 0; department < DEPARTMENTS; department++) {
            List<String> names = new ArrayList<>();
            for (int doctor = 0; doctor < DOCTORS_PER_DEPARTMENT; doctor++) {
                names.add("Doctor-" + department + "-" + doctor);
            }
            doctors.put("Department-" + department, names);
        }
        return doctors;
    }

    private static List<String> fetchFromDatabase(Map<String, List<String>> source, String department)
            throws InterruptedException {
        Thread.sleep(2);
        return new ArrayList<>(source.get(department));
    }

    private static long timeFetchEachChange(Map<String, List<String>> source) throws InterruptedException {
        long start = System.nanoTime();
        for (int i = 0; i < CHANGES_PER_RUN; i++) {
            fetchFromDatabase(source, "Department-" + (i % DEPARTMENTS));
        }
        return System.nanoTime() - start;
    }

    private static long timeLoadedMap(Map<String, List<String>> loaded) {
        long start = System.nanoTime();
        for (int i = 0; i < CHANGES_PER_RUN; i++) {
            loaded.get("Department-" + (i % DEPARTMENTS));
        }
        return System.nanoTime() - start;
    }

    private static long median(long[] values) {
        java.util.Arrays.sort(values);
        return values[values.length / 2];
    }

    private static long[] measureFetchEachChange(Map<String, List<String>> source) throws InterruptedException {
        timeFetchEachChange(source);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeFetchEachChange(source);
        }
        return runs;
    }

    private static long[] measureLoadedMap(Map<String, List<String>> loaded) {
        timeLoadedMap(loaded);
        long[] runs = new long[5];
        for (int i = 0; i < runs.length; i++) {
            runs[i] = timeLoadedMap(loaded);
        }
        return runs;
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, List<String>> source = makeDoctors();
        Map<String, List<String>> loaded = new LinkedHashMap<>(source);
        long fetchMedian = median(measureFetchEachChange(source));
        long mapMedian = median(measureLoadedMap(loaded));
        int cachedDoctorCount = loaded.values().stream().mapToInt(List::size).sum();
        System.out.println("approach,median_ns_per_8_changes,retained_doctor_records");
        System.out.printf("fetch_each_change,%d,0%n", fetchMedian);
        System.out.printf("loaded_map,%d,%d%n", mapMedian, cachedDoctorCount);
    }
}
