package io.github.marabezzi.msclientes.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.marabezzi.msclientes.domain.Cliente;
import io.github.marabezzi.msclientes.infra.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository repository;
	
	@Transactional
	public Cliente save(Cliente cliente) {
		return repository.save(cliente);
	}
	
	public Optional<Cliente> getByCPF(String cpf){
		return repository.findByCpf(cpf);
	}
}
