CREATE TABLE public.producto (
    id_producto INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL,

    CONSTRAINT chk_producto_codigo CHECK (
        CHAR_LENGTH(TRIM(codigo)) BETWEEN 1 AND 10
        AND codigo = UPPER(TRIM(codigo))
    ),

    CONSTRAINT chk_producto_nombre CHECK (
        CHAR_LENGTH(TRIM(nombre)) BETWEEN 3 AND 80
    ),

    CONSTRAINT chk_producto_categoria CHECK (
        categoria IN ('Papelería', 'Tecnología', 'Accesorios')
    ),

    CONSTRAINT chk_producto_precio CHECK (
        precio > 0
    ),

    CONSTRAINT chk_producto_stock CHECK (
        stock >= 0
    )
);
