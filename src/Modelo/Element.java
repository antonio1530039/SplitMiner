/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.util.ArrayList;

/**
 *
 * @author Antonio
 */
public class Element {
    
    public Character Name;
    public int cPosX;
    public int cPosY;
    public ArrayList<Character> Antecesores;
    public String type;
    
    
    public Element(){
        this.Name = ' ';
        this.cPosX = 0;
        this.cPosY = 0;
        this.type = "";
        Antecesores = new ArrayList<>();
    }
    
    public Element(Character name){
        this.Name = name;
        this.cPosX = 0;
        this.cPosY = 0;
        this.type="";
        Antecesores = new ArrayList<>();
    }
    
    
}
