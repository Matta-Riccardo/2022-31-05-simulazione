package it.polito.tdp.nyc.model;

public class CityDistance implements Comparable <CityDistance>{

	private String nome;
	private Double distance;
	
	public CityDistance(String nome, Double distance) {
		super();
		this.nome = nome;
		this.distance = distance;
	}
	
	public String getNome() {
		return nome;
	}
	
	public Double getDistance() {
		return distance;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public void setDistance(Double distance) {
		this.distance = distance;
	}

	@Override
	public int compareTo(CityDistance o) {
		return this.getDistance().compareTo(o.getDistance());
	}
	
}
