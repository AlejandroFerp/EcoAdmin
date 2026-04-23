package com.iesdoctorbalmis.spring.servicios;

import java.util.regex.Pattern;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

/** Valida formulas de tarifa. Variables: w (peso kg), L (distancia km). */
@Component
public class TarifaValidator {

    private static final Pattern WHITELIST = Pattern.compile("^[0-9wL\\s+\\-*/().]+$");
    private static final Pattern DOBLE_OP = Pattern.compile("[+*/][+\\-*/]");
    private static final Pattern DOBLE_VAL = Pattern.compile("[wL][wL0-9]|[0-9][wL]");

    public record ResultadoValidacion(boolean valido, String mensaje) {}

    public ResultadoValidacion validar(String formula) {
        if (formula == null || formula.isBlank())
            return new ResultadoValidacion(false, "La formula no puede estar vacia.");
        String f = formula.trim();
        if (!WHITELIST.matcher(f).matches())
            return new ResultadoValidacion(false, "Caracteres no permitidos. Solo: digitos, w, L, +, -, *, /, (, ), punto.");
        if (!f.contains("w") && !f.contains("L"))
            return new ResultadoValidacion(false, "La formula debe contener al menos una variable (w o L).");
        if (DOBLE_OP.matcher(f).find())
            return new ResultadoValidacion(false, "Operadores consecutivos no permitidos (p.ej. *+, /-).");
        if (DOBLE_VAL.matcher(f).find())
            return new ResultadoValidacion(false, "Dos valores seguidos sin operador (p.ej. wL, 2w).");
        try {
            double r = new ExpressionBuilder(f).variables("w","L").build()
                .setVariable("w",1.0).setVariable("L",1.0).evaluate();
            if (!Double.isFinite(r))
                return new ResultadoValidacion(false, "La formula produce un resultado no finito.");
        } catch (ArithmeticException e) {
            return new ResultadoValidacion(false, "La formula produce un resultado no finito (division por cero).");
        } catch (Exception e) {
            return new ResultadoValidacion(false, "Formula invalida: " + e.getMessage());
        }
        return new ResultadoValidacion(true, "OK");
    }

    public double calcular(String formula, double w, double L) {
        return new ExpressionBuilder(formula).variables("w","L").build()
            .setVariable("w", w).setVariable("L", L).evaluate();
    }
}
