package calculatrice;

import javax.swing.JFrame;

public class Gui {

	public Gui() {
		//super("Dessin");
		Modele modele = new Modele();
		Controleur controleur = new Controleur(modele);
		View vue = new View(controleur,modele);
		vue.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		vue.pack();
		vue.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> { 
				new Gui(); 
		}); 

	}

}