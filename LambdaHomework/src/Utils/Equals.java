package Utils;

public class Equals {

    public final Expression left;
    public final Expression right;

    public Equals(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Equals equals = (Equals) o;

        if (left != null ? !left.equals(equals.left) : equals.left != null) return false;
        return !(right != null ? !right.equals(equals.right) : equals.right != null);

    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}