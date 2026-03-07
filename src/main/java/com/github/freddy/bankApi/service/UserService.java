package com.github.freddy.bankApi.service;

import com.github.freddy.bankApi.dto.request.StaffUserRequest;
import com.github.freddy.bankApi.dto.request.UpdatePasswordRequest;
import com.github.freddy.bankApi.dto.request.RegisterRequest;
import com.github.freddy.bankApi.dto.response.AccountResponse;
import com.github.freddy.bankApi.dto.response.UserProfileResponse;
import com.github.freddy.bankApi.dto.response.RegistrationResponse;
import com.github.freddy.bankApi.entity.User;
import com.github.freddy.bankApi.exception.ConflictException;
import com.github.freddy.bankApi.exception.NotFoundException;
import com.github.freddy.bankApi.mapper.UserMapper;
import com.github.freddy.bankApi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável por operações relacionadas a usuários.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cria um novo usuário cliente + conta bancária padrão.
     * Endpoint público (para auto-registro).
     */
    @Transactional
    public RegistrationResponse createNewUser(RegisterRequest data) {
        log.debug("Tentativa de registro de cliente: email={}, BI={}", data.email(), data.biNumber());

        // Verificação de unicidade
        if (userRepository.existsByEmail(data.email())) {
            log.warn("Registro falhou: email já cadastrado - {}", data.email());
            throw new ConflictException("Este e-mail já está cadastrado");
        }

        String biUpper = data.biNumber().toUpperCase();
        if (userRepository.existsByBi(biUpper)) {
            log.warn("Registro falhou: BI já cadastrado - {}", biUpper);
            throw new ConflictException("Este Bilhete de Identidade já está cadastrado");
        }

        User newUser = userMapper.toEntity(data);
        User savedUser = userRepository.save(newUser);

        // Cria conta bancária apenas para clientes normais
        AccountResponse accountResponse = accountService.createDefaultAccount(savedUser, data.accountType());

        log.info("Cliente e conta criados com sucesso: ID={}, email={}, conta={}",
                savedUser.getId(), savedUser.getEmail(), accountResponse.accountNumber());

        return userMapper.toResponse(savedUser, accountResponse);
    }

    /**
     * Cria um usuário interno (admin, staff, suporte, etc.) SEM criar conta bancária.
     * Endpoint protegido (somente ROLE_ADMIN).
     */
    @Transactional
    public UserProfileResponse createInternalUser(StaffUserRequest request) {
        log.debug("Tentativa de criação de usuário interno: email={}, role={}", request.email(), request.role());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Criação falhou: email já cadastrado - {}", request.email());
            throw new ConflictException("Este e-mail já está cadastrado");
        }

        String biUpper = request.biNumber().toUpperCase();
        if (userRepository.existsByBi(biUpper)) {
            log.warn("Criação falhou: BI já cadastrado - {}", biUpper);
            throw new ConflictException("Este Bilhete de Identidade já está cadastrado");
        }
        User newUser = userMapper.toInternalEntity(request);
        User savedUser = userRepository.save(newUser);

        log.info("Usuário interno criado com sucesso: ID={}, email={}, role={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        return new UserProfileResponse(
                savedUser.getId(),savedUser.getName(), savedUser.getBi(), savedUser.getPhoneNumber(),
                savedUser.getRole()
        );
    }

    /**
     * Retorna o perfil básico do usuário (sem dados sensíveis).
     */
    public UserProfileResponse getProfile(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getBi(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    /**
     * Atualiza o número de telefone do usuário.
     */
    @Transactional
    public void updatePhoneNumber(UUID userId, String newPhoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (newPhoneNumber.equals(user.getPhoneNumber())) {
            throw new IllegalStateException("O número informado já está registrado");
        }
        String normalized  = userMapper.normalizePhoneNumber(newPhoneNumber);
        user.setPhoneNumber(normalized);

        log.info("Número de telefone atualizado com sucesso para usuário ID: {}", userId);
    }

    /**
     * Atualiza a senha do usuário autenticado.
     * Valida a senha atual antes de alterar.
     */
    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        log.debug("Tentativa de atualização de senha para usuário ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Valida senha atual
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Atualização de senha falhou: senha atual incorreta para usuário ID: {}", userId);
            throw new BadCredentialsException("Senha atual incorreta");
        }

        if (request.newPassword().equals(request.currentPassword())) {
            throw new IllegalStateException("A nova senha não pode ser igual à senha atual");
        }
        // Encripta e actualiza
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(encodedNewPassword);

        userRepository.save(user);

        log.info("Senha atualizada com sucesso para usuário ID: {}", userId);
    }

   /* public Page<UserProfileResponse> listUsers(Pageable pageable, String role, String bi) {

    }*/
}