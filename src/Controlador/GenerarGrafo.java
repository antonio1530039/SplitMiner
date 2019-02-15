/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class GenerarGrafo {
    
    public LinkedHashSet<Character> firsts = new LinkedHashSet<Character>();
    public LinkedHashSet<Character> lasts = new LinkedHashSet<Character>();

    public LinkedHashMap<Character, Integer> fi = new LinkedHashMap<Character, Integer>();
    public LinkedHashMap<Character, Integer> la = new LinkedHashMap<Character, Integer>();
    
    
    public void computeGraph(LinkedHashMap tracesList, LinkedHashMap<String, Integer> WFG) {
        String key = null;
        Character first, second;

        for (int i = 0; i < tracesList.size(); i++) {
            //procesa cada una de las listas
            ArrayList<Character> traza = (ArrayList<Character>) tracesList.values().toArray()[i];

            int tam = traza.size();

            firsts.add(traza.get(0));
            lasts.add(traza.get(tam - 1));

            if (!fi.containsKey(traza.get(0))) {
                fi.put(traza.get(0), 1);
            } else {
                int freq = fi.get(traza.get(0));
                fi.put(traza.get(0), freq + 1);
            }
            if (!la.containsKey(traza.get(tam - 1))) {
                la.put(traza.get(tam - 1), 1);
            } else {
                int freq = la.get(traza.get(tam - 1));
                la.put(traza.get(tam - 1), freq + 1);
            }

            //***************//
            for (int j = 0; j < tam - 1; j++) {
                first = traza.get(j);
                second = traza.get(j + 1);
                key = first + "," + second;

                if (!WFG.containsKey(key)) {
                    WFG.put(key, 1);
                } else {
                    int freq = WFG.get(key);
                    WFG.put(key, freq + 1);
                }
            }
        }

        //el grafo esta calculado, para cada par (a,b): freq
        System.out.println("\n\t1.Calculando tareas inciales y finales...");
        System.out.println("\t\tTarea(s) inicial(es): " + firsts);
        System.out.println("\t\tTarea(s) final(es): " + lasts);

        System.out.println("\t\tTarea(s) inicial(es): " + fi);
        System.out.println("\t\tTarea(s) final(es): " + la);

        //el modelo es WFG, es sobre el que se aplican todos los procesos de la Fig. 2. 
        //PRIMERO, se muestra el ESTADO INICIAL (a).
        System.out.println("\n\t2.Grafo calculado. Mostrando ARCOS y FRECUENCIA:");
        Utils.mostrarGrafo(2, WFG);
        return;

    }

}
