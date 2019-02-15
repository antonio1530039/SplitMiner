package splitminer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class JoinsFinder {

    LinkedList<Character> cloneTask;
    BPMNModel BPMN;
    WFG WFG;

    public JoinsFinder(BPMNModel bpmn, WFG wfg) {
        this.BPMN = bpmn;
        this.cloneTask = new LinkedList<>();
        this.cloneTask.addAll(BPMN.T);
        this.WFG = wfg;//Para utilizar funciones y obtener el grafo
    }

    public String findNotation() {
        StringBuilder notation = new StringBuilder();
        continueExploring(notation, BPMN.i);
        removeExtraOrs();
        return notation.toString().replace(",}", "}");
    }

    public void continueExploring(StringBuilder notation, Character actual) {
        if (cloneTask.contains(actual)) { //verificar que actual sea una tarea
            notation.append(" " + actual);
            cloneTask.remove(actual);
            continueExploring(notation, getSucesorOantecesor(actual, 's'));
        } else if (BPMN.Gand.contains(actual) || BPMN.Gxor.contains(actual) || BPMN.Gor.contains(actual)) { // es compuerta, resolverla
            LinkedList<String> ramas = new LinkedList<String>();
            HashMap<Character, LinkedList<Character>> cierres = resolveGateway(actual, ramas);
            ArrayList<String> cierresYanteriores = conectarCierres(cierres, notation, actual, ramas);
            for (String s : cierresYanteriores) { //para cada cierre retornado por la compuerta, tomar como siguientes estos, los anteriores no son usados aqui...
                continueExploring(notation, s.split(",")[0].charAt(0));
            }
        }
    }

    public ArrayList<String> conectarCierres(HashMap<Character, LinkedList<Character>> cierres, StringBuilder notation, Character gateway, LinkedList<String> ramas) {
        String type = "";
        if (BPMN.Gand.contains(gateway)) {
            type = "AND";
        } else if (BPMN.Gxor.contains(gateway)) {
            type = "XOR";
        }
        notation.append(" " + type + "{ ");
        //Agregar sus ramas a la notacion
        for (String rama : ramas) {
            notation.append(rama + ",");
        }
        notation.append("}");
        
        ArrayList<String> paraCierre = new ArrayList<>(); //Esta lista es retornada, obtiene el anterior del o de los cierres con las nuevas compuertas creadas  (es utilizado en exploreBranch)
        if (cierres.size() == 1) { //Cierre del mismo tipo
            Character symbol = ' ';
            if (BPMN.Gand.contains(gateway)) {
                symbol = (char) (BPMN.Gand.getLast() + 1);
                BPMN.Gand.add(symbol);
            } else if (BPMN.Gxor.contains(gateway)) {
                symbol = (char) (BPMN.Gxor.getLast() + 1);
                BPMN.Gxor.add(symbol);
            }
            for (Map.Entry<Character, LinkedList<Character>> entry : cierres.entrySet()) {
                Character cierre = entry.getKey(); //Recuperar cierre
                LinkedList<Character> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre
                for (Character a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                    WFG.WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                    WFG.WFG.put(a + "," + symbol, 1); //nueva coneccion a la compuerta
                }
                System.out.println("\t\tJoin: " + symbol + " creado");
                WFG.WFG.put(symbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
                paraCierre.add(cierre + "," + symbol);
            }
            //return paraCierre;
            //continueExploring(notation, cierre);
        } else if (cierres.size() > 1) { //Cierre de Ors
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
                System.out.println("\t\tJoin: " + orSymbol + " creado");
                WFG.WFG.put(orSymbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
                paraCierre.add(cierre + "," + orSymbol);
            }
            //return paraCierre;
        }
        return paraCierre;
    }

    public HashMap<Character, LinkedList<Character>> resolveGateway(Character gate, LinkedList<String> ramas) {
        HashSet<Character> sigs = WFG.successors(gate);
        HashMap<Character, LinkedList<Character>> cierres = new HashMap<>();
        for (Character s : sigs) {
            StringBuilder notationRama = new StringBuilder();
            ArrayList<String> cierreYanteriores = exploreBranch(s, notationRama);
            if (cierreYanteriores.size() > 0) {
                for (String cierreYanterior : cierreYanteriores) {
                    String vals[] = cierreYanterior.split(",");
                    Character cierre = vals[0].charAt(0);
                    Character anterior = vals[1].charAt(0);
                    if (cierres.containsKey(cierre)) {
                        LinkedList<Character> list = cierres.get(cierre);
                        list.add(anterior);
                        cierres.put(cierre, list);
                    } else {
                        LinkedList<Character> list = new LinkedList<>();
                        list.add(anterior);
                        cierres.put(cierre, list);
                    }
                }
                ramas.add(notationRama.toString());
            }
        }
        return cierres;
    }

    public ArrayList<String> exploreBranch(Character nodo, StringBuilder notation) {
        ArrayList<String> cierres = new ArrayList<>();
        if (WFG.getNumberEdgesToA(nodo) > 1) { //si tiene mas de un arco incidente
            cierres.add(nodo + "," + getSucesorOantecesor(nodo, 'a')); //retornar el mismo nodo y el anterior de este
        } else {
            if (BPMN.Gand.contains(nodo) || BPMN.Gxor.contains(nodo) || BPMN.Gor.contains(nodo)) { //es compuerta... resolver
                LinkedList<String> ramas = new LinkedList<>();
                HashMap<Character, LinkedList<Character>> cierresGateway = resolveGateway(nodo, ramas);
                cierres.addAll(conectarCierres(cierresGateway, notation, nodo, ramas));
            } else if (cloneTask.contains(nodo)) {//es tarea... agregar a notacion y eliminar de lista
                notation.append(" " + nodo);
                cloneTask.remove(nodo);
                Character s = getSucesorOantecesor(nodo, 's');
                if (s != null) {
                    if (WFG.getNumberEdgesToA(s) > 1) {
                        cierres.add(s + "," + nodo);
                    } else {
                        cierres = exploreBranch(s, notation);
                    }
                }
            }
        }
        return cierres;
    }

    //encuentra el sucesor o antecesor del nodo dado, la cual es una tarea y se espera que solo tenga un solo sucesor  
    public Character getSucesorOantecesor(Character task, Character type) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.WFG.entrySet());
        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (type == 's') {
                if (task == c0) {
                    return c1;
                }
            } else {
                if (task == c1) {
                    return c0;
                }
            }
        }
        return null;
    }
    
    public void removeExtraOrs(){
        if(BPMN.Gor.isEmpty())
            return;
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.WFG.entrySet());
        for(Map.Entry<String, Integer> entry : edges){
            Character c1 = entry.getKey().split(",")[1].charAt(0);
            if(BPMN.Gor.contains(c1) && WFG.getNumberEdgesToA(c1) == 1){
                Character sucesorDeOr = getSucesorOantecesor(c1,'s');
                if(BPMN.Gor.contains(sucesorDeOr)){
                    WFG.WFG.remove(c1 + "," + sucesorDeOr );
                    WFG.remplazarEdges(c1, sucesorDeOr);
                    BPMN.Gor.remove(c1);
                }
            }
        }
    }
}
