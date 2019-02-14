/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Antonio
 */
public class gJPanel extends JPanel {

    int ScreenWidth;
    int ScreenHeight;
    int radio;
    Character ElementSelected;
    HashMap<Character, Element> Elements;
    BPMNModel BPMN;

    public gJPanel(int width, int height, HashMap<Character, Element> elements, BPMNModel bpmn, String text) {
        Elements = elements;
        ScreenWidth = width;
        ScreenHeight = height;
        radio = ScreenWidth / 30;
        ElementSelected = ' ';
        BPMN = bpmn;
        
        JTextArea textField=new JTextArea();
        textField.setBounds(5, 5, ScreenWidth, ScreenWidth);
        textField.setText(text);
        textField.setFont(textField.getFont().deriveFont(20f));
        textField.setEditable(false);
        add(textField);
        
        setBackground(new Color(255, 255, 255));
        setSize(ScreenWidth, ScreenHeight);
        
        this.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent me) {
                clickAt(me.getX(), me.getY());
            }
            @Override
            public void mouseReleased(MouseEvent me) {
                ElementSelected = ' ';
            }
            @Override
            public void mouseEntered(MouseEvent me) {}
            public void mouseExited(MouseEvent me) {}
            @Override
            public void mouseClicked(MouseEvent me) {
                clickAt(me.getX(), me.getY());
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragElementSelected(e.getX(), e.getY());
            }
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Map.Entry<Character, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue();
            if (e.type.equals("Task")) {
                g.drawOval(e.cPosX, e.cPosY, radio, radio);
                g.drawString(e.Name.toString(), e.cPosX + (radio / 2), e.cPosY + (radio / 2));
            } else {
                g.setColor(Color.red);
                drawDiamond(g, e.cPosX + (radio / 2), e.cPosY + (radio / 2));
                g.setColor(Color.black);
                if (BPMN.Gxor.contains(e.Name)) {
                    g.drawString("Xor", e.cPosX + (radio / 2) - 3, e.cPosY + (radio / 2));
                } else if(BPMN.Gand.contains(e.Name)) {
                    g.drawString("And", e.cPosX + (radio / 2) - 3, e.cPosY + (radio / 2));
                }else{
                    g.drawString("Or", e.cPosX + (radio / 2) - 3, e.cPosY + (radio / 2));
                }
            }
            //Dibujar lineas
            if (!e.Antecesores.isEmpty()) {
                g.setColor(Color.black);
                for (Character antecesor : e.Antecesores) {
                    Element a = Elements.get(antecesor);
                    drawArrowLine(g, a.cPosX + (2 * (radio / 2)), a.cPosY + (radio / 2), e.cPosX, e.cPosY + (radio / 2), ScreenWidth / 300, ScreenWidth / 300);
                }
            }
        }
    }

    public void clickAt(int x, int y) { //Dada una posición x, y, verificar si se dio clic a dentro del radio de un elemento
        for (Map.Entry<Character, Element> entry : Elements.entrySet()) {
            Element e = entry.getValue(); //get the element
            if (x <= (e.cPosX + radio) && y <= (e.cPosY + radio) && x >= e.cPosX && y >= e.cPosY) {
                ElementSelected = e.Name;
                break;
            }
        }
    }

    public void dragElementSelected(int x, int y) {
        if (ElementSelected != ' ') {
            Elements.get(ElementSelected).cPosX = x - (radio / 2); //reasignar posicion al arrastrar mouse, para que el elemento quede en el centro del cursor
            Elements.get(ElementSelected).cPosY = y - (radio / 2);
            this.repaint();
        }
    }

    private void drawDiamond(Graphics g, int x, int y) {
        Polygon p = new Polygon();
        p.addPoint(x, y - (radio / 2));
        p.addPoint(x - (radio / 2), y);
        p.addPoint(x, y + (radio / 2));
        p.addPoint(x + (radio / 2), y);
        g.drawPolygon(p);
    }

    /**
     * procedimiento obtenido de stackoverflow
     * https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

}
