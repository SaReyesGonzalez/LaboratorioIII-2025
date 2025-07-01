package cl.ucn.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de lógica de negocios que permite calcular descuentos para clientes
 * según su nivel de lealtad, antigüedad, número de órdenes y si poseen
 * promociones activas.
 * Además, registra los descuentos aplicados en un log persistente.
 */
public class LoyaltyDiscountEngine {

    /**
     * EntityManager usado para acceder y modificar datos persistentes.
     */
    private final EntityManager em;

    /**
     * Crea una nueva instancia del motor de descuentos.
     *
     * @param em EntityManager para operaciones con la base de datos.
     */
    public LoyaltyDiscountEngine(EntityManager em) {
        this.em = em;
    }

    /**
     * Calcula el descuento para un cliente dado, en función de su nivel de lealtad,
     * antigüedad, número total de órdenes y si tiene una promoción activa.
     * También registra el descuento aplicado en los logs.
     *
     * @param customer cliente sobre el cual se aplicará el cálculo.
     * @return porcentaje de descuento como número entre 0.0 y 1.0.
     * @throws IllegalArgumentException si el cliente es nulo o sus datos son
     *                                  inválidos.
     */
    public double computeDiscount(Customer customer) {
        if (customer == null || customer.getId() == null || customer.getJoinDate() == null) {
            throw new IllegalArgumentException("Invalid customer");
        }

        long years = ChronoUnit.YEARS.between(customer.getJoinDate(), LocalDate.now());

        double base = switch (customer.getLevel()) {
            case BASIC -> 0;
            case SILVER -> 0.05;
            case GOLD -> 0.10;
            case PLATINUM -> 0.15;
            default -> 0;
        };

        if (years > 5)
            base += 0.05;
        if (customer.getTotalOrders() > 100)
            base += 0.05;
        if (customer.isHasActivePromo())
            base += 0.10;

        // Tope máximo del 30%
        if (base > 0.30)
            base = 0.30;

        logDiscount(customer.getId(), base);
        return base;
    }

    /**
     * Calcula el descuento para un cliente dado su identificador,
     * recuperando primero el cliente desde la base de datos.
     *
     * @param customerId identificador del cliente.
     * @return porcentaje de descuento aplicado.
     */
    public double computeDiscountById(String customerId) {
        Customer customer = em.find(Customer.class, customerId);
        return computeDiscount(customer);
    }

    /**
     * Registra en la base de datos un log con el descuento aplicado a un cliente.
     *
     * @param customerId ID del cliente.
     * @param discount   descuento aplicado.
     */
    private void logDiscount(String customerId, double discount) {
        em.getTransaction().begin();
        em.persist(new DiscountLog(customerId, discount));
        em.getTransaction().commit();
    }

    /**
     * Cuenta cuántos registros de descuento existen para un cliente específico.
     *
     * @param customerId ID del cliente.
     * @return número de logs asociados al cliente.
     */
    public long countLogsForCustomer(String customerId) {
        return em.createQuery(
                "SELECT COUNT(l) FROM DiscountLog l WHERE l.customerId = :cid", Long.class)
                .setParameter("cid", customerId)
                .getSingleResult();
    }
}
