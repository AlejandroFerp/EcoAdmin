package com.iesdoctorbalmis.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import com.iesdoctorbalmis.spring.servicios.TarifaValidator;

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
        // Menos unario al inicio
        var r = validator.validar("-w + L");
        // exp4j soporta menos unario: debe ser valido
        assertThat(r.valido()).isTrue();
    }

    @Test
    void calculoExacto() {
        double resultado = validator.calcular("w * 0.5 + L * 0.1", 10.0, 20.0);
        assertThat(resultado).isCloseTo(7.0, within(0.0001));
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
    void formulaSinVariables() {
        var r = validator.validar("2 + 3");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("variable");
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
        // w / 0 produce infinito -- debe rechazarse
        var r = validator.validar("w / 0");
        assertThat(r.valido()).isFalse();
        assertThat(r.mensaje()).contains("finito");
    }
}
