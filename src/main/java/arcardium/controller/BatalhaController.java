package arcardium.controller;

import arcardium.utils.ConsoleUtils;
import arcardium.model.Inimigo;
import arcardium.model.Magia;
import arcardium.model.Mago;
import arcardium.model.Jogador;
import arcardium.model.MagiaFactory;
import arcardium.model.Personagem;
import arcardium.utils.AnsiColors;
import arcardium.view.BatalhaView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Gerencia toda a lógica de uma batalha em turnos entre um Mago e um Inimigo.
 * Esta classe é o "motor" do combate.
 *
 * @author Éric
 */
public class BatalhaController {

    private final BatalhaView view;

    public BatalhaController() {
        this.view = new BatalhaView();

    }

    public void iniciarBatalha(Jogador jogador, List<Inimigo> grupoInimigos, MagiaFactory magiaFactory) {
        List<Personagem> filaDeTurnos = new ArrayList<>();
        Mago mago = (Mago) jogador.getHeroi();
        filaDeTurnos.add(mago);
        filaDeTurnos.addAll(grupoInimigos);
        filaDeTurnos.sort(Comparator.comparingInt(Personagem::getAgi).reversed());
        Scanner sc = new Scanner(System.in);
        int recompensaGold = grupoInimigos.stream().mapToInt(Inimigo::getRecompensaOuro).sum();
        int recompensaXp = grupoInimigos.stream().mapToInt(Inimigo::getRecompensaXp).sum();
        int turno = 1;
        ConsoleUtils.limparTela();
        while (mago.getHp() > 0 && !grupoInimigos.isEmpty()) {
            ConsoleUtils.limparTela();
            view.exbirHeaderCombate(turno, jogador);
            turno++;
            for (Personagem personagemDaVez : filaDeTurnos) {
                if (personagemDaVez.getHp() <= 0) {
                    continue;
                }

                if (personagemDaVez instanceof Mago) {
                    view.exibirStatusTurno(jogador, mago, grupoInimigos);
                }

                personagemDaVez.setEstaDefendendo(false);
                personagemDaVez.processarEfeitosPorTurno();

                if (personagemDaVez.getHp() <= 0) {
                    continue;
                }

                if (personagemDaVez instanceof Mago) {
                    view.exibirOrdemDosTurnos(filaDeTurnos);
                    System.out.println("=========== [" + AnsiColors.red("SEU TURNO") + "] ============");
                    turnoDoJogador(mago, grupoInimigos, sc);
                    ConsoleUtils.aguardarEnter();

                } else if (personagemDaVez instanceof Inimigo) {
                    Inimigo inimigo = (Inimigo) personagemDaVez;
                    view.exibirTurnoInimigo(inimigo.getNome());

                    Magia magia = inimigo.escolherAcao(inimigo, List.of(mago));

                    if (inimigo.lancarHabilidade(magia, List.of(mago))) {
                        view.exibirAtaqueInimigo(magia, inimigo, List.of(mago));
                        ConsoleUtils.aguardarEnter();
                    }
                }
            }

            removerInimigosDerrotados(grupoInimigos,filaDeTurnos);
        }

        processarFimDeCombate(mago, jogador, recompensaXp, recompensaGold, magiaFactory, sc, grupoInimigos);
    }

    private void processarFimDeCombate(Mago mago, Jogador jogador, int xp, int ouro, MagiaFactory magiaFactory, Scanner sc, List<Inimigo> grupoInimigos) {
        if (mago.getHp() > 0) {
            view.exibirFimDeBatalha(mago.getNome(), true);
            view.exibirEspolios(xp, ouro, mago);
            jogador.ganharXP(xp);
            jogador.ganharOuro(ouro);

            List<Magia> jaPossuidas = new ArrayList<>(mago.getMagias());
            List<Magia> novasMagias = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Magia nova = magiaFactory.criarMagiaUnica(jaPossuidas);
                novasMagias.add(nova);
                jaPossuidas.add(nova);
            }

            view.exibirRecompensaMagias(novasMagias);

            int escolha;
            while (true) {
                try {
                    escolha = sc.nextInt();
                    if (escolha >= 1 && escolha <= 3) {
                        Magia selecionada = novasMagias.get(escolha - 1);
                        if (mago.aprenderMagia(selecionada)) {
                            view.exibirMagiaAprendida(selecionada);
                        } else {
                            view.exibirMagiasTroca(mago.getMagias(), selecionada);
                            int indiceTroca = sc.nextInt();
                            if (indiceTroca >= 1 && indiceTroca <= mago.getMagias().size()) {
                                mago.trocarMagia(indiceTroca - 1, selecionada);
                                view.exibirMagiaAprendida(selecionada);
                            }
                        }
                        break;
                    } else if (escolha == 0) {
                        view.exibirMensagem("Você optou por não aprender nenhuma magia.");
                        break;
                    }
                } catch (Exception e) {
                    view.exibirMensagem("Entrada inválida, tente novamente.");
                    sc.nextLine();
                }
            }

        } else {
            for (Inimigo inimigo : grupoInimigos) {
                view.exibirFimDeBatalha(inimigo.getNome(), true);
            }
        }
    }

    private void removerInimigosDerrotados(List<Inimigo> grupoInimigos, List<Personagem> filaDeTurnos) {
        Iterator<Inimigo> iterator = grupoInimigos.iterator();
        while (iterator.hasNext()) {
            Inimigo inimigo = iterator.next(); 
            if (inimigo.getHp() <= 0) {      
                view.exibirInimigoDerrotado(inimigo.getNome());
                iterator.remove();              
                filaDeTurnos.remove(inimigo);   
            }
        }
    }

    private void turnoDoJogador(Mago mago, List<Inimigo> inimigos, Scanner sc) {
        view.exibirMenuJogador(mago.getNome());
        int opcao = -1;

        while (opcao != 0) {
            try {
                opcao = sc.nextInt();
                sc.nextLine();

                switch (opcao) {
                    case 1 -> {
                        view.exibirMagias(mago);
                        int escolha = sc.nextInt();
                        if (escolha < 1 || escolha > mago.getMagias().size()) {
                            view.exibirMensagem("> Cast [cancelado]");
                            view.exibirMenuJogador(mago.getNome());
                            break;
                        }
                        Magia magia = mago.getMagias().get(escolha - 1);

                        switch (magia.getTipoAlvo()) {
                            case ALVO_UNICO -> {
                                if (inimigos.size() > 1) {
                                    view.exibirOpcoesAlvos(inimigos);
                                    int idx = sc.nextInt();
                                    if (idx < 1 || idx > inimigos.size()) {
                                        break;
                                    }
                                    Inimigo alvo = inimigos.get(idx - 1);
                                    if (mago.lancarMagia(magia, List.of(alvo))) {
                                        view.exibirAtaque(magia, mago, List.of(alvo));
                                    }
                                } else {
                                    Inimigo unico = inimigos.get(0);
                                    if (mago.lancarMagia(magia, List.of(unico))) {
                                        view.exibirAtaque(magia, mago, List.of(unico));
                                    }
                                }
                            }
                            case TODOS_INIMIGOS -> {
                                if (mago.lancarMagia(magia, new ArrayList<>(inimigos))) {
                                    view.exibirAtaque(magia, mago, inimigos);
                                }
                            }
                            case ALIADO -> {
                                if (mago.lancarMagia(magia, new ArrayList<>())) {
                                    view.exibirMagiaAliado(magia, mago);
                                }
                            }
                        }
                        opcao = 0;
                    }
                    case 2 -> {
                        view.exibirMensagem("Você usou seu [TURNO] para [DEFENDER]");
                        mago.setEstaDefendendo(true);
                        opcao = 0;
                    }
                    case 0 ->
                        view.exibirMensagem("Você fugiu!");
                    default ->
                        view.exibirMensagem("Opção inválida, tente novamente.");
                }
            } catch (Exception e) {
                sc.nextLine();
                view.exibirMensagem("Entrada inválida. Tente novamente.");
            }
        }
    }
}
