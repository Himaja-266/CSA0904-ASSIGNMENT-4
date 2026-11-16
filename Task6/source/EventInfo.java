public record EventInfo(long id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
