package io.github.marabezzi.mscartoes.application;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.marabezzi.mscartoes.domain.Cartao;
import io.github.marabezzi.mscartoes.infra.repository.CartaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartaoService {
	
	private final CartaoRepository repository;
	
	@Transactional
	public Cartao save(Cartao cartao) {
		return repository.save(cartao);
	}
	
	public List<Cartao> getCartoesRendaMenorIgual(Long renda){
		var rendaBiDecimal = BigDecimal.valueOf(renda);
		return repository.findByRendaLessThanEqual(rendaBiDecimal);
	}
}
