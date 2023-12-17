package projectnwt2023.backend.devices;

public enum State {
    offline, online;

    public int getNumericValue() {
        return this.ordinal(); // Enum ordinal starts from 0
    }
}
