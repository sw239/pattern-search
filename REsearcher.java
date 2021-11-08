//Name: Lan Niu   ID: 1320386
//Name: Shizhen Wang  ID: 1240171

 
import java.util.ArrayList;
import java.io.*;

public class REsearcher{
    public static ArrayList<String> chars;    
    public static ArrayList<Integer> state1;  
    public static ArrayList<Integer> state2;  
    public static boolean mathced = false;
    public static void main(String[] args) throws IOException{
	chars = new ArrayList<String>();   
	state1 = new ArrayList<Integer>();
	state2 = new ArrayList<Integer>();
	if(args.length != 1){   
	    System.err.println("The output for FSM and a Text file are needed for REsearch class!");
	    return;
	}

	generateFSM();   
		
	BufferedReader brText = new BufferedReader(new FileReader(args[0]));
	String line;
	while((line = brText.readLine()) != null)  
	    checkLine(chars, state1, state2, line);
	if(!mathced)    
	    System.err.println("No result!");
		
    }
    
    private static void checkLine(ArrayList<String> chars, ArrayList<Integer> state1, 
				  ArrayList<Integer> state2, String st){
	Deque<Integer> deque;
	for(int i = 0; i < st.length(); i ++){
	    deque = new Deque<Integer>();
	    deque.pushTail(null);        
	    deque.pushTail(state1.get(0));  
	    boolean sucess = checkCharacter(chars, state1, state2, st, deque, i);
	    if (sucess) {  		
		System.out.println(st);
		mathced = true;
		break;
	    }
	}
		
    }
    
    private static boolean checkCharacter(ArrayList<String> chars, ArrayList<Integer> state1, 
					  ArrayList<Integer> state2, String st, Deque<Integer> deque, int i){
	for(int offset = 0; i + offset < st.length(); offset++){  
	    String ch = st.substring(i + offset, i + offset + 1);
	    while(true){
		Integer state = deque.popTail(); 
		if(state == null){           
		    if(!deque.isEmpty())
			deque.pushHead(null);    
		    break;
		}
		if(state.equals(chars.size() -1))return true;
		else if(chars.get(state).equals(" ")){        
		    if(state1.get(state).equals(chars.size() -1)
		       || state2.get(state).equals(chars.size() -1))return true;
		    deque.pushTail(state1.get(state));
		    deque.pushTail(state2.get(state));
		}else if(chars.get(state).equals(".")){
		    
		    if(!checkSpecialChar(ch)){
			if(state1.get(state).equals(chars.size() -1))return true;
			deque.pushHead(state1.get(state));
		    }
		}else if(chars.get(state).equals(ch)){
		   
		    if(state1.get(state).equals(chars.size() -1))return true;
		    deque.pushHead(state1.get(state));
		}
	    }
	    if(deque.isEmpty())
		return false;
	}
	
	while(true){
	    Integer state = deque.popTail();
	    if(state == null){
		if(deque.isEmpty()) return false;
		else deque.pushTail(null);
	    }if(chars.get(state).equals(" ")){
		if(state1.get(state).equals(chars.size() -1) || 
		   state2.get(state).equals(chars.size() -1))return true;
		else{
		    deque.pushTail(state1.get(state));
		    deque.pushTail(state2.get(state));
		}
	    }
	}
    }
  
    private static void generateFSM() throws IOException{
	File f= new File("output.fsm");
	BufferedReader br = new BufferedReader(new FileReader(f));
	String line;
	while ((line = br.readLine()) != null){
	    if(line.split(",").length != 4)
		throw new ArrayIndexOutOfBoundsException("Setup failed: Wrong output from complier!");
	    chars.add(line.split(",")[1]);
	    state1.add(Integer.parseInt(line.split(",")[2]));
	    state2.add(Integer.parseInt(line.split(",")[3]));
	}
    }
    
   
    private static boolean checkSpecialChar(String character){
	if (character.equals("*") || character.equals("^")
	    || character.equals("?") || character.equals(".")
	    || character.equals("\\") || character.equals("[")
	    || character.equals("]") || character.equals("(")
	    || character.equals(")") || character.equals("|")){
	    return true;
	}
	return false;
    }
    static class Deque<T>{
	Node<T> head;
	Node<T> tail;
	
	private class Node<T>{
	    T data;
	    Node<T> left;
	    Node<T> right;
		
	    public Node(T d){
		this.data = d;
	    }
	    public T get(){
		return data;
	    }
	    public void setLeft(Node<T> l){
		this.left = l;
	    }
	    public void setRight(Node<T> r){
		this.right = r;
	    }
	    public Node<T> getLeft(){
		return left;
	    }
	    public Node<T> getRight(){
		return right;
	    }
	}
	
	public void pushTail(T d){
	    if(tail == null){  
		tail = new Node<T>(d);
		head = tail;
	    }else{
		tail.setRight(new Node<T>(d));
		tail.getRight().setLeft(tail);
		tail = tail.getRight();
	    }
	}
	
	public T popTail(){
	    if(tail == null) 
		throw new RuntimeException("The deque is empty."); 
	    else{
		T v = tail.get();
		tail = tail.getLeft();
		if(tail == null) head = null;
		else tail.setRight(null);
		return v;
	    }
	}
	
	public void pushHead(T d){
	    if(head == null){   
		head = new Node<T>(d);
		tail = head;
	    }else{
		head.setLeft(new Node<T>(d));
		head.getLeft().setRight(head);
		head = head.getLeft();
	    }
	}
	public boolean isEmpty(){
	    return head == null;
	}
    }
}
