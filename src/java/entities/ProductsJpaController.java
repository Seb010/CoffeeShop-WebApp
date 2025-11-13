/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import entities.exceptions.IllegalOrphanException;
import entities.exceptions.NonexistentEntityException;
import entities.exceptions.PreexistingEntityException;
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
public class ProductsJpaController implements Serializable {

    public ProductsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Products products) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (products.getOrderItemsCollection() == null) {
            products.setOrderItemsCollection(new ArrayList<OrderItems>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<OrderItems> attachedOrderItemsCollection = new ArrayList<OrderItems>();
            for (OrderItems orderItemsCollectionOrderItemsToAttach : products.getOrderItemsCollection()) {
                orderItemsCollectionOrderItemsToAttach = em.getReference(orderItemsCollectionOrderItemsToAttach.getClass(), orderItemsCollectionOrderItemsToAttach.getId());
                attachedOrderItemsCollection.add(orderItemsCollectionOrderItemsToAttach);
            }
            products.setOrderItemsCollection(attachedOrderItemsCollection);
            em.persist(products);
            for (OrderItems orderItemsCollectionOrderItems : products.getOrderItemsCollection()) {
                Products oldProductIdOfOrderItemsCollectionOrderItems = orderItemsCollectionOrderItems.getProductId();
                orderItemsCollectionOrderItems.setProductId(products);
                orderItemsCollectionOrderItems = em.merge(orderItemsCollectionOrderItems);
                if (oldProductIdOfOrderItemsCollectionOrderItems != null) {
                    oldProductIdOfOrderItemsCollectionOrderItems.getOrderItemsCollection().remove(orderItemsCollectionOrderItems);
                    oldProductIdOfOrderItemsCollectionOrderItems = em.merge(oldProductIdOfOrderItemsCollectionOrderItems);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findProducts(products.getId()) != null) {
                throw new PreexistingEntityException("Products " + products + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Products products) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Products persistentProducts = em.find(Products.class, products.getId());
            Collection<OrderItems> orderItemsCollectionOld = persistentProducts.getOrderItemsCollection();
            Collection<OrderItems> orderItemsCollectionNew = products.getOrderItemsCollection();
            List<String> illegalOrphanMessages = null;
            for (OrderItems orderItemsCollectionOldOrderItems : orderItemsCollectionOld) {
                if (!orderItemsCollectionNew.contains(orderItemsCollectionOldOrderItems)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain OrderItems " + orderItemsCollectionOldOrderItems + " since its productId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<OrderItems> attachedOrderItemsCollectionNew = new ArrayList<OrderItems>();
            for (OrderItems orderItemsCollectionNewOrderItemsToAttach : orderItemsCollectionNew) {
                orderItemsCollectionNewOrderItemsToAttach = em.getReference(orderItemsCollectionNewOrderItemsToAttach.getClass(), orderItemsCollectionNewOrderItemsToAttach.getId());
                attachedOrderItemsCollectionNew.add(orderItemsCollectionNewOrderItemsToAttach);
            }
            orderItemsCollectionNew = attachedOrderItemsCollectionNew;
            products.setOrderItemsCollection(orderItemsCollectionNew);
            products = em.merge(products);
            for (OrderItems orderItemsCollectionNewOrderItems : orderItemsCollectionNew) {
                if (!orderItemsCollectionOld.contains(orderItemsCollectionNewOrderItems)) {
                    Products oldProductIdOfOrderItemsCollectionNewOrderItems = orderItemsCollectionNewOrderItems.getProductId();
                    orderItemsCollectionNewOrderItems.setProductId(products);
                    orderItemsCollectionNewOrderItems = em.merge(orderItemsCollectionNewOrderItems);
                    if (oldProductIdOfOrderItemsCollectionNewOrderItems != null && !oldProductIdOfOrderItemsCollectionNewOrderItems.equals(products)) {
                        oldProductIdOfOrderItemsCollectionNewOrderItems.getOrderItemsCollection().remove(orderItemsCollectionNewOrderItems);
                        oldProductIdOfOrderItemsCollectionNewOrderItems = em.merge(oldProductIdOfOrderItemsCollectionNewOrderItems);
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
                Integer id = products.getId();
                if (findProducts(id) == null) {
                    throw new NonexistentEntityException("The products with id " + id + " no longer exists.");
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
            Products products;
            try {
                products = em.getReference(Products.class, id);
                products.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The products with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<OrderItems> orderItemsCollectionOrphanCheck = products.getOrderItemsCollection();
            for (OrderItems orderItemsCollectionOrphanCheckOrderItems : orderItemsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Products (" + products + ") cannot be destroyed since the OrderItems " + orderItemsCollectionOrphanCheckOrderItems + " in its orderItemsCollection field has a non-nullable productId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(products);
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

    public List<Products> findProductsEntities() {
        return findProductsEntities(true, -1, -1);
    }

    public List<Products> findProductsEntities(int maxResults, int firstResult) {
        return findProductsEntities(false, maxResults, firstResult);
    }

    private List<Products> findProductsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Products.class));
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

    public Products findProducts(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Products.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Products> rt = cq.from(Products.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
