package dados;

import modelo.EstacaoMeteorologica;
import modelo.Ponto;
import modelo.Temperatura;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class Dados {

    protected final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("dados");
    public EntityManager entityManager = entityManagerFactory.createEntityManager();

    public void iniciarTransacao() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }

    public void confirmarTransacao() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }

    public void desfazerTransacao() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }

    public <T> T salvar(T objeto) {
        try {
            return entityManager.merge(objeto);
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível salvar o objeto: " + objeto.getClass().getSimpleName(), e);
        }
    }

    public <T> List<T> buscarListaPorCampo(Class<T> classe, String campo, Object valor) {
        try {
            return entityManager.createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE e." + campo + " = :valor", classe)
                    .setParameter("valor", valor)
                    .getResultList();
        } catch (NoResultException e) {
            throw new RuntimeException("Não foi possível buscar a lista de " + classe.getSimpleName() + " por " + campo, e);
        }
    }

    public <T> T buscarUnicoPorCampo(Class<T> classe, String campo, Object valor) {
        try {
            return entityManager.createQuery("SELECT c FROM " + classe.getSimpleName() + " c WHERE c." + campo + " = :valor", classe)
                    .setParameter("valor", valor)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException(classe.getSimpleName() + " com " + campo + " '" + valor + "' não foi encontrado.");
        }
    }

    public <T> List<T> buscarPorCampoContendo(Class<T> classe, String campo, String valor) {
        try {
            return entityManager.createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE LOWER(e." + campo + ") LIKE :valor", classe)
                    .setParameter("valor", "%" + valor + "%")
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível buscar a lista de " + classe.getSimpleName() + " por " + campo, e);
        }
    }

    public <T> T buscarMaisRecente(Class<T> classe) {
        try {
            return entityManager.createQuery("SELECT e FROM " + classe.getSimpleName() + " e ORDER BY e.id DESC", classe)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Nenhum objeto do tipo '" + classe.getSimpleName() + "' encontrado.");
        }
    }

    public <T> List<T> listarTodos(Class<T> classe) {
        try {
            return entityManager.createQuery("SELECT e FROM " + classe.getSimpleName() + " e", classe).getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível listar as entidades: " + classe.getSimpleName(), e);
        }
    }

    public <T> void deletar(T objeto) {
        try {
            if (!entityManager.contains(objeto)) {
                objeto = entityManager.merge(objeto);
            }
            entityManager.remove(objeto);
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível deletar o objeto: " + objeto.getClass().getSimpleName(), e);
        }
    }

    public <T> boolean existeAlgum(Class<T> classe) {
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM " + classe.getSimpleName() + " e", Long.class).getSingleResult();
        return count > 0;
    }

    public <T> boolean existeAlgumComEsseCampo(Class<T> classe, String campo, Object valor) {
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM " + classe.getSimpleName() + " e WHERE e." + campo + " = :valor", Long.class)
                .setParameter("valor", valor)
                .getSingleResult();
        return count > 0;
    }

    public <T> List<T> buscarComCampoNaoVazio(Class<T> classe, String campo) {
        try {
            return entityManager.createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE e." + campo + " IS EMPTY", classe).getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível listar as entidades: " + classe.getSimpleName(), e);
        }
    }

    public List<Temperatura> buscarTemperaturasHistoricas(Ponto ponto, List<LocalDateTime> datas) {
        if (ponto == null || ponto.getId() == null || datas == null || datas.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return entityManager.createQuery("SELECT t FROM Temperatura t WHERE t.ponto = :ponto AND t.dataHora IN :datas", Temperatura.class)
                    .setParameter("ponto", ponto)
                    .setParameter("datas", datas)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível buscar as temperaturas históricas do ponto.");
        }
    }

    public List<EstacaoMeteorologica> buscarEstacoesAssociadasAPropriedades() {
        return entityManager.createQuery(
                        "SELECT DISTINCT e FROM Propriedade p JOIN p.centroide.estacoesMeteorologicas e",
                        EstacaoMeteorologica.class)
                .getResultList();
    }

    public List<Ponto> buscarPontosCentraisDePropriedadesComEstacoes() {
        return entityManager.createQuery(
                        "SELECT DISTINCT p.centroide FROM Propriedade p JOIN p.centroide.estacoesMeteorologicas e WHERE e IS NOT NULL",
                        Ponto.class)
                .getResultList();
    }

    public List<Temperatura> buscarPrevisoesComTemperaturaRealNula() {
        return entityManager.createQuery(
                        "SELECT t FROM Temperatura t WHERE t.temperaturaReal IS NULL AND t.dataHora < :agora", Temperatura.class)
                .setParameter("agora", LocalDateTime.now())
                .getResultList();
    }

    public List<Temperatura> listarTodasAsPrevisoes() {
        try {
            return entityManager.createQuery("SELECT t FROM Temperatura t WHERE t.temperaturaPrevista IS NOT NULL", Temperatura.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível listar as previsões de temperatura.");
        }
    }

    public EstacaoMeteorologica buscarEstacaoMaisProximaComDados(Ponto ponto) {
        return entityManager.createQuery(
                        "SELECT e FROM EstacaoMeteorologica e " +
                                "JOIN e.localizacao p " +
                                "JOIN p.historicoTemperaturas h " +
                                "WHERE h.temperaturaReal IS NOT NULL " +
                                "ORDER BY " + "(POWER(e.localizacao.latitude - :lat, 2) + POWER(e.localizacao.longitude - :lng, 2)) ASC", EstacaoMeteorologica.class)
                .setParameter("lat", ponto.getLatitude())
                .setParameter("lng", ponto.getLongitude())
                .setMaxResults(1)
                .getSingleResult();
    }

    public List<EstacaoMeteorologica> buscarEstacoesDentroDoRaio(Ponto ponto, Double raioEmGraus) {
        return entityManager.createQuery(
                        "SELECT e FROM EstacaoMeteorologica e " +
                                "WHERE SQRT(POWER(e.localizacao.latitude - :lat, 2) + POWER(e.localizacao.longitude - :lng, 2)) <= :raio " +
                                "AND e.localizacao.historicoTemperaturas IS NOT EMPTY " +
                                "ORDER BY SQRT(POWER(e.localizacao.latitude - :lat, 2) + POWER(e.localizacao.longitude - :lng, 2)) ASC",
                        EstacaoMeteorologica.class)
                .setParameter("lat", ponto.getLatitude())
                .setParameter("lng", ponto.getLongitude())
                .setParameter("raio", raioEmGraus)
                .getResultList();
    }

    public EstacaoMeteorologica buscarEstacaoMaisProximaDoQuadrante(Ponto ponto, String condicaoQuadrante) {
        try {
            return entityManager.createQuery(
                            "SELECT e FROM EstacaoMeteorologica e " +
                                    "WHERE " + condicaoQuadrante + " AND e.localizacao.historicoTemperaturas IS NOT EMPTY " +
                                    "ORDER BY (POWER(e.localizacao.latitude - :lat, 2) + POWER(e.localizacao.longitude - :lng, 2)) ASC",
                            EstacaoMeteorologica.class)
                    .setParameter("lat", ponto.getLatitude())
                    .setParameter("lng", ponto.getLongitude())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}