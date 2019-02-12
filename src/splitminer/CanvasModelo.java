/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;

/**
 *
 * @author Antonio
 */
public class CanvasModelo extends Canvas{
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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
