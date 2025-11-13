/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import entities.exceptions.NonexistentEntityException;
import entities.exceptions.RollbackFailureException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;
import java.util.List;

/**
 *
 * @author seb
 */
public class OrderItemsJpaController implements Serializable {

    public OrderItemsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(OrderItems orderItems) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Orders orderId = orderItems.getOrderId();
            if (orderId != null) {
                orderId = em.getReference(orderId.getClass(), orderId.getId());
                orderItems.setOrderId(orderId);
            }
            Products productId = orderItems.getProductId();
            if (productId != null) {
                productId = em.getReference(productId.getClass(), productId.getId());
                orderItems.setProductId(productId);
            }
            em.persist(orderItems);
            if (orderId != null) {
                orderId.getOrderItemsCollection().add(orderItems);
                orderId = em.merge(orderId);
            }
            if (productId != null) {
                productId.getOrderItemsCollection().add(orderItems);
                productId = em.merge(productId);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(OrderItems orderItems) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            OrderItems persistentOrderItems = em.find(OrderItems.class, orderItems.getId());
            Orders orderIdOld = persistentOrderItems.getOrderId();
            Orders orderIdNew = orderItems.getOrderId();
            Products productIdOld = persistentOrderItems.getProductId();
            Products productIdNew = orderItems.getProductId();
            if (orderIdNew != null) {
                orderIdNew = em.getReference(orderIdNew.getClass(), orderIdNew.getId());
                orderItems.setOrderId(orderIdNew);
            }
            if (productIdNew != null) {
                productIdNew = em.getReference(productIdNew.getClass(), productIdNew.getId());
                orderItems.setProductId(productIdNew);
            }
            orderItems = em.merge(orderItems);
            if (orderIdOld != null && !orderIdOld.equals(orderIdNew)) {
                orderIdOld.getOrderItemsCollection().remove(orderItems);
                orderIdOld = em.merge(orderIdOld);
            }
            if (orderIdNew != null && !orderIdNew.equals(orderIdOld)) {
                orderIdNew.getOrderItemsCollection().add(orderItems);
                orderIdNew = em.merge(orderIdNew);
            }
            if (productIdOld != null && !productIdOld.equals(productIdNew)) {
                productIdOld.getOrderItemsCollection().remove(orderItems);
                productIdOld = em.merge(productIdOld);
            }
            if (productIdNew != null && !productIdNew.equals(productIdOld)) {
                productIdNew.getOrderItemsCollection().add(orderItems);
                productIdNew = em.merge(productIdNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = orderItems.getId();
                if (findOrderItems(id) == null) {
                    throw new NonexistentEntityException("The orderItems with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            OrderItems orderItems;
            try {
                orderItems = em.getReference(OrderItems.class, id);
                orderItems.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orderItems with id " + id + " no longer exists.", enfe);
            }
            Orders orderId = orderItems.getOrderId();
            if (orderId != null) {
                orderId.getOrderItemsCollection().remove(orderItems);
                orderId = em.merge(orderId);
            }
            Products productId = orderItems.getProductId();
            if (productId != null) {
                productId.getOrderItemsCollection().remove(orderItems);
                productId = em.merge(productId);
            }
            em.remove(orderItems);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<OrderItems> findOrderItemsEntities() {
        return findOrderItemsEntities(true, -1, -1);
    }

    public List<OrderItems> findOrderItemsEntities(int maxResults, int firstResult) {
        return findOrderItemsEntities(false, maxResults, firstResult);
    }

    private List<OrderItems> findOrderItemsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(OrderItems.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public OrderItems findOrderItems(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(OrderItems.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrderItemsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<OrderItems> rt = cq.from(OrderItems.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
