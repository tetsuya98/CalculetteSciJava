package calculatrice;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.FileReader;
import java.io.BufferedReader;

@SuppressWarnings("serial")  
	public class Graphe extends JPanel  {
		private Color m_color = null;
		private int[] tabPoint;
		private int m_width, m_height, m_xmin=0, m_xmax=1,ymax,ymin;
		private boolean DeuxFonc, divise=false;
		private Color colorF1 = Color.BLACK,colorF2 = Color.RED;

		public Graphe(int p_width, int p_height ) {
			this.m_width = p_width;
			this.m_height = p_height;
			this.setPreferredSize(new Dimension(p_width,p_height)); //dimension de la zone de Graphe
		}

		public int[] getPoint()
		{
			return this.tabPoint;
		}

		public void setPoint(int[] tab)
		{
			this.tabPoint=tab;
		}

		public void setBool(boolean b)
		{
			this.DeuxFonc=b;
		}

		public void setBornes(int p_xmin, int p_xmax)
		{
			this.m_xmin=p_xmin;
			this.m_xmax=p_xmax;
		}

		public int getXmin()
		{
			return this.m_xmin;
		}

		public int getXmax()
		{
			return this.m_xmax;
		}

		public int getYmin()
		{
			return this.ymin;
		}

		public int getYmax()
		{
			return this.ymax;
		}

		public int getWidth()
		{
			return this.m_width;
		}

		public int getHeight()
		{
			return this.m_height;
		}

		public void setColorF1(Color c)
		{
			this.colorF1=c;
		}

		public void setColorF2(Color c)
		{
			this.colorF2=c;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			int nbx=0,nby=0;
			int echelle = (this.m_width)/(this.m_xmax-this.m_xmin);
			int min,max,echelley,taille;
			String s;

			this.setBackground(Color.WHITE);
			g.setColor(Color.lightGray);
			g.drawLine(0, this.m_height/2, this.m_width, this.m_height/2);	
			int zero=this.m_xmin;
			int pos=0;
			while(zero!=0)
			{
				zero=zero+1;
				pos=pos+echelle;
			}
			g.drawLine(pos, 0, pos, this.m_height);
			g.drawString("0",pos-10,this.m_height/2+15);
			g.drawString("" + this.m_xmax,this.m_width-20,this.m_height/2+15);
			g.drawString("" + this.m_xmin,10,this.m_height/2+15);
			g.setColor(colorF1);

				int[] x = new int[this.m_xmax-this.m_xmin+1];
				int[] x2 = new int[this.m_xmax-this.m_xmin+1];

				for(int i=this.m_xmin; i<=this.m_xmax; i++)
				{
					x[nbx]=i*echelle+pos;
					x2[nbx]=i*echelle+pos;
					nbx=nbx+1;
				}

				nbx=0;
				if(tabPoint.length>1)
					this.ymin=tabPoint[0];this.ymax=tabPoint[0];
				
				for(int i=0;i<tabPoint.length && divise==false;i++)
				{
					if(tabPoint[i]>=100)
					{
						s="" + tabPoint[i];
						if(s.charAt(s.length()-1)=='0' && s.charAt(s.length()-2)=='0')
						{
							this.divise=true;
						}
					}
				}

				if(this.divise==true)
				{
					System.out.println("ok");
					for(int i=0;i<tabPoint.length;i++)
					{
						tabPoint[i]=tabPoint[i]/100;
						System.out.println("tab [" + i + "] = " + tabPoint[i]);
					}
					this.divise=false;
				}


				if(this.DeuxFonc==false)
					taille=tabPoint.length/2;
				else
					taille=tabPoint.length;
				
				this.ymin=tabPoint[0];
				this.ymax=tabPoint[0];
				for(int i=0;i<taille;i++)
				{
					if(tabPoint[i]>this.ymax)
						this.ymax=tabPoint[i];
					if(tabPoint[i]<this.ymin)
						this.ymin=tabPoint[i];
				}
				echelley = (this.m_height/2)/(this.ymax-this.ymin);

				if(this.DeuxFonc==false)
				{
					int[] y = new int[tabPoint.length];
					for (int i=0; i<tabPoint.length; i++)
					{
						y[nby]=(this.m_height/2)-tabPoint[i]*echelley;
						nby=nby+1;
					}
					g.drawPolyline(x,y,x.length);
				}
				else
				{
					int[] y = new int[tabPoint.length];
					int[] y2 = new int[tabPoint.length];
					for (int i=0; i<tabPoint.length/2; i++)
					{
						y[nby]=(this.m_height/2)-tabPoint[i]*echelley;
						nby=nby+1;
					}
					for (int i=tabPoint.length/2; i<tabPoint.length; i++)
					{
						y2[nbx]=(this.m_height/2)-tabPoint[i]*echelley;
						nbx=nbx+1;	
					}
					g.drawPolyline(x,y,x.length);
					g.setColor(colorF2);
					g.drawPolyline(x,y2,x2.length);
				}
		}
	}