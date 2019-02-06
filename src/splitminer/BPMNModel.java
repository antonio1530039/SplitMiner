package splitminer;
import java.io.*;
import java.util.*;


public class BPMNModel{

   public LinkedList<Character> T;
   public Character i;
   public Character o;
   public LinkedHashMap<Character,HashSet<Character>> ANDs;
   public LinkedHashMap<Character,HashSet<Character>> XORs;

   public LinkedList<Character> Gand;
   public LinkedList<Character> Gxor;
   public LinkedList<Character> Gor;
   
   public BPMNModel(){
      T = new LinkedList<Character>();
      ANDs = new LinkedHashMap<Character,HashSet<Character>>();
      XORs= new LinkedHashMap<Character,HashSet<Character>>();
      Gand = new LinkedList<Character>();
      Gxor = new LinkedList<Character>();
      Gor = new LinkedList<Character>();
   
   }
   
}
