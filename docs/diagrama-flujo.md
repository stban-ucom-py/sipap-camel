# Diagrama del flujo SIPAP educativo

```mermaid
flowchart TB
    subgraph Productores
        T1["timer:itauProducer"]
        T2["timer:atlasProducer"]
        T3["timer:familiarProducer"]
    end

    T1 --> ENTRY["direct:sipap-in\nCorrelation Identifier"]
    T2 --> ENTRY
    T3 --> ENTRY
    ENTRY -. "Wire Tap" .-> AUDIT["direct:audit"]

    subgraph "Pipes and Filters"
        ENTRY --> PARSER["direct:parse\nParser TLV"]
        PARSER --> TRANSLATOR["Message Translator\nTransferencia JSON"]
        TRANSLATOR --> VALIDATOR["direct:validate\nReglas funcionales"]
        VALIDATOR --> FILTER{"Message Filter"}
    end

    PARSER -. "Excepción" .-> DEAD["direct:dead-letter\nRECHAZADA"]
    FILTER -- "inválida" --> REJECT["direct:rejected\nRECHAZADA"]
    FILTER -- "válida" --> ROUTER{"Content-Based Router\nchoice(codigo_entidad)"}
    ROUTER -- "0015" --> ITAU["direct:itau"]
    ROUTER -- "0007" --> ATLAS["direct:atlas"]
    ROUTER -- "0020" --> FAMILIAR["direct:familiar"]
    ITAU --> RESULT["ResultadoTransferencia\nPROCESADA"]
    ATLAS --> RESULT
    FAMILIAR --> RESULT
```

Todos los canales internos son endpoints `direct:` del mismo `CamelContext`. La auditoría obtiene
una copia mediante Wire Tap y no modifica el intercambio principal.
