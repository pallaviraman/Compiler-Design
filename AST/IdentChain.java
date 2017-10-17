package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentChain extends ChainElem {

	public IdentChain(Token firstToken) {
		super(firstToken);
	}
	Dec declaration;

	public Dec getDec() {
		return declaration;
	}

	public void setDec(Dec dec) {
		this.declaration = dec;
	}

	@Override
	public String toString() {
		return "IdentChain [firstToken=" + firstToken + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentChain(this, arg);
	}

}
