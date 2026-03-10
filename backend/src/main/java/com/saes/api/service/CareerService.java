package com.saes.api.service;

import com.saes.api.dto.CareerDTO;
import com.saes.api.model.Career;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class CareerService {

    @PersistenceContext(unitName = "SAESPU")
    private EntityManager em;

    public CareerDTO create(CareerDTO payload) {
        Career career = payload.toEntity();
        career.setActiva(true);
        em.persist(career);
        em.flush();
        return new CareerDTO(career);
    }

    public List<CareerDTO> list(Boolean activa) {
        String jpql = "SELECT c FROM Career c";
        TypedQuery<Career> query;
        if (activa == null) {
            query = em.createQuery(jpql, Career.class);
        } else {
            query = em.createQuery(jpql + " WHERE c.activa = :activa", Career.class);
            query.setParameter("activa", activa);
        }
        return query.getResultStream().map(CareerDTO::new).collect(Collectors.toList());
    }

    public CareerDTO findById(Long id) {
        Career career = em.find(Career.class, id);
        if (career == null) {
            return null;
        }
        return new CareerDTO(career);
    }

    public void delete(Long id) {
        Career career = em.find(Career.class, id);
        if (career != null) {
            em.remove(career);
        }
    }
}
