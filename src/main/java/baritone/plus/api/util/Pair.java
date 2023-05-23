package baritone.plus.api.util;

public record Pair<A, B>(A left, B right) {

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public A left() {
        return left;
    }

    public B right() {
        return right;
    }

    public String toString(String separator) {
        return String.format("%s%s%s", this.left, separator, this.right);
    }
}