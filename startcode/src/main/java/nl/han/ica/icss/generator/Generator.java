package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;

public class Generator {

	public String generate(AST ast) {
		StringBuilder generatedCSS = new StringBuilder();
		for(ASTNode node : ast.root.getChildren()){
			generatedCSS.append(traverseTreeAndGenerateCSS(node));
		}
        return generatedCSS.toString();
	}

	public String traverseTreeAndGenerateCSS(ASTNode node) {
		StringBuilder generatedString = new StringBuilder();
		if (node instanceof Stylerule) {
			for (Selector selector : ((Stylerule) node).selectors) {
				generatedString.append(selector.toString() + " ");
			}
			generatedString.append('{' + System.lineSeparator());
			for (ASTNode declaration : node.getChildren()) {
				if (declaration instanceof Declaration) {
					generatedString.append("  "+((Declaration) declaration).property.name + ": " + ((Declaration) declaration).expression.getValue()+";");
					generatedString.append(System.lineSeparator());
				}
			}
			generatedString.append('}' + System.lineSeparator() + System.lineSeparator());
		}
		return generatedString.toString();
	}
}
