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

    public void follow(StringBuilder notation, Character actual) {

        if (BPMN.T.contains(actual)) {
            notation.append(" " + actual);
            cloneTask.remove(actual);
            follow(notation, sucesor(actual));
        } else {
            String type = "";
            if (BPMN.Gand.contains(actual)) {
                type = "AND";
            } else if (BPMN.Gxor.contains(actual)) {
                type = "XOR";
            } else if (BPMN.Gor.contains(actual)) {
                type = "OR";
            }
            notation.append(" " + type + "{ ");

            //Resolver la compuerta
            LinkedList<String> ramas = new LinkedList<String>();
            HashMap<Character, LinkedList<Character>> cierres = resolveGateway(actual, ramas);
            //Agregar sus ramas a la notacion
            for (String rama : ramas) {
                notation.append(rama + ",");
            }
            notation.append("}"); //cierre de la compuerta

            //Para cada cierre, realizar la reconexion, dependiendo si es un cierre (mismo tipo de compuerta) o mas de un cierre (or)
            if (cierres.size() == 1) { //Cierre del mismo tipo
                Character symbol = ' ';
                if (BPMN.Gand.contains(actual)) {
                    symbol = (char) (BPMN.Gand.getLast() + 1);
                    BPMN.Gand.add(symbol);
                } else if (BPMN.Gxor.contains(actual)) {
                    symbol = (char) (BPMN.Gxor.getLast() + 1);
                    BPMN.Gxor.add(symbol);
                }

                Character cierre = ' ';
                for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                    cierre = entry.getKey(); //Recuperar cierre
                    LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre

                    for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                        WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.WFG.put(a + "," + symbol, 1); //nueva coneccion a la compuerta
                    }
                    System.out.println("And " + symbol + " creado");
                    WFG.WFG.put(symbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
                }
                follow(notation, cierre);
            } else if (cierres.size() > 1) { //Cierre de Ors
                //Modificar todos los arcos definidos por cad cierre en el mapa
                for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                    Character orSymbol;//Definir el simbolo de la compuerta Or
                    if (BPMN.Gor.isEmpty()) {
                        orSymbol = '!';
                    } else {
                        orSymbol = (char) (BPMN.Gor.getLast() + 1);
                    }
                    BPMN.Gor.add(orSymbol);
                    Character cierre = entry.getKey(); //Recuperar cierre
                    LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre
                    for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                        WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.WFG.put(a + "," + orSymbol, 1); //nueva coneccion a la compuerta
                    }
                    System.out.println("Or " + orSymbol + " creado");
                    WFG.WFG.put(orSymbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
                }

                //Para cada cierre creado, llamar nuevamente a follow
                for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                    follow(notation, entry.getKey());
                }
            }
        }

    }

    public HashMap<Character, LinkedList<Character>> resolveGateway(Character gate, LinkedList<String> ramas) {
        HashSet<Character> sigs = WFG.successors(gate); //Obtener sucesores
        HashMap<Character, LinkedList<Character>> cierres = new HashMap<>();
        for (Character s : sigs) {
            StringBuilder notationRama = new StringBuilder();
            String cierre = exploreBranch(s, notationRama);
            Character c = cierre.split(",")[0].charAt(0);
            Character a = cierre.split(",")[1].charAt(0);

            if (!cierre.equals("")) {
                if (cierres.containsKey(c)) {
                    LinkedList<Character> list = cierres.get(c);
                    list.add(a);
                    cierres.put(c, list);
                } else {
                    LinkedList<Character> list = new LinkedList<Character>();
                    list.add(a);
                    cierres.put(c, list);
                }
                ramas.add(notationRama.toString());
            }

        }
        return cierres;
        //Falta exploreBranch

    }

    //para el símbolo de entrada que es una compuerta, explora cada rama     
    public void resolve(Character gate, LinkedList<String> ramas, HashMap<Character, LinkedList<Character>> vals) {

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
        if (s == null) { //Cuando un nodo ya no tiene sucesor
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
            while (WFG.getNumberEdgesToA(cierre) <= 1) {
                s = cierre;
                cierre = FindNext(notationRama, cierre);

                //if(cierre == '0' )
                //  cierre = cloneTask.get(0); 
            }
            last.append("" + s);
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
