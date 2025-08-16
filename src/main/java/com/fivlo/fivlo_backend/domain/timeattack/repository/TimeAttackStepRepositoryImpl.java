package com.fivlo.fivlo_backend.domain.timeattack.repository;

import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackStep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

@Repository
public class TimeAttackStepRepositoryImpl implements TimeAttackStepRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public int sumDurationBySessionId(Long sessionId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<TimeAttackStep> root = cq.from(TimeAttackStep.class);

        // select coalesce(sum(durationInSeconds), 0)
        cq.select(cb.coalesce(cb.sum(root.get("durationInSeconds")), 0));
        cq.where(cb.equal(root.get("timeAttackSession").get("id"), sessionId));

        Integer result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0;
    }
}
