package calculatrice;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import autrevent.AutreEvent;
import autrevent.AutreEventListener;
import autrevent.AutreEventNotifieur;
import java.awt.event.ActionEvent;
import java.lang.Object;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class View extends JFrame implements AutreEventListener
{
	private JScrollPane s = new JScrollPane();
	private JButton tabBouton[]=new JButton[43];
	private JTextField varText[] = new JTextField[7];
	private JLabel varLabel[] = new JLabel[7];
	private JTextField tabListe[] = new JTextField[40];
	private JTextField histListe[] = new JTextField[20];
	private Controleur controleur;
	private Modele modele;
	private AutreEventNotifieur notifieur = new AutreEventNotifieur();
	private JTextField zoneSaisie = new JTextField();
	private boolean boolNeg=false,boutNeg=true;
	private String[] tabVar=new String[7];
	private JDialog dialogVar = new JDialog(new javax.swing.JFrame(), "Definitions des variables"); 
	private JDialog dialogStat = new JDialog(new javax.swing.JFrame(), "Edition des listes"); 
	private JDialog dialogFx = new JDialog(new javax.swing.JFrame(), "Edition des fonctions"); 
	private JDialog dialogGraphe = new JDialog(new javax.swing.JFrame(), "Graphique");
	private JDialog dialogModif = new JDialog(new javax.swing.JFrame(), "Personnalisation");
	private JDialog dialogHist = new JDialog(new javax.swing.JFrame(), "Historique"); 
	private String tabStat[]=new String[20];
	private JLabel labelMoyenne, labelMediane, labelEtendue, labelQuartile1, labelQuartile3, labelInterQ;
	private JTextField[] tabFonction = new JTextField[4]; 
	private Graphe zoneGraphe;
	private Color couleurChoose = new Color(255,255,255);
	private Color[] tabColor = new Color[4];

	public enum Operation
	{
		PLUS ('+'),
		MOINS ('-'),
		MULTI('*'),
		DIV('/'),
		MOD('%');

		private char name;

		Operation(char name)
		{
			this.name=name;
		}

		public char toChar()
		{
			return name;
		}
	}

	public View(Controleur controleur, Modele modele)
	{
		super("Calculatrice");
		this.modele=modele;
		modele.addAutreEventListener(this);
		this.controleur=controleur;
		notifieur.addAutreEventListener(controleur);
		this.setPreferredSize( new Dimension(320,300) );


		int n=0;
		JPanel principale=new JPanel();
		BoxLayout geometrieManager = new BoxLayout(principale, BoxLayout.Y_AXIS);
		principale.setLayout(geometrieManager);
		zoneSaisie.setEditable(false);
		zoneSaisie.setFont(new Font("Arial", Font.PLAIN, 17));
		zoneSaisie.setBackground(Color.WHITE);
		principale.add(zoneSaisie);
		
		JMenuBar barreDeMenu = new JMenuBar();
		JMenu menuModifier = new JMenu("Modifier");
		barreDeMenu.add(menuModifier);
		this.setJMenuBar(barreDeMenu);
		JMenuItem itemCouleur = new JMenuItem("Couleur");
		menuModifier.add(itemCouleur);
		JMenuItem itemSaveColor = new JMenuItem("Save");
		menuModifier.add(itemSaveColor);
		dialogModif.setSize(700,200);

		itemSaveColor.addActionListener((ActionEvent ae) -> { 
			notifieur.diffuserAutreEvent(new AutreEvent(this, tabColor));
			JOptionPane.showMessageDialog(null,"Personnalisation Sauvegard\u00e9e", "Save Color", JOptionPane.INFORMATION_MESSAGE);
		});
		
		JMenuBar barreDeReset = new JMenuBar();
		JButton menuReset = new JButton("Reset");
		barreDeReset.add(menuReset);
		dialogModif.setJMenuBar(barreDeReset);

		Box tools = new Box(BoxLayout.Y_AXIS); //box contenant composant permettant de changer le fond et la police
		tools.setBorder(BorderFactory.createEtchedBorder());

		JPanel colorPanel = new JPanel(); //panel contenant le changement de couleur
		colorPanel.setPreferredSize(new Dimension(300,125));

		JPanel chooseChange = new JPanel();
		BoxLayout geometrieManagerRBP = new BoxLayout(chooseChange, BoxLayout.Y_AXIS);
		chooseChange.setLayout(geometrieManagerRBP);

		ButtonGroup selectionColor = new ButtonGroup();
		JRadioButton comboPolice = new JRadioButton("Police");
		selectionColor.add(comboPolice);
		comboPolice.setSelected(true);
		JRadioButton comboBack = new JRadioButton("Fond");
		selectionColor.add(comboBack);
		JRadioButton comboF1 = new JRadioButton("Fonction 1");
		selectionColor.add(comboF1);
		JRadioButton comboF2 = new JRadioButton("Fonction 2");
		selectionColor.add(comboF2);

		chooseChange.add(comboPolice);
		chooseChange.add(comboBack);
		chooseChange.add(comboF1);
		chooseChange.add(comboF2);
		colorPanel.add(chooseChange);

		JButton echantillonButton = new JButton("Echantillon");
		colorPanel.add(echantillonButton);
		echantillonButton.setVisible(true);
		JTextField echantillonText = new JTextField();
		colorPanel.add(echantillonText);
		echantillonText.setPreferredSize(new Dimension(300,30));
		echantillonText.setVisible(true);
		echantillonText.setEnabled(false);
		JButton colorModif = new JButton("Changer"); //bouton permet de changer la couleur de fond
		colorPanel.add(colorModif);
		JDialog dialogColor = new JDialog(new javax.swing.JFrame(), "Color Chooser"); 
		dialogColor.setSize(700,400);
		JPanel dialogColorPanel = new JPanel();
		BoxLayout geometrieManagerDCP = new BoxLayout(dialogColorPanel, BoxLayout.Y_AXIS);
		dialogColorPanel.setLayout(geometrieManagerDCP);
		JPanel colorChooserPanel = new JPanel();
		dialogColorPanel.add(colorChooserPanel);
		JColorChooser choix = new JColorChooser();
		choix.setPreviewPanel(new JPanel());
		colorChooserPanel.add(choix);
		JButton colorValider = new JButton("OK");
		dialogColorPanel.add(colorValider);
		dialogColor.add(dialogColorPanel);
		tools.add(colorPanel);
		dialogModif.add(tools);

		JMenu menuHistorique = new JMenu("Historique");
		barreDeMenu.add(menuHistorique);
		JMenuItem itemVoir = new JMenuItem("Voir");
		menuHistorique.add(itemVoir);
		JMenuItem itemReset =  new JMenuItem("Reset");
		menuHistorique.add(itemReset);

		dialogHist.setSize(500,650);
		JPanel fenetreHist= new JPanel();
		dialogHist.add(fenetreHist);
		BoxLayout histGeometrie = new BoxLayout(fenetreHist, BoxLayout.Y_AXIS);
		fenetreHist.setLayout(histGeometrie);
		JPanel panelHist = new JPanel();
		fenetreHist.add(panelHist);
		for(int i=0; i<histListe.length; i++)
		{
			histListe[i] = new JTextField(40);
			histListe[i].setEditable(false);
			panelHist.add(histListe[i]);
			int tmp = i;
			histListe[i].addMouseListener(new MouseListener() { 
				public void mouseClicked(MouseEvent me) {
					notifieur.diffuserAutreEvent(new AutreEvent(this, tmp));
				}
				public void mouseEntered(MouseEvent me) {}
				public void mouseExited(MouseEvent me) {}
				public void mousePressed(MouseEvent me) {}
				public void mouseReleased(MouseEvent me) {}
			});
		}

		itemVoir.addActionListener((ActionEvent ae) -> { 
			notifieur.diffuserAutreEvent(new AutreEvent(this, "menuHist"));
			dialogHist.setVisible(true);
		});

		itemReset.addActionListener((ActionEvent ae) -> { 
			notifieur.diffuserAutreEvent(new AutreEvent(this, "menuReset"));
			JOptionPane.showMessageDialog(null,"Historique Vid\u00e9e", "Reset Historic", JOptionPane.INFORMATION_MESSAGE); 
		});

		
		JPanel radioPanel=new JPanel();
		BoxLayout radioGeometrie = new BoxLayout(radioPanel, BoxLayout.X_AXIS);
		radioPanel.setLayout(radioGeometrie);
		ButtonGroup selection=new ButtonGroup();
		JRadioButton choixSci=new JRadioButton("Sci"); //creation du bouton radio Sci
		selection.add(choixSci);
		radioPanel.add(choixSci);
		JRadioButton choixBas=new JRadioButton("Bas");	 //creation du bouton radio Bas
		choixBas.setSelected(true);
		selection.add(choixBas);
		radioPanel.add(choixBas);

		JRadioButton choixVar=new JRadioButton("Var");	 //creation du bouton radio Bas
		selection.add(choixVar);
		radioPanel.add(choixVar);
		JButton fermer=new JButton("OFF");
		tabBouton[n]=fermer;
		n++;
		radioPanel.add(fermer);
		principale.add(radioPanel);

 		JPanel outilPanel=new JPanel();		//panel "(, ), puissance"
		JButton parOuvrante = new JButton("(");
		tabBouton[n]=parOuvrante;
		n++;
		outilPanel.add(parOuvrante);
		JButton parFermante = new JButton(")");
		tabBouton[n]=parFermante;
		n++;
		outilPanel.add(parFermante);
		JButton puisDeux = new JButton("x^2");
		tabBouton[n]=puisDeux;
		n++;
		outilPanel.add(puisDeux);
		JButton puisY = new JButton("x^y");
		tabBouton[n]=puisY;
		n++;
		outilPanel.add(puisY);

		JButton boutonNeg = new JButton("(-)");
		outilPanel.add(boutonNeg);

		JPanel calculPanel=new JPanel();

		JPanel chiffrePanel=new JPanel(new GridLayout(4,3));
		JButton boutonSept = new JButton("7");
		tabBouton[n]=boutonSept;
		n++;
		chiffrePanel.add(boutonSept);
		JButton boutonHuit =new JButton("8");
		tabBouton[n]=boutonHuit;
		n++;
		chiffrePanel.add(boutonHuit);
		
		JButton boutonNeuf =new JButton("9");
		tabBouton[n]=boutonNeuf;
		n++;
		chiffrePanel.add(boutonNeuf);
		
		JButton boutonQuatre =new JButton("4");
		tabBouton[n]=boutonQuatre;
		n++;
		chiffrePanel.add(boutonQuatre);
		
		JButton boutonCinq =new JButton("5");
		tabBouton[n]=boutonCinq;
		n++;
		chiffrePanel.add(boutonCinq);
		
		JButton boutonSix =new JButton("6");
		tabBouton[n]=boutonSix;
		n++;
		chiffrePanel.add(boutonSix);
		
		JButton boutonUn =new JButton("1");
		tabBouton[n]=boutonUn;
		n++;
		chiffrePanel.add(boutonUn);
		
		JButton boutonDeux =new JButton("2");
		chiffrePanel.add(boutonDeux);
		tabBouton[n]=boutonDeux;
		n++;

		JButton boutonTrois =new JButton("3");
		chiffrePanel.add(boutonTrois);
		tabBouton[n]=boutonTrois;
		n++;

		JButton boutonZero =new JButton("0");
		chiffrePanel.add(boutonZero);
		tabBouton[n]=boutonZero;
		n++;

		JButton boutonPoint =new JButton(".");
		chiffrePanel.add(boutonPoint);
		tabBouton[n]=boutonPoint;
		n++;

		JButton boutonMod =new JButton("%");
		tabBouton[n]=boutonMod;
		n++;
		chiffrePanel.add(boutonMod);
		calculPanel.add(chiffrePanel);

		JPanel operationPanel=new JPanel();
		BoxLayout operationGeometrie = new BoxLayout(operationPanel, BoxLayout.Y_AXIS);
		operationPanel.setLayout(operationGeometrie);

		JPanel opBasiquePanel = new JPanel(new GridLayout(3,2));
		JButton boutonEffacer =new JButton("DEL");
		opBasiquePanel.add(boutonEffacer);
		tabBouton[n]=boutonEffacer;
		n++;

		JButton boutonReset =new JButton("c");
		opBasiquePanel.add(boutonReset);
		tabBouton[n]=boutonReset;
		n++;

		JButton boutonPlus =new JButton("+");
		opBasiquePanel.add(boutonPlus);
		tabBouton[n]=boutonPlus;
		n++;

		JButton boutonMoins =new JButton("-");
		opBasiquePanel.add(boutonMoins);
		tabBouton[n]=boutonMoins;
		n++;

		JButton boutonDiviser =new JButton("/");
		opBasiquePanel.add(boutonDiviser);
		tabBouton[n]=boutonDiviser;
		n++;

		JButton boutonMultip =new JButton("*");
		opBasiquePanel.add(boutonMultip);
		tabBouton[n]=boutonMultip;
		n++;

		operationPanel.add(opBasiquePanel);
		JButton boutonEgal =new JButton("=");
		
		operationPanel.add(boutonEgal);

		calculPanel.add(operationPanel);

		

		JPanel panelScient= new JPanel();
		BoxLayout sciGeometrie = new BoxLayout(panelScient, BoxLayout.Y_AXIS);
		panelScient.setLayout(sciGeometrie);

		JPanel panelSci1 = new JPanel();
		JPanel panelSci2 = new JPanel();
		JPanel panelSci3 = new JPanel();


		JButton boutonFx = new JButton("f(x)");
		panelSci1.add(boutonFx);
		JButton boutonStat = new JButton("stat");
		panelSci1.add(boutonStat);
		JButton boutonCos = new JButton("cos");
		panelSci2.add(boutonCos);
		tabBouton[n]=boutonCos;
		n++;
		JButton boutonSin = new JButton("sin");
		panelSci2.add(boutonSin);
		tabBouton[n]=boutonSin;
		n++;
		JButton boutonTan = new JButton("tan");
		panelSci2.add(boutonTan);
		tabBouton[n]=boutonTan;
		n++;
		JButton boutonArccos = new JButton("arccos");
		panelSci2.add(boutonArccos);
		tabBouton[n]=boutonArccos;
		n++;
		JButton boutonArcsin = new JButton("arcsin");
		panelSci2.add(boutonArcsin);
		tabBouton[n]=boutonArcsin;
		n++;
		JButton boutonArctan =new JButton("arctan");
		panelSci3.add(boutonArctan);
		tabBouton[n]=boutonArctan;
		n++;
		JButton boutonAbs =new JButton("|x|");
		panelSci3.add(boutonAbs);
		tabBouton[n]=boutonAbs;
		n++;
		JButton boutonFact =new JButton("x!");
		panelSci3.add(boutonFact);
		tabBouton[n]=boutonFact;
		n++;
		JButton boutonRac =new JButton("sqrt");
		panelSci3.add(boutonRac);
		tabBouton[n]=boutonRac;
		n++;
		JButton boutonLn =new JButton("ln");
		panelSci3.add(boutonLn);
		tabBouton[n]=boutonLn;
		n++;
		JButton boutonLog =new JButton("log");
		panelSci3.add(boutonLog);
		tabBouton[n]=boutonLog;
		n++;

		dialogStat.setSize(250,650);
		JPanel fenetreStat= new JPanel();
		dialogStat.add(fenetreStat);
		BoxLayout statGeometrie = new BoxLayout(fenetreStat, BoxLayout.Y_AXIS);
		fenetreStat.setLayout(statGeometrie);
		JPanel panelEdition= new JPanel();
		fenetreStat.add(panelEdition);
		JPanel panelEdition1 = new JPanel();
		JPanel panelEdition2 = new JPanel();
		BoxLayout editionGeometrie = new BoxLayout(panelEdition1, BoxLayout.Y_AXIS);
		panelEdition1.setLayout(editionGeometrie);
		BoxLayout edition2Geometrie = new BoxLayout(panelEdition2, BoxLayout.Y_AXIS);
		panelEdition2.setLayout(edition2Geometrie);
		panelEdition.add(panelEdition1);
		panelEdition.add(panelEdition2);
		panelEdition1.add(new JLabel("Liste 1"));
		panelEdition2.add(new JLabel("Liste 2"));
		for(int i=0; i<tabListe.length; i++)
		{
			tabListe[i] = new JTextField(5);
			if(i<=19)
				panelEdition1.add(tabListe[i]);
			else
				panelEdition2.add(tabListe[i]);
		}
		JPanel panelStat = new JPanel();
		fenetreStat.add(panelStat);
		JButton resetListe = new JButton("Reset");
		panelStat.add(resetListe);
		JButton statListe1 = new JButton("Liste1");
		panelStat.add(statListe1);
		JButton statListe2 = new JButton("Liste2");
		panelStat.add(statListe2);
		JPanel panelResultat = new JPanel();
		BoxLayout resultatGeometrie = new BoxLayout(panelResultat, BoxLayout.Y_AXIS);
		panelResultat.setLayout(resultatGeometrie);
		panelResultat.add(new JLabel("Resultat :\n"));
		labelMoyenne = new JLabel("  Moyenne : ");
		panelResultat.add(labelMoyenne);
		labelMediane = new JLabel("  Mediane : ");
		panelResultat.add(labelMediane);
		labelEtendue = new JLabel("  Etendue : ");
		panelResultat.add(labelEtendue);
		labelQuartile1 = new JLabel("  1er Quartile : ");
		panelResultat.add(labelQuartile1);
		labelQuartile3 = new JLabel("  3eme Quartile : ");
		panelResultat.add(labelQuartile3);
		labelInterQ = new JLabel("  Interquartile : ");
		panelResultat.add(labelInterQ);
		fenetreStat.add(panelResultat);

		JPanel panelVar = new JPanel();
		BoxLayout varGeometrie = new BoxLayout(panelVar, BoxLayout.Y_AXIS);
		panelVar.setLayout(varGeometrie);

		JPanel panelVar1 = new JPanel();
		JPanel panelVar2 = new JPanel();

		JButton boutonA = new JButton("A");
		panelVar1.add(boutonA);
		tabBouton[n]=boutonA;
		n++;
		JButton boutonB = new JButton("B");
		panelVar1.add(boutonB);
		tabBouton[n]=boutonB;
		n++;
		JButton boutonC = new JButton("C");
		panelVar1.add(boutonC);
		tabBouton[n]=boutonC;
		n++;
		JButton boutonD = new JButton("D");
		panelVar1.add(boutonD);
		tabBouton[n]=boutonD;
		n++;
		JButton boutonE = new JButton("E");
		panelVar1.add(boutonE);
		tabBouton[n]=boutonE;
		n++;
		JButton boutonF = new JButton("F");
		panelVar2.add(boutonF);
		tabBouton[n]=boutonF;
		n++;
		JButton boutonG = new JButton("G");
		panelVar2.add(boutonG);
		tabBouton[n]=boutonG;
		n++;
		JButton boutonPi = new JButton("pi");
		panelVar2.add(boutonPi);
		tabBouton[n]=boutonPi;
		n++;
		JButton boutonExp = new JButton("e");
		panelVar2.add(boutonExp);
		tabBouton[n]=boutonExp;
		n++;
		JButton boutonDef = new JButton("DEF");
		panelVar2.add(boutonDef);
	

		dialogVar.setSize(200,210);
		JPanel fenetreVar= new JPanel();
		BoxLayout fenetreGeometrie = new BoxLayout(fenetreVar, BoxLayout.Y_AXIS);
		fenetreVar.setLayout(fenetreGeometrie);
		JPanel defVar = new JPanel(new GridLayout(4,4));
		JLabel labelA = new JLabel(" A= ");
		defVar.add(labelA );
		varLabel[0] = labelA;
		JTextField JtextA = new JTextField("0");
		defVar.add(JtextA);
		varText[0] = JtextA;
		JLabel labelB = new JLabel(" B= ");
		defVar.add(labelB );
		varLabel[1] = labelB;
		JTextField JtextB = new JTextField("0");
		defVar.add(JtextB);
		varText[1] = JtextB;
		JLabel labelC= new JLabel(" C= ");
		defVar.add(labelC );
		varLabel[2] = labelC;
		JTextField JtextC = new JTextField("0");
		defVar.add(JtextC);
		varText[2] = JtextC;
		JLabel labelD = new JLabel(" D= ");
		defVar.add(labelD );
		varLabel[3] = labelD;
		JTextField JtextD = new JTextField("0");
		defVar.add(JtextD);
		varText[3] = JtextD;
		JLabel labelE = new JLabel(" E= ");
		defVar.add(labelE );
		varLabel[4] = labelE;
		JTextField JtextE = new JTextField("0");
		defVar.add(JtextE);
		varText[4] = JtextE;
		JLabel labelF = new JLabel(" F= ");
		defVar.add(labelF );
		varLabel[5] = labelF;
		JTextField JtextF = new JTextField("0");
		defVar.add(JtextF);
		varText[5] = JtextF;
		JLabel labelG = new JLabel(" G= ");
		defVar.add(labelG );
		varLabel[6] = labelG;
		JTextField JtextG = new JTextField("0");
		defVar.add(JtextG);
		varText[6] = JtextG;
		JPanel panelBouton = new JPanel();

		fenetreVar.add(defVar);
		JButton boutonVal = new JButton("Valider");
		panelBouton.add(boutonVal);
		JButton boutonResetVar = new JButton("Reset");
		panelBouton.add(boutonResetVar);
		fenetreVar.add(panelBouton);

	
		dialogFx.setSize(100,210);
		Box boxGeneral = new Box(BoxLayout.Y_AXIS);
		JPanel panelSaisie = new JPanel(new GridLayout(4,4));
		boxGeneral.add(panelSaisie);
		n=0;
		panelSaisie.add(new JLabel("f1(x)"));
		JTextField JTextf1=new JTextField(4);
		panelSaisie.add(JTextf1);
		tabFonction[n]=JTextf1;
		n++;
		panelSaisie.add(new JLabel("f2(x)"));
		JTextField JTextf2=new JTextField(4);
		panelSaisie.add(JTextf2);
		tabFonction[n]=JTextf2;
		n++;
		panelSaisie.add(new JLabel("x min"));
		JTextField JTextXmin=new JTextField(4);
		tabFonction[n]=JTextXmin;
		n++;
		panelSaisie.add(JTextXmin);
		panelSaisie.add(new JLabel("x max"));
		JTextField JTextXmax=new JTextField(4);
		tabFonction[n]=JTextXmax;
		panelSaisie.add(JTextXmax);
		dialogFx.add(boxGeneral);

		JPanel panelBtFx = new JPanel(); 
		boxGeneral.add(panelBtFx);
		JButton voirGraphe = new JButton("voir Graphe");
		panelBtFx.add(voirGraphe);
		JButton resetFx = new JButton("resetFx");
		panelBtFx.add(resetFx);

		zoneGraphe = new Graphe(400,400);
		zoneGraphe.setOpaque(true);
		dialogGraphe.setSize(zoneGraphe.getWidth()+20, zoneGraphe.getHeight()+60);
		Box boxGraphe = new Box(BoxLayout.Y_AXIS);
		boxGraphe.add(zoneGraphe);
		JLabel point = new JLabel();
		boxGraphe.add(point);
		dialogGraphe.add(boxGraphe);
		dialogGraphe.setResizable(false);
		dialogGraphe.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent me) {
				String co = me.toString();
				String[] co2=co.split(",");
				co2[1]=co2[1].replace("(","");
				co2[2]=co2[2].replace(")","");
				co=co2[1] + " , " + co2[2];
				double x= Integer.parseInt(co2[1]);
				double y= Integer.parseInt(co2[2]);
				int echelle = (zoneGraphe.getWidth())/(zoneGraphe.getXmax()-zoneGraphe.getXmin());						
				int zero=zoneGraphe.getXmin();
				int pos=0;
				while(zero!=0)
				{
					zero=zero+1;
					pos=pos+echelle;
				}
				int echelley = (zoneGraphe.getHeight()/2)/(zoneGraphe.getYmax()-zoneGraphe.getYmin());
				x=(x-pos)/echelle;
				y=((-y+10)+(zoneGraphe.getHeight()/2))/echelley;
				x=x*100;
				x=x+25;
				x=(int)x;
				x=x/100;
				y=y*100;
				y=y+12;
				y=(int)y;
				y=y/100;
				point.setText("( " + x + " ; " + y + " )");
			}
			public void mouseEntered(MouseEvent me) {}
			public void mouseExited(MouseEvent me) {}
			public void mousePressed(MouseEvent me) {}
			public void mouseReleased(MouseEvent me) {}
		});
		Toolkit tk = Toolkit.getDefaultToolkit();
		Cursor curseur;
		try{
			BufferedImage image = ImageIO.read(new File("curseur.png"));
			curseur = tk.createCustomCursor( image, new Point( 1, 1 ), "Pointeur" );
			dialogGraphe.setCursor(curseur);	
		}
		catch(Exception e){}

		dialogVar.add(fenetreVar);
		panelVar.add(panelVar1);
		panelVar.add(panelVar2);
		panelVar.setVisible(false);

		panelScient.add(panelSci1);
		panelScient.add(panelSci2);
		panelScient.add(panelSci3);
		panelScient.setVisible(false);

		principale.add(panelScient);
		principale.add(panelVar);
		principale.add(outilPanel);
		principale.add(calculPanel);

		this.add(principale);

		choixSci.addActionListener((ActionEvent ae) -> { 
			panelScient.setVisible(true);
			panelVar.setVisible(false);
			dialogVar.setVisible(false);
			this.setSize(370,420);
		});

		choixBas.addActionListener((ActionEvent ae) -> { 
			panelScient.setVisible(false);
			panelVar.setVisible(false);
			dialogVar.setVisible(false);
			zoneSaisie.setPreferredSize( new Dimension( 200, 5 ) );;
			this.setSize(320,300);
		});

		choixVar.addActionListener((ActionEvent ae) -> { 
			panelVar.setVisible(true);
			panelScient.setVisible(false);
			dialogVar.setVisible(false);
			this.setSize(350,375);
		});

		resetFx.addActionListener((ActionEvent ae) -> {
			for(int i=0; i<tabFonction.length; i++)
				tabFonction[i].setText("");
			notifieur.diffuserAutreEvent(new AutreEvent(this, "resetFx"));
		});

		boutonFx.addActionListener((ActionEvent ae) -> {
			dialogFx.setVisible(true);
		});

		boutonStat.addActionListener((ActionEvent ae) -> { 
			dialogStat.setVisible(true);
		});

		boutonDef.addActionListener((ActionEvent ae) -> { 
			dialogVar.setVisible(true);
		});

		voirGraphe.addActionListener((ActionEvent ae) -> { 
			try{
				int x = Integer.parseInt(tabFonction[2].getText());
				int y = Integer.parseInt(tabFonction[3].getText());
				zoneGraphe.setBornes(x,y);
				String[] tab=new String[4];
				if(y>x)
				{
					for(int i=0; i<tabFonction.length; i++)
						tab[i]=tabFonction[i].getText();
					if(!tab[0].equals("")&&!tab[1].equals("")) 
						zoneGraphe.setBool(true);
					else
						zoneGraphe.setBool(false);
					if(!tab[0].equals("") || !tab[1].equals(""))
					{
						notifieur.diffuserAutreEvent(new AutreEvent(this, tab));
						dialogGraphe.setVisible(true);
					}
				}
			}
			catch(Exception e){}
		});

		statListe1.addActionListener((ActionEvent ae) -> { 
			for(int i=0; i<=19; i++)
				tabStat[i]=tabListe[i].getText();
			notifieur.diffuserAutreEvent(new AutreEvent(this, tabStat));
		});

		statListe2.addActionListener((ActionEvent ae) -> { 
			int nb=0;
			for(int i=20; i<=39; i++)
			{
				tabStat[nb]=tabListe[i].getText();
				nb=nb+1;
			}
			notifieur.diffuserAutreEvent(new AutreEvent(this, tabStat));
		});

		boutonResetVar.addActionListener((ActionEvent ae) -> {
			for (int i = 0; i < tabVar.length; i++) {
				tabVar[i] = "0";
				varText[i].setText("O");
				varText[i].setForeground(Color.BLACK);
				varLabel[i].setForeground(Color.BLACK);
			}
			notifieur.diffuserAutreEvent(new AutreEvent(this, boutonResetVar));
		});

		boutonVal.addActionListener((ActionEvent ae) -> { 
			tabVar[0]=JtextA.getText();
			tabVar[1]=JtextB.getText();			
			tabVar[2]=JtextC.getText();			
			tabVar[3]=JtextD.getText();			
			tabVar[4]=JtextE.getText();			
			tabVar[5]=JtextF.getText();			
			tabVar[6]=JtextG.getText();			
			notifieur.diffuserAutreEvent(new AutreEvent(this, tabVar));
		});

		resetListe.addActionListener((ActionEvent ae) -> { 
			for(int i=0;i<tabListe.length;i++)
				tabListe[i].setText("");
		});

    	for(int i=0;i<tabBouton.length;i++)
		{
			tabBouton[i].addActionListener(new ActionListener() {
		        	public void actionPerformed(ActionEvent ev) {
		        			notifieur.diffuserAutreEvent(new AutreEvent(this,((JButton)ev.getSource())));
		        			if(boolNeg==true)
							{
								for(int j=5;j<14;j++)
									tabBouton[j].setText(tabBouton[j].getText().substring(1,2));
								boolNeg=false;
							}
							for(Operation op : Operation.values())
							{
								if(op.toChar()==((JButton)ev.getSource()).getText().charAt(0))
								boutNeg=true;
							}
		            	}
		    	});
		}

		boutonEgal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ec) {
				notifieur.diffuserAutreEvent(new AutreEvent(this, zoneSaisie.getText()));
				notifieur.diffuserAutreEvent(new AutreEvent(this, "menuHist"));
			}
		});

		boutonNeg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ec) {
				if(boolNeg==false && boutNeg==true)
				{
					for(int i=5;i<14;i++)
						tabBouton[i].setText("-" + tabBouton[i].getText());
					boolNeg=true;
					boutNeg=false;
				}
				else
				{
					if(tabBouton[5].getText().length()==2)
					{
						for(int i=5;i<14;i++)
							tabBouton[i].setText(tabBouton[i].getText().substring(1,2));
						boolNeg=false;
						boutNeg=true;
					}
				}	
			}
		});

		itemCouleur.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				dialogModif.setVisible(true);
			}
		});

		menuReset.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ev) {
        			zoneSaisie.setForeground(Color.BLACK);
        			zoneSaisie.setBackground(Color.WHITE);
        			zoneGraphe.setColorF1(Color.BLACK);
        			zoneGraphe.setColorF2(Color.RED);
        			couleurChoose = Color.WHITE;
        			echantillonText.setBackground(Color.WHITE);
            	}
    	});

		echantillonButton.addActionListener((ActionEvent ae) -> { 
			dialogColor.setVisible(true);
		});

		colorValider.addActionListener((ActionEvent ae) -> { 
			couleurChoose = choix.getColor();
			dialogColor.setVisible(false);
			echantillonText.setBackground(couleurChoose);
		});

		colorModif.addActionListener((ActionEvent ae) -> {
			if (comboBack.isSelected()) {	
				zoneSaisie.setBackground(couleurChoose); //on change la couleur de fond*/	
				tabColor[0] = couleurChoose; 
			}
			if (comboPolice.isSelected()) {	
				zoneSaisie.setForeground(couleurChoose); //on change la couleur de fond*/	
				tabColor[1] = couleurChoose; 
			}
			if (comboF1.isSelected()) {	
				if(couleurChoose.getRGB()!=-1)
					zoneGraphe.setColorF1(couleurChoose); //on change la couleur de fond*/	
				tabColor[2] = couleurChoose; 
			}
			if (comboF2.isSelected()) {	
				if(couleurChoose.getRGB()!=-1)
					zoneGraphe.setColorF2(couleurChoose); //on change la couleur de fond*/	
				tabColor[3] = couleurChoose; 
			}
		});

		try {

			BufferedReader br = new BufferedReader(new FileReader("save.csv"));
			int tour = 0;
			String line;
			while ((line = br.readLine()) != null) {
				tabColor[tour] = new Color(Integer.parseInt(line), true);
				System.out.println(line + " | "+ tabColor[tour].toString());
				tour = tour + 1;
			}	
			zoneSaisie.setBackground(tabColor[0]); 
			zoneSaisie.setForeground(tabColor[1]); 
			zoneGraphe.setColorF1(tabColor[2]); 
			zoneGraphe.setColorF2(tabColor[3]); 

		}catch(Exception e) {
			zoneSaisie.setBackground(Color.WHITE); 
			zoneSaisie.setForeground(Color.BLACK); 
			zoneGraphe.setColorF1(Color.BLACK); 
			zoneGraphe.setColorF2(Color.RED); 
			//e.printStackTrace();
		}
	}


	@SuppressWarnings("unchecked")
	public void actionADeclancher(AutreEvent event) { //si la vue à été notifié d'un changement
		String tmp = "";
	   if (event.getSource() == modele) { //si cela vient du modele
	   		if (event.getDonnee() instanceof String)  { 
		   		String texteSaisie = (String) event.getDonnee();
		   		if(texteSaisie.length()>=3 && texteSaisie.charAt(texteSaisie.length()-2)=='0' && texteSaisie.charAt(texteSaisie.length()-3)=='.' && texteSaisie.charAt(texteSaisie.length()-1)==' ')
	   				zoneSaisie.setText(texteSaisie.substring(0,texteSaisie.length()-3));
	   			else
	   				zoneSaisie.setText(texteSaisie);
		   		if(texteSaisie.equals("OFF"))
		   		{
		   			System.exit(0);
		   		}
		   		if(texteSaisie.equals("")) 
		   		{	
		   			boolNeg=false;
		   			boutNeg=true;
		   		}
	   		}
	   		if (event.getDonnee() instanceof String[]) {
	   			String[] donnee = (String[]) event.getDonnee();
		   			for (int i = 0; i < donnee.length; i++) {
		   				if (donnee[i].equals("false")) {
		   					varText[i].setForeground(Color.RED);
		   					varLabel[i].setForeground(Color.RED);
		   				}else {
		   					if (varText[i].getText().contains(",")) {
		   						tmp = varText[i].getText();
		   						varText[i].setText(tmp.replace(",", "."));
		   					}
		   					varText[i].setForeground(Color.BLACK);
		   					varLabel[i].setForeground(Color.BLACK);
		   				}
		   			}
		   	}
		   	if (event.getDonnee() instanceof Double[])
		   	{
	   			Double[] res = (Double[]) event.getDonnee();
	   			labelMoyenne.setText("Moyenne : " + res[0]);
				labelMediane.setText("Mediane : " + res[1]);
				labelEtendue.setText("Etendue : " + res[2]);
				labelQuartile1.setText("1er Quartile : " + res[3]);
				labelQuartile3.setText("3eme Quartile : " + res[4]);
				labelInterQ.setText("Interquartile : " + res[5]);
	   		}
	   		if (event.getDonnee() instanceof int[])
		   	{
		   		int[] points = (int[]) event.getDonnee();
		   		for(int i=0;i<points.length;i++)
		   			System.out.println(points[i]);
		   			zoneGraphe.setPoint(points);
		   			zoneGraphe.repaint();
	   		}

	   		if (event.getDonnee() instanceof StringBuilder) {
	   			StringBuilder sb = (StringBuilder) event.getDonnee();
	   			if (!sb.toString().equals("")){
	   				String[] lines = sb.toString().split(";");
					for(int i = 0; i < lines.length; i++){
						histListe[i].setText(lines[i]);
					}
	   			}else{
	   				for(int i = 0; i < 20; i++){
						histListe[i].setText("");
					}
	   			}
	   			
	   		}
   		}
	}
}
	
