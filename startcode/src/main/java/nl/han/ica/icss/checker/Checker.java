package nl.han.ica.icss.checker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.types.*;

public class Checker {

    private int currentScope;
    private LinkedList<HashMap<String,ExpressionType>> variableTypes;

    public void check(AST ast) {
        currentScope = 0;
        variableTypes = new LinkedList<>();
        variableTypes.add(new HashMap<>());

        //CHeck de kinderen van de AST of te wel de ICSS.
        for (ASTNode node : ast.root.getChildren()) {
            CH01DefinedVariables(node);
            CH02OperandsAreEqual(node);
            CH03NoColorLiteralInOperation(node);
            CH04DeclarationContainsRightLiteral(node);
            CH05CheckIfBoolean(node);
            CH06CheckIfVariablesInScope();
        }
    }
    
    //CH01|Controleer of er geen variabelen worden gebruikt die niet gedefinieerd zijn.
    public void CH01DefinedVariables(ASTNode node) {
        if (node instanceof Stylerule) {
            currentScope++;
            variableTypes.add(new HashMap<>());
        }
        if (node instanceof VariableAssignment) {
            variableTypes.get(currentScope).put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        }
        //Check of een variabel niet refeert na een variabel die niet gedefinieerd is.
        if (node instanceof VariableReference) {
            if (!variableTypes.get(currentScope).containsKey(((VariableReference) node).name) && !variableTypes.get(0).containsKey(((VariableReference) node).name)) {
                node.setError("Variable not defined!");
            }
        }
        //Voer de functie recursief uit met de kind van de ASTNode
        for (ASTNode nodes : node.getChildren()) {
            CH01DefinedVariables(nodes);
        }
    }

    //Functie om de type van een Expression te bepalen.
    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        return null;
    }

    //CH02|Controleer of de operanden van de operaties plus en min van gelijk type zijn en dat vermenigvuldigen enkel met scalaire waarden gebeurt.
    // Je mag geen pixels bij percentages optellen bijvoorbeeld.
    public void CH02OperandsAreEqual(ASTNode node){
        Set<ExpressionType> literalsInOperation = new HashSet<ExpressionType>();
        if (node instanceof Operation){
            traverseThroughOperationAndGetLiteralTypes((Operation) node, literalsInOperation);
        }
        //Check of het een vermenigvuldiging operatie is.
        if (node instanceof MultiplyOperation){
            if (!checkIfOnlyScalar(literalsInOperation)) {
                node.setError("Can't multiply one or two non scalar values!");
            }
        }
        //Check of het een plus of minus operatie is
        if (node instanceof AddOperation || node instanceof SubtractOperation){
            if (!checkIfOnlyOneLiteralType(literalsInOperation)) {
                node.setError("Operands are not of same type!");
            }
        }
        //Voer de check recursief uit met de kinderen.
        for (ASTNode nodes : node.getChildren()){
            CH02OperandsAreEqual(nodes);
        }

    }

    //Check of er niet meer types van literal zijn.
    private boolean checkIfOnlyOneLiteralType(Set<ExpressionType> literals) {
        if (literals.contains(null)) {
            literals.remove(null);
        }
        if (literals.size() > 1) {
            return false;
        }
        return true;
    }

    //Check voor scalar type.
    private boolean checkIfOnlyScalar(Set<ExpressionType> literals) {
        if (literals.contains(null)) {
            literals.remove(null);
        }

        for (ExpressionType type : literals) {
            if (type != ExpressionType.SCALAR) {
                return false;
            }
        }
        return true;
    }

    //Doorzoek de boom en haal alle types van literals
    private void traverseThroughOperationAndGetLiteralTypes(Operation operation, Set<ExpressionType> list) {
        if (operation.lhs instanceof Literal) {
            list.add(getExpressionType((Expression) operation.lhs));
        } else if (operation.lhs instanceof VariableReference) {
            addExpressionTypeOfReferenceToList((VariableReference) operation.lhs, list);
        } else {
            traverseThroughOperationAndGetLiteralTypes((Operation) operation.lhs, list);
        }

        if (operation.rhs instanceof Literal) {
            list.add(getExpressionType((Expression) operation.rhs));
        } else if (operation.rhs instanceof VariableReference) {
            addExpressionTypeOfReferenceToList((VariableReference) operation.rhs, list);
        } else {
            traverseThroughOperationAndGetLiteralTypes((Operation) operation.rhs, list);
        }
        if (list.contains(null)) {
            list.remove(null);
        }
    }

    private void addExpressionTypeOfReferenceToList(VariableReference ref, Set<ExpressionType> list) {
        for (HashMap<String, ExpressionType> maps : variableTypes) {
            if (maps.containsKey(ref.name)) {
                ExpressionType type = maps.get(ref.name);
                list.add(type);
            }
        }
    }

    //CH03|Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
    public void CH03NoColorLiteralInOperation(ASTNode node) {
        //Check of astnode een operatie is
        if (node instanceof Operation) {
            //Check of left hand side of right hand side een color literal is, zoja geef error.
            if (((Operation) node).lhs instanceof ColorLiteral || ((Operation) node).rhs instanceof ColorLiteral) {
                node.setError("A color is being used in an "+ node.getNodeLabel() + " operation");
            }
        }
        //Voer functie recursief uit met kinderen.
        for (ASTNode nodes : node.getChildren()) {
            CH03NoColorLiteralInOperation(nodes);
        }
    }

    //CH04|Controleer of bij declaraties het type van de value klopt met de property. Declaraties zoals width: `#ff0000` of `color: 12px` zijn natuurlijk onzin.
    public void CH04DeclarationContainsRightLiteral(ASTNode node) {
        if (node instanceof Declaration) {
            String propertyName = ((Declaration) node).property.name; //haal naam van property op.
            if (((Declaration) node).expression instanceof Operation) {
                Set<ExpressionType> literalsInOperation = new HashSet<ExpressionType>();
                Expression e = ((Declaration) node).expression;
                traverseThroughOperationAndGetLiteralTypes((Operation) e, literalsInOperation); //Haal de literal types op
                if (literalsInOperation.size() != 1) { //Als er meer dan een type literal is, geef error.
                    node.setError("The declaration contains an amount of expression types unequal to 1");
                } else {
                    if (!checkIfAllowedPropertyExpressionTypeCombination(literalsInOperation.iterator().next(), propertyName)) { //if only 1 expression
                        node.setError("There is an illegal combination of property "+ propertyName+" and an declaration ");  //Check first item of hashset
                    }
                }
            }
            //Check de property of juiste type gebruikt
            if (((Declaration) node).expression instanceof Literal) {
                ExpressionType type = getExpressionType(((Declaration) node).expression);
                if (!checkIfAllowedPropertyExpressionTypeCombination(type, propertyName)) {
                    node.setError("There is an illegal combination of property "+ propertyName+" and an declaration ");
                }
            }
            //Check de variabel of juiste type gebruikt
            if (((Declaration) node).expression instanceof VariableReference) {
                VariableReference reference = (VariableReference) ((Declaration) node).expression;
                ExpressionType type = variableTypes.get(0).get(reference.name);
                if (!checkIfAllowedPropertyExpressionTypeCombination(type, propertyName)) {
                    node.setError("There is an illegal combination of property "+ propertyName+" and an declaration ");
                }
            }
        }
        //Check de kinderen recursief
        for (ASTNode nodes : node.getChildren()) {
            CH04DeclarationContainsRightLiteral(nodes);
        }
    }

    //Functie om een property te checken voor de juiste type waarden.
    private boolean checkIfAllowedPropertyExpressionTypeCombination(ExpressionType expressionType, String propertyName) {
        if ((propertyName.equals("width") || propertyName.equals("height"))) {
            if(expressionType == ExpressionType.SCALAR || expressionType == ExpressionType.PIXEL || expressionType == ExpressionType.PERCENTAGE){
                return true;
            }
        } else if ((propertyName.equals("background-color") || propertyName.equals("color"))) {
            if(expressionType == ExpressionType.COLOR){
                return true;
            }
        }
        return false;
    }

    //CH05|Controleer of de conditie bij een if-statement van het type boolean is (zowel bij een variabele-referentie als een boolean literal)
    public void CH05CheckIfBoolean(ASTNode node) {
        if (node instanceof IfClause) {
            Expression e = ((IfClause) node).conditionalExpression;
            //Check als het een variabel is
            if (e instanceof VariableReference) {
                for (HashMap<String, ExpressionType> map : variableTypes) {
                    if (map.containsKey(((VariableReference) e).name)) {
                        if (map.get(((VariableReference) e).name) != ExpressionType.BOOL) { //Als het geen boolean expression is, geef error
                            node.setError("A non boolean variable is being used as boolean in if statement: "
                                            + map.get(((VariableReference) e).name));
                        }
                    }
                }
            } else if (!(e instanceof BoolLiteral)) {
                node.setError("Not a boolean condition in if statement!");
            }
        }
        //Check de kinderen recursief.
        for (ASTNode child : node.getChildren()) {
            CH05CheckIfBoolean(child);
        }
    }



    //CH06|Controleer of variabelen enkel binnen hun scope gebruikt worden
    public void CH06CheckIfVariablesInScope(){

    }



}
