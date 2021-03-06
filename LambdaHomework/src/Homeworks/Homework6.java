package Homeworks;

import Utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Homework6 {

    private static final String VAR_NAME = "t";
    private static final String FUNC_NAME = "f";

    private static Variable getVariable(int n) {
        return new Variable(VAR_NAME + n);
    }

    private static Expression getArrow(Variable v1, Variable v2) {
        List<Expression> args = new ArrayList<>();
        args.add(v1);
        args.add(v2);
        return new Function(args, FUNC_NAME);
    }

    private static Expression getArrow(int n1, int n2) {
        return getArrow(getVariable(n1), getVariable(n2));
    }


    private static int makeEquations(Expression expression,
                                     List<Equals> system,
                                     AtomicInteger nextNumber,
                                     Map<Variable, Integer> counter,
                                     Map<Variable, Integer> context) {

        if (expression instanceof Variable) {
            if (counter.containsKey(expression)) {
                return counter.get(expression);
            }
            if (context.containsKey(expression)) {
                return context.get(expression);
            }
            int resNumber = nextNumber.getAndIncrement();
            context.put((Variable) expression, resNumber);
            return resNumber;
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            Variable variable = (Variable) it.getVariable();
            Expression statement = it.getStatement();
            int oldNumber = -1;
            if(counter.containsKey(variable))
                oldNumber = counter.get(variable);

            int newNumber = nextNumber.getAndIncrement();
            counter.put(variable, newNumber);
            int stNumber = makeEquations(statement, system, nextNumber, counter, context);
            int resNumber = nextNumber.getAndIncrement();
            system.add(new Equals(getVariable(resNumber), getArrow(newNumber, stNumber)));
            counter.remove(variable);
            if (oldNumber != -1) {
                counter.put(variable, oldNumber);
            }
            return resNumber;
        } else if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            Expression left = it.getLeft();
            Expression right = it.getRight();
            int leftNumber = makeEquations(left, system, nextNumber, counter, context);
            int rightNumber = makeEquations(right, system, nextNumber, counter, context);
            int resNumber = nextNumber.getAndIncrement();
            system.add(new Equals(getVariable(leftNumber), getArrow(rightNumber, resNumber)));
            return resNumber;
        }
        throw new IllegalArgumentException("Unknown type");
    }

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

    private static class SystemException extends RemoteException {
        public SystemException(String s) {
            super(s);
        }
    }

    private static List<Equals> solveSystem(List<Equals> system) throws SystemException {
        repeat:
        while (true) {
            if (system.isEmpty()) break;

            for (Equals eq : system) {
                //1 rule
                if (eq.left.equals(eq.right)) {
                    system = filterEquals(system, eq);
                    continue repeat;
                }

                Expression leftPart = eq.left;
                Expression rightPart = eq.right;

                if (leftPart instanceof Function && rightPart instanceof Function) {
                    Function leftF = (Function) leftPart;
                    Function rightF = (Function) rightPart;

                    //3 rule - conflict
                    if (!leftF.getName().equals(rightF.getName()) || leftF.getArgs().size() != rightF.getArgs().size()) {
                        throw new SystemException("Система неразрешима: " + leftF + " != " + rightF);
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
                if (leftPart instanceof Function && rightPart instanceof Variable) {
                    List<Equals> nextSystem = filterEquals(system, eq);
                    nextSystem.add(new Equals(rightPart, leftPart));
                    system = nextSystem;
                    continue repeat;
                }

                //6 rule (check)
                if (leftPart instanceof Variable) {
                    if (getFreeVariables(rightPart).contains(leftPart)) {
                        throw new SystemException("Система неразрешима: переменная " + leftPart + " входит свободно в " + rightPart);
                    }

                    //5 rule (eliminate)
                    List<Equals> nextSystem = filterEquals(system, eq);

                    boolean isInG = false;
                    for (Equals neq : nextSystem) {
                        if (getFreeVariables(neq.left).contains(leftPart)) {
                            isInG = true;
                            break;
                        }
                        if (getFreeVariables(neq.right).contains(leftPart)) {
                            isInG = true;
                            break;
                        }
                    }

                    if (isInG) {
                        nextSystem = substitute(nextSystem, (Variable) leftPart, rightPart);
                        nextSystem.add(new Equals(leftPart, rightPart));
                        system = nextSystem;
                        continue repeat;
                    }
                }

            }
            break;
        }
        return system;
    }


    private static String parseTermToType(Expression expression) {
        if (expression instanceof Variable) {
            return expression.toString();
        }
        Function function = (Function) expression;
        List<Expression> args = function.getArgs();
        if (args.size() != 2) {
            throw new IllegalArgumentException("Incorrect arguments size in term");
        }
        StringBuilder result = new StringBuilder();
        if (args.get(0) instanceof Variable) {
            result.append(((Variable) args.get(0)).getName());
        } else {
            result.append("(").append(parseTermToType(args.get(0))).append(")");
        }
        result.append("->");
        result.append(parseTermToType(args.get(1)));
        return result.toString();
    }

    public static void main(String[] args) throws ParseException {
        try (
                Scanner in = new Scanner(new File("TestCases/HW6/task6.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW6/task6.out"))
        ) {
            String input = getInput(in);
            Parser.LambdaParser parser = new Parser.LambdaParser();
            Expression expression = parser.parse(input);

            List<Equals> system = new ArrayList<>();
            Map<Variable, Integer> context = new HashMap<>();
            int equationAnswer = makeEquations(expression, system, new AtomicInteger(1), new HashMap<Variable, Integer>(), context);

            try {
                List<Equals> solution = solveSystem(system);

                Map<Expression, Expression> leftToRight = new HashMap<>();
                for (Equals eq : solution) {
                    leftToRight.put(eq.left, eq.right);
                }

                Expression answer = leftToRight.get(getVariable(equationAnswer));
                String resultingType;
                if (answer == null) {
                    resultingType = getVariable(equationAnswer).toString();
                } else {
                    resultingType = parseTermToType(answer);
                }

                out.println(resultingType);

                for (Map.Entry<Variable, Integer> entry : context.entrySet()) {
                    int index = entry.getValue();
                    Expression termExpression = leftToRight.get(getVariable(index));
                    String freeVarType = parseTermToType(termExpression);
                    out.println(entry.getKey() + ":" + freeVarType);
                }

            } catch (SystemException e) {
                out.println("Лямбда-выражение не имеет типа.");
            }

        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static String getInput(Scanner in) {
        StringBuilder input = new StringBuilder();
        while (in.hasNextLine()) {
            input.append(in.nextLine());
        }
        return input.toString().trim();
    }
}
