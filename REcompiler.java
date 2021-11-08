//Name: Lan Niu   ID: 1320386
//Name: Shizhen Wang  ID: 1240171
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class REcompiler {
    
    public static void main(String[] args) throws IOException {
        
        Parser pas = new Parser(args[0]);
        pas.parse();
    }

    static class Parser {
    
	private  char[] ck;
	private char[] specials; // list of non literals
	private int j; // index of the string we are currently parsing
	private char[] p; // String[] containing the given regexp
	private String reg;
	private List<Character> ch;	// character at given state
	private List<Integer> next1;
	private List<Integer> next2;
	private int state = 0;
    
    
	public Parser(String regexp) {
	    // create variables
	    reg = regexp;
	    specials = new char[] {'.','\\','*','?','|','[',']','(',')','^'}; // set of all special operators inside our grammar
	    j = 0;
	    ch = new ArrayList<Character>();
	    next1 = new ArrayList<Integer>();
	    next2 = new ArrayList<Integer>();
	    p = regexp.toCharArray();
	}
    
	public void parse() throws IOException {
	    int initial = expression();
	    if (!outOfBounds()) error("Did not consume entire input"); // check if index is matching a correct regex
	    else valid(initial);
	}
    
	private void setState(int s, Character c, int n1, int n2){
	    ch.add(s, c);
	    next1.add(s, n1);
	    next2.add(s, n2);
	}
    
	private void overwriteState(int s, Character c, int n1, int n2){
        
	    if (c != (char)0) ch.set(s, c);
	    next1.set(s, n1);
	    next2.set(s, n2);
	}
    
	private int expression() {
	    setState(state++, (char)0, state, state);
	   
	    
	    int r = term();
        
	    if (!outOfBounds() && p[j] == ('|')) {
            
		j++;
		setState(state, (char)0, 0, 0);
		int pipeState =	state++;
		int t = expression();
            
		overwriteState(pipeState, (char)0, t, r);
		
		overwriteState(0, (char)0, pipeState, pipeState);
            
		
		int index = pipeState - 4;
		if (index >= 0 && ch.get(index) == (char)0 && next1.get(index) == (index + 1) && next2.get(index) == index + 2) {
		    overwriteState(pipeState - 1, ch.get(pipeState - 1), state, state);
		}
            
		if (ch.get(pipeState - 1) != (char)0) overwriteState(pipeState - 1, ch.get(pipeState - 1), state, state);
		
		else overwriteState(pipeState - 1, ch.get(pipeState - 1), next1.get(pipeState - 1), state);
            
		r = pipeState;
            
	    }
        
	    if (!outOfBounds() && (isLiteral(p[j]) || p[j] != ')')) expression();
	    return r;
	}
    
	private int term() {
        
	    int r = state;
        
	  
	    if (p[j] == '\\') {
		j++;
		if (!outOfBounds()) {
		    setState(state++, p[j], state, state);
		    j++;
		    if (outOfBounds()) return r;
		}
		else error("escape character last character");
	    }
        
	 
	    if (!outOfBounds() && p[j] == '|') {
		return r;
	    }
        
	    else {
		r = factor();
            
		
		if (!outOfBounds() && ((p[j] == '*') || (p[j] == '?'))) {
		    if (p[j - 1] != ')' && p[j - 1] != ']') {
                    
			for (int i = 0; i < ch.size(); i++) {
			    if (next1.get(i) == state - 1) next1.set(i, state);
			    if (next2.get(i) == state - 1) next2.set(i, state);
			}
                    
			
			if (p[j] == '*') overwriteState(r, p[j - 1], state, state);
			else overwriteState(r, p[j - 1], state + 1, state + 1);
			setState(state++, (char)0, r, state);
		    }
		    else {
			overwriteState(r - 1, (char)0, state, state);
			setState(state++, (char)0, r, state);
			if (p[j] == '?') overwriteState(state - 2, (char)0, state, state);
		    }
		    r = state - 1;
		    j++;
		}
	    }
        
	    if (!outOfBounds() && (isLiteral(p[j]) || p[j] != ')')) term();
        
	    return r;
	}
    
	private int factor() {
       
	    int r = state;
       
	    if (!outOfBounds() && isLiteral(p[j]) || p[j] == '.') {
		setState(state++, p[j], state, state);
		j++; 
	    }
	    else if (p[j] == '(') {
		j++;
		if (p[j] == ')') error("Empty Parentheses");
		r = expression();
		if (!outOfBounds() && p[j] == ')') j++;
		else error("Unclosed parentheses inside factor expression");
	    }
	    else if (p[j] == '^'){

		if(p[j+1]!='[') error("!!!");
		else{
		    StringBuilder sb = new StringBuilder();
		    int count=0;j++;j++;
		    int z=0;
   
		    
		    while (p[j] != ']'){

			j++;
			count++;
			if (outOfBounds()) error("Unfinished range");
		    }
		    ck= new char[count];
		    j=j-count;
   
		    for(int i=0;i<ck.length;i++){
			if(p[j]!=']'){
			    ck[i]=p[j];
			}
			j++;
		    }
   
		    j++;

		    if(j!=p.length){
			while(j<p.length){

			    if(notrepeat(p[j])){
				sb.append(p[j]);
			    }
			    j++;

			}

		    }
		    else{sb.append((char)0);}
		    int endState = (sb.length() * 2) - 1 + state;
           
		    if (sb.length() == 1) {
			setState(state++, sb.charAt(0), state, state);

		    }
		    else if (sb.length() == 2) {
			setFinalBranch(sb.charAt(0), sb.charAt(1), endState);
		    }
		    else {
               
			for (int i = 0; i < sb.length() - 2; i++) {
			    setState(state++, (char)0, state, state + 1);
			    setState(state++, sb.charAt(i), endState, endState);
			}
               
			setFinalBranch(sb.charAt(sb.length() - 2), sb.charAt(sb.length() - 1), endState);
		    }
		}

	    }
	    else if (p[j] == '['){ 
           
		StringBuilder sb = new StringBuilder(); 
           
		j++; 
           
		if (p[j] == ']') {
		    sb.append(p[j]);
		    j++;
		}
           
		if (outOfBounds()) error("Unfinished range");
           
		while (p[j] != ']'){
		    sb.append(p[j]);
		    j++;
		    if (outOfBounds()) error("Unfinished range");
		}
           
		j++; 
           
		int endState = (sb.length() * 2) - 1 + state;
           
		if (sb.length() == 1) {
		    setState(state++, sb.charAt(0), state, state);
		}
		else if (sb.length() == 2) {
		    setFinalBranch(sb.charAt(0), sb.charAt(1), endState);
		}
		else {
               
		    for (int i = 0; i < sb.length() - 2; i++) {
			setState(state++, (char)0, state, state + 1);
			setState(state++, sb.charAt(i), endState, endState);
		    }
               
		    setFinalBranch(sb.charAt(sb.length() - 2), sb.charAt(sb.length() - 1), endState);
		}
	    }
	    return r;
	}
    
	private void setFinalBranch(Character c1, Character c2, int end) {
	    setState(state++, (char)0, state, state + 1);
	    setState(state++, c1, end, end);
	    setState(state++, c2, end, end);
	    setState(state++, (char)0, state, state);
	}
    
	private void error(String ex) {
	    System.out.println("ERROR: " + ex + ": " + reg);
	    System.exit(-1);
	}
    
	private void valid(int Start) throws IOException {
	    setState(state, ' ', 0, 0);
	    System.out.println('\n' + "SUCCESS: VALID REGEX " + reg);
	    printFSM();
	    outputFSM();
	}
	private boolean notrepeat(char c){
	    for(char h : ck) {
		if (c == h) {return false;}
	    }
	    return true;
	}
	
	private boolean isLiteral(char c){
	    for(char h : specials) {
		if (c == h) return false;
	    }
	    return true;
	}
    
	
	private boolean outOfBounds() {
	    if (j >= p.length) return true;
	    return false;
	}
    
	public void printFSM() {
        
	    StringBuilder sb0;
	
	    for(int i =0;i<ch.size();i++)
		{
		    sb0 = new StringBuilder();

		    sb0.append(i+" ");
		    if(ch.get(i)==(char)0)
			{
			    sb0.append("  ");
			}
		    else
			{
			    sb0.append(ch.get(i)+" ");
			}
		    sb0.append(next1.get(i)+" ");
		    sb0.append(next2.get(i)+" ");
		    System.out.println(sb0.toString());
		}
			
	}
    
	public void outputFSM() throws IOException{
	    String abs = System.getProperty("user.dir");
	    File output = new File(abs + File.separator + "output.fsm");
	    FileWriter fr = new FileWriter(output);
	    BufferedWriter wr = new BufferedWriter(fr);
   
	    StringBuilder sb0;
	
	    for(int i =0;i<ch.size();i++)
		{
		    sb0 = new StringBuilder();

		    sb0.append(i+",");
		    if(ch.get(i)==(char)0)
			{
			    sb0.append(" ,");
			}
		    else
			{
			    sb0.append(ch.get(i)+",");
			}
		    sb0.append(next1.get(i)+",");
		    sb0.append(next2.get(i));
		    wr.write(sb0.toString());
		    wr.newLine();
		}
	    wr.close();
	}
    }

}
