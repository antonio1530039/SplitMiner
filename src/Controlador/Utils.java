package Controlador;

import splitminer.*;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Utils {
    
    
     //Encontrar el numero de edges entrantes ( *a )
    public static int getNumberEdgesToA(Character a, LinkedHashMap<String, Integer> WFG) {
        int i = 0;
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            if (a == entry.getKey().split(",")[1].charAt(0)) {
                i++;
            }
        }
        return i;
    }
    
    public static void mostrarGrafo(int numTabs, LinkedHashMap<String, Integer> WFG) {
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            for (int i = 0; i < numTabs; i++) {
                System.out.print("\t");
            }
            System.out.println("[" + entry.getKey() + "]" + " - " + entry.getValue());
        }
    }
    
    public static void interseccion(HashSet<Character> dest, HashSet<Character> src) {
        HashSet<Character> temp = new HashSet<Character>();

        for (Character task : dest) {
            temp.add(task);
        }

        dest.clear();

        for (Character task : src) {
            if (temp.contains(task)) {
                dest.add(task);
            }
        }

    }

    public static void resta(HashSet<Character> dest, HashSet<Character> src) {
        for (Character task : src) {
            dest.remove(task);
        }

    }

    public static void union(HashSet<Character> dest, HashSet<Character> src) {
        for (Character task : src) {
            dest.add(task);
        }

    }

    public static boolean iguales(HashSet<Character> first, HashSet<Character> second) {

        /*if(!first.isEmpty()){
           
         if (second.containsAll(first)){
            return true;
           
         }
      }*/
        if (first.size() != second.size()) {
            return false;
        }

        for (Character task : second) {
            if (!first.contains(task)) {
                return false;

            }
        }

        return true;
    }

    //remueve edges de la forma a*    
    public static void removerEdges(Character a, LinkedHashMap<String, Integer> WFG) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c = vals[0].charAt(0);

            if (a == c) {
                WFG.remove(key);
            }
        }

    }

    public static void removerEdges(String edge, LinkedHashMap<String, Integer> WFG) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();

            if (key.equals(edge)) {
                WFG.remove(key);
            }
        }

    }

    //reemplaza 'a' por 'b' en todos los edges *a    
    public static void remplazarEdges(Character a, Character b, LinkedHashMap<String, Integer> WFG) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (a == c1) {
                WFG.remove(key);
                key = c0 + "," + b;
                WFG.put(key, 1);
            }
        }

    }

    

//all nodes following 'task', given the current pruened WFG
    public static HashSet<Character> successors(Character task, LinkedHashMap<String, Integer> WFG) {

        HashSet<Character> sucesores = new LinkedHashSet<Character>();

        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            String c1 = vals[0];
            Character c = vals[0].charAt(0);
            if (task == c) {
                sucesores.add(vals[1].charAt(0));
            }

        }

        return sucesores;
    }
    
    
    //all nodes before 'task', given the current pruened WFG
    public HashSet<Character> antecessors(Character task, LinkedHashMap<String, Integer> WFG) {

        HashSet<Character> antecesores = new LinkedHashSet<Character>();

        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c = vals[1].charAt(0);
            if (task == c) {
                antecesores.add(vals[0].charAt(0));
            }

        }

        return antecesores;
    }
    
    
    public static void mostrarModelo(BPMNModel BPMN,LinkedHashMap<String, Integer> WFG) {
        int spaces = 0;
        mostrarNodo(spaces, BPMN.i, WFG);
    }

    public static void mostrarNodo(int spaces, Character t, LinkedHashMap<String, Integer> WFG) {

        for (int i = 0; i < spaces; i++) {
            System.out.print(" ");
        }

        if ((t == '1') || (t == '2') || (t == '3') || (t == '4') || (t == '5')) { //Modificar para comparar desde 1-9
            System.out.println("+ AND ");
        } else if ((t == 'A') || (t == 'B') || (t == 'C') || (t == 'D') || (t == 'E') || (t == 'F')) { //Modificar para comparar desde A-Z (en caso de que existan)
            System.out.println("+ XOR ");
        } else if (t == '!' || t == '"') {
            System.out.println("+ OR ");
        } else {
            System.out.println("+ " + t);
        }

        HashSet<Character> sucesores = Utils.successors(t, WFG);

        for (Character t2 : sucesores) {
            mostrarNodo(spaces + 3, t2, WFG);
        }

    }

    
}
