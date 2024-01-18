package br.com.systemalp.gestao_vagas.modules.candidate.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.systemalp.gestao_vagas.modules.candidate.dto.AuthCandidateRequestDTO;
import br.com.systemalp.gestao_vagas.modules.candidate.dto.AuthCandidateResponseDTO;
import br.com.systemalp.gestao_vagas.modules.candidate.repositories.CandidateRepository;

@Service
public class AuthCandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${security.token.secret.candidate}")
    private String secretKey;

    public AuthCandidateResponseDTO execute(AuthCandidateRequestDTO authCandidateDTO) throws AuthenticationException {
        var candidate = this.candidateRepository.findByUsername(authCandidateDTO.username())
                .orElseThrow(() -> {
                    throw new UsernameNotFoundException("Username/password incorrect");
                });

        var passwordMathes = this.passwordEncoder
                .matches(authCandidateDTO.password(), candidate.getPassword());

        if (!passwordMathes) {
            throw new AuthenticationException();
        }

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        var expriresIn = Instant.now().plus(Duration.ofMinutes(10));
        var token = JWT.create()
                .withIssuer("javagas")
                .withSubject(candidate.getId().toString())
                .withClaim("rules", Arrays.asList("candidate"))
                .withExpiresAt(expriresIn)
                .sign(algorithm);

        var authCandidateResponse = AuthCandidateResponseDTO.builder()
                .access_token(token)
                .exprires_in(expriresIn.toEpochMilli())
                .build();
        return authCandidateResponse;
    }
}