package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List <DadosSerie> dadosSeries = new ArrayList<>();

    private Optional<Serie> serieBusca;
    private SerieRepository repositorio;
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }
    private List<Serie> series = new ArrayList<>();


    public void exibeMenu() {
        var opcao = -1;
        while (opcao !=0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4- Busca pelo Titulo da Serie 
                    5- Buscar Series por Ator     
                    6- Buscar top 5 Series
                    7- Buscar Serie por Categoria
                    8- Buscar Serie por Temporada e Avaliação
                    9- Buscar por título do episódio    
                    10- Buscar top 5 episodios da serie      
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();

            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorNome();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    buscarSeriaPorTemporadaMenorQueEAvaliacaoMaiorQue();
                    break;
                case 9:
                    buscarSeriePorTrechoDoEpisodio();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }




    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);

        dadosSeries.add(dados);
        repositorio.save( serie);

        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        if (dados == null) {
            System.out.println("Série não encontrada. Tente novamente.");
            return getDadosSerie();  // Chamada recursiva para tentar novamente
        }

        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

if (serie.isPresent()) {

    var serieEcontrada = serie.get();


    List<DadosTemporada> temporadas = new ArrayList<>();


        for (int i = 1; i <= serieEcontrada.getTotalTemporadas(); i++) {
            var json = consumo.obterDados(ENDERECO + serieEcontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

    List<Episodio> episodios = temporadas.stream()
            .flatMap(d -> d.episodios().stream()
                    .map(e -> new Episodio(d.numero(), e)))
            .collect(Collectors.toList());
    serieEcontrada.setEpisodios(episodios);
    repositorio.save(serieEcontrada);
}

   else {
    System.out.println("Série não encontrada");
}}
    private void listarSeriesBuscadas() {
         series =  repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorNome() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();
       serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da série:"+ serieBusca.get());
        } else System.out.println("Serie não encontrada!");

    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o nome do Ator");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de qual valor?");
        var avaliacao = leitura.nextDouble();
    List<Serie> seriesEncontradas  = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor ,avaliacao);
        System.out.println("Series em que " + nomeAtor + "  atuou:");
        seriesEncontradas.forEach(s-> System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Digite a categoria desejada:");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> listaPorGenero = repositorio.findByGenero(categoria);
        System.out.println("Lista por Gênero:");
        listaPorGenero.forEach(g-> System.out.println(g.getTitulo()));
    }

    private void buscarSeriaPorTemporadaMenorQueEAvaliacaoMaiorQue() {
        System.out.println("Digite o numero maximo de temporadas desejado");
        Integer numeroTemporadas = leitura.nextInt();
        System.out.println("Digite a avaliacao minima desejada");
        double avaliacaoMinima = leitura.nextDouble();
        List<Serie> seriesTemporada = repositorio.buscaSeriePorTemporadaEAvaliacao(numeroTemporadas, avaliacaoMinima);
        seriesTemporada.forEach(serie -> System.out.println(serie));

    }
    private void buscarTop5Series() {
    List<Serie> top5Series = repositorio.findFirst5ByOrderByAvaliacaoDesc();
        top5Series.forEach(t -> System.out.println(t));
    }
    private void buscarSeriePorTrechoDoEpisodio() {
        String trechoEpisodio = leitura.nextLine();
        List<Episodio> listaEpisodios = repositorio.buscaPorTrecho(trechoEpisodio);
        listaEpisodios.forEach(System.out::println);
    
    }

    //Código omitido

//Código omitido

    private void topEpisodiosPorSerie(){
        buscarSeriePorNome();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao() ));
        }
    }


    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorNome();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento");
          var anoLancamento  =leitura.nextInt();
          leitura.nextLine();

            List<Episodio> s = repositorio.buscaSerieDepoisDeUmaData(serie, anoLancamento);
            s.forEach(System.out::println);
        }


    }
}