package dados;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Dados {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("dados");
    private static EntityManager entityManager = entityManagerFactory.createEntityManager();
}
