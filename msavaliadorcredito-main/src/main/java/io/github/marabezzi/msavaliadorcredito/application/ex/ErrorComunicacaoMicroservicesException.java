package io.github.marabezzi.msavaliadorcredito.application.ex;

import lombok.Getter;

public class ErrorComunicacaoMicroservicesException extends Exception{
	
	@Getter
	private Integer status;
	public ErrorComunicacaoMicroservicesException(String msg, Integer status) {
		super(msg);
	}

}
