package nl.han.ica.icss.parser;

import java.util.Stack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private Stack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new Stack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(ast.root);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterVariableassignment(ICSSParser.VariableassignmentContext ctx) {
		ASTNode assignment = new VariableAssignment();
		currentContainer.peek().addChild(assignment);
		currentContainer.push(assignment);
	}

	@Override
	public void exitVariableassignment(ICSSParser.VariableassignmentContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = new Stylerule();
		currentContainer.peek().addChild(styleRule);
		currentContainer.push(styleRule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterSelectorId(ICSSParser.SelectorIdContext ctx) {
		Selector idSelector = new IdSelector(ctx.getText());
		currentContainer.peek().addChild(idSelector);
	}

	@Override
	public void enterSelectorClass(ICSSParser.SelectorClassContext ctx) {
		Selector idSelector = new ClassSelector(ctx.getText());
		currentContainer.peek().addChild(idSelector);
	}

	@Override
	public void enterSelectorTag(ICSSParser.SelectorTagContext ctx) {
		Selector idSelector = new TagSelector(ctx.getText());
		currentContainer.peek().addChild(idSelector);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = new Declaration();
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		ASTNode propertyName = new PropertyName(ctx.getText());
		currentContainer.peek().addChild(propertyName);
	}

	@Override
	public void enterBoolliteral(ICSSParser.BoolliteralContext ctx) {
		Expression exp;
		if (ctx.FALSE() != null) {
			exp = new BoolLiteral(false);
		} else {
			exp = new BoolLiteral(true);
		}
		currentContainer.peek().addChild(exp);
	}

	@Override
	public void enterColorliteral(ICSSParser.ColorliteralContext ctx) {
		Expression exp = new ColorLiteral(ctx.getText());
		currentContainer.peek().addChild(exp);
	}

	@Override
	public void enterPercentageliteral(ICSSParser.PercentageliteralContext ctx) {
		Expression exp = new PercentageLiteral(ctx.getText());
		currentContainer.peek().addChild(exp);
	}

	@Override
	public void enterPixelliteral(ICSSParser.PixelliteralContext ctx) {
		Expression exp = new PixelLiteral(ctx.getText());
		currentContainer.peek().addChild(exp);
	}

	@Override
	public void enterScalarliteral(ICSSParser.ScalarliteralContext ctx) {
		Expression exp = new ScalarLiteral(ctx.getText());
		currentContainer.peek().addChild(exp);
	}

	@Override
	public void enterAddOperation(ICSSParser.AddOperationContext ctx) {
		Operation operation = new AddOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}

	@Override
	public void enterSubstractOperation(ICSSParser.SubstractOperationContext ctx) {
		Operation operation = new SubtractOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}

	@Override
	public void enterMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		Operation operation = new MultiplyOperation();
		currentContainer.peek().addChild(operation);
		currentContainer.push(operation);
	}

	@Override
	public void exitSubstractOperation(ICSSParser.SubstractOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

	@Override
	public void exitMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

	@Override
	public void exitAddOperation(ICSSParser.AddOperationContext ctx) {
		Operation o = (Operation)currentContainer.pop();
	}

	@Override
	public void enterVariablereference(ICSSParser.VariablereferenceContext ctx) {
		ASTNode reference = new VariableReference(ctx.getText());
		currentContainer.peek().addChild(reference);
	}

	@Override
	public void enterIf_statement(ICSSParser.If_statementContext ctx) {
		ASTNode ifStatement = new IfClause();
		currentContainer.peek().addChild(ifStatement);
		currentContainer.push(ifStatement);
	}

	@Override
	public void exitIf_statement(ICSSParser.If_statementContext ctx) {
		currentContainer.pop();
	}

}
