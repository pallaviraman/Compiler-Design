package cop5556sp17;



import cop5556sp17.AST.Dec;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Set;


public class SymbolTable {
	
	
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
		
		static class SymbolNode{
			public SymbolNode(Dec dec,int scope,SymbolNode next)
			{
				super();
				this.dec = dec;
				this.next = next;
				this.scope = scope;
			}
			int scope;
			Dec dec;
			SymbolNode next = null;
		}
		
		HashMap<String, SymbolNode> symbolTable;
		Stack<Integer> scopeStack;
		int currScope; 
		int nextscope;
		
	
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		currScope = ++nextscope;
		scopeStack.push(currScope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		if(scopeStack.size()>0)
		{
			scopeStack.pop();
			currScope = (int)scopeStack.peek();
		}
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		SymbolNode node = symbolTable.get(ident);
		while(node !=null)
		{
			if(node.scope == currScope){return false;}
			node = node.next;
		}
		symbolTable.put(ident,new SymbolNode(dec,currScope,symbolTable.get(ident)));
		
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		SymbolNode node = symbolTable.get(ident);
		if(node == null)
		{
			return null;
		}
		SymbolNode temp;
			temp = node;
			int scope = currScope;
			while(temp != null)
			{
				if(temp.scope <= scope)
				{
					return temp.dec;
				}
				temp = temp.next;
			}
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		currScope = 0;
		nextscope =0;
		symbolTable = new HashMap<String,SymbolNode>();
		scopeStack = new Stack<Integer>();
		scopeStack.push(currScope);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		StringBuilder sb = new StringBuilder();
		sb.append("ScopeStack: \n");
		for(int i : scopeStack)
		{
			sb.append(i).append('\n');
		}
		sb.append("SymbolEntries: \n");
		Set<Entry<String, SymbolNode>>  symEntries = symbolTable.entrySet();
		for (Entry<String, SymbolNode> symEntry: symEntries){
			sb.append(symEntry.getKey()).append(':');
			SymbolNode entry = symEntry.getValue();
			while (entry != null){
				sb.append('[').append(entry.scope).append(',').append(entry.dec.toString()).append("] ");
				entry = entry.next;
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	


}
