package cop5556sp17;
import java.util.List;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this,arg);
		binaryChain.getE1().visit(this,arg);
		if(binaryChain.getArrow().kind == ARROW)
		{
			if(binaryChain.getE0().getTypeName() == URL && binaryChain.getE1().getTypeName() == IMAGE)
			{
				binaryChain.setTypeName(IMAGE);
			}
			else if(binaryChain.getE0().getTypeName() == FILE && binaryChain.getE1().getTypeName() == IMAGE)
			{
				binaryChain.setTypeName(IMAGE);
			}
			else if((binaryChain.getE0().getTypeName() == FRAME && (binaryChain.getE1().getClass().equals(FrameOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == KW_XLOC || binaryChain.getE1().getFirstToken().kind == KW_YLOC)))
			{
				binaryChain.setTypeName(INTEGER);
			}
			else if((binaryChain.getE0().getTypeName() == FRAME && (binaryChain.getE1().getClass().equals(FrameOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == KW_SHOW || binaryChain.getE1().getFirstToken().kind == KW_HIDE || binaryChain.getE1().getFirstToken().kind == KW_MOVE)))
			{
				binaryChain.setTypeName(FRAME);
			}
			else if((binaryChain.getE0().getTypeName() == IMAGE && (binaryChain.getE1().getClass().equals(ImageOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == OP_WIDTH || binaryChain.getE1().getFirstToken().kind == OP_HEIGHT)))
			{
				binaryChain.setTypeName(INTEGER);
			}
			else if(binaryChain.getE0().getTypeName() == IMAGE && binaryChain.getE1().getTypeName() == FRAME)
			{
				binaryChain.setTypeName(FRAME);
			}
			else if(binaryChain.getE0().getTypeName() == IMAGE && binaryChain.getE1().getTypeName() == FILE)
			{
				binaryChain.setTypeName(NONE);
			}
			else if((binaryChain.getE0().getTypeName() == IMAGE && (binaryChain.getE1().getClass().equals(FilterOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == OP_GRAY || binaryChain.getE1().getFirstToken().kind == OP_BLUR || binaryChain.getE1().getFirstToken().kind == OP_CONVOLVE)))
			{
				binaryChain.setTypeName(IMAGE);
			}
			else if((binaryChain.getE0().getTypeName() == IMAGE && (binaryChain.getE1().getClass().equals(ImageOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == KW_SCALE)))
			{
				binaryChain.setTypeName(IMAGE);
			}
			else if(binaryChain.getE0().getTypeName() == IMAGE && (binaryChain.getE1().getClass().equals(IdentChain.class) && binaryChain.getE1().getTypeName() == IMAGE))
			{
				binaryChain.setTypeName(IMAGE);
			}
			else if(binaryChain.getE0().getTypeName() == INTEGER && (binaryChain.getE1().getClass().equals(IdentChain.class) && binaryChain.getE1().getTypeName() == INTEGER))
			{
				binaryChain.setTypeName(INTEGER);
			}
			else
			{

				throw new TypeCheckException("binaryChain error");
			}
		}
		else if(binaryChain.getArrow().kind == BARARROW)
		{
			if((binaryChain.getE0().getTypeName() == IMAGE && (binaryChain.getE1().getClass().equals(FilterOpChain.class)) && (binaryChain.getE1().getFirstToken().kind == OP_GRAY || binaryChain.getE1().getFirstToken().kind == OP_BLUR || binaryChain.getE1().getFirstToken().kind == OP_CONVOLVE)))
			{
				binaryChain.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("binaryChain error");
			}
		}
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryExpression.getE0().visit(this,null);
		binaryExpression.getE1().visit(this,null);
		switch(binaryExpression.getOp().kind)
		{
		case PLUS:
		case MINUS:
		{
			if(binaryExpression.getE0().getTypeName() == INTEGER && binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(INTEGER);
			}
			else if(binaryExpression.getE0().getTypeName() == IMAGE &&binaryExpression.getE1().getTypeName() == IMAGE)
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		}
		case TIMES:
			if(binaryExpression.getE0().getTypeName() == INTEGER &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(INTEGER);
			}
			else if(binaryExpression.getE0().getTypeName() == INTEGER &&binaryExpression.getE1().getTypeName() == IMAGE)
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else if(binaryExpression.getE0().getTypeName() == IMAGE &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		case DIV:
		{
			if(binaryExpression.getE0().getTypeName() == INTEGER &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(INTEGER);
			}
			else if(binaryExpression.getE0().getTypeName() == IMAGE &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		}
		case MOD:
		{
			if(binaryExpression.getE0().getTypeName() == INTEGER &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(INTEGER);
			}
			else if(binaryExpression.getE0().getTypeName() == IMAGE &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		}
		case LT:
		case GT:
		case LE:
		case GE:
		{
			if(binaryExpression.getE0().getTypeName() == INTEGER &&binaryExpression.getE1().getTypeName() == INTEGER)
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else if(binaryExpression.getE0().getTypeName() == BOOLEAN &&binaryExpression.getE1().getTypeName() == BOOLEAN)
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		}
		case EQUAL:
		case NOTEQUAL:
		{
			if(binaryExpression.getE0().getTypeName() == binaryExpression.getE1().getTypeName())
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error");
			}
			break;
		}
		case OR:
		case AND:
		{
			if(binaryExpression.getE0().getTypeName() == BOOLEAN &&binaryExpression.getE1().getTypeName() == BOOLEAN)
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else
			{
				throw new TypeCheckException("binaryExpression error"); 
			}
			break;
		}
		default:
		{
			throw new TypeCheckException("binaryExpression error");
		}
		}
		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		for(Dec dec :block.getDecs())
		{
			dec.visit(this, arg);
		}
		for(Statement statement : block.getStatements())
		{
			statement.visit(this, arg);
		}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, arg);
		if(filterOpChain.getArg().getExprList().size() == 0)
		{
			filterOpChain.setTypeName(IMAGE);
		}else
		{
			throw new TypeCheckException("filterOpChain error");
		}
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		frameOpChain.getArg().visit(this, null);
		if(frameOpChain.getFirstToken().kind == KW_SHOW ||frameOpChain.getFirstToken().kind == KW_HIDE)
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.setTypeName(NONE);
			}else
			{
				throw new TypeCheckException("frameOpChain error");
			}
		}
		else if(frameOpChain.getFirstToken().kind == KW_XLOC ||frameOpChain.getFirstToken().kind == KW_YLOC)
		{
			if(frameOpChain.getArg().getExprList().size() == 0)
			{
				frameOpChain.setTypeName(INTEGER);
			}else
			{
				throw new TypeCheckException("frameOpChain error");
			}
		}
		else if(frameOpChain.getFirstToken().kind == KW_MOVE)
		{
			if(frameOpChain.getArg().getExprList().size() == 2)
			{
				frameOpChain.setTypeName(NONE);
			}else
			{
				throw new TypeCheckException("frameOpChain error");
			}
		}
		else
		{
			throw new TypeCheckException("Bug in parser");
		}
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident = identChain.getFirstToken().getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null)
		{
			throw new TypeCheckException("ident undeclared");
		}
		TypeName type = symtab.lookup(ident).getTypeName();
		identChain.setTypeName(type);
		identChain.setDec(dec);
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident = identExpression.getFirstToken().getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null)
		{
			throw new TypeCheckException("ident undeclared");
		}
		identExpression.setTypeName(dec.getTypeName());
		identExpression.setDec(dec);
		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, null);
		if(ifStatement.getE().getTypeName() != BOOLEAN)
		{
			throw new TypeCheckException("If statement error");
		}
		ifStatement.getB().visit(this,null);

		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, null);
		if(sleepStatement.getE().getTypeName() != INTEGER)
		{
			throw new TypeCheckException("Sleep statement error");
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, null);
		if(whileStatement.getE().getTypeName() != BOOLEAN)
		{
			throw new TypeCheckException("While statement error");
		}
		whileStatement.getB().visit(this,null);
		return whileStatement;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(declaration.getFirstToken().kind == KW_INTEGER)
		{
			declaration.setTypeName(INTEGER);
		}
		else if(declaration.getFirstToken().kind == KW_IMAGE)
		{
			declaration.setTypeName(IMAGE);
		}
		else if(declaration.getFirstToken().kind == KW_FRAME)
		{
			declaration.setTypeName(FRAME);
		}
		else if(declaration.getFirstToken().kind == KW_FILE)
		{
			declaration.setTypeName(FILE);
		}
		else if(declaration.getFirstToken().kind == KW_BOOLEAN)
		{
			declaration.setTypeName(BOOLEAN);
		}
		else if(declaration.getFirstToken().kind == KW_URL)
		{
			declaration.setTypeName(URL);
		}
		else
		{
			declaration.setTypeName(NONE);
		}
		if(!(symtab.insert(declaration.getIdent().getText(), declaration)))
		{
			throw new TypeCheckException("clash of idents");
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(ParamDec param : program.getParams())
		{
			param.visit(this,null);
		}
		program.getB().visit(this,null);
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		assignStatement.getVar().visit(this, null);
		assignStatement.getE().visit(this, null);
		if(!(assignStatement.getVar().getDec().getTypeName() == assignStatement.getE().getTypeName()))
		{
			throw new TypeCheckException("Assignment statement error");
		}
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident = identX.getFirstToken().getText();
		Dec dec = symtab.lookup(ident);
		if(dec == null)
		{
			throw new TypeCheckException("identlvalue undeclared");
		}
		identX.setDec(dec);
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(paramDec.getFirstToken().kind == KW_INTEGER)
		{
			paramDec.setTypeName(INTEGER);
		}
		else if(paramDec.getFirstToken().kind == KW_IMAGE)
		{
			paramDec.setTypeName(IMAGE);
		}
		else if(paramDec.getFirstToken().kind == KW_FRAME)
		{
			paramDec.setTypeName(FRAME);
		}
		else if(paramDec.getFirstToken().kind == KW_FILE)
		{
			paramDec.setTypeName(FILE);
		}
		else if(paramDec.getFirstToken().kind == KW_BOOLEAN)
		{
			paramDec.setTypeName(BOOLEAN);
		}
		else if(paramDec.getFirstToken().kind == KW_URL)
		{
			paramDec.setTypeName(URL);
		}
		else
		{
			paramDec.setTypeName(NONE);
		}
		if(!(symtab.insert(paramDec.getIdent().getText(),paramDec)))
		{
			throw new TypeCheckException("clash of idents");
		}
		return paramDec;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, null);
		if(imageOpChain.getFirstToken().kind == OP_WIDTH ||imageOpChain.getFirstToken().kind == OP_HEIGHT)
		{
			if(imageOpChain.getArg().getExprList().size() == 0)
			{
				imageOpChain.setTypeName(INTEGER);
			}else
			{
				throw new TypeCheckException("imageOpChain error");
			}
		}
		else if(imageOpChain.getFirstToken().kind == (KW_SCALE) )
		{
			if(imageOpChain.getArg().getExprList().size() == 1)
			{
				imageOpChain.setTypeName(IMAGE);
			}else
			{
				throw new TypeCheckException("frameOpChain error");
			}
		}
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(Expression expr : tuple.getExprList())
		{
			expr.visit(this,null);
		}
		for(Expression expr : tuple.getExprList())
		{
			if((expr.getTypeName() != INTEGER))
			{
				throw new TypeCheckException("Tuple error");
			}
		}
		return tuple;
	}


}
