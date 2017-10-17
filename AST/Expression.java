package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}
	TypeName expressionType;

	public TypeName getTypeName() {
		return expressionType;
	}

	public void setTypeName(TypeName type) {
		this.expressionType = type;
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
