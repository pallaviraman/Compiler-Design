package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program prog =program();
		matchEOF();
		return prog;
	}

	 
	public Expression expression() throws SyntaxException {
		//TODO
		Token first = t;
		Expression e0 = term();
		Kind kind = t.kind;
		while(kind.equals(LT)||kind.equals(LE)||kind.equals(GT)||kind.equals(GE)||kind.equals(EQUAL)||kind.equals(NOTEQUAL))
		{
			Token op = t;
			relOp();
			Expression e1 = term();
			kind = t.kind;
			e0 = new BinaryExpression(first,e0,op,e1);
		}
		return e0;
		//throw new UnimplementedFeatureException();
	}

	
	public Expression term() throws SyntaxException {
		//TODO
		Token first = t;
		Expression e0 = elem();
		Kind kind = t.kind;
		while(kind.equals(PLUS)||kind.equals(MINUS)||kind.equals(OR))
		{
			Token op = t;
			weakOp();
			Expression e1 = elem();
			kind = t.kind;
			e0 = new BinaryExpression(first,e0,op,e1);
		}
		return e0;
		//throw new UnimplementedFeatureException();
	}

	public Expression elem() throws SyntaxException {
		//TODO
		Token first = t;
		Expression e0 = factor();
		Kind kind = t.kind;
		while(kind.equals(TIMES)||kind.equals(DIV)||kind.equals(AND)||kind.equals(MOD))
		{
			Token op = t;
			strongOp();
			Expression e1 = factor();
			kind = t.kind;
			e0 = new BinaryExpression(first,e0,op,e1);
		}
		return e0;
		//throw new UnimplementedFeatureException();
	}
	

	public Expression factor() throws SyntaxException {
		Token first = t;
		Kind kind = t.kind;
		Expression e0 = null;
		switch (kind) {
		case IDENT: {
			e0 = new IdentExpression(first);
			consume();
		}
			break;
		case INT_LIT: {
			e0 = new IntLitExpression(first);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e0 = new BooleanLitExpression(first);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e0 = new ConstantExpression(first);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e0 = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e0;
	}

	public Block block() throws SyntaxException {
		//TODO
		Token first = t;
		Kind kind = t.kind;
		Block b;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		if(kind.equals(LBRACE))
		{
			consume();
			kind = t.kind;
			while(kind.equals(KW_INTEGER)||kind.equals(KW_BOOLEAN)||kind.equals(KW_IMAGE)||kind.equals(KW_FRAME)||kind.equals(OP_SLEEP)||kind.equals(KW_WHILE)||kind.equals(KW_IF)||kind.equals(IDENT)||kind.equals(OP_BLUR)||kind.equals(OP_GRAY)||kind.equals(OP_CONVOLVE)||kind.equals(KW_SHOW)||kind.equals(KW_HIDE)||kind.equals(KW_MOVE)||kind.equals(KW_XLOC)||kind.equals(KW_YLOC)||kind.equals(OP_WIDTH)||kind.equals(OP_HEIGHT)||kind.equals(KW_SCALE))
			{
				if(kind.equals(OP_SLEEP)||kind.equals(KW_WHILE)||kind.equals(KW_IF)||kind.equals(IDENT)||kind.equals(OP_BLUR)||kind.equals(OP_GRAY)||kind.equals(OP_CONVOLVE)||kind.equals(KW_SHOW)||kind.equals(KW_HIDE)||kind.equals(KW_MOVE)||kind.equals(KW_XLOC)||kind.equals(KW_YLOC)||kind.equals(OP_WIDTH)||kind.equals(OP_HEIGHT)||kind.equals(KW_SCALE))
				{
					Statement s =statement();
					statements.add(s);
					kind = t.kind;
				}
				else
				{
					Dec d = dec();
					decs.add(d);
					kind = t.kind;
				}
			}
			match(RBRACE);
		}
		else
		{
			throw new SyntaxException("No braces in a block");
		}
		b = new Block(first,decs,statements);
		return b;
		//throw new UnimplementedFeatureException();
	}

	public Program program() throws SyntaxException {
		//TODO
		Token first = t;
		Kind kind = t.kind;
		Block b = null;
		Program prog = null;
		ArrayList<ParamDec> params = new ArrayList<ParamDec>();
		if(kind.equals(IDENT))
		{
			if(scanner.peek().kind.equals(KW_URL)||scanner.peek().kind.equals(KW_FILE)||scanner.peek().kind.equals(KW_INTEGER)||scanner.peek().kind.equals(KW_BOOLEAN))
			{
				consume();
				ParamDec param = paramDec();
				params.add(param);
				kind = t.kind;
				while(kind.equals(COMMA))
				{
					consume();
					param = paramDec();
					params.add(param);
					kind = t.kind;
				}
				b = block();
			}
			else
			{
				consume();
				b = block();
			}
		}
		else
		{
			throw new SyntaxException("program is empty");
		}
		prog = new Program(first,params,b);
		return prog;
		//throw new UnimplementedFeatureException();
	}
	
	public ParamDec paramDec() throws SyntaxException {
		//TODO
		Token first = t;
		Kind kind = t.kind;
		ParamDec param = null;
		switch(kind)
		{
		case KW_URL:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				param = new ParamDec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_FILE:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				param = new ParamDec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_INTEGER:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				param = new ParamDec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_BOOLEAN:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				param = new ParamDec(first,t);
			}
			match(IDENT);
		}
		break;
		default:
		{
			throw new SyntaxException("Illegal dec");
		}
		}
		return param;
		//throw new UnimplementedFeatureException();
	}

	
	public Dec dec() throws SyntaxException {
		//TODO
		Kind kind = t.kind;
		Token first = t;
		Dec d = null;
		switch(kind)
		{
		case KW_INTEGER:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				d = new Dec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_BOOLEAN:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				d = new Dec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_IMAGE:
		{
			consume();
			if(t.kind.equals(IDENT))
			{
				d = new Dec(first,t);
			}
			match(IDENT);
		}
		break;
		case KW_FRAME:
		{
			consume();
			{
				if(t.kind.equals(IDENT))
				{
					d = new Dec(first,t);
				}
			}
			match(IDENT);
		}
		break;
		default:
		{
			throw new SyntaxException("Illegal dec");
		}
		}
		return d;
		//throw new UnimplementedFeatureException();
	}

	public Statement statement() throws SyntaxException {
		//TODO
		Token first = t;
		Statement s = null;
		Kind kind = t.kind;
		if(kind.equals(OP_SLEEP))
		{
			consume();
			Expression e = expression();
			s = new SleepStatement(first,e);
			match(SEMI);
		}
		else if(kind.equals(KW_WHILE))
		{
			s = whileStatement();
		}
		else if(kind.equals(KW_IF))
		{
			s = ifStatement();
		}
		else if(kind.equals(IDENT) && scanner.peek().kind.equals(ASSIGN) )
		{
			s = assign();
			match(SEMI);
		}
		else
		{
			s = chain();
			match(SEMI);
		}
		return s;
		//throw new UnimplementedFeatureException();
	}

	public Chain chain() throws SyntaxException {
		//TODO
		Token first =t;
		Chain c0 = chainElem();
		Token op = null;
		if(t.kind.equals(ARROW)|| t.kind.equals(BARARROW))
		{
			op = t;
		}
		arrowOp();
		ChainElem c1 = chainElem();
		c0 = new BinaryChain(first,c0,op,c1);
		Kind kind = t.kind;
		while(kind.equals(ARROW)||kind.equals(BARARROW))
		{
			op = t;
			arrowOp();
			c1 = chainElem();
			c0 = new BinaryChain(first,c0,op,c1);
			kind = t.kind;
		}
		return c0;
		//throw new UnimplementedFeatureException();
	}


	public ChainElem chainElem() throws SyntaxException {
		//TODO
		Token first = t;
		ChainElem ch = null;
		Tuple tup = null;
		Kind kind = t.kind;
		if(kind.equals(IDENT))
		{
			ch = new IdentChain(first);
			consume();
		}
		else if(kind.equals(OP_BLUR)||kind.equals(OP_GRAY)||kind.equals(OP_CONVOLVE))
		{
			filterOp();
			tup = arg();
			ch = new FilterOpChain(first,tup);
		}
		else if(kind.equals(KW_SHOW)||kind.equals(KW_HIDE)||kind.equals(KW_MOVE)||kind.equals(KW_XLOC)||kind.equals(KW_YLOC))
		{
			frameOp();
			tup = arg();
			ch = new FrameOpChain(first,tup);
		}
		else if(kind.equals(OP_WIDTH)||kind.equals(OP_HEIGHT)||kind.equals(KW_SCALE))
		{
			imageOp();
			tup = arg();
			ch = new ImageOpChain(first,tup);
		}
		else
		{
			throw new SyntaxException("No chain element");
		}
		//throw new UnimplementedFeatureException();
		return ch;
	}


	public Tuple arg() throws SyntaxException {
		//TODO
		Tuple tup = null;
		Token first = t;
		List<Expression> exprlist = new ArrayList<Expression>();
		Kind kind = t.kind;
		if(kind.equals(LPAREN))
		{
			consume();
			Expression e =expression();
			exprlist.add(e);
			kind = t.kind;
			while(kind.equals(COMMA))
			{
				consume();
				e = expression();
				exprlist.add(e);
				kind = t.kind;
			}
			match(RPAREN);
			tup = new Tuple(first,exprlist);
		}
		else
		{
			tup = new Tuple(t,exprlist);
		}
		return tup;
		//throw new UnimplementedFeatureException();
	}
	
	
	public Statement whileStatement() throws SyntaxException{
		Token first = t;
		Kind kind = t.kind;
		Statement s = null;
		if(kind.equals(KW_WHILE))
		{
			consume();
			kind = t.kind;
			if(kind.equals(LPAREN))
			{
				consume();
				Expression e = expression();
				match(RPAREN);
				Block b = block();
				s = new WhileStatement(first,e,b);
			}
			else
			{
				throw new SyntaxException("no condition in if statement"); 
			}
		}
		return s;
	}
	
	public Statement ifStatement() throws SyntaxException{
		Token first = t;
		Kind kind = t.kind;
		Statement s =null;
		if(kind.equals(KW_IF))
		{
			consume();
			kind = t.kind;
			if(kind.equals(LPAREN))
			{
				consume();
				Expression e =expression();
				match(RPAREN);
				Block b = block();
				s = new IfStatement(first,e,b);
			}
			else
			{
				throw new SyntaxException("no condition in if statement"); 
			}
		}
		return s;
	}
	

	public Statement assign() throws SyntaxException {
		Token first = t;
		Kind kind = t.kind;
		Statement s = null;
		if(kind.equals(IDENT))
		{
			IdentLValue i = new IdentLValue(first);
			consume();
			kind = t.kind;
			if(kind.equals(ASSIGN))
			{
				consume();
				Expression e = expression();
				s = new AssignmentStatement(first,i,e);
			}
			else
			{
				throw new SyntaxException("No assign symbol");
			}
		}
		else
		{
			throw new SyntaxException("assign statement empty");
		}
		return s;
	}
	
	void strongOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case TIMES: {
			consume();
		}
			break;
		case DIV: {
			consume();
		}
			break;
		case AND: {
			consume();
		}
			break;
		case MOD: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not a strong operator");
		}
	}
	
	void weakOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case PLUS: {
			consume();
		}
			break;
		case MINUS: {
			consume();
		}
			break;
		case OR: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not a weak operator");
		}
	}
	
	void relOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case LT: {
			consume();
		}
			break;
		case LE: {
			consume();
		}
			break;
		case GT: {
			consume();
		}
			break;
		case GE: {
			consume();
		}
			break;
		case EQUAL: {
			consume();
		}
			break;
		case NOTEQUAL: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not a rel operator");
		}
	}
	
	void imageOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case OP_WIDTH: {
			consume();
		}
			break;
		case OP_HEIGHT: {
			consume();
		}
			break;
		case KW_SCALE:	{
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not an image operator");
		}
	}
	
	void frameOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case KW_SHOW: {
			consume();
		}
			break;
		case KW_HIDE: {
			consume();
		}
			break;
		case KW_MOVE: {
			consume();
		}
			break;
		case KW_XLOC: {
			consume();
		}
			break;
		case KW_YLOC: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not a frame operator");
		}
	}
	
	void filterOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case OP_BLUR: {
			consume();
		}
			break;
		case OP_GRAY: {
			consume();
		}
			break;
		case OP_CONVOLVE: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not a filter operator");
		}
	}
	
	
	void arrowOp() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case ARROW: {
			consume();
		}
			break;
		case BARARROW: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("not an arrow operator");
		}
	}

	
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF but got" + t.kind);
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind.equals(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
