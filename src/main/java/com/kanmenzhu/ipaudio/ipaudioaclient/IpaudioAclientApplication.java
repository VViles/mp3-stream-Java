package com.kanmenzhu.ipaudio.ipaudioaclient;

import com.kanmenzhu.ipaudio.ipaudioaclient.network.TcpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IpaudioAclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpaudioAclientApplication.class, args);

		Sample.sendMp3(-1);
	}
}
