package it.polito.tdp.nyc.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.nyc.model.Event.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class Simulatore {

	//DATI IN INGRESSO
	private Graph<City, DefaultWeightedEdge> grafo; //Serve per trovare i quartieri da visitare valutandone il loro peso
	private List<City> cities; //Niente di più che la lista contente i vertici del grafo
	private City partenza; //Il quartierer da cui parto con la simulazione, ovvero quello scelto dalla lista
	private int N; //numero dei tecnici che decido di adoperare per la simulazione
	
	//DATI IN USCITA
	private int durata; //durata in minuti della simulazione
	private List<Integer> revisionati; //revisionati.get(i) = numero di hotspot revisionati dal tecnico 'i' (i tra 0 e N-1)	
	
	//MODELLO DEL MONDO
	private List<City> daVisitare; //quartieri ancora da visitare (escluso currentCity che rimuovo come prima cosa dalla lista)
	private City currentCity; //quartiere in elaborazione
	private int hotSpotRimanenti; //hotspot ancora da revisonare nel quartiere
	private int tecniciOccupati; //quanti tecnici sono impegnati ---> quando arriva a 0, se ho altri quartieri da visionare, mi sposto di quartiere
	
	//CODA DEGLI EVENTI
	private PriorityQueue<Event> queue;

	public Simulatore(Graph<City, DefaultWeightedEdge> grafo, List<City> cities) {
		super();
		//Inizializzo qui quei valori di ingresso che rimarranno costanti in tutta la simulazione 
		this.grafo = grafo;
		this.cities = cities;
	}
	
	public void init(City partenza, int N) {
		//Inizializzo i restanti valori di ingresso che si modificano
		this.partenza = partenza;
		this.N = N;
		
		//Inizializzo gli output
		this.durata = 0;
		this.revisionati = new ArrayList<Integer>();
		for(int i = 0; i < this.N; i++) {
			revisionati.add(0);
		}
		
		//Inizializzo il mondo degli eventi
		this.currentCity = this.partenza;
		
		this.daVisitare = new ArrayList<City>(this.cities);
		this.daVisitare.remove(this.currentCity);
		
		this.hotSpotRimanenti = this.currentCity.getnHotSpot();
		this.tecniciOccupati = 0;
		
		//Creo la coda
		this.queue = new PriorityQueue<>();
		
		//Caricamento iniziale della coda
		int i = 0; //Corrisponderà al tecnico 
		while(this.tecniciOccupati < this.N && this.hotSpotRimanenti > 0) {
			//Posso assegnare allora un tecnico ad un hotspot
			queue.add(new Event(0, EventType.INIZIO_HS, i));
			this.tecniciOccupati++;
			this.hotSpotRimanenti--;
			i++;
		}
		
	}
	
	public void run() {
		
		while(!queue.isEmpty()) {
			Event e = queue.poll();
			this.durata = e.getTime(); //Alla fine della simulazione in questo campo sarà possibile consultare la durata effettiva della simulazione
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		
		//Esctraggo i campi dell'evento per averli in strutture dati definite con delle variabili
		int time = e.getTime();
		EventType type = e.getType();
		int tecnico = e.getTecnico();
		
		switch(type) {
		case INIZIO_HS:
			this.revisionati.set(tecnico, this.revisionati.get(tecnico)+1);
			
			if(Math.random()<0.1) {
				queue.add(new Event(time+25, EventType.FINE_HS, tecnico));
			}else {
				queue.add(new Event(time+10, EventType.FINE_HS, tecnico));
			}
			
			break;
			
		case FINE_HS:
			this.tecniciOccupati--;
			
			if(this.hotSpotRimanenti > 0) {
				//Mi sposto allora su un altro hotspot da visionare
				int durataSpostamento = (int)(Math.random()*11)+10;
				this.tecniciOccupati++;
				this.hotSpotRimanenti--;
				queue.add(new Event(time+durataSpostamento, EventType.INIZIO_HS, tecnico));
				
			}else if(this.tecniciOccupati > 0) {
				//Non ci sono più altri hotspot da visitare ma alcuni dei colleghi non hanno ancora terminato il lavoro
				//Non devo fare nulla e aspetto che gli altri finiscono
				
			}else if(this.daVisitare.size() > 0){
				//Tutti abbiamo finito e allora si deve cambiare quartiere, vado a scegliere il quartiere daVisitare rimanente più vicino
				City destinazione = piùVicino(this.currentCity, this.daVisitare);
				int durataSpostamento =  (int) (this.grafo.getEdgeWeight(this.grafo.getEdge(this.currentCity, destinazione))/50.0 * 60.0);
				
				this.currentCity = destinazione;
				this.daVisitare.remove(destinazione);
				this.hotSpotRimanenti = this.currentCity.getnHotSpot();
				
				this.queue.add(new Event(time+durataSpostamento, EventType.NUOVO_QUARTIERE, -1));
				
			}else {
				//Non ho più quartieri da visitare, qui la simulazione finisce
			}
			
			break;
			
		case NUOVO_QUARTIERE:
			int i = 0;
			while(this.tecniciOccupati < this.N && this.hotSpotRimanenti > 0) {
				//Posso assegnare allora un tecnico ad un hotspot
				queue.add(new Event(time, EventType.INIZIO_HS, i));
				i++;
				this.tecniciOccupati++;
				this.hotSpotRimanenti--;
			}
			break;
		}
		
	}

	private City piùVicino(City current, List<City> vicine) {
		
		double min = 100000.0;
		City destinazione = null;
		for(City c : vicine) {
			double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(current, c));
			if(peso < min){
				min = peso;
				destinazione = c;
			}
		}
		
		return destinazione;
	}

	public int getDurata() {
		return durata;
	}

	public List<Integer> getRevisionati() {
		return revisionati;
	}
	
}
