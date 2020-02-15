package calculatrice;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import autrevent.AutreEvent;
import autrevent.AutreEventListener;
import autrevent.AutreEventNotifieur;

public class Modele 
{

	private AutreEventNotifieur notifieur = new AutreEventNotifieur();
	private String texteSaisie="";
	private boolean effacer=false, boolOpSci=false, boolOp=false;
	private int parOuvrante=0, parFermante=0, changeOp=0;
	private String[] vars = {"0", "0", "0", "0", "0", "0", "0"};
	private String fonction1 = "", fonction2 = "", lastResult="";

	public enum Operation
	{
		PLUS("+"),
		MOINS("-"),
		MULTI("*"),
		DIV("/"),
		MOD("%");

		private String name;

		Operation(String name)
		{
			this.name=name;
		}

		public String toString()
		{
			return name;
		}
	}

	public enum OperationSci
	{
		COS("cos"),
		SIN("sin"),
		TAN("tan"),
		ARCCOS("arccos"),
		ARCSIN("arcsin"),
		ARCTAN("arctan"),
		RAC("sqrt"),
		LOG("log"),
		LN("ln"),
		ABS("|x|");

		private String name;

		OperationSci(String name)
		{
			this.name=name;
		}

		public String toString()
		{
			return name;
		}
	}

	public String analyse(String saisie) //fonction d'analyse
	{
		String res = saisie;

		if (res.contains("A") || res.contains("B") || res.contains("C") || res.contains("D") || res.contains("E") || res.contains("F") || res.contains("G") || res.contains("pi") || res.contains("e")) {
			res = searchVar(res); //appel de la fonction de recherche des variables
		}

		if (res.contains("-")) {
			res = searchNeg(res); //appel de la fonction de recherche de négatif
		}

		if (res.contains("(")) {
			int nbBrackets = stringOccur(res, "("); //recupère le nombre total de parenthèse
			res = searchBracket(res, nbBrackets); //appel de la fonction de calcul avec des parenthèses
		}else{
			res = searchFct(res); //appel de la fonction de recherche de factorielle
			res = searchPower(res); //appel de la fonction de recherche de puissance/exposant
			res = searchMultiDiv(res); //appel de la fonction de recherche de multiplication/division/modulo
			res = searchAddSous(res); //appel de la fonctnion de recherche d'addition et de soustraction
		}

		try{
			Double test = Double.parseDouble(res); //vérifie si le resultat à le bon type & format
			lastResult=res; //concerver le dernier resultat pour le reprendre par la suite
			setHistorique(saisie + " = " +res); //mettre a jour l'historique
			notifieur.diffuserAutreEvent(new AutreEvent(this, res)); //notifie la vue pour afficher le résultat
		}catch(Exception e){
			notifieur.diffuserAutreEvent(new AutreEvent(this, "erreur")); //notifie la vue en cas d'erreur
			effacer=true;
		}
		effacer=true;

		return res;
	}


	public String searchBracket(String saisie, int nbBrackets) { //fonction de recherche et d'analyse de parenthèse
		String[] calcs = saisie.split(" "); //on sépare la chaîne 
		String res, power = "none", calc_sci = "none"; 
		boolean br = false; //boolean pour vérifier la présence de paranthèse
  		int m_nbBrackets = nbBrackets; //nombre de parenthèses
		int m_countBrackets = 0; 
		for (int i = 0; i < calcs.length; i++) { //on parcourt toute la saisie
			if (calcs[i].contains("(")) { //si l'élément est une parenthèse ouvrante
				m_countBrackets++; //on incremente le compteur de parenthèse
				if (calcs[i].length() > 1)
					calc_sci = calcs[i].replace("(", "");
				if (m_countBrackets == m_nbBrackets) { // si la parenthèse ouvrante est la dernière de la saisie
					for (int j = i; j < calcs.length; j++) { // on parcourt la suite de la chaîne
						if (calcs[j].contains(")")) { // si l'élément est une parenthèse fermante
							res = searchFct(concatenationPartielle(calcs, i+1, j-1)); //on lance tous les calculs
							res = searchPower(res); 
							res = searchMultiDiv(res);
							res = searchAddSous(res);
							if (!calc_sci.equals("none")) {
								res = searchSci(calc_sci, res);
							}
							if (calcs[j].contains("^")) //si il y a un exposant sur la parenthèse
								power = calcs[j].replace(")^", ""); //on garde l'exposant 
							calcs[j] = res; //on stocke le résultat de la parenthèse
							if(!power.equals("none")) //si il y a un exposant 
								calcs[j] = exposant2(calcs[j],  power); //on calcul l'exposant
							for (int k = i; k<j; k++) {
								calcs[k] = " "; //on supprime de la saisie les éléments déjà utilisés
							}
							power = "none"; //on remets la puissance à "null"
							br = true; //si on a trouver une parenthèse
							calc_sci = "none";
							m_nbBrackets--; //on décremente le nombre de parenthèse dans la saisie
							break;
						}
					}
				}
			}
			if (br) { break; } //si on calculer une parenthèse on quitte la boucle
		}
		res = concatenation(calcs);//on cancatène le reste de la saisie

		if (m_nbBrackets !=0) { //si il y a encore des parenthèses
			return searchBracket(res, m_nbBrackets); //on rappel la fonction de recherche de parenthèses
		}else { //si il n'y a plus de parenthèse
			res = searchPower(res); //on lance tous les calculs sur le reste de la saisie
			res = searchMultiDiv(res);
			res = searchAddSous(res);
		}

		return res; //on retourne le résultat final
	}

	public String searchMultiDiv(String saisie) { //fonction de recherche de multiplication/division/modulo
		String[] calcs = saisie.split(" "); // sépare la chaine
		int tour = 0; //compteur de tours
		try {
			for (int i = 0; i < calcs.length; i++) { //on parcourtla saisie
				switch(calcs[i]) {
				  case "*": //si l'élément est une multiplication
				  	calcs[i] = this.multiplication(calcs[i-1], calcs[i+1]); //on effectue la multiplication
					calcs[i-1] = " "; //on supprime les éléments utilisés
					calcs[i+1] = " ";
					tour = 1; //on incrèmente le nombre de tour
				    break;
				  case "/":
				    calcs[i] = this.division(calcs[i-1], calcs[i+1]);
					calcs[i-1] = " ";
					calcs[i+1] = " ";
					tour = 1;
				    break;
				   case "%":
				    calcs[i] = this.modulo(calcs[i-1], calcs[i+1]);
					calcs[i-1] = " ";
					calcs[i+1] = " ";
					tour = 1;
				    break;
				  default:
				    break;
				} 
				if (tour == 1) { break; }else{ tour = 0;} //si on à fait un tour/calcul on quitte la boucle
			}
			String res = concatenation(calcs); //on concatène la saisie
			if (res.contains("*") || res.contains("/") || res.contains("%")) { //si il reste des calculs
				return searchMultiDiv(res); //on rappel la fonction de recherche 
			}

			return res; //on retourne le résultat final
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String searchAddSous(String saisie) { //fonction de recherche d'addition et de soustraction
		try {
			String[] calcs = saisie.split(" "); //on sépare la chaîne
			int tour = 0; //compteur de tours
			for (int i = 0; i < calcs.length; i++) { //on parcourtla saisie
				switch(calcs[i]) {
				  case "+": //si l'élément est une addition
				  	calcs[i] = this.addition(calcs[i-1], calcs[i+1]); //on effectue l'addition
					calcs[i-1] = " "; //on supprime les éléments utilisés
					calcs[i+1] = " ";
					tour = 1; //on incrémente le nombre de tours
				    break;
				  case "-":
				    calcs[i] = this.soustraction(calcs[i-1], calcs[i+1]);
					calcs[i-1] = " ";
					calcs[i+1] = " ";
					tour = 1;
				    break;
				  default:
				    break;
				} 
				if (tour == 1) { break; }else{ tour = 0;} //si on a fait un tour/calcul on quite la boucle

			}
			
			String res = concatenation(calcs); //on concaténe la saisie
			if (res.contains("+") || res.contains("- ")) { //si il reste des opération
				return searchAddSous(res); //on rappel la fonctiond de recherche
			}
			return res; //on retourne le résultat final
		}catch(Exception e){
			return "erreur";
		}
	}

	public String searchPower(String saisie) { //fonction de recherche d'exposant
		try {
			String[] calcs = saisie.split(" "); //on sépare la chaine
			for (int i = 0; i < calcs.length; i++) { //on parcourtla saisie
				if (calcs[i].contains("^") && calcs[i].length() >= 3) { //si l'élément est une puissance
					calcs[i] = this.exposant(calcs[i]); //on effectue la puissance
				} 
			}
			
			String res = concatenation(calcs); //on concaténe le calcul
			if (res.contains("^")) { //si il reste des puissances
				return searchPower(res); //on rappel la fonction
			}

			return res;
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String searchFct(String saisie) { //fonction de recherche de factorielle
		try{
			String[] calcs = saisie.split(" "); //on sépare la chaine
			for (int i = 0; i < calcs.length; i++) { //on parcourtla saisie
				if (calcs[i].contains("!")) { //si l'élément est une factorielle
					calcs[i] = this.factorielle(calcs[i].replace("!", "")); //on effectue la factorielle
				} 
			}

			String res = concatenation(calcs);
			if (res.contains("!")) {
				return searchFct(res);
			}

			return res;
		}catch(Exception e) {
			return "erreur";
		}
		

	}

	public String searchSci(String calc_sci, String nb) { //fonction de calcul scientifique
		double n = Double.parseDouble(nb); //on convertit le nombre en double
		switch(calc_sci) {
			  case "abs": 
			  	return Math.abs(n)+""; //valeur absolue
			  case "log":
			  	return Math.log10(n)+""; //logarithme base 10
			  case "ln":
			  	return Math.log(n)+""; //logarithme népérien
			  case "sin":
			  	return Math.sin(n)+""; //sinus
			  case "cos":
			  	return Math.cos(n)+""; //cosinus
			  case "tan":
			  	return Math.tan(n)+""; //tangente
			  case "arcsin":
			  	return Math.asin(n)+""; //arcSinus
			  case "arccos":
			  	return Math.acos(n)+""; //arcCosinus
			  case "arctan":
			  	return Math.atan(n)+""; //arcTangente
			  case "sqrt":
			  	return Math.sqrt(n)+""; //racine carré
			  default:
			    break;
			} 

			return "erreur";
	}

	public String searchVar(String saisie) { //fonction de recherche de variable
		try{
			String[] calcs = saisie.split(" "); //on sépare la chaine
			for (int i = 0; i < calcs.length; i++) {
				if (calcs[i].equals("A") || calcs[i].equals("B") || calcs[i].equals("C") || calcs[i].equals("D") || calcs[i].equals("E") || calcs[i].equals("F") || calcs[i].equals("G") || calcs[i].equals("e") || calcs[i].equals("pi")) {
					calcs[i] = changeVar(calcs[i]); //si c'est une variable on la remplace par ça valeur
				}
			}
			String res = concatenation(calcs); //on concatène le calcul
			return res;
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String searchNeg(String saisie) { //mettre les négatifs entre parenthèse (pour les exposants par ex)
		try {
			String[] calcs = saisie.split(" "); //on sépare la chaine
			boolean check = false, check2 = true;
			for (int i = 0; i < calcs.length; i++) { //on parcourtla saisie
				if (!calcs[i].equals("-") && calcs[i].contains("-")) { //si il y a un "-"
					String[] test = calcs[i].split(""); //on sépare la chaine par caractère
					String tmp = "";
					calcs[i] = "( -";
					for (int j = 0; j < test.length; j++) { //on parcourtla chaine
						if (test[j].equals("0") || test[j].equals("1") || test[j].equals("2") || test[j].equals("3") || test[j].equals("4") || test[j].equals("5") || test[j].equals("6") || test[j].equals("7") || test[j].equals("8") || test[j].equals("9")) {
							// si l'élement est un nombre
							if (check2) 
								calcs[i] = calcs[i] + test[j];
							else
								tmp = tmp + test[j];
						}else{ 	//si il n'est pas un nombre
							if (test[j].equals("-")) 
								check = true; 
							if (calcs[i].length() > 3) 
								check2 = false;
							if (check)
								tmp = tmp + test[j];
						}
					}
					tmp = tmp.replace("-", "");
					calcs[i] = calcs[i] + " )" + tmp;
				}
			}
			String res = concatenation(calcs);
			
			return res;
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String addition(String nb1, String nb2) { //fonction addition
		try {
			Double res = Double.parseDouble(nb1) + Double.parseDouble(nb2);
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String soustraction(String nb1, String nb2) { //fonction soustraction
		try {
			Double res = Double.parseDouble(nb1) - Double.parseDouble(nb2);
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String division(String nb1, String nb2) { //fonction division
		try {
			Double res = Double.parseDouble(nb1) / Double.parseDouble(nb2);
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String multiplication(String nb1, String nb2) { //fonction multiplication
		try {
			Double res = Double.parseDouble(nb1) * Double.parseDouble(nb2);
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String modulo(String nb1, String nb2) { //fonction modulo
		try {
			Double res = Double.parseDouble(nb1) % Double.parseDouble(nb2);
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
	}

	public String exposant(String nb) { //fonction exposant
		try {
			String s_calcs = nb.replace("^", " "); //je remplace le signe exposant par un espace
			String[] calcs = s_calcs.split(" "); //je splite la chaine 
			Double res = Math.pow(Double.parseDouble(calcs[0]), Double.parseDouble(calcs[1])); //j'effecture la puissance
			return res+""; //je retourne le résultat
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String exposant2(String nb, String power) { //fonction exposant en cas de parenthèse
		try {
			String m_power = power.replace("^", ""); //je supprime le signe exposant
			Double res = Math.pow(Double.parseDouble(nb), Double.parseDouble(m_power)); //j'effectue la puissance
			return res+"";
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String concatenation(String[] calcs) { //fonction de concaténation
		try {
			String saisie = "";
			for (int i = 0; i < calcs.length; i++) { //je parcoure le tableau
				if (!calcs[i].equals(" ") && !calcs[i].equals("")) { //si l'element n'est pas vide ou null
					saisie += calcs[i]+" "; //je le concatène au reste en le séparant avec un espace
				}
			}
			return saisie;
		}catch(Exception e) {
			return "erreur";
		}
		
	}


	public String concatenationPartielle(String[] calcs, int debut, int fin) { //fonction de concaténation partielle
		try {
			String saisie = "";
			for (int i = debut; i < fin+1; i++) { //je parcour seulement ce que je souaite concaténer
				if (!calcs[i].equals(" ") && !calcs[i].equals("")) { //si l'element n'est ni null ni vide
					saisie += calcs[i]+" "; //je le concaténe avec un espapce
				}
			}
			return saisie;
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String factorielle(String nb) { //fonction de calcul de factorielle
		try {
			Double res = Double.parseDouble("1"); //on initialise la resultat
			int m_nb = Integer.parseInt(nb); //on récupére le facteur de la factorielle
			if (m_nb > 0) { //si la factorielle n'est pas nulle
				for (int i = 1; i <= m_nb; i++) { //on parcour le nnombre de chiffre necessaire à la factorielle
					res = res * i; //on effectue les multiplications
				}
				return res+""; //on retourne le résultat
			}
			return res+""; //si la factorielle est nulle on retourne 1
		}catch(Exception e){
			return "erreur";
		}
	}


	public static final int stringOccur(String text, String string) { //fonction qui determine le nombre d'occurence d'une chaine dans une autre
    	return regexOccur(text, Pattern.quote(string));
	}

	public static final int regexOccur(String text, String regex) { //fonction qui determine le nombre d'occurence d'un pattern dans une chaine
	    Matcher matcher = Pattern.compile(regex).matcher(text); //on cherche les occurences du pattern dans la chaine
	    int occur = 0;
	    while(matcher.find()) { //tant qu'il y a une occurence
	        occur ++; //on incrémente le compteur
	    }
	    return occur;
	}

	public String changeVar(String s) //fonction de conversion des variables
	{
		switch(s) //on remplace une variable par sa valeur
		{
			case "A":
				return vars[0];
			case "B":
				return vars[1];
			case "C":
				return vars[2];
			case "D":
				return vars[3];
			case "E":
				return vars[4];
			case "F":
				return vars[5];
			case "G":
				return vars[6];
			case "pi":
				return "3.14159265";
			case "e":
				return "2.71828182";
			default:
			    break;
		}
		return "erreur";
	}

	public void setVar(String[] tab) { //fonction d'affectation des variables
		Double test;
		String[] m_tab = tab; //on récupére le tableau de variable
		int errCount = 0; //compteur d'erreur
		String[] varCheck = new String[tab.length]; //tableau de vérification des variables 
		for (int i = 0; i < tab.length; i++) { //on parcourt le tableau de variable
			if (m_tab[i].contains(",")){ //si la variable est décimal on remplace la ","" par un "."
				m_tab[i] = m_tab[i].replace(",", ".");
			}
			try {
				test = Double.parseDouble(m_tab[i]); //on convertit la valeur en double
				varCheck[i] = "true"; //l'affectation à reussi
				vars[i] = m_tab[i]; //on affecte la variable
			}catch(Exception e) {
				varCheck[i] = "false"; //l'affectation à échoué
				errCount++; //on augmante le nombre d'erreur
			}
		}	
		//pour que la vue notifie l'utilisation des erreurs
		notifieur.diffuserAutreEvent(new AutreEvent(this, varCheck)); //on notifie la vue du tableau de vérifiation
		
	}

	public void resetVar(){ //fonction de remise à zéro des variables
		for (int i = 0; i < vars.length; i++) {
			vars[i] = "0";
		}
	}

	public void stats(String[] liste){ //fonction de calcul statistique
		int cpt=0;
		Double[] res = new Double[6]; //tableau des résultats
		Double sum=0.0, tmp;
		for (int i = 0; i < liste.length; i++) { //on détermine le nombre d'element de la liste (max 20)
			if (!liste[i].equals("")) {
				cpt+=1;
			}
		}
		Double[] m_liste = new Double[cpt]; //on crée un nouveau tableau de la bonne taille
		for (int i = 0; i < liste.length; i++) { //on parcour
			if (!liste[i].equals("")) {
				m_liste[i] = Double.parseDouble(liste[i]);
				sum+=Double.parseDouble(liste[i]);
			}
		}

		m_liste = tri(m_liste);

		res[0] = sum / cpt;
		res[2] = m_liste[cpt-1] - m_liste[0];
		Double index=Double.parseDouble(cpt/4+"");
		res[3] = m_liste[arrondie(index + "")];
		index=index=Double.parseDouble(3*cpt/4+"");
		res[4] = m_liste[arrondie(index +"")];
		res[5] = res[4] - res[3];

		index=index=Double.parseDouble(cpt/2+"");
		if (cpt % 2 == 0) {
			res[1] = (m_liste[arrondie(index+"")] + m_liste[arrondie(index+"")-1]) / 2;
		}else{
			res[1] = m_liste[arrondie(index+"")];
		}

		notifieur.diffuserAutreEvent(new AutreEvent(this, res));
	}

	public Double[] tri(Double tab[]) {
		int longueur = tab.length;
		Double[] tableau = tab;
		Double tampon = 0.0;
		boolean permut;
 
		do {
			// hypothèse : le tableau est trié
			permut = false;
			for (int i = 0; i < longueur - 1; i++) {
				// Teste si 2 éléments successifs sont dans le bon ordre ou non
				if (tableau[i] > tableau[i + 1]) {
					// s'ils ne le sont pas, on échange leurs positions
					tampon = tableau[i];
					tableau[i] = tableau[i + 1];
					tableau[i + 1] = tampon;
					permut = true;
				}
			}
		} while (permut);

		return tableau;
	}

	public  int arrondie(String nbr) {
		String[] nbr1 = nbr.split("\\.");
		if(nbr1[1].equals("0")) {
			return Integer.parseInt(nbr1[0]);
		}else {
			return Integer.parseInt(nbr1[0])+1;
		}
	}

	public void calcFonction(String[] foncs, int xMin, int xMax) {
		try {
			setFonction(foncs);
			int j=0;
			int[] courbe = null;
			if(!fonction1.equals("") && !fonction2.equals("")) {
				courbe = new int[(Math.abs(xMax-xMin)+1)*2];
			}else{
				courbe = new int[Math.abs(xMax-xMin)+1];
			}
			if (!fonction1.equals("")) {
				String f1 = fonction1;
				for (int i = xMin; i <= xMax; i++) {
					courbe[j] = Integer.parseInt(round2f10(analyse(replaceX(f1, i))));
					j = j + 1;
				}
			}
			if (!fonction2.equals("")) {
				String f2 = fonction2;
				for (int i = xMin; i <= xMax; i++) {
					courbe[j] = Integer.parseInt(round2f10(analyse(replaceX(f2, i))));
					j = j + 1;
				}
			}
			if (courbe != null) {
				notifieur.diffuserAutreEvent(new AutreEvent(this, courbe));
			}
		}catch(Exception e) {
			notifieur.diffuserAutreEvent(new AutreEvent(this, "erreur fonction"));
		}
		
	}

	public void setFonction(String[] foncs) {
		try {
			if (!foncs[0].equals("")){
				fonction1 = foncs[0].replace(" ", "");
				for(Operation op : Operation.values()) {
					fonction1 = fonction1.replace(op.toString(), " "+op.toString()+" ");
				}
				fonction1 = fonction1.replace("(", "( ");
				fonction1 = fonction1.replace(")", " )");
			}
			
			if (!foncs[1].equals("")) {
				fonction2 = foncs[1].replace(" ", "");
				for(Operation op : Operation.values()) {
					fonction2 = fonction2.replace(op.toString(), " "+op.toString()+" ");
				}
				fonction2 = fonction2.replace("(", "( ");
				fonction2 = fonction2.replace(")", " )");
			}
		}catch(Exception e) {
			fonction1 = "";
			fonction2 = "";
		}
	}

	public void resetFonction() {
		fonction1 = "";
		fonction2 = "";
	}

	public String replaceX(String fonc, int x) {
		try {
			String[] calc = fonc.split(" ");
			for (int i = 0; i < calc.length; i++) {
				if (calc[i].equals("x")) {
					calc[i] = x+"";
				}
				if (calc[i].contains("x")) {
					if (calc[i].contains("^")){
						calc[i] = calc[i].replace("x", x+"");
					}else{
						String tmp = " * " + x;
						calc[i] = calc[i].replace("x", tmp);
					}
				}
			}
			return concatenation(calc);
		}catch(Exception e) {
			return "erreur";
		}
		
	}

	public String round2f10(String p_nb) {
		try {
			String nb;
			if (p_nb.contains(".")) {
				String[] sNb = p_nb.split("\\.");
				sNb[1] = sNb[1].replace(" ", "");
				String[] sNb2 = sNb[1].split("");
				if (sNb2.length < 2) {
					nb = sNb[0] + sNb2[0] + "0";
					return nb.replace(" ", "");
				}else{
					nb = sNb[0] + sNb2[0] + sNb2[1];
					return nb.replace(" ", "");
				}
			}else {
				nb = p_nb.replace(" ", "") + "00";
				return nb;
			}
		}catch(Exception e) {
			return "erreur";
		}
	}
 
	public void setHistorique(String calc) {
		String line;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();  
		try{
			br = new BufferedReader(new FileReader("historique.csv"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}	
			if (stringOccur(sb.toString(), ";") >= 20) {
				int i = sb.indexOf(";");
				sb.delete(0,i+1);
			}
		}catch(Exception e) { System.out.println("no historic");}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("historique.csv")));
			if (br != null) {
				writer.write(sb.toString());
			}


			writer.write(calc+";");
			writer.close();
		}		
		catch (Exception e) {
			System.out.println("error historic");
		}
	}

	public void getHistorique() {
		String line;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();  
		try{
			br = new BufferedReader(new FileReader("historique.csv"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}	
			notifieur.diffuserAutreEvent(new AutreEvent(this, sb));
		}catch(Exception e) { notifieur.diffuserAutreEvent(new AutreEvent(this, sb)); }
	}

	public void getIHistorique(int i) {
		String line;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();  
		try{
			br = new BufferedReader(new FileReader("historique.csv"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}	
			String[] lines = sb.toString().split(";");
			String tmp = lines[i].substring(0, lines[i].indexOf("=")-1);
			effacer=false;
			texteSaisie=tmp;
			notifieur.diffuserAutreEvent(new AutreEvent(this, tmp));
		}catch(Exception e) { notifieur.diffuserAutreEvent(new AutreEvent(this, "erreur")); }
	}

	public void resetHistorique() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("historique.csv")));
			writer.write("");
			writer.close();
			StringBuilder sb = new StringBuilder(); 
			notifieur.diffuserAutreEvent(new AutreEvent(this, sb));
		}catch(Exception e) {

		}
	}

	public void saveColor(Color[] color) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("save.csv")));
			writer.write(Integer.toString(color[0].getRGB())+"\n");
			writer.write(Integer.toString(color[1].getRGB())+"\n");
			writer.write(Integer.toString(color[2].getRGB())+"\n");
			writer.write(Integer.toString(color[3].getRGB())+"\n");
			writer.close();
		}catch(Exception e) {
		}
	}

	public void saisie(String s)
	{
		if(effacer==true)
		{
			texteSaisie="";
			effacer=false;
		}
		if(texteSaisie.length()>2)
		{
			for(Operation op : Operation.values())
			{
				if((op.toString().equals(s))||op.toString().charAt(0)==texteSaisie.charAt(texteSaisie.length()-1))
					changeOp++;
			}
		}
		if((changeOp==2)&&(s.charAt(0)!=texteSaisie.charAt(texteSaisie.length()-1)))
		{
			texteSaisie=texteSaisie.substring(0,texteSaisie.length()-2);
		}
		changeOp=0;

		for(OperationSci op : OperationSci.values())
		{
			if(op.toString().equals(s))
			{
				if(s.equals("|x|"))
					s="abs";
				boolOpSci=true;
				parOuvrante=parOuvrante+1;
			}
		}

		for(Operation op : Operation.values())
		{
			if(op.toString().equals(s))
			{
				boolOp=true;
			}
		}

		if(boolOpSci==true)
		{
			texteSaisie=texteSaisie + " " + s + "(";  
			boolOpSci=false;
		}
		else
		{
			if(texteSaisie.equals("") && boolOp==true)
			{
				if(lastResult.equals("erreur")==false && lastResult.equals("NaN")==false)
				{
					texteSaisie=lastResult + s;
					boolOp=false;
				}
			}
			else
			{
				boolOp=false;
				if(s.equals(")")&&parOuvrante>parFermante)
				{
					texteSaisie=texteSaisie + " )";
					parFermante++;
				}
				else
				{
					if(s.equals("("))
						parOuvrante++;

					if(((s.equals("x^2") || s.equals("x^y") || s.equals("x!")) && (texteSaisie.length()==0)) || s.equals(")"))
						texteSaisie=texteSaisie;
					else
					{
						if((texteSaisie.length()==0)||texteSaisie.charAt(texteSaisie.length()-1)=='.' || texteSaisie.charAt(texteSaisie.length()-1)=='^' && !s.equals("x^2"))
						{
							texteSaisie=texteSaisie + s;
						}
						else
						{
							String c= String.valueOf(texteSaisie.charAt(texteSaisie.length()-1));
							try {
								Integer.parseInt(c);
								if(s.equals("."))
								{
									texteSaisie=texteSaisie + s;
								}
								else
								{
									if(s.equals("x^2")||s.equals("x!"))
									{
										texteSaisie=texteSaisie + s.substring(1,s.length());;
									}
									else
									{
										if(s.equals("x^y"))
										{
											texteSaisie=texteSaisie + "^";
										}
										else
										{
											try{
												Integer.parseInt(s);
												texteSaisie=texteSaisie + s;
											} catch (NumberFormatException e){
												texteSaisie=texteSaisie + " " + s;
											}	
										}
									}
								}
							} catch (NumberFormatException e){
								if(!s.equals("x^2") && !s.equals("x^y") && !s.equals("x!"))
									texteSaisie=texteSaisie + " " + s;
								else
								{
									if(s.equals("x^2")||s.equals("x!"))
									{
										texteSaisie=texteSaisie + s.substring(1,s.length());;
									}
									else
										texteSaisie=texteSaisie + "^";
								}
							}
						}
					}	
				}
			}
		}
		notifieur.diffuserAutreEvent(new AutreEvent(this, texteSaisie));
	}
	
	public void resetSaisie()
	{
		texteSaisie="";
		parOuvrante=0;
		parFermante=0;
		boolOp=false;
		notifieur.diffuserAutreEvent(new AutreEvent(this, texteSaisie));
	}

	public void effaceSaisie()
	{
		if(texteSaisie.length()==1)
		{
			texteSaisie="";
			parOuvrante=0; parFermante=0;
		}
		else
		{
			if(texteSaisie.length()!=0)
			{
				if(texteSaisie.length()>=2 && texteSaisie.charAt(texteSaisie.length()-2)=='-')
					texteSaisie=texteSaisie.substring(0,texteSaisie.length()-2);
				else
				{
					if(texteSaisie.charAt(texteSaisie.length()-2)==' ')
					{
						if(texteSaisie.charAt(texteSaisie.length()-1)=='(')
						{
							parOuvrante=parOuvrante-1;
						}
						if(texteSaisie.charAt(texteSaisie.length()-1)==')')
							parFermante=parFermante-1;
						texteSaisie=texteSaisie.substring(0,texteSaisie.length()-2);
					}
					else
					{
						if(texteSaisie.charAt(texteSaisie.length()-1)=='(')
						{
							while(texteSaisie.charAt(texteSaisie.length()-1)!=' ')
							{
								texteSaisie=texteSaisie.substring(0,texteSaisie.length()-1);
							}
						}
						texteSaisie=texteSaisie.substring(0,texteSaisie.length()-1);
					}
				}
			}
		}
		notifieur.diffuserAutreEvent(new AutreEvent(this, texteSaisie));
	}

	public void close()
	{
		notifieur.diffuserAutreEvent(new AutreEvent(this,"OFF"));
	}

	
	public void addAutreEventListener(AutreEventListener listener) {
    	notifieur.addAutreEventListener(listener);
    }
      
    public void removeAutreEventListener(AutreEventListener listener) {
        notifieur.removeAutreEventListener(listener);
    }
}
