package com.vdoichev.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Calculator {
    private static final String REGEX_FOR_VALIDATE = "[^\\d.\\-+/*\\s()]";
    private static final String REGEX_VALIDATE_BRACKETS = "\\([\\d+/*-.\\s]*\\)";
    private static final String REGEX_VALIDATE_BETWEEN_BRACKETS = "[\\d+/*-.\\s]*";
    private static final String REGEX_VALIDATE_COUNT_OPERATORS = "[+/*-]{3,}";
    private static final String REGEX_VALIDATE_SECOND_OPERATOR = "[+/*]{2,}";
    private static final String REGEX_FOR_EXTRACT_NUMBERS = "[^\\d.]+";
    private static final String REGEX_FOR_EXTRACT_OPERATORS = "[^-+/*]+";

    private List<String> operatorsList;
    private int countOperatorsInList = 0;
    private List<Double> numbersList;
    private int countNumbersInList = 0;
    private int countNumbers = 0;

    public int getCountNumbers() {
        return countNumbers;
    }

    public void setCountNumbers(int countNumbers) {
        this.countNumbers = countNumbers;
    }

    public int getCountNumbersInList() {
        return countNumbersInList;
    }

    public void setCountNumbersInList(int countNumbersInList) {
        this.countNumbersInList = countNumbersInList;
        if (getCountNumbers() > 0) {
            setCountNumbers(getCountNumbers() - 1 + getCountNumbersInList());
        } else {
            setCountNumbers(getCountNumbersInList());
        }
    }

    public int getCountOperatorsInList() {
        return countOperatorsInList;
    }

    public void setCountOperatorsInList(int countOperatorsInList) {
        this.countOperatorsInList = countOperatorsInList;
    }

    private boolean isNotSingleArgument() {
        return numbersList.size() > 1;
    }

    /**
     * **************************************************
     * методи відповідальні за разразхунок
     * **************************************************
     */

    public Double calculate(String expression) {
        if (validateExpression(expression)) {
            if (expression.indexOf('(') != -1) {
                return calculateWithBrackets(expression);
            } else {
                return calculateWithoutBrackets(expression);
            }
        } else {
            System.out.println("Incorrect expression!");
            return null;
        }
    }

    private double calculateWithBrackets(String expression) {
        Double result;
        Pattern pattern = Pattern.compile(REGEX_VALIDATE_BRACKETS);
        do {
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                String localExpression = matcher.group();
                result = calculateWithoutBrackets(localExpression);
                expression = matcher.replaceFirst(result.toString());
            }
        } while ((expression.indexOf('(') != -1));
        result = calculateWithoutBrackets(expression);
        return result;
    }

    private Double calculateWithoutBrackets(String expression) {
        transformExpressionToLists(expression);
        if (numbersList.size() > 0) {
            while (isNotSingleArgument()) {
                int i = getIndexByPriority(operatorsList);
                double result = operate(operatorsList.get(i), numbersList.get(i - 1), numbersList.get(i));
                numbersList.set(i - 1, result);
                numbersList.remove(i);
                operatorsList.remove(i);
            }
            return numbersList.get(0);
        } else {
            return Double.parseDouble(expression);
        }
    }

    public int getIndexByPriority(List<String> operatorsList) {
        for (int i = 1; i < operatorsList.size(); i++) {
            if (operatorsList.get(i).equals("*") ||
                    operatorsList.get(i).equals("/")) {
                return i;
            }
        }
        for (int i = 1; i < operatorsList.size(); i++) {
            if (operatorsList.get(i).equals("+") ||
                    operatorsList.get(i).equals("-")) {
                return i;
            }
        }

        return 0;
    }

    private double operate(String operator, Double firstNumber, Double secondNumber) {
        switch (operator) {
            case "+":
                return firstNumber + secondNumber;
            case "-":
                return firstNumber - secondNumber;
            case "*":
                return firstNumber * secondNumber;
            case "/":
                return firstNumber / secondNumber;
            default:
                return firstNumber;
        }
    }

    /**
     * ***********************************************************
     * методи відповідальні за перетворення виразу у листи
     * ***********************************************************
     **/
    private void transformExpressionToLists(String expression) {
        operatorsList = extractOperators(expression);
        setCountOperatorsInList(operatorsList.size());
        numbersList = extractNumbers(expression);
        setCountNumbersInList(numbersList.size());
    }

    private List<Double> stringListToDoubleList(List<String> numbersStringList) {
        List<Double> doubleList = new ArrayList<>();
        for (int i = 0; i < operatorsList.size(); i++) {
            int length = operatorsList.get(i).length();

            if (length == 2 && operatorsList.get(i).charAt(1) == '-') {
                replaceToNegativeNumber(numbersStringList, i, length);
            }
            if (length == 1 && i == 0 && operatorsList.get(i).charAt(0) == '-') {
                replaceToNegativeNumber(numbersStringList, i, length);
            }
            doubleList.add(Double.parseDouble(numbersStringList.get(i)));
        }
        return doubleList;
    }

    private void replaceToNegativeNumber(List<String> numbersStringList, int index, int operatorLength) {
        String str = "-" + numbersStringList.get(index);
        numbersStringList.set(index, str);
        str = (operatorLength == 2) ? operatorsList.get(index).substring(0, 1) : "";
        operatorsList.set(index, str);
    }

    private List<Double> extractNumbers(String expression) {
        return stringListToDoubleList(correctNumbersArray(expression.split(REGEX_FOR_EXTRACT_NUMBERS)));
    }

    private List<String> correctNumbersArray(String[] extractNumbers) {
        return Arrays
                .stream(extractNumbers)
                .filter(x -> x.length() > 0)
                .collect(Collectors.toList());
    }

    private static List<String> extractOperators(String expression) {
        return new ArrayList<>(Arrays.asList(expression.split(REGEX_FOR_EXTRACT_OPERATORS)));
    }

    /**
     * ***********************************************************
     * методи відповідальні за валідацію виразу
     * ***********************************************************
     **/

    public boolean validateExpression(String expression) {
        Pattern pattern = Pattern.compile(REGEX_FOR_VALIDATE);
        Matcher matcher = pattern.matcher(expression);
        return !matcher.find() &&
                validateBrackets(expression) &&
                validateCountOperators(expression) &&
                validateSecondOperator(expression);
    }

    private static boolean validateBrackets(String expression) {
        Pattern pattern = Pattern.compile(REGEX_VALIDATE_BRACKETS);
        Matcher matcher = pattern.matcher(expression);
        do {
            expression = matcher.replaceAll("");
            matcher = pattern.matcher(expression);
        } while (matcher.find());
        return expression.matches(REGEX_VALIDATE_BETWEEN_BRACKETS);
    }

    private static boolean validateCountOperators(String expression) {
        Pattern pattern = Pattern.compile(REGEX_VALIDATE_COUNT_OPERATORS);
        Matcher matcher = pattern.matcher(expression);

        return !matcher.find();
    }

    private static boolean validateSecondOperator(String expression) {
        Pattern pattern = Pattern.compile(REGEX_VALIDATE_SECOND_OPERATOR);
        Matcher matcher = pattern.matcher(expression);

        return !matcher.find();
    }
}
