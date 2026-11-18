package br.edu.uniesp.sistemadegestao.internal.seguranca.repository;
import br.edu.uniesp.sistemadegestao.internal.seguranca.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByCelular(String celular);
    Optional<UsuarioEntity> findByClienteId(Long clienteId);
    boolean existsByCelular(String celular);
}
