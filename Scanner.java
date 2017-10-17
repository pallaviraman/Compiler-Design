package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State {
		START,
		IN_IDENT,
		IN_DIGIT,
		AFTER_EQ,
		AFTER_NOT,
		AFTER_LESS,
		AFTER_GREAT,
		AFTER_OR,
		AFTER_MINUS,
		AFTER_SLASH,
		IN_COMMENT,
		END_COMMENT
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			if(this.kind == Kind.IDENT)
			{
				return chars.substring(pos, pos+length);
			}
			else if(this.kind == Kind.INT_LIT)
			{
				return chars.substring(pos,pos+length);
			}
			else
			{
				return this.kind.getText();
			}
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			int index =0;
			if(linenos.isEmpty())
			{
				return (new LinePos(0,pos));
			}
			else
			{
				LinePos lpos;
				index = Collections.binarySearch(linenos,pos);
				if(index == 0)
				{
					lpos = new LinePos(0,pos);
				}
				else
				{
					lpos = new LinePos(Math.abs(index+2),pos-linenos.get(Math.abs(index+2))-1);
				}
				return lpos;
			}
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			String str = chars.substring(pos,pos+length);
			int val = Integer.parseInt(str);
			return val;
		}
		
		public boolean isKind(Kind kind)
		{
			for(Kind itr : Kind.values())
			{
				if(itr.equals(kind))
				{
					return true;
				}
			}
			return false;
		}
		
		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
	}

	 
	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		linenos = new ArrayList<Integer>();
		keywords = new HashMap<String, Kind>(); 
		for(Kind token : Kind.values())
		{
			String str = token.getText();
			if(str.length()>0 && Character.isLetter(str.charAt(0)) && !str.equals("eof"))
			{
				keywords.put(token.getText(),token);
			}
		}
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int length = chars.length();
		linenos.add(0);
		State state = State.START;
		int startPos =0;
		boolean isBar = false;
		//boolean isNot = false;
		//boolean isLess = false;
		//boolean isGreat = false;
		//int count_eq =0;
		int ch;
		while(pos <= length)
		{
			ch = pos<length? chars.charAt(pos):-1;
			switch(state)
			{
				case START:
				{
					pos = skipWhiteSpace(pos);
					ch = pos<chars.length()? chars.charAt(pos):-1;
					startPos = pos;
					isBar = false;
					//isNot = false;
					//isLess = false;
					//isGreat = false;
					//count_eq =0;
					switch(ch)
					{
					case -1:
					{
						pos++;
					}break;
					case ';':
					{
						tokens.add(new Token(Kind.SEMI,startPos,1));pos++;
					}break;
					case ',':
					{
						tokens.add(new Token(Kind.COMMA,startPos,1));pos++;
					}break;
					case ')':
					{
						tokens.add(new Token(Kind.RPAREN,startPos,1));pos++;
					}break;
					case '(':
					{
						tokens.add(new Token(Kind.LPAREN,startPos,1));pos++;
					}break;
					case '{':
					{
						tokens.add(new Token(Kind.LBRACE,startPos,1));pos++;
					}break;
					case '}':
					{
						tokens.add(new Token(Kind.RBRACE,startPos,1));pos++;
					}break;
					case '&':
					{
						tokens.add(new Token(Kind.AND,startPos,1));pos++;
					}break;
					case '+':
					{
						tokens.add(new Token(Kind.PLUS,startPos,1));pos++;
					}break;
					case '*':
					{
						tokens.add(new Token(Kind.TIMES,startPos,1));pos++;
					}break;
					case '%':
					{
						tokens.add(new Token(Kind.MOD,startPos,1));pos++;
					}break;
					case '0':
					{
						tokens.add(new Token(Kind.INT_LIT,startPos,1));pos++;
					}break;
					case '=':
					{
						state = State.AFTER_EQ;pos++;
					}break;
					case '|':
					{
						state = State.AFTER_OR;pos++;
						isBar = true;
					}break;
					case '!':
					{
						state = State.AFTER_NOT;pos++;
						//isNot = true;
					}break;
					case '<':
					{
						state = State.AFTER_LESS;pos++;
						//isLess = true;
					}break;
					case '>':
					{
						state = State.AFTER_GREAT;pos++;
						//isGreat = true;
					}break;
					case '-':
					{
						state = State.AFTER_MINUS;pos++;
					}break;
					case '/':
					{
						state = State.AFTER_SLASH;pos++;
					}break;
					default:
					{
						if(Character.isDigit(ch))
						{
							state = State.IN_DIGIT;pos++;
						}
						else if(Character.isJavaIdentifierStart(ch))
						{
							state = State.IN_IDENT;
							pos++;
						}
						else
						{
							throw new IllegalCharException("illegal char" + ch + "at pos"+pos);
						}
					}break;
					}
				}break;
				case AFTER_EQ:
				{
					if(ch == '=')
					{
						/*if(isLess)
						{
							count_eq++;pos++;state = State.AFTER_EQ;
						}
						else if(isGreat)
						{
							count_eq++;pos++;state = State.AFTER_EQ;
						}
						else if(isNot)
						{
							count_eq++;pos++;state = State.AFTER_EQ;
						}
						else*/
						{
							tokens.add(new Token(Kind.EQUAL,startPos,2));pos++;state = State.START;
						}
					}
					else
					{
						/*if(isLess)
						{
							if(count_eq %2 ==0)
							{
								tokens.add(new Token(Kind.LT,startPos,1));
								int num = count_eq /2;
								for(int i =1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+1+2*(i-1),2));
								}
							}
							else
							{
								tokens.add(new Token(Kind.LE,startPos,2));
								int num = (count_eq-1)/2;
								for(int i=1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+2*i,2));
								}
							}
							state = State.START;
						}
						else if(isGreat)
						{
							if(count_eq %2 ==0)
							{
								tokens.add(new Token(Kind.GT,startPos,1));
								int num = count_eq /2;
								for(int i =1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+1+2*(i-1),2));
								}
							}
							else
							{
								tokens.add(new Token(Kind.GE,startPos,2));
								int num = (count_eq-1)/2;
								for(int i=1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+2*i,2));
								}
							}
							state = State.START;
						}
						else if(isNot)
						{
							if(count_eq %2 ==0)
							{
								tokens.add(new Token(Kind.NOT,startPos,1));
								int num = count_eq /2;
								for(int i =1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+1+2*(i-1),2));
								}
							}
							else
							{
								tokens.add(new Token(Kind.NOTEQUAL,startPos,2));
								int num = (count_eq-1)/2;
								for(int i=1;i<=num;i++)
								{
									tokens.add(new Token(Kind.EQUAL,startPos+2*i,2));
								}
							}
							state = State.START;
						}
						else*/
						{
							throw new IllegalCharException("illegal char " + '=' + " at pos " + (pos-1));
						}
					}
				}break;
				case AFTER_OR:
				{
					if(ch == '-')
					{
						state = State.AFTER_MINUS;pos++;
					}
					else
					{
						tokens.add(new Token(Kind.OR,startPos,1));state = State.START;
					}
				}break;
				case AFTER_MINUS:
				{
					if(ch == '>')
					{
						if(isBar)
						{
							tokens.add(new Token(Kind.BARARROW,startPos,3));pos++;state = State.START;
						}
						else
						{
							tokens.add(new Token(Kind.ARROW,startPos,2));pos++;state = State.START;
						}
					}
					else
					{
						if(isBar)
						{
							tokens.add(new Token(Kind.OR,startPos,1));
							tokens.add(new Token(Kind.MINUS,startPos+1,1));state = State.START;
						}
						else
						{
							tokens.add(new Token(Kind.MINUS,startPos,1));state = State.START;
						}
					}
				}break;
				case AFTER_NOT:
				{
					if(ch == '=')
					{
						//count_eq++;
						//state = State.AFTER_EQ;pos++;
						tokens.add(new Token(Kind.NOTEQUAL,startPos,2));state = State.START;pos++;
					}
					else
					{
						tokens.add(new Token(Kind.NOT,startPos,1));state = State.START;
					}
				}break;
				case AFTER_LESS:
				{
					if(ch == '=')
					{
						//count_eq++;
						//state = State.AFTER_EQ;pos++;
						tokens.add(new Token(Kind.LE,startPos,2));state = State.START;pos++;
					}
					else if(ch == '-')
					{
						tokens.add(new Token(Kind.ASSIGN,startPos,2));pos++;state = State.START;
					}
					else
					{
						tokens.add(new Token(Kind.LT,startPos,1));state = State.START;
					}
				}break;
				case AFTER_GREAT:
				{
					if(ch == '=')
					{
						//count_eq++;
						//state = State.AFTER_EQ;pos++;
						tokens.add(new Token(Kind.GE,startPos,2));state = State.START;pos++;
					}
					else
					{
						tokens.add(new Token(Kind.GT,startPos,1));state = State.START;
					}
				}break;
				case AFTER_SLASH:
				{
					if(ch == '*')
					{
						state = State.IN_COMMENT;pos++;
					}
					else
					{
						tokens.add(new Token(Kind.DIV,startPos,1));state = State.START;
					}
				}break;
				case IN_COMMENT:
				{
					if(Character.isWhitespace(ch))
					{
						if(ch == '\n')
						{
							linenos.add(pos);
						}
						pos++;
					}
					else if(ch == '*')
					{
						state = State.END_COMMENT;
						pos++;
					}
					else
					{
						pos++;
					}
				}break;
				case END_COMMENT:
				{
					if(ch == '/')
					{
						pos++;
						state = State.START;
					}
					else
					{
						state = State.IN_COMMENT;
					}
				}break;
				case IN_IDENT:
				{
					if(Character.isJavaIdentifierPart(ch))
					{
						pos++;
					}
					else
					{
						String identifier = chars.substring(startPos, pos);
						Kind token = keywords.get(identifier);
						if(token == null) 
						{
							tokens.add(new Token(Kind.IDENT,startPos,pos-startPos));state = State.START;
						}
						else
						{
							tokens.add(new Token(token,startPos,pos-startPos));state = State.START;
						}
					}
				}break;
				case IN_DIGIT:
				{
					if(Character.isDigit(ch))
					{
						pos++;
					}
					else
					{
						String identifier = chars.substring(startPos, pos);
						try{
							int val = Integer.parseInt(identifier);
						}
						catch(NumberFormatException e)
						{
							throw new IllegalNumberException("illegal number " + identifier + " at pos " + (pos-1));
						}
						tokens.add(new Token(Kind.INT_LIT,startPos,pos-startPos));state = State.START;
					}
				}break;
			}
			if(ch == -1)
			{
				tokens.add(new Token(Kind.EOF,pos,0));
				break;
			}
		}
		return this;  
	}

	public int skipWhiteSpace(int pos)
	{
		int ch;
		while(true)
		{
			ch = pos<chars.length()? chars.charAt(pos):-1;
			if(Character.isWhitespace(ch))
			{
				if(ch == '\n')
				{
					linenos.add(pos);
				}
				pos++;
			}
			else
			{
				break;
			}
		}
		return pos;
	}
	
	final ArrayList<Token> tokens;
	final ArrayList<Integer> linenos;
	final String chars;
	int tokenNum=0;
	private static Map<String, Kind> keywords;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
