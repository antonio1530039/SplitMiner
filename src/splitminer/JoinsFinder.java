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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cti
 */
public class JoinsFinder {

    LinkedList<Character> cloneTask;
    BPMNModel BPMN;
    WFG WFG;

    public JoinsFinder(BPMNModel bpmn, WFG wfg) {
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

    public String findNotation() {

        //comenzamos con la tarea inical
        Character next = BPMN.i;
        StringBuilder notation = new StringBuilder();
        while (next != null) {
            if (next == '0')//este valor se devuelve solo en el caso en que en un joint no hay una tarea comun a todas las ramas del split
            {
                next = cloneTask.get(0);     //en ese caso, se toma como siguiente tarea alguna que no se haya considerado en la lista de tareas, revisar esta parte 
            }
            next = FindNext(notation, next); //findNext devuelve la siguiente tarea que se debe considerar para agregarla a la notación                              //
        }
        System.out.println("Tareas al finalizar: " + cloneTask.toString());
        return notation.toString().replace(",}", "}");
    }

    public Character FindNext(StringBuilder notation, Character s) {
        //si el nodo actual es una tarea, regresamos su sucesor y terminamos

        if (BPMN.T.contains(s)) { //Es una tarea
            notation.append(" " + s);
            cloneTask.remove(s); //ELIMINAR EN LA LISTA DE TAREAS QUE ESTA TAREA YA SE CONSIDERO
            return sucesor(s);
        } else { //Es una compuerta
            System.out.println("Compuerta encontrada...");

            String type = "";
            if (BPMN.Gand.contains(s)) {
                type = "AND";
            } else if (BPMN.Gxor.contains(s)) {
                type = "XOR";
            } else if (type.equals("")) {
                return '0';        //caso en el que no es ni compuerta ni tarea, este caso se espera que no ocurra
            }
            //se creara una notacion de compuerta
            notation.append(" " + type + "{ ");

            HashMap<Character, LinkedList<Character>> vals = new HashMap<Character, LinkedList<Character>>();

            LinkedList<String> ramas = new LinkedList<String>();

            //explora cada rama del split, y para cada una obtiene el elemento de cierre.
            //resolve() regresa cuando ha encontrado un nodo cierre para cada rama.
            //en ramas, se regresan cada una de las notaciones de las ramas (string) que se encontraron, 
            //en vals se regresan los nodos de cierre encontrados.
            resolve(s, ramas, vals);
            //agregar cada una de las ramas encontradas a la notacion
            for (String rama : ramas) {
                notation.append(rama + ",");
            }

            notation.append("}"); //cierre de la compuerta
            System.out.println("\t\t\tHashMap Vals:" + vals.toString());

            //Obtener lista de valores del HashMap vals
            List<Map.Entry<Character, LinkedList<Character>>> listVals = new ArrayList(vals.entrySet());
            if (vals.size() == 1) { //se crea un join del mismo tipo que la compuerta

                //Definir el simbolo de la compuerta y agregarlo a la lista de compuertas (para no repetir simbolos)
                Character symbol = ' ';
                if (BPMN.Gand.contains(s)) {
                    symbol = (char) (BPMN.Gand.getLast() + 1);
                    BPMN.Gand.add(symbol);
                } else if (BPMN.Gxor.contains(s)) {
                    symbol = (char) (BPMN.Gxor.getLast() + 1);
                    BPMN.Gxor.add(symbol);
                }

                //Modificar todos los arcos definidos por cad cierre en el mapa
                Character cierre = ' ';
                for (Map.Entry<Character, LinkedList<Character>> entry : listVals) {
                    cierre = entry.getKey(); //Recuperar cierre
                    LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre

                    for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                        WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.WFG.put(a + "," + symbol, 1); //nueva coneccion a la compuerta
                    }

                    //Conectar la nueva compuerta al nodo cierre
                    WFG.WFG.put(symbol + "," + cierre, 1);

                }
                return cierre;//terminar la función, si todas las ramas van al mismo nodo, solo debe haber un elemento en vals

            } else if (vals.size() > 1) { //se crean compuertas ors de cierre de la compuerta
                //Modificar todos los arcos definidos por cad cierre en el mapa
                for (Map.Entry<Character, LinkedList<Character>> entry : listVals) {
                    //Definir el simbolo de la compuerta Or
                    Character orSymbol;
                    if (BPMN.Gor.isEmpty()) {
                        orSymbol = '!';
                    } else {
                        orSymbol = (char) (BPMN.Gor.getLast() + 1);
                    }
                    Character cierre = entry.getKey(); //Recuperar cierre
                    LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre
                    for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                        WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.WFG.put(a + "," + orSymbol, 1); //nueva coneccion a la compuerta
                    }

                    //Conectar la nueva compuerta al nodo cierre
                    WFG.WFG.put(orSymbol + "," + cierre, 1);
                }
                return '0'; //tomar alguna otra tarea cuya rama no se ha explorado
            }
            return '0';
        }

    }

    //para el símbolo de entrada que es una compuerta, explora cada rama     
    public void resolve(Character gate, LinkedList<String> ramas, HashMap<Character, LinkedList<Character>> vals) {

        //PENDIENTE, CASO DONDE LA XOR TIENE LA RAMA VACIA
        HashSet<Character> sigs = WFG.successors(gate);
        for (Character t : sigs) {
            //String notationRama = ""; //la notacion de esta rama 
            StringBuilder notationRama = new StringBuilder();
            //regresa el nodo de cierre para esa rama, es decir, el primer nodo que tiene mas
            //de un arco incidente
            StringBuilder last = new StringBuilder();
            Character cierre = ' ';
            if (WFG.getNumberEdgesToA(t) > 1) { //Si t tiene mas de un arco incidente; *el sucesor es el cierre... se resuelve ahi mismo, *la rama es vacia, *el antecesor es la misma compuerta
                cierre = t;
                notationRama.append("");
                last.append(gate);
            } else {
                cierre = explorarRama(t, notationRama, last);
            }

            if (cierre != ' ') {
                if (vals.containsKey(cierre)) {
                    LinkedList<Character> list = vals.get(cierre);
                    list.add(last.charAt(0));
                    vals.put(cierre, list);
                } else {
                    LinkedList<Character> list = new LinkedList<Character>();
                    list.add(last.charAt(0));
                    vals.put(cierre, list);
                }

                ramas.add(notationRama.toString());
            }

        }
    }

    public Character explorarRama(Character nodo, StringBuilder notationRama, StringBuilder last) {
        Character s = sucesor(nodo);
        if (s == null) {
            return ' ';    //PENDIENTE DE REVISAR
        }
        Character cierre;
        if (WFG.getNumberEdgesToA(s) > 1) {//este es el nodo de cierre para esta rama, regresar con la notación de esta rama hasta el momento
            cierre = s;
            notationRama.append(" " + nodo);
            last.append(nodo.toString());
        } else {
            //crear la notación para este nuevo segmento del grafo, hasta donde se encuentre el posible join
            cierre = FindNext(notationRama, nodo);
            while(WFG.getNumberEdgesToA(cierre) <= 1){
                s = cierre;
                cierre = FindNext(notationRama, cierre);
            }
            last.append(""+s);
        }
        return cierre;
    }

    //encuentra el sucesor del nodo dado, la cual es una tarea y se espera que solo tenga un solo sucesor  
    public Character sucesor(Character task) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.WFG.entrySet());
        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (task == c0) {
                return c1;
            }
        }
        return null;

    }

}
