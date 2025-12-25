package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert MovieSession "
                    + movieSession.toString(), e);
        }
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            MovieSession movieSession = session.get(MovieSession.class, id);
            if (movieSession != null) {
                Hibernate.initialize(movieSession.getMovie());
                Hibernate.initialize(movieSession.getCinemaHall());
            }
            return Optional.ofNullable(movieSession);
        } catch (Exception e) {
            throw new DataProcessingException("Can't get a MovieSession by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> getMovieSessionsQuery =
                    session.createQuery("from MovieSession ms "
                                    + "join fetch ms.movie m "
                                    + "join fetch ms.cinemaHall "
                                    + "where m.id = :movieId "
                                    + "and ms.showTime between :startDateTime and :endDateTime",
                            MovieSession.class);
            getMovieSessionsQuery.setParameter("movieId", movieId);
            getMovieSessionsQuery.setParameter("startDateTime", date.atStartOfDay());
            getMovieSessionsQuery.setParameter("endDateTime", date.atTime(23, 59, 59));
            return getMovieSessionsQuery.getResultList();
        } catch (Exception e) {
            throw new DataProcessingException(
                    "Can't find available sessions for movie with id " + movieId, e);
        }
    }
}
