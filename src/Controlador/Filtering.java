/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import splitminer.*;
import Modelo.BPMNModel;
import Modelo.Edge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author cti
 */
public class Filtering {
    
    LinkedHashMap<String, Integer> WFG;
    
    
    public Filtering( BPMNModel BPMN, double percentile, LinkedHashMap<String, Integer> wfg, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts){
        WFG = wfg;
        
        //NUEVOS CAMBIOS: FILTERING      
        System.out.println("\n\t3.FILTERING: percentil = '" + percentile + "'.");

        int removed = filtering(BPMN, percentile, firsts, lasts);
        System.out.println("\n\t\t" + removed + " edges fueron removidos.");

        System.out.println("\t Grafo actual (filtering):");
        Utils.mostrarGrafo(2, WFG);

    }
    
    
    public int filtering(BPMNModel BPMN, double percentile, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts ) {

        int freq1 = 0;
        int removed = 0;
        int inf = 10000;

        BPMN.i = (Character) firsts.toArray()[0];
        BPMN.o = (Character) lasts.toArray()[0];

        Map<Character, Integer> Cf = new HashMap<Character, Integer>();
        Map<Character, Integer> Cb = new HashMap<Character, Integer>();

        Set<Integer> F = new TreeSet<Integer>();

        Cf.put(BPMN.i, inf);
        Cb.put(BPMN.o, inf);

        Cf.put(BPMN.o, 0);
        Cb.put(BPMN.i, 0);

        for (Character t : BPMN.T) {
            if (t.equals(BPMN.i) || t.equals(BPMN.o)) //procesa todas las tasks, excepto la tarea inicial y final
            {
                continue;
            }

            Cf.put(t, 0);
            Cb.put(t, 0);

            //para la tarea t, calcular la más alta frecuencia de sus incoming edges y las más alta de sus outgoing edges
            int fi = maxFreqIncomingEdges(t);
            int fo = maxFreqOutgoingEdges(t);
            F.add(fi);
            F.add(fo);

            System.out.println(t + ": maxIn = " + fi + "; maxOut = " + fo);
        }

        System.out.println("F = " + F);

        int fth = calcularNpercentile(F, percentile);
        System.out.println("   ft = " + fth);

        Map<String, Character> Ei = new HashMap<String, Character>();
        Map<String, Character> Eo = new HashMap<String, Character>();

        discoverBestIncomingEdges(BPMN, Cf, Ei);
        discoverBestOutgoingEdges(BPMN, Cb, Eo);

        List<Map.Entry<String, Integer>> edgesMain = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edgesMain) {
            String key = entry.getKey();
            int fe = entry.getValue();

            if (!(Ei.containsKey(key) || Eo.containsKey(key) || fe > fth)) {
                WFG.remove(key);
                System.out.println("REMOVER************" + key);
                removed++;
            }
        }

        return removed;

    }
    
    //revisa todos los arcos *t para determinar cual de los arcos de entrada tiene la freq más grande
    private int maxFreqIncomingEdges(Character t) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        int maxFreq = 0;
        int freqIn = 0;

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (t == c1) {
                freqIn = entry.getValue();

                if (freqIn > maxFreq) {
                    maxFreq = freqIn;
                }
            }
        }

        return maxFreq;
    }
    
    //revisa todos los arcos t* para determinar cual de los arcos de salida tiene la freq más grande
    private int maxFreqOutgoingEdges(Character t) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        int maxFreq = 0;
        int freqOut = 0;

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (t == c0) {
                freqOut = entry.getValue();

                if (freqOut > maxFreq) {
                    maxFreq = freqOut;
                }
            }
        }

        return maxFreq;
    }
    
    private int calcularNpercentile(Set F, double percentile) {

        int n = F.size();

        double val = n * percentile;
        double techo = Math.ceil(val);
        double prom = 0.0;

        Object[] array = F.toArray();

        if (val < techo) {
            return (Integer) array[(int) techo - 1];
        } else {

            if (techo != 0) {
                prom = ((Integer) array[(int) techo - 1] + (Integer) array[(int) techo]) / 2.0;
            }

            return (int) prom;

        }

    }
    
    
    private void discoverBestIncomingEdges(BPMNModel BPMN, Map<Character, Integer> Cf, Map<String, Character> Ei) {

        LinkedList<Character> queue = new LinkedList<Character>();
        Set<Character> U = new TreeSet<Character>();

        for (Character t : BPMN.T) {
            if (t != BPMN.i) {
                U.add(t);
            }
        }

        System.out.println("Mapa Cf " + Cf);
        queue.add(BPMN.i);
        Character p = ' ';
        Character n = ' ';
        int fe = 0;

        int Cmax;

        while (queue.size() != 0) {
            p = queue.get(0);
            queue.remove(0);

            //calcular p*
            List<Edge> l = obtenerPout(p);

            for (Edge e : l) {
                n = e.tarjet;
                fe = e.freq;

                System.out.println("\t p = " + p + "; n = " + n);
                if (Cf.get(p) <= fe) {
                    Cmax = Cf.get(p);
                } else {
                    Cmax = fe;
                }

                if (Cmax > Cf.get(n)) {
                    Cf.put(n, Cmax);
                    Ei.put(p + "," + n, n);

                    //si 
                    if (!(queue.contains(n) && !U.contains(n))) {
                        U.add(n);
                    }
                }

                if (U.contains(n)) {
                    U.remove(n);
                    queue.add(n);
                }
            }
        }
    }
    
    
    private void discoverBestOutgoingEdges(BPMNModel BPMN, Map<Character, Integer> Cb, Map<String, Character> Eo) {

        LinkedList<Character> queue = new LinkedList<Character>();
        Set<Character> U = new TreeSet<Character>();

        for (Character t : BPMN.T) {
            if (t != BPMN.o) {
                U.add(t);
            }
        }

        queue.add(BPMN.o);
        Character p = ' ';
        Character n = ' ';
        int fe = 0;

        int Cmax;

        while (queue.size() != 0) {
            n = queue.get(0);
            queue.remove(0);

            //calcular *n
            List<Edge> l = obtenerPin(n);

            for (Edge e : l) {
                p = e.tarjet;
                fe = e.freq;

                if (Cb.get(n) <= fe) {
                    Cmax = Cb.get(n);
                } else {
                    Cmax = fe;
                }

                if (Cmax > Cb.get(p)) {
                    Cb.put(p, Cmax);
                    Eo.put(p + "," + n, p);

                    //si 
                    if (!(queue.contains(p) && !U.contains(p))) {
                        U.add(p);
                    }
                }

                if (U.contains(p)) {
                    U.remove(p);
                    queue.add(p);
                }
            }
        }
    }
    
    
    private List<Edge> obtenerPout(Character p) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
        List<Edge> l = new LinkedList<Edge>();

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (p == c0) {
                l.add(new Edge(c1, entry.getValue()));
            }
        }

        return l;
    }

    private List<Edge> obtenerPin(Character n) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
        List<Edge> l = new LinkedList<Edge>();

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (n == c1) {
                l.add(new Edge(c0, entry.getValue()));
            }
        }

        return l;
    }
    
}
