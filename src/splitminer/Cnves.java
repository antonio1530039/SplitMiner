/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JFrame;

/**
 *
 * @author Antonio
 */
public class Cnves extends JFrame {

    int ScreenWidth;
    int ScreenHeight;
    int PosX;
    int PosY;
    int radio;
    
    Character ElementSelected;

    int i = 0;
    HashMap<Character, Element> Elements;
    //Graph
    WFG WFG;
    //BPMN model
    BPMNModel BPMN;

    public Cnves(BPMNModel bpmn, WFG wfg) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ScreenWidth = (int) screenSize.getWidth();
        ScreenHeight = (int) screenSize.getHeight();

        BPMN = bpmn;
        WFG = wfg;
        Elements = new HashMap<>();

        //Initial position of the first element in the graph
        PosX = ScreenWidth / 15;
        PosY = ScreenHeight / 20;
        radio = ScreenWidth / 30;
        
        ElementSelected = ' ';

        super.setTitle("Model");
        super.setSize(ScreenWidth, ScreenHeight);
        super.setVisible(true);
        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent me) {
                 clickAt(me.getX(),me.getY());
            }

            public void mouseReleased(MouseEvent me) {
                ElementSelected = ' ';
            }

            public void mouseEntered(MouseEvent me) {}

            public void mouseExited(MouseEvent me) {}

            public void mouseClicked(MouseEvent me) {
                clickAt(me.getX(),me.getY());
            }
        });
        
        this.addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e) {
                dragElementSelected(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
            
        });
    }

    public HashMap<Character, Set<Character>> getAllSucesores() {
        HashMap<Character, Set<Character>> allSucesores = new HashMap<>();

        for (Character c : BPMN.T) {
            allSucesores.put(c, WFG.successors(c));
        }
        for (Character c : BPMN.Gxor) {
            allSucesores.put(c, WFG.successors(c));
        }
        for (Character c : BPMN.Gand) {
            allSucesores.put(c, WFG.successors(c));
        }

        System.out.println("Todos los sucesores: " + allSucesores.toString());
        return allSucesores;
    }

    public void start() {
        HashMap<Character, Set<Character>> allSucesores = getAllSucesores();
        for (Entry<Character, Set<Character>> entry : allSucesores.entrySet()) {
            Character nodo = entry.getKey();
            //Procesar nodo
            if (!Elements.containsKey(nodo)) {
                processElement(new Element(nodo));
                i++;
            }
            //Para cada sucesor del nodo, agregar su antecesor para crear la linea
            for (Character s : entry.getValue()) {
                if (!Elements.containsKey(s)) {
                    Element sucesor = new Element(s);
                    sucesor.Antecesores.add(nodo);
                    processElement(sucesor);
                    i++;
                } else {
                    Elements.get(s).Antecesores.add(nodo);
                }
            }
        }
    }

    public void processElement(Element e) {
        e.cPosX = PosX;
        e.cPosY = PosY;
        if (BPMN.T.contains(e.Name)) {
            e.type = "Task";
            BPMN.T.remove(e.Name);
        } else {
            e.type = "Gateway";
        }
        Elements.put(e.Name, e);
        PosX += ScreenWidth / 15;
        PosY += ScreenHeight / 20;

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Map.Entry<Character, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue();
            if (e.type.equals("Task")) {
                g.setColor(Color.white);
                g.fillOval(e.cPosX, e.cPosY, radio, radio);
                g.setColor(Color.black);
                g.drawString(e.Name.toString(), e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            } else {
                g.setColor(Color.black);
                g.fillRoundRect(e.cPosX, e.cPosY, radio, radio, radio / 4, radio / 4);
                g.setColor(Color.white);
                g.drawString(e.Name.toString(), e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            }
            //Dibujar linea
            if (!e.Antecesores.isEmpty()) {
                g.setColor(Color.black);
                for (Character antecesor : e.Antecesores) {
                    Element a = Elements.get(antecesor);
                    g.drawLine(a.cPosX + (2 * (radio / 2)), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2));
                }
            }
        }
    }
    
    public void clickAt(int x, int y){ //Dada una posición x, y, verificar si se dio clic a un elemento dadas sus posiciones
        for(Map.Entry<Character, Element> entry: Elements.entrySet()){
            Element e = entry.getValue(); //get the element
            if( x <= (e.cPosX+radio) && y <= (e.cPosY+radio) && x >= e.cPosX && y >= e.cPosY){
                ElementSelected = e.Name;
                break;
            }
        }
    }
    
    public void dragElementSelected(int x, int y){
        System.out.println("Dragging: " + ElementSelected);
        if(ElementSelected != ' '){
            Elements.get(ElementSelected).cPosX = x;
            Elements.get(ElementSelected).cPosY = y;
            this.repaint();
        }
    }

}
