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
        StringBuilder notation = new StringBuilder();
        continueExploring(notation, BPMN.i);
        System.out.println("Tareas al finalizar: " + cloneTask.toString());
        return notation.toString().replace(",}", "}");
    }

    public void continueExploring(StringBuilder notation, Character actual) {
        if (BPMN.T.contains(actual)) { //verificar que actual sea una tarea
            notation.append(" " + actual);
            cloneTask.remove(actual);
            continueExploring(notation, sucesor(actual));
        } else if (BPMN.Gand.contains(actual) || BPMN.Gxor.contains(actual) || BPMN.Gor.contains(actual)) { //verificar que en realidad sea una compuerta
            //Resolver la compuerta
            LinkedList<String> ramas = new LinkedList<String>();
            HashMap<Character, LinkedList<Character>> cierres = resolveGateway(actual, ramas);
            conectarCierres(cierres, notation, actual, ramas);
        }
    }

    public void conectarCierres(HashMap<Character, LinkedList<Character>> cierres, StringBuilder notation, Character gateway, LinkedList<String> ramas) {
        String type = "";
        if (BPMN.Gand.contains(gateway)) {
            type = "AND";
        } else if (BPMN.Gxor.contains(gateway)) {
            type = "XOR";
        } else if (BPMN.Gor.contains(gateway)) {
            type = "OR";
        }
        notation.append(" " + type + "{ ");
        //Para cada cierre, realizar la reconexion, dependiendo si es un cierre (mismo tipo de compuerta) o mas de un cierre (or)
        if (cierres.size() == 1) { //Cierre del mismo tipo
            Character symbol = ' ';
            if (BPMN.Gand.contains(gateway)) {
                symbol = (char) (BPMN.Gand.getLast() + 1);
                BPMN.Gand.add(symbol);
            } else if (BPMN.Gxor.contains(gateway)) {
                symbol = (char) (BPMN.Gxor.getLast() + 1);
                BPMN.Gxor.add(symbol);
            }
            //Agregar sus ramas a la notacion
            for (String rama : ramas) {
                notation.append(rama + ",");
            }
            notation.append("}"); //cierre de la compuerta

            Character cierre = ' ';
            for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                cierre = entry.getKey(); //Recuperar cierre
                LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre
                System.out.println("Cierre:: " + cierre);

                if (anteriores.isEmpty()) {
                    System.out.println("Anteriores esta vacio");
                    WFG.remplazarEdges(cierre, symbol);
                } else {
                    for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                        WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.WFG.put(a + "," + symbol, 1); //nueva coneccion a la compuerta
                    }
                    System.out.println("Join: " + symbol + " creado");
                }

                WFG.WFG.put(symbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
            }
            continueExploring(notation, cierre);
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

            //Para cada cierre creado, continuar explorando
            for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                continueExploring(notation, entry.getKey());
            }
        }
    }

    public HashMap<Character, LinkedList<Character>> resolveGateway(Character gate, LinkedList<String> ramas) {
        HashSet<Character> sigs = WFG.successors(gate); //Obtener sucesores
        HashMap<Character, LinkedList<Character>> cierres = new HashMap<>();
        for (Character s : sigs) {
            StringBuilder notationRama = new StringBuilder();
            ArrayList<String> cierreYanteriores = exploreBranch(s, notationRama);
            System.out.println("Explore branch: " + cierreYanteriores + ", se exploro: " + s);
            if (cierreYanteriores.size() > 0) {
                for (String cierreYanterior : cierreYanteriores) {
                    String vals[] = cierreYanterior.split(",");

                    if (vals.length == 2) {
                        Character cierre = vals[0].charAt(0);
                        Character anterior = vals[1].charAt(0);
                        if (cierres.containsKey(cierre)) {
                            LinkedList<Character> list = cierres.get(cierre);
                            list.add(anterior);
                            cierres.put(cierre, list);
                        } else {
                            LinkedList<Character> list = new LinkedList<Character>();
                            list.add(anterior);
                            cierres.put(cierre, list);
                        }
                    }

                }
                ramas.add(notationRama.toString());
            }
        }
        return cierres;
    }

    public ArrayList<String> exploreBranch(Character nodo, StringBuilder notation) {
        ArrayList<String> cierres = new ArrayList<>();
        if (BPMN.Gand.contains(nodo) || BPMN.Gxor.contains(nodo) || BPMN.Gor.contains(nodo)) { //Si es una compuerta,,, explorar
            LinkedList<String> ramas = new LinkedList<>();
            HashMap<Character, LinkedList<Character>> cierresGateway = resolveGateway(nodo, ramas);
            //conectarCierres(HashMap<Character, LinkedList<Character>> cierres, StringBuilder notation, Character gateway, LinkedList<String> ramas)
            conectarCierres(cierresGateway, notation, nodo, ramas);
            for (Map.Entry<Character, LinkedList<Character>> entry : cierresGateway.entrySet()) {
                Character c = entry.getKey();
                //LinkedList<Character> anterioresList = entry.getValue();
                cierres.addAll(exploreBranch(c, notation));
                continueExploring(notation, c);
            }
        } else {//si es una tarea
            notation.append(" " + nodo);
            cloneTask.remove(nodo);
            Character s = sucesor(nodo);
            if (s != null) { //si el sucesor es nulo, entonces el nodo actual es el final, por lo que se verifica .....
                if (WFG.getNumberEdgesToA(s) > 1) {
                    cierres.add(s + "," + nodo);
                } else {
                    cierres = exploreBranch(s, notation);
                }
            } else {
                cierres.add(nodo + ",");
            }

        }
        return cierres;
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
