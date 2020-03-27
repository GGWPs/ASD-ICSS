package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

public class RemoveIf implements Transform {

    //TR02|Implementeer de `RemoveIf `transformatie. Deze transformatie verwijdert alle `IfClause`s uit de AST.
    //Wanneer de conditie van de `IfClause` `TRUE` is wordt deze vervangen door de body van het if-statement.
    // Als de conditie `FALSE` is dan verwijder je de `IfClause`volledig uit de AST.
    @Override
    public void apply(AST ast) {
        for(ASTNode node : ast.root.getChildren()){
            if(node instanceof Stylerule){
                checkIfStatements(node);
            }
        }
    }

    private void checkIfStatements(ASTNode parent){
        for(ASTNode node : parent.getChildren()){
            if(node instanceof IfClause){ //Check of de ASTNode een IfClause is
                checkIfStatements(node); //Voer de functie recursief uit
                BoolLiteral boolLit = (BoolLiteral)((IfClause) node).conditionalExpression;
                if(boolLit.value) { //Als conditie van IfClause TRUE is.
                    for (ASTNode child : node.getChildren()) {
                        if (child instanceof Declaration) {
                            parent.addChild(child); //Voeg body toe.
                        }
                    }
                }
                parent.removeChild(node); //Als conditie false is, verwijder de hele IfClause node.
            }
        }
    }
}
