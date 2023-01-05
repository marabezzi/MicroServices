package io.github.marabezzi.msavaliadorcredito.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;
import io.github.marabezzi.msavaliadorcredito.application.ex.DadosClienteNotFoundException;
import io.github.marabezzi.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.marabezzi.msavaliadorcredito.application.ex.ErrorComunicacaoMicroservicesException;
import io.github.marabezzi.msavaliadorcredito.domain.model.Cartao;
import io.github.marabezzi.msavaliadorcredito.domain.model.CartaoAprovado;
import io.github.marabezzi.msavaliadorcredito.domain.model.CartaoCliente;
import io.github.marabezzi.msavaliadorcredito.domain.model.DadosCliente;
import io.github.marabezzi.msavaliadorcredito.domain.model.DadosSolicitacaoEmissaoCartao;
import io.github.marabezzi.msavaliadorcredito.domain.model.ProtocoloSolicitacaoCartao;
import io.github.marabezzi.msavaliadorcredito.domain.model.RetornoAvaliacaoCliente;
import io.github.marabezzi.msavaliadorcredito.domain.model.SituacaoCliente;
import io.github.marabezzi.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.marabezzi.msavaliadorcredito.infra.clients.ClienteResourceClient;
import io.github.marabezzi.msavaliadorcredito.infra.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

	private final ClienteResourceClient clientesClient;
	private final CartoesResourceClient cartoesClient;
	private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

	public SituacaoCliente obterSituacaoCliente(String cpf)
			throws DadosClienteNotFoundException, ErrorComunicacaoMicroservicesException {
		// obterDadosCliente - MSCLIENTE
		// obter cartoes do cliente - MSCARTOES

		try {
			ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosCliente(cpf);
			ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesClient.getCartoesByCliente(cpf);

			return SituacaoCliente.builder().cliente(dadosClienteResponse.getBody()).cartoes(cartoesResponse.getBody())
					.build();

		} catch (FeignException e) {
			int status = e.status();
			if (HttpStatus.NOT_FOUND.value() == status) {
				throw new DadosClienteNotFoundException();
			}
			throw new ErrorComunicacaoMicroservicesException(e.getMessage(), status);
		}
	}
	
	public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda) 
			throws DadosClienteNotFoundException, ErrorComunicacaoMicroservicesException {
		try {
			ResponseEntity<DadosCliente> dadosClienteResponse = clientesClient.dadosCliente(cpf);
			ResponseEntity<List<Cartao>> cartoesResponse = cartoesClient.getCartoesRendaAteh(renda);
			
			List<Cartao> cartoes = cartoesResponse.getBody();
			var listaCartoesAprovados = cartoes.stream().map(cartao -> {
				
				DadosCliente dadosCliente = dadosClienteResponse.getBody();
				
				BigDecimal limiteBasico = cartao.getLimiteBasico();
				BigDecimal idadeBD =  BigDecimal.valueOf(dadosCliente.getIdade());
				var fator = idadeBD.divide(BigDecimal.valueOf(10));
				BigDecimal limiteAprovado = fator.multiply(limiteBasico);
				
				CartaoAprovado aprovado = new CartaoAprovado();
				aprovado.setCartao(cartao.getNome());
				aprovado.setBandeira(cartao.getBandeira());
				aprovado.setLimiteAprovado(limiteAprovado);
				
				return aprovado;
				}).collect(Collectors.toList());
			
				return new RetornoAvaliacaoCliente(listaCartoesAprovados);
		} catch (FeignException e) {
			int status = e.status();
			if (HttpStatus.NOT_FOUND.value() == status) {
				throw new DadosClienteNotFoundException();
			}
			throw new ErrorComunicacaoMicroservicesException(e.getMessage(), status);
		}	
	}
	
	public ProtocoloSolicitacaoCartao solicitacaoEmissaoCartao(DadosSolicitacaoEmissaoCartao dados) {
		try {
			emissaoCartaoPublisher.solicitarCartao(dados);
			var protocolo = UUID.randomUUID().toString();
			return new ProtocoloSolicitacaoCartao(protocolo);
		}catch (Exception e) {
			throw new ErroSolicitacaoCartaoException(e.getMessage());
		}
	}
}	
