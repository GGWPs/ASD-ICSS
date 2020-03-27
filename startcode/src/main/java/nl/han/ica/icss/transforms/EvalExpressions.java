package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.HashMap;
import java.util.LinkedList;

public class EvalExpressions implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;
    private int currentScope;


    //TR01|Implementeer de `EvalExpressions` transformatie. Deze transformatie vervangt alle `Expression` knopen in de AST door een `Literal` knoop met de berekende waarde.
    public EvalExpressions() {
        variableValues = new LinkedList<>();
        variableValues.add(new HashMap<String, Literal>());
    }

    @Override
    public void apply(AST ast) {
        currentScope = 0;
        traverseThroughTreeAndApplyTransformation(ast.root);
    }

    //Functie om door een ASTBoom te doorlopen en transformatie toe te passen.
    public void traverseThroughTreeAndApplyTransformation(ASTNode node) {
        for (ASTNode nodes : node.getChildren()) {
            if(nodes instanceof Stylerule){
                variableValues.add(new HashMap<String, Literal>());
                ++currentScope;
            }
            //Check of het een variabel assigned.
            if (nodes instanceof VariableAssignment) {
                if (((VariableAssignment) nodes).expression instanceof Operation) { //Indien er een som bij betrokken is, calculeer hem eerst en voeg hem daarna toe.
                    Literal resultOfOperation = calculateOperation((Operation) ((VariableAssignment) nodes).expression);
                    nodes.removeChild(((VariableAssignment) nodes).expression);
                    nodes.addChild(resultOfOperation);
                    addVariableValueToList(((VariableAssignment) nodes).name.name, resultOfOperation);
                } else {
                    addVariableValueToList(((VariableAssignment) nodes).name.name, (Literal) ((VariableAssignment) nodes).expression);
                }
            }
            if(nodes instanceof VariableReference){
                String key = ((VariableReference) nodes).name;
                node.removeChild(nodes);
                if(variableValues.get(currentScope).containsKey(key)){
                    node.addChild(variableValues.get(currentScope).get(key));}
                else{
                    node.addChild(variableValues.get(0).get(key));
                }
            }

            if (nodes instanceof Operation) {
                Literal resultOfOperation = calculateOperation((Operation) nodes);
                node.removeChild(nodes);
                node.addChild(resultOfOperation);
            } else {
                traverseThroughTreeAndApplyTransformation(nodes);
            }
        }
    }

    private Literal calculateOperation(Operation node) {
        replaceChildOperationByLiteral(node);
        replaceChildReferencesByLiteral(node);
        //Als de AST een optelsom bevat, tel de waarden op
        if(node instanceof AddOperation) {
            if (node.lhs instanceof PercentageLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value + ((PercentageLiteral) node.rhs).value;
                PercentageLiteral result = new PercentageLiteral(resultValue);
                return result;
            }
            else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PixelLiteral) node.lhs).value + ((PixelLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            }
            else{
                int resultValue = ((ScalarLiteral)node.lhs).value + ((ScalarLiteral)node.rhs).value;
                ScalarLiteral result = new ScalarLiteral(resultValue);
                return result;
            }
        }
        //Als de AST een aftrek som bevat, voer de som uit.
        if(node instanceof SubtractOperation){
            if (node.lhs instanceof PercentageLiteral) {
                int resultValue = ((PercentageLiteral) node.lhs).value - ((PercentageLiteral) node.rhs).value;
                PercentageLiteral result = new PercentageLiteral(resultValue);
                return result;
            }
            else if (node.lhs instanceof PixelLiteral) {
                int resultValue = ((PixelLiteral) node.lhs).value - ((PixelLiteral) node.rhs).value;
                PixelLiteral result = new PixelLiteral(resultValue);
                return result;
            }
            else{
                int resultValue = ((ScalarLiteral)node.lhs).value - ((ScalarLiteral)node.rhs).value;
                ScalarLiteral result = new ScalarLiteral(resultValue);
                return result;
            }
        }
        //Als het een vermenigvuldig som, vermenigvuldig de waarden.
        if(node instanceof MultiplyOperation){
            int resultValue = ((ScalarLiteral)node.lhs).value * ((ScalarLiteral)node.rhs).value;
            ScalarLiteral result = new ScalarLiteral(resultValue);
            return result;
        }
        return null;
    }

    private void addVariableValueToList(String key, Literal value) {
        variableValues.get(0).put(key, value);
    }

    private void replaceChildOperationByLiteral(Operation node) {
        if (node.lhs instanceof Operation) {
            Literal lit = calculateOperation((Operation) node.lhs);
            node.lhs = null;
            node.addChild(lit);
        }
        if (node.rhs instanceof Operation) {
            Literal lit = calculateOperation((Operation) node.rhs);
            node.rhs = null;
            node.addChild(lit);
        }
    }

    private void replaceChildReferencesByLiteral(Operation node) {
        if (node.lhs instanceof VariableReference) {
            Literal lit;
            if(variableValues.get(currentScope).containsKey(((VariableReference) node.lhs).name)){
                lit = variableValues.get(currentScope).get(((VariableReference) node.lhs).name);
            }
            else {
                lit = variableValues.get(0).get(((VariableReference) node.lhs).name);
            }
            node.lhs = null;
            node.addChild(lit);
        }
        if (node.rhs instanceof VariableReference) {
            Literal lit;
            if(variableValues.get(currentScope).containsKey(((VariableReference) node.rhs).name)){
                lit = variableValues.get(currentScope).get(((VariableReference) node.rhs).name);
            } else {
                lit = variableValues.get(0).get(((VariableReference) node.rhs).name);
            }
            node.rhs = null;
            node.addChild(lit);
        }
    }

}
