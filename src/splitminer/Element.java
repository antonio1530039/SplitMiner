/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

/**
 *
 * @author Antonio
 */
public class Element {
    
    Character Name;
    int cPosX;
    int cPosY;
    Character Antecesor;
    
    
    public Element(){
        this.Name = ' ';
        this.cPosX = 0;
        this.cPosY = 0;
        this.Antecesor = ' ';
    }
    
    public Element(Character name){
        this.Name = name;
        this.cPosX = 0;
        this.cPosY = 0;
        this.Antecesor = ' ';
    }
    
    
}
