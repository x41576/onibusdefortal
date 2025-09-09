package com.x415;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Value;

@Serdeable
@Value
public final class Linha {

    private final String idLinha;
    private final Long id;
    private final String name;
    private final List<Trajeto> trajetos;
    private final List<Empresa> empresas;
    private final String shortName;
    private final String numero;
    private final Object busServiceClass;

    @Serdeable
    @Value
    public static final class Trajeto {

        private final String _id;
        private final Long id_migracao;
        private final String nome;
        private final String id;
        private final StartPoint startPoint;
        private final EndPoint endPoint;

        @Serdeable
        @Value
        public static final class StartPoint {

            private final String _id;
            private final Long id_migracao;
        }

        @Serdeable
        @Value
        public static final class EndPoint {

            private final String _id;
            private final Long id_migracao;
        }
    }

    @Serdeable
    @Value
    public static final class Empresa {

        private final Long id;
        private final String nome;
    }
}
