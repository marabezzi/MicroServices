package io.github.marabezzi.mscartoes.application.representation;

import java.math.BigDecimal;

import io.github.marabezzi.mscartoes.domain.BandeiraCartao;
import io.github.marabezzi.mscartoes.domain.Cartao;
import lombok.Data;

@Data
public class CartaoSaveRequest {
	private String nome;
	private BandeiraCartao bandeira;
	private BigDecimal renda;
	private BigDecimal limite;
	
	public Cartao toModal() {
		return new Cartao(nome, bandeira, renda, limite);
	}
}
