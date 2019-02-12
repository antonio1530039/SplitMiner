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

        super.setTitle("Model");
        super.setSize(ScreenWidth, ScreenHeight);
        super.setBackground(new java.awt.Color(197, 225, 165));
        super.setVisible(true);
        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();
                System.out.println("PRESED AT: X:" + x + " Y:" + y);
            }

            public void mouseReleased(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();
                System.out.println("RELEASED AT:" + x + " Y:" + y);
            }

            public void mouseEntered(MouseEvent me) {

                int x = me.getX();
                int y = me.getY();
                System.out.println("ENTERED AT:" + x + " Y:" + y);
            }

            public void mouseExited(MouseEvent me) {

            }

            public void mouseClicked(MouseEvent me) {
                int x = me.getX();
                int y = me.getY();
                System.out.println("X:" + x + " Y:" + y);
            }
        });
        
        this.addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("Draging mouse");
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
            
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

}
