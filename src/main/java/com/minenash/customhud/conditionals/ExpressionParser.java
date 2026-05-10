package com.minenash.customhud.conditionals;

import com.minenash.customhud.CustomHud;
import com.minenash.customhud.HudElements.list.ListProviderSet;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.VariableParser;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.ErrorException;
import com.minenash.customhud.errors.ErrorType;
import com.minenash.customhud.errors.Errors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import com.minenash.customhud.util.Tuple;

@SuppressWarnings("DuplicatedCode")
public class ExpressionParser {

    public enum TokenType { START_PREN, END_PREN, FULL_PREN, FUNCTION, AND, OR, MATH, COMPARISON, NUMBER, STRING, BOOLEAN, VARIABLE, NEGATED_VARIABLE, IF, ELSE, TERNARY }
    public enum Comparison { LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUALS, EQUALS, NOT_EQUALS, HAS, IS_IN, NOT_HAS, NOT_IS_IN }
    public enum MathOperator { ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD, EXPONENT, IF_NULL }
    public record TernaryTokens(List<Token> conditional, List<Token> left, List<Token> right) {}

    record Token(TokenType type, Object value) {
        public String toString() {
            return type + (value == null ? "" : " (" + value + ")");
        }
    }

    public static Operation parseExpression(String input, String source, Profile profile, int debugLine, ComplexData.Enabled enabled, ListProviderSet listSuppliers, boolean forCondition) {
        if (input.isBlank() || input.equals(",") || input.equals(", "))
            return forCondition ? new Operation.Literal(1) : new Operation.Literal(0);
        try {
            List<Token> tokens = getTokens(input, profile, debugLine, enabled, listSuppliers);
            Operation c = getConditional(tokens, forCondition);
            CustomHud.logInDebugMode("Tree for Conditional on line " + debugLine + ":");
            c.printTree(0);
            CustomHud.logInDebugMode("");
            return c;
        }
        catch (ErrorException e) {
            Errors.addError(profile.name, debugLine, source, e.type, e.context);
            CustomHud.logInDebugMode("[Line: " + debugLine + "] Conditional Couldn't Be Parsed: " + e.getMessage());
            CustomHud.logInDebugMode("Input: \"{" + input + "}\"");
            return forCondition ? new Operation.Literal(1) : new Operation.Literal(0);
        }
    }

    private static final List<TokenType> SUBTRACTABLE = List.of(TokenType.NUMBER, TokenType.BOOLEAN,
            TokenType.STRING, TokenType.VARIABLE, TokenType.END_PREN);
    private static List<Token> getTokens(String original, Profile profile, int debugLine, ComplexData.Enabled enabled, ListProviderSet listSuppliers) throws ErrorException {

        List<Token> tokens = new ArrayList<>();
        char[] chars = original.toCharArray();



        for (int i = 0; i < chars.length;) {
            char c = chars[i];
            if (c == '(') tokens.add(new Token(TokenType.START_PREN, null));
            else if (c == ')') tokens.add(new Token(TokenType.END_PREN, null));
            else if (c == '?' && i+1 != chars.length && chars[i+1] == '?') {tokens.add(new Token(TokenType.MATH, MathOperator.IF_NULL)); i++;}
            else if (c == '?') tokens.add(new Token(TokenType.IF, null));
            else if (c == ';') tokens.add(new Token(TokenType.ELSE, null));
            else if (c == '∧' || c == '⋀') tokens.add(new Token(TokenType.AND, null));
            else if (c == '∨' || c == '⋁') tokens.add(new Token(TokenType.OR, null));
            else if (c == '|') {
                if (i + 1 != chars.length && chars[i + 1] == '|') i++;
                tokens.add(new Token(TokenType.OR, null));
            }
            else if (c == '&') {
                if (i + 1 != chars.length && chars[i + 1] == '&') i++;
                tokens.add(new Token(TokenType.AND, null));
            }
            else if (c == '=') {
                if (i + 1 != chars.length && chars[i + 1] == '=') i++;
                tokens.add(new Token(TokenType.COMPARISON, Comparison.EQUALS));
            }
            else if (c == '+') tokens.add(new Token(TokenType.MATH, MathOperator.ADD));
            else if (c == '*') tokens.add(new Token(TokenType.MATH, MathOperator.MULTIPLY));
            else if (c == '/') tokens.add(new Token(TokenType.MATH, MathOperator.DIVIDE));
            else if (c == '%') tokens.add(new Token(TokenType.MATH, MathOperator.MOD));
            else if (c == '^') tokens.add(new Token(TokenType.MATH, MathOperator.EXPONENT));
            else if (c == '-' && i+1 < chars.length && (!tokens.isEmpty() && SUBTRACTABLE.contains(tokens.get(tokens.size() - 1).type)))
                tokens.add(new Token(TokenType.MATH, MathOperator.SUBTRACT));
            else if (c == '!') {
                if (i + 1 == chars.length)
                    throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "!");
                if (chars[i + 1] == '=') {
                    tokens.add(new Token(TokenType.COMPARISON, Comparison.NOT_EQUALS));
                    i++;
                }
                else if (chars[i + 1] == '(') {
                    tokens.add(new Token(TokenType.START_PREN, NEGATE));
                    i++;
                }
                else if (chars[i + 1] == 'h' && i + 1 + 2 < chars.length && (original.startsWith("has ", i+1) || original.startsWith("has(", i+1))) {
                    tokens.add(new Token(TokenType.COMPARISON, Comparison.NOT_HAS));
                    i+=4;
                    continue;
                }
                else if (isVar(chars[i + 1])) {
                    i += parseVariable(tokens, chars, i, profile, debugLine, enabled, listSuppliers) - 1;
                }
                else
                    throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "!");
            }
            else if (c == '>') {
                boolean hasEqual =  i + 1 != chars.length && chars[i + 1] == '=';
                tokens.add(new Token(TokenType.COMPARISON, hasEqual ? Comparison.GREATER_THAN_OR_EQUALS : Comparison.GREATER_THAN));
                i += hasEqual ? 2 : 1;
                continue;
            }
            else if (c == '<') {
                boolean hasEqual =  i + 1 != chars.length && chars[i + 1] == '=';
                tokens.add(new Token(TokenType.COMPARISON, hasEqual ? Comparison.LESS_THAN_OR_EQUAL : Comparison.LESS_THAN));
                i += hasEqual ? 2 : 1;
                continue;
            }
            else if (c == 'h' && i + 2 < chars.length && (original.startsWith("has ", i) || original.startsWith("has(", i))) {
                tokens.add(new Token(TokenType.COMPARISON, Comparison.HAS));
                i+=3;
                continue;
            }
            else if (c == '∈' || c == '∊') tokens.add(new Token(TokenType.COMPARISON, Comparison.IS_IN));
            else if (c == '∉') tokens.add(new Token(TokenType.COMPARISON, Comparison.NOT_IS_IN));
            else if (c == '∋' || c == '∍') tokens.add(new Token(TokenType.COMPARISON, Comparison.HAS));
            else if (c == '∌') tokens.add(new Token(TokenType.COMPARISON, Comparison.NOT_HAS));
            else if (c == '√' && i+1 != chars.length && chars[i+1] == '(') {
                tokens.add(new Token(TokenType.START_PREN, (Function<Double,Double>)Math::sqrt));
                i++;
            }
            else if (c == 'f' && i + 4 < chars.length && original.startsWith("false", i)) {
                tokens.add(new Token(TokenType.BOOLEAN, false));
                i+=5;
                continue;
            }
            else if (c == 't' && i + 3 < chars.length && original.startsWith("true", i)) {
                tokens.add(new Token(TokenType.BOOLEAN, true));
                i+=4;
                continue;
            }
            else if (c == '"') {
                StringBuilder builder = new StringBuilder();
                i++;
                while (i < chars.length && chars[i] != '"')
                    builder.append(chars[i++]);
                tokens.add(new Token(TokenType.STRING, builder.toString()));
            }
            else if ((isNum(c) && c != '.') || c == '-') {
                StringBuilder builder = new StringBuilder();
                builder.append(chars[i++]);
                while (i < chars.length && isNum(chars[i]))
                    builder.append(chars[i++]);

                tokens.add(new Token(TokenType.NUMBER, Double.parseDouble(builder.toString())));
                continue;
            }
            else if (isVar(c)) {
                i += parseVariable(tokens, chars, i, profile, debugLine, enabled, listSuppliers);
                continue;
            }
            i++;

        }

//        for (Token token : tokens)
//            System.out.println("[A]" + token);

        int start = -1;
        for (int i = 0; i < tokens.size(); i++) {
            TokenType type = tokens.get(i).type();
            if (type == TokenType.START_PREN) {
                start = i;
            }
            else if (type == TokenType.END_PREN) {
                reducePren(tokens, start, i);
                start = -1;
                i = -1;
            }
        }

        reduceTernary0(tokens);

//        System.out.println("---------------");
//        for (Token token : tokens)
//            System.out.println("[B]" + token);
        return tokens;

    }

    private static boolean isNum(char c) {
        return c == '.' || (c >= '0' && c <= '9');
    }
    private static boolean isVar(char c) {
        return c == '.' || c == ':' || c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '[' || c == ']' || c == ',' || c == 'π' || c == 'τ' || c == 'φ';
    }

    private static int parseVariable(List<Token> tokens, char[] chars, int i, Profile profile, int debugLine, ComplexData.Enabled enabled, ListProviderSet listSuppliers) {
        Tuple<Token,Integer> func = getFunctionStart(chars, i);
        if (func != null) {
            tokens.add(func.getA());
            return func.getB();
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');
        int offset = 0;

        boolean negate = false;
        if (chars[i] == '!') {
            negate = true;
            i++;
            offset++;
        }

        while (i < chars.length && isVar(chars[i])) {
            builder.append(chars[i]);
            i++;
            offset++;
        }
        builder.append('}');

        HudElement element = VariableParser.parseElement(builder.toString(), profile, debugLine, enabled, listSuppliers);
        if (element == null)
            tokens.add(new Token(TokenType.BOOLEAN, false));
        else
            tokens.add(new Token(negate ? TokenType.NEGATED_VARIABLE : TokenType.VARIABLE, element));
        return offset;
    }


    private static final double DR = 0.017453292519943295;
    private static final double RD = 57.29577951308232;
    private static final Function<Double,Double> NEGATE = (in) -> in > 0 ? 0.0 : 1.0;
    private static final Function<Double,Double> SIN  = (in) -> round (Math.sin(in * DR) );
    private static final Function<Double,Double> COS  = (in) -> round (Math.cos(in * DR) );
    private static final Function<Double,Double> TAN  = (in) -> round (Math.tan(in * DR) );
    private static final Function<Double,Double> CSC  = (in) -> round (1 / (Math.sin(in * DR)) );
    private static final Function<Double,Double> SEC  = (in) -> round (1 / (Math.cos(in * DR)) );
    private static final Function<Double,Double> COT  = (in) -> round (1 / (Math.tan(in * DR)) );
    private static final Function<Double,Double> ASIN = (in) -> round (RD * Math.asin(in) );
    private static final Function<Double,Double> ACOS = (in) -> round (RD * Math.acos(in) );
    private static final Function<Double,Double> ATAN = (in) -> round (RD * Math.atan(in) );
    private static final Function<Double,Double> ACSC = (in) -> round (RD * Math.asin(1 / in) );
    private static final Function<Double,Double> ASEC = (in) -> round (RD * Math.acos(1 / in) );
    private static final Function<Double,Double> ACOT = (in) -> round (RD * Math.atan(1 / in) );
    private static final Function<Double,Double> ROUND = (in) -> (double) Math.round(in);
    private static final Function<Double,Double> LOG_2 = (in) -> Math.log(in) / Math.log(2);
    private static double round(double in) { return Math.round(in * 100000) / 100000D; }

    private static Tuple<Token,Integer> getFunctionStart(char[] chars, int i) {
        int pren = -1;
        for (int j = i; j < chars.length; j++) {
            if (chars[j] == '(') {
                pren = j;
                break;
            }
            if (!isFunc(chars[j]) )
                return null;
        }
        if (pren == -1)
            return null;

        String funcStr = new String(Arrays.copyOfRange(chars, i, pren));
        Function<Double,Double> func = switch ( funcStr ) {
            case "sin" -> SIN;
            case "cos" -> COS;
            case "tan" -> TAN;
            case "csc" -> CSC;
            case "sec" -> SEC;
            case "cot" -> COT;
            case "asin" -> ASIN;
            case "acos" -> ACOS;
            case "atan" -> ATAN;
            case "acsc" -> ACSC;
            case "asec" -> ASEC;
            case "acot" -> ACOT;

            case "round" -> ROUND;
            case "ceil" -> Math::ceil;
            case "floor" -> Math::floor;
            case "ln", "log_e" -> Math::log;
            case "ln1p", "log_e1p" -> Math::log1p;
            case "log2" -> LOG_2;
            case "log", "log10" -> Math::log10;
            case "sqrt" -> Math::sqrt;
            case "abs" -> Math::abs;


            default -> null;
        };
        return func == null ? null : new Tuple<>(new Token(TokenType.START_PREN, func), funcStr.length()+1);

    }
    private static boolean isFunc(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }


    private static void reducePren(List<Token> original, int start, int end) {
        Function<Double,Double> func = (Function<Double, Double>) original.get(start).value;
        if (func == null)
            original.set(start, new Token(TokenType.FULL_PREN, new ArrayList<>(original.subList(start+1, end))));
        else
            original.set(start, new Token(TokenType.FUNCTION, new Tuple<>( func, new ArrayList<>(original.subList(start+1, end)))));
        for (; end > start; end--)
            original.remove(end);
    }

    private static void reduceTernary0(List<Token> original) {
        for (Token token : original)
            if (token.type == TokenType.FULL_PREN)
                reduceTernary0( (List<Token>) token.value );
        reduceTernary(original);
    }

    private static void reduceTernary(List<Token> original) {
        boolean foundIf;
        do {
            int start = 0;
            int ifIndex = -1;
            int elseIndex = -1;
            foundIf = false;

            for (int i = 0; i < original.size(); i++) {
                TokenType type = original.get(i).type();
                if (type == TokenType.IF) {
                    foundIf = true;
                    start = elseIndex != -1 ? elseIndex + 1 : ifIndex + 1;
                    ifIndex = i;
                    elseIndex = -1;
                } else if (type == TokenType.ELSE && ifIndex != -1) {
                    if (elseIndex == -1) {
                        elseIndex = i;
                    } else {
                        List<Token> conditional = new ArrayList<>(original.subList(start, ifIndex));
                        List<Token> left = new ArrayList<>(original.subList(ifIndex + 1, elseIndex));
                        List<Token> right = new ArrayList<>(original.subList(elseIndex + 1, i));
                        Token ter = new Token(TokenType.TERNARY, new TernaryTokens(conditional, left, right));
                        original.set(start, ter);
                        while (--i > start)
                            original.remove(i);
                        i = -1;
                        start = 0;
                        ifIndex = elseIndex = -1;
                    }
                }
            }
            if (elseIndex != -1) {
                List<Token> conditional = new ArrayList<>(original.subList(start, ifIndex));
                List<Token> left = new ArrayList<>(original.subList(ifIndex + 1, elseIndex));
                List<Token> right = new ArrayList<>(original.subList(elseIndex + 1, original.size()));
                Token ter = new Token(TokenType.TERNARY, new TernaryTokens(conditional, left, right));
                original.set(start, ter);
                if (original.size() > start + 1)
                    original.subList(start + 1, original.size()).clear();
            } else if (ifIndex != -1) {
                List<Token> conditional = new ArrayList<>(original.subList(start, ifIndex));
                List<Token> left = new ArrayList<>(original.subList(ifIndex + 1, original.size()));
                List<Token> right = Collections.emptyList();
                Token ter = new Token(TokenType.TERNARY, new TernaryTokens(conditional, left, right));
                original.set(start, ter);
                if (original.size() > start + 1)
                    original.subList(start + 1, original.size()).clear();
            }
        } while (foundIf);



    }

    private static Operation getConditional(List<Token> tokens, boolean forCondition) throws ErrorException {
        if (tokens.isEmpty())
            throw new ErrorException(ErrorType.EMPTY_SECTION, "");
        List<List<Token>> ors = split(tokens, TokenType.OR);
        List<Operation> conditionals = new ArrayList<>();
        for (var or : ors)
            conditionals.add(getAndConditional(or));

        if (conditionals.size() == 1) {
            Operation o = conditionals.get(0);
            return forCondition && o instanceof Operation.Element oe ? new Operation.ElementUseBool(oe.element()) : o;
        }
        return new Operation.Or(conditionals);

    }

    private static Operation getAndConditional(List<Token> tokens) throws ErrorException {
        List<List<Token>> ands = split(tokens, TokenType.AND);
        List<Operation> conditionals = new ArrayList<>();
        for (var and : ands)
            conditionals.add(getComparisonOperation(and));

        return conditionals.size() == 1 ? conditionals.get(0) : new Operation.And(conditionals);
    }

    private static Operation getComparisonOperation(List<Token> tokens) throws ErrorException {
        if (tokens.size() == 1)
            return getPrimitiveOperation(tokens.get(0));

        int comparatorIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.COMPARISON) {
                comparatorIndex = i;
                break;
            }
        }

        if (comparatorIndex == -1)
            return getMathOperation(tokens);
        else if (comparatorIndex == 0 )
            throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No values on the left of comparison");
        else if (comparatorIndex == tokens.size()-1)
            throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No values on the right of comparison");

        HudElement left = comparatorIndex == 1 ?
                getValueElement(tokens.get(0)) :
                new SudoElements.Op(getMathOperation(tokens.subList(0, comparatorIndex)));
        HudElement right = comparatorIndex == tokens.size()-2 ?
                getValueElement(tokens.get(tokens.size()-1)) :
                new SudoElements.Op(getMathOperation(tokens.subList(comparatorIndex+1, tokens.size())));

        return new Operation.Comparison(left, right, (Comparison) tokens.get(comparatorIndex).value());
    }

    private static Operation getMathOperation(List<Token> tokens) throws ErrorException {
        if (tokens.size() == 1)
            return getPrimitiveOperation(tokens.get(0));
        if (tokens.size() == 2) {
            if (tokens.get(0).type == TokenType.MATH)
                throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No value on left side of operator");
            else if (tokens.get(1).type == TokenType.MATH)
                throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No value on right side of operator");
            else
                throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No operation");
        }

        Tuple<List<List<Token>>, List<MathOperator>> ifNullPairs = split(tokens, List.of(MathOperator.IF_NULL));
        List<Operation> ops0 = new ArrayList<>();

        for (var partTokens0 : ifNullPairs.getA()) {
            Tuple<List<List<Token>>, List<MathOperator>> addPairs = split(partTokens0, List.of(MathOperator.ADD, MathOperator.SUBTRACT));
            List<Operation> ops1 = new ArrayList<>();

            for (var partTokens : addPairs.getA()) {
                Tuple<List<List<Token>>, List<MathOperator>> multiplyPairs = split(partTokens, List.of(MathOperator.MULTIPLY, MathOperator.DIVIDE, MathOperator.MOD));
                List<Operation> ops2 = new ArrayList<>();

                for (var partPartToken : multiplyPairs.getA()) {
                    Tuple<List<List<Token>>, List<MathOperator>> exponentPairs = split(partPartToken, List.of(MathOperator.EXPONENT));
                    List<HudElement> elements = new ArrayList<>();
                    for (var partPartPartToken : exponentPairs.getA()) {

                        if (partPartPartToken.size() > 1)
                            throw new ErrorException(ErrorType.CONDITIONAL_WRONG_NUMBER_OF_TOKENS, "No operation between values");
                        elements.add( getValueElement(partPartPartToken.get(0)) );
                    }
                    if (elements.size() == 1)
                        ops2.add(new Operation.Element(elements.get(0)));
                    else
                        ops2.add(new Operation.MathOperation(elements, exponentPairs.getB()));
                }
                ops1.add(ops2.size() == 1 ? ops2.get(0) : new Operation.MathOperationsOp(ops2, multiplyPairs.getB()));
            }
            ops0.add( ops1.size() == 1 ? ops1.get(0) : new Operation.MathOperationsOp(ops1, addPairs.getB()) );
        }
        return ops0.size() == 1 ? ops0.get(0) : new Operation.MathOperationsOp(ops0, ifNullPairs.getB());

    }

    @SuppressWarnings("unchecked")
    private static HudElement getValueElement(Token token) throws ErrorException {
        return switch (token.type()) {
            case VARIABLE -> (HudElement) token.value();
            case NEGATED_VARIABLE -> new SudoElements.Op(new Operation.Negate((HudElement) token.value()));
            case STRING -> new SudoElements.Str((String) token.value());
            case NUMBER -> new SudoElements.Num((Number) token.value());
            case BOOLEAN -> new SudoElements.Bool((Boolean) token.value());
            case FULL_PREN -> new SudoElements.Op(getConditional((List<Token>) token.value(), false));
            case FUNCTION -> {
                Tuple<Function<Double,Double>,List<Token>> pair = (Tuple<Function<Double, Double>, List<Token>>) token.value();
                yield new SudoElements.Op(new Operation.Func(pair.getA(), getConditional(pair.getB(), false)));
            }
            case TERNARY -> {
                TernaryTokens tokens = (TernaryTokens) token.value();
                yield new SudoElements.Op( new Operation.Ternary(
                        getConditional( tokens.conditional, true),
                        getConditional( tokens.left, false),
                        getConditional( tokens.right, false)
                ) );
            }
            case IF -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "? (IF)");
            case ELSE -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "; (ELSE)");

            default -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, token.type().toString());
        };
    }

    @SuppressWarnings("unchecked")
    private static Operation getPrimitiveOperation(Token token) throws ErrorException {
        return switch (token.type) {
            case FULL_PREN -> getConditional((List<Token>) token.value(), false);
            case BOOLEAN -> new Operation.Literal(((Boolean) token.value()) ? 1 : 0);
            case NUMBER -> new Operation.Literal((Double) token.value());
            case VARIABLE -> new Operation.Element((HudElement) token.value());
            case NEGATED_VARIABLE -> new Operation.Negate((HudElement) token.value());
            case FUNCTION -> {
                Tuple<Function<Double,Double>,List<Token>> pair = (Tuple<Function<Double, Double>, List<Token>>) token.value();
                yield new Operation.Func(pair.getA(), getConditional(pair.getB(), false));
            }
            case TERNARY -> {
                TernaryTokens tokens = (TernaryTokens) token.value();
                yield new Operation.Ternary(
                        getConditional( tokens.conditional, true),
                        getConditional( tokens.left, false),
                        getConditional( tokens.right, false)
                );
            }
            case IF -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "? (IF)");
            case ELSE -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, "; (ELSE)");

            default -> throw new ErrorException(ErrorType.CONDITIONAL_UNEXPECTED_VALUE, token.type().toString());
        };
    }

    private static List<List<Token>> split(List<Token> tokens, TokenType type) {
        List<List<Token>> sections = new ArrayList<>();
        List<Token> current = new ArrayList<>();

        for (Token token : tokens) {
            if (token.type() == type) {
                sections.add(current);
                current = new ArrayList<>();
            }
            else
                current.add(token);
        }
        sections.add(current);
        return sections;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static Tuple<List<List<Token>>, List<MathOperator>> split(List<Token> tokens, List<MathOperator> ops) {
        List<List<Token>> sections = new ArrayList<>();
        List<MathOperator> operators = new ArrayList<>();
        List<Token> current = new ArrayList<>();

        for (Token token : tokens) {
            if ( token.type == TokenType.MATH && ops.contains(token.value())) {
                sections.add(current);
                operators.add((MathOperator) token.value());
                current = new ArrayList<>();
            }
            else
                current.add(token);
        }
        sections.add(current);
        return new Tuple<>(sections, operators);
    }



}