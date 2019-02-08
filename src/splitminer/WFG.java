package splitminer;

import java.io.*;
import java.util.*;

//Esta clase construye el Grafo Dirigido Ponderado e identifica los selfbucles, shortbucles  y la propiedad de concurrencia
public class WFG {

    List<Character> autoLoops = new LinkedList<Character>();
    LinkedHashMap<Character, LinkedList<Character>> parallelRelations = new LinkedHashMap<Character, LinkedList<Character>>();
    LinkedHashMap<String, Integer> WFG = new LinkedHashMap<String, Integer>();

    Set<Character> firsts = new LinkedHashSet<Character>();
    Set<Character> lasts = new LinkedHashSet<Character>();

    /**
     * ***
     */
    LinkedHashMap<Character, Integer> fi = new LinkedHashMap<Character, Integer>();
    LinkedHashMap<Character, Integer> la = new LinkedHashMap<Character, Integer>();

    /**
     * ***
     */
    static Character xorGate = 'A';
    static Character andGate = '1';

    //Set de visita; en este, se encuentran todos los nodos del grafo
    HashMap<Character, Integer> nodosVisitados = new HashMap<>();


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

    public int filtering(BPMNModel BPMN, double percentile) {

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

    public void computeGraph(LinkedHashMap tracesList) {

        WFG = new LinkedHashMap<String, Integer>();

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
        mostrarGrafo(2);

        return;

    }

    public void preProcesarGrafo(LinkedHashMap tracesList, BPMNModel BPMN, double percentile) {

        //SEGUNDO se buscan y eliminan los auloLoops: (a,a) en el grafo
        System.out.println("\n\t1.Removiendo AUTOLOOPS y SHORTLOOPS del grafo actual...");

        int numLoops = removeAutoLoops(BPMN.T);

        //TERCERO se buscan y eliminan los shortLoops: (a,x), (x,a) en el grafo, para una misma traza
        int shortLoops = removeShortLoops(tracesList);

        //CUARTO: CALCULAR LAS RELACIONES DE PARALELISMO
        System.out.println("\n\t2.Identificar y remover RELACIONES de PARALELISMO...");

        identifyParallelRelations();

        System.out.println("\t Grafo actual con las modificaciones previas (AUTOLOOP, SHORTLOOP, PARALLEL RELATIONS):");
        mostrarGrafo(2);

        //NUEVOS CAMBIOS: FILTERING      
        System.out.println("\n\t3.FILTERING: percentil = '" + percentile + "'.");

        int removed = filtering(BPMN, percentile);
        System.out.println("\n\t\t" + removed + " edges fueron removidos.");

        System.out.println("\t Grafo actual (filtering):");
        mostrarGrafo(2);

        //System.exit(0);
    }

    public void crearModeloBPMN(BPMNModel BPMN) {

        BPMN.i = (Character) firsts.toArray()[0];
        BPMN.o = (Character) lasts.toArray()[0];

        //SEPTIMO, SE DETECTAN SPLITS 'AND, 'XOR'. ALGORITMOS 2, 3 Y 4 DEL PAPER
        System.out.println("\n\t1.Crear el Modelo BPMN");
        initProcess(BPMN, BPMN.i);

    }

    public void postProcesamiento(BPMNModel BPMN) {
        //OCTAVO, SE REMUEVEN COMPUERTAS DUPLICADAS 
        //todas las compuertas XOR y AND se han detectado. Ahora, eliminar compuertas repetidas.
        System.out.println("\n\t1.Remover compuertas duplicadas ANDs");
        removeDuplicateGates(BPMN);
        System.out.println("\t  Resultado:");
        mostrarGrafo(2);

        System.out.println("\n\t1.Remover compuertas duplicadas XORs");
        removeDuplicateGatesXOR(BPMN);
        System.out.println("\t  Resultado:");
        mostrarGrafo(2);

        //Y SE DETECTAN 'JOINS'
        System.out.println("\n\t2.Detectar JOINS (pendiente de realizar)");
        detectarJoins(BPMN);
        // System.out.println("\t  Resultado:");
        mostrarGrafo(2);

        //Y SE REINTEGRAN 'AUTOLOPS'
        System.out.println("\n\t3.Re-integrar autolops al modelo final");
        reintegraALoops(BPMN);
        System.out.println("\t  Resultado:");
        mostrarGrafo(2);

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

    public void removeDuplicateGates(BPMNModel BPMN) {

        //para dos símbolos de compuerta, revisar si sus conjuntos son iguales
        Collection<Map.Entry<Character, HashSet<Character>>> list2 = BPMN.ANDs.entrySet();
        //Collection<HashSet<Character>> list = BPMN.ANDs.values();

        // System.out.println("***LISTA 2:"+BPMN.ANDs.entrySet());
        List<Map.Entry<Character, HashSet<Character>>> list = new ArrayList<Map.Entry<Character, HashSet<Character>>>(list2);
        int i = 0;
        while (i < list.size()) {
            Map.Entry<Character, HashSet<Character>> entry1 = list.get(i);
            //ver si hay alguna repetida
            int j = i + 1;
            while (j < list.size()) {
                Map.Entry<Character, HashSet<Character>> entry2 = list.get(j);

                if (iguales(entry1.getValue(), entry2.getValue())) {
                    //compuertas repetidas, 'c' y 'cr'eliminar una de ellas del grafo

                    //1. remueve todos los arcos 'cr'*
                    removerEdges(entry2.getKey());
                    //2. en todos los arcos *'cr', reemplaza 'cr' por 'c'
                    remplazarEdges(entry2.getKey(), entry1.getKey());

                    list.remove(j);
                } else {
                    j++;
                }

            }
            i++;
        }

    }

    public void removeDuplicateGatesXOR(BPMNModel BPMN) {

        //para dos símbolos de compuerta, revisar si sus conjuntos son iguales
        Collection<Map.Entry<Character, HashSet<Character>>> list2 = BPMN.XORs.entrySet();
        //Collection<HashSet<Character>> list = BPMN.ANDs.values();

        // System.out.println("***LISTA 2:"+BPMN.ANDs.entrySet());
        List<Map.Entry<Character, HashSet<Character>>> list = new ArrayList<Map.Entry<Character, HashSet<Character>>>(list2);
        int i = 0;
        while (i < list.size()) {
            Map.Entry<Character, HashSet<Character>> entry1 = list.get(i);
            //ver si hay alguna repetida
            int j = i + 1;
            while (j < list.size()) {
                Map.Entry<Character, HashSet<Character>> entry2 = list.get(j);

                if (iguales(entry1.getValue(), entry2.getValue())) {
                    //compuertas repetidas, 'c' y 'cr'eliminar una de ellas del grafo

                    //1. remueve todos los arcos 'cr'*
                    removerEdges(entry2.getKey());
                    //2. en todos los arcos *'cr', reemplaza 'cr' por 'c'
                    remplazarEdges(entry2.getKey(), entry1.getKey());

                    list.remove(j);
                } else {
                    j++;
                }

            }
            i++;
        }

    }

    LinkedList<Character> Gands = new LinkedList<>();
    LinkedList<Character> Gors = new LinkedList<>();
    LinkedList<Character> Gxors = new LinkedList<>();

    public void detectarJoins(BPMNModel BPMN) {

        System.out.println("\t\tDetectando joins y creando noatación...");

        //Lo siguiente es a manera de prueba.....................
        WFG.clear();
        /*
        //Modelo 1
        WFG.put("A,b", 1);
        WFG.put("A,f", 1);
        WFG.put("a,A", 1);
        WFG.put("1,c", 1);
        WFG.put("1,d", 1);
        WFG.put("b,1", 1);
        WFG.put("c,e", 1);
        WFG.put("d,e", 1);
        WFG.put("f,1", 1);
        
        BPMN.Gand.clear();
        BPMN.Gor.clear();
        BPMN.Gxor.clear();
        
        BPMN.Gand.add('1');
        BPMN.Gxor.add('A');
        
        
        BPMN.T.clear();
        BPMN.T.add('a');
        BPMN.T.add('b');
        BPMN.T.add('f');
        BPMN.T.add('c');
        BPMN.T.add('d');
        BPMN.T.add('e');
        
        BPMN.i = 'a';
        /*
        Notation:  a XOR{  b, f} AND{  c, d} e
		[A,b] - 1
		[A,f] - 1
		[a,A] - 1
		[1,c] - 1
		[1,d] - 1
		[b,B] - 1
		[f,B] - 1
		[B,1] - 1
		[c,2] - 1
		[d,2] - 1
		[2,e] - 1
        */

        
        
        
        
        /*
        //Modelo 2
        WFG.put("1,b", 1);
        WFG.put("1,c", 1);
        WFG.put("A,1", 1);
        WFG.put("A,e", 1);
        WFG.put("a,A", 1);
        WFG.put("b,d", 1);
        WFG.put("c,d", 1);
        WFG.put("e,d", 1);

        
        BPMN.Gand.clear();
        BPMN.Gor.clear();
        BPMN.Gxor.clear();
        
        BPMN.Gand.add('1');
        BPMN.Gxor.add('A');
        

        
        BPMN.T.clear();
        BPMN.T.add('a');
        BPMN.T.add('b');
        BPMN.T.add('c');
        BPMN.T.add('e');
        BPMN.T.add('d');
        BPMN.i = 'a';
        
        /*
        Notation:  a XOR{  AND{  b, c}, e} d
		[1,b] - 1
		[1,c] - 1
		[A,1] - 1
		[A,e] - 1
		[a,A] - 1
		[b,2] - 1
		[c,2] - 1
		[2,d] - 1
		[b,B] - 1
		[e,B] - 1
		[B,d] - 1
        
        
        */
 
 
 
 
 
 
        
        
        
         /*
        //Modelo 3
        WFG.put("a,b", 1);
        WFG.put("b,c", 1);
        WFG.put("A,d", 1);
        WFG.put("A,h", 1);
        WFG.put("c,A", 1);
        WFG.put("d,e", 1);
        WFG.put("B,f", 1);
        WFG.put("B,g", 1);
        WFG.put("e,B", 1);
        WFG.put("g,f", 1);
        WFG.put("h,d", 1);

        
        
        BPMN.Gand.clear();
        BPMN.Gor.clear();
        BPMN.Gxor.clear();
        
        
        
        BPMN.Gxor.add('A');
        BPMN.Gxor.add('B');
        
        
        BPMN.T.clear();
        BPMN.T.add('a');
        BPMN.T.add('b');
        BPMN.T.add('c');
        BPMN.T.add('h');
        BPMN.T.add('d');
        BPMN.T.add('e');
        BPMN.T.add('g');
        BPMN.T.add('f');
        BPMN.i = 'a';
        /*
        Notation:  a b c XOR{ , h} d e XOR{ , g} f
		[a,b] - 1
		[b,c] - 1
		[A,h] - 1
		[c,A] - 1
		[d,e] - 1
		[B,g] - 1
		[e,B] - 1
		[A,C] - 1
		[h,C] - 1
		[C,d] - 1
		[B,D] - 1
		[g,D] - 1
		[D,f] - 1        
        */
 
 
        
        
        
        
        
        
        /*
        //Modelo 4
        WFG.put("a,b", 1);
        WFG.put("A,c", 1);
        WFG.put("A,h", 1);
        WFG.put("b,A", 1);
        WFG.put("c,d", 1);
        WFG.put("d,e", 1);
        WFG.put("B,f", 1);
        WFG.put("B,g", 1);
        WFG.put("e,B", 1);
        WFG.put("g,f", 1);
        WFG.put("h,i", 1);
        WFG.put("i,j", 1);
        WFG.put("j,e", 1);

        BPMN.Gand.clear();
        BPMN.Gor.clear();
        BPMN.Gxor.clear();

        BPMN.Gxor.add('A');
        BPMN.Gxor.add('B');
        
        BPMN.T.clear();
        BPMN.T.add('a');
        BPMN.T.add('b');
        BPMN.T.add('c');
        BPMN.T.add('d');
        BPMN.T.add('e');
        BPMN.T.add('h');
        BPMN.T.add('i');
        BPMN.T.add('j');
        BPMN.T.add('g');
        BPMN.T.add('f');
        BPMN.i = 'a';
        
        /* 
        
        Notation:  a b XOR{  c d, h i j} e XOR{ , g} f
		[a,b] - 1
		[A,c] - 1
		[A,h] - 1
		[b,A] - 1
		[c,d] - 1
		[B,g] - 1
		[e,B] - 1
		[h,i] - 1
		[i,j] - 1
		[d,C] - 1
		[j,C] - 1
		[C,e] - 1
		[B,D] - 1
		[g,D] - 1
		[D,f] - 1
        
        */
    
        /*
        //Modelo 5
        WFG.put("1,b", 1);
        WFG.put("1,c", 1);
        WFG.put("a,1", 1);
        WFG.put("A,d", 1);
        WFG.put("A,e", 1);
        WFG.put("b,A", 1);
        WFG.put("e,f", 1);
        WFG.put("f,1", 1);
        WFG.put("c,A", 1);

        
        
        BPMN.Gand.clear();
        BPMN.Gor.clear();
        BPMN.Gxor.clear();

        BPMN.Gand.add('1');
        BPMN.Gxor.add('A');
 

        BPMN.T.clear();
        BPMN.T.add('a');
        BPMN.T.add('b');
        BPMN.T.add('c');
        BPMN.T.add('d');
        BPMN.T.add('e');
        BPMN.T.add('f');
        BPMN.i = 'a';
        */
        
        
        //.....................
        ///
        

        Gands.addAll(BPMN.Gand);
        Gxors.addAll(BPMN.Gxor);
        Gors.addAll(BPMN.Gor);

        JoinsFinder jf = new JoinsFinder(BPMN, this);
        
        String notation = jf.findNotation();
        System.out.println("Notation: " + notation);
        
    }


    //Encontrar el numero de edges entrantes ( *a )
    public int getNumberEdgesToA(Character a) {
        int i = 0;
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            if (a == entry.getKey().split(",")[1].charAt(0)) {
                i++;
            }
        }
        return i;
    }

    public void reintegraALoops(BPMNModel BPMN) {
        ArrayList<String> keys = new ArrayList<String>();
        Iterator<Map.Entry<String, Integer>> iter = WFG.entrySet().iterator();
        try {
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                String key = entry.getKey();

                if (autoLoops.size() > 0) {

                    for (Character car : autoLoops) {

                        String first = "" + key.charAt(0);
                        String second = "" + key.charAt(2);
                        String clave = "";

                        if (first.indexOf("" + car) > -1) {
                            iter.remove();
                            first = "@" + first;
                            clave = first + "," + second;
                            keys.add(clave);
                            break;
                        } else {
                            if (second.indexOf("" + car) > -1) {
                                iter.remove();
                                second = "@" + second;
                                clave = first + "," + second;
                                keys.add(clave);
                                break;
                            }

                        }

                    }
                }
            }
        } catch (IllegalStateException exeption) {
            System.out.println(" Error de indice en un array");
        }
        for (String cl : keys) {

            WFG.put(cl, 1);

        }

    }

    public void Algorithm2(BPMNModel BPMN, Character a) {

        System.out.println("\n\t-Iniciando ALGORITMO 2 con TAREA = '" + a + "'");

        HashSet<Character> sucesores = new HashSet<Character>();

        sucesores = successors(a);

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

            concurrentTasks = parallelRelations.get((Character) task);

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
        removerEdges(a);
        mostrarGrafo(3);

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

        mostrarGrafo(3);

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

                    if ((iguales(Fk1, Fk2)) && (k1 != k2)) {

                        X.add(k2);
                        Ck2 = C.get(k2);
                        union(Cu, Ck2);
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
                resta(sucesores, X);
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
            union(CFk1, Cu);
            union(CFk1, Fi);

            for (Character k2 : sucesores) {
                if (k1 == k2) {
                    continue;
                }
                CFk2 = new HashSet<Character>();
                union(CFk2, C.get(k2));
                union(CFk2, F.get(k2));

                if (iguales(CFk1, CFk2)) {
                    //System.out.println("\tk1:\t"+k1+"\tk2:\t"+k2+"\tCFk1:\t"+CFk1+"\tCFk2:\t"+CFk2); //{(t1, Ct1), (t2, Ct2), ...}

                    A.add(k2);
                    union(Cu, C.get(k2));
                    interseccion(Fi, F.get(k2));

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
            resta(sucesores, A);
            andGate++;
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
    public void removerEdges(Character a) {

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

    public void removerEdges(String edge) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();

            if (key.equals(edge)) {
                WFG.remove(key);
            }
        }

    }

    //reemplaza 'a' por 'b' en todos los edges *a    
    public void remplazarEdges(Character a, Character b) {

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

    public void mostrarGrafo(int numTabs) {
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            for (int i = 0; i < numTabs; i++) {
                System.out.print("\t");
            }
            System.out.println("[" + entry.getKey() + "]" + " - " + entry.getValue());
        }
    }

//all nodes following 'task', given the current pruened WFG
    public HashSet<Character> successors(Character task) {

        HashSet<Character> sucesores = new LinkedHashSet<Character>();

        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c = vals[0].charAt(0);

            if (task == c) {
                sucesores.add(vals[1].charAt(0));
            }

        }

        return sucesores;
    }
    
    
    //all nodes before 'task', given the current pruened WFG
    public HashSet<Character> antecessors(Character task) {

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

    public void mostrarModelo(BPMNModel BPMN) {

        int spaces = 0;
        mostrarNodo(spaces, BPMN.i);
    }

    public void mostrarNodo(int spaces, Character t) {

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

        HashSet<Character> sucesores = successors(t);

        for (Character t2 : sucesores) {
            mostrarNodo(spaces + 3, t2);
        }

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

}
