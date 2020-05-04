public class Pair<T, U> {
    public T first;
    public U second;

    public Pair(T t, U u) {
        this.first = t;
        this.second = u;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}