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

    private final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("dados");
    private final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<>();

    private EntityManager obterEntityManager() {
        EntityManager entityManager = entityManagerThreadLocal.get();
        if (entityManager == null || !entityManager.isOpen()) {
            throw new IllegalStateException("Nenhuma transação ativa. Chame iniciarTransacao() antes de usar o EntityManager.");
        }
        return entityManager;
    }

    public void iniciarTransacao() {
        if (entityManagerThreadLocal.get() == null || !entityManagerThreadLocal.get().isOpen()) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManagerThreadLocal.set(entityManager);
            entityManager.getTransaction().begin();
        }
    }

    public void confirmarTransacao() {
        EntityManager entityManager = entityManagerThreadLocal.get();

        if (entityManager != null && entityManager.getTransaction().isActive()) {
            try {
                entityManager.getTransaction().commit();
            } finally {
                if (entityManager.isOpen()) {
                    entityManager.close();
                }
                entityManagerThreadLocal.remove();
            }
        }
    }

    public void desfazerTransacao() {
        EntityManager entityManager = entityManagerThreadLocal.get();
        if (entityManager != null && entityManager.getTransaction().isActive()) {
            try {
                entityManager.getTransaction().rollback();
            } finally {
                if (entityManager.isOpen()) {
                    entityManager.close();
                }
                entityManagerThreadLocal.remove();
            }
        }
    }

    public <T> void salvar(T objeto) {
        try {
            obterEntityManager().merge(objeto);
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível salvar o objeto: " + objeto.getClass().getSimpleName(), e);
        }
    }

    public <T> List<T> buscarListaPorCampo(Class<T> classe, String campo, Object valor) {
        try {
            return obterEntityManager().createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE e." + campo + " = :valor", classe)
                    .setParameter("valor", valor)
                    .getResultList();
        } catch (NoResultException e) {
            throw new RuntimeException("Não foi possível buscar a lista de " + classe.getSimpleName() + " por " + campo, e);
        }
    }

    public <T> T buscarUnicoPorCampo(Class<T> classe, String campo, Object valor) {
        try {
            return obterEntityManager().createQuery("SELECT c FROM " + classe.getSimpleName() + " c WHERE c." + campo + " = :valor", classe)
                    .setParameter("valor", valor)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException(classe.getSimpleName() + " com " + campo + " '" + valor + "' não foi encontrado.");
        }
    }

    public <T> List<T> buscarPorCampoContendo(Class<T> classe, String campo, String valor) {
        try {
            return obterEntityManager().createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE LOWER(e." + campo + ") LIKE :valor", classe)
                    .setParameter("valor", "%" + valor + "%")
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível buscar a lista de " + classe.getSimpleName() + " por " + campo, e);
        }
    }

    public <T> T buscarMaisRecente(Class<T> classe) {
        try {
            return obterEntityManager().createQuery("SELECT e FROM " + classe.getSimpleName() + " e ORDER BY e.id DESC", classe)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Nenhum objeto do tipo '" + classe.getSimpleName() + "' encontrado.");
        }
    }

    public <T> List<T> listarTodos(Class<T> classe) {
        try {
            return obterEntityManager().createQuery("SELECT e FROM " + classe.getSimpleName() + " e", classe).getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível listar as entidades: " + classe.getSimpleName(), e);
        }
    }

    public <T> void deletar(T objeto) {
        try {
            if (!obterEntityManager().contains(objeto)) {
                objeto = obterEntityManager().merge(objeto);
            }
            obterEntityManager().remove(objeto);
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível deletar o objeto: " + objeto.getClass().getSimpleName(), e);
        }
    }


    public <T> boolean existeAlgum(Class<T> classe) {
        Long count = obterEntityManager().createQuery("SELECT COUNT(e) FROM " + classe.getSimpleName() + " e", Long.class).getSingleResult();
        return count > 0;
    }

    public <T> boolean existeAlgumComEsseCampo(Class<T> classe, String campo, Object valor) {
        Long count = obterEntityManager().createQuery("SELECT COUNT(e) FROM " + classe.getSimpleName() + " e WHERE e." + campo + " = :valor", Long.class)
                .setParameter("valor", valor)
                .getSingleResult();
        return count > 0;
    }

    public <T> boolean existeAlgumVazio(Class<T> classe, String campo) {
        Long count = obterEntityManager().createQuery("SELECT COUNT(e) FROM " + classe.getSimpleName() + " e WHERE e." + campo + " IS EMPTY", Long.class)
                .getSingleResult();
        return count > 0;
    }

    public <T> List<T> buscarOndeCampoForVazio(Class<T> classe, String campo) {
        try {
            return obterEntityManager().createQuery("SELECT e FROM " + classe.getSimpleName() + " e WHERE e." + campo + " IS EMPTY", classe)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível listar as entidades: " + classe.getSimpleName(), e);
        }
    }

    public List<Temperatura> buscarTemperaturasHistoricas(Ponto ponto, List<LocalDateTime> datas) {
        if (ponto == null || ponto.getId() == null || datas == null || datas.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return obterEntityManager().createQuery("SELECT t FROM Temperatura t WHERE t.ponto = :ponto AND t.dataHora IN :datas", Temperatura.class)
                    .setParameter("ponto", ponto)
                    .setParameter("datas", datas)
                    .getResultList();
        } catch (PersistenceException e) {
            throw new RuntimeException("Não foi possível buscar as temperaturas históricas do ponto.");
        }
    }

    public List<EstacaoMeteorologica> buscarEstacoesAssociadasAPropriedades() {
        return obterEntityManager().createQuery(
                        "SELECT DISTINCT e FROM Propriedade p JOIN p.centroide.estacoesMeteorologicas e",
                        EstacaoMeteorologica.class)
                .getResultList();
    }

    public List<Ponto> buscarCentroidesAssociadosAEstacoes() {
        return obterEntityManager().createQuery(
                        "SELECT DISTINCT p.centroide FROM Propriedade p JOIN p.centroide.estacoesMeteorologicas e WHERE e IS NOT NULL",
                        Ponto.class)
                .getResultList();
    }

    public List<Temperatura> buscarPrevisoesComTemperaturaCalculadaVazia() {
        return obterEntityManager().createQuery(
                        "SELECT t FROM Temperatura t WHERE t.temperaturaReal IS NULL AND t.dataHora < :agora", Temperatura.class)
                .setParameter("agora", LocalDateTime.now())
                .getResultList();
    }

    public EstacaoMeteorologica buscarEstacaoMaisProximaComDados(Ponto ponto) {
        return obterEntityManager().createQuery(
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
        return obterEntityManager().createQuery(
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
            return obterEntityManager().createQuery(
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