/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arcardium.model;

import arcardium.model.enums.NomeEfeito;
import arcardium.model.enums.TipoAlvo;
import arcardium.model.enums.TipoDeEfeito;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric
 */

public class Mago extends Heroi {

    private List<Magia> magias;

    public Mago(String nome, int hp, int mp, int atk, int def, int agi) {
        super(nome, hp, mp, atk, def, agi);
        this.magias = new ArrayList<>();

    }

    public List<Magia> getMagias() {
        return magias;
    }

    public List<Magia> getMagiasAprimoraveis() {
        List<Magia> magiasAprimoraveis = new ArrayList<>();
        for (Magia m : magias) {
            if (m.getNivel() < 3) {
                magiasAprimoraveis.add(m);
            }
        }
        return magiasAprimoraveis;
    }

    public void setMagias(List<Magia> magias) {
        this.magias = magias;
    }

    public void aprenderMagia(Magia m) {
        magias.add(m);
    }

    public void aprimorarMagia(Magia m) {
        m.aprimorarMagia();
    }

    public boolean lancarMagia(Magia magia, List<Inimigo> alvos) {
        int custo = magia.getCustoMana();
        int mpAtual = super.getMp();
        int valor = magia.getValorEfeito();
        int duracao = magia.getDuracaoEfeito();
        NomeEfeito nomeEfeito = magia.getNomeEfeito();
        if (mpAtual >= custo) {
            TipoDeEfeito efeito = magia.getTipoEfeito();
            if (magia.getTipoAlvo() == TipoAlvo.ALIADO) {

                switch (efeito) {
                    case BUFF_ATAQUE:
                        this.aplicarEfeito(efeito, valor, duracao, nomeEfeito);
                        break;
                    case CURA:
                        this.receberCura(valor);
                        break;
                    case BUFF_DEFESA:
                        this.aplicarEfeito(efeito, valor, duracao, nomeEfeito);
                        break;

                }

            } else {
                for (Inimigo alvo : alvos) {
                    switch (efeito) {
                        case DEBUFF_ATAQUE:
                            alvo.aplicarEfeito(efeito, valor, duracao, nomeEfeito);
                            break;
                        case DEBUFF_DEFESA:
                            alvo.aplicarEfeito(efeito, valor, duracao, nomeEfeito);
                            break;
                        case DANO_POR_TURNO:
                            alvo.aplicarEfeito(efeito, valor, duracao , nomeEfeito);
                            break;
                        case DANO_DIRETO:
                            alvo.tomarDano(valor);
                    }
                }

            }
            super.setMp(mpAtual - custo);
            return true;
        } else {
            System.out.println("MP insuficiente para lançar " + magia.getNome());
            return false;
        }
    }

}
