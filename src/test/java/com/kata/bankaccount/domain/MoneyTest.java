package com.kata.bankaccount.domain;

import com.kata.bankaccount.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void scales_amount_to_two_decimal_places() {
        Money money = Money.of("10.555", "EUR");
        assertThat(money.amount()).isEqualByComparingTo("10.56");
    }

    @Test
    void rejects_negative_amount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Money.of(new BigDecimal("-0.01"), EUR));
    }

    @Test
    void allows_zero_amount() {
        Money zero = Money.of(BigDecimal.ZERO, EUR);
        assertThat(zero.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void add_returns_sum_with_same_currency() {
        Money a = Money.of("100.00", "EUR");
        Money b = Money.of("50.00", "EUR");
        assertThat(a.add(b).amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void subtract_returns_difference() {
        Money a = Money.of("100.00", "EUR");
        Money b = Money.of("40.00", "EUR");
        assertThat(a.subtract(b).amount()).isEqualByComparingTo("60.00");
    }

    @Test
    void subtract_to_zero_is_valid() {
        Money a = Money.of("100.00", "EUR");
        assertThat(a.subtract(a).amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void subtract_below_zero_throws() {
        Money a = Money.of("10.00", "EUR");
        Money b = Money.of("20.00", "EUR");
        assertThatIllegalArgumentException().isThrownBy(() -> a.subtract(b));
    }

    @Test
    void add_with_different_currencies_throws() {
        Money eur = Money.of("10.00", "EUR");
        Money usd = Money.of("10.00", "USD");
        assertThatIllegalArgumentException().isThrownBy(() -> eur.add(usd));
    }

    @Test
    void isGreaterThanOrEqual_equal_amounts_returns_true() {
        Money a = Money.of("50.00", "EUR");
        Money b = Money.of("50.00", "EUR");
        assertThat(a.isGreaterThanOrEqual(b)).isTrue();
    }

    @Test
    void isGreaterThanOrEqual_larger_than_other_returns_true() {
        Money a = Money.of("100.00", "EUR");
        Money b = Money.of("50.00", "EUR");
        assertThat(a.isGreaterThanOrEqual(b)).isTrue();
    }

    @Test
    void isGreaterThanOrEqual_smaller_than_other_returns_false() {
        Money a = Money.of("10.00", "EUR");
        Money b = Money.of("50.00", "EUR");
        assertThat(a.isGreaterThanOrEqual(b)).isFalse();
    }

    @Test
    void structural_equality_holds() {
        Money a = Money.of("10.00", "EUR");
        Money b = Money.of("10.00", "EUR");
        assertThat(a).isEqualTo(b);
    }
}
