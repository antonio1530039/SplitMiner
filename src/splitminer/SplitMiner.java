/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import Controlador.CrearModelo;
import Controlador.FilesManagement;
import Controlador.Filtering;
import Controlador.GenerarGrafo;
import Controlador.PostProcesamiento;
import Controlador.PreprocesarGrafo;
import Interfaz.gBuildGraphicModel;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author cti
 */
public class SplitMiner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final String filename = "Sepsis Cases.txt";
        double umbral = 0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
        LinkedHashMap<Integer, ArrayList<Character>> tracesList; //lista de trazas
        LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>(); //Grafo
        BPMNModel BPMN = new BPMNModel(); //Modelo BPMN

        FilesManagement f = new FilesManagement(BPMN);
        ///////
        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");

        try {
            if (filename.endsWith(".txt")) {
                tracesList = f.readDataInputTrazas(filename);
            } else if (filename.endsWith(".csv")) {
                tracesList = f.readDataInput(filename);
            } else {
                System.out.println("El tipo de archivo de entrada no es valido...EXIT");
                return;
            }
        } catch (Exception e) {
            System.out.println("El archivo '" + filename + "' no se puede abrir. Exit");
            return;
        }

        System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
        }

        
        
        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        GenerarGrafo generarGrafo = new GenerarGrafo();

        generarGrafo.computeGraph(tracesList, WFG);
 
        
        ///////
        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        PreprocesarGrafo preprocesarGrafo = new PreprocesarGrafo(WFG, tracesList, BPMN);

        
        
        Filtering filtering = new Filtering(BPMN, umbral, WFG, generarGrafo.firsts, generarGrafo.lasts);

        
        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        CrearModelo crearModelo = new CrearModelo(BPMN, generarGrafo.firsts, generarGrafo.lasts, WFG, preprocesarGrafo.parallelRelations);
        
        /////////
        
        
        System.out.println("\nPASO 5: POST-PROCESAMIENTO");
      
        //g1.postProcesamiento(BPMN);
        
        PostProcesamiento postprocesamiento = new PostProcesamiento(BPMN, WFG, preprocesarGrafo.autoLoops);

        System.out.println("\nPASO 6: Mostrando modelo grafico");
        
        gBuildGraphicModel c = new gBuildGraphicModel(BPMN, WFG, "Model's notation: " + postprocesamiento.notation);
        
        System.out.println("Notacion al final: " + postprocesamiento.notation);

    }

}
