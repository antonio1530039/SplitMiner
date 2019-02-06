import java.io.*;
import java.util.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Npercentile{


   public int calcularNpercentile(Set F, double percentile){
   
   
     
   //Paso 1. Ordenamiento del conjunto F
   
      Set<Integer> sortedF = new TreeSet<Integer>(F);
      System.out.println("Sorted Set: " + sortedF);
   
   // Paso2. Multiplicar  percentil por el numero total de valores 
      double index= F.size()* percentile;
        
      long iPart = (long) index;
      double fPart = index - iPart;
   
   
   
   
   //Paso 3. Contar el numero de posiciones de izquierda a derecha segun lo marca el index
   
   // si el index resulto con fracción considerar el elemento del conjunto en la posicion marcada por el indice redondeado
   // si el indice resulto sin fraccion se considera elemento del conjunto marcado por el indice junto con sucesor inmediato en promedio
   
      int nper1=0,nper2=0;
   
      if(fPart==0) {
         nper1=(int)sortedF.toArray()[(int) index-1];
         nper2=(int)sortedF.toArray()[(int) index];
         int average= (nper1+nper2)/2;
      
       System.out.println("Value_Average: " + average);
       return average;
      }
      else{ 
         int a = (int) Math.round(index);
         nper1=(int)sortedF.toArray()[a-1];
         System.out.println("Value: " + nper1);
         return nper1;
      }
   
   }
   
   

   public static void main(String[] args)throws Exception {
   
      Set<Integer> F  = new LinkedHashSet<Integer>();
      F.add(20);
      F.add(30);
      F.add(40);
      F.add(60);
      F.add(10);
      F.add(20);
      F.add(50);
      F.add(50);
      F.add(50);
      F.add(50);
   
      Npercentile n1=new Npercentile();
   
      int sal=n1.calcularNpercentile(F,0.5);
   
   }

}