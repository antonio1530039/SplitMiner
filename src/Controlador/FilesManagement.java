package Controlador;

import splitminer.*;
import Modelo.BPMNModel;
import java.io.*;
import java.util.*;

//Esta clase lee el archivo.csv del log de eventos y construye el conjunto de trazas de la forma
//(a,b,c,d)
//(a,b,e,f)
//(a,b,c)


public class FilesManagement  {
  
   static BPMNModel BPMN;
   
   
   public FilesManagement(BPMNModel bpmn){
       BPMN = bpmn;
   }
   
   public static LinkedHashMap<Integer,ArrayList<Character>> readDataInput(String filename) throws Exception{
   
      ArrayList<ArrayList<String>> bitacora  = new ArrayList<ArrayList<String>>();
   
      int index;
      int i = 0;
      String inputLine = null;
      BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));
      	
      if(filename.contains(".csv")){
         inputLine = rd.readLine();                    //first line is the header, next lines are the data
         if( inputLine != null ){                      //determinar #columnas y crear ese nuemero de listas (vectores para los valores por columna)
            //String [] temp = inputLine.split(";");
           
           String [] temp = inputLine.split(";|,");// solo utilizar en caso de tener files con espacio
            for ( i = 0; i < temp.length; i++)
               bitacora.add(new ArrayList<String>());
         }  
         
        
   
            //para todas las lineas subsecuentes, leer los datos por columnas
         do{ 
           
            String [] temp = inputLine.split(";|,");
            for(i = 0; i < temp.length; i++)
               bitacora.get(i).add(temp[i].trim());
               
         }while((inputLine = rd.readLine()) != null);
      }
      else{
         System.out.println("\tEste modulo solo procesa archivos CSV");
         i = -1;
      }
      
      rd.close();
      
      //procede a obtener la lista de actividades
      
      int numCols = i;
      
      LinkedHashMap<String,Character> activityList = new LinkedHashMap<String,Character>();
      char value;
      //ascii code in literal a    
      int s = 97;
      String strVal = null;
      ArrayList<String> listValues = null;
      
      int k = 0;
      while(!bitacora.get(k).get(0).equals("Activity") && (k < numCols)){
         k++;
      }
      
      if( k == numCols){
         System.out.println("\tLos datos de entrada no contienen una columna 'Activity'");
         return null;
      }
      
      listValues = bitacora.get(k);   //recupera la lista 'Activity' y su tamaño
      int sizeList = listValues.size();
      
      for (int j = 1; j < sizeList; j++){
         value = (char)(s);
         String activityName = listValues.get(j);
         if (!(activityList.containsKey(activityName))) {
            //System.out.println(activityName+"  ID:"+value);
            activityList.put(activityName,value);
            s = s + 1;
         }                           
      }  
      
      System.out.println("\nSe han detectado todos los nombres de las actividades:");
      
      //recupera el set de tasks T
      Set<Map.Entry<String,Character>> tasks =  activityList.entrySet();
      int tam = activityList.size();
      Character task;
      
      for(Map.Entry<String,Character> entry : tasks) {
         task = entry.getValue();
         BPMN.T.add(task);
      }
   
          
      //procede a calcular la lista de trazas
      LinkedHashMap<Integer,ArrayList<Character>> tracesList = new LinkedHashMap<Integer,ArrayList<Character>>();
      ArrayList<Character> traces = new ArrayList<Character>();
      
      String key;
      int ID=0,IDnext=0; 
      String colName = "";
      ArrayList<String> listCASE_ID = null;
      ArrayList<String> listACTIVITY = null;
   
      int tamList = bitacora.get(0).size();
      
      //recupera la lista de CASE_ID y ACTIVITY
      for (k = 0; k < numCols; k++){
         if(bitacora.get(k).get(0).equals("Case ID"))
            listCASE_ID = bitacora.get(k);
         else if (bitacora.get(k).get(0).equals("Activity"))
            listACTIVITY = bitacora.get(k);
         else 
            continue;
      }
      
      ID = Integer.parseInt(listCASE_ID.get(1));
      //recorre las listas
      for (int j = 1; j < tamList; j++){
          
         IDnext = Integer.parseInt(listCASE_ID.get(j)); 
         
         if ( IDnext != ID){
            tracesList.put(ID,traces);
            ID = IDnext;
            traces = new ArrayList<Character>();
         } 
         
         key = listACTIVITY.get(j);
         value = activityList.get(key);
         traces.add(value);    
      }
      
      tracesList.put(ID,traces);  //la ultima
      showDataInfo(activityList,tracesList);
              
      System.out.println("\n\t2. Trazas detectadas. ");
      
      
      
   
      return tracesList;
   
        
                                     
                      
   }
   
   public static void showDataInfo(LinkedHashMap<String,Character> activityList,LinkedHashMap<Integer,ArrayList<Character>> tracesList){
   
      System.out.println("\n\t1. Datos del dataset ");
   
             
      System.out.println("\t --Actividades--  " );
   
      for (Map.Entry<String,Character> entry1 : activityList.entrySet()) {
         System.out.println("\t\t" + entry1.getKey() + " - " + entry1.getValue());
      }
       
       
      List<ArrayList<Character>> uno = new ArrayList<ArrayList<Character>>(tracesList.values());
      uno.get(0); 
       
      
   
      int min=uno.size();    
      int max=-1;  
      int suma=0; 
      int total=0;
      
      for (Map.Entry<Integer,ArrayList<Character>> entry : tracesList.entrySet()) {
         int t=entry.getValue().size();
         total=total+t;
         if(t<min){
            min=t; 
         }else{
            if(max<t) 
               max=t;    
         }
              
         suma=suma+t;   
      }
      float average=(float)suma/tracesList.size();
      System.out.println("\n\t # Actividades: " + activityList.size());
      System.out.println("\t # Trazas: " + tracesList.size()); 
      System.out.println("\t # Eventos: "+total);   
      System.out.println("\t Minimo de eventos por traza: " + min);
      System.out.println("\t Maximo de eventos por traza:  " + max);
      System.out.println("\t Promedio del tamanio de traza: " + average);
   
   
   
   
   }
   
   public static LinkedHashMap<Integer,ArrayList<Character>> readDataInputTrazas(String filename) throws Exception{
   
      LinkedHashMap<Integer,ArrayList<Character>> tracesList = new LinkedHashMap<Integer,ArrayList<Character>>();
      LinkedHashMap<String,Character> activityList = new LinkedHashMap<String,Character>();
      
       
      int ID = 0;
      
      String inputLine = null;
      BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));
   
    
      ArrayList<Character> traza = null;
    
      
      
     /*PARA MANEJAR TRAZAS*/ 
      /*while( (inputLine = rd.readLine()) != null){
         traza = new ArrayList<Character>();
         String [] temp = inputLine.split(";");
         for (int i = 0; i < temp.length; i++){
            Character task = temp[i].trim().charAt(0);
            traza.add(task);
            activityList.put(""+task, task);
         }
         tracesList.put(ID,traza);  //la ultima
         ID++;
      } */
         
      //
     
     
    //CONVERTIR ACTIVIDADES A TRAZAS
    
      int s=97;
      while((inputLine = rd.readLine()) != null)
      { 
         traza = new ArrayList<Character>();
        //String [] temp = inputLine.split(";|,| "); solo en caso de tener archivos con espacio
         String [] temp = inputLine.split(";");
      
             
         for (int i = 0; i < temp.length; i++){
         
            Character task=(char)(s);
            String clave=""+temp[i].trim();                                 
            if (!(activityList.containsKey(clave))) {
               activityList.put(""+temp[i].trim(),task);
               traza.add(task);
               s=s+1;
            }else
            {
               Character value=activityList.get(clave);
            
            // System.out.println("CLAVE: "+clave+"   VALOR:"+value);
            
            
               traza.add(value);
            }
                         
         }
         tracesList.put(ID,traza);  //la ultima
         ID++;
      
           
      }
      
      
      showDataInfo(activityList,tracesList);    
                    
      System.out.println("\n\t1. Trazas detectadas.");
                       
      rd.close();
      
      //recupera el set de tasks T
      Set<Map.Entry<String,Character>> tasks =  activityList.entrySet();
      int tam = activityList.size();
      Character task;
      
      for(Map.Entry<String,Character> entry : tasks) {
         task = entry.getValue();
         BPMN.T.add(task);
      }
      
         
      return tracesList;                               
                      
   }


 
   
      
   public static void main(String[] args)throws Exception {
      Console c = System.console();
      LinkedHashMap<Integer,ArrayList<Character>> tracesList;
          //LOGS REALES
        //  final String filename = "Event logs/Logs Reales/Hospital Billing - Event Log/Hospital Billing.txt";
        //final String filename = "Event logs/Logs Reales/Sepsis Cases - Event Log/sepsi2.txt";
               
          //final String filename = "Event logs/Logs Reales/Sistema Electoral/electoral.csv";

       final String filename = "P1.txt";

       /*
                [A,c] - 1
		[A,h] - 1
		[1,A] - 1
		[1,b] - 1
		[a,1] - 1
		[B,e] - 1
		[B,g] - 1
		[b,B] - 1
		[C,d] - 1
		[C,g] - 1
		[c,C] - 1
		[d,f] - 1
		[D,c] - 1
		[D,f] - 1
		[e,D] - 1
		[g,d] - 1
		[h,d] - 1
       
     
       

      
       TRAZAS 3  - SALIDA SIN JOINS
                [A,b] - 1X
		[A,f] - 1X
		[a,A] - 1X
		[1,c] - 1X
		[1,d] - 1X
		[b,1] - 1X
		[c,e] - 1X
		[d,e] - 1X
		[f,d] - 1X
       
       
       
       TRAZAS 3 / SALIDA CON JOINS (Simbolos raros como ! " son ors)
                [A,b] - 1X
		[A,f] - 1X
		[a,A] - 1X
		[1,c] - 1
		[b,1] - 1
		[c,!] - 1
		[d,!] - 1
		[!,e] - 1
		[1,"] - 1
		[f,"] - 1
		[",d] - 1
      
       
       
       
       
       
       */
       
       
         //  final String filename = "Logs/T1_ejemplo.txt";
      //final String filename = "Logs/trazas.txt";
      //
     //final String filename = "Logs/sepsi2.txt";
    //final String filename = "Logs/logPREP2.csv";
   //final String filename = "Logs/seguros_exe_correcto.csv";-- revisar si tiene espacio en blanco,; o ,
      //double umbral =0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
      double umbral =0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
      
      System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");
   
      try{
         if (filename.endsWith(".txt"))
            tracesList = readDataInputTrazas(filename);
         else if (filename.endsWith(".csv"))
            tracesList = readDataInput(filename);
         else{
            System.out.println("El tipo de archivo de entrada no es valido...EXIT");
            return;
         }        
      }
      catch(Exception e){
         System.out.println("El archivo '" + filename + "' no se puede abrir. Exit");
         return;
      }
      
      
      //System.out.println("\t2. Mostrando el conjunto de tareas T: " + BPMN.T);
      
      System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
      for (Map.Entry<Integer,ArrayList<Character>> entry : tracesList.entrySet()) {
         System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
      }
      
      System.out.println("\n");
     
      System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");
     
      //WFG g1 = new WFG();
      
      //g1.computeGraph(tracesList);
      
      System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");
   
      //g1.preProcesarGrafo(tracesList, BPMN, umbral);
      
      System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");
      
      //g1.crearModeloBPMN(BPMN);
      
      System.out.println("\nPASO 5: POST-PROCESAMIENTO");
      
      //g1.postProcesamiento(BPMN);
      
      System.out.println("\nPASO 6: MODELO PRELIMINAR");
   
      //g1.mostrarModelo(BPMN);
   
   
   }




}

