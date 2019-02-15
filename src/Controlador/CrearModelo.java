/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.BPMNModel;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class CrearModelo {
    
    static Character xorGate = 'A';
    static Character andGate = '1';
    
    LinkedHashMap<String, Integer> WFG;
    
    LinkedHashMap<Character, LinkedList<Character>> ParallelRelations;
    
    
    public CrearModelo(BPMNModel BPMN, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts, LinkedHashMap<String, Integer> wfg, LinkedHashMap<Character, LinkedList<Character>> parallelRelations){
        WFG = wfg;
        ParallelRelations = parallelRelations;
        BPMN.i = (Character) firsts.toArray()[0];
        BPMN.o = (Character) lasts.toArray()[0];
        //SEPTIMO, SE DETECTAN SPLITS 'AND, 'XOR'. ALGORITMOS 2, 3 Y 4 DEL PAPER
        System.out.println("\n\t1.Crear el Modelo BPMN");
        initProcess(BPMN, BPMN.i);
    }
    
    
    public void initProcess(BPMNModel BPMN, Character a) {

        Algorithm2(BPMN, a);

        for (Character task : BPMN.T) {
            if (task == BPMN.i || task == BPMN.o) {
                continue;
            }
            Algorithm2(BPMN, task);
        }

        //System.out.println("\n\n" + BPMN.ANDs);    
    }
    
    
    public void Algorithm2(BPMNModel BPMN, Character a) {

        System.out.println("\n\t-Iniciando ALGORITMO 2 con TAREA = '" + a + "'");

        HashSet<Character> sucesores = new HashSet<Character>();

        sucesores = Utils.successors(a, WFG);

        System.out.println("\n\t\t- Procesando SUCESORES de '" + a + "': " + sucesores);

        //Dada la lista de sucesores, para cada uno, encontrar 
        //su 'futuro', esto es cuales de <los otros sucesores> mantienen una relación de concurrencia con el. 
        Object[] sucessorsArray = sucesores.toArray();

        LinkedList<Character> concurrentTasks = new LinkedList<Character>();
        HashSet<Character> future = null;
        HashSet<Character> cover = null;

        LinkedHashMap<Character, HashSet<Character>> C = new LinkedHashMap<Character, HashSet<Character>>();
        LinkedHashMap<Character, HashSet<Character>> F = new LinkedHashMap<Character, HashSet<Character>>();

        //C = { [t, A] }, t in sucesores, A es subconjunto de los nodos   COVERTURA
        //F = { [t, B] }, t in sucesores, B es subconjunto de los nodos   FUTURO
        for (Object task : sucessorsArray) {        //procesa cada sucesor
            future = new HashSet<Character>();  //vacio
            cover = new HashSet<Character>();
            cover.add((Character) task);
            C.put((Character) task, cover);          //la misma tarea

            concurrentTasks = ParallelRelations.get((Character) task);

            if (concurrentTasks == null) {
                System.out.println("\n\t\t\tNo hay tareas concurrentes para '" + task + "'");
                System.out.println("\t\t\tNo hay FUTURO de '" + task + "'");
                F.put((Character) task, future);   //futuro vacio
                continue;
            }

            for (Character taskp : concurrentTasks) {// verifica que las tareas concurrentes esten en la lista de sucesores

                if (sucesores.contains(taskp)) {
                    future.add(taskp);
                }

            }

            F.put((Character) task, future);

            System.out.println("\n\t\t\tTareas concurrentes de '" + task + "' son: " + concurrentTasks);
            System.out.println("\t\t\tFuturo de '" + task + "' es: " + future);

        }

        //Del conjunto de edges Em, en la llamada al Algoritmo 3 y 4, no considerar los edges a*
        //cuando finaliza el algoritmo 3 y 4, se vuelven a considerar para Em los a*
        //System.out.println("\n\n\t\t\tC: " + C); //{(t1, Ct1), (t2, Ct2), ...}
        //System.out.println("\t\t\tF: " + F); //{(t1, Ft1), (t2, Ft2), ...} 
        //BPMN = {
        //    T = set of tasks
        //    i = initial task
        //    o = final task
        //    G+ = and gateways
        //    G* = xor gateways
        //    G0 = or gateways
        //    Em = set of edges (a,b), a != o, b != i, a,b a task or a gateway
        //REMOVER LOS EDGES (a,x): a* 
        System.out.println("\n\n\t\t\tRemoviendo EDGES (" + a + ", ...)");
        Utils.removerEdges(a, WFG);
        Utils.mostrarGrafo(3, WFG);

        System.out.println("\n\n\t\t\tC: " + C); //{(t1, Ct1), (t2, Ct2), ...}
        System.out.println("\t\t\tF: " + F); //{(t1, Ft1), (t2, Ft2), ...} 
        System.out.println("\t\t\tsucesores: " + sucesores);
        int i = 0;
        while (sucesores.size() > 1) {
            i++;
            System.out.println("\n\t\t\tITERACION " + i);

            System.out.println("\n\t\t\t-Iniciando ALGORITMO 3...");
            Algorithm3(BPMN, sucesores, C, F);
            System.out.println("\n\t\t\t-Fin de ALGORITMO 3...");

            System.out.println("\t\t\t-Iniciando ALGORITMO 4...");
            // System.out.println("HOLA");
            // System.out.println("\t\t-BPMN"+BPMN+"\t\t-sucesores"+ sucesores+"\t\tC"+C+"\t\tF"+ F);
            Algorithm4(BPMN, sucesores, C, F);
            System.out.println("\n\t\t\t-Fin de ALGORITMO 4...");
        }

        System.out.println("\n\t\tFIN de ALGORITMO 2 para TAREA = '" + a + "'. Sucesores MODIFICADOS K = " + sucesores);
        System.out.println("\n\t\t AGREGANDO Nuevos EDGES:");

        //agregar los nuevos edges con las task en sucesores
        String key = null;
        for (Character task : sucesores) {
            key = a + "," + task;
            WFG.put(key, 1);
        }

        System.out.println("\n\t\t Grafo resultante:");

         Utils.mostrarGrafo(3, WFG);

    }
    
    
    public void Algorithm3(BPMNModel BPMN, HashSet<Character> sucesores, LinkedHashMap<Character, HashSet<Character>> C, LinkedHashMap<Character, HashSet<Character>> F) {

        HashSet<Character> X = null;

        do {
            X = new HashSet<Character>();
            HashSet<Character> Fs = null;
            HashSet<Character> Fk1 = null;
            HashSet<Character> Fk2 = null;

            HashSet<Character> Cu = null;
            HashSet<Character> Ck2 = null;

            for (Character k1 : sucesores) {
                Cu = C.get(k1);
                Fs = F.get(k1);
                Fk1 = F.get(k1);
                for (Character k2 : sucesores) {
                    /*if(k1 == k2) 
                  continue;*/
                    Fk2 = F.get(k2);
                    //System.out.println("\tk1:\t"+k1+"\tk2:\t"+k2+"\tFk1:\t"+Fk1+"\tFk2:\t"+Fk2); //{(t1, Ct1), (t2, Ct2), ...}

                    if ((Utils.iguales(Fk1, Fk2)) && (k1 != k2)) {

                        X.add(k2);
                        Ck2 = C.get(k2);
                        Utils.union(Cu, Ck2);
                    }

                }

                if (X.size() > 0) {
                    X.add(k1);
                    break;
                }

            }
            if (X.size() > 0) {
                System.out.println("\n\t\t\t Nueva Compuerta XOR. VALOR DE X = " + X);
                BPMN.Gxor.add(xorGate);
                BPMN.XORs.put(xorGate, X);

                //se agregan los nuevos edges al grafo, para todo x in X.
                String key = null;
                for (Character task : X) {
                    key = xorGate + "," + task;
                    WFG.put(key, 1);
                    //actualiza C y F
                    C.put(task, new HashSet<Character>());
                    F.put(task, new HashSet<Character>());

                }

                C.put(xorGate, Cu);
                F.put(xorGate, Fs);

                System.out.println("\n\t\t\t C: " + C);
                System.out.println("\t\t\t F: " + F);

                sucesores.add(xorGate);
                Utils.resta(sucesores, X);
                xorGate++;
            }
        } while (X.size() > 0);

    }
    
    
     public void Algorithm4(BPMNModel BPMN, HashSet<Character> sucesores, LinkedHashMap<Character, HashSet<Character>> C, LinkedHashMap<Character, HashSet<Character>> F) {

        HashSet<Character> A = null;
        HashSet<Character> Cu = null;
        HashSet<Character> Fi = null;

        HashSet<Character> CFk1 = null;
        HashSet<Character> CFk2 = null;

        HashSet<Character> Ck2 = null;

        A = new HashSet<Character>();
        for (Character k1 : sucesores) {

            Cu = C.get(k1);
            Fi = F.get(k1);
            CFk1 = new HashSet<Character>();
            Utils.union(CFk1, Cu);
            Utils.union(CFk1, Fi);

            for (Character k2 : sucesores) {
                if (k1 == k2) {
                    continue;
                }
                CFk2 = new HashSet<Character>();
                Utils.union(CFk2, C.get(k2));
                Utils.union(CFk2, F.get(k2));

                if (Utils.iguales(CFk1, CFk2)) {
                    //System.out.println("\tk1:\t"+k1+"\tk2:\t"+k2+"\tCFk1:\t"+CFk1+"\tCFk2:\t"+CFk2); //{(t1, Ct1), (t2, Ct2), ...}

                    A.add(k2);
                    Utils.union(Cu, C.get(k2));
                    Utils.interseccion(Fi, F.get(k2));

                    //System.out.println("\tA:\t"+A+"\tCu:\t"+Cu+"\tFi:\t"+Fi); //{(t1, Ct1), (t2, Ct2), ...}
                }

            }
            if (A.size() > 0) {
                A.add(k1);
                //System.out.println("A:"+A);
                break;
            }

        }

        //System.out.println("SIZE:"+A.size());
        if (A.size() > 0) {
            System.out.println("\n\t\t Nueva compuerta AND. VALOR DE A = " + A);

            BPMN.Gand.add(andGate);
            BPMN.ANDs.put(andGate, A);

            String key = null;
            for (Character task : A) {
                key = andGate + "," + task;
                WFG.put(key, 1);
                //actualiza C y F.
                C.put(task, new HashSet<Character>());
                F.put(task, new HashSet<Character>());

            }

            C.put(andGate, Cu);
            F.put(andGate, Fi);

            System.out.println("\n\t\t\t C: " + C);
            System.out.println("\t\t\t F: " + F);

            sucesores.add(andGate);
            Utils.resta(sucesores, A);
            andGate++;
        }

    }
    
}
