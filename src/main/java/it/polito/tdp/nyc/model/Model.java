package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.nyc.db.NYCDao;

public class Model {
	
	private List<String> providers;
	private Graph<City, DefaultWeightedEdge> grafo;
	private List<City> vertici;
	
	private int durata;
	private List<Integer> revisionati;
	
	public Model() {
		
	}
	
	public List<String> getProviders(){
		
		if(this.providers == null) {
			NYCDao dao = new NYCDao();
			this.providers = dao.getProviders();
		}
		return this.providers;
	}
	
	public void creaGrafo(String provider) {
		NYCDao dao = new NYCDao();
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		this.vertici = new ArrayList<City>(dao.getVertici(provider));
		
		//Aggiungo i vertici
		Graphs.addAllVertices(this.grafo, this.vertici);
		
		//Aggiungo gli archi
		for(City c1 : this.vertici) {
			for(City c2 : this.vertici) {
				if(!c1.equals(c2)) {
					double peso = LatLngTool.distance(c1.getPosizione(), c2.getPosizione(), LengthUnit.KILOMETER);
					Graphs.addEdgeWithVertices(this.grafo, c1, c2, peso);
				}
			}
		}
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<City> getVertici(){
		return vertici;
	}
	
	public List<CityDistance> getCityDistances(City scelto){
		
		List<CityDistance> result = new ArrayList<>();
		List<City> vicini = Graphs.neighborListOf(this.grafo, scelto);
		
		for(City c : vicini) {
			result.add(new CityDistance(c.getNome(), this.grafo.getEdgeWeight(this.grafo.getEdge(scelto, c))));
		}
		
		Collections.sort(result);
		return result;
	}
	
	public void simula(City scelto, int N) {
		Simulatore sim = new Simulatore(this.grafo, this.vertici);
		sim.init(scelto, N);
		sim.run();
		this.durata = sim.getDurata();
		this.revisionati = sim.getRevisionati();
		
	}

	public int getDurata() {
		return durata;
	}

	public List<Integer> getRevisionati() {
		return revisionati;
	}
	
	
}
