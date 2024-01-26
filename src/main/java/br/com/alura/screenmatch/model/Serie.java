package br.com.alura.screenmatch.model;

import br.com.alura.screenmatch.service.ConsultaChatGPT;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
@Entity
@Table(name = "series")

public class Serie {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    private Integer totalTemporadas;
    private double avaliacao;
    private String atores;

    @Enumerated(EnumType.STRING)
    private Categoria genero;
    private String sinopse;
    private String poster;

@OneToMany (mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)

    private List<Episodio> episodios = new ArrayList<>();

public Serie() {};
    public Serie (DadosSerie dadosSerie) {
        this.titulo = dadosSerie.titulo();
        this.totalTemporadas = dadosSerie.totalTemporadas();
        this.avaliacao = OptionalDouble.of (Double.valueOf(dadosSerie.avaliacao())).orElse(0);

        this.genero = Categoria.fromString(dadosSerie.genero().split(",")[0].trim());
        this.sinopse = ConsultaChatGPT.obterTraducao(dadosSerie.sinopse()).trim() ;
        this.poster = dadosSerie.poster();
        this.atores = dadosSerie.atores();
    }


    public void setEpisodios (List<Episodio> episodios) {
        episodios.forEach(e -> e.setSerie(this));
        this.episodios = episodios;
    }
    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }

    public void setTotalTemporadas(Integer totalTemporadas) {
        this.totalTemporadas = totalTemporadas;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getAtores() {
        return atores;
    }

    public void setAtores(String atores) {
        this.atores = atores;
    }

    public Categoria getGenero() {
        return genero;
    }

    public void setGenero(Categoria genero) {
        this.genero = genero;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @Override
    public String toString() {
        return  ", genero=" + genero +
                "titulo='" + titulo + '\'' +
                ", totalTemporadas=" + totalTemporadas +
                ", avaliacao=" + avaliacao +
                ", atores='" + atores + '\'' +

                ", sinopse='" + sinopse + '\'' +
                ", poster='" + poster + '\'' +
                ", episodios ='" + episodios + '\'' ;
    }
}
