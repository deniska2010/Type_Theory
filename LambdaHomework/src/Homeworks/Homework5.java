package Homeworks;

import Utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Homework5 {

    private static List<Equals> filterEquals(List<Equals> initial, Equals excludeEq) {
        List<Equals> result = new ArrayList<>();
        for (Equals eq : initial) {
            if(!eq.equals(excludeEq)) result.add(eq);
        }
        return result;
    }

    private static Set<Variable> getFreeVariables(Expression expression) {
        HashSet<Variable> set = new HashSet<>();
        if (expression instanceof Variable) {
            set.add((Variable) expression);
        } else {
            Function function = (Function) expression;
            for (Expression var : function.getArgs()) {
                set.addAll(getFreeVariables(var));
            }
        }
        return set;
    }

    private static Expression substitute(Expression expression, Variable termVariable, Expression replacement) {
        if (expression instanceof Variable) {
            if (expression.equals(termVariable)) {
                return replacement;
            }
            return expression;
        } else {
            Function function = (Function) expression;
            List<Expression> newArgs = new ArrayList<>();
            for (Expression arg : function.getArgs()) {
                newArgs.add(substitute(arg, termVariable, replacement));
            }
            return new Function(newArgs, function.getName());
        }
    }

    private static List<Equals> substitute(List<Equals> system, Variable termVariable, Expression replacement) {
        List<Equals> result = new ArrayList<>();
        for (Equals eq : system) {
            Expression sLeft = substitute(eq.left, termVariable, replacement);
            Expression sRight = substitute(eq.right, termVariable, replacement);
            result.add(new Equals(sLeft, sRight));
        }
        return result;
    }

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        try (
                Scanner input = new Scanner(new File("TestCases/HW5/task5.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW5/task5.out"))) {

            Parser.TermParser parser = new Parser.TermParser();

            List<Equals> system = new ArrayList<>();

            while (input.hasNextLine()) {
                String line = input.nextLine();
                int index = line.indexOf("=");
                String subTerm1 = line.substring(0, index);
                String subTerm2 = line.substring(index + 1);
                Expression term1 = parser.parse(subTerm1);
                Expression term2 = parser.parse(subTerm2);
                system.add(new Equals(term1, term2));
            }

            repeat:
            while (true) {
                if (system.isEmpty()) break;

                for (Equals eq : system) {
                    //1 rule
                    if (eq.left.equals(eq.right)) {
                        system = filterEquals(system, eq);
                        continue repeat;
                    }

                    Expression left = eq.left;
                    Expression right = eq.right;

                    if (left instanceof Function && right instanceof Function) {
                        Function leftF = (Function) left;
                        Function rightF = (Function) right;

                        //3 rule - conflict
                        if (!leftF.getName().equals(rightF.getName()) || leftF.getArgs().size() != rightF.getArgs().size()) {
                            out.println("Система неразрешима: " + leftF + " != " + rightF);
                            return;
                        }

                        //2 rule - decompose
                        List<Equals> nextSystem = filterEquals(system, eq);
                        List<Expression> lefts = leftF.getArgs();
                        List<Expression> rights = rightF.getArgs();

                        for (int i = 0; i < lefts.size(); i++) {
                            nextSystem.add(new Equals(lefts.get(i), rights.get(i)));
                        }

                        system = nextSystem;
                        continue repeat;
                    }

                    //4 rule (swap)
                    if (left instanceof Function && right instanceof Variable) {
                        List<Equals> nextSystem = filterEquals(system, eq);
                        nextSystem.add(new Equals(right, left));
                        system = nextSystem;
                        continue repeat;
                    }

                    //6 rule (check)
                    if (left instanceof Variable) {
                        if (getFreeVariables(right).contains(left)) {
                            out.println("Система неразрешима: переменная " + left + " входит свободно в " + right);
                            return;
                        }

                        //5 rule (eliminate)
                        List<Equals> nextSystem = filterEquals(system, eq);

                        boolean isInG = false;
                        for (Equals neq : nextSystem) {
                            if (getFreeVariables(neq.left).contains(left)) {
                                isInG = true;
                                break;
                            }
                            if (getFreeVariables(neq.right).contains(left)) {
                                isInG = true;
                                break;
                            }
                        }

                        if (isInG) {
                            nextSystem = substitute(nextSystem, (Variable) left, right);
                            nextSystem.add(new Equals(left, right));
                            system = nextSystem;
                            continue repeat;
                        }
                    }

                }
                break;
            }

            for (Equals eq : system) {
                out.println(eq.left + "=" + eq.right);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
