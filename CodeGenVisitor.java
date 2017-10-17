package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int arrayindex=0;
	int slotnumber=0;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		Block blocks = program.getB();
		ArrayList<Dec> decs = blocks.getDecs();
		for(Dec dec : decs)
		{
			String fieldname = dec.getIdent().getText();
			if(dec.getTypeName() == TypeName.INTEGER || dec.getTypeName() == TypeName.BOOLEAN)
			{
				mv.visitLocalVariable(fieldname,dec.getTypeName().getJVMTypeDesc(),null,startRun,endRun,dec.getSlotNumber());
			}
			else
			{
				mv.visitLocalVariable(fieldname,dec.getTypeName().getJVMTypeDesc(),null,startRun,endRun,dec.getSlotNumber());
			}
		}
		
		
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		
		if(assignStatement.getVar().getTypeName() == IMAGE && assignStatement.getE().getTypeName() == IMAGE)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);
		}
			
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain c0 = binaryChain.getE0();
		ChainElem c1 = binaryChain.getE1();
		c0.visit(this, "onLeft");

		
		if(c0.getTypeName() == TypeName.URL)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
		}
		else if(c0.getTypeName() == TypeName.FILE)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
		}
		else if(c0.getTypeName() == TypeName.NONE)
		{
			mv.visitInsn(POP);
		}
		if(c1 instanceof FilterOpChain)
		{
			if(binaryChain.getArrow().kind == ARROW)
			{
				mv.visitInsn(ACONST_NULL);
			}
			else if(binaryChain.getArrow().kind == BARARROW)
			{
				mv.visitInsn(DUP);
			}
		}
		c1.visit(this, "onRight");
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		binaryExpression.getE0().visit(this,arg);
		binaryExpression.getE1().visit(this, arg);
		Token op = binaryExpression.getOp();
		Kind kind = op.kind;
		switch(kind)
		{
		case PLUS:
		{
			if(binaryExpression.getE0().getTypeName() == IMAGE && binaryExpression.getE1().getTypeName() == IMAGE)
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", PLPRuntimeImageOps.addSig, false);
			}
			else
			{
				mv.visitInsn(IADD);
			}
			break;
		}
		case MINUS:
		{
			if(binaryExpression.getE0().getTypeName() == IMAGE && binaryExpression.getE1().getTypeName() == IMAGE)
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", PLPRuntimeImageOps.subSig, false);
			}
			else
			{
				mv.visitInsn(ISUB);
			}
			break;
		}
		case OR:
			mv.visitInsn(IOR);
			break;
		case TIMES:
		{
			if(binaryExpression.getE0().getTypeName() == IMAGE && binaryExpression.getE1().getTypeName() == TypeName.INTEGER)
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig, false);
			}
			else if(binaryExpression.getE0().getTypeName() == TypeName.INTEGER && binaryExpression.getE1().getTypeName() == IMAGE )
			{
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig, false);
			}
			else
			{
				mv.visitInsn(IMUL);
			}
			break;
		}
		case DIV:
		{
		if(binaryExpression.getE0().getTypeName() == IMAGE && binaryExpression.getE1().getTypeName() == TypeName.INTEGER)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", PLPRuntimeImageOps.divSig, false);
		}
		else
		{
			mv.visitInsn(IDIV);
		}
			break;
		}
		case AND:
		{
			mv.visitInsn(IAND);
			break;
		}
		case MOD:
		{
		if(binaryExpression.getE0().getTypeName() == IMAGE && binaryExpression.getE1().getTypeName() == TypeName.INTEGER)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", PLPRuntimeImageOps.modSig, false);
		}
		else
		{
			mv.visitInsn(IREM);
		}
			break;
		}
		case LE:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPLE, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
		case GE:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPGE, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
		case LT:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPLT, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
		case GT:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPGT, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
		case EQUAL:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
		case NOTEQUAL:
		{
			Label truelabel = new Label();
			Label falselabel = new Label();
			mv.visitJumpInsn(IF_ICMPNE, truelabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, falselabel);
			mv.visitLabel(truelabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(falselabel);
			break;
		}
			
	}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label enterscope = new Label();
		Label leavescope = new Label();
		mv.visitLabel(enterscope);
		for(Dec dec : block.getDecs())
		{
			dec.visit(this, arg);
		}
		for(Statement statement : block.getStatements())
		{
			statement.visit(this,arg);
			if(statement.getClass().equals(BinaryChain.class))
			{
				mv.visitInsn(POP);
			}
		}
		mv.visitLabel(leavescope);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if (booleanLitExpression.getValue()) {
			mv.visitLdcInsn(1);
		} else {
			mv.visitLdcInsn(0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if(constantExpression.getFirstToken().kind  == KW_SCREENWIDTH )
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		else
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlotNumber(++slotnumber);
		if(declaration.getTypeName() == TypeName.INTEGER || declaration.getTypeName() == TypeName.BOOLEAN)
		{
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE,declaration.getSlotNumber());
		}
		else if(declaration.getTypeName() == TypeName.IMAGE || declaration.getTypeName() == TypeName.FRAME)
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE,declaration.getSlotNumber());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		filterOpChain.getArg().visit(this,arg);
		if(filterOpChain.getFirstToken().kind == OP_GRAY)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().kind == OP_CONVOLVE)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().kind == OP_BLUR)
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.getFirstToken().kind == KW_SHOW)
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if(frameOpChain.getFirstToken().kind == KW_HIDE)
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else if(frameOpChain.getFirstToken().kind == KW_MOVE)
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		else if(frameOpChain.getFirstToken().kind == KW_XLOC)
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else if(frameOpChain.getFirstToken().kind == KW_YLOC)
		{
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec =  identChain.getDec();
		if(arg.equals("onLeft"))
		{
			if(dec instanceof ParamDec)
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText() , identChain.getDec().getTypeName().getJVMTypeDesc());
			}
			else
			{
				if(identChain.getDec().getTypeName() == TypeName.INTEGER || identChain.getDec().getTypeName() == TypeName.BOOLEAN)
				{
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNumber());
				}
				else
				{
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
				}
			}
		}
		else
		{
			if(identChain.getDec() instanceof ParamDec)
			{
				if(identChain.getDec().getTypeName() == TypeName.INTEGER || identChain.getDec().getTypeName() == TypeName.BOOLEAN)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText() , identChain.getDec().getTypeName().getJVMTypeDesc());
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText() , identChain.getDec().getTypeName().getJVMTypeDesc());
				}
				else if(identChain.getDec().getTypeName() == TypeName.FILE)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className,  identChain.getFirstToken().getText(), "Ljava/io/File;");
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", PLPRuntimeImageIO.writeImageDesc, false);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className,  identChain.getFirstToken().getText(), "Ljava/io/File;");
				}
			}
			else
			{
				if(identChain.getDec().getTypeName() == TypeName.INTEGER || identChain.getDec().getTypeName() == TypeName.BOOLEAN)
				{
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNumber());
				}
				else if(identChain.getDec().getTypeName() == TypeName.FRAME)
				{
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					
				}
				else if(identChain.getDec().getTypeName() == TypeName.IMAGE)
				{
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		if(identExpression.getDec() instanceof ParamDec)
		{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText() , identExpression.getDec().getTypeName().getJVMTypeDesc());
		}
		else
		{
			if(identExpression.getDec().getTypeName() == TypeName.INTEGER || identExpression.getDec().getTypeName() == TypeName.BOOLEAN)
			{
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNumber());
			}
			else 
			{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlotNumber());
			}
			
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		if(identX.getDec() instanceof ParamDec)
		{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText() , identX.getDec().getTypeName().getJVMTypeDesc());
		}
		else
		{
			if(identX.getDec().getTypeName() == TypeName.INTEGER || identX.getDec().getTypeName() == TypeName.BOOLEAN)
			{
				mv.visitVarInsn(ISTORE, identX.getDec().getSlotNumber());
			}
			else if(identX.getDec().getTypeName() == IMAGE)
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlotNumber());
			}
			else
			{
				mv.visitVarInsn(ASTORE, identX.getDec().getSlotNumber());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Label truelabel = new Label();
		Label falselabel = new Label();
		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, falselabel);
		mv.visitLabel(truelabel);
		ifStatement.getB().visit(this,arg);
		mv.visitLabel(falselabel);
		ArrayList<Dec> decs = ifStatement.getB().getDecs();
		for(Dec dec : decs)
		{
			String fieldname = dec.getIdent().getText();
			if(dec.getTypeName() == TypeName.INTEGER )
			{
				mv.visitLocalVariable(fieldname,"I",null,truelabel,falselabel,dec.getSlotNumber());
			}
			else if(dec.getTypeName() == TypeName.BOOLEAN)
			{
				mv.visitLocalVariable(fieldname,"Z",null,truelabel,falselabel,dec.getSlotNumber());
			}
		}

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
	imageOpChain.getArg().visit(this, arg);
	if(imageOpChain.getFirstToken().kind == KW_SCALE)
	{
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale", PLPRuntimeImageOps.scaleSig, false);
	}
	else if(imageOpChain.getFirstToken().kind == OP_WIDTH)
	{
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
	}
	else if(imageOpChain.getFirstToken().kind == OP_HEIGHT)
	{
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
	}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		if(paramDec.getTypeName() == TypeName.INTEGER)
		{
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH,arrayindex++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getTypeName() == TypeName.BOOLEAN)
		{
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false));
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH,arrayindex++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if(paramDec.getTypeName() == TypeName.URL)
		{
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH,arrayindex++);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", "([Ljava/lang/String;I)Ljava/net/URL;", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		else if(paramDec.getTypeName() == TypeName.FILE)
		{
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH,arrayindex++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep","(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {	
		for(Expression expr : tuple.getExprList())
		{
			expr.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label truelabel = new Label();
		Label falselabel = new Label();
		mv.visitLabel(truelabel);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, falselabel);
		whileStatement.getB().visit(this,arg);
		mv.visitJumpInsn(GOTO, truelabel);
		mv.visitLabel(falselabel);
		ArrayList<Dec> decs = whileStatement.getB().getDecs();
		for(Dec dec : decs)
		{
			String fieldname = dec.getIdent().getText();
			mv.visitLocalVariable(fieldname,dec.getTypeName().getJVMTypeDesc(),null,truelabel,falselabel,dec.getSlotNumber());
		}
		return null;
	}

}
