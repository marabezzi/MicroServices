package io.github.marabezzi.mscartoes.infra.mqueue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.marabezzi.mscartoes.domain.Cartao;
import io.github.marabezzi.mscartoes.domain.ClienteCartao;
import io.github.marabezzi.mscartoes.domain.DadosSolicitacaoEmissaoCartao;
import io.github.marabezzi.mscartoes.infra.repository.CartaoRepository;
import io.github.marabezzi.mscartoes.infra.repository.ClienteCartaoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class EmissaoCartaoSubscriber {
	
	private final CartaoRepository cartaoRepository;
	private final ClienteCartaoRepository clienteCartaoRepository;
	
	@RabbitListener(queues = "${mq.queues.emissao-cartoes}")
	public void receberSolicitacaoEmissao(@Payload String payload) {
		try {
			var mapper = new ObjectMapper();
			
			DadosSolicitacaoEmissaoCartao dados = mapper.readValue(payload, DadosSolicitacaoEmissaoCartao.class);
			Cartao cartao = cartaoRepository.findById(dados.getIdCartao()).orElseThrow();
			
			ClienteCartao clienteCartao = new ClienteCartao();
			clienteCartao.setCartao(cartao);
			clienteCartao.setCpf(dados.getCpf());
			clienteCartao.setLimite(dados.getLimiteLiberado());
			
			clienteCartaoRepository.save(clienteCartao);
			
			} catch (Exception e) {
			log.error("Erro ao receber solicitação de cartão: {} ", e.getMessage());
		}
	}
}
	
