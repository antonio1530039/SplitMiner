/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JFrame;

/**
 *
 * @author Antonio
 */
public class G_BuildGraphicModel extends JFrame {

    int ScreenWidth;
    int ScreenHeight;
    int PosX;
    int PosY;

    HashMap<Character, Element> Elements;
    //Graph
    WFG WFG;
    //BPMN model
    BPMNModel BPMN;

    public G_BuildGraphicModel(BPMNModel bpmn, WFG wfg) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ScreenWidth = (int) screenSize.getWidth();
        ScreenHeight = (int) screenSize.getHeight();

        BPMN = bpmn;
        WFG = wfg;
        Elements = new HashMap<>();

        //posicion inicial del primer elemento en el canvas
        PosX = ScreenWidth / 15;
        PosY = ScreenHeight / 3;
        
        buildModel();
        setTitle("Model");
        setSize(ScreenWidth, ScreenHeight);
        add(new G_JPanel(ScreenWidth, ScreenHeight, Elements, BPMN)); //Agregar el JPanel, mandando en su contructor los elementos necesarios para la graficacion de los elementos (Elements)
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void buildModel() {
        HashMap<Character, Set<Character>> allSucesores = getAllSucesores();
        for (Entry<Character, Set<Character>> entry : allSucesores.entrySet()) {
            Character nodo = entry.getKey();
            //Procesar nodo
            if (!Elements.containsKey(nodo)) {
                processElement(new Element(nodo));
            }
            //Para cada sucesor del nodo, agregar sus antecesores para crear las flechas
            for (Character s : entry.getValue()) {
                if (!Elements.containsKey(s)) {
                    Element sucesor = new Element(s);
                    sucesor.Antecesores.add(nodo);
                    processElement(sucesor);
                } else {
                    Elements.get(s).Antecesores.add(nodo);
                }
            }
        }
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
        
        if(PosX > ScreenWidth){
            PosY += ScreenHeight / 20; //salto en caso de exceder el limite del ancho de la pantalla
            PosX = ScreenWidth / 15; //posicion inicial de X
        }
            
    }

    
    
    

}
