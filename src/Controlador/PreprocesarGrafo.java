
package Controlador;

import splitminer.*;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PreprocesarGrafo {
    
    public LinkedHashMap<Character, LinkedList<Character>> parallelRelations = new LinkedHashMap<Character, LinkedList<Character>>();
    
    public LinkedHashMap<String, Integer> WFG;
    public LinkedList<Character> autoLoops = new LinkedList<Character>();
    
    
    public PreprocesarGrafo(LinkedHashMap<String, Integer> wfg, LinkedHashMap tracesList, BPMNModel BPMN){
        WFG = wfg;
        
        //SEGUNDO se buscan y eliminan los auloLoops: (a,a) en el grafo
        System.out.println("\n\t1.Removiendo AUTOLOOPS y SHORTLOOPS del grafo actual...");

        int numLoops = removeAutoLoops(BPMN.T);

        //TERCERO se buscan y eliminan los shortLoops: (a,x), (x,a) en el grafo, para una misma traza
        int shortLoops = removeShortLoops(tracesList);

        //CUARTO: CALCULAR LAS RELACIONES DE PARALELISMO
        System.out.println("\n\t2.Identificar y remover RELACIONES de PARALELISMO...");

        identifyParallelRelations();

        System.out.println("\t Grafo actual con las modificaciones previas (AUTOLOOP, SHORTLOOP, PARALLEL RELATIONS):");
        Utils.mostrarGrafo(2, WFG);
    }
 
     /*
   Identifica todos los nodos con AUTOLOOP, y remueve los edges del grafo
   
     */
    public int removeAutoLoops(LinkedList<Character> activityList) {
        //para cada actividad 'a', buscar si existe 'a'_'a' en el grafo y quitarlo
        int tam = activityList.size();
        Character current;
        String edge;

        for (Character task : activityList) {
            edge = task + "," + task;
            if (WFG.containsKey(edge)) {
                WFG.remove(edge);
                autoLoops.add(task);
                System.out.println("\t\t... Removiendo autoop (" + edge + ")");

            }
        }

        int autoloops = autoLoops.size();

        System.out.println("\n\t\tSe removieron '" + autoloops + "' AUTOLOOPs.\n");
        return autoloops;

    }
    
    
    

    /* 
     Identifica y remueve los shortLoops en el grafo. Asume que no hay autoloops
     */
    public int removeShortLoops(LinkedHashMap tracesList) {

        //para cada actividad 'a', buscar si existe 'a'_'x' y 'x'_'a' en el Grafo, siempre que a,x,a existe en una traza en la lista
        int shortloops = 0;

        String entryKey = null;
        Character first, second, third;
        ArrayList<Character> traza = null;

        for (int i = 0; i < tracesList.size(); i++) {
            //procesa cada una de las trazas
            traza = (ArrayList<Character>) tracesList.values().toArray()[i];

            for (int j = 0; j < traza.size() - 2; j++) {
                first = traza.get(j);
                second = traza.get(j + 1);
                third = traza.get(j + 2);

                if ((first == third) && (first != second)) {
                    WFG.remove(first + "," + second);
                    WFG.remove(second + "," + third);
                    // System.out.println("\t\t... Removiendo shorloop (" + first + "," + second + "," + first + ")");
                    shortloops++;
                }

            }

        }

        System.out.println("\n\t\tSe removieron '" + shortloops + "' SHORTLOOPs.\n");
        return shortloops;
    }
    
    
    /*

     */
    public int identifyParallelRelations() {
        //busca edges de la forma a,b y b,a, que cumplan con la relación: |a -> b| - |b -> a|
        //                                                                -------------------  < e
        //                                                                |a -> b| + |b -> a| 

        // recorre marca todos los casos (a,b) (b,a) en el grafo
        int numParallelRelations = 0;

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
        Map.Entry<String, Integer> entry = null;
        String edge = null;
        int freq1 = 0;
        int freq2 = 0;

        for (int i = 0; i < edges.size();) {
            entry = edges.get(i);
            edge = entry.getKey();
            freq1 = entry.getValue();

            String vals[] = edge.split(",");
            Character activity1 = vals[0].charAt(0);
            Character activity2 = vals[1].charAt(0);

            String edgeParalel = activity2 + "," + activity1;

            if (WFG.containsKey(edgeParalel)) {
                freq2 = WFG.get(edgeParalel);

                double div = (double) (freq1 - freq2) / (double) (freq1 + freq2);

                if (div < 0)//%%%%%agregado
                {
                    div *= -1.0;
                }

                //if(div < 0.3){
                if (div < 0.3) {
                    System.out.println("\t\t....expresion DIV = " + div);
                    System.out.println("\t\tArco removido =" + edge);
                    WFG.remove(edgeParalel);
                    WFG.remove(edge);
                    // }
                    // else{//%%%%%agregado

                    LinkedList<Character> values = null;
                    System.out.println("ACTIVIDAD 1:" + activity1 + "  ACTIVITY 2:" + activity2);
                    if (parallelRelations.containsKey(activity1)) {
                        values = parallelRelations.get(activity1);
                    } else {
                        values = new LinkedList<Character>();
                    }
                    values.add(activity2);
                    parallelRelations.put(activity1, values);

                    //al reves tambien aplica
                    if (parallelRelations.containsKey(activity2)) {
                        values = parallelRelations.get(activity2);
                    } else {
                        values = new LinkedList<Character>();
                    }
                    values.add(activity1);
                    parallelRelations.put(activity2, values);

                    numParallelRelations += 2;

                    edges.remove(i);
                    continue;
                }//%%%%%agregado
                i++;//%%%%%agregado

            } else {
                i++;
            }
        }

        System.out.println("\n\t\t" + numParallelRelations + " relaciones paralelas encontradas: " + parallelRelations);

        return numParallelRelations;

    }
    
    
}
