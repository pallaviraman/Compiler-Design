package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Statement extends ASTNode {

	public Statement(Token firstToken) {
		super(firstToken);
	}
	TypeName expressionType;

	public TypeName getTypeName() {
		return expressionType;
	}

	public void setTypeName(TypeName type) {
		this.expressionType = type;
	}
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
