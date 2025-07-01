package cl.ucn.modelo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoyaltyDiscountEngineTest {

    private EntityManager em;
    private EntityTransaction tx;
    private LoyaltyDiscountEngine engine;

    @Before
    public void setUp() {
        em = mock(EntityManager.class);
        tx = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(tx);
        engine = new LoyaltyDiscountEngine(em);
    }

    @Test
    public void testDescuentoNulo() {
        Customer c = new Customer("1", LocalDate.now(), 0, Customer.LoyaltyLevel.BASIC, false);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.0, descuento, 0.0001);
    }

    @Test
    public void testClienteSilver() {
        Customer c = new Customer("2", LocalDate.now().minusYears(3), 50, Customer.LoyaltyLevel.SILVER, false);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.05, descuento, 0.0001);
    }

    @Test
    public void testAntiguedadMayorA5Anios() {
        Customer c = new Customer("3", LocalDate.now().minusYears(6), 10, Customer.LoyaltyLevel.GOLD, false);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.15, descuento, 0.0001);
    }

    @Test
    public void testMasDe100Ordenes() {
        Customer c = new Customer("4", LocalDate.now().minusYears(2), 101, Customer.LoyaltyLevel.GOLD, false);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.15, descuento, 0.0001);
    }

    @Test
    public void testPromocionActiva() {
        Customer c = new Customer("5", LocalDate.now(), 0, Customer.LoyaltyLevel.BASIC, true);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.10, descuento, 0.0001);
    }

    @Test
    public void testDescuentoMaximo() {
        Customer c = new Customer("6", LocalDate.now().minusYears(10), 150, Customer.LoyaltyLevel.PLATINUM, true);
        double descuento = engine.computeDiscount(c);
        assertEquals(0.30, descuento, 0.0001);
    }

    @Test
    public void testBusquedaPorId() {
        Customer c = new Customer("8", LocalDate.now(), 0, Customer.LoyaltyLevel.BASIC, false);
        when(em.find(Customer.class, "8")).thenReturn(c);
        double descuento = engine.computeDiscountById("8");
        assertEquals(0.0, descuento, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExcepcionPorNulo() {
        engine.computeDiscount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExcepcionPorIdFaltante() {
        Customer c = new Customer(null, LocalDate.now(), 0, Customer.LoyaltyLevel.BASIC, false);
        engine.computeDiscount(c);
    }
}
