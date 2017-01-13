package Utils;

public class Applicative implements Expression {
    private final Expression left;
    private final Expression right;

    public Applicative(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + right + ")";
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Applicative that = (Applicative) o;

        if (getLeft() != null ? !getLeft().equals(that.getLeft()) : that.getLeft() != null) return false;
        return !(getRight() != null ? !getRight().equals(that.getRight()) : that.getRight() != null);

    }

    @Override
    public int hashCode() {
        int result = getLeft() != null ? getLeft().hashCode() : 0;
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        return result;
    }
}
