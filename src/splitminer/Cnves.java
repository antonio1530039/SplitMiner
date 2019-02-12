/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

/**
 *
 * @author Antonio
 */
public class Cnves extends JFrame implements MouseListener{

    int width;
    int height;

    /////////
    int PosX;
    int PosY;
    LinkedHashMap<Character, Element> Elements;
    //Graph
    WFG WFG;
    //BPMN model
    BPMNModel BPMN;

    public Cnves(BPMNModel bpmn, WFG wfg) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();
        
        BPMN = bpmn;
        WFG = wfg;
        Elements = new LinkedHashMap<>();

        //Initial position of the first element in the graph
        //PosX = width / 50;
        //PosY = height / 5;
        PosX = width/50;
        PosY = height / 5;

        super.setTitle("Model");

        super.setSize(width, height);

        super.setVisible(true);

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);

    }
    
    
    public void start(){
        Element next = new Element(BPMN.i);
        while(next != null){
            next = FindNext(next);
        }
        System.out.println("Elements: " + Elements.toString());
        
    }

    public Element FindNext(Element e) {
        Element nextElement = new Element();
        
        //Define the posX and y of the actual element
        e.cPosX = PosX;
        e.cPosY = PosY;
        
        //define the type of element
        if(BPMN.T.contains(e.Name)){ //Es tarea 
            drawTask(PosX, PosY);//Graficar tarea
            PosX += width/25;
            Elements.put(e.Name, e);
            BPMN.T.remove(e.Name);
            //Get next
            Character next = sucesor(e.Name);
            if(next != null){
                nextElement.Name = next;
                nextElement.Antecesor = e.Name;
                return nextElement;
            }
        }else{//gateway
            drawGateway(PosX, PosY);
            PosX += width/25;
            Elements.put(e.Name, e);
            resolve(e.Name);
            
            
        }
        return null;
    }
    
    //para el símbolo de entrada que es una compuerta, explora cada rama     
    public void resolve(Character gate){
        HashSet<Character> sigs = WFG.successors(gate);
        for (Character t : sigs) {
            FindNext(new Element(t));
            Character sucesor = sucesor(t);
            while(sucesor != null){
                FindNext(new Element(sucesor));
                sucesor = sucesor(sucesor);
            }
        }
    }

    
    
    //encuentra el sucesor del nodo dado, la cual es una tarea y se espera que solo tenga un solo sucesor  
    public Character sucesor(Character task) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.WFG.entrySet());
        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c0 = vals[0].charAt(0);
            Character c1 = vals[1].charAt(0);
            if (task == c0) {
                return c1;
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics g) {
        //g.fillOval(width/60, height/5, width/50, width/50); //the size of the circle is given by the width of the screen ;;; width /50 = 1 r of the circle
    }

    public void drawTask(int x, int y) {
        this.getGraphics().fillOval(x, y, width/50, width/50);
    }
    
    public void drawGateway(int x, int y){
        this.getGraphics().fillRect(x, y, width/25, height/25);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("Click");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Pressed");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("Released");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println("Mouse entered");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        System.out.println("Mouse exited");
    }

}
