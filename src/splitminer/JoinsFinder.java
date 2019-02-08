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

    public void findNotation() {

        //comenzamos con la tarea inical
        Character next = BPMN.i;
        notation = "";
        while (next != null) {
            if (next == '0')//este valor se devuelve solo en el caso en que en un joint no hay una tarea comun a todas las ramas del split
            {
                next = cloneTask.get(0);     //en ese caso, se toma como siguiente tarea alguna que no se haya considerado en la lista de tareas, revisar esta parte 
            }
            next = FindNext(next); //findNext devuelve la siguiente tarea que se debe considerar para agregarla a la notación                              //
        }
        System.out.println("Tareas: " + cloneTask.toString());
        notation = notation.replace(",}", " }");
    }

    public Character FindNext(Character s) {
        //si el nodo actual es una tarea, regresamos su sucesor y terminamos

        if (BPMN.T.contains(s)) { //Es una tarea
            notation += " " + s;
            System.out.println(notation);
            //si es una tarea, entonces solo tiene un sucesor, es cierto esto?

            //ELIMINAR EN LA LISTA DE TAREAS QUE ESTA TAREA YA SE CONSIDERO
            cloneTask.remove(s);
            return sucesor(s);
        } else { //es una compuerta
            System.out.println("Compuerta encontrada...");
            //si no es tarea, debe ser una compuerta
            String type = "";

            if (BPMN.Gand.contains(s)) {
                type = "AND";
            } else if (BPMN.Gxor.contains(s)) {
                type = "XOR";
            } else if (type.equals("")) {
                return '0';        //caso en el que no es ni compuerta ni tarea, este caso se espera que no ocurra
            } else {
                type = "OR";
            }

            //se creara una notacion de compuerta
            notation += " " + type + "{ ";

            HashSet<Character> vals = new HashSet<Character>();
            LinkedList<String> ramas = new LinkedList<String>();

            //explora cada rama del split, y para cada una obtiene el elemento de cierre.
            //resolve() regresa cuando ha encontrado un nodo cierre para cada rama.
            //en ramas, se regresan cada una de las notaciones de las ramas (string) que se encontraron, 
            //en vals se regresan los nodos de cierre encontrados.
            resolve(s, ramas, vals);
            //agregar cada una de las ramas encontradas a la notacion
            for (String rama : ramas) {
                notation += (rama + ",");
                if (!rama.equals("") && cloneTask.contains(rama.charAt(0))) {
                    cloneTask.remove(cloneTask.indexOf(rama.charAt(0))); //Remover de la lista de tareas !!! Validar si se da el caso de que una rama contiene una compuerta
                }
            }

            notation += "}"; //cierre de la compuerta

            //MODIFICAR EL GRAFO, AGRGANDO NUEVOS ARCOS, FALTA!!!!
            //para cada valor en vals, conectar y reconectar, agrgando nuevos arcos a la nueva compuerta
            System.out.println("\t\t\t>>>>>>>>>> " + vals.toString());
            if (vals.size() == 1) { //se crea un join del mismo tipo que la compuerta
                Character candidate = (Character) vals.toArray()[0];
                Character symbol = ' ';
                if (BPMN.Gand.contains(s)) {
                    symbol = (char) (BPMN.Gand.getLast() + 1);
                    BPMN.Gand.add(symbol);
                } else if (BPMN.Gxor.contains(s)) {
                    symbol = (char) (BPMN.Gxor.getLast() + 1);
                    BPMN.Gxor.add(symbol);
                }
                //Modificar nuevos arcos

                //Obtener antecesores del candidato
                HashSet<Character> antecesores = WFG.antecessors(candidate);

                for (Character c : antecesores) {
                    //Verificar que el antecesor pertenezca a la compuerta
                    if (esSucesorDeCompuerta(c, s)) {
                        WFG.WFG.remove(c + "," + candidate);
                        WFG.WFG.put(c + "," + symbol, 1);
                        WFG.WFG.put(symbol + "," + candidate, 1);//Crear nodo del join cuyo sucesor es c
                        System.out.println("\t\t\tJoin de tipo: " + symbol + " creado");
                    }
                }
                return candidate;//terminar la función, si todas las ramas van al mismo nodo, solo debe haber un elemento en vals
            } else if (vals.size() > 1) { //se crean compuertas ors de cierre de la compuerta
                for (Character candidate : vals) {

                    //Obtener antecesores del candidato
                    HashSet<Character> antecesores = WFG.antecessors(candidate);

                    //Verificar que los antecesores pertenezcan ala compuerta
                    for (Character c : antecesores) {
                        if (esSucesorDeCompuerta(c, s)) {
                            Character orSymbol;
                            if (BPMN.Gor.isEmpty()) {
                                orSymbol = '!';
                            } else {
                                orSymbol = (char) (BPMN.Gor.getLast() + 1);
                            }
                            WFG.WFG.remove(c + "," + candidate);
                            WFG.WFG.put(c + "," + orSymbol, 1);
                            WFG.WFG.put(orSymbol + "," + candidate, 1);//Crear nodo del join cuyo sucesor es c
                            System.out.println("\t\t\tJoin de tipo: " + orSymbol + " creado");
                        }
                    }
                }
                return '0'; //tomar alguna otra tarea cuya rama no se ha explorado
            }

            return '0';
        }

    }

    //para el símbolo de entrada que es una compuerta, explora cada rama     
    public void resolve(Character gate, LinkedList<String> ramas, HashSet<Character> vals) {

        HashSet<Character> sigs = WFG.successors(gate);
        for (Character t : sigs) {
            //String notationRama = ""; //la notacion de esta rama 
            StringBuilder notationRama = new StringBuilder();
            //regresa el nodo de cierre para esa rama, es decir, el primer nodo que tiene mas
            //de un arco incidente
            Character cierre = explorarRama(t, notationRama);
            if(cierre != ' '){
                vals.add(cierre);
                ramas.add(notationRama.toString());
            }
            
        }
    }

    public Character explorarRama(Character nodo, StringBuilder notation) {
        Character s = sucesor(nodo);
        System.out.println("Explorar rama de: " + nodo + " su sucesor es: " + s);
        if (s == null) {
            return ' ';
        }
        Character cierre;
        if (WFG.getNumberEdgesToA(s) > 1) {//este es el nodo de cierre para esta rama, regresar con la notación de esta rama hasta el momento
            cierre = s;
            notation.append(nodo);
        } else {
            //crear la notación para este nuevo segmento del grafo, hasta donde se encuentre el posible join
            cierre = FindNext(nodo);
            notation.append("");
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

    public Character antecesor(Character task) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.WFG.entrySet());
        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (task == c1) {
                return c0;
            }
        }
        return null;

    }

    public boolean esSucesorDeCompuerta(Character task, Character gate) {
        Character antecesor = antecesor(task);
        if (antecesor == gate) {
            return true;
        }
        while (antecesor != BPMN.i) {
            antecesor = antecesor(antecesor);
            if (antecesor == gate) {
                return true;
            }
        }
        return false;
    }

}
