/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import entities.exceptions.IllegalOrphanException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author seb
 */
public class OrdersJpaController implements Serializable {

    public OrdersJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Orders orders) throws RollbackFailureException, Exception {
        if (orders.getOrderItemsCollection() == null) {
            orders.setOrderItemsCollection(new ArrayList<OrderItems>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Users userId = orders.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                orders.setUserId(userId);
            }
            Collection<OrderItems> attachedOrderItemsCollection = new ArrayList<OrderItems>();
            for (OrderItems orderItemsCollectionOrderItemsToAttach : orders.getOrderItemsCollection()) {
                orderItemsCollectionOrderItemsToAttach = em.getReference(orderItemsCollectionOrderItemsToAttach.getClass(), orderItemsCollectionOrderItemsToAttach.getId());
                attachedOrderItemsCollection.add(orderItemsCollectionOrderItemsToAttach);
            }
            orders.setOrderItemsCollection(attachedOrderItemsCollection);
            em.persist(orders);
            if (userId != null) {
                userId.getOrdersCollection().add(orders);
                userId = em.merge(userId);
            }
            for (OrderItems orderItemsCollectionOrderItems : orders.getOrderItemsCollection()) {
                Orders oldOrderIdOfOrderItemsCollectionOrderItems = orderItemsCollectionOrderItems.getOrderId();
                orderItemsCollectionOrderItems.setOrderId(orders);
                orderItemsCollectionOrderItems = em.merge(orderItemsCollectionOrderItems);
                if (oldOrderIdOfOrderItemsCollectionOrderItems != null) {
                    oldOrderIdOfOrderItemsCollectionOrderItems.getOrderItemsCollection().remove(orderItemsCollectionOrderItems);
                    oldOrderIdOfOrderItemsCollectionOrderItems = em.merge(oldOrderIdOfOrderItemsCollectionOrderItems);
                }
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

    public void edit(Orders orders) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Orders persistentOrders = em.find(Orders.class, orders.getId());
            Users userIdOld = persistentOrders.getUserId();
            Users userIdNew = orders.getUserId();
            Collection<OrderItems> orderItemsCollectionOld = persistentOrders.getOrderItemsCollection();
            Collection<OrderItems> orderItemsCollectionNew = orders.getOrderItemsCollection();
            List<String> illegalOrphanMessages = null;
            for (OrderItems orderItemsCollectionOldOrderItems : orderItemsCollectionOld) {
                if (!orderItemsCollectionNew.contains(orderItemsCollectionOldOrderItems)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain OrderItems " + orderItemsCollectionOldOrderItems + " since its orderId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                orders.setUserId(userIdNew);
            }
            Collection<OrderItems> attachedOrderItemsCollectionNew = new ArrayList<OrderItems>();
            for (OrderItems orderItemsCollectionNewOrderItemsToAttach : orderItemsCollectionNew) {
                orderItemsCollectionNewOrderItemsToAttach = em.getReference(orderItemsCollectionNewOrderItemsToAttach.getClass(), orderItemsCollectionNewOrderItemsToAttach.getId());
                attachedOrderItemsCollectionNew.add(orderItemsCollectionNewOrderItemsToAttach);
            }
            orderItemsCollectionNew = attachedOrderItemsCollectionNew;
            orders.setOrderItemsCollection(orderItemsCollectionNew);
            orders = em.merge(orders);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getOrdersCollection().remove(orders);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getOrdersCollection().add(orders);
                userIdNew = em.merge(userIdNew);
            }
            for (OrderItems orderItemsCollectionNewOrderItems : orderItemsCollectionNew) {
                if (!orderItemsCollectionOld.contains(orderItemsCollectionNewOrderItems)) {
                    Orders oldOrderIdOfOrderItemsCollectionNewOrderItems = orderItemsCollectionNewOrderItems.getOrderId();
                    orderItemsCollectionNewOrderItems.setOrderId(orders);
                    orderItemsCollectionNewOrderItems = em.merge(orderItemsCollectionNewOrderItems);
                    if (oldOrderIdOfOrderItemsCollectionNewOrderItems != null && !oldOrderIdOfOrderItemsCollectionNewOrderItems.equals(orders)) {
                        oldOrderIdOfOrderItemsCollectionNewOrderItems.getOrderItemsCollection().remove(orderItemsCollectionNewOrderItems);
                        oldOrderIdOfOrderItemsCollectionNewOrderItems = em.merge(oldOrderIdOfOrderItemsCollectionNewOrderItems);
                    }
                }
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
                Integer id = orders.getId();
                if (findOrders(id) == null) {
                    throw new NonexistentEntityException("The orders with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Orders orders;
            try {
                orders = em.getReference(Orders.class, id);
                orders.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orders with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<OrderItems> orderItemsCollectionOrphanCheck = orders.getOrderItemsCollection();
            for (OrderItems orderItemsCollectionOrphanCheckOrderItems : orderItemsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Orders (" + orders + ") cannot be destroyed since the OrderItems " + orderItemsCollectionOrphanCheckOrderItems + " in its orderItemsCollection field has a non-nullable orderId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Users userId = orders.getUserId();
            if (userId != null) {
                userId.getOrdersCollection().remove(orders);
                userId = em.merge(userId);
            }
            em.remove(orders);
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

    public List<Orders> findOrdersEntities() {
        return findOrdersEntities(true, -1, -1);
    }

    public List<Orders> findOrdersEntities(int maxResults, int firstResult) {
        return findOrdersEntities(false, maxResults, firstResult);
    }

    private List<Orders> findOrdersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Orders.class));
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

    public Orders findOrders(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Orders.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrdersCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Orders> rt = cq.from(Orders.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
