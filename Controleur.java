package calculatrice;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import autrevent.AutreEvent;
import autrevent.AutreEventListener;
import autrevent.AutreEventNotifieur;

public class Controleur implements AutreEventListener {

	private Modele modele;

	public Controleur(Modele modele) {
		this.modele = modele;
	}
	
	public void actionADeclancher(AutreEvent event) 
	{
		if (event.getDonnee() instanceof String)  { //si la vue envoie une chaine
			String saisie = (String) event.getDonnee();
			if (saisie.equals("menuHist")){ //si c'est l'historique
				modele.getHistorique(); //on appel la fonction d'affectation
			}else{
				if (saisie.equals("menuReset")) { //si c'est le reset de l'historique
					modele.resetHistorique(); //on appel la fonction de reset
				}else{
					if(saisie.equals("resetFx")) //si c'est le reset des fonctions
						modele.resetFonction(); //on appel la fonction de reset
					else
						if(saisie.equals("resetVar")) //si c'est le reset des variables
							modele.resetVar(); //on appel la fonction de reset
						else
							modele.analyse(saisie); //sinon on appel l'analyse du calcul
				}
				
			}
		}

		if (event.getDonnee() instanceof Integer)  { //si la vue envoie un entrier
			modele.getIHistorique((int) event.getDonnee()); //on appel la fonction de récuperation de l'historique
		}

		if (event.getDonnee() instanceof JButton)  {  //si la vue envoie un bouton
			String saisie = ((JButton)event.getDonnee()).getText();
			if(saisie.equals("c")) //si c'est le bouton reset
			{
				modele.resetSaisie();
			}
			else
			{
				if(saisie.equals("DEL")) //si c'est le bouton supprimer
				{
					modele.effaceSaisie();
				}
				else
				{
					if(saisie.equals("OFF")) //si c'est le bouton éteindre
						modele.close();
					else
						modele.saisie(saisie); //sinon c'est la saisie
				}
			}

		}

		if (event.getDonnee() instanceof String[]) { //si la vue envoie un tableau de chaine
			String tab[] = (String[])event.getDonnee();
			if(tab.length==7) //si c'est un tableau de 7
				modele.setVar(tab); //c'est les variables
			else
				if(tab.length==4) //si c'est un tableau de 4
				{	//c'est les fonctions
					int xmin=Integer.parseInt(tab[2]);
					int xmax=Integer.parseInt(tab[3]);
					modele.calcFonction(tab,xmin,xmax);
				}
				else
					modele.stats(tab); //sinon c'est les statistiques
		}

		if (event.getDonnee() instanceof Color[]) { //si la vue envoie un tableau de couleur
			modele.saveColor((Color[])event.getDonnee());
		}

	}	
}
