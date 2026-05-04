package com.alejandrofernandez.ecoadmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import com.alejandrofernandez.ecoadmin.servicios.TarifaValidator;

class TarifaValidatorTest {

    private final TarifaValidator validator = new TarifaValidator();

    // --- Formulas validas ---

    @Test
    void formulaSimpleConW() {
        var r = validator.validar("w * 22");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaSimpleConL() {
        var r = validator.validar("L * 0.1");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaCombinadaWyL() {
        var r = validator.validar("w * 0.5 + L * 0.1");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaConParentesis() {
        var r = validator.validar("(w + L) * 2.5");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaMinusUnarioInicio() {
        var r = validator.validar("-w + L");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaNumericaFija() {
        // Tarifa fija sin variables: debe ser valida
        var r = validator.validar("10");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaNumericaFijaCompleja() {
        // Tarifa fija como expresion numerica
        var r = validator.validar("2.5 * 4");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaMayusculaW() {
        // W mayuscula debe normalizarse a w y ser valida
        var r = validator.validar("W * 22");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaMinusculaL() {
        // l minuscula debe normalizarse a L y ser valida
        var r = validator.validar("w * 0.5 + l * 0.1");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void formulaMayusculasAmbas() {
        var r = validator.validar("W * 0.5 + l * 0.1");
        assertThat(r.valido()).isTrue();
    }

    @Test
    void calculoExacto() {
        double resultado = validator.calcular("w * 0.5 + L * 0.1", 10.0, 20.0);
        assertThat(resultado).isCloseTo(7.0, within(0.0001));
    }

    @Test
    void calculoFormulaNumerica() {
        double resultado = validator.calcular("10", 0.0, 0.0);
        assertThat(resultado).isCloseTo(10.0, within(0.0001));
    }

    // --- Formulas invalidas ---

    @Test
    void formulaVacia() {
        var r = validator.validar("   ");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("vacia");
    }

    @Test
    void formulaNull() {
        var r = validator.validar(null);
        assertThat(r.valido()).isFalse();
    }

    @Test
    void formulaConCaracteresIlegales() {
        var r = validator.validar("w * x + L");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("no permitidos");
    }

    @Test
    void formulaConInyeccionSQL() {
        var r = validator.validar("w; DROP TABLE usuarios;");
        assertThat(r.valido()).isFalse();
    }

    @Test
    void formulaConInyeccionScript() {
        var r = validator.validar("w + alert(1)");
        assertThat(r.valido()).isFalse();
    }

    @Test
    void formulaConDobleOperador() {
        var r = validator.validar("w */ L");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("consecutivos");
    }

    @Test
    void formulaConDobleVariable() {
        var r = validator.validar("wL + 1");
        assertThat(r.valido()).isFalse();
    }

    @Test
    void formulaConParentesisDesbalanceados() {
        var r = validator.validar("(w + L");
        assertThat(r.valido()).isFalse();
    }

    @Test
    void formulaDivisionPorCero() {
        var r = validator.validar("w / 0");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("finito");
    }
}