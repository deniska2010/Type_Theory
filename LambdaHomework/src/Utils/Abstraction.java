package Utils;

public class Abstraction implements Expression {
    private final Expression variable;
    private final Expression statement;

    public Abstraction(Expression variable, Expression statement) {
        this.variable = variable;
        this.statement = statement;
    }

    public Expression getVariable() {
        return variable;
    }

    public Expression getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return "(" + "\\" + variable.toString() + "." + statement.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Abstraction that = (Abstraction) o;

        if (getVariable() != null ? !getVariable().equals(that.getVariable()) : that.getVariable() != null)
            return false;
        return !(getStatement() != null ? !getStatement().equals(that.getStatement()) : that.getStatement() != null);

    }

    @Override
    public int hashCode() {
        int result = getVariable() != null ? getVariable().hashCode() : 0;
        result = 31 * result + (getStatement() != null ? getStatement().hashCode() : 0);
        return result;
    }
}
