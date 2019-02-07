/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cti
 */
public class JoinsFinder {
    
    LinkedList<Character> cloneTask;
    BPMNModel BPMN;
    WFG WFG;
    String notation;//la notación es un String que se va acumulando
    
    public JoinsFinder(BPMNModel bpmn, WFG wfg){
        //esta es la lista de tareas, iremos quitando los nodos que se vayan visitando
        //la idea es tener visible globalmente a esta variable, y cuando se visite una tarea
        //incluirla en la notación, se quita de la lista,
     
        this.BPMN = bpmn;
        this.cloneTask = new LinkedList<>();
        this.cloneTask.addAll(BPMN.T);
        System.out.println("CloneTask: " + cloneTask.toString());
        //Para utilizar funciones y obtener el grafo
        this.WFG = wfg;
    }
    
    
     public void findNotation(){
         
      //comenzamos con la tarea inical
      Character next = BPMN.i;
      notation = "";
      while(next != null){
         if(next == '0')//este valor se devuelve solo en el caso en que en un joint no hay una tarea comun a todas las ramas del split
            next = cloneTask.get(0);     //en ese caso, se toma como siguiente tarea alguna que no se haya considerado en la lista de tareas, revisar esta parte 
      
         next = FindNext(next); //findNext devuelve la siguiente tarea que se debe considerar para agregarla a la notación                              //
      }
   }
     
     public Character FindNext(Character s){
     //si el nodo actual es una tarea, regresamos su sucesor y terminamos
     
     if(BPMN.Gand.contains(s) || BPMN.Gxor.contains(s)){ //es una compuerta
          System.out.println("Compuerta encontrada...");
          //si no es tarea, debe ser una compuerta
          String type = "";
          
         if(BPMN.Gand.contains(s))
            type = "AND";
         else if(BPMN.Gxor.contains(s))
            type = "XOR";
         else if (type.equals(""))
            return '0';        //caso en el que no es ni compuerta ni tarea, este caso se espera que no ocurra
         else
             type = "OR";
         
         //se creara una notacion de compuerta
         notation += " " + type + "{ ";
         
         HashSet<Character> vals  = new HashSet<Character>();
         LinkedList<String> ramas = new LinkedList<String>();
         
         //explora cada rama del split, y para cada una obtiene el elemento de cierre.
         //resolve() regresa cuando ha encontrado un nodo cierre para cada rama.
         //en ramas, se regresan cada una de las notaciones de las ramas (string) que se encontraron, 
         //en vals se regresan los nodos de cierre encontrados.
         resolve(s,ramas,vals);
         //agregar cada una de las ramas encontradas a la notacion
         for(String rama:ramas)
            notation += (rama + ",");
         
         notation += "}"; //cierre de la compuerta
         
         //MODIFICAR EL GRAFO, AGRGANDO NUEVOS ARCOS, FALTA!!!!
            //para cada valor en vals, conectar y reconectar, agrgando nuevos arcos a la nueva compuerta
         
         if(vals.size() == 1){ //se crea un join del mismo tipo que la compuerta
             Character candidate = (Character)vals.toArray()[0];
             Character symbol = ' ';
             if (BPMN.Gand.contains(s)) {
                symbol = (char) (BPMN.Gand.getLast() + 1);
                BPMN.Gand.add(symbol);
            }else if (BPMN.Gxor.contains(s)) {
                symbol = (char) (BPMN.Gxor.getLast() + 1);
                BPMN.Gxor.add(symbol);
            }
             //Modificar nuevos arcos
             WFG.remplazarEdges(candidate, symbol);//Desconectar los nodos y conectarlos a la nueva compuerta join
             WFG.WFG.put(symbol + "," + candidate, 1);//Crear nodo del join cuyo sucesor es c
             System.out.println("Se retorna: " + candidate);
             return candidate;   //terminar la función, si todas las ramas van al mismo nodo, solo debe haber un elemento en vals
         }
         else { //se crean compuertas ors de cierre de la compuerta
             for (Character candidate : vals) {
                Character orSymbol;
                if (WFG.Gors.isEmpty()) {
                    orSymbol = '!';
                } else {
                    orSymbol = (char) (WFG.Gors.getLast() + 1);
                }
                WFG.Gors.add(orSymbol);
                WFG.remplazarEdges(candidate, orSymbol); //Desconectar nodos y conectarlos a la nueva compuerta join
                WFG.WFG.put(orSymbol + "," + candidate, 1);//Crear nodo del join cuyo sucesor es c
                System.out.println("\n\t\t\tJoin de tipo: " + orSymbol + " creado");
            }
             System.out.println("0 " );
             
             return '0'; //tomar alguna otra tarea cuya rama no se ha explorado
         }
            
      
          
      }else{
         notation += " " + s;
         System.out.println(notation);
         //si es una tarea, entonces solo tiene un sucesor, es cierto esto?
         
         //ELIMINAR EN LA LISTA DE TAREAS QUE ESTA TAREA YA SE CONSIDERO
         cloneTask.remove(s);
         return sucesor(s);
      }
      
   }
     
     
     //para el símbolo de entrada que es una compuerta, explora cada rama     
   public void resolve(Character gate, LinkedList<String> ramas, HashSet<Character> vals){
      
      HashSet<Character> sigs = WFG.successors(gate);
      for(Character t : sigs){
         //String notationRama = ""; //la notacion de esta rama 
        StringBuilder notationRama = new StringBuilder ();
        //regresa el nodo de cierre para esa rama, es decir, el primer nodo que tiene mas
         //de un arco incidente
         Character cierre = explorarRama(t,notationRama);
         vals.add(cierre);
         ramas.add(notationRama.toString());
      }
   }
      
   public Character explorarRama(Character nodo, StringBuilder notation){
      Character s = sucesor(nodo);
      System.out.println("Explorar rama de: " + nodo + " su sucesor es: " + s);
      if(s == null)
          return '0';
      if(WFG.getNumberEdgesToA(s) > 1){//este es el nodo de cierre para esta rama, regresar con la notación de esta rama hasta el momento
          notation.append(nodo);
         return s;
      }
      else {
      //crear la notación para este nuevo segmento del grafo, hasta donde se encuentre el posible join
         Character next = FindNext( nodo);
         notation.append(nodo);
         return next;
      }
   }
    
    //encuentra el sucesor del nodo dado, la cual es una tarea y se espera que solo tenga un solo sucesor  
   public Character sucesor(Character task){
      List<Map.Entry<String,Integer>> edges = new ArrayList(WFG.WFG.entrySet());
      for (Map.Entry<String,Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);  
         if(task == c0)
            return c1;
      }
      return null;
   
   }
    
    
    
}
