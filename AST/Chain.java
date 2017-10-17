package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	TypeName expressionType;

	public TypeName getTypeName() {
		return expressionType;
	}

	public void setTypeName(TypeName type) {
		this.expressionType = type;
	}

}
