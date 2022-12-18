package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Date {
    @Schema(minimum = "1", maximum = "31", example = "12")
    private Integer dia;
    @Schema(minimum = "1", maximum = "12", example = "4")
    private Integer mes;
    @Schema(minimum = "1900", example = "1984")
    private Integer ano;

    public Date() {
    }

    public Date(Integer dia, Integer mes, Integer ano) {
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
    }

    public Integer getDia() {
        return dia;
    }

    public Date setDia(Integer dia) {
        this.dia = dia;
        return this;
    }

    public Integer getMes() {
        return mes;
    }

    public Date setMes(Integer mes) {
        this.mes = mes;
        return this;
    }

    public Integer getAno() {
        return ano;
    }

    public Date setAno(Integer ano) {
        this.ano = ano;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Date date = (Date) o;
        return Objects.equals(dia, date.dia) && Objects.equals(mes, date.mes) && Objects.equals(ano, date.ano);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dia, mes, ano);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Date.class.getSimpleName() + "[", "]")
                .add("day=" + dia)
                .add("month=" + mes)
                .add("year=" + ano)
                .toString();
    }
}
